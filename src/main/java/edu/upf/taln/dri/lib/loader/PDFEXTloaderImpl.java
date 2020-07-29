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
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.tidy.Tidy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import edu.upf.taln.dri.common.analyzer.pdf.PDFPaperParser;
import edu.upf.taln.dri.common.connector.pdfext.PDFextConn;
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
public class PDFEXTloaderImpl implements PDFloader {

	private static Logger logger = Logger.getLogger(PDFEXTloaderImpl.class);

	private static SAXParserFactory factory = SAXParserFactory.newInstance();
	static {
		factory.setValidating(false);
		factory.setNamespaceAware(true);
	}

	@Override
	public Document parsePDF(byte[] PDFbyteArray, String PDFfileName) throws DRIexception {
		Document retDocument = null;

		// Execute PDFX
		boolean correctlyConverted = false;
		String PDFEXTresult = "";

		for(int i = 0; i < 1; i++) {
			PDFEXTresult = PDFextConn.processPDF(PDFbyteArray, null, 720, PDFfileName);

			if(PDFEXTresult.length() > 600) {
				correctlyConverted = true;
			}

			if(correctlyConverted) {
				break;
			}
		}

		if(correctlyConverted) {
			// Pre-process document to correctly manage false hypenations
			PDFEXTresult = correctHypenationNewlines(PDFEXTresult);

			// Check if the generated XML is well formed
			String XMLerror = this.checkIfXMLIsWellFormed(PDFEXTresult);
			if(XMLerror != null) {
				System.err.println("XML file is not well-formed (length: " + ((PDFEXTresult != null) ? PDFEXTresult.length() : "NULL") + "):");
				logger.error("XML file is not well-formed (length: " + ((PDFEXTresult != null) ? PDFEXTresult.length() : "NULL") + "):");

				System.err.println("ERROR: " + ((XMLerror != null) ? XMLerror: "NULL"));
				logger.error("ERROR: " + ((XMLerror != null) ? XMLerror: "NULL"));

				System.err.println("Trying to tidy...");
				logger.error("Trying to tidy...");
				/* Jtidy */
				Tidy tidy = new Tidy(); // obtain a new Tidy instance
				tidy.setXmlTags(true);
				tidy.setXmlOut(true);

				boolean tidyCorrected = false;
				try {
					InputStream inStr = IOUtils.toInputStream(PDFEXTresult, "UTF-8");
					ByteArrayOutputStream outStr = new ByteArrayOutputStream();
					tidy.parse(inStr, outStr); // run tidy, providing an input and output stream
					String PDFEXTresultNEW = new String(outStr.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
					if(PDFEXTresultNEW != null && PDFEXTresultNEW.length() > new Integer(PDFEXTresult.length() / 2)) {
						PDFEXTresult = PDFEXTresultNEW;

						System.err.println("Tidy correctly finalized - tidy file length " + ((PDFEXTresult != null) ? PDFEXTresult.length() : "NULL"));
						logger.error("Tidy correctly finalized - tidy file length " + ((PDFEXTresult != null) ? PDFEXTresult.length() : "NULL"));
						tidyCorrected = true;

					}
					else {
						System.err.println("Impossible to correct the XML errors with Jtidy.");
						logger.error("Impossible to correct the XML errors with Jtidy.");
					}
				}
				catch(Exception e) {
					// Do nothing 
				}

				if(!tidyCorrected) {
					Util.notifyException("XML errors in PDFext output ", 
							new Exception(((XMLerror != null) ? XMLerror : "NULL")), logger);
				}

			}

			if(PDFEXTresult == null || PDFEXTresult.length() < 600) {
				return null;
			}

			gate.Document gateDoc = null;
			try {
				gateDoc = Factory.newDocument(PDFEXTresult);

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

	public String correctHypenationNewlines(String inputXML) {
		if(inputXML == null) {
			return null;
		}

		// STEP 1: Remove white spaces inside divs
		String outputSTEP1 = "";

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(inputXML);
			ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			org.w3c.dom.Document doc = builder.parse(input);

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//div");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for(int nIndex = 0; nIndex < nl.getLength(); nIndex++) {
				Node node = nl.item(nIndex);

				if(node.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				NodeList childOfDiv = node.getChildNodes();
				if(childOfDiv.getLength() == 1) {
					String textOfNode = node.getTextContent();
					textOfNode = " " + textOfNode.trim();
					node.setTextContent(textOfNode);
				}
				else {
					logger.info("Multiple children element!");
				}
			}

			// Normalize
			org.w3c.dom.Element el = doc.getDocumentElement();
			el.normalize();

			// Serialize to string
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();

			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSOutput lsOutput =  domImplementationLS.createLSOutput();
			lsOutput.setEncoding("UTF-8");
			Writer stringWriter = new StringWriter();
			lsOutput.setCharacterStream(stringWriter);
			lsSerializer.write(doc, lsOutput);

			outputSTEP1 = stringWriter.toString();

		} catch (Exception e1) {
			e1.printStackTrace();
			outputSTEP1 = inputXML;
		}

		// STEP 2: properly treat false hypenyms

		String outputSTEP2 = "";

		String[] inputXMLlines = outputSTEP1.split("\n");
		String multiLineString = "";
		for(String inputXMLline : inputXMLlines) {
			if(inputXMLline != null && !inputXMLline.equals("")) {

				// If the XML line ends with a false hypenation, put the 
				if(inputXMLline.trim().endsWith("<false-hyphen/></div>") || inputXMLline.trim().endsWith("<false-hyphen /></div>") || inputXMLline.trim().endsWith("-</div>")) {
					try {
						String inputLineNoXML = inputXMLline.trim().replaceAll("<[^>]+>", "");
						String inputLineUpToFirstSpace = inputLineNoXML.split(" ")[0];
						Integer positionLast = multiLineString.lastIndexOf("<false-hyphen/></div>");
						if(positionLast != -1) {
							multiLineString = multiLineString.substring(0, positionLast) + inputLineUpToFirstSpace + multiLineString.substring(positionLast, multiLineString.length());
							inputXMLline = inputXMLline.trim().replaceFirst(inputLineUpToFirstSpace, "");
						}
					} catch(Exception e) {
						// Do nothing
					}

					multiLineString += inputXMLline.trim(); // Replace "-</div>" for "</div>"?
				}
				else {
					if(multiLineString == null || multiLineString.equals("")) {
						outputSTEP2 += (outputSTEP2.trim().equals("")) ? inputXMLline.trim() : "\n" + inputXMLline.trim();
					}
					else {
						try {
							String inputLineNoXML = inputXMLline.trim().replaceAll("<[^>]+>", "");
							String inputLineUpToFirstSpace = inputLineNoXML.split(" ")[0];
							Integer positionLast = multiLineString.lastIndexOf("<false-hyphen/></div>");
							if(positionLast != -1) {
								multiLineString = multiLineString.substring(0, positionLast) + inputLineUpToFirstSpace + multiLineString.substring(positionLast, multiLineString.length());
								inputXMLline = inputXMLline.trim().replaceFirst(inputLineUpToFirstSpace, "");
							}
						} catch(Exception e) {
							// Do nothing
						}
						multiLineString += inputXMLline.trim();
						outputSTEP2 += (outputSTEP2.trim().equals("")) ? multiLineString : "\n" + multiLineString;

						// System.out.println("*** ML: " + multiLineString);

						multiLineString = "";
					}
				}

			}
		}

		// STEP 3: remove superflous newlines
		StringBuffer outputSTEP3 = new StringBuffer();

		try {
			String[] lines = outputSTEP2.split("\n");
			for(int lineIndex = 0; lineIndex < lines.length; lineIndex++) {

				if(lines[lineIndex].startsWith("<?xml") || lines[lineIndex].trim().endsWith(".</div>") || lines[lineIndex].trim().endsWith("?</div>") || lines[lineIndex].trim().endsWith("!</div>")) {
					if(lines[lineIndex].startsWith("<?xml")) {
						outputSTEP3.append(lines[lineIndex].trim().replace("\"?>", "\" ?>").replace("UTF-16", "UTF-8") + "\n");
					}
					else {
						outputSTEP3.append(lines[lineIndex].trim() + "\n");
					}
				}
				else {
					outputSTEP3.append(lines[lineIndex].trim());
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return outputSTEP3.toString();
	}

	// http://www.java2s.com/Code/Java/XML/HandlingSAXerrorsduringparsing.htm
	public String checkIfXMLIsWellFormed(String aXml){

		SaxHandler handler = new SaxHandler();

		try{
			SAXParser parser = factory.newSAXParser();
			parser.parse(IOUtils.toInputStream(aXml, "UTF-8"), handler);

			return null;
		} catch(SAXException e) {
			return (handler != null) ? handler.parseErrors : "Generic parse error";
		} catch(IOException io) {
			return (handler != null) ? handler.parseErrors : "Generic parse error";
		} catch (ParserConfigurationException e) {
			return (handler != null) ? handler.parseErrors : "Generic parse error";
		}

	}

	public class SaxHandler extends DefaultHandler {

		public String parseErrors = "";

		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if (qName.equals("order")) {
			}
		}

		public void error(SAXParseException ex) throws SAXException {
			System.out.println("ERROR: [at " + ex.getLineNumber() + "] " + ex);
			parseErrors += "ERROR: [at " + ex.getLineNumber() + "] " + ex + "\n";
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			System.out.println("FATAL_ERROR: [at " + ex.getLineNumber() + "] " + ex);
			parseErrors += "FATAL_ERROR: [at " + ex.getLineNumber() + "] " + ex + "\n";
		}

		public void warning(SAXParseException ex) throws SAXException {
			System.out.println("WARNING: [at " + ex.getLineNumber() + "] " + ex);
			parseErrors += "WARNING: [at " + ex.getLineNumber() + "] " + ex + "\n";
		}
	}
}
