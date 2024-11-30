package gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * A class which represents the collection of repetition results for a query.
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class QueryRepResults {

    /**
     * The representation of a query repetition result.
     *
     */
    public static class QueryRepResult {
        protected static Logger logger = Logger.getLogger(QueryRepResult.class.getSimpleName());
        public static final QueryRepResult DEFAULT;
        public static final QueryRepResult STARTED;

        static {
            DEFAULT = new QueryRepResult(0, 0, 0, 0, QueryEvaluationFlag.NOTSTARTED, ResultException.NONE);
            STARTED = new QueryRepResult(0, 0, 0, 0, QueryEvaluationFlag.STARTED, ResultException.NONE);
        }

        // --- Data members ------------------------------
        long evalTime; // evaluation time (nsecs)
        long scanTime; // scan results time (nsecs)
        long noResults; // no of results returned
        long noScanErrors; // no of scan errors encountered
        QueryEvaluationFlag evalFlag; // the evaluation stage that has been completed
        ResultException resException; // the exception that caused query evaluation problem

        // --- Constructors ------------------------------
        public QueryRepResult(long evalTime, long scanTime, long noResults, 
                long noScanErrors, QueryEvaluationFlag evalFlag,
                ResultException resException) {
            this.evalTime = evalTime;
            this.scanTime = scanTime;
            this.noResults = noResults;
            this.noScanErrors = noScanErrors;
            this.evalFlag = evalFlag;
            this.resException = resException;
        }

        public QueryRepResult(QueryRepResult qryresult) {
            this.evalTime = qryresult.evalTime;
            this.scanTime = qryresult.scanTime;
            this.noResults = qryresult.noResults;
            this.noScanErrors = qryresult.noScanErrors;
            this.evalFlag = qryresult.evalFlag;
            this.resException = qryresult.resException;
        }

        public QueryRepResult() {
        }

        // --- Data Accessors ------------------------------
        public long getEvalTime() {
            return evalTime;
        }

        public void setEvalTime(long evalTime) {
            this.evalTime = evalTime;
        }

        public long getScanTime() {
            return scanTime;
        }

        public void setScanTime(long scanTime) {
            this.scanTime = scanTime;
        }

        public void addToScanTime(long extraScanTime) {
            this.scanTime = this.scanTime + extraScanTime;
        }

        public long getNoResults() {
            return noResults;
        }

        public void setNoResults(long noResults) {
            this.noResults = noResults;
        }

        public long getNoScanErrors() {
            return noScanErrors;
        }

        public void setNoScanErrors(long noScanErrors) {
            this.noScanErrors = noScanErrors;
        }

        public QueryEvaluationFlag getEvalFlag() {
            return evalFlag;
        }

        public void setEvalFlag(QueryEvaluationFlag evalFlag) {
            logger.info("Transitioning " + this.transitionMsg(evalFlag));
            this.evalFlag = evalFlag;
        }

        public ResultException getResException() {
            return resException;
        }

        public void setResException(ResultException resException) {
            this.resException = resException;
        }        
        
        public long getTotalTime() {
            return evalTime + scanTime;
        }

        public void incNoResults() {
            ++noResults;
        }

        public void incNoScanErrors() {
            ++noScanErrors;
        }

        // prints a msg showing the transition from the internal status to a n
        public String transitionMsg(QueryEvaluationFlag newEvalFlag) {
            return "(" + this.evalFlag.getName() + " => "
                    + newEvalFlag.getName() + ")";
        }

        @Override
        public String toString() {
            String msg = "<" + this.evalFlag.getName() + "-" + this.resException.getName() + "> "
                    + evalTime + " + "
                    + scanTime + " = "
                    + getTotalTime() + " nsecs, "
                    + noResults + " results, "
                    + noScanErrors + " scan errors";
            return msg;
        }

        public String plainPrint() {
            String msg = "<" + this.evalFlag.getName() + "-" + this.resException.getName() + "> "
                    + evalTime + " + "
                    + scanTime + " = "
                    + getTotalTime() + " nsecs, "
                    + noResults + " results, "
                    + noScanErrors + " scan errors";
            return msg;
        }
    }

    // --- Data members ------------------------------
    // Results for a specific repetition
    Map<Integer, QueryRepResult> qryRepResults = new HashMap<>();

    // --- Constructors ------------------------------
    public QueryRepResults() {
    }

    public QueryRepResults(Map<Integer, QueryRepResult> qryRepResults) {
        this.qryRepResults = qryRepResults;
    }

    // --- Data Accessors ------------------------------
    public Map<Integer, QueryRepResult> getQryRepResults() {
        return qryRepResults;
    }

    public void setQryRepResults(Map<Integer, QueryRepResult> qryRepResults) {
        this.qryRepResults = qryRepResults;
    }

    public QueryRepResult getQryRepResult(int qryRepNo) {
        return qryRepResults.get(qryRepNo);
    }

    public void setQryRepResult(int qryRepNo, QueryRepResult qryRepResult) {
        qryRepResults.put(qryRepNo, qryRepResult);
    }

    // --- Methods ------------------------------
    public long getTotalTimeForReps() {
        long total = 0;
        for (int repNo = 0; repNo < qryRepResults.size(); repNo++) {
            total += qryRepResults.get(repNo).getTotalTime();
        }
        return total;
    }
}
