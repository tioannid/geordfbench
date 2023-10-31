/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.rdf4jsut;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 * @since 08/05/2018
 * @description Perform all actions for the RDF4J repositories
 * @syntax 1: RepoUtil <create> <repodir> {indexes}
 *         2: RepoUtil <createman> 
 * 2: RepoUtil <load> <repodir> <RDFFormatString> <file>
 * 3: RepoUtil <dirload> <repodir> <trigfiledir> <printflag>
 */
public abstract class RepoUtil {

    static String repoDir;
    static String indexes;
    static String wktIdxList;
    static String baseDirString;
    static String repoId;
    static boolean removeExistingRepo;
    static boolean hasLucene;
    static int queryNo;
    static String rdfFormat;
    static String fileDir;
    static boolean displayProgress;
    public static void main(String[] args) throws Exception {
        if (args[0].equalsIgnoreCase("create")) { // create a standalone repo with indexes
            repoDir = args[1];
            indexes = (args.length == 2) ? "" : args[2];
            System.out.println("RDF4J created repo \"" + repoDir + "\" in " + 
                    Rdf4jSUT.RDF4J.createNativeRepo(repoDir, indexes) + " msecs");
        } else if (args[0].equalsIgnoreCase("createman")) { // create a repo with Id using repo manager with indexes and lucene index and overwriting existing repo
            baseDirString = args[1];
            repoId = args[2];
            removeExistingRepo = Boolean.parseBoolean(args[3]);
            hasLucene=Boolean.parseBoolean(args[4]);
            indexes = args[5];
            wktIdxList = args[6];
            System.out.println("RDF4J created with manager lucene repo \"" + baseDirString + "/repositories/" + repoId + "\" in " + 
                    Rdf4jSUT.RDF4J.createNativeRepoWithManager(baseDirString, repoId, removeExistingRepo, hasLucene, indexes, wktIdxList) + " msecs");
        } else if (args[0].equalsIgnoreCase("query")) { // query from templates against standalone repo
            baseDirString = args[1];
            queryNo = Integer.parseInt(args[2]);
            System.out.println("RDF4J template-query " + queryNo + " against repo \"" + baseDirString + "\" in " + 
                    Rdf4jSUT.RDF4J.templateQueryInNativeRepo(baseDirString, queryNo) + " msecs");
        } else if (args[0].equalsIgnoreCase("queryman")) { // query from templates against repo through repo manager
            baseDirString = args[1];
            repoId = args[2];
            queryNo = Integer.parseInt(args[3]);
            System.out.println("RDF4J template-query " + queryNo + " through manager in \"" + baseDirString + "\" against repo \"" + repoId + "\" in " + 
                    Rdf4jSUT.RDF4J.templateQueryNativeRepoWithManager(baseDirString, repoId, queryNo) + " msecs");
        } else if (args[0].equalsIgnoreCase("load")) {
            System.out.println("RDF4J loaded fileloc \"" + args[3] + "\" in repo \"" + args[1] + "\" in " + 
                    Rdf4jSUT.RDF4J.loadInNativeRepo(args[1], args[2], args[3]) + " msecs");
        } else if (args[0].equalsIgnoreCase("loadman")) {
            System.out.println("RDF4J loaded with manager file \"" + args[2] + "\" in repo \"" + args[1] + "\" in " + 
                    Rdf4jSUT.RDF4J.loadInNativeRepoWithManager(args[1], args[2], args[3], args[4]) + " msecs");
        } else if (args[0].equalsIgnoreCase("dirload")) {
            System.out.println("RDF4J loaded all files from \"" + args[3] + "\" in repo \"" + args[1] + "\" in " + 
                    Rdf4jSUT.RDF4J.loadDirInNativeRepo(args[1], args[2], args[3], Boolean.parseBoolean(args[4])) + " msecs");
        } else if (args[0].equalsIgnoreCase("dirloadman")) {
            baseDirString = args[1];
            repoId = args[2];
            rdfFormat = args[3];
            fileDir = args[4];
            displayProgress = Boolean.parseBoolean(args[5]);
            System.out.println("RDF4J loaded with manager all files from \"" + fileDir + "\" to repo \"" + baseDirString + "/repositories/" + repoId + "\" in " + 
                    Rdf4jSUT.RDF4J.loadDirInNativeRepoWithManager(baseDirString, repoId, rdfFormat, fileDir, displayProgress) + " msecs");
        }
    }
}
