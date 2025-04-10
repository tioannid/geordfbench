#!/bin/bash
CWD=`pwd`
cd ../scripts
set -o allexport # or we could use: set -a
source prepareRunEnvironment.sh VM GraphDBSUT GraphDB_Scal10K_`date -I`
set +o allexport # or we could use: set +a
./printRunEnvironment.sh
cd ${CWD}/scripts/CreateRepos
./createAllGraphDBRepos.sh
