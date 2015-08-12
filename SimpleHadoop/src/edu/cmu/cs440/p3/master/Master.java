package edu.cmu.cs440.p3.master;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.cs440.p3.Interface.Mapper;
import edu.cmu.cs440.p3.Interface.Reducer;
import edu.cmu.cs440.p3.communication.Message;
import edu.cmu.cs440.p3.configuration.Config;
import edu.cmu.cs440.p3.io.InputSplit;
import edu.cmu.cs440.p3.task.MapTask;
import edu.cmu.cs440.p3.task.ReduceTask;
import edu.cmu.cs440.p3.task.Task;

/**
 * coordinates and monitors tasks
 */
public class Master {

	private static Config config;
	private int maxMaps;
	private int maxReduces;
	private boolean isAlive;
	private static volatile Map<String, List<Task>> workers;
	private static volatile Map<Long, List<Map<Integer, String>>> taskResultCollector;
	private StatusChecker checker;

	public Master(Config config) {
		Master.config = config;
		workers = new ConcurrentHashMap<String, List<Task>>();
		taskResultCollector = new ConcurrentHashMap<Long, List<Map<Integer, String>>>();
		maxMaps = config.getMaxMaps();
		maxReduces = config.getMaxReduces();
		isAlive = false;
		checker = new StatusChecker(workers, config);
	}

	/**
	 * Process the exception
	 */
	public void exception(Message message) throws SocketTimeoutException,
			UnknownHostException, IOException, ClassNotFoundException {

		/* remove the tasks from the job tracking table */
		for (String c : workers.keySet()) {
			int i = 0;
			while (i < workers.get(c).size()) {
				if (workers.get(c).get(i).getTaskID() == message.getTask()
						.getTaskID()) {
					workers.get(c).remove(i);
				} else {
					i++;
				}
			}
		}

		/* print out or send back the exception message to the task issuer */
		if (message.getTask().getIssuer().equals("Master")) {
			System.out.println("Task " + message.getTask().getTaskID()
					+ " can't be finished.");
		} else {
			Message msg = new Message(Message.TYPE.RESULT);
			msg.setMsg("Task " + message.getTask().getTaskID()
					+ " can't be finished.");
			String issuer = message.getTask().getIssuer();
			Message.sendRequest(config.getClientAddr(issuer),
					config.getClientPort(issuer), 5000, msg);
		}
	}

