/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.geordfbench.runtime.sys.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryEvaluationFlag;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.ResultException;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.impl.JenaBasedGeographicaSystem;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;

public class JenaBasedExecutor extends AbstractExecutor<JenaBasedGeographicaSystem> implements Runnable {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(JenaBasedExecutor.class.getSimpleName());
    }

    // --- Data members ------------------------------
    protected QuerySolution firstBindingSet; // capability to record the first binding set
    protected boolean isTimedout;
    protected boolean isOpNotsupported;
    protected int noRecsToPause = 100; // number of scanned records after which executor thread pauses to receive main thread's interrupts
    protected int noMsecsToPause = 100; // number of msecs to pause the worker thread

    // --- Constructors ------------------------------
    public JenaBasedExecutor(String query, JenaBasedGeographicaSystem geoSys,
            IReportSpec reportSpec, QueryRepResult queryRepResult) {
        super(query, geoSys, reportSpec, queryRepResult);
        this.firstBindingSet = null;
        isTimedout = false;
        isOpNotsupported = false;
    }

    public JenaBasedExecutor(String query, JenaBasedGeographicaSystem geoSys,
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
    @Override
    public Map<String, String> getFirstBindingSetValueMap() {
        Map<String, String> valueMap = null;
        String bindingName;
        if (firstBindingSet != null) {
            valueMap = new HashMap<>();
            Iterator<String> vars = firstBindingSet.varNames();
            while (vars.hasNext()) {
                bindingName = vars.next();
                valueMap.put(bindingName, firstBindingSet.get(bindingName).toString());
            }
        }
        return valueMap;
    }

    @Override
    public void run() {
        long tStart = System.currentTimeMillis();
        qryRepResult.setEvalFlag(QueryEvaluationFlag.STARTED);
        geoSys.getRepository().begin(ReadWrite.READ);
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

        // logger.info("Preparing query...");
        qryRepResult.setEvalFlag(QueryEvaluationFlag.PREPARING);
        Query qry;
        QueryExecution qe;
        // Prepare the query
        try {
            qry = QueryFactory.create(this.query);
        } finally {

        }
        // Evaluate query
        t1 = System.nanoTime();
        qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATING);
        try {
            qe = QueryExecutionFactory.create(qry, geoSys.getConnection());
            int remainingExecutionTimeInSecs = (int) ((this.getMaxQueryExecTime() * 1000 - (System.currentTimeMillis() - tStart)) / 1000);
            qe.setTimeout(remainingExecutionTimeInSecs, TimeUnit.SECONDS);
            logger.debug("remainingExecutionTimeInSecs = " + remainingExecutionTimeInSecs);
        } finally {

        }
        qryRepResult.setEvalFlag(QueryEvaluationFlag.EVALUATED);
        // record execution/evaluation time
        t2 = System.nanoTime();
        qryRepResult.setEvalTime(t2 - t1);
        // if there is a valid request for rows to display, first display headers
        if (displayRowsFlag) {
            // process noResults           
            bindingNames = qry.getResultVars();
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
                geoSys.getRepository().end();
                return;
            }
        }
        // scan phase
        qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNING);
        t3 = System.nanoTime();
        ResultSet results;
        try {
            results = qe.execSelect();
        } finally {
        }
        // get first binding set
        try {
            if (results.hasNext()) {
                firstBindingSet = results.next();
                if (rowsDisplayed < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (firstBindingSet.get(label) + "\t");
                    }
                    logger.info(bindingLine);
                    rowsDisplayed++;
                }
                // incement the number of results scanned/retrieved
                qryRepResult.incNoResults();
            }
        } finally {
            logger.debug("Normal exit from 1st part of scan phase");
        }
        qryRepResult.setScanTime(System.nanoTime() - t3);

        // iterate from 2nd binding set onwards
        QuerySolution bindingSet = null;
        bindingLine = "";
        // position scan time reference point
        t4 = System.nanoTime();
        try {
            while (results.hasNext()) {
                bindingSet = results.next();
                if (rowsDisplayed < rowsToDisplay) {
                    bindingLine = "";
                    for (String label : bindingNames) {
                        bindingLine += (bindingSet.get(label) + "\t");
                    }
                    logger.info(bindingLine);
                    rowsDisplayed++;
                }
                qryRepResult.incNoResults();

                if (Thread.currentThread().isInterrupted()) {
                    qryRepResult.setResException(ResultException.TIMEDOUT);
                    logger.error("Caught the interruption myself! I wasn't done! Query prepared and evaluated. Scanning in progress ..."
                            + qryRepResult.getNoResults()
                            + " results, so far. Not big enough patience window defined... :(");
                    try {
                        qe.close();
                    } catch (Exception ex) {
                        logger.error("Exception - queryExecution.close() ERROR - " + ex.getMessage());
                    }
                    geoSys.getRepository().end();
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
        } catch (InterruptedException ex) {
            // Timeout occurred during current thread's sleep
            qryRepResult.setResException(ResultException.TIMEDOUT);
            logger.error("InterruptedException - I wasn't done, but sleeping a bit! Query prepared and evaluated. Scanning in progress ..."
                    + qryRepResult.getNoResults()
                    + " results, so far. Not big enough patience window defined... :(");
            qe.close();
            geoSys.getRepository().end();
            return;
        } catch (QueryCancelledException ex) {
            // Timeout occurred during current thread's sleep
            qryRepResult.setResException(ResultException.TIMEDOUT);
            logger.error("QueryCancelledException - I wasn't done, but sleeping a bit! Query prepared and evaluated. Scanning in progress ..."
                    + qryRepResult.getNoResults()
                    + " results, so far. Not big enough patience window defined... :(");
            qe.close();
            geoSys.getRepository().end();
            return;
        } finally {
            logger.debug("Normal exit from 2nd part of scan phase");
        }
        qryRepResult.addToScanTime(System.nanoTime() - t4); // record scanning time
        if (Thread.currentThread().isInterrupted()) {
            qryRepResult.setResException(ResultException.TIMEDOUT);
            logger.error("Caught the interruption myself! - I was almost done, though! Query prepared, evaluated and scanned. No time to update status ..."
                    + qryRepResult.getNoResults()
                    + " results, so far. Not big enough patience window defined... :(");
            try {
                qe.close();
            } catch (Exception ex) {
                logger.error("Exception - tupleQueryResult.close() - " + ex.getMessage());
            }
            geoSys.getRepository().end();
            return;
        } else {
            qryRepResult.setEvalFlag(QueryEvaluationFlag.SCANNED);
            qe.close();

            if (displayRowsFlag) {
                logger.info("<------------------------------------");
            }
            qryRepResult.setEvalFlag(QueryEvaluationFlag.COMPLETED);
        }
        // Important ‑ free up resources used running the query
        qe.close();
        geoSys.getRepository().end();
    }

    @Override
    public QueryRepResult call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
