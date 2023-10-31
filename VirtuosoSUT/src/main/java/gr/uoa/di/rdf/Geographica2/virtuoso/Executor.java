/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.virtuoso;

import java.io.IOException;
import java.util.List;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

/* Utility class Executor
 ** Executes queries on Virtuoso
 */

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 19/11/2018
 */
class Executor implements Runnable {

    // --------------------- Data Members ----------------------------------
    private final String query;
    private final Virtuoso sut;
    private BindingSet firstBindingSet;
    private int dispres;
    private long[] returnValue;

    // --------------------- Constructors ----------------------------------
    public Executor(String query, Virtuoso sut, int timeoutSecs, int dispres) {
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
        long t1 = 0;
        long t2 = 0;
        long t3 = 0;
        long results = 0;
        long noOfScanErrors = 0;
        int printedrow = 0;
        String bindingLine = "";
        String labelsTitle = "\t";
        List<String> bindings = null;
        boolean displayRowsFlag = rowsToDisplay != 0;
        VirtuosoSUT.logger.info("Evaluating query...");
        TupleQuery tupleQuery = sut.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
        // Evaluate and time the evaluation of the prepared query
        TupleQueryResult tupleQueryResult = null;
        t1 = System.nanoTime();
        // if there is an exception during evaluation throw it and return
        try {
            tupleQueryResult = tupleQuery.evaluate();
        } catch (QueryEvaluationException ex) {
            VirtuosoSUT.logger.error("[Query evaluation phase]", ex);
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
            VirtuosoSUT.logger.info(labelsTitle);
            VirtuosoSUT.logger.info("------------------------------------>");
            while (tupleQueryResult.hasNext()) {
                try {
                    firstBindingSet = tupleQueryResult.next();
                    if (printedrow < rowsToDisplay) {
                        bindingLine = "";
                        for (String label : bindings) {
                            bindingLine += (firstBindingSet.getValue(label) + "\t");
                        }
                        VirtuosoSUT.logger.info(bindingLine);
                        printedrow++;
                    }
                    results++;
                } catch (Exception ex) {
                    noOfScanErrors++;
                    VirtuosoSUT.logger.error("[Query full scan phase]");
                }
            }
        } else {
            while (tupleQueryResult.hasNext()) {
                try {
                    firstBindingSet = tupleQueryResult.next();
                    results++;
                } catch (Exception ex) {
                    noOfScanErrors++;
                    VirtuosoSUT.logger.error("[Query full scan phase]");
                }
            }
        }
        t3 = System.nanoTime();
        if (displayRowsFlag) {
            VirtuosoSUT.logger.info("<------------------------------------\n");
        }
        VirtuosoSUT.logger.info("Query evaluated with " + results + " results and " + noOfScanErrors + " scan errors!");
        this.returnValue = new long[]{t2 - t1, t3 - t2, t3 - t1, results};
    }
    
}