	/**
	 * finish reduce task
	 */
	public void reducer(Message message) {

		String workerID = message.getTask().getWorkerID();
		boolean lastOne = removeTask(workerID, message.getTask().getTaskID());

		// wait for all reduce task to finish
		if (lastOne) {
			try {
				String issuer = message.getTask().getIssuer();
				if (issuer.equals("Master"))
					System.out.println("Task from master finished");
				else {
					Message msg = new Message(Message.TYPE.RESULT);
					msg.setMsg("Task " + message.getTask().getTaskID()
							+ " is finished.");
					Message.sendRequest(config.getClientAddr(issuer),
							config.getClientPort(issuer), 5000, msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * finish the map task
	 */
	public void mapper(Message message) throws InterruptedException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, Exception {
		boolean lastOne = removeTask(message.getTask().getWorkerID(), message
				.getTask().getTaskID());

		// failure happened
		if (!taskResultCollector.containsKey(message.getTask().getTaskID()))
			return;
		// collect the intermediate result
		List<Map<Integer, String>> taskResults = taskResultCollector
				.get(message.getTask().getTaskID());
		MapTask task = (MapTask) message.getTask();
		taskResults.add(task.getIntermediateResults());
		if (lastOne) {
			List<Thread> runningThreads = new ArrayList<Thread>();
			List<String> freeWorkers = getFreeWorkers(task
					.getIntermediateResults().size());
			long taskID = System.currentTimeMillis();
			Reducer reduce;
			try {
				@SuppressWarnings("unchecked")
				Class<Reducer> reduceClass = (Class<Reducer>) (Class
						.forName("edu.cmu.cs440.p3.example."
								+ task.getReducerClass()));
				Constructor<?> reduceConstructor = reduceClass.getConstructor();
				reduce = (Reducer) reduceConstructor.newInstance();
			} catch (ClassNotFoundException e1) {
				message.setMsg("Reduce Class cannot be found.");
				return;
			}
			for (int i = 0; i < freeWorkers.size(); i++) {

				// collect info for a reduce task
				String workerID = freeWorkers.get(i);
				ReduceTask reduceTask = new ReduceTask();
				reduceTask.setJob(reduce);
				reduceTask.setTaskID(taskID);
				reduceTask.setReducerClass(task.getReducerClass());
				reduceTask.setOutputFolder(task.getOutputFolder());
				reduceTask.setReducerID(i);
				reduceTask.setIssuer(task.getIssuer());
				reduceTask.setWorkerID(workerID);
				reduceTask.setInputFiles(new ArrayList<String>());

				for (int j = 0; j < taskResults.size(); j++) {
					if (taskResults.get(j).get(i) != null) {
						reduceTask.getInputFiles().add(
								taskResults.get(j).get(i));
					}
				}

				/* Send out the reduce tasks */
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Message msg = new Message(Message.TYPE.REDUCER);
							msg.setTask(reduceTask);
							Message.sendRequest(config.getClientAddr(workerID),
									config.getClientPort(workerID), 0, msg);
						} catch (Exception e) {
							Master.exceptionHandler(workerID);
						}
					}

				});
				runningThreads.add(t);
				workers.get(freeWorkers.get(i)).add(reduceTask);
				t.start();
			}

			// send out all reduce tasks
			taskResultCollector.remove(task.getTaskID());
			try {
				for (Thread t : runningThreads)
					t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runningThreads.clear();
		}
	}

	/**
	 * send map tasks to workers
	 */
	public void submit(Message message, Socket clientSock) {
		String[] args = message.getMsg().split(" ");
		String mapperClass = args[0];
		String inputFile = args[1];
		String reducerClass = args[2];
		String outputFolder = args[3];
		try {
			Mapper map;
			@SuppressWarnings("unchecked")
			Class<Mapper> mapClass = (Class<Mapper>) (Class
					.forName("edu.cmu.cs440.p3.example." + mapperClass));
			Constructor<?> mapConstructor = mapClass.getConstructor();
			map = (Mapper) mapConstructor.newInstance();
			if (map.getNumMapper() > Math.min(maxMaps, workers.size())
					|| map.getNumReducer() > Math.min(maxReduces,
							workers.size())) {
				message.setMsg("map-reduce maximum size limit");
				return;
			}

			// partition input file
			InputSplit splitter = new InputSplit(inputFile, map.getNumMapper());
			ArrayList<InputSplit> splits = splitter.getSplits();

			// collect all info for a map task
			List<Thread> runningThreads = new ArrayList<Thread>();
			List<String> freeWorker = getFreeWorkers(map.getNumMapper());
			long taskID = System.currentTimeMillis();
			taskResultCollector.put(taskID,
					new ArrayList<Map<Integer, String>>());
			for (int i = 0; i < freeWorker.size(); i++) {

				/* Prepare the payload and sent out the map tasks */
				String workerID = freeWorker.get(i);
				MapTask mapTask = new MapTask();
				mapTask.setTaskID(taskID);
				mapTask.setMapperClass(mapperClass);
				mapTask.setInputFile(inputFile);
				mapTask.setOutputFolder(outputFolder);
				mapTask.setReducerClass(reducerClass);
				mapTask.setMapperID(i);
				mapTask.setIssuer(message.getId());
				mapTask.setWorkerID(workerID);
				mapTask.setSplit(splits.get(i));
				mapTask.setJob(map);

				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Message msg = new Message(Message.TYPE.MAPPER);
							msg.setTask(mapTask);
							Message.sendRequest(config.getClientAddr(workerID),
									config.getClientPort(workerID), 0, msg);
						} catch (Exception e) {
							Master.exceptionHandler(workerID);
						}
					}

				});
				runningThreads.add(t);
				workers.get(freeWorker.get(i)).add(mapTask);
				t.start();
			}
			for (Thread t : runningThreads)
				t.join();
			runningThreads.clear();
			message.setMsg("Task " + taskID + " is running");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle any exceptions, restart tasks
	 */
	public static void exceptionHandler(String workerID) {

		// failure happens
		if (!workers.containsKey(workerID))
			return;
		List<Task> remainingTasks = workers.get(workerID);
		workers.remove(workerID);
		String replacement = null;
		try {

			// look for replacement
			for (Task p : remainingTasks) {
				List<String> allWorkers = new ArrayList<String>(
						workers.keySet());
				Collections.sort(allWorkers, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return ((Integer) workers.get(o1).size())
								.compareTo((Integer) workers.get(o2).size());
					}
				});
				for (String c : allWorkers) {
					if (!containsTask(p.getTaskID(), workers.get(c))) {
						replacement = c;
						break;
					}
				}

				// notice the user
				if (replacement == null) {
					System.out.println("fail to handle exception");
					taskResultCollector.remove(p.getTaskID());
					if (p.getIssuer().equals("Master")) {
						System.out.println("Task " + p.getTaskID()
								+ " is failed on " + p.getWorkerID());
					} else {
						Message msg = new Message(Message.TYPE.RESULT);
						msg.setMsg("Task " + p.getTaskID() + " is failed on "
								+ p.getWorkerID());
						Message.sendRequest(
								config.getClientAddr(p.getIssuer()),
								config.getClientPort(p.getIssuer()), 5000, msg);

					}
					return;
				}

				// if replacement found
				workers.get(replacement).add(p);
				Message msg = null;
				if (p instanceof MapTask) {
					((MapTask) p).setWorkerID(replacement);
					msg = new Message(Message.TYPE.MAPPER);
					msg.setTask(p);
				} else if (p instanceof ReduceTask) {
					((ReduceTask) p).setWorkerID(replacement);
					msg = new Message(Message.TYPE.REDUCER);
					msg.setTask(p);
				}
				Message.sendRequest(config.getClientAddr(replacement),
						config.getClientPort(replacement), 5000, msg);
			}
		} catch (Exception e) {
			exceptionHandler(replacement);
		}
	}

	/**
	 * Remove a task
	 */
	private synchronized boolean removeTask(String workerID, long taskID) {
		boolean lastOne = true;
		for (String c : workers.keySet()) {
			if (c.equals(workerID)) {
				int i = 0;
				while (i < workers.get(c).size()) {
					if (workers.get(c).get(i).getTaskID() == taskID)
						workers.get(c).remove(i);
					else
						i++;
				}
			} else {
				for (int i = 0; i < workers.get(c).size(); i++)
					lastOne = lastOne
							&& !(workers.get(c).get(i).getTaskID() == taskID);
			}
		}
		return lastOne;
	}

	/**
	 * get workers with least burden
	 */
	public List<String> getFreeWorkers(int n) {
		List<String> allWorkers = new ArrayList<String>(workers.keySet());
		Collections.sort(allWorkers, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return ((Integer) workers.get(o1).size())
						.compareTo((Integer) workers.get(o2).size());
			}
		});
		return allWorkers.subList(0, n);
	}

	/**
	 * check if contains the task
	 */
	public static boolean containsTask(long taskID, List<Task> pool) {
		for (Task t : pool) {
			if (t.getTaskID() == taskID)
				return true;
		}
		return false;
	}

	public void shutdown() throws IOException {
		isAlive = false;
	}

	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * add a new worker
	 */
	public void register(String worker) {
		workers.put(worker, new ArrayList<Task>());
	}

	/**
	 * start the facility
	 */
	public void start() {
		this.isAlive = true;
		new Thread(this.checker).start();
	}

	/**
	 * list all tasks
	 */
	public void monitor(Message msg) {
		if (workers.containsKey(msg.getMsg())) {
			msg.setMsg(workers.get(msg.getMsg()).size() + "");
		} else {
			msg.setMsg(0 + "");
		}
	}
}
