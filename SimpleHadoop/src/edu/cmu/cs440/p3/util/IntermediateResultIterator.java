package edu.cmu.cs440.p3.util;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * The iterator that iterates on each intermediate pair
 */

public class IntermediateResultIterator implements Iterator<KeyValuePair>,
		Closeable {

	private int bufferSize = 1000;
	private ObjectInputStream in;
	private List<KeyValuePair> pairs = new LinkedList<KeyValuePair>();
	private ListIterator<KeyValuePair> pairItr = null;

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public IntermediateResultIterator(String filePath, int bufferSize)
			throws FileNotFoundException, IOException {
		this.in = new ObjectInputStream(new FileInputStream(filePath));
		this.bufferSize = bufferSize;
	}

	/**
	 * load key-value pair into a list.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int load(List list, ObjectInputStream in, int size) {
		int num = 0;
		for (int i = 0; i < size; i++) {
			KeyValuePair pair = null;
			try {
				pair = (KeyValuePair) in.readObject();
			} catch (Exception e) {
			}
			if (pair == null)
				break;
			list.add(pair);
			num++;
		}
		return num;
	}

	@Override
	public boolean hasNext() {

		if (this.pairItr == null || !this.pairItr.hasNext()) {
			this.pairs.clear();
			int k = this.load(this.pairs, this.in, this.bufferSize);
			this.pairItr = this.pairs.listIterator();
			if (k == 0)
				return false;
		}

		return this.pairItr.hasNext();
	}

	@Override
	public KeyValuePair next() {
		if (this.pairItr == null || !this.pairItr.hasNext())
			return null;
		return this.pairItr.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		if (this.in != null)
			this.in.close();
	}
}
