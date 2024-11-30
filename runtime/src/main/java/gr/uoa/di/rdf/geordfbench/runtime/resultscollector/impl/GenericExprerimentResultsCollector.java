package gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl;

import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.ExecutionType;
import gr.uoa.di.rdf.geordfbench.runtime.experiments.Experiment;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.IExperimentResultsCollector;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class GenericExprerimentResultsCollector implements IExperimentResultsCollector {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(GenericExprerimentResultsCollector.class.getSimpleName());

    // --- Data members ------------------------------
    // a map of repetition results for each query and execution type
    Map<ExecutionType, Map<Integer, QueryRepResults>> execTypeQueryResults;
    int[] qif; // array of query numbers
    // number of repetitions requested for each ExecutionType
    Map<ExecutionType, Integer> execTypeReps;
    long maxDurationSecsPerQueryRep;

    // --- Constructors ------------------------------
    public GenericExprerimentResultsCollector(int[] qif,
            Map<ExecutionType, Integer> execTypeReps,
            long maxDurationSecsPerQueryRep) {
        this.qif = qif;
        this.execTypeReps = execTypeReps;
        this.maxDurationSecsPerQueryRep = maxDurationSecsPerQueryRep + 1;
        // initialize the collector structure for all 
        // ExecutionType, query pair. Do that only if the execTypeReps has
        // repetitions for the ExecutionType. Repetion results must not be
        // instantiated now, since different experiments will need to
        // initialize some and not all ExecutionTypes.
        execTypeQueryResults = new HashMap<>();
        int qryNo;
        for (ExecutionType et : execTypeReps.keySet()) {
            Map<Integer, QueryRepResults> queryRepResults = new HashMap<>();
            for (int qifNo = 0; qifNo < qif.length; qifNo++) {
                qryNo = qif[qifNo];
                queryRepResults.put(qryNo, new QueryRepResults());
            }
            this.execTypeQueryResults.put(et, queryRepResults);
        }
    }

    // --- Data Accessors ------------------------------
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        GenericExprerimentResultsCollector.logger = logger;
    }

    public Map<ExecutionType, Map<Integer, QueryRepResults>> getExecTypeQueryResults() {
        return execTypeQueryResults;
    }

    public void setExecTypeQueryResults(Map<ExecutionType, Map<Integer, QueryRepResults>> execTypeQueryResults) {
        this.execTypeQueryResults = execTypeQueryResults;
    }

    public int[] getQif() {
        return qif;
    }

    public void setQif(int[] qif) {
        this.qif = qif;
    }

    public Map<ExecutionType, Integer> getExecTypeReps() {
        return execTypeReps;
    }

    public void setExecTypeReps(Map<ExecutionType, Integer> execTypeReps) {
        this.execTypeReps = execTypeReps;
    }

    public long getMaxDurationSecsPerQueryRep() {
        return maxDurationSecsPerQueryRep;
    }

    public void setMaxDurationSecsPerQueryRep(long maxDurationSecsPerQueryRep) {
        this.maxDurationSecsPerQueryRep = maxDurationSecsPerQueryRep;
    }

    @Override
    public int getExecTypeReps(ExecutionType execType) {
        return this.execTypeReps.get(execType);
    }

    @Override
    public Map<Integer, QueryRepResults> getExecTypeResults(ExecutionType execType) {
        Map<Integer, QueryRepResults> execTypeResults = null;
        try {
            execTypeResults = this.execTypeQueryResults.get(execType);
        } catch (Exception ex) {

        }
        return execTypeResults;
    }

    public QueryRepResults getExecTypeQueryResults(ExecutionType execType, int queryNo) {
        return getExecTypeResults(execType).get(queryNo);
    }

    // --- Methods ------------------------------
    @Override
    public void updateResult(ExecutionType cacheType, int queryNo,
            int queryRepNo, QueryRepResult queryRepResult) {
        getExecTypeResults(cacheType)
                .get(queryNo)
                .setQryRepResult(queryRepNo, queryRepResult);
    }

    @Override
    public String getExecResultString(ExecutionType cacheType, int queryNo,
            int queryRepNo) {
        QueryRepResult qryRepResult = getExecTypeResults(cacheType)
                .get(queryNo).getQryRepResult(queryRepNo);
        return qryRepResult.toString();
    }

    @Override
    public boolean hasExecutionAnyProblems(ExecutionType execType, int queryNo) {
        boolean hasProblem = false;
            QueryRepResult qryRepResult
                    = getExecTypeQueryResults(execType, queryNo).getQryRepResult(0);
            hasProblem = qryRepResult.resException.hasException();
        return hasProblem;
    }

    @Override
    public boolean hasExecutionTimedOut(ExecutionType execType, int queryNo) {
        boolean hasTimedOut = false;
        try {
            QueryRepResult qryRepResult
                    = getExecTypeQueryResults(execType, queryNo).getQryRepResult(0);
            hasTimedOut = ResultException.isTimedOut(qryRepResult.resException);
        } catch (Exception ex) {
            logger.error("SHOULDN'T HAVE HAPPENED! :( :(");
        }
        return hasTimedOut;
    }

    @Override
    public boolean hasExecutionUnsupportedOperation(ExecutionType execType, int queryNo) {
        boolean hasUnsupportedOperation = false;
        try {
            QueryRepResult qryRepResult
                    = getExecTypeQueryResults(execType, queryNo).getQryRepResult(0);
            hasUnsupportedOperation = ResultException.isUnsupportedOperator(qryRepResult.getNoResults());
        } catch (Exception ex) {
            logger.error("SHOULDN'T HAVE HAPPENED! :( :(");
        }
        return hasUnsupportedOperation;
    }

    @Override
    public boolean hasExecutionException(ExecutionType execType, int queryNo) {
        boolean hasTimedOut = false, hasUnsupportedOperation = false;
        try {
            QueryRepResult qryRepResult
                    = getExecTypeQueryResults(execType, queryNo).getQryRepResult(0);
            hasTimedOut = ResultException.isTimedOut(qryRepResult.getNoResults());
            hasUnsupportedOperation = ResultException.isUnsupportedOperator(qryRepResult.getNoResults());
        } catch (Exception ex) {
            logger.error("SHOULDN'T HAVE HAPPENED! :( :(");
        }
        return (hasTimedOut || hasUnsupportedOperation);
    }

    @Override
    public void printAll() {
        int qifNo;
        int qryNo = 0, qryRepNo;
        QueryRepResults results = null;
        String msg = "";
        Map<Integer, QueryRepResults> cacheTypeResults;
        for (ExecutionType cacheType : execTypeReps.keySet()) {
            msg = "Cache " + cacheType;
            // retrieve the results for the chosen ExecutionType/cacheType
            cacheTypeResults = getExecTypeResults(cacheType);
            // skip if there are no results
            if (cacheTypeResults == null) {
                logger.info(msg + "has no results");
                continue;
            }
            logger.info(msg);
            // for each query ...
            for (qifNo = 0; qifNo < qif.length; qifNo++) // get query no from qif if necessary
            {
                // get the real query number (dereference the query number)
                qryNo = qif[qifNo];
                msg = "\tQuery " + qryNo;
                logger.info(msg);
                // retrieve the results for the chosen query number
                results = getExecTypeQueryResults(cacheType, qryNo);
                if (results == null) {
                    logger.info(msg + "has no results");
                    continue;
                }
                for (qryRepNo = 0; qryRepNo < results.getQryRepResults().size(); qryRepNo++) {
                    msg = "\t\tRep " + qryRepNo + "\t" + results.getQryRepResult(qryRepNo).plainPrint();
                    logger.info(msg);
                }
            }
        }
    }

