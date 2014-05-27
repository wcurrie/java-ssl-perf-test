#!/bin/sh
in=$1
out=${in%.csv}.png
server_cpu=${in%.csv}-server-cpu.csv
client_cpu=${in%.csv}-client-cpu.csv
handshakes=${in%.csv}-handshake-timing.csv

/opt/local/bin/gnuplot <<EOF
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
plot "$in" using 4:(\$2 == -1 ? 1/0 : \$2):1:4 title "rtts" with xerrorbars lt 3, \
     "$handshakes" using 2:3:1:2 title "handshakes" with xerrorbars lt 4, \
     "$in" using 1:(\$2 == -1 ? 0 : 1/0) title "errors"

set size 1,0.2
set origin 0,0
set title "server cpu usage % time during soak test"
set ylabel "cpu %"
set xlabel
set xrange restore
set yrange [0:100]
plot "$server_cpu" using 1:4 title "server cpu",\
     "$client_cpu" using 1:4 title "client cpu"
EOF
