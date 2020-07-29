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
package edu.upf.taln.dri.module.summary.lexrank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.languageDetector.LanguageDetector;
import edu.upf.taln.dri.module.summary.lexrank.summ.DummyItem;
import edu.upf.taln.dri.module.summary.lexrank.summ.LexRankResults;
import edu.upf.taln.dri.module.summary.lexrank.summ.LexRanker;
import edu.upf.taln.dri.module.summary.lexrank.summ.MapUtil;
import edu.upf.taln.dri.module.summary.util.similarity.SimLangENUM;
import edu.upf.taln.dri.module.summary.util.similarity.TFIDFVectorWiki;
import gate.Annotation;
import gate.Document;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;



/**
 * LexRank summarizer
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Summary generator")
public class LexRankSummarizer extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(LexRankSummarizer.class);	

	private static final long serialVersionUID = 1L;

	// *******************************
	// LexRank parameters:
	// > linkThrashold_LR: The LexRank paper suggests a value of 0.1
	private double linkThrashold_LR = 0.01d;
	// > isContinuous_LR: Whether or not to use a continuous version of the LexRank algorithm, If set to false,
	// all similarity links above the similarity threshold will be considered equal; otherwise, the similarity
	// scores are used. The paper authors note that non-continuous LexRank seems to perform better.
	private boolean isContinuous_LR = false; 
	// *******************************

	private static Map<SimLangENUM, TFIDFVectorWiki> TFIDFcomput = new HashMap<SimLangENUM, TFIDFVectorWiki>();

	@Override
	public Resource init() {
		return this;
	}


	public void execute() throws ExecutionException {

		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

		// STEP 1: If we consider that the document to summarize contains N sentence annotations, we have to generate a map
		// that maps each sentence to an integer starting from 0 up to (N-1) in order to support the generation of the 
		// similarity matrix
		Map<Integer, Annotation> sentenceIndexToAnnotationMap = new HashMap<Integer, Annotation>();

		// Getting all sentences of the document that could be potentially included in the summary 
		// Select body sentence list
		Set<Integer> abstractSentenceIDs = new HashSet<Integer>();
		List<Annotation> abstractAnnList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.abstractAnnType);
		for(Annotation abstractAnn : abstractAnnList) {
			if(abstractAnn != null) {
				List<Annotation> sentInAbstractList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractAnn);
				for(Annotation sentInAbst : sentInAbstractList) {
					if(sentInAbst != null && sentInAbst.getId() != null) {
						abstractSentenceIDs.add(sentInAbst.getId());
					}
				}
			}
		}

		List<Annotation> allSentenceAnnList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		List<Annotation> sentenceAnntoationSelected = new ArrayList<Annotation>();
		for(Annotation ann : allSentenceAnnList) {
			if(ann != null && ann.getId() != null && !abstractSentenceIDs.contains(ann.getId())) {
				sentenceAnntoationSelected.add(ann);
			}
		}

		if(sentenceAnntoationSelected == null || sentenceAnntoationSelected.size() < 1) {
			logger.error(">>> NO SENTENCES SELECTED TO GENERATE THE SUMMARY");
			return;
		}

		// Check the majority language of the body of the paper (if English or Spanish)
		String selectedMajorityLang = LanguageDetector.getMajorityLanguage(sentenceAnntoationSelected, ImporterBase.langAnnFeat);
		
		// Only English and Spanish considered!
		TFIDFVectorWiki currentTFIDFVectorWiki = null;
		if(!selectedMajorityLang.trim().toLowerCase().equals("es")) {
			if(!TFIDFcomput.containsKey(SimLangENUM.English)) {
				try {
					TFIDFcomput.put(SimLangENUM.English, new TFIDFVectorWiki(SimLangENUM.English));
				} catch (InvalidParameterException e) {
					Util.notifyException("Impossible to initialize the TF IDF tables accessor (English))", e, logger);
				}
			}
			currentTFIDFVectorWiki = TFIDFcomput.get(SimLangENUM.English);
		}
		else if(selectedMajorityLang.trim().toLowerCase().equals("es") && !TFIDFcomput.containsKey(SimLangENUM.Spanish)) {
			if(!TFIDFcomput.containsKey(SimLangENUM.Spanish)) {
				try {
					TFIDFcomput.put(SimLangENUM.Spanish, new TFIDFVectorWiki(SimLangENUM.Spanish));
				} catch (InvalidParameterException e) {
					Util.notifyException("Impossible to initialize the TF IDF tables accessor (Spanish))", e, logger);
				}
			}
			currentTFIDFVectorWiki = TFIDFcomput.get(SimLangENUM.Spanish);
		}
		
		
		Integer sentenceIndex = 0;
		for(Annotation sentenceAnnotation : sentenceAnntoationSelected) {
			if(sentenceAnnotation != null) {
				sentenceIndexToAnnotationMap.put(sentenceIndex++, sentenceAnnotation);
			}
		}

		logger.info("*** Start Ranking " + sentenceIndexToAnnotationMap.size() + " sentences...");

		// STEP 2: Lex rank needs a sentence similarity matrix - square symmetric matrix, N * N, where N is the size of the sentenceIndexToAnnotationMap
		// Instantiate and initialize the matrix with 0 sentence similarity values
		double[][] similarityMatrix = new double[sentenceIndexToAnnotationMap.size()][sentenceIndexToAnnotationMap.size()];

		for(int i = 0; i < sentenceIndexToAnnotationMap.size(); i++) {
			for(int j = 0; j < sentenceIndexToAnnotationMap.size(); j++) {
				similarityMatrix[i][j] = 0d;
			}
		}

		logger.info("*** Instantiated similarity matrix of size " + sentenceIndexToAnnotationMap.size());

		// STEP 3: Populate the similarity matrix computing the similarity value for each pair of sentences
		Integer computedSimilarityCounter = 0;
		for(int x_val = 0; x_val < sentenceIndexToAnnotationMap.size(); x_val++) {

			for(int y_val = 0; y_val <= x_val; y_val++) {
				if(y_val < x_val) {

					// Get the pair of sentences to analyze
					Annotation firstSentenceAnn = sentenceIndexToAnnotationMap.get(x_val);
					Annotation secondSentenceAnn = sentenceIndexToAnnotationMap.get(y_val);

					// Compute the similarity value between the two sentences (double, usually from 0 to 1, where 1 is total similarity)
					double similarityValue = computeSentenceSimilarity(this.document, firstSentenceAnn, secondSentenceAnn, currentTFIDFVectorWiki);

					// Store the similarity value in the matrix
					similarityMatrix[y_val][x_val] = similarityMatrix[x_val][y_val] = similarityValue;

					computedSimilarityCounter++;
					if(computedSimilarityCounter % 100 == 0) {
						logger.info(" ...computed similarity of " + computedSimilarityCounter + " sentence pairs over " + ( (int) (Math.pow( (double) sentenceIndexToAnnotationMap.size(), 2d) / 2d ) ));
					}

				}
				else if(y_val == x_val) {
					similarityMatrix[y_val][x_val] = 1d;
				}
			}
		}

		// Check similarity matrix consistency
		for (int i = 0; i < similarityMatrix.length; ++i) {
			for (int j = 0; j <= i; ++j) {
				if(similarityMatrix[i][j] != similarityMatrix[j][i]) {
					logger.error(">>> NOT SYMMATRIC MATRIX, ERROR AT INDEXES [" + i + ", " + j + "] " + 
							"->" + similarityMatrix[i][j] + " == " + similarityMatrix[j][i]);
				}
			}
		}

		logger.info("*** Populated similarity matrix of size " + sentenceIndexToAnnotationMap.size());

		// Printing similarity matrix - UNCOMMENT IF NEEDED
		/*
				for(int i = 0; i < sentenceIndexToAnnotationMap.size(); i++) {
					for(int j = 0; j < sentenceIndexToAnnotationMap.size(); j++) {
						System.out.print(similarityMatrix[i][j] + " ");
					}
					System.out.print("\n");
				}
		 */

		// STEP 4: LexRank computation
		System.out.println("*** Starting LexRank computations...");
		List<DummyItem> items = new ArrayList<DummyItem>();
		for (int i = 0; i < similarityMatrix.length; ++i) {
			items.add(new DummyItem(i, similarityMatrix));
		}
		LexRankResults<DummyItem> results = LexRanker.rank(items, linkThrashold_LR, isContinuous_LR);

		double max = results.scores.get(results.rankedResults.get(0));

		HashMap<Integer, Double> rankedSentenceMap = new HashMap<Integer, Double>();
		for(DummyItem res : results.rankedResults) {
			double itemScore = results.scores.get(res) / max; // Normalize to 1
			rankedSentenceMap.put(res.id, itemScore);
		}

		// In the SortedSet sortedRrankedSentenceMap there is a sorted (by decreasing LexRank / centrality value) collection of Map entries
		// each one with the index of the sentence in the similarity matrix as key and the ranking as value
		Map<Integer, Double> sortedRrankedSentenceMap = MapUtil.sortByValue(rankedSentenceMap);


		// STEP 5: Print the sorted list of sentences (from the most to the least relevant as scored by LexRank)
		// System.out.println("\nRANKED SENTENCE LIST:");
		// Integer rankingPosition = 1;
		for(Map.Entry<Integer, Double> sortedRrankedSentenceMapEntry : sortedRrankedSentenceMap.entrySet()) {
			try {
				// Get sentence Annotation object
				Annotation sentenceAnnotation = sentenceIndexToAnnotationMap.get(sortedRrankedSentenceMapEntry.getKey());

				sentenceAnnotation.setFeatures((sentenceAnnotation.getFeatures() != null) ? sentenceAnnotation.getFeatures() : gate.Factory.newFeatureMap()); 
				sentenceAnnotation.getFeatures().put(ImporterBase.sentence_lexRankScore, sortedRrankedSentenceMapEntry.getValue());

				retMap.put(sentenceAnnotation, sortedRrankedSentenceMapEntry.getValue());

				// String sentenceText = gateDoc.getContent().getContent(sentenceAnnotation.getStartNode().getOffset(), sentenceAnnotation.getEndNode().getOffset()).toString();

				// System.out.println(rankingPosition++ + " > LexRank value: " + sortedRrankedSentenceMapEntry.getValue() + " :"
				//		+ "\n >>> TEXT: " + sentenceText);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Given two sentences of a document, the similarity value between them is returned as double.
	 * 
	 * SUBSTITUTE WITH CUSTOM SENTENCE SIMILARITY
	 * 
	 * @param gateDoc
	 * @param sentence1
	 * @param sentence2
	 * @return
	 */
	private double computeSentenceSimilarity(Document doc, Annotation sentence1, Annotation sentence2, TFIDFVectorWiki currentTFIDFVectorWiki) {
		return currentTFIDFVectorWiki.cosSimTFIDF(sentence1, doc, sentence2, doc);
	}

	@Override
	public boolean resetAnnotations() {
		
		List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		
		for(Annotation sentAnn : sentenceAnnList) {
			if(sentAnn != null && sentAnn.getFeatures() != null) {
				sentAnn.getFeatures().remove(ImporterBase.sentence_lexRankScore);
			}
		}
		
		return false;
	}

}
