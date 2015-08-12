package edu.cmu.cs.cs440.p2.test;

public class PrintImpl implements Print {
	private int counter = 0;

	public PrintImpl() {
	}

	@Override
	public String printString(String s) {
		return s;
	}

	@Override
	public int printCounter(String s) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter++;
		return counter;
	}

	@Override
	public String printString() {
		return "args pass";
	}

	@Override
	public String printException(String s) {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public void reset() {
		counter = 0;
	}

	@Override
	public int getCounter() {
		return counter;
	}

}
