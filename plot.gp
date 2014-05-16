#!/bin/sh
in=$1
out=${in%.csv}.png

gnuplot <<EOF
set datafile separator ","
set term pngcairo size 1280,960
set output "$out"
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
plot "$in" using 1:(\$2 == -1 ? 1/0 : \$2) title "rtts", \
     "$in" using 1:(\$2 == -1 ? 0 : 1/0) title "errors"  
EOF
