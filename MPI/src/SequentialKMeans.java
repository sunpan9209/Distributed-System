import java.util.*;
import java.io.*;

/**
 * sequential KMeans Algorithm
 * 
 * @author Pan
 */
public class SequentialKMeans {

	/**
	 * centroid
	 * 
	 * @author Pan
	 */
	private class Centroid {
		Data centroid;
		Data lastCentroid;
		List<Data> data;

		public Centroid(Data d) {
			this.centroid = d;
			this.lastCentroid = null;
			this.data = new ArrayList<Data>();
		}

		@SuppressWarnings("unused")
		public Centroid(Data d, List<Data> givenData) {
			this.centroid = d;
			this.lastCentroid = null;
			this.data = givenData;
		}

		public String toString() {
			return (data.toString());
		}
	}

	private static int K = 3;
	// maximum number of iterations
	private static int maxMu = 100;
	private int k;
	private int distanceThreshold;
	private List<Data> dataList;
	private List<Centroid> centroids;

	public SequentialKMeans(List<Data> inputData, int k, int distance)
			throws IllegalArgumentException {
		if (inputData.size() < k)
			throw new IllegalArgumentException();

		this.dataList = inputData;
		this.k = k;
		this.centroids = new ArrayList<Centroid>();
		this.distanceThreshold = distance;
	}

	public List<List<Data>> kMeans() {
		pickInitialCentroids();
		int i;
		for (i = 0; i < maxMu; i++) {
			assignData();
			recalculateCentroids();
			if (checkConvergence())
				break;
		}
		System.out.println("Converged after " + (i + 1) + " iterations");
		return printCentroids();
	}

	/**
	 * pick up centroids randomly
	 */
	private void pickInitialCentroids() {
		List<Integer> picked = new ArrayList<Integer>();
		int numElements = dataList.size();
		for (int i = 0; i < k; i++) {
			int index = -1;
			do {
				index = (int) (Math.random() * numElements);
			} while (picked.contains(index));
			picked.add(index);
			Data newCentroid = dataList.get(index);
			Centroid newCluster = new Centroid(newCentroid);
			centroids.add(newCluster);
		}
	}

	/**
	 * assign data to centroids
	 */
	private void assignData() {
		Iterator<Centroid> resetIter = centroids.iterator();
		while (resetIter.hasNext()) {
			resetIter.next().data.clear();
		}
		Iterator<Data> diter = dataList.iterator();
		while (diter.hasNext()) {
			Data d = diter.next();
			Iterator<Centroid> citer = centroids.iterator();
			Centroid closest = null;
			int minDistance = Integer.MAX_VALUE;
			while (citer.hasNext()) {
				Centroid c = citer.next();
				int thisDistance = d.distance(c.centroid);
				if (thisDistance < minDistance) {
					closest = c;
					minDistance = thisDistance;
				}
			}
			closest.data.add(d);
		}
		return;
	}

	/**
	 * find a new centroid by calculating the average of data
	 */
	private void recalculateCentroids() {
		Iterator<Centroid> citer = centroids.iterator();
		while (citer.hasNext()) {
			Centroid eachCentroid = citer.next();
			eachCentroid.lastCentroid = eachCentroid.centroid;
			if (dataList.size() != 0) {
				Data average = dataList.get(0).average(eachCentroid.data);
				eachCentroid.centroid = average;
			}
		}
	}

	/**
	 * check if the distance reach the threshold
	 */
	private boolean checkConvergence() {
		Iterator<Centroid> citer = centroids.iterator();
		int maxDistance = 0;
		while (citer.hasNext()) {
			Centroid c = citer.next();
			int thisDistance = c.centroid.distance(c.lastCentroid);
			maxDistance = Math.max(thisDistance, maxDistance);
		}
		return (maxDistance <= distanceThreshold);
	}

	/**
	 * return a list of groups of data
	 */
	private List<List<Data>> printCentroids() {
		List<List<Data>> finalList = new ArrayList<List<Data>>();
		Iterator<Centroid> iter = centroids.iterator();
		while (iter.hasNext()) {
			Centroid c = iter.next();
			finalList.add(c.data);
			System.out.println("Cluster has centroid " + c.centroid);
		}
		return finalList;
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out
					.println("Usage: SequentialKMeans <points or dna> <K> <threshold> <inputfile>");
			return;
		}
		long startTime = System.currentTimeMillis();
		K = Integer.parseInt(args[1]);
		int distanceT = Integer.parseInt(args[2]);
		String filename = args[3];

		// Select which data type to use
		List<Data> theData = null;
		if (args[0].equals("points"))
			theData = getPointData(filename);
		else if (args[0].equals("dna"))
			theData = getDNAData(filename);
		SequentialKMeans worker = new SequentialKMeans(theData, K, distanceT);
		System.out.println("Starting calculating, K=" + K);
		worker.kMeans();
		long endTime = System.currentTimeMillis();
		System.out
				.println("Execution time was " + (endTime - startTime) + "ms");
	}

	/**
	 * collect point data from a file
	 */
	private static List<Data> getPointData(String filename) {
		List<Data> randomList = new ArrayList<Data>();
		try {
			File f = new File(filename);
			@SuppressWarnings("resource")
			Scanner s = new Scanner(f);
			while (s.hasNextInt()) {
				Point p = new Point(s.nextInt(), s.nextInt());
				randomList.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return randomList;
	}

	/**
	 * collect DNA data from a file
	 */
	private static List<Data> getDNAData(String filename) {
		List<Data> randomList = new ArrayList<Data>();
		try {
			File f = new File(filename);
			@SuppressWarnings("resource")
			Scanner s = new Scanner(f);
			while (s.hasNextLine()) {
				DNA dna = new DNA(s.nextLine());
				randomList.add(dna);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return randomList;

	}
}
