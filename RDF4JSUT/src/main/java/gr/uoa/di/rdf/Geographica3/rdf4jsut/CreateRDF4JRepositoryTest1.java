package gr.uoa.di.rdf.Geographica3.rdf4jsut;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.apache.commons.io.file.PathUtils;
import org.apache.log4j.Logger;

/**
 * A class that checks the following use cases: 1) Native Sail a) Create new
 * repository only if repository does not exist b) Overwrite existing repository
 * 2) Lucene Sail a) Create new repository only i;f repository does not exist b)
 * Overwrite existing repository
 *
 * IMPORTANT: 1) This class is equivalent to the similarly named class in the
 * test packages. Since Lucene v7.x has a problem running with JUnit, the Lucene
 * related tests fail, however, this class proves that the tested code actually
 * runs properly during regular execution. 2) It is expected that when the RDF4J
 * dependencies of the Runtime module are updated the problem described in point
 * 1 will be removed and this class will not longer be required.
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 09/09/2024
 * @updatedate 18/09/2024
 */
public class CreateRDF4JRepositoryTest1 {

    // --- Static Members -----------------------
    public static Logger logger = Logger.getLogger(CreateRDF4JRepositoryTest1.class.getSimpleName());

    // --- Data Members -------------------------
    String[] createNativeRepoArgs,
            createNativeRepoOverwriteArgs,
            createLuceneRepoArgs,
            createLuceneRepoOverwriteArgs;
    String createNativeRepoArgsLine;
    final String NATIVE_REPO_BASE_REL_DIR = "src/test/resources/NativeRepos",
            LUCENE_REPO_BASE_REL_DIR = "src/test/resources/LuceneRepos";
    final String NATIVE_REPO_SERVER_REL_DIR = NATIVE_REPO_BASE_REL_DIR + "/server",
            LUCENE_REPO_SERVER_REL_DIR = LUCENE_REPO_BASE_REL_DIR + "/server";
    String NATIVE_REPO_DIR_ABS, LUCENE_REPO_DIR_ABS;
    String NATIVE_SCALABILITY_10K_REPO_ABS, LUCENE_SCALABILITY_10K_REPO_ABS;

    public void setupAll() {
        logger.info("BEFORE ALL EXPERIMENTS");
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
    }

