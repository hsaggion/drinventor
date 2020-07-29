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

import java.util.HashSet;
import java.util.Set;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.coref.sieve.dicts.DictCollections;
import edu.upf.taln.dri.module.coref.sieve.model.AnimacyENUM;
import edu.upf.taln.dri.module.coref.sieve.model.GenderENUM;
import edu.upf.taln.dri.module.coref.sieve.model.NumberENUM;
import edu.upf.taln.dri.module.coref.sieve.model.PersonENUM;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;

/**
 * Co-reference spotting utilities.
 * 
 *
 */
public class Util {

	public static PersonENUM getPersonDetails(Annotation candidateCorefMentionAnn, gate.Document gateDoc) {
		PersonENUM persRetVal = null;

		String corefMentionText = GateUtil.getAnnotationText(candidateCorefMentionAnn, gateDoc).orElse("");
		String corefMentionType = GateUtil.getStringFeature(candidateCorefMentionAnn, "mentionType").orElse(null);

		// No head values of the candidate coreference mention are needed, since the Person is only for pronouns that are all head mentions
		// Integer headID = GateUtil.getIntegerFeature(candidateCorefMentionAnn, "headID").orElse(null);
		// Annotation headAnnotation = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(headID);
		// String headString = (headID != null) ? GateUtil.getAnnotationText(headAnnotation, corefBuilder.getDocument()).orElse(null) : null;
		// String personCandidate = GateUtil.getStringFeature(candidateCorefMentionAnn, "person").orElse(null);

		if(candidateCorefMentionAnn == null || corefMentionText == null || corefMentionText.equals("") || !corefMentionType.equals("PRONOMINAL")) {
			return PersonENUM.UNKNOWN;
		}

		// Only for pronouns
		if(corefMentionType == null || !corefMentionType.equals("PRONOMINAL")) {
			return PersonENUM.UNKNOWN;
		}

		corefMentionText = corefMentionText.trim().toLowerCase();

		NumberENUM number = getNumberDetails(candidateCorefMentionAnn, gateDoc);
		GenderENUM gender = getGenderDetails(candidateCorefMentionAnn, gateDoc);
		AnimacyENUM animacy = getAnimacyDetails(candidateCorefMentionAnn, gateDoc);

		if(number == null) number = NumberENUM.UNKNOWN; 
		if(gender == null) gender = GenderENUM.UNKNOWN;
		if(animacy == null) animacy = AnimacyENUM.UNKNOWN; 

		// Equal to STNLP impl

		if(DictCollections.firstPersonPronouns.contains(corefMentionText)) {
			if (number.equals(NumberENUM.SINGULAR)) {
				persRetVal = PersonENUM.I;
			} else if (number.equals(NumberENUM.PLURAL)) {
				persRetVal = PersonENUM.WE;
			} else {
				persRetVal = PersonENUM.UNKNOWN;
			}
		} else if(DictCollections.secondPersonPronouns.contains(corefMentionText)) {
			persRetVal = PersonENUM.YOU;
		} else if(DictCollections.thirdPersonPronouns.contains(corefMentionText)) {
			if (gender.equals(GenderENUM.MALE) && number.equals(NumberENUM.SINGULAR)) {
				persRetVal = PersonENUM.HE;
			} else if (gender.equals(GenderENUM.FEMALE) && number.equals(NumberENUM.SINGULAR)) {
				persRetVal = PersonENUM.SHE;
			} else if ((gender.equals(GenderENUM.NEUTRAL) || animacy.equals(AnimacyENUM.INANIMATE)) && number.equals(NumberENUM.SINGULAR)) {
				persRetVal = PersonENUM.IT;
			} else if (number.equals(NumberENUM.PLURAL)) {
				persRetVal = PersonENUM.THEY;
			} else {
				persRetVal = PersonENUM.UNKNOWN;
			}
		} else {
			persRetVal = PersonENUM.UNKNOWN;
		}

		return persRetVal;
	}


