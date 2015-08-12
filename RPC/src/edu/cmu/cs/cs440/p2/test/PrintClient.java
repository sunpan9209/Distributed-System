package edu.cmu.cs.cs440.p2.test;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketTimeoutException;

import edu.cmu.cs.cs440.p2.rmi.Communication;
import edu.cmu.cs.cs440.p2.rmi.RemoteObjectRef;

/**
 * client for test cases.
 */
public class PrintClient {
	private static String testString = "normal pass";

	public PrintClient() {
	}

	/**
	 * test concurrency
	 */
	public class Concurrency extends Thread {
		private Print print;

		public Concurrency(Print p) {
			this.print = p;
		}

		public void run() {
			try {
				print.printCounter("test");
			} catch (Exception e) {
				if (e.getCause().getClass() != ArithmeticException.class) {
					System.out.println("Exception not correctly thrown");
					e.printStackTrace();
				} else {
					System.out.println("Exception correctly thrown");
				}
			}
		}
	}

	public static void main(String args[]) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		RemoteObjectRef ror1 = Communication.lookup("print1");
		Print o1 = (Print) ror1.localise();
		RemoteObjectRef ror2 = Communication.lookup("print2");
		Print o2 = (Print) ror2.localise();
		System.out.println(o1.printString(testString));
		System.out.println(o1.printString());

		int counter1 = 0;
		int counter2 = 0;
		try {
			for (int i = 0; i < 5; i++) {
				counter1 = o1.printCounter(testString);
				counter2 = o2.printCounter(testString);
				if ((counter1 != (i + 1)) || (counter2 != (i + 1))) {
					System.out.println("consistency fail");
				}
			}
		} catch (Exception e) {
			if (e.getCause().getClass() != ArithmeticException.class) {
				System.out.println("Exception not correctly thrown");
				e.printStackTrace();
			} else {
				System.out.println("Exception correctly thrown");
			}
		}
		System.out.println("consistency pass");

		o1.reset();
		PrintClient client = new PrintClient();
		try {
			int i = 0;
			for (; i < 10; i++) {
				(client.new Concurrency(o1)).start();
			}
			Thread.sleep(3000);
			if (o1.getCounter() == i) {
				System.out.println("concurrency pass");
			} else {
				System.out.println("concurrency fail: counter is "
						+ o1.getCounter() + "race condition happens");
			}
		} catch (Exception e) {
			if (e.getCause().getClass() != ArithmeticException.class) {
				System.out.println("Exception not correctly thrown");
				e.printStackTrace();
			} else {
				System.out.println("Exception correctly thrown");
			}
		}

		try {
			System.out.println(o1.printException(testString));
		} catch (UndeclaredThrowableException e) {
			if (e.getCause() instanceof SocketTimeoutException)
				System.out.println("exception pass");
		}
	}
}
