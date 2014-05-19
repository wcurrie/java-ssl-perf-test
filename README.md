Ping pong between a server and set of clients to compare plaintext, ssl with session ids and a possible worst case of new session for every message.

The results:
![chart](https://raw.githubusercontent.com/wcurrie/java-ssl-perf-test/master/results/diff.png "")

Unsurprisingly, forcing the server to do public key crypto for every new connection keeps it busy.