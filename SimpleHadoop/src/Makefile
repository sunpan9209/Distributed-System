all:
	javac edu/cmu/cs440/p3/*/*.java
	jar cf dslab.jar *

master:
	java -cp dslab.jar edu.cmu.cs440.p3.master.MasterTerminal edu/cmu/cs440/p3/configuration/config.properties

worker_1:
	java -cp dslab.jar edu.cmu.cs440.p3.worker.WorkerTerminal worker_1 edu/cmu/cs440/p3/configuration/config.properties

worker_2:
	java -cp dslab.jar edu.cmu.cs440.p3.worker.WorkerTerminal worker_2 edu/cmu/cs440/p3/configuration/config.properties

worker_3:
	java -cp dslab.jar edu.cmu.cs440.p3.worker.WorkerTerminal worker_3 edu/cmu/cs440/p3/configuration/config.properties

clean:
	rm -rf edu/cmu/cs440/p3/*/*.class
	rm -r tmp
	rm dslab.jar
