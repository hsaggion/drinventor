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
package edu.upf.taln.dri.common.connector.proxy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import edu.upf.taln.dri.common.util.Util;

/**
 * Converting papers in PDF format to XML by means of PDFX (http://pdfx.cs.man.ac.uk/).
 * Also utility methods to compress their images are added. 
 * 
 *
 */
public class PDFproxyConn {

	private static Logger logger = Logger.getLogger(PDFproxyConn.class);	

	private static final String serviceURL = "http://pdfconv.taln.upf.edu/h2cw/api/p/upload/NO_TAG";

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

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				AuthScope.ANY,
				new UsernamePasswordCredentials("conv", "pdfconvpswd"));

		httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).setDefaultCredentialsProvider(credsProvider).setKeepAliveStrategy(myStrategy).build();
	}

	private static Random rnd = new Random();

	/**
	 * Compress the images included in a PDF file in order to reduce the file size.
	 * The input and output file names are includes full file paths.
	 * 
	 * @param inputPDF
	 * @param compressionFactor
	 * @param greyImages
	 * @return
	 */
	public static byte[] pdfCompress(byte[] inputPDF, float compressionFactor, boolean greyImages) {
		ByteArrayOutputStream pdfByteOutputStream = new ByteArrayOutputStream();

		compressionFactor = (compressionFactor <= 0f || compressionFactor >= 1f) ? 0.7f : compressionFactor;

		logger.debug("Start compressing PDF file with compression factor: " + compressionFactor);

		// Read the file
		PdfReader reader = null;
		try {
			reader = new PdfReader(inputPDF);
		} catch (IOException e) {
			Util.notifyException("Reading PDF contents", e, logger);
		}

		int n = reader.getXrefSize();
		PdfObject object;
		PRStream stream;


		// Look for image and manipulate image stream
		try {
			for (int i = 0; i < n; i++) {
				try {
					object = reader.getPdfObject(i);
					if (object == null || !object.isStream()) {
						continue;
					}

					stream = (PRStream)object;

					PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);

					if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
						int width = 10;
						int height = 10;

						BufferedImage img = null;

						if(!greyImages) {
							try {
								PdfImageObject image = new PdfImageObject(stream);

								BufferedImage bi = image.getBufferedImage();
								if (bi == null) continue;
								width = (int)(bi.getWidth() * compressionFactor);
								height = (int)(bi.getHeight() * compressionFactor);

								width = (width <= 0) ? 1 : width;
								height = (height <= 0) ? 1 : height;

								img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
								AffineTransform at = AffineTransform.getScaleInstance(compressionFactor, compressionFactor);

								Graphics2D g = img.createGraphics();
								g.drawRenderedImage(bi, at);
							} catch (Exception e) {
								Util.notifyException("Compressing PDF contents - compressing image (images by " + compressionFactor + ", force gray " + greyImages + ")", e, logger);
							}
						}
						else {
							img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

							Graphics2D g = img.createGraphics();
							g.setColor(Color.GRAY);
							g.fillRect(0, 0, width, height);
						}

						if(img != null) {
							ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
							ImageIO.write(img, "JPG", imgBytes);
							stream.clear();
							stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
							stream.put(PdfName.TYPE, PdfName.XOBJECT);
							stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
							stream.put(PdfName.FILTER, PdfName.DCTDECODE);
							stream.put(PdfName.WIDTH, new PdfNumber(width));
							stream.put(PdfName.HEIGHT, new PdfNumber(height));
							stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
							stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
						}
						else {
							throw new Exception("Compressing image with index " + i + ": null value.");
						}
					}
				} catch (Exception e) {
					Util.notifyException("Compressing PDF contents - element (images by " + compressionFactor + ", force gray " + greyImages + ")", e, logger);
				}
			}
		} catch (Exception e) {
			Util.notifyException("Compressing PDF contents (images by " + compressionFactor + ")", e, logger);
		}

		logger.debug("Compressed file with compression factor: " + compressionFactor);

		try {
			PdfStamper stamper = new PdfStamper(reader, pdfByteOutputStream, PdfWriter.VERSION_1_5);
			stamper.setFullCompression();
			stamper.close();
			reader.close();
		} catch (DocumentException e) {
			Util.notifyException("Output compressed PDF contents (images by " + compressionFactor + ")", e, logger);
		} catch (IOException e) {
			Util.notifyException("Output compressed PDF contents (images by " + compressionFactor + ")", e, logger);
		}

		return pdfByteOutputStream.toByteArray();
	}

	/**
	 * Get an PDF file (max 5Mb) by means of its path and transform it to an XML annotated file by means of
	 * the PDFX Web Service (http://pdfx.cs.man.ac.uk/).
	 * 
	 * @param inputFilePath
	 * @param timeout set the socket timeout in milliseconds
	 * @return
	 */
	public static Map<String, String> processPDF(String inputFilePath, String tags, int timeout) {
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

		// Check size < 5Mb:
		long fileSizeBytes = file.length();
		if(fileSizeBytes > 5242800l) {
			logger.debug("IMPOSSIBLE TO CONVERT FILE SINCE IT IS GREATER THAN 5Mb - file name " + file.getName() + " - file size: " + file.length());
			retMap.put("CONVERSION_ERROR_" + rnd.nextInt(), "IMPOSSIBLE TO CONVERT FILE SINCE IT IS GREATER THAN 5Mb - file name " + file.getName() + " - file size: " + file.length());
			return retMap;
		}

		Path path = Paths.get(file.getAbsolutePath());
		byte[] data = {};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			Util.notifyException("Obtaining PDF as byte array", e, logger);
		}

		String result = processPDF(data, tags, timeout);
		retMap.put(file.getName().replace(".pdf", "") + "_PDFX.xml", result.toString());

		logger.debug("End converting to XML file: " + inputFilePath);

		return retMap;
	}

	/**
	 * Get an PDF file (max 5Mb) as a byte array and transform it to an XML annotated file by means of
	 * the PDFX Web Service (http://pdfx.cs.man.ac.uk/).
	 * 
	 * @param inputBytes input byte array
	 * @param timeout set the socket timeout in milliseconds
	 * @return
	 */
	public static String processPDF(byte[] inputBytes, String tags, int timeout) {

		StringBuffer result = new StringBuffer("");

		// Read file
		if(inputBytes.length == 0) {
			result.append("Incorrect or empty PDF file");
			return result.toString();
		}

		HttpPost post = new HttpPost(serviceURL);

		
		Builder requestBuilder = RequestConfig.custom()
				.setConnectTimeout(720 * 1000) // Until a connection with the server is established.
				.setConnectionRequestTimeout(720 * 1000) // Waiting a connection from the manager, then ConnectionPoolTimeoutException
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
			builder.addTextBody("info", tags);
		}
		builder.addBinaryBody("file", inputBytes, ContentType.APPLICATION_OCTET_STREAM, "PDF_PAPER.pdf");
		HttpEntity multipart = builder.build();
		
		post.setEntity(multipart);
		
		// Invoke PDF proxy service
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
			if(post.getConfig() != null) {
				logger.info("ConnectionRequestTimeout: " + post.getConfig().getConnectionRequestTimeout());
				logger.info("ConnectTimeout: " + post.getConfig().getConnectTimeout());
				logger.info("CookieSpe: " + ((post.getConfig().getCookieSpec() != null) ? post.getConfig().getCookieSpec() : "NULL"));
				logger.info("SocketTimeout: " + post.getConfig().getSocketTimeout());
				if(post.getConfig().getProxy() != null) {
					logger.info("Proxy HostName: " + ((post.getConfig().getProxy().getHostName() != null) ? post.getConfig().getProxy().getHostName() : "NULL"));
					logger.info("Proxy Port: " + post.getConfig().getProxy().getPort());
					logger.info("Proxy SchemeName: " + ((post.getConfig().getProxy().getSchemeName() != null) ? post.getConfig().getProxy().getSchemeName() : "NULL"));
				}
				else {
					logger.info(" --- NO PROXY ---");
				}
			}
			
			logger.info("Sending PDF proxy parsing request for file with length " + inputBytes.length);
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(post, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.debug("PDF proxy parsing request processed in " + (endTime - startTime)  + " milliseconds with response: " + 
					((response != null) ? (response.length() > 15 ? response.substring(0, 15) : response) :"NULL") + " (sent file length: " + inputBytes.length + ")");
		} catch (ClientProtocolException e) {
			logger.error("PDF proxy processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("PDF proxy processing exception / Exception " + e.getMessage());
		}

		return result.append( (response != null) ? response : "ERROR" ).toString();
	}

	/**
	 * Convert by means of PDFX a PDF file or recursively all PDF files in a directory (http://pdfx.cs.man.ac.uk/).
	 * Compression of PDF files images can be activated, specifying also a compression factor.
	 * 
	 * @param fileOrDirFullPath
	 * @param tags
	 * @param enablePDFcompression
	 * @param compressionFactor
	 * @param recursiveDir
	 * @return The number of PDF files correctly converted and stored
	 */
	public static int convertFilesAndStore(String fileOrDirFullPath, String tags, boolean enablePDFcompression, float compressionFactor, boolean recursiveDir, int timeout) {

		compressionFactor = (0f < compressionFactor && compressionFactor < 10f) ? compressionFactor : 0.5f;

		int convertedPDFcount = 0;
		File parent = new File(fileOrDirFullPath);

		if(parent.exists() && parent.isDirectory()) {
			logger.debug("Converting PDF from directory: " + parent.getAbsolutePath() + " - recursively: " + recursiveDir);

			// Get all sub-directories and files
			File[] children = parent.listFiles();
			for(File file : children) {
				if(!recursiveDir && file.isDirectory()) {
					continue;
				}
				convertedPDFcount += convertFilesAndStore(file.getAbsolutePath(), tags, enablePDFcompression, compressionFactor, recursiveDir, timeout);
			}
		}
		else {
			if(parent.getName().toLowerCase().endsWith(".pdf")) {
				boolean compressed = false;

				// Check if the file-size is > 5Mb
				File fileToCompress = new File(parent.getAbsolutePath());
				Long fileSizeBytes = fileToCompress.length();
				boolean toCompress = false;
				if(fileSizeBytes > 5242800) {
					toCompress = true;
				}

				Path filePath = Paths.get(parent.getAbsolutePath());
				byte[] originalPDF = {};
				try {
					originalPDF = Files.readAllBytes(filePath);
				} catch (IOException e) {
					Util.notifyException("Reading PDF file", e, logger);
				}
				byte[] compressedPDF = null;
				if(enablePDFcompression && toCompress) {
					try {
						compressedPDF = pdfCompress(originalPDF, compressionFactor, false);
						if(compressedPDF.length > 5242800) {
							compressedPDF = pdfCompress(originalPDF, compressionFactor, true);
							if(compressedPDF.length > 0) {
								compressed = true;
							}
						}
						else if(compressedPDF.length > 0) {
							compressed = true;
						}
					}
					catch(Exception e) {
						Util.notifyException("Compressing PDF file", e, logger);
					}
				}

				try {
					String conversionResult = processPDF(((compressed) ? compressedPDF : originalPDF), tags, timeout);
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
				catch(Exception e) {
					Util.notifyException("Converting PDF file", e, logger);
				}
			}
		}

		return convertedPDFcount;
	}

}
