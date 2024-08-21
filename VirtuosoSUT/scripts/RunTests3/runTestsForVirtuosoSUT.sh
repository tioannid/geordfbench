#!/bin/bash

# SYNTAX :
#    <script> JVM_Xmx MAIN_CLASS_ARGS 

SCRIPT_NAME=`basename "$0"`
SYNTAX1="SYNTAX 1: $SCRIPT_NAME <JVM_Xmx> <MAIN_CLASS_ARGS>"
SYNTAX="$SYNTAX1
\t<JVM_Xmx>\t\t:\tJVM max memory, e.g. -Xmx6g
\t<MAIN_CLASS_ARGS>\t:\tmain class arguments

EXAMPLE=
-Xmx8g
\t...maximum heap size for the Java application
-surl \t\t'http://localhost:1111'
\t...Virtuoso server URL
-susr \t\tdba
\t...Virtuoso server user name
-spwd \t\tdba
\t...Virtuoso server user password
-rbd \t\t'virtuoso-opensource/repos'
\t...repos base directory
-expdesc \t'180#_2023-03-05_VirtuosoSUT_ScalFunc_10K'
\t...experiment short description
-ds \t\t'~/json_defs/datasets/scalability_10Koriginal.json'
\t...dataset JSON specification file
-h \t\t'~/json_defs/hosts/ubuntu_vma_tioaHOSToriginal_1.json'
\t...host JSON specification file
-rs \t\t'~/json_defs/reportspecs/simplereportspec_original.json'
\t...report specification JSON file
-rpsr \t\t'~/json_defs/reportsources/ubuntu_vma_tioaRepSrcoriginal.json'
\t...reportsource specification JSON file
-es \t\t'~/json_defs/reportspecs/scalabilityESoriginal.json'
\t...execution specification JSON file
-qs \t\t'~/json_defs/querysets/scalabilityFuncQSoriginal.json'
\t...queryset JSON specification file
-qif \t\t'0,1,2'
\t...query inclusion filter (list of query indexes to use)
-qef \t\t'0,2'
\t...query exclusion filter (list of query indexes to exclude)"

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

# define the JVM options/parameters
JAVA_OPTS="-Dlog4j.configuration=file:${LOG4J_CONFIGURATION}"
#echo "JAVA_OPTS = $JAVA_OPTS"

# remove the 1st argument so that only the MAIN_CLASS_ARGS remain
shift

# change to the ../target directory to more easily create the classpath
cd ${BASE}/../../target
# define the class path
CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)runtime/src/main/resources/timestamps.txt"

# define the executing-main class
MAIN_CLASS="gr.uoa.di.rdf.Geographica3.virtuososut.RunVirtuosoExperiment"

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
