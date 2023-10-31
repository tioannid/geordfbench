/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.io.ParseException;
//import com.vividsolutions.jts.io.WKTReader;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 */
public class SyntheticGenerator {
	String unit = "<http://www.opengis.net/def/uom/OGC/1.0/metre>";
//	final String unit = "<http://www.opengis.net/def/uom/OGC/1.0/degree>";
	
	// our Universe!
	private double minX = 0;
	private double maxX = 90d;
	// Our universe is rectangular :)
	private double minY = minX;
	private double maxY = maxX;

	// deltas of the x and y axes
	// Denote the distance of the center of two concecutive hexagons
	// when moving along the x or y axis (not both);
	private double deltaX, deltaY;

	// hexagon's size
	private double hexagonSide;

	// Number of polygon per axis :)
	private long numberOfPolygonsPerAxis;

	// a counter for the id of the shape that is currently being produced
	private long currentId;

	// all supported types
	private enum Shape {HEXAGON_SMALL, HEXAGON_LARGE, LINESTRING, POINT, HEXAGON_LARGE_CENTER};
	private HashMap<Shape, String> namedGraphs = new HashMap<SyntheticGenerator.Shape, String>() {
		{
			put(Shape.HEXAGON_SMALL, "http://geographica.di.uoa.gr/generator/landOwnership");
			put(Shape.HEXAGON_LARGE, "http://geographica.di.uoa.gr/generator/state");
			put(Shape.LINESTRING, "http://geographica.di.uoa.gr/generator/road");
			put(Shape.POINT, "http://geographica.di.uoa.gr/generator/pointOfInterest");
			put(Shape.HEXAGON_LARGE_CENTER, "http://geographica.di.uoa.gr/generator/stateCenter");
		}
	};
	
	// the extension functions that will be used for generating queries
	private enum TopologicalFunction {INTERSECTS, TOUCHES, WITHIN};
	private HashMap<TopologicalFunction, String> extensionFunctions = new HashMap<TopologicalFunction, String>() {
		{
			put(TopologicalFunction.INTERSECTS, "geof:sfIntersects");
			put(TopologicalFunction.TOUCHES, "geof:sfTouches");
			put(TopologicalFunction.WITHIN, "geof:sfWithin");
		}
	};
	
	// prefixes
	String prefixes =	" PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + 
						" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
						" PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
						" PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> \n" +
						" PREFIX geof: <http://www.opengis.net/def/function/geosparql/> \n" +
						" PREFIX geo: <http://www.opengis.net/ont/geosparql#> \n" +
						" PREFIX geo-sf: <http://www.opengis.net/ont/sf#> \n"
						;

	// folder where the output files will be stored
	// one file per geometry type will be created
	private File output;
	private HashMap<Shape, BufferedWriter> outputWriters;

	// maximum tag value
	private Integer MAX_TAG_VALUE = 1024;

	//private double[] selectivities = new double[]{0.4, 0.3, 0.2, 0.1,  0.001};
	private double[] selectivities = new double[]{1, 0.75, 0.5, 0.25, 0.1,  0.001};
	/**
	 * @param n The number of hexagons that will be generated along an axis.
	 * In total, n^2 hexagons will be generated.
	 */	
	public SyntheticGenerator (String outputFolder, long numberOfPolygonsPerAxis) {
		this.numberOfPolygonsPerAxis = numberOfPolygonsPerAxis;
		this.deltaX = (maxX - minX) / (numberOfPolygonsPerAxis + 0.5); 
		this.hexagonSide = deltaX * Math.sqrt(3d) / 3d;	
		this.deltaY = 3d * hexagonSide / 2d;

		output = new File(outputFolder);
		outputWriters = new HashMap<SyntheticGenerator.Shape, BufferedWriter>(Shape.values().length);
		
		while (numberOfPolygonsPerAxis < MAX_TAG_VALUE) {
			MAX_TAG_VALUE = MAX_TAG_VALUE/2;
		}
	}

	/**
	 * Generates the small hexagons corresponding to land ownerships
	 * @throws IOException 
	 */
	public void generateSmallHexagons() throws IOException {
		generate(Shape.HEXAGON_SMALL, this.numberOfPolygonsPerAxis, this.hexagonSide, this.deltaX);
	}

	/**
	 * Generates the large hexagons that corresponds to states
	 * @throws IOException 
	 */
	public void generateLargeHexagons() throws IOException {
		generate(Shape.HEXAGON_LARGE, this.numberOfPolygonsPerAxis/3, 3d*this.hexagonSide, 3d*this.deltaX);
	}
	
	/**
	 * Generates the large hexagons that corresponds to states
	 * @throws IOException 
	 */
	public void generateLargeHexagonCenters() throws IOException {
		generate(Shape.HEXAGON_LARGE_CENTER, this.numberOfPolygonsPerAxis/3, 3d*this.hexagonSide, 3d*this.deltaX);
	}

	/**
	 * Generates the linestrings that corresponds to roads
	 * @throws IOException 
	 */
	public void generateLineStrings() throws IOException {
		generate(Shape.LINESTRING, this.numberOfPolygonsPerAxis/2, this.hexagonSide, this.deltaX/2d);
	}
	
