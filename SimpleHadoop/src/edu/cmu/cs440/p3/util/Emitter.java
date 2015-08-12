package edu.cmu.cs440.p3.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * emitting key value pairs to an intermediary results file
 */
public class Emitter {
	private String tmpDir;
	private int tmpFileNum = 1;
	private int bufferSize;
	private List<String> splitPaths = new ArrayList<String>();
	private List<KeyValuePair> buffer = new LinkedList<KeyValuePair>();

	/**
	 * create temp file path
	 * 
	 * @param tmpFileNum
	 * @return temp split filepath
	 */
	private String getSplitFilePath(int tmpFileNum) {
		return this.tmpDir + File.separator + "split_" + tmpFileNum;
	}

	/**
	 * Collect the result and emit to the split file
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void collect(String key, String value) throws IOException {
		this.buffer.add(new KeyValuePair(key, value));
		if (this.buffer.size() >= this.bufferSize)
			emit();
	}

	/**
	 * emit the result to intermediate file
	 * 
	 * @throws IOException
	 */
	public void emit() throws IOException {
		Collections.sort(buffer);
		String splitPath = this.getSplitFilePath(tmpFileNum);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				splitPath));
		for (KeyValuePair pair : buffer) {
			out.writeObject(pair);
			out.flush();
		}
		out.close();
		buffer.clear();
		this.splitPaths.add(splitPath);
		this.tmpFileNum++;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public int getTmpFileNum() {
		return tmpFileNum;
	}

	public void setTmpFileNum(int tmpFileNum) {
		this.tmpFileNum = tmpFileNum;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public List<String> getSplitPaths() {
		return splitPaths;
	}

	public void setSplitPaths(List<String> splitPaths) {
		this.splitPaths = splitPaths;
	}

	public List<KeyValuePair> getBuffer() {
		return buffer;
	}

	public void setBuffer(List<KeyValuePair> buffer) {
		this.buffer = buffer;
	}
}
