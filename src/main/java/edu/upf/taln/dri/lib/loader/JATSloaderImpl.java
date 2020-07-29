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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Implementation of the JATS loading methods of Dr Inventor. <br/><br/>
 * 
 * To get an instance of a JATSloader by the {@link edu.upf.taln.dri.lib.loader.JATSloader JATSloader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getJATSloader() getJATSloader()}.
 * 
 *
 */
public class JATSloaderImpl implements JATSloader {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(JATSloaderImpl.class);


	@Override
	public Document parseJATS(byte[] JATSbyteArray, String JATSfileName) throws DRIexception {
		Document retDocument = null;

		String JATSXML = "";
		try {
			JATSXML = new String(JATSbyteArray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while reading contents of JATS XML file");
		}
		
		gate.Document gateDoc = null;
		try {
			// For PUBMED JATS XML add XML head & sanitize
			if(!JATSXML.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")) {
				JATSXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + JATSXML;
			}
			
			String correctHD = "<!DOCTYPE article PUBLIC \"-//NLM//DTD JATS (Z39.96) Journal Publishing DTD v1.1d3 20150301//EN\" \"http://jats.nlm.nih.gov/publishing/1.1d3/JATS-journalpublishing1.dtd\">";
			String originalHD = "<!DOCTYPE article PUBLIC \"-//NLM//DTD JATS (Z39.96) Journal Archiving and Interchange DTD v1.0 20120330//EN\" \"JATS-archivearticle1.dtd\">";
			JATSXML = JATSXML.replace(originalHD, correctHD);
			
			
			gateDoc = Factory.newDocument(JATSXML);
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while instantiating Document from JATS XML");
		}

		if(gateDoc != null) {
			// Set name feature of the document
			gateDoc.getFeatures().put("name", (StringUtils.isNotBlank(JATSfileName) ? JATSfileName : "NO_NAME"));
			
			// OLD CODE, NEVER INSTANTIATE DOCUMENT IMPL OBJECTS: new DocumentImpl(gateDoc);
			retDocument = edu.upf.taln.dri.lib.Factory.createNewDocument();
			retDocument.loadXMLString(gateDoc.toXml());
			
			gateDoc.cleanup();
			Factory.deleteResource(gateDoc);
		}

		return retDocument;
	}

	@Override
	public Document parseJATS(String absoluteFilePath) throws DRIexception {

		if(absoluteFilePath == null || absoluteFilePath.length() == 0) {
			throw new InvalidParameterException("Invalid JATS file absolute path (null or empty String)");
		}

		File inputPDF = new File(absoluteFilePath);
		if(!inputPDF.exists()) {
			throw new ResourceAccessException("The file at: '" + absoluteFilePath + "' does not exist");
		}

		Document retDocument = null;
		
		byte[] originalJATS = {};
		Path filePath = Paths.get(absoluteFilePath);
		try {
			originalJATS = Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while reading JATS XML file");
		}

		String fileName = (new File(absoluteFilePath)).getName().replace(".xml", "") + "_JATS.xml";
		retDocument = parseJATS(originalJATS, fileName);

		return retDocument;
	}

	@Override
	public Document parseJATS(File file) throws DRIexception {

		if(file == null) {
			throw new InvalidParameterException("Invalid File object (null)");
		}

		if(!file.exists()) {
			throw new ResourceAccessException("Invalid File object (does not exist)");
		}

		String absoluteFilePath = file.getAbsolutePath();

		Document retDocument = parseJATS(absoluteFilePath);

		return retDocument;
	}

	@Override
	public Document parseJATS(URL url) throws DRIexception {
		if(url == null) {
			throw new InvalidParameterException("Invalid URL (null)");
		}

		Document retDocument = null;

		try {
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36");
			connection.setRequestProperty("Accept", "application/pdf");

			InputStream in = connection.getInputStream();

			int contentLength = connection.getContentLength();

			ByteArrayOutputStream tmpOut;
			if (contentLength != -1) {
				tmpOut = new ByteArrayOutputStream(contentLength);
			} else {
				tmpOut = new ByteArrayOutputStream(45000000);
			}

			byte[] buf = new byte[2048];
			while (true) {
				int len = in.read(buf);
				if (len == -1) {
					break;
				}
				tmpOut.write(buf, 0, len);
			}
			in.close();
			tmpOut.close();

			retDocument = parseJATS(tmpOut.toByteArray(), url.getPath().toString());

		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while instantiating Document from the URL of a JATS XML file");
		}

		return retDocument;
	}
}