	public static GenderENUM getGenderDetails(Annotation candidateCorefMentionAnn, gate.Document gateDoc) {
		GenderENUM genderRetVal = null;

		if(gateDoc == null) {
			return null;
		}

		String corefMentionText = GateUtil.getAnnotationText(candidateCorefMentionAnn, gateDoc).orElse("");
		String corefMentionType = GateUtil.getStringFeature(candidateCorefMentionAnn, "mentionType").orElse(null);
		Integer headID = GateUtil.getIntegerFeature(candidateCorefMentionAnn, "headID").orElse(null);
		Annotation headAnnotation = gateDoc.getAnnotations(ImporterBase.driAnnSet).get(headID);
		String headString = (headID != null) ? GateUtil.getAnnotationText(headAnnotation, gateDoc).orElse(null) : null;
		String genderCandidate = GateUtil.getStringFeature(candidateCorefMentionAnn, "gender").orElse(null);

		if(candidateCorefMentionAnn == null || corefMentionText == null || corefMentionText.equals("")) {
			return GenderENUM.UNKNOWN;
		}


		if (corefMentionType.equals("PRONOMINAL")) {
			if (DictCollections.malePronouns.contains(headString)) {
				genderRetVal = GenderENUM.MALE;
			} else if (DictCollections.femalePronouns.contains(headString)) {
				genderRetVal = GenderENUM.FEMALE;
			}
		}

		if(genderRetVal == null || genderRetVal.equals(GenderENUM.UNKNOWN)) {
			if(DictCollections.maleWords.contains(headString.trim().toLowerCase())) {
				genderRetVal = GenderENUM.MALE;
			}
			else if(DictCollections.femaleWords.contains(headString.trim().toLowerCase()))  {
				genderRetVal = GenderENUM.FEMALE;
			}
			else if(DictCollections.neutralWords.contains(headString.trim().toLowerCase()))   {
				genderRetVal = GenderENUM.NEUTRAL;
			}
		}

		// Not checked:
		//- if the mention is a person type (NER), check for every token of the name in the male, female, neutral list

		// If the gender is not assigned by previous code, use the gender eventually assigned by JAPE
		if(genderRetVal == null) {
			if(genderCandidate != null && genderCandidate.equals("MALE")) {
				genderRetVal = GenderENUM.MALE;
			}
			else if(genderCandidate != null && genderCandidate.equals("FEMALE")) {
				genderRetVal = GenderENUM.FEMALE;
			}
			else if(genderCandidate != null && genderCandidate.equals("NEUTRAL")) {
				genderRetVal = GenderENUM.NEUTRAL;
			}
			else {
				genderRetVal = GenderENUM.UNKNOWN;
			}
		}

		return genderRetVal;

	}

	public static NumberENUM getNumberDetails(Annotation candidateCorefMentionAnn, gate.Document gateDoc) {
		NumberENUM numberRetVal = null;

		if(gateDoc == null) {
			return null;
		}

		String corefMentionText = GateUtil.getAnnotationText(candidateCorefMentionAnn, gateDoc).orElse("");
		String corefMentionType = GateUtil.getStringFeature(candidateCorefMentionAnn, "mentionType").orElse(null);
		Integer headID = GateUtil.getIntegerFeature(candidateCorefMentionAnn, "headID").orElse(null);
		Annotation headAnnotation = gateDoc.getAnnotations(ImporterBase.driAnnSet).get(headID);
		String headString = (headID != null) ? GateUtil.getAnnotationText(headAnnotation, gateDoc).orElse(null) : null;
		String numberCandidate = GateUtil.getStringFeature(candidateCorefMentionAnn, "number").orElse(null);

		if(candidateCorefMentionAnn == null || corefMentionText == null || corefMentionText.equals("")) {
			return NumberENUM.UNKNOWN;
		}

		String pos = GateUtil.getStringFeature(candidateCorefMentionAnn, "category").orElse(null);

		if(corefMentionType != null && corefMentionType.equals("PRONOMINAL")) { // Number of pronouns
			if (DictCollections.pluralPronouns.contains(headString.trim().toLowerCase())) {
				numberRetVal = NumberENUM.PLURAL;
			} else if (DictCollections.singularPronouns.contains(headString.trim().toLowerCase())) {
				numberRetVal = NumberENUM.SINGULAR;
			} else {
				numberRetVal = NumberENUM.UNKNOWN;
			}
		}
		else if(pos != null && pos.startsWith("N") && pos.endsWith("S")) { // Noun plural
			numberRetVal = NumberENUM.PLURAL;
		}
		else if(pos != null && pos.startsWith("N")) {
			numberRetVal = NumberENUM.SINGULAR;
		}

		// Not checked:
		// - if mention type is LIST --> plural
		// - if NER is ORGANIZATION --> singular
		// - if not PRONOMINAL and number is UNKNOWN --> check in singular and plural nouns list

		// If the number is not assigned by previous code, use the number eventually assigned by JAPE
		if(numberRetVal == null) {
			if(numberCandidate != null && numberCandidate.equals("SINGULAR")) {
				numberRetVal = NumberENUM.SINGULAR;
			}
			else if(numberCandidate != null && numberCandidate.equals("PLURAL")) {
				numberRetVal = NumberENUM.PLURAL;
			}
			else {
				numberRetVal = NumberENUM.UNKNOWN;
			}
		}

		return numberRetVal;
	}

