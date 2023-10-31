
package gr.uoa.di.rdf.Geographica.generators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Generator {
	static BigDecimal X_MAX = new BigDecimal("10.24");
	static BigDecimal X_DELTA = new BigDecimal("0.01");
	//static double X_DELTA = 0.0024;//small grid= 0.01, medium=0.0034, big=0.001
	static BigDecimal Y_MAX = new BigDecimal("10.24");
	//static double Y_DELTA = 0.0024; //small grid= 0.01, medium=0.0034, big=0.001
	static BigDecimal Y_DELTA = new BigDecimal("0.01");

	//	// *_MAX and *_DELTA implicitly control the number of nodes created!
	//	// *_MAX and *_DELTA implicitly control the number of nodes created!
	//	static double X_MAX = 10.24;
	//	static double X_DELTA = 0.1;
	//	//static double X_DELTA = 0.0024;//small grid= 0.01, medium=0.0034, big=0.001
	//	static double Y_MAX = X_MAX;
	//	//static double Y_DELTA = 0.0024; //small grid= 0.01, medium=0.0034, big=0.001
	//	static double Y_DELTA = X_DELTA;

	static BigDecimal GEO_PRED_DELTA = new BigDecimal("0.00000001");

	static int MAX_TAG_VAL = 1024;

	static final String A_IRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	static final String NODE_IRI = "http://www.wktrdf.org/Node";
	static final String ID_IRI = "http://www.wktrdf.org/id";
	static final String HAS_GEO_IRI = "http://example.org/sparql/geo/hasGeography";
	static final String HAS_TAG_IRI = "http://www.wktrdf.org/hasTag";
	static final String TAG_IRI = "http://www.wktrdf.org/Tag";
	static final String KEY_IRI = "http://www.wktrdf.org/key";
	static final String VALUE_IRI = "http://www.wktrdf.org/value";
	static final String GEOGRAPHY_TYPE_IRI = "http://strdf.di.uoa.gr/ontology#WKT";

	private static int triples = 0;
	private static int tagId = 1;

	static int generate(int geoStmtsPerNode, BufferedWriter of, int geometryType) throws IOException

	// Parse the input and store it into temporary files
	{
		short x_steps = X_MAX.divide(X_DELTA).shortValue();
		short y_steps = Y_MAX.divide(Y_DELTA).shortValue();
		ArrayList<Integer> grid = new ArrayList<Integer>(x_steps*y_steps);

		for (short i = 0; i <= x_steps; i++) {
			for (short j = 0; j <= y_steps; j++) {
				if (i == 0 && j == 0) {
					//we will insert the 0,0 manually so that we will always 
					//get the same dictionary encoding for the predicates
					continue;
				}
				//shorts are 16bits
				//ints are 32bits
				int combined = i<<16; // shift
				combined=combined|j;  // inclusive or
				grid.add(combined);
			}
		}

		System.out.println("Shuffling "+ grid.size()+1 +" elements ...");

		int seed = 7;
		seed = 31 * seed + x_steps;
		seed = 31 * seed + y_steps;

		Random rnd = new Random(seed);
		Collections.shuffle(grid, rnd);
		System.out.println("Shuffling complete...");

		generateTriplesForNode(0,geoStmtsPerNode, of, geometryType);
		
		for (Integer value : grid) {
			generateTriplesForNode(value, geoStmtsPerNode, of, geometryType);

		}

		//		for (BigDecimal xCoord = new BigDecimal("0"); (xCoord.compareTo(X_MAX) <= 0); xCoord = xCoord.add(X_DELTA)) {
		//			for (BigDecimal yCoord = new BigDecimal("0"); (yCoord.compareTo(Y_MAX) <= 0); yCoord = yCoord.add(Y_DELTA)) {
		//				// Node's subject.
		//				sSub = new StringBuffer();
		//				sSub.append(NODE_IRI);
		//				sSub.append(nodeId++);
		//				subject.value = sSub.toString();
		//
		//				predicate.value = A_IRI;
		//				object.value = NODE_IRI;
		//				object.type = TripleEntry.EntryType.Iri;
		//
		//				// Triple: geordf:Node123 a geordf:Node
		//				process(subject, predicate, object, of);
		//				triples++;
		//
		//				predicate.value = ID_IRI;
		//
		//				sObj = new StringBuffer();
		//				sObj.append("(");
		//				sObj.append(xCoord); 
		//				sObj.append(" ");
		//				sObj.append(yCoord);
		//				sObj.append(")");
		//				object.value = sObj.toString();
		//				object.type = TripleEntry.EntryType.Literal;
		//
		//				// Triple: geordf:Node123 geordf:id "(0 0)"
		//				process(subject, predicate, object, of);triples++;
		//
		//				// construct geo-triples
		//				for (int currGeoStmt = 1; currGeoStmt <= geoStmtsPerNode; currGeoStmt++) {
		//					sPred = new StringBuffer();
		//					sPred.append(HAS_GEO_IRI);
		//					sPred.append(currGeoStmt);
		//					predicate.value = sPred.toString();
		//
		//					// Create a different geography for each geo statement (of each node and generally)
		//					sObj = new StringBuffer();
		//
		//					BigDecimal x,y;
		//					if (currGeoStmt != 1) {
		//						BigDecimal geoDelta = GEO_PRED_DELTA.multiply(new BigDecimal(currGeoStmt - 1));
		//						x = xCoord.add(geoDelta);
		//						y = yCoord.add(geoDelta);
		//					} else {
		//						x = xCoord;
		//						y = yCoord;
		//					}
		//
		//					sObj.append("POINT (");
		//					sObj.append(x);
		//					sObj.append(" ");
		//					sObj.append(y);
		//					sObj.append(")");
		//					object.value = sObj.toString();
		//					object.type = TripleEntry.EntryType.TypedLiteral;
		//					object.typeIri = GEOGRAPHY_TYPE_IRI;
		//
		//					// Triple: geordf:Node123 geo:hasGeography1 "POINT(0 0)"^^geo:geography
		//					process(subject, predicate, object, of);triples++;
		//				}
		//
		//				// construct tags
		//				String nodeSubject = subject.value;
		//				for (int currTagNum = 1; ((nodeId & (currTagNum - 1)) == 0) && currTagNum <= MAX_TAG_VAL; currTagNum *= 2) {
		//					// Overwrite tag Uri
		//					subject.value = nodeSubject;
		//
		//					// triple from node to tag
		//					predicate.value = HAS_TAG_IRI;
		//
		//					sObj = new StringBuffer();
		//					sObj.append(TAG_IRI);
		//					sObj.append(tagId++);
		//					object.value = sObj.toString();
		//					object.type = TripleEntry.EntryType.Iri;
		//
		//					// Triple: geordf:Node123 geordf:hasTag geordf:Tag12
		//					process(subject, predicate, object, of);triples++;
		//
		//					// definition of tag
		//					subject.value = object.value;
		//					predicate.value = A_IRI;
		//					object.value = TAG_IRI;
		//					object.type = TripleEntry.EntryType.Iri;
		//
		//					// Triple: geordf:Tag12 a geordf:Tag
		//					process(subject, predicate, object, of);triples++;
		//
		//					predicate.value = KEY_IRI;
		//					sObj = new StringBuffer();
		//					sObj.append(currTagNum);
		//					object.value = sObj.toString();
		//					object.type = TripleEntry.EntryType.Literal;
		//
		//					// Triple geordf:Tag12 geordf:key "4"
		//					process(subject, predicate, object, of);triples++;
		//
		//					predicate.value = VALUE_IRI;
		//
		//					// Triple geordf:Tag12 geordf:value "4"
		//					process(subject, predicate, object, of);triples++;
		//				}
		//			}
		//		}

		System.out.println("Generated " + triples + " triples.\n");

		return triples;
	}

	private static void generateTriplesForNode(Integer value, int geoStmtsPerNode, BufferedWriter of, int geometryType) throws IOException {

		TripleEntry subject = new TripleEntry();
		TripleEntry predicate = new TripleEntry();
		TripleEntry object = new TripleEntry();

		// These never change.
		subject.type = TripleEntry.EntryType.Iri;
		predicate.type = TripleEntry.EntryType.Iri;

		int bitmask = 0xFFFF;

		short i = (short) (value>>16);
		short j = (short) (value & bitmask);

		BigDecimal xCoord = X_DELTA.multiply(new BigDecimal(i));
		BigDecimal yCoord = Y_DELTA.multiply(new BigDecimal(j));
		
		BigDecimal dx = X_DELTA.multiply(new BigDecimal("1.1"));
		@SuppressWarnings("unused")
		BigDecimal dy = X_DELTA.multiply(new BigDecimal("1.1"));

		int nodeId = i * ((X_MAX.divide(X_DELTA)).intValue() +1) + j + 1;

		StringBuffer sSub = new StringBuffer();
		StringBuffer sPred = new StringBuffer();
		StringBuffer sObj = new StringBuffer();

		// Node's subject.
		sSub = new StringBuffer();
		sSub.append(NODE_IRI);
		sSub.append(nodeId);
		subject.value = sSub.toString();

		predicate.value = A_IRI;
		object.value = NODE_IRI;
		object.type = TripleEntry.EntryType.Iri;

		// Triple: geordf:Node123 a geordf:Node
		process(subject, predicate, object, of);
		triples++;

		predicate.value = ID_IRI;

		sObj = new StringBuffer();
		sObj.append("(");
		sObj.append(xCoord); 
		sObj.append(" ");
		sObj.append(yCoord);
		sObj.append(")");
		object.value = sObj.toString();
		object.type = TripleEntry.EntryType.Literal;

		// Triple: geordf:Node123 geordf:id "(0 0)"
		process(subject, predicate, object, of);triples++;

		// construct geo-triples
		for (int currGeoStmt = 1; currGeoStmt <= geoStmtsPerNode; currGeoStmt++) {
			sPred = new StringBuffer();
			sPred.append(HAS_GEO_IRI);
			sPred.append(currGeoStmt);
			predicate.value = sPred.toString();

			// Create a different geography for each geo statement (of each node and generally)
			sObj = new StringBuffer();

			BigDecimal x,y;
			if (currGeoStmt != 1) {
				BigDecimal geoDelta = GEO_PRED_DELTA.multiply(new BigDecimal(currGeoStmt - 1));
				x = xCoord.add(geoDelta);
				y = yCoord.add(geoDelta);
			} else {
				x = xCoord;
				y = yCoord;
			}
			
			if (geometryType == 0) {
				sObj.append("POINT (");
				sObj.append(x);
				sObj.append(" ");
				sObj.append(y);
				sObj.append(")");
			} else if (geometryType == 1) {
				sObj.append("POLYGON ((");
				sObj.append(x         + " " + y         + ",");
				sObj.append(x.add(dx) + " " + y         + ",");
				sObj.append(x.add(dx) + " " + y.add(dx) + ",");
				sObj.append(x         + " " + y.add(dx) + ",");
				sObj.append(x         + " " + y         );
				sObj.append("))");
			}
			
			object.value = sObj.toString();
			object.type = TripleEntry.EntryType.TypedLiteral;
			object.typeIri = GEOGRAPHY_TYPE_IRI;

			// Triple: geordf:Node123 geo:hasGeography1 "POINT(0 0)"^^geo:geography
			process(subject, predicate, object, of);triples++;
		}

		// construct tags
		String nodeSubject = subject.value;
		for (int currTagNum = 1; ((nodeId & (currTagNum - 1)) == 0) && currTagNum <= MAX_TAG_VAL; currTagNum *= 2) {
			// Overwrite tag Uri
			subject.value = nodeSubject;

			// triple from node to tag
			predicate.value = HAS_TAG_IRI;

			sObj = new StringBuffer();
			sObj.append(TAG_IRI);
			sObj.append(tagId++);
			object.value = sObj.toString();
			object.type = TripleEntry.EntryType.Iri;

			// Triple: geordf:Node123 geordf:hasTag geordf:Tag12
			process(subject, predicate, object, of);triples++;

			// definition of tag
			subject.value = object.value;
			predicate.value = A_IRI;
			object.value = TAG_IRI;
			object.type = TripleEntry.EntryType.Iri;

			// Triple: geordf:Tag12 a geordf:Tag
			process(subject, predicate, object, of);triples++;

			predicate.value = KEY_IRI;
			sObj = new StringBuffer();
			sObj.append(currTagNum);
			object.value = sObj.toString();
			object.type = TripleEntry.EntryType.Literal;

			// Triple geordf:Tag12 geordf:key "4"
			process(subject, predicate, object, of);triples++;

			predicate.value = VALUE_IRI;

			// Triple geordf:Tag12 geordf:value "4"
			process(subject, predicate, object, of);triples++;
		}
	}

	static void process(TripleEntry subject, TripleEntry predicate, TripleEntry object, BufferedWriter file) throws IOException {
		file.write(subject.toString() + " "  + predicate.toString() + " " + object.toString() + " .\n");
	}

	public static void main(String[] args) {
		//   10mil: 0.01
		//  100mil: 0.0034
		//  500mil: 0.0015
		// 1000mil: 0.001

		// Check the arguments
		if (args.length < 2) {
			System.err.println("Usage: Generator <outputfile> <geometry type> [<geo statements per node>]");
			System.err.println("           where <outputfile>                 is the absolute path for the file to be generated");
			System.err.println("                 <geometry type>              is the type of the geometries to be generated (0: points, 1:polygons)");
			System.err.println("                 [<geo statements per node>]  is how many geometries per node should be generated (default: 1)");
			return;
		}

		int geometryType = new Integer(args[1]); 
		int geoStmtsPerNode = 1;
		if (args.length == 3) {
			geoStmtsPerNode = new Integer(args[2]);
		}

		try{
			// Create file 
			FileWriter fstream = new FileWriter(args[0]);
			BufferedWriter out = new BufferedWriter(fstream);
			int triples = generate(geoStmtsPerNode, out, geometryType);
			out.close();


			fstream = new FileWriter(args[0] + ".log");
			out = new BufferedWriter(fstream);
			// Print settings and document in a file...
			out.write("Settings:");
			out.write("\nGrid y direction: 0.0 to " + X_MAX + " in steps of " + X_DELTA);
			out.write("\nGrid y direction: 0.0 to " +Y_MAX + " in steps of " + Y_DELTA);
			out.write("\nresulting in a total of " + (X_MAX.divide(X_DELTA)).multiply((Y_MAX.divide(Y_DELTA))).toString() + " nodes in the grid.");
			out.write("\nGeo-statements per node: " + geoStmtsPerNode + ", delta between node's geogr.: " +GEO_PRED_DELTA);
			out.write("\nHighest tag value: " + MAX_TAG_VAL + "\n");
			out.write("\nNumber of triples: " + triples + "\n");

			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
}