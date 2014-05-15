#!/bin/sh

a=results/10-threads-1000-pings-with-session-cache.csv
b=results/10-threads-1000-pings-no-session-cache.csv
out=results/diff.png

gnuplot <<EOF
set datafile separator ","
set term pngcairo size 1280,960
set output "$out"
set title "ping round trip time during soak test"
set xlabel "time into test run (ms)"
set ylabel "ping rtt (ms)"
plot "$a", "$b"
EOF
