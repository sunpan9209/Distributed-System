package edu.cmu.cs.cs440.p1.process;

import java.io.Serializable;

/**
 * migratable process interface, your process must implement this interface.
 */
public interface MigratableProcess extends Runnable, Serializable {

	/**
	 * Called just before the MigratableProcess is serialized.
	 */
	public void suspend();

	/**
	 * Should return the class name with all of the arguments that were passed
	 * to it.
	 * 
	 * @return The class name and all of its original arguments.
	 */
	public String toString();

	public boolean isDone();

	public boolean isSuspended();
}
