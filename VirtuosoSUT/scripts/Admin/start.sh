#! /bin/bash

# Starts the Virtuoso Server instance

# Read parameter with the server path
binPath=${1}
# echo "binPath = ${binPath}"
dbPath=${2}
# echo "dbPath = ${dbPath}"
delaySecs=${3}
# echo "delaySecs = ${delaySecs}"

# from http://vos.openlinksw.com/owiki/wiki/VOS/VirtRDFPerformanceTuning#Linux-only%20--%20%22swappiness%22
sudo /sbin/sysctl -w vm.swappiness=10 2>&1 > /dev/null 

# retain the current working directory
cwd=`pwd`
echo "cwd = ${cwd}"
# change working directory to database folder
cd ${dbPath}
echo "changed working directory to ${dbPath}"
# start virtuoso server
${binPath}/virtuoso-t

# MUST WAIT, otherwise the client connections WILL FAIL!
sleep ${delaySecs}

# restore working directory
cd $cwd
echo "changed working directory back to ${cwd}"