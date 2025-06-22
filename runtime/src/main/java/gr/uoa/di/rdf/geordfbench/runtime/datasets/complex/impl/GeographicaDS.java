package gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.IGeospatialSimpleDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.ScalabilityEnum;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.impl.GenericGeospatialSimpleDS;
import org.apache.log4j.Logger;

/**
 * A base concrete implemetation of the {@link IGeospatialDataSet} interface.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class GeographicaDS implements IGeospatialDataSet {

    // --- Static members -----------------------------
    static Logger logger
            = Logger.getLogger(GeographicaDS.class.getSimpleName());

    /**
     * Creates the Census complex geographica dataset
     *
     * @return the Census complex geographica dataset
     */
    public static GeographicaDS newCensusGeographicaDS() {
        return new GeographicaDS(GenericGeospatialSimpleDS.newCensusSDS(), "", 0);

    }

    /**
     * Creates the RealWorld complex geographica dataset
     *
     * @return the RealWorld complex geographica dataset
     */
    public static GeographicaDS newRealWorldGeographicaDS() {
        GeographicaDS rw_ds
                = new GeographicaDS();
        rw_ds.setName("realworld");
        rw_ds.setRelativeBaseDir("RealWorldWorkload");
        rw_ds.setN(0);
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newCorineSDS());
        rw_ds.addSimpleGeospatialDataSetContext("clc", "<http://geographica.di.uoa.gr/dataset/clc>");
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newDBpediaSDS());
        rw_ds.addSimpleGeospatialDataSetContext("dbpedia", "<http://geographica.di.uoa.gr/dataset/dbpedia>");
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newGagSDS());
        rw_ds.addSimpleGeospatialDataSetContext("gag", "<http://geographica.di.uoa.gr/dataset/gag>");
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newGeonamesSDS());
        rw_ds.addSimpleGeospatialDataSetContext("geonames", "<http://geographica.di.uoa.gr/dataset/geonames>");
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newHotspotsSDS());
        rw_ds.addSimpleGeospatialDataSetContext("hotspot", "<http://geographica.di.uoa.gr/dataset/hotspots>");
        rw_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newLinkedGeoDataSDS());
        rw_ds.addSimpleGeospatialDataSetContext("lgd", "<http://geographica.di.uoa.gr/dataset/lgd>");
        return rw_ds;
    }

    /**
     * Creates the Scalability N complex geographica dataset
     *
     * @param enumScal the scale or subset of the Scalability complex dataset
     * @return the Scalability N complex geographica dataset
     */
    public static GeographicaDS newScalabilityDS(ScalabilityEnum enumScal) {
        return new GeographicaDS(GenericGeospatialSimpleDS.newScalabilitySDS(enumScal), "", 0);
    }

    /**
     * Creates the Synthetic complex geographica dataset
     *
     * @return the Synthetic complex geographica dataset
     */
    public static GeographicaDS newSyntheticGeographicaDS() {
        GeographicaDS synth_ds
                = new GeographicaDS();
        synth_ds.setName("synthetic");
        synth_ds.setRelativeBaseDir("SyntheticWorkload");
        synth_ds.setN(512);
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newHexagonLargeSDS());
        synth_ds.addSimpleGeospatialDataSetContext("hexagonlarge", "");
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newHexagonLargeCenterSDS());
        synth_ds.addSimpleGeospatialDataSetContext("hexagonlargecenter", "");
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newHexagonSmallSDS());
        synth_ds.addSimpleGeospatialDataSetContext("hexagonsmall", "");
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newLineStringSDS());
        synth_ds.addSimpleGeospatialDataSetContext("linesting", "");
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newPointSDS());
        synth_ds.addSimpleGeospatialDataSetContext("point", "");
        return synth_ds;
    }

    /**
     * Creates the Synthetic POIs complex geographica dataset
     *
     * @return the Synthetic POIs complex geographica dataset
     */
    public static GeographicaDS newSyntheticPOIsGeographicaDS() {
        GeographicaDS synth_ds
                = new GeographicaDS();
        synth_ds.setName("syntheticPOIs");
        synth_ds.setRelativeBaseDir("Synthetic_POIs");
        synth_ds.setN(512);
        synth_ds.addSimpleGeospatialDataSet(GenericGeospatialSimpleDS.newPointSDS());
        synth_ds.addSimpleGeospatialDataSetContext("point", "");
        return synth_ds;
    }
    // --- Data members ------------------------------
    String name;
    String relativeBaseDir;
    List<IGeospatialSimpleDataSet> simpleGeospatialDataSetList;
    Map<String, String> mapDataSetContexts;
    int n;

    // --- Constructors -----------------------------------
    /**
     * Constructs a Geographica data set with more than one simple geospatial
     * dataset and the corresponding contexts or Deserializes a Geographica data
     * set with one or more simple geospatial dataset and the corresponding
     * contexts
     *
     * @param name the complex dataset name
     * @param relativeBaseDir the relative directory of the dataset
     * @param simpleGeospatialDataSetList a list of the comprising simple
     * datasets
     * @param mapDataSetContexts a map with the simple dataset contexts/graph
     * IRIs
     * @param N an int representing the scaling of a synthetic dataset. Should
     * be 0 for regular datasets.
     */
    public GeographicaDS(String name, String relativeBaseDir,
            List<IGeospatialSimpleDataSet> simpleGeospatialDataSetList,
            Map<String, String> mapDataSetContexts,
            int N) {
        this.name = name;
        this.relativeBaseDir = relativeBaseDir;
        this.simpleGeospatialDataSetList = simpleGeospatialDataSetList;
        this.mapDataSetContexts = mapDataSetContexts;
        this.n = N;
    }

    /**
     * Constructs a Geographica data set with one simple geospatial dataset and
     * one context (can be ""). This is the common form of dataset to run
     * experiments on Geographica 3.
     *
     * @param simpleGeospatialDataSet A {@link IGeospatialSimpleDataSet} dataset
     * @param context a String representing the context/graph IRI
     * @param N an int representing the scaling of a synthetic dataset. Should
     * be 0 for regular datasets.
     */
    public GeographicaDS(IGeospatialSimpleDataSet simpleGeospatialDataSet,
            String context, int N) {
        this.name = simpleGeospatialDataSet.getName();
        this.relativeBaseDir = simpleGeospatialDataSet.getRelativeBaseDir();
        this.simpleGeospatialDataSetList = new ArrayList<>();
        this.simpleGeospatialDataSetList.add(simpleGeospatialDataSet);
        this.mapDataSetContexts = new HashMap<>();
        this.mapDataSetContexts.put(simpleGeospatialDataSet.getName(), context);
        this.n = N;
    }

    public GeographicaDS() {
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

    /**
     * Sets the dataset’s name.
     *
     * @param name A string representing the dataset name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the dataset’s relative base directory.
     *
     * @return A string representing the relative directory
     */
    @Override
    public String getRelativeBaseDir() {
        return relativeBaseDir;
    }

    /**
     * Sets the dataset’s relative base directory.
     *
     * @param relativeBaseDir a String with the relative base directory for teh
     * dataset.
     */
    public void setRelativeBaseDir(String relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    /**
     * Gets the list of simple geospatial datasets that make up this geospatial
     * dataset.
     *
     * @return the list of simple geospatial datasets
     */
    @Override
    public List<IGeospatialSimpleDataSet> getSimpleGeospatialDataSetList() {
        return simpleGeospatialDataSetList;
    }

    public void setSimpleGeospatialDataSetList(List<IGeospatialSimpleDataSet> simpleGeospatialDataSetList) {
        this.simpleGeospatialDataSetList = simpleGeospatialDataSetList;
    }

    /**
     * Adds a simple geospatial dataset to the list of simple geospatial
     * datasets that make up this geospatial dataset.
     *
     * @param simpleGeoDS the simple geospatial dataset to add to the list
     */
    @Override
    public void addSimpleGeospatialDataSet(IGeospatialSimpleDataSet simpleGeoDS) {
        if (simpleGeospatialDataSetList == null) {
            simpleGeospatialDataSetList = new LinkedList<>();
        }
        simpleGeospatialDataSetList.add(simpleGeoDS);
    }

    /**
     * Gets a simple geospatial dataset, searching by name in the list of simple
     * geospatial datasets.
     *
     * @param name simple geospatial dataset name
     * @return a simple geospatial dataset with the provided name
     */
    @Override
    public IGeospatialSimpleDataSet getSimpleGeospatialDataset(String name) {
        if (simpleGeospatialDataSetList != null) {
            return simpleGeospatialDataSetList.get(
                    simpleGeospatialDataSetList.indexOf(name));
        } else {
            return null; // TODO: through exception
        }
    }

    public Map<String, String> getMapDataSetContexts() {
        return mapDataSetContexts;
    }

    public void setMapDataSetContexts(Map<String, String> mapDataSetContexts) {
        this.mapDataSetContexts = mapDataSetContexts;
    }

    /**
     * Gets the context/graph IRI, searching by simple geospatial dataset name
     * in the map of simple geospatial dataset
     * <name, context> pairs.
     *
     * @param name simple geospatial dataset name
     * @return a String representing the context or graph IRI
     */
    @Override
    public String getSimpleGeospatialDataSetContext(String name) {
        if (mapDataSetContexts != null) {
            return mapDataSetContexts.get(name);
        } else {
            return null; // TODO: through exception
        }
    }

    @Override
    public void addSimpleGeospatialDataSetContext(String name, String context) {
        if (mapDataSetContexts == null) {
            mapDataSetContexts = new HashMap<>();
        }
        mapDataSetContexts.put(name, context);
    }

    public int getN() {
        return n;
    }

    public void setN(int N) {
        this.n = N;
    }

    // --- Methods -----------------------------------
    /**
     * Is this geospatial dataset made of only 1 simple geospatial dataset?
     *
     * @return is this a simple geospatial dataset?
     */
    @JsonIgnore
    @Override
    public boolean isSimple() {
        boolean rtn = false;
        if (simpleGeospatialDataSetList != null) {
            rtn = (simpleGeospatialDataSetList.size() == 1);
        }
        return rtn;
    }

    /**
     * Gets the merged union of the maps of useful namespace prefixes of this
     * geospatial dataset.
     *
     * @return the map of namespace prefixes and IRIs
     */
    @JsonIgnore
    @Override
    public Map<String, String> getMapUsefulNamespacePrefixes() {
        Map<String, String> mergedMap = new HashMap<>();
        for (IGeospatialSimpleDataSet geoDS : simpleGeospatialDataSetList) {
            mergedMap.putAll(geoDS.getMapUsefulNamespacePrefixes());
        }
        return mergedMap;
    }

    /**
     * Gets the prefix header from the map of useful namespace prefixes to be
     * used in queries against this geospatial dataset.
     *
     * @return a string representing the prefix header
     */
    @JsonIgnore
    @Override
    public String getNamespacePrefixHeader() {
        StringBuilder prefixes = new StringBuilder();
        for (IGeospatialSimpleDataSet geoDS : simpleGeospatialDataSetList) {
            prefixes.append(geoDS.getNamespacePrefixHeader()).append("\n");
        }
        return prefixes.toString();
    }

    /**
     * Get the dataset description
     *
     * @return A String containing a description of the dataset
     */
    @Override
    public String prettyPrint() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        String msg;
        msg = "{" + this.getClass().getSimpleName() + ": "
                + name + ", "
                + relativeBaseDir + ",";

        for (IGeospatialSimpleDataSet ds : simpleGeospatialDataSetList) {
            msg = msg + "\n\t" + ds.toString();
        }
        msg = msg + "}";
        return msg;
    }

    /**
     * Serialize the geographica dataset to a JSON string
     *
     * @return the serialized JSON string
     */
    public String serializeToJSON() {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // serialize the object
        StringWriter strwrt = new StringWriter();
        try {
            mapper.writeValue(strwrt, this);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return strwrt.toString();
    }

    /**
     * Serialize the geographica dataset to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void serializeToJSON(File serObjFile) throws JsonGenerationException,
            JsonMappingException, IOException {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // serialize the object
        mapper.writeValue(serObjFile, this);
    }
}
