# SYNTAX :
#    <script> repoDir repoIndexes RDFFileType trigDir
SCRIPT_NAME=`basename "$0"`
SYNTAX="
SYNTAX: $SCRIPT_NAME <RDF4JRepoBaseDir> <RepoID> <JVM_Xmx> <upgradeLuceneIdxs>
\t<RDF4JRepoBaseDir>\t:\tbase directory where repo is stored,
\t<RepoID>\t:\trepo ID,
\t<JVM_Xmx>\t\t:\tJVM max memory e.g. -Xmx6g,
\t<upgradeLuceneIdxs> :\t Upgrade Lucene indexes"

# STEP 0: Find the directory where the script is located in
BASE="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# STEP 1: Validate the script's syntax
#      1.1: check number of arguments
if (( $# != 4 )); then
    echo -e "Illegal number of parameters $SYNTAX"
    exit 1
fi

#      1.2: assign arguments to variables
RDF4JRepoBaseDir=$1
#echo "RDF4JRepoBaseDir = ${RDF4JRepoBaseDir}"
RepoID=$2
#echo "RepoID = ${RepoID}"
JVM_Xmx=$3
#echo "JVM_Xmx = ${JVM_Xmx}"
upgradeLuceneIdxs=$4
#echo "upgradeLuceneIdxs = ${upgradeLuceneIdxs}"

#      1.3: check whether the directory (<RDF4JRepoBaseDir>) does not exists
dirs=(  "$RDF4JRepoBaseDir" )
for dir in "${dirs[@]}"; do
	if [ ! -d "$dir" ]; then
		echo -e "Directory \"$dir\" does not exist!"
		exit 2
	fi		
done

# STEP 2: Prepare options for LOG4J, JAVA VM, CLASS PATH and MAIN CLASS		

# define the configuration file for the Apache LOG4J framework, which is common
# for all Geographica and is located in the Runtime module
LOG4J_CONFIGURATION=${BASE}/../../../runtime/src/main/resources/log4j.properties
#echo "LOG4J_CONFIGURATION = $LOG4J_CONFIGURATION"

# define the JVM options/parameters
JAVA_OPTS="${JVM_Xmx} -Dlog4j.configuration=file:${LOG4J_CONFIGURATION}"
#echo "JAVA_OPTS = $JAVA_OPTS"

# change to the ../target directory to more easily create the classpath
cd ${BASE}/../../target
# define the class path
CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"

# define the executing-main class
MAIN_CLASS="gr.uoa.di.rdf.geordfbench.rdf4jsut.RepoUtil"
LUCENE_UPGRADE_CLASS="org.apache.lucene.index.IndexUpgrader"
luceneIndexDir=${RDF4JRepoBaseDir}/repositories/${RepoID}/luceneidx
#echo ${luceneIndexDir}

if [ "$upgradeLuceneIdxs" = true ]; then
    #define the lucene index upgrade command
    EXEC_UPGRADE_LUCENE_IDXS="java -cp $CLASS_PATH $LUCENE_UPGRADE_CLASS -delete-prior-commits -verbose ${luceneIndexDir}"
    #echo ${EXEC_UPGRADE_LUCENE_IDXS}
    eval ${EXEC_UPGRADE_LUCENE_IDXS}
fi

# define the run command to QUERY_1 REPO
EXEC_QUERY_1_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS queryman \"${RDF4JRepoBaseDir}\" $RepoID 1"
#echo $EXEC_QUERY_1_REPO

# define the run command to QUERY_2 REPO
EXEC_QUERY_2_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS queryman \"${RDF4JRepoBaseDir}\" $RepoID 2"
#echo $EXEC_QUERY_2_REPO

# define the run command to QUERY_3 REPO
EXEC_QUERY_3_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS queryman \"${RDF4JRepoBaseDir}\" $RepoID 3"
#echo $EXEC_QUERY_3_REPO

# define the run command to QUERY_4 REPO
EXEC_QUERY_4_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS queryman \"${RDF4JRepoBaseDir}\" $RepoID 4"
#echo $EXEC_QUERY_4_REPO

# define the run command to QUERY_5 REPO
EXEC_QUERY_5_REPO="java $JAVA_OPTS -cp $CLASS_PATH $MAIN_CLASS queryman \"${RDF4JRepoBaseDir}\" $RepoID 5"
#echo $EXEC_QUERY_5_REPO

# execute commnads
echo -e "Validating repo \"${RDF4JRepoBaseDir}/repositories/${RepoID}\""
echo -e "QUERY 1: Total Number of triples"
eval ${EXEC_QUERY_1_REPO}
echo -e "\nQUERY 2: Number of triples Per Graph"
eval ${EXEC_QUERY_2_REPO}
echo -e "\nQUERY 3: Find geometries that intersect with given point"
eval ${EXEC_QUERY_3_REPO}
echo -e "\nQUERY 4: Find geometries(excluding problematic!) that intersect with given point"
eval ${EXEC_QUERY_4_REPO}
echo -e "\nQUERY 5: Find the buffer of literal point"
eval ${EXEC_QUERY_5_REPO}

exit 0