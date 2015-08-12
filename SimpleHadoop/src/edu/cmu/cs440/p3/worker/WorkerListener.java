package edu.cmu.cs440.p3.worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.cmu.cs440.p3.configuration.Config;

/**
 * listens for network communication in and deals with it appropriately.
 */
public class WorkerListener extends Thread {
	private Worker worker;
	private volatile boolean running;
	private ServerSocket sSock;

	public WorkerListener(Worker worker, Config config, String workerID) {
		this.running = true;
		this.worker = worker;
		try {
			this.sSock = new ServerSocket(config.getClientPort(workerID));
		} catch (IOException e) {
			System.err.println("Unable to open Server Socket");
			e.printStackTrace();
		}
	}

	/**
	 * stop the socket listener
	 */
	public void stopWorker() {
		running = false;
		try {
			sSock.close();
		} catch (IOException e) {
			System.err.println("Error closing socket in stopWorker");
			e.printStackTrace();
		}
	}

	public void run() {
		while (running) {
			Socket s;
			try {
				s = sSock.accept();
			} catch (Exception e) {
				continue;
			}
			WorkerCommHandler handler = new WorkerCommHandler(worker, s);
			handler.start();
		}
	}
}
