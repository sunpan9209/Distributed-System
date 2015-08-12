package edu.cmu.cs440.p3.Interface;

import java.io.Serializable;

import edu.cmu.cs440.p3.util.Emitter;

/**
 * The Mapper interface where the user program has to extend from
 */
public interface Mapper extends Serializable {

	/**
	 * according to hadoop
	 * 
	 * @param key
	 * @param value
	 * @param collector
	 * @throws Exception
	 */
	public abstract void Map(long key, String value, Emitter collector)
			throws Exception;

	public int getNumMapper();

	public int getNumReducer();
}
