package gr.uoa.di.rdf.geordfbench.runtime.sut.impl;

import gr.uoa.di.rdf.geordfbench.runtime.sys.executor.RDF4JbasedExecutor;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.ResultException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryEvaluationFlag;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.geordfbench.runtime.sut.ISUT;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.IGeographicaSystem;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.eclipse.rdf4j.query.QueryEvaluationException;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 09/12/2019
 */
public abstract class RDF4JBasedSUT implements ISUT<RDF4JBasedGeographicaSystem> {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(RDF4JBasedSUT.class.getSimpleName());

    // --- Data members ------------------------------
    protected IHost host;
    protected Map<String, String> sysProperties;
    protected RDF4JBasedGeographicaSystem _sys;
    protected IReportSpec reportSpec;
    protected IExecutionSpec execSpec;
    protected Map<String, String> firstBindingSetValueMap;
    protected int noRecsToPause = -1; // number of scanned records after which executor thread pauses to receive main thread's interrupts
    protected int noMsecsToPause = 100; // number of msecs to pause the worker thread

    // --- Constructors ------------------------------
    public RDF4JBasedSUT(IHost host, Map<String, String> sysProperties,
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
    public RDF4JBasedGeographicaSystem getSystem() {
        return _sys;
    }

    @Override
    public void initialize() {
        try {
            logger.info("Initializing..");
            // create an RDF4JBasedGeographicaSystem instance with default constructor
            this._sys = new RDF4JBasedGeographicaSystem(this.sysProperties);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize " + this.getClass().getSimpleName());
            logger.fatal(e.toString());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(RDF4JBasedSUT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Override
    // Using Callable interface and Futures for thread management
    public QueryRepResult runTimedQueryByExecutor_ORIGINAL(String query, long timeoutSecs) throws Exception {
        QueryRepResult qryRepResult = new QueryRepResult(QueryRepResult.DEFAULT);
        boolean isFutureDone = false;
        //maintains a thread for executing the doWork method
        final ExecutorService execService = Executors.newFixedThreadPool(1);
        //set the execService thread working
        RDF4JbasedExecutor executor = new RDF4JbasedExecutor(query, _sys,
                this.reportSpec, qryRepResult);

        final Future<QueryRepResult> future = execService.submit((Callable<QueryRepResult>) executor);

        //check the outcome of the execService thread and limit the time allowed for it to complete
        long tt1 = System.nanoTime();
        long tt2 = 0;
        try {
            //logger.info("Future started");
            /* Wait if necessary for at most <timeoutsSecs> for the computation 
            ** to complete, and then retrieves its result, if available */
            qryRepResult = future.get(timeoutSecs, TimeUnit.SECONDS);
            isFutureDone = true;
        } catch (InterruptedException e) { // current thread was interrupted while waiting
            future.cancel(true);
            logger.error(e.toString());
        } catch (ExecutionException e) {    // the computation threw an exception
            executor.setIsOpNotsupported(true);
            qryRepResult = executor.getQryRepResult();
            future.cancel(true);
            tt2 = System.nanoTime();
            logger.error(e.toString());
        } catch (TimeoutException e) {  // the wait timed out
            executor.setIsTimedout(true);
            qryRepResult = executor.getQryRepResult();
            future.cancel(true);
            tt2 = System.nanoTime();
            logger.info("Time out occurred!");
            this.restart();
            // this.close();
        } catch (QueryEvaluationException e) {
            logger.error(e.toString());
        } finally {
            //logger.info("Executor shutting down the execService...");
            execService.shutdown(); // Disable new tasks from being submitted
            try {
                //logger.info("Executor waiting for termination...");
                execService.awaitTermination(timeoutSecs, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error(e.toString());
            }
            System.gc();
        }
        if (isFutureDone) {
            this.firstBindingSetValueMap = executor.getFirstBindingSetValueMap();
            return qryRepResult;
        } else {
//            logger.info("Future did not terminate properly ("
//                    + String.valueOf((executor.isIsTimedout()) ? "timed out" : (executor.isIsOpNotsupported()) ? "unsupported operation" : "unknown") + ")"
//                    + "\nExecutor returned (at interrupt time) query repetition result: " + qryRepResult.toString()
//                    + "\nExecutor returned (NOW) query repetition result: " + executor.getQryRepResult().toString());
            if (executor.isIsOpNotsupported()) {
                //logger.error("Future not done AND OperationNotSupported!");
                qryRepResult.setNoResults(ResultException.UNSUPPORTED_OPERATOR.getResultException());
                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            } else if (executor.isIsTimedout()) {
                //logger.error("Future not done AND Timed out!");
                qryRepResult.setNoResults(ResultException.TIMEDOUT.getResultException());
                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
            }
//            logger.info("Modified query repetition result is: " + qryRepResult.toString());
            return qryRepResult;
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
        RDF4JbasedExecutor executor = new RDF4JbasedExecutor(query, _sys,
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
//        if (isFutureDone) {
//            this.firstBindingSetValueMap = executor.getFirstBindingSetValueMap();
////            logger.info("Future did not terminate properly ("
////                    + String.valueOf((executor.isIsTimedout()) ? "timed out" : (executor.isIsOpNotsupported()) ? "unsupported operation" : "unknown") + ")"
////                    + "\nExecutor returned (at interrupt time) query repetition result: " + qryRepResult.toString()
////                    + "\nExecutor returned (NOW) query repetition result: " + executor.getQryRepResult().toString());
//            if (executor.isIsOpNotsupported()) {
//                //logger.error("Future not done AND OperationNotSupported!");
//                qryRepResult.setNoResults(ResultException.UNSUPPORTED_OPERATOR.getResultException());
//                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
//            } else if (executor.isIsTimedout()) {
//                //logger.error("Future not done AND Timed out!");
//                qryRepResult.setNoResults(ResultException.TIMEDOUT.getResultException());
//                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
//            }
////            logger.info("Modified query repetition result is: " + qryRepResult.toString());
//        }
        return qryRepResult;
    }

    @Override
    public long[] runUpdate(String query
    ) {

        logger.info("Executing update...");
        long t1 = System.nanoTime();

        Update preparedUpdate = null;
        try {
            preparedUpdate = this._sys.getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
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
        // TODO - Να ελέγξω ποιός και τί ήθελε να κάνει!
        // Runtime run = Runtime.getRuntime();
        // Process pr = run.exec(restart_script);
        // pr.waitFor();
        //      
        System.gc();
        try {
            Thread.sleep(this.execSpec.getClearCacheDelaymSecs()); // TODO use different parameter for Java GC
        } catch (InterruptedException e) {
            logger.fatal("Cannot clear caches");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        logger.info("Closed (caches not cleared)");
    }

    /**
     * Plain RDF4JBasedSUTs have only operating system (JVM) caches to clear
     */
    @Override
    public void clearCaches() {
        host.getOs().clearCaches(this.execSpec.getClearCacheDelaymSecs());
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
                    logger.error("Exception occured while restarting RDF4J. ");
                    logger.debug(e.toString());
                } finally {
                    _sys = null;
                }
            }
            // reset important properties
            this.firstBindingSetValueMap = null;
            // this._sys.setInitialized(false);
            this.initialize();
            logger.info("RDF4J restarted");
        } catch (RuntimeException e) {
            logger.fatal("Cannot restart RDF4J");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    // Bypass translation for RDF4J
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
     * By default RDF4J based SUT do not use a server component, therefore the
     * default functionality is left empty.
     */
    @Override
    public void startServer() {
    }

    /**
     * By default RDF4J based SUT do not use a server component, therefore the
     * default functionality is left empty.
     */
    @Override
    public void stopServer() {
    }

}
