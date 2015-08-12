package edu.cmu.cs440.p3.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.cmu.cs440.p3.communication.Message;

/**
 * handle incoming message
 */
public class WorkerCommHandler extends Thread {
	private Worker worker;
	private Socket s;

	public WorkerCommHandler(Worker worker, Socket s) {
		this.worker = worker;
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
		case STATUS:
			// check status
			if (msg.getMsg().equals("QUERY"))
				msg.setMsg("HEALTHY");
			break;

		case MAPPER:
			// start a map task
			worker.map(msg);
			break;

		case REDUCER:
			// start a reduce task
			worker.reduce(msg);
			break;

		case RESULT:
			// print the result
			System.out.println(msg.getMsg());

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
