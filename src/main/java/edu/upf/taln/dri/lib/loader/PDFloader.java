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
 * Interface to access the PDF importing methods of Dr. Inventor. <br/><br/>
 * 
 * To get an instance of a PDFloader by the {@link edu.upf.taln.dri.lib.loader.PDFloader PDFloader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getPDFloader() getPDFloader()}.
 * 
 *
 */
public interface PDFloader {
	
	/**
	 * Convert a PDF file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param PDFbyteArray the byte array of the PDF file
	 * @param PDFfileName considered if not null or empty
	 * @return
	 * @throws DRIexception
	 */
	public Document parsePDF(byte[] PDFbyteArray, String PDFfileName) throws DRIexception;
	
	/**
	 * Convert a PDF file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param absoluteFilePath the absolute path of the PDF file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parsePDF(String absoluteFilePath) throws DRIexception;
	
	/**
	 * Convert a PDF file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param file the PDF file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parsePDF(File file) throws DRIexception;
	
	/**
	 * Convert a PDF file to a {@link edu.upf.taln.dri.lib.model.Document Document}
	 * 
	 * @param url the URL of the file to convert
	 * @return
	 * @throws DRIexception 
	 */
	public Document parsePDF(URL url) throws DRIexception;
	
}
