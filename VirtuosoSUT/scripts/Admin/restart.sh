#! /bin/bash

# Restarts the Virtuoso Server instance

# Read parameter with the server path
binPath=${1}
dbPath=${2}
delaySecs=${3}

# stop Virtuoso server
./stop.sh

# start Virtuoso server
./start.sh ${binPath} ${dbPath} ${delaySecs} 