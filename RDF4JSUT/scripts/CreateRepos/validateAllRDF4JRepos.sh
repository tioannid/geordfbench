# SYNTAX :
#    <script> <RDF4JRepoBaseDir> <JVM_Xmx>
SCRIPT_NAME=`basename "$0"`
SYNTAX="
SYNTAX: $SCRIPT_NAME <RDF4JRepoBaseDir> <JVM_Xmx>
\t<RDF4JRepoBaseDir>\t:\tbase directory where RDF4J repos are stored,
\t<JVM_Xmx>\t\t:\tJVM max memory e.g. -Xmx6g"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# != 2 )); then
    # in case no arguments are present there might be environment variables defined
    # globally ! Please check and then exit if necessary
    if [ -z ${RDF4JRepoBaseDir+x} ] || [ -z ${JVM_Xmx+x} ]; then
        echo -e "Illegal number of parameters $SYNTAX"
	echo "As an alternative, some or all of the following environment variables {RDF4JRepoBaseDir, JVM_Xmx} is/are not set";
	return 1    # return instead of exit because we need to source the script
    fi
else
        export RDF4JRepoBaseDir=${1}
        export JVM_Xmx=${2}
fi

upgradeLuceneIdxs=false

echo -e "Running script with syntax: 
\t./${SCRIPT_NAME} ${RDF4JRepoBaseDir} ${JVM_Xmx}"
echo -e "Script start time: `date`"

# Real World dataset
#./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} realworld ${JVM_Xmx} ${upgradeLuceneIdxs}
# Synthetic dataset
#./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} synthetic ${JVM_Xmx} ${upgradeLuceneIdxs}
# Real World dataset - Points only!
#./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} realworld_points ${JVM_Xmx} ${upgradeLuceneIdxs}
# Synthetic dataset - Points Of Interest only!
#./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} synthetic_pois ${JVM_Xmx} ${upgradeLuceneIdxs}

#exit 0;

# OSM+CORINE2012 datasets - Scalability 10K, 100K, 1M, 10M, 100M, 500M
levels=( "10K" )
#levels=(  "10K" "100K" "1M" "10M" "100M" "500M" )
for level in "${levels[@]}"; do
    echo -e "\t./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} scalability_${level} ${JVM_Xmx} ${upgradeLuceneIdxs}"
    ./validateRDF4JRepo.sh ${RDF4JRepoBaseDir} scalability_${level} ${JVM_Xmx} ${upgradeLuceneIdxs}
done
echo -e "Script end time: `date`"