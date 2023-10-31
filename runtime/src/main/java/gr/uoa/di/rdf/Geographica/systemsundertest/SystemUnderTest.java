/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */

package gr.uoa.di.rdf.Geographica.systemsundertest;

import java.io.IOException;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * @author George Garbis <ggarbis@di.uoa.gr>
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
