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
package edu.upf.taln.dri.common.connector.bibsonomy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.bibsonomy.model.PersonName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.upf.taln.dri.common.connector.bibsonomy.model.BibTexWrap;
import edu.upf.taln.dri.common.util.Util;


/**
 * Collection of static methods to retrieve bibliographic entries by Bibsonomy
 * REF: http://www.bibsonomy.org/help/doc/api.html
 * 
 *
 */
public class BibsonomyStandaloneConn {

	private static Logger logger = Logger.getLogger(BibsonomyStandaloneConn.class);

	private static final String serviceURL = "http://www.bibsonomy.org/api/posts";

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
	 * Parse a bibliographic entry by Bibsonomy, given a Bibsonomy API user ID and apiKey
	 * 
	 * @param title
	 * @param userId
	 * @param apiKey
	 * @param timeout Response timeout (if not in [1, 299], set equal to 15)
	 * @return
	 */
	public static List<BibTexWrap> getBibTexWrap(String title, String userId, String apiKey, int timeout) {

		if(title == null || title.equals("")) {
			return new ArrayList<BibTexWrap>();
		}
		
		title = sanitizeTitle(title).trim();
		
		// Exact title search
		logger.debug("Retrieving from Bibsonomy (exact match): " + "\"" + title + "\"");
		List<BibTexWrap> results = getBibTexWrapInt("\"" + title + "\"", userId, apiKey, timeout);
		
		if(results == null || results.size() == 0) {
			logger.debug("Retrieving from Bibsonomy (relaxed match): " + title);
			results = getBibTexWrapInt(title, userId, apiKey, timeout);
		}

		return results;
	}
	
	
	private static List<BibTexWrap> getBibTexWrapInt(String title, String userId, String apiKey, int timeout) {

		if(title == null || title.equals("")) {
			return new ArrayList<BibTexWrap>();
		}
		
		timeout = (timeout > 0 && timeout < 300) ? timeout : 15;
		
		StringBuilder requestUrl = new StringBuilder(serviceURL);

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("resourcetype", "bibtex"));
		paramList.add(new BasicNameValuePair("search", title));
		String querystring = URLEncodedUtils.format(paramList, "utf-8");
		requestUrl.append("?");
		requestUrl.append(querystring);

		HttpGet get = new HttpGet(requestUrl.toString());
		String authString = userId + ":" + apiKey;
		byte[] bytesEncoded = Base64.encodeBase64(authString.getBytes());
		get.setHeader("Authorization", "Basic " + new String(bytesEncoded));
		get.setHeader("Accept", "text/xml");

		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(15 * 1000)
				.setSocketTimeout(30 * 1000).build();

		get.setConfig(config);

		// Invoke Bibsonomy Post suggestion service
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

