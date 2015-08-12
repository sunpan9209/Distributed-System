package edu.cmu.cs.cs440.p1.example;

import edu.cmu.cs.cs440.p1.process.MigratableProcess;

/**
 * print numbers from 1 to 30
 */
public class PrintNums implements MigratableProcess {

	/**
     *
     */
	private static final long serialVersionUID = 1L;
	private volatile boolean suspending = false;
	private boolean done = false;
	private int a = 0;

	public PrintNums(String[] args) {
	}

	public void run() {
		while (!suspending) {

			if (a < 30) {
				System.out.println(++a);
			} else {
				done = true;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		suspending = false;
	}

	public void suspend() {
		suspending = true;
		while (suspending) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		System.out.println("Migratable Process-PrintNumbers");
		return null;
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
