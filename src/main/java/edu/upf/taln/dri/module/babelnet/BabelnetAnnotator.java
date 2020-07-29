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
package edu.upf.taln.dri.module.babelnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.connector.babelnet.BabelfyUtil;
import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.languageDetector.LanguageDetector;
import gate.Annotation;
import gate.Document;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.GateException;
import it.uniroma1.lcl.babelfy.commons.BabelfyToken;
import it.uniroma1.lcl.babelfy.commons.PosTag;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.commons.annotation.TokenOffsetFragment;
import it.uniroma1.lcl.jlt.util.Language;


/**
 * This module enrich the textual contents of a paper by applying WSD, by invoking BabelNet
 * REFERENCE: http://babelnet.org/
 * 
 */
@CreoleResource(name = "DRI Modules - BabelNet annotator")
public class BabelnetAnnotator extends AbstractLanguageAnalyser implements ProcessingResource, Serializable, DRIModule {

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static Logger logger = Logger.getLogger(BabelnetAnnotator.class);

	// Input set for annotation
	private String sentenceAnnotationSetToAnalyze = ImporterBase.driAnnSet;
	private String sentenceAnnotationTypeToAnalyze = ImporterBase.sentenceAnnType;
	private String tokenAnnotationSetToAnalyze = ImporterBase.driAnnSet;
	private String tokenAnnotationTypeToAnalyze = ImporterBase.tokenAnnType;
	private Set<String> sentenceIdsToAnalyze = new HashSet<String>();
	private String babelnetAPIkey = "";
	private String babelnetLanguage = null;


	public String getBabelnetAPIkey() {
		return new String(babelnetAPIkey);
	}

	@RunTime
	@CreoleParameter(defaultValue = "", comment = "Set the babelNet API key - http://babelnet.org/")
	public void setBabelnetAPIkey(String babelnetAPIkey) {
		this.babelnetAPIkey = babelnetAPIkey;
	}

	public String getBabelnetLanguage() {
		return new String(babelnetLanguage);
	}

	@RunTime
	@CreoleParameter(defaultValue = "", comment = "Set the language to consider for BabelNet disambiguation")
	public void setBabelnetLanguage(String babelnetLanguage) {
		this.babelnetLanguage = babelnetLanguage;
	}

	public String getSentenceAnnotationSetToAnalyze() {
		return sentenceAnnotationSetToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "Set name of the annotation set where the sentences to parse are annotated")
	public void setSentenceAnnotationSetToAnalyze(
			String sentenceAnnotationSetToAnalyze) {
		this.sentenceAnnotationSetToAnalyze = sentenceAnnotationSetToAnalyze;
	}

