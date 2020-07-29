/*
 * ******************************************************************************************************
 * Dr. Inventor Text Mining Framework Java Library
 * 
 * This code has been developed by the Natural Language Processing Group of the
 * Universitat Pompeu Fabra in the context of the FP7 European Project Dr. Inventor
 * Call: FP7-ICT-2013.8.1 - Agreement No: 611383
 * 
 * Dr. Inventor Text Mining Framework Java Library is available under an open licence, GPLv3, for non-commercial applications.
 * ******************************************************************************************************
 */
package edu.upf.taln.dri.common.connector.crossref;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.connector.crossref.model.CrossRefResult;

/**
 * CrossRef connector example
 * 
 *
 */
public class ExampleCrossRef {

	private static Logger logger = Logger.getLogger(ExampleCrossRef.class.getName());
	
	public static void main(String[] args) {
		
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		String biblioEntry = "[ZDPSS01] ZHANG L., DUGAS-PHOCION G., SAMSON J.-S.,SEITZ S. M.: Single view modeling of free-form scenes. CVPR1 (2001), 990. 3";
		CrossRefResult parsingResults = CrossRefConn.parseCitations(biblioEntry, 15);
		logger.info("CrossRef result: " + ((parsingResults != null) ? parsingResults.toString() : "NULL"));
		
		biblioEntry = "21. David Gunning, Vinay K. Chaudhri, Peter E. Clark, Ken Barker, Shaw-Yi Chaw, Mark Greaves, Benjamin Grosof, Alice Leung, David D. McDonald, Sunil Mishra, John Pacheco, Bruce Porter, Aaron Spaulding, Dan Tecuci, and Jing Tien. Project Halo update: Progress toward Digital Aristotle. AI Magazine, 31(3):33–58, 2010.";
		parsingResults = CrossRefConn.parseCitations(biblioEntry, 15);
		logger.info("CrossRef result: " + ((parsingResults != null) ? parsingResults.toString() : "NULL"));
		
		biblioEntry = null;
		parsingResults = CrossRefConn.parseCitations(biblioEntry, 15);
		logger.info("CrossRef result: " + ((parsingResults != null) ? parsingResults.toString() : "NULL"));

		
	}

}
