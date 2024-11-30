package gr.uoa.di.rdf.geordfbench.graphdbsut;

import gr.uoa.di.rdf.geordfbench.runtime.sut.impl.RDF4JBasedSUT;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

/**
 * TODO: Remove class ASAP
 * It is used in create repo script and deals with spatial indexing
 * Move functionality elsewhere 
 * @author tioannid
 */
public class GraphDB {

    // ---------------- Static Mmebers & Methods ---------------------------
    protected static Logger logger = Logger.getLogger(RDF4JBasedSUT.class.getSimpleName());

    private static String[] GeoSPARQLPluginDDLQueries = new String[]{
        // Q1: Configure GeoSPARQL plugin
        "PREFIX : <http://www.ontotext.com/plugins/geosparql#>"
        + "\n INSERT DATA { _:s :ignoreErrors \"true\" . };"
        + "\n INSERT DATA { _:s :enabled \"true\" . };"
        + "\n INSERT DATA { _:s :prefixTree \"<<algorithm>>\"; :precision \"@@precision@@\" . };"
        + "\n INSERT DATA { _:s :maxBufferedDocs \"5000\" . };"
        + "\n INSERT DATA { _:s :ramBufferSizeMB \"512.0\" . };",
        // Q2: Enable plugin
        "PREFIX : <http://www.ontotext.com/plugins/geosparql#>"
        + "\n INSERT DATA { _:s :enabled \"true\" . }",
        // Q3: Disable plugin
        "PREFIX : <http://www.ontotext.com/plugins/geosparql#>"
        + "\n INSERT DATA { _:s :enabled \"false\" . }",
        // Q4: Force reindex geometry data
        //     usually used after a configuration change or when index files
        //     are either corrupted or have been mistakenly deleted
        "PREFIX : <http://www.ontotext.com/plugins/geosparql#>"
        + "\n INSERT DATA {_:s :forceReindex . }"

    };

    static final String EMPTY_TEMPLATE_TTL
            = "/home/tioannid/NetBeansProjects/PhD/Geographica/GraphDBSUT/graphdb-free_template.ttl";
    // --------------------- Data Members ----------------------------------
    private final String baseDir;     // base directory for repository manager
    private final LocalRepositoryManager repositoryManager;   // repository manager
    private final String repositoryId;    // repository Id
    private Repository repository;  // repository
    private RepositoryConnection connection;    // repository connection

    // --------------------- Constructors ----------------------------------
    // Constructor 1: Opens an existing repository <repositoryId> in <baseDir>
    public GraphDB(String baseDir, String repositoryId) {
        this(baseDir, repositoryId, false);
    }

    // Constructor 2: Creates a new EMPTY repository <repositoryId> in <baseDir>
    public GraphDB(String baseDir, String repositoryId, boolean createRepository) {
        this(baseDir, repositoryId, createRepository, EMPTY_TEMPLATE_TTL);
    }

    /* Constructor 3: Creates/Opens a repository <repositoryId> in <baseDir>
        **        following the template <templateTTL>
     */
    public GraphDB(String baseDir, String repositoryId, boolean createRepository, String templateTTL) {
        // check if baseDir exists, otherwise throw exception
        File dir = new File(baseDir);
        if (!dir.exists()) {
            throw new RuntimeException("Directory " + baseDir + " does not exist.");
        } else {
            this.baseDir = baseDir;
        }
        // create a new embedded instance of GraphDB in baseDir
        repositoryManager = new LocalRepositoryManager(dir);
        repositoryManager.init();
        // if repository does not exist check what the user requested
        if (!repositoryManager.hasRepositoryConfig(repositoryId)) {
            if (!createRepository) {    // do not create new repository
                throw new RuntimeException("Repository " + repositoryId + " does not exist. Cannot proceed unless a new repository is created!");
            } else { // create a new repository
                if (!createNewRepository(repositoryId, templateTTL)) {
                    throw new RuntimeException("Failed creating repository " + repositoryId);
                }
            }
        }
        // repository exists
        this.repositoryId = repositoryId;
        // open the repository configuration to check if it OK
        try {
            RepositoryConfig repconfig = repositoryManager.getRepositoryConfig(repositoryId);
        } catch (RepositoryConfigException e) {
            logger.error("GraphDB repository configuration exception " + e.toString());
            throw new RuntimeException("Error retrieving repository " + repositoryId + " configuration");
        } catch (RepositoryException e) {
            logger.error("GraphDB repository exception " + e.toString());
            throw new RuntimeException("Generic error with repository " + repositoryId + " configuration");
        }

        // retrieve the repository
        try {
            repository = repositoryManager.getRepository(repositoryId);
        } catch (RepositoryConfigException e) {
            logger.error("GraphDB repository configuration exception " + e.toString());
            throw new RuntimeException("Error retrieving repository " + repositoryId);
        } catch (RepositoryException e) {
            logger.error("GraphDB repository exception " + e.toString());
            throw new RuntimeException("Generic error with repository " + repositoryId);
        }
        // create a repository connection, otherwise throw exception
        connection = repository.getConnection();
        if (connection == null) {
            throw new RuntimeException("Could not establish connection to repository " + repositoryId);
        }
    }

    // --------------------- Data Accessors --------------------------------
    public String getBaseDir() {
        return baseDir;
    }

    public LocalRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Repository getRepository() {
        return repository;
    }

    public RepositoryConnection getConnection() {
        return connection;
    }

