/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.strabon;

import eu.earthobservatory.runtime.postgis.Strabon;
import gr.uoa.di.rdf.Geographica.systemsundertest.SystemUnderTest;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 *
 */
public class StrabonSUT implements SystemUnderTest {

    static Logger logger = Logger.getLogger(StrabonSUT.class.getSimpleName());
    public static Properties Properties = new Properties();
    final private static String PROPERTIES_FILE_NAME = "strabonsut.properties";
    static String SYSCMD_POSTGRES_STOP;
    static String SYSCMD_POSTGRES_START;
    static String SYSCMD_POSTGRES_RESTART;
    static String SYSCMD_SYNC;
    static String SYSCMD_CLEARCACHE;

    private Strabon strabon = null;
    private BindingSet firstBindingSet;

    String db = null;
    String user = null;
    String passwd = null;
    Integer port = null;
    String host = null;

    static {
        InputStream is = StrabonSUT.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
        logger.info("Reading StrabonSUT properties from file : " + StrabonSUT.class.getResource("/" + PROPERTIES_FILE_NAME));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        // load the properties
        try {
            Properties.load(in);
        } catch (IOException ex) {
            logger.fatal("Exception during static initialization of " + StrabonSUT.class.getSimpleName());
        }
        // read all values
        SYSCMD_POSTGRES_STOP = Properties.getProperty("POSTGRES_STOP");
        SYSCMD_POSTGRES_START = Properties.getProperty("POSTGRES_START");
        SYSCMD_POSTGRES_RESTART = Properties.getProperty("POSTGRES_RESTART");
        SYSCMD_SYNC = Properties.getProperty("SYSCMD_SYNC");
        SYSCMD_CLEARCACHE = Properties.getProperty("SYSCMD_CLEARCACHE");
    }

    public StrabonSUT(String db, String user, String passwd, Integer port,
            String host) throws Exception {

        this.db = db;
        this.user = user;
        this.passwd = passwd;
        this.port = port;
        this.host = host;
    }

    public BindingSet getFirstBindingSet() {
        return firstBindingSet;
    }

    public Object getSystem() {
        return this.strabon;
    }

