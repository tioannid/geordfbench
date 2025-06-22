/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.runtime.sys.executor;

import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.RDF4JBasedGeographicaSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryEvaluationFlag;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.ResultException;
import java.util.concurrent.ExecutionException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.UnsupportedQueryLanguageException;
import org.eclipse.rdf4j.repository.RepositoryException;

public class RDF4JbasedExecutor extends AbstractExecutor<RDF4JBasedGeographicaSystem> implements Runnable {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(RDF4JbasedExecutor.class.getSimpleName());

    // --- Data members ------------------------------
    protected BindingSet firstBindingSet; // capability to record the first binding set
    protected boolean isTimedout;
    protected boolean isOpNotsupported;
    protected int noRecsToPause = 100; // number of scanned records after which executor thread pauses to receive main thread's interrupts
    protected int noMsecsToPause = 100; // number of msecs to pause the worker thread

    // --- Constructors ------------------------------
    public RDF4JbasedExecutor(String query, RDF4JBasedGeographicaSystem geoSys,
            IReportSpec reportSpec, QueryRepResult queryRepResult) {
        super(query, geoSys, reportSpec, queryRepResult);
        this.firstBindingSet = null;
        isTimedout = false;
        isOpNotsupported = false;
    }

    public RDF4JbasedExecutor(String query, RDF4JBasedGeographicaSystem geoSys,
            IReportSpec reportSpec, QueryRepResult queryRepResult,
            int noRecsToPause, int noMsecsToPause) {
        super(query, geoSys, reportSpec, queryRepResult);
        this.firstBindingSet = null;
        isTimedout = false;
        isOpNotsupported = false;
        this.noRecsToPause = noRecsToPause;
        this.noMsecsToPause = noMsecsToPause;
    }

    public boolean isIsTimedout() {
        return isTimedout;
    }

    public void setIsTimedout(boolean isTimedout) {
        this.isTimedout = isTimedout;
    }

    public boolean isIsOpNotsupported() {
        return isOpNotsupported;
    }

    public void setIsOpNotsupported(boolean isOpNotsupported) {
        this.isOpNotsupported = isOpNotsupported;
    }

    public int getNoRecsToPause() {
        return noRecsToPause;
    }

    public void setNoRecsToPause(int noRecsToPause) {
        this.noRecsToPause = noRecsToPause;
    }

    public int getNoMsecsToPause() {
        return noMsecsToPause;
    }

    public void setNoMsecsToPause(int noMsecsToPause) {
        this.noMsecsToPause = noMsecsToPause;
    }

    // --- Methods -----------------------------------
//    @Override
    public QueryRepResult call_new() {
        long t1, t2;
        int printedrow = 0;
        String bindingLine, labelsTitle = "\t";
        List<String> bindingNames = null;
        int rowsToDisplay = reportSpec.getNoQueryResultToReport();
        boolean displayRowsFlag = (rowsToDisplay != 0);
        qryRepResult.setEvalFlag(QueryEvaluationFlag.STARTED);

        logger.info("Evaluating query...");
        TupleQuery tupleQuery = null;
        TupleQueryResult tupleQueryResult = null;
        try {
            tupleQuery = geoSys.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
            // Evaluate and time the evaluation of the prepared query
            t1 = System.nanoTime();
            // if there is an exception during evaluation throw it and return
            tupleQueryResult = tupleQuery.evaluate();
            if (isOpNotsupported || isTimedout) {
                return qryRepResult;
            }
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
            // record execution/evaluation time
            qryRepResult.setEvalTime(System.nanoTime() - t1);
            // if there is a valid request for rows to display, first display headers
            if (displayRowsFlag) {
                // process noResults
                bindingNames = tupleQueryResult.getBindingNames();
                for (String label : bindingNames) {
                    labelsTitle += (label + "\t\t");
                }
                logger.info(labelsTitle);
                logger.info("------------------------------------>");
            }
            // scan phase
            t2 = System.nanoTime();
            // get first binding set
            if (tupleQueryResult.hasNext()) {
                try {
                    firstBindingSet = tupleQueryResult.next();
                } catch (RuntimeException ex) { // probably operator not supported
                    qryRepResult.incNoScanErrors();
                    throw new RuntimeException("Probably, operator not supported");
                } catch (Exception e) { // probably invalid geometry
                    qryRepResult.incNoScanErrors();
                    throw new Exception("Probably, invalid geometry]");
                }
                if (printedrow < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (firstBindingSet.getValue(label) + "\t");
                    }
                    logger.info(bindingLine);
                    printedrow++;
                }
                // incement the number of results scanned/retrieved
                qryRepResult.incNoResults();
            }
            qryRepResult.setScanTime(System.nanoTime() - t2);
            // iterate from 2nd binding set onwards
            BindingSet bindingSet = null;
            bindingLine = "";
            while (tupleQueryResult.hasNext() && !(isOpNotsupported || isTimedout)) {
                try {
                    bindingSet = tupleQueryResult.next();
                } catch (RuntimeException ex) { // probably operator not supported
                    qryRepResult.incNoScanErrors();
                    logger.error(ex.getMessage());
                    break;
                } catch (Exception e) { // probably invalid geometry
                    qryRepResult.incNoScanErrors();
                    logger.error(e.getMessage());
                }
                try {
                    if (printedrow < rowsToDisplay) {
                        bindingLine = "";
                        for (String label : bindingNames) {
                            bindingLine += (bindingSet.getValue(label) + "\t");
                        }
                        logger.info(bindingLine);
                        printedrow++;
                    }
                    // incement the number of results scanned/retrieved
                    qryRepResult.incNoResults();
                } catch (Exception e) {
                    //logger.info("DBG#2.5.1 - Scanning Phase Print block Exception");
                }
            }
            qryRepResult.setScanTime(System.nanoTime() - t2); // record scanning time
            if (isOpNotsupported || isTimedout) {
                return qryRepResult;
            }
            qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNED);