			logger.debug("Sending Bibsonomy bibliographic entry parsing request...");
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(get, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.debug("Bibsonomy bibliographic entry processed in " + (endTime - startTime)  + " milliseconds with response Code : " + response.get("status"));
		} catch (ClientProtocolException e) {
			logger.error("Bibsonomy processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("Bibsonomy processing exception / Exception " + e.getMessage());
		}

		List<BibTexWrap> BibTexWrapList = new ArrayList<BibTexWrap>();
		String parsingResults = response.get("body");
		if(StringUtils.isNotBlank(parsingResults)) {

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

				try {
					DocumentBuilder db = dbf.newDocumentBuilder();

					Document dom = db.parse(new InputSource(new ByteArrayInputStream(parsingResults.getBytes("utf-8"))));

					NodeList BibTexWrapElementList = dom.getElementsByTagName("post");

					// Parse each citation element of FreeCite result
					if(BibTexWrapElementList != null && BibTexWrapElementList.getLength() > 0) {

						for(int BibTexWrapResultIndex = 0; BibTexWrapResultIndex < BibTexWrapElementList.getLength(); BibTexWrapResultIndex++) {
							Element BibTexWrapElem = (Element) BibTexWrapElementList.item(BibTexWrapResultIndex);
							
							BibTexWrap newBibTexWrap = new BibTexWrap();

							NodeList bibEntryXML = BibTexWrapElem.getElementsByTagName("bibtex");
							
							if(bibEntryXML.getLength() == 1) {
								Node BibTexWrapEntryNode = bibEntryXML.item(0);

								Element BibTexWrapEntryElem = (Element) BibTexWrapEntryNode;
								// BibTexWrapAbstract
								newBibTexWrap.setAbstract(BibTexWrapEntryElem.getAttribute("bibtexAbstract"));

								// address
								newBibTexWrap.setAddress(BibTexWrapEntryElem.getAttribute("address"));
								
								
								// author
								newBibTexWrap.setAuthorList(BibTexWrapEntryElem.getAttribute("author"));
								// ORIG_AUTHOR_COLUMN_SEPARATOR = " and "
								newBibTexWrap.setAuthor(new ArrayList<PersonName>());
								if(StringUtils.isNotEmpty(BibTexWrapEntryElem.getAttribute("author"))) {
									String[] splitAuthors = BibTexWrapEntryElem.getAttribute("author").split(" and ");
									for(int k = 0; k < splitAuthors.length; k++) {
										PersonName pn = new PersonName();
										
										String authorName = splitAuthors[k].trim();
										if(authorName.charAt(0) == ',' && authorName.length() > 1) authorName = authorName.substring(1);
										if(authorName.charAt(authorName.length() - 1) == ',' && authorName.length() > 1) authorName = authorName.substring(0, authorName.length() - 1);  
										
										Integer firstCommaIndex = authorName.indexOf(",");
										if(firstCommaIndex != -1) {
											pn.setLastName(authorName.substring(0, firstCommaIndex));
											if(firstCommaIndex < authorName.length()) {
												pn.setFirstName(authorName.substring(firstCommaIndex));
											}
										}
										else {
											pn.setLastName(authorName);
										}
										
										newBibTexWrap.getAuthor().add(pn);
									}
								}

								// BibTexWrapKey
								newBibTexWrap.setBibtexKey(BibTexWrapEntryElem.getAttribute("bibtexKey"));

								// booktitle
								newBibTexWrap.setBooktitle(BibTexWrapEntryElem.getAttribute("booktitle"));

								// chapter
								newBibTexWrap.setChapter(BibTexWrapEntryElem.getAttribute("chapter"));

								// day
								newBibTexWrap.setDay(BibTexWrapEntryElem.getAttribute("day"));

								// edition
								newBibTexWrap.setEdition(BibTexWrapEntryElem.getAttribute("edition"));

								// editor
								newBibTexWrap.setEditorList(BibTexWrapEntryElem.getAttribute("editor"));
								// ORIG_EDITOR_COLUMN_SEPARATOR = " and "
								newBibTexWrap.setEditor(new ArrayList<PersonName>());
								if(StringUtils.isNotEmpty(BibTexWrapEntryElem.getAttribute("editor"))) {
									String[] splitEditors = BibTexWrapEntryElem.getAttribute("editor").split(" and ");
									for(int k = 0; k < splitEditors.length; k++) {
										PersonName pn = new PersonName();
										
										String editorName = splitEditors[k].trim();
										if(editorName.charAt(0) == ',' && editorName.length() > 1) editorName = editorName.substring(1);
										if(editorName.charAt(editorName.length() - 1) == ',' && editorName.length() > 1) editorName = editorName.substring(0, editorName.length() - 1);  
										
										Integer firstCommaIndex = editorName.indexOf(",");
										if(firstCommaIndex != -1) {
											pn.setLastName(editorName.substring(0, firstCommaIndex));
											if(firstCommaIndex < editorName.length()) {
												pn.setFirstName(editorName.substring(firstCommaIndex));
											}
										}
										else {
											pn.setLastName(editorName);
										}
										
										newBibTexWrap.getEditor().add(pn);
									}
								}

								// entrytype
								newBibTexWrap.setEntrytype(BibTexWrapEntryElem.getAttribute("entrytype"));

								// institution
								newBibTexWrap.setInstitution(BibTexWrapEntryElem.getAttribute("institution"));

								// interhash
								newBibTexWrap.setInterHash(BibTexWrapEntryElem.getAttribute("interhash"));

								// intrahash
								newBibTexWrap.setIntraHash(BibTexWrapEntryElem.getAttribute("intrahash"));

								// journal
								newBibTexWrap.setJournal(BibTexWrapEntryElem.getAttribute("journal"));

								// misc
								newBibTexWrap.setMisc(BibTexWrapEntryElem.getAttribute("misc"));

								// month
								newBibTexWrap.setMonth(BibTexWrapEntryElem.getAttribute("month"));

								// note
								newBibTexWrap.setNote(BibTexWrapEntryElem.getAttribute("note"));

								// number
								newBibTexWrap.setNumber(BibTexWrapEntryElem.getAttribute("number"));

								// openURL
								newBibTexWrap.setOpenURL(BibTexWrapEntryElem.getAttribute("openURL"));

								// organization
								newBibTexWrap.setOrganization(BibTexWrapEntryElem.getAttribute("organization"));

								// pages
								newBibTexWrap.setPages(BibTexWrapEntryElem.getAttribute("pages"));

								// privnote
								newBibTexWrap.setPrivnote(BibTexWrapEntryElem.getAttribute("privnote"));

								// publisher
								newBibTexWrap.setPublisher(BibTexWrapEntryElem.getAttribute("publisher"));

								// school
								newBibTexWrap.setSchool(BibTexWrapEntryElem.getAttribute("school"));

								// series
								newBibTexWrap.setSeries(BibTexWrapEntryElem.getAttribute("series"));

								// title
								newBibTexWrap.setTitle(BibTexWrapEntryElem.getAttribute("title"));

								// type
								newBibTexWrap.setType(BibTexWrapEntryElem.getAttribute("type"));

								// url
								newBibTexWrap.setUrl(BibTexWrapEntryElem.getAttribute("url"));

								// volume
								newBibTexWrap.setVolume(BibTexWrapEntryElem.getAttribute("volume"));

								// year
								newBibTexWrap.setYear(BibTexWrapEntryElem.getAttribute("year"));

								/* NOT ADDED TO newBibTexWrap
									newBibTexWrap.setAnnote(annote);								
									newBibTexWrap.setCount(count);								
									newBibTexWrap.setCrossref(crossref);								
									newBibTexWrap.setDiscussionItems(discussionItems);								
									newBibTexWrap.setDocuments(documents);								
									newBibTexWrap.setExtraUrls(extraUrls);								
									newBibTexWrap.setHowpublished(howpublished);
									newBibTexWrap.setKey(key);								
									newBibTexWrap.setMiscFields(miscFields);								
									newBibTexWrap.setMonth(month);								
									newBibTexWrap.setNumberOfRatings(numberOfRatings);
									newBibTexWrap.setPosts(posts);
									newBibTexWrap.setRating(rating);
									newBibTexWrap.setScraperId(scraperId);
									newBibTexWrap.setScraperMetadata(scraperMetadata);
									newBibTexWrap.setVolume(volume);
									newBibTexWrap.setYear(year);
								 */

								BibTexWrapList.add(newBibTexWrap);
							}
							else {
								logger.debug("Multiple bibtex entries per post.");
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

		return BibTexWrapList;
	}
	
	private static String sanitizeTitle(String searchTitle) {

		if(searchTitle != null) {
			searchTitle = searchTitle.replace("\n", " ");
			searchTitle = searchTitle.replace(":", " ");
			// searchTitle = searchTitle.replace("-", " ");
			// searchTitle = searchTitle.replace("−", " ");
			// searchTitle = searchTitle.replace("_", " ");
			// searchTitle = searchTitle.replace(",", " ");
			searchTitle = searchTitle.replace("\"", " ");
			// searchTitle = searchTitle.replace("'", " ");
			// searchTitle = searchTitle.replace("´", " ");
			// searchTitle = searchTitle.replace("´", " ");
			// searchTitle = searchTitle.replace("?", " ");
			// searchTitle = searchTitle.replace("!", " ");
			// searchTitle = searchTitle.replace(".", " ");
			// searchTitle = searchTitle.replace("ç", " ");
			// searchTitle = searchTitle.replace("Ç", " ");
		}

		return searchTitle;
	}

}
