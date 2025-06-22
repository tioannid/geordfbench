#! /bin/bash

# Stores N-Triples in Virtuoso

# SYNTAX :
#    <script>


# STEP 0: Script related stuff & Initialization

# 0.1: Find the script name and directory where the script is located in
SCRIPT_NAME=`basename "$0"`
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 0.2: Define any utility functions()
function timer()
{
    if [[ $# -eq 0 ]]; then
        echo $(date '+%s')
    else
        local  stime=$1
        etime=$(date '+%s')

        if [[ -z "$stime" ]]; then stime=$etime; fi

        dt=$((etime - stime))
        ds=$((dt % 60))
        dm=$(((dt / 60) % 60))
        dh=$((dt / 3600))
        printf '%d:%02d:%02d' $dh $dm $ds
    fi
}

function help() {
	echo "Usage: $SCRIPT_NAME [options] <tripleFileDir>"
	echo "Store files in named graphs in Virtuoso. No default graph exists!"
	echo "The <tripleFileDir> should be listed in the 'DirsAllowed' line in virtuoso.ini"
	echo "	-U, --username:   username (default: dba)"
	echo "	-P, --password:   password (default: dba)" 
	echo "	-d, --database:   database (default: database)"
        echo "  -c, --config:     configuration file (default: virtuoso.ini)"
        echo "  -m, --mapcontext: context map file (default: map_to_contexts.txt)"
	echo "	-h, --help:		print help"
}

# 0.3: Define Constants
DEFAULT_CONTEXTMAP_FILE="map_to_contexts.txt"
DEFAULT_INI="virtuoso.ini"
DEFAULT_CONTEXT="<http://geographica.gr>"
VSO_STARTUP_DELAY=15
ADMINSCRIPTSDIR="${BASE}/../Admin"

# 0.4: Define default values for variables
db="database"
username="dba"
password="dba"
config=virtuoso_geographica.ini # template config file with placeholders
contextsmapfile=${DEFAULT_CONTEXTMAP_FILE}

# 0.5: Consume all script options (pairs of arguments -option <value>
# If arguments exist, check the 1st argument for short or long versions
# of options
while  [ $# -gt 0 ] && [ "${1:0:1}" == "-" ]; do
	case ${1} in
		-h | --help)
			help
			exit
		;;
		-d | --database)
			db=${2}
			shift; shift
		;;
		-U | --username)
			username=${2}
			shift; shift
		;;
		-P | --password)
			password=${2}
			shift; shift
		;;
                -c | --config)
                        config=${2}
                        shift; shift
		;;
                -m | --mapcontext)
                        contextsmapfile=${2}
                        shift; shift
	esac
done

# 0.6: Using Geographica exported variables define final file & directory locations
dbPath="${VirtuosoDataBaseDir}/${db}"
#echo "dbPath = $dbPath"
binPath="${VirtuosoBaseDir}/bin"
#echo "binPath = $binPath"
contextmapfilePath=${GeoRDFBenchScriptsDir}/${contextsmapfile}
#echo ${contextmapfilePath}

