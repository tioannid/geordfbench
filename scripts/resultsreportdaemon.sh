#!/bin/bash
# Completion Report Listening Daemon
# Useful for long running experiments, such as Scalability for 100M, 500M datasets

port=${1} # listening port
logfile=${2} # report file path
reportheader=${3} # report header

# print report header with host related info
echo "${reportheader}" > ${logfile}
echo "Listening host : ${HOSTNAME}" >> ${logfile}

# start listening
#while true; do
    nc -l ${port} | while read msg; do logentrytimestampt=`date --iso-8601='seconds'`; echo "[ ${logentrytimestampt} ] - ${msg}" >> ${logfile}; done; 
#done
