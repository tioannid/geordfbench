package gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.impl;

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
import java.util.HashMap;
import java.util.Map;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl.GeographicaDS;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.simple.ScalabilityEnum;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.IQuerySetPartOfWorkload;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.DynamicTempParamQS;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.MacroQS;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl.StaticTempParamQS;
import gr.uoa.di.rdf.geordfbench.runtime.workloadspecs.IGeospatialWorkLoadSpec;
import org.apache.log4j.Logger;

/**
 * A base concrete implemetation of the {@link IGeospatialDataSet} interface.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class SimpleGeospatialWL implements IGeospatialWorkLoadSpec {

    // --- Static members -----------------------------
    static Logger logger
            = Logger.getLogger(SimpleGeospatialWL.class.getSimpleName());

    /**
     * Creates the Census Macro Geocoding geographica workload
     *
     * @return the Census Macro Geocoding geographica workload
     */
    public static SimpleGeospatialWL newCensusMacroGeoWL() {
        return new SimpleGeospatialWL("CensusMacroGeo", "",
                GeographicaDS.newCensusGeographicaDS(),
                MacroQS.newMacroGeocodingQS());
    }

    /**
     * Creates the Scalability N spatial function geographica workload
     *
     * @param enumScal the scale or subset of the Scalability complex dataset
     * @return the Scalability N spatial function geographica workload
     */
    public static SimpleGeospatialWL newScalabilityFuncWL(ScalabilityEnum enumScal) {
        return new SimpleGeospatialWL("ScalabilityFunc", "",
                GeographicaDS.newScalabilityDS(enumScal),
                StaticTempParamQS.newScalabilityFuncQS());
    }

    /**
     * Creates the Scalability N spatial predicate geographica workload
     *
     * @param enumScal the scale or subset of the Scalability complex dataset
     * @return the Scalability N spatial predicate geographica workload
     */
    public static SimpleGeospatialWL newScalabilityPredWL(ScalabilityEnum enumScal) {
        return new SimpleGeospatialWL("ScalabilityPred", "",
                GeographicaDS.newScalabilityDS(enumScal),
                StaticTempParamQS.newScalabilityPredQS());
    }

    /**
     * Creates the Real World Micro geographica workload
     *
     * @return the Real World Micro geographica workload
     */
    public static SimpleGeospatialWL newRWMicroWL() {
        return new SimpleGeospatialWL("RWMicro", "",
                GeographicaDS.newRealWorldGeographicaDS(),
                DynamicTempParamQS.newRWMicroQS());
    }

    /**
     * Creates the Real World Macro Compute Statistics geographica workload
     *
     * @return the Real World Macro Compute Statistics geographica workload
     */
    public static SimpleGeospatialWL newRWMacroCompStatsWL() {
        return new SimpleGeospatialWL("RWMacroCompStats", "",
                GeographicaDS.newRealWorldGeographicaDS(),
                MacroQS.newMacroComputeStatisticsQS());
    }

    /**
     * Creates the Real World Macro RapidMapping geographica workload
     *
     * @return the Real World Macro RapidMapping geographica workload
     */
    public static SimpleGeospatialWL newRWMacroRapidMappingWL() {
        return new SimpleGeospatialWL("RWMacroRapidMapping", "",
                GeographicaDS.newRealWorldGeographicaDS(),
                MacroQS.newMacroRapidMappingQS());
    }

    /**
     * Creates the Real World Macro MapSearch geographica workload
     *
     * @return the Real World Macro MapSearch geographica workload
     */
    public static SimpleGeospatialWL newRWMacroMapSearchWL() {
        return new SimpleGeospatialWL("RWMacroMapSearch", "",
                GeographicaDS.newRealWorldGeographicaDS(),
                MacroQS.newMacroMapSearchQS());
    }

    /**
     * Creates the Real World Macro RevGeo geographica workload
     *
     * @return the Real World Macro RevGeo geographica workload
     */
    public static SimpleGeospatialWL newRWMacroRevGeoWL() {
        return new SimpleGeospatialWL("RWMacroRevGeo", "",
                GeographicaDS.newRealWorldGeographicaDS(),
                MacroQS.newMacroReverseGeocodingQS());
    }

    /**
     * Creates the Synthetic N=512 geographica workload
     *
     * @return the Synthetic N=512 geographica workload
     */
    public static SimpleGeospatialWL newSyntheticWL() {
        return new SimpleGeospatialWL("Synthetic-512", "",
                GeographicaDS.newSyntheticGeographicaDS(),
                StaticTempParamQS.newSyntheticQS(512));
    }

    // --- Data members ------------------------------
    String name;
    String relativeBaseDir;
    IGeospatialDataSet geospatialDataset;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
    IQuerySetPartOfWorkload geospatialQueryset;

    // --- Constructors -----------------------------------
    /**
     * Constructs a Geographica workload spec with one geospatial dataset and
     * one geospatial queryset
     *
     * @param name the workload name
     * @param relativeBaseDir the relative directory of the workload
     * specification
     * @param geospatialDataset the geospatial dataset specification
     * @param geospatialQueryset the geospatialqueryset specification
     */
    public SimpleGeospatialWL(String name, String relativeBaseDir,
            IGeospatialDataSet geospatialDataset,
            IQuerySetPartOfWorkload geospatialQueryset) {
        this.name = name;
        this.relativeBaseDir = relativeBaseDir;
        this.geospatialDataset = geospatialDataset;
        this.geospatialQueryset = geospatialQueryset;
    }

    public SimpleGeospatialWL() {
    }

    // --- Data Accessors -----------------------------------
    /**
     * Gets the workload’s name.
     *
     * @return A string representing the workload name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the workload’s name.
     *
     * @param name A string representing the workload name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the workload’s relative base directory. The dataset and queryset
     * specification components base directories are relative to this directory.
     *
     * @return A string representing the relative directory
     */
    @Override
    public String getRelativeBaseDir() {
        return relativeBaseDir;
    }

    /**
     * Sets the workload’s relative base directory. The dataset and queryset
     * specification components base directories are relative to this directory.
     *
     * @param relativeBaseDir A string representing the relative directory
     */
    public void setRelativeBaseDir(String relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    @Override
    public IGeospatialDataSet getGeospatialDataset() {
        return geospatialDataset;
    }

    public void setGeospatialDataset(IGeospatialDataSet geospatialDataset) {
        this.geospatialDataset = geospatialDataset;
    }

    @Override
    public IQuerySetPartOfWorkload getGeospatialQueryset() {
        return geospatialQueryset;
    }

    public void setGeospatialQueryset(IQuerySetPartOfWorkload geospatialQueryset) {
        this.geospatialQueryset = geospatialQueryset;
    }

    // --- Methods -----------------------------------
    /**
     * Gets the merged union of the maps of useful namespace prefixes of the
     * geospatial dataset and queryset.
     *
     * @return the map of namespace prefixes and IRIs
     */
    @JsonIgnore
    @Override
    public Map<String, String> getMapUsefulNamespacePrefixes() {
        Map<String, String> mergedMap = new HashMap<>(geospatialDataset.getMapUsefulNamespacePrefixes());
        mergedMap.putAll(geospatialQueryset.getMapUsefulNamespacePrefixes());
        return mergedMap;
    }

    /**
     * Gets the combined prefix header for the dataset and queryset.
     *
     * @return a string representing the prefix header
     */
    @JsonIgnore
    @Override
    public String getNamespacePrefixHeader() {
        StringBuilder prefixes = new StringBuilder();
        prefixes.append(geospatialDataset.getNamespacePrefixHeader()).append("\n");
        prefixes.append(geospatialQueryset.getNamespacePrefixHeader()).append("\n");
        return prefixes.toString();
    }

    /**
     * Get the workload description
     *
     * @return A String containing a description of the workload
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
        msg = msg + "\n\t" + geospatialDataset.toString();
        msg = msg + "\n\t" + geospatialQueryset.toString();
        msg = msg + "}";
        return msg;
    }

    /**
     * Serialize the geographica workload to a JSON string
     *
     * @return the serialized JSON string
     */
    @Override
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
     * Serialize the geographica workload to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Override
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
