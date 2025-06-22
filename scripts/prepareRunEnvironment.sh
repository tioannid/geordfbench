#!/bin/bash

# Example execution
# source ./prepareRunEnvironment.sh teleios3 GraphDBSUT "run graphdb tests"

# CONSTANTS
# Environment
ValidEnvironments=( "VM" "PAVILIONDV7" "PYRAVLOS6" "TELEIOS3" "NUC8I7BEH" )
DefaultEnvironment="VM"
ValidEnvironmentsMsgLine=""
for env in "${ValidEnvironments[@]}"; do
    if [ "$ValidEnvironmentsMsgLine" = "" ]; then # for the 1st item
        ValidEnvironmentsMsgLine="${env}"
    else                                          # for the remaining items
        ValidEnvironmentsMsgLine="${ValidEnvironmentsMsgLine} | ${env}"
    fi
done
ValidEnvironmentsMsgLine="{$ValidEnvironmentsMsgLine}"
# SUT
ValidSUTs=("RDF4JSUT" "GraphDBSUT" "StrabonSUT" "StardogSUT" "VirtuosoSUT" "JenaGeoSPARQLSUT" )
DefaultSUT="RDF4JSUT"
ValidSUTsMsgLine=""
for sut in "${ValidSUTs[@]}"; do
    if [ "$ValidSUTsMsgLine" = "" ]; then # for the 1st item
        ValidSUTsMsgLine="${sut}"
    else                                  # for the remaining items
        ValidSUTsMsgLine="${ValidSUTsMsgLine} | ${sut}"
    fi
done
ValidSUTsMsgLine="{$ValidSUTsMsgLine}"
# Description
DefaultExperimentShortDesc="NoDescription"
# SYNTAX :
#    <script> environment
SCRIPT_NAME=`basename "${BASH_SOURCE}"`
SYNTAX1="SYNTAX1: source $SCRIPT_NAME <environment> <activesut> <short description>"
SYNTAX2="SYNTAX2: source $SCRIPT_NAME <environment> <activesut>"
SYNTAX3="SYNTAX3: source $SCRIPT_NAME <environment>"
SYNTAX="
$SYNTAX1
$SYNTAX2
$SYNTAX3
\t<environment>\t:\tEnvironment the experiment will run on. One of ${ValidEnvironmentsMsgLine}
\t<activesut>\t:\tActive SUT. One of ${ValidSUTsMsgLine}
\t<shortdesc>\t:\tExperiment short description"

# STEP 0: Find the directory where the script is located in, Geographica/scripts
export GeoRDFBenchScriptsDir="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export GeoRDFBenchJSONLibDir=`readlink -f "${GeoRDFBenchScriptsDir}/../json_defs"`

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# > 3 ))  || (( $# < 1 )); then
    echo -e "Illegal number of arguments $SYNTAX"
    return 1
fi

#      1.2: read the arguments
export Environment=${1^^}
if [[ ! " ${ValidEnvironments[*]} " =~ " ${Environment} " ]]; then
    # whatever you want to do when array doesn't contain value
    export Environment=${DefaultEnvironment}
fi

# check if mercurial or git is installed and define the changeset value
Changeset="00"
changeset_set_by=""
# check the installation path of the git utility
git_installed=`type -p "git"` # returns empty string if binary is not installed
# if git binary is installed then retrieve the git commit
if [[ ! -z "$git_installed" ]]; then
	git_repo_exists=`git rev-parse --is-inside-work-tree`
	if [[ $git_repo_exists == "true" ]]; then
		echo "Git repo exists"
		Changeset=`git rev-parse --short HEAD`  # should be a 7 digit hexadecimal number
		changeset_set_by="git"
	fi
fi
if [[ $changeset_set_by == "" ]]; then # there was no git repo active in this folder tree
	hg_installed=`type -p "hg"` # returns empty string if binary is not installed
	# if mercurial binary is installed then retrieve the hg commit
	if [[ ! -z "$hg_installed" ]]; then
		hg_repo_exists=$((`hg identify 2>&1 | cut -b 1-5 -` != "abort"))
		if [[ $hg_repo_exists -eq 0 ]]; then
			echo "Mercurial repo exists" 
			Changeset=`hg identify | cut -d " " -f1`  # should be a 10 digit hexadecimal number
			changeset_set_by="hg"
		fi
	fi
