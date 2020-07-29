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
package edu.upf.taln.dri.common.connector.google.scholar;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upf.taln.dri.common.connector.google.scholar.model.GoogleScholarResult;
import edu.upf.taln.dri.common.util.Util;

/**
 * Utility methods to parse bibliographic entries by Google Scholar
 * REF: https://scholar.google.com/
 * 
 *
 */
public class GoogleScholarConn {

	private static Logger logger = Logger.getLogger(GoogleScholarConn.class);	

	private static final String serviceURL = "http://scholar.google.es/";

	private static Random rnd = new Random();

	// Wait time after a query
	public static Integer maxSleepTimeInSec = 1;

	// Proxy usage parameters
	public static boolean useProxy = false;
	public static String proxyScheme = "http";
	public static String proxyHost = "88.150.156.39";
	public static String proxyPath = "/prx.php";


	private static CloseableHttpClient httpClient = null;
	private static CookieStore cookieStore = null;
	private static HttpContext httpContext = null;

	static {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");

			// Set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} }, new SecureRandom());

		}
		catch(Exception e) {
			Util.notifyException("Instantiating SSL context (proxy: " + useProxy + ")", e, logger);
		}

		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslConnectionFactory)
				.register("http", new PlainConnectionSocketFactory())
				.build();
		PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(registry);
		ccm.setMaxTotal(5);
		ccm.setDefaultMaxPerRoute(5);
		
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		clientBuilder.setSSLSocketFactory(sslConnectionFactory);
		clientBuilder.setConnectionManager(ccm);
		
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
		clientBuilder.setKeepAliveStrategy(myStrategy);
		
		cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		
		httpClient = clientBuilder.build();
	}

	/**
	 * Given a text string, retrieves the first 10 Google Scholar results
	 * 
	 * @param paperInfo Text string to parse
	 * @param expandCitations Number of citations to expand by retrieving detailed information (if not in [0, 10], set equal to 0)
	 * @return
	 */
	public static List<GoogleScholarResult> parseAddress(String paperInfo, int expandCitations) {
		List<GoogleScholarResult> results = new ArrayList<GoogleScholarResult>();

		if(StringUtils.isBlank(paperInfo)) {
			return results;
		}

		if(expandCitations < 0 || expandCitations > 10) {
			expandCitations = 0;
		}

		maxSleepTimeInSec = (maxSleepTimeInSec == null || maxSleepTimeInSec < 0 || maxSleepTimeInSec > 30) ? 10 : maxSleepTimeInSec;

		logger.debug("Start searching Google Scholar for: '" + paperInfo + "', retrieving detailed info of the first " + expandCitations + " search results.");

		// Setting up search parameters
		try {
			Document doc;
			try {
				sanitizeTitle(paperInfo);

				URI queryURI = null;

				if(useProxy) {
					URIBuilder builder_APPO = new URIBuilder();
					builder_APPO.setScheme("https").setHost("scholar.google.es").setPath("/scholar")
					.setParameter("hl", "es")
					.setParameter("q", paperInfo);
					URI url_APPO = builder_APPO.build();

					URIBuilder builder = new URIBuilder();
					builder.setScheme(proxyScheme).setHost(proxyHost).setPath(proxyPath)
					.setParameter("url", url_APPO.toASCIIString());
					queryURI = builder.build();
					logger.debug("Fetching data from (proxy: " + useProxy + "): " + queryURI.toASCIIString());
				}
				else {
					URIBuilder builder = new URIBuilder();
					builder.setScheme("https").setHost("scholar.google.es").setPath("/scholar")
					.setParameter("hl", "es")
					.setParameter("q", paperInfo);
					queryURI = builder.build();
					logger.debug("Fetching data from (proxy: " + useProxy + "): " + queryURI.toASCIIString());
				}
				String str = performQuery(queryURI);

				logger.debug("Response: " + str);

				doc = Jsoup.parse(str);

				// Parse Google Scholar response
				// [@id="gs_ccl"]/div[1]
				Elements gsResults = doc.getElementsByClass("gs_r");

				if(gsResults != null && gsResults.size() > 0) {

					// Go through all Google Scholar results
					for(int i = 0; i < gsResults.size(); i++) {
						String title = "";
						String link = "";
						String secondLine = "";
						String year = "";
						Map<String, String> authorName_LinkMap = new HashMap<String, String>();
						String abstractSnippet = "";
						Map<String, String> citationType_ContentMap = new HashMap<String, String>();
						String refText = "";
						List<String> refAuthorsList = new ArrayList<String>();
						List<String> refTitleList = new ArrayList<String>();
						List<String> refYearList = new ArrayList<String>();
						List<String> refJournalList = new ArrayList<String>();

						Element gsResult = gsResults.get(i);
						if(gsResult != null) {
							// Parse Google Scholar result
							Elements gs_ri = gsResult.getElementsByClass("gs_ri");
							if(gs_ri != null && gs_ri.size() == 1 && gs_ri.get(0) != null) {
								Element gs_riElem = gs_ri.get(0);

								// A) DIV CLASS gs_rt: get title and link to the paper
								Elements gs_rt = gs_riElem.getElementsByClass("gs_rt");
								if(gs_rt != null && gs_rt.size() == 1 && gs_rt.get(0) != null) {
									Element gs_rtElem = gs_rt.get(0);
									Elements linkElems = gs_rtElem.getElementsByTag("a");
									if(linkElems != null && linkElems.size() == 1 && linkElems.get(0) != null) {
										Element linkElem = linkElems.get(0);
										String titleValue = linkElem.text();
										String linkValue = linkElem.attr("href");

										if(titleValue != null) {
											title = titleValue;
										}
										if(linkValue != null) {
											link = linkValue;
										}
									}
									
									if(title == null || title.equals("")) {
										String gs_rtElemText = gs_rtElem.text();
										if(gs_rtElemText != null) {
											title = gs_rtElemText;
										}
									}
								}

								// B) DIV CLASS gs_a: get authors, year, conf
								Elements gs_a = gs_riElem.getElementsByClass("gs_a");
								if(gs_a != null && gs_a.size() == 1 && gs_a.get(0) != null) {
									Element gs_aElem = gs_a.get(0);
									String elemValue = gs_aElem.text();

									if(elemValue != null) {
										secondLine = elemValue;

										// Extract year
										String[] secondLineSplit = secondLine.split(" ");
										for(int k = 0; k < secondLineSplit.length; k++) {
											if(secondLineSplit[k] != null && !secondLineSplit[k].trim().equals("")) {
												String splitElem = secondLineSplit[k].trim();
												try {
													Integer intValue = Integer.valueOf(splitElem);
													if(intValue > 1800 && intValue < 2020) {
														year = intValue.toString();
													}
												}
												catch (Exception e) {
													// Do nothing
												}
											}
										}
									}

									// Extract author page links if present
									// baseURLGoogleScholar - authorNameLinkMap
									Elements authorLinks = gs_aElem.getElementsByTag("a");
									if(authorLinks != null && authorLinks.size() > 0) {
										for(int w = 0; w < authorLinks.size(); w++) {
											Element authorLink = authorLinks.get(w);
											if(authorLink != null) {
												String authorNameValue = authorLink.text().trim();
												String authorLinkValue = authorLink.attr("href").trim();

												if(authorNameValue != null && !authorNameValue.equals("")) {
													authorLinkValue = (authorLinkValue != null) ? serviceURL + authorLinkValue: "NULL";
													authorName_LinkMap.put(authorNameValue, authorLinkValue);
												}
											}
										}
									}
									else {
										// No author links
										Integer indexOfSeparator1 = secondLine.indexOf("… -");
										Integer indexOfSeparator2 = secondLine.indexOf(" -");
										if(indexOfSeparator1 != -1 || indexOfSeparator2 != -1) {
											String authorList = "";
											if(indexOfSeparator1 != -1 && indexOfSeparator2 != -1) {
												if(indexOfSeparator1 < indexOfSeparator2) {
													authorList = secondLine.substring(0, indexOfSeparator1);
												}
												else {
													authorList = secondLine.substring(0, indexOfSeparator2);
												}
											}
											else if(indexOfSeparator1 == -1) {
												authorList = secondLine.substring(0, indexOfSeparator2);
											}
											else if(indexOfSeparator2 == -1) {
												authorList = secondLine.substring(0, indexOfSeparator1);
											}

											authorList = authorList.trim();
											if(!authorList.equals("")) {
												String[] authorListSplit = authorList.split(",");
												if(authorListSplit != null && authorListSplit.length > 0) {
													for(int y = 0; y < authorListSplit.length; y++) {
														if(authorListSplit[y] != null && authorListSplit[y].trim().length() > 0) {
															authorName_LinkMap.put(authorListSplit[y].trim(), "NULL");
														}
													}
												}
											}
										}
									}

								}

								// C) DIV CLASS gs_rs: Get abstract snippet
								Elements gs_rs = gs_riElem.getElementsByClass("gs_rs");
								if(gs_rs != null && gs_rs.size() == 1 && gs_rs.get(0) != null) {
									Element gs_rsElem = gs_rs.get(0);
									String elemValue = gs_rsElem.text();

									if(elemValue != null) {
										abstractSnippet = elemValue.replace("\n", "");
									}
								}

								// D) DIV CLASS gs_fl: Get citations
								Elements gs_fl = gs_riElem.getElementsByClass("gs_fl");
								if(gs_fl != null && gs_fl.size() == 1 && gs_fl.get(0) != null) {
									Element gs_flElem = gs_fl.get(0);
									Elements linkElems = gs_flElem.getElementsByTag("a");
									for(int e = 0; e < linkElems.size(); e++) {
										Element linkElem = linkElems.get(e);
										String onclickValue = linkElem.attr("onclick");

										if(onclickValue != null && onclickValue.contains("gs_ocit")) {
											onclickValue = onclickValue.trim();
											// On-click value: return gs_ocit(event,'FbQnq5IDATMJ','4')

											// Get event code
											String eventCode = onclickValue.replace("return gs_ocit(event,'", "");
											Integer firstOcc = eventCode.indexOf("'");
											if(firstOcc != null && firstOcc != -1) {
												eventCode = eventCode.substring(0, firstOcc);
											}

											// Get scirp
											Integer lastOcc = onclickValue.lastIndexOf("'");
											String scirp = null;
											if(lastOcc != null && lastOcc != -1) {
												scirp = onclickValue.substring(lastOcc - 1, lastOcc);
											}
											scirp = scirp.trim();

											if(eventCode != null && !eventCode.equals("") && scirp != null && !scirp.equals("")) {

												logger.debug("Analyzing citation with eventCode: " + eventCode + " and scirp: " + scirp);

												if(i < expandCitations) {
													logger.debug("Query citation with eventCode: " + eventCode + " and scirp: " + scirp);

													// Retrieve from Google citation text
													// https://scholar.google.es/scholar?q=info:Jmw5fLxGtsQJ:scholar.google.com/&output=cite&scirp=0&hl=es
													String strCite = null;
													URI urlCite = null;
													Document docCite;

													if(useProxy) {
														URIBuilder builder_APPO = new URIBuilder();
														builder_APPO.setScheme("https").setHost("scholar.google.es").setPath("/scholar")
														.setParameter("output", "cite")
														.setParameter("q", "info:" + eventCode + ":scholar.google.com/");
														URI url_APPO = builder_APPO.build();

														URIBuilder builder = new URIBuilder();
														builder.setScheme("http").setHost("88.150.156.39").setPath("/prx.php")
														.setParameter("url", url_APPO.toASCIIString());
														urlCite = builder.build();
														logger.debug("Fetching data from (proxy: " + useProxy + "): " + urlCite.toASCIIString());
													}
													else {
														URIBuilder builder = new URIBuilder();
														builder.setScheme("https").setHost("scholar.google.es").setPath("/scholar")
														.setParameter("output", "cite")
														.setParameter("q", "info:" + eventCode + ":scholar.google.com/");
														urlCite = builder.build();
														logger.debug("Fetching data from (proxy: " + useProxy + "): " + urlCite.toASCIIString());
													}

													strCite = performQuery(urlCite);

													logger.debug("Response: " + strCite.toString());

													docCite = Jsoup.parse(strCite.toString());

													logger.debug("Fetched data: " + docCite.toString());

													// Parsing docCite
													Elements tbodyElements = docCite.getElementsByTag("tbody");
													if(tbodyElements != null && tbodyElements.size() == 1 && tbodyElements.get(0) != null) {
														Element tbodyElement = tbodyElements.get(0);

														Elements trElements = tbodyElement.getElementsByTag("tr");
														if(trElements != null && trElements.size() >= 1 && trElements.get(0) != null) {
															for(int t = 0; t < trElements.size(); t++) {
																Element trElement = trElements.get(t);

																// Get citation type
																String citationType = "";
																Elements thElements = trElement.getElementsByTag("th");
																if(thElements != null && thElements.size() == 1 && thElements.get(0) != null) {
																	citationType = thElements.get(0).text();
																}

																// Get citation contents
																String citationContents = "";
																Elements tdElements = trElement.getElementsByTag("td");
																if(tdElements != null && tdElements.size() == 1 && tdElements.get(0) != null) {
																	citationContents = tdElements.get(0).text();
																}

																if(citationType != null && !citationType.trim().equals("") && citationContents != null && !citationContents.trim().equals("")) {
																	citationContents = citationContents.replace("\n", "");
																	citationType_ContentMap.put(citationType, citationContents);
																}
															}
														}
													}

													// Retrieve formatted citation
													Elements linkToParsedCitElements = docCite.getElementsByTag("a");
													if(linkToParsedCitElements != null && linkToParsedCitElements.size() > 0) {
														for(int w = 0; w < linkToParsedCitElements.size(); w++) {
															Element parsedCitElem = linkToParsedCitElements.get(w);
															if(parsedCitElem != null) {
																String parsedCitContent = parsedCitElem.text().trim();
																String parsedCitLink = parsedCitElem.attr("href").trim();
																String parsedCitClass = parsedCitElem.attr("class").trim();

																if(parsedCitContent != null && !parsedCitLink.equals("") &&
																		parsedCitClass != null && !parsedCitClass.equals("") &&
																		parsedCitLink != null && !parsedCitLink.equals("")) {
																	if(parsedCitClass.equals("gs_citi") && parsedCitContent.toLowerCase().equals("refman")) {
																		// Retrieve RefMan citation style
																		String strRef = null;
																		URI urlRef = null;
																		try {
																			if(useProxy) {
																				URIBuilder builder_APPO = new URIBuilder(parsedCitLink);
																				URI url_APPO = builder_APPO.build();

																				URIBuilder builder = new URIBuilder();
																				builder.setScheme("http").setHost("88.150.156.39").setPath("/prx.php")
																				.setParameter("url", url_APPO.toASCIIString());
																				urlRef = builder.build();
																				logger.debug("Fetching data from (proxy: " + useProxy + "): " + urlRef.toASCIIString());
																			}
																			else {
																				URIBuilder builder = new URIBuilder(parsedCitLink);
																				urlRef = builder.build();
																				logger.debug("Fetching data from (proxy: " + useProxy + "): " + urlRef.toASCIIString());
																			}

																			strRef = performQuery(urlRef);

																			if(strRef != null && !strRef.equals("")) {
																				// Parsing RefMan citation style
																				refText = strRef;

																				String[] strRefSplit = strRef.split("\n");
																				if(strRefSplit != null && strRefSplit.length > 0) {
																					for(int n = 0; n < strRefSplit.length; n++) {
																						if(strRefSplit[n] != null && !strRefSplit[n].trim().equals("")) {
																							strRefSplit[n] = strRefSplit[n].trim();

																							if(strRefSplit[n].startsWith("A1  - ")) {
																								strRefSplit[n] = strRefSplit[n].replace("A1  - ", "");
																								strRefSplit[n] = strRefSplit[n].trim();
																								if(!strRefSplit[n].equals("")) {
																									refAuthorsList.add(strRefSplit[n]);
																								}
																							}

																							if(strRefSplit[n].startsWith("T1  - ")) {
																								strRefSplit[n] = strRefSplit[n].replace("T1  - ", "");
																								strRefSplit[n] = strRefSplit[n].trim();
																								if(!strRefSplit[n].equals("")) {
																									refTitleList.add(strRefSplit[n]);
																								}
																							}

																							if(strRefSplit[n].startsWith("Y1  - ")) {
																								strRefSplit[n] = strRefSplit[n].replace("Y1  - ", "");
																								strRefSplit[n] = strRefSplit[n].trim();
																								if(!strRefSplit[n].equals("")) {
																									refYearList.add(strRefSplit[n]);
																								}
																							}

																							if(strRefSplit[n].startsWith("JO  - ")) {
																								strRefSplit[n] = strRefSplit[n].replace("JO  - ", "");
																								strRefSplit[n] = strRefSplit[n].trim();
																								if(!strRefSplit[n].equals("")) {
																									refJournalList.add(strRefSplit[n]);
																								}
																							}

																						}
																					}
																				}
																			}

																		} catch (Exception e1) {
																			Util.notifyException("Expanding citation entry / retrieving RefMan citation style", e1, logger);
																		}
																	}
																}
															}
														}
													}
												}
												else {
													logger.debug("Skipped citation with eventCode: " + eventCode + " and scirp: " + scirp);
												}
											}
										}
									}
								}
							}
						}

						// Google Scholar result entry processing
						if(StringUtils.isNotBlank(title)) {
							GoogleScholarResult gsr = new GoogleScholarResult();
							gsr.setTitle(title);
							gsr.setLink(StringUtils.defaultIfBlank(link, "NONE"));
							gsr.setSecondLine(StringUtils.defaultIfBlank(secondLine, "NONE"));
							gsr.setYear(StringUtils.defaultIfBlank(year, "NONE"));
							gsr.setAuthorName_LinkMap(authorName_LinkMap);
							gsr.setAbstractSnippet(StringUtils.defaultIfBlank(abstractSnippet, "NONE"));
							gsr.setCitationType_ContentMap(citationType_ContentMap);
							gsr.setRefText(StringUtils.defaultIfBlank(refText, "NONE"));
							gsr.setRefAuthorsList(refAuthorsList);
							gsr.setRefTitleList(refTitleList);
							gsr.setRefYearList(refYearList);
							gsr.setRefJournalList(refJournalList);

							logger.debug("Added entry: " + gsr);

							results.add(gsr);
						}
					}
				}
			} catch (Exception e) {
				Util.notifyException("Result parsing (proxy: " + useProxy + ")", e, logger);
			}

			return results;
		} catch (Exception e) {
			Util.notifyException("Global exception (proxy: " + useProxy + ")", e, logger);
			return null;
		}
	}

	private static String performQuery(URI queryURI) {

		String queryResult = null;
		
		HttpGet httpGetCite = null;
		CloseableHttpResponse responseGet = null;
		try {
			httpGetCite = new HttpGet(queryURI);

			httpGetCite.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36");
			httpGetCite.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpGetCite.setHeader("accept-language", "es-ES,es;q=0.8,en;q=0.6,it;q=0.4,ca;q=0.2");
			httpGetCite.setHeader("cache-control", "max-age=0");
			
			try {
				responseGet = httpClient.execute(httpGetCite, httpContext);

				org.apache.http.StatusLine status = responseGet.getStatusLine();
				if (status.getStatusCode() != 200) {
					logger.warn("Failed Google Scholar Query - HTTP Status code: " + httpGetCite.getURI());
				}
				
				HttpEntity entity = responseGet.getEntity();
				queryResult = EntityUtils.toString(entity, "UTF-8");
				
			} catch (IOException e) {
				Util.notifyException("Invoking service (proxy: " + useProxy + ")", e, logger);
			} finally {
				try {
					responseGet.close();
				}
				catch (Exception e) {
					Util.notifyException("Invoking service / closing resp (proxy: " + useProxy + ")", e, logger);
				}
			}
			
		}
		finally {
			try {
				httpGetCite.releaseConnection();
			} catch (Exception e) {
				Util.notifyException("Invoking service / releasing connection (proxy: " + useProxy + ")", e, logger);
			}
		}
		
		// Pause between two queries
		try {
			Thread.sleep(rnd.nextInt(1000) * maxSleepTimeInSec);
		} catch (InterruptedException e) {
			Util.notifyException("Sleep between two consecutive calls (proxy: " + useProxy + ")", e, logger);
		}

		return queryResult;
	}

	private static String sanitizeTitle(String searchTitle) {

		if(searchTitle != null) {
			searchTitle = searchTitle.replace(":", " ");
			searchTitle = searchTitle.replace("-", " ");
			searchTitle = searchTitle.replace("_", " ");
			searchTitle = searchTitle.replace(",", " ");
			searchTitle = searchTitle.replace("\"", " ");
			searchTitle = searchTitle.replace("'", " ");
			searchTitle = searchTitle.replace("´", " ");
			searchTitle = searchTitle.replace("´", " ");
			searchTitle = searchTitle.replace("?", " ");
			searchTitle = searchTitle.replace("!", " ");
			searchTitle = searchTitle.replace(".", " ");
			searchTitle = searchTitle.replace("ç", " ");
			searchTitle = searchTitle.replace("Ç", " ");
		}

		return searchTitle;
	}
	
}
