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
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.coref.SieveTypeEnum;
import gate.Annotation;

/**
 * Co-reference by relative pronoun spotting.
 * 
 *
 */
public class RelativePronounSieve extends Sieve {
	
	private static Logger logger = Logger.getLogger(RelativePronounSieve.class);	
	
	public RelativePronounSieve() {
		super(SieveTypeEnum.RELATIVE_PRONOUN, 2);
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

		return (relativePronoun(coreMentionAnn, antecedentCandidateAnn) || relativePronoun(antecedentCandidateAnn, coreMentionAnn));
		
	}

	/**
	 * Check if the mentionSec is a relative pronoun that refers to the mentionCore
	 * 
	 * @param mentionCore
	 * @param mentionSec
	 * @return
	 */
	private boolean relativePronoun(Annotation mentionCore, Annotation mentionSec) {
		
		String mentionCoreType = GateUtil.getStringFeature(mentionCore, "mentionType").orElse(null);
		List<Integer> mentionCoreRelPronList = GateUtil.getListIntegerFeature(mentionCore, "relativePronouns").orElse(null);

		if( mentionCoreRelPronList != null && mentionCoreRelPronList.contains(mentionSec.getId()) ) { // SKIPPED: (Util.strCompareCI(mentionCoreType, "NOMINAL") || Util.strCompareCI(mentionCoreType, "PROPER") || Util.strCompareCI(mentionCoreType, "PRONOMINAL"))
			return true;
		}

		return false;
	}
}
