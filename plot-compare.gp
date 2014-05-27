#!/bin/sh

prefix=$1

server_cpu=${in%.csv}-server-cpu.csv
client_cpu=${in%.csv}-client-cpu.csv
a=results/${prefix}-with-session-cache.csv
a1=${a%.csv}-server-cpu.csv
a2=${a%.csv}-client-cpu.csv
b=results/${prefix}-no-session-cache.csv
b1=${b%.csv}-server-cpu.csv
b2=${b%.csv}-client-cpu.csv
c=results/${prefix%-*}-plaintext.csv
c1=${c%.csv}-server-cpu.csv
c2=${c%.csv}-client-cpu.csv
out=results/diff.png

gnuplot <<EOF
set datafile separator ","
set term pngcairo dashed size 1280,960
set output "$out"
set multiplot
set size 1,0.8
set origin 0,0.2
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
set xrange [] writeback
plot "$a" using 4:(\$2 == -1 ? 1/0 : \$2):1:4 title "rtt cache" with xerrorbars,\
     "$b" using 4:(\$2 == -1 ? 1/0 : \$2):1:4 title "rtt no cache" with xerrorbars,\
     "$c" using 4:(\$2 == -1 ? 1/0 : \$2):1:4 title "rtt no ssl" with xerrorbars

set size 1,0.2
set origin 0,0
set title "server cpu usage % time during soak test"
set ylabel "cpu %"
set xlabel
set xrange restore
plot "$a1" using 1:4 title "server cpu % cache",\
     "$b1" using 1:4 title "server cpu % no cache",\
     "$c1" using 1:4 title "server cpu % no ssl"
unset multiplot     
EOF
