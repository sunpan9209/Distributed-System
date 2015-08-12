package edu.cmu.cs.cs440.p1.example;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import edu.cmu.cs.cs440.p1.IO.TransactionalFileInputStream;
import edu.cmu.cs.cs440.p1.IO.TransactionalFileOutputStream;
import edu.cmu.cs.cs440.p1.process.MigratableProcess;

public class GrepProcess implements MigratableProcess {
	private static final long serialVersionUID = 1L;

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private volatile boolean suspending = false;
	private boolean done = false;

	public GrepProcess(String args[]) throws Exception {
		if (args.length != 3) {
			System.out
					.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}

		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}

	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				@SuppressWarnings("deprecation")
				String line = in.readLine();

				if (line == null)
					break;

				if (line.contains(query)) {
					out.println(line);
				}

				// Make grep take longer so that we don't require extremely
				// large files for interesting results
				try {
					Thread.sleep(6000);

				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			done = true;
		} catch (IOException e) {
			System.out.println("GrepProcess: Error: " + e);
		}

		suspending = false;
	}

	public void suspend() {
		suspending = true;
		while (suspending);
	}

	@Override
	public String toString() {
		StringBuilder showstring = new StringBuilder("GrepProcess ");
		showstring.append(this.query);
		showstring.append(" ");
		showstring.append(this.inFile.getFileName());
		showstring.append(" ");
		showstring.append(this.outFile.getFileName());
		return showstring.toString();
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public boolean isSuspended() {
		return this.suspending;
	}

}
