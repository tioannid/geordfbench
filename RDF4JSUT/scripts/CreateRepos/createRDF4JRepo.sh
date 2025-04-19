# SYNTAX :
#    <script> repoDir repoId rmRepo repoIndexes RDFFileType RDFDir -Xmx EnableLuceneSail wktIdxList RptDaemonIP RptDaemonPort
SCRIPT_NAME=`basename "$0"`
SYNTAX="
SYNTAX: $SCRIPT_NAME <repoDir> <repoId> <rmRepo> <repoIndexes> <RDFFileType> <RDFDir> <-Xmx> <EnableLuceneSail> <wktIdxList> <RptDaemonIP> <RptDaemonPort>
\t<repoDir>\t:\tdirectory where repo will be stored (usually the base dir),
\t<repoId>\t:\trepository Id,
\t<rmRepo>\t:\tremove repository if it exists,
\t<repoIndexes>\t:\tindexes to create for the repo,
\t<RDFFileType>\t:\tRDF file type,
\t<RDFDir>\t:\tdirectory for RDF triple files to load,
\t<-Xmx>\t\t:\tJVM max memory e.g. -Xmx6g
\t<EnableLuceneSail>\t:\thas Lucene support
\t<wktIdxList>\t:\tWKT Index List,
\t<RptDaemonIP>\t:\treport daemon IP,
\t<RptDaemonPort>\t:\treport daemon port"

MAP_CONTEXTS_FILE="${GeoRDFBenchScriptsDir}/map_to_contexts.txt"

# STEP 0: Find the directory where the script is located in
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# != 11 )); then
    echo -e "Illegal number of parameters $SYNTAX"
	exit 1
fi

#      1.2: assign arguments to variables
RepoDir=$1
#echo $RepoDir
RepoID=$2
#echo $RepoID
RmRepo=${3^^}
#echo $RmRepo
RepoIndexes=$4
#echo $RepoIndexes
RDFFileType=${5^^}
#echo $RDFFileType
RDFDir=$6
#echo $RDFDir
JVM_Xmx=$7
#echo $JVM_Xmx
EnableLuceneSail=$8
#echo $EnableLuceneSail
WKTIdxList=$9
#echo $WKTIdxList
RptDaemonIP=${10}
#echo ${RptDaemonIP}
RptDaemonPort=${11}
#echo ${RptDaemonPort}

#      1.3: check whether the directory (<RDFDir>) do not exist
dirs=(  "$RDFDir" )
for dir in "${dirs[@]}"; do
	if [ ! -d "$dir" ]; then
		echo -e "Directory \"$dir\" does not exist.\nNo N-Triple files to load!"
		exit 2
	fi		
done

#      1.4: check whether the directory (<repoDir>) exists
if [ "$RmRepo" == "FALSE" ]
then
dirs=(  "${RepoDir}/repositories/${RepoID}" )
for dir in "${dirs[@]}"; do
	if [ -d "$dir" ]; then
		echo -e "A repository might already exist in directory \"$dir\".\nRemove it manually"
		exit 3
	fi		
done
fi

# STEP 2: Prepare options for LOG4J, JAVA VM, CLASS PATH and MAIN CLASS		

# define the configuration file for the Apache LOG4J framework, which is common
# for all Geographica and is located in the Runtime module
LOG4J_CONFIGURATION=${BASE}/../../../runtime/src/main/resources/log4j.properties
#echo "LOG4J_CONFIGURATION = $LOG4J_CONFIGURATION"

# define the JVM options/parameters
RDF4J_DEBUG="-Dorg.eclipse.rdf4j.repository.debug=true"
JAVA_OPTS="${JVM_Xmx} -Dlog4j.configuration=file:${LOG4J_CONFIGURATION} ${RDF4J_DEBUG}"
#echo "JAVA_OPTS = $JAVA_OPTS"

# change to the ../target directory to more easily create the classpath
cd ${BASE}/../../target
# define the class path
CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"

# define the executing-main class
MAIN_CLASS="gr.uoa.di.rdf.geordfbench.rdf4jsut.RepoUtil"

