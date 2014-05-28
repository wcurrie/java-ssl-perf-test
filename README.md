Ping pong between a server and set of clients to compare plaintext, ssl with session ids and a possible worst case of new session for every message.

Simple test setup is just two laptops: one running the server, one for a single JVM creating a bunch of client sockets.

The results:
![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/jsse-diff.png "")

Unsurprisingly, forcing the server to do public key crypto for every new connection keeps it busy.

Using tomcat native, things look a little better.
![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/tcnative-diff.png "")

Comparing jsse (bog standard jdk SSL) with tomcat native (a JNI bridge to openssl) we can measure:

* round trip time
* time to complete SSL handshake (observed server side)
* server cpu load

![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/rtt-boxplot.png "")

![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/handshake-boxplot.png "")

![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/cpu-boxplot.png "")
