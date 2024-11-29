/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.graphdbsut;

import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

/**
 *
 * @author tioannid
 */
public class GraphDBSUT implements SystemUnderTest {

    // --------------------- Class Members ---------------------------------
    static Logger logger = Logger.getLogger(GraphDBSUT.class.getSimpleName());
    /* The following commands run on UBUNTU 16.04LTS
    ** and demand that the <sudo xxx> commands are added to
    ** /etc/sudoers for the system user that will be running the test
     */
    static final String SYSCMD_SYNC = "sync";
    static final String SYSCMD_CLEARCACHE = "sudo /sbin/sysctl vm.drop_caches=3";

    /* Utility static nested class GraphDBSUT.GraphDB
    ** Similar to Strabon, encapsulates key objects of GraphDB
     */
    public static class GraphDB {

        // ---------------- Static Mmebers & Methods ---------------------------
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

    /* Utility class GraphDBSUT.Executor
    ** Executes queries on GraphDB
     */
    static class Executor implements Runnable {

        // --------------------- Data Members ----------------------------------
        private final String query;
        private final GraphDB graphDB;
        private BindingSet firstBindingSet;
        private int dispres;
        /*
        private long evaluationTime,
                fullResultScanTime,
                noOfResults;
         */
        private long[] returnValue;

        // --------------------- Constructors ----------------------------------
        public Executor(String query, GraphDB graphDB, int timeoutSecs, int dispres) {
            this.query = query;
            this.graphDB = graphDB;
            this.dispres = dispres;
            /*
            this.evaluationTime = timeoutSecs + 1;
            this.fullResultScanTime = timeoutSecs + 1;
            this.noOfResults = -1;
             */
            this.returnValue = new long[]{timeoutSecs + 1, timeoutSecs + 1, timeoutSecs + 1, -1};
        }

        // --------------------- Data Accessors --------------------------------
        /*
        public long[] getExecutorResults() {
            return new long[]{evaluationTime, fullResultScanTime, evaluationTime + fullResultScanTime, noOfResults};
        }
         */
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
                //runQuery();
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

//        private void runQuery() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {
//
//            logger.info("Evaluating query...");
//            TupleQuery tupleQuery = graphDB.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
//
//            // Evaluate and time the evaluation of the prepared query
//            // noOfResults = 0;
//            long results = 0;
//
//            long t1 = System.nanoTime();
//            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
//            long t2 = System.nanoTime();
//
//            if (tupleQueryResult.hasNext()) {
//                firstBindingSet = tupleQueryResult.next();
//                //noOfResults++;
//                results++;
//            }
//
//            while (tupleQueryResult.hasNext()) {
//                //noOfResults++;
//                results++;
//                tupleQueryResult.next();
//            }
//            long t3 = System.nanoTime();
//
//            logger.info("Query evaluated");
//
//            // Calculate durations: Evaluation, Full ResultSet Scan
//            /*
//            fullResultScanTime = System.nanoTime() - t2;
//            evaluationTime = t2 - t1;
//             */
//            this.returnValue = new long[]{t2 - t1, t3 - t2, t3 - t1, results};
//        }
//        private void runQueryPrintLimit(int rowsToDisplay) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {
//            long t1 = 0, t2 = 0, t3 = 0;
//            long results = 0, noOfScanErrors = 0;
//            int printedrow = 0;
//            String bindingLine = "", labelsTitle = "\t";
//            List<String> bindings = null;
//            boolean displayRowsFlag = (rowsToDisplay != 0);
//
//            logger.info("Evaluating query...");
//            TupleQuery tupleQuery = graphDB.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
//
//            // Evaluate and time the evaluation of the prepared query
//            TupleQueryResult tupleQueryResult = null;
//            t1 = System.nanoTime();
//            // if there is an exception during evaluation throw it and return
//            try {
//                tupleQueryResult = tupleQuery.evaluate();
//            } catch (QueryEvaluationException ex) {
//                logger.error("[Query evaluation phase]", ex);
//                throw new QueryEvaluationException("[Query evaluation phase]", ex);
//            }
//            t2 = System.nanoTime();
//
//            // if there is a valid request for rows to display, first display headers
//            if (displayRowsFlag) {
//                // process results
//                bindings = tupleQueryResult.getBindingNames();
//                for (String label : bindings) {
//                    labelsTitle += (label + "\t\t");
//                }
//                logger.info("\n" + labelsTitle + "\n------------------------------------>");
//            }
//
//            while (tupleQueryResult.hasNext()) {
//                try {
//                    firstBindingSet = tupleQueryResult.next();
//                    if (displayRowsFlag) {
//                        if (printedrow < rowsToDisplay) {
//                            bindingLine = "";
//                            for (String label : bindings) {
//                                bindingLine += (firstBindingSet.getValue(label) + "\t");
//                            }
//                            logger.info(bindingLine);
//                            printedrow++;
//                        }
//                    }
//                    results++;
//                } catch (Exception ex) {
//                    noOfScanErrors++;
//                    logger.error("[Query full scan phase]");
//                }
//            }
//            t3 = System.nanoTime();
//            logger.info("\t<-----------\n\n");
//            logger.info("Query evaluated with " + results + " results and " + noOfScanErrors + " scan errors!");
//            this.returnValue = new long[]{t2 - t1, t3 - t2, t3 - t1, results};
//        }
//    }
        private void runQueryPrintLimitedRows(int rowsToDisplay) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {
            long t1 = 0, t2 = 0, t3 = 0;
            long results = 0, noOfScanErrors = 0;
            int printedrow = 0;
            String bindingLine = "", labelsTitle = "\t";
            List<String> bindings = null;
            boolean displayRowsFlag = (rowsToDisplay != 0);

            logger.info("Evaluating query...");
            TupleQuery tupleQuery = graphDB.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);

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
    private GraphDB graphDB;
    private int displres;
    private BindingSet firstBindingSet;

