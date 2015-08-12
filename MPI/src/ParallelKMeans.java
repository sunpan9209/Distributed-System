import java.util.*;
import java.io.*;
import mpi.*;

/**
 * KMeans algorithm using openMP
 * 
 * @author Pan
 */
public class ParallelKMeans {
	private static final int PARTSIZE = 0;
	private static final int CLUSTER = 1;
	private static final int DATA = 2;
	private static final int DONECHECK = 3;

	/**
	 * Centroid
	 * 
	 * @author Pan
	 */
	private static class Centroid implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6153970616077198291L;
		Data centroid;
		Data lastCentroid;
		List<Data> data;

		public Centroid(Data d) {
			this.centroid = d;
			this.lastCentroid = null;
			this.data = new ArrayList<Data>();
		}

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
	private static int mu = 100;
	private static List<Data> dataList;
	private static List<Centroid> centroids;

	// Instance rank
	private static int myrank;
	private static int distanceThreshold;
	private static int p; // num processors
	private static int partSize; // size of data sent to each processor

	public static void main(String[] args) {
		try {
			MPI.Init(args);
			if (args.length != 4) {
				System.out
						.println("Usage: java SequentialKMeans <points or dna> <K> <threshold> <inputfile>");
				return;
			}
			K = Integer.parseInt(args[1]);
			myrank = MPI.COMM_WORLD.Rank();
			p = MPI.COMM_WORLD.Size();
			if (myrank == 0)
				master(args);
			else
				slave();
			MPI.Finalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Master Command
	 * 
	 * @param args
	 * @throws MPIException
	 */
	public static void master(String[] args) throws MPIException {
		distanceThreshold = Integer.parseInt(args[2]);
		String filename = args[3];
		List<Data> theData = null;
		if (args[0].equals("points"))
			theData = getPointData(filename);
		else if (args[0].equals("dna"))
			theData = getDNAData(filename);
		long startTime = System.currentTimeMillis();
		initialize(theData);
		int numWorkers = p - 1;
		partSize = dataList.size() / (numWorkers);
		int remainer = dataList.size() - numWorkers * partSize;

		System.out.println("Num processors " + p);

		pickInitialCentroids();
		printCentroids();
		int[] psize = { partSize + 1 };
		int index = 0;
		Data[] dataArray = dataList.toArray(new Data[0]);

		// send work to slaves
		for (int rank = 1; rank <= numWorkers; rank++) {
			if (remainer == 0) {
				// alocate remainer
				psize[0]--;
				remainer--;
			} else
				remainer--;

			// sent data size first
			MPI.COMM_WORLD.Send(psize, 0, 1, MPI.INT, rank, PARTSIZE);

			// sent data
			MPI.COMM_WORLD.Send(dataArray, index, psize[0], MPI.OBJECT, rank,
					DATA);
			index += psize[0];
		}
		int i;
		for (i = 0; i < mu; i++) {
			Centroid[] centroidArray = new Centroid[K];
			for (int c = 0; c < centroidArray.length; c++) {
				centroidArray[c] = new Centroid(centroids.get(c).centroid);
			}

			// slaves start at rank 1
			for (int rank = 1; rank <= numWorkers; rank++) {
				MPI.COMM_WORLD.Send(centroidArray, 0, K, MPI.OBJECT, rank,
						CLUSTER);
			}

			// wait for work of slaves done
			Centroid[][] results = new Centroid[numWorkers][K];
			int counter = 0;
			while (counter < numWorkers) {
				Status s = MPI.COMM_WORLD.Recv(results[counter], 0, K,
						MPI.OBJECT, MPI.ANY_SOURCE, CLUSTER);
				counter++;
			}

			// jobs for master
			mergeData(results);
			recalculateCentroids();
			boolean done = checkConvergence();
			boolean[] doneArray = { done };
			for (int rank = 1; rank <= numWorkers; rank++) {
				MPI.COMM_WORLD.Send(doneArray, 0, 1, MPI.BOOLEAN, rank,
						DONECHECK);
			}
			if (done)
				break;
		}
		System.out.println("Finished after " + i + " iteration");
		printCentroids();
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) + "ms");
	}

