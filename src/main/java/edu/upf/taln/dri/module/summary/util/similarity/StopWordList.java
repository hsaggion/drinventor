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
package edu.upf.taln.dri.module.summary.util.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upf.taln.dri.lib.exception.InternalProcessingException;

/**
 * Loader of stop word lists from external files
 * 
 *
 */
public class StopWordList {

	private static final Logger logger = LoggerFactory.getLogger(StopWordList.class);

	private static Map<SimLangENUM, Set<String>> stopwordLists = new HashMap<SimLangENUM, Set<String>>();

	private static String resourcePath = "";
	
	public static void setResourcePath(String resPath) {
		resourcePath = resPath;
		resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;
	}
	
	private static void initStopWordList(SimLangENUM lang) throws InternalProcessingException {

		if(lang != null && !stopwordLists.containsKey(lang)) {
			resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;

			String stopwordsFileName = resourcePath + "stopwords" + File.separator;

			switch(lang) {
			case English:
				stopwordsFileName += "stopwords_EN.list";
				break;
			case Spanish:
				stopwordsFileName += "stopwords_ES.list";
				break;
			case Catalan:
				stopwordsFileName += "stopwords_CA.list";
				break;
			default:
				stopwordsFileName += "stopwords_EN.list";
			}

			File stopwordsFile = new File(stopwordsFileName);

			if(stopwordsFile != null && stopwordsFile.exists() && stopwordsFile.isFile()) {
				// Load stopwords
				Set<String> stopwordsLst = new HashSet<String>();
				stopwordLists.put(lang, stopwordsLst);

				try(BufferedReader br = new BufferedReader(new FileReader(stopwordsFile))) {
					for(String line; (line = br.readLine()) != null; ) {
						line = line.trim();
						if(line.length() > 0) {
							stopwordsLst.add(line);
						}
					}
				} catch (IOException e) {
					throw new InternalProcessingException("Impossible to read stopword list for " + lang + " from file: '" +
							((stopwordsFileName != null) ? stopwordsFileName : "NULL")+ "' - " + e.getMessage());
				}

				logger.info("Loaded " + lang + " stop words: " + stopwordsLst.size() + " words.");

			}
			else {
				throw new InternalProcessingException("Impossible to read stopword list for " + lang + " from file: '" +
						((stopwordsFileName != null) ? stopwordsFileName : "NULL")+ "'");
			}
		}
		else {
			throw new InternalProcessingException("Specify a language to load a stopword list.");
		}

	}


	public static boolean getStopwords(SimLangENUM lang, String word) {

		if(lang != null && stopwordLists.get(lang) == null) {
			try {
				initStopWordList(lang);
			} catch (InternalProcessingException e) {
				e.printStackTrace();
				logger.error("ERROR: " + e.getMessage());
			}
		}

		if(lang != null && word != null && !word.equals("") && stopwordLists.get(lang) != null) {
			return stopwordLists.get(lang).contains(word.trim().toLowerCase());
		}

		return false;
	}

	public static Set<String> getStopwordList(SimLangENUM lang) {

		if(lang != null && stopwordLists.get(lang) == null) {
			try {
				initStopWordList(lang);
			} catch (InternalProcessingException e) {
				e.printStackTrace();
				logger.error("ERROR: " + e.getMessage());
			}
		}

		if(lang != null && stopwordLists.get(lang) != null) {
			return Collections.unmodifiableSet(stopwordLists.get(lang));
		}

		return new HashSet<String>();
	}

}
