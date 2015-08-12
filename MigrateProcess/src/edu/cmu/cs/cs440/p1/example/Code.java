package edu.cmu.cs.cs440.p1.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import edu.cmu.cs.cs440.p1.IO.TransactionalFileInputStream;
import edu.cmu.cs.cs440.p1.IO.TransactionalFileOutputStream;
import edu.cmu.cs.cs440.p1.process.MigratableProcess;

/**
 * encode or decode a file
 */
public class Code implements MigratableProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String option;
	private String input;
	private String output;
	private volatile boolean suspending = false;
	private boolean done = false;
	private TransactionalFileInputStream inStream;
	private TransactionalFileOutputStream outStream;

	/**
	 * constructor
	 **/
	public Code() {
	}

	public Code(String[] args) throws Exception {
		if (args.length != 3
				|| (!args[0].equals("encode") && !args[0].equals("decode"))) {
			System.out.println("usage: Code <option> <inputfile> <outputfile>");
			System.out.println("options:");
			throw new Exception("Invalid arguments");
		}
		if (!new File(args[1]).isFile()) {
			System.out.println("Not a valid file");
			throw new Exception("Invalid arguments");
		}
		option = args[0];
		input = args[1];
		output = args[2];
		inStream = new TransactionalFileInputStream(input);
		outStream = new TransactionalFileOutputStream(output);
	}

	public String toString() {
		return "Migratable Process: Code   " + option + "  "
				+ inStream.getFileName() + "  " + outStream.getFileName();
	}

	@Override
	public void run() {
		suspending = false;
		DataInputStream in = new DataInputStream(inStream);
		DataOutputStream out = new DataOutputStream(outStream);
		char current = '\0';
		char newchar = '\0';
		while (!suspending && !done) {
			try {
				current = in.readChar();
			} catch (EOFException eof) {
				done = true;
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}

			// encode
			if (option.equals("encode")) {
				newchar = (char) (current + 3);
			} else {
				newchar = (char) (current - 3);
			}
			try {
				out.writeChar(newchar);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
