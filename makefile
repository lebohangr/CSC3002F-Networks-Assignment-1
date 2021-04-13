# makefile

JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

SRCDIR=src
BINDIR=bin

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES= Session.class Logger.class Client.class SenderThread.class ReceiverThread.class Server.class 
CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

runClient:
	java -cp bin/ Client

runServer:
	java -cp bin/ Server


clean:
	rm $(BINDIR)/*.class

runDoc: 
	javadoc ./src/*.java -d ./doc
doc_clean:
	rm -rf $(DOC)
