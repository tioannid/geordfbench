/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */

package gr.uoa.di.rdf.Geographica2.systemsundertest;

import java.io.IOException;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Univ>
 */
public interface SystemUnderTest {
	
	long[] runQueryWithTimeout(String query, int timeoutSecs) throws Exception ;
	long[] runUpdate(String query) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException;
	void initialize() ;
	void close() ;
	void clearCaches();
	void restart();
	Object getSystem();
	public String translateQuery(String query, String label);
	public BindingSet getFirstBindingSet();
}
