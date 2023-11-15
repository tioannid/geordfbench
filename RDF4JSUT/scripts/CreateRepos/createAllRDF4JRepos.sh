# SYNTAX :
SCRIPT_NAME=`basename "$0"`
SYNTAX1="SYNTAX 1: $SCRIPT_NAME <RemoveIfExists> <DatasetBaseDir> <RDF4JRepoBaseDir> <JVM_Xmx> <EnableLuceneSail> <CompletionReportDaemonIP> <CompletionReportDaemonPort> <ScalabilityGenScriptName> <ScalabilityGzipRefDSName>"
SYNTAX2="SYNTAX 2: $SCRIPT_NAME <RemoveIfExists>"
SYNTAX="$SYNTAX1
$SYNTAX2
\t<RemoveIfExists>\t:\tremove repository if it exists (true = overwrite)
\t<DatasetBaseDir>\t:\tbase directory under which RDF dataset triple files are stored in various subdirectories
\t<RDF4JRepoBaseDir>\t:\tbase directory where RDF4J repos are stored
\t<JVM_Xmx>\t\t:\tJVM max memory e.g. -Xmx6g
\t<EnableLuceneSail>\t:\tenable Lucene Sail
\t<CompletionReportDaemonIP>\t:\treport daemon IP
\t<CompletionReportDaemonPort>\t:\treport daemon port
\t<ScalabilityGenScriptName>\t:\tscalability generator script path
\t<ScalabilityGzipRefDSName>\t:\tscalability gzipped reference dataset"


# STEP 1: Validate the script's syntax
#      1.1: check number of arguments

# check if the Geographica/scripts/prepareRunEnvironment.sh has initialized 
# the required variables for this script ...
if [ -z ${DatasetBaseDir+x} ] || [ -z ${RDF4JRepoBaseDir+x} ] || [ -z ${JVM_Xmx+x} ] || [ -z ${EnableLuceneSail+x} ] || [ -z ${CompletionReportDaemonIP+x} ] || [ -z ${CompletionReportDaemonPort+x} ] || [ -z ${ScalabilityGenScriptName+x} ] || [ -z ${ScalabilityGzipRefDSName+x} ]; then
    # One or more or the required variables has not been initialized
    # check the number of arguments ...
    echo -e "Some of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are missing:
