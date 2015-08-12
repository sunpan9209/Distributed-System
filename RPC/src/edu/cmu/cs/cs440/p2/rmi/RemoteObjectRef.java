package edu.cmu.cs.cs440.p2.rmi;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import edu.cmu.cs.cs440.p2.client.RemoteStub;

/**
 * remote object representation, contain information for locating the remote
 * object.
 */
public class RemoteObjectRef implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8018249739293873575L;

	private String address;
	private int port;
	private String name;
	private String key;

	public RemoteObjectRef(String address, int port, String key, String name) {
		this.address = address;
		this.key = key;
		this.name = name;
		this.port = port;
	}

	/**
	 * Create a stub for the client used for communicating with the server.
	 * 
	 * @return remote object interface
	 */
	public Object localise() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		RemoteStub stub = new RemoteStub(this.address, this.port, 3000,
				this.key);
		Class<?> ifClass = Class.forName(this.name);
		Object o = Proxy.newProxyInstance(ifClass.getClassLoader(),
				new Class[] { ifClass }, stub);
		return o;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
