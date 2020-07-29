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
package edu.upf.taln.dri.module.importer.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * Class including static methods to normalize the XML files generated by PDFX.
 * 
 *
 */
public class UtilPDFX {

	private static Logger logger = Logger.getLogger(UtilPDFX.class.getName());

	public static StringBuffer processPDFX_XML(File fileToProcess) throws Exception {

		if(fileToProcess == null) {
			logger.info("ERROR: required to sanitize an XML file that does not exist!!!");
			throw new Exception("ERROR READING FILE");
		}

		StringBuffer outputString = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(fileToProcess), "UTF8") );

			Integer lineNum = 0;
			String appoString;
			while ((appoString = in.readLine()) != null) {
				// appoString = new String(appoString.getBytes(), "UTF-8").trim();

				// Processing line
				//   1 - Wrong hypenation
				appoString = appoString.replace("- ", "");

				// Adding line to output
				if(lineNum == 0) {
					outputString.append(appoString);
				}
				else {
					outputString.append("\n" +  appoString);
				}

				lineNum++;
			}

			in.close();
		} 
		catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			throw e;
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}

		return outputString;
	}


	public static String processPDFX_XMLformString(String stringToProcess) throws Exception {

		if(stringToProcess == null) {
			logger.info("ERROR: required to sanitize an XML file that does not exist!!!");
			throw new Exception("ERROR READING FILE");
		}

		boolean occurredReplacements = true;
		while (occurredReplacements) {
			int originalLength = stringToProcess.length();
			// Processing line
			//   1 - Wrong hypenation
			stringToProcess = stringToProcess.replace("- ", "");
			
			if(originalLength == stringToProcess.length()) {
				occurredReplacements = false;
			}
		}
		
		return stringToProcess;
	}

}
