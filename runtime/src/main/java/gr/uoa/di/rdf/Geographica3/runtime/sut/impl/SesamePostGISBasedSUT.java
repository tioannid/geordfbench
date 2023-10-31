package gr.uoa.di.rdf.Geographica3.runtime.sut.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.Geographica3.runtime.sut.ISUT;
import gr.uoa.di.rdf.Geographica3.runtime.sys.executor.SesamePostGISBasedExecutor;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.IGeographicaSystem;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.SesamePostGISBasedGeographicaSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 14/02/2023
 */
public abstract class SesamePostGISBasedSUT implements ISUT<SesamePostGISBasedGeographicaSystem> {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(SesamePostGISBasedSUT.class.getSimpleName());
    public final static Properties Properties;
    final private static String PROPERTIES_FILE_NAME = "postgresql.properties";
    static String SYSCMD_POSTGRES_STOP;
    static String SYSCMD_POSTGRES_START;
    static String SYSCMD_POSTGRES_RESTART;
    static String SYSCMD_POSTGRES_STATUS;

    static {
        InputStream is = SesamePostGISBasedSUT.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
        logger.info("Reading " + SesamePostGISBasedSUT.class.getSimpleName() + " properties from file : " + SesamePostGISBasedSUT.class.getResource("/" + PROPERTIES_FILE_NAME));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        // load the properties
        Properties = new Properties();
        try {
            Properties.load(in);
        } catch (IOException ex) {
            logger.fatal("Exception during static initialization of " + SesamePostGISBasedSUT.class.getSimpleName());
        }
        // read all values
        SYSCMD_POSTGRES_STOP = Properties.getProperty("POSTGRES_STOP");
        SYSCMD_POSTGRES_START = Properties.getProperty("POSTGRES_START");
        SYSCMD_POSTGRES_RESTART = Properties.getProperty("POSTGRES_RESTART");
        SYSCMD_POSTGRES_STATUS = Properties.getProperty("POSTGRES_STATUS");
    }

    // --- Data members ------------------------------
    protected IHost host;
    protected Map<String, String> sysProperties;
    protected SesamePostGISBasedGeographicaSystem _sys;
    protected IReportSpec reportSpec;
    protected IExecutionSpec execSpec;
    protected Map<String, String> firstBindingSetValueMap;
    protected boolean postgreSQLInitiallyRunning;
    protected int noRecsToPause = -1; // number of scanned records after which executor thread pauses to receive main thread's interrupts
    protected int noMsecsToPause = 100; // number of msecs to pause the worker thread

    // --- Constructors ------------------------------
    public SesamePostGISBasedSUT(IHost host, Map<String, String> sysProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        this.host = host;
        if (sysProperties == null) {
            System.out.println("ALERT! sysProperties is NULL!");
        }
        this.sysProperties = sysProperties;
        this.reportSpec = reportSpec;
        this.execSpec = execSpec;
        this.firstBindingSetValueMap = new HashMap<>();
        this.postgreSQLInitiallyRunning = chechPostgreSQLStatus();
    }

    // --- Data Accessors -----------------------------------
    @Override
    public IHost getHost() {
        return host;
    }

    @Override
    public IReportSpec getReportSpec() {
        return reportSpec;
    }

    @Override
    public IExecutionSpec getExecSpec() {
        return execSpec;
    }

    @Override
    public int getNoRecsToPause() {
        return noRecsToPause;
    }

    @Override
    public void setNoRecsToPause(int noRecsToPause) {
        this.noRecsToPause = noRecsToPause;
    }

    @Override
    public int getNoMsecsToPause() {
        return noMsecsToPause;
    }

    @Override
    public void setNoMsecsToPause(int noMsecsToPause) {
        this.noMsecsToPause = noMsecsToPause;
    }

    
    // --- Methods -----------------------------------
    /**
     * Return the value of the binding with {@link bindingName} from the first
     * BindingSet.
     *
     * @return a String representing the value for the binding
     */
    @Override
    public Map<String, String> getFirstBindingSetValueMap() {
        return firstBindingSetValueMap;
    }

    @Override
    public SesamePostGISBasedGeographicaSystem getSystem() {
        return _sys;
    }

    @Override
    public void initialize() throws Exception {
        try {
            logger.info("Initializing..");
            // create an SesamePostGISBasedGeographicaSystem instance with default constructor
            this._sys = new SesamePostGISBasedGeographicaSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize " + this.getClass().getSimpleName());
            logger.fatal(e.toString());
        }
    }

    @Override
    // Using Runnable interface and explicit Thread management
    public QueryRepResult runTimedQueryByExecutor(String query, long timeoutSecs) throws Exception {
        QueryRepResult qryRepResult = new QueryRepResult(QueryRepResult.DEFAULT);
        //maintains a thread for executing the doWork method
        logger.info("Starting QueryExecutor thread");
        long patience = timeoutSecs * (long) Math.pow(10, 9);
//        int noRecsToPause = -1; // number of scanned records after which executor thread pauses to receive main thread's interrupts
//        int noMsecsToPause = 100; // number of msecs to pause the worker thread
        SesamePostGISBasedExecutor executor = new SesamePostGISBasedExecutor(query, _sys,
                this.reportSpec, qryRepResult, noRecsToPause, noMsecsToPause);
        executor.setMaxQueryExecTime((int)timeoutSecs); // explicit conversion is ok!
        Thread qet = new Thread(executor);
        long startTime = System.nanoTime();
        if (this._sys == null) {
            logger.error("The _sys object is null. Cannot start executor!");
            throw new Exception("[The _sys object is null. Cannot start executor!");
        }
        qet.start();
        logger.info("Waiting for QueryExecutor thread to finish");
//        boolean isFutureDone = true;
        long timeoutProgressDuration; // nsecs
        long timeoutProgressStep = timeoutSecs * 1000 / 4; // msecs
        logger.info("Timeout progress step is " + timeoutProgressStep + " msecs");
        float timeoutProgressPercentage;
        while (qet.isAlive()) {
            // Wait maximum of 1 second
            // for MessageLoop thread
            // to finish.
            qet.join(timeoutProgressStep);
            timeoutProgressDuration = System.nanoTime() - startTime;
            timeoutProgressPercentage = timeoutProgressDuration * 100 / patience;
            if ((timeoutProgressDuration > patience)) {
                if (qet.isAlive()) {
                    qet.interrupt();
                    logger.info("Timeout expired! Sent interrupt to worker thread and waiting for it to join.");
                    // Shouldn't be long now
                    // -- wait indefinitely
                    qet.join();
                }
            } else {
                logger.info("Percentage of expired timeout is " + timeoutProgressPercentage + " %");
            }
        }
        this.firstBindingSetValueMap = executor.getFirstBindingSetValueMap();
        System.gc();
        return qryRepResult;
    }

    @Override
    public long[] runUpdate(String query) {

        logger.info("Executing update...");
        long t1 = System.nanoTime();

//        Update preparedUpdate = null;
//        try {
//            preparedUpdate = this._sys.getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
//        } catch (RepositoryException e) {
//            logger.error("[RDF4J.update]", e);
//        }
//        logger.info("[RDF4J.update] executing update query: " + query);
//
//        try {
//            preparedUpdate.execute();
//        } catch (UpdateExecutionException e) {
//            logger.error("[RDF4J.update]", e);
//        }
//
        long t2 = System.nanoTime();
//        logger.info("Update executed");

        long[] ret = {-1, -1, t2 - t1, -1};
        return ret;
    }

