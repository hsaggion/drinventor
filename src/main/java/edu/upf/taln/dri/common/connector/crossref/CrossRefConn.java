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
package edu.upf.taln.dri.common.connector.crossref;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import edu.upf.taln.dri.common.connector.crossref.model.CrossRefResult;
import edu.upf.taln.dri.common.util.Util;

/**
 * Collection of static methods to parse bibliographic entries by CrossRef
 * REF: http://search.crossref.org/help/api
 * 
 *
 */
public class CrossRefConn {

	private static Logger logger = Logger.getLogger(CrossRefConn.class);

	private static final String serviceURL = "http://search.crossref.org/links";
	private static final String serviceDOIURL = "http://search.crossref.org/dois";

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
	public static CrossRefResult parseCitations(String biblioEntry, int timeout) {
		
		// **********************************************************
		// ATTENTION: CrossRef reference parser is DISABLED
		// The parser is going to be adapted to changes in CrossRef API
		// https://github.com/CrossRef/rest-api-doc#field-queries
		logger.warn("CrossRef reference parser is DISABLED. The parser is going to be adapted to changes in CrossRef API https://github.com/CrossRef/rest-api-doc#field-queries");
		return new CrossRefResult();
		// **********************************************************
		
		/*
		if(StringUtils.isBlank(biblioEntry)) {
			return null;
		}

		timeout = (timeout > 0 && timeout < 300) ? timeout : 15;

		HttpPost post = new HttpPost(serviceURL);

		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(15 * 1000)
				.setSocketTimeout(30 * 1000).build();

		post.setConfig(config);

		post.setHeader("Content-Type", "application/jsonl");

		try {
			String xml = "[ \"" + biblioEntry.replace("\"", "\\\"") + "\" ]";
			HttpEntity entity = new ByteArrayEntity(xml.getBytes("UTF-8"));
			post.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			Util.notifyException("Encoding bibliographic entries", e, logger);
		}

		// Invoke CrossRef service
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

			logger.debug("Sending CrossRef bibliographic entry parsing request...");
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(post, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.debug("CrossRef bibliographic entry processed in " + (endTime - startTime)  + " milliseconds with response Code : " + response.get("status"));
		} catch (ClientProtocolException e) {
			logger.error("CrossRef processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("CrossRef processing exception / Exception " + e.getMessage());
		}

		String parsingResults = response.get("body");
		if(StringUtils.isNotBlank(parsingResults)) {

			try {
				JSONObject json = new JSONObject(parsingResults.toString());

				boolean queryOkCheck = false;
				queryOkCheck = json.getBoolean("query_ok");

				if(queryOkCheck) {
					JSONArray resultArray = json.getJSONArray("results");
					if(resultArray != null && resultArray.length() > 0) {
						// Get first result - to improve by ranking results thus choosing the best one
						JSONObject firstResult = (JSONObject) resultArray.get(0);
						if(firstResult != null) {

							CrossRefResult xref = new CrossRefResult();

							if(firstResult.has("doi") && StringUtils.isNotBlank(firstResult.getString("doi"))) {
								xref.setDoi(StringUtils.defaultIfBlank(firstResult.getString("doi"), ""));
								if(firstResult.has("text") && StringUtils.isNotBlank(firstResult.getString("text")) ) {
									xref.setOriginalText(StringUtils.defaultIfBlank(firstResult.getString("text"), ""));
								}
								
								// Query by DOI to get more specific info
								expandDOI(xref, firstResult.getString("doi"));

								if(StringUtils.isNotBlank(xref.getTitle())) {
									return xref;
								}
							}
							
						}
					}
				}
			}
			catch (Exception e) {
				Util.notifyException("Global error", e, logger);
			}

		}

		return null;
		*/
	}


	private static void expandDOI(CrossRefResult xref, String doi) {

		if(xref == null || StringUtils.isBlank(doi)) {
			return;
		}

		HttpGet request;
		try {
			request = new HttpGet(serviceDOIURL + "?q=" + URLEncoder.encode(doi, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Util.notifyException("Encoding URL", e, logger);
			return;
		}

		// Invoke CrossRef service
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

			logger.debug("Sending CrossRef bibliographic entry parsing request (DOI)...");
			long startTime = System.currentTimeMillis();
			response = httpClient.execute(request, responseHandler);
			long endTime = System.currentTimeMillis();
			logger.debug("CrossRef bibliographic entry (DOI) processed in " + (endTime - startTime)  + " milliseconds with response Code : " + response.get("status"));
		} catch (ClientProtocolException e) {
			logger.error("CrossRef processing exception / client protocol " + e.getMessage());
		} catch (Exception e) {
			logger.error("CrossRef processing exception / Exception " + e.getMessage());
		}


		try {

			JSONArray resultGETArray = new JSONArray(response.get("body"));

			if(resultGETArray != null && resultGETArray.length() > 0) {
				JSONObject firstGETResult = (JSONObject) resultGETArray.get(0);
				if(firstGETResult != null) {
					try {
						xref.setScore(StringUtils.defaultIfBlank(firstGETResult.getString("score"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}

					try {
						xref.setNormalizedScore(StringUtils.defaultIfBlank(firstGETResult.getString("normalizedScore"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}

					try {
						xref.setTitle(StringUtils.defaultIfBlank(firstGETResult.getString("title"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}

					try {
						xref.setFullCitation(StringUtils.defaultIfBlank(firstGETResult.getString("fullCitation"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}

					try {
						xref.setYear(StringUtils.defaultIfBlank(firstGETResult.getString("year"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}

					try {
						xref.setCoins(StringUtils.defaultIfBlank(firstGETResult.getString("coins"), ""));
					}
					catch(JSONException jse) {
						/* Do nothing */
					}
				}
			}

		} catch (JSONException e) {
			Util.notifyException("Parsing JSON", e, logger);
		}
		
	}

}