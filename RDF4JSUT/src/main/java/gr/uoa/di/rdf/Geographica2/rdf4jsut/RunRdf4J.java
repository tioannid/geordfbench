/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uoa.di.rdf.Geographica2.rdf4jsut;

import gr.uoa.di.rdf.Geographica2.systemsundertest.RunSystemUnderTest;

import org.apache.log4j.Logger;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class RunRdf4J extends RunSystemUnderTest {

    private static final Logger logger = Logger.getLogger(RunRdf4J.class.getSimpleName());

    @Override
    protected void addOptions() {
        super.addOptions();

        options.addOption("bd", "basedir", true, "BaseDir");
        options.addOption("rp", "repository", true, "RepositoryID");
        options.addOption("cr", "create", true, "CreateRepository");
        options.addOption("lc", "haslucene", true, "HasLucene");
        options.addOption("idx", "indexes", true, "Indexes");
        options.addOption("wktidx", "wktindexes", true, "WKT Index List");
        options.addOption("dr", "displres", true, "Display Results");
    }

    @Override
    protected void logOptions() {
        super.logOptions();

        logger.info("Excluded options");
        logger.info("BaseDir:\t" + cmd.getOptionValue("basedir"));
        logger.info("RepositoryID:\t" + cmd.getOptionValue("repository"));
        logger.info("CreateRepository:\t" + cmd.getOptionValue("create"));
        logger.info("HasLucene:\t" + cmd.getOptionValue("haslucene"));
        logger.info("Indexes:\t" + cmd.getOptionValue("indexes"));
        logger.info("WKTindexes:\t" + cmd.getOptionValue("wktindexes"));
        logger.info("Displres:\t" + cmd.getOptionValue("displres"));
        
    }
    
    @Override
    protected void initSystemUnderTest() throws Exception {
        String basedir = (cmd.getOptionValue("basedir") != null ? cmd.getOptionValue("basedir") : "");
        String repository = cmd.getOptionValue("repository");
        boolean create = Boolean.parseBoolean(cmd.getOptionValue("create"));
        boolean haslucene = Boolean.parseBoolean(cmd.getOptionValue("haslucene"));
        String indexes = cmd.getOptionValue("indexes");
        String wktindexes = cmd.getOptionValue("wktindexes");
        int displres = Integer.parseInt(cmd.getOptionValue("displres"));
        sut = new Rdf4jSUT(basedir, repository, create, haslucene, indexes, wktindexes, displres);
    }

    public static void main(String[] args) throws Exception  {
        RunSystemUnderTest runSUT = new RunRdf4J();
        runSUT.run(args);
    }
}