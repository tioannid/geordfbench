package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.file.PathUtils;
import org.apache.log4j.Logger;

/**
 * A class that checks the following use cases: 1) Native Sail a) Load N-Triple
 * file in existing repo, 2) Lucene Sail a) Load N-Triple file in existing repo
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 18/09/2024
 * @updatedate 27/09/2024
 */
public class LoadRDF4JRepositoryTest1 {

    // --- Static Members -----------------------
    public static Logger logger = Logger.getLogger(LoadRDF4JRepositoryTest1.class.getSimpleName());

    // --- Static Methods -----------------------
    // Recursively delete a directory
    static boolean recursivelyDeleteDir(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                recursivelyDeleteDir(file);
            }
        }
        return dir.delete();
    }

    // --- Data Members -------------------------
    String[] createNativeRepoArgs,
            createNativeRepoOverwriteArgs,
            createLuceneRepoArgs,
            createLuceneRepoOverwriteArgs,
            loadDirNativeRepoArgs,
            loadDirLuceneRepoArgs,
            queryNativeRepoArgs,
            queryLuceneRepoArgs;
    String createNativeRepoArgsLine,
            loadDirNativeRepoArgsLine,
            queryNativeRepoArgsLine;
    final String NATIVE_REPO_BASE_REL_DIR = "src/test/resources/NativeRepos",
            LUCENE_REPO_BASE_REL_DIR = "src/test/resources/LuceneRepos",
            RDF_DATA_FILES_REL_DIR = "src/test/resources/RDFDataFiles";
    final String NATIVE_REPO_SERVER_REL_DIR = NATIVE_REPO_BASE_REL_DIR + "/server",
            LUCENE_REPO_SERVER_REL_DIR = LUCENE_REPO_BASE_REL_DIR + "/server";
    String NATIVE_REPO_DIR_ABS, LUCENE_REPO_DIR_ABS, RDF_DATA_FILES_ABS_DIR;
    String NATIVE_SCALABILITY_10K_REPO_ABS, LUCENE_SCALABILITY_10K_REPO_ABS;

    public void setupAll() {
        logger.info("Setup experiment");
        // Create an arguments array for creating a Native repo (no overwrite)
        createNativeRepoArgsLine
                = "createman " // choose operation mode for RepoUtil class
                + NATIVE_REPO_SERVER_REL_DIR + " "// folder where maneger will create repos
                + "scalability_10K " // repo name
                + "false " // overwrite repo if it exists?
                + "false " // enable Lucene Sail spatial indexing?
                + "spoc,posc " // repo standard index schemes
                // spoc = Subject Predicate Object Context
                // posc = Predicate Object Subject Context
                + "http://www.opengis.net/ont/geosparql#asWKT"; // Lucene spatial index predicate
        createNativeRepoArgs = createNativeRepoArgsLine.split(" ");
        // Create an arguments array for creating/overwrite a Native repo
        createNativeRepoOverwriteArgs = createNativeRepoArgs.clone();
        createNativeRepoOverwriteArgs[3] = "true"; // enable overwrite
        // Create an arguments array for creating a Lucene repo (no overwrite)
        createLuceneRepoArgs = createNativeRepoArgs.clone();
        createLuceneRepoArgs[1] = LUCENE_REPO_SERVER_REL_DIR;
        createLuceneRepoArgs[4] = "true"; // enable Lucene Sail with spatial indexing
        // Create an arguments array for creating/overwrite a Lucene repo
        createLuceneRepoOverwriteArgs = createLuceneRepoArgs.clone();
        createLuceneRepoOverwriteArgs[3] = "true"; // enable overwrite
        /* Find the absolute paths of the Native and Lucene Repo base directories 
           in the test resources folder */
        NATIVE_REPO_DIR_ABS
                = new File(NATIVE_REPO_SERVER_REL_DIR).getAbsolutePath();
        LUCENE_REPO_DIR_ABS
                = new File(LUCENE_REPO_SERVER_REL_DIR).getAbsolutePath();
        /* Find the absolute path of the RDFDataFiles directory
           in the test resources folder */
        RDF_DATA_FILES_ABS_DIR
                = new File(RDF_DATA_FILES_REL_DIR).getAbsolutePath();
        /* Find the absolute paths of the Native and Lucene <scalability_10K> Repo 
           directories in the test resources folder */
        NATIVE_SCALABILITY_10K_REPO_ABS
                = new File(NATIVE_REPO_SERVER_REL_DIR
                        + "/repositories/scalability_10K").getAbsolutePath();
        LUCENE_SCALABILITY_10K_REPO_ABS
                = new File(LUCENE_REPO_SERVER_REL_DIR
                        + "/repositories/scalability_10K").getAbsolutePath();
        // create the Native Repo base directory if it doesn't exist
        File nativeRepoBaseDir = new File(NATIVE_REPO_DIR_ABS);
        if (!nativeRepoBaseDir.exists()) {
            nativeRepoBaseDir.mkdirs();
        }
        // create the Lucene Repo base directory if it doesn't exist
        File luceneRepoBaseDir = new File(LUCENE_REPO_DIR_ABS);
        if (!luceneRepoBaseDir.exists()) {
            luceneRepoBaseDir.mkdirs();
        }

        // Create an arguments array for loading to an existing Native repo
        loadDirNativeRepoArgsLine
                = "dirloadman " // choose operation mode for RepoUtil class
                + NATIVE_REPO_SERVER_REL_DIR + " "// folder where maneger will create repos
                + "scalability_10K " // repo name
                + "N-Triples " // RDF format of data to load
                + RDF_DATA_FILES_ABS_DIR + " " // RDF data files directory
                + "true"; // display/print progress
        loadDirNativeRepoArgs = loadDirNativeRepoArgsLine.split(" ");
        // Create an arguments array for loading to an existing Lucene repo
        loadDirLuceneRepoArgs = loadDirNativeRepoArgs.clone();
        loadDirLuceneRepoArgs[1] = LUCENE_REPO_SERVER_REL_DIR;

        // Create an arguments array for querying an existing Native repo
        queryNativeRepoArgsLine
                = "queryman " // choose operation mode for RepoUtil class
                + NATIVE_REPO_SERVER_REL_DIR + " "// folder where maneger will create repos
                + "scalability_10K " // repo name
                + "1"; // number [1..5] of the query from the set of 5 preloaded
        queryNativeRepoArgs = queryNativeRepoArgsLine.split(" ");
        // Create an arguments array for querying an existing Lucene repo
        queryLuceneRepoArgs = queryNativeRepoArgs.clone();
        queryLuceneRepoArgs[1] = LUCENE_REPO_SERVER_REL_DIR;
    }

    public void shouldCreateANativeRepo() throws Exception {
        logger.info("EXPERIMENT: Should Create A Native Repo");
        /* trying to create the same repo in the same location using overwrite=true
           should logically re-create the repo
         */
        logger.info("Create the repo with overwrite=true.");
        try {
            RepoUtil.main(createNativeRepoOverwriteArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void shouldLoadToANativeRepo() throws Exception {
        logger.info("EXPERIMENT: Should Load To A Native Repo");
        // trying to load RDF data to an existing Native repo
        logger.info("Loading N-Triple data to an existing Native repo");
        try {
            RepoUtil.main(loadDirNativeRepoArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void shouldQueryANativeRepo(String msg) throws Exception {
        logger.info(msg);
        // trying to query a Native repo for number of triples
        logger.info("Querying a Native repo for the number of triples");
        try {
            RepoUtil.main(queryNativeRepoArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void shouldCreateALuceneRepo() throws Exception {
        logger.info("EXPERIMENT: Should Create A Lucene Repo");
        /* trying to create the same repo in the same location using overwrite=true
           should logically re-create the repo
         */
        logger.info("Create the repo with overwrite=true.");
        try {
            RepoUtil.main(createLuceneRepoOverwriteArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void shouldLoadToALuceneRepo() throws Exception {
        logger.info("EXPERIMENT: Should Load To A Lucene Repo");
        // trying to load RDF data to an existing Lucene repo
        logger.info("Loading N-Triple data to an existing Lucene repo");
        try {
            RepoUtil.main(loadDirLuceneRepoArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void shouldQueryALuceneRepo(String msg) throws Exception {
        logger.info(msg);
        // trying to query a Lucene repo for number of triples
        logger.info("Querying a Lucene repo for the number of triples");
        try {
            RepoUtil.main(queryLuceneRepoArgs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void tearDownAll() {
        logger.info("Remove all repositories");
        // wait for fsync in repository operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        // delete the Native Repo base directory if it exists
        File nativeRepoBaseDir = new File(NATIVE_REPO_BASE_REL_DIR);
        if (nativeRepoBaseDir.exists()) {
            try {
                PathUtils.deleteDirectory(nativeRepoBaseDir.toPath());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        // wait for fsync in repository operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        // delete the Lucene Repo base directory if it exists
        File luceneRepoBaseDir = new File(LUCENE_REPO_BASE_REL_DIR);
        if (luceneRepoBaseDir.exists()) {
            try {
                PathUtils.deleteDirectory(luceneRepoBaseDir.toPath());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        LoadRDF4JRepositoryTest1 test1 = new LoadRDF4JRepositoryTest1();
        // make sure all Native and Lucene test repos are deleted
        test1.tearDownAll();
        test1.setupAll();
        // create 1 Native scalability_10K repo
        test1.shouldCreateANativeRepo();
        // query the empty Native scalability_10K repo
        test1.shouldQueryANativeRepo("EXPERIMENT: Query An Empty Native Repo");
        // load Scalability 10K data into Native scalability_10K repo
        test1.shouldLoadToANativeRepo();
        // query Scalability 10K data from the loaded Native scalability_10K repo
        test1.shouldQueryANativeRepo("EXPERIMENT: Query A Loaded Native Repo");
        // create 1 Lucene scalability_10K repo
        test1.shouldCreateALuceneRepo();
        // query the empty Lucene scalability_10K repo
        test1.shouldQueryALuceneRepo("EXPERIMENT: Query An Empty Lucene Repo");
        // load Scalability 10K data into Lucene scalability_10K repo
        test1.shouldLoadToALuceneRepo();
        // query Scalability 10K data from the loaded Lucene scalability_10K repo
        test1.shouldQueryALuceneRepo("EXPERIMENT: Query A Loaded Lucene Repo");
        // make sure all Native and Lucene test repos are deleted
        test1.tearDownAll();
    }
}
