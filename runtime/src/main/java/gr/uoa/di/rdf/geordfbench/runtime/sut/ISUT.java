/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.geordfbench.runtime.sut;

import java.util.Map;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
import gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost;
import gr.uoa.di.rdf.geordfbench.runtime.reportspecs.IReportSpec;
import gr.uoa.di.rdf.geordfbench.runtime.resultscollector.impl.QueryRepResults.QueryRepResult;
import gr.uoa.di.rdf.geordfbench.runtime.sys.interfaces.IGeographicaSystem;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 * @param <S>
 */
public interface ISUT<S> {

    S getSystem();

    /**
     * Return the value of the binding with {@link bindingName} from the first
     * BindingSet.
     *
     * @return a map representing the labels and values for the first bindingset
     */
    public Map<String, String> getFirstBindingSetValueMap();

    IGeographicaSystem getGeographicaSystem();

    void initialize() throws Exception;

    void close();

    void clearCaches();

    void restart();
    
    /**
     * Application server start
     */
    void startServer();
    
    /**
     * Application server stop
     */
    void stopServer();
    
    QueryRepResult runTimedQueryByExecutor(String query, long timeoutSecs) throws Exception;

    long[] runUpdate(String query);

    public String translateQuery(String query, String label);

    IHost getHost();

    IReportSpec getReportSpec();

    IExecutionSpec getExecSpec();

    int getNoRecsToPause();

    void setNoRecsToPause(int noRecsToPause);

    int getNoMsecsToPause();

    void setNoMsecsToPause(int noMsecsToPause);
}
