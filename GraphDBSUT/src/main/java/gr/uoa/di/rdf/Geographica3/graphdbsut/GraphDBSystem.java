/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.graphdbsut;

import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;

public class GraphDBSystem extends RDF4JBasedGeographicaSystem {

    // --- Static members -----------------------------
    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful RDF4JBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        USEFULL_PREFIXES_MAP.put("ext", "<http://rdf.useekm.com/ext#>");
        logger = Logger.getLogger(GraphDBSystem.class.getSimpleName());
    }

    /**
     * Construct the system properties map for an RDF4J based system, which
     * has/should have the following list of properties. It is dependent to the
     * list of static final strings KEY_XXXXX above.
     *
     * @param baseDir the base directory of the RDF4J server repos, e.g.
     * "RDF4J_3.4.3_Repos/server"
     * @param repositoryId the repository name
     * @param indexes the indexing scheme to use (default: “spoc, posc”
     * specifies two indexes; a subject-predicate-object-context index and a
     * predicate-object-subject-context index
     * @param createOverwriteRepository <tt>true</tt> if existing repository
     * should be overwritten
     * @param hasLucene <tt>true</tt> if LuceneSail should be added to the SAIL
     * stack
     * @param wktIdxList a String with the LuceneSail custom #asWKT properties
     * list to spatially index
     * @return
     */
    public static Map<String, String> constructSysPropsMap(String baseDir,
            String repositoryId, String indexes, boolean createOverwriteRepository,
            boolean hasLucene, String wktIdxList) {
        Map<String, String> sysMap
                = RDF4JBasedGeographicaSystem.constructSysPropsMap(baseDir,
                        repositoryId, indexes, createOverwriteRepository);
        return sysMap;
    }

    // --- Data members ------------------------------
    // --- Constructors ------------------------------    
    public GraphDBSystem() {
    }

    public GraphDBSystem(Map<String, String> sysProps) throws Exception {
        super(sysProps);
    }

    // construct an GraphDBSystem from an existing repo location
    public GraphDBSystem(String baseDir, String repositoryId) throws Exception {
        super(baseDir, repositoryId);
    }

    // --- Methods -----------------------------------
    /**
     * Updates data members from the repository configuration. Assumes GraphDB
     * needs to override completely this method and not call super.
     *
     * @param repconfig the repository configuration object
     */
    @Override
    protected void updateDataMembersFromConfig(RepositoryConfig repconfig) {
        indexes = "";
        createOverwriteRepository = false;
    }
}
