all:	
	javac edu/cmu/cs/cs440/p2/rmi/*.java edu/cmu/cs/cs440/p2/client/*.java edu/cmu/cs/cs440/p2/message/*.java edu/cmu/cs/cs440/p2/server/*.java edu/cmu/cs/cs440/p2/test/*.java

demo: all
	@echo "Running Registry server...\n"
	java edu.cmu.cs.cs440.p2.server.RMIRegistry &
	sleep 1
	@echo "Running Proxy Dispatcher...\n"
	java edu.cmu.cs.cs440.p2.server.ProxyDispatcher &
	sleep 1
	@echo "Test program running...\n"
	java edu.cmu.cs.cs440.p2.test.PrintClient 

clean:
	rm edu/cmu/cs/cs440/p2/rmi/*.class edu/cmu/cs/cs440/p2/client/*.class edu/cmu/cs/cs440/p2/message/*.class edu/cmu/cs/cs440/p2/server/*.class edu/cmu/cs/cs440/p2/test/*.class

