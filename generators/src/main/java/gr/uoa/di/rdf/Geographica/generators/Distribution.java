/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 */
package gr.uoa.di.rdf.Geographica.generators;

//import java.util.Random;

/**
 * @author Kostis Kyzirakos <kkyzir@di.uoa.gr>
 */
public class Distribution {	
	
//	private Distributions type;
	
//	private double min, max;
	
//	private static volatile long seed = 1232777807144237L;
	
	
	public Distribution(Distributions type, double min, double max) {
//		this.type = type;
//		this.min = min;
//		this.max = max;
		
		switch (type) {
		case UNIFORM:
			
			break;
		case ZIPF:
			
			break;
		default:
			break;
		}		
	}
	
	public double next() {
		double value = 0;
		
		
		
		return value;
	}
}