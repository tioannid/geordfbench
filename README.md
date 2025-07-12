# GeoRDFBench Framework: Geospatial Semantic Benchmarking Simplified

[![License](https://img.shields.io/badge/license-Apache2.0-green)](./LICENSE)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.13244137.svg)](https://doi.org/10.5281/zenodo.13244137)
[![Website](https://img.shields.io/badge/website-GeoRDFBench_Framework-yellow)](https://geordfbench.di.uoa.gr/)
[![JSON Specs Endpoint](https://img.shields.io/badge/docs-JSON_Specs_Endpoint-red)](https://geordfbench.di.uoa.gr/jsonapi/api-docs/)

**GeoRDFBench** can be used by researchers and practitioners interested in Semantic Benchmarking with SPARQL language. The framework however provides unique features for the geospatial aspects of RDF stores that support the GeoSPARQL standard. Documentation for the project is maintained at our [GeoRDFBench web site](https://geordfbench.di.uoa.gr/). 

For a quick acquaintance with the platform, please run the dockerized example at [dockerized example](https://geordfbench.di.uoa.gr/rdf4j_scal10k_workload_docker_iframe.html) which exhibits running a GeoSPARQL experiment with the [RDF4J](https://rdf4j.org/) RDF store against the *Scalability 10K* dataset of the [Geographica 2](https://geographica2.di.uoa.gr/) benchmark.

The [GeoRDFBench Framework Samples](https://github.com/tioannid/geordfbench_samples) is a maven project using GeoRDFBench's **runtime** as dependence and showcases the use of the JSON Generator's API for constructing the workload specification for the [LUBM(1, 0)](https://swat.cse.lehigh.edu/projects/lubm/) benchmark.

The GeoRDFBench web site also provides the [Multiple stores against LUBM(1, 0) Workload](https://geordfbench.di.uoa.gr/multistore_lubm_1_0_workload_docker_iframe.html) dockerized example showcasing the use of GeoRDFBench to run experiments with *RDF4J*, *GraphDB* and *JenaGeoSPARQL* against **LUBM(1, 0)** workload.

## News

There is a new [JSON Specification Library Endpoint](https://geordfbench.di.uoa.gr/jsonapi/) module. Its main features are:
     
* It's a JavaScript module running on [Node.js](https://nodejs.org) JS runtime environment and takes advantage of the [Express](https://expressjs.com) web framework.
* It allows the deployment of the JSON Specification Library to a remote server, different from the experiments server, therefore promoting a more distributed approach to the GeoRDFBench Framework's operation.
* The user can choose the JSON Specification Library location which holds the **json_defs** structure.
* The user can choose the endpoint's listening port.
* The experiment JSON specification arguments can now also be REST API URLs from the endpoint instead of file paths. An example can be found in the *RunRDF4JExperimentRemoteSpecsTest* test class of the RDF4JSUT module.
* The user can find details for the API in the [Swagger (OpenAPI Specification) documentation](https://geordfbench.di.uoa.gr/jsonapi/api-docs/)
* The endpoint implements a REST API which currently allows GET operations, such as:
    * Get a list of available [Specification categories](https://geordfbench.di.uoa.gr/jsonapi/categories)
    * Get a list of available [Datasets](https://geordfbench.di.uoa.gr/jsonapi/datasets)
    * Get the JSON spec for dataset [LUBM_1_0DSoriginal](https://geordfbench.di.uoa.gr/jsonapi/datasets/LUBM_1_0DSoriginal.json)
    * Get a list of available [Querysets](https://geordfbench.di.uoa.gr/jsonapi/querysets)
    * Get the JSON spec for queryset [scalabilityFuncQSoriginal](https://geordfbench.di.uoa.gr/jsonapi/querysets/scalabilityFuncQSoriginal.json)
    * Get a list of available [Execution Specs](https://geordfbench.di.uoa.gr/jsonapi/executionspecs)
    * Get the JSON spec for execution specification [scalabilityESoriginal](https://geordfbench.di.uoa.gr/jsonapi/executionspecs/scalabilityESoriginal.json)
    * ...

## Next Steps

* New capabilities:
    * a __user interface module__ to be used as a front-end with the new JSON Specs Endpoint which will further assist the interactive generation and modification of JSON specifications,
    * implement the remaining Create, Update, Delete **endpoint module's** operations on JSON specification libraries,
    * support for the **Hadoop file system**,
    * a **fourth Spark-based framework API**, so that Spark-based distributed GeoSPARQL solutions, 
        such as [Strabo 2](http://cgi.di.uoa.gr/~koubarak/publications/2022/strabo2.pdf) can be tested, and
    * **inference** support for RDF modules

* New RDF Modules for SPARQL/GeoSPARQL:
    * [Anzo Graph](https://docs.cambridgesemantics.com/anzo/v5.4/userdoc/Home.htm) by Cambridge Semantics (now Altair),
    * [Neo4J](https://neo4j.com/) with [neosemantics (n10s)](https://neo4j.com/labs/neosemantics/) plugin

* New Semantic Languages:
    * [Graph Query Language (GQL)](https://www.gqlstandards.org/) for running benchmarks on native LPGs


## Sustainability plan and limitations

A full list of issues can be found [here](https://github.com/tioannid/geordfbench/issues?q=is%3Aissue).
Their status can be followed up in a [GitHub Project](https://github.com/users/tioannid/projects/3).

GeoRDFBench's runtime was created to support:
- `RDF store integration`: fast integration of new or updated RDF stores by 
<ins>incorporating the common logic of working with RDF repositories through any 
of the well-known RDF Java Frameworks</ins>: **OpenRDF Sesame**, **Eclipse RDF4J**, 
**Apache Jena**.
- `Benchmark specification Ser/Der`: easy construction of **GeoSPARQL/SPARQL
benchmark specifications** and even easier **serialization/deserialization of 
these JSON** specification files, increasing the **reusability of these 
specifications** and equally important improving the **repeatability of 
experiments** and **confidence on experiment results**.

Current limitations include the *non automatic serialization/deserialization of 
the benchmark environment (host, os, RDF store components) specifications* and 
the *lack of support for distributed RDF stores*.