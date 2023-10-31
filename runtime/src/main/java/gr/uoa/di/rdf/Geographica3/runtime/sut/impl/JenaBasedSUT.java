package gr.uoa.di.rdf.Geographica3.runtime.sut.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.Geographica3.runtime.hosts.IHost;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.Geographica3.runtime.sut.ISUT;
import gr.uoa.di.rdf.Geographica3.runtime.sys.executor.JenaBasedExecutor;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.IGeographicaSystem;
import gr.uoa.di.rdf.Geographica3.runtime.sys.interfaces.impl.JenaBasedGeographicaSystem;
import java.util.logging.Level;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 20/03/2023
 */
public abstract class JenaBasedSUT implements ISUT<JenaBasedGeographicaSystem> {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(JenaBasedSUT.class.getSimpleName());

    // --- Data members ------------------------------
    protected IHost host;
    protected Map<String, String> sysProperties;
    protected JenaBasedGeographicaSystem _sys;
    protected IReportSpec reportSpec;
    protected IExecutionSpec execSpec;
    protected Map<String, String> firstBindingSetValueMap;
    protected int noRecsToPause = -1; // number of scanned records after which executor thread pauses to receive main thread's interrupts
    protected int noMsecsToPause = 100; // number of msecs to pause the worker thread

    // --- Constructors ------------------------------
    public JenaBasedSUT(IHost host, Map<String, String> sysProperties,
            IReportSpec reportSpec, IExecutionSpec execSpec) {
        this.host = host;
        if (sysProperties == null) {
            System.out.println("ALERT! sysProperties is NULL!");
        }
        this.sysProperties = sysProperties;
        this.reportSpec = reportSpec;
        this.execSpec = execSpec;
        this.firstBindingSetValueMap = new HashMap<>();
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
    public JenaBasedGeographicaSystem getSystem() {
        return _sys;
    }

    @Override
    public void initialize() {
        try {
            logger.info("Initializing..");
            // create a JenaBasedGeographicaSystem instance with default constructor
            this._sys = new JenaBasedGeographicaSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize " + this.getClass().getSimpleName());
            logger.fatal(e.toString());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JenaBasedSUT.class.getName()).log(Level.SEVERE, null, ex);
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
//        logger.info("noRecsToPause = " + noRecsToPause + ", noMsecsToPause = " + noMsecsToPause);
        JenaBasedExecutor executor = new JenaBasedExecutor(query, _sys,
                this.reportSpec, qryRepResult, noRecsToPause, noMsecsToPause);
        executor.setMaxQueryExecTime((int) timeoutSecs); // explicit conversion is ok!
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
//
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
        logger.info("Update executed");

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
            logger.error("TODO - Handle this Exception!");
            e.printStackTrace();
        }
        System.gc();
        try {
            Thread.sleep(this.execSpec.getClearCacheDelaymSecs()); // TODO use different parameter for Java GC
        } catch (InterruptedException e) {
            logger.error("Cannot clear caches");
        }
        logger.info("Closed (caches not cleared)");
    }

    /**
     * Plain JenaBasedSUTs have only operating system (JVM) caches to clear
     */
    @Override
    public void clearCaches() {
        host.getOs().clearCaches(execSpec.getClearCacheDelaymSecs());
    }

    @Override
    public void restart() {
        Process pr;

        try {
            logger.info("Restarting..");

            // close _sys
            if (_sys != null) {
                try {
                    _sys.close();
                } catch (Exception e) {
                    logger.error("Exception occured while restarting "
                            + _sys.getName() + ".\n" + e.toString());
                } finally {
                    _sys = null;
                }
            }
            // reset important properties
            this.firstBindingSetValueMap = null;
            // this._sys.setInitialized(false);
            this.initialize();
            logger.info(_sys.getName() + " restarted");
        } catch (RuntimeException e) {
            logger.error("Cannot restart " + _sys.getName() + "\n" + e.getMessage());
        }
    }

    // Bypass translation for Jena based systems
    @Override
    public String translateQuery(String query, String label
    ) {
        return query;
    }

    @Override
    public IGeographicaSystem getGeographicaSystem() {
        return this._sys;
    }

    /**
     * By default Jena based SUT do not use an application server component, therefore the
     * default functionality is left empty.
     */
    @Override
    public void startServer() {
    }

    /**
     * By default Jena based SUT do not use a application server component, therefore the
     * default functionality is left empty.
     */
    @Override
    public void stopServer() {
    }

}
