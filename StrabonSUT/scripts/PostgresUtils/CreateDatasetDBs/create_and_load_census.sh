# SYNTAX: <scriptname>
# Precondition 1 : there must exist ~/Strabon
# Precondition 2 : Postgres should be installed and its binaries exist in the PATH

if [ -z ${DatasetBaseDir+x} ] || [ -z ${StrabonBaseDir+x} ] || [ -z ${JVM_Xmx+x} ]; then
    # One or more or the required variables has not been initialized
    # check the number of arguments ...
    echo -e "Some of the following environment variables (Geographica/scripts/prepareRunEnvironment.sh) are missing:
\t{DatasetBaseDir, StrabonBaseDir, JVM_Xmx}";
        # script cannot proceed,
     exit 1    # return instead of exit because we need to source the script
fi

DBName=census
# recreate database <arg1> and optimize
sudo -u postgres `which dropdb` ${DBName}
sudo -u postgres `which createdb` ${DBName} -T template_postgis
sudo -u postgres `which psql` -c 'VACUUM ANALYZE;' ${DBName}

# create the class path for java
cd ${StrabonBaseDir}/runtime/target
export CLASS_PATH="$(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done;echo $myVar;)"

FilesDir=${DatasetBaseDir}/Census/NO_CRS
# load N-Triple files in different contexts
java ${JVM_Xmx} -cp $CLASS_PATH  eu.earthobservatory.runtime.postgis.StoreOp localhost 5432 ${DBName} postgres postgres "${2}/census.new_york_roads.rdf.NO_CRS.nt" -g ""

# verify that each graph has the expected number of triples
#java ${JVM_Xmx} -cp $CLASS_PATH eu.earthobservatory.runtime.postgis.QueryOp localhost 5432 ${DBName} postgres postgres "SELECT ?g (count(*) as ?count) WHERE { GRAPH ?g {?s ?p ?o .} } GROUP BY ?g" TRUE

# return to home directory
cd ~
