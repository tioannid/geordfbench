package gr.uoa.di.rdf.Geographica3.runtime.hosts;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.impl.SimpleHost;
import gr.uoa.di.rdf.Geographica3.runtime.os.IOS;
import java.io.File;
import java.io.IOException;

/**
 * An host system where Geographica3 experiments can run.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
@JsonDeserialize(as = SimpleHost.class)
public interface IHost {

    /**
     * Gets the host’s name.
     *
     * @return A string representing the host name
     */
    String getName();

    /**
     * Sets the host’s name.
     *
     * @param name the host name
     */
    void setName(String name);

    /**
     * Gets the host’s IP address.
     *
     * @return A string representing the host IP address
     */
    String getIpAddr();

    /**
     * Sets the host’s IP address.
     *
     * @param IPAddr the host's IP address
     */
    void setIpAddr(String IPAddr);

    /**
     * Gets the host’s RAM in GB.
     *
     * @return An integer representing the max RAM of the host
     */
    int getRam();

    /**
     * Sets the host’s RAM in GB.
     *
     * @param RAM the max RAM of the host
     */
    void setRam(int RAM);

    /**
     * Gets the host’s IOS (operating system).
     *
     * @return The host's {@link IOS} (operating system) 
     */
    IOS getOs();

    /**
     * Sets the host’s IOS (operating system).
     * 
     * @param os An {@link IOS} object representing the host's operating system
     */
    void setOs(IOS os);

    /**
     * Gets the source files directory on the host.
     *
     * @return A String with the host's directory o fthe source files
     */    
    String getSourceFileDir();

    /**
     * Sets the source files directory on the host.
     *
     * @param sourceFileDir A String with the source files directory on the host
     */    
    void setSourceFileDir(String sourceFileDir);

    /**
     * Gets the repos base directory on the host.
     *
     * @return A String with the host's repo base directory
     */    
    String getReposBaseDir();

    /**
     * Sets the repos base directory on the host.
     *
     * @param reposBaseDir A String with the repos base directory on the host
     */    
    void setReposBaseDir(String reposBaseDir);

    /**
     * Gets the reports base directory on the host.
     *
     * @return A String with the host's reports base directory
     */    
    String getReportsBaseDir();

    /**
     * Sets the reports base directory on the host.
     *
     * @param reportsBaseDir the host's reports base directory 
     */    
    void setReportsBaseDir(String reportsBaseDir);
    
    /**
     * Serialize the geographica host to a JSON string
     *
     * @return the serialized JSON string
     */
    String serializeToJSON();

    /**
     * Serialize the geographica host to a JSON file
     *
     * @param serObjFile File with the serialized object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    void serializeToJSON(File serObjFile) throws JsonGenerationException, JsonMappingException, IOException;
    
}