    @Override
    public void close() {
        logger.info("Closing..");
        try {
            if (_sys != null) {
                _sys.close();
                _sys = null;

            } else {
                logger.info("There is no instance of " + this.getClass().getSimpleName());
            }
            this.firstBindingSetValueMap = null;
        } catch (Exception e) {
            logger.error("Could not close " + this.getClass().getSimpleName());
            e.printStackTrace();
        }
        System.gc();
        try { // put main thread to sleep in order for jvm to garbage collect
            Thread.sleep(this.execSpec.getClearCacheDelaymSecs()); // TODO use different parameter for Java GC
        } catch (InterruptedException e) {
            logger.fatal("Fatal error during system GC and main thread sleep");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        logger.info("Closed (caches not cleared)");
        // if necessary restore PostgreSQL status
        if (this.postgreSQLInitiallyRunning) {
            String[] start_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_START};
            Process pr;
            // start PostgreSQL
            try {
                logger.info("Restoring PostgreSQL to its original running state...");
                pr = Runtime.getRuntime().exec(start_postgres);
                pr.waitFor();
                if (pr.exitValue() != 0) {
                    logger.error("Something went wrong while starting PostgreSQL");
                    logger.error("... with command " + Arrays.toString(start_postgres));
                }
            } catch (Exception e) {
                logger.error("Cannot start PostgreSQL");
            }
        }
    }

    
    @Override
    public void clearCaches() {
        String[] stop_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_STOP};
        String[] start_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_START};
        Process pr;

        // stop PostgreSQL
        try {
            logger.info("Stopping PostgreSQL...");
            pr = Runtime.getRuntime().exec(stop_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stopping PostgreSQL");
                logger.error("... with command " + Arrays.toString(stop_postgres));
            }
        } catch (Exception e) {
            logger.fatal("Cannot stop PostgreSQL");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        // system clear caches
        host.getOs().clearCaches(this.execSpec.getClearCacheDelaymSecs());
        // start PostgreSQL
        try {
            logger.info("Starting PostgreSQL...");
            pr = Runtime.getRuntime().exec(start_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting PostgreSQL");
                logger.error("... with command " + Arrays.toString(start_postgres));
            }
        } catch (Exception e) {
            logger.fatal("Cannot start PostgreSQL");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    @Override
    public void restart() {
        String[] restart_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_RESTART};
        Process pr;

        // restart PostgreSQL
        try {
            logger.info("Restarting PostgreSQL...");
            pr = Runtime.getRuntime().exec(restart_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while restarting PostgreSQL");
                logger.error("... with command " + Arrays.toString(restart_postgres));
            }
        } catch (Exception e) {
            logger.fatal("Cannot stop PostgreSQL");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        try {
            // put main thread to sleep in order for jvm to garbage collect
            Thread.sleep(this.execSpec.getClearCacheDelaymSecs());
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SesamePostGISBasedSUT.class.getName()).log(Level.SEVERE, null, ex);
        }

        // restarting _sys
        try {
            // close _sys
            if (_sys != null) {
                try {
                    _sys.close();
                } catch (Exception e) {
                    logger.error("Exception occured while closing " + _sys.getClass().getSimpleName());
                    logger.debug(e.toString());
                } finally {
                    _sys = null;
                }
            }
            // reset important properties
            this.firstBindingSetValueMap = null;
            // re-initialize SUT
            this.initialize();
            logger.info(this.getClass().getSimpleName() + " restarted");
        } catch (RuntimeException e) {
            logger.error("Cannot restart " + this.getClass().getSimpleName());
        } catch (Exception ex) { // internal system of SUT not initialized
            logger.error(ex.getMessage());
        }
    }

    // Bypass translation for generic SesamePostGISBasedSUT
    @Override
    public String translateQuery(String query, String label) {
        return query;
    }

    @Override
    public IGeographicaSystem getGeographicaSystem() {
        return this._sys;
    }

    private boolean chechPostgreSQLStatus() {
        String[] status_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_STATUS};
        ProcessBuilder ps;
        Process pr;
        BufferedReader in;
        String line;
        boolean isRunning = true;

        // chech PostgreSQL status
        try {
            logger.info("Checking if PostgreSQL is running...");
            ps = new ProcessBuilder(status_postgres);
            ps.redirectErrorStream(true);
            pr = ps.start();
            in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            if ((line = in.readLine()) != null) {
                isRunning = false;
                logger.info("PostgreSQL is not currently running");
            } else {
                logger.info("PostgreSQL is currently running");
            }
            pr.waitFor();
            in.close();
        } catch (Exception e) {
            logger.fatal("Cannot check PostgreSQL status");
        }
        return isRunning;
    }

    /**
     * By default SesamePostGIS based SUT do not use a server-service component, 
     * therefore the default functionality is left empty. The PostGIS DBMS server
     * is not considered as the application server service.
     */    
    @Override
    public void startServer() {
    }

    /**
     * By default SesamePostGIS based SUT do not use a server-service component, 
     * therefore the default functionality is left empty. The PostGIS DBMS server
     * is not considered as the application server service.
     */    
    @Override
    public void stopServer() {
    }

}
