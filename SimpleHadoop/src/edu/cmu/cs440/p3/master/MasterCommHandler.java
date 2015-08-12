package edu.cmu.cs440.p3.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.cmu.cs440.p3.communication.Message;

/**
 * handle incoming message
 */
public class MasterCommHandler extends Thread {
	private Master master;
	private Socket s;

	public MasterCommHandler(Master master, Socket s) {
		this.master = master;
		this.s = s;
	}

	public void run() {
		ObjectInputStream oIs;
		ObjectOutputStream oOs;

		try {
			oOs = new ObjectOutputStream(s.getOutputStream());
			oIs = new ObjectInputStream(s.getInputStream());
		} catch (Exception e) {
			System.err.println("error getting I/O streams");
			try {
				s.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}

		Message msg;

		try {
			msg = (Message) oIs.readObject();
		} catch (Exception e) {
			System.err.println("Error receiving msg");
			try {
				s.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}

		switch (msg.getType()) {
		case REGISTER:
			// add new worker to the worker table and reply back
			master.register(msg.getMsg());
			break;

		case SUBMIT:
			// submit a task
			if (!master.isAlive()) {
				msg.setMsg("Facility has not been started yet.");
			} else {
				master.submit(msg, s);
			}
			break;

		case START:
			// start the facility
			if (msg.getMsg().equals("START")) {
				msg.setMsg("Start Successfully");
				master.start();
			}
			break;

		case STOP:
			// don't accept any new task
			if (msg.getMsg().equals("STOP")) {
				msg.setMsg("Stop Successfully");
				try {
					master.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;

		case MONITOR:
			// return the number of processes running on the worker
			if (!master.isAlive()) {
				msg.setMsg("The Map-Reduce facility has not been started yet.");
			} else {
				master.monitor(msg);
			}
			break;

		case MAPPER:
			// finish the map task and send out the reduce tasks
			try {
				master.mapper(msg);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;

		case REDUCER:
			// finish the reduce task and send back the result to the issuer
			master.reducer(msg);
			break;

		case EXCEPTION:
			// worker encounters a problem
			try {
				master.exception(msg);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
			}

		default:
			break;
		}
		try {
			oOs.writeObject(msg);
			oOs.flush();
			oOs.close();
			s.close();
		} catch (IOException e) {
			System.err.println("Error replying a new Job message");
			e.printStackTrace();
		}
		return;
	}
}