// - based on the sut creates, if missing, the "ISUT-experiment" subdirectory
// - it uses only the total times, that is measurements[..][..][2]
// - it calculates the generalizedAverage of the total times
// - creates a short file
// - creates a long file
    @SuppressWarnings("all")
    @Override
    public void exportStatistics(Experiment exp) {
        FileWriter fstream = null;
        BufferedWriter out = null;
        String shortFileName, longFileName;
        File file;
        int qryNo = 0, qryRepNo;
        int generalizedAveragePos = 0;
        long generalizedAverage = -1;
        long noOfResults = 0;
        int qifNo;
        QueryRepResults results = null;

        // If not exists create experiment folder following the format:
        // logPath / SutName / experimentName
        String dirPath = exp.logPath + "/"
                + exp.getSut().getClass().getSimpleName() + "-"
                + exp.getClass().getSimpleName();
        logger.info("Export statistics in \"" + dirPath + "\"");
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean result = dir.mkdirs(); // create entire tree of directories
            if (result) {
                logger.info("Created non existing directory");
            }
        } else {
            logger.info("Directory already exists!");
        }
        // for each registered execution type ...
        Map<Integer, QueryRepResults> cacheTypeResults;
        for (ExecutionType cacheType : execTypeReps.keySet()) {
            // retrieve the results for the chosen ExecutionType/cacheType
            cacheTypeResults = getExecTypeResults(cacheType);
            // skip if there are no results
            if (cacheTypeResults == null) {
                continue;
            }
            // for each query ...
            for (qifNo = 0; qifNo < qif.length; qifNo++) // get query no from qif if necessary
            {
                // get the real query number (dereference the query number)
                qryNo = qif[qifNo];
                // retrieve the results for the chosen query number
                results = getExecTypeQueryResults(cacheType, qryNo);
                if (results == null) {
                    continue;
                }
                // Create short file
                shortFileName = dirPath + "/" + String.format("%02d", qryNo) + "-" + exp.getQrySet().getQuery(qryNo).getLabel() + "-" + cacheType.name().toLowerCase();
                file = new File(shortFileName);
                try {
                    if (!file.createNewFile()) {
                        logger.error("File " + shortFileName + " already exists");
                    }
                    fstream = new FileWriter(shortFileName, true);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
                out = new BufferedWriter(fstream);
                // If time out write 0 as generalizedAverage otherwise
                // calculate either QUERY_MEDIAN, QUERY_MEAN or QUERYSET_MEAN

                if (cacheType.equals(ExecutionType.COLD) || cacheType.equals(ExecutionType.WARM)) {
//                    if (!this.hasExecutionTimedOut(ExecutionType.COLD, qryNo)) {
                    switch (exp.getExecSpec().getAvgFunc()) {
                        case QUERY_MEDIAN:
                            try {
                                generalizedAveragePos = this.getMedianPos(results);
                                // if generalizedAveragePos < 0, this signals an error condition
                                // and the generalizedAverage is set to the same value!
                                if (generalizedAveragePos >= 0) {
                                    generalizedAverage = results.getQryRepResult(generalizedAveragePos).getTotalTime();
                                } else {
                                    logger.info("generalizedAverage(MEDIAN) = " + generalizedAverage);
                                    generalizedAverage = generalizedAveragePos;
                                }
                            } catch (RuntimeException re) {
                                logger.error(re.getMessage());
                            }
                            break;
                        case QUERY_MEAN:
                            generalizedAverage = this.getMean(results);
                    }
                    if (generalizedAveragePos >= 0) {
                        noOfResults = results.getQryRepResult(generalizedAveragePos).noResults;
                    } else {
                        logger.info("noOfResults(MEDIAN) = " + 0);
                        noOfResults = 0;
                    }
//                    } else {
//                        // If time out write -1 as generalizedAverage and o for no of results
//                        generalizedAverage = -1;
//                        //
//                        QueryRepResults tmp = getExecTypeQueryResults(cacheType, qryNo);
//                        QueryRepResult tmp1 = tmp.getQryRepResults().get(cacheType.ordinal());
//                        noOfResults=tmp1.getNoResults();
//                    }
                    try {
                        out.write(noOfResults + " " + generalizedAverage + "\n");
                        out.close();
                        logger.info("Statistiscs printed: " + shortFileName);
                    } catch (IOException ex) {
                        logger.error(ex.getMessage());
                    }
                } else if (cacheType.equals(ExecutionType.COLD_CONTINUOUS)) {
                    try {
                        out.write(results.qryRepResults.size() + " " + results.getTotalTimeForReps() + "\n");
                        out.close();
                    } catch (IOException ex) {
                        logger.error(ex.getMessage());
                    }
                } else { // TODO: if some new ExecutionType is added in the future

                }

                // Create long file
                longFileName = shortFileName + "-long";
                file = new File(longFileName);
                try {
                    if (!file.createNewFile()) {
                        logger.error("File " + longFileName + " already exists");
                    }
                    fstream = new FileWriter(longFileName, true);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
                out = new BufferedWriter(fstream);
                try {
                    for (qryRepNo = 0; qryRepNo < results.getQryRepResults().size(); qryRepNo++) {
                        out.write(results.getQryRepResult(qryRepNo).getNoResults() + " "
                                + results.getQryRepResult(qryRepNo).getEvalTime() + " "
                                + results.getQryRepResult(qryRepNo).getScanTime() + " "
                                + results.getQryRepResult(qryRepNo).getTotalTime()
                                + "\n");
                    }
                    out.close();
                    logger.info("Statistiscs printed: " + longFileName);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
    }

    /**
     * Finds the repetition which represents the QUERY_MEDIAN value of the total
     * execution time = query exec + result scan.
     *
     * @param results
     * @return the position in qryResults[i][] where the generalizedAverage
     * total execution time lies. If results is null then returns -1
     */
    public int getMedianPos(QueryRepResults results) {
        SortedMap<Long, Integer> smp = new TreeMap<>();
        int repetitions = results.getQryRepResults().size();
        // in case of a skipped query then results.getQryRepResults().size() equals 0!
        if (repetitions == 0) {
            return -1;
        }
        // sort results by total time = exec time + scan results time
        for (int qryRepNo = 0; qryRepNo < repetitions; qryRepNo++) {
            smp.put(results.getQryRepResult(qryRepNo).getTotalTime(), qryRepNo);
        }
        logger.debug("Sorted map of total values = " + smp.toString());
        // calculate generalizedAverage
        int mapMedianPos = (repetitions / 2); // generalizedAverage map entry index
        int medianQryRepPos = 0; // the index of the generalizedAverage query repetition value in qryResults[]
        int mapMeasurementIdx = 0; // index of the current map measurement
        if (repetitions % 2 == 1) { // odd number of repetitions
            logger.debug("Odd number of values, therefore we select the middle one in position " + (repetitions / 2));
            for (Entry<Long, Integer> measurement : smp.entrySet()) {
                if (mapMeasurementIdx == mapMedianPos) {
                    medianQryRepPos = measurement.getValue();
                    break;
                } else {
                    mapMeasurementIdx++;
                }
            }
        } else { // even number of repetitions
            throw new RuntimeException("Even number of values, cannot calculate median value position!");
        }
        logger.debug("Median query repetion index is " + medianQryRepPos);
        return medianQryRepPos;
    }

    /**
     * Finds the repetition which represents the QUERY_MEAN value of the total
     * execution time = query exec + result scan.
     *
     * @param results
     * @return the mean total execution time for all repetitions of the query
     */
    public long getMean(QueryRepResults results) {
        int repetitions = results.getQryRepResults().size();
        long totalExecTime = 0;
        // sort results by total time = exec time + scan results time
        for (int qryRepNo = 0; qryRepNo < repetitions; qryRepNo++) {
            totalExecTime = totalExecTime + results.getQryRepResult(qryRepNo).getTotalTime();
        }
        return (totalExecTime / repetitions);
    }
}
