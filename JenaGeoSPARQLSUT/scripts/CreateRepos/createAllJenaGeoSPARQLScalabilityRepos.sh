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

# create the class path for JenaGeoSPARQL
cd ${JenaBaseDir}/lib
export CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"
#echo $CLASS_PATH

# Check if JenaGeoSPARQL repo directory structure exists
if [ ! -d ${JenaGeoSPARQLRepoBaseDir} ]; then
    echo -e "${JenaGeoSPARQLRepoBaseDir} dir does not exist.\n\tCreating it now ..."
    mkdir -p ${JenaGeoSPARQLRepoBaseDir}
fi

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
    DatabaseName=scalability_${level}
    RepoDir="${JenaGeoSPARQLRepoBaseDir}/${DatabaseName}"
    # create database directory if necessary
    if [ ! -d ${RepoDir} ]; then
        echo -e "\t${RepoDir} repo does not exist"
        mkdir -p ${RepoDir}   # create directory
        echo -e "\t${RepoDir} directory created"
    fi
    # load all N-Triple file
    # using tdb2_tdbloader which is appropriate for loading into new TDB2 databases
    # - first increase the vm.max_map_count value
    #   as described in https://jena.apache.org/documentation/tdb2/tdb2_cmds.html
    # - In case we are inside a docker container do not use 'sudo'
    if [ -f /.dockerenv ]; then
        # sysctl: setting key "vm.max_map_count", ignoring: Read-only file system
        ## cur_vm_max_map_count=`sysctl -a 2>&1 | grep vm.max_map_count | cut -d "=" -f2 | xargs`
        ## sysctl -w vm.max_map_count=262144
        echo "Running in a docker container. File system is read-only. Cannot set sysctl key \"vm.max_map_count\""
    else
        cur_vm_max_map_count=`sudo sysctl -a 2>&1 | grep vm.max_map_count | cut -d "=" -f2 | xargs`
        sudo sysctl -w vm.max_map_count=262144
    fi
    
    # - then bulk load and ...
    for file in `ls -1 ${ScalabilityRDFBaseDir}/${level}/*.nt`; do
        ${JenaBaseDir}/bin/tdb2.tdbloader --loc ${RepoDir} ${file}
    done
    # - then restore the old value
    # - In case we are inside a docker container do not use 'sudo'
    if [ -f /.dockerenv ]; then
        # sysctl: setting key "vm.max_map_count", ignoring: Read-only file system
        ## sysctl -w vm.max_map_count=$cur_vm_max_map_count
        echo "Running in a docker container. File system is read-only. Cannot restore sysctl key \"vm.max_map_count\""
    else
        sudo sysctl -w vm.max_map_count=$cur_vm_max_map_count
    fi    
	
    # print repository size in MB
    echo -e "JenaGeoSPARQL TDB2 repository \"${RepoDir}\" has size: `du -hs -BM ${RepoDir} | cut -d 'M' -f 1`MB"
done


# return to base directory
cd $BASE
