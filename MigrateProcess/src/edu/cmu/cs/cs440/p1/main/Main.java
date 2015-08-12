package edu.cmu.cs.cs440.p1.main;

import java.io.IOException;

import edu.cmu.cs.cs440.p1.process.ProcessManager;
import edu.cmu.cs.cs440.p1.worker.Worker;

/**
 * run the program
 */
public class Main {
	/**
	 * usage: java Main -s master IP or java Main
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			if (args[0].equals("-s")) {
				System.out.println("hostname:" + args[1]);
				System.out.println("I am a worker");
				Worker worker = new Worker();
				worker.run(args[1]); // start the worker
			} else
				System.out.println("Invalid Argument: -s for worker");
		} else if (args.length > 2 || args.length == 1)
			System.out.println("Usage: java Main [-s <master hostname or ip>]");
		else {
			System.out.println("I am a master");
			new ProcessManager().run(); // start the manager.
		}
	}
}
