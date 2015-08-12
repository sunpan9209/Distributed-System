package edu.cmu.cs.cs440.p1.example;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import edu.cmu.cs.cs440.p1.IO.TransactionalFileInputStream;
import edu.cmu.cs.cs440.p1.IO.TransactionalFileOutputStream;
import edu.cmu.cs.cs440.p1.process.MigratableProcess;

/**
 * compress a file
 */
public class FileZipper implements MigratableProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -190883903784921173L;
	private String input;
	private String output;
	private volatile boolean suspending = false;
	private boolean done = false;
	private TransactionalFileInputStream inStream;
	private TransactionalFileOutputStream outStream;

	/**
	 * constructor
	 **/
	public FileZipper() {
	}

	public FileZipper(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("usage: FileZipper <inputfile>");
			throw new Exception("Invalid arguments");
		}
		if (!new File(args[0]).isFile()) {
			System.out.println("Not a valid file");
			throw new Exception("Invalid arguments");
		}
		input = args[0];
		output = args[0] + ".gz";
		inStream = new TransactionalFileInputStream(input);
		outStream = new TransactionalFileOutputStream(output);
	}

	public String toString() {
		return "FileCompression: " + input;
	}

	@Override
	public void run() {
		DataInputStream in = new DataInputStream(inStream);
		try {
			GZIPOutputStream out = new GZIPOutputStream(outStream);
			byte[] buf = new byte[256];
			int numbercount = 0;
			while (!suspending && !done) {
				numbercount = in.read(buf);
				if (numbercount == -1) {
					done = true;
					break;
				}
				out.write(buf, 0, numbercount);
				out.flush();
				try {
					Thread.sleep(9000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inStream.closeStream();
		inStream.setMigrated(true);
		outStream.closeStream();
		outStream.setMigrated(true);
		suspending = false;
	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending && !done)
			;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public boolean isSuspended() {
		return suspending;
	}

}