	public static AnimacyENUM getAnimacyDetails(Annotation candidateCorefMentionAnn, gate.Document gateDoc) {
		AnimacyENUM animacyRetVal = null;

		String corefMentionText = GateUtil.getAnnotationText(candidateCorefMentionAnn, gateDoc).orElse("");
		String corefMentionType = GateUtil.getStringFeature(candidateCorefMentionAnn, "mentionType").orElse(null);
		Integer headID = GateUtil.getIntegerFeature(candidateCorefMentionAnn, "headID").orElse(null);
		Annotation headAnnotation = gateDoc.getAnnotations(ImporterBase.driAnnSet).get(headID);
		String headString = (headID != null) ? GateUtil.getAnnotationText(headAnnotation, gateDoc).orElse(null) : null;
		String animacyCandidate = GateUtil.getStringFeature(candidateCorefMentionAnn, "animacy").orElse(null);

		if(candidateCorefMentionAnn == null || corefMentionText == null || corefMentionText.equals("")) {
			return AnimacyENUM.UNKNOWN;
		}

		if(corefMentionType != null && corefMentionType.equals("PRONOMINAL")) { // Number of pronouns
			if (DictCollections.animatePronouns.contains(headString.trim().toLowerCase())) {
				animacyRetVal = AnimacyENUM.ANIMATE;
			} else if (DictCollections.inanimatePronouns.contains(headString.trim().toLowerCase())) {
				animacyRetVal = AnimacyENUM.INANIMATE;
			} else {
				animacyRetVal = AnimacyENUM.UNKNOWN;
			}
		}
		else {
			// If the type of mention is not PRONOMINAL it is possible to use the animacy list
			if(DictCollections.animateWords.contains(headString))  {
				animacyRetVal = AnimacyENUM.ANIMATE;
			}
			else if(DictCollections.inanimateWords.contains(headString)) {
				animacyRetVal = AnimacyENUM.INANIMATE;
			}
		}

		// Not checked:
		// - determine the animacy on the basis of the NER type
		// - check if to disable or not the lookup in the animate / inanimate unigram lists

		// If the number is not assigned by previous code, use the number eventually assigned by JAPE
		if(animacyRetVal == null) {
			if(animacyCandidate != null && animacyCandidate.equals("ANIMATE")) {
				animacyRetVal = AnimacyENUM.ANIMATE;
			}
			else if(animacyCandidate != null && animacyCandidate.equals("INANIMATE")) {
				animacyRetVal = AnimacyENUM.INANIMATE;
			}
			else {
				animacyRetVal = AnimacyENUM.UNKNOWN;
			}
		}

		return animacyRetVal;
	}
	