fi
	
#       set the active SUT and description
if (( $# == 3 )); then
    export ActiveSUT=${2}
    if [[ ! " ${ValidSUTs[*]} " =~ " ${ActiveSUT} " ]]; then
        # whatever you want to do when array doesn't contain value
        export ActiveSUT=${DefaultSUT}
    fi
    ShortDesc="${3}"
elif (( $# == 2 )); then
    export ActiveSUT=${2}
    if [[ ! " ${ValidSUTs[*]} " =~ " ${ActiveSUT} " ]]; then
        # whatever you want to do when array doesn't contain value
        export ActiveSUT=${DefaultSUT}
    fi
    ShortDesc="${DefaultExperimentShortDesc}"
else
    export ActiveSUT=${DefaultSUT}
    ShortDesc="${DefaultExperimentShortDesc}"    
fi
echo "Running script with syntax: source ${SCRIPT_NAME} ${Environment} ${ActiveSUT} ${ShortDesc}"

export ExperimentShortDesc="${ActiveSUT}_${ShortDesc}"

#       1.3: set the versions of the SUTs
export verRDF4J="4.3.15"
export verGRAPHDB="10.8.4" # could not find the 10.8.5 distributable, moved to 11.0 directly
export verSTARDOG="8.2.2"
export verVIRTUOSO="7.2.14"
export verJENA="4.10.0"
export verSTRABON="3.3.3-SNAPSHOT"

#       1.4: set other SUT dependent variables
#           GraphDB dependent, environment independent
if [ -z ${EnableGeoSPARQLPlugin+x} ]; then
	export EnableGeoSPARQLPlugin=false
	export IndexingAlgorithm=quad   # default: quad
	export IndexingPrecision=11     # default: 11, quad (1-50), geohash(1-24)
fi
if [ "${EnableGeoSPARQLPlugin}" = "true" ]; then
	echo "EnableGeoSPARQLPlugin was explicitly set to true"
	if [ -z ${IndexingAlgorithm+x} ]; then  # IndexingAlgorithm not provided
		export IndexingAlgorithm=quad   	# default: quad
		export IndexingPrecision=11     	# default: 11, quad (1-50), geohash(1-24)
	else					# IndexingAlgorithm provided
		# check if IndexingAlgorithm is valid
		echo "IndexingAlgorithm was explicitly set to ${IndexingAlgorithm}"
		ValidIndexingAlgorithm=$( [[ "$IndexingAlgorithm" == "quad" || "$IndexingAlgorithm" == "geohash" ]]; echo $? )
		if ! [ "$ValidIndexingAlgorithm" -eq 0 ]; then	# IndexingAlgorithm is invalid
				echo -e "IndexingAlgorithm = ${IndexingAlgorithm} is not in valid range [quad, geohash]"
    				return 1
		fi
		# check if IndexingPrecision is in range
		if [ -z ${IndexingPrecision+x} ]; then # IndexingPrecision not provided
			export IndexingPrecision=11    		# default: 11, quad (1-50), geohash(1-24)
		else				       # IndexingPrecision provided
			echo "IndexingPrecision was explicitly set to ${IndexingPrecision}"
			if [ "$IndexingAlgorithm" = quad ]; then 
				IndexPrecisionRange="[1..50]"
				if [ "$IndexingPrecision" -lt 1 ] || [ "$IndexingPrecision" -gt 50 ]; then
					echo -e "IndexPrecision = ${IndexingPrecision} is not in valid range ${IndexPrecisionRange}"
    					return 1
				fi 
			elif [ "$IndexingAlgorithm" = geohash ]; then
				IndexPrecisionRange="[1..24]"
				if [ "$IndexingPrecision" -lt 1 ] || [ "$IndexingPrecision" -gt 24 ]; then
					echo -e "IndexPrecision = ${IndexingPrecision} is not in valid range ${IndexPrecisionRange}"
    					return 1
				fi 
			fi
		fi
	fi	
fi
	
#           RDF4J dependent, environment independent
if [ -z ${EnableLuceneSail+x} ]; then
	export EnableLuceneSail=false
fi
if [ "${EnableLuceneSail}" = "true" ]; then
    export RDF4JLuceneReposPrefix="Lucene"
else
    export RDF4JLuceneReposPrefix=""
fi

# Stardog dependent, environment independent variables
export StardogSpatialPrecision=11 #  value lower than the default (11) can improve the performance of spatial queries at the cost of accuracy

# Virtuoso dependent, environment variables
export VirtuosoTemplateConfigurationFile="${GeoRDFBenchScriptsDir}/../VirtuosoSUT/scripts/CreateRepos/virtuoso_geographica.ini"
export NumberOfBuffers=2000 # default value
export MaxDirtyBuffers=1200 # default value

# STEP 2: Set the values of the exported environment variables

# formulate the results folder name
DateTimeISO8601=`date --iso-8601='date'`
export ResultsDirName="${Changeset}#_${DateTimeISO8601}_${ExperimentShortDesc}"
export ExperimentDesc=${ResultsDirName}

# VM is considered the default environment, 
# VM is the development environment, but more environments
# can be added by editing the following IF-THEN-ELSE structure

#       set common base directory for experiment environment
if [ "$Environment" == "VM" ]; then
    export EnvironmentBaseDir="/media/sf_VM_Shared/PHD"
elif [ "$Environment" == "PAVILIONDV7" ]; then 
    export EnvironmentBaseDir="/dataDisk"
    export SourceDataBaseDir="/dataDisk"
elif [ "$Environment" == "PYRAVLOS6" ]; then 
    export EnvironmentBaseDir="/home/theofilos"
    export SourceDataBaseDir="/home/theofilos"
elif [ "$Environment" == "TELEIOS3" ]; then 
    export EnvironmentBaseDir="/home/journal"
elif [ "$Environment" == "NUC8I7BEH" ]; then 
    export EnvironmentBaseDir="/data"
fi

export MBInBytes=$((1024*1024))
export GBInBytes=$((1024*MBInBytes))
export SystemMemorySizeInGB=11 # default value, VM development machine
if [ "$Environment" == "VM" ]; then
    # common for all SUTs
    export SystemMemorySizeInGB=11
    export DatasetBaseDir="${EnvironmentBaseDir}/Geographica2_Datasets"
    export QuerysetBaseDir="${DatasetBaseDir}/QuerySets"
    export ResultsBaseDir="${EnvironmentBaseDir}/Results_Store/VM_Results"
    #export CompletionReportDaemonIP="127.0.0.1"
    export CompletionReportDaemonIP="10.0.2.15"
    export CompletionReportDaemonPort="3333"
    export JVM_Xmx="-Xmx8g"
    # GraphDBSUT only
    export GraphDBBaseDir="${EnvironmentBaseDir}/graphdb-${verGRAPHDB}"
    # RDF4JSUT only
    export RDF4JRepoBaseDir="${EnvironmentBaseDir}/RDF4J_${verRDF4J}_${RDF4JLuceneReposPrefix}Repos/server"
    # StrabonSUT only
    export StrabonBaseDir="/home/tioannid/NetBeansProjects/PhD/Strabon"
    export StrabonLoaderBaseDir="/media/sf_VM_Shared/PHD/NetBeansProjects/PhD/StrabonLoader"
    # VirtuosoSUT only
    export VirtuosoBaseDir="/home/tioannid/Downloads/virtuoso-opensource"
    export VirtuosoDataBaseDir="${VirtuosoBaseDir}/repos"
    # StardogSUT only
    export STARDOG_HOME="/home/tioannid/Downloads/StarDog" # directory where all Stardog databases and files will be stored
    # https://docs.stardog.com/operating-stardog/server-administration/capacity-planning#memory-usage
    export STARDOG_SERVER_JAVA_ARGS="-Xms3g -Xmx3g -XX:MaxDirectMemorySize=4g"
    # JenaGeoSPARQL only
    export JenaBaseDir="${EnvironmentBaseDir}/apache-jena-${verJENA}"
    export JenaGeoSPARQLRepoBaseDir="${EnvironmentBaseDir}/JenaGeoSPARQL_${verJENA}_Repos"
elif [ "$Environment" == "PAVILIONDV7" ]; then
    # common for all SUTs
    export SystemMemorySizeInGB=16
    export DatasetBaseDir="${SourceDataBaseDir}/Geographica2_Datasets"
    export QuerysetBaseDir="${DatasetBaseDir}/QuerySets"
    export ResultsBaseDir="${EnvironmentBaseDir}/Results_Store/PAVILIONDV7"
    export CompletionReportDaemonIP="192.168.1.66"
    export CompletionReportDaemonPort="3333"
    export JVM_Xmx="-Xmx8g"
    # GraphDBSUT only
    export GraphDBBaseDir="${EnvironmentBaseDir}/graphdb-${verGRAPHDB}"
    # RDF4JSUT only
    export RDF4JRepoBaseDir="${EnvironmentBaseDir}/RDF4J_${verRDF4J}_${RDF4JLuceneReposPrefix}Repos/server"
    # StrabonSUT only
    export StrabonBaseDir="${EnvironmentBaseDir}/Strabon"
    export StrabonLoaderBaseDir="${EnvironmentBaseDir}/StrabonLoader"
    # VirtuosoSUT only
    export VirtuosoBaseDir="${EnvironmentBaseDir}/virtuoso-opensource"
    export VirtuosoDataBaseDir="${VirtuosoBaseDir}/repos"
    # StardogSUT only
    export STARDOG_HOME="${EnvironmentBaseDir}/StarDog" # directory where all Stardog databases and files will be stored
    # https://docs.stardog.com/operating-stardog/server-administration/capacity-planning#memory-usage
    export STARDOG_SERVER_JAVA_ARGS="-Xms4g -Xmx4g -XX:MaxDirectMemorySize=6g"
    # JenaGeoSPARQL only
    export JenaBaseDir="${EnvironmentBaseDir}/apache-jena-${verJENA}"
    export JenaGeoSPARQLRepoBaseDir="${EnvironmentBaseDir}/JenaGeoSPARQL_${verJENA}_Repos"
elif [ "$Environment" == "PYRAVLOS6" ]; then 
    # common for all SUTs
    export SystemMemorySizeInGB=128
    export DatasetBaseDir="${EnvironmentBaseDir}/Geographica_Datasets"
    export QuerysetBaseDir="${DatasetBaseDir}/QuerySets"
    export ResultsBaseDir="${EnvironmentBaseDir}"
    export CompletionReportDaemonIP="10.0.10.16" # PYRAVLOS6
    export CompletionReportDaemonPort="3333"
    export JVM_Xmx="-Xmx64g"
    # GraphDBSUT only
    export GraphDBBaseDir="${EnvironmentBaseDir}/graphdb-${verGRAPHDB}"
    # RDF4JSUT only
    export RDF4JRepoBaseDir="${EnvironmentBaseDir}/RDF4J_${verRDF4J}_${RDF4JLuceneReposPrefix}Repos/server"
    # StrabonSUT only
    export StrabonBaseDir="${EnvironmentBaseDir}/Strabon"
    export StrabonLoaderBaseDir="${EnvironmentBaseDir}/StrabonLoader"
    # VirtuosoSUT only
    export VirtuosoBaseDir="${EnvironmentBaseDir}/virtuoso-opensource"
    export VirtuosoDataBaseDir="${VirtuosoBaseDir}/repos"
    # StardogSUT only
    export STARDOG_HOME="${EnvironmentBaseDir}/StarDog" # directory where all Stardog databases and files will be stored
    # https://docs.stardog.com/operating-stardog/server-administration/capacity-planning#memory-usage
    export STARDOG_SERVER_JAVA_ARGS="-Xms30g -Xmx30g -XX:MaxDirectMemorySize=80g"
    # JenaGeoSPARQL only
    export JenaBaseDir="${EnvironmentBaseDir}/apache-jena-${verJENA}"
    export JenaGeoSPARQLRepoBaseDir="${EnvironmentBaseDir}/JenaGeoSPARQL_${verJENA}_Repos"
elif [ "$Environment" == "TELEIOS3" ]; then 
    # common for all SUTs
    export SystemMemorySizeInGB=32
    export DatasetBaseDir="${EnvironmentBaseDir}/Geographica_Datasets"
    export QuerysetBaseDir="${DatasetBaseDir}/QuerySets"
    export ResultsBaseDir="${EnvironmentBaseDir}"
    export CompletionReportDaemonIP="10.0.10.13" # TELEIO4
    export CompletionReportDaemonPort="3333"
    export JVM_Xmx="-Xmx24g"
    # GraphDBSUT only
    export GraphDBBaseDir="${EnvironmentBaseDir}/graphdb-${verGRAPHDB}"
    # RDF4JSUT only
    export RDF4JRepoBaseDir="${EnvironmentBaseDir}/RDF4J_${verRDF4J}_${RDF4JLuceneReposPrefix}Repos/server"
    # StrabonSUT only
    export StrabonBaseDir="${EnvironmentBaseDir}/Strabon"
    export StrabonLoaderBaseDir="${EnvironmentBaseDir}/StrabonLoader"
    # VirtuosoSUT only
    export VirtuosoBaseDir="${EnvironmentBaseDir}/virtuoso-opensource"
    export VirtuosoDataBaseDir="${VirtuosoBaseDir}/repos"
    # StardogSUT only
    export STARDOG_HOME="${EnvironmentBaseDir}/StarDog" # directory where all Stardog databases and files will be stored
    # https://docs.stardog.com/operating-stardog/server-administration/capacity-planning#memory-usage
    export STARDOG_SERVER_JAVA_ARGS="-Xms8g -Xmx8g -XX:MaxDirectMemorySize=17g"
    # JenaGeoSPARQL only
    export JenaBaseDir="${EnvironmentBaseDir}/apache-jena-${verJENA}"
    export JenaGeoSPARQLRepoBaseDir="${EnvironmentBaseDir}/JenaGeoSPARQL_${verJENA}_Repos"
elif [ "$Environment" == "NUC8I7BEH" ]; then 
    # common for all SUTs
    export SystemMemorySizeInGB=32
    export DatasetBaseDir="${EnvironmentBaseDir}/Geographica2_Datasets"
    export QuerysetBaseDir="${DatasetBaseDir}/QuerySets"
    export ResultsBaseDir="${EnvironmentBaseDir}"
    export CompletionReportDaemonIP="192.168.1.44"
    export CompletionReportDaemonPort="3333"
    export JVM_Xmx="-Xmx24g"
    # GraphDBSUT only
    export GraphDBBaseDir="${EnvironmentBaseDir}/graphdb-${verGRAPHDB}"
    # RDF4JSUT only
    export RDF4JRepoBaseDir="${EnvironmentBaseDir}/RDF4J_${verRDF4J}_${RDF4JLuceneReposPrefix}Repos/server"
    # StrabonSUT only
    export StrabonBaseDir="${EnvironmentBaseDir}/Strabon"
    export StrabonLoaderBaseDir="${EnvironmentBaseDir}/StrabonLoader"
    # VirtuosoSUT only
    export VirtuosoBaseDir="${EnvironmentBaseDir}/virtuoso-opensource-7.2.14"
    export VirtuosoDataBaseDir="${VirtuosoBaseDir}/repos"
    # StardogSUT only
    export STARDOG_HOME="${EnvironmentBaseDir}/StarDog" # directory where all Stardog databases and files will be stored
    # https://docs.stardog.com/operating-stardog/server-administration/capacity-planning#memory-usage
    export STARDOG_SERVER_JAVA_ARGS="-Xms8g -Xmx8g -XX:MaxDirectMemorySize=17g"
    # JenaGeoSPARQL only
    export JenaBaseDir="${EnvironmentBaseDir}/apache-jena-${verJENA}"
    export JenaGeoSPARQLRepoBaseDir="${EnvironmentBaseDir}/JenaGeoSPARQL_${verJENA}_Repos"
fi

# GraphDB reads dynamically the data dir from the properties file
if [ "$ActiveSUT" == "GraphDBSUT" ]; then
    # read the GraphDB data directory from the config file
    GraphDB_Properties_File="${GraphDBBaseDir}/conf/graphdb.properties"
    matchedLine=`grep -e "^graphdb.home.data =" $GraphDB_Properties_File`
    export GraphDBDataDir="${matchedLine##*= }"
    # if graphdb.home.data is not explicitly set then assign default path
    if [ -z ${GraphDBDataDir} ]; then
            export GraphDBDataDir="${GraphDBBaseDir}/data"
    fi
fi

# Stardog specific
if [ "$ActiveSUT" == "StardogSUT" ]; then
    # StardogSUT only
    #  - The Stardog installation folder
    export StardogBaseDir="${EnvironmentBaseDir}/stardog-${verSTARDOG}"
    #  - The Stardog extensions folder
    export STARDOG_EXT="${StardogBaseDir}/server/ext" #  enable support for complex shapes with JTS jar
    #  - Create the $STARDOG_HOME if it doesn't exist
    if [ ! -d ${STARDOG_HOME} ]; then
        echo -e "${STARDOG_HOME} dir does not exist.\n\tCreating it now ..."
        mkdir -p ${STARDOG_HOME}
    fi
    export StardogLoadPropertyFile="${STARDOG_HOME}/stardog.properties.load"
    #  - Create the stardog.properties.load and stardog.properties.query files
    cat << EOF > ${StardogLoadPropertyFile}
# Enable support for JTS in the geospatial module
spatial.use.jts=true
# Bulk loading optimization properties
memory.mode=bulk_load
#strict.parsing=false
strict.parsing=true
# Disable query timeout
query.timeout=0s
# Increase database connection timeout from default=10m to the maximum expected
# query execution time which is 24h
#database.connection.timeout=24h
EOF
    export StardogQueryPropertyFile="${STARDOG_HOME}/stardog.properties.query"
    cat <<- EOF > ${StardogQueryPropertyFile}
# Enable support for JTS in the geospatial module
spatial.use.jts=true
# Query execution optimization properties
memory.mode=read_optimized
strict.parsing=true
# Disable query timeout
query.timeout=0s
# Increase database connection timeout from default=10m to the maximum expected
# query execution time which is 24h
#database.connection.timeout=24h
EOF
    #  - Make sure that Stradog instance is shutdown
    ${StardogBaseDir}/bin/stardog-admin server stop
fi

# Virtuoso must modify the template configuration file's parameters
# that are dependent on system memory and the input dataset location
if [ "$ActiveSUT" == "VirtuosoSUT" ]; then
    # calculate the NumberOfBuffers and MaxDirtyBuffers
    Virtuoso8KPageSizeInBytes=8700 # according to the documentation...
    NumberOfBuffers=$((SystemMemorySizeInGB*GBInBytes/Virtuoso8KPageSizeInBytes))
    NumberOfBuffers=$((NumberOfBuffers*5/10))
    export NumberOfBuffers=$( printf "%.0f" $NumberOfBuffers ) # round to int
    MaxDirtyBuffers=$((6*NumberOfBuffers/10)) # 60% of NumberOfBuffers
    export MaxDirtyBuffers=$( printf "%.0f" $MaxDirtyBuffers ) # round to int
fi

#   Dataset related variables
scalabilityDir="Scalability"
export ScalabilityGenScriptName="${DatasetBaseDir}/${scalabilityDir}/scalabilityDSGen.sh"
export ScalabilityGzipRefDSName="${DatasetBaseDir}/${scalabilityDir}/scalability500MRefDS.nt.gz"

# define Results base directory
export ExperimentResultDir="${ResultsBaseDir}/${ActiveSUT}/${ResultsDirName}"
