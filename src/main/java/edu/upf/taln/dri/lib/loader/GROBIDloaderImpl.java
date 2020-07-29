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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

import edu.upf.taln.dri.common.analyzer.pdf.PDFPaperParser;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Factory;
import gate.creole.ResourceInstantiationException;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Implementation of the PDF loading methods of Dr Inventor. <br/><br/>
 * 
 * To get an instance of a PDFimporter by the {@link edu.upf.taln.dri.lib.loader.PDFloader PDFloader interface}, always use the
 * {@link edu.upf.taln.dri.lib.Factory Factory} method {@link edu.upf.taln.dri.lib.Factory#getPDFloader() getPDFloader()}.
 * 
 *
 */
public class GROBIDloaderImpl implements PDFloader {

	private static Logger logger = Logger.getLogger(GROBIDloaderImpl.class);

	private static Random rnd = new Random();
	private static boolean isInitialized = false;

	private static void initGROBID(String GROBIDhome, String GROBIDproperties) {
		// String pGrobidHome = "/home/francesco/Downloads/GORBID_TEST/grobid-grobid-parent-0.4.0/grobid-home";
		// String pGrobidProperties = "/home/francesco/Downloads/GORBID_TEST/grobid-grobid-parent-0.4.0/grobid-home/config/grobid.properties";

		if(isInitialized) {
			return;
		}
		else {
			try {
				logger.info("Initializing GROBID...");

				MockContext.setInitialContext(GROBIDhome, GROBIDproperties);      
				GrobidProperties.getInstance();

				logger.info("GROBID correctly initialized (home set to: " + ((GrobidProperties.get_GROBID_HOME_PATH() != null) ? GrobidProperties.get_GROBID_HOME_PATH() : "NULL") + ").");
				isInitialized = true;
			} 
			catch (Exception e) {
				Util.notifyException("Imitializing GROBID", e, logger);
			}

		}

	}

	@Override
	public Document parsePDF(byte[] PDFbyteArray, String PDFfileName) throws DRIexception {
		initGROBID(edu.upf.taln.dri.lib.Factory.GROBIDhome, edu.upf.taln.dri.lib.Factory.GROBIDproperties);

		Document retDocument = null;

		// Create temp file
		String tempFileName = rnd.nextInt(100000) + "_tempGROBID_PDF";
		File tempPDFfile = null;
		try {
			tempPDFfile = File.createTempFile(tempFileName, ".pdf");
			FileOutputStream fos = new FileOutputStream(tempPDFfile);
			fos.write(PDFbyteArray);
			fos.close();
		} catch (IOException e) {
			Util.notifyException("Creating temporal PDF file", e, logger);
		}


		// Execute GROBID
		String GROBIDresult = "";
		try {
			Engine engine = GrobidFactory.getInstance().createEngine();
			GROBIDresult = engine.fullTextToTEI(tempPDFfile, GrobidAnalysisConfig.defaultInstance());
		} 
		catch (Exception e) {
			Util.notifyException("Converting PDF by GROBID", e, logger);
		}

		// Delete temp file
		try {
			tempPDFfile.delete();
		} 
		catch (Exception e) {
			Util.notifyException("Deleting temporal PDF file", e, logger);
		}


		if(GROBIDresult != null && GROBIDresult.length() > 600) {

			gate.Document gateDoc = null;
			try {
				gateDoc = Factory.newDocument(GROBIDresult);

				if(gateDoc.getContent().size() <= 30l) {
					Util.notifyException("Document textual content long less than 30 chars", 
							new Exception("Ignored document - text contents too short."), logger);
					gateDoc = null;
				}
			} catch (ResourceInstantiationException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while instantiating Document from PDF file contents");
			}

			if(gateDoc != null) {
				// Set name feature of the document
				gateDoc.getFeatures().put("name", (StringUtils.isNotBlank(PDFfileName) ? PDFfileName : "NO_NAME"));

				// Set header parsing results
				String headerString = "";

				ByteArrayInputStream PDFinputStream = new ByteArrayInputStream(PDFbyteArray);
				List<String> headerSentences = PDFPaperParser.getHeaderSentences(PDFinputStream, true);

				if(headerSentences != null && headerSentences.size() > 0) {
					for(String headerLine : headerSentences) {
						headerLine = headerLine.replace("\n", " ").trim();
						headerString = (!headerString.equals("")) ? headerString + " <NL> " + headerLine : headerString + headerLine;
					}
				}

				gateDoc.getFeatures().put(ImporterBase.headerDOC_OrigDocFeat, headerString);

				// OLD CODE, NEVER INSTANTIATE DOCUMENT IMPL OBJECTS: new DocumentImpl(gateDoc);
				retDocument = edu.upf.taln.dri.lib.Factory.createNewDocument();
				retDocument.loadXMLString(gateDoc.toXml());

				gateDoc.cleanup();
				Factory.deleteResource(gateDoc);
			}
		}
		else {
			logger.info("Error while converting PDF file by GROBID");
		}

		// Empty temporary folder
		try {
			File GROBIDtempFolder = new File(edu.upf.taln.dri.lib.Factory.GROBIDhome + File.separator  + "tmp");
			if(GROBIDtempFolder != null && GROBIDtempFolder.exists() && GROBIDtempFolder.isDirectory()) {
				File[] files = GROBIDtempFolder.listFiles();
				if(files != null) {
					for(File f: files) {
						if(f.isDirectory()) {
							deleteFolder(f);
						} else {
							try {
								f.delete();
							}
							catch (Exception e) {
								// DO NOTHING
							}
						}
					}
				}
				
				files = GROBIDtempFolder.listFiles();
				logger.info("GROBID temporary folder empty (" + files.length +" files contained).");
			}
			else {
				logger.info("Impossible to remove contents of GROBID temporary folder.");
			}
		}
		catch (Exception e) {
			// DO NOTHING
		}

		return retDocument;
	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					try {
						f.delete();
					}
					catch (Exception e) {
						// DO NOTHING
					}
				}
			}
		}
		try {
			folder.delete();
		}
		catch (Exception e) {
			// DO NOTHING
		}
	}

	@Override
	public Document parsePDF(String absoluteFilePath) throws DRIexception {

		if(absoluteFilePath == null || absoluteFilePath.length() == 0) {
			throw new InvalidParameterException("Invalid PDF file absolute path (null or empty String)");
		}

		File inputPDF = new File(absoluteFilePath);
		if(!inputPDF.exists()) {
			throw new ResourceAccessException("The file at: '" + absoluteFilePath + "' does not exist");
		}

		Document retDocument = null;

		byte[] originalPDF = {};
		Path filePath = Paths.get(absoluteFilePath);
		try {
			originalPDF = Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while reading PDF file");
		}

		String fileName = (new File(absoluteFilePath)).getName().replace(".pdf", "") + "_GROBID.xml";
		retDocument = parsePDF(originalPDF, fileName);

		return retDocument;
	}

	@Override
	public Document parsePDF(File file) throws DRIexception {

		if(file == null) {
			throw new InvalidParameterException("Invalid File object (null)");
		}

		if(!file.exists()) {
			throw new ResourceAccessException("Invalid File object (does not exist)");
		}

		String absoluteFilePath = file.getAbsolutePath();

		Document retDocument = parsePDF(absoluteFilePath);

		return retDocument;
	}

	@Override
	public Document parsePDF(URL url) throws DRIexception {
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

			retDocument = parsePDF(tmpOut.toByteArray(), url.getPath().toString());

		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while instantiating Document from PDF URL");
		}

		return retDocument;
	}

}