	public static boolean entityAttributesAgree(Annotation candidateCorefMentionAnn, Annotation potentialAntecedentAnn, boolean ignoreGender, CorefChainBuilder corefBuilder){

		// Get clusters of both mentions
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(candidateCorefMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(potentialAntecedentAnn.getId());

		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}

		// ******************************
		// ******************************
		// Number
		boolean numberConcordance = checkNumberConcordance(candidateCorefMentionAnn, potentialAntecedentAnn, corefBuilder);
		
		if(!numberConcordance) return false;

		// ******************************
		// ******************************
		// gender
		boolean genderConcordance = checkGenderConcordance(candidateCorefMentionAnn, potentialAntecedentAnn, corefBuilder);
		
		if(!genderConcordance) return false;

		// ******************************
		// ******************************
		// animacy
		boolean animacyConcordance = checkAnimacyConcordance(candidateCorefMentionAnn, potentialAntecedentAnn, corefBuilder);
		
		if(!animacyConcordance) return false;
		
		// ******************************
		// ******************************
		// NE type - NOT CHECKED!!!
		boolean NERconcordance = checkNERconcordance(candidateCorefMentionAnn, potentialAntecedentAnn, corefBuilder);
		
		if(!NERconcordance) return false;
		
		return true;
	}


	public static boolean checkNumberConcordance(Annotation candidateCorefMentionAnn, Annotation potentialAntecedentAnn, CorefChainBuilder corefBuilder) {
		// Get clusters of both mentions
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(candidateCorefMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(potentialAntecedentAnn.getId());

		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}


		boolean hasExtraAnt = false;
		boolean hasExtraThis = false;

		// ******************************
		// ******************************
		// Number
		
		Set<NumberENUM> coreCorefNUMBERS = new HashSet<NumberENUM>();
		for(Integer coreAnnId : coreCoreferenceChain) {
			// Get coreAnn from ID
			Annotation coreAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(coreAnnId);
			if(coreAnn == null) continue;

			NumberENUM number = getNumberDetails(coreAnn, corefBuilder.getDocument());
			if(number != null) coreCorefNUMBERS.add(number);
		}

		Set<NumberENUM> antecedentNUMBERS = new HashSet<NumberENUM>();
		for(Integer antecAnnId : antecedentCoreferenceChain) {
			// Get antecAnn from ID
			Annotation antecAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(antecAnnId);
			if(antecAnn == null) continue;

			NumberENUM number = getNumberDetails(antecAnn, corefBuilder.getDocument());
			if(number != null) antecedentNUMBERS.add(number);
		}


		if(!coreCorefNUMBERS.contains(NumberENUM.UNKNOWN)){
			for(NumberENUM antecNum : antecedentNUMBERS){
				if(!antecNum.equals(NumberENUM.UNKNOWN) && !coreCorefNUMBERS.contains(antecNum)) {
					hasExtraAnt = true;
					break;
				}
			}
		}
		if(!antecedentNUMBERS.contains(NumberENUM.UNKNOWN)){
			for(NumberENUM coreNum : coreCorefNUMBERS){
				if(!coreNum.equals(NumberENUM.UNKNOWN) && !antecedentNUMBERS.contains(coreNum)) {
					hasExtraThis = true;
					break;
				}
			}
		}

		// Doesn't match if the coreferent (or the antecedent) has no UNKNOWN values and the other has some non UNKNOWN that doesn't match
		if(hasExtraAnt || hasExtraThis) return false; // ORIGINAL: hasExtraAnt && hasExtraThis

		return true;
	}

	public static boolean checkGenderConcordance(Annotation candidateCorefMentionAnn, Annotation potentialAntecedentAnn, CorefChainBuilder corefBuilder) {
		// Get clusters of both mentions
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(candidateCorefMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(potentialAntecedentAnn.getId());

		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}


		boolean hasExtraAnt = false;
		boolean hasExtraThis = false;

		// ******************************
		// ******************************
		// Gender

		Set<GenderENUM> coreCorefGENDERS = new HashSet<GenderENUM>();
		for(Integer coreAnnId : coreCoreferenceChain) {
			// Get coreAnn from ID
			Annotation coreAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(coreAnnId);
			if(coreAnn == null) continue;
			
			GenderENUM gender = getGenderDetails(coreAnn, corefBuilder.getDocument());
			if(gender != null) coreCorefGENDERS.add(gender);
		}

		Set<GenderENUM> antecedentGENDERS = new HashSet<GenderENUM>();
		for(Integer antecAnnId : antecedentCoreferenceChain) {
			// Get antecAnn from ID
			Annotation antecAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(antecAnnId);
			if(antecAnn == null) continue;
			
			GenderENUM gender = getGenderDetails(antecAnn, corefBuilder.getDocument());
			if(gender != null) antecedentGENDERS.add(gender);
		}

		if(!coreCorefGENDERS.contains(GenderENUM.UNKNOWN)){
			for(GenderENUM genderAntec : antecedentGENDERS){
				if(!genderAntec.equals(GenderENUM.UNKNOWN) && !coreCorefGENDERS.contains(genderAntec)) {
					hasExtraAnt = true;
					break;
				}
			}
		}
		if(!antecedentGENDERS.contains(GenderENUM.UNKNOWN)){
			for(GenderENUM genderCore : coreCorefGENDERS){
				if(!genderCore.equals(GenderENUM.UNKNOWN)&& !antecedentGENDERS.contains(genderCore)) {
					hasExtraThis = true;
					break;
				}
			}
		}

		// Doesn't match if the coreferent (or the antecedent) has no UNKNOWN values and the other has some non UNKNOWN that doesn't match
		if(hasExtraAnt || hasExtraThis) return false; // ORIGINAL: hasExtraAnt && hasExtraThis

		return true;
	}

