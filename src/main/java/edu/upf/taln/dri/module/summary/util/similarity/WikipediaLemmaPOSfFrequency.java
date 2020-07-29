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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;

/**
 * Utility class to retrieve lemma document frequency in Wikipedia.
 * 
 *
 */
public class WikipediaLemmaPOSfFrequency {

	private static final Logger logger = LoggerFactory.getLogger(WikipediaLemmaPOSfFrequency.class);

	private static Map<SimLangENUM, Map<String, Integer>> wordFrequency = new HashMap<SimLangENUM, Map<String, Integer>>();

	private static String resourcePath = "";

	public static void setResourcePath(String resPath) {
		resourcePath = resPath;
		resourcePath = (resourcePath.endsWith(File.separator)) ? resourcePath : resourcePath + File.separator;
	}
	
	/**
	 * Load a map with LEMMA_POS key and document frequency from Wikipedia
	 * 
	 * @param lang
	 * @return
	 * @throws InvalidParameterException 
	 * @throws TextDigesterException
	 */
	private static void loadLemmaPOSdocFrequencyMap(SimLangENUM lang) throws InternalProcessingException, InvalidParameterException {


		if(lang != null) {

			if(wordFrequency.containsKey(lang) && wordFrequency.get(lang) != null) {
				return;
			}

			Map<String, Integer> retMap = new HashMap<String, Integer>();

			String tfidfFileName = resourcePath + "tfidfmodels" + File.separator;

			switch(lang) {
			case English:
				tfidfFileName += "enwiki_lemma_POS_TF_DF.dat";
				break;
			case Spanish:
				tfidfFileName += "eswiki_lemma_POS_TF_DF.dat";
				break;
			case Catalan:
				tfidfFileName += "cawiki_lemma_POS_TF_DF.dat";
				break;
			default:
				tfidfFileName += "enwiki_lemma_POS_TF_DF.dat";
			}

			File tfidfFile = new File(tfidfFileName);

			if(tfidfFile != null && tfidfFile.exists() && tfidfFile.isFile()) {


				try(BufferedReader br = new BufferedReader(new FileReader(tfidfFile))) {
					for(String line; (line = br.readLine()) != null; ) {
						line = line.trim();
						try {
							if(line.length() > 0) {
								String[] splitLine = line.split(" ");
								if(splitLine.length == 4) {
									String lemma = splitLine[0];
									String POS = splitLine[1];
									// String termFrequ = splitLine[2];
									String docFrequ = splitLine[3];

									if(!Strings.isNullOrEmpty(lemma) && !Strings.isNullOrEmpty(POS) && !Strings.isNullOrEmpty(docFrequ)) {
										retMap.put(lemma.trim() + "_" + POS.trim().substring(0, 1), Integer.valueOf(docFrequ));
									}
								}
							}
						}
						catch(Exception e) {
							/* Do nothing */
						}
					}
				} catch (IOException e) {
					throw new InternalProcessingException("Impossible to read tfidf list for " + lang + " from file: '" +
							((tfidfFileName != null) ? tfidfFileName : "NULL")+ "' - " + e.getMessage());
				}

				logger.info("Loaded tfidf of " + lang + " with: " + retMap.size() + " words.");

				wordFrequency.put(lang, retMap);	

			}
			else {
				throw new InternalProcessingException("Impossible to read TFIDF for " + lang + " from file: '" +
						((tfidfFileName != null) ? tfidfFileName : "NULL")+ "'");
			}
		}
		else {
			throw new InvalidParameterException("Specify a language to load a tfidf word list.");
		}

		return;
	}

	public static Integer getTotNumDoc(SimLangENUM lang) throws InternalProcessingException, InvalidParameterException {

		if(lang == null) {
			throw new InvalidParameterException("Please, specify a language");
		}

		switch(lang) {
		case English:
			return 4487682;
		case Spanish:
			return 1061535;
		case Catalan:
			return 450885;
		default:
			return null;
		}
	}

	public static Integer getDocumentFrequency(SimLangENUM lang, String lemmaPOS) throws InternalProcessingException, InvalidParameterException {

		if(lang == null) {
			throw new InternalProcessingException("Please, specify a language");
		}

		if(Strings.isNullOrEmpty(lemmaPOS)) {
			return 0;
		}

		loadLemmaPOSdocFrequencyMap(lang);

		return (wordFrequency.get(lang).containsKey(lemmaPOS) ? wordFrequency.get(lang).get(lemmaPOS) : 0);
	}

}
