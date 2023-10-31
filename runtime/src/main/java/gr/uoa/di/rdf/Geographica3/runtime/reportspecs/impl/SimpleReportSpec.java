/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica3.runtime.reportspecs.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class SimpleReportSpec implements IReportSpec {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(SimpleReportSpec.class.getSimpleName());

    // --- Data members ------------------------------
    int noQueryResultToReport;

    // --- Constructors ------------------------------    
    public SimpleReportSpec(int noQueryResultToReport) {
        this.noQueryResultToReport = noQueryResultToReport;
    }

    public SimpleReportSpec() {
    }

    // --- Data Accessors ------------------------------   
    @Override
    public int getNoQueryResultToReport() {
        return this.noQueryResultToReport;
    }

    public void setNoQueryResultToReport(int noQueryResultToReport) {
        this.noQueryResultToReport = noQueryResultToReport;
    }

    // --- Methods -------------------------------------
    /**
     * Serialize the simple report spec to a JSON string
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
     * Serialize the simple report spec to a JSON file
     *
     * @param serObjFile File with the serialized object
     */
    @Override
    public void serializeToJSON(File serObjFile) {
        // create the mapper
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            // serialize the object
            mapper.writeValue(serObjFile, this);
        } catch (IOException ex) {
            logger.error(ex.toString());
        }
    }
}
