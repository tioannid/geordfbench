package gr.uoa.di.rdf.geordfbench.runtime.hosts.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.os.IOS;

/**
 * An abstract implementation of a {@link IHost} system where the Geographica
 * benchmark can run.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class SimpleHost implements IHost {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(SimpleHost.class.getSimpleName());

    // --- Data members ------------------------------
    protected String name;  // Teleios3, VM, etc.
    protected String ipAddr;
    protected int ram;     // installed RAM
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
            property = "classname")
    protected IOS os; // installed operating system
    protected String sourceFileDir;
    protected String reposBaseDir;
    protected String reportsBaseDir;

    // --- Constructors ------------------------------
    public SimpleHost() {
    }

    public SimpleHost(String name, String ipAddr, int ram, IOS os, String sourceFileDir, String reposBaseDir, String reportsBaseDir) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.ram = ram;
        this.os = os;
        this.sourceFileDir = sourceFileDir;
        this.reposBaseDir = reposBaseDir;
        this.reportsBaseDir = reportsBaseDir;
    }

    // --- Data Accessors ----------------------------
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getIpAddr() {
        return ipAddr;
    }

    @Override
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    @Override
    public int getRam() {
        return ram;
    }

    @Override
    public void setRam(int ram) {
        this.ram = ram;
    }

    @Override
    public IOS getOs() {
        return os;
    }

    @Override
    public void setOs(IOS os) {
        this.os = os;
    }

    @Override
    public String getSourceFileDir() {
        return sourceFileDir;
    }

    @Override
    public void setSourceFileDir(String sourceFileDir) {
        this.sourceFileDir = sourceFileDir;
    }

    @Override
    public String getReposBaseDir() {
        return reposBaseDir;
    }

    @Override
    public void setReposBaseDir(String reposBaseDir) {
        this.reposBaseDir = reposBaseDir;
    }

    @Override
    public String getReportsBaseDir() {
        return reportsBaseDir;
    }

    @Override
    public void setReportsBaseDir(String reportsBaseDir) {
        this.reportsBaseDir = reportsBaseDir;
    }

    // --- Methods -----------------------------------
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{ "
                + this.name + ", "
                + this.ipAddr + ", "
                + this.ram + "GB, "
                + this.os.toString()
                + " }"; //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Serialize the simple host to a JSON string
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
     * Serialize the geographica host to a JSON file
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
