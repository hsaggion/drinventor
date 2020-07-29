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
package edu.upf.taln.dri.module.summary.titlesim;

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
 * Title similarity summarizer
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Summary generator")
public class TitleSimSummarizer extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(TitleSimSummarizer.class);

	private static final long serialVersionUID = 1L;

	private static Map<SimLangENUM, TFIDFVectorWiki> TFIDFcomput = new HashMap<SimLangENUM, TFIDFVectorWiki>();

	@Override
	public Resource init() {
		return this;
	}


	public void execute() throws ExecutionException {

		Map<Annotation, Double> retMap = new HashMap<Annotation, Double>();

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

		// Get title
		List<Annotation> titleAnnList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.titleAnnType);
		if(titleAnnList == null || titleAnnList.size() == 0) {
			logger.error(">>> NO TITLE AVAILABLE TO GENERATE THE SUMMARY");
			return;
		}

		Annotation titleAnn = null;
		if(titleAnnList.size() == 1) {
			titleAnn = titleAnnList.iterator().next();
		}
		else {
			for(Annotation titleAnnElem : titleAnnList) {
				if(titleAnnElem != null) {
					String titleLang = GateUtil.getStringFeature(titleAnnElem, ImporterBase.langAnnFeat).orElse(null);

					if(titleLang != null && titleLang.toLowerCase().trim().equals("es") && selectedMajorityLang.trim().toLowerCase().equals("es")) {
						titleAnn = titleAnnElem;
						break;
					}
					else if( (titleLang == null || !titleLang.toLowerCase().trim().equals("es")) && !selectedMajorityLang.trim().toLowerCase().equals("es")) {
						titleAnn = titleAnnElem;
						break;
					}

				}
			}

			if(titleAnn == null) {
				titleAnn = titleAnnList.iterator().next();
			}
		}

		if(titleAnn == null) {
			logger.error(">>> NO TITLE AVAILABLE TO GENERATE THE SUMMARY");
			return;
		}		


		// Rank sentences
		for(Annotation sentenceAnn : sentenceAnntoationSelected) {
			if(sentenceAnn != null) {
				try {
					sentenceAnn.setFeatures((sentenceAnn.getFeatures() != null) ? sentenceAnn.getFeatures() : gate.Factory.newFeatureMap());

					Double similarityWithTitle = computeSentenceSimilarity(this.document, sentenceAnn, titleAnn, currentTFIDFVectorWiki);

					sentenceAnn.getFeatures().put(ImporterBase.sentence_titleSimScore, similarityWithTitle);

					retMap.put(sentenceAnn, similarityWithTitle);
				}
				catch(Exception e) {
					/* Do nothing */
				}
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
				sentAnn.getFeatures().remove(ImporterBase.sentence_titleSimScore);
			}
		}
		return false;
	}

}
