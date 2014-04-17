.PHONY: all clean doc release
BASE=.
PSDIR=$(BASE)/general


compile: clean
	javac -Xlint -g -cp "$(PSDIR)/djep-1.0.0.jar:$(PSDIR)/jep-2.3.0.jar:$(PSDIR)/peersim-1.0.5.jar:$(PSDIR)/colt.jar" `find src/ -iname "*.java"`
	cd src && jar -cf bbones.jar `find . -name "*.class"` && mv bbones.jar ..  && cd ..

clean:
	rm -rf doc *.txt
	rm -f `find . -iname "*.class"`
	rm -f output.log

release: clean compile

run: release
	time java -Xmx2g -cp "$(PSDIR)/djep-1.0.0.jar:$(PSDIR)/jep-2.3.0.jar:bbones.jar:$(PSDIR)/peersim-1.0.5.jar:$(PSDIR)/colt.jar" peersim.Simulator ./general/config_file.cfg #> output.log 2>&1