	public String getSentenceAnnotationTypeToAnalyze() {
		return sentenceAnnotationTypeToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Sentence", comment = "The type of sentence annotations")
	public void setSentenceAnnotationTypeToAnalyze(
			String sentenceAnnotationTypeToAnalyze) {
		this.sentenceAnnotationTypeToAnalyze = sentenceAnnotationTypeToAnalyze;
	}

	public String getTokenAnnotationSetToAnalyze() {
		return tokenAnnotationSetToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "Set name of the annotation set where the token of the sentences to parse are annotated")
	public void setTokenAnnotationSetToAnalyze(String tokenAnnotationSetToAnalyze) {
		this.tokenAnnotationSetToAnalyze = tokenAnnotationSetToAnalyze;
	}

	public String getTokenAnnotationTypeToAnalyze() {
		return tokenAnnotationTypeToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Token", comment = "The type of token annotations")
	public void setTokenAnnotationTypeToAnalyze(String tokenAnnotationTypeToAnalyze) {
		this.tokenAnnotationTypeToAnalyze = tokenAnnotationTypeToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "", comment = "The ids of all the sentence type annotations to parse. If empty or null all annotations of sentence type will be parsed")
	public void setSentenceIdsToAnalyze(Set<String> sentenceIdsToAnalyze) {
		this.sentenceIdsToAnalyze = sentenceIdsToAnalyze;
	}

	public Set<String> getSentenceIdsToAnalyze() {
		return sentenceIdsToAnalyze;
	}


	@Override
	public Resource init() {

		return this;
	}

	@Override
	public void execute() {
		this.annotationReset = false;

		Language lang = Language.EN;
		if(babelnetLanguage.toLowerCase().trim().equals("english")) {
			lang = Language.EN;
		}
		else if(babelnetLanguage.toLowerCase().trim().equals("spanish")) {
			lang = Language.ES;
		}
		else {
			lang = Language.EN;
			logger.error("Error while setting the BabelNet language - set to English as default");
		}

		// Check input parameters
		sentenceAnnotationSetToAnalyze = StringUtils.defaultString(sentenceAnnotationSetToAnalyze, ImporterBase.driAnnSet);
		sentenceAnnotationTypeToAnalyze = StringUtils.defaultString(sentenceAnnotationTypeToAnalyze, ImporterBase.sentenceAnnType);
		tokenAnnotationSetToAnalyze = StringUtils.defaultString(tokenAnnotationSetToAnalyze, ImporterBase.driAnnSet);
		tokenAnnotationTypeToAnalyze = StringUtils.defaultString(tokenAnnotationTypeToAnalyze, ImporterBase.tokenAnnType);
		
		// Get all document sentences
		List<Annotation> sentenceAnnotationList = GateUtil.getAnnInDocOrder(this.document, sentenceAnnotationSetToAnalyze, sentenceAnnotationTypeToAnalyze);
		
		if(sentenceIdsToAnalyze != null && sentenceIdsToAnalyze.size() > 0) {
			Set<Integer> sentenceIdsToAnalyzeInt = new HashSet<Integer>();
			for(String sentIdToAnalyzeString : sentenceIdsToAnalyze) {
				try {
					if(sentIdToAnalyzeString != null && sentIdToAnalyzeString.trim().length() > 0) {
						Integer sentIdToAnalyzeInt = Integer.valueOf(sentIdToAnalyzeString);
						sentenceIdsToAnalyzeInt.add(sentIdToAnalyzeInt);
					}
				}
				catch(Exception e) {
					/* Do nothing */
				}
			}
			
			int originalSentCount = sentenceAnnotationList.size();
			int removedCount = 0;
			for (Iterator<Annotation> iter = sentenceAnnotationList.iterator(); iter.hasNext(); ) {
				Annotation ann = iter.next();
			    if (ann != null & ann.getId() != null && !sentenceIdsToAnalyzeInt.contains(ann.getId())) {
			        iter.remove();
			        removedCount++;
			    }
			}
			
			logger.info("   - Enabled sentence ID filter --> sentences to disambiguate: " + sentenceAnnotationList.size() + " (num sentences filtered out: " + removedCount + " over " + originalSentCount + ")");
		}
		
		
		// Split sentences into 40 units groups
		List<List<Annotation>> sentenceGroupsList = new ArrayList<List<Annotation>>();
		List<Annotation> sentenceGroupAppo = new ArrayList<Annotation>();
		for(int k = 0; k < sentenceAnnotationList.size(); k++) {
			sentenceGroupAppo.add(sentenceAnnotationList.get(k));
			if((k > 0 && (k % 40 == 0)) || (k == sentenceAnnotationList.size() - 1)) {
				sentenceGroupsList.add(sentenceGroupAppo);
				sentenceGroupAppo = new ArrayList<Annotation>();
			}
		}

		// Invoke Babelfy for every group of 80 sentences
		int babelnetSynsetCounter = 0;
		for(List<Annotation> sentenceGroup : sentenceGroupsList) {

			List<BabelfyToken> babelfyTokenListToDisambiguate = new ArrayList<BabelfyToken>();

			// Populate list of tokens to disambiguate - START
			Map<Integer, Integer> tokenListIDtoTokenDocIDmap = new HashMap<Integer, Integer>();

			for(Annotation sentenceAnn : sentenceGroup) {
				if(sentenceAnn != null) {
					List<Annotation> tokenOfSentenceList = GateUtil.getAnnInDocOrderContainedAnn(this.document, tokenAnnotationSetToAnalyze, tokenAnnotationTypeToAnalyze, sentenceAnn);

					// Get all tokens of a sentence
					int tokenListBeforeSentenceTokenAddition = babelfyTokenListToDisambiguate.size();

					// Get all in citation tokens
					List<Annotation> citSpanAnnList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType, sentenceAnn);

					for(Annotation tokenAnn : tokenOfSentenceList) {
						if(tokenAnn != null) {

							// Not consider token in citation span
							for(Annotation citSpanAnn : citSpanAnnList) {
								if(citSpanAnn != null && tokenAnn.withinSpanOf(citSpanAnn)) {
									continue;
								}
							}

							String tokenPOS = GateUtil.getStringFeature(tokenAnn, ImporterBase.token_POSfeat).orElse("_NO__POS_");
							PosTag pt = null;
							
							if(babelnetLanguage.toLowerCase().trim().equals("spanish")) {
								if(tokenPOS.toLowerCase().startsWith("n")) {
									pt = PosTag.NOUN;
								}
								else if(tokenPOS.toLowerCase().startsWith("a")) {
									pt = PosTag.ADJECTIVE;
								}
								else if(tokenPOS.toLowerCase().startsWith("v")) {
									pt = PosTag.VERB;
								}
								else if(tokenPOS.toLowerCase().startsWith("r")) {
									pt = PosTag.ADVERB;
								}
								else {
									// Not considered other POS tokens
									pt = PosTag.OTHER;
								}
							}
							else {
								if(tokenPOS.toLowerCase().startsWith("n")) {
									pt = PosTag.NOUN;
								}
								else if(tokenPOS.toLowerCase().startsWith("j")) {
									pt = PosTag.ADJECTIVE;
								}
								else if(tokenPOS.toLowerCase().startsWith("v")) {
									pt = PosTag.VERB;
								}
								else if(tokenPOS.toLowerCase().startsWith("rb")) {
									pt = PosTag.ADVERB;
								}
								else {
									// Not considered other POS tokens
									pt = PosTag.OTHER;
								}
							}
							
							

							if(pt != null) {
								BabelfyToken bt = new BabelfyToken(GateUtil.getAnnotationText(tokenAnn, this.document).orElse("_NO_TEXT_"), 
										GateUtil.getStringFeature(tokenAnn, ImporterBase.token_LemmaFeat).orElse("_NO_LEMMA_"), 
										pt, lang);
								babelfyTokenListToDisambiguate.add(bt);

								tokenListIDtoTokenDocIDmap.put(babelfyTokenListToDisambiguate.size() - 1, tokenAnn.getId());
							}
						}
					}

					if(babelfyTokenListToDisambiguate.size() > tokenListBeforeSentenceTokenAddition) {
						babelfyTokenListToDisambiguate.add(BabelfyToken.EOS);
					}
				} 
			}
			// Populate list of tokens to disambiguate - END

			// Disambiguate the list of tokens
			List<SemanticAnnotation> semanticAnnotations = new ArrayList<SemanticAnnotation>();

			try {
				logger.info("Disambiguating token list of size: " + babelfyTokenListToDisambiguate.size() + "...");
				semanticAnnotations = BabelfyUtil.disambiguateTokenList(babelnetAPIkey, babelfyTokenListToDisambiguate, lang);
			}
			catch(Exception e) {
				Util.notifyException("Disambiguating token list with api key: " + babelnetAPIkey, e, logger);
			}

			if(semanticAnnotations != null && semanticAnnotations.size() > 0) {
				logger.info("Identified " + semanticAnnotations.size() + " BabelNet concepts by analyzing the group of " + ((sentenceGroup != null) ? sentenceGroup.size() : "NULL") + " sentences.");
			}
			else {
				logger.error("Impossible to identify BabelNet concepts by analyzing the group of " + ((sentenceGroup != null) ? sentenceGroup.size() : "NULL") + " sentences - check if your BabelNet API key is valid and has babelcoins available.");
			}

			// Report semantic annotations results
			if(semanticAnnotations != null && semanticAnnotations.size() > 0) {
				for(SemanticAnnotation semanticAnnotation : semanticAnnotations) {
					if(semanticAnnotation != null) {
						try {
							TokenOffsetFragment tfo = semanticAnnotation.getTokenOffsetFragment();
							if(tfo != null && 
									tfo.getStart() >= 0 && tfo.getStart() < babelfyTokenListToDisambiguate.size() &&
									tfo.getEnd() >= 0 && tfo.getEnd() < babelfyTokenListToDisambiguate.size() ) {

								// Retrieve from and to token IDs
								Integer fromTokenID = tokenListIDtoTokenDocIDmap.get(tfo.getStart());
								Integer toTokenID = tokenListIDtoTokenDocIDmap.get(tfo.getEnd());

								if(fromTokenID != null && toTokenID != null) {
									Annotation fromTokenAnn = this.document.getAnnotations(tokenAnnotationSetToAnalyze).get(fromTokenID);
									Annotation toTokenAnn = this.document.getAnnotations(tokenAnnotationSetToAnalyze).get(toTokenID);
									if(fromTokenAnn != null && toTokenAnn != null &&
											fromTokenAnn.getStartNode().getOffset() < toTokenAnn.getEndNode().getOffset()) {
										// Create annotation
										FeatureMap fm = gate.Factory.newFeatureMap();

										if(semanticAnnotation.getBabelNetURL() != null) {
											fm.put(ImporterBase.babelnet_DisItem_babelnetURLfeat, semanticAnnotation.getBabelNetURL());
										}
										if(semanticAnnotation.getBabelSynsetID() != null) {
											fm.put(ImporterBase.babelnet_DisItem_synsetIDfeat, semanticAnnotation.getBabelSynsetID());
										}
										if(semanticAnnotation.getDBpediaURL() != null) {
											fm.put(ImporterBase.babelnet_DisItem_dbpediaURLfeat, semanticAnnotation.getDBpediaURL());
										}

										try {
											fm.put(ImporterBase.babelnet_DisItem_scoreFeat, new Double(semanticAnnotation.getScore()));
										}
										catch (Exception e) {
											Util.notifyException("Populating BabelNet annotation with feature", e, logger);
										}

										try {
											fm.put(ImporterBase.babelnet_DisItem_coherenceScoreFeat, new Double(semanticAnnotation.getCoherenceScore()));
										}
										catch (Exception e) {
											Util.notifyException("Populating BabelNet annotation with feature", e, logger);
										}

										try {
											fm.put(ImporterBase.babelnet_DisItem_golbalScoreFeat, new Double(semanticAnnotation.getGlobalScore()));
										}
										catch (Exception e) {
											Util.notifyException("Populating BabelNet annotation with feature", e, logger);
										}

										if(semanticAnnotation.getSource() != null) {
											fm.put(ImporterBase.babelnet_DisItem_sourceFeat, semanticAnnotation.getSource().toString());
										}

										fm.put(ImporterBase.babelnet_DisItem_numTokensFeat, new Integer(tfo.getEnd() - tfo.getStart() + 1));

										this.document.getAnnotations(ImporterBase.babelnet_AnnSet).add(fromTokenAnn.getStartNode().getOffset(), toTokenAnn.getEndNode().getOffset(),
												ImporterBase.babelnet_DisItem, fm);
										babelnetSynsetCounter++;
									}
								}
							}
						}
						catch (Exception e) {
							Util.notifyException("Creating BabelNet annotation", e, logger);
						}
					}
				}
			}

		}

		
		logger.info("Spotted " + babelnetSynsetCounter + " BabelNet concepts inside the document.");

	}

	public static void main(String[] args) {

		// Initialize and execute parser
		try {
			Gate.init();


		} catch(GateException ge) {
			logger.error("ERROR (GateException): while executing parser " + ge.getMessage());
			ge.printStackTrace();
		}
	}
	
	/**
	 * Determine the majority language from the list of sentence annotations
	 * and parse all the sentence annotations by means of the parser for that language.
	 * 
	 * If the list of sentence annotations is null or empty all the annotation of the sentence type specified are parsed.
	 * 
	 * @param isLangAware
	 * @param doc
	 * @param sentenceAnnList
	 * @param sentAnnSet
	 * @param sentAnnType
	 * @param tokenAnnSet
	 * @param tokenAnnType
	 */
	public static void languageAwareDisambiguation(boolean isLangAware, BabelnetAnnotator babelAnn, Document doc, List<Annotation> sentenceAnnList,
			String sentAnnSet, String sentAnnType, String tokenAnnSet, String tokenAnnType) {
		
		String selectedMajorityLang = "";
		if(sentenceAnnList != null && sentenceAnnList.size() > 0) {
			selectedMajorityLang = LanguageDetector.getMajorityLanguage(sentenceAnnList, ImporterBase.langAnnFeat);
		}
		
		// Select parser on the basis of the majority language
		String disambiguationLanguage = "english";
		if(isLangAware) {
			disambiguationLanguage = (selectedMajorityLang != null && selectedMajorityLang.toLowerCase().equals("es")) ? "spanish" : "english";
		}
		else if(selectedMajorityLang != null && selectedMajorityLang.length() > 0 && !selectedMajorityLang.toLowerCase().equals("en")) {
			disambiguationLanguage = "english";
			logger.warn("You're parsing a text by means of the English parser, even if the language tag of its text is " + selectedMajorityLang);
		}
		
		
		// Store original setting of parser
		String original_sentenceAnnotationSet = new String(babelAnn.getSentenceAnnotationSetToAnalyze());
		String original_sentenceAnnotationType = new String(babelAnn.getSentenceAnnotationTypeToAnalyze());
		String original_tokenAnnotationSet = new String(babelAnn.getTokenAnnotationSetToAnalyze());
		String original_tokenAnnotationType = new String(babelAnn.getTokenAnnotationTypeToAnalyze());
		String original_disambiguationLanguage = new String(babelAnn.getBabelnetLanguage());
		
		// Set sentence and token annotation types
		babelAnn.setSentenceAnnotationSetToAnalyze(sentAnnSet);
		babelAnn.setSentenceAnnotationTypeToAnalyze(sentAnnType);
		babelAnn.setTokenAnnotationSetToAnalyze(tokenAnnSet);
		babelAnn.setTokenAnnotationTypeToAnalyze(tokenAnnType);
		babelAnn.setBabelnetLanguage(disambiguationLanguage);
		
		// Eventually limit the parsing only to a set of sentence IDs
		if(sentenceAnnList != null && sentenceAnnList.size() > 0) {
			List<Integer> sentenceAnnIDs = GateUtil.fromAnnListToAnnIDlist(sentenceAnnList);
			babelAnn.setSentenceIdsToAnalyze(Util.fromListIntToSetString(sentenceAnnIDs));
		}
		else {
			babelAnn.setSentenceIdsToAnalyze(null);
		}
		
		babelAnn.setDocument(doc);
		
		// Execute parsing
		babelAnn.execute();
		
		// Reset parser parameters
		babelAnn.setDocument(null);
		babelAnn.setSentenceIdsToAnalyze(null);
		
		babelAnn.setSentenceAnnotationSetToAnalyze(original_sentenceAnnotationSet);
		babelAnn.setSentenceAnnotationTypeToAnalyze(original_sentenceAnnotationType);
		babelAnn.setTokenAnnotationSetToAnalyze(original_tokenAnnotationSet);
		babelAnn.setTokenAnnotationTypeToAnalyze(original_tokenAnnotationType);
		babelAnn.setBabelnetLanguage(original_disambiguationLanguage);
		
	}
	
	public static void languageAwareDisambiguation(boolean isLangAware, BabelnetAnnotator babelAnn, Document doc, Annotation sentenceAnn,
			String sentAnnSet, String sentAnnType, String tokenAnnSet, String tokenAnnType) {
		
		List<Annotation> sentAnnList = new ArrayList<Annotation>();
		sentAnnList.add(sentenceAnn);
		languageAwareDisambiguation( isLangAware, babelAnn, doc, sentAnnList, sentAnnSet, sentAnnType, tokenAnnSet, tokenAnnType);
		
	}

	@Override
	public boolean resetAnnotations() {

		if(!this.annotationReset) {
			// Remove all annotation from the BabelNet annotaiton set
			Set<String> babelnetAnnotationTypes = this.document.getAnnotations(ImporterBase.babelnet_AnnSet).getAllTypes();

			Set<Integer> annotationIDsToRemove = new HashSet<Integer>();
			for(String babelnetAnnotationType : babelnetAnnotationTypes) {
				if(babelnetAnnotationType != null) {
					List<Annotation> annotationsToRemove = GateUtil.getAnnInDocOrder(this.document, ImporterBase.babelnet_AnnSet, babelnetAnnotationType);
					for(Annotation annToRem : annotationsToRemove) {
						if(annToRem != null && annToRem.getId() != null) {
							annotationIDsToRemove.add(annToRem.getId());
						}
					}
				}
			}

			for(Integer annIDtoRem : annotationIDsToRemove) {
				if(annIDtoRem != null) {
					Annotation annToRem = this.document.getAnnotations(ImporterBase.babelnet_AnnSet).get(annIDtoRem);
					if(annToRem != null) {
						this.document.getAnnotations(ImporterBase.babelnet_AnnSet).remove(annToRem);
					}
				}
			}

			this.annotationReset = true;
		}

		return true;
	}

}