    // --------------------- Methods --------------------------------   
    /*
        ** Preconditions: baseDir and repositoryManager must be initialized
     */
    public boolean createNewRepository(String repositoryId, String templateTTL) {
        boolean result = false;
        InputStream config = null;
        try {
            TreeModel graph = new TreeModel();
            config = new BufferedInputStream(new FileInputStream(templateTTL));
            RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
            rdfParser.setRDFHandler(new StatementCollector(graph));
            rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
            config.close();
            Resource repositoryNode = Models.subject(graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElse(null);
            graph.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
                    SimpleValueFactory.getInstance().createLiteral(repositoryId));
            RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
            repositoryManager.addRepositoryConfig(repositoryConfig);
        } catch (FileNotFoundException ex) {
            logger.error("Template " + templateTTL + " was not found!");
        } catch (IOException ex) {
            logger.error("Error while parsing the repository configuration!");
        } finally {
            try {
                config.close();
                result = true;
            } catch (IOException ex) {
                logger.error("Error while parsing the repository configuration!");
            }
        }
        return result;
    }

    public void close() {
        logger.info("[GraphDB.close] Closing connection...");

        try {
            connection.commit();
        } catch (RepositoryException e) {
            logger.error("[GraphDB.close]", e);
        } finally {
            try {
                connection.close();
                repository.shutDown();
            } catch (RepositoryException e) {
                logger.error("[GraphDB.close]", e);
            }
            logger.info("[GraphDB.close] Connection closed.");
        }
    }

    // Execute GeoSPARQL Update the current configuration
    public static long execGeoSPARQL_UpdateConfiguration(String indexingAlgorith, int indexingPrecision, String baseDirString, String repositoryId) {
        String queryString = GeoSPARQLPluginDDLQueries[0];
        // we need to dynamically replace the indexing algorithm and precision
        queryString = queryString.replace("<<algorithm>>", indexingAlgorith);
        queryString = queryString.replace("@@precision@@", String.valueOf(indexingPrecision));
        return execQuery(baseDirString, repositoryId, queryString);
    }

    // Execute GeoSPARQL Enable plugin
    public static long execGeoSPARQL_EnablePlugin(String baseDirString, String repositoryId) {
        String queryString = GeoSPARQLPluginDDLQueries[1];
        return execQuery(baseDirString, repositoryId, queryString);
    }

    // Execute GeoSPARQL Enable plugin
    public static long execGeoSPARQL_DisablePlugin(String baseDirString, String repositoryId) {
        String queryString = GeoSPARQLPluginDDLQueries[2];
        return execQuery(baseDirString, repositoryId, queryString);
    }

    // Execute GeoSPARQL Enable plugin
    public static long execGeoSPARQL_ForceReindex(String baseDirString, String repositoryId) {
        String queryString = GeoSPARQLPluginDDLQueries[3];
        return execQuery(baseDirString, repositoryId, queryString);
    }

    // Execute GeoSPARQL DDL Query in <repoDir> 
    public static long execQuery(String baseDirString, String repositoryId, String query) {

        long start = System.currentTimeMillis();
        // create a new LocalRepositoryManager in <baseDirString>
        File baseDir = new File(baseDirString);
        LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
        manager.init();
        // request the repository <repoId> back from the LocalRepositoryManager
        Repository repository = null;
        try {
            repository = manager.getRepository(repositoryId);
            repository.init();
        } catch (RepositoryException | RepositoryConfigException e) {
            logger.error(e.getMessage());
        }
        // Open a connection to the database
//            RepositoryNotificationsListener listener = null;
//            NotifyingOwlimConnection nConn = null;
        RepositoryConnection conn = null;
        try {
            conn = repository.getConnection();

//                listener = new RepositoryNotificationsListener() {
//
//                    @Override
//                    public void transactionStarted(long tid) {
//                        System.out.println("Started transaction " + tid);
//                    }
//
//                    @Override
//                    public void transactionComplete(long tid) {
//                        System.out.println("Finished transaction " + tid);
//                    }
//
//                    @Override
//                    public void addStatement(Resource rsrc, IRI iri, Value value, Resource rsrc1, boolean bln, long l) {
//                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                    }
//
//                    @Override
//                    public void removeStatement(Resource rsrc, IRI iri, Value value, Resource rsrc1, boolean bln, long l) {
//                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                    }
//                };
//
//                nConn = new NotifyingOwlimConnection(conn);
//                IRI _s = SimpleValueFactory.getInstance().createIRI("_:s");
//                IRI _enabled = SimpleValueFactory.getInstance().createIRI(":enabled");
//                Literal _true = SimpleValueFactory.getInstance().createLiteral("\"true\"");
//                // subscribe for statements 
//                nConn.subscribe(listener, null, null, _true);
            conn.begin();
            Update updateQuery = conn.prepareUpdate(QueryLanguage.SPARQL, query);
            logger.info("Executing query : " + query + "\n");
            updateQuery.execute();
            conn.commit();
        } catch (UpdateExecutionException e) {
            logger.error(e.getMessage());
            conn.rollback();
        } catch (MalformedQueryException | RepositoryException e) {
            logger.error(e.getMessage());
            conn.rollback();

        } finally {
            // before our program exits, make sure the database is properly shut down.
            try {
                repository.shutDown();

            } catch (RepositoryException e) {
                logger.error(e.getMessage());
            }
        }
        return (System.currentTimeMillis() - start);
    }
}