\t{DatasetBaseDir, RDF4JRepoBaseDir, JVM_Xmx, EnableLuceneSail, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# != 9 )); then
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters!\n$SYNTAX"
	exit 1    # return instead of exit because we need to source the script
    else
        # all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override any related values defined through Geographica/scripts/prepareRunEnvironment.sh"
        export RemoveIfExists=${1}
        export DatasetBaseDir=${2}
        export RDF4JRepoBaseDir=${3}
        export JVM_Xmx=${4}
        export EnableLuceneSail=${5}
        export CompletionReportDaemonIP=${6}
        export CompletionReportDaemonPort=${7}
        export ScalabilityGenScriptName=${8}
        export ScalabilityGzipRefDSName=${9}
    fi
else
    # all required variables have been initialized
    echo -e "All of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are defined: 
\t{DatasetBaseDir, RDF4JRepoBaseDir, JVM_Xmx, EnableLuceneSail, CompletionReportDaemonIP, CompletionReportDaemonPort, ScalabilityGenScriptName, ScalabilityGzipRefDSName}";
    if (( $# == 9 )); then
        # but, all arguments have been provided to completely customize the run
        echo -e "All script arguments have been provided and will override all related values of Geographica/scripts/prepareRunEnvironment.sh"
        export RemoveIfExists=${1}
        export DatasetBaseDir=${2}
        export RDF4JRepoBaseDir=${3}
        export JVM_Xmx=${4}
        export EnableLuceneSail=${5}
        export CompletionReportDaemonIP=${6}
        export CompletionReportDaemonPort=${7}
        export ScalabilityGenScriptName=${8}
        export ScalabilityGzipRefDSName=${9}
    elif (( $# == 1 )); then
        # one argument is provided and can only be the <RemoveIfExists>
        RemoveIfExists=${1}
    else
        # some non anticipated combination of arguments is available and 
        # I will not handle this scenario
        # script cannot proceed, because arguments are missing
        echo -e "Illegal number of parameters!\n$SYNTAX"
	exit 2    # return instead of exit because we need to source the script
    fi
fi

# Validate if $RemoveIfExists is boolean
RemoveIfExists=${RemoveIfExists^^}
if [ "$RemoveIfExists" = "TRUE" ]; then
    RemoveIfExists="true"
else
    RemoveIfExists="false"
fi

# Validate if $EnableLuceneSail is boolean
EnableLuceneSail=${EnableLuceneSail^^}
if [ "$EnableLuceneSail" = "TRUE" ]; then
    EnableLuceneSail="true"
else
    EnableLuceneSail="false"
fi

echo -e "Running script with syntax: 
\t./${SCRIPT_NAME} ${RemoveIfExists} ${DatasetBaseDir} ${RDF4JRepoBaseDir} ${JVM_Xmx} ${EnableLuceneSail} ${CompletionReportDaemonIP} ${CompletionReportDaemonPort} ${ScalabilityGenScriptName} ${ScalabilityGzipRefDSName}"
echo -e "Script start time: `date`"

# Check if repo directory structure exists
if [ ! -d ${RDF4JRepoBaseDir} ]; then
    echo -e "${RDF4JRepoBaseDir} dir does not exist.\n\tCreating it now ..."
    mkdir -p ${RDF4JRepoBaseDir}
fi

# Real World dataset
#WKTIdxList="http://www.opengis.net/ont/geosparql#asWKT http://geo.linkedopendata.gr/corine/ontology#asWKT http://dbpedia.org/property/asWKT http://geo.linkedopendata.gr/gag/ontology/asWKT http://www.geonames.org/ontology#asWKT http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT http://linkedgeodata.org/ontology/asWKT"
#echo -e "\t./createRDF4JRepo.sh ${RDF4JRepoBaseDir} realworld ${RemoveIfExists} \"spoc,posc,cosp\" trig ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${JVM_Xmx} ${EnableLuceneSail} \"${WKTIdxList}\" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
#./createRDF4JRepo.sh ${RDF4JRepoBaseDir} realworld ${RemoveIfExists} "spoc,posc,cosp" trig ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Synthetic dataset
#WKTIdxList="http://geographica.di.uoa.gr/generator/pointOfInterest/asWKT http://geographica.di.uoa.gr/generator/stateCenter/asWKT http://geographica.di.uoa.gr/generator/state/asWKT http://geographica.di.uoa.gr/generator/landOwnership/asWKT http://geographica.di.uoa.gr/generator/road/asWKT"
#./createRDF4JRepo.sh ${RDF4JRepoBaseDir} synthetic ${RemoveIfExists} "spoc,posc" n-triples ${DatasetBaseDir}/SyntheticWorkload/Synthetic ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Real World dataset - Points only!
#WKTIdxList="http://www.opengis.net/ont/geosparql#asWKT http://dbpedia.org/property/asWKT http://www.geonames.org/ontology#asWKT"
#./createRDF4JRepo.sh ${RDF4JRepoBaseDir} realworld_points ${RemoveIfExists} "spoc,posc,cosp" trig ${DatasetBaseDir}/RealWorldWorkload/NO_CRS/RealWorld_Points ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Synthetic dataset - Points Of Interest only!
#WKTIdxList="http://www.opengis.net/ont/geosparql#asWKT http://geographica.di.uoa.gr/generator/pointOfInterest/asWKT"
#./createRDF4JRepo.sh ${RDF4JRepoBaseDir} synthetic_pois ${RemoveIfExists} "spoc,posc" n-triples ${DatasetBaseDir}/SyntheticWorkload/Synthetic_POIs ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
# Census dataset
#WKTIdxList="http://geographica.di.uoa.gr/cencus/ontology#asWKT"
#./createRDF4JRepo.sh ${RDF4JRepoBaseDir} census ${RemoveIfExists} "spoc,posc" n-triples ${DatasetBaseDir}/Census/NO_CRS ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}

#exit 0;

# OSM+CORINE2012 datasets - Scalability 10K, 100K, 1M, 10M, 100M, 500M
WKTIdxList="http://www.opengis.net/ont/geosparql#asWKT"
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
    echo "Generating scalability ${level} repository ..."
    echo -e "\t./createRDF4JRepo.sh ${RDF4JRepoBaseDir} scalability_${level} ${RemoveIfExists} \"spoc,posc\" ${RDFFormat} ${DatasetBaseDir}/Scalability/${level} ${JVM_Xmx} ${EnableLuceneSail} \"${WKTIdxList}\" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}"
    ./createRDF4JRepo.sh ${RDF4JRepoBaseDir} scalability_${level} ${RemoveIfExists} "spoc,posc" ${RDFFormat} ${DatasetBaseDir}/Scalability/${level} ${JVM_Xmx} ${EnableLuceneSail} "${WKTIdxList}" ${CompletionReportDaemonIP} ${CompletionReportDaemonPort}
done
echo -e "Script end time: `date`"