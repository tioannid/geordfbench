/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.rdf4jsut;

import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import static org.eclipse.rdf4j.sail.lucene.LuceneSail.WKT_FIELDS;
import org.eclipse.rdf4j.sail.lucene.config.LuceneSailConfig;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 08/05/2018
 */
public class Rdf4jSUT implements SystemUnderTest {

    // --------------------- Class Members ---------------------------------
    static Logger logger = Logger.getLogger(Rdf4jSUT.class.getSimpleName());
    /* The following commands run on UBUNTU 16.04LTS
    ** and demand that the <sudo xxx> commands are added to
    ** /etc/sudoers for the system user that will be running the test
     */
    static final String SYSCMD_SYNC = "sync";
    static final String SYSCMD_CLEARCACHE = "sudo /sbin/sysctl vm.drop_caches=3";

    /* Utility static nested class Rdf4jSUT.RDF4J
    ** Similar to GraphDB, encapsulates key objects of RDF4J
     */
    public static class RDF4J {

        // ---------------- Static Mmebers & Methods ---------------------------
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

        // Creating a Native RDF Repository in <repoDir>
        public static long createNativeRepo(String repoDir, String indexes) {
            long start = System.currentTimeMillis();
            File dataDir = new File(repoDir);
            NativeStore ns = null;
            if ("".equals(indexes)) {
                ns = new NativeStore(dataDir, "spoc,posc");
            } else {
                ns = new NativeStore(dataDir, indexes);
            }
            LuceneSail lucenesail = new LuceneSail();
            // set any parameters
            // ... this one stores the Lucene index files into memory
            lucenesail.setParameter(LuceneSail.WKT_FIELDS,
                    "http://www.opengis.net/ont/geosparql#asWKT "
                    + "http://geo.linkedopendata.gr/corine/ontology#asWKT "
                    + "http://dbpedia.org/property/asWKT "
                    + "http://geo.linkedopendata.gr/gag/ontology/asWKT "
                    + "http://www.geonames.org/ontology#asWKT "
                    + "http://teleios.di.uoa.gr/ontologies/noaOntology.owl#asWKT "
                    + "http://linkedgeodata.org/ontology/asWKT");
            // ... this one stores the Lucene index files into memory
            lucenesail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
            // wrap base sail
            lucenesail.setBaseSail(ns);
            Repository repo = new SailRepository(lucenesail);
            repo.initialize();
            return (System.currentTimeMillis() - start);
        }

