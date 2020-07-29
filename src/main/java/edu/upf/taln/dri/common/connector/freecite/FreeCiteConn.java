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
package edu.upf.taln.dri.common.connector.freecite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.upf.taln.dri.common.connector.freecite.model.FreeCiteResult;
import edu.upf.taln.dri.common.util.Util;

/**
 * Collection of static methods to parse bibliographic entries by FreeCite
 * REF: http://freecite.library.brown.edu/welcome/api_instructions
 * 
 *
 */
public class FreeCiteConn {

	private static Logger logger = Logger.getLogger(FreeCiteConn.class);

	private static final String serviceURL = "http://freecite.library.brown.edu/citations/create";

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
	 * Parse a list of bibliographic entries
	 * 
	 * @param citations List of bibliographic entries
	 * @param timeout Response timeout (if not in [1, 299], set equal to 15)
	 * @return
	 */
	public static List<FreeCiteResult> parseCitations(List<String> citations, int timeout) {

		// **********************************************************
		// ATTENTION: FreeCite reference parser has been discontinued
		// This client class should be adapted to rely on AnyCite (https://anystyle.io/)
		// in order to parse citations
		logger.warn("FreeCite reference parser is DISABLED. This client class should be adapted to rely on AnyCite (https://anystyle.io/) in order to parse citations.");
		return new ArrayList<FreeCiteResult>();
		// **********************************************************
		
		/*
		if(CollectionUtils.isEmpty(citations)) {
			return new ArrayList<FreeCiteResult>();
		}

		timeout = (timeout > 0 && timeout < 300) ? timeout : 15;

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		Consumer<String> postBodyBuilder = (citString) -> {
			if(StringUtils.isNotBlank(citString)) {
				nameValuePairs.add(new BasicNameValuePair("citation[]", citString));
			}
		};

		if(!CollectionUtils.isEmpty(citations)) {
			citations.stream().forEach(postBodyBuilder);
		}

		HttpPost post = new HttpPost(serviceURL);

		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(15 * 1000)
				.setSocketTimeout(30 * 1000).build();

		post.setConfig(config);

		post.setHeader("Accept", "text/xml");

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Util.notifyException("Encoding bibliographic entries", e, logger);
		}

		// Invoke FreeCite service
		Map<String, String> response = null;
		try {
			// When using a ResponseHandler, HttpClient will automatically take care of ensuring release of the connection 
			// back to the connection manager regardless whether the request execution succeeds or causes an exception.
			ResponseHandler<Map<String, String>> responseHandler = new ResponseHandler<Map<String, String>>() {

				@Override
				public Map<String, String> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					String respString = "";
					if (status >= 200 && status < 300) {
						BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
						StringBuffer result = new StringBuffer();
						String line = "";
						while ((line = rd.readLine()) != null) {
							result.append(line + "\n");
						}
						rd.close();
						respString = result.length() > 0 ? result.toString() : null;
					} else {
						respString = "ERROR CODE: " + status;
					}

					Map<String, String> retMap = new HashMap<String, String>();
					retMap.put("body", respString);
					retMap.put("status", status + "");
					return retMap;
				}

			};

			logger.debug("Sending FreeCite bibliographic entry parsing request...");
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(post, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.debug("FreeCite bibliographic entry processed in " + (endTime - startTime)  + " milliseconds with response Code : " + response.get("status"));
		} catch (ClientProtocolException e) {
			logger.error("FreeCite processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("FreeCite processing exception / Exception " + e.getMessage());
		}

		List<FreeCiteResult> citationList = new ArrayList<FreeCiteResult>();
		String parsingResults = response.get("body");
		if(StringUtils.isNotBlank(parsingResults)) {

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

				try {
					DocumentBuilder db = dbf.newDocumentBuilder();

					InputSource is = new InputSource(new StringReader(parsingResults));
					Document dom = db.parse(is);

					NodeList citationElem = dom.getElementsByTagName("citation");

					// Parse each citation element of FreeCite result
					if(citationElem != null && citationElem.getLength() > 0) {

						for(int citIndex = 0; citIndex < citationElem.getLength(); citIndex++) {
							Element citEl = (Element) citationElem.item(citIndex);
							String isValid = citEl.getAttribute("valid");
							if(isValid.toLowerCase().equals("true")) {

								FreeCiteResult citation = new FreeCiteResult();
								// Authors
								NodeList authorsElem = dom.getElementsByTagName("authors");
								if(authorsElem != null && authorsElem.getLength() > 0) {
									for(int i = 0; i < authorsElem.getLength(); i++) {
										Element authEl = (Element) authorsElem.item(i);
										if(authEl != null) {
											NodeList singleAuthElem = dom.getElementsByTagName("author");
											if(singleAuthElem != null && singleAuthElem.getLength() > 0) {
												for(int k = 0; k < singleAuthElem.getLength(); k++) {
													Element sAuthElem = (Element) singleAuthElem.item(k);
													if(sAuthElem != null) {
														String authorName = sAuthElem.getTextContent();
														if(StringUtils.isNotBlank(authorName)) {
															citation.getAuthorNames().add(authorName);
														}
													}
												}
											}
										}
									}
								}

								// Title
								NodeList titleElem = dom.getElementsByTagName("title");
								if(titleElem != null && titleElem.getLength() > 0) {
									Element titleElement = (Element) titleElem.item(0);
									if(titleElement != null) {
										citation.setTitle(StringUtils.defaultIfBlank(titleElement.getTextContent(), ""));
									}
								}

								// Journal
								NodeList journalElem = dom.getElementsByTagName("journal");
								if(journalElem != null && journalElem.getLength() > 0) {
									Element journalElement = (Element) journalElem.item(0);
									if(journalElement != null) {
										citation.setJournal(StringUtils.defaultIfBlank(journalElement.getTextContent(), ""));
									}
								}

								// Pages
								NodeList pagesElem = dom.getElementsByTagName("pages");
								if(pagesElem != null && pagesElem.getLength() > 0) {
									Element pagesElement = (Element) pagesElem.item(0);
									if(pagesElement != null) {
										citation.setPages(StringUtils.defaultIfBlank(pagesElement.getTextContent(), ""));
									}
								}

								// Year
								NodeList yearElem = dom.getElementsByTagName("year");
								if(yearElem != null && yearElem.getLength() > 0) {
									Element yearElement = (Element) yearElem.item(0);
									if(yearElement != null) {
										citation.setYear(StringUtils.defaultIfBlank(yearElement.getTextContent(), ""));
									}
								}

								// raw_string
								NodeList raw_stringElem = dom.getElementsByTagName("raw_string");
								if(raw_stringElem != null && raw_stringElem.getLength() > 0) {
									Element raw_stringElement = (Element) raw_stringElem.item(0);
									if(raw_stringElement != null) {
										citation.setRawString(StringUtils.defaultIfBlank(raw_stringElement.getTextContent(), ""));
									}
								}

								citationList.add(citation);
							}
						}
					}

				}catch(ParserConfigurationException pce) {
					Util.notifyException("Invoking service", pce, logger);
				}catch(SAXException se) {
					Util.notifyException("Response parsing", se, logger);
				}catch(IOException ioe) {
					Util.notifyException("API communication", ioe, logger);
				}
			}
			catch (Exception e) {
				Util.notifyException("Global error", e, logger);
			}

		}

		return citationList;
		*/
	}

}
