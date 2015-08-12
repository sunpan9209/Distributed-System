package edu.cmu.cs440.p3.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.cmu.cs440.p3.communication.Message;
import edu.cmu.cs440.p3.configuration.Config;

/**
 * serves as the entry and terminal of the master process
 */
public class MasterTerminal {
	private static Master master;
	private static Config config;

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Usage: Master <Properties File>");
			System.exit(0);
		}
		config = new Config(args[0]);
		master = new Master(config);
		MasterListener server = new MasterListener(master, config);
		server.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			try {
				System.out.print("=> ");
				String input = reader.readLine();
				if (input == null)
					continue;
				String[] cmdLine = input.split(" ");
				if (cmdLine[0].equals("submit")) {
					if (cmdLine.length != 5) {
						System.out
								.println("Usage: submit <MapClass> <InputFile> <ReduceClass> <OutputFolder>");
						continue;
					}
					if (!new File(cmdLine[2]).exists()) {
						System.out.println(cmdLine[2] + " doesn't exist!");
						continue;
					}

					// send out the message
					Message msg = new Message(Message.TYPE.SUBMIT);
					msg.setMsg(input.substring(input.indexOf(" ") + 1));
					msg.setId("Master");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Message responseMsg = Message.sendRequest(
										config.getMasterAddr(),
										config.getMasterPort(), 0, msg);
								if (responseMsg.getType() == Message.TYPE.SUBMIT) {
									System.out.println(responseMsg.getMsg());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
				// start command
				else if (cmdLine[0].equals("start")) {
					Message msg = new Message(Message.TYPE.START);
					msg.setMsg("START");
					Message responseMsg = Message.sendRequest(
							config.getMasterAddr(), config.getMasterPort(),
							5000, msg);
					System.out.println(responseMsg.getMsg());
				}
				// stop command
				else if (cmdLine[0].equals("stop")) {
					Message msg = new Message(Message.TYPE.STOP);
					msg.setMsg("STOP");
					Message responseMsg = Message.sendRequest(
							config.getMasterAddr(), config.getMasterPort(),
							5000, msg);
					System.out.println(responseMsg.getMsg());
				}
				// monitor command
				else if (cmdLine[0].equalsIgnoreCase("monitor")) {
					Message msg = new Message(Message.TYPE.MONITOR);
					msg.setMsg("Master");
					Message responseMsg = Message.sendRequest(
							config.getMasterAddr(), config.getMasterPort(),
							5000, msg);
					System.out.println(responseMsg.getMsg());
				}
				// quit
				else if (cmdLine[0].equalsIgnoreCase("quit")) {
					server.stopServer();
					master.shutdown();
					System.exit(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
	}
}
