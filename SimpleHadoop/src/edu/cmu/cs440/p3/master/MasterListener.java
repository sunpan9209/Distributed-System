package edu.cmu.cs440.p3.master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.cmu.cs440.p3.configuration.Config;

/**
 * listens for network communication in and deals with it appropriately.
 */
public class MasterListener extends Thread {
	private Master master;
	private volatile boolean running;
	ServerSocket sSock;

	public MasterListener(Master master, Config config) {
		this.running = true;
		this.master = master;
		try {
			this.sSock = new ServerSocket(config.getMasterPort());
		} catch (IOException e) {
			System.err.println("Unable to open Server Socket");
			e.printStackTrace();
		}
	}

	/**
	 * stop the socket listener
	 */
	public void stopServer() {
		running = false;
		try {
			sSock.close();
		} catch (IOException e) {
			System.err.println("Error closing socket in stopServer");
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
			MasterCommHandler handler = new MasterCommHandler(master, s);
			handler.start();
		}
	}

}
