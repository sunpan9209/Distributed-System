package edu.cmu.cs440.p3.master;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.cs440.p3.communication.Message;
import edu.cmu.cs440.p3.configuration.Config;
import edu.cmu.cs440.p3.task.Task;

/**
 * The tool frequently check the running status of the client every 5s.
 */
public class StatusChecker implements Runnable {
	private volatile boolean terminating;
	private volatile ArrayList<Thread> runningThreads;
	private boolean isAlive;
	private Map<String, List<Task>> workers;
	private Config config;

	public StatusChecker(Map<String, List<Task>> workers, Config config) {
		runningThreads = new ArrayList<Thread>();
		this.workers = workers;
		this.config = config;
	}

	@Override
	public void run() {
		isAlive = true;

		while (!terminating) {
			try {
				for (String id : workers.keySet()) {
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {

							try {
								// check the status of the worker
								Message msg = new Message(Message.TYPE.STATUS);
								msg.setMsg("QUERY");
								Message response = Message.sendRequest(
										config.getClientAddr(id),
										config.getClientPort(id), 5000, msg);
								if (response.getType() == Message.TYPE.STATUS) {
									if (response.getMsg().equals("HEALTHY")) {
										return;
									}
								}
							} catch (Exception e) {
								// no reply
								Master.exceptionHandler(id);
							}

						}

					});
					runningThreads.add(t);
					t.start();
				}

				for (Thread t : runningThreads)
					t.join();
				runningThreads.clear();

				/* issue the command every 5 sec. */
				Thread.sleep(5000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		terminating = false;
	}

	public void shutdown() throws IOException {
		if (!isAlive) {
			return;
		}
		terminating = true;
		while (terminating)
			;
		isAlive = false;
		System.out.println("StatusChecker quiting...");
	}

	public boolean isAlive() {
		return isAlive;
	}

}
