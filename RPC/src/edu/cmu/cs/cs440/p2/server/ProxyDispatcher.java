package edu.cmu.cs.cs440.p2.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.cmu.cs.cs440.p2.message.RMIMessage;
import edu.cmu.cs.cs440.p2.rmi.Communication;
import edu.cmu.cs.cs440.p2.rmi.Remote440;
import edu.cmu.cs.cs440.p2.test.PrintImpl;

/**
 * receive the function call, register remote objects.
 */
public class ProxyDispatcher {
	private Map<String, Object> pool;

	public ProxyDispatcher() throws IOException {
		pool = new ConcurrentHashMap<String, Object>();
	}

	/**
	 * listen for the function call from client.
	 */
	@SuppressWarnings("resource")
	public void serve() {
		try {
			ServerSocket serverSock = new ServerSocket(
					Communication.getDEFAULT_SERVER_PORT());
			Executor executor = Executors.newCachedThreadPool();
			while (true) {
				Socket sock = serverSock.accept();
				ObjectInputStream in = new ObjectInputStream(
						sock.getInputStream());
				RMIMessage incomingMsg = (RMIMessage) in.readObject();
				if (incomingMsg == null)
					continue;
				executor.execute(new ProxyThread(incomingMsg, sock));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addObj(String key, Object o) {
		pool.put(key, o);
	}

	/**
	 * invoke method concurrently and return the message.
	 */
	public class ProxyThread implements Runnable {
		private Socket sock;
		private RMIMessage incomingMessage;

		public ProxyThread(RMIMessage incomingMsg, Socket sock) {
			this.sock = sock;
			this.incomingMessage = incomingMsg;
		}

		@Override
		public void run() {
			try {
				Remote440 obj = (Remote440) pool.get(incomingMessage.getKey());
				Method method = obj.getClass().getMethod(
						incomingMessage.getMethod(),
						incomingMessage.getArgTypes());
				try {
					incomingMessage.setReturnVal(method.invoke(
							pool.get(incomingMessage.getKey()),
							incomingMessage.getArgs()));
				} catch (Exception e) {
					incomingMessage.setException(e);
				}
			} catch (SecurityException e) {
				incomingMessage.setException(e);
			} catch (NoSuchMethodException e) {
				incomingMessage.setException(e);
			} catch (IllegalArgumentException e) {
				incomingMessage.setException(e);
			} finally {
				try {
					ObjectOutputStream out = new ObjectOutputStream(
							sock.getOutputStream());
					out.writeObject(incomingMessage);
					out.flush();
					out.close();
					if (!sock.isClosed())
						sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * launch the server dispatcher.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			System.out.println("usage: ProxyDispatcher");
			return;
		}
		ProxyDispatcher dispatcher = new ProxyDispatcher();
		PrintImpl print1 = new PrintImpl();
		Communication.rebind(dispatcher, "print1", print1);
		PrintImpl print2 = new PrintImpl();
		Communication.rebind(dispatcher, "print2", print2);
		dispatcher.serve();
		System.exit(0);
	}
}
