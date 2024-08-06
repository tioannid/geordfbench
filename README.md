# GeoRDFBench Framework
> Geospatial Semantic Benchmarking Simplified

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.13243858.svg)](https://doi.org/10.5281/zenodo.13243858)

**GeoRDFBench** can be used by researchers and practitioners interested in Semantic Benchmarking with SPARQL language. The framework however provides unique features for the geospatial aspects of RDF stores that support the GeoSPARQL standard. Documentation for the project is maintained at our [GeoRDFBench web site](https://geordfbench.di.uoa.gr/). 

For a quick acquaintance with the platform, please run the dockerized example at [dockerized example](https://geordfbench.di.uoa.gr/rdf4j_scal10k_workload_docker_iframe.html) which exhibits running a GeoSPARQL experiment with the [RDF4J](https://rdf4j.org/) RDF store against the *Scalability 10K* dataset of the [Geographica 2](https://geographica2.di.uoa.gr/) benchmark.

The [GeoRDFBench Framework Samples](https://github.com/tioannid/geordfbench_samples) is a maven project using GeoRDFBench's **runtime** as dependence and showcases the use of the JSON Generator's API for constructing the workload specification for the [LUBM(1, 0)](https://swat.cse.lehigh.edu/projects/lubm/) benchmark.

The GeoRDFBench web site also provides the [Multiple stores against LUBM(1, 0) Workload](https://geordfbench.di.uoa.gr/multistore_lubm_1_0_workload_docker_iframe.html) dockerized example showcasing the use of GeoRDFBench to run experiments with *RDF4J*, *GraphDB* and *JenaGeoSPARQL* against **LUBM(1, 0)** workload.