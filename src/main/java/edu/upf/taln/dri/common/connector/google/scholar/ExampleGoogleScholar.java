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
package edu.upf.taln.dri.common.connector.google.scholar;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.connector.google.scholar.model.GoogleScholarResult;

/**
 * Google Scholar connector example
 * 
 *
 */
public class ExampleGoogleScholar {

	private static Logger logger = Logger.getLogger(ExampleGoogleScholar.class.getName());
	
	public static void main(String[] args) {

		Logger.getRootLogger().setLevel(Level.INFO);
		
		String searchString = "Danqi Chen and Christopher D Manning. 2014. A fast and accurate dependency parser using neural networks. In EMNLP.";
		List<GoogleScholarResult> res = GoogleScholarConn.parseAddress(searchString, 3);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			logger.info(" RESULT: " + res.get(i).toString());
		}

		/*
		searchString = "Tagpedia";
		res = GoocleScholarConn.parseAddress(searchString, 3);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			System.out.println(" RESULT: " + res.get(i).toString());
		}

		searchString = "Dbpedia";
		res = GoocleScholarConn.parseAddress(searchString, 3);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			System.out.println(" RESULT: " + res.get(i).toString());
		}

		searchString = "Wikify";
		res = GoocleScholarConn.parseAddress(searchString, 31);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			System.out.println(" RESULT: " + res.get(i).toString());
		}

		searchString = "Solr";
		res = GoocleScholarConn.parseAddress(searchString, 3);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			System.out.println(" RESULT: " + res.get(i).toString());
		}

		searchString = "Apache";
		res = GoocleScholarConn.parseAddress(searchString, 3);
		System.out.println(" Search string: " + searchString);
		System.out.println(" TOTAL RESULT: " + res.size());
		for(int i = 0; i < res.size(); i++) {
			System.out.println(" RESULT: " + res.get(i).toString());
		}
		 */
	}

}
