package gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.UnknownTransactionStateException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import static org.eclipse.rdf4j.sail.lucene.LuceneSail.WKT_FIELDS;
import org.eclipse.rdf4j.sail.lucene.impl.config.LuceneSailConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;

/**
 * An RDF4J base extention of the {@link AbstractGeographicaSystem<C>} abstract
 * implementation, which adds RDF4J specific repository management capabilities.
 * <c> = <org.eclipse.rdf4j.repository.RepositoryConnection>
 * This class should be subclassed to generate RDF4J based systems such as
 * GraphDB.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class RDF4JBasedGeographicaSystem extends AbstractGeographicaSystem<RepositoryConnection> {

    // --- Static members -----------------------------
    static public final String KEY_BASEDIR = "basedir";
    static public final String KEY_REPOSITORYID = "repositoryid";
    static public final String KEY_INDEXES = "indexes";
    static public final String KEY_CREATEOVERWRITEREPOSITORY = "createoverwriterepository";
    protected static LocalRepositoryManager repositoryManager;
    private static String[] validationQueries = new String[]{
        // Q1: returns the number of triples
        "SELECT (count(*) as ?count) WHERE { ?x ?p ?y . } ",
        // Q2: returns graph and the number of triples they contain
        "SELECT ?g (count(*) as ?count) WHERE { GRAPH ?g { ?x ?p ?y . } } GROUP BY ?g ORDER BY DESC(?count) ",
        // Q3: returns geometries and wkt that intersect with a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + " SELECT ?s1 ?o1 WHERE {\n"
        + " ?s1 geo:asWKT ?o1 .\n"
        + "  FILTER(geof:sfIntersects(?o1, \"POINT (-3.9468805 51.618055)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
        + "}",
        // Q4: returns geometries and wkt that their buffer intersects with a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + " SELECT ?s1 ?o1 WHERE {\n"
        + " ?s1 geo:asWKT ?o1 .\n"
        + "  FILTER(geof:sfIntersects(geof:buffer(?o1,0,<http://www.opengis.net/def/uom/OGC/1.0/metre>), \"POINT (-3.9468805 51.618055)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)).\n"
        + "}",
        // Q5: returns the buffer of a point
        "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n"
        + "PREFIX geo: <http://www.opengis.net/ont/geosparql#>  \n"
        + "PREFIX lgd: <http://data.linkedeodata.eu/ontology#>\n"
        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
        + "SELECT (geof:buffer(\"POINT(23.708496093749996 37.95719224376526)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 1, <http://www.opengis.net/def/uom/OGC/1.0/metre>) as ?o2)\n"
        + "WHERE { }"
    };

    // --- Static block/clause -----------------------
    static {
        // initialize the map of useful RDF4JBasedGeographicaSystem related RDF prefixes
        // Empty by intention. It is a reminder that subclasses should use the
        // static initialization block!
        logger = Logger.getLogger(RDF4JBasedGeographicaSystem.class.getSimpleName());
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
     * @return
     */
    public static Map<String, String> constructSysPropsMap(String baseDir,
            String repositoryId, String indexes,
            boolean createOverwriteRepository) {
        Map<String, String> sysMap = new HashMap<>();
        sysMap.put(KEY_BASEDIR, baseDir);
        sysMap.put(KEY_REPOSITORYID, repositoryId);
        sysMap.put(KEY_INDEXES, indexes);
        sysMap.put(KEY_CREATEOVERWRITEREPOSITORY, Boolean.toString(createOverwriteRepository));
        return sysMap;
    }

    // --- Data members ------------------------------
    protected String baseDir;
    protected String repositoryId;
    protected String indexes = "";
    protected boolean createOverwriteRepository = false;
    protected Repository repository;

    // --- Constructors ------------------------------    
    public RDF4JBasedGeographicaSystem() {

    }

    /**
     * Constructs an RDF4J based system connected to a specific repository
     * through a repository manager, using a map of system properties in order
     * get properly initialized with the init() function.
     *
     * @param sysProps a map of system properties
     */
    public RDF4JBasedGeographicaSystem(Map<String, String> sysProps) throws Exception {
        this.setSysProps(sysProps);
    }

    // construct an RDF4JBasedGeographicaSystem from an existing repo location
    /**
     * Constructs an RDF4J based system from an existing repository and
     * dynamically reading from the repository configuration the data members
     * for the RDF4J based system. Will create the repository manager only if
     * needed and if there is a valid RDF4J repository server directory. It is
     * not intended or suggested to use this to construct new repositories since
     * not all possible parameters can be provided.
     *
     * @param baseDir the base directory of the RDF4J server repos, e.g.
     * "RDF4J_3.4.3_Repos/server"
     * @param repositoryId the repository name
     */
    public RDF4JBasedGeographicaSystem(String baseDir, String repositoryId) throws Exception {
        RepositoryConfig repconfig;
        File dir;

        // set mandatory data mambers
        this.baseDir = baseDir;
        this.repositoryId = repositoryId;

        // retrieve or initialize the Repository Manager for baseDir
        if (repositoryManager == null) {
            //  - check if baseDir exists, otherwise throw exception
            dir = new File(baseDir);
            if (!dir.exists()) {
                throw new RuntimeException("Directory " + baseDir + " does not exist.");
            }
            // create a new LocalRepositoryManager
            repositoryManager = new LocalRepositoryManager(new File(baseDir));
            repositoryManager.init();
        }
        // check if repository exists throught the config
        repconfig = getRepositoryConfig(repositoryId);
        if (repconfig == null) { // repository does not exist
            throw new RuntimeException("Repository " + repositoryId + " does not exist. Cannot proceed unless a new repository is created!");
        } else {
            // retrieve the repository from the repository manager
            try {
                repository = repositoryManager.getRepository(repositoryId);
            } catch (RepositoryConfigException e) {
                logger.error("Repository configuration exception " + e.toString());
                throw new RuntimeException("Error retrieving repository " + repositoryId);
            } catch (RepositoryException e) {
                logger.error("Repository exception " + e.toString());
                throw new RuntimeException("Generic error with repository " + repositoryId);
            }
            // create a repository connection, otherwise throw exception
            try {
                connection = repository.getConnection();
            } catch (RepositoryException e) {
                logger.error("During RDF4JBasedGeographicaSystem construction and while trying to get a connection from an existing repository - RepositoryException - " + e.getMessage());
            }
            if (connection == null) {
                throw new RuntimeException("Could not establish connection to repository " + repositoryId);
            }
            updateDataMembersFromConfig(repconfig);
            super.init();
        }
    }

    protected RepositoryConfig getRepositoryConfig(String repositoryId) {
        RepositoryConfig repconfig = null;
        if (repositoryManager.hasRepositoryConfig(repositoryId)) {
            try {
                repconfig = repositoryManager.getRepositoryConfig(repositoryId);
            } catch (RepositoryConfigException e) {
                logger.error("Repository configuration exception " + e.toString());
                throw new RuntimeException("Error retrieving repository " + repositoryId + " configuration");
            } catch (RepositoryException e) {
                logger.error("Repository exception " + e.toString());
                throw new RuntimeException("Generic error with repository " + repositoryId + " configuration");
            }
        }
        return repconfig;
    }

    /**
     * Updates data members from the repository configuration. Assumes
     * NativeStore option only
     *
     * @param repconfig the repository configuration object
     */
    protected void updateDataMembersFromConfig(RepositoryConfig repconfig) {
        NativeStoreConfig nsc;
        SailImplConfig sic;
        LuceneSailConfig lsc;
        SailRepositoryConfig src;

        src = (SailRepositoryConfig) repconfig.getRepositoryImplConfig();
        sic = src.getSailImplConfig();

        if (sic instanceof LuceneSailConfig) {
            lsc = (LuceneSailConfig) sic;
            nsc = (NativeStoreConfig) lsc.getDelegate();
        } else {
            nsc = (NativeStoreConfig) sic;
        }
        indexes = nsc.getTripleIndexes();
        createOverwriteRepository = false;
    }

    /**
     * Set certain data members by reading the repository config. The approach
     * in this function is to load
     *
     * @param repconfig
     */
    protected void updateDataMembersFromConfig_new(RepositoryConfig repconfig) {
        RepositoryImplConfig ric;
        NativeStoreConfig nsc;
        LuceneSailConfig lsc;
        SailImplConfig sic;
        SailRepositoryConfig src;

        ric = repconfig.getRepositoryImplConfig();

        TreeModel graph = new TreeModel();
        Resource rs = ric.export(graph);
        logger.info(graph.toString());
        src = (SailRepositoryConfig) ric;
        sic = src.getSailImplConfig();
        nsc = (NativeStoreConfig) sic;
        indexes = nsc.getTripleIndexes();
        createOverwriteRepository = false;
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
        boolean validates = true;
        // check if the mandatory properties for this class
        // exist in the sysPros
        validates = validates
                && sysProps.containsKey(KEY_BASEDIR)
                && sysProps.containsKey(KEY_REPOSITORYID) //                && sysProps.containsKey(KEY_INDEXES)
                //                && sysProps.containsKey(KEY_CREATEOVERWRITEREPOSITORY)
                //                && sysProps.containsKey(KEY_HASLUCENE)
                //                && sysProps.containsKey(KEY_WKTIDXLIST)
                ;
        if (!validates) { // exit immediately if invalid
            return validates;
        }
        // assign map values to data members
        try {
            // mandatory fields
            this.baseDir = sysProps.get(KEY_BASEDIR);
            this.repositoryId = sysProps.get(KEY_REPOSITORYID);
            // optional fields
            if (sysProps.containsKey(KEY_INDEXES)) {
                this.indexes = sysProps.get(KEY_INDEXES);
            }
            if (sysProps.containsKey(KEY_CREATEOVERWRITEREPOSITORY)) {
                this.createOverwriteRepository = Boolean.valueOf(sysProps.get(KEY_CREATEOVERWRITEREPOSITORY));
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
        //  - create a LocalRepositoryManager and initialize it if 
        //    it doesn't exist because the more specific system's init()
        //    might had created already!
        if (repositoryManager == null) {
            repositoryManager = new LocalRepositoryManager(dir);
            repositoryManager.init();
        }

        // if connection has been created by the more specific system's init()
        // then skip creating the repo and the connection
        if (connection == null) {
            // STEP 2: Does the repository exist?
            //  - if not create it
            //  - if yes drop and re-create it
            repconfig = getRepositoryConfig(repositoryId);
            if (repconfig == null) { // repository does not exist
                if (!createOverwriteRepository) {    // do not create new repository
                    throw new RuntimeException("Repository " + repositoryId + " does not exist. Cannot proceed unless a new repository is created!");
                } else { // create a new repository
                    lastMeasuredOperation = createNativeLuceneRepoWithManager(baseDir, repositoryId, false, indexes, "");
                    repconfig = getRepositoryConfig(repositoryId);
                }
            } else { // repository exists
                if (createOverwriteRepository) { // re-create repository
                    if (!repositoryManager.removeRepository(repositoryId)) {
                        throw new RuntimeException("Failed to remove repository " + repositoryId + "!");
                    }
                    lastMeasuredOperation = createNativeLuceneRepoWithManager(baseDir, repositoryId, false, indexes, "");
                }
            }

            // STEP 3: Get the repository and connection data members        
            //  - retrieve again the repository from the repository manager
//            repconfig = getRepositoryConfig(repositoryId);
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
            try {
                connection = repository.getConnection();
            } catch (RepositoryException e) {
                logger.error("During RDF4JBasedGeographicaSystem init() and while trying to get a connection from an existing repository - RepositoryException - " + e.getMessage());
            }
//            catch (Throwable t) {
//                logger.error(t.getMessage());
//                repository.shutDown();
//                repository.init();
//                connection = repository.getConnection();
//            }
            if (connection == null) {
                throw new RuntimeException("Could not establish connection to repository " + repositoryId);
            }
        } else {
            repconfig = getRepositoryConfig(repositoryId);
        }
        updateDataMembersFromConfig(repconfig);

        // STEP 5: Call the super class initialization procedure             
        super.init();
    }

    // Commit transactions, close connection, shutdown undelying repository
    @Override
    public void close() {
        String errorMsg;
        logger.info("Closing connection...");
        try {
            connection.commit();
        } catch (UnknownTransactionStateException e) { // if the transaction state can not be determined. This can happen for instance when communication with a repository fails or times out.
            errorMsg = e.getMessage();
            logger.error("Upon connection commit - UnknownTransactionStateException - " + errorMsg);
        } catch (RepositoryException e) { // If the connection could not be committed, or if the connection does not have an active transaction.
            errorMsg = e.getMessage();
            logger.error("Upon connection commit - RepositoryException - " + errorMsg);
        } finally {
            try {
                if (connection.isOpen()) {
                    connection.close();
                    connection = null;
                }
            } catch (RepositoryException e) { // If the connection could not be closed.
                errorMsg = e.getMessage();
                logger.error("Upon connection close - RepositoryException - " + errorMsg);
            } catch (RuntimeException e) {
                errorMsg = e.getMessage();
                logger.error("Upon connection close - RuntimeException - " + errorMsg);
            }
            try {
                repository.shutDown(); // Shuts the repository down, releasing any resources that it keeps hold of. Once shut down, the repository can no longer be used until it is re-initialized.
                repository = null;
            } catch (RepositoryException e) { // An exception thrown by classes from the Repository API to indicate an error. Most of the time, this exception will wrap another exception that indicates the actual source of the error.
                errorMsg = e.getMessage();
                logger.error("Upon repository shut down - RepositoryException - " + errorMsg);
            } catch (RuntimeException e) {
                errorMsg = e.getMessage();
                logger.error("Upon repository shut down - RuntimeException - " + errorMsg);
            }
            logger.info("Repository closed.");
        }
        super.close();
    }

    /**
     * Create a Native Store with an optional Lucene Sail on top, using
     * optionally custom {@link indexes} and optionally custom list
     * {@link wktIdxList} of spatial indexed #asWKT properties.
     *
     * @param baseDir
     * @param repositoryId
     * @param hasLucene
     * @param indexes
     * @param wktIdxList
     * @return a long representing the duration of repository creation in msecs
     */
    public long createNativeLuceneRepoWithManager(String baseDir,
            String repositoryId, boolean hasLucene,
            String indexes, String wktIdxList) {
        long start = System.currentTimeMillis();

        // create a configuration for the SAIL stack
        if ("".equals(indexes)) {
            indexes = "spoc,posc";
        }
        SailImplConfig backendConfig = null;
        if (hasLucene) { // if requested, add lucene support
            logger.info("Creating NativeStore base sail with " + indexes + " indexes and forceSync = false");
            backendConfig = new NativeStoreConfig(indexes, false);
            logger.info("Adding Lucene sail on top of NativeStore");
            LuceneSailConfig lcfg = new LuceneSailConfig("./luceneidx", backendConfig);
            logger.info("Lucene sail will spatially index properties: " + wktIdxList);
            lcfg.setParameter(WKT_FIELDS, wktIdxList);
            backendConfig = lcfg;
        } else {
            logger.info("Creating NativeStore base sail with " + indexes + " indexes");
            backendConfig = new NativeStoreConfig(indexes);
        }
        // stack an inferencer config on top of our backend-config
        //backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);
        // create a configuration for the repository implementation
        RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
        // create a new RepositoryConfig object for <repoId>
        RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
        repositoryManager.addRepositoryConfig(repConfig);
        return (System.currentTimeMillis() - start);
    }

    // convert String --> RDFFormat
    private static RDFFormat stringToRDFFormat(String rdfFormatString) {
        // find which RDFFormat is supplied
        String suffix = "";
        RDFFormat rdffmt = null;
        if (rdfFormatString.equalsIgnoreCase(RDFFormat.NTRIPLES.getName())) { // N-Triples
            rdffmt = RDFFormat.NTRIPLES;
        } else if (rdfFormatString.equalsIgnoreCase(RDFFormat.TRIG.getName())) { // TRIG
            rdffmt = RDFFormat.TRIG;
        } else if (rdfFormatString.equalsIgnoreCase(RDFFormat.TURTLE.getName())) { // TRIG
            rdffmt = RDFFormat.TURTLE;
        } else {
            rdffmt = RDFFormat.NTRIPLES;        // default is N-Triples
        };
        return rdffmt;
    }

    // Load an RDF file in a Native RDF Repository in <repoDir>
    public long loadDirInNativeRepoWithManager(String rdfFileDir,
            String rdfFormatString, boolean printFlag) {
        RDFFormat rdffmt = stringToRDFFormat(rdfFormatString);
        long startLoad = System.currentTimeMillis(), endLoad;
        long startFile = 0;
        // Open a connection to the database
        // Using try-with-resources statement with a RepositoryConnection which
        // implements AutoCloseable means that the conn object will be closed in
        // all scenarios
        try (RepositoryConnection conn = repository.getConnection()) {
            File dir = new File(rdfFileDir);
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith("." + rdffmt.getDefaultFileExtension());
                }
            });
            for (File file : files) {
                try (InputStream input
                        = new FileInputStream(file)) {
                    if (printFlag) {
                        logger.info("Loading file " + file.getName() + " ...");
                        startFile = System.currentTimeMillis();
                    }
                    conn.add(input, "", rdffmt);
                    if (printFlag) {
                        logger.info("Finished loading file " + file.getName() + " in " + (System.currentTimeMillis() - startFile) + " msecs");
                    }
                } catch (IOException ex) {
                    logger.error(ex);
                } catch (RDFParseException ex) {
                    logger.error(ex);
                } catch (RepositoryException ex) {
                    logger.error(ex);
                }
            }
            endLoad = System.currentTimeMillis();
        }
        return (endLoad - startLoad);
    }

    // Query Native RDF Repository in <repoDir> for total records or records per graph 
    public long templateQueryNativeRepoWithManager(String baseDirString, String repoId, int queryNo) {
        String queryString = validationQueries[(queryNo <= validationQueries.length) ? queryNo - 1 : 0];
        long start = System.currentTimeMillis();
        if (repositoryManager == null) {
            // create a new LocalRepositoryManager in <baseDirString>
            File baseDir = new File(baseDirString);
            repositoryManager = new LocalRepositoryManager(baseDir);
            repositoryManager.init();
        }
        // request the repository <repoId> back from the LocalRepositoryManager
        if (repository == null) {
            repository = repositoryManager.getRepository(repoId);
            repository.init();
        }
        // Open a connection to the database
        // Using try-with-resources statement with a RepositoryConnection which
        // implements AutoCloseable means that the conn object will be closed in
        // all scenarios
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            System.out.println(queryString + "\n");
            try (TupleQueryResult result = tupleQuery.evaluate()) {

                // process results
                List<String> bindings = result.getBindingNames();
                String labelsTitle = "\t";
                for (String label : bindings) {
                    labelsTitle += (label + "\t\t");
                }
                System.out.println(labelsTitle + "\n------------------------------------>");
                BindingSet binding;
                String bindingLine = "";
                while (result.hasNext()) {
                    binding = result.next();
                    bindingLine = "";
                    for (String label : bindings) {
                        bindingLine += (binding.getValue(label) + "\t");
                    }
                    System.out.println(bindingLine);
                }
                System.out.println("<------------------------------------");
            }
        }
        return (System.currentTimeMillis() - start);
    }

}
