/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.runtime.os.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.Geographica3.runtime.os.IOS;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class GenericLinuxOS implements IOS {

    // --- Static members -----------------------------
    static Logger logger = Logger.getLogger(GenericLinuxOS.class.getSimpleName());

    // --- Data members ------------------------------
    protected String name;  // Linux, Windows, etc
    protected String shell_cmd;
    protected String sync_cmd;
    protected String clearcache_cmd;

    // --- Constructors ------------------------------
    public GenericLinuxOS() {
    }

    public GenericLinuxOS(String name, String shell_cmd, String sync_cmd, String clearcache_cmd) {
        this.name = name;
        this.shell_cmd = shell_cmd;
        this.sync_cmd = sync_cmd;
        this.clearcache_cmd = clearcache_cmd;
    }

    // --- Data Accessors ----------------------------
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        GenericLinuxOS.logger = logger;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShell_cmd() {
        return shell_cmd;
    }

    public void setShell_cmd(String shell_cmd) {
        this.shell_cmd = shell_cmd;
    }

    public String getSync_cmd() {
        return sync_cmd;
    }

    public void setSync_cmd(String sync_cmd) {
        this.sync_cmd = sync_cmd;
    }

    public String getClearcache_cmd() {
        return clearcache_cmd;
    }

    public void setClearcache_cmd(String clearcache_cmd) {
        this.clearcache_cmd = clearcache_cmd;
    }

    // --- Methods -----------------------------------
    /**
     * Clears system caches and use the appropriate delay for it to take effect.
     *
     * @param delay_mSecs
     */
    @Override
    public void clearCaches(int delay_mSecs) {

        String[] sys_sync = {shell_cmd, "-c", sync_cmd};
        String[] clear_caches = {shell_cmd, "-c", clearcache_cmd};

        Process pr;

        try {
            // Synchronize cached writes to persistent storage
            logger.info("Syncing...");
            pr = Runtime.getRuntime().exec(sys_sync);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while system " + name + " was syncing");
            }

            logger.info("Clearing caches...");
            pr = Runtime.getRuntime().exec(clear_caches);
            pr.waitFor();
            if (pr.exitValue() != 0) {
                logger.error("Something went wrong while clearing caches of system " + name);
            }

            Thread.sleep(delay_mSecs);
            logger.info("Caches cleared after delay of " + delay_mSecs + " msecs");
        } catch (Exception e) {
            logger.fatal("Cannot clear caches");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.fatal(stacktrace);
        }
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{ "
                + this.name
                + " }";
    }
}
