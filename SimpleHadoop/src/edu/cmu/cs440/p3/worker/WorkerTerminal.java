package edu.cmu.cs440.p3.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.cmu.cs440.p3.communication.Message;
import edu.cmu.cs440.p3.configuration.Config;

/**
 * serves as the entry and terminal of the worker process
 */
public class WorkerTerminal {
	private static Worker worker;
	private static Config config;
	private static BufferedReader reader;
	private static boolean running = true;
	private static WorkerListener server;

	/**
	 * stop the worker
	 */
	public static void stopProgram() {
		running = false;
		try {
			reader.close();
		} catch (Exception e) {
		}
		server.stopWorker();
		System.exit(0);
	}

	/**
	 * ask to join the facility
	 */
	private static void register(String workerID) {
		Message msg = new Message(Message.TYPE.REGISTER);
		msg.setMsg(workerID);
		try {
			Message.sendRequest(config.getMasterAddr(), config.getMasterPort(),
					5000, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Usage: Worker <Worker ID> <Properties File>");
			System.exit(0);
		}
		config = new Config(args[1]);
		worker = new Worker(config, args[0]);
		server = new WorkerListener(worker, config, args[0]);
		server.start();
		register(args[0]);
		reader = new BufferedReader(new InputStreamReader(System.in));
		while (running) {
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
					msg.setId(args[0]);
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
					msg.setMsg(args[0]);
					Message responseMsg = Message.sendRequest(
							config.getMasterAddr(), config.getMasterPort(),
							5000, msg);
					System.out.println(responseMsg.getMsg());
				}
				// quit
				else if (cmdLine[0].equalsIgnoreCase("quit")) {
					stopProgram();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
	}
}
