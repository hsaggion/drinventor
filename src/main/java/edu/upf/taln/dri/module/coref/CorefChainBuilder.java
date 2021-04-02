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
package edu.upf.taln.dri.module.coref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.coref.sieve.AppositionSieve;
import edu.upf.taln.dri.module.coref.sieve.ExactMatchSieve;
import edu.upf.taln.dri.module.coref.sieve.PredicateNominativeSieve;
import edu.upf.taln.dri.module.coref.sieve.PronounSieve;
import edu.upf.taln.dri.module.coref.sieve.RelativePronounSieve;
import edu.upf.taln.dri.module.coref.sieve.dicts.DictCollections;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.parser.MateParser;
import gate.Annotation;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

/**
 * Spot mention of entities and build coreference chains
 * 
 */
@CreoleResource(name = "DRI Modules - Coreference Chain Builder")
public class CorefChainBuilder extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(CorefChainBuilder.class);

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static Integer ruleId = 1;
	private static Map<SieveTypeEnum, Integer> matchBySieveType = new HashMap<SieveTypeEnum, Integer>();

	public List<String> corefChainCreationSteps;

	// Input and output annotation
	private String outputCorefAS = "Coreference";

	// Internal variables to support the creation of coreference chains
	private static List<List<Annotation>> sentenceOrderedCorefMap = new ArrayList<List<Annotation>>();
	private static Map<Integer, Set<Integer>> corefChainMap = new HashMap<Integer, Set<Integer>>();
	public String getOutputCorefAS() {
		return outputCorefAS;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Coreference", comment = "The name of the output annotation set to store coreference chains.")
	public void setOutputCorefAS(String outputCorefAS) {
		this.outputCorefAS = outputCorefAS;
	}


	@Override
	public Resource init() {

		return this;
	}


	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		this.corefChainCreationSteps = new ArrayList<String>();

		DictCollections.initDictionaries();

		// Check input parameters
		this.outputCorefAS = StringUtils.isNotBlank(this.outputCorefAS) ? this.outputCorefAS : ImporterBase.coref_ChainAnnSet;

		long startFilter = System.currentTimeMillis();

		// CANDIDATE MENTIONS FILTER 1: use specific heuristics to remove candidate mentions
		List<Annotation> prunedCorefMentionList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		logger.debug("Initial candidate coreference number: " + prunedCorefMentionList.size());

		Set<Annotation> corefsToDel = new HashSet<Annotation>();
		List<String> blankCoref_DEB = new ArrayList<String>();
		List<String> pronominalShort_DEB = new ArrayList<String>();
		List<String> notPronominalShortLowercased_DEB = new ArrayList<String>();
		List<String> percentMoneyParititvePleonastic_DEB = new ArrayList<String>();
		List<String> inCit_DEB = new ArrayList<String>();
		for(Annotation  corefMention : prunedCorefMentionList) {
			String corefMentionText = GateUtil.getAnnotationText(corefMention, this.document).orElse(null);
			String corefMentionType = GateUtil.getStringFeature(corefMention, "mentionType").orElse(null);

			if(StringUtils.isBlank(corefMentionText)) {
				corefsToDel.add(corefMention);
				blankCoref_DEB.add(GateUtil.getAnnotationText(corefMention, this.document).orElse("_UNDEFINED_TEXT_"));
				continue;
			}
			else if(Util.strCompareCI(corefMentionType, "PRONOMINAL") && corefMentionText.length() <= 1) {
				corefsToDel.add(corefMention);
				pronominalShort_DEB.add(GateUtil.getAnnotationText(corefMention, this.document).orElse("_UNDEFINED_TEXT_"));
				continue;
			}
			else if(!Util.strCompareCI(corefMentionType, "PRONOMINAL") && corefMentionText.length() == 2 &&
					(Character.isLowerCase(corefMentionText.charAt(0)) && Character.isLowerCase(corefMentionText.charAt(1)))) {
				corefsToDel.add(corefMention);
				notPronominalShortLowercased_DEB.add(GateUtil.getAnnotationText(corefMention, this.document).orElse("_UNDEFINED_TEXT_"));
				continue;
			}

			// Remove co-reference mentions with a specific set of features
			String percent = GateUtil.getStringFeature(corefMention, "percent").orElse(null);
			String money = GateUtil.getStringFeature(corefMention, "money").orElse(null);
			String demonym = GateUtil.getStringFeature(corefMention, "demonym").orElse(null);
			String partitive = GateUtil.getStringFeature(corefMention, "partitive").orElse(null);
			String pleonastic = GateUtil.getStringFeature(corefMention, "pleonastic").orElse(null);
			String nonword = GateUtil.getStringFeature(corefMention, "nonword").orElse(null);
			if(Util.strCompareCI(percent, "true") ||
					Util.strCompareCI(money, "true") ||
					Util.strCompareCI(demonym, "true") ||
					Util.strCompareCI(partitive, "true") ||
					Util.strCompareCI(pleonastic, "true") ||
					Util.strCompareCI(nonword, "true") ) {
				corefsToDel.add(corefMention);
				percentMoneyParititvePleonastic_DEB.add(GateUtil.getAnnotationText(corefMention, this.document).orElse("_UNDEFINED_TEXT_"));
				continue;
			}


			// Remove a candidate co-reference is equal or included in an in-line citation
			List<Annotation> ilineCitationList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType);
			for(Annotation ilineCitation : ilineCitationList) {
				if(ilineCitation != null && GateUtil.containedIn(ilineCitation, corefMention)) {
					corefsToDel.add(corefMention);
					inCit_DEB.add(GateUtil.getAnnotationText(corefMention, this.document).orElse("_UNDEFINED_TEXT_"));
					break;
				}
			}

		}
		for(Annotation corefToDel : corefsToDel) {
			this.document.getAnnotations(ImporterBase.driAnnSet).remove(corefToDel);
		}

		logger.debug("FILTER 1 results:");
		logger.debug("Candidate removed because blankCoref:" + blankCoref_DEB.size() + " ( " + blankCoref_DEB + " )");
		logger.debug("Candidate removed because pronominalShort:" + pronominalShort_DEB.size() + " ( " + pronominalShort_DEB + " )");
		logger.debug("Candidate removed because notPronominalShortLowercased:" + notPronominalShortLowercased_DEB.size() + " ( " + notPronominalShortLowercased_DEB + " )");
		logger.debug("Candidate removed because percentMoneyParititvePleonastic:" + percentMoneyParititvePleonastic_DEB.size() + " ( " + percentMoneyParititvePleonastic_DEB + " )");
		logger.debug("Candidate removed because inCit:" + inCit_DEB.size() + " ( " + inCit_DEB + " )");


		// CANDIDATE MENTIONS FILTER 2: remove a candidate mention if a larger one with the same head word exists
		List<Annotation> corefMentionList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		logger.debug("Candidate coreference number after FILTER 1 / before FILTER 2: " + corefMentionList.size());

		Map<Integer, Annotation> headIdLongestMentionMap = new HashMap<Integer, Annotation>();
		corefsToDel = new HashSet<Annotation>();
		Integer coreferenceMentionsWithSameHeadID_DEB = 0;
		for(Annotation corefMention : corefMentionList) {
			Optional<Integer> headId = GateUtil.getIntegerFeature(corefMention, "headID");
			if(headId.isPresent()) {
				if(headIdLongestMentionMap.containsKey(headId) && !corefMention.getId().equals(headIdLongestMentionMap.get(headId).getId())) {
					Optional<String> newString = GateUtil.getAnnotationText(corefMention, this.document);
					Optional<String> currentString = GateUtil.getAnnotationText(headIdLongestMentionMap.get(headId), this.document);
					if(newString.isPresent() && currentString.isPresent()) {
						Annotation longersCoref = (newString.get().length() > currentString.get().length()) ? corefMention : headIdLongestMentionMap.get(headId);
						corefsToDel.add((newString.get().length() > currentString.get().length()) ? headIdLongestMentionMap.get(headId) : corefMention );
						headIdLongestMentionMap.put(headId.get(), longersCoref);
						coreferenceMentionsWithSameHeadID_DEB++;
						logger.debug("Analyzing the candidate coreferences (with same head ID)\n - '" + newString.get() + "'\n - '" + currentString.get() + "'");
						logger.debug("The longer one is: '" + GateUtil.getAnnotationText(longersCoref, this.document).orElse("_UNDEFINED_TEXT_") + "'");
					}
				}
				else {
					headIdLongestMentionMap.put(headId.get(), corefMention);
				}
			}
		}
		for(Annotation corefToDel : corefsToDel) {
			this.document.getAnnotations(ImporterBase.driAnnSet).remove(corefToDel);
		}
		logger.debug("Candidate removed because with the same head id of a longer one " + coreferenceMentionsWithSameHeadID_DEB);

		corefMentionList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		logger.debug("Candidate coreference number after FILTER 2: " + corefMentionList.size());

		logger.debug("Mention filters executed in " + (System.currentTimeMillis() - startFilter) + " milliseconds.");

		// Adding Appo, PredNom and RelPro lists
		logger.debug("Adding Appo, PredNom and RelPro candidate mentions...");
		long startAddAppoPredNomRelPro = System.currentTimeMillis();
		addListOfAppoPredNomRelPro();
		logger.debug("Appo, PredNom and RelPro lists added in " + (System.currentTimeMillis() - startAddAppoPredNomRelPro) + " milliseconds.");

		// Check if the candidate coreference includes a comma or a W-starting POS token; if so, remove
		// from the candidate coreference all the tokens starting from the comma or a W-starting POS token up to the last token
		logger.debug("Removing appositions and W-starting POS element from candidate mentions...");
		long startRemoveAppoAndWPOSstartElem = System.currentTimeMillis();
		removeAppositionsAndWPOSstartTokens();
		logger.debug("Removed appositions and W-starting POS element from candidate mentions in " + (System.currentTimeMillis() - startRemoveAppoAndWPOSstartElem) + " milliseconds.");


		logger.debug("*******************************************************");
		logger.debug("************* PRE-PROCESSING FINISHED *****************");
		logger.debug("*******************************************************\n\n");

		// ****************************************************************
		// ******************* BUILD COREFERENCE CHAINS *******************
		// ****************************************************************

		long startSieveExection = System.currentTimeMillis();

		// Initialization of variables
		sentenceOrderedCorefMap = new ArrayList<List<Annotation>>(); // A list of candidate coreference sentences each one including a list of candidate coreferences
		corefChainMap = new HashMap<Integer, Set<Integer>>();

		// Populate corefChainMap - key the ID of each candidate coreference and value the set of candidate coreferences IDs
		List<Annotation> corefAnnotationsList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		for(Annotation corefAnnotation : corefAnnotationsList) {
			if(corefAnnotation != null) {
				Set<Integer> corefChain = new HashSet<Integer>();
				corefChain.add(corefAnnotation.getId());
				corefChainMap.put(corefAnnotation.getId(), corefChain);
			}
		}
		logger.debug("We have created an initial set of " + corefChainMap.size() + " singleton goreference chains.");

		// Populate the ordered list of sentences, each one with its ordered list of candidate mentions
		// The order of mentions inside a sentence is the reversed occurrence order
		logger.debug("-----------------------------------------------------------------------------------------------");
		logger.debug("-----------------------------------------------------------------------------------------------");
		logger.debug("Populating the list of sentences (document order) and for each sentence the list of candidate mentions (depth-first visit of dependency tree):");
		List<Annotation> orderedSentenceList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		for(Annotation sentence : orderedSentenceList) {
			if(sentence != null) {
				List<Annotation> corefInSentence = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate, sentence);
				if(!CollectionUtils.isEmpty(corefInSentence)) {

					// Order mentions by tree-traversal order - START
					List<Integer> treeNodesIDordered = new ArrayList<Integer>();

					List<Annotation> tokensOfSentence = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sentence);

					// Get root token
					List<Annotation> rootTokenAnnList = new ArrayList<Annotation>();
					for(Annotation tokenOfSentence : tokensOfSentence) {
						String depRel = GateUtil.getStringFeature(tokenOfSentence, MateParser.depKindFeat).orElse(null);
						Integer targetId = GateUtil.getIntegerFeature(tokenOfSentence, MateParser.depTargetIdFeat).orElse(null);

						if(depRel != null && depRel.equals("ROOT") && targetId == null) {
							rootTokenAnnList.add(tokenOfSentence);
						}
					}

					if(rootTokenAnnList.size() != 1) {
						logger.warn("Impossible to define the root token of sentence: " + GateUtil.getAnnotationText(sentence, this.document).orElse("_NO_SENTENCE_TEXT_"));
					}
					else {
						// Once we found the root node 
						Annotation rootNodeAnn = rootTokenAnnList.get(0);
						
						populateTree(rootNodeAnn, treeNodesIDordered, tokensOfSentence);
					}
					// Order mentions by tree-traversal order - END

					if(treeNodesIDordered.size() > 0) {

						// Order the mentions with respect to their occurrences in the treeNodesIDordered
						List<Annotation> newCorefInSentence = new ArrayList<Annotation>();

						Map<Integer, List<Annotation>> positionListCorefAnnMap = new TreeMap<Integer, List<Annotation>>();
						for(Annotation sentCoref : corefInSentence) {
							Integer headId = GateUtil.getIntegerFeature(sentCoref, "headID").orElse(null);
							if(headId != null) {
								Integer orderNumber = null;
								for(int p = 0; p < treeNodesIDordered.size(); p++) {
									if(treeNodesIDordered.get(p).equals(headId)) {
										orderNumber = p;
										break;
									}
								}

								if(orderNumber != null) {
									if(positionListCorefAnnMap.containsKey(orderNumber)) {
										List<Annotation> annotationsAtThatOrderNumber = positionListCorefAnnMap.get(orderNumber);
										annotationsAtThatOrderNumber.add(sentCoref);
									}
									else {
										List<Annotation> annotationsAtThatOrderNumber = new ArrayList<Annotation>();
										annotationsAtThatOrderNumber.add(sentCoref);
										positionListCorefAnnMap.put(orderNumber, annotationsAtThatOrderNumber);
									}
								}
								else {
									logger.warn("No order number defined for candidate coreference mention: " + GateUtil.getAnnotationText(sentCoref, this.document));
								}
							}
							else {
								logger.warn("No headID defined for candidate coreference mention: " + GateUtil.getAnnotationText(sentCoref, this.document));
							}
						}

						for(Entry<Integer, List<Annotation>> elem : positionListCorefAnnMap.entrySet()) {
							if(elem != null && elem.getKey() != null && elem.getValue() != null) {
								List<Annotation> annList = elem.getValue();
								if(annList == null || annList.size() == 0) {
									logger.warn("Incorrect coreference mention sentence ordering");
								}
								else {
									if(annList.size() == 1) {
										newCorefInSentence.add(annList.get(0));
									}
									else {
										logger.warn("There are two mentions that have the same head token");
										// There are two mentions that have the same head token in position elem.getKey()
										// Sort them by string length
										Map<Integer, Annotation> lenghtCorefAnnMap = new TreeMap<Integer, Annotation>();
										for(Annotation ann : annList) {
											String annString = GateUtil.getAnnotationText(ann, this.document).orElse(null);
											if(ann != null && annString != null && annString.length() > 0) {
												lenghtCorefAnnMap.put(annString.length(), ann);
											}
										}

										// Order from shorter to longer
										for(Entry<Integer, Annotation> lenghtCorefAnn : lenghtCorefAnnMap.entrySet()) {
											if(lenghtCorefAnn != null && lenghtCorefAnn.getKey() != null && lenghtCorefAnn.getValue() != null) {
												newCorefInSentence.add(lenghtCorefAnn.getValue());
											}
										}
									}
								}
							}
						}

						sentenceOrderedCorefMap.add(newCorefInSentence);
					}
					else {
						logger.warn("Impossible to define tree ordering of coreference mention, use reverse occurrence order of mentions in the document");
						Collections.reverse(corefInSentence);

						sentenceOrderedCorefMap.add(corefInSentence);
					}
				}

				// Print ordered candidate co-reference stats
				List<String> candidateCorefString = new ArrayList<String>();
				for(Annotation candidateCoref : corefInSentence) {
					if(candidateCoref != null) {
						candidateCorefString.add(GateUtil.getAnnotationText(candidateCoref, this.document).orElse("_NO_CANDIDATE_COREF_TEXT_"));
					} 
				}
				logger.debug("Sentence: '" + GateUtil.getAnnotationText(sentence, this.document).orElse("_NULL_SENTENCE_") + "'");
				logger.debug(" with coreference list: ");
				for(String candidateCorefStr : candidateCorefString) {
					logger.debug("       - " + candidateCorefStr);
				}
			}
		}
		logger.debug("-----------------------------------------------------------------------------------------------");
		logger.debug("-----------------------------------------------------------------------------------------------");

		// NB: the apposition list (COREFm annotation IDs) of each mention is contained in the feature "appositions" of the same mentions
		// NB: the predicate nominative list (COREFm annotation IDs) of each mention is contained in the feature "predicateNominatives" of the same mentions
		// NB: the relative pronoun list (COREFm annotation IDs) of each mention is contained in the feature "relativePronouns" of the same mentions

		printCoreferenceByType();

		logger.debug("-----------------------------------------------------------------------------------------------");
		logger.debug("------------------------------ START SIEVE APPLICATION ----------------------------------------");

		// Apply sieves
		ExactMatchSieve exactMatch = new ExactMatchSieve();
		long startExactMatchSieve = System.currentTimeMillis();
		exactMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying EXACT_MATCH in " + (System.currentTimeMillis() - startExactMatchSieve) + " ms:");
		printNumChains();

		/*
		RelaxedMatchSieve relaxedMatch = new RelaxedMatchSieve();
		long startRelaxedMatchSieve = System.currentTimeMillis();
		relaxedMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying RELAXED_MATCH in " + (System.currentTimeMillis() - startRelaxedMatchSieve) + " ms:");
		printNumChains();
		 */

		PronounSieve personalPronounMatch = new PronounSieve();
		long startPronounSieve = System.currentTimeMillis();
		personalPronounMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying PRONOMINAL_MATCH in " + (System.currentTimeMillis() - startPronounSieve) + " ms:");
		printNumChains();

		AppositionSieve appositionMatch = new AppositionSieve();
		long startAppositionSieve = System.currentTimeMillis();
		appositionMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying APPOSITION in " + (System.currentTimeMillis() - startAppositionSieve) + " ms:");
		printNumChains();

		PredicateNominativeSieve predicateNominativeMatch = new PredicateNominativeSieve();
		long startPredicateNominativeSieve = System.currentTimeMillis();
		predicateNominativeMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying PREDICATE_NOMINATIVE in " + (System.currentTimeMillis() - startPredicateNominativeSieve) + " ms:");
		printNumChains();

		RelativePronounSieve relativePronounMatch = new RelativePronounSieve();
		long startRelativePronounSieve = System.currentTimeMillis();
		relativePronounMatch.applySieve(this);
		logger.info("Number of corefernece chains after applying RELATIVE_PRONOUN in " + (System.currentTimeMillis() - startRelativePronounSieve) + " ms:");
		printNumChains();


		logger.debug("------------------------------ SIEVE APPLICATION ENDED ----------------------------------------");
		logger.debug("-----------------------------------------------------------------------------------------------");

		logger.debug("EXEC: coreference chains created in " + (System.currentTimeMillis() - startSieveExection) + " milliseconds.");

		
		logger.debug("\n\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.debug("\n++++++++++   CUSTOM OREF CHAINS   ++++++++++++++++++++++++");
		// Create "we" coreference chain
		// by removing all the "we" / "our" pronominal candidate coreference mention from every coreference chain and create a new coreference chain with these mentions 
		List<Annotation> corefMentions_WEchain = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		
		Set<Integer> setOfIDsOfWeCorefChain = new HashSet<Integer>();
		for(Annotation candidateCore : corefMentions_WEchain) {
			if(candidateCore != null) {
				try {
					String text = GateUtil.getAnnotationText(candidateCore, this.document).orElse(null);
					if( text != null && 
						( Util.strCompare(text.trim(), "we") || Util.strCompare(text.trim(), "our") || Util.strCompare(text.trim(), "We") || Util.strCompare(text.trim(), "Our") ) ) {
						
						// Add the new IDs of coreference chain element to the ID set
						setOfIDsOfWeCorefChain.add(candidateCore.getId());
						
						// Remove these mentions from all coreference chains
						for(Entry<Integer, Set<Integer>> corefChainMapEntry : corefChainMap.entrySet()) {
							if(corefChainMapEntry != null && corefChainMapEntry.getKey() != null && corefChainMapEntry.getValue() != null) {
								Set<Integer> intToRemoveFromCorefChain = new HashSet<Integer>();
								if(!corefChainMapEntry.getValue().equals(setOfIDsOfWeCorefChain)) {
									for(Integer corefChainElem : corefChainMapEntry.getValue()) {
										if(corefChainElem != null && corefChainElem.equals(candidateCore.getId())) {
											intToRemoveFromCorefChain.add(corefChainElem);
										}
									}
								}
								
								for(Integer intToRemove : intToRemoveFromCorefChain) {
									corefChainMapEntry.getValue().remove(intToRemove);
								}
							}
						} 
						
						// Set the new coreference chain of the mention uner analysis (candidateCore mention) equal to the "we" coreference chain
						if(corefChainMap.containsKey(candidateCore.getId())) {
							corefChainMap.put(candidateCore.getId(), setOfIDsOfWeCorefChain);
						}
						
					}
				}
				catch (Exception e) {
					// Do nothing
				}
			}
		}
		logger.debug("Added " + setOfIDsOfWeCorefChain.size() + " elements to the 'we' coreference chain.");


		logger.debug("\n\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.debug("\n++++++++++   RESULTS   +++++++++++++++++++++++++++++++++++");
		// Create coreference cluster annotations
		this.getDocument().removeAnnotationSet(this.outputCorefAS);

		Collection<Set<Integer>> corefChainsIdSets = corefChainMap.values();
		Integer corefChainId = -1;
		Set<Integer> consideredMentions = new HashSet<Integer>();
		for(Set<Integer> corefChainIdSet : corefChainsIdSets) {
			if(corefChainIdSet != null && corefChainIdSet.size() > 1) {
				// Check if this co-reference chain has not already been considered - one of its mentions belongs to the consideredMentions set
				if(consideredMentions.contains(corefChainIdSet.iterator().next())) {
					continue;
				}

				corefChainId++;

				logger.debug("***** NEW CHAIN WITH ID " + corefChainId + " and " + corefChainIdSet.size() + " elements.");

				String corefName = "";

				// Convert the set of candidate co-reference ids of the chain to a list of annotation ids and than a sorted list of annotations
				List<Integer> corefChainIdList = new ArrayList<Integer>();
				corefChainIdList.addAll(corefChainIdSet);

				List<Annotation> corefChainAnnList = new ArrayList<Annotation>();
				for(Integer corefChainIdElem : corefChainIdList) {
					Annotation ann = this.document.getAnnotations(ImporterBase.driAnnSet).get(corefChainIdElem);
					if(ann != null) {
						corefChainAnnList.add(ann);
						consideredMentions.add(ann.getId());
					}
				}
				Collections.sort(corefChainAnnList, new OffsetComparator());

				// For each element / candidate coreference annotation of the coreference chain retrieve the rule that matched it
				// The ruleMatchCount is populated with key as rule type and value as number of matches of that rule
				Map<String, Integer> ruleMatchCount = new HashMap<String, Integer>();
				for(Annotation corefListElem : corefChainAnnList) {
					if(corefListElem != null) {
						if(corefListElem.getFeatures() != null) {
							for(Entry<Object, Object> fmEntry : corefListElem.getFeatures().entrySet()) {
								if(fmEntry != null && fmEntry.getKey() != null && fmEntry.getValue() != null) {
									String keyStr = (String) fmEntry.getKey();
									if(keyStr.startsWith("RULE_")) {
										String ruleValue = (String) fmEntry.getValue();
										String corefMatchType = "";
										// EXACT_MATCH, RELAXED_MATCH, APPOSITION, PREDICATE_NOMINATIVE, RELATIVE_PRONOUN, PRONOMINAL_MATCH
										if(ruleValue.startsWith("EXACT_MATCH")) {
											corefMatchType = "E";
										}
										else if(ruleValue.startsWith("RELAXED_MATCH")) {
											corefMatchType = "R";
										}
										else if(ruleValue.startsWith("APPOSITION")) {
											corefMatchType = "A";
										}
										else if(ruleValue.startsWith("PREDICATE_NOMINATIVE")) {
											corefMatchType = "N";
										}
										else if(ruleValue.startsWith("RELATIVE_PRONOUN")) {
											corefMatchType = "L";
										}
										else if(ruleValue.startsWith("PRONOMINAL_MATCH")) {
											corefMatchType = "P";
										}

										if(ruleMatchCount.containsKey(corefMatchType)) {
											ruleMatchCount.put(corefMatchType, ruleMatchCount.get(corefMatchType) + 1);
										}
										else {
											ruleMatchCount.put(corefMatchType, 1);
										}

										logger.debug("CHAIN ID " + corefChainId + ") " + GateUtil.getAnnotationText(corefListElem, this.document).orElse("__NO_COREF_TEXT__") + " (id: " + corefListElem.getId() + ") MATCHED WITH RUEL: " + ruleValue);
									}
								}
							}

						}
					}
				}

				String corefMatch = "";
				for(Entry<String, Integer> ruleMatchCountEntry : ruleMatchCount.entrySet()) {
					if(!ruleMatchCountEntry.getKey().equals("")) {
						corefMatch += ruleMatchCountEntry.getKey() + ruleMatchCountEntry.getValue();
					}
				}

				// Add Chain... annotations with size, match source and first element name
				for(Annotation corefListElem : corefChainAnnList) {
					if(corefListElem != null) {
						try {
							// The corefName is the name of the first element of the coreference set
							if(corefName.equals("")) {
								corefName = GateUtil.getAnnotationText(corefListElem, this.document).orElse("");
								corefName = corefName.replace(" ", "_");
								corefName = corefName.replace(",", "_");
								corefName = corefName.replace(":", "_");
								corefName = corefName.replace(";", "_");
								corefName = corefName.replace("\"", "_");
								corefName = corefName.replace("\'", "_");
							}

							if(corefName.equals("")) {
								corefName = "NO_NAME_CHAIN";
							}

							if(corefName.length() > 15) {
								corefName = corefName.substring(0, 14) + "...";
							}

							// Feature filtering
							FeatureMap fm = Factory.newFeatureMap();
							if(corefListElem.getFeatures() != null) {
								if(corefListElem.getFeatures().containsKey("COREFmID")) {
									fm.put(ImporterBase.coref_Candidate + "_ID", corefListElem.getId());
								}
								if(corefListElem.getFeatures().containsKey("headID")) {
									fm.put("headID", corefListElem.getFeatures().get("headID"));
								}
								if(corefListElem.getFeatures().containsKey("headString")) {
									fm.put("headString", corefListElem.getFeatures().get("headString"));
								}

								if(GateUtil.getStringFeature(corefListElem, "mentionType").orElse(null) != null) {
									fm.put("type", GateUtil.getStringFeature(corefListElem, "mentionType").orElse(null));
								}
								if(edu.upf.taln.dri.module.coref.sieve.Util.getGenderDetails(corefListElem, this.document) != null) {
									fm.put("gender", edu.upf.taln.dri.module.coref.sieve.Util.getGenderDetails(corefListElem, this.document).toString());
								}
								if(edu.upf.taln.dri.module.coref.sieve.Util.getNumberDetails(corefListElem, this.document) != null) {
									fm.put("number", edu.upf.taln.dri.module.coref.sieve.Util.getNumberDetails(corefListElem, this.document).toString());
								}
								if(edu.upf.taln.dri.module.coref.sieve.Util.getPersonDetails(corefListElem, this.document) != null) {
									fm.put("person", edu.upf.taln.dri.module.coref.sieve.Util.getPersonDetails(corefListElem, this.document).toString());
								}
								if(edu.upf.taln.dri.module.coref.sieve.Util.getAnimacyDetails(corefListElem, this.document) != null) {
									fm.put("animacy", edu.upf.taln.dri.module.coref.sieve.Util.getAnimacyDetails(corefListElem, this.document).toString());
								}

								for(Entry<Object, Object> fmEntry : corefListElem.getFeatures().entrySet()) {
									if(fmEntry != null && fmEntry.getKey() != null && fmEntry.getValue() != null) {
										String keyStr = (String) fmEntry.getKey();
										if(keyStr.startsWith("RULE_")) {
											fm.put(keyStr, (String) fmEntry.getValue());
										}
									}
								}

							}

							// Add coreferenceChain annotation
							this.getDocument().getAnnotations(this.outputCorefAS).add(corefListElem.getStartNode().getOffset(), corefListElem.getEndNode().getOffset(),
									ImporterBase.coref_Coreference + "_" + corefChainId + "_s" + corefChainIdSet.size() + "_" + corefMatch + "_" + corefName, fm);
						} catch (InvalidOffsetException e) {
							Util.notifyException("Adding element of a coreference chain", e, logger);
						}
					}
				}
			}
		}
		logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		// Print stats
		logger.debug("***************");
		printNumChains();
		logger.debug("Number of mention-antecedent matched by sieve:");
		for(Entry<SieveTypeEnum, Integer> matchBySieveTypeEntry : matchBySieveType.entrySet()) {
			logger.debug("   > Sieve: " + matchBySieveTypeEntry.getKey() + " > matches: " + matchBySieveTypeEntry.getValue());
		}

		// Add as document feature the corefCreationOutput list of String
		this.document.setFeatures((this.document.getFeatures() != null) ? this.document.getFeatures() : Factory.newFeatureMap());
		this.document.getFeatures().put("corefCreationOutput", this.corefChainCreationSteps);

		// Transfer from the ImporterBase.driAnnSet annotation set (Analysis) to the CorefSpot annotation set all the
		// annotations useful to spot co-reference chains, with annotation type starting with 'COREF_' or
		// of type ImporterBase.coref_Candidate (CrefMention) or SPURIOUSCOREFm.
		Set<String> driAnnTypes = this.document.getAnnotations(ImporterBase.driAnnSet).getAllTypes();
		Set<Integer> annotationIdToDel = new HashSet<Integer>();
		for(String driAnnType : driAnnTypes) {
			if(driAnnType != null && driAnnType.startsWith("COREF_") ||
					driAnnType.equals(ImporterBase.coref_Candidate) || driAnnType.equals("SPURIOUSCOREFm")) {
				List<Annotation> gateCorefSpotAnnotations = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, driAnnType);
				for(Annotation gateCorefSpotAnnotation : gateCorefSpotAnnotations) {
					if(gateCorefSpotAnnotation != null) {
						try {
							annotationIdToDel.add(gateCorefSpotAnnotation.getId());
							this.document.getAnnotations(ImporterBase.coref_SpotAnnSet).add(
									gateCorefSpotAnnotation.getStartNode().getOffset(), gateCorefSpotAnnotation.getEndNode().getOffset(),
									gateCorefSpotAnnotation.getType(), gateCorefSpotAnnotation.getFeatures());
						} catch (InvalidOffsetException e) {
							/* Do nothing */
						}
					}
				}
			}
		}
		for(Integer annToDel : annotationIdToDel) {
			if(annToDel != null && this.document.getAnnotations(ImporterBase.driAnnSet).get(annToDel) != null) {
				this.document.getAnnotations(ImporterBase.driAnnSet).remove(this.document.getAnnotations(ImporterBase.driAnnSet).get(annToDel));
			}
		}
	}

	/**
	 * Print co-references by type
	 */
	private void printCoreferenceByType() {
		logger.debug("*******************************************");
		logger.debug("***** CANDIDATE COREFERENCE BY TYPE *******");
		// Number of mentions by type count
		Map<String, Integer> mentionTypeCountMap = new HashMap<String, Integer>();
		List<Annotation> corefAnnotationsList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);
		for(Annotation corefAnnotation : corefAnnotationsList) {
			String mentionTypeCoref = GateUtil.getStringFeature(corefAnnotation, "mentionType").orElse(null);
			if(corefAnnotation != null && mentionTypeCoref != null) {
				if(mentionTypeCountMap.containsKey(mentionTypeCoref)) {
					mentionTypeCountMap.put(mentionTypeCoref, mentionTypeCountMap.get(mentionTypeCoref) + 1);
				}
				else {
					mentionTypeCountMap.put(mentionTypeCoref, 1);
				}
			}
		}
		logger.debug("Candidate mentions number by type: ");
		for(Entry<String, Integer> mentionTypeCountMapENtry : mentionTypeCountMap.entrySet()) {
			logger.debug("     > " + mentionTypeCountMapENtry.getKey() + " : " + mentionTypeCountMapENtry.getValue());
		}

		logger.debug("*******************************************");
		logger.debug("*******************************************\n");
	}

	/**
	 * Print number of chains and number of coreferences that belong or not to a chain
	 */
	private void printNumChains() {

		Integer mentCount = 0;
		Integer mentSingleton = 0;
		Integer mentInCorefChain = 0;
		for(Entry<Integer, Set<Integer>> mentionListMapEntry : corefChainMap.entrySet()) {
			mentCount++;
			if(mentionListMapEntry != null && mentionListMapEntry.getValue() == null) {
				logger.info("ATTENTION! Coreference ref null");
			}
			else if(mentionListMapEntry.getValue().size() > 1) {
				mentInCorefChain++;
			}
			else if(mentionListMapEntry.getValue().size() == 1) {
				mentSingleton++;
			}
		}
		logger.info("Total number of mentions : " + mentCount  + " of which: "
				+ mentInCorefChain + " belong to a coreference chain and " + mentSingleton + " are singleton.");

	}

	// Getters
	public List<List<Annotation>> getSentenceOrderedCorefMap() {
		return sentenceOrderedCorefMap;
	}

	public Map<Integer, Set<Integer>> getCorefChainMap() {
		return corefChainMap;
	}

	/**
	 * Generate a list of token IDs by visitng the dependency parse tree
	 * 
	 * @param rootNodeAnn
	 * @param treeNodesIDordered
	 * @param tokensOfSentence
	 */
	public void populateTree(Annotation rootNodeAnn, List<Integer> treeNodesIDordered, List<Annotation> tokensOfSentence) {
		treeNodesIDordered.add(rootNodeAnn.getId());

		// Get children annotations with SBJ relation first
		for(Annotation tokenAnn : tokensOfSentence) {
			String depRel = GateUtil.getStringFeature(tokenAnn, MateParser.depKindFeat).orElse(null);
			Integer targetId = GateUtil.getIntegerFeature(tokenAnn, MateParser.depTargetIdFeat).orElse(null);

			// Check if the node is a children with SBJ relation and put it
			if(depRel != null && depRel.equals("SBJ") && targetId != null && targetId.equals(rootNodeAnn.getId())) {
				populateTree(tokenAnn, treeNodesIDordered, tokensOfSentence);
			}
		}

		// Get children annotations with relation different from SBJ
		for(Annotation tokenAnn : tokensOfSentence) {
			String depRel = GateUtil.getStringFeature(tokenAnn, MateParser.depKindFeat).orElse(null);
			Integer targetId = GateUtil.getIntegerFeature(tokenAnn, MateParser.depTargetIdFeat).orElse(null);

			// Check if the node is a children with SBJ relation and put it
			if(depRel != null && !depRel.equals("SBJ") && targetId != null && targetId.equals(rootNodeAnn.getId())) {
				populateTree(tokenAnn, treeNodesIDordered, tokensOfSentence);
			}
		}

	}


	// Check if to skip the mention (coreMentionAnn) - search pruning
	public static boolean skipMention(Annotation mentionAnn, SieveTypeEnum sieveType, gate.Document doc) {

		boolean skip = false;

		if(mentionAnn != null) {
			try {

				// Get the ordered list of mentions in the co-reference chain
				Set<Integer> mentionChainIdSet = corefChainMap.get(mentionAnn.getId());

				Set<Annotation> mentionChainAnnSet = new HashSet<Annotation>();
				for(Integer mentionChainIdElem : mentionChainIdSet) {
					Annotation ann = doc.getAnnotations(ImporterBase.driAnnSet).get(mentionChainIdElem);
					if(ann != null) {
						mentionChainAnnSet.add(ann);
					}
				}

				List<Annotation> mentionChainList = new ArrayList<Annotation>();
				mentionChainList.addAll(mentionChainAnnSet);
				Collections.sort(mentionChainList, new OffsetComparator());

				// Skip if not the first mention in the chain
				if(!sieveType.equals(SieveTypeEnum.EXACT_MATCH) && !sieveType.equals(SieveTypeEnum.RELATIVE_PRONOUN) &&
						!sieveType.equals(SieveTypeEnum.PREDICATE_NOMINATIVE) && !sieveType.equals(SieveTypeEnum.APPOSITION) &&
						mentionChainList != null && !mentionChainList.get(0).getId().equals(mentionAnn.getId())
						) {
					skip = true;

					logger.debug("SKIP MENTION: not EXACT_MATCH, RELATIVE_PRONOUN, PREDICATE_NOMINATIVE, APPOSITION and "
							+ "not the first in coreference chain with id " + mentionAnn.getId() +" (chain composed by " + mentionChainList.size() + " mentions)");
					int countElem = 0;
					for(Annotation mentionChainElem : mentionChainList) {
						countElem++;
						logger.debug("     " + countElem + " > " + GateUtil.getAnnotationText(mentionChainElem, doc).orElse("_NO_COREF_MENTION_TEXT_"));
					}
				}



				// Check if start with an indefinite article - unlikely to have an antecedent (e.g. "A commission" was set up to .... )
				List<Integer> appositionsList = GateUtil.getListIntegerFeature(mentionAnn, "appositions").orElse(null);
				List<Integer> predicateNominativesList = GateUtil.getListIntegerFeature(mentionAnn, "predicateNominatives").orElse(null);
				String mentionText = GateUtil.getAnnotationText(mentionAnn, doc).orElse(null);
				if( (appositionsList == null || appositionsList.size() == 0) && (predicateNominativesList == null || predicateNominativesList.size() == 0) &&
						mentionText != null && (mentionText.toLowerCase().startsWith("a ") || mentionText.toLowerCase().startsWith("an ")) && 
						!sieveType.equals(SieveTypeEnum.EXACT_MATCH) && !sieveType.equals(SieveTypeEnum.APPOSITION) && !sieveType.equals(SieveTypeEnum.PRONOMINAL_MATCH)) {
					skip = true;

					logger.debug("SKIP MENTION: is not EXACT MATCH, APPOSITION or PRONOMINAL_MATCH sieve and starts with an indefinite article");
				}

				// Skip if indefinite pronoun
				// Check if start with an indefinite pronoun - unlikely to have an antecedent (e.g. "Some" say that... )
				String pronounType = GateUtil.getStringFeature(mentionAnn, "pronounType").orElse(null);
				if(Util.strCompareCI(pronounType, "INDEFINITE")) {
					skip = true;

					logger.debug("SKIP MENTION: is not EXACT MATCH sieve and starts with an indefinite pronoun (JAPE match - pronounType)");
				}

				// Skip when start with indefinite pronoun / determiner
				String startWithIndefPronoun = GateUtil.getStringFeature(mentionAnn, "startIndPro").orElse(null);
				if(Util.strCompareCI(startWithIndefPronoun, "true") && 
						!sieveType.equals(SieveTypeEnum.EXACT_MATCH) && !sieveType.equals(SieveTypeEnum.APPOSITION) && !sieveType.equals(SieveTypeEnum.PRONOMINAL_MATCH)) {
					skip = true;

					logger.debug("SKIP MENTION: is not EXACT MATCH, APPOSITION or PRONOMINAL_MATCH sieve and starts with an indefinite pronoun (JAPE match - startIndPro)");
				}

				String startWithIndefDeterminer = GateUtil.getStringFeature(mentionAnn, "startIndDet").orElse(null);
				if(Util.strCompareCI(startWithIndefDeterminer, "true") && 
						!sieveType.equals(SieveTypeEnum.EXACT_MATCH) && !sieveType.equals(SieveTypeEnum.APPOSITION) && !sieveType.equals(SieveTypeEnum.PRONOMINAL_MATCH)) {
					skip = true;

					logger.debug("SKIP MENTION: is not EXACT MATCH, APPOSITION or PRONOMINAL_MATCH sieve and starts with an indefinite pronoun (JAPE match - startIndDet)");
				}

				// Additional pronoun list to check if starts with
				// Ref. pronouns list: http://www.k12reader.com/term/indefinite-pronouns/
				if( mentionText != null && 
						(
								mentionText.toLowerCase().startsWith("anybody ") || mentionText.toLowerCase().startsWith("anyone ") ||
								mentionText.toLowerCase().startsWith("anything ") || mentionText.toLowerCase().startsWith("everybody ") ||
								mentionText.toLowerCase().startsWith("everyone ") || mentionText.toLowerCase().startsWith("everything ") ||
								mentionText.toLowerCase().startsWith("some ") || mentionText.toLowerCase().startsWith("nobody ") ||
								mentionText.toLowerCase().startsWith("none ") || mentionText.toLowerCase().startsWith("no one ") ||
								mentionText.toLowerCase().startsWith("nothing ") || mentionText.toLowerCase().startsWith("an ") ||
								mentionText.toLowerCase().startsWith("somebody ") || mentionText.toLowerCase().startsWith("someone ") ||
								mentionText.toLowerCase().startsWith("something ") 
								) && 
								!sieveType.equals(SieveTypeEnum.EXACT_MATCH) && !sieveType.equals(SieveTypeEnum.APPOSITION) && !sieveType.equals(SieveTypeEnum.PRONOMINAL_MATCH)) {
					skip = true;

					logger.debug("SKIP MENTION: is not EXACT MATCH, APPOSITION or PRONOMINAL_MATCH sieve and starts with an indefinite pronoun (string match)");
				}

			}
			catch (Exception e) {
				Util.notifyException("Checking if to skip candidate coreference", e, logger);
			}
		}

		return skip;
	}

	// *********************************************************************************
	// *********************************************************************************
	// ***************************** OTHER METHODS *************************************
	// *********************************************************************************
	// *********************************************************************************

	/**
	 * Get set of most common values in a Value (key) / frequency (value) map
	 * 
	 * @param map
	 * @return
	 */
	private static Set<String> mostCommonValues(Map<String, Integer> map) {
		Set<String> result = new HashSet<String>();

		int highestFreq = -1;
		if(map != null) {
			for(Entry<String, Integer> entry : map.entrySet()) {
				if(entry != null && StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null && 
						(highestFreq == -1 || entry.getValue() >= highestFreq) ) {
					result.add(entry.getKey());
					highestFreq = entry.getValue().intValue();
				}
			}
		}

		return result;
	}

	/**
	 * Generate an ordered map of feature value (key) / occurrence count (decreasing occurrence values)
	 * 
	 * @param annList
	 * @param featName
	 * @return
	 */
	private static Map<String, Integer> sortedFeatureValueCount(Set<Annotation> annList, String featName) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		if(!CollectionUtils.isEmpty(annList) && StringUtils.isNotBlank(featName)) {
			for(Annotation ann : annList) {
				String featValue = GateUtil.getStringFeature(ann, featName).orElse(null);
				if(ann != null && featValue != null) {
					if(result.containsKey(featValue)) {
						result.put(featValue, result.get(featValue) + 1);
					}
					else {
						result.put(featValue, 1);
					}
				}
			}
		}

		return Util.sortByValueDec(result);
	}

	public void mergeTwoCoreferenceChains(Annotation c1, Annotation c2, SieveTypeEnum sieveType) {

		if(c1.getId().equals(c2.getId())) {
			return;
		}

		String corefStr = GateUtil.getAnnotationText(c1, this.document).orElse("_COREF_TEXT_NULL_");
		Integer coreHeadID = GateUtil.getIntegerFeature(c1, "headID").orElse(null);
		String corefHeadStr = null; 
		if(coreHeadID != null) {
			corefHeadStr = GateUtil.getAnnotationText(this.document.getAnnotations(ImporterBase.driAnnSet).get(coreHeadID), this.document).orElse(null);
		}
		String antecStr = GateUtil.getAnnotationText(c2, this.document).orElse("_ANTEC_TEXT_NULL_");
		Integer anteHeadID = GateUtil.getIntegerFeature(c2, "headID").orElse(null);
		String antefHeadStr = null; 
		if(anteHeadID != null) {
			antefHeadStr = GateUtil.getAnnotationText(this.document.getAnnotations(ImporterBase.driAnnSet).get(anteHeadID), this.document).orElse(null);
		}
		String sieveStr = (sieveType != null) ? sieveType.toString() : "_SIEVE_TYPE_NULL_";
		this.corefChainCreationSteps.add("Connected '" + corefStr + "' (head: " + ((corefHeadStr != null) ? corefHeadStr : "_NO_HEAD_") +
				") to the antecedent '" + antecStr + "' (head: " + ((antefHeadStr != null) ? antefHeadStr : "_NO_HEAD_") + ") by the sieve " + sieveStr);

		Set<Integer> set1 = corefChainMap.get(c1.getId());
		Set<Integer> set2 = corefChainMap.get(c2.getId());

		if(set1 != null && set2 != null && set1.equals(set2)) {
			return;
		}

		if(set1 == null && set2 == null) {
			Set<Integer> newCorefSet = new HashSet<Integer>();
			newCorefSet.add(c1.getId());
			newCorefSet.add(c2.getId());
			corefChainMap.put(c1.getId(), newCorefSet);
			corefChainMap.put(c2.getId(), newCorefSet);
		}
		else if(set1 != null && set2 == null) {
			set1.add(c2.getId());
			corefChainMap.put(c2.getId(), set1);
		}
		else if(set1 == null && set2 != null) {
			set2.add(c1.getId());
			corefChainMap.put(c1.getId(), set2);
		}
		else {
			Set<Integer> mergedSet = new HashSet<Integer>();
			for(Integer ann1 : set1) {
				mergedSet.add(ann1);
			}

			for(Integer ann2 : set2) {
				mergedSet.add(ann2);
			}

			for(Integer ann1 : set1) {
				corefChainMap.put(ann1, mergedSet);
			}

			for(Integer ann2 : set2) {
				corefChainMap.put(ann2, mergedSet);
			}
		}

		// Add features to coref mentions to say which rule has joined the mentions c1 and c2
		if(c1.getFeatures() == null) {
			c1.setFeatures(Factory.newFeatureMap());
		}
		c1.getFeatures().put("RULE_" + ++ruleId, sieveType + "__" + c2.getId());

		/*
		if(c2.getFeatures() == null) {
			c2.setFeatures(Factory.newFeatureMap());
		}
		c2.getFeatures().put("RULE_" + ruleId, sieveType + "__" + c1.getId());
		 */

		// Update the number of matches generated by the sieve
		if(matchBySieveType.containsKey(sieveType)) {
			Integer sieveMatchCount = matchBySieveType.get(sieveType);
			sieveMatchCount = sieveMatchCount + 1;
			matchBySieveType.put(sieveType, sieveMatchCount);
		}
		else {
			matchBySieveType.put(sieveType, 1);
		}

	}

	/**
	 * Generate for each co-reference mention the lists (feature id lists) of:
	 * - appositions
	 * - predicate nominatives
	 * - relative pronouns
	 */
	private void addListOfAppoPredNomRelPro() {

		int numberAppo_DEB = 0;
		int numberPredNom_DEB = 0;
		int numberRelPron_DEB = 0;

		List<Annotation> corefAnontationList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);

		for(Annotation corefAnontation : corefAnontationList) {
			List<Annotation> overlappingSentenceList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, corefAnontation);
			if(overlappingSentenceList.size() == 1) {
				List<Annotation> corefAnontationInSentenceList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate, overlappingSentenceList.get(0));

				for(Annotation sameSentenceCoref : corefAnontationInSentenceList) {
					if(sameSentenceCoref != null && !sameSentenceCoref.getId().equals(corefAnontation.getId())) {
						// For every pair of distinct co-reference mentions in the sentence...

						Integer corefAnnotationHeadId = GateUtil.getIntegerFeature(corefAnontation, "headID").orElse(null);
						Annotation corefAnnotationHeadToken = this.document.getAnnotations(ImporterBase.driAnnSet).get(corefAnnotationHeadId);
						Integer sameSentenceCorefHeadId = GateUtil.getIntegerFeature(sameSentenceCoref, "headID").orElse(null);
						Annotation sameSentenceCorefHeadToken = this.document.getAnnotations(ImporterBase.driAnnSet).get(sameSentenceCorefHeadId);

						if(corefAnnotationHeadId != null && corefAnnotationHeadToken != null && sameSentenceCorefHeadId != null && sameSentenceCorefHeadToken != null) {

							String functionCorefAnnotatioHeadToken = GateUtil.getStringFeature(corefAnnotationHeadToken, MateParser.depKindFeat).orElse(null);
							Integer dependencyCorefAnnotatioHeadToken = GateUtil.getIntegerFeature(corefAnnotationHeadToken, MateParser.depTargetIdFeat).orElse(null);
							String functionSameSentCorefHeadToken = GateUtil.getStringFeature(sameSentenceCorefHeadToken, MateParser.depKindFeat).orElse(null);
							Integer dependencySameSentCorefHeadToken = GateUtil.getIntegerFeature(sameSentenceCorefHeadToken, MateParser.depTargetIdFeat).orElse(null);

							// Other variables (useful to support debug):
							String corefHeadText = GateUtil.getAnnotationText(this.document.getAnnotations(ImporterBase.driAnnSet).get(corefAnnotationHeadId), this.document).orElse(null);
							String sameSentHeadText = GateUtil.getAnnotationText(this.document.getAnnotations(ImporterBase.driAnnSet).get(sameSentenceCorefHeadId), this.document).orElse(null);


							logger.debug("Candidate coreference '" + GateUtil.getAnnotationText(corefAnontation, this.document) +
									"' (head: " + ((corefHeadText != null) ? corefHeadText : "_NO_TEXT_") + ", id:" + ((corefAnnotationHeadId != null) ? corefAnnotationHeadId : "_NO_ID_") + ")");
							logger.debug("Candidate coreference head dep: '" + ((functionCorefAnnotatioHeadToken != null) ? functionCorefAnnotatioHeadToken : "_NO_HEAD_DEP_") + ", "
									+ " target id:" + ((dependencyCorefAnnotatioHeadToken != null) ? dependencyCorefAnnotatioHeadToken : "_NO_HEAD_TARGET_ID_") );
							logger.debug("Same sentence coreference '" + GateUtil.getAnnotationText(sameSentenceCoref, this.document) +
									"' (head: " + ((sameSentHeadText != null) ? sameSentHeadText : "_NO_TEXT_") + ", id:" + ((sameSentenceCorefHeadId != null) ? sameSentenceCorefHeadId : "_NO_ID_") + ")");
							logger.debug("Same sentence coreference head dep: '" + ((functionSameSentCorefHeadToken != null) ? functionSameSentCorefHeadToken : "_NO_HEAD_DEP_") + ", "
									+ " target id:" + ((dependencySameSentCorefHeadToken != null) ? dependencySameSentCorefHeadToken : "_NO_HEAD_TARGET_ID_") );


							// Appositions
							if(Util.strCompareCI(functionSameSentCorefHeadToken, "APPO") && Util.intCompare(dependencySameSentCorefHeadToken, corefAnnotationHeadId)) {
								corefAnontation.setFeatures((corefAnontation.getFeatures() != null) ? corefAnontation.getFeatures() : Factory.newFeatureMap());
								if(corefAnontation.getFeatures().get("appositions") == null) {
									corefAnontation.getFeatures().put("appositions", new ArrayList<Integer>());
								}
								List<Integer> appositionList = (List<Integer>) corefAnontation.getFeatures().get("appositions");
								if(!appositionList.contains(sameSentenceCoref.getId())) {
									appositionList.add(sameSentenceCoref.getId());
								}

								numberAppo_DEB++;

								logger.debug("The candidate coreference '" + GateUtil.getAnnotationText(corefAnontation, this.document) +
										"' (head: " + ((corefHeadText != null) ? corefHeadText : "_NO_TEXT_") + ", id:" + ((corefAnnotationHeadId != null) ? corefAnnotationHeadId : "_NO_ID_") + ") "
										+ "has as apposition '" + GateUtil.getAnnotationText(sameSentenceCoref, this.document) +
										"' (head: " + ((sameSentHeadText != null) ? sameSentHeadText : "_NO_TEXT_") + ", id:" + ((sameSentenceCorefHeadId != null) ? sameSentenceCorefHeadId : "_NO_ID_") + ") ' in sentence:\n"
										+ GateUtil.getAnnotationText(overlappingSentenceList.get(0), this.document).orElse("_NO_SENTENCE_TEXT_"));

							}

							// Predicate nominatives
							if( Util.intCompare(dependencyCorefAnnotatioHeadToken, dependencySameSentCorefHeadToken) && 
									(Util.strCompareCI(functionSameSentCorefHeadToken, "PRD") || Util.strCompareCI(functionSameSentCorefHeadToken, "OPRD")) ) {
								corefAnontation.setFeatures((corefAnontation.getFeatures() != null) ? corefAnontation.getFeatures() : Factory.newFeatureMap());
								if(corefAnontation.getFeatures().get("predicateNominatives") == null) {
									corefAnontation.getFeatures().put("predicateNominatives", new ArrayList<Integer>());
								}
								List<Integer> predicateNominativesList = (List<Integer>) corefAnontation.getFeatures().get("predicateNominatives");
								if(!predicateNominativesList.contains(sameSentenceCoref.getId())) {
									predicateNominativesList.add(sameSentenceCoref.getId());
								}

								numberPredNom_DEB++;

								/*
								logger.debug("The candidate coreference '" + GateUtil.getAnnotationText(corefAnontation, this.document) +
										"' (head: " + ((corefHeadText != null) ? corefHeadText : "_NO_TEXT_") + ", id:" + ((corefAnnotationHeadId != null) ? corefAnnotationHeadId : "_NO_ID_") + ") "
										+ "has as predicate nominative '" + GateUtil.getAnnotationText(sameSentenceCoref, this.document) +
										"' (head: " + ((sameSentHeadText != null) ? sameSentHeadText : "_NO_TEXT_") + ", id:" + ((sameSentenceCorefHeadId != null) ? sameSentenceCorefHeadId : "_NO_ID_") + ") ' in sentence:\n"
										+ GateUtil.getAnnotationText(overlappingSentenceList.get(0), this.document).orElse("_NO_SENTENCE_TEXT_"));
								 */
							}

							// Relative pronouns
							Integer antecedent = GateUtil.getIntegerFeature(sameSentenceCoref, "antecedent").orElse(null);
							if(antecedent != null && Util.intCompare(corefAnnotationHeadId, antecedent)) {
								corefAnontation.setFeatures((corefAnontation.getFeatures() != null) ? corefAnontation.getFeatures() : Factory.newFeatureMap());
								if(corefAnontation.getFeatures().get("relativePronouns") == null) {
									corefAnontation.getFeatures().put("relativePronouns", new ArrayList<Integer>());
								}
								List<Integer> relativePronounsList = (List<Integer>) corefAnontation.getFeatures().get("relativePronouns");
								if(!relativePronounsList.contains(sameSentenceCoref.getId())) {
									relativePronounsList.add(sameSentenceCoref.getId());
								}
								/*
								numberRelPron_DEB++;
								logger.debug("The candidate coreference '" + GateUtil.getAnnotationText(corefAnontation, this.document) +
										"' (head: " + ((corefHeadText != null) ? corefHeadText : "_NO_TEXT_") + ", id:" + ((corefAnnotationHeadId != null) ? corefAnnotationHeadId : "_NO_ID_") + ") "
										+ "has as referring relative pronoun '" + GateUtil.getAnnotationText(sameSentenceCoref, this.document) +
										"' (head: " + ((sameSentHeadText != null) ? sameSentHeadText : "_NO_TEXT_") + ", id:" + ((sameSentenceCorefHeadId != null) ? sameSentenceCorefHeadId : "_NO_ID_") + ") ' in sentence:\n"
										+ GateUtil.getAnnotationText(overlappingSentenceList.get(0), this.document).orElse("_NO_SENTENCE_TEXT_"));
								 */
							}
						}

					}
				}
			}
		}

		logger.debug("Number of appositions identified: " + numberAppo_DEB);
		logger.debug("Number of predicate nominatives identified: " + numberPredNom_DEB);
		logger.debug("Number of relative pronouns identified: " + numberRelPron_DEB);

	}


	private void removeAppositionsAndWPOSstartTokens() {

		List<Integer> coreferenceMentionToDelete = new ArrayList<Integer>();

		List<Annotation> candidateCoreferences = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.coref_Candidate);

		for(Annotation ann : candidateCoreferences) {
			if(ann != null) {
				String originalCandidateCoreferenceStr = GateUtil.getAnnotationText(ann, this.document).orElse(null);
				String reducedCandidateCoreferenceStr = null;

				String corefMentionType = GateUtil.getStringFeature(ann, "mentionType").orElse(null);
				Integer corefMentionHeadId = GateUtil.getIntegerFeature(ann, "headID").orElse(null);
				if(Util.strCompareCI(corefMentionType, "NOMINAL") || Util.strCompareCI(corefMentionType, "PROPER")) {
					List<Annotation> mentionTokenAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, ImporterBase.driAnnSet, ImporterBase.tokenAnnType, ann);

					if(mentionTokenAnnList != null) {
						int headIdIndex = Integer.MAX_VALUE;
						for(int k = 0; k < mentionTokenAnnList.size(); k++) {
							if(corefMentionHeadId != null && mentionTokenAnnList.get(k).getId().equals(corefMentionHeadId)) {
								headIdIndex = k;
								break;
							}
						}

						Integer firstCommaTokenOccurrence = null;
						Integer firstPOSWtokenOccurrence = null;
						for(int k = 0; k < mentionTokenAnnList.size(); k++) {
							String tokenString = GateUtil.getAnnotationText(mentionTokenAnnList.get(k), this.document).orElse(null);
							String tokenPOS = GateUtil.getStringFeature(mentionTokenAnnList.get(k), ImporterBase.token_POSfeat).orElse(null);
							if(headIdIndex < k && tokenString != null) {
								if(tokenString.trim().equals(",") && firstCommaTokenOccurrence == null) {
									firstCommaTokenOccurrence = k;
								}
								if(tokenPOS != null && tokenPOS.toLowerCase().startsWith("w") && firstPOSWtokenOccurrence == null) {
									firstPOSWtokenOccurrence = k;
								}
							}
						}

						if(firstCommaTokenOccurrence != null && 
								mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset() > ann.getStartNode().getOffset()) {
							reducedCandidateCoreferenceStr = GateUtil.getDocumentText(this.document, ann.getStartNode().getOffset(), mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset()).orElse(null);
						}
						else if(firstPOSWtokenOccurrence != null && 
								mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset() > ann.getStartNode().getOffset()) {
							reducedCandidateCoreferenceStr = GateUtil.getDocumentText(this.document, ann.getStartNode().getOffset(), mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset()).orElse(null);
						}
						else {
							reducedCandidateCoreferenceStr = GateUtil.getAnnotationText(ann, this.document).orElse(null);
						}

						// Create a new, reduced candidate coreference mention
						// The start offset is equal to the offset of the start node of the original mention to reduce
						// The end offset is equal to:
						//    - the end node offset of the previous token with respect to the token containing a WPOS or a comma, if the precious token is equal or after the head token
						//    - the start node offset of the token containing a WPOS or a comma
						long startOfReducedCandidateCorefMent = ann.getStartNode().getOffset();
						long endOfReducedCandidateCorefMent = Long.MIN_VALUE;
						if(firstCommaTokenOccurrence != null && 
								mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset() > ann.getStartNode().getOffset()) {
							if(firstCommaTokenOccurrence > headIdIndex) {
								endOfReducedCandidateCorefMent = mentionTokenAnnList.get(firstCommaTokenOccurrence - 1).getEndNode().getOffset();
							}
							else {
								endOfReducedCandidateCorefMent = mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset();
							}
						}
						else if(firstPOSWtokenOccurrence != null && 
								mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset() > ann.getStartNode().getOffset()) {
							if(firstPOSWtokenOccurrence > headIdIndex) {
								endOfReducedCandidateCorefMent = mentionTokenAnnList.get(firstPOSWtokenOccurrence - 1).getEndNode().getOffset();
							}
							else {
								endOfReducedCandidateCorefMent = mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset();
							}
						}

						// Check if no reduced candidate coreference mention exists already
						boolean corefExists = false;
						Annotation existingCoref = null;
						if(endOfReducedCandidateCorefMent >= 0l) {
							for(Annotation annExistCheck : candidateCoreferences) {
								// If an existing candidate coreference has the same start offset and 
								// an end offset that is +-1 equal to the end offset of the reduced coreference to add
								if(annExistCheck != null && annExistCheck.getStartNode().getOffset() == startOfReducedCandidateCorefMent &&
										annExistCheck.getEndNode().getOffset() >= (endOfReducedCandidateCorefMent - 1l) && 
										annExistCheck.getEndNode().getOffset() <= (endOfReducedCandidateCorefMent + 1l) ) {
									existingCoref = annExistCheck;
									corefExists = true;
								}
							}
						}

						if(startOfReducedCandidateCorefMent < endOfReducedCandidateCorefMent) {
							if(!corefExists) {
								// Create a new, reduced candidate coreference mention if it does not exist
								try {
									Integer newAnnotationId = this.document.getAnnotations(ImporterBase.driAnnSet).add(startOfReducedCandidateCorefMent, endOfReducedCandidateCorefMent, ImporterBase.coref_Candidate, ann.getFeatures());
									Annotation newAnnotation = this.document.getAnnotations(ImporterBase.driAnnSet).get(newAnnotationId);

									Integer reducedHeadID = GateUtil.getIntegerFeature(newAnnotation, "headID").orElse(null);
									String reducedHeadText = GateUtil.getAnnotationText(newAnnotation, this.document).orElse(null);

									Integer originalHeadID = GateUtil.getIntegerFeature(ann, "headID").orElse(null);
									String originalHeadText = GateUtil.getAnnotationText(ann, this.document).orElse(null);

									logger.debug("Reduced the candidate mention '" + originalCandidateCoreferenceStr +
											"' (head: " + ((originalHeadText != null) ? originalHeadText : "_NO_TEXT_") + ", " + ((originalHeadID != null) ? originalHeadID : "_NO_ID_") + ")"
											+ " to '" + reducedCandidateCoreferenceStr + "' (head: " + ((reducedHeadText != null) ? reducedHeadText : "_NO_TEXT_") + ", " + ((reducedHeadID != null) ? reducedHeadID : "_NO_ID_") + ")");

									// Delete the old mention
									coreferenceMentionToDelete.add(ann.getId());

								} catch (InvalidOffsetException e) {
									logger.error("Failed to add a reduced candidate coreference mention - " + e.getMessage());
								}
							}
							else {
								// Report all the attributes / features from the extended coreference to delete to the existing reduced version
								Integer existingCorefHeadID = GateUtil.getIntegerFeature(existingCoref, "headID").orElse(null);
								Integer originalCorefHeadID = GateUtil.getIntegerFeature(ann, "headID").orElse(null);
								if(existingCorefHeadID != null && originalCorefHeadID != null && originalCorefHeadID.equals(existingCorefHeadID)) {
									existingCoref.setFeatures(ann.getFeatures());
								}
							}

							// In any case, it is possible to delete the old mention
							coreferenceMentionToDelete.add(ann.getId());
						}
					}
				}
			}
		}

		for(Integer coreferenceMentionToDel : coreferenceMentionToDelete) {
			Annotation annToDel = this.document.getAnnotations(ImporterBase.driAnnSet).get(coreferenceMentionToDel);
			if(annToDel != null) {
				this.document.getAnnotations(ImporterBase.driAnnSet).remove(annToDel);
			}
		}

	}

	@Override
	public boolean resetAnnotations() {
		
		if(!this.annotationReset) {
			// Check input parameters
			this.outputCorefAS = StringUtils.isNotBlank(this.outputCorefAS) ? this.outputCorefAS : ImporterBase.coref_ChainAnnSet;

			this.document.removeAnnotationSet(this.outputCorefAS);

			// Remove the annotations used to spot co-references, all included in
			// the "CorefSpot" (ImporterBase.coref_SpotAnnSet) annotaiton set
			this.document.removeAnnotationSet(ImporterBase.coref_SpotAnnSet);
			
			this.annotationReset = true;
		}
		
		return true;
	}

}