# define the run command arguments to CREATE REPO
CREATE_REPO_ARGS="createman \"$RepoDir\" \"$RepoID\" \"$RmRepo\" \"$EnableLuceneSail\" \"$RepoIndexes\" \"$WKTIdxList\""
echo "CREATE_REPO_ARGS = ${CREATE_REPO_ARGS}"
EXEC_CREATE_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS ${CREATE_REPO_ARGS}" 
#echo $EXEC_CREATE_REPO
#exit 0;

# STEP 4: For the directory $RDFDir create TRIG from N-Triple files
#      4.1: change working directory to $NtripleDir
cd $RDFDir
#      4.2: if $RDFFileType is TRIG, convert N-Triples to TRIG
if [ "${RDFFileType}" = "TRIG" ]; then
    #      4.2.1: check if $MAP_CONTEXTS_FILE file exists
    if [ -e $MAP_CONTEXTS_FILE ]; then
            MapToContextFile_Exists=1
    else
            MapToContextFile_Exists=0
    fi
    #      4.2.2: For each N-Triple file in the $datafir do ...
    for i in *.nt; do 
    #      4.2.3: convert N-Triple file to TRIG with default graph using the rdf2rdf-1.0.1-2.3.1.jar program
            filename=$(basename "$i"); 
            extension="${filename##*.}"; 
            fname="${filename%.*}"; 
            trigfilename="${fname}.trig"; 
            if [ ! -f $trigfilename ]; then 
                java -jar "${BASE}/rdf2rdf-1.0.1-2.3.1.jar" $filename $trigfilename;
        #      4.2.4: if $MapToContextFile_Exists set the corresponding graph IRI in the TRIG file
                if [ $MapToContextFile_Exists -eq 1 ]; then
                        matchedline=`grep -e $i $MAP_CONTEXTS_FILE`
                        # echo "File $i found in file $MAP_CONTEXTS_FILE in line : $matchedline"
                        matchedcontext=`echo -e "$matchedline" | awk -F"\t" ' { printf $2 }'`
                        # echo "Corresponding context is : $matchedcontext"
                        sed -i 's+{$+'${matchedcontext}' {+' $trigfilename
                        echo "Modified graph name in TRIG file $trigfilename to $matchedcontext"
                fi
            else
                echo "TRIG file \"$trigfilename\" already exists!" 
            fi
    done
fi

# change to the ../target directory
cd ${BASE}/../../target
# define the run command arguments to LOAD REPO
LOAD_REPO_ARGS="dirloadman \"$RepoDir\" \"$RepoID\" \"$RDFFileType\" \"$RDFDir\" true"
echo "LOAD_REPO_ARGS = ${LOAD_REPO_ARGS}"
# define the run command to LOAD RDF FILES FROM DIR TO REPO
EXEC_LOAD_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS ${LOAD_REPO_ARGS}"
#echo $EXEC_LOAD_REPO

# execute commands - Create repo
eval ${EXEC_CREATE_REPO}
# send completion report signal to listening daemon
# both IP=${RptDaemonIP} and Port=${RptDaemonPort} depend on the daemon setup
logEntry="RDF4J repo \"${RepoID}\" creation completed at "`date --iso-8601='seconds'`
#nc ${RptDaemonIP} ${RptDaemonPort} <<< ${logEntry}

# execute commnads - Load repo
eval ${EXEC_LOAD_REPO}
# send completion report signal to listening daemon
# both IP=${RptDaemonIP} and Port=${RptDaemonPort} depend on the daemon setup
logEntry="RDF4J repo \"${RepoID}\" loading completed at "`date --iso-8601='seconds'`
#nc ${RptDaemonIP} ${RptDaemonPort} <<< ${logEntry}


# print repository size in MB
echo -e "RDF4J repository \"${RepoDir}/repositories/${RepoID}\" has size: `du -hs -BM ${RepoDir}/repositories/${RepoID} | cut -d 'M' -f 1`MB"
exit 0
