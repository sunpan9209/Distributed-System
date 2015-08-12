package edu.cmu.cs440.p3.task;

import java.util.List;

import edu.cmu.cs440.p3.Interface.Reducer;

public class ReduceTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552240454021458949L;

	private int reducerID;
	private List<String> inputFiles;
	private Reducer job;
	private String outputFolder;

	public int getReducerID() {
		return reducerID;
	}

	public void setReducerID(int reducerID) {
		this.reducerID = reducerID;
	}

	public List<String> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<String> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public Reducer getJob() {
		return job;
	}

	public void setJob(Reducer job) {
		this.job = job;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

}
