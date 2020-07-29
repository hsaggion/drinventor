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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.coref.SieveTypeEnum;
import edu.upf.taln.dri.module.coref.sieve.dicts.DictCollections;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;

/**
 * Co-reference by relaxed string match spotting.
 * 
 *
 */
public class RelaxedMatchSieve extends Sieve {
	
	private static Logger logger = Logger.getLogger(RelaxedMatchSieve.class);	
	
	public RelaxedMatchSieve() {
		super(SieveTypeEnum.RELAXED_MATCH, 5);
	}
	
	public List<Annotation> getOrderedAntecedentList(CorefChainBuilder corefBuilder, Annotation coreMentionAnn, int currentSentID, int antecedentSentID) {
		List<Annotation> returnAntecedentList = new ArrayList<Annotation>();
		
		// logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn + ") ORDERING CANDIDATE ANTECEDENTS OF SENTENCE " + antecedentSentID + " STARTS...");
		
		if(antecedentSentID == currentSentID) {
			logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn + ") "
					+ "We're looking for antecedents in order of depth-first exploration of the dependency tree.");

			// Get all the mentions of the same sentence with start offset < than the end offset of the mention under analysis (coreMentionAnn)
			for(Annotation sentCorefAnn : corefBuilder.getSentenceOrderedCorefMap().get(currentSentID)) {
				if(sentCorefAnn != null && !sentCorefAnn.getId().equals(coreMentionAnn.getId()) &&
						sentCorefAnn.getStartNode().getOffset() < coreMentionAnn.getEndNode().getOffset()) {
					returnAntecedentList.add(sentCorefAnn);
				}
			}
			
		}
		else {
			logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn + ") "
					+ "We're looking for antecedents in order of depth-first exploration of the dependency tree.");
			for(Annotation sentCorefAnn : corefBuilder.getSentenceOrderedCorefMap().get(antecedentSentID)) {
				returnAntecedentList.add(sentCorefAnn);
			}
		}
		
		return returnAntecedentList;
	}

	
	public boolean checkCorefMatch(CorefChainBuilder corefBuilder, Annotation coreMentionAnn, Annotation antecedentCandidateAnn, int distance) {
		if(coreMentionAnn == null || antecedentCandidateAnn == null) {
			return false;
		}

		String coreMentionStr = null;
		String antecMentionStr = null;
		
		// TODO: check if the coreference mention is an apposition? If so, return false

		// Return the coreMentionStr and antecMentionStr up to the first comma or the first token that has a POS that starts with 'W'
		
		// Get the full string of core mention
		String corefMentionType = GateUtil.getStringFeature(coreMentionAnn, "mentionType").orElse(null);
		Integer corefMentionHeadId = GateUtil.getIntegerFeature(coreMentionAnn, "headID").orElse(null);
		if(Util.strCompareCI(corefMentionType, "NOMINAL") || Util.strCompareCI(corefMentionType, "PROPER")) {
			List<Annotation> mentionTokenAnnList = GateUtil.getAnnInDocOrderContainedAnn(corefBuilder.getDocument(),
					ImporterBase.driAnnSet, ImporterBase.tokenAnnType, coreMentionAnn);
			
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
					String tokenString = GateUtil.getAnnotationText(mentionTokenAnnList.get(k), corefBuilder.getDocument()).orElse(null);
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
						mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset() > coreMentionAnn.getStartNode().getOffset()) {
					coreMentionStr = GateUtil.getDocumentText(corefBuilder.getDocument(),
							coreMentionAnn.getStartNode().getOffset(), mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset()).orElse(null);
				}
				else if(firstPOSWtokenOccurrence != null && 
						mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset() > coreMentionAnn.getStartNode().getOffset()) {
					coreMentionStr = GateUtil.getDocumentText(corefBuilder.getDocument(),
							coreMentionAnn.getStartNode().getOffset(), mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset()).orElse(null);
				}
				else {
					coreMentionStr = GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse(null);
				}
			}
		}

		// Get the full string of antecedent mention
		String antecMentionType = GateUtil.getStringFeature(antecedentCandidateAnn, "mentionType").orElse(null);
		Integer antecMentionHeadId = GateUtil.getIntegerFeature(antecedentCandidateAnn, "headID").orElse(null);
		if(Util.strCompareCI(antecMentionType, "NOMINAL") || Util.strCompareCI(antecMentionType, "PROPER")) {
			List<Annotation> mentionTokenAnnList = GateUtil.getAnnInDocOrderContainedAnn(corefBuilder.getDocument(),
					ImporterBase.driAnnSet, ImporterBase.tokenAnnType, antecedentCandidateAnn);
			
			if(mentionTokenAnnList != null) {
				int headIdIndex = Integer.MAX_VALUE;
				for(int k = 0; k < mentionTokenAnnList.size(); k++) {
					if(antecMentionHeadId != null && mentionTokenAnnList.get(k).getId().equals(antecMentionHeadId)) {
						headIdIndex = k;
						break;
					}
				}
				
				Integer firstCommaTokenOccurrence = null;
				Integer firstPOSWtokenOccurrence = null;
				for(int k = 0; k < mentionTokenAnnList.size(); k++) {
					String tokenString = GateUtil.getAnnotationText(mentionTokenAnnList.get(k), corefBuilder.getDocument()).orElse(null);
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
						mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset() > antecedentCandidateAnn.getStartNode().getOffset()) {
					antecMentionStr = GateUtil.getDocumentText(corefBuilder.getDocument(),
							antecedentCandidateAnn.getStartNode().getOffset(), mentionTokenAnnList.get(firstCommaTokenOccurrence).getStartNode().getOffset()).orElse(null);
				}
				else if(firstPOSWtokenOccurrence != null && 
						mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset() > antecedentCandidateAnn.getStartNode().getOffset()) {
					antecMentionStr = GateUtil.getDocumentText(corefBuilder.getDocument(),
							antecedentCandidateAnn.getStartNode().getOffset(), mentionTokenAnnList.get(firstPOSWtokenOccurrence).getStartNode().getOffset()).orElse(null);
				}
				else {
					antecMentionStr = GateUtil.getAnnotationText(antecedentCandidateAnn, corefBuilder.getDocument()).orElse(null);
				}
			}
		}

		// TODO: now comparisons are case insensitive, as Stanford Coref - check if to lowercase or not before exact match!!!

		if(coreMentionStr != null && antecMentionStr != null && !coreMentionStr.equals("") && !antecMentionStr.equals("") &&
				!DictCollections.allPronouns.contains(coreMentionStr.trim().toLowerCase()) && !DictCollections.allPronouns.contains(antecMentionStr.trim().toLowerCase()) &&
				( Util.strCompareCI(coreMentionStr, antecMentionStr) || Util.strCompareCI(coreMentionStr, antecMentionStr + " 's") || Util.strCompareCI(antecMentionStr, coreMentionStr + " 's") ) ) {
			return true;
		}
		else {
			return false;
		}
		
	}

}