    // --------------------- Constructors ----------------------------------
    public GraphDBSUT(String baseDir, String repositoryId, boolean createRepository, int displres) {
        this.baseDir = baseDir;
        this.repositoryId = repositoryId;
        this.createRepository = createRepository;
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
        return graphDB;
    }

    @Override
    public void initialize() {
        try {
            graphDB = new GraphDB(baseDir, repositoryId, createRepository);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize GraphDB(\"" + baseDir + "\", \"" + repositoryId + "\"");
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
        Executor runnable = new Executor(query, graphDB, timeoutSecs, this.displres);

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
            logger.info("Restarting GraphDB...");
            this.restart();
            logger.info("Closing GraphDB...");
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
            preparedUpdate = this.graphDB.getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
        } catch (RepositoryException e) {
            logger.error("[Strabon.update]", e);
        }
        logger.info("[Strabon.update] executing update query: " + query);

        try {
            preparedUpdate.execute();
        } catch (UpdateExecutionException e) {
            logger.error("[Strabon.update]", e);
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
            graphDB.close();
            graphDB = null;
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
            logger.info("Restarting GraphDB ...");

            if (graphDB != null) {
                try {
                    graphDB.close();
                } catch (Exception e) {
                    logger.error("Exception occured while restarting GraphDB. ");
                    logger.debug(e.toString());
                } finally {
                    graphDB = null;
                }
            }
            firstBindingSet = null;
            graphDB = new GraphDB(baseDir, repositoryId, createRepository);
            logger.info("GraphDB restarted");
        } catch (RuntimeException e) {
            logger.fatal("Cannot restart GraphDB");
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

        if (label.matches("Q14_Within_GeoNames_Point_Buffer")) {
            translatedQuery = translatedQuery.replaceAll("3000, <http://www.opengis.net/def/uom/OGC/1.0/metre>", "0.03");
        } else if (label.matches("Q4_Buffer_GeoNames")
                || label.matches("Q5_Buffer_LGD")) {
            translatedQuery = translatedQuery.replaceAll("4, <http://www.opengis.net/def/uom/OGC/1.0/metre>", "0.04");
        } else if (label.matches("Q6_Area_CLC")) {
            translatedQuery = translatedQuery.replaceAll("strdf:area", "ext:area");
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
            // we have to divide by 100000 to convert it to degrees, because this
            // the analogy of Km/degree around the average latitude 45 and GraphDB
            // uses geof:buffer(geom, TOL) where TOL is expressed in the same units
            // as geom, which for the Synthetic_POIs dataset is in EPSG 4326, that 
            // is degrees!! The SyntheticGenerator especially for POIs returns
            // radious in Km, that is the cRadius is already divide by 1000
            cRadious = cRadious / 100;
            // 5. create the new filter using the desired format
            String newFilter = String.format("FILTER(geof:sfWithin(%s, geof:buffer(\"POINT(45 45)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, %d))).\n}\n", cGeom, cRadious);
            // 6. replace old with new filter
            translatedQuery = translatedQuery.substring(0, translatedQuery.indexOf("FILTER")) + newFilter;

        }

        return translatedQuery;
    }
}
