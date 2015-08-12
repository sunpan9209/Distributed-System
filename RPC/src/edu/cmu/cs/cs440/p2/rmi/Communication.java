package edu.cmu.cs.cs440.p2.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.cmu.cs.cs440.p2.server.ProxyDispatcher;

/**
 * transparently communicate with rmi registry server, bind and lookup the ror
 * information.
 */
public class Communication {

	private static String DEFAULT_SERVER_IP = "localhost"; // should be the
															// actual server
															// address.
	private static int DEFAULT_REGISTRY_PORT = 15440; // available port
	private static int DEFAULT_SERVER_PORT = 15640; // available port
	private static int TIMEOUT = 3000;

	/**
	 * send a request to registry and look up the ror.
	 * 
	 * @param key
	 *            name the client is calling with
	 * @return the remote object reference
	 */
	public static RemoteObjectRef lookup(String key) {
		Object readObject = null;
		Class<?> readClass = null;
		Socket sock = null;
		try {
			sock = new Socket(DEFAULT_SERVER_IP, DEFAULT_REGISTRY_PORT);
			ObjectOutputStream outStream = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(
					sock.getInputStream());
			outStream.writeObject(key);
			readObject = inStream.readObject();
			readClass = readObject.getClass();
			if (readClass == RemoteObjectRef.class) {
				return (RemoteObjectRef) readObject;
			} else {
				System.out.println("No Ref");
				return null;
			}
		} catch (Exception e) {
			System.out.println("lookup error");
			e.printStackTrace();
			return null;
		} finally {
			if (!sock.isClosed())
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * register a remote object on the server with remote reference table by
	 * sending a "rebind" request. It will firstly create a remote reference for
	 * the object and add it to the object table on the dispatcher.
	 * 
	 * @param p
	 *            dispatcher on the server, maintaining a remote object table
	 * @param key
	 *            method name
	 * @param object
	 *            remote object
	 */
	public static void rebind(ProxyDispatcher p, String key, Remote440 object) {
		p.addObj(key, object);
		try {
			String className = object.getClass().getInterfaces()[0].getName();
			RemoteObjectRef ror = new RemoteObjectRef(DEFAULT_SERVER_IP,
					DEFAULT_SERVER_PORT, key, className);
			Socket sock = new Socket(DEFAULT_SERVER_IP, DEFAULT_REGISTRY_PORT);
			ObjectOutputStream out = new ObjectOutputStream(
					sock.getOutputStream());
			out.writeObject(ror);
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getDEFAULT_SERVER_IP() {
		return DEFAULT_SERVER_IP;
	}

	public static void setDEFAULT_SERVER_IP(String dEFAULT_SERVER_IP) {
		DEFAULT_SERVER_IP = dEFAULT_SERVER_IP;
	}

	public static int getDEFAULT_REGISTRY_PORT() {
		return DEFAULT_REGISTRY_PORT;
	}

	public static void setDEFAULT_REGISTRY_PORT(int dEFAULT_REGISTRY_PORT) {
		DEFAULT_REGISTRY_PORT = dEFAULT_REGISTRY_PORT;
	}

	public static int getDEFAULT_SERVER_PORT() {
		return DEFAULT_SERVER_PORT;
	}

	public static void setDEFAULT_SERVER_PORT(int dEFAULT_SERVER_PORT) {
		DEFAULT_SERVER_PORT = dEFAULT_SERVER_PORT;
	}

	public static int getTIMEOUT() {
		return TIMEOUT;
	}

	public static void setTIMEOUT(int tIMEOUT) {
		TIMEOUT = tIMEOUT;
	}

}
