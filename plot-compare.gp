#!/bin/sh

a=results/100-threads-10000-pings-with-session-cache.csv
a1=results/100-threads-10000-pings-with-session-cache-server-cpu.csv
b=results/100-threads-10000-pings-no-session-cache.csv
b1=results/100-threads-10000-pings-no-session-cache-server-cpu.csv
c=results/100-threads-10000-pings-plaintext.csv
c1=results/100-threads-10000-pings-plaintext-server-cpu.csv
out=results/diff.png

gnuplot <<EOF
set datafile separator ","
set term pngcairo size 1280,960
set output "$out"
set multiplot
set size 1,0.8
set origin 0,0.2
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
set xrange [] writeback
plot "$a" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtt cache",\
     "$b" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtt no cache",\
     "$c" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtt no ssl"

set size 1,0.2
set origin 0,0
set title "server cpu usage % time during soak test"
set ylabel "cpu %"
set xlabel
set xrange restore
plot "$a1" using 1:4 title "cpu % cache",\
     "$b1" using 1:4 title "cpu % no cache",\
     "$c1" using 1:4 title "cpu % no ssl"
unset multiplot     
EOF
