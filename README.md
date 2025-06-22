# GeoRDFBench Framework: Geospatial Semantic Benchmarking Simplified

[![License](https://img.shields.io/badge/license-Apache2.0-green)](./LICENSE)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.13244137.svg)](https://doi.org/10.5281/zenodo.13244137)
[![Website](https://img.shields.io/badge/website-GeoRDFBench_Framework-yellow)](https://geordfbench.di.uoa.gr/)
[![Data generator docs](https://img.shields.io/badge/docs-Data_generator-red)](https://github.com/kg-construct/KROWN/blob/main/data-generator/README.md)
[![Execution framework docs](https://img.shields.io/badge/docs-Execution_framework-red)](https://github.com/kg-construct/KROWN/blob/main/execution-framework/README.md)

**GeoRDFBench** can be used by researchers and practitioners interested in Semantic Benchmarking with SPARQL language. The framework however provides unique features for the geospatial aspects of RDF stores that support the GeoSPARQL standard. Documentation for the project is maintained at our [GeoRDFBench web site](https://geordfbench.di.uoa.gr/). 

For a quick acquaintance with the platform, please run the dockerized example at [dockerized example](https://geordfbench.di.uoa.gr/rdf4j_scal10k_workload_docker_iframe.html) which exhibits running a GeoSPARQL experiment with the [RDF4J](https://rdf4j.org/) RDF store against the *Scalability 10K* dataset of the [Geographica 2](https://geographica2.di.uoa.gr/) benchmark.

The [GeoRDFBench Framework Samples](https://github.com/tioannid/geordfbench_samples) is a maven project using GeoRDFBench's **runtime** as dependence and showcases the use of the JSON Generator's API for constructing the workload specification for the [LUBM(1, 0)](https://swat.cse.lehigh.edu/projects/lubm/) benchmark.

The GeoRDFBench web site also provides the [Multiple stores against LUBM(1, 0) Workload](https://geordfbench.di.uoa.gr/multistore_lubm_1_0_workload_docker_iframe.html) dockerized example showcasing the use of GeoRDFBench to run experiments with *RDF4J*, *GraphDB* and *JenaGeoSPARQL* against **LUBM(1, 0)** workload.

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