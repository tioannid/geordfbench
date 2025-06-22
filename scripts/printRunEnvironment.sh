#!/bin/bash

# common for all SUTs
echo "All SUTs"
echo "--------"
echo "Environment = $Environment"
echo "EnvironmentBaseDir = $EnvironmentBaseDir"
echo "GeoRDFBenchScriptsDir = $GeoRDFBenchScriptsDir"
echo "GeoRDFBenchJSONLibDir = $GeoRDFBenchJSONLibDir"
echo "DatasetBaseDir = $DatasetBaseDir"
echo "QuerysetBaseDir = $QuerysetBaseDir"
echo "ResultsBaseDir = $ResultsBaseDir"
echo "ResultsDirName = $ResultsDirName"
echo "ActiveSUT = $ActiveSUT"
echo "ExperimentResultDir = $ExperimentResultDir"
echo "ExperimentDesc = $ExperimentDesc"
echo "CompletionReportDaemonIP = $CompletionReportDaemonIP"
echo "CompletionReportDaemonPort = $CompletionReportDaemonPort"
echo "ScalabilityGenScriptName = ${ScalabilityGenScriptName}"
echo "ScalabilityGzipRefDSName = ${ScalabilityGzipRefDSName}"
echo "SystemMemorySizeInGB = ${SystemMemorySizeInGB} GBs"
echo "JVM_Xmx = $JVM_Xmx"
echo ""

# GraphDBSUT only
if [[ $ActiveSUT == "GraphDBSUT" ]]; then
	echo "GraphDB SUT"
	echo "-----------"
	echo "GraphDBBaseDir = $GraphDBBaseDir"
	echo "GraphDBDataDir = $GraphDBDataDir"
	echo "EnableGeoSPARQLPlugin = $EnableGeoSPARQLPlugin"
	echo "IndexingAlgorithm = $IndexingAlgorithm"
	echo "IndexingPrecision = $IndexingPrecision"
	echo "Version = ${verGRAPHDB}"
	echo ""
fi

# RDF4JSUT only
if [[ $ActiveSUT == "RDF4JSUT" ]]; then
	echo "RDF4J SUT"
	echo "---------"
	echo "RDF4JRepoBaseDir = $RDF4JRepoBaseDir"
	echo "EnableLuceneSail = $EnableLuceneSail"
	echo "RDF4JLuceneReposPrefix = ${RDF4JLuceneReposPrefix}"
	echo "Version = ${verRDF4J}"
	echo ""
fi

# StrabonSUT only
if [[ $ActiveSUT == "StrabonSUT" ]]; then
	echo "Strabon SUT"
	echo "-----------"
	echo "StrabonBaseDir = $StrabonBaseDir"
	echo "StrabonLoaderBaseDir = $StrabonLoaderBaseDir"
	echo "Version = ${verSTRABON}"
	echo ""
fi

# Virtuoso SUT only
if [[ $ActiveSUT == "VirtuosoSUT" ]]; then
	echo "Virtuoso SUT"
	echo "-----------"
	echo "VirtuosoBaseDir = $VirtuosoBaseDir"
	echo "VirtuosoDataBaseDir = $VirtuosoDataBaseDir"
	echo "VirtuosoTemplateConfigurationFile = $VirtuosoTemplateConfigurationFile"
	echo "NumberOfBuffers = ${NumberOfBuffers} 8k-pages"
	echo "MaxDirtyBuffers = ${MaxDirtyBuffers} 8k-pages"
	echo "Version = ${verVIRTUOSO}"
	echo ""
fi

# StardogSUT only
if [[ $ActiveSUT == "StardogSUT" ]]; then
	echo "Stardog SUT"
	echo "-----------"
	echo "StardogBaseDir = $StardogBaseDir"
	echo "STARDOG_HOME = $STARDOG_HOME"
	echo "StardogSpatialPrecision = $StardogSpatialPrecision"
	echo "STARDOG_EXT = $STARDOG_EXT"
	echo "StardogLoadPropertyFile = $StardogLoadPropertyFile"
	echo "StardogQueryPropertyFile = $StardogQueryPropertyFile"
	echo "STARDOG_SERVER_JAVA_ARGS = ${STARDOG_SERVER_JAVA_ARGS}"
	echo "Version = ${verSTARDOG}"
	echo ""
fi

# JenaGeoSPARQLSUT only
if [[ $ActiveSUT == "JenaGeoSPARQLSUT" ]]; then
	echo "JenaGeoSPARQL SUT"
	echo "-----------------"
	echo "JenaBaseDir = $JenaBaseDir"
	echo "JenaGeoSPARQLRepoBaseDir = $JenaGeoSPARQLRepoBaseDir"
	echo "Version = ${verJENA}"
	echo ""
fi
