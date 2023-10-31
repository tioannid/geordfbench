#!/bin/bash

# change to the rdf4j_provider folder
cd ../rdf4j_provider

# install 3rd party JARs, following instructions from https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html
# use "java -jar xxx.jar" to find the version number and then use it in the following commands

# install Virtuoso RDF4J 2.x Provider JAR file
mvn install:install-file -Dfile=virt_rdf4j.jar -DgroupId=com.virtuoso.openlink -DartifactId=virt_rdf4j -Dversion=2.1.4 -Dpackaging=jar

# change to lib directory
cd lib

# install Virtuoso JDBC 4 Driver JAR file 
mvn install:install-file -Dfile=virtjdbc4_2.jar -DgroupId=com.virtuoso.openlink -DartifactId=virtjdbc4_2 -Dversion=4.2 -Dpackaging=jar
