package gr.uoa.di.rdf.Geographica3.runtime.datasets.simple.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.datasets.simple.IGeospatialSimpleDataSet;
import gr.uoa.di.rdf.Geographica3.runtime.datasets.simple.ScalabilityEnum;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * Base implementation of the {@link IGeospatialSimpleDataSet}. Initializes the
 * static map of useful prefixes with common namespace prefixes.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class GenericGeospatialSimpleDS implements IGeospatialSimpleDataSet {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(GenericGeospatialSimpleDS.class.getSimpleName());
    public static final Map<String, String> USEFULL_PREFIXES_MAP = new HashMap<>();
    static public final String NTRIPLES_STR = RDFFormat.NTRIPLES.getName().toUpperCase();

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful general RDF related prefixes
        USEFULL_PREFIXES_MAP.put("xsd", "<http://www.w3.org/2001/XMLSchema#>");
        USEFULL_PREFIXES_MAP.put("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
        USEFULL_PREFIXES_MAP.put("rdfs", "<http://www.w3.org/2000/01/rdf-schema#>");
        USEFULL_PREFIXES_MAP.put("owl", "<http://www.w3.org/2002/07/owl#>");
        USEFULL_PREFIXES_MAP.put("geo", "<http://www.opengis.net/ont/geosparql#>");
        USEFULL_PREFIXES_MAP.put("geof", "<http://www.opengis.net/def/function/geosparql/>");
        USEFULL_PREFIXES_MAP.put("geo-sf", "<http://www.opengis.net/ont/sf#>");
    }

    /**
     * Creates the Corine geographica simple dataset
     *
     * @return the Corine GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newCorineSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("corine",
                        "NO_CRS/RealWorld",
                        "corine.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("clc", "<http://geo.linkedopendata.gr/corine/ontology#>");
        sds.addAsWKT("corineAsWKT", "<http://geo.linkedopendata.gr/corine/ontology#asWKT>");
        sds.addHasGeometry("corineHasGeometry", "<http://geo.linkedopendata.gr/corine/ontology#hasGeometry>");
        return sds;
    }

    /**
     * Creates the DBpedia geographica simple dataset
     *
     * @return the DBpedia GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newDBpediaSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("dbpedia",
                        "NO_CRS/RealWorld",
                        "dbpedia.nt", NTRIPLES_STR);
        sds.addAsWKT("dbpediaAsWKT", "<http://dbpedia.org/property/asWKT>");
        return sds;
    }

    /**
     * Creates the GAG geographica simple dataset
     *
     * @return the GAG GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newGagSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("gag",
                        "NO_CRS/RealWorld",
                        "gag.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("gag", "<http://geo.linkedopendata.gr/gag/ontology/>");
        sds.addAsWKT("gagAsWKT", "<http://geo.linkedopendata.gr/gag/ontology/asWKT>");
        sds.addHasGeometry("gagHasGeometry", "<http://geo.linkedopendata.gr/gag/ontology/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Geonames geographica simple dataset
     *
     * @return the Geonames GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newGeonamesSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("geonames",
                        "NO_CRS/RealWorld",
                        "geonames.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("geonames", "<http://www.geonames.org/ontology#>");
        sds.addAsWKT("geonamesAsWKT", "<http://www.geonames.org/ontology#asWKT>");
        sds.addHasGeometry("geonamesHasGeometry", "<http://www.geonames.org/ontology#hasGeometry>");
        return sds;
    }

    /**
     * Creates the Hotspots geographica simple dataset
     *
     * @return the Hotspots GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newHotspotsSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("hotspots",
                        "NO_CRS/RealWorld",
                        "hotspots.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("noa", "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#>");
        sds.addAsWKT("noaAsWKT", "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT>");
        sds.addHasGeometry("noaHasGeometry", "<http://teleios.di.uoa.gr/ontologies/noaOntology.owl#hasGeometry>");
        return sds;
    }

    /**
     * Creates the LinkedGeoData geographica simple dataset
     *
     * @return the LinkedGeoData GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newLinkedGeoDataSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("linkedgeodata",
                        "NO_CRS/RealWorld",
                        "linkedgeodata.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("lgdo", "<http://linkedgeodata.org/ontology/>");
        sds.addAsWKT("lgdAsWKT", "<http://linkedgeodata.org/ontology/asWKT>");
        sds.addHasGeometry("lgdHasGeometry", "<http://linkedgeodata.org/ontology/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Census geographica simple dataset
     *
     * @return the Census GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newCensusSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("census",
                        "Census/NO_CRS",
                        "census.new_york_roads.rdf.NO_CRS.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("census", "<http://geographica.di.uoa.gr/cencus/ontology#>");
        sds.addAsWKT("censusAsWKT", "<http://geographica.di.uoa.gr/cencus/ontology#asWKT>");
        sds.addHasGeometry("censusHasGeometry", "<http://geographica.di.uoa.gr/cencus/ontology#hasGeometry>");
        return sds;
    }

    /**
     * Creates a Scalability N geographica simple dataset
     *
     * @param enumScal
     * @return the Census GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newScalabilitySDS(ScalabilityEnum enumScal) {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("scalability_" + enumScal.toString(),
                        "Scalability/" + enumScal.toString(),
                        "scalability_" + enumScal.toString() + ".nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("lgo", "<http://data.linkedeodata.eu/ontology#>");
        sds.addAsWKT("scalabilityAsWKT", "<http://www.opengis.net/ont/geosparql#asWKT>");
        sds.addHasGeometry("scalabilityHasGeometry", "<http://www.opengis.net/ont/geosparql#hasGeometry>");
        return sds;
    }

    /**
     * Creates the Synthetic HEXAGON_LARGE geographica simple dataset
     *
     * @return the Synthetic HEXAGON_LARGE GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newHexagonLargeSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("hexagonlarge",
                        "Synthetic",
                        "HEXAGON_LARGE.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("state", "<http://geographica.di.uoa.gr/generator/state/>");
        sds.addAsWKT("stateAsWKT", "<http://geographica.di.uoa.gr/generator/state/asWKT>");
        sds.addHasGeometry("stateHasGeometry", "<http://geographica.di.uoa.gr/generator/state/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Synthetic HEXAGON_LARGE_CENTER geographica simple dataset
     *
     * @return the Synthetic HEXAGON_LARGE_CENTER GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newHexagonLargeCenterSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("hexagonlargecenter",
                        "Synthetic",
                        "HEXAGON_LARGE_CENTER.nt", NTRIPLES_STR);
        sds.addAsWKT("stateCenterAsWKT", "<http://geographica.di.uoa.gr/generator/stateCenter/asWKT>");
        sds.addHasGeometry("stateCenterHasGeometry", "<http://geographica.di.uoa.gr/generator/stateCenter/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Synthetic HEXAGON_SMALL geographica simple dataset
     *
     * @return the Synthetic HEXAGON_SMALL GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newHexagonSmallSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("hexagonsmall",
                        "Synthetic",
                        "HEXAGON_SMALL.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("landOwnership", "<http://geographica.di.uoa.gr/generator/landOwnership/>");
        sds.addAsWKT("landOwnershipAsWKT", "<http://geographica.di.uoa.gr/generator/landOwnership/asWKT>");
        sds.addHasGeometry("landOwnershipHasGeometry", "<http://geographica.di.uoa.gr/generator/landOwnership/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Synthetic LINESTRING geographica simple dataset
     *
     * @return the Synthetic LINESTRING GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newLineStringSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("linestring",
                        "Synthetic",
                        "LINESTRING.nt", NTRIPLES_STR);
        sds.addAsWKT("roadAsWKT", "<http://geographica.di.uoa.gr/generator/road/asWKT>");
        sds.addHasGeometry("roadHasGeometry", "<http://geographica.di.uoa.gr/generator/road/hasGeometry>");
        return sds;
    }

    /**
     * Creates the Synthetic POINT geographica simple dataset
     *
     * @return the Synthetic POINT GenericGeospatialSimpleDS
     */
    public static GenericGeospatialSimpleDS newPointSDS() {
        GenericGeospatialSimpleDS sds
                = new GenericGeospatialSimpleDS("point",
                        "Synthetic",
                        "POINT.nt", NTRIPLES_STR);
        sds.addUsefulNamespacePrefix("pointOfInterest", "<http://geographica.di.uoa.gr/generator/pointOfInterest/>");
        sds.addAsWKT("pointOfInterestAsWKT", "<http://geographica.di.uoa.gr/generator/pointOfInterest/asWKT>");
        sds.addHasGeometry("pointOfInterestHasGeometry", "<http://geographica.di.uoa.gr/generator/pointOfInterest/hasGeometry>");
        return sds;
    }

    // --- Data members ------------------------------
    String name;
    String relativeBaseDir;
    String dataFile;
    String rdfFormat;
    Map<String, String> mapUsefulNamespacePrefixes;
    Map<String, String> mapAsWKT;
    Map<String, String> mapHasGeometry;

    // --- Constructor -----------------------------------
    public GenericGeospatialSimpleDS(String name, String relativeBaseDir, String dataFile, String rdfFormat) {
        this.name = name;
        this.relativeBaseDir = relativeBaseDir;
        this.dataFile = dataFile;
        this.rdfFormat = rdfFormat;
        this.mapUsefulNamespacePrefixes = new HashMap<>();
        this.mapUsefulNamespacePrefixes.putAll(USEFULL_PREFIXES_MAP);
    }

    public GenericGeospatialSimpleDS() {
    }

    // --- Data Accessors -----------------------------------
    /**
     * Gets the dataset’s name.
     *
     * @return A string representing the dataset name
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the dataset’s relative base directory where the RDF file is located.
     *
     * @return A string representing the dataset relative base directory
     */
    @Override
    public String getRelativeBaseDir() {
        return relativeBaseDir;
    }

    public void setRelativeBaseDir(String relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    /**
     * Gets the dataset’s file name which when prefixed by the
     * {@link getRelativeBaseDir()} provides the full path to the file.
     *
     * @return A string representing the dataset file name
     */
    @Override
    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Gets the dataset’s RDF file format.
     *
     * @return A string representing the dataset RDF file format
     */
    @Override
    public String getRdfFormat() {
        return rdfFormat;
    }

    public void setRdfFormat(String rdfFormat) {
        this.rdfFormat = rdfFormat;
    }

    /**
     * Gets the map of useful namespace prefixes for the dataset.
     *
     * @return a map with the dataset useful namespace prefixes.
     */
    @Override
    public Map<String, String> getMapUsefulNamespacePrefixes() {
        return mapUsefulNamespacePrefixes;
    }

    public void setMapUsefulNamespacePrefixes(Map<String, String> mapUsefulNamespacePrefixes) {
        this.mapUsefulNamespacePrefixes = mapUsefulNamespacePrefixes;
    }

    // --- Methods -----------------------------------
    /**
     * Adds a useful namespace prefix for the dataset.
     *
     * @param nsPrefix A String containing the namespace prefix
     * @param nsIRI A String containing the namespace IRI file name
     */
    @Override
    public void addUsefulNamespacePrefix(String nsPrefix, String nsIRI) {
        if (this.mapUsefulNamespacePrefixes == null) {
            this.mapUsefulNamespacePrefixes = new HashMap<>();
        }
        if (!this.mapUsefulNamespacePrefixes.containsValue(nsIRI)) {
            this.mapUsefulNamespacePrefixes.put(nsPrefix, nsIRI);
        }
    }

    /**
     * Gets the prefix header for the dataset useful namespace prefixes.
     *
     * @return A String containing the header with the namespace prefixes
     */
    @JsonIgnore
    @Override
    public String getNamespacePrefixHeader() {
        String prefixes = "";
        if (this.mapUsefulNamespacePrefixes != null) {
            for (String nsprefix : this.mapUsefulNamespacePrefixes.keySet()) {
                prefixes = prefixes
                        + "PREFIX " + nsprefix + ": " + this.mapUsefulNamespacePrefixes.get(nsprefix) + " \n";
            }
        }
        return prefixes;
    }

    /**
     * Get the dataset description
     *
     * @return A String containing a description of the dataset
     */
    @Override
    public String prettyPrint() {
        StringBuilder strb = new StringBuilder();
        strb.append("name : ").append(name);
        strb.append("relativeBaseDir : ").append(relativeBaseDir);
        strb.append("dataFile : ").append(dataFile);
        strb.append("rdfFormat : ").append(rdfFormat);
        strb.append("mapUsefulNamespacePrefixes : ").append(this.getNamespacePrefixHeader());

        return strb.toString();
    }

    @Override
    public String toString() {
        String msg;
        msg = "{" + this.getClass().getSimpleName() + ": "
                + name + ", "
                + rdfFormat + "}";
        return msg;
    }

    /**
     * Adds an asWKT property to the map of asWKT properties.
     *
     * @param name identifier name for the asWKT property in the map
     * @param asWKT a String representing the asWKT property
     */
    @Override
    public void addAsWKT(String name, String asWKT) {
        if (this.mapAsWKT == null) {
            this.mapAsWKT = new HashMap<>();
        }
        this.mapAsWKT.put(name, asWKT);
    }

    /**
     * Adds an hasGeometry property to the map of hasGeometry properties.
     *
     * @param name identifier name for the hasGeometry property in the map
     * @param hasGeometry a String representing the hasGeometry property
     */
    @Override
    public void addHasGeometry(String name, String hasGeometry) {
        if (this.mapHasGeometry == null) {
            this.mapHasGeometry = new HashMap<>();
        }
        this.mapHasGeometry.put(name, hasGeometry);
    }

    /**
     * Gets the map of asWKT properties of the dataset.
     *
     * @return a map with the dataset asWKT properties
     */
    @Override
    public Map<String, String> getMapAsWKT() {
        return mapAsWKT;
    }

    /**
     * Gets the map of hasGeometry properties of the dataset.
     *
     * @return a map with the dataset hasGeometry properties
     */
    @Override
    public Map<String, String> getMapHasGeometry() {
        return mapHasGeometry;
    }

    /**
     * Gets a printout of the map of asWKT properties.
     *
     * @return A String with the printout
     */
    @Override
    public String printAsWKTs() {
        String msg = "";
        if (this.mapAsWKT != null) {
            for (String key : this.mapAsWKT.keySet()) {
                msg = msg + key + ": " + this.mapAsWKT.get(key) + " \n";
            }
        }
        return msg;
    }

    /**
     * Gets a printout of the map of hasGeometry properties.
     *
     * @return A String with the printout
     */
    @Override
    public String printHasGeometries() {
        String msg = "";
        if (this.mapHasGeometry != null) {
            for (String key : this.mapHasGeometry.keySet()) {
                msg = msg + key + ": " + this.mapHasGeometry.get(key) + " \n";
            }
        }
        return msg;
    }
}