            if (displayRowsFlag) {
                logger.info("<------------------------------------");
            }
            qryRepResult.setEvalFlag(QueryEvaluationFlag.COMPLETED);
            // logger.info("Query evaluated: " + qryRepResult.toString());

        } catch (MalformedQueryException e) {
            logger.info("Query could not be processed by the query parser, typically due to syntax errors");
        } catch (UnsupportedQueryLanguageException e) {
            logger.info("A specific query language is not supported. A typical cause of this exception is that the class library for the specified query language is not present in the classpath");
        } catch (RepositoryException e) {
            logger.info("Repository API indicates an error. Most of the time, this exception will wrap another exception that indicates the actual source of the error.");
        } catch (QueryEvaluationException ex) {
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            throw new QueryEvaluationException("[Query evaluation phase]", ex);
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        } catch (RuntimeException e) { // probably operator not supported
            logger.error(e.getMessage());
        } catch (Exception e) { // probably invalid geometry
            logger.error(e.getMessage());
        } finally {
            // return this.qryRepResult;
        }
        return this.qryRepResult;
    }

    @Override
    public QueryRepResult call() throws MalformedQueryException,
            UnsupportedQueryLanguageException, RepositoryException,
            Exception {
        long t1, t2;
        int printedrow = 0;
        String bindingLine, labelsTitle = "\t";
        List<String> bindingNames = null;
        int rowsToDisplay = reportSpec.getNoQueryResultToReport();
        boolean displayRowsFlag = (rowsToDisplay != 0);
        qryRepResult.setEvalFlag(QueryEvaluationFlag.STARTED);

        // Prepared the tuple query and handle any errors
        logger.info("Preparing query...");
        TupleQuery tupleQuery = null;
        String errorMsg = "";
        try {
            tupleQuery = geoSys.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
        } catch (MalformedQueryException e) {
            // Query could not be processed by the query parser, typically due to syntax errors
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            throw new MalformedQueryException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        } catch (UnsupportedQueryLanguageException e) {
            // A specific query language is not supported. A typical cause of this exception is that the class library for the specified query language is not present in the classpath
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            throw new UnsupportedQueryLanguageException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        } catch (RepositoryException e) {
            // Repository API indicates an error. Most of the time, this exception will wrap another exception that indicates the actual source of the error
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            throw new RepositoryException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        }

        // Evaluate and time the evaluation of the prepared query
        logger.info("Evaluating query...");
        TupleQueryResult tupleQueryResult = null;
        t1 = System.nanoTime();
        // if there is an exception during evaluation throw it and return
        try {
            tupleQueryResult = tupleQuery.evaluate();
        } catch (QueryEvaluationException e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            throw new QueryEvaluationException("[Query evaluation phase: " + errorMsg + " ]", e);
        } catch (RuntimeException e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            throw new RuntimeException("[Query evaluation phase: " + errorMsg + " ]", e);
        } catch (Exception e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            throw new Exception("[Query evaluation phase: " + errorMsg + " ]", e);
        }

        if (isOpNotsupported || isTimedout) {
            return qryRepResult;
        }
        qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
        // record execution/evaluation time
        qryRepResult.setEvalTime(System.nanoTime() - t1);

        // if there is a valid request for rows to display, first display headers
        if (displayRowsFlag) {
            // process noResults
            bindingNames = tupleQueryResult.getBindingNames();
            for (String label : bindingNames) {
                labelsTitle += (label + "\t\t");
            }
            logger.info(labelsTitle);
            logger.info("------------------------------------>");
        }
        // scan phase
        t2 = System.nanoTime();
        // get first binding set
        try {
            if (tupleQueryResult.hasNext()) {
                firstBindingSet = tupleQueryResult.next();
                if (printedrow < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (firstBindingSet.getValue(label) + "\t");
                    }
                    logger.info(bindingLine);
                    printedrow++;
                }
                // incement the number of results scanned/retrieved
                qryRepResult.incNoResults();
            }
        } catch (RuntimeException ex) { // probably operator not supported
            qryRepResult.incNoScanErrors();
            throw new RuntimeException("Probably, operator not supported");
        } catch (Exception e) { // probably invalid geometry
            qryRepResult.incNoScanErrors();
            throw new Exception("Probably, invalid geometry]");
        }
        qryRepResult.setScanTime(System.nanoTime() - t2);

        // iterate from 2nd binding set onwards
        BindingSet bindingSet = null;
        bindingLine = "";
        while (tupleQueryResult.hasNext() && !(isOpNotsupported || isTimedout)) {
            try {
                bindingSet = tupleQueryResult.next();
            } catch (RuntimeException ex) { // probably operator not supported
                qryRepResult.incNoScanErrors();
                logger.error(ex.getMessage());
                break;
            } catch (Exception e) { // probably invalid geometry
                qryRepResult.incNoScanErrors();
                logger.error(e.getMessage());
            }
            try {
                if (printedrow < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (bindingSet.getValue(label) + "\t");
                    }
                    logger.info(bindingLine);
                    printedrow++;
                }
                // incement the number of results scanned/retrieved
                qryRepResult.incNoResults();
            } catch (Exception e) {
                //logger.info("DBG#2.5.1 - Scanning Phase Print block Exception");
            }
        }
        tupleQueryResult.close();
        qryRepResult.setScanTime(System.nanoTime() - t2); // record scanning time
        if (isOpNotsupported || isTimedout) {
            return qryRepResult;
        }
        qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNED);

        if (displayRowsFlag) {
            logger.info("<------------------------------------");
        }
        qryRepResult.setEvalFlag(QueryEvaluationFlag.COMPLETED);
        // logger.info("Query evaluated: " + qryRepResult.toString());
        return this.qryRepResult;
    }

    @Override
    public Map<String, String> getFirstBindingSetValueMap() {
        Map<String, String> valueMap = null;
        if (this.firstBindingSet != null) {
            valueMap = new HashMap<>();
            for (String bindingName : this.firstBindingSet.getBindingNames()) {
                valueMap.put(bindingName, this.firstBindingSet.getBinding(bindingName).getValue().stringValue());
            }
        }
        return valueMap;
    }

    @Override
    public void run() {
        long tStart = System.currentTimeMillis();
        qryRepResult.setEvalFlag(QueryEvaluationFlag.STARTED);
        boolean willPause = (noRecsToPause > 0);
        if (willPause) {
            logger.info("Will pause every " + noRecsToPause + " records to receive main thread interrupts");
        }
        int cntPauses = 0;
        long t1, t2, t3, t4;
        String bindingLine, labelsTitle = "\t";
        List<String> bindingNames = null;
        int rowsToDisplay = reportSpec.getNoQueryResultToReport(); // rows to display
        boolean displayRowsFlag = (rowsToDisplay != 0);
        int rowsDisplayed = 0; // counter of displayed rows, <= rowsToDisplay

        // ----- Prepare the tuple query and handle any errors -----
        // logger.info("Preparing query...");
        TupleQuery tupleQuery = null;
        String errorMsg = "";
        qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARING);
        try {
            tupleQuery = geoSys.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
            int remainingExecutionTimeInSecs = (int) ((this.getMaxQueryExecTime() * 1000 - (System.currentTimeMillis() - tStart)) / 1000);
            tupleQuery.setMaxExecutionTime(remainingExecutionTimeInSecs);
            logger.debug("remainingExecutionTimeInSecs = " + remainingExecutionTimeInSecs);
        } catch (MalformedQueryException e) {
            // Query could not be processed by the query parser, typically due to syntax errors
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            qryRepResult.setResException(ResultException.SYNTAX_ERROR);
            throw new MalformedQueryException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        } catch (UnsupportedQueryLanguageException e) {
            // A specific query language is not supported. A typical cause of this exception is that the class library for the specified query language is not present in the classpath
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            qryRepResult.setResException(ResultException.UNSUPPORTED_QUERYLANGUAGE);
            throw new UnsupportedQueryLanguageException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        } catch (RepositoryException e) {
            // Repository API indicates an error. Most of the time, this exception will wrap another exception that indicates the actual source of the error
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARETUPLE_ERROR);
            qryRepResult.setResException(ResultException.REPOSITORY_API_ERROR);
            throw new RepositoryException("[Tuple query preparation phase: " + errorMsg + " ]", e);
        }

        // Evaluate and time the evaluation of the prepared query
        // logger.info("Evaluating query...");
        TupleQueryResult tupleQueryResult = null;
        t1 = System.nanoTime();
        qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATING);
        // if there is an exception during evaluation throw it and return
        try {
            tupleQueryResult = tupleQuery.evaluate();
        } catch (QueryEvaluationException e) {
            if (e instanceof QueryInterruptedException) { // evaluation timed out
                errorMsg = "Evaluation timed-out";
                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_TIMEDOUT);
                qryRepResult.setResException(ResultException.QUERY_EVALUATION_FAILED);
                qryRepResult.setEvalTime(System.nanoTime() - t1);
            } else { // HTTPQueryEvaluationException or ValueExprEvaluationException
                errorMsg = e.getMessage();
                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
                qryRepResult.setResException(ResultException.QUERY_EVALUATION_FAILED);
            }
            logger.error(errorMsg);
            if (tupleQueryResult != null) {
                tupleQueryResult.close();
            }
            if (e instanceof QueryInterruptedException) {
                throw new QueryEvaluationException("[Query evaluation phase: " + errorMsg + " ]");
            } else {
                throw new QueryEvaluationException("[Query evaluation phase: " + errorMsg + " ]", e);
            }
        } catch (RuntimeException e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            qryRepResult.setResException(ResultException.QUERY_EVALUATION_RUNTIME_ERROR);
            tupleQueryResult.close();
            throw new RuntimeException("[Query evaluation phase: " + errorMsg + " ]", e);
        } catch (Exception e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg);
            qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);
            qryRepResult.setResException(ResultException.QUERY_EVALUATION_UNKNOWN_ERROR);
            tupleQueryResult.close();
            try {
                throw new Exception("[Query evaluation phase: " + errorMsg + " ]", e);
            } catch (Exception ex) {
            }
        }
        qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
        // record execution/evaluation time
        t2 = System.nanoTime();
        qryRepResult.setEvalTime(t2 - t1);

        // if there is a valid request for rows to display, first display headers
        if (displayRowsFlag) {
            // process noResults
            bindingNames = tupleQueryResult.getBindingNames();
            for (String label : bindingNames) {
                labelsTitle += (label + "\t\t");
            }
            logger.info(labelsTitle);
            logger.info("------------------------------------>");
        }
        if (willPause) {
            try {
                cntPauses++;
                logger.info("Pausing no-" + cntPauses + " for " + noMsecsToPause + "ms to accept main thread interrupt...");
                Thread.sleep(noMsecsToPause);
            } catch (InterruptedException ex) {
                qryRepResult.setResException(ResultException.TIMEDOUT);
                logger.error("I wasn't done! Query prepared and evaluated only. No scanning occured. Small patience window defined... :(");
                return;
            }
        }

        // scan phase
        qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNING);
        t3 = System.nanoTime();
        // get first binding set
        try {
            if (tupleQueryResult.hasNext()) {
                firstBindingSet = tupleQueryResult.next();
                if (rowsDisplayed < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (firstBindingSet.getValue(label) + "\t");
                    }
                    logger.info(bindingLine);
                    rowsDisplayed++;
                }
                // incement the number of results scanned/retrieved
                qryRepResult.incNoResults();
            }
        } catch (QueryEvaluationException e) { // probably operator not supported or timedout
            // as it seems that the scanning of the 1st record causes the
            // query evaluation, which seems to occur in a lazy fashion!
            if (Thread.currentThread().isInterrupted()) {
                qryRepResult.setResException(ResultException.TIMEDOUT);
                logger.error("QueryEvaluationException - I wasn't done! Query prepared and evaluated. Scanning phase-1st record in progress ..."
                        + qryRepResult.getNoResults()
                        + " results, so far. Not big enough patience window defined... :(");
                // record scan time
                qryRepResult.setScanTime(System.nanoTime() - t3);
                tupleQueryResult.close();
                return;
            } else {
                qryRepResult.incNoScanErrors();
                qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATION_ERROR);  // it is set to EVALUATION_ERROR because it seems to fire in a lazy manner
                qryRepResult.setResException(ResultException.UNSUPPORTED_OPERATOR);
                errorMsg = e.getMessage();
                // record scan time
                qryRepResult.setScanTime(System.nanoTime() - t3);
                logger.error("QueryEvaluationException - " + errorMsg);
                tupleQueryResult.close();
                throw new QueryEvaluationException("[Query scanning phase-1st record (lazy evaluation): " + errorMsg + " ]", e);
            }
        } catch (RuntimeException e) { // probably operator not supported or timedout
            if (Thread.currentThread().isInterrupted()) {
                qryRepResult.setResException(ResultException.TIMEDOUT);
                logger.error("RuntimeException - I wasn't done! Query prepared and evaluated. Scanning phase-1st record in progress ..."
                        + qryRepResult.getNoResults()
                        + " results, so far. Not big enough patience window defined... :(");
                // record scan time
                qryRepResult.setScanTime(System.nanoTime() - t3);
                tupleQueryResult.close();
                return;
            } else {
                qryRepResult.incNoScanErrors();
                qryRepResult.setResException(ResultException.UNSUPPORTED_OPERATOR);
                errorMsg = e.getMessage();
                logger.error("RuntimeException - " + errorMsg);
                // record scan time
                qryRepResult.setScanTime(System.nanoTime() - t3);
                tupleQueryResult.close();
                throw new RuntimeException("[Query scanning phase: " + errorMsg + " ]", e);
            }
        } catch (Exception e) { // probably invalid geometry
            qryRepResult.incNoScanErrors();
            qryRepResult.setResException(ResultException.INVALID_GEOMETRY);
            errorMsg = e.getMessage();
            logger.error("Exception - " + errorMsg);
            // record scan time
            qryRepResult.setScanTime(System.nanoTime() - t3);
            tupleQueryResult.close();
            try {
                throw new Exception("[Query scanning phase: " + errorMsg + " ]", e);
            } catch (Exception ex) {
            }
        }
        qryRepResult.setScanTime(System.nanoTime() - t3);

        // iterate from 2nd binding set onwards
        BindingSet bindingSet = null;
        bindingLine = "";
        // position scan time reference point
        t4 = System.nanoTime();
        try {
            while (tupleQueryResult.hasNext()) {
                bindingSet = tupleQueryResult.next();
                if (rowsDisplayed < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (bindingSet.getValue(label) + "\t");
                    }
                    logger.info(bindingLine);
                    rowsDisplayed++;
                }
                qryRepResult.incNoResults();

                if (Thread.currentThread().isInterrupted()) {
                    qryRepResult.setResException(ResultException.TIMEDOUT);
                    qryRepResult.addToScanTime(System.nanoTime() - t4);
                    logger.error("Caught the interruption myself! I wasn't done! Query prepared and evaluated. Scanning in progress ..."
                            + qryRepResult.getNoResults()
                            + " results, so far. Not big enough patience window defined... :(");
                    try {
                        tupleQueryResult.close();
                    } catch (Exception ex) {
                        logger.error("Exception - tupleQueryResult.close() ERROR - " + ex.getMessage());
                    }
                    return;
                }

                // every noRecsToPause scanned results pause thread to receive interrupt 
                // request from main thread
                if (willPause && (qryRepResult.getNoResults() % noRecsToPause == 0)) {
                    // update scan time before pausing
                    qryRepResult.addToScanTime(System.nanoTime() - t4);
                    cntPauses++;
                    logger.info("Pausing no-" + cntPauses + " for " + noMsecsToPause + "ms to accept main thread interrupt...");
                    Thread.sleep(noMsecsToPause);
                    logger.info("Continuing my work...");
                    // reposition scan time reference point
                    t4 = System.nanoTime();
                }
            }
        } catch (InterruptedException e) { // Timeout occurred during current thread's sleep
            qryRepResult.setResException(ResultException.TIMEDOUT);
            // update scan time before pausing
            qryRepResult.addToScanTime(System.nanoTime() - t4);
            logger.error("InterruptedException - I wasn't done, but sleeping a bit! Query prepared and evaluated. Scanning in progress ..."
                    + qryRepResult.getNoResults()
                    + " results, so far. Not big enough patience window defined... :(");
            tupleQueryResult.close();
            return;
        } catch (RuntimeException e) { // Timeout occurred during scanning but not during current thread's sleep
            if (Thread.currentThread().isInterrupted()) {
                qryRepResult.setResException(ResultException.TIMEDOUT);
                // update scan time before pausing
                qryRepResult.addToScanTime(System.nanoTime() - t4);
                logger.error("RuntimeException - I wasn't done! Query prepared and evaluated. Scanning in progress ..."
                        + qryRepResult.getNoResults()
                        + " results, so far. Not big enough patience window defined... :(");
                try {
                    tupleQueryResult.close();
                } catch (Exception ex) {
                    logger.error("RuntimeException+Exception - tupleQueryResult.close() ERROR - " + ex.getMessage());
                }
                return;
            } else {
                if (e instanceof QueryInterruptedException) { // evaluation timed out
                    errorMsg = "Evaluation timed-out";
                    logger.error("QueryInterruptedException - " + errorMsg);
                    qryRepResult.setResException(ResultException.TIMEDOUT);
                    qryRepResult.addToScanTime(System.nanoTime() - t4);
                } else { // HTTPQueryEvaluationException or ValueExprEvaluationException
                    errorMsg = e.getMessage();
                    logger.error("RuntimeException - " + errorMsg);
                    qryRepResult.incNoScanErrors();
                    qryRepResult.setResException(ResultException.UNSUPPORTED_OPERATOR);
                }
                tupleQueryResult.close();
                throw new RuntimeException("[RuntimeException - Query scanning phase: " + errorMsg + " ]", e);
            }
        } catch (Exception e) { // probably invalid geometry
            qryRepResult.incNoScanErrors();
            qryRepResult.setResException(ResultException.INVALID_GEOMETRY);
            errorMsg = e.getMessage();
            logger.error("Exception - " + errorMsg);
            tupleQueryResult.close();
            try {
                throw new Exception("[Query scanning phase: " + errorMsg + " ]", e);
            } catch (Exception ex) {
            }
        } catch (Throwable t) {
            if (Thread.currentThread().isInterrupted()) {
                qryRepResult.setResException(ResultException.TIMEDOUT);
                logger.error("Throwable - I wasn't done! Query prepared and evaluated. Scanning in progress ..."
                        + qryRepResult.getNoResults()
                        + " results, so far. Not big enough patience window defined... :(");
                try {
                    tupleQueryResult.close();
                } catch (Exception ex) {
                    logger.error("Throwable+Exception - tupleQueryResult.close() THROWABLE - " + t.getMessage());
                }
                return;
            } else {
                qryRepResult.incNoScanErrors();
                qryRepResult.setResException(ResultException.QUERY_EVALUATION_RUNTIME_ERROR);
                errorMsg = "Throwable in a non interrupted thread";
                logger.error("Throwable - " + errorMsg);
                tupleQueryResult.close();
                throw new RuntimeException("[Throwable - Query scanning phase: " + errorMsg + " ]");
            }
        }
        qryRepResult.addToScanTime(System.nanoTime() - t4); // record scanning time
        if (Thread.currentThread().isInterrupted()) {
            qryRepResult.setResException(ResultException.TIMEDOUT);
            logger.error("Caught the interruption myself! - I was almost done, though! Query prepared, evaluated and scanned. No time to update status ..."
                    + qryRepResult.getNoResults()
                    + " results, so far. Not big enough patience window defined... :(");
            try {
                tupleQueryResult.close();
            } catch (Exception ex) {
                logger.error("Exception - tupleQueryResult.close() - " + ex.getMessage());
            }
            return;
        } else {
            qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNED);
            tupleQueryResult.close();

            if (displayRowsFlag) {
                logger.info("<------------------------------------");
            }
            qryRepResult.setEvalFlag(QueryEvaluationFlag.COMPLETED);
        }
    }

}
