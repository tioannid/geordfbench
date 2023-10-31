/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica2.virtuoso;

import gr.uoa.di.rdf.Geographica2.systemsundertest.SystemUnderTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 02/11/2018
 */
public class VirtuosoSUT implements SystemUnderTest {

    static Logger logger = Logger.getLogger(SystemUnderTest.class.getSimpleName());
    public static Properties Properties = new Properties();
    final private static String PROPERTIES_FILE_NAME = "virtuososut.properties";
    static String SYSCMD_VSO_STOP;
    static String SYSCMD_VSO_START;
    static String SYSCMS_VSO_START_DELAY;
    static String SYSCMD_VSO_RESTART;
    static final String SYSCMD_SYNC;
    static final String SYSCMD_CLEARCACHE;

    static {
        InputStream is = VirtuosoSUT.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
        logger.info("Reading " + VirtuosoSUT.class.getName() + " properties from file : " + VirtuosoSUT.class.getResource("/" + PROPERTIES_FILE_NAME));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        // load the properties
        try {
            Properties.load(in);
        } catch (IOException ex) {
            logger.fatal("Exception during static initialization of " + VirtuosoSUT.class.getSimpleName());
        }
        // read all values
        SYSCMD_VSO_STOP = Properties.getProperty("VSO_STOP");
        SYSCMD_VSO_START = Properties.getProperty("VSO_START");
        SYSCMD_VSO_RESTART = Properties.getProperty("VSO_RESTART");
        SYSCMS_VSO_START_DELAY = Properties.getProperty("VSO_START_DELAY");
        SYSCMD_SYNC = Properties.getProperty("SYSCMD_SYNC");
        SYSCMD_CLEARCACHE = Properties.getProperty("SYSCMD_CLEARCACHE");
    }

    // --------------------- Data Members ----------------------------------
    private BindingSet firstBindingSet;
    private String basedir = null;
    private String database = null;
    private String user = null;
    private String passwd = null;
    private Integer port = null;
    private String host = null;
    private String script_start = null;
    private String script_stop = null;
    private Repository repository;  // repository
    private RepositoryConnection connection;    // repository connection
    private Virtuoso sut = null;
    private int displres;

    // --------------------- Constructors ----------------------------------
    public VirtuosoSUT(String basedir, String database, String user, String passwd, Integer port, String host, String startScript, String stopScript, int displres) throws Exception {
        this.basedir = basedir;
        this.database = database;
        this.user = user;
        this.passwd = passwd;
        this.port = port;
        this.host = host;
        this.script_start = startScript;
        this.script_stop = stopScript;
        this.displres = displres;
        SYSCMD_VSO_START = SYSCMD_VSO_START + " " + basedir + "/" + database + " " + SYSCMS_VSO_START_DELAY;
        SYSCMD_VSO_RESTART = SYSCMD_VSO_RESTART + " " + basedir + "/" + database + " " + SYSCMS_VSO_START_DELAY;
        this.repository = null;
        this.connection = null;
    }

    // --------------------- Data Accessors --------------------------------
    public BindingSet getFirstBindingSet() {
        return firstBindingSet;
    }

    public void setSut(Virtuoso sut) {
        if (this.sut != null) {
            try {
                this.sut.close();
            } catch (Exception ex) {
                logger.error("Exception occured while closing running Virtuoso instance ");
            }
        }
        this.sut = sut;
        this.firstBindingSet = null;
    }

    // --------------------- Methods --------------------------------
    @Override
    public void initialize() {
        try {
            sut = new Virtuoso(basedir, database, user, passwd, port, host);
        } catch (RuntimeException e) {
            logger.fatal("Cannot initialize Virtuoso(\"" + basedir + "/" + database + "\")");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.fatal(sw.toString());
        }
    }

    @Override
    public void close() {
        logger.info("Closing..");
        try {
            setSut(null);
        } catch (Exception e) {

        }
        System.gc();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.fatal("Cannot close Virtuoso");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
        logger.info("Closed (caches not cleared)");
    }

    @Override
    public Object getSystem() {
        return sut;
    }