	/**
	 * Generates the points that corresponds to points of interest
	 * @throws IOException 
	 */
	public void generatePoints() throws IOException {
		generate(Shape.POINT, this.numberOfPolygonsPerAxis, this.hexagonSide, this.deltaX/2d);
	}

	/**
	 * @param n: number of polygons to generate
	 * @param a: the length of the hexagon's side
	 * @param dx: distance along the x-axis between successive centers
	 * @throws IOException 
	 */
	private void generate(Shape shp, long n, double a, double dx) throws IOException {
		currentId = 0;
		double x, y, epsilon;

		switch (shp) {
		case HEXAGON_LARGE:
		case HEXAGON_SMALL:
			x = dx/2d;
			y = a;
			for (int i = 1; i <= n; i++) {
				//generate a line
				generateHexagonLine(shp, n, x, y, a, dx);

				if (i%2 == 0)
					x -= dx/2d;
				else
					x += dx/2d;
				y += 3d*a/2d;
			}
			break;

		case HEXAGON_LARGE_CENTER:
			x = dx/2d;
			y = a;
			for (int i = 1; i <= n; i++) {
				//generate a line
				generateHexagonCenter(shp, n, x, y, a, dx);

				if (i%2 == 0)
					x -= dx/2d;
				else
					x += dx/2d;
				y += 3d*a/2d;
			}
			break;
			
		case LINESTRING:
			epsilon=dx/6d;
			x = 3d*dx + epsilon;
			y = a - epsilon/2d;

			generateVerticalLineStrings(Shape.LINESTRING, n, x, y, a, this.deltaX, epsilon);
			
			x = a - epsilon/2d;
			y = 3d*dx + epsilon;
			generateHorizontalLineStrings(Shape.LINESTRING, n, x, y, a, this.deltaX, epsilon);	
			
			break;
		case POINT:
			for (int i = 0; i < n; i++) {
				double x1 = minX + (double)i*(maxX - minX)/((double)n);
				double y1 = minY;
				double x2 = x1 + (maxX - minX)/((double)n);
				double y2 = this.deltaY*((double)this.numberOfPolygonsPerAxis); //maxy
				
				generatePointsAlongLine(n, x1, y1, x2, y2);
			}			
			
			break;
		default:
			break;
		}
	}
	
	/**
	 * @param n: number of points to generate along the line defined by A(x1, y1), B(x2, y2)
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @throws IOException
	 */
	private void generatePointsAlongLine(long n, double x1, double y1, double x2, double y2) throws IOException {
		double slope = (y2 - y1)/(x2 - x1);
		double deltaX = (x2 - x1)/(double)n;
				
		for (int i = 1; i <= n; i++) {
			double x = x1 + (double)i*deltaX;
			double y = slope*(x - x1) + y1;
			
			generateInstance(Shape.POINT ,x, y, 0, 0, false, false);			
		}
	}

	/**
	 * @param n: number of polygons to generate
	 * @param x: x-coordinate of the center of the first hexagon
	 * @param y: y-coordinate of the center of the first hexagon
	 * @param a: the length of the hexagon's side
	 * @param dx: distance along the x-axis between successive centers
	 * @throws IOException 
	 */
	private void generateHexagonLine(Shape shp, long n, double x, double y, double a, double dx) throws IOException {
		for (int i = 1; i <= n; i++) {
			generateInstance(shp ,x, y, a, 0, true, false);
			x += dx;
		}
	}
	
	/**
	 * @param n: number of polygons to generate
	 * @param x: x-coordinate of the center of the first hexagon
	 * @param y: y-coordinate of the center of the first hexagon
	 * @param a: the length of the hexagon's side
	 * @param dx: distance along the x-axis between successive centers
	 * @throws IOException 
	 */
	private void generateHexagonCenter(Shape shp, long n, double x, double y, double a, double dx) throws IOException {
		for (int i = 1; i <= n; i++) {
			generateInstance(shp ,x, y, a, 0, true, false);
			x += dx;
		}
	}
	
	/**
	 * @param n: number of polygons to generate
	 * @param x: x-coordinate of the center of the first hexagon
	 * @param y: y-coordinate of the center of the first hexagon
	 * @param a: the length of the hexagon's side
	 * @param dx: distance along the x-axis between successive centers
	 * @throws IOException 
	 */
	private void generateVerticalLineStrings(Shape shp, long n, double x, double y, double a, double dx, double epsilon) throws IOException {
		for (int i = 1; i <= n; i++) {
			generateInstance(shp ,x, y, a, epsilon, (i%2==0), true);
			x += (2d - 3d/(double)n)*dx;
		}
	}
	
	/**
	 * @param n: number of polygons to generate
	 * @param x: x-coordinate of the center of the first hexagon
	 * @param y: y-coordinate of the center of the first hexagon
	 * @param a: the length of the hexagon's side
	 * @param dx: distance along the x-axis between successive centers
	 * @throws IOException 
	 */
	private void generateHorizontalLineStrings(Shape shp, long n, double x, double y, double a, double dx, double epsilon) throws IOException {
		for (int i = 1; i <= n; i++) {
			generateInstance(shp ,x, y, a, epsilon, (i%2==0), false);
			y += (3d - 3d/(double)n)*a;
		}
	}

