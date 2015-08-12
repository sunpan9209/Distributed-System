package edu.cmu.cs440.p3.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.cmu.cs440.p3.Interface.Mapper;
import edu.cmu.cs440.p3.Interface.Reducer;
import edu.cmu.cs440.p3.communication.Message;
import edu.cmu.cs440.p3.configuration.Config;
import edu.cmu.cs440.p3.io.InputSplit;
import edu.cmu.cs440.p3.io.Record;
import edu.cmu.cs440.p3.io.RecordReader;
import edu.cmu.cs440.p3.io.RecordWriter;
import edu.cmu.cs440.p3.task.MapTask;
import edu.cmu.cs440.p3.task.ReduceTask;
import edu.cmu.cs440.p3.util.Emitter;
import edu.cmu.cs440.p3.util.IntermediateResultIterator;
import edu.cmu.cs440.p3.util.KeyValuePair;
import edu.cmu.cs440.p3.util.MergeSorter;
import edu.cmu.cs440.p3.util.Partitioner;

/**
 * execute the map or reduce task
 * 
 * @param <V>
 * @param <K>
 */
public class Worker {
	private boolean isAlive;
	private String workerID;
	private Config config;
	private String addr;
	private int port;

	public Worker(Config config, String workerID) {
		this.setConfig(config);
		this.setWorkerID(workerID);
		this.isAlive = true;
		this.addr = config.getMasterAddr();
		this.port = config.getMasterPort();
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void shutdown() throws IOException {
		isAlive = false;
	}

	/**
	 * execute map task
	 * 
	 * @throws IOException
	 */
	public void map(Message msg) {
		MapTask mapTask = (MapTask) msg.getTask();
		Mapper mapper = mapTask.getJob();
		InputSplit inputsplit = mapTask.getSplit();
		RecordReader reader = new RecordReader(inputsplit);
		Record record = null;
		Emitter emitter = null;
		MergeSorter msorter = new MergeSorter();
		String tmpDir = "tmp" + File.separator + mapTask.getWorkerID()
				+ File.separator + mapTask.getTaskID() + File.separator + "map";
		String sortPath = tmpDir + File.separator + "pairs.sorted";
		new File(tmpDir).mkdirs();
		try {
			record = reader.getRecord();
			emitter = new Emitter();
			emitter.setBufferSize(config.getBufferSize());
			emitter.setTmpDir(tmpDir);

			// run map task
			mapper.Map(record.getKey(), record.getValue(), emitter);
			while ((record = reader.nextRecord()) != null) {
				mapper.Map(record.getKey(), record.getValue(), emitter);
			}
			emitter.emit();
			msorter.setBufferSize(config.getBufferSize());
			msorter.setTmpDir(tmpDir);
			msorter.sortSplits(emitter.getSplitPaths(), sortPath);

			// partition
			Partitioner partitioner = new Partitioner();
			partitioner.setBufferSize(config.getBufferSize());
			partitioner.setPartitionNum(mapTask.getJob().getNumReducer());
			partitioner.setTmpDir(tmpDir);
			Map<Integer, String> res = partitioner.partition(sortPath);

			// send back msg
			mapTask.setIntermediateResults(res);
			Message message = new Message(Message.TYPE.MAPPER);
			message.setTask(mapTask);
			Message.sendRequest(addr, port, 5000, message);
		} catch (Exception e) {
			e.printStackTrace();
			sendException(msg);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}
		System.out.println("mapping done" + sortPath);
	}

	/**
	 * execute the reduce task
	 * 
	 * @param msg
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void reduce(Message msg) {
		ReduceTask reduceTask = (ReduceTask) msg.getTask();
		Reducer reducer = reduceTask.getJob();
		MergeSorter msorter = new MergeSorter();

		// prepare the paths and parameters
		int bufferSize = config.getBufferSize();
		List<String> inputs = reduceTask.getInputFiles();
		String tmpReduceDir = "tmp" + File.separator + workerID
				+ File.separator + reduceTask.getTaskID() + File.separator
				+ "reduce";
		String Identifier = "output_" + reduceTask.getTaskID() + "_" + workerID;
		String outputFile = reduceTask.getOutputFolder() + File.separator
				+ Identifier;
		String sortPath = tmpReduceDir + File.separator + "pairs.sorted";
		new File(tmpReduceDir).mkdirs();
		new File(reduceTask.getOutputFolder()).mkdirs();

		// do the reduce
		try {
			RecordWriter writer = new RecordWriter(outputFile, 0,
					reducer.getRecordLength());
			msorter.setBufferSize(bufferSize);
			msorter.setTmpDir(tmpReduceDir);
			msorter.sortSplits(inputs, sortPath);
			reduce(sortPath, bufferSize, reducer, writer);
			Message message = new Message(Message.TYPE.REDUCER);
			message.setTask(reduceTask);
			Message.sendRequest(addr, port, 5000, message);
		} catch (Exception e) {
			e.printStackTrace();
			sendException(msg);
		}
	}

	/**
	 * Notify the master for any exceptions
	 */
	private void sendException(Message msg) {
		try {
			Message exceptionMsg = new Message(Message.TYPE.EXCEPTION);
			exceptionMsg.setTask(msg.getTask());
			Message.sendRequest(addr, port, 5000, exceptionMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * combine values and execute the reduce method
	 * 
	 * @throws Exception
	 */
	private void reduce(String path, int bufferSize, Reducer reducer,
			RecordWriter writer) throws Exception {
		IntermediateResultIterator reader = new IntermediateResultIterator(
				path, bufferSize);
		KeyValuePair currPair = null;
		KeyValuePair lastPair = null;
		String key = null;
		List<String> values = new ArrayList<String>();
		while (reader.hasNext()) {
			currPair = reader.next();
			if (lastPair == null) {
				lastPair = currPair;
				key = lastPair.getKey();
			}
			if (lastPair.getKey().compareTo(currPair.getKey()) != 0) {
				reducer.Reduce(key, values.iterator(), writer);
				lastPair = currPair;
				key = lastPair.getKey();
				values.clear();
			}
			values.add(currPair.getValue());
		}
		reader.close();
		writer.close();
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public String getWorkerID() {
		return workerID;
	}

	public void setWorkerID(String workerID) {
		this.workerID = workerID;
	}

}
