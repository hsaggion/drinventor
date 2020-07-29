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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;
import gate.Document;

/**
 * Utility class to compute TD-IDF vectors from textual excerpts.
 * 
 *
 */
public class TFIDFVectorWiki {

	private static final Logger logger = LoggerFactory.getLogger(TFIDFVectorWiki.class);

	private SimLangENUM lang;

	// Parameters to extract terms
	public boolean onlyWordKind = true;
	public boolean getLemma = true;
	public boolean toLowerCase = true;
	public boolean removeStopWords = true;
	public boolean appendPOS = true;
	public Set<String> stopWordsList = new HashSet<String>();
	
	// Constructor
	public TFIDFVectorWiki(SimLangENUM langIN) throws InvalidParameterException {
		if(langIN == null) {
			throw new InvalidParameterException("Specify a language to load a tfidf word list and stopword list.");
		}
		
		stopWordsList = StopWordList.getStopwordList(langIN);
		lang = langIN;
	}

	public Map<String, Double> computeTFIDFvect(Annotation ann, Document doc) {
		Map<String, Double> retMap = new HashMap<String, Double>();

		List<String> annotation_terms = extractTokenList(ann, doc, null, onlyWordKind, getLemma, toLowerCase, appendPOS, removeStopWords, stopWordsList);
		
		// Get all the distinct annotation terms
		Set<String> annotation_terms_set = new HashSet<String>(annotation_terms);
		
		for(String term : annotation_terms_set) {
			try {
				Integer tf = 0;
				for(int k = 0; k < annotation_terms.size(); k++) {
					if(annotation_terms.get(k).equals(term)) {
						tf++;
					}
				}
				double normalized_tf = (double) tf / (double) annotation_terms.size();
				
				double idf = 0d;
				Integer documentFreq = WikipediaLemmaPOSfFrequency.getDocumentFrequency(lang, term);
				if(documentFreq != null && documentFreq > 0) {
					idf = Math.log( ((double) WikipediaLemmaPOSfFrequency.getTotNumDoc(lang)) / ((double) documentFreq) ); 
				}
				
				retMap.put(term, normalized_tf * idf);
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}

		}
		
		return retMap;
	}
	
