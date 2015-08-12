package edu.cmu.cs440.p3.Interface;

import java.io.Serializable;
import java.util.Iterator;

import edu.cmu.cs440.p3.io.RecordWriter;

/**
 * The Reducer interface where the user program has to extend from
 */
public interface Reducer extends Serializable {

	/**
	 * according to Hadoop.
	 * 
	 * @throws Exception
	 */
	public abstract void Reduce(String key, Iterator<String> values,
			RecordWriter writer) throws Exception;

	public int getRecordLength();
}
