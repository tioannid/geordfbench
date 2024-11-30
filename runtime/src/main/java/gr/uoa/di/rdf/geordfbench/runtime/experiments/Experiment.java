/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
package gr.uoa.di.rdf.geordfbench.runtime.experiments;

import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.BehaviourOnColdExecutionFailure;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec.ExecutionType;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.GenericExprerimentResultsCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.IQuerySet;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;
import gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.IGeospatialDataSet;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.reportsource.IReportSource;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.IExperimentResultsCollector;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.geordfbench.runtime.sut.ISUT;

// <C> represents the connection class for each library (Sesame, RDF4J, Jena)
// IMPORTANT: Concrete subclass of Experiment should take care of appropriately
// instantiating the corresponding qrySet. This will definitely require at
// least an extra constructor parameter 'boolean usePred". This fact is also
// exhibited in the Experiment.run() method
public class Experiment {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(Experiment.class.getSimpleName());

    // --- Data members ------------------------------
    long expID;
    protected String description;
    protected IExecutionSpec execSpec;
    public String logPath;
    protected IQuerySet qrySet = null;
    protected int[] qif = null;
    protected ISUT sut = null;
    protected IGeospatialDataSet geoDS = null;
    protected IReportSpec rptSpec = null;
    protected IReportSource rptSrcSpec;
    protected IExperimentResultsCollector expResColl;
    boolean usePred;

    // --- Constructors ------------------------------
    // 1. Autonomous constructor:
    //      all data members receive user-provided, properly instantiated objects
    //      queriesToRun[] is initialized through the qrySet object
    public Experiment(ISUT sut,
            IQuerySet queriesSet,
            int[] qif,
            IGeospatialDataSet geodataset,
            IExecutionSpec execSpec,
            IReportSpec rptSpec,
            IReportSource rptSrcSpec,
            String logPath, String description) throws Exception {
        this.expID = 0;
        this.sut = sut;
        sut.initialize();
        this.qrySet = queriesSet;
        // -- apply SUT-dependent translations to queryset
        logger.info(sut.getGeographicaSystem().getClass().getSimpleName() + "-dependent translation of the queryset " + qrySet.getName());
        for (Integer pos : qrySet.getMapQueries().keySet()) {
            IQuery qry = qrySet.getQuery(pos);
            qry.setText(this.sut.translateQuery(qry.getText(), qry.getLabel()));
        }
        // -- merge SUT-dependent namespace prefixes to queryset namespace prefixes
        logger.info(sut.getGeographicaSystem().getClass().getSimpleName() + "-dependent namespace prefixes merged with the prefixes of queryset " + qrySet.getName());
        qrySet.addMapUsefulNamespacePrefixes(sut.getGeographicaSystem().getMapUsefulPrefixes());
        this.geoDS = geodataset;
        this.qif = qif;
        this.rptSpec = rptSpec;
        this.rptSrcSpec = rptSrcSpec;
        this.expResColl = new GenericExprerimentResultsCollector(this.qif,
                execSpec.getExecTypeReps(),
                execSpec.getMaxDurationSecsPerQueryRep());
        this.qrySet.addMapUsefulNamespacePrefixes(geoDS.getMapUsefulNamespacePrefixes());
        // validate that Scalability query set has been appropriately filtered
        getUsePred();
        //TODO - CHECK!!! this.qrySet.augmentPrefixesHeader(this.sut.getGeographicaSystem().getMapUsefulPrefixes());
        this.execSpec = execSpec;
        this.logPath = logPath;
        this.description = description;
        sut.close();
    }

    // --- Data Accessors -----------------------------------
    public long getExpID() {
        return expID;
    }

    public void setExpID(long expID) {
        this.expID = expID;
    }

    public IQuerySet getQrySet() {
        return qrySet;
    }

    public int[] getQif() {
        return qif;
    }

    public void setQif(int[] qif) {
        this.qif = qif;
    }

    public IExecutionSpec getExecSpec() {
        return execSpec;
    }

    public ISUT getSut() {
        return sut;
    }

