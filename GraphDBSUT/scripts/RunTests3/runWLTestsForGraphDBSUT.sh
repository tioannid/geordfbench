#!/bin/bash

# SYNTAX :
#    <script> JVM_Xmx MAIN_CLASS_ARGS
# EXAMPLE :
# <script> -Xmx24g -rbd GraphDB_Repos -expdesc 175#_2023-02-25_GraphDBSUT_Run_Scal10K 
#          -wl /home/tioannid/NetBeansProjects/PhD/geordfbench/json_defs/workloads/scalabilityFunc10K_WLoriginal.json 
#          -h /home/tioannid/NetBeansProjects/PhD/geordfbench/json_defs/hosts/ubuntu_vma_tioaHOSToriginal_1.json 
#          -rs /home/tioannid/NetBeansProjects/PhD/geordfbench/json_defs/reportspecs/simplereportspec_original.json 
#          -rpsr /home/tioannid/NetBeansProjects/PhD/geordfbench/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json
SCRIPT_NAME=`basename "$0"`
SYNTAX1="SYNTAX 1: $SCRIPT_NAME <JVM_Xmx> <MAIN_CLASS_ARGS>"
SYNTAX="$SYNTAX1
\t<JVM_Xmx>\t\t:\tJVM max memory, e.g. -Xmx6g
\t<MAIN_CLASS_ARGS>\t:\tmain class arguments

EXAMPLE=
-Xmx8g
\t...maximum heap size for the Java application
-rbd \t\t'~/Downloads/GraphDB_Repos'
\t...repos base directory
-expdesc \t'175#_2023-02-25_RDF4JSUT_Run_Scal10K'
\t...experiment short description
-ds \t\t'~/json_defs/datasets/scalability_10Koriginal.json'
\t...dataset JSON specification file
-wl \t\t'~/json_defs/workloads/scalabilityFunc10K_WLoriginal.json'
\t...workload JSON specification file
-qif \t\t'0,1,2'
\t...query inclusion filter (list of query indexes to use)
-qef \t\t'0,2'
\t...query exclusion filter (list of query indexes to exclude)
-h \t\t'~/json_defs/hosts/ubuntu_vma_tioaHOSToriginal.json'
\t...host JSON specification file
-rs \t\t'~/json_defs/reportspecs/simplereportspec_original.json'
\t...report specification JSON file
-rpsr \t\t'~/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json'
\t...reportsource specification JSON file"

if (( $# < 2 )); then
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters! $SYNTAX"
	exit 1   
fi

# find the directory where the script is located in
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#echo "BASE = $BASE"

# define the configuration file for the Apache LOG4J framework, which is common
# for all Geographica and is located in the Runtime module
LOG4J_CONFIGURATION=${BASE}/../../../runtime/src/main/resources/log4j.properties
#echo "LOG4J_CONFIGURATION = $LOG4J_CONFIGURATION"

if [ -z ${GraphDBBaseDir+x} ]; then
    echo "${SCRIPT_NAME}: The following required environment variable {GraphDBBaseDir} is not set";
    echo "Please set it, like, e.g.: export GraphDBBaseDir=\"/data\""
    exit 1    # return instead of exit because we need to source the script
fi

# define the JVM options/parameters
#JAVA_OPTS="${1} -Dlog4j.configuration=file:${LOG4J_CONFIGURATION} -Dgraphdb.extra.plugins=${GraphDBBaseDir}/lib/plugins"
JAVA_OPTS="${1} -Dlog4j.configuration=file:${LOG4J_CONFIGURATION} -Dgraphdb.dist=${GraphDBBaseDir}"
#echo "JAVA_OPTS = $JAVA_OPTS"

# remove the 1st argument so that only the MAIN_CLASS_ARGS remain
shift

# change to the ../target directory to more easily create the classpath
cd ${BASE}/../../target
# define the class path
CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)classes:runtime/src/main/resources/timestamps.txt"

# define the executing-main class
MAIN_CLASS="gr.uoa.di.rdf.geordfbench.graphdbsut.RunGraphDBExperimentWorkload"

# run experiment
START_TIME=`date`
# set the ARGS
ARGS="$@"
echo "ARGS = $ARGS"
# define the run command
EXEC="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS ${ARGS}"
# echo "EXEC = ${EXEC}"

# record start time of experiment
EXPIR_START_TIME=$SECONDS

eval ${EXEC}

# record end time of experiment
DURATION_SECS=$(($SECONDS-$EXPIR_START_TIME))
DURATION_HOURS=$(($DURATION_SECS / 3600))
DURATION_REMAINING_SECS=$(($DURATION_SECS % 3600))
echo "-------------------------------" >> geographica.log
echo "Experiment Duration : $DURATION_HOURS hours $((DURATION_REMAINING_SECS / 60)) min and $((DURATION_REMAINING_SECS % 60)) secs" >> geographica.log

# record hardware description used by the experiment
echo "-------------------------------" >> geographica.log
echo "Host used : $HOSTNAME" >> geographica.log

CPU_INFO=`lscpu`
echo "-------------------------------" >> geographica.log
echo "CPU Used " >> geographica.log
echo $CPU_INFO >> geographica.log
MEM_INFO=`cat /proc/meminfo`
echo "-------------------------------" >> geographica.log
echo "MEMORY Info " >> geographica.log
echo $MEM_INFO >> geographica.log

mv geographica.log ../geographica.log
  
END_TIME=`date`

# print test duration
echo "Start time = $START_TIME"
echo "End time = $END_TIME"
