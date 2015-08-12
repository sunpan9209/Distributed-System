package edu.cmu.cs.cs440.p2.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.cs.cs440.p2.rmi.Communication;
import edu.cmu.cs.cs440.p2.rmi.RemoteObjectRef;

/**
 * maintain the remote reference table.
 */
public class RMIRegistry {
	private int serverPort;
	private Map<String, RemoteObjectRef> pool;

	public RMIRegistry(int port) {
		this.serverPort = port;
		this.pool = new ConcurrentHashMap<String, RemoteObjectRef>();
	}

	/**
	 * Start the RMIRegistry service The service receives the
	 * RemoteObjectReference from the Proxy dispatcher server and answer the
	 * look up requests from the client.
	 */
	@SuppressWarnings("resource")
	public void start() {
		ServerSocket serverSock = null;
		try {
			serverSock = new ServerSocket(serverPort);
		} catch (Exception e) {
			System.out.println("ServerSocket Error!");
			e.printStackTrace();
		}
		while (true) {
			try {
				Socket socket = serverSock.accept();
				InputStream in = socket.getInputStream();
				ObjectInputStream objIn = new ObjectInputStream(in);
				OutputStream out = socket.getOutputStream();
				ObjectOutputStream objOut = new ObjectOutputStream(out);
				Object readObject = null;
				try {
					readObject = objIn.readObject();
				} catch (ClassNotFoundException e) {
					System.out.println("no such class");
					e.printStackTrace();
				}
				Class<?> objClass = readObject.getClass();
				if (objClass == String.class) {
					String objString = (String) readObject;
					System.out.println("Looking up " + objString);
					RemoteObjectRef ror = pool.get(objString);
					if (ror == null)
						System.out.println(objString + " not found.");
					else
						System.out.println(objString + " found.");
					objOut.writeObject(ror);
				} else if (objClass == RemoteObjectRef.class) {
					RemoteObjectRef ror = (RemoteObjectRef) readObject;
					System.out.println("register " + ror.getKey());
					String key = ror.getKey();
					pool.put(key, ror);
				}
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * start RMIRegistry at either the given port or the default port
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("usage: RMIRegistry");
			return;
		}
		System.out.println("Launch RMIRegistry");
		new RMIRegistry(Communication.getDEFAULT_REGISTRY_PORT()).start();
		return;
	}
}