    public IGeospatialDataSet getGeoDS() {
        return geoDS;
    }

    public IReportSpec getRptSpec() {
        return rptSpec;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public IExperimentResultsCollector getExpResColl() {
        return expResColl;
    }

    public void setExpResColl(IExperimentResultsCollector expResColl) {
        this.expResColl = expResColl;
    }

    public IReportSource getRptSrcSpec() {
        return rptSrcSpec;
    }

    public void setRptSrcSpec(IReportSource rptSrcSpec) {
        this.rptSrcSpec = rptSrcSpec;
    }

    public String getType() {
        return qrySet.getName() + "-" + geoDS.getName();
    }

    // --- Methods -----------------------------------
    public void run() throws Exception {
        IQuery qry = null;
        int coldRepetitions = execSpec.getExecTypeReps(ExecutionType.COLD);
        int warmRepetitions = execSpec.getExecTypeReps(ExecutionType.WARM);
        int coldcontinousRepetitions = execSpec.getExecTypeReps(ExecutionType.COLD_CONTINUOUS);

        long maxDurationSecsPerQueryRep = execSpec.getMaxDurationSecsPerQueryRep();
        int queriesToRunN = this.qrySet.getQueriesNum();
        int qryNo, qryRepNo = 0;
        QueryRepResult queryRepResult;
        int qifNo;
        long maxDurationMilliSecs = execSpec.getMaxDurationSecs() * 1000;
        long curDurationMilliSecs = 0, startTime = System.currentTimeMillis();
        int querySetIterations = 0;
        ExecutionType cache_type;
        List<Integer> problematicQueries = new ArrayList<>();

//        logger.info("Experiment Result Collector before loop");
//        this.expResColl.printAll();
        // while the maximum duration (msecs) has not been exceeded continue...
        while (curDurationMilliSecs < maxDurationMilliSecs) {
            // at this point send a trigger to the queryset in order to
            // initialize the queries (very dynamic, random-based or
            // file-driven querysets)
            this.qrySet.initializeQuerysetIteration();

            // if there are cold runs to be made continue...
            if (coldRepetitions > 0) {
                // COLD
                cache_type = ExecutionType.COLD;
                // Run all coldRepetitions of each query so stats can be printed
                for (qifNo = 0; qifNo < queriesToRunN; qifNo++) {
                    // get query no from qif if necessary
                    qryNo = qif[qifNo];
                    // retrieve query in position-qryNo for qryRepNo
                    qry = qrySet.getQuery(qryNo).getNamespaceHeaderedQuery(qrySet.getNamespacePrefixHeader());
                    // checks and initializations for warm cache executions only!
                    try {
                        for (qryRepNo = 0; qryRepNo < coldRepetitions; qryRepNo++) {
                            // log query execution startServer
                            logger.info("|==> Executing query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): \n"
                                    + qry.getText());
                            // clear caches
                            sut.clearCaches();
                            // initialize system
                            sut.initialize();
                            // execute query qryNo repetition qryRepNo
                            queryRepResult = sut.runTimedQueryByExecutor(qry.getText(), maxDurationSecsPerQueryRep);
                            expResColl.updateResult(cache_type, qryNo, qryRepNo, queryRepResult);
                            // log query execution completion
                            logger.info("|<== Executed query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): "
                                    + expResColl.getExecResultString(cache_type, qryNo, qryRepNo) + " - "
                                    + ((qry.isResultAccurate(queryRepResult)) ? "ACCURATE" : "ACCURACY NOT DETERMINED"));
                            rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
                                    qry.getLabel(), qryNo, qryRepNo, queryRepResult);
                            // close sut
                            sut.close();
                            // If query times out abort all remaining coldRepetitions
                            if (queryRepResult.getResException().hasException()) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("While evaluating query(" + cache_type.name() + ", "
                                + qryNo + ", " + qryRepNo + ")\n" + e.getMessage());
                        sut.close();
                        sut.clearCaches();
                        sut.initialize();
                    }
                }
            }
//            logger.info("Experiment Result Collector after COLD runs");
//            this.expResColl.printAll();

            // if there are warm runs to be made continue...
            if (warmRepetitions > 0) {
                // WARM
                cache_type = ExecutionType.WARM;
                // Run all coldRepetitions of each query so stats can be printed
                for (qifNo = 0; qifNo < queriesToRunN; qifNo++) {
                    // get query no from qif if necessary
                    qryNo = qif[qifNo];
                    // retrieve query in position-qryNo for qryRepNo
                    qry = qrySet.getQuery(qryNo).getNamespaceHeaderedQuery(qrySet.getNamespacePrefixHeader());
                    // If cold run has exception (e.g. timed out) then skip warm run as well
                    if (expResColl.hasExecutionAnyProblems(ExecutionType.COLD, qryNo)) {
                        logger.info("Skip warm run of query " + qryNo
                                + " because cold run had a problem "
                                + expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0).toString());
                        // insert a copy of the COLD repetition 0 entry to WARM  repetition 0
                        expResColl.updateResult(cache_type, qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
                        rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
                                qry.getLabel(), qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
                        continue;
//                    } else if (expResColl.hasExecutionTimedOut(ExecutionType.COLD, qryNo)) {
//                        logger.info("Skip warm run of query " + qryNo
//                                + " because cold has timed out "
//                                + expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0).toString());
//                        // insert a copy of the COLD repetition 0 entry to WARM  repetition 0
//                        expResColl.updateResult(cache_type, qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
//                        rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
//                                    qry.getLabel(), qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
//                        continue;
//                    } else if (expResColl.hasExecutionUnsupportedOperation(ExecutionType.COLD, qryNo)) {
//                        logger.info("Skip warm run of query " + qryNo
//                                + " because cold rep 0 raised an unsupported operation exception"
//                                + expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0).toString());
//                        // insert a copy of the COLD repetition 0 entry to WARM  repetition 0
//                        expResColl.updateResult(cache_type, qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
//                        rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
//                                    qry.getLabel(), qryNo, 0, expResColl.getExecTypeResults(ExecutionType.COLD).get(qryNo).getQryRepResult(0));
//                        continue;
                    }
                    // warm up cache by running one cold execution
                    // clear caches
                    sut.clearCaches();
                    // initialize system
                    sut.initialize();
                    try {
                        // execute cold execution to warm up caches and discards
                        // the returned value intentioanally
                        logger.info("|==> Pseudo-executing query ["
                                + qryNo + ", " + qry.getLabel() + "] to warm up caches");
                        queryRepResult = sut.runTimedQueryByExecutor(qry.getText(), maxDurationSecsPerQueryRep);
                        // now that the caches are warm, perform the warm execution measurements
                        for (qryRepNo = 0; qryRepNo < warmRepetitions; qryRepNo++) {
                            // log query execution startServer
                            logger.info("|==> Executing query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): \n"
                                    + qry.getText());
                            // execute query qryNo repetition qryRepNo
                            queryRepResult = sut.runTimedQueryByExecutor(qry.getText(), maxDurationSecsPerQueryRep);
                            expResColl.updateResult(cache_type, qryNo, qryRepNo, queryRepResult);
                            // log query execution completion
                            logger.info("|<== Executed query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): "
                                    + expResColl.getExecResultString(cache_type, qryNo, qryRepNo) + " - "
                                    + ((qry.isResultAccurate(queryRepResult)) ? "ACCURATE" : "ACCURACY NOT DETERMINED"));
                            rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
                                    qry.getLabel(), qryNo, qryRepNo, queryRepResult);
                            // If query times out abort all remaining warmRepetitions
                            if (queryRepResult.getResException().hasException()) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("While evaluating query(" + cache_type.name() + ", "
                                + qryNo + ", " + qryRepNo + ")\n" + e.getMessage());
                        sut.close();
                        sut.clearCaches();
                        sut.initialize();
                    }
                }
            }
//            logger.info("Experiment Result Collector after WARM runs");
//            this.expResColl.printAll();

            // if there are cold continuous runs to be made continue...
            if (coldcontinousRepetitions == -1) {
                // COLD_CONTINUOUS
                cache_type = ExecutionType.COLD_CONTINUOUS;
                // initialize system only once!
                if (querySetIterations == 0) {
                    // clear caches once!
                    sut.clearCaches();
                    // initialize system once!
                    sut.initialize();
                }
                // Run all queries
                for (qifNo = 0; qifNo < queriesToRunN; qifNo++) {
                    // get query no from qif if necessary
                    qryNo = qif[qifNo];
                    // if query belongs to the problematic queries skip it
                    if (problematicQueries.contains(qryNo)) {
                        logger.info("Query " + qryNo + " is skipped, because its previous execution had a problem");
                        continue;
                    }
                    // retrieve query in position-qryNo for qryRepNo
                    qry = qrySet.getQuery(qryNo).getNamespaceHeaderedQuery(qrySet.getNamespacePrefixHeader());
                    try {
                        // 1 repetition of each query
                        for (qryRepNo = querySetIterations; qryRepNo < (querySetIterations + 1); qryRepNo++) {
                            // log query execution startServer
                            logger.info("|==> Executing query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): \n"
                                    + qry.getText());
                            // execute query qryNo repetition qryRepNo
                            queryRepResult = sut.runTimedQueryByExecutor(qry.getText(), maxDurationSecsPerQueryRep);
                            // Update firstBindingSet value map
                            qrySet.setFirstBindingSetValues(sut.getFirstBindingSetValueMap());
                            // update the experiment results collector
                            expResColl.updateResult(cache_type, qryNo, qryRepNo, queryRepResult);
                            // log query execution completion
                            logger.info("|<== Executed query ["
                                    + qryNo + ", " + qry.getLabel() + "] ("
                                    + cache_type.name() + ", "
                                    + qryRepNo + "): "
                                    + expResColl.getExecResultString(cache_type, qryNo, qryRepNo) + " - "
                                    + ((qry.isResultAccurate(queryRepResult)) ? "ACCURATE" : "ACCURACY NOT DETERMINED"));
                            rptSrcSpec.insQueryExecution(this.expID, cache_type.name(),
                                    qry.getLabel(), qryNo, qryRepNo, queryRepResult);
                            // If query times out abort all remaining coldRepetitions
                            if (queryRepResult.getResException().hasException()) {
                                if (this.execSpec.getOnColdFailure().equals(BehaviourOnColdExecutionFailure.EXCLUDE_AND_PROCEED)) {
                                    problematicQueries.add(qryNo);
                                } else {
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("While evaluating query(" + cache_type.name() + ", "
                                + qryNo + ", " + qryRepNo + ")\n" + e.getMessage());
                        sut.close();
                        sut.clearCaches();
                        sut.initialize();
                    }
                }
            }

            querySetIterations++;
            curDurationMilliSecs = System.currentTimeMillis() - startTime;
            // Break from the external loop which concerns only macro type
            // experiments
            if (coldcontinousRepetitions == 0) {
                break;
            }
        }
        // close sut
        sut.close();
        // flush any deferred records in the ReportSource
        rptSrcSpec.flush();
        // print statistics
        expResColl.exportStatistics(this);
        expResColl.printAll();
    }

    private void getUsePred() {
        boolean hasPred;
        boolean isValid;
        if (qrySet != null) {
            int pos = (Integer) (this.qrySet.getMapQueries().keySet().toArray()[0]);
            hasPred = qrySet.getMapQueries().get(pos).isUsePredicate();
            isValid = true;
            for (Map.Entry<Integer, IQuery> e : qrySet.getMapQueries().entrySet()) {
                isValid = isValid & (hasPred == e.getValue().isUsePredicate());
            }
            if (!isValid) {
                throw new RuntimeException(qrySet.getName() + " query set contains both function and predicate queries!");
            }
        } else {
            throw new RuntimeException(qrySet.getName() + " query set is null!");
        }
    }

}