    @Override
    public void clearCaches() {

        String[] stop_vso = {"/bin/sh", "-c", SYSCMD_VSO_STOP};
        String[] start_vso = {"/bin/sh", "-c", SYSCMD_VSO_START};
        String[] sys_sync = {"/bin/sh", "-c", SYSCMD_SYNC};
        String[] clear_caches = {"/bin/sh", "-c", SYSCMD_CLEARCACHE};

        Process pr;

        try {
            logger.info("Clearing caches...");

            pr = Runtime.getRuntime().exec(stop_vso);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stopping vso server");
                logger.error("... with command " + Arrays.toString(stop_vso));
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

            pr = Runtime.getRuntime().exec(start_vso);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting vso server");
                logger.error("... with command " + Arrays.toString(start_vso));
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

    @Override
    public void restart() {

        String[] stop_vso = {"/bin/sh", "-c", SYSCMD_VSO_STOP};
        String[] start_vso = {"/bin/sh", "-c", SYSCMD_VSO_START};

        Process pr;

        try {
            logger.info("Restarting Virtuoso ...");

            pr = Runtime.getRuntime().exec(stop_vso);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while stopping vso server");
                logger.error("... with command " + Arrays.toString(stop_vso));
            }

            pr = Runtime.getRuntime().exec(start_vso);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while starting vso server");
                logger.error("... with command " + Arrays.toString(start_vso));
            }

            Thread.sleep(5000);

            setSut(new Virtuoso(basedir, database, user, passwd, port, host));
            logger.info("Virtuoso restarted");
        } catch (Exception e) {
            logger.fatal("Cannot restart Virtuoso");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }

    @Override
    public long[] runQueryWithTimeout(String query, int timeoutSecs) throws Exception {
        //maintains a thread for executing the doWork method
        final ExecutorService pool = Executors.newFixedThreadPool(1);
        //set the pool thread working
        Executor runnable = new Executor(query, sut, timeoutSecs, this.displres);

        final Future<?> future = pool.submit(runnable);
        boolean isTimedout = false;
        //check the outcome of the pool thread and limit the time allowed for it to complete
        long tt1 = System.nanoTime();
        try {
            logger.debug("Future started");
            /* Wait if necessary for at most <timeoutsSecs> for the computation 
            ** to complete, and then retrieves its result, if available */
            future.get(timeoutSecs, TimeUnit.SECONDS);
            logger.debug("Future end");
        } catch (InterruptedException e) { // current thread was interrupted while waiting
            logger.debug(e.toString());
        } catch (ExecutionException e) {    // the computation threw an exception
            logger.debug(e.toString());
        } catch (TimeoutException e) {  // the wait timed out
            isTimedout = true;
            logger.info("timed out!");
            logger.info("Restarting Virtuoso...");
            this.restart();
            logger.info("Closing Virtuoso...");
            this.close();
        } finally {
            logger.debug("Future canceling...");
            future.cancel(true);
            logger.debug("Executor shutting down...");
            pool.shutdown();
            try {
                logger.debug("Executor waiting for termination...");
                pool.awaitTermination(timeoutSecs, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.debug(e.toString());
            }
            System.gc();
        }

        // logger.debug("RetValue: " + runnable.getExecutorResults());
        logger.debug("RetValue: " + runnable.getRetValue());

        if (isTimedout) {
            long tt2 = System.nanoTime();
            return new long[]{tt2 - tt1, tt2 - tt1, tt2 - tt1, -1};
        } else {
            this.firstBindingSet = runnable.getFirstBindingSet();
            //return runnable.getExecutorResults();
            return runnable.getRetValue();
        }
    }

    @Override
    public long[] runUpdate(String query) throws MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

        logger.info("Executing update...");
        long t1 = System.nanoTime();

        Update preparedUpdate = null;
        try {
            preparedUpdate = this.sut.getConnection().prepareUpdate(QueryLanguage.SPARQL, query);
        } catch (RepositoryException e) {
            logger.error("[Virtuoso.update]", e);
        }
        logger.info("[Virtuoso.update] executing update query: " + query);

        try {
            preparedUpdate.execute();
        } catch (UpdateExecutionException e) {
            logger.error("[Virtuoso.update]", e);
        }

        long t2 = System.nanoTime();
        logger.info("Update executed");

        long[] ret = {-1, -1, t2 - t1, -1};
        return ret;
    }

    public String translateQuery(String query, String label) {

        String translatedQuery = query;

//		givenPoint = "bif:st_point(23.71622, 37.97945)";
//		givenRadius = "2.93782";
        if (label.equals("Intersects_GeoNames_Point_Buffer")) {
            translatedQuery = translatedQuery.replace("geof:sfWithin", "bif:st_within");
            translatedQuery = translatedQuery.replace("geof:buffer(\"POINT(23.71622 37.97945)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 3000, <http://www.opengis.net/def/uom/OGC/1.0/metre>)", "bif:st_point(23.71622, 37.97945), 2.93782");
        } else if (label.equals("Intersects_GeoNames_Point_Distance")) {
            translatedQuery = translatedQuery.replace("geof:distance", "bif:st_distance");
            translatedQuery = translatedQuery.replace(", <http://www.opengis.net/def/uom/OGC/1.0/metre>:", "");
        } else if (label.equals("Area_CLC")) {
            translatedQuery = null;
        } else if (label.equals("Equals_GeoNames_DBPedia")) {
            translatedQuery = translatedQuery.replace("geof:sfEquals(?o1, ?o2)", "bif:st_within(?o1, ?o2, 0)");
        } else if (label.contains("Synthetic_Join_Distance_")
                || label.contains("Synthetic_Selection_Distance_")) {
            translatedQuery = translatedQuery.replace("http://www.opengis.net/ont/geosparql#wktLiteral", "http://www.openlinksw.com/schemas/virtrdf#Geometry");
            translatedQuery = translatedQuery.replace("geof:distance", "bif:st_distance");
            translatedQuery = translatedQuery.replace(", <http://www.opengis.net/def/uom/OGC/1.0/metre>", "");

            String[] querySplitted = translatedQuery.split("<= ");
            if (querySplitted.length == 2) {
                String distanceString = query.split("<= ")[1];
                distanceString = distanceString.split("\\)")[0];
                double distanceInMeter = Double.parseDouble(distanceString);
                double distanceInKm = distanceInMeter / 1000;
                translatedQuery = translatedQuery.replace(distanceString, String.format("%f", distanceInKm));
            }
        }

        return translatedQuery;
    }

}
