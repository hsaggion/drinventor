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
 * Interface to access the plain text importing methods of Dr. Inventor. <br/><br/>
 * 
 * To get an instance of a PlainTextLoader by the {@link edu.upf.taln.dri.lib.loader.PlainTextLoader PlainTextLoader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getPlainTextLoader() getPlainTextLoader()}.
 * 
 *
 */
public interface PlainTextLoader {
	
	/**
	 * Convert a plain text to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * IMPORTANT: the text should be UTF-8 encoded
	 * 
	 * @param absoluteFilePath the absolute path of the (plain) text file to convert
	 * @return
	 * @throws DRIexception
	 */
	public Document parsePlainText(String absoluteFilePath) throws DRIexception;
	
	/**
	 * Convert a plain text to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * IMPORTANT: the text should be UTF-8 encoded
	 * 
	 * @param file the (plain) text file to import
	 * @return
	 * @throws DRIexception
	 */
	public Document parsePlainText(File file) throws DRIexception;
	
	/**
	 * Convert a plain text to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * IMPORTANT: the text should be UTF-8 encoded
	 * 
	 * @param url the URL of the plain text file to convert
	 * @return
	 * @throws DRIexception
	 */
	public Document parsePlainText(URL url) throws DRIexception;
	
	/**
	 * Convert a plain text to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * IMPORTANT: the text should be UTF-8 encoded
	 * 
	 * @param textToLoad the plain text to load into the {@link edu.upf.taln.dri.lib.model.Document Document}
	 * @param docName the document name can be retrieved by the method {@link edu.upf.taln.dri.lib.model.Document#getName() Document.getName()}. Left null or empty string if you do not need to specify any name for the document
	 * @return
	 * @throws DRIexception
	 */
	public Document parseString(String textToLoad, String docName) throws DRIexception;
	
	
}
