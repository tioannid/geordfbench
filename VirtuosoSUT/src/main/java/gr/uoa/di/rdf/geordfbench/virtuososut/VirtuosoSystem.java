/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.virtuososut;

import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import java.io.File;
import java.net.URL;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import virtuoso.rdf4j.driver.VirtuosoRepository;

public class VirtuosoSystem extends RDF4JBasedGeographicaSystem {

    // --- Static members -----------------------------
    static public final String KEY_SERVER_URL = "serverurl";
    static public final String KEY_USERNAME = "username";
    static public final String KEY_USERPASSWORD = "userpassword";
    static public final String KEY_QUERYTIMEOUT = "querytimeout";

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful RDF4JBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        USEFULL_PREFIXES_MAP.put("bif", "<http://www.openlinksw.com/schemas/bif#>");
        logger = Logger.getLogger(VirtuosoSystem.class.getSimpleName());
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
     * @param serverurl Stardog HTTP server URL, e.g. http://localhost:5820
     * @param userName
     * @param userPassword
     * @param queryTimeout Virtuoso repository's queryTimeout value (secs)
     * @return
     */
    public static Map<String, String> constructSysPropsMap(String baseDir,
            String repositoryId, String indexes, boolean createOverwriteRepository,
            String serverurl, String userName, String userPassword,
            int queryTimeout) {
        Map<String, String> sysMap
                = RDF4JBasedGeographicaSystem.constructSysPropsMap(baseDir,
                        repositoryId, indexes, createOverwriteRepository);
        // augment system properties with Stardog extra required authentication properties
        sysMap.put(KEY_SERVER_URL, serverurl);
        sysMap.put(KEY_USERNAME, userName);
        sysMap.put(KEY_USERPASSWORD, userPassword);
        sysMap.put(KEY_QUERYTIMEOUT, String.valueOf(queryTimeout));
        return sysMap;
    }

    // --- Data members ------------------------------
    protected String srvurl;
    protected String srvusername;
    protected String srvuserpasswd;
    // Specifically for Virtuoso, the querytimeout seems to not be affected by
    // the MaxQueryExecutionTime in the [SPARQL] section of virtuoso.ini of the
    // specific repository. The virtuoso.rdf4j.driver.VirtuosoRepository class
    // allows to set the queryTimeout and since the this.repository is recreated
    // in the init() method, we have to explicitly set the queryTimeout to the
    // value specified by the ExecSpec.
    protected int querytimeout;  // in seconds

    // --- Constructors ------------------------------    
    public VirtuosoSystem() {
    }

    public VirtuosoSystem(Map<String, String> sysProps) throws Exception {
        super(sysProps);
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

    @Override
    protected boolean validateSysProps(Map<String, String> sysProps) {
        boolean validates = super.validateSysProps(sysProps);
        // check if the additional mandatory properties for Virtuoso
        // exist in the sysPros
        validates = validates
                && sysProps.containsKey(KEY_SERVER_URL)
                && sysProps.containsKey(KEY_USERNAME)
                && sysProps.containsKey(KEY_USERPASSWORD);
        if (!validates) { // exit immediately if invalid
            return validates;
        }
        // assign map values to the additional data members for Virtuoso
        try {
            // additional mandatory fields for Virtuoso
            this.srvurl = sysProps.get(KEY_SERVER_URL);
            this.srvusername = sysProps.get(KEY_USERNAME);
            this.srvuserpasswd = sysProps.get(KEY_USERPASSWORD);
            this.querytimeout = Integer.valueOf(sysProps.get(KEY_QUERYTIMEOUT));
        } catch (Exception e) {
            validates = false;
            throw new RuntimeException("System properties validation failed!");
        }
        return validates;
    }

    @Override
    public void init() throws Exception {
        // check if <baseDir + "/" + repositoryId> exists, otherwise throw exception
        File dir = new File(baseDir + "/" + repositoryId);
        if (!dir.exists()) {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exist.");
        }
        // retrieve the repository and initialize it
        try {
            // Virtuoso server URL is HTTP based, but since we will use the
            // JDBC protocol, we need only the host:port part or the srvurl
            URL hostURL = new URL(srvurl);
            String jdbcVirtuosoURL = "jdbc:virtuoso://" + hostURL.getHost() + ":" + hostURL.getPort();
            this.repository = new VirtuosoRepository(jdbcVirtuosoURL, srvusername, srvuserpasswd);
            logger.info("Uninitialized VirtuosoRepository created with query timeout = " + ((VirtuosoRepository) repository).getQueryTimeout());
            ((VirtuosoRepository) repository).setQueryTimeout(this.querytimeout);
            this.repository.initialize();
            logger.info("Initialized VirtuosoRepository has query timeout = " + ((VirtuosoRepository) repository).getQueryTimeout());
        } catch (RepositoryException e) {
            logger.error("Virtuoso repository exception " + e.toString());
            throw new RuntimeException("Error retrieving database " + repositoryId);
        }
        // create a repository connection, otherwise throw exception
        connection = repository.getConnection();
        if (connection == null) {
            logger.error("Could not establish connection to database " + repositoryId);
            throw new RuntimeException("Could not establish connection to database " + repositoryId);
        }
        // STEP 5: Don't call the super class initialization procedure but do it manually
        this.initialized = true;
    }
}