        // Creating a Native RDF Repository in <repoDir> with optional Lucene support
        public static long createNativeRepoWithManager(String baseDirString, String repoId, boolean removeExisting, boolean hasLucene, String indexes, String wktIdxList) {
            long start = System.currentTimeMillis();
            // create a new LocalRepositoryManager in <baseDirString>
            File baseDir = new File(baseDirString);
            LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
            manager.initialize();
            // if necessary remove existing repository
            if (removeExisting) {
                if (manager.hasRepositoryConfig(repoId)) {
                    if (!manager.removeRepository(repoId)) {
                        return -1; // signals ERROR
                    }
                }
            }
            // create a configuration for the SAIL stack
            if ("".equals(indexes)) {
                indexes = "spoc,posc";
            }
            SailImplConfig backendConfig = new NativeStoreConfig(indexes);
            if (hasLucene) { // if requested, add lucene support
                LuceneSailConfig lcfg = new LuceneSailConfig("./luceneidx", backendConfig);
                lcfg.setParameter(WKT_FIELDS, wktIdxList);
                backendConfig = lcfg;
            }
            // stack an inferencer config on top of our backend-config
            //backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);
            // create a configuration for the repository implementation
            RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
            // create a new RepositoryConfig object for <repoId>
            RepositoryConfig repConfig = new RepositoryConfig(repoId, repositoryTypeSpec);
            manager.addRepositoryConfig(repConfig);
            Repository repository = manager.getRepository(repoId);
            repository.initialize();
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

        // Load a rdf file in a Native RDF Repository in <repoDir>
        public static long loadInNativeRepoWithManager(String baseDirString, String repoId, String rdfFormatString, String file) {
            RDFFormat rdffmt = stringToRDFFormat(rdfFormatString);
            long start = System.currentTimeMillis();
            // create a new LocalRepositoryManager in <baseDirString>
            File baseDir = new File(baseDirString);
            LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
            manager.initialize();
            // request the repository <repoId> back from the LocalRepositoryManager
            Repository repository = manager.getRepository(repoId);
            repository.initialize();
            // Open a connection to the database
            try (RepositoryConnection conn = repository.getConnection()) {
                try (InputStream input
                        = new FileInputStream(file)) {
                    // add the RDF data from the inputstream directly to our database
                    conn.add(input, "", rdffmt);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RDFParseException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RepositoryException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repository.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // Load a TRIG file in a Native RDF Repository in <repoDir>
        public static long loadInNativeRepo(String repoDir, String rdfFormatString, String file) {
            RDFFormat rdffmt = stringToRDFFormat(rdfFormatString);
            long start = System.currentTimeMillis();
            File dataDir = new File(repoDir);
            Repository repo = new SailRepository(new NativeStore(dataDir));
            repo.initialize();
            // Open a connection to the database
            try (RepositoryConnection conn = repo.getConnection()) {
                try (InputStream input
                        = new FileInputStream(file)) {
                    // add the RDF data from the inputstream directly to our database
                    conn.add(input, "", rdffmt);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RDFParseException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RepositoryException ex) {
                    java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repo.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // Load a TRIG file in a Native RDF Repository in <repoDir>
        public static long loadDirInNativeRepo(String repoDir, String rdfFormatString, String trigFileDir, boolean printFlag) {
            RDFFormat rdffmt = stringToRDFFormat(rdfFormatString);
            long start = System.currentTimeMillis();
            long t1 = 0;
            File dataDir = new File(repoDir);
            Repository repo = new SailRepository(new NativeStore(dataDir));
            repo.initialize();
            // Open a connection to the database
            try (RepositoryConnection conn = repo.getConnection()) {
                File dir = new File(trigFileDir);
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
                            t1 = System.currentTimeMillis();
                        }
                        conn.add(input, "", rdffmt);
                        if (printFlag) {
                            logger.info("Finished loading file " + file.getName() + " in " + (System.currentTimeMillis() - t1) + " msecs");
                        }
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RDFParseException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RepositoryException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repo.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // Load a TRIG file in a Native RDF Repository in <repoDir>
        public static long loadDirInNativeRepoWithManager(String baseDirString, String repoId, String rdfFormatString, String trigFileDir, boolean printFlag) {
            RDFFormat rdffmt = stringToRDFFormat(rdfFormatString);
            long start = System.currentTimeMillis();
            long t1 = 0;
            // create a new LocalRepositoryManager in <baseDirString>
            File baseDir = new File(baseDirString);
            LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
            manager.initialize();
            // request the repository <repoId> back from the LocalRepositoryManager
            Repository repository = manager.getRepository(repoId);
            repository.initialize();
            // Open a connection to the database
            try (RepositoryConnection conn = repository.getConnection()) {
                File dir = new File(trigFileDir);
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
                            t1 = System.currentTimeMillis();
                        }
                        conn.add(input, "", rdffmt);
                        if (printFlag) {
                            logger.info("Finished loading file " + file.getName() + " in " + (System.currentTimeMillis() - t1) + " msecs");
                        }
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RDFParseException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RepositoryException ex) {
                        java.util.logging.Logger.getLogger(Rdf4jSUT.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repository.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // Query Native RDF Repository in <repoDir> for total records or records per graph 
        public static long templateQueryInNativeRepo(String repoDir, int queryNo) {
            String queryString = validationQueries[(queryNo <= validationQueries.length) ? queryNo - 1 : 0];
            long start = System.currentTimeMillis();
            File dataDir = new File(repoDir);
            Repository repo = new SailRepository(new NativeStore(dataDir));
            repo.initialize();
            // Open a connection to the database
            try (RepositoryConnection conn = repo.getConnection()) {
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
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repo.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // Query Native RDF Repository in <repoDir> for total records or records per graph 
        public static long templateQueryNativeRepoWithManager(String baseDirString, String repoId, int queryNo) {
            String queryString = validationQueries[(queryNo <= validationQueries.length) ? queryNo - 1 : 0];
            long start = System.currentTimeMillis();
            // create a new LocalRepositoryManager in <baseDirString>
            File baseDir = new File(baseDirString);
            LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
            manager.initialize();
            // request the repository <repoId> back from the LocalRepositoryManager
            Repository repository = manager.getRepository(repoId);
            repository.initialize();
            // Open a connection to the database
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
            } finally {
                // before our program exits, make sure the database is properly shut down.
                repository.shutDown();
            }
            return (System.currentTimeMillis() - start);
        }

        // --------------------- Data Members ----------------------------------
        private final String baseDir;     // base directory for repository manager
        private final LocalRepositoryManager repositoryManager;   // repository manager
        private final String repositoryId;    // repository Id
        private Repository repository;  // repository
        private RepositoryConfig repconfig; // repository configuration
        private RepositoryConnection connection;    // repository connection
        private String indexes;
        private boolean hasLucene;

        // --------------------- Constructors ----------------------------------
        // Constructor 1: Connects to a repository <baseDir> using default indexes
        public RDF4J(String baseDir) {
            this(baseDir, null);
        }

        // Constructor 2: Connects to a repository <baseDir> using <indexes>
        public RDF4J(String repoDir, String indexes) {
            repositoryManager = null;
            repositoryId = "";
            this.indexes = indexes;
            this.hasLucene = false;
            // check if repoDir exists, otherwise throw exception
            File dir = new File(repoDir);
            if (!dir.exists()) {
                throw new RuntimeException("Directory " + repoDir + " does not exist.");
            } else {
                this.baseDir = repoDir;
            }
            NativeStore ns;
            if (indexes != null) { // if present use explicitly provided indexes
                ns = new NativeStore(dir, indexes);
            } else {    // otherwise use the default indexes provided by NativeStore
                ns = new NativeStore(dir);
            }
            this.repository = new SailRepository(ns);
            this.repository.initialize();

            // create a repository connection, otherwise throw exception
            try {
                this.connection = this.repository.getConnection();
            } catch (Exception e) {
                logger.error(e.toString());
            }
            if (this.connection == null) {
                throw new RuntimeException("Could not establish connection to RDF4J repository in directory " + this.baseDir);
            }
        }

        // Constructor 3: Connects to a repository with a repo manager <baseDir> using <indexes>
        public RDF4J(String baseDir, String repositoryId, boolean createRepository, boolean hasLucene, String indexes, String wktIdxList) {
            // check if baseDir exists, otherwise throw exception
            File dir = new File(baseDir);
            if (!dir.exists()) {
                throw new RuntimeException("Directory " + baseDir + " does not exist.");
            } else {
                this.baseDir = baseDir;
            }
            // create a new embedded instance of RDF4J in baseDir
            repositoryManager = new LocalRepositoryManager(dir);
            repositoryManager.initialize();
            // if repository does not exist check what the user requested
            if (!repositoryManager.hasRepositoryConfig(repositoryId)) {
                if (!createRepository) {    // do not create new repository
                    throw new RuntimeException("Repository " + repositoryId + " does not exist. Cannot proceed unless a new repository is created!");
                } else { // create a new repository
                    createNativeRepoWithManager(baseDir, repositoryId, createRepository, hasLucene, indexes, wktIdxList);
                    this.hasLucene = hasLucene;
                }
            }
            // repository exists
            this.repositoryId = repositoryId;
            RepositoryConfig repconfig;
            // open the repository configuration to check if it OK
            try {
                repconfig = repositoryManager.getRepositoryConfig(repositoryId);
            } catch (RepositoryConfigException e) {
                logger.error("RDF4J repository configuration exception " + e.toString());
                throw new RuntimeException("Error retrieving repository " + repositoryId + " configuration");
            } catch (RepositoryException e) {
                logger.error("RDF4J repository exception " + e.toString());
                throw new RuntimeException("Generic error with repository " + repositoryId + " configuration");
            }

            // retrieve the repository
            try {
                repository = repositoryManager.getRepository(repositoryId);
            } catch (RepositoryConfigException e) {
                logger.error("RDF4J repository configuration exception " + e.toString());
                throw new RuntimeException("Error retrieving repository " + repositoryId);
            } catch (RepositoryException e) {
                logger.error("RDF4J repository exception " + e.toString());
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

        public RepositoryConnection getConnection() {
            return connection;
        }

        public Repository getRepo() {
            return this.repository;
        }

        public String getIndexes() {
            return this.indexes;
        }

        // --------------------- Methods --------------------------------   
        public void close() {
            logger.info("[RDF4J.close] Closing connection...");

            try {
                connection.commit();
            } catch (RepositoryException e) {
                logger.error("[RDF4J.close]", e);
            } finally {
                try {
                    connection.close();
                    this.repository.shutDown();
                } catch (RepositoryException e) {
                    logger.error("[RDF4J.close]", e);
                }
                logger.info("[RDF4J.close] Connection closed.");
            }
        }
    }

    /* Utility class RDF4JSUT.Executor
    ** Executes queries on RDF4J
     */
    static class Executor implements Runnable {

        // --------------------- Data Members ----------------------------------
        private final String query;
        private final RDF4J sut;
        private BindingSet firstBindingSet;
        private int dispres;
        /*
        private long evaluationTime,
                fullResultScanTime,
                noOfResults;
         */
        private long[] returnValue;

        // --------------------- Constructors ----------------------------------
        public Executor(String query, RDF4J sut, int timeoutSecs, int dispres) {
            this.query = query;
            this.sut = sut;
            this.dispres = dispres;
            this.returnValue = new long[]{timeoutSecs + 1, timeoutSecs + 1, timeoutSecs + 1, -1};
        }

        // --------------------- Data Accessors --------------------------------
        public long[] getRetValue() {
            return returnValue;
        }

        public BindingSet getFirstBindingSet() {
            return firstBindingSet;
        }

        // --------------------- Methods --------------------------------
        @Override
        public void run() {
            try {
                runQueryPrintLimitedRows(this.dispres);
            } catch (MalformedQueryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (QueryEvaluationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TupleQueryResultHandlerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private void runQueryPrintLimitedRows(int rowsToDisplay) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {
            long t1 = 0, t2 = 0, t3 = 0;
            long results = 0, noOfScanErrors = 0;
            int printedrow = 0;
            String bindingLine = "", labelsTitle = "\t";
            List<String> bindings = null;
            boolean displayRowsFlag = (rowsToDisplay != 0);

            logger.info("Evaluating query...");
            TupleQuery tupleQuery = sut.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);

            // Evaluate and time the evaluation of the prepared query
            TupleQueryResult tupleQueryResult = null;
            t1 = System.nanoTime();
            // if there is an exception during evaluation throw it and return
            try {
                tupleQueryResult = tupleQuery.evaluate();
            } catch (QueryEvaluationException ex) {
                logger.error("[Query evaluation phase]", ex);
                throw new QueryEvaluationException("[Query evaluation phase]", ex);
            }
            t2 = System.nanoTime();

            // if there is a valid request for rows to display, first display headers
            if (displayRowsFlag) {
                // process results
                bindings = tupleQueryResult.getBindingNames();
                for (String label : bindings) {
                    labelsTitle += (label + "\t\t");
                }
                logger.info(labelsTitle);
                logger.info("------------------------------------>");

                while (tupleQueryResult.hasNext()) {
                    try {
                        firstBindingSet = tupleQueryResult.next();

                        if (printedrow < rowsToDisplay) {
                            bindingLine = "";
                            for (String label : bindings) {
                                bindingLine += (firstBindingSet.getValue(label) + "\t");
                            }
                            logger.info(bindingLine);
                            printedrow++;
                        }

                        results++;
                    } catch (Exception ex) {
                        noOfScanErrors++;
                        logger.error("[Query full scan phase]");
                    }
                }
            } else {
                while (tupleQueryResult.hasNext()) {
                    try {
                        firstBindingSet = tupleQueryResult.next();
                        results++;
                    } catch (Exception ex) {
                        noOfScanErrors++;
                        logger.error("[Query full scan phase]");
                    }
                }
            }

            t3 = System.nanoTime();
            if (displayRowsFlag) {
                logger.info("<------------------------------------\n");
            }
            logger.info("Query evaluated with " + results + " results and " + noOfScanErrors + " scan errors!");
            this.returnValue = new long[]{t2 - t1, t3 - t2, t3 - t1, results};
        }
    }

// --------------------- Data Members ----------------------------------
    private String baseDir;     // base directory for repository manager
    private String repositoryId;    // repository Id
    private boolean createRepository;
    private boolean hasLucene;
    private String indexes;
    private String wktIdxList;
    private int displres;
    private RDF4J rdf4j;
    private BindingSet firstBindingSet;

    // --------------------- Constructors ----------------------------------
    public Rdf4jSUT(String baseDir, String repositoryId, boolean createRepository, boolean hasLucene, String indexes, String wktIdxList, int displres) {
        this.baseDir = baseDir;
        this.repositoryId = repositoryId;
        this.createRepository = createRepository;
        this.hasLucene = hasLucene;
        this.indexes = indexes;
        this.wktIdxList = wktIdxList;
        this.displres = displres;
    }

    // --------------------- Data Accessors --------------------------------
    @Override
    public BindingSet getFirstBindingSet() {
        return firstBindingSet;
    }

    // --------------------- Methods --------------------------------
    @Override
    public Object getSystem() {
        return rdf4j;
    }

    @Override
    public void initialize() {
        try {
            rdf4j = new RDF4J(baseDir, repositoryId, createRepository, hasLucene, indexes, wktIdxList);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize RDF4J(\"" + baseDir + "\")");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.fatal(sw.toString());
        }
    }

    @Override
    public long[] runQueryWithTimeout(String query, int timeoutSecs) throws Exception {
        //maintains a thread for executing the doWork method
        final ExecutorService pool = Executors.newFixedThreadPool(1);
        //set the pool thread working
        Executor runnable = new Executor(query, rdf4j, timeoutSecs, this.displres);

        final Future<?> future = pool.submit(runnable);
        boolean isTimedout = false;
        //check the outcome of the pool thread and limit the time allowed for it to complete
        long tt1 = System.nanoTime();
        try {
            logger.debug("Future started");
            /* Wait if necessary for at most <timeoutsSecs> for the computation 
            ** to complete, and then retrieves its result, if available */
            future.get(timeoutSecs, TimeUnit.SECONDS);
            logger.debug("Future end");
        } catch (InterruptedException e) { // current thread was interrupted while waiting
            logger.debug(e.toString());
        } catch (ExecutionException e) {    // the computation threw an exception
            logger.debug(e.toString());
        } catch (TimeoutException e) {  // the wait timed out
            isTimedout = true;
            logger.info("timed out!");
            logger.info("Restarting RDF4J...");
            this.restart();
            logger.info("Closing RDF4J...");
            this.close();
        } finally {
            logger.debug("Future canceling...");
            future.cancel(true);
            logger.debug("Executor shutting down...");
            pool.shutdown();
            try {
                logger.debug("Executor waiting for termination...");
                pool.awaitTermination(timeoutSecs, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.debug(e.toString());
            }
            System.gc();
        }

        // logger.debug("RetValue: " + runnable.getExecutorResults());
        logger.debug("RetValue: " + runnable.getRetValue());

        if (isTimedout) {
            long tt2 = System.nanoTime();
            return new long[]{tt2 - tt1, tt2 - tt1, tt2 - tt1, -1};
        } else {
            this.firstBindingSet = runnable.getFirstBindingSet();
            //return runnable.getExecutorResults();
            return runnable.getRetValue();
        }
    }

    @Override
    public long[] runUpdate(String query) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

        logger.info("Executing update...");
        long t1 = System.nanoTime();

        Update preparedUpdate = null;
        try {
            preparedUpdate = this.rdf4j.getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
        } catch (RepositoryException e) {
            logger.error("[RDF4J.update]", e);
        }
        logger.info("[RDF4J.update] executing update query: " + query);

        try {
            preparedUpdate.execute();
        } catch (UpdateExecutionException e) {
            logger.error("[RDF4J.update]", e);
        }

        long t2 = System.nanoTime();
        logger.info("Update executed");

        long[] ret = {-1, -1, t2 - t1, -1};
        return ret;
    }

    @Override
    public void close() {
        logger.info("Closing..");
        try {
            rdf4j.close();
            rdf4j = null;
            firstBindingSet = null;
        } catch (Exception e) {

        }
        // TODO - Να ελέγξω ποιός και τί ήθελε να κάνει!
        // Runtime run = Runtime.getRuntime();
        // Process pr = run.exec(restart_script);
        // pr.waitFor();
        //
        System.gc();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.fatal("Cannot clear caches");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        logger.info("Closed (caches not cleared)");
    }

    @Override
    public void clearCaches() {

        String[] sys_sync = {"/bin/sh", "-c", SYSCMD_SYNC};
        String[] clear_caches = {"/bin/sh", "-c", SYSCMD_CLEARCACHE};

        Process pr;

        try {
            logger.info("Clearing caches...");

            pr = Runtime.getRuntime().exec(sys_sync);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while system sync");
            }

            pr = Runtime.getRuntime().exec(clear_caches);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while clearing caches");
            }

            Thread.sleep(5000);
            logger.info("Caches cleared");
        } catch (Exception e) {
            logger.fatal("Cannot clear caches");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    @Override
    public void restart() {
        Process pr;

        try {
            logger.info("Restarting RDF4J ...");

            if (rdf4j != null) {
                try {
                    rdf4j.close();
                } catch (Exception e) {
                    logger.error("Exception occured while restarting RDF4J. ");
                    logger.debug(e.toString());
                } finally {
                    rdf4j = null;
                }
            }
            firstBindingSet = null;
            rdf4j = new RDF4J(baseDir);
            logger.info("RDF4J restarted");
        } catch (RuntimeException e) {
            logger.fatal("Cannot restart RDF4J");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    @Override
    public String translateQuery(String query, String label) {
        String translatedQuery = null;
        translatedQuery = query;

        if (label.matches("Q6_Area_CLC")) {
            translatedQuery = translatedQuery.replaceAll("strdf:area", "geof:area");
        } else if (label.indexOf("Synthetic_Selection_Distance") != -1) { // Synthetic_POIs experiment
            // convert this: FILTER ( bif:st_within(?geo1, bif:st_point(45, 45), 5000.000000)) 
            // .....to this: FILTER ( geof:sfWithin(?geo1, geof:buffer("POINT(23.71622 37.97945)"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 5000, <http://www.opengis.net/def/uom/OGC/1.0/metre>)))
            // 1. locate the last part of the query which starts with FILTER
            String cGeom = "";
            long cRadious = 0;
            String oldFilter = translatedQuery.substring(translatedQuery.indexOf("FILTER"));
            // 2. split to 4 parts using the comma as delimiter
            String[] oldfilterPart = oldFilter.split(",");
            // 3. split part-0 using the ( as delimiter
            //    ?geo1 is portion-2 of part-0
            cGeom = oldfilterPart[0].split("\\(")[2];
            // 4. split part-3 using the ) as delimiter
            //    RADIOUS is portion-0 of part-3 converted to long 
            cRadious = (long) Float.parseFloat(oldfilterPart[3].split("\\)")[0]);
            // In the SyntheticOnly experiment the radius is in meters therefore 
            // we would have had to divide by 100000 to convert it to degrees, because this
            // the analogy of Km/degree around the average latitude 45 but RDF4J
            // uses geof:buffer(geom, radius, uom:metre) and therefore there is
            // no need to convert cRadious!! The SyntheticGenerator especially for POIs returns
            // radious in Km, that is the cRadius is already divide by 1000 and
            // in order to be converted back to meters, it has to multiplied by 1000
            cRadious = cRadious * 1000;
            // 5. create the new filter using the desired format
            String newFilter = String.format("FILTER(geof:sfWithin(%s, geof:buffer(\"POINT(45 45)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, %d, <http://www.opengis.net/def/uom/OGC/1.0/metre>))).\n}\n", cGeom, cRadious);
            // 6. replace old with new filter
            translatedQuery = translatedQuery.substring(0, translatedQuery.indexOf("FILTER")) + newFilter;
        }

        return translatedQuery;
    }

}
