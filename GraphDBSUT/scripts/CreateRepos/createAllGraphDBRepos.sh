# SYNTAX :
#    <script> DatasetBaseDir GraphDBBaseDir EnableGeoSPARQLPlugin IndexingAlgorithm IndexingPrecision ReportDaemonIP ReportDaemonPort
SCRIPT_NAME=`basename "$0"`
SYNTAX1="SYNTAX 1: $SCRIPT_NAME <DatasetBaseDir> <GraphDBBaseDir> <JVM_Xmx> <EnableGeoSPARQLPlugin> <IndexingAlgorithm> <IndexingPrecision> <CompletionReportDaemonIP> <CompletionReportDaemonPort> <ScalabilityGenScriptName> <ScalabilityGzipRefDSName>"
SYNTAX2="SYNTAX 2: $SCRIPT_NAME"
SYNTAX="$SYNTAX1
$SYNTAX2
\t<DatasetBaseDir>\t:\tbase directory under which RDF dataset triple files are stored in various subdirectories,
\t<GraphDBBaseDir>\t:\tGraphDB base installation directory,
\t<JVM_Xmx>\t\t:\tJVM max memory e.g. -Xmx6g
\tEnableGeoSPARQLPlugin\t:\ttrue|false,
\tIndexingAlgorithm\t:\tquad|geohash,
\tIndexingPrecision\t:\tquad=(1..25), geohash=(1..24),
\t<CompletionReportDaemonIP>\t:\treport daemon IP,
\t<CompletionReportDaemonPort>\t:\treport daemon port,
\t<ScalabilityGenScriptName>\t:\tscalability generator script path,
\t<ScalabilityGzipRefDSName>\t:\tscalability gzipped reference dataset"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments

# check if the Geographica/scripts/prepareRunEnvironment.sh has initialized 
# the required variables for this script ...

if [ -z ${DatasetBaseDir+x} ] || [ -z ${GraphDBBaseDir+x} ] || [ -z ${JVM_Xmx+x} ] || [ -z ${EnableGeoSPARQLPlugin+x} ] || [ -z ${IndexingAlgorithm+x} ] || [ -z ${IndexingPrecision+x} ] || [ -z ${CompletionReportDaemonIP+x} ] || [ -z ${CompletionReportDaemonPort+x} ] || [ -z ${ScalabilityGenScriptName+x} ] || [ -z ${ScalabilityGzipRefDSName+x} ]; then
    # One or more or the required variables has not been initialized
    # check the number of arguments ...
    echo -e "Some of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are missing:
\t{DatasetBaseDir, GraphDBBaseDir, JVM_Xmx, EnableGeoSPARQLPlugin, IndexingAlgorithm, IndexingPrecision, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# != 10 )); then
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters!\n$SYNTAX"
	exit 1    # return instead of exit because we need to source the script
    else
        # all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override any related values defined through Geographica/scripts/prepareRunEnvironment.sh"
        export DatasetBaseDir=${1}
        export GraphDBBaseDir=${2}
        export JVM_Xmx=${3}
        export EnableGeoSPARQLPlugin=${4}
        export IndexingAlgorithm=${5}
        export IndexingPrecision=${6}
        export CompletionReportDaemonIP=${7}
        export CompletionReportDaemonPort=${8}
        export ScalabilityGenScriptName=${9}
        export ScalabilityGzipRefDSName=${10}
    fi
else
    # all required variables have been initialized
    echo -e "All of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are defined: 
\t{DatasetBaseDir, GraphDBBaseDir, JVM_Xmx, EnableGeoSPARQLPlugin, IndexingAlgorithm, IndexingPrecision, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# == 10 )); then
        # but, all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override all related values of Geographica/scripts/prepareRunEnvironment.sh"
        export DatasetBaseDir=${1}
        export GraphDBBaseDir=${2}
        export JVM_Xmx=${3}
        export EnableGeoSPARQLPlugin=${4}
        export IndexingAlgorithm=${5}
        export IndexingPrecision=${6}
        export CompletionReportDaemonIP=${7}
        export CompletionReportDaemonPort=${8}
        export ScalabilityGenScriptName=${9}
        export ScalabilityGzipRefDSName=${10}
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

# Validate if $EnableGeoSPARQLPlugin is boolean
EnableGeoSPARQLPlugin=${EnableGeoSPARQLPlugin^^}
if [ "$EnableGeoSPARQLPlugin" = "TRUE" ]; then
    EnableGeoSPARQLPlugin="true"
else
    EnableGeoSPARQLPlugin="false"
fi

echo -e "Running script with syntax: 
\t./${SCRIPT_NAME} ${DatasetBaseDir} ${GraphDBBaseDir} ${JVM_Xmx} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort} ${ScalabilityGenScriptName} ${ScalabilityGzipRefDSName}"
echo -e "Script start time: `date`"

# Check if repo directory structure exists
if [ ! -d ${GraphDBDataDir} ]; then
    echo "${GraphDBDataDir} dir does not exist.\n\tCreating it now ..."
    mkdir -p ${GraphDBDataDir}
fi


# Real World dataset
ConfigTTL="realworld.ttl"
RDFFormat="TRIG"
#echo -e "\t./createGraphDBRepo.sh ${ConfigTTL} ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
#./createGraphDBRepo.sh ${ConfigTTL} ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Synthetic dataset
#./createGraphDBRepo.sh synthetic.ttl ${DatasetBaseDir}/SyntheticWorkload/Synthetic N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Real World dataset - Points only!
#./createGraphDBRepo.sh realworld_points.ttl ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld_Points TRIG ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Synthetic dataset - Points Of Interest only!
#./createGraphDBRepo.sh synthetic_pois.ttl ${DatasetBaseDir}/SyntheticWorkload/Synthetic_POIs N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Census dataset
#./createGraphDBRepo.sh census.ttl ${DatasetBaseDir}/Census/NO_CRS N-TRIPLES ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}


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

    ConfigTTL=scalability_${level}.ttl
    ScalabilityRDFDir="${ScalabilityRDFBaseDir}/${level}"
    echo "Generating scalability ${level} repository ..."
    echo "./createGraphDBRepo.sh ${ConfigTTL} ${ScalabilityRDFDir} ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
    ./createGraphDBRepo.sh ${ConfigTTL} ${DatasetBaseDir}/Scalability/${level} ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
done

exit 0;

# PregenSynthetic dataset(s)
levels=( "512" )
#levels=(  "512" "768" "1024" "2048" )
RDFFormat="N-TRIPLES"
for level in "${levels[@]}"; do
    ConfigTTL=pregensynthetic_${level}.ttl
    echo "./createGraphDBRepo.sh ${ConfigTTL} ${DatasetBaseDir}/PregenSynthetic/${level} ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
    ./createGraphDBRepo.sh ${ConfigTTL} ${DatasetBaseDir}/PregenSynthetic/${level} ${RDFFormat} ${GraphDBBaseDir} ${EnableGeoSPARQLPlugin} ${IndexingAlgorithm} ${IndexingPrecision} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
done

