package gr.uoa.di.rdf.Geographica.generators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Dummy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: Dummy <file>");
		}
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(args[0]));
			while ((line = br.readLine()) != null) {
				String subject = null;
				String predicate = null;
				String object = null;
//				StringTokenizer tknz = new StringTokenizer(line, " ");
//				int i = 0;
//				while (tknz.hasMoreElements()) {
//					String token = tknz.nextToken();
//					i++;
//					if (i == 1) {
//						subject = token;
//					} else if (i == 2) {
//						predicate = token;
//					} if (i == 3) {
//						
//						if (token.startsWith("<")) {
//							// ignore objects that are not literals
//							object = null;
//						}
//						int lastPeriod = token.lastIndexOf(".");
//						object = token.substring(0, lastPeriod - 1);
//					}
//				}
				

				if (line.length() == 0) {
					continue;
				}

				if (line.startsWith("#"))
					continue;

				int firstSpace = line.indexOf(" ");
				int secondSpace = line.indexOf(" ", firstSpace+1);
				int lastPeriod = line.lastIndexOf(".");

				if (firstSpace == -1 || secondSpace == -1 || lastPeriod == -1 ) {
					System.err.println("Line: " + line);
					System.err.println("firstSpace: "+firstSpace);
					System.err.println("secondSpace:"+secondSpace);
					System.err.println("lastPeriod:"+lastPeriod);
					//System.exit(0);
					continue;
				}

				subject = line.substring(0, firstSpace);
				predicate = line.substring(firstSpace+1, secondSpace);
				object = line.substring(secondSpace+1, lastPeriod-1);
				
				if (!object.startsWith("\"")) {
					System.out.println(line);
					continue;
				}
				
				int lastQuotes = object.lastIndexOf("\"");
				// value
				int carretPos = object.indexOf("^", lastQuotes);
				if (carretPos == -1) {
					// untyped literal
					System.out.println(line);
					continue;
				}
				String literal = object.substring(1, carretPos-1);
				// datatype
				String datatype = object.substring(carretPos+2).replaceAll("<", "").replaceAll(">", "").trim();
				
				// if not datatype sysout
				if (!datatype.equals("http://www.openlinksw.com/schemas/virtrdf#Geometry")) {
					System.out.println(line);
					continue;
				}
				
				// spatial type
				int parenthesisPos = literal.indexOf("(");
				String type = literal.substring(0, parenthesisPos).trim();
				
				String newObject = null;
				if (type.equals("POINT")) {
					System.out.println(line);
					continue;
				} else if (type.equals("LINESTRING")) {
					int start = literal.indexOf("(");
					int stop = literal.indexOf(",");
					newObject = literal.substring(start + 1, stop -1).trim();
				} else if (type.equals("POLYGON")) {
					int start = literal.indexOf("(");
					int stop = literal.indexOf(",");
					newObject = literal.substring(start + 2, stop -1).trim();
				} else if (type.equals("MULTIPOLYGON")) {
					int start = literal.indexOf("(");
					int stop = literal.indexOf(",");
					newObject = literal.substring(start + 3, stop -1).trim();
				} else {					
					System.err.println("File     : " + args[0]);
					System.err.println("Triple   : " + line);
					System.err.println("Subject  : " + subject);
					System.err.println("Predicate: " + predicate);
					System.err.println("Object   : " + object);
					System.err.println("Type     : " + type);
					System.exit(0);
				}	
				
				//print triple
				System.out.println(subject + " " + predicate + " \"POINT(" + newObject + ")\"^^<http://www.openlinksw.com/schemas/virtrdf#Geometry> .");
			}

		} catch (IOException e) {
			System.out.println("Line: " + line);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Line: " + line);
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
