# SYNTAX :
#    <script> RepoName RDFDir RDFFormat StardogBaseDir STARDOG_HOME StardogSpatialPrecision ReportDaemonIP ReportDaemonPort
#
SCRIPT_NAME=`basename "$0"`
SYNTAX="
SYNTAX: $SCRIPT_NAME RepoName RDFDir RDFFormat StardogBaseDir STARDOG_HOME StardogSpatialPrecision ReportDaemonIP ReportDaemonPort
\tRepoName\t:\trepo name,
\tRDFDir\t:\tdirectory with the RDF data files to load,
\tRDFFormat\t:\tRDF format {N-TRIPLES | TRIG }
\StardogBaseDir\t:\tbase directory of the Stardog installation,
\t<STARDOG_HOME>\t:\tdirectory to store databases and files,
\StardogSpatialPrecision\t:\tspatial precision (default=11),
\tReportDaemonIP\t:\treport daemon IP,
\tReportDaemonPort\t:\treport daemon port"

# Assumptions
# 1) The N-Triple files for the dataset are located in the RDFDir directory
# 2) The rdf2rdf-1.0.1-2.3.1.jar is located in the same directory as the script
# 3) The pgrep command should be available
# 4) A "maps_to_contexts.txt" file exists in ntripleDir if context/graph IRIs need to be specified in TRIG files
# 5) The repoConf file has the appropriate repository name!

MAP_CONTEXTS_FILE="${GeoRDFBenchScriptsDir}/map_to_contexts.txt"

# STEP 0: Find the directory where the script is located in
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments and assign them to variables
if (( $# != 8 )); then
    echo -e "Illegal number of parameters ${SYNTAX}"
    exit 1
fi

RepoName=${1}
# echo $RepoName
RDFDir=$2
# echo $RDFDir
RDFFormat=${3^^}
# echo $RDFFormat
StardogBaseDir=$4
# echo $StardogBaseDir
STARDOG_HOME=$5
# echo $StardogBaseDir
StardogSpatialPrecision=$6
#echo $STARDOG_HOME
ReportDaemonIP=$7
#echo "ReportDaemonIP = $ReportDaemonIP"
ReportDaemonPort=$8
#echo "ReportDaemonPort = $ReportDaemonPort"

#      1.3: check whether the directories (RDFDir StardogBaseDir) exist
dirs=(  "$RDFDir" "$StardogBaseDir" )
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

#      2.2: check whether the repos $RepoName already exists
#      2.2.1: check if <STARDOG_HOME> contains any of the repos (repoName )
RepoDir="${STARDOG_HOME}/${RepoName}"
# echo $RepoDir 
if [ -d "$RepoDir" ]; then
	echo "Repo ${RepoName} already exists in --> ${RepoDir}"
	exit 5
fi		

# STEP 4: For the directory $RDFDir remove TRIG files that are created by N-Triple files
#      4.1: change working directory to $RDFDir
cd $RDFDir
# STEP 6: Remove TRIG file to avoid allocating space
if [ "${RDFFormat}" = "TRIG" ]; then
    rm *.trig
fi
GraphFileList=""
#      4.2: if $RDFFormat is TRIG, do not convert N-Triples to TRIG
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
            if [ $MapToContextFile_Exists -eq 1 ]; then
                matchedline=`grep -e $i $MAP_CONTEXTS_FILE`
                # echo "File $i found in file $MAP_CONTEXTS_FILE in line : $matchedline"
                matchedcontext=`echo -e "$matchedline" | awk -F"\t" ' { printf $2 }'`
                # echo "Corresponding context is : $matchedcontext"
                # remove first < and last > from IRI
                matchedcontext=`echo "${matchedcontext}"   | sed 's/^<//;s/>$//'`
                GraphFileList="${GraphFileList} @${matchedcontext} ${filename}" 
            fi
    done
else
    GraphFileList="${GraphFileList} ${RDFDir}"
fi
#      4.2.6: Set the filetype to N-TRIPLES
rdffiletype='.nt'

# STEP 5: Create $RepoName repository with config file $RepoConfig and load RDF files while measuring the elapsed time
echo "time  ${StardogBaseDir}/bin/stardog-admin db create -o spatial.enabled=true spatial.use.jts=true -n ${RepoName} ${GraphFileList}"
time ${StardogBaseDir}/bin/stardog-admin db create -o spatial.enabled=true spatial.use.jts=true -n ${RepoName} ${GraphFileList}


# send completion report signal to listening daemon
# both IP=${ReportDaemonIP} and Port=${ReportDaemonPort} depend on the daemon setup
logEntry="Stardog repo \"${RepoName}\" creation completed at "`date --iso-8601='seconds'`
#echo $logEntry
#nc $ReportDaemonIP $ReportDaemonPort <<< $logEntry

# print repository size in MB
echo -e "Stardog repository \"${RepoDir}/\" has size: `du -hs -BM ${RepoDir} | cut -d 'M' -f 1`MB"

exit 0
