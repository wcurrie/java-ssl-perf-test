#!/bin/sh
in=$1
out=${in%.csv}.png
cpu=${in%.csv}-server-cpu.csv

gnuplot <<EOF
set datafile separator ","
set term pngcairo size 1280,960
set output "$out"
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
set y2tics
set y2label "cpu load"
plot "$in" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtts", \
     "$in" using 1:(\$2 == -1 ? 0 : 1/0) title "errors", \
     "$cpu" using 1:4 title "cpu load" axes x1y2
EOF
