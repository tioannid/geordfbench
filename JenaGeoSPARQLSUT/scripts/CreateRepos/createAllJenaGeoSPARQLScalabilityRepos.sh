# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# != 6 )); then
    # in case no arguments are present there might be environment variables defined
    # globally ! Please check and then exit if necessary
    if [ -z ${DatasetBaseDir} ] || [ -z ${JenaBaseDir+x} ] || [ -z ${JenaGeoSPARQLRepoBaseDir+x} ] || [ -z ${JVM_Xmx+x} ] || [ -z ${ScalabilityGenScriptName+x} ] || [ -z ${ScalabilityGzipRefDSName+x} ]; then
        echo -e "Illegal number of parameters $SYNTAX"
	echo "As an alternative, some or all of the following environment variables {DatasetBaseDir, JenaBaseDir, JenaGeoSPARQLRepoBaseDir, JVM_Xmx, ScalabilityGenScriptName, ScalabilityGzipRefDSName} is/are not set";
	return 1    # return instead of exit because we need to source the script
    fi
else
        export DatasetBaseDir=${1}
        export JenaBaseDir=${2}
	export JenaGeoSPARQLRepoBaseDir=${3}
	export JVM_Xmx=${4}
        export ScalabilityGenScriptName=${5}
        export ScalabilityGzipRefDSName=${6}
fi

# get the directory where the script is located
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "`date`\n"

# create the class path for Strabon StoreOp and QueryOp classes
cd ${JenaBaseDir}/lib
export CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"
#echo $CLASS_PATH

# Check if JenaGeoSPARQL repo directory structure exists
if [ ! -d ${JenaGeoSPARQLRepoBaseDir} ]; then
    echo -e "${JenaGeoSPARQLRepoBaseDir} dir does not exist.\n\tCreating it now ..."
    mkdir -p ${JenaGeoSPARQLRepoBaseDir}
fi

levels=(  "10K" )
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
    DatabaseName=scalability_${level}
    RepoDir="${JenaGeoSPARQLRepoBaseDir}/${DatabaseName}"
    # create database directory if necessary
    if [ ! -d ${RepoDir} ]; then
        echo -e "\t${RepoDir} repo does not exist"
        mkdir -p ${RepoDir}   # create directory
        echo -e "\t${RepoDir} directory created"
    fi
    # load all N-Triple file
    # using tdbloader2 which is appropriate for loading into new databases
    for file in `ls -1 ${ScalabilityRDFBaseDir}/${level}/*.nt`; do
        ${JenaBaseDir}/bin/tdbloader2 --loc ${RepoDir} ${file}
    done
    # print repository size in MB
    echo -e "JenaGeoSPARQL TDB repository \"${RepoDir}\" has size: `du -hs -BM ${RepoDir} | cut -d 'M' -f 1`MB"
done


# return to base directory
cd $BASE
