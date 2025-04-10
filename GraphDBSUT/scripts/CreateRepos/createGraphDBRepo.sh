# SYNTAX :
#    <script> RepoConfig RDFDir RDFFormat GraphDBBaseDir EnableGeoSPARQLPlugin IndexingAlgorithm IndexingPrecision ReportDaemonIP ReportDaemonPort
#
SCRIPT_NAME=`basename "$0"`
SYNTAX="
SYNTAX: $SCRIPT_NAME RepoConfig RDFDir RDFFormat GraphDBBaseDir EnableGeoSPARQLPlugin IndexingAlgorithm IndexingPrecision ReportDaemonIP ReportDaemonPort
\tRepoConf\t:\tconfiguration file for repo (with the repo name!),
\tRDFDir\t:\tdirectory with the RDF data files to load,
\tRDFFormat\t:\tRDF format {N-TRIPLES | TRIG }
\tGraphDBBaseDir\t:\tbase directory of the GraphDB installation,
\tEnableGeoSPARQLPlugin\t:\ttrue|false,
\tIndexingAlgorithm\t:\tquad|geohash,
\tIndexingPrecision\t:\tquad=(1..25), geohash=(1..24),
\tReportDaemonIP\t:\treport daemon IP,
\tReportDaemonPort\t:\treport daemon port"

# Assumptions
# 1) The N-Triple files for the dataset are located in the RDFDir directory
# 2) The rdf2rdf-1.0.1-2.3.1.jar is located in the same directory as the script
# 3) The pgrep command should be available
# 4) A "maps_to_contexts.txt" file exists in ntripleDir if context/graph IRIs need to be specified in TRIG files
# 5) The repoConf file has the appropriate repository name!

MAP_CONTEXTS_FILE="${GeographicaScriptsDir}/map_to_contexts.txt"

