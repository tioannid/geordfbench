package gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import gr.uoa.di.rdf.geordfbench.runtime.querysets.simple.IQuery;

/**
 *
 * @author GeoRDFBench Creator <GeoRDFBench@Creator>
 */
public class MacroComputeStatisticsQS extends MacroQS {

    // --- Static members -----------------------------
    Random rnd = new Random(0);
    static List<String> municipalities;
    static int qryExecuting = 0;

    static {
        logger = Logger.getLogger(MacroComputeStatisticsQS.class.getSimpleName());
        String timestampsFile = "municipalities.txt";
        municipalities = new ArrayList<String>();

        InputStream is = MacroComputeStatisticsQS.class.getResourceAsStream("/" + timestampsFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String givenTimestamp;
        try {
            while ((givenTimestamp = in.readLine()) != null) {
                municipalities.add(givenTimestamp);
            }
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MacroComputeStatisticsQS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- Data members ------------------------------
    String municipalityWKTTemplateName;
    @JsonIgnore
    String municipalityName = null;
    @JsonIgnore
    String municipalityWKT = null;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public MacroComputeStatisticsQS(String name, String relativeBaseDir,
            boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            String municipalityWKTTemplateName) {
        super(name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.municipalityWKTTemplateName = municipalityWKTTemplateName;
    }

    public MacroComputeStatisticsQS() {
    }

    // --- Data Accessors ----------------------------
    public String getMunicipalityWKTTemplateName() {
        return municipalityWKTTemplateName;
    }

    public void setMunicipalityWKTTemplateName(String municipalityWKTTemplateName) {
        this.municipalityWKTTemplateName = municipalityWKTTemplateName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName.split("\t")[0];
        // remove the double quotes that surround the timestamp
        this.municipalityName = this.municipalityName.replace("\"", "");
        this.municipalityWKT = municipalityName.split("\t")[1];
        // remove the double quotes that surround the polygon
        this.municipalityWKT = this.municipalityWKT.replace("\"", "");
    }

    // --- Methods ----------------------------    
    @Override
    public IQuery getQuery(int position) {
        // retrieve the query from the map
        IQuery qs = super.getQuery(position);
        // replace the template parameter for the municipalityWKT for all queries
        if (this.municipalityWKT != null) {
            qs.setText(qs.getText().replace(this.municipalityWKTTemplateName, this.municipalityWKT));
        }
        qryExecuting = position;
        return qs;
    }

    @Override
    public void initializeQuerysetIteration() {
        super.initializeQuerysetIteration();
        // retrieve a random timestamp
        String timestamp = municipalities.get(rnd.nextInt(municipalities.size()));
        // update both data members with one setter operation
        this.setMunicipalityName(timestamp);
    }
}
