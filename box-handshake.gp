set term pngcairo dashed size 1280,960
set output "results/handshake-boxplot.png"

set style fill solid 0.25 border -1
set style boxplot outliers pointtype 7
set style data boxplot
set boxwidth  0.5
set pointsize 0.5

unset key
set border 2
set xtics ("native cache" 1, "native no cache" 2,"jsse cache" 3, "jsse no cache" 4) scale 0.0
set xtics nomirror
set ytics nomirror

set datafile separator ","
set title "time to complete SSL handshake (ms)"

plot 'results/100-threads-10000-pings-tcnative-with-session-cache-handshake-timing.csv' using (1):3, \
     'results/100-threads-10000-pings-tcnative-no-session-cache-handshake-timing.csv' using (2):3, \
     'results/100-threads-10000-pings-jsse-with-session-cache-handshake-timing.csv' using (3):3, \
     'results/100-threads-10000-pings-jsse-no-session-cache-handshake-timing.csv' using (4):3
