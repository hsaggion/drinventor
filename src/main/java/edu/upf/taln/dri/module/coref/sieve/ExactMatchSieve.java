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
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.coref.SieveTypeEnum;
import edu.upf.taln.dri.module.coref.sieve.dicts.DictCollections;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;

public class ExactMatchSieve extends Sieve {
	
	private static Logger logger = Logger.getLogger(ExactMatchSieve.class);	
	
	public ExactMatchSieve() {
		super(SieveTypeEnum.EXACT_MATCH, 30);
	}
	
	public List<Annotation> getOrderedAntecedentList(CorefChainBuilder corefBuilder, Annotation coreMentionAnn, int currentSentID, int antecedentSentID) {
		List<Annotation> returnAntecedentList = new ArrayList<Annotation>();
		
		// logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn + ") ORDERING CANDIDATE ANTECEDENTS OF SENTENCE " + antecedentSentID + " STARTS...");
		
		if(antecedentSentID == currentSentID) {
			logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn.getId() + ") "
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
			logger.debug("sent " + currentSentID + " with mention GATE id: " + coreMentionAnn.getId() + ") "
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
		
		// Get main and antecedent coreference chains
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(coreMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(antecedentCandidateAnn.getId());
		
		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}
		
		boolean matched = false;
		
		for(Integer coreCorefId : coreCoreferenceChain) {
			
			// Get coreCorefAnnotation from ID
			Annotation coreCorefAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(coreCorefId);
			if(coreCorefAnn == null) continue; 
			
			// TODO: check if it is an apposition? If so, return false
			
			String coreMentionStr = null;
			String corefMentionType = GateUtil.getStringFeature(coreCorefAnn, "mentionType").orElse(null);
			if(Util.strCompareCI(corefMentionType, "NOMINAL") || Util.strCompareCI(corefMentionType, "PROPER")) {
				Optional<String> mentionContent = GateUtil.getAnnotationText(coreCorefAnn, corefBuilder.getDocument());
				if(mentionContent.isPresent()) {
					coreMentionStr = mentionContent.get();
				}
			}
			
			if( coreMentionStr != null && !DictCollections.allPronouns.contains(coreMentionStr.trim().toLowerCase()) ) {
				for(Integer antecedentCorefId : antecedentCoreferenceChain) {
					// Get antecedentCorefAnn from ID
					Annotation antecedentCorefAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(antecedentCorefId);
					if(antecedentCorefAnn == null) continue;
					
					String antecMentionStr = null;
					String antecMentionType = GateUtil.getStringFeature(antecedentCorefAnn, "mentionType").orElse(null);
					if(Util.strCompareCI(antecMentionType, "NOMINAL") || Util.strCompareCI(antecMentionType, "PROPER")) {
						Optional<String> mentionContent = GateUtil.getAnnotationText(antecedentCorefAnn, corefBuilder.getDocument());
						if(mentionContent.isPresent()) {
							antecMentionStr = mentionContent.get();
						}
					}
					
					if( antecMentionStr == null || DictCollections.allPronouns.contains(antecMentionStr.trim().toLowerCase()) ) continue;
					
					if(!coreMentionStr.equals("") && !antecMentionStr.equals("") &&
						( Util.strCompareCI(coreMentionStr, antecMentionStr) || Util.strCompareCI(coreMentionStr, antecMentionStr + " 's") || Util.strCompareCI(antecMentionStr, coreMentionStr + " 's") ) ) {
						matched = true;
					}
					
				}
			}
		}
		
		return matched;
	}

}
