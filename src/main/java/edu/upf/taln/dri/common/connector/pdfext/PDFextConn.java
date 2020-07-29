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
package edu.upf.taln.dri.common.connector.pdfext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.connector.pdfext.localappo.PDFEXTresult;
import edu.upf.taln.dri.common.connector.pdfext.localappo.PDFextStatic;
import edu.upf.taln.dri.common.util.Util;

/**
 * Converting papers in PDF format to XML by means of PDFext (http://pdfext.taln.upf.edu/).
 * 
 * EXPERIMENTAL!
 * 
 *
 */
public class PDFextConn {

	private static Logger logger = Logger.getLogger(PDFextConn.class);	

	private static final String serviceURL = "http://pdfext.taln.upf.edu/api/pdfext/upload";
	
	// When set to true, if PDF2HTMLex is installed locally, the PDF to XML conversion is performed locally
	private static boolean localConversionEnabled = true;
	
	private static Random rnd = new Random();
	
	// Proxy usage parameters
	public static boolean useProxy = false;
	public static String proxyScheme = "http";
	public static String proxyHost = "88.150.156.39";
	public static Integer proxyPort = 8080;

	private static CloseableHttpClient httpClient = null;

	static {
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase("timeout")) {
						try {
							return Long.parseLong(value) * 1000;
						} catch(NumberFormatException ignore) {
							/* Do nothing */
						}
					}
				}
				return 30 * 1000;
			}

		};

		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setMaxTotal(5);
		poolingConnManager.setDefaultMaxPerRoute(5);

		httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).setKeepAliveStrategy(myStrategy).build();
	}

	/**
	 * Get an PDF file (max 5Mb) by means of its path and transform it to an XML annotated file by means of
	 * the PDFX Web Service (http://pdfx.cs.man.ac.uk/).
	 * 
	 * @param inputFilePath
	 * @param timeout set the socket timeout in milliseconds
	 * @return
	 */
	public static Map<String, String> processPDFfile(String inputFilePath, int timeout) {
		logger.debug("Start processing file " + inputFilePath);

		Map<String, String> retMap = new HashMap<String, String>();

		// Read file
		if(StringUtils.isBlank(inputFilePath)) {
			retMap.put("No file", "Incorrect or empty PDF file path");
			return retMap;
		}

		File file = new File(inputFilePath);

		if(!file.exists() || !file.isFile()) {
			retMap.put("No file", "PDF file empty or not existing");
			return retMap;
		}

		Path path = Paths.get(file.getAbsolutePath());
		byte[] data = {};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			Util.notifyException("Obtaining PDF as byte array", e, logger);
		}

		String result = processPDF(data, "", timeout, file.getName());
		retMap.put(file.getName().replace(".pdf", "") + "_PDFext.xml", result.toString());

		logger.debug("End converting to XML file (PDFext): " + inputFilePath);

		return retMap;
	}

	/**
	 * Get a PDF file as a byte array and transform it to an XML annotated file by means of
	 * the PDFext Web Service (http://pdfext.taln.upf.edu/).
	 * 
	 * @param inputBytes input byte array
	 * @param timeout set the socket timeout in milliseconds
	 * @return
	 */
	public static String processPDF(byte[] inputBytes, String tags, int timeout, String fileName) {
		
		/* START PDF TO XML CONVERSION LOCAL */
		if(localConversionEnabled) {
			// Store inputBytes in temp folder
			String storageAppoFolder = null;
			try {
				// Retrieve tomcat tmporal folder path
				String temporalDirPath = System.getProperty("java.io.tmpdir");
				File temporalFolder = new File(temporalDirPath);
				if(temporalFolder != null && temporalFolder.exists() && temporalFolder.isDirectory()) {

					// Create CACHE dir in tomcat tmporal folder path, if not existing
					String temporalFolderCACHEpath = (temporalDirPath.endsWith(File.separator)) ? temporalDirPath + "CACHE" : temporalDirPath + File.separator + "CACHE";
					File temporalFolderCACHE = new File(temporalFolderCACHEpath);

					if(temporalFolderCACHE == null || !temporalFolderCACHE.exists() && !temporalFolderCACHE.isDirectory() ) {
						boolean dirCreated = temporalFolderCACHE.mkdir();
						logger.info("CACHE DISABLED - Temporal folder dir correctly created " + dirCreated);
					}

					// Set CACHE starageFolder if correctly created
					temporalFolderCACHE = new File(temporalFolderCACHEpath);
					if(temporalFolderCACHE != null && temporalFolderCACHE.exists() && temporalFolderCACHE.isDirectory()) {
						storageAppoFolder = temporalFolderCACHEpath;
						logger.info("CACHE DISABLED - Temporal folder dir correctly set to " + temporalFolderCACHEpath);
					}
					else {
						throw new Exception("Impossible to set cache temporary folder.");
					}
				}
				else {
					throw new Exception("Impossible to set cache temporary folder.");
				}
			} catch (Exception e) {
				logger.error("Impossible to set cache temporary folder.");
				e.printStackTrace();
			}
			
			if(storageAppoFolder != null) {
				// Sotre byte array to file
				String fileNamePDFappo = "PDF_" + rnd.nextInt(100000) + ".pdf";
				File filePDFappo = new File(storageAppoFolder, fileNamePDFappo);
				
				if(!filePDFappo.exists()) {
					try {
						filePDFappo.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						logger.error("Impossible create PDF file in cache temporary folder.");
					}
				}
				
				filePDFappo = new File(storageAppoFolder, fileNamePDFappo);
				
				if(filePDFappo != null && filePDFappo.exists() && filePDFappo.isFile()) {
					try {
						FileUtils.writeByteArrayToFile(new File(filePDFappo.getAbsolutePath()), inputBytes);
						
						PDFEXTresult result = PDFextStatic.convertPDF(filePDFappo.getAbsolutePath(), storageAppoFolder);
						
						if(result != null && result.getResXML() != null && result.getResXML().length() > 300) {
							return result.getResXML();
						}
						else {
							logger.error("Impossible to store PDF in cache temporary folder (3 - XML conversion result null or smaller than 300 chars).");
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						logger.error("Impossible to store PDF in cache temporary folder (2 - Exception while converting file to XML). - " + e.getClass().toString());
					} finally {
						// Delete appo PDF file
						logger.info("Delete appo PDF file: " + filePDFappo.delete());
					}
				}
				else {
					logger.error("Impossible to store PDF in cache temporary folder (1 - file not correctly created in folder).");
				}
			}
			else {
				logger.error("Impossible to set cache temporary folder.");
			}
			
			return "ERROR PDF TO XML CONVERSION (LOCAL)";
		}
		/* END PDF TO XML CONVERSION LOCAL */
		
		
		StringBuffer result = new StringBuffer("");

		// Read file
		if(inputBytes.length == 0) {
			result.append("Incorrect or empty PDF file");
			return result.toString();
		}

		HttpPost post = new HttpPost(serviceURL);

		Builder requestBuilder = RequestConfig.custom()
				.setConnectTimeout(360 * 1000) // Until a connection with the server is established.
				.setConnectionRequestTimeout(360 * 1000) // Waiting a connection from the manager, then ConnectionPoolTimeoutException
				.setSocketTimeout(timeout * 1000); // Maximum period inactivity between two consecutive data packets arriving at client side after connection is established.

		if(useProxy) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort, proxyScheme);
			requestBuilder.setProxy(proxy);
		}

		RequestConfig config = requestBuilder.build();

		post.setConfig(config);

		// Proper header settings
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if(StringUtils.isNotBlank(tags)) {
			builder.addTextBody("tag", tags);
		}
		builder.addTextBody("isXML", "true"); // Always convert to XML (not to HTML)
		builder.addBinaryBody("file", inputBytes, ContentType.APPLICATION_OCTET_STREAM, (fileName != null && !fileName.equals("")) ? fileName : "PDF_PAPER.pdf");
		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		// Invoke PDFX service
		String response = null;
		try {
			// When using a ResponseHandler, HttpClient will automatically take care of ensuring release of the connection 
			// back to the connection manager regardless whether the request execution succeeds or causes an exception.
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
						StringBuffer result = new StringBuffer();
						String line = "";
						while ((line = rd.readLine()) != null) {
							result.append(line + "\n");
						}
						rd.close();
						return result.length() > 0 ? result.toString() : null;
					} else {
						return "ERROR CODE: " + status;
					}
				}

			};

			logger.info("Sending PDFX parsing request for file with length " + inputBytes.length);
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(post, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.info("PDFext parsing request processed in " + (endTime - startTime)  + " milliseconds with response: " + 
					((response != null) ? (response.length() > 15 ? response.substring(0, 15) : response) :"NULL") + " (sent file length: " + inputBytes.length + ")");
		} catch (ClientProtocolException e) {
			logger.error("PDFext processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("PDFext processing exception / Exception " + e.getMessage());
		}

		return result.append( (response != null) ? response : "ERROR" ).toString();
	}
	
	/**
	 * Convert by means of PDFext a PDF file or recursively all PDF files in a directory.
	 * 
	 * @param fileOrDirFullPath
	 * @param tags
	 * @param recursiveDir
	 * @param timeout
	 * @return
	 */
	public static int convertFilesAndStore(String fileOrDirFullPath, String tags, boolean recursiveDir, int timeout) {
		
		int convertedPDFcount = 0;
		File parent = new File(fileOrDirFullPath);

		if(parent.exists() && parent.isDirectory()) {
			logger.debug("Converting PDF files from directory: " + parent.getAbsolutePath() + " - recursively: " + recursiveDir);

			// Get all sub-directories and files
			File[] children = parent.listFiles();
			for(File file : children) {
				if(!recursiveDir && file.isDirectory()) {
					continue;
				}
				convertedPDFcount += convertFilesAndStore(file.getAbsolutePath(), tags, recursiveDir, timeout);
			}
		}
		else {
			if(parent.getName().toLowerCase().endsWith(".pdf")) {

				Path filePath = Paths.get(parent.getAbsolutePath());
				byte[] originalPDF = {};
				try {
					originalPDF = Files.readAllBytes(filePath);
				} catch (IOException e) {
					Util.notifyException("Reading PDF file", e, logger);
				}
				
				try {
					boolean correctlyConverted = false;
					String conversionResult = "";

					logger.info("       > Processing PDF file (PDFext): " + parent.getName());
					
					for(int i = 0; i < 2; i++) {
						
						conversionResult = processPDF(originalPDF, tags, timeout, parent.getName());
						
						if(conversionResult.length() > 600) {
							correctlyConverted = true;
						}

						if(correctlyConverted) {
							logger.info("          > attempt number " + (i+1) + " over " + 2 + "  > OK");
							break;
						}
						else {
							logger.info("          > attempt number " + (i+1) + " over " + 2 + "  > NOT CORRECTLY CONVERTED");
						}
					}
					
					if(correctlyConverted) {
						convertedPDFcount++;

						// Store PDF
						String fileName = parent.getAbsolutePath().replace(".pdf", "") + "_PDFX.xml";
						File file = new File(fileName);

						if (!file.exists()) {
							file.createNewFile();
						}

						FileWriter fw = new FileWriter(file.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(conversionResult);
						bw.flush();
						bw.close();
					}
					else {
						logger.info("Error converting PDF files to XML - " + parent.getName());
					}
				}
				catch(Exception e) {
					Util.notifyException("Converting PDF file", e, logger);
				}
			}
		}

		return convertedPDFcount;
	}
	
	
	public static void main(String[] args) {
		convertFilesAndStore(args[0], null, true, 720);
	}

}
