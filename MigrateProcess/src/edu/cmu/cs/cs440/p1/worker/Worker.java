package edu.cmu.cs.cs440.p1.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.cs.cs440.p1.process.MigratableProcess;
import edu.cmu.cs.cs440.p1.process.ProcessManager;
import edu.cmu.cs.cs440.p1.process.ProcessSerialization;

/**
 * worker server
 */
public class Worker {

	private Map<Integer, MigratableProcess> pidToProcess = new ConcurrentHashMap<Integer, MigratableProcess>();
	private PrintWriter out;

	public Worker() {
	}

	public Map<Integer, MigratableProcess> getPidToProcess() {
		return pidToProcess;
	}

	/**
	 * listen to the socket and execute the command
	 * 
	 * @param hostname
	 * @throws IOException
	 */
	public void run(String hostname) throws IOException {
		Monitor monitor = new Monitor(this);
		new Thread(monitor).start();

		// connect to the process manager
		int port = ProcessManager.MANAGER_PORT;
		Socket socket = new Socket(InetAddress.getByName(hostname), port);
		System.out.println("Connected to the master");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

		// get ready for reading message
		String str = null;
		String[] args = null;
		while (true) {
			str = in.readLine();
			args = str.split(" ");

			// start new process
			if (args[0].equals("start")) {
				this.startNewProcess(args);
			}

			// suspend
			else if (args[0].equals("suspend")) {
				int migrateProcessID = -1;
				try {
					migrateProcessID = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.err.println("wrong process ID format");
					continue;
				}
				MigratableProcess mProc = this.pidToProcess
						.get(migrateProcessID);
				if (mProc == null) {
					System.err.println("wrong process ID");
					continue;
				}
				mProc.suspend();
				System.out.println("Suspended process " + migrateProcessID);
				this.pidToProcess.remove(migrateProcessID);
				ProcessSerialization.serialize(mProc, migrateProcessID);
			}

			// exit
			else if (args[0].equals("quit")) {
				System.out.println("Quit received from master");
				socket.close();
				System.exit(1);
			}
		}
	}

	/**
	 * start a new process
	 * 
	 * @param args
	 */
	private void startNewProcess(String[] args) {
		int processID = Integer.valueOf(args[1]);
		MigratableProcess mProc = ProcessSerialization.deserialize(processID);
		this.pidToProcess.put(processID, mProc);
		System.out.println("Starting Process " + processID);
		Thread thread = new Thread(mProc);
		thread.start();
	}

	/**
	 * send a worker message
	 * 
	 * @param msg
	 */
	public void sendMessage(String msg) {
		try {
			out.write(msg);
			out.flush();
		} catch (Exception e) {
			System.err.println("Command Error");
			e.printStackTrace();
		}
	}

	/**
	 * monitor the status of every process
	 */
	private class Monitor implements Runnable {

		private Worker worker;

		public Monitor(Worker worker) {
			this.worker = worker;
		}

		@Override
		public void run() {
			while (true) {
				for (Map.Entry<Integer, MigratableProcess> e : worker
						.getPidToProcess().entrySet()) {
					int pid = e.getKey();
					MigratableProcess process = e.getValue();
					if (process.isDone()) {
						worker.getPidToProcess().remove(pid);
						worker.sendMessage("done " + Integer.toString(pid)
								+ "\n");
						System.out.println("Finished process " + pid);
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
