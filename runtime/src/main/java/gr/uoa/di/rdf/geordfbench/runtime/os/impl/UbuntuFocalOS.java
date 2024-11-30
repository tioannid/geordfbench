/**
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 
 * @updatedate 15/10/2024
 */
package gr.uoa.di.rdf.geordfbench.runtime.os.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.log4j.Logger;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class UbuntuFocalOS extends GenericLinuxOS {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(UbuntuFocalOS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    // --- Constructors ------------------------------
    // 1. Autonomous constructor:
    //      all data members receive user-provided,
    //      properly instantiated objects, no use for in-class utilities.
    //      Will be used if an Ubuntu OS has different initialization values
    //      than the ones used in the partial constructor below.
    public UbuntuFocalOS(String name, String shell_cmd, String sync_cmd, String clearcache_cmd) {
        super(name, shell_cmd, sync_cmd, clearcache_cmd);
    }

    // 2. Partial constructor:
    //      a default Ubuntu OS is created.
    //      All other data members receive default values.
    //      This is the constructor that should be used in most situations!
    public UbuntuFocalOS() {
        this("Ubuntu-focal", "/bin/sh", "sync", "sudo /sbin/sysctl vm.drop_caches=3");
    }

}
