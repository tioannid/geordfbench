/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.runtime.sys.executor;

import java.util.Map;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.Geographica3.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import java.util.concurrent.Callable;

public abstract class AbstractExecutor<IGeographicaSystem> implements Callable<QueryRepResult> {

    // --- Static members -----------------------------
    protected static Logger logger = Logger.getLogger(AbstractExecutor.class.getSimpleName());

    // --- Data members ------------------------------
    protected final String query;
    protected final IGeographicaSystem geoSys;
    protected IReportSpec reportSpec;
    protected QueryRepResult qryRepResult;
    protected int maxQueryExecTime; // max query execution time in secs

    // --- Constructors ------------------------------
    public AbstractExecutor(String query, IGeographicaSystem geoSys, 
            IReportSpec reportSpec, QueryRepResult queryRepResult) {
        this.query = query;
        this.geoSys = geoSys;
        this.reportSpec = reportSpec;
        this.qryRepResult = queryRepResult;
    }

    // --- Data Accessors -----------------------------------
    public QueryRepResult getQryRepResult() {
        return qryRepResult;
    }

    public int getMaxQueryExecTime() {
        return maxQueryExecTime;
    }

    public void setMaxQueryExecTime(int maxQueryExecTime) {
        this.maxQueryExecTime = maxQueryExecTime;
    }

    // --- Methods -----------------------------------
    /**
     * Gets the first binding set <label, value> pairs as a map. This is a more
     * generic representation (abstraction) from the commonly specific
     * BindingSet type used by each RDF store.
     *
     * @return a map of binding set <label, value> pairs
     */
    public abstract Map<String, String> getFirstBindingSetValueMap();
}
