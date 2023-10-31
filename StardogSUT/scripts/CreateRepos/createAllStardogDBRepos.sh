# SYNTAX :
#    <script> DatasetBaseDir StardogBaseDir STARDOG_HOME STARDOG_SERVER_JAVA_ARGS StardogSpatialPrecision CompletionReportDaemonIP CompletionReportDaemonPort ScalabilityGenScriptName ScalabilityGzipRefDSName
SCRIPT_NAME=`basename "$0"`
SYNTAX1="SYNTAX 1: $SCRIPT_NAME <DatasetBaseDir> <StardogBaseDir> <STARDOG_HOME> <STARDOG_SERVER_JAVA_ARGS> <StardogSpatialPrecision> <CompletionReportDaemonIP> <CompletionReportDaemonPort> <ScalabilityGenScriptName> <ScalabilityGzipRefDSName>"
SYNTAX2="SYNTAX 2: $SCRIPT_NAME"
SYNTAX="$SYNTAX1
$SYNTAX2
\t<DatasetBaseDir>\t:\tbase directory under which RDF dataset triple files are stored in various subdirectories,
\t<StardogBaseDir>\t:\tStardog base installation directory,
\t<STARDOG_HOME>\t\t:\tdirectory to store databases and files,
\t<STARDOG_SERVER_JAVA_ARGS>\t:\tStardog server java arguments, min-max JVM heap, max direct off-heap memory e.g. '-Xms4g -Xmx4g -XX:MaxDirectMemorySize=6g'
\t<StardogSpatialPrecision>\t:\tspatial precision (default=11),
\t<CompletionReportDaemonIP>\t:\treport daemon IP,
\t<CompletionReportDaemonPort>\t:\treport daemon port,
\t<ScalabilityGenScriptName>\t:\tscalability generator script path,
\t<ScalabilityGzipRefDSName>\t:\tscalability gzipped reference dataset"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments

# check if the Geographica/scripts/prepareRunEnvironment.sh has initialized 
# the required variables for this script ...

if [ -z ${DatasetBaseDir+x} ] || [ -z ${StardogBaseDir+x} ] || [ -z ${STARDOG_HOME+x} ]|| [ -z ${STARDOG_SERVER_JAVA_ARGS+x} ] || [ -z ${StardogSpatialPrecision+x} ] || [ -z ${CompletionReportDaemonIP+x} ] || [ -z ${CompletionReportDaemonPort+x} ] || [ -z ${ScalabilityGenScriptName+x} ] || [ -z ${ScalabilityGzipRefDSName+x} ]; then
    # One or more or the required variables has not been initialized
    # check the number of arguments ...
    echo -e "Some of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are missing:
