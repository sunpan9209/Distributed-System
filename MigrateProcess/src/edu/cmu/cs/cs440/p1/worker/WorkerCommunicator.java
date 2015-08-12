package edu.cmu.cs.cs440.p1.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import edu.cmu.cs.cs440.p1.process.ProcessManager;

/**
 * contain address, port, socketStream and monitor the inputStream of the
 * worker.
 */
public class WorkerCommunicator implements Runnable {

	private InetAddress iaddr;
	private int port;
	private BufferedReader in;
	private PrintWriter out;
	private ProcessManager manager;

	public WorkerCommunicator(ProcessManager manager) {
		this.manager = manager;
	}

	public String toString() {
		return "\tAddress: " + iaddr + "\tport number: " + port;
	}

	public InetAddress getIaddr() {
		return iaddr;
	}

	public void setIaddr(InetAddress iaddr) {
		this.iaddr = iaddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	@Override
	public void run() {
		try {
			while (true) {
				String inMsg = in.readLine();
				String[] args = inMsg.split(" ");
				if (args[0].equals("done")) {
					int processID = Integer.valueOf(args[1]);
					manager.getPidToStatus().remove(processID);
					manager.getPidToWorker().remove(processID);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