	public static boolean checkAnimacyConcordance(Annotation candidateCorefMentionAnn, Annotation potentialAntecedentAnn, CorefChainBuilder corefBuilder) {
		// Get clusters of both mentions
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(candidateCorefMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(potentialAntecedentAnn.getId());

		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}


		boolean hasExtraAnt = false;
		boolean hasExtraThis = false;

		// ******************************
		// ******************************
		// Animacy

		Set<AnimacyENUM> coreCorefANIMACY = new HashSet<AnimacyENUM>();
		for(Integer coreAnnId : coreCoreferenceChain) {
			// Get coreAnn from ID
			Annotation coreAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(coreAnnId);
			if(coreAnn == null) continue;

			AnimacyENUM animacy = getAnimacyDetails(coreAnn, corefBuilder.getDocument());
			if(animacy != null) coreCorefANIMACY.add(animacy);
		}

		Set<AnimacyENUM> antecedentANIMACY = new HashSet<AnimacyENUM>();
		for(Integer antecAnnId : antecedentCoreferenceChain) {
			// Get antecAnn from ID
			Annotation antecAnn = corefBuilder.getDocument().getAnnotations(ImporterBase.driAnnSet).get(antecAnnId);
			if(antecAnn == null) continue;

			AnimacyENUM animacy = getAnimacyDetails(antecAnn, corefBuilder.getDocument());
			if(animacy != null) antecedentANIMACY.add(animacy);
		}

		if(!coreCorefANIMACY.contains(AnimacyENUM.UNKNOWN)){
			for(AnimacyENUM antecAnim : antecedentANIMACY){
				if(!antecAnim.equals(AnimacyENUM.UNKNOWN) && !coreCorefANIMACY.contains(antecAnim)) {
					hasExtraAnt = true;
					break;
				}
			}
		}
		if(!antecedentANIMACY.contains(AnimacyENUM.UNKNOWN)){
			for(AnimacyENUM coreAnim : coreCorefANIMACY){
				if(!coreAnim.equals(AnimacyENUM.UNKNOWN) && !antecedentANIMACY.contains(coreAnim)) {
					hasExtraThis = true;
					break;
				}
			}
		}

		// Doesn't match if the coreferent (or the antecedent) has no UNKNOWN values and the other has some non UNKNOWN that doesn't match
		if(hasExtraAnt || hasExtraThis) return false; // ORIGINAL: hasExtraAnt && hasExtraThis

		return true;
	}
	
	public static boolean checkNERconcordance(Annotation candidateCorefMentionAnn, Annotation potentialAntecedentAnn, CorefChainBuilder corefBuilder) {
		// Get clusters of both mentions
		Set<Integer> coreCoreferenceChain = corefBuilder.getCorefChainMap().get(candidateCorefMentionAnn.getId());
		Set<Integer> antecedentCoreferenceChain = corefBuilder.getCorefChainMap().get(potentialAntecedentAnn.getId());

		if(coreCoreferenceChain == null || antecedentCoreferenceChain == null) {
			return false;
		}


		// boolean hasExtraAnt = false;
		// boolean hasExtraThis = false;

		// ******************************
		// ******************************
		// NER
		
		// TODO: NOT CHECKED

		return true;
	}

}
