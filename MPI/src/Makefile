JC = mpijavac
RMI = rmic
ECHO = (echo)
JFLAGS = -g -cp /usr/local/lib/openmpi/lib/mpi.jar:.

.SUFFIXES: .java .class
.java.class:
	@$(JC) $(JFLAGS) $*.java

CLASSES = \
	Data.java \
	Point.java \
	SequentialKMeans.java \
	ParallelKMeans.java \
	DNA.java \

default: classes 
		@echo "Compile Done"

classes: 	$(CLASSES:.java=.class)

tests:		$(TESTS:.java=.class)

clean:
	@find . -type f -name "*.class" -delete
	@echo "Clean Done"