	/**
	 * @param x: x-coordinate of the hexagon's center
	 * @param y: y-coordinate of the hexagon's center
	 * @param a: the length of the hexagon's side
	 */
	private String generateHexagon(double x, double y, double a) {

		double dx = Math.sqrt(3d) * a / 2d;

		StringBuffer sb = new StringBuffer(1024);
		sb.append("POLYGON ((");

		// P1
		sb.append(x);
		sb.append(" ");
		sb.append(y-a);
		sb.append(", ");
		// P2
		sb.append(x+dx);
		sb.append(" ");
		sb.append(y-a/2d);
		sb.append(", ");
		// P3
		sb.append(x+dx);
		sb.append(" ");
		sb.append(y+a/2d);
		sb.append(", ");
		// P4
		sb.append(x);
		sb.append(" ");
		sb.append(y+a);
		sb.append(", ");
		// P5
		sb.append(x-dx);
		sb.append(" ");
		sb.append(y+a/2d);
		sb.append(", ");
		// P6
		sb.append(x-dx);
		sb.append(" ");
		sb.append(y-a/2d);
		sb.append(", ");		
		// P1
		sb.append(x);
		sb.append(" ");
		sb.append(y-a);
		sb.append("))");

		return sb.toString();
	}
	
	/**
	 * @param x: x-coordinate of the lowest point of the line string
	 * @param y: y-coordinate of the lowest point of the line string
	 * @param a: the length of the hexagon's side 
	 * @param epsilon: a small value that will be added/substracted to the x-coordinate of the linestring's points
	 * @param forward: should the second point have a larger x-coordinate than the first?
	 * @return
	 */
	private String generateLineString(double x, double y, double a, double epsilon, boolean forward, boolean vertical) {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("LINESTRING (");

		double maxy = this.deltaY*((double)this.numberOfPolygonsPerAxis);
		double maxx = this.deltaX*((double)this.numberOfPolygonsPerAxis);
		
		int points = 0;
		while ((vertical && y < maxy) || ((!vertical) && x < maxx)) {
			if (vertical) {
				if (forward) {
					x += epsilon;
					forward = false;
				} else {
					x -= epsilon/2d;
					forward = true;
				}
				
			} else {
				if (forward) {
					y += epsilon;
					forward = false;
				} else {
					y -= epsilon/2d;
					forward = true;
				}
			}		
			
			if (x > maxX || y > maxY)
				break;
			
			points++;
			sb.append(x);
			sb.append(" ");
			sb.append(y);
			sb.append(", ");
			
			if (vertical)
				y += a*2d;
			else
				x += deltaX*2d;
			
		}
		
		int pos = sb.lastIndexOf(",");
		sb.replace(pos, pos+2, ")");

		if (points < 2)
			return "";
		
		return sb.toString();
	}
	
	/**
	 * @param x: x-coordinate of the hexagon's center
	 * @param y: y-coordinate of the hexagon's center
	 */
	private String generatePoint(double x, double y) {
		return "POINT ( " + x + " " + y +")";
	}

