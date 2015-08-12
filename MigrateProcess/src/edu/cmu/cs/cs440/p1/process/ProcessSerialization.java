package edu.cmu.cs.cs440.p1.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * serialize or deserialize a process.
 */
public class ProcessSerialization {

	// Directory where the processes will be serialized to in the shared DFS.
	// Must end with "/".
	public static String serializeDirectory = new File("file").getPath() + "/";

	/**
	 * serialize
	 * 
	 * @param task
	 * @param pid
	 */
	public static void serialize(MigratableProcess task, int pid) {
		FileOutputStream file;
		try {
			System.out.println("Serializing Process " + pid);
			new File(serializeDirectory + Integer.toString(pid) + ".obj")
					.delete();
			file = new FileOutputStream(serializeDirectory
					+ Integer.toString(pid) + ".obj");
			ObjectOutputStream objectOutStrm = new ObjectOutputStream(file);
			objectOutStrm.writeObject(task);
			objectOutStrm.flush();
			objectOutStrm.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * deserialize
	 * 
	 * @param pid
	 * @return MigratableProcess
	 */
	public static MigratableProcess deserialize(int pid) {
		FileInputStream file;
		try {
			System.out.println("DeSerializing Process " + pid);
			file = new FileInputStream(serializeDirectory
					+ Integer.toString(pid) + ".obj");
			ObjectInputStream objectInStrm = new ObjectInputStream(file);
			Object o = objectInStrm.readObject();
			MigratableProcess m = (MigratableProcess) o;
			objectInStrm.close();
			new File(serializeDirectory + Integer.toString(pid) + ".obj")
					.delete();
			m.toString();
			System.out.println("Done");
			return m;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
