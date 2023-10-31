#!/bin/bash
DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#24gb ram
#$DIR/Meat 24 $[1*1024*1024]
/home/benchmark/virtuoso/bin/virtuoso-stop.sh
sync && sudo echo 3 > /proc/sys/vm/drop_caches;
/home/benchmark/virtuoso/bin/virtuoso-start.sh
sleep 1
echo "clearing cache... `date`" >> $DIR/clear.log


sleep 1;
