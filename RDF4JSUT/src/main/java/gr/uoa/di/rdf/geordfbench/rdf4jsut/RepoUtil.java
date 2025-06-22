/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.geordfbench.rdf4jsut;

import static gr.uoa.di.rdf.geordfbench.runtime.hosts.IHost.SEP;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @description Perform all actions for the RDF4J repositories
 * @syntax 1: RepoUtil <create> <repodir> {indexes} 2: RepoUtil <createman>
 * 2: RepoUtil <load> <repodir> <RDFFormatString> <file>
 * 3: RepoUtil <dirload> <repodir> <trigfiledir> <printflag>
 * 
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 08/05/2018
 * @updatedate 27/09/2024
 */
public class RepoUtil {

    static Logger logger = Logger.getLogger(RepoUtil.class.getSimpleName());

    static String repoDir;
    static String indexes;
    static String wktIdxList;
    static String baseDir;
    static String repositoryId;
    static boolean reCreateRepo;
    static boolean hasLucene;
    static int queryNo;
    static String rdfFormat;
    static String fileDir;
    static boolean displayProgress;
    static RDF4JSystem rdf4j = null;

    public static void main(String[] args) {
//        if (args[0].equalsIgnoreCase("create")) { // create a standalone repo with indexes
//            repoDir = args[1];
//            indexes = (args.length == 2) ? "" : args[2];
//            System.out.println("RDF4J created repo \"" + repoDir + "\" in "
//                    + Rdf4jSUT.RDF4J.createNativeRepo(repoDir, indexes) + " msecs");
//        } else

        if (args[0].equalsIgnoreCase("createman")) { // create a repo with Id using repo manager with indexes and lucene index and overwriting existing repo
            baseDir = args[1];
            repositoryId = args[2];
            reCreateRepo = Boolean.parseBoolean(args[3]);
            hasLucene = Boolean.parseBoolean(args[4]);
            indexes = args[5];
            wktIdxList = args[6];
            Map<String, String> sysMap = RDF4JSystem.constructSysPropsMap(baseDir, repositoryId, indexes, reCreateRepo, hasLucene, wktIdxList);
            try {
                rdf4j = new RDF4JSystem(sysMap);
                logger.info("RDF4J created with manager " + ((hasLucene) ? "lucene " : "") + "repo \"" + baseDir + "/repositories/".replace("/", SEP) + repositoryId + "\" in "
                        + RDF4JSystem.lastMeasuredOperation + " msecs");
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                if (rdf4j != null) {
                    rdf4j.close();
                }
            }
//        } else if (args[0].equalsIgnoreCase("query")) { // query from templates against standalone repo
//            baseDir = args[1];
//            queryNo = Integer.parseInt(args[2]);
//            System.out.println("RDF4J template-query " + queryNo + " against repo \"" + baseDir + "\" in "
//                    + Rdf4jSUT.RDF4J.templateQueryInNativeRepo(baseDir, queryNo) + " msecs");
//        } else if (args[0].equalsIgnoreCase("queryman")) { // query from templates against repo through repo manager
//            baseDir = args[1];
//            repositoryId = args[2];
//            queryNo = Integer.parseInt(args[3]);
//            System.out.println("RDF4J template-query " + queryNo + " through manager in \"" + baseDir + "\" against repo \"" + repositoryId + "\" in "
//                    + Rdf4jSUT.RDF4J.templateQueryNativeRepoWithManager(baseDir, repositoryId, queryNo) + " msecs");
//        } else if (args[0].equalsIgnoreCase("load")) {
//            System.out.println("RDF4J loaded fileloc \"" + args[3] + "\" in repo \"" + args[1] + "\" in "
//                    + Rdf4jSUT.RDF4J.loadInNativeRepo(args[1], args[2], args[3]) + " msecs");
//        } else if (args[0].equalsIgnoreCase("loadman")) {
//            System.out.println("RDF4J loaded with manager file \"" + args[2] + "\" in repo \"" + args[1] + "\" in "
//                    + Rdf4jSUT.RDF4J.loadInNativeRepoWithManager(args[1], args[2], args[3], args[4]) + " msecs");
//        } else if (args[0].equalsIgnoreCase("dirload")) {
//            System.out.println("RDF4J loaded all files from \"" + args[3] + "\" in repo \"" + args[1] + "\" in "
//                    + Rdf4jSUT.RDF4J.loadDirInNativeRepo(args[1], args[2], args[3], Boolean.parseBoolean(args[4])) + " msecs");
        } else if (args[0].equalsIgnoreCase("dirloadman")) {
            baseDir = args[1];
            repositoryId = args[2];
            rdfFormat = args[3];
            fileDir = args[4];
            displayProgress = Boolean.parseBoolean(args[5]);

            // create an RDF4JSystem from an existing repo that has just been
            // created by "createman" option above!
            try {
                rdf4j = new RDF4JSystem(baseDir, repositoryId);
                // load 
                RDF4JSystem.lastMeasuredOperation = rdf4j.loadDirInNativeRepoWithManager(fileDir, rdfFormat, displayProgress);
                logger.info("RDF4J loaded with manager all files from \"" + fileDir + "\" to repo \"" + baseDir + "/repositories/".replace("/", SEP) + repositoryId + "\" in "
                        + RDF4JSystem.lastMeasuredOperation + " msecs");
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                if (rdf4j != null) {
                    rdf4j.close();
                }
            }
        } else if (args[0].equalsIgnoreCase("queryman")) { // query from templates against repo through repo manager
            baseDir = args[1];
            repositoryId = args[2];
            queryNo = Integer.parseInt(args[3]);

            // create an RDF4JSystem from an existing repo that has just been
            // created by "createman" option above!
            try {
                rdf4j = new RDF4JSystem(baseDir, repositoryId);
                System.out.println("RDF4J run template-query " + queryNo + " through manager in \"" + baseDir + "\" against repo \"" + repositoryId + "\" in "
                        + rdf4j.templateQueryNativeRepoWithManager(baseDir, repositoryId, queryNo) + " msecs");
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                if (rdf4j != null) {
                    rdf4j.close();
                }
            }
        }
    }
}
