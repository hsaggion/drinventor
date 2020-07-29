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
package edu.upf.taln.dri.module.coref.sieve;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.coref.SieveTypeEnum;
import edu.upf.taln.dri.module.coref.sieve.model.PersonENUM;
import gate.Annotation;

/**
 * Generic co-reference sieve.
 * 
 *
 */
public abstract class Sieve {

	private static Logger logger = Logger.getLogger(Sieve.class);	

	private SieveTypeEnum st;
	private Integer maxSentenceDistance;

	// Constructor
	public Sieve(SieveTypeEnum st, int maxSentDistance) {
		super();
		this.st = st;
		this.maxSentenceDistance = (maxSentDistance >= 0) ? maxSentDistance : Integer.MAX_VALUE;
	}

	// Getters and setters
	public SieveTypeEnum getSt() {
		return st;
	}

	// Other methods
	/**
	 * Apply a co-reference sieve to all the candidate mentions of a text that are not skipped by the
	 * CorefChainBuilder.skipMention method.
	 * The default visiting order of candidate co-reference mentions is from the first to the last sentence
	 * and in each sentence from the last to the first candidate co-reference mention.
	 * 
	 * ORDER OF ANTECEDENT CHECK IN A SENTENCE:
	 * For each candidate co-reference mention the candidate co-reference antecedents are checked starting from the same sentence
	 * backwards up to the beginning of the text. The order of candidate co-reference antecedents in eachs entence is defined
	 * by the implementation of the method: getOrderedAntecedentList.
	 * 
	 * CHECK IF THE CANDIDATE COREFERENCE MATCH AN ANTECEDENT:
	 * The implementation of the method checkCorefMatch checks if the candidate co-reference mention and a candidate 
	 * co-reference antecedent match.
	 * 
	 * For each candidate co-reference mention we match the first antecedent among those visited, then we consider the 
	 * next candidate co-reference mention and search for its antecedents.
	 * 
	 * The coreference chains of the corefBuilder are properly modified.
	 * 
	 * @param corefBuilder
	 */
	public void applySieve(CorefChainBuilder corefBuilder) {

		List<List<Annotation>> sentenceOrderedCorefMap = corefBuilder.getSentenceOrderedCorefMap();
		Map<Integer, Set<Integer>> corefChainMap = corefBuilder.getCorefChainMap();

		logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		logger.debug("^^^^^^^ START APPLYING SIEVE:" + st);

		// Browsing order of sentences:
		// FROM THE FIRST SENTENCE TO THE LAST SETNTENCE
		for(int corefMentionSentID = 0; corefMentionSentID < sentenceOrderedCorefMap.size(); corefMentionSentID++) {

			List<Annotation> sentenceCoreferenceList = sentenceOrderedCorefMap.get(corefMentionSentID);
			logger.debug("sent " + corefMentionSentID + ") including " + sentenceCoreferenceList.size() + " candidate mentions:");

			// Browsing order of candidate mentions inside sentence (to find the antecedent of each candidate mention):
			// DEFAULT BY DEPTH-FIRST BROWSING OF THE NODES OF THE DEPENDENCY TREE
			Integer currentMentionAnnId = -1;
			for(Annotation coreMentionAnn : sentenceCoreferenceList) {
				currentMentionAnnId++;

				if(coreMentionAnn != null) {

					// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") candidate mention: " + GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()));

					if(CorefChainBuilder.skipMention(coreMentionAnn, st, corefBuilder.getDocument())) {
						logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") SKIPPED mention " + GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()));
						continue;
					}

					boolean antecedentCorefMatched = false;

					// The co-reference mention is not to skip - retrieve and check for matching all its antecedents
					for(int antecedentSentID = corefMentionSentID; antecedentSentID >= 0; antecedentSentID--) {

						int sentenceDistnace = corefMentionSentID - antecedentSentID;

						if(antecedentCorefMatched || (sentenceDistnace > this.maxSentenceDistance)) {
							if(sentenceDistnace > this.maxSentenceDistance) {
								logger.debug("sent " + corefMentionSentID + ") Sentence distnace equal to " + 
										sentenceDistnace + ", greater than the maximum sentence distance to find aa antecedent (MAX DISTANCE: " + this.maxSentenceDistance + ").");
							}
							break;
						}

						logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") looking for candidate coreference antecedents in previous sentence with order number: " + antecedentSentID + "...");

						// Order antecedents in sentence i of coreMentionAnn (in corefMentionSentID)
						List<Annotation> orderedSentenceAntecedents = getOrderedAntecedentList(corefBuilder, coreMentionAnn, corefMentionSentID, antecedentSentID);

						// Print ordered antecedents list
						logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") Candidate antecedents in previous sentence with order number: " + antecedentSentID + ":");
						for(Annotation orderedSentenceAntecedent : orderedSentenceAntecedents) {
							if(orderedSentenceAntecedent != null) {
								logger.debug("          > " + GateUtil.getAnnotationText(orderedSentenceAntecedent, corefBuilder.getDocument()).orElse("_NO_CANDIDATE_MENTION_ANTECEDENT_TEXT_"));
							}
						}

						// Mentions are sorted by length whenever we have two mentions beginning at the same position and having the same head
						// The shorter / the one that has a lower end offset is positioned before the longer

						// How many sentences is distant from the sentence of the current mention?
						// If the antecedent is not found in a sentence within this distance, stop searching

						// Apply co-reference rules of this sieve by selecting the most approapriate antecedent in the orderedSentenceAntecedents
						if(!CollectionUtils.isEmpty(orderedSentenceAntecedents)) {
							for (Annotation antecedentCandidate : orderedSentenceAntecedents) {

								if(antecedentCorefMatched) {
									break;
								}

								// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") Analyzing antecedent '" + GateUtil.getAnnotationText(antecedentCandidate, this.document) +
								//		"' of candidate coreference '" + GateUtil.getAnnotationText(coreMentionAnn, this.document) + "'...");

								Set<Integer> corefClusterMain = corefChainMap.get(coreMentionAnn.getId());
								Set<Integer> corefClusterAntecedent = corefChainMap.get(antecedentCandidate.getId());

								// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") Candidate coreference cluster: " + ((corefClusterMain != null) ? corefClusterMain.size() : "NULL"));
								// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") Antecedent cluster: " + ((corefClusterAntecedent != null) ? corefClusterAntecedent.size() + "" : "NULL"));
								// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") " + ((corefClusterMain.equals(corefClusterAntecedent)) ? " SAME CLUSTER " : " NOT SAME CLUSTER " ));

								if(corefClusterMain == null || corefClusterAntecedent == null) {
									logger.warn("No coreference cluster found for mention");
									continue;
								}
								if(corefClusterMain.equals(corefClusterAntecedent)) {
									logger.debug("sent " + corefMentionSentID + ") The candidate coreference and the antecedent already belong to the same cluster, going to the analysis of the next antecedent.");
									continue;
								}

								boolean matchCandidates = false;

								// ^^^^^^^^^
								// SKIP RULE: Skip if one mention is the apposition of the other - NOT VALID
								/*
								List<Integer> appositionsList_coreMention = GateUtil.getListIntegerFeature(coreMentionAnn, "appositions").orElse(null);
								List<Integer> appositionsList_antecedent = GateUtil.getListIntegerFeature(antecedentCandidate, "appositions").orElse(null);
								if( ( appositionsList_coreMention != null && appositionsList_coreMention.contains(antecedentCandidate.getId())) ||
										( appositionsList_antecedent != null && appositionsList_antecedent.contains(coreMentionAnn.getId()) )) {
									logger.info("Skipped pair of candidate coreference mentions since one is apposition of the other: " + 
											GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_") + " - " + 
											GateUtil.getAnnotationText(antecedentCandidate, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_"));
									continue;
								}
								*/

								// ^^^^^^^^^
								// SKIP RULE: Skip if the antecedent is "this" and the distance is greater than 3 sentences
								String antecedentCandidateStr = GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse(null);
								if(antecedentCandidateStr != null && antecedentCandidateStr.trim().toLowerCase().equals("this") && sentenceDistnace > 3) {
									logger.debug("Skipped pair of candidate coreference mentions since the antecedent is this and the sentence dstance is greater than 3: " + 
											GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_") + " - " + 
											GateUtil.getAnnotationText(antecedentCandidate, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_"));
									continue;
								}
								
								// ^^^^^^^^^
								// SKIP RULE: Skip if PRONOMINAL_MATCH sieve, and the antecedent person is I or YOU and
								// the distance is greater than 3 sentences
								PersonENUM antecedentPerson = Util.getPersonDetails(antecedentCandidate, corefBuilder.getDocument());
								if(st.equals(SieveTypeEnum.PRONOMINAL_MATCH) && 
									antecedentPerson != null && (antecedentPerson.equals(PersonENUM.I) || antecedentPerson.equals(PersonENUM.YOU)) &&
									sentenceDistnace > 3) {
									logger.debug("Skipped pair of candidate coreference mentions since PRONOMINAL_MATCH sieve with the antecedent that "
											+ "is I ot YOU and the sentence dstance is greater than 3: " + 
											GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_") + " - " + 
											GateUtil.getAnnotationText(antecedentCandidate, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_"));
									continue;
								}
								
								// ^^^^^^^^^
								// SKIP RULE: if the candidate mention is contained in the antecedent or vice versa
								if( (coreMentionAnn.getStartNode().getOffset() >= antecedentCandidate.getStartNode().getOffset() && coreMentionAnn.getEndNode().getOffset() <= antecedentCandidate.getEndNode().getOffset()) ||
									(antecedentCandidate.getStartNode().getOffset() >= coreMentionAnn.getStartNode().getOffset() && antecedentCandidate.getEndNode().getOffset() <= coreMentionAnn.getEndNode().getOffset())) {
									logger.debug("Skipped pair of candidate coreference mentions since one is contained in the other one: " + 
											GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_") + " - " + 
											GateUtil.getAnnotationText(antecedentCandidate, corefBuilder.getDocument()).orElse("_NO_MENTION_TEXT_"));
									continue;
								}

								// Invoking sieve implementation
								matchCandidates = checkCorefMatch(corefBuilder, coreMentionAnn, antecedentCandidate, sentenceDistnace);
								if(matchCandidates) {
									logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") EXACT_MATCH: matched.");
								}

								// Check if to merge the candidates clusters with respect to the sieve info
								if(matchCandidates) {
									corefBuilder.mergeTwoCoreferenceChains(coreMentionAnn, antecedentCandidate, st);
									logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") MATCHED " + st.toString() + ", merging clusters...");
									logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") new candidate coreference cluster: " + corefChainMap.get(coreMentionAnn.getId()));
									logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") new antecedent cluster: " + corefChainMap.get(antecedentCandidate.getId()));
									logger.debug("sent " + corefMentionSentID + " mention id: " + currentMentionAnnId + ") " + ((corefChainMap.get(coreMentionAnn.getId()).equals(corefChainMap.get(antecedentCandidate.getId()))) ? " SAME CLUSTER " : " NOT SAME CLUSTER " ));
									antecedentCorefMatched = true;
								}
								else {
									// logger.debug("sent " + currentSentenceID + " mention id: " + currentMentionAnnId + ") NO MATCH.");
								}

							}

						}

					}
				}
			}
		}

		logger.debug("^^^^^^^ APPLYED SIEVE: " + st);
		logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	/**
	 * Implementations will generate an ordered list of candidate co-reference antecedents from sentence with order ID antecedentSentID
	 * considering as core co-reference mention the coreMentionAnn of sentence with order ID currentSentID
	 * 
	 * @param corefBuilder
	 * @param coreMentionAnn
	 * @param currentSentID
	 * @param antecedentSentID
	 * @return
	 */
	abstract List<Annotation> getOrderedAntecedentList(CorefChainBuilder corefBuilder, Annotation coreMentionAnn, int currentSentID, int antecedentSentID);
	
	/**
	 * Implementations will check if antecedentCandidate is a candidate co-reference antecedent of the co-reference mention coreMentionAnn
	 * 
	 * @param corefBuilder
	 * @param coreMentionAnn
	 * @param antecedentCandidateAnn
	 * @param distance
	 * @return
	 */
	abstract boolean checkCorefMatch(CorefChainBuilder corefBuilder, Annotation coreMentionAnn, Annotation antecedentCandidateAnn, int distance);
}
