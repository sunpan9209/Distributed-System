package edu.cmu.cs.cs440.p2.test;

import edu.cmu.cs.cs440.p2.rmi.Remote440;

public interface Print extends Remote440 {

	public String printString(String s);

	public int printCounter(String s);

	public String printString();

	public String printException(String s);
	
	public void reset();
	
	public int getCounter();
}
