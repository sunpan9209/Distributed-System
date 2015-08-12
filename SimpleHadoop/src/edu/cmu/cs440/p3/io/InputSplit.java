package edu.cmu.cs440.p3.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * partition the original file by size of mappers
 */
public class InputSplit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private long length;
	private long start;
	private int mapnum;

	@SuppressWarnings("resource")
	public ArrayList<InputSplit> getSplits() throws IOException {

		RandomAccessFile file;
		ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
		file = new RandomAccessFile(path, "r");
		long FileSize = file.length();
		long SplitSize = FileSize / (mapnum);

		long current_start = 0;
		long count = 0;

		while (count < mapnum) {
			this.start = current_start;
			this.length = SplitSize;
			if (count != mapnum - 1) {
				splits.add(new InputSplit(path, length, this.start));
				FileSize -= length;
			} else {
				splits.add(new InputSplit(path, FileSize, this.start));
			}
			current_start = current_start + length;
			count++;
		}
		return splits;
	}

	public InputSplit(String path, int mapnum) {
		this.path = path;
		this.mapnum = mapnum;
	}

	public InputSplit(String path, long length, long start) {
		this.path = path;
		this.length = length;
		this.start = start;
	}

	public String getpath() {
		return this.path;
	}

	public long getlength() {
		return this.length;
	}

	public long getstart() {
		return this.start;
	}
}
