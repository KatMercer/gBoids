JC=javac
.SUFFIXES: .java .class
SOURCES=*.java
OBJECTS=$(SOURCES:.java=.class)

default: $(OBJECTS)

.java.class:
	$(JC) $*.java

clean:	
	rm -f *.class
	touch *
	ls

handin: clean
	handin cmsc280 lab3 ../lab3

