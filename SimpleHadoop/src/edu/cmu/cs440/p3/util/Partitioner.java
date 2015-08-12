package edu.cmu.cs440.p3.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * partition sorted file by hashcode value of key
 */
public class Partitioner {

	private int bufferSize;
	private String tmpDir = null;
	private int partitionNum = 1;
	private Map<Integer, ObjectOutputStream> partitionMap = new HashMap<Integer, ObjectOutputStream>();

	public int getPartitionNum() {
		return partitionNum;
	}

	public void setPartitionNum(int partitionNum) {
		this.partitionNum = partitionNum;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * partition by hashcode
	 * 
	 * @param key
	 * @param value
	 * @param numPartitions
	 * @return
	 */
	private int getPartitionID(String key, String value, int numPartitions) {
		return Math.abs(key.hashCode() % numPartitions);
	}

	/**
	 * put results into temp files
	 * 
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Map<Integer, String> partition(String filePath)
			throws FileNotFoundException, IOException {
		Map<Integer, String> partitionPathMap = new HashMap<Integer, String>();
		IntermediateResultIterator reader = new IntermediateResultIterator(
				filePath, this.bufferSize);
		KeyValuePair pair = null;
		this.partitionMap.clear();
		while (reader.hasNext()) {
			pair = reader.next();
			int partitionID = this.getPartitionID(pair.getKey(),
					pair.getValue(), this.partitionNum);
			if (!this.partitionMap.containsKey(partitionID)) {
				String partitionPath = this.tmpDir + File.separator
						+ "partition_" + partitionID;
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(partitionPath));
				this.partitionMap.put(partitionID, out);
				partitionPathMap.put(partitionID, partitionPath);
			}
			ObjectOutputStream out = this.partitionMap.get(partitionID);
			out.writeObject(pair);
		}
		reader.close();
		for (ObjectOutputStream out : this.partitionMap.values()) {
			out.flush();
			out.close();
		}
		return partitionPathMap;
	}
}
