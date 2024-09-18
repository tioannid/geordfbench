/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import static gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.AbstractGeographicaSystem.lastMeasuredOperation;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import java.io.File;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.lucene.config.LuceneSailConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;

public class RDF4JSystem extends RDF4JBasedGeographicaSystem {

    // --- Static members -----------------------------
    static public final String KEY_HASLUCENE = "haslucene";
    static public final String KEY_WKTIDXLIST = "wktidxlist";

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful RDF4JBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(RDF4JSystem.class.getSimpleName());
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
        sysMap.put(KEY_HASLUCENE, Boolean.toString(hasLucene));
        sysMap.put(KEY_WKTIDXLIST, wktIdxList);
        return sysMap;
    }

    // --- Data members ------------------------------
    protected boolean hasLucene = false;
    protected String wktIdxList = "";

    // --- Constructors ------------------------------    
    public RDF4JSystem() {
    }

    public RDF4JSystem(Map<String, String> sysProps) throws Exception {
        super(sysProps);
    }

    // construct an RDF4JSystem from an existing repo location
    public RDF4JSystem(String baseDir, String repositoryId) throws Exception {
        super(baseDir, repositoryId);
    }

    /**
     * Updates data members from the repository configuration. May have
     * LuceneSail over NativeStore
     *
     * @param repconfig the repository configuration object
     */
    @Override
    protected void updateDataMembersFromConfig(RepositoryConfig repconfig) {
        NativeStoreConfig nsc;
        LuceneSailConfig lsc;
        SailImplConfig sic;
        SailRepositoryConfig src;

        src = (SailRepositoryConfig) repconfig.getRepositoryImplConfig();
        sic = src.getSailImplConfig();
        hasLucene = false;
        if (sic instanceof LuceneSailConfig) {
            hasLucene = true;
            wktIdxList = ((LuceneSailConfig) sic).getParameter("wktFields");
            lsc = (LuceneSailConfig) sic;
            nsc = (NativeStoreConfig) lsc.getDelegate();
        } else {
            nsc = (NativeStoreConfig) sic;
        }
    }

    // --- Methods -----------------------------------
    /**
     * Validates map values before they can be copied to object properties.
     *
     * @param sysProps A Map with system properties
     * @return a {@code true} value means that the provided properties are
     * accepted by the system, a {@code false} value means that some required
     * property by the system is missing or does not have an acceptable value.
     */
    @Override
    protected boolean validateSysProps(Map<String, String> sysProps) {
        boolean validates = super.validateSysProps(sysProps);
        if (!validates) { // exit immediately if invalid
            return validates;
        }
        // assign map values to data members
        try {
            // mandatory fields
//            this.baseDir = sysProps.get(KEY_BASEDIR);
//            this.repositoryId = sysProps.get(KEY_REPOSITORYID);
            // optional fields
            if (sysProps.containsKey(KEY_HASLUCENE)) {
                this.hasLucene = Boolean.parseBoolean(sysProps.get(KEY_HASLUCENE));
            }
            if (sysProps.containsKey(KEY_WKTIDXLIST)) {
                this.wktIdxList = sysProps.get(KEY_WKTIDXLIST);
            }
        } catch (Exception e) {
            validates = false;
            throw new RuntimeException("System properties validation failed!");
        }
        return validates;
    }

    /**
     * Initializes the RDF4JBasedGeographicaSystem using the system properties,
     * the last step of which is to set the {@link initialized} property to
     * {@code true}. It uses the mandatory fields {@link baseDir} and
     * {@link repositoryId} to create a repository manager. If the repository
     * does not exist and {@link createOverwriteRepository}=<tt>true</tt>
     * it creates it by using the provided system properties. If the repository
     * exists and {@link createOverwriteRepository}=<tt>true</tt>
     * the existing repository is removed first and it re-creates it by using
     * the provided system properties. Afterwards it dynamically retrieves and
     * updates the non mandatory data members from the configuration. Each
     * subclass should define in more detail the steps to properly initialize
     * the specific type of system, the main work being creating and setting the
     * connection property based on the appropriately initialized/set sysProps
     * map structure.
     */
    @Override
    public void init() throws Exception {
        RepositoryConfig repconfig;

        // STEP 1: Create the repository manager, which requires a valid baseDir
        //  - check if baseDir exists, otherwise throw exception
        File dir = new File(baseDir);
        if (!dir.exists()) {
            throw new RuntimeException("Directory " + baseDir + " does not exist.");
        }
        //  - create a LocalRepositoryManager and initialize it
        repositoryManager = new LocalRepositoryManager(dir);
        repositoryManager.init();

        // STEP 2: Does the repository exist?
        //  - if not create it
        //  - if yes drop and re-create it
        repconfig = getRepositoryConfig(repositoryId);
        if (repconfig == null) { // repository does not exist
            // create a new repository
            lastMeasuredOperation = createNativeLuceneRepoWithManager(baseDir, repositoryId, hasLucene, indexes, wktIdxList);
        } else { // repository exists
            if (createOverwriteRepository) { // re-create repository
                if (!repositoryManager.removeRepository(repositoryId)) {
                    throw new RuntimeException("Failed to remove repository " + repositoryId + "!");
                }
                logger.info("Successfully deleted repository " + repositoryId);
                lastMeasuredOperation = createNativeLuceneRepoWithManager(baseDir, repositoryId, hasLucene, indexes, wktIdxList);
            } else {
                logger.info("Repository " + repositoryId + " already exists, no need to create it!");
            }
        }

        // STEP 3: Get the repository and connection data members        
        //  - retrieve again the repository from the repository manager
        repconfig = getRepositoryConfig(repositoryId);
        try {
            repository = repositoryManager.getRepository(repositoryId);
        } catch (RepositoryConfigException e) {
            logger.error("Repository configuration exception " + e.toString());
            throw new RuntimeException("Error retrieving repository " + repositoryId);
        } catch (RepositoryException e) {
            logger.error("Repository exception " + e.toString());
            throw new RuntimeException("Generic error with repository " + repositoryId);
        }
        //  - create a repository connection, otherwise throw exception
        connection = repository.getConnection();
        if (connection == null) {
            throw new RuntimeException("Could not establish connection to repository " + repositoryId);
        }
        updateDataMembersFromConfig(repconfig);

        // STEP 5: Call the super class initialization procedure             
        super.init();
    }

}
