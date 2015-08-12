package edu.cmu.cs440.p3.task;

import java.util.Map;

import edu.cmu.cs440.p3.Interface.Mapper;
import edu.cmu.cs440.p3.io.InputSplit;

/**
 * MapTask holds all the info for a map task
 */
public class MapTask extends Task {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8305200298461581217L;

	private int mapperID;
	private String outputFolder;
	private String inputFile;
	private InputSplit split;

	public InputSplit getSplit() {
		return split;
	}

	public void setSplit(InputSplit split) {
		this.split = split;
	}

	private Map<Integer, String> intermediateResults;

	private Mapper job;

	public int getMapperID() {
		return mapperID;
	}

	public void setMapperID(int mapperID) {
		this.mapperID = mapperID;
	}

	public Mapper getJob() {
		return job;
	}

	public void setJob(Mapper job) {
		this.job = job;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public Map<Integer, String> getIntermediateResults() {
		return intermediateResults;
	}

	public void setIntermediateResults(Map<Integer, String> intermediateResults) {
		this.intermediateResults = intermediateResults;
	}
}