	/**
	 * Compute the TF IDF similarity among the two token lists
	 * 
	 * Term frequency of a sentence: number of times the token appears in the sentence
	 * Inverse document frequency: logarithm of the total number of documents divided by the number of docs in which the token appears
	 * 
	 * @param tokenSent1
	 * @param tokenSetn2
	 * @return
	 */
	public double cosSimTFIDF(Map<String, Double> tokenDoc1, Map<String, Double> tokenDoc2) {
		Multiset<String> tfMS = HashMultiset.create();
		for(String str : tokenDoc1.keySet()) { 
			tfMS.add(str);
		}
		for(String str : tokenDoc2.keySet()) { 
			tfMS.add(str);
		}

		// Compute the word vectors
		String[] wordArray = tfMS.elementSet().toArray(new String[tfMS.elementSet().size()]);

		Double[] vectSent1 = new Double[wordArray.length];
		for(int i = 0; i < wordArray.length; i++) {
			try {
				if(tokenDoc1.containsKey(wordArray[i])) {
					vectSent1[i] = tokenDoc1.get(wordArray[i]);
				}
				else {
					vectSent1[i] = 0d;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}

		}


		Double[] vectSent2 = new Double[wordArray.length];
		for(int i = 0; i < wordArray.length; i++) {
			try {
				if(tokenDoc2.containsKey(wordArray[i])) {
					vectSent2[i] = tokenDoc2.get(wordArray[i]);
				}
				else {
					vectSent2[i] = 0d;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception calculating sentSim_TFIDF_DocCentric");
			}
		}

		// Compute cosine similarity of vectSent1 and vectSent2
		double sumNumer = 0d;
		for(int i = 0; i < vectSent1.length; i++) {
			sumNumer += (vectSent1[i] * vectSent2[i]);
		}

		double sumDenom1 = 0d;
		for(int i = 0; i < vectSent1.length; i++) {
			sumDenom1 += vectSent1[i] * vectSent1[i];
		}
		double sumDenom2 = 0d;
		for(int i = 0; i < vectSent2.length; i++) {
			sumDenom2 += (vectSent2[i] * vectSent2[i]);
		}
		double sumDenom = Math.sqrt(sumDenom1) * Math.sqrt(sumDenom2);
		
		logger.debug("Cosine similarity - " + ((sumDenom != 0d) ? (sumNumer / sumDenom) : 0d));
		
		return (sumDenom != 0d) ? (sumNumer / sumDenom) : 0d;
	}
	
	/**
	 * Compute the TF IDF similarity among the two document annotations
	 * 
	 * Term frequency of a sentence: number of times the token appears in the sentence
	 * Inverse document frequency: logarithm of the total number of documents divided by the number of docs in which the token appears
	 * 
	 * @param ann1
	 * @param doc1
	 * @param ann2
	 * @param doc2
	 * @return
	 */
	public double cosSimTFIDF(Annotation ann1, Document doc1, Annotation ann2, Document doc2) {
		return cosSimTFIDF(computeTFIDFvect(ann1, doc1), computeTFIDFvect(ann2, doc2));
	}
	
	
	/**
	 * Given an annotation of a TDDocument, extract the list of tokens (eventually repeated in case of multiple occurrences)
	 * 
	 * @param ann
	 * @param doc
	 * @param onlyWordKind
	 * @param getLemma
	 * @param toLowerCase
	 * @param removeStopWords
	 * @return
	 */
	public static List<String> extractTokenList(Annotation ann, Document doc, TokenFilterInterface tokenFilter, 
			boolean onlyWordKind, boolean getLemma, boolean toLowerCase, boolean appendPOS, boolean removeStopWords, Set<String> stopWordsList) {
		List<String> annotationTokens = new ArrayList<String>();

		List<Integer> tokenIDnotToConsider = new ArrayList<Integer>();
		if(tokenFilter != null) {
			tokenIDnotToConsider = tokenFilter.getTokenListNotToConsider(ann, doc);
		}
		

		List<Annotation> intersectingTokensList = gate.Utils.inDocumentOrder(
				doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(
						ann.getStartNode().getOffset(), 
						ann.getEndNode().getOffset() ));

		if(intersectingTokensList != null && intersectingTokensList.size() > 0) {

			for(Annotation tokenAnn : intersectingTokensList) {
				if(tokenFilter != null && tokenIDnotToConsider.contains(tokenAnn.getId())) {
					continue; // It's a token not to consider
				}

				if(onlyWordKind && 
					(!tokenAnn.getFeatures().containsKey(ImporterBase.token_POSfeat) || ((String) tokenAnn.getFeatures().get(ImporterBase.token_POSfeat)).toLowerCase().startsWith("f")) ) {
					continue; // It's a token with POS feature staring with 'F', thus a punctuation, not to consider
				}

				String string = "";
				if(getLemma) {
					string = (String) ((tokenAnn.getFeatures().containsKey(ImporterBase.token_LemmaFeat)) ? tokenAnn.getFeatures().get(ImporterBase.token_LemmaFeat) : "");
				}
				else {
					string = GateUtil.getAnnotationText(tokenAnn, doc).orElse("");
				}
				string = (toLowerCase) ? string.trim().toLowerCase(): string.trim();

				if(!Strings.isNullOrEmpty(string)) {
					if(!removeStopWords || (removeStopWords && !stopWordsList.contains(string.toLowerCase().trim())) ) {
						if(appendPOS) {
							if(tokenAnn.getFeatures().containsKey(ImporterBase.token_POSfeat) && !Strings.isNullOrEmpty((String) tokenAnn.getFeatures().get(ImporterBase.token_POSfeat))) {
								annotationTokens.add(string + "_" + ((String) tokenAnn.getFeatures().get(ImporterBase.token_POSfeat)).substring(0, 1));
							}
						}
						else {
							annotationTokens.add(string);
						}
					}
				}
			}
		}

		return annotationTokens;
	}
}
