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
package edu.upf.taln.dri.common.connector.proxy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * PDFX connector example
 * 
 *
 */
public class ExamplePDFproxyConn {

	private static Logger logger = Logger.getLogger(ExamplePDFproxyConn.class.getName());
	
	public static void main(String[] args) {

		Logger.getRootLogger().setLevel(Level.INFO);
		
		PDFproxyConn.convertFilesAndStore("/home/francesco/Desktop/DRILIB_EXP/TEST", "TEST", true, 0.2f, true, 400);
		
		logger.info("Conversion completed");
	}

}
