#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac
JAVA = java

# define a makefile variable for compilation flags
# the -g flag compiles with debugging information
#
JFLAGS = -g -cp

JAR = java-getopt-1.0.14.jar;.

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default: DES.class

# this target entry builds the Average class
# the Average.class file is dependent on the Average.java file
# and the rule associated with this entry gives the command to create it
#
DES.class: DES.java
	$(JCC) $(JFLAGS) $(JAR) DES.java

# To start over from scratch, type 'make clean'.  
# Removes all .class files, so that the next make rebuilds them
#
clean: 
	del *.class

run-d:
	$(JAVA) -cp java-getopt-1.0.14.jar;. DES -d

run-h:
	$(JAVA) -cp java-getopt-1.0.14.jar;. DES -h 

run-k:
	$(JAVA) -cp java-getopt-1.0.14.jar;. DES -k

run-e:
	$(JAVA) -cp java-getopt-1.0.14.jar;. DES -e