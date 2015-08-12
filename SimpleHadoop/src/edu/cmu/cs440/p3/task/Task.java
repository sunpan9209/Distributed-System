package edu.cmu.cs440.p3.task;

import java.io.Serializable;

/**
 * A generic abstract class for all tasks
 */
public abstract class Task implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7731342204632172596L;

	private long taskID;
	private String workerID;
	private String issuer;
	private String mapperClass;
	private String reducerClass;

	public String getMapperClass() {
		return mapperClass;
	}

	public void setMapperClass(String mapperClass) {
		this.mapperClass = mapperClass;
	}

	public String getReducerClass() {
		return reducerClass;
	}

	public void setReducerClass(String reducerClass) {
		this.reducerClass = reducerClass;
	}

	public long getTaskID() {
		return taskID;
	}

	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}

	public String getWorkerID() {
		return workerID;
	}

	public void setWorkerID(String workerID) {
		this.workerID = workerID;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

}
