package edu.cmu.cs440.p3.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import edu.cmu.cs440.p3.task.Task;

/**
 * The Message passed command
 */
public class Message implements Serializable {

	public static enum TYPE {
		REGISTER, SUBMIT, START, STOP, MONITOR, STATUS, MAPPER, REDUCER, RESULT, EXCEPTION,
	}

	private static final long serialVersionUID = 3085890411891643458L;
	private TYPE type;
	private Task task = null;
	private String msg = null;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String id = null;

	/**
	 * Send a Message to a host and return the response Message
	 */
	public static Message sendRequest(String remoteIp, int remotePort,
			int timeout, Message requestMsg) throws UnknownHostException,
			IOException, ClassNotFoundException, SocketTimeoutException {
		Socket sock = new Socket(remoteIp, remotePort);
		sock.setSoTimeout(timeout);
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		out.writeObject(requestMsg);
		out.flush();
		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
		Message responseMsg = (Message) in.readObject();
		out.close();
		in.close();
		sock.close();
		return responseMsg;
	}

	public Message(TYPE type) {
		this.type = type;
	}

	public TYPE getType() {
		return type;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

}