\t{DatasetBaseDir, StardogBaseDir, STARDOG_HOME, STARDOG_SERVER_JAVA_ARGS, StardogSpatialPrecision, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# != 9 )); then
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters!\n$SYNTAX"
	exit 1    # return instead of exit because we need to source the script
    else
        # all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override any related values defined through Geographica/scripts/prepareRunEnvironment.sh"
        export DatasetBaseDir=${1}
        export StardogBaseDir=${2}
        export STARDOG_HOME=${3}
        export STARDOG_SERVER_JAVA_ARGS=${4}
        export StardogSpatialPrecision=${5}
        export CompletionReportDaemonIP=${6}
        export CompletionReportDaemonPort=${7}
        export ScalabilityGenScriptName=${8}
        export ScalabilityGzipRefDSName=${9}
    fi
else
    # all required variables have been initialized
    echo -e "All of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are defined:
\t{DatasetBaseDir, StardogBaseDir, STARDOG_HOME, STARDOG_SERVER_JAVA_ARGS, StardogSpatialPrecision, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# == 9 )); then
        # but, all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override all related values of Geographica/scripts/prepareRunEnvironment.sh"
        export DatasetBaseDir=${1}
        export StardogBaseDir=${2}
        export STARDOG_HOME=${3}
        export STARDOG_SERVER_JAVA_ARGS=${4}
        export StardogSpatialPrecision=${5}
        export CompletionReportDaemonIP=${6}
        export CompletionReportDaemonPort=${7}
        export ScalabilityGenScriptName=${8}
        export ScalabilityGzipRefDSName=${9}
    elif (( $# == 0 )); then
        # no argument was provided and default values of environment variables 
        # in (Geographica/scripts/prepareRunEnvironment.sh) will be used!
        echo "0 args provided"
    else
        # some non anticipated combination of arguments is available and 
        # I will not handle this scenario
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters!\n$SYNTAX"
	exit 2    # return instead of exit because we need to source the script
    fi
fi

# Optimize Stardog properties file for Bulk Load
${StardogBaseDir}/bin/stardog-admin server stop
rm ${STARDOG_HOME}/stardog.properties
cp ${StardogLoadPropertyFile} ${STARDOG_HOME}/stardog.properties
${StardogBaseDir}/bin/stardog-admin server start
$StardogBaseDir/bin/stardog-admin property get | grep spatial

echo -e "Running script with syntax: \t./${SCRIPT_NAME} ${DatasetBaseDir} ${StardogBaseDir} ${STARDOG_HOME} \"${STARDOG_SERVER_JAVA_ARGS}\" ${StardogSpatialPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort} ${ScalabilityGenScriptName} ${ScalabilityGzipRefDSName}"
echo -e "Script start time: `date`"

# Check if repo directory structure exists - however for Stardog the Geographica/scripts/prepareRunEnvironment.sh creates $ STARDOG_HOME if necessary
# NO ACTION NEEDED!

# Real World dataset
RDFFormat="TRIG"
#echo -e "\t./createStardogDBRepo.sh realworld ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${RDFFormat} ${StardogBaseDir} ${STARDOG_HOME} ${StardogSpatialPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
#./createStardogDBRepo.sh realworld ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${RDFFormat} ${StardogBaseDir} ${STARDOG_HOME} ${StardogSpatialPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
#$StardogBaseDir/bin/stardog-admin metadata get realworld | grep spatial
# Synthetic dataset
#./createStardogDBRepo.sh synthetic.ttl ${DatasetBaseDir}/SyntheticWorkload/Synthetic N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Real World dataset - Points only!
#./createStardogDBRepo.sh realworld_points.ttl ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld_Points TRIG ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Synthetic dataset - Points Of Interest only!
#./createStardogDBRepo.sh synthetic_pois.ttl ${DatasetBaseDir}/SyntheticWorkload/Synthetic_POIs N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Census dataset
#./createStardogDBRepo.sh census.ttl ${DatasetBaseDir}/Census/NO_CRS N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}


#exit 0;

# Scalability dataset(s)
levels=( "10K" )
#levels=(  "10K" "100K" "1M" "10M" "100M" "500M" )
ScalabilityRDFBaseDir="${DatasetBaseDir}/Scalability"
RDFFormat="N-TRIPLES"
for level in "${levels[@]}"; do
    # Check if Scalability level directory does not exist, create and populate it
    echo "Checking/Creating scalability ${level} dataset ..."
    ScalabilityRDFDir="${ScalabilityRDFBaseDir}/${level}"
    if [ ! -d ${ScalabilityRDFDir} ]; then
        echo -e "\tScalability ${level} dataset does not exist"
        mkdir -p ${ScalabilityRDFDir}   # create directory
        echo -e "\t${ScalabilityRDFDir} directory created"
        eval "${ScalabilityGenScriptName} ${ScalabilityGzipRefDSName} ${level} > ${ScalabilityRDFDir}/scalability_${level}.nt" # extract Scalability dataset for the ${level}
        echo -e "\tScalability Generator script successfully extracted scalability ${level} dataset to \"${ScalabilityRDFDir}/scalability_${level}.nt\""
    else
        echo -e "\tScalability ${level} dataset already exists"
    fi

    ScalabilityRepoName=scalability_${level}
    ScalabilityRDFDir="${ScalabilityRDFBaseDir}/${level}"
    echo "Generating scalability ${level} repository ..."
    echo "./createStardogDBRepo.sh ${ScalabilityRepoName} ${DatasetBaseDir}/Scalability/${level} ${RDFFormat} ${StardogBaseDir} ${STARDOG_HOME} ${StardogSpatialPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
    ./createStardogDBRepo.sh ${ScalabilityRepoName} ${DatasetBaseDir}/Scalability/${level} ${RDFFormat} ${StardogBaseDir} ${STARDOG_HOME} ${StardogSpatialPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
    $StardogBaseDir/bin/stardog-admin metadata get ${ScalabilityRepoName} | grep spatial
done

#exit 0;

# Optimize Stardog properties file for Querying
rm ${STARDOG_HOME}/stardog.properties
cp ${StardogQueryPropertyFile} ${STARDOG_HOME}/stardog.properties
${StardogBaseDir}/bin/stardog-admin server stop