	private void generateInstance(Shape shp, double x, double y, double a, double epsilon, boolean forward, boolean vertical) throws IOException {		
		String prefix = "", geometry = "", className = "";		
		currentId++;
		
		switch (shp) {
		case HEXAGON_SMALL:
			geometry = generateHexagon(x, y, a);
			prefix = "http://geographica.di.uoa.gr/generator/landOwnership/";
			className = "LandOwnership";
			break;

		case HEXAGON_LARGE:
			geometry = generateHexagon(x, y, a);
			prefix = "http://geographica.di.uoa.gr/generator/state/";
			className = "State";
			break;
			
		case HEXAGON_LARGE_CENTER:
			geometry = generatePoint(x, y);
			prefix = "http://geographica.di.uoa.gr/generator/stateCenter/";
			className = "StateCenter";
			break;

		case LINESTRING:
			geometry = generateLineString(x, y, a, epsilon, forward, vertical);
			prefix = "http://geographica.di.uoa.gr/generator/road/";
			className = "Road";
			break;

		case POINT:
			geometry = generatePoint(x, y);
			prefix = "http://geographica.di.uoa.gr/generator/pointOfInterest/";
			className = "PointOfInterest";
			break;
		}		

		writeFile(shp, "<" + prefix + currentId + "/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + prefix + className +"> .");			
		writeFile(shp, "<" + prefix + currentId + "/> <" + prefix + "hasGeometry> <" + prefix + "geometry/" + currentId + "/> .");
		writeFile(shp, "<" + prefix + "geometry/" + currentId + "/> <" + prefix + "asWKT> \"" + geometry + "\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> .");

//		for (int tagId = 1;  ((currentId-(int)(currentId/MAX_TAG_VALUE)) % tagId == 0) && tagId <= MAX_TAG_VALUE; tagId *= 2) {
//		for (int tagId = 1;  ((currentId+MAX_TAG_VALUE/2) % tagId == 0) && tagId <= MAX_TAG_VALUE; tagId *= 2) {
		for (int tagId = 1;  (currentId % tagId == 0) && tagId <= MAX_TAG_VALUE; tagId *= 2) {
			if (tagId > 1 && tagId < MAX_TAG_VALUE)
				continue;
			
			writeFile(shp, "<" + prefix + currentId + "/> <" + prefix + "hasTag> <" + prefix + currentId + "/tag/" + tagId + "/> .");				
			writeFile(shp, "<" + prefix + currentId + "/tag/" + tagId + "/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + prefix + "Tag> .");
			writeFile(shp, "<" + prefix + currentId + "/tag/" + tagId + "/> <" + prefix + "hasKey> \"" + tagId + "\" .");
			writeFile(shp, "<" + prefix + currentId + "/tag/" + tagId + "/> <" + prefix + "hasValue> \"" + tagId + "\" .");
		}		
		
	}

	private void writeFile(Shape shp, String triple) throws IOException {
		BufferedWriter bw = outputWriters.get(shp);
		if (bw == null) {
			File file = new File(output.getAbsolutePath() + File.separator + shp.toString() + ".nt");
			if (file.exists()) {
				throw new IOException("File " + file.getAbsolutePath() + " already exists. Aborting...");
			}

			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			outputWriters.put(shp, bw);
		}

		bw.write(triple);
		bw.write("\n");

	}

	public void close() throws IOException {
		for (Shape shp: outputWriters.keySet()) {
			BufferedWriter bw = outputWriters.get(shp);
			bw.flush();
			bw.close();
		}
		outputWriters.clear();
	}

	public String[][][] generateQueries() {
		String[][][] queries = new String[3][2][];

		// Intersects
		queries[0][0] = generateSpatialSelection(TopologicalFunction.INTERSECTS, Shape.HEXAGON_SMALL);
		queries[0][1] = generateSpatialJoin(TopologicalFunction.INTERSECTS, Shape.HEXAGON_SMALL, Shape.HEXAGON_LARGE);

		// Touches
		// skip selections
		queries[1][0] = new String[queries[0][0].length];
		for (int i = 0 ; i < queries[0][0].length; i++)
			queries[1][0][i] = "";
		queries[1][1] = generateSpatialJoin(TopologicalFunction.TOUCHES, Shape.HEXAGON_LARGE, Shape.HEXAGON_LARGE);

		// Within
		queries[2][0] = generateSpatialSelection(TopologicalFunction.WITHIN, Shape.POINT);
		queries[2][1] = generateSpatialJoin(TopologicalFunction.WITHIN, Shape.POINT, Shape.HEXAGON_LARGE);

		return queries;
	}

	public String[][][] generatePointQueries() {
//		String[][][] queries = new String[3][2][]; // TODO
		String[][][] queries = new String[1][2][]; // TODO

		// Intersects
		queries[0][0] = generateSpatialSelectionPoints(Shape.POINT);
		queries[0][1] = generateSpatialJoinPoints(Shape.POINT, Shape.HEXAGON_LARGE_CENTER);

//		// Touches
//		// skip selections
//		queries[1][0] = new String[queries[0][0].length];
//		for (int i = 0 ; i < queries[0][0].length; i++)
//			queries[1][0][i] = "";
//		queries[1][1] = generateSpatialJoin(TopologicalFunction.TOUCHES, Shape.HEXAGON_LARGE, Shape.HEXAGON_LARGE);
//
//		// Within
//		queries[2][0] = generateSpatialSelection(TopologicalFunction.WITHIN, Shape.POINT);
//		queries[2][1] = generateSpatialJoin(TopologicalFunction.WITHIN, Shape.POINT, Shape.HEXAGON_LARGE);
		
		return queries;
	}
	
	/**
	 * @param shp1: The first shape to be used
	 * @param shp2: The second shape to be used
	 * @return
	 */
	private String[] generateSpatialJoinPoints(Shape shp1, Shape shp2) {
		String[] queries = new String[4];
		String header =  prefixes +
						" SELECT ?s1 ?s2 \n" + 
						" WHERE {\n";
		String partA =	//" GRAPH <" + namedGraphs.get(shp1) + "> { \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasGeometry> ?s1Geo . \n" +
						"       ?s1Geo <"+namedGraphs.get(shp1)+"/asWKT> ?geo1 . \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasTag> ?tag1 . \n" +
						"       ?tag1 <"+namedGraphs.get(shp1)+"/hasKey> \"KEY1\" .  \n" ;
						//"       }\n" +
		String partB =	//" GRAPH <" + namedGraphs.get(shp2) + "> { \n" +
						"       ?s2 <"+namedGraphs.get(shp2)+"/hasGeometry> ?s2Geo . \n" +
						"       ?s2Geo <"+namedGraphs.get(shp2)+"/asWKT> ?geo2 . \n" +
						"       ?s2 <"+namedGraphs.get(shp2)+"/hasTag> ?tag2 . \n" +
						"       ?tag2 <"+namedGraphs.get(shp2)+"/hasKey> \"KEY2\" .  \n" ;
						//"       }\n" +
		String footer =	"       FILTER ( " + "geof:distance" + "(?geo1, ?geo2, <http://www.opengis.net/def/uom/OGC/1.0/metre>) <= DISTANCE) .\n" +
						" }\n";
		
		double radius = (3d*this.hexagonSide)*5;
		double midX = (maxX - minX)/2;
		double midY = (maxY - minY)/2;
		double distanceInMeters = -1;
		try {
			WKTReader wktReader = new WKTReader();
			
			Geometry start = wktReader.read("POINT( "+midX+" "+midY+")");
			Geometry end = wktReader.read("POINT( "+(midX+radius)+" "+midY+")");
			
			distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:4326"));
//			distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:3857"));
		
			System.out.println("Start = "+start.toText()+" End = "+end.toText());
		    System.out.println("Distance = " + distanceInMeters + "m ");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		queries[0] = header + partA.replace("KEY1", "1") + partB.replace("KEY2", "1") + footer.replace("DISTANCE", Double.toString(distanceInMeters));
		queries[1] = header + partA.replace("KEY1", "1") + partB.replace("KEY2", this.MAX_TAG_VALUE.toString()) + footer.replace("DISTANCE", Double.toString(distanceInMeters));
		queries[2] = header + partB.replace("KEY2", "1") + partA.replace("KEY1", this.MAX_TAG_VALUE.toString()) + footer.replace("DISTANCE", Double.toString(distanceInMeters));		
		queries[3] = header + partA.replace("KEY1", this.MAX_TAG_VALUE.toString()) + partB.replace("KEY2", this.MAX_TAG_VALUE.toString()) + footer.replace("DISTANCE", Double.toString(distanceInMeters));
		
		return queries;
	}
	
	/**
	 * @param function: The topological function that will appear at the filter clause
	 * @param shp1: The first shape to be used
	 * @param shp2: The second shape to be used
	 * @return
	 */
	private String[] generateSpatialSelectionPoints(Shape shp1) {
		String[] queries = new String[this.selectivities.length*2];
		
		
		String query = 	prefixes + 
						" SELECT ?s1 \n" + 
						" WHERE {\n" +
						//" GRAPH <" + namedGraphs.get(shp1) + "> { \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasGeometry> ?s1Geo . \n" +
						"       ?s1Geo <"+namedGraphs.get(shp1)+"/asWKT> ?geo1 . \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasTag> ?tag1 . \n" +
						"       ?tag1 <"+namedGraphs.get(shp1)+"/hasKey> \"KEY1\" .  \n" +
						//"       }\n" +						
//						"       FILTER ( " + extensionFunctions.get(function) + "(?geo1, \"CONSTANT\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)) .\n" +
						"       FILTER ( " + "bif:st_within" + "(?geo1, bif:st_point(45, 45), DISTANCE)) .\n" +
						" }\n";
		
		for (int i = 0; i < this.selectivities.length; i++) {
			String[] distanceAndCenter = defineDistanceForSelectivity(shp1, this.selectivities[i]);
			
			String distance = null;
			
			if (unit.equals("<http://www.opengis.net/def/uom/OGC/1.0/degree>"))
				distance = distanceAndCenter[0]; // degree
			else {
				distance = distanceAndCenter[1];  // metre
				distance = String.format("%f", (Double.parseDouble(distance)/1000));
			}
			
			
			queries[2*i] = query.replace("DISTANCE", distance).replace("KEY1", "1");
			queries[2*i+1] = query.replace("DISTANCE", distance).replace("KEY1", this.MAX_TAG_VALUE.toString());
		}
		
		return queries;
	}
	
	/**
	 * @param function: The topological function that will appear at the filter clause
	 * @param shp1: The first shape to be used
	 * @param shp2: The second shape to be used
	 * @return
	 */
	private String[] generateSpatialJoin(TopologicalFunction function,  Shape shp1, Shape shp2) {
		String[] queries = new String[4];
		String header =  prefixes +
						" SELECT ?s1 ?s2 \n" + 
						" WHERE {\n";
		String partA =	//" GRAPH <" + namedGraphs.get(shp1) + "> { \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasGeometry> ?s1Geo . \n" +
						"       ?s1Geo <"+namedGraphs.get(shp1)+"/asWKT> ?geo1 . \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasTag> ?tag1 . \n" +
						"       ?tag1 <"+namedGraphs.get(shp1)+"/hasKey> \"KEY1\" .  \n" ;
						//"       }\n" +
		String partB =	//" GRAPH <" + namedGraphs.get(shp2) + "> { \n" +
						"       ?s2 <"+namedGraphs.get(shp2)+"/hasGeometry> ?s2Geo . \n" +
						"       ?s2Geo <"+namedGraphs.get(shp2)+"/asWKT> ?geo2 . \n" +
						"       ?s2 <"+namedGraphs.get(shp2)+"/hasTag> ?tag2 . \n" +
						"       ?tag2 <"+namedGraphs.get(shp2)+"/hasKey> \"KEY2\" .  \n" ;
						//"       }\n" +
		String footer =	"       FILTER ( " + extensionFunctions.get(function) + "(?geo1, ?geo2)) .\n" +
						" }\n";
		
		queries[0] = header + partA.replace("KEY1", "1") + partB.replace("KEY2", "1") + footer;
		queries[1] = header + partA.replace("KEY1", "1") + partB.replace("KEY2", this.MAX_TAG_VALUE.toString()) + footer;
		queries[2] = header + partB.replace("KEY2", "1") + partA.replace("KEY1", this.MAX_TAG_VALUE.toString()) + footer;		
		queries[3] = header + partA.replace("KEY1", this.MAX_TAG_VALUE.toString()) + partB.replace("KEY2", this.MAX_TAG_VALUE.toString()) + footer;
		
		return queries;
	}
	
	/**
	 * @param function: The topological function that will appear at the filter clause
	 * @param shp1: The first shape to be used
	 * @param shp2: The second shape to be used
	 * @return
	 */
	private String[] generateSpatialSelection(TopologicalFunction function,  Shape shp1) {
		String[] queries = new String[this.selectivities.length*2];
		String query = 	prefixes + 
						" SELECT ?s1 \n" + 
						" WHERE {\n" +
						//" GRAPH <" + namedGraphs.get(shp1) + "> { \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasGeometry> ?s1Geo . \n" +
						"       ?s1Geo <"+namedGraphs.get(shp1)+"/asWKT> ?geo1 . \n" +
						"       ?s1 <"+namedGraphs.get(shp1)+"/hasTag> ?tag1 . \n" +
						"       ?tag1 <"+namedGraphs.get(shp1)+"/hasKey> \"KEY1\" .  \n" +
						//"       }\n" +						
						"       FILTER ( " + extensionFunctions.get(function) + "(?geo1, \"CONSTANT\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)) .\n" +
						" }\n";
		
		for (int i = 0; i < this.selectivities.length; i++) {
			String bb = definePolygonForSelectivity(shp1, this.selectivities[i]);
			queries[2*i] = query.replace("CONSTANT", bb).replace("KEY1", "1");
			queries[2*i+1] = query.replace("CONSTANT", bb).replace("KEY1", this.MAX_TAG_VALUE.toString());
		}
		
		return queries;
	}
	
	/**
	 * @param shp: A shape (that corresponds to a specific distribution along the universe
	 * @param selectivity: The percentage (0-100.0) of spatial objects of type shp that should be selected.
	 * @return ret[0] distance in degree, ret[1] distance in metre
	 */
	private String[] defineDistanceForSelectivity(Shape shp, double selectivity) {
//		StringBuffer sb = new StringBuffer(1024);
		double x1, x2, x3, x4, y1, y2, y3, y4, epsilon;
		String ret[] = new String[2];
		
		switch (shp) {
			
		case POINT:
			
			double lanes = Math.floor(selectivity*this.numberOfPolygonsPerAxis);
			
			// calculate the coordinates of 4 points (counter clockwise starting from the bottom left corner)
			x1 = minX; 
			y1 = minY;
			x2 = lanes*(maxX-minX)/((double)this.numberOfPolygonsPerAxis - 1) + 
				 (selectivity - lanes)*(maxX-minX)/(Math.pow((double)this.numberOfPolygonsPerAxis-1,2));
			y2 = minY;
			x3 = x2;
			y3 = maxY;
			x4 = minX;
			y4 = maxY;
			
			// if possible, expand the rectangle by a small epsilon
			epsilon = (deltaX+deltaY)/10d;
			if (x1-epsilon > minX) x1 -= epsilon;
			if (y1-epsilon > minY) y1 -= epsilon;
			if (x2+epsilon < maxX) x2 += epsilon;
			if (y2-epsilon > minY) y2 -= epsilon;
			if (x3+epsilon < maxX) x3 += epsilon;
			if (y3+epsilon < maxY) y3 += epsilon;
			if (x4-epsilon > minX) x4 -= epsilon;
			if (y4+epsilon < maxY) y4 += epsilon;
			
//			sb.append("POLYGON ((");
//			sb.append(x1);sb.append(" ");sb.append(y1);sb.append(", ");
//			sb.append(x2);sb.append(" ");sb.append(y2);sb.append(", ");
//			sb.append(x3);sb.append(" ");sb.append(y3);sb.append(", ");
//			sb.append(x4);sb.append(" ");sb.append(y4);sb.append(", ");
//			sb.append(x1);sb.append(" ");sb.append(y1);
//			sb.append("))");
//			System.out.println("Rectangle: "+sb);
			double area = (x2-x1)*(y3-y2);
			System.out.println("Area: "+area);
			double radius = Math.sqrt(area/Math.PI);
			ret[0]=null;
			if (radius > 45) {
				System.out.println("Real Radius(degrees): "+radius);
				radius = 45;
				ret[0] = "64";
			}
			System.out.println("Radius(degrees): "+radius);
			
			// done!
			// Compute distance between (midX, midY) and (x3, y3)
			try {
				WKTReader wktReader = new WKTReader();
				
				double midX = 45;
				double midY = 45;
				
				Geometry start = wktReader.read("POINT( "+midX+" "+midY+")");
				// Radius east
				Geometry end = wktReader.read("POINT( "+(midX+radius)+" "+midY+")");
				double distanceInMeterEast = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:4326"));
//				distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:3857"));
				System.out.println("Radius east(meters): "+distanceInMeterEast);
				// Radius west
				end = wktReader.read("POINT( "+(midX-radius)+" "+midY+")");
				double distanceInMeterWest = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:4326"));
//				distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:3857"));
				System.out.println("Radius west(meters): "+distanceInMeterWest);
				// Radius north
				end = wktReader.read("POINT( "+midX+" "+(midY+radius)+")");
				double distanceInMeterNorth = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:4326"));
//				distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:3857"));
				System.out.println("Radius north(meters): "+distanceInMeterNorth);
				// Radius south
				end = wktReader.read("POINT( "+midX+" "+(midY-radius)+")");
				double distanceInMeterSouth = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:4326"));
//				distanceInMeters = JTS.orthodromicDistance(start.getCoordinate(), end.getCoordinate(), CRS.decode("EPSG:3857"));
				System.out.println("Radius south(meters): "+distanceInMeterSouth);
			
				double distanceInMeter = (distanceInMeterEast+distanceInMeterWest+distanceInMeterNorth+distanceInMeterSouth)/4;
				System.out.println("Radius mean(meters): "+distanceInMeter);
				
				if (ret[0] == null) {
					ret[0] = String.format("%f",radius);
					if (selectivity > 0.001) // Small correction to achieve better selectiviy
						ret[1] = String.format("%f",distanceInMeter*0.8);
					else
						ret[1] = String.format("%f",distanceInMeter*1.42);
				} else {
					ret[1] = "5000000";
				}
				
				return ret;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;

		default:
			break;
		}
		
		
		
		return null;
	}

	
	/**
	 * @param shp: A shape (that corresponds to a specific distribution along the universe
	 * @param selectivity: The percentage (0-100.0) of spatial objects of type shp that should be selected.
	 * @return
	 */
	private String definePolygonForSelectivity(Shape shp, double selectivity) {
		StringBuffer sb = new StringBuffer(1024);
		double x1, x2, x3, x4, y1, y2, y3, y4, epsilon;

		switch (shp) {
		case HEXAGON_SMALL:
			long nodesPerAxis = (long)Math.ceil(Math.sqrt((double)this.numberOfPolygonsPerAxis*(double)this.numberOfPolygonsPerAxis*selectivity)); 
			
			// calculate the coordinates of 4 points (counter clockwise starting from the bottom left corner)
			x1 = minX + this.deltaX/2d;
			y1 = minY + this.hexagonSide;
			x2 = x1 + (nodesPerAxis - 1)*this.deltaX;
			y2 = y1;
			x3 = x1 + (nodesPerAxis - 1)*this.deltaX;
			y3 = y1 + (nodesPerAxis - 1)*this.deltaY;
			x4 = x1;
			y4 = y1 + (nodesPerAxis - 1)*this.deltaY;
			
			// expand the polygon to cover the hexagons
			x1 -= this.deltaX/2d;
			y1 -= this.hexagonSide;
			x2 += this.deltaX;
			y2 -= this.hexagonSide;
			x3 += this.deltaX;
			y3 += this.hexagonSide;
			x4 -= this.deltaX/2d;
			y4 += this.hexagonSide;
			
			// if possible, expand the rectangle by a small epsilon
			epsilon = (deltaX+deltaY)/10d;
			if (x1-epsilon > minX) x1 -= epsilon;
			if (y1-epsilon > minY) y1 -= epsilon;
			if (x2+epsilon < maxX) x2 += epsilon;
			if (y2-epsilon > minY) y2 -= epsilon;
			if (x3+epsilon < maxX) x3 += epsilon;
			if (y3+epsilon < maxY) y3 += epsilon;
			if (x4-epsilon > minX) x4 -= epsilon;
			if (y4+epsilon < maxY) y4 += epsilon;
			
			// done!
			sb.append("POLYGON ((");
			sb.append(x1);sb.append(" ");sb.append(y1);sb.append(", ");
			sb.append(x2);sb.append(" ");sb.append(y2);sb.append(", ");
			sb.append(x3);sb.append(" ");sb.append(y3);sb.append(", ");
			sb.append(x4);sb.append(" ");sb.append(y4);sb.append(", ");
			sb.append(x1);sb.append(" ");sb.append(y1);
			sb.append("))");
			break;
			
		case POINT:
			double lanes = Math.floor(selectivity*this.numberOfPolygonsPerAxis);
			
			
			// calculate the coordinates of 4 points (counter clockwise starting from the bottom left corner)
			x1 = minX; 
			y1 = minY;
			x2 = lanes*(maxX-minX)/((double)this.numberOfPolygonsPerAxis - 1) + 
				 (selectivity - lanes)*(maxX-minX)/(Math.pow((double)this.numberOfPolygonsPerAxis-1,2));
			y2 = minY;
			x3 = x2;
			y3 = maxY;
			x4 = minX;
			y4 = maxY;
			
			// if possible, expand the rectangle by a small epsilon
			epsilon = (deltaX+deltaY)/10d;
			if (x1-epsilon > minX) x1 -= epsilon;
			if (y1-epsilon > minY) y1 -= epsilon;
			if (x2+epsilon < maxX) x2 += epsilon;
			if (y2-epsilon > minY) y2 -= epsilon;
			if (x3+epsilon < maxX) x3 += epsilon;
			if (y3+epsilon < maxY) y3 += epsilon;
			if (x4-epsilon > minX) x4 -= epsilon;
			if (y4+epsilon < maxY) y4 += epsilon;
			
			// done!
			sb.append("POLYGON ((");
			sb.append(x1);sb.append(" ");sb.append(y1);sb.append(", ");
			sb.append(x2);sb.append(" ");sb.append(y2);sb.append(", ");
			sb.append(x3);sb.append(" ");sb.append(y3);sb.append(", ");
			sb.append(x4);sb.append(" ");sb.append(y4);sb.append(", ");
			sb.append(x1);sb.append(" ");sb.append(y1);
			sb.append("))");
			break;

		default:
			break;
		}
		
		
		
		return sb.toString();
	}
	
	public Integer returnMaxTagValue() {
		return this.MAX_TAG_VALUE;
	}
	
	public double[] returnSelectivities() {
		return this.selectivities;
	}

	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: SyntheticGenerator <OUTPUTPATH> <N>");
			System.err.println("       where <OUTPUT PATH> is the folder where the generated RDF files will be stored,");
			System.err.println("             <N> is number of generated hexagons along the x axis");
		}

		String outputPath = args[0];
		int numberOfPolygons = new Integer(args[1]);

		SyntheticGenerator g = new SyntheticGenerator(outputPath, numberOfPolygons);

		try {
			System.out.println("-------------------------------------");
			System.out.println("Generating " + numberOfPolygons*numberOfPolygons + " hexagons...");
			g.generateSmallHexagons();
			System.out.println("-------------------------------------");
			System.out.println("Generating " + (numberOfPolygons/3)*(numberOfPolygons/3) + " large hexagons...");
			g.generateLargeHexagons();
			System.out.println("-------------------------------------");
			System.out.println("Generating " + (numberOfPolygons/3)*(numberOfPolygons/3) + " large hexagon centers...");
			g.generateLargeHexagonCenters();
			System.out.println("-------------------------------------");
			System.out.println("Generating " + numberOfPolygons + " linestrings...");
			g.generateLineStrings();
			System.out.println("-------------------------------------");
			System.out.println("Generating " + numberOfPolygons*numberOfPolygons + " points...");
			g.generatePoints();			
			System.out.println("-------------------------------------");
			
			System.out.println("-------------------------------------");
			System.out.println("You may run the filler as follows: ");
			System.out.print("filler ");
			Iterator<Entry<Shape, String>> it = g.namedGraphs.entrySet().iterator();
			while (it.hasNext()) {
		        Entry<Shape, String> pair = it.next();
		        Shape shp = pair.getKey();
		        String namedGraph = pair.getValue();
		        
		        System.out.print(g.output.getAbsolutePath() + File.separator + shp.toString() + ".nt");
		        System.out.print(" '"+namedGraph+"' ");
			}
			System.out.print("\n");
			System.out.println("-------------------------------------");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				g.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		System.out.println("\n\nGeneral Queries\n");
		String[][][] q = g.generateQueries();
		for (int i = 0; i < q.length; i++) {
			String[][] queriesForFunction = q[i];
			for (int j = 0; j < queriesForFunction.length; j++) {
				String[] queries = queriesForFunction[j];
				for (int k = 0; k < queries.length; k++) {
					System.out.println(queries[k]);
					//System.out.println("\"" +
					//	queries[k].replace("\n", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("\"", "\\\\\\\"")
					//		+"\" \\");
				}
			}
		}
		
		System.out.println("\n\nPoint Queries\n");
		String[][][] qp = g.generatePointQueries();
		for (int i = 0; i < qp.length; i++) {
			String[][] queriesForFunction = qp[i];
			for (int j = 0; j < queriesForFunction.length; j++) {
				String[] queries = queriesForFunction[j];
				for (int k = 0; k < queries.length; k++) {
					System.out.println(queries[k]);
					//System.out.println("\"" +
					//	queries[k].replace("\n", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("  ", " ")
					//		.replace("\"", "\\\\\\\"")
					//		+"\" \\");
				}
			}
		}
		
		/*
		for (double d: g.selectivities) {
			System.out.println(g.definePolygonForSelectivity(Shape.HEXAGON_SMALL, d));
		}
		System.out.println();
		for (double d: g.selectivities) {
			System.out.println(g.definePolygonForSelectivity(Shape.POINT, d));
		}
		*/
		
		System.out.println("Maximum tag generated: " + g.MAX_TAG_VALUE);
	}
}