# STEP 0: Find the directory where the script is located in
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments and assign them to variables
if (( $# != 9 )); then
    echo -e "Illegal number of parameters ${SYNTAX}"
    exit 1
fi

RepoConfig=${BASE}/$1
# echo $RepoConfig
#      1.2: extract the repository ID from the config file
RepoName=`grep -e "repositoryID" ${RepoConfig} | awk -F"\"" ' { printf $2 }'`
# echo $RepoName
RDFDir=$2
# echo $RDFDir
RDFFormat=${3^^}
# echo $RDFFormat
GraphDBBaseDir=$4
# echo $GraphDBBaseDir
EnableGeoSPARQLPlugin=${5^^}
# echo $EnableGeoSPARQLPlugin
IndexingAlgorithm=$6
# echo $tIndexingAlgorithm
IndexingPrecision=$7
#echo $tIndexingPrecision
ReportDaemonIP=$8
#echo "ReportDaemonIP = $ReportDaemonIP"
ReportDaemonPort=$9
#echo "ReportDaemonPort = $ReportDaemonPort"

#      1.3: check whether the directories (RDFDir GraphDBBaseDir) exist
dirs=(  "$RDFDir" "$GraphDBBaseDir" )
for dir in "${dirs[@]}"; do
	if [ ! -d "$dir" ]; then
		echo "Directory ${dir} does not exist!"
		exit 2
	fi		
done

#      1.4: check whether the RDF format is N-Triples or TRIG
if [ "${RDFFormat}" != "TRIG" ] && [ "${RDFFormat}" != "N-TRIPLES" ]; then
    echo "Supported RDF formats are : {N-TRIPLES | TRIG}!"
    exit 3
fi

# STEP 2: Assess whether the script's preconditions are met
#      2.1: check whether the $GraphDBBaseDir/bin/importrdf exists
ImportRDF_Exe="${GraphDBBaseDir}/bin/importrdf"
PreLoad_Exe="${GraphDBBaseDir}/bin/importrdf preload"
if [ ! -e "$ImportRDF_Exe" ]; then
	echo "File ${ImportRDF_Exe} does not exist!"
	exit 4
fi

#      2.2: check whether the repos $RepoName already exists
#      2.2.1: check if <GraphDBDataDir>/repositories contains any of the repos (repoName )
RepoDir="${GraphDBDataDir}/repositories/${RepoName}"
# echo $RepoDir 
if [ -d "$RepoDir" ]; then
	echo "Repo ${RepoName} already exists in --> ${RepoDir}"
	exit 5
fi		

# STEP 3: Terminate the GraphDB server process, if it's running
kill -9 `pgrep -f graphdb` > /dev/null 2>&1

# STEP 4: For the directory $RDFDir create TRIG from N-Triple files
#      4.1: change working directory to $RDFDir
cd $RDFDir
#      4.2: if $RDFFormat is TRIG, convert N-Triples to TRIG
if [ "${RDFFormat}" = "TRIG" ]; then
    #      4.2.1: check if $MAP_CONTEXTS_FILE file exists
    if [ -e $MAP_CONTEXTS_FILE ]; then
            MapToContextFile_Exists=1
    else
            MapToContextFile_Exists=0
    fi
    #      4.2.2: For each N-Triple file in the $RDFDir do ...
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
    #      4.2.5: Set the filetype to TRIG
    rdffiletype='.trig'
else
    #      4.2.6: Set the filetype to N-TRIPLES
    rdffiletype='.nt'
fi
# STEP 5: Create $RepoName repository with config file $RepoConfig and load RDF files while measuring the elapsed time
time $PreLoad_Exe -c $RepoConfig *$rdffiletype

# STEP 6: Remove TRIG file to avoid allocating space
#if [ "${RDFFormat}" = "TRIG" ]; then
#    rm *.trig
#fi

# send completion report signal to listening daemon
# both IP=${ReportDaemonIP} and Port=${ReportDaemonPort} depend on the daemon setup
logEntry="GraphDB repo \"${RepoName}\" creation completed at "`date --iso-8601='seconds'`
#echo $logEntry
#nc $ReportDaemonIP $ReportDaemonPort <<< $logEntry

# print repository size in MB
echo -e "GraphDB repository (no GeoSPARQL plugin) \"${RepoDir}/\" has size: `du -hs -BM ${RepoDir} | cut -d 'M' -f 1`MB"

# Disabled as it does not work with containers
# sudo /sbin/sysctl vm.drop_caches=3

if [ "${EnableGeoSPARQLPlugin}" = "TRUE" ]; then
    # enable the GeoSPARQL plugin on the repo
    LOG4J_CONFIGURATION=${BASE}/../../../runtime/src/main/resources/log4j.properties
    #echo "LOG4J_CONFIGURATION = $LOG4J_CONFIGURATION"
    JAVA_OPTS="${JVM_Xmx} -Dregister-external-plugins=${GraphDBBaseDir}/lib/plugins -Dlog4j.configuration=file:${LOG4J_CONFIGURATION}"
    #JAVA_OPTS="${JVM_Xmx} -Dgraphdb.extra.plugins=${GraphDBBaseDir}/lib/plugins -Dlog4j.configuration=file:${LOG4J_CONFIGURATION}"
    #echo "JAVA_OPTS = $JAVA_OPTS"
    cd ${BASE}/../../target
    #pwd
    CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"
    MAIN_CLASS="gr.uoa.di.rdf.geordfbench.graphdbsut.RepoUtil"
    EXEC_QUERY_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} \"${GraphDBDataDir}\" $RepoName"
    #echo $EXEC_QUERY_REPO
    eval ${EXEC_QUERY_REPO}
    # print repository size in MB
    echo -e "GraphDB repository (GeoSPARQL plugin ${IndexingAlgorithm}:${IndexingPrecision}) \"${RepoDir}/\" has size: `du -hs -BM ${RepoDir} | cut -d 'M' -f 1`MB"
fi
exit 0