	/**
	 * slave command
	 * 
	 * @throws MPIException
	 */
	public static void slave() throws MPIException {
		boolean[] doneArray = { false };
		int[] psize = new int[1];
		Status s = MPI.COMM_WORLD.Recv(psize, 0, 1, MPI.INT, 0, PARTSIZE);
		partSize = psize[0];
		Data[] data = new Data[partSize];
		Status s2 = MPI.COMM_WORLD.Recv(data, 0, partSize, MPI.OBJECT, 0, DATA);
		dataList = new ArrayList<Data>(Arrays.asList(data));

		// calculate
		while (!doneArray[0]) {
			Centroid[] centroidArray = new Centroid[K];

			Status s1 = MPI.COMM_WORLD.Recv(centroidArray, 0, K, MPI.OBJECT, 0,
					CLUSTER);
			assignData(centroidArray);
			MPI.COMM_WORLD.Send(centroidArray, 0, K, MPI.OBJECT, 0, CLUSTER);
			Status s3 = MPI.COMM_WORLD.Recv(doneArray, 0, 1, MPI.BOOLEAN, 0,
					DONECHECK);
		}
	}

	/**
	 * check if threshold reached
	 * 
	 * @return
	 */
	private static boolean checkConvergence() {
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
	 * add data
	 * 
	 * @param inputData
	 */
	public static void initialize(List<Data> inputData) {
		dataList = inputData;
		centroids = new ArrayList<Centroid>();
		System.out.println("Data size " + dataList.size());
	}

	/**
	 * pick centroids randomly
	 */
	private static void pickInitialCentroids() {
		List<Integer> picked = new ArrayList<Integer>();
		int numElements = dataList.size();
		for (int i = 0; i < K; i++) {
			int index = -1;
			do {
				index = (int) (Math.random() * numElements);
			} while (picked.contains(index));
			picked.add(index);
			Data newCentroid = dataList.get(index);
			Centroid centroid = new Centroid(newCentroid);
			centroids.add(centroid);
		}
	}

	/**
	 * assign data to different centroids
	 * 
	 * @param centroidArray
	 */
	private static void assignData(Centroid[] centroidArray) {
		centroids = new ArrayList<Centroid>(Arrays.asList(centroidArray));
		Iterator<Centroid> resetIter = centroids.iterator();
		while (resetIter.hasNext()) {
			resetIter.next().data = new ArrayList<Data>();
		}
		Iterator<Data> diter = dataList.iterator();
		while (diter.hasNext()) {
			Data d = diter.next();
			if (d == null)
				break;
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
	 * find a new centroid
	 */
	private static void recalculateCentroids() {
		Iterator<Centroid> citer = centroids.iterator();
		while (citer.hasNext()) {
			Centroid eachCentroid = citer.next();
			eachCentroid.lastCentroid = eachCentroid.centroid;
			List<Data> data = eachCentroid.data;
			if (data.size() != 0) {
				Data average = dataList.get(0).average(data);
				eachCentroid.centroid = average;
			}
		}
	}

	/**
	 * form groups of data for printing
	 * 
	 * @return
	 */
	private static List<List<Data>> printCentroids() {
		List<List<Data>> finalList = new ArrayList<List<Data>>();
		Iterator<Centroid> iter = centroids.iterator();
		while (iter.hasNext()) {
			Centroid c = iter.next();
			finalList.add(c.data);
			System.out.println("Cluster has centroid " + c.centroid);
		}
		return finalList;
	}

	private static List<Data> getPointData(String filename) {
		List<Data> randomList = new ArrayList<Data>();
		try {
			File f = new File(filename);
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

	private static List<Data> getDNAData(String filename) {
		List<Data> randomList = new ArrayList<Data>();
		try {
			File f = new File(filename);
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

	/**
	 * merge data from all slaves
	 */
	public static void mergeData(Centroid[][] cs) {
		Iterator<Centroid> resetIter = centroids.iterator();
		while (resetIter.hasNext()) {
			resetIter.next().data.clear();
		}
		for (int j = 0; j < cs.length; j++) {
			for (int i = 0; i < K; i++) {
				centroids.get(i).data.addAll(cs[j][i].data);
			}
		}
	}
}
