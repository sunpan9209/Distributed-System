package edu.cmu.cs.cs440.p1.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.cs.cs440.p1.worker.WorkerCommunicator;

/**
 * take control of all processes and workers.
 */
public class ProcessManager implements Runnable {

	private Map<Integer, ProcessStatus> pidToStatus = new ConcurrentHashMap<Integer, ProcessStatus>();
	private Map<Integer, Integer> pidToWorker = new ConcurrentHashMap<Integer, Integer>();
	private Map<Integer, WorkerCommunicator> workerToCommunicator = new ConcurrentHashMap<Integer, WorkerCommunicator>();
	private int pid = 0;
	public static final int MANAGER_PORT = 8006;
	private String helpMessage = "usage:\n" + "\thelp: print this message\n"
			+ "\tworker: print worker hosts list\n"
			+ "\tprocess: print process list on every worker host\n"
			+ "\tstart <workerID> <ProcessName> <arg1> <arg2> ....\n"
			+ "\tmigrate <ProcessID> <workerSRC> <wrokerDES>\n"
			+ "\tsuspend <ProcessID>\n" + "\tresume <ProcessID> <workerID>\n";

	public Map<Integer, ProcessStatus> getPidToStatus() {
		return pidToStatus;
	}

	public Map<Integer, Integer> getPidToWorker() {
		return pidToWorker;
	}

	public Map<Integer, WorkerCommunicator> getWorkerToCommunicator() {
		return workerToCommunicator;
	}

