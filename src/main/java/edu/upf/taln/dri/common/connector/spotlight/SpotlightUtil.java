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
package edu.upf.taln.dri.common.connector.spotlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Collection of utility methods to access the DBpedia Spotlight Disambiguation & Entity Linking service.
 * 
 *
 */
public class SpotlightUtil {

	public static String ORGANIZATIONtype = "DBpedia%3AOrganisation%2CFreebase%3A%2Forganization%2CSchema%3AOrganization";
	
	public static String LOCATIONtype = "DBpedia%3APlace%2CFreebase%3A%2Flocation%2CSchema%3APlace";
	
	public static String CITYtype = "DBpedia%3ASettlement%2CFreebase%3A%2Flocation%2Fcitytown%2CSchema%3ACity";
	
	public static String STATEtype = "DBpedia%3ACountry%2CFreebase%3A%2Flocation%2Fcountry%2CSchema%3ACountry";
	
	private static Logger logger = Logger.getLogger(SpotlightUtil.class);

	public static Integer waitTimeAfterRequestInSeconds = 1;

	private static ObjectMapper mapper = new ObjectMapper();

	private static HttpClient client = HttpClientBuilder.create().build();

	private static String spotURL = "http://spotlight.sztaki.hu:2222/rest/annotate?";
	private static String charset = "UTF-8";

	
	/**
	 * Spot organization / location / city / state mentions in text
	 * 
	 * @param text text to parse
	 * @param timeourInSecods connection timeout
	 * @param URLencodedTypes type of mention to spot
	 * @return
	 */
	public static Map<String, String> spotTextOrganization(String text, Integer timeourInSecods, String URLencodedTypes) {

		logger.info("Spotlight disambiguation > text: " + ((text != null) ? text : "NULL") + " - URL encoded type: " + ((URLencodedTypes != null) ? URLencodedTypes : "NULL"));
		
		Map<String, String> surfaceURImap = new HashMap<String, String>();

		Map<String, Object> mapObject = new HashMap<String, Object>();

		StringBuffer result = new StringBuffer();

		Double confidence = 0.5d;
		Integer support = 0;
		String spotter = "Default";
		String disambiguator = "Default";
		String policy = "whitelist";
		String types = URLencodedTypes;
		String sparql = "";


		try {				
			String url = spotURL + "confidence=" + confidence
					+ "&text=" + URLEncoder.encode(text.trim(), charset) 
					+ "&support=" + support 
					+ "&spotter=" + spotter 
					+ "&disambiguator=" + disambiguator
					+ "&policy=" + policy 
					+ "&types=" + types
					+ "&sparql=" + sparql;

			logger.debug("Querying: " + url);

			// Execute query
			HttpGet post = new HttpGet(url);

			// Setting timeout
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(5 * 1000)
					.setConnectionRequestTimeout(timeourInSecods * 1000)
					.setSocketTimeout(5 * 1000).build();

			post.setConfig(config);

			// Proper header settings
			post.setHeader("Accept", "application/json");

			// Invoke service
			HttpResponse response;
			try {
				logger.debug("Sending SPOTLIGHT request...");
				long startTime = System.currentTimeMillis();
				response = client.execute(post);
				long endTime = System.currentTimeMillis();
				logger.debug("SPOTLIGHT request processed in " + (endTime - startTime)  + " milliseconds with response Code : " + response.getStatusLine().getStatusCode());

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				try {

					if(result != null && !result.toString().equals("")) {
						mapObject = mapper.readValue(result.toString(), new TypeReference<Map<String, Object>>() {});
						if(mapObject.containsKey("Resources")) {
							List<Object> resourceList = (List<Object>) mapObject.get("Resources");
							if(resourceList != null && resourceList.size() > 0) {
								for(Object resource : resourceList) {
									Map<String, Object> resMap = (Map<String, Object>) resource;
									String URIstr = "";
									String surfaceForm = "";
									if(resMap.containsKey("@URI")) {
										URIstr = (String) resMap.get("@URI");
									}
									if(resMap.containsKey("@surfaceForm")) {
										surfaceForm = (String) resMap.get("@surfaceForm");
									}
									if(URIstr != null && !URIstr.equals("") && surfaceForm != null && !surfaceForm.equals("")) {
										surfaceURImap.put(surfaceForm, URIstr);
									}
								}
							}
						}
					}
				} catch (JsonGenerationException e) {

				} catch (JsonMappingException e) {

				} catch (IOException e) {

				}

			} catch (ClientProtocolException e) {
				logger.error("Invoking service - ClientProtocolException: " + e.getMessage());
			} catch (IOException e) {
				logger.error("Invoking service - IOException: " + e.getMessage());
			}


			logger.debug("SPOTTED: " + result.toString());

		}
		catch (Exception e) {
			logger.error("ERROR SPOTTING ORGANIZATIONS: " + e.getMessage());
		}


		if(waitTimeAfterRequestInSeconds != null && waitTimeAfterRequestInSeconds > 0) {
			try {
				Thread.currentThread().sleep(waitTimeAfterRequestInSeconds * 1000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
		
		logger.info("Spotlight disambiguation completed");
		
		return surfaceURImap;
	}
	
	
	

	/**
	 * Compare Bing search result numbers for different queries
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> surfaceURImap = SpotlightUtil.spotTextOrganization("Theoretical Computer Science, TU Dresden, Germany", 15, SpotlightUtil.ORGANIZATIONtype);
		for(Map.Entry<String, String> entry : surfaceURImap.entrySet()) {
			logger.info(entry.getKey() + "  -  " + entry.getValue());
		}
		
		surfaceURImap = SpotlightUtil.spotTextOrganization("Theoretical Computer Science, TU Dresden, Germany", 15, SpotlightUtil.LOCATIONtype);
		for(Map.Entry<String, String> entry : surfaceURImap.entrySet()) {
			logger.info(entry.getKey() + "  -  " + entry.getValue());
		}
		
		surfaceURImap = SpotlightUtil.spotTextOrganization("Theoretical Computer Science, TU Dresden, Germany", 15, SpotlightUtil.CITYtype);
		for(Map.Entry<String, String> entry : surfaceURImap.entrySet()) {
			logger.info(entry.getKey() + "  -  " + entry.getValue());
		}
		
		surfaceURImap = SpotlightUtil.spotTextOrganization("Theoretical Computer Science, TU Dresden, Germany", 15, SpotlightUtil.STATEtype);
		for(Map.Entry<String, String> entry : surfaceURImap.entrySet()) {
			logger.info(entry.getKey() + "  -  " + entry.getValue());
		}
	}

}
