JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	edu/cmu/cs/cs440/p1/example/Code.java \
	edu/cmu/cs/cs440/p1/example/FileZipper.java \
	edu/cmu/cs/cs440/p1/example/PrintNums.java \
	edu/cmu/cs/cs440/p1/example/GrepProcess.java \
	edu/cmu/cs/cs440/p1/IO/TransactionalFileInputStream.java \
	edu/cmu/cs/cs440/p1/IO/TransactionalFileOutputStream.java \
	edu/cmu/cs/cs440/p1/main/Main.java \
	edu/cmu/cs/cs440/p1/worker/Worker.java \
	edu/cmu/cs/cs440/p1/worker/WorkerCommunicator.java \
	edu/cmu/cs/cs440/p1/process/MigratableProcess.java \
	edu/cmu/cs/cs440/p1/process/ProcessManager.java \
	edu/cmu/cs/cs440/p1/process/ProcessSerialization.java \
	edu/cmu/cs/cs440/p1/process/ProcessStatus.java \

RMCLASSES = \
	edu/cmu/cs/cs440/p1/example/Code.class \
	edu/cmu/cs/cs440/p1/example/FileZipper.class \
	edu/cmu/cs/cs440/p1/example/PrintNums.class \
	edu/cmu/cs/cs440/p1/example/GrepProcess.class \
	edu/cmu/cs/cs440/p1/IO/TransactionalFileInputStream.class \
	edu/cmu/cs/cs440/p1/IO/TransactionalFileOutputStream.class \
	edu/cmu/cs/cs440/p1/main/Main.class \
	edu/cmu/cs/cs440/p1/worker/Worker.class \
	edu/cmu/cs/cs440/p1/worker/WorkerCommunicator.class \
	edu/cmu/cs/cs440/p1/process/MigratableProcess.class \
	edu/cmu/cs/cs440/p1/process/ProcessManager.class \
	edu/cmu/cs/cs440/p1/process/ProcessSerialization.class \
	edu/cmu/cs/cs440/p1/process/ProcessStatus.class \


default: classes

classes: $(CLASSES:.java=.class)

clean:	
	rm $(RMCLASSES)
	
