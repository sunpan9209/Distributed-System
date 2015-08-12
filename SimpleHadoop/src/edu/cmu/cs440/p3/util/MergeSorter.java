package edu.cmu.cs440.p3.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This serves as a tool to merge sort the intermediate records output from the
 * mapper
 */
public class MergeSorter {

	private int bufferSize;
	private String tmpDir;

	/**
	 * merge two files.
	 * 
	 * @param filePath1
	 * @param filePath2
	 * @param mergedFilePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void mergeTwoTmpFiles(String filePath1, String filePath2,
			String mergedFilePath) throws FileNotFoundException, IOException {
		int numPair = 0;
		IntermediateResultIterator itr1 = new IntermediateResultIterator(
				filePath1, this.bufferSize);
		IntermediateResultIterator itr2 = new IntermediateResultIterator(
				filePath2, this.bufferSize);
		ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(mergedFilePath)));
		KeyValuePair pair1 = itr1.hasNext() ? itr1.next() : null;
		KeyValuePair pair2 = itr2.hasNext() ? itr2.next() : null;
		KeyValuePair currPair = null;
		KeyValuePair lastPair = null;
		while (pair1 != null || pair2 != null) {
			if (pair1 != null && (pair2 == null || pair1.compareTo(pair2) <= 0)) {
				currPair = pair1;
				pair1 = itr1.hasNext() ? itr1.next() : null;
			} else {
				currPair = pair2;
				pair2 = itr2.hasNext() ? itr2.next() : null;
			}
			out.writeObject(currPair);
			numPair++;
			if (lastPair == null) {
				lastPair = currPair;
			} else if (currPair.compareTo(lastPair) == 0) {
				continue;
			} else {
				lastPair = currPair;
			}

		}
		out.flush();
		out.close();
		itr1.close();
		itr2.close();
		System.out.println("pairNum: " + numPair);
		return;
	}

	private String getFilePath(int phaseNum, int tmpFileSeq) {
		return this.tmpDir + File.separator + "tmp_" + phaseNum + "_"
				+ tmpFileSeq;
	}

	/**
	 * Sort the split files
	 */
	public void sortSplits(List<String> splitPaths, String sortedPath)
			throws FileNotFoundException, IOException {
		List<String> toMergePaths = new ArrayList<String>(splitPaths);
		int maxSeq = splitPaths.size();
		int lastSeq = -1;
		int phaseNum = 1;
		if (maxSeq != 1) {
			while (maxSeq > 1) {
				List<String> mergedPaths = new ArrayList<String>();
				lastSeq = -1;
				if (maxSeq % 2 != 0) {
					lastSeq = maxSeq;
					maxSeq = maxSeq - 1;
				}
				for (int i = 1; i <= maxSeq / 2; i++) {
					mergeTwoTmpFiles(toMergePaths.get(i - 1),
							toMergePaths.get(i + maxSeq / 2 - 1),
							getFilePath(phaseNum + 1, i));
					mergedPaths.add(getFilePath(phaseNum + 1, i));
				}
				maxSeq = maxSeq / 2;

				// remains one file to merge
				if (lastSeq != -1) {
					FileOperation.renameFile(toMergePaths.get(lastSeq - 1),
							getFilePath(phaseNum + 1, maxSeq + 1), true);
					mergedPaths.add(getFilePath(phaseNum + 1, maxSeq + 1));
					maxSeq++;
				}

				phaseNum++;
				toMergePaths = mergedPaths;
			}
		}
		FileOperation
				.renameFile(toMergePaths.get(maxSeq - 1), sortedPath, true);
		return;
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
}
