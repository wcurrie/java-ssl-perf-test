#!/bin/sh

a=results/100-threads-10000-pings-with-session-cache.csv
a1=results/100-threads-10000-pings-with-session-cache-server-cpu.csv
b=results/100-threads-10000-pings-no-session-cache.csv
b1=results/100-threads-10000-pings-no-session-cache-server-cpu.csv
out=results/diff.png

gnuplot <<EOF
set datafile separator ","
set term pngcairo size 1280,960
set output "$out"
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
plot "$a" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtt cache",\
     "$b" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtt no cache",\
     "$a1" using 1:(\$4 == 100 ? 3000 : 1/0) title "100% cpu cache",\
     "$b1" using 1:(\$4 == 100 ? 3200 : 1/0) title "100% cpu no cache"
EOF
