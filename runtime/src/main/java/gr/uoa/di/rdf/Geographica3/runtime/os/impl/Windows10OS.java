/**
 *
 * @author Theofilos Ioannidis <tioannid@di.uoa.gr>
 * @creationdate 13/10/2024
 * @updatedate 15/10/2024
 */
package gr.uoa.di.rdf.Geographica3.runtime.os.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.log4j.Logger;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
        property = "classname")
public class Windows10OS extends GenericWindowsOS {

    // --- Static members -----------------------------
    static {
        logger = Logger.getLogger(Windows10OS.class.getSimpleName());
    }

    // --- Data members ------------------------------
    // --- Constructors ------------------------------
    // 1. Autonomous constructor:
    //      all data members receive user-provided,
    //      properly instantiated objects, no use for in-class utilities.
    //      Will be used if an Windows 10 has different initialization values
    //      than the ones used in the partial constructor below.
    public Windows10OS(String name, String shell_cmd, String sync_cmd, String clearcache_cmd) {
        super(name, shell_cmd, sync_cmd, clearcache_cmd);
    }

    // 2. Partial constructor:
    //      a default Windows 10 is created.
    //      All other data members receive default values.
    //      This is the constructor that should be used in most situations!
    public Windows10OS() {
        this("Windows 10", "cmd.exe", "echo sync", "echo drop_caches");
    }

}