	@Override
	public void run() {
		ConnectionListener listener = new ConnectionListener(this);
		Thread newThread = new Thread(listener);
		newThread.start();

		// start the console
		System.out.println("Initializing Console");
		BufferedReader console = new BufferedReader(new InputStreamReader(
				System.in));
		String input;
		String[] args;

		// prompt the user to enter instructions
		while (true) {
			System.out.print("Manager->");
			try {
				input = console.readLine();
				args = input.split(" ");
				if (args[0].equals("quit")) {
					System.out.println("Closing all workers.");
					WorkerCommunicator communicator;
					Set<Integer> workers = workerToCommunicator.keySet();
					Iterator<Integer> workerIterator = workers.iterator();
					while (workerIterator.hasNext()) {
						communicator = workerToCommunicator.get(workerIterator
								.next());
						communicator.getOut().write(input + "\n");
						communicator.getOut().flush();
					}
					System.out.println("Exiting...");
					System.exit(0);
				} else if (args.length == 0) {
					continue;
				} else if (args[0].equals("help")) {
					System.out.println(helpMessage);
				}

				// list workers
				else if (args[0].equals("worker")) {
					System.out.println("Workers:");
					System.out.println(this.workerToCommunicator.keySet()
							.toString());
				}

				// list processes
				else if (args[0].equals("process")) {
					System.out.println("Running processes:");
					for (Map.Entry<Integer, ProcessStatus> e : pidToStatus
							.entrySet()) {
						if (e.getValue().equals(ProcessStatus.RUNNING)) {
							System.out.println(e.getKey());
						}
					}
					System.out.println();
					System.out.println("Suspended processes:");
					for (Map.Entry<Integer, ProcessStatus> e : pidToStatus
							.entrySet()) {
						if (e.getValue().equals(ProcessStatus.SUSPENDED)) {
							System.out.println(e.getKey());
						}
					}
				}

				// start a new process running on a worker
				else if (args[0].equals("start") && args.length > 1) {
					int hostID = -1;
					try {
						hostID = Integer.parseInt(args[1]);
					} catch (Exception e) {
						System.err.println("Wrong ID format");
						continue;
					}
					if (hostID > -1
							&& hostID < this.workerToCommunicator.size()) {

						// serialize new process
						MigratableProcess process;
						@SuppressWarnings("unchecked")
						Class<MigratableProcess> processClass = (Class<MigratableProcess>) (Class
								.forName("edu.cmu.cs.cs440.p1.example."
										+ args[2]));
						Constructor<?> processConstructor = processClass
								.getConstructor(String[].class);
						Object[] arguments = { Arrays.copyOfRange(args, 3,
								args.length) };
						process = (MigratableProcess) processConstructor
								.newInstance(arguments);
						ProcessSerialization.serialize(process, pid);
						pidToStatus.put(pid, ProcessStatus.SUSPENDED);
						System.out.println("pid:" + pid);
						System.out.println("workerID:" + hostID);

						// send the command
						WorkerCommunicator worker = this.workerToCommunicator
								.get(hostID);
						worker.getOut().write(
								"start " + Integer.toString(pid) + "\n");
						worker.getOut().flush();
						pidToStatus.put(pid, ProcessStatus.RUNNING);
						pidToWorker.put(pid, hostID);
						pid++;
					} else {
						System.err.println("Wrong worker ID");
					}
				}

				// suspend command
				else if (args[0].equals("suspend") && args.length > 1) {
					int processID = Integer.valueOf(args[1]);
					if (pidToStatus.get(processID) != null) {
						if (pidToStatus.get(processID).equals(
								ProcessStatus.RUNNING)) {
							WorkerCommunicator worker = workerToCommunicator
									.get(pidToWorker.get(processID));
							worker.getOut().write(
									"suspend " + Integer.toString(processID)
											+ "\n");
							worker.getOut().flush();
							pidToStatus.put(processID, ProcessStatus.SUSPENDED);
							this.pidToWorker.remove(processID);
						} else {
							System.out.println("The process is not running");
						}
					} else {
						System.out.println("Invalid process ID");
					}
				}

				// resume command
				else if (args[0].equals("resume") && args.length > 1) {
					int processID = Integer.valueOf(args[1]);
					int workerID = Integer.valueOf(args[2]);
					System.out.println("pid:" + processID);
					System.out.println("workerID:" + workerID);
					if (pidToStatus.get(processID) != null) {
						if (pidToStatus.get(processID).equals(
								ProcessStatus.SUSPENDED)) {
							if (workerToCommunicator.get(workerID) != null) {
								WorkerCommunicator worker = workerToCommunicator
										.get(workerID);
								worker.getOut().write(
										"start " + Integer.toString(processID)
												+ "\n");
								worker.getOut().flush();
								pidToStatus.put(processID,
										ProcessStatus.RUNNING);
								pidToWorker.put(processID, workerID);
							} else {
								System.out.println("no such worker");
							}
						} else {
							System.out.println("The process is not suspended");
						}
					} else {
						System.out.println("Invalid process ID");
					}
				}

				// migrate command
				else if (args[0].equals("migrate") && args.length > 1) {
					int processID = Integer.valueOf(args[1]);
					WorkerCommunicator workerSrc = null;
					WorkerCommunicator workerDes = null;
					try {
						workerSrc = this.workerToCommunicator.get(Integer
								.parseInt(args[2]));
						workerDes = this.workerToCommunicator.get(Integer
								.parseInt(args[3]));
					} catch (NumberFormatException e) {
						System.err.println("wrong worker ID format");
						continue;
					}
					if (workerSrc == null || workerDes == null) {
						System.err.println("wrong worker ID");
						continue;
					}
					if (pidToStatus.get(processID) != null) {
						// suspend and resume the process
						if (pidToStatus.get(processID).equals(
								ProcessStatus.RUNNING)) {
							workerSrc.getOut().write(
									"suspend " + Integer.toString(processID)
											+ "\n");
							workerSrc.getOut().flush();
							pidToStatus.put(processID, ProcessStatus.SUSPENDED);
							this.pidToWorker.remove(processID);
							Thread.sleep(5000);
							workerDes.getOut().write(
									"start " + Integer.toString(processID)
											+ "\n");
							workerDes.getOut().flush();
							pidToStatus.put(processID, ProcessStatus.RUNNING);
							pidToWorker.put(processID,
									Integer.parseInt(args[3]));
						} else if (pidToStatus.get(processID).equals(
								ProcessStatus.SUSPENDED)) {
							workerDes.getOut().write(
									"start " + Integer.toString(processID)
											+ "\n");
							workerDes.getOut().flush();
							pidToStatus.put(processID, ProcessStatus.RUNNING);
							pidToWorker.put(processID,
									Integer.parseInt(args[3]));
						} else {
							System.err.println("Wrong status");
						}
					} else {
						System.out.println("Invalid process ID");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("No Such Process!");
			} catch (NoSuchMethodException e) {
				System.out.println("No Constructor!");
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				System.out.println("Fail to instantiate an object.");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception Occured.");
			}
		}
	}

	/**
	 * listen for new connnection from workers.
	 */
	private class ConnectionListener implements Runnable {
		private ServerSocket socketListener = null;
		private ProcessManager manager;
		private int workerNum = 0;

		public ConnectionListener(ProcessManager manager) {
			this.manager = manager;
			try {
				socketListener = new ServerSocket(ProcessManager.MANAGER_PORT);
				System.out.println("Server listening on port: "
						+ socketListener.getLocalPort());
			} catch (IOException e) {
				System.err.println("Fail to open socket.");
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Socket socket = socketListener.accept();
					System.out.println("Connection established to "
							+ socket.getInetAddress() + ":" + socket.getPort());
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					WorkerCommunicator communicator = new WorkerCommunicator(
							this.manager);

					// save all the information
					communicator.setIaddr(socket.getInetAddress());
					communicator.setPort(socket.getPort());
					communicator.setIn(in);
					communicator.setOut(out);
					manager.getWorkerToCommunicator().put(workerNum,
							communicator);
					Thread newThread = new Thread(communicator);
					System.out.println("Starting a new listener for a worker");
					newThread.start();
					workerNum++;
				} catch (IOException e) {
					System.err.println("Fail to accept worker server request.");
				}
			}
		}

	}
}
