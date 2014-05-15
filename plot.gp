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
plot "$in"
EOF
