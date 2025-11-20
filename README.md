# GeoRDFBench Framework: Geospatial Semantic Benchmarking Simplified

[![License](https://img.shields.io/badge/license-Apache2.0-green)](./LICENSE)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.16598753.svg)](https://doi.org/10.5281/zenodo.16598753)
[![Website](https://img.shields.io/badge/website-GeoRDFBench_Framework-yellow)](https://geordfbench.di.uoa.gr/)
[![JSON Specification UI Endpoint](https://img.shields.io/badge/docs-JSON_Specs_UI_Endpoint-green)](https://geordfbench.di.uoa.gr/jsonui/)
[![JSON Specification Library Endpoint](https://img.shields.io/badge/docs-JSON_Specs_Lib_Endpoint-red)](https://geordfbench.di.uoa.gr/jsonapi/api-docs/)
[![Paper](https://img.shields.io/badge/preprint-GeoRDFBench_Framework_Paper-pink)](https://doi.org/10.5281/zenodo.7906349)

**GeoRDFBench** can be used by researchers and practitioners interested in Semantic Benchmarking with SPARQL language. The framework however provides unique features for the geospatial aspects of RDF stores that support the GeoSPARQL standard. Documentation for the project is maintained at our [GeoRDFBench web site](https://geordfbench.di.uoa.gr/).

For a quick acquaintance with the platform, please run the dockerized example at [dockerized example](https://geordfbench.di.uoa.gr/rdf4j_scal10k_workload_docker_iframe.html) which exhibits running a GeoSPARQL experiment with the [RDF4J](https://rdf4j.org/) RDF store against the _Scalability 10K_ dataset of the [Geographica 2](https://geographica2.di.uoa.gr/) benchmark.

The [GeoRDFBench Framework Samples](https://github.com/tioannid/geordfbench_samples) is a maven project using GeoRDFBench's **runtime** as dependence and showcases the use of the JSON Generator's API for constructing the workload specification for the [LUBM(1, 0)](https://swat.cse.lehigh.edu/projects/lubm/) benchmark.

The GeoRDFBench web site also provides the [Multiple stores against LUBM(1, 0) Workload](https://geordfbench.di.uoa.gr/multistore_lubm_1_0_workload_docker_iframe.html) dockerized example showcasing the use of GeoRDFBench to run experiments with _RDF4J_, _GraphDB_ and _JenaGeoSPARQL_ against **LUBM(1, 0)** workload.

## News

The new **v2.2.0** [JSON Specification Library Endpoint](https://geordfbench.di.uoa.gr/jsonapi/) module. Its main features are:

- It's a JavaScript module running on [Node.js](https://nodejs.org) JS runtime environment and takes advantage of the [Express](https://expressjs.com) web framework.
- It allows the deployment of the JSON Specification Library to a remote server, different from the experiments server, therefore promoting a more distributed approach to the GeoRDFBench Framework's operation.
- The user can choose the **JSON Specification Library** location which holds the **json_defs** structure, the endpoint's **listening port** and configure which **CRUD operations** are enabled for each specification category. The implemented operations are: _(**C**)reate, (**R**)ead and (**D**)elete_. Since the _(**U**)pdate_ operation can impair the desired immutability of reference specifications, a more safe _(**Copy/Clone**)_ operation is offered by the new **JSON Specification UI Endpoint** to subtitute that.
- The experiment JSON specification arguments can now also be REST API URLs from the endpoint instead of file paths. An example can be found in the _RunRDF4JExperimentRemoteSpecsTest_ test class of the RDF4JSUT module.
- The user can find details for the API in the [Swagger (OpenAPI Specification) documentation](https://geordfbench.di.uoa.gr/jsonapi/api-docs/)
- The endpoint implements a REST API which currently allows GET operations, such as:
  - Get a list of available [Specification categories](https://geordfbench.di.uoa.gr/jsonapi/categories)
  - Get the [Endpoint's configuration](https://geordfbench.di.uoa.gr/jsonapi/config)
  - Get a list of available [Datasets](https://geordfbench.di.uoa.gr/jsonapi/datasets)
  - Get the JSON spec for dataset [LUBM_1_0DSoriginal](https://geordfbench.di.uoa.gr/jsonapi/datasets/LUBM_1_0DSoriginal.json)
  - Get a list of available [Querysets](https://geordfbench.di.uoa.gr/jsonapi/querysets)
  - Get the JSON spec for queryset [scalabilityFuncQSoriginal](https://geordfbench.di.uoa.gr/jsonapi/querysets/scalabilityFuncQSoriginal.json)
  - Get a list of available [Execution Specs](https://geordfbench.di.uoa.gr/jsonapi/executionspecs)
  - Get the JSON spec for execution specification [scalabilityESoriginal](https://geordfbench.di.uoa.gr/jsonapi/executionspecs/scalabilityESoriginal.json)
  - ...

The new **v2.4.0** [JSON Specification UI Endpoint](https://geordfbench.di.uoa.gr/jsonui/) module. Its main features are:

- It's a JavaScript module running on [Node.js]("https://nodejs.org/) JS runtime environment and takes advantage of the [Express](https://expressjs.com/) web framework.
- It provides a user interface through which the user can manage a JSON Specification Library (through the Library Endpoint).
- It uses [Pug (formerly known as Jade)](https://pugjs.org/) high performance template engine for Node.js to server dynamic forms to the user.
- It can be hosted to the same or different server hosting the JSON Specification Library Endpoint. It can be hosted to the same or different server from the experiments server, therefore promoting a more distributed approach to the GeoRDFBench Framework's operation.
- The user can configure the JSON Specification Library Endpoint which is managed.
- The user can choose the endpoint's listening port.
- The endpoint offers the following operations, such as:
  - Get the status of the JSON Specification Library and UI Endpoints.
  - Get the Swagger (OpenAPI Specification) documentation of the JSON Specification Library Endpoint.
  - Get the list of available [Specification categories](https://geordfbench.di.uoa.gr/jsonui/categories)
  - Get the List, Create new, Copy from existing, Delete of Report Specs, Hosts, Execution Specs, Datasets and Querysets.
  - ...(Workloads to be added shortly!)

## Short Term Next Steps

- New capabilities:

  - add multi-user capabilities (user authentication and authorization) to the **JSON Specification UI Endpoint** which will allow each user their own copy of the JSON Specification Library,

- New RDF Modules for SPARQL/GeoSPARQL:

  - [Neo4J](https://neo4j.com/) with [neosemantics (n10s)](https://neo4j.com/labs/neosemantics/) plugin

## Medium Term Next Steps

- New capabilities:

  - **inference** support for RDF modules
  - support for the **Hadoop file system**,
  - a **fourth Spark-based framework API**, so that Spark-based distributed GeoSPARQL solutions,
    such as [Strabo 2](http://cgi.di.uoa.gr/~koubarak/publications/2022/strabo2.pdf) can be tested, and

- New RDF Modules for SPARQL/GeoSPARQL:

  - [Anzo Graph](https://docs.cambridgesemantics.com/anzo/v5.4/userdoc/Home.htm) by Cambridge Semantics (now Altair),

- New Semantic Languages:
  - [Graph Query Language (GQL)](https://www.gqlstandards.org/) for running benchmarks on native LPGs

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

Current limitations include the _non automatic serialization/deserialization of
the benchmark environment (host, os, RDF store components) specifications_ and
the _lack of support for distributed RDF stores_.
