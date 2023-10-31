/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
package gr.uoa.di.rdf.Geographica3.runtime.os.impl;

import org.apache.log4j.Logger;

public class UbuntuBionicOS extends GenericLinuxOS {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(UbuntuBionicOS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    // --- Constructors ------------------------------
    // 1. Autonomous constructor:
    //      all data members receive user-provided,
    //      properly instantiated objects, no use for in-class utilities.
    //      Will be used if an Ubuntu OS has different initialization values
    //      than the ones used in the partial constructor below.
    public UbuntuBionicOS(String name, String shell_cmd, String sync_cmd, String clearcache_cmd) {
        super(name, shell_cmd, sync_cmd, clearcache_cmd);
    }

    // 2. Partial constructor:
    //      a default Ubuntu OS is created.
    //      All other data members receive default values.
    //      This is the constructor that should be used in most situations!
    public UbuntuBionicOS() {
        this("Ubuntu-bionic", "/bin/sh", "sync", "sudo /sbin/sysctl vm.drop_caches=3");
    }

}
