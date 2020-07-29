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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.analyzer.pdf.PDFPaperParser;
import edu.upf.taln.dri.common.connector.pdfx.PDFXConn;
import edu.upf.taln.dri.common.connector.proxy.PDFproxyConn;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.pdf.UtilPDFX;
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
public class PDFXloaderImpl implements PDFloader {

	private static Logger logger = Logger.getLogger(PDFXloaderImpl.class);

	public static Boolean proxyEnabled = false;
	public static String proxyScheme = "http";
	public static String proxyHostName = "proxy";
	public static Integer proxyPort = 8080;

	public static Boolean PDFXproxyEnabled = false;

	@Override
	public Document parsePDF(byte[] PDFbyteArray, String PDFfileName) throws DRIexception {
		Document retDocument = null;

		boolean compressed = false;
		byte[] compressedPDF = {};
		if(PDFbyteArray.length > 5242800) {
			compressedPDF = PDFXConn.pdfCompress(PDFbyteArray, 0.2f, false);
			if(compressedPDF.length > 5242800) {
				compressedPDF = PDFXConn.pdfCompress(PDFbyteArray, 0.2f, true);
				if(compressedPDF.length > 0) {
					compressed = true;
				}
			}
			else if(compressedPDF.length > 0) {
				compressed = true;
			}
		}

		if(compressed) {
			logger.info("Compressed PDF from " + PDFbyteArray.length + " (original size) to " + compressedPDF.length + " (new compressed size)");
			if(compressedPDF.length > 5242800) {
				logger.info("PDF length is greater than 5Mb (" + compressedPDF.length + " bytes) - the PDF file cannot be converted to XML. PDF files with size lower than 5Mb can be converted.");
				return null;
			}
		}

		// Execute PDFX
		boolean correctlyConverted = false;
		String PDFXresult = "";
		
		if(PDFXproxyEnabled) {
			for(int i = 0; i < 3; i++) {
				PDFXresult = PDFproxyConn.processPDF(((compressed) ? compressedPDF : PDFbyteArray), "NO_TAG", 2160);

				if(PDFXresult.length() > 600) {
					correctlyConverted = true;
				}
				else {
					logger.info("Waiting " + (120 * (i+1)) + " seconds before attempting again PDF conversion (conversion " + (i+1) + " over 3)");
					try {
						Thread.sleep((120l * new Long(i+1)) * 1000l);
					} catch (InterruptedException e) {
						Util.notifyException("Waiting before asking a new PDF conversion attempt", e, logger);
					}
				}

				if(correctlyConverted) {
					break;
				}
			}
		}
		else {
			for(int i = 0; i < 3; i++) {
				PDFXresult = PDFXConn.processPDF(((compressed) ? compressedPDF : PDFbyteArray), 720);
				
				if(PDFXresult.length() > 600) {
					correctlyConverted = true;
				}
				
				if(!correctlyConverted) {
					logger.info("Converion attempt " + (i + 1) + " over 3 failed: \n" + ((PDFXresult != null) ? PDFXresult : "NULL"));
				}
				
				
				if(correctlyConverted) {
					break;
				}
			}
		}

		if(correctlyConverted) {
			String paperXMLnormalized = PDFXresult;
			try {
				paperXMLnormalized = UtilPDFX.processPDFX_XMLformString(PDFXresult);
			} catch (Exception e) {
				Util.notifyException("Normalizing converted PDF file", e, logger);
			}

			gate.Document gateDoc = null;
			try {
				gateDoc = Factory.newDocument(paperXMLnormalized);
				
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
			Util.notifyException("Conversion of PDF file by PDFX", new Exception("Impossible to convert PDF file by PDFX (" + ((PDFXresult != null) ? PDFXresult : "NULL") + ")"), logger);
		}

		return retDocument;
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

		if(proxyEnabled) {
			PDFXConn.proxyHost = proxyHostName;
			PDFXConn.proxyPort = proxyPort;
			PDFXConn.proxyScheme = proxyScheme;
		}

		byte[] originalPDF = {};
		Path filePath = Paths.get(absoluteFilePath);
		try {
			originalPDF = Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while reading PDF file");
		}

		String fileName = (new File(absoluteFilePath)).getName().replace(".pdf", "") + "_PDFX.xml";
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
