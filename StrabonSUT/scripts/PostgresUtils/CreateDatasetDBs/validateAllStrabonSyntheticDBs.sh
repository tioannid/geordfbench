# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# != 3 )); then
    # in case no arguments are present there might be environment variables defined
    # globally ! Please check and then exit if necessary
    if [ -z ${DatasetBaseDir+x} ] || [ -z ${StrabonBaseDir+x} ] || [ -z ${JVM_Xmx+x} ]; then
        echo -e "Illegal number of parameters $SYNTAX"
	echo "As an alternative, some or all of the following environment variables {DatasetBaseDir, StrabonBaseDir, JVM_Xmx} is/are not set";
	return 1    # return instead of exit because we need to source the script
    fi
else
        export DatasetBaseDir=${1}
	export StrabonBaseDir=${2}
	export JVM_Xmx=${3}
fi

# get the directory where the script is located
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "`date`\n"

# create the class path for Strabon StoreOp and QueryOp classes
cd ${StrabonBaseDir}/runtime/target
export CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"
#echo $CLASS_PATH

levels=(  "512" )
#levels=(  "512" "768" "1024" "2048" )
for level in "${levels[@]}"; do
        DatabaseName=synthetic_${level}
        # recreate database <arg1> and optimize
        #sudo -u postgres `which dropdb` ${DatabaseName}
        #sudo -u postgres `which createdb` ${DatabaseName} -T template_postgis
        sudo -u postgres `which psql` -c 'VACUUM ANALYZE;' ${DatabaseName}
        # load all N-Triple file
        #for file in `ls -1 ${DatasetBaseDir}/Synthetic/${level}/*.nt`; do
        #    java ${JVM_Xmx} -cp $CLASS_PATH eu.earthobservatory.runtime.postgis.StoreOp localhost 5432 ${DatabaseName} postgres postgres ${file}
        #done

        echo "Querying database \"${DatabaseName}\" to verify the number of entries..."
        # verify that each graph has the expected number of triples
        java ${JVM_Xmx} -cp $CLASS_PATH eu.earthobservatory.runtime.postgis.QueryOp localhost 5432 ${DatabaseName} postgres postgres "SELECT (count(*) as ?count) WHERE { ?s ?p ?o . }" TRUE
done

# return to base directory
cd $BASE
