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
package edu.upf.taln.dri.lib.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Implementation of the plain text importing methods of Dr Inventor. <br/><br/>
 * 
 * To get an instance of a PDFimporter by the {@link edu.upf.taln.dri.lib.loader.PlainTextLoader PlainTextLoader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getPlainTextLoader() getPlainTextLoader()}.
 * 
 *
 */
public class PlainTextLoaderImpl implements PlainTextLoader {

	private static Logger logger = Logger.getLogger(PlainTextLoaderImpl.class);

	public static Boolean proxyEnabled = false;
	public static String proxyScheme = "http";
	public static String proxyHostName = "proxy";
	public static Integer proxyPort = 8080;
	public static Integer socketTimeoutInMs = 120000;
	
	@Override
	public Document parsePlainText(String absoluteFilePath) throws DRIexception {

		if(absoluteFilePath == null || absoluteFilePath.length() == 0) {
			throw new InvalidParameterException("Invalid (plain) text file absolute path (null or empty String)");
		}

		File inputPlainTextFile = new File(absoluteFilePath);
		if(!inputPlainTextFile.exists()) {
			throw new ResourceAccessException("The (plain) text file at:'" + absoluteFilePath + "' does not exist");
		}

		Document retDocument = parsePlainText(inputPlainTextFile);

		return retDocument;
	}

	@Override
	public Document parsePlainText(File file) throws DRIexception {

		if(file == null) {
			throw new InvalidParameterException("Invalid File object (null)");
		}

		if(!file.exists()) {
			throw new ResourceAccessException("Invalid File object (does not exist)");
		}

		Document retDocument = null;

		StringBuffer strBuffer = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(file), "UTF-8"));

			String str;
			while ((str = in.readLine()) != null) {
				strBuffer.append(str + "\n");
			}

			in.close();

			retDocument = parseString(strBuffer.toString(), file.getName());
		} 
		catch (UnsupportedEncodingException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (IOException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}


		return retDocument;
	}

	@Override
	public Document parsePlainText(URL url) throws DRIexception {
		if(url == null) {
			throw new InvalidParameterException("Invalid URL (null)");
		}

		Document retDocument = null;

		StringBuffer strBuffer = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader( new InputStreamReader(url.openStream()));

			String str;
			while ((str = in.readLine()) != null) {
				strBuffer.append(str + "\n");
			}

			in.close();

			retDocument = parseString(strBuffer.toString(), url.toURI().toASCIIString());
		} 
		catch (UnsupportedEncodingException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (IOException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}


		return retDocument;
	}

	@Override
	public Document parseString(String textToLoad, String docName) throws DRIexception {

		if(textToLoad == null) {
			throw new InvalidParameterException("Invalid plain text string (null)");
		}

		if(textToLoad.equals("")) {
			throw new InvalidParameterException("Invalid plain text string (empty)");
		}

		Document retDocument = null;

		gate.Document gateDoc = null;
		try {
			gateDoc = Factory.newDocument(textToLoad);
		} catch (ResourceInstantiationException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		if(gateDoc != null) {
			String fileName = (docName != null) ? docName : "";
			gateDoc.getFeatures().put("name", fileName);

			// OLD CODE, NEVER INSTANTIATE DOCUMENT IMPL OBJECTS: new DocumentImpl(gateDoc);
			retDocument = edu.upf.taln.dri.lib.Factory.createNewDocument();
			retDocument.loadXMLString(gateDoc.toXml());
			
			gateDoc.cleanup();
			Factory.deleteResource(gateDoc);
		}

		return retDocument;
	}

}
