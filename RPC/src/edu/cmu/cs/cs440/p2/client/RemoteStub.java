package edu.cmu.cs.cs440.p2.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import edu.cmu.cs.cs440.p2.message.RMIMessage;

/**
 * client-side stub marshalling/unmarshalling and communications using the JAVA
 * reflection mechanism.
 */
public class RemoteStub implements InvocationHandler {

	private String serverIP;
	private int serverPort;
	private int timeout;
	private String key;

	public RemoteStub(String ip, int port, int timeout, String key) {
		this.serverPort = port;
		this.serverIP = ip;
		this.timeout = timeout;
		this.key = key;
	}

	/**
	 * send RMIMessage and invoke remote method, the return value should the
	 * result of the invoked method including exceptions.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Class<?>[] argTypes = new Class<?>[args == null ? 0 : args.length];
		for (int i = 0; i < argTypes.length; i++) {
			argTypes[i] = args[i].getClass();
		}
		RMIMessage msg = new RMIMessage(method.getName(), this.key, args,
				argTypes);
		RMIMessage response = RMIMessage.sendRequest(this.serverIP,
				this.serverPort, this.timeout, msg);
		if (response == null) {
			return null;
		}
		if (response.getException() != null)
			throw response.getException();
		return response.getReturnVal();
	}

}