    public void shouldNotCreateANativeRepoWhenRepoExists() {
        logger.info("EXPERIMENT: Should Not Create A Native Repo When Repo Exists");
        FileTime fileTimeBefore = FileTime.from(Instant.now());
        FileTime fileTimeAfter = FileTime.fromMillis(fileTimeBefore.toMillis() + 1);
        BasicFileAttributes attr = null;
        /* find the absolute path of the Native <scalability_10K>/config.ttl in the test 
           resources folder */
        File nativeScal10KRepo = new File(NATIVE_SCALABILITY_10K_REPO_ABS + "/config.ttl");
        Path nativeScal10KRepoPath = nativeScal10KRepo.toPath();
        // a repo should be created before testing
        logger.info("=> Precondition: A repo should exist before testing. Checking...");
        if (!nativeScal10KRepo.exists()) {
            // create the repo using overwrite=false
            logger.info("No repo existed. Will create one now.");
            try {
                RepoUtil.main(createNativeRepoArgs);
                attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        } else {
            logger.info("Repo already exists. No need to create one.");
            try {
                attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        }

        // create a delay of 1 second
        logger.info("=> Precondition: Thread sleeping for 1 sec.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        }

        /* trying to create the same repo in the same location using overwrite=false
           should logically ignore the request
         */
        logger.info("=> Action: Create the same repo in the same location with overwrite=false.");
        try {
            RepoUtil.main(createNativeRepoArgs);
            attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
            fileTimeAfter = attr.creationTime();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        logger.info("=> Expected result: Repo should not be overwritten, timestaps before and after should match.");
        logger.info("=> Actual result:\n"
                + "\t\tInitial repository timestamp = " + fileTimeBefore.toString() + "\n"
                + "\t\tRepository timestamp after attempting to create it (no overwrite) = " + fileTimeAfter.toString());
    }

    public void shouldOverwriteAnExistingNativeRepo() throws Exception {
        logger.info("EXPERIMENT: Should Overwrite An Existing Native Repo");
        FileTime fileTimeBefore = FileTime.from(Instant.now());
        FileTime fileTimeAfter = FileTime.fromMillis(fileTimeBefore.toMillis() + 1);
        BasicFileAttributes attr = null;
        /* find the absolute path of the Native <scalability_10K>/config.ttl in the test 
           resources folder */
        File nativeScal10KRepo = new File(NATIVE_SCALABILITY_10K_REPO_ABS + "/config.ttl");
        Path nativeScal10KRepoPath = nativeScal10KRepo.toPath();
        // a repo should be created before testing
        logger.info("=> Precondition: A repo should exist before testing. Checking...");
        if (!nativeScal10KRepo.exists()) {
            // create the repo using overwrite=false
            logger.info("No repo existed. Will create one now.");
            try {
                RepoUtil.main(createNativeRepoArgs);
                attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        } else {
            logger.info("Repo already exists. No need to create one.");
            try {
                attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        }

        // create a delay of 1 second
        logger.info("=> Precondition: Thread sleeping for 1 sec.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        }

        /* trying to create the same repo in the same location using overwrite=true
           should logically re-create the repo
         */
        logger.info("=> Action: Create the same repo in the same location with overwrite=true.");
        try {
            RepoUtil.main(createNativeRepoOverwriteArgs);
            attr = Files.readAttributes(nativeScal10KRepoPath, BasicFileAttributes.class);
            fileTimeAfter = attr.creationTime();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        logger.info("=> Expected result: Repo should be overwritten, after timestap should be greater than before timestamp.");
        logger.info("=> Actual result:\n"
                + "\t\tInitial repository timestamp = " + fileTimeBefore.toString() + "\n"
                + "\t\tOverwritten repository timestamp = " + fileTimeAfter.toString());
    }

    public void shouldNotCreateALuceneRepoWhenRepoExists() throws Exception {
        logger.info("EXPERIMENT: Should Not Create A Lucene Repo When Repo Exists");
        FileTime fileTimeBefore = FileTime.from(Instant.now());
        FileTime fileTimeAfter = FileTime.fromMillis(fileTimeBefore.toMillis() + 1);
        BasicFileAttributes attr = null;
        /* find the absolute path of the Lucene <scalability_10K>/config.ttl in the test 
           resources folder */
        File luceneScal10KRepo = new File(LUCENE_SCALABILITY_10K_REPO_ABS + "/config.ttl");
        Path luceneScal10KRepoPath = luceneScal10KRepo.toPath();
        // a repo should be created before testing
        logger.info("=> Precondition: A repo should exist before testing. Checking...");
        if (!luceneScal10KRepo.exists()) {
            // create the repo using overwrite=false
            logger.info("No repo existed. Will create one now.");
            try {
                RepoUtil.main(createLuceneRepoArgs);
                attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (Throwable e) {
                logger.info(e.getMessage());
            }
        } else {
            logger.info("Repo already exists. No need to create one.");
            try {
                attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        }

        // create a delay of 1 second
        logger.info("=> Precondition: Thread sleeping for 1 sec.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        }

        /* trying to create the same repo in the same location using overwrite=false
           should logically ignore the request
         */
        logger.info("=> Action: Create the same repo in the same location with overwrite=false.");
        try {
            RepoUtil.main(createLuceneRepoArgs);
            attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
            fileTimeAfter = attr.creationTime();
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
        logger.info("=> Expected result: Repo should not be overwritten, timestaps before and after should match.");
        logger.info("=> Actual result:\n"
                + "\t\tInitial repository timestamp = " + fileTimeBefore.toString() + "\n"
                + "\t\tRepository timestamp after attempting to create it (no overwrite) = " + fileTimeAfter.toString());
    }

    public void shouldOverwriteAnExistingLuceneRepo() throws Exception {
        logger.info("EXPERIMENT: Should Overwrite An Existing Lucene Repo");
        FileTime fileTimeBefore = FileTime.from(Instant.now());
        FileTime fileTimeAfter = FileTime.fromMillis(fileTimeBefore.toMillis() + 1);
        BasicFileAttributes attr = null;
        /* find the absolute path of the Lucene <scalability_10K>/config.ttl in the test 
           resources folder */
        File luceneScal10KRepo = new File(LUCENE_SCALABILITY_10K_REPO_ABS + "/config.ttl");
        Path luceneScal10KRepoPath = luceneScal10KRepo.toPath();
        // a repo should be created before testing
        logger.info("=> Precondition: A repo should exist before testing. Checking...");
        if (!luceneScal10KRepo.exists()) {
            // create the repo using overwrite=false
            logger.info("No repo existed. Will create one now.");
            try {
                RepoUtil.main(createLuceneRepoArgs);
                attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (Throwable e) {
                logger.info(e.getMessage());
            }
        } else {
            logger.info("Repo already exists. No need to create one.");
            try {
                attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
                fileTimeBefore = attr.creationTime();
            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
        }

        // create a delay of 1 second
        logger.info("=> Precondition: Thread sleeping for 1 sec.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        }

        /* trying to create the same repo in the same location using overwrite=true
           should logically re-create the repo
         */
        logger.info("=> Action: Create the same repo in the same location with overwrite=true.");
        try {
            RepoUtil.main(createLuceneRepoOverwriteArgs);
            attr = Files.readAttributes(luceneScal10KRepoPath, BasicFileAttributes.class);
            fileTimeAfter = attr.creationTime();
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
        logger.info("=> Expected result: Repo should be overwritten, after timestap should be greater than before timestamp.");
        logger.info("=> Actual result:\n"
                + "\t\tInitial repository timestamp = " + fileTimeBefore.toString() + "\n"
                + "\t\tOverwritten repository timestamp = " + fileTimeAfter.toString());
    }

    public void tearDownAll() {
        logger.info("AFTER ALL EXPERIMENTS");
        // wait for fsync in repository operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        // delete the Native Repo base directory if it exists
        File nativeRepoBaseDir = new File(NATIVE_REPO_BASE_REL_DIR);
        try {
            PathUtils.deleteDirectory(nativeRepoBaseDir.toPath());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        // wait for fsync in repository operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        // delete the Lucene Repo base directory if it exists
        File luceneRepoBaseDir = new File(LUCENE_REPO_BASE_REL_DIR);
        try {
            PathUtils.deleteDirectory(luceneRepoBaseDir.toPath());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) throws Exception {
        CreateRDF4JRepositoryTest1 test1 = new CreateRDF4JRepositoryTest1();
        test1.setupAll();
        test1.shouldOverwriteAnExistingNativeRepo();
        test1.shouldNotCreateANativeRepoWhenRepoExists();
        test1.shouldOverwriteAnExistingLuceneRepo();
        test1.shouldNotCreateALuceneRepoWhenRepoExists();
        test1.tearDownAll();

    }
}
