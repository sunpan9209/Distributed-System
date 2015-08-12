package edu.cmu.cs440.p3.configuration;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * parse the configuration properties file
 */
public class Config {

	private Properties prop = new Properties();

	private Map<String, String> workerAddrMap = new ConcurrentHashMap<String, String>();

	private Map<String, Integer> workerPortMap = new ConcurrentHashMap<String, Integer>();

	public Config(String configFile) {
		try {
			prop.load(new FileInputStream(configFile));
			String[] workers = prop.getProperty("workers").split(",");
			for (int i = 0; i < workers.length; i++) {
				workerAddrMap.put(workers[i],
						InetAddress.getByName(prop.getProperty(workers[i]))
								.getHostAddress());
				workerPortMap.put(workers[i], Integer.parseInt(prop
						.getProperty(workers[i] + "_port")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getMaxMaps() {
		return Integer.parseInt(prop.getProperty("max_maps"));
	}

	public int getMaxReduces() {
		return Integer.parseInt(prop.getProperty("max_reduces"));
	}
	
	public int getBufferSize() {
		return Integer.parseInt(prop.getProperty("buffer_size"));
	}

	public String getMasterAddr() {
		try {
			return InetAddress.getByName(prop.getProperty("master"))
					.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getMasterPort() {
		return Integer.parseInt(prop.getProperty("master_port"));
	}

	public Map<String, String> getClientAddrs() {
		return workerAddrMap;
	}

	public String getClientAddr(String clientID) {
		return getClientAddrs().get(clientID);
	}

	public Map<String, Integer> getClientPorts() {
		return workerPortMap;
	}

	public int getClientPort(String clientID) {
		return getClientPorts().get(clientID);
	}

}