# STEP 1: Validate the script's parameters
# 1.1: Check number of remaining arguments
if (( $# != 1 )); then
        help
	exit 1
fi

# 1.2: Assign arguments to variables
TripleFileDir=${1}
#echo "TripleFileDir = ${TripleFileDir}"

# 1.3: Check whether the directory (<TripleFileDir>) do not exist
if [ ! -d "${TripleFileDir}" ]; then
        echo -e "Directory \"${TripleFileDir}\" does not exist.\nNo Triple files to load!"
        exit 2
fi		

# STEP 2: Create database & start Virtuoso
# 2.1: Create database folder, copy to it the template config file and modify the placeholders
echo "Creating Virtuoso database: ${dbPath}"
if test ! -d ${dbPath}; then
	echo "Creating directory ${dbPath}"
        mkdir -p "${dbPath}" 2>/dev/null
        cp ${BASE}/${config} ${dbPath}/${DEFAULT_INI}
        # update the DirsAllowed parameter by replacing the DatasetBaseDir placeholder
        # with the value of the enviroment variable set by prepareRunEnvironment.sh
        sed -i -e 's@DatasetBaseDir@'"${DatasetBaseDir}"'@g' ${dbPath}/${DEFAULT_INI}
        # update the NumberOfBuffers, MaxDirtyBuffers parameters by replacing the
        # appropriate lines with the value of the enviroment variables set by 
        # sourcing the prepareRunEnvironment.sh script
        sed -i -e 's@^.*NumberOfBuffers.*$@NumberOfBuffers='"$NumberOfBuffers"'@g' ${dbPath}/${DEFAULT_INI}
        sed -i -e 's@^.*MaxDirtyBuffers.*$@MaxDirtyBuffers='"$MaxDirtyBuffers"'@g' ${dbPath}/${DEFAULT_INI}
fi

echo -e "${ADMINSCRIPTSDIR}/start.sh ${binPath} ${dbPath} ${VSO_STARTUP_DELAY}"
#exit 0;

# 2.2: Startup Virtuoso instance from the database directory
# TODELETE: cd ${dbPath}
${ADMINSCRIPTSDIR}/start.sh ${binPath} ${dbPath} ${VSO_STARTUP_DELAY} 

# STEP 3: Load all N-Triple (*.nt) files in $TripleFileDir to the database
# 3.1: Change working directory to $NtripleDir
cd ${TripleFileDir}
# 3.2: check if ${contextmapfilePath} file exists
if [ -e ${contextmapfilePath} ]; then
        MapToContextFile_Exists=1
else
        MapToContextFile_Exists=0
fi

# 3.3: For each N-Triple file in the $TripleFileDir create the graphs
t1=$(timer)
for file in *.nt; do 
    # 3.3.1: ${file} <=> dir/${filename}
    filename=$(basename "${file}");
    # 3.3.2: ${filename} <=> ${fname}.${extension}
    extension="${filename##*.}"; 
    fname="${filename%.*}"; 
    # 3.3.3: if $MapToContextFile_Exists ...
    if [ $MapToContextFile_Exists -eq 1 ]; then
        # 3.3.3.1: Get the graph name for the file, if there is one...
        matchedline=`grep -e ${file} ${contextmapfilePath}`
        if [ "x${matchedline}" != "x" ]; then
            # echo "File $i found in file ${contextmapfilePath} in line : $matchedline"
            namedGraph=`echo -e "$matchedline" | awk -F"\t" ' { printf $2 }'`     
            # echo "Corresponding context is : ${namedGraph}"
            # 3.3.3.2: Strip the < > characters from namedGraph
            namedGraph=${namedGraph%>}
            namedGraph=${namedGraph#<}
            # echo "Corresponding context is : $namedGraph"
            # 3.3.3.3: Create '${file}.graph' with ${namedGraph} as its contents
            echo ${namedGraph} > ${file}.graph
            # 3.3.3.4: Create named graph in Virtuoso database
            ${binPath}/isql 1111 ${username} ${password} exec="sparql create graph <${namedGraph}>;"
        fi
    fi
done
# 3.3.4: Create default graph file for files that do not have a graph
echo ${DEFAULT_CONTEXT} > global.graph

# 3.4: Create extra full index, bulk load files

# The OGPS full index is created by default, but for queries that
# Graph and Predicate are provided but Subject is not, we can 
# improve performance by creating the GPOS full index
${binPath}/isql -U ${username} -P ${password} exec="CREATE BITMAP INDEX RDF_QUAD_GPOS ON DB.DBA.RDF_QUAD (G, P, O, S) PARTITION (O VARCHAR (-1, 0hexffff));"
t01=$(timer)
echo "Create PGOS: $((t01-t1))secs"
${binPath}/isql -U ${username} -P ${password} exec="ld_dir('${TripleFileDir}', '*.nt', '');"
t02=$(timer)
echo "Adds files to control list set up in the virtuoso.ini file: $((t02-t01)) secs"
${binPath}/isql -U ${username} -P ${password} exec="rdf_loader_run();"
t03=$(timer)
echo "RDF Bulk load: $((t03-t02)) secs"
wait
t04=$(timer)
echo "Wait: $((t04-t03)) secs"
t05=$(timer)
#${binPath}/isql -U ${username} -P ${password} exec="statistics DB.DBA.RDF_QUAD;"
t06=$(timer)
#echo "Gather statistics: $((t06-t05))"
${binPath}/isql -U ${username} -P ${password} exec="checkpoint;"
t2=$(timer)
echo "Checkpoint: $((t2-t06)) secs"
echo "Files loaded:"
${binPath}/isql -U ${username} -P ${password} exec="select * from DB.DBA.load_list"
echo "Storing time all: $((t2-t1)) secs"

cd ${dbPath}
${ADMINSCRIPTSDIR}/stop.sh

