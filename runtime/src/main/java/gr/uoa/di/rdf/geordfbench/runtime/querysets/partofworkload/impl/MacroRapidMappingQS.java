package gr.uoa.di.rdf.geordfbench.runtime.querysets.partofworkload.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.uoa.di.rdf.geordfbench.runtime.executionspecs.IExecutionSpec;
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
public class MacroRapidMappingQS extends MacroQS {

    // --- Static members -----------------------------
    Random rnd = new Random(0);
    static List<String> timestamps;
    static int qryExecuting = 0;

    static {
        logger = Logger.getLogger(MacroRapidMappingQS.class.getSimpleName());
        String timestampsFile = "timestamps.txt";
        timestamps = new ArrayList<String>();

        InputStream is = MacroRapidMappingQS.class.getResourceAsStream("/" + timestampsFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String givenTimestamp;
        try {
            while ((givenTimestamp = in.readLine()) != null) {
                timestamps.add(givenTimestamp);
            }
            in.close();
            in = null;
            is.close();
            is = null;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MacroRapidMappingQS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --- Data members ------------------------------
    String timestampTemplateName;
    String polygonTemplateName;
    @JsonIgnore
    String timestamp = null;
    @JsonIgnore
    String polygonWKT = null;

    // --- Constructors ------------------------------
    // 1. Partial constructor:
    //      The query list is deserialized from a JSON file placed
    //      in the resources folder or the classpath in general
    //      Queries are placed on a map property
    public MacroRapidMappingQS(IExecutionSpec executionSpec, String name, 
            String relativeBaseDir, boolean hasPredicateQueriesAlso,
            Map<Integer, IQuery> mapQueries,
            Map<String, String> mapUsefulNamespacePrefixes,
            String toponymTemplateName, String rectangleTemplateName) {
        super(executionSpec, name, relativeBaseDir, hasPredicateQueriesAlso,
                mapQueries, mapUsefulNamespacePrefixes);
        this.timestampTemplateName = toponymTemplateName;
        this.polygonTemplateName = rectangleTemplateName;
    }

    public MacroRapidMappingQS() {
    }

    // --- Data Accessors ----------------------------
    public String getTimestampTemplateName() {
        return timestampTemplateName;
    }

    public void setTimestampTemplateName(String timestampTemplateName) {
        this.timestampTemplateName = timestampTemplateName;
    }

    public String getPolygonTemplateName() {
        return polygonTemplateName;
    }

    public void setPolygonTemplateName(String polygonTemplateName) {
        this.polygonTemplateName = polygonTemplateName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp.split("\t")[0];
        // remove the double quotes that surround the timestamp
        this.timestamp = this.timestamp.replace("\"", "");
        this.polygonWKT = timestamp.split("\t")[1];
        // remove the double quotes that surround the polygon
        this.polygonWKT = this.polygonWKT.replace("\"", "");
    }

    // --- Methods ----------------------------    
    @Override
    public IQuery getQuery(int position) {
        // retrieve the query from the map
        IQuery qs = super.getQuery(position);
        // translate or construct a ground query only if in RUN mode
        if (this.getAction().equals(IExecutionSpec.Action.RUN)) {
            // replace the template parameter for the polygon for all queries
            qs.setText(qs.getText().replace(this.polygonTemplateName, this.polygonWKT));
            switch (position) {
                case 3:
                case 4: // replace the template parameter for the timestamp for 
                    // queries 3, 4
                    qs.setText(qs.getText().replace(this.timestampTemplateName,
                            this.timestamp));
                    break;
            }
        }
        qryExecuting = position;
        return qs;
    }

    @Override
    public void initializeQuerysetIteration() {
        super.initializeQuerysetIteration();
        // retrieve a random timestamp
        String timestamp = timestamps.get(rnd.nextInt(timestamps.size()));
        // update both data members with one setter operation
        this.setTimestamp(timestamp);
    }

}
