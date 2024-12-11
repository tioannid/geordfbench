/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.stardogsut;

import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.rdf4j.StardogRepository;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;

public class StardogSystem extends RDF4JBasedGeographicaSystem {

    // --- Static members -----------------------------
    static public final String KEY_SERVER_URL = "serverurl";
    static public final String KEY_USERNAME = "username";
    static public final String KEY_USERPASSWORD = "userpassword";

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful RDF4JBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        USEFULL_PREFIXES_MAP.put("unit", "<http://qudt.org/vocab/unit#>");
        logger = Logger.getLogger(StardogSystem.class.getSimpleName());
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
     * @return
     */
    public static Map<String, String> constructSysPropsMap(String baseDir,
            String repositoryId, String indexes, boolean createOverwriteRepository,
            String serverurl, String userName, String userPassword) {
        Map<String, String> sysMap
                = RDF4JBasedGeographicaSystem.constructSysPropsMap(baseDir,
                        repositoryId, indexes, createOverwriteRepository);
        // augment system properties with Stardog extra required authentication properties
        sysMap.put(KEY_SERVER_URL, serverurl);
        sysMap.put(KEY_USERNAME, userName);
        sysMap.put(KEY_USERPASSWORD, userPassword);
        return sysMap;
    }

    // --- Data members ------------------------------
    // --- Data members ------------------------------
    protected String srvurl;
    protected String srvusername;
    protected String srvuserpasswd;
    protected ConnectionConfiguration connectionConfiguration;

    // --- Constructors ------------------------------    
    public StardogSystem() {
    }

    public StardogSystem(Map<String, String> sysProps) throws Exception {
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
        } catch (Exception e) {
            validates = false;
            throw new RuntimeException("System properties validation failed!");
        }
        return validates;
    }

    @Override
    public void init() {
//        // First need to initialize the Stardog instance which will automatically start the embedded server.
//        Stardog stardog = Stardog.builder().create();
//        // start an http server on the default port
//        Server server = Stardog.buildServer().bind(new InetSocketAddress("localhost", 5820));
// For Stardog LocalRepositoryManager cannot be used, therfore
        // the member should be null
        repositoryManager = null;
        if (connectionConfiguration == null) {
            connectionConfiguration = ConnectionConfiguration.to(this.repositoryId)
                    .server(this.sysProps.get(KEY_SERVER_URL))
                    .credentials(this.sysProps.get(KEY_USERNAME), this.sysProps.get(KEY_USERPASSWORD));
        }
        //Connection aConn = connectionConfiguration.connect();
        if (repository == null) {
            repository = new StardogRepository(connectionConfiguration);
            repository.init();
        }

        if (connection == null) {
            //  - create a repository connection, otherwise throw exception
            connection = repository.getConnection();
            if (connection == null) {
                throw new RuntimeException("Could not establish connection to repository " + repositoryId);
            }
        }
        // STEP 5: Don't call the super class initialization procedure but do it manually
        this.initialized = true;
    }

}