    public void initialize() {
        try {
            strabon = new Strabon(db, user, passwd, port, host, true);
        } catch (Exception e) {
            logger.fatal("Cannot initialize strabon");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    static class Executor implements Runnable {

        private String query;
        private Strabon strabon;
        private long[] returnValue;
        private BindingSet firstBindingSet;

        public Executor(String query, Strabon strabon, int timeoutSecs) {
            this.query = query;
            this.strabon = strabon;
            this.returnValue = new long[]{timeoutSecs + 1, timeoutSecs + 1, timeoutSecs + 1, -1};
        }

        public long[] getRetValue() {
            return returnValue;
        }

        public BindingSet getFirstBindingSet() {
            return firstBindingSet;
        }

        public void run() {
            try {
                runQuery();
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

        public void runQuery() throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

            logger.info("Evaluating query...");
            TupleQuery tupleQuery = (TupleQuery) strabon.query(query,
                    eu.earthobservatory.utils.Format.TUQU, strabon
                            .getSailRepoConnection(), (OutputStream) System.out);

            long results = 0;

            long t1 = System.nanoTime();
            TupleQueryResult result = tupleQuery.evaluate();
            long t2 = System.nanoTime();

            if (result.hasNext()) {
                this.firstBindingSet = result.next();
                results++;
            }
            while (result.hasNext()) {
                results++;
                result.next();
            }
            long t3 = System.nanoTime();

            logger.info("Query evaluated");
            this.returnValue = new long[]{t2 - t1, t3 - t2, t3 - t1, results};
        }
    }

    public long[] runQueryWithTimeout(String query, int timeoutSecs) {
        //maintains a thread for executing the doWork method
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        //set the executor thread working
        Executor runnable = new Executor(query, strabon, timeoutSecs);

        final Future<?> future = executor.submit(runnable);
        boolean isTimedout = false;
        //check the outcome of the executor thread and limit the time allowed for it to complete
        long tt1 = System.nanoTime();
        try {
            logger.debug("Future started");
            future.get(timeoutSecs, TimeUnit.SECONDS);
            logger.debug("Future end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            isTimedout = true;
            logger.info("time out!");
            logger.info("Restarting Strabon...");
            this.restart();
            logger.info("Closing Strabon...");
            this.close();
        } finally {
            logger.debug("Future canceling...");
            future.cancel(true);
            logger.debug("Executor shutting down...");
            executor.shutdown();
            try {
                logger.debug("Executor waiting for termination...");
                executor.awaitTermination(timeoutSecs, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.gc();
        }

        logger.debug("RetValue: " + runnable.getRetValue());

        if (isTimedout) {
            long tt2 = System.nanoTime();
            return new long[]{tt2 - tt1, tt2 - tt1, tt2 - tt1, -1};
        } else {
            this.firstBindingSet = runnable.getFirstBindingSet();
            return runnable.getRetValue();
        }
    }

    public long[] runUpdate(String query) throws MalformedQueryException {

        logger.info("Executing update...");
        long t1 = System.nanoTime();
        strabon.update(query, strabon.getSailRepoConnection());
        long t2 = System.nanoTime();
        logger.info("Update executed");

        long[] ret = {-1, -1, t2 - t1, -1};
        return ret;
    }

    public void close() {

        logger.info("Closing..");
        try {
            strabon.close();
            strabon = null;
            firstBindingSet = null;
        } catch (Exception e) {
        }
        System.gc();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.fatal("Cannot close Strabon");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        logger.info("Closed (caches not cleared)");
    }

    public void restart() {

        String[] restart_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_RESTART};

        Process pr;

        try {
            logger.info("Restarting Strabon (Postgres) ...");

            pr = Runtime.getRuntime().exec(restart_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stoping postgres");
                logger.error("... with command " + Arrays.toString(restart_postgres));
            }

            Thread.sleep(5000);

            if (strabon != null) {
                try {
                    strabon.close();
                } catch (Exception e) {
                    logger.error("Exception occured while restarting Strabon. ");
                    e.printStackTrace();
                } finally {
                    strabon = null;
                }
            }
            firstBindingSet = null;
            strabon = new Strabon(db, user, passwd, port, host, true);
            logger.info("Strabon (Postgres) restarted");
        } catch (Exception e) {
            logger.fatal("Cannot restart Strabon");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    public void clearCaches() {

        String[] stop_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_STOP};
        String[] sys_sync = {"/bin/sh", "-c", SYSCMD_SYNC};
        String[] clear_caches = {"/bin/sh", "-c", SYSCMD_CLEARCACHE};
        String[] start_postgres = {"/bin/sh", "-c", SYSCMD_POSTGRES_START};

        Process pr;

        try {
            logger.info("Clearing caches...");

            pr = Runtime.getRuntime().exec(stop_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stopping postgres");
                logger.error("... with command " + Arrays.toString(stop_postgres));
            }

            pr = Runtime.getRuntime().exec(sys_sync);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while system sync");
            }

            pr = Runtime.getRuntime().exec(clear_caches);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while clearing caches");
            }

            pr = Runtime.getRuntime().exec(start_postgres);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting postgres");
                logger.error("... with command " + Arrays.toString(start_postgres));
            }

            Thread.sleep(5000);
            logger.info("Caches cleared");
        } catch (Exception e) {
            logger.fatal("Cannot clear caches");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    public String translateQuery(String query, String label) {
        String translatedQuery = null;
        translatedQuery = query;

        translatedQuery = translatedQuery.replaceAll("geof:union", "strdf:union");
        
        if (label.matches("Q6_Area_CLC")) {
            translatedQuery = translatedQuery.replaceAll("strdf:area", "geof:area");
        } else if (label.indexOf("Synthetic_Selection_Distance") != -1) {
            // convert this: FILTER ( bif:st_within(?geo1, bif:st_point(45, 45), 5000.000000)) 
            // .....to this: FILTER ( geof:sfWithin(?geo1, geof:buffer("POINT(23.71622 37.97945)"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 5000, <http://www.opengis.net/def/uom/OGC/1.0/metre>)))
            // 1. locate the last part of the query which starts with FILTER
            String cGeom = "";
            long cRadious = 0;
            String oldFilter = translatedQuery.substring(translatedQuery.indexOf("FILTER"));
            // 2. split to 4 parts using the comma as delimiter
            String[] oldfilterPart = oldFilter.split(",");
            // 3. split part-0 using the ( as delimiter
            //    ?geo1 is portion-2 of part-0
            cGeom = oldfilterPart[0].split("\\(")[2];
            // 4. split part-3 using the ) as delimiter
            //    RADIOUS is portion-0 of part-3 converted to long 
            cRadious = (long) Float.parseFloat(oldfilterPart[3].split("\\)")[0]);
            // 5. create the new filter using the desired format
            String newFilter = String.format("FILTER(geof:sfWithin(%s, geof:buffer(\"POINT(45 45)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, %d, <http://www.opengis.net/def/uom/OGC/1.0/metre>))).\n}\n", cGeom, cRadious);
            // 6. replace old with new filter
            translatedQuery = translatedQuery.substring(0, translatedQuery.indexOf("FILTER")) + newFilter;
        }
        return translatedQuery;
    }
}
