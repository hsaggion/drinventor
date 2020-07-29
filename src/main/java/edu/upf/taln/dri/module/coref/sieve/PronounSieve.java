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
import edu.upf.taln.dri.module.coref.sieve.dicts.DictCollections;
import edu.upf.taln.dri.module.coref.sieve.model.GenderENUM;
import edu.upf.taln.dri.module.coref.sieve.model.NumberENUM;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;

/**
 * Co-reference by pronoun spotting.
 * 
 *
 */
public class PronounSieve extends Sieve {

	private static Logger logger = Logger.getLogger(PronounSieve.class);	

	public PronounSieve() {
		super(SieveTypeEnum.PRONOMINAL_MATCH, 5);
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

		// ATTENTION: if the pronoun is not personal or possessive, thus is of POS type
		// - wh-determiner (WDT, example: which) 
		// - wh-pronoun possessive (WP, example: who, what) 
		// - wh-pronoun (WP$, example: whose)
		// the relative pronoun sieve will analyze them and properly connect in coreference chains
		Integer corefMentionHeadId = GateUtil.getIntegerFeature(coreMentionAnn, "headID").orElse(null);
		if(corefMentionHeadId != null) {
			Annotation headTokenAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(corefMentionHeadId);
			String headTokenPOS = GateUtil.getStringFeature(headTokenAnn, ImporterBase.token_POSfeat).orElse(null);
			if(headTokenAnn != null && headTokenPOS != null && headTokenPOS.startsWith("W")) {
				return false;
			}
		}

		// Pre checking filters


		// Actual pronominal match
		String coreMentionStr = GateUtil.getAnnotationText(coreMentionAnn, corefBuilder.getDocument()).orElse(null);
		String corefMentionType = GateUtil.getStringFeature(coreMentionAnn, "mentionType").orElse(null);
		
		if(coreMentionStr != null && corefMentionType != null && corefMentionType.equals("PRONOMINAL") && DictCollections.allPronouns.contains(coreMentionStr.trim().toLowerCase())) {

			boolean genderMatch = edu.upf.taln.dri.module.coref.sieve.Util.checkGenderConcordance(coreMentionAnn, antecedentCandidateAnn, corefBuilder);
			boolean numberMatch = edu.upf.taln.dri.module.coref.sieve.Util.checkNumberConcordance(coreMentionAnn, antecedentCandidateAnn, corefBuilder);
			// Animacy and NER are not considered
			// boolean animacyMatch = edu.upf.taln.dri.module.coref.sieve.Util.checkAnimacyConcordance(coreMentionAnn, antecedentCandidateAnn, corefBuilder);
			// boolean NETmatch = edu.upf.taln.dri.module.coref.sieve.Util.checkNERconcordance(coreMentionAnn, antecedentCandidateAnn, corefBuilder);

			// Not implemented demonym list, organization and entity person disagreement

			if(genderMatch && numberMatch) {
				GenderENUM coreGen = Util.getGenderDetails(coreMentionAnn, corefBuilder.getDocument());
				NumberENUM coreNum = Util.getNumberDetails(coreMentionAnn, corefBuilder.getDocument());
				String anteMentionStr = GateUtil.getAnnotationText(antecedentCandidateAnn, corefBuilder.getDocument()).orElse(null);
				GenderENUM antecGen = Util.getGenderDetails(antecedentCandidateAnn, corefBuilder.getDocument());
				NumberENUM antecNum = Util.getNumberDetails(antecedentCandidateAnn, corefBuilder.getDocument());
				logger.debug("The pronoun '" + ((coreMentionStr != null) ? coreMentionStr : "_NOT_PRESENT_") + "' and the antecedent "
						+ "'" + ((anteMentionStr != null) ? anteMentionStr : "_NOT_PRESENT_") + "' MATCH "
						+ "(gender match: " + genderMatch + " (core: " + ((coreGen != null) ? coreGen : "NULL") + ", antec: " + ((antecGen != null) ? antecGen : "NULL") + ") "
						+ ", number match: " + numberMatch + " (core: " + ((coreNum != null) ? coreNum : "NULL") + ", antec: " + ((antecNum != null) ? antecNum : "NULL") + ") ).");
				return true;
			}
			else {
				GenderENUM coreGen = Util.getGenderDetails(coreMentionAnn, corefBuilder.getDocument());
				NumberENUM coreNum = Util.getNumberDetails(coreMentionAnn, corefBuilder.getDocument());
				String anteMentionStr = GateUtil.getAnnotationText(antecedentCandidateAnn, corefBuilder.getDocument()).orElse(null);
				GenderENUM antecGen = Util.getGenderDetails(antecedentCandidateAnn, corefBuilder.getDocument());
				NumberENUM antecNum = Util.getNumberDetails(antecedentCandidateAnn, corefBuilder.getDocument());
				logger.debug("The pronoun '" + ((coreMentionStr != null) ? coreMentionStr : "_NOT_PRESENT_") + "' and the antecedent "
						+ "'" + ((anteMentionStr != null) ? anteMentionStr : "_NOT_PRESENT_") + "' does not match "
						+ "(gender match: " + genderMatch + " (core: " + ((coreGen != null) ? coreGen : "NULL") + ", antec: " + ((antecGen != null) ? antecGen : "NULL") + ") "
						+ ", number match: " + numberMatch + " (core: " + ((coreNum != null) ? coreNum : "NULL") + ", antec: " + ((antecNum != null) ? antecNum : "NULL") + ") ).");
			}
		}

		return false;

	}

}
