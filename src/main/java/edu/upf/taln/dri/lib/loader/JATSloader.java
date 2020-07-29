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

import java.io.File;
import java.net.URL;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;

/**
 * Interface to access the JATS XML importing methods of Dr. Inventor. <br/><br/>
 * 
 * To get an instance of a JATSloader by the {@link edu.upf.taln.dri.lib.loader.JATSloader JATSloader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getJATSloader() getJATSloader()}.
 * 
 *
 */
public interface JATSloader {
	
	/**
	 * Convert a JATS XML file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param JATSbyteArray the byte array of the PDF file
	 * @param JATSfileName considered if not null or empty
	 * @return
	 * @throws DRIexception
	 */
	public Document parseJATS(byte[] JATSbyteArray, String JATSfileName) throws DRIexception;
	
	/**
	 * Convert a JATS XML file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param absoluteFilePath the absolute path of the PDF file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parseJATS(String absoluteFilePath) throws DRIexception;
	
	/**
	 * Convert a JATS XML file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param file the PDF file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parseJATS(File file) throws DRIexception;
	
	/**
	 * Convert a JATS XML file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param url the URL of the file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parseJATS(URL url) throws DRIexception;
	
}
