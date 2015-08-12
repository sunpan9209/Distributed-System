package edu.cmu.cs.cs440.p2.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * used for communications between client stub and the server proxy dispatcher,
 * convey function call information, return value and exceptions.
 */
public class RMIMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6239613864335263725L;

	private String method;
	private String key;
	private Class<?>[] argTypes;
	private Object[] args;
	private Object returnVal;
	private Exception exception;

	public RMIMessage(String method, String key, Object[] args,
			Class<?>[] argTypes) {
		this.args = args;
		this.argTypes = argTypes;
		this.key = key;
		this.method = method;
	}

	/**
	 * Send a RMIMessage to a host and return the response RMIMessage. open
	 * socket, write request, receive reponse, close socket and stream.
	 * 
	 * @param ip
	 *            Server's IP address
	 * @param port
	 *            Server's port number
	 * @param timeout
	 *            Timeout limit
	 * @param request
	 *            RMIMessage being sent
	 * @return the response RMIMessage
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static RMIMessage sendRequest(String ip, int port, int timeout,
			RMIMessage request) throws UnknownHostException, IOException,
			ClassNotFoundException, SocketTimeoutException {
		Socket sock = new Socket(ip, port);
		sock.setSoTimeout(timeout);

		// Write this message to the host by marshalling
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		out.writeObject(request);
		out.flush();

		// Read the Object
		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
		RMIMessage response = (RMIMessage) in.readObject();

		if (in != null)
			in.close();
		if (out != null)
			out.close();
		if (sock != null)
			sock.close();

		return response;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Class<?>[] getArgTypes() {
		return argTypes;
	}

	public void setArgTypes(Class<?>[] argTypes) {
		this.argTypes = argTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Object getReturnVal() {
		return returnVal;
	}

	public void setReturnVal(Object returnVal) {
		this.returnVal = returnVal;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}
