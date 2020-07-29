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
package edu.upf.taln.dri.module.rhetclassifier.feats;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.AnnotationPositionInDocumentC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.AnnotationPositionInSectionC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.AnnotationTextC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.AnnotatorAgreementC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.AnnotatorAgreementC.AnnotationTypeENUM;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.ContainSubjectivityCueC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.ContainTextInListC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.ContainWordsInListC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_CITSprevNextSentencesC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_CITSprevNextSentencesC.REF_SENT_AND_TYPE;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_ContainTextInList_AL_negC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_PassiveModalTenseC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_PassiveModalTenseC.PASSIVE_MODAL_TENSE;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_RelativePositionOfFirstMatchOfAnnotationNominalC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_RelativePositionOfFirstMatchOfWordsInListC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_SentLengthNominalC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_TFIDFsimilarityC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DERIV_TFIDFsimilarityC.TYPE_SIM;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentIdentifierC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentSectionIdentifierC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentSectionIdentifierC.SectionTypeENUM;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentSectionPositionC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentSectionPositionC.SectionPositionENUM;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.DocumentSentenceIdentifierC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.FeatureValueOfFirstFilteredMatchC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.IntersectingAnnotationBooleanCountC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.IntersectingAnnotationStringCountC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.IntersectingAnnotationTextC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.IntersectingGroupsOfCitationCountC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.NgramsC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.NumberOfDependencyRelationsByTypeC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.RelativePositionOfFirstMatchOfAnnotationC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.RelativePositionOfFirstMatchOfWordsInListC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.RhetoricalClassGetterC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.SectionNumberC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.SectionTypeC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.SkipgramsC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.StaticLists;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.TokenFromDependencyRelationsC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.TreeMaxDepthOfDependencyRelationsC;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.formulaic.ActionLexicon;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.formulaic.ConceptLexicon;
import edu.upf.taln.ml.feat.FeatureSet;
import edu.upf.taln.ml.feat.NominalW;
import edu.upf.taln.ml.feat.NumericW;
import edu.upf.taln.ml.feat.StringW;
import edu.upf.taln.ml.feat.exception.FeatureException;
import gate.Annotation;


/**
 * 
 *
 */
public class FeatG {

	private static Logger logger = Logger.getLogger(FeatG.class.getName());

	public static boolean only5categories = true;

	public static boolean includeAbstractSentences = true;
	public static boolean includeContentSentences = true;

	// Annotation names
	public static String inputAS = "Analysis";
	public static String inputFreelingAS = "fl";
	public static String inputTokenAN = "Token";
	public static String inputSentenceAN = "Sentence";
	public static String inputInlineCitationAN = "CitSpan";
	public static String inputInlineCitationMarkerAN = "CitMarker";

	// Set of rhetorical features (NOMINAL class)
	public static Set<String> rhetCatValues = new HashSet<String>();
	public static Set<String> sectTypeValues = new HashSet<String>();

	private static void initVariables() {
		rhetCatValues.add("DRI_Background");
		rhetCatValues.add("DRI_Outcome");
		rhetCatValues.add("DRI_Challenge");
		rhetCatValues.add("DRI_Approach");
		rhetCatValues.add("DRI_Unspecified");
		rhetCatValues.add("DRI_FutureWork");
		rhetCatValues.add("Sentence");
		// Cat filter - START
		// Only top level categories
		if(!only5categories) {
			rhetCatValues.add("DRI_Challenge_Hypothesis");
			rhetCatValues.add("DRI_Challenge_Goal");
			rhetCatValues.add("DRI_Outcome_Contribution");
		}
		// Cat filter - END
		
		sectTypeValues.add("ABSTRACT");
		sectTypeValues.add("INTRO");
		sectTypeValues.add("BACKGROUND");
		sectTypeValues.add("IMPLEMENTATION");
		sectTypeValues.add("METHOD_ALGORITHM");
		sectTypeValues.add("RESULT_EVALUTAITON");
		sectTypeValues.add("EXPERIMENT");
		sectTypeValues.add("DISCUSSION_CONCLUSION_FUTURE_WORK");
		sectTypeValues.add("MODEL_DESCRIPTION");
		sectTypeValues.add("ACKNOLWEDGMENT");
		sectTypeValues.add("REFERENCES");
		sectTypeValues.add("NO_SECTION_TYPE");
	}


	public static void main(String[] args) throws Exception {
		// Do nothing
	}

	public static FeatureSet<Annotation, DocumentCtx> generateFeatSet() {

		initVariables();

		// Feature schema header
		// The object is a citing sentence annotation and the context is the document the sentence belongs to
		FeatureSet<Annotation, DocumentCtx> featSet = new FeatureSet<Annotation, DocumentCtx>();
		try {

			// Adding document identifier
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("DOC_ID", new DocumentIdentifierC()));

			// Adding document sentence identifier
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SENT_ID", new DocumentSentenceIdentifierC()));

			// Adding document section identifier, ordinal number, percentage and title
			// SECTION_ID: SECT_" + sectionOrderNumber + "__" + sectionName --> sectionOrderNumber: from 1, incremented by 1 every time a new section header is found - only h1 headers considered
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SECTION_ID", new DocumentSectionIdentifierC(SectionTypeENUM.ID)));
			// SECTION_TITLE: sectionName
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SECTION_TITLE", new DocumentSectionIdentifierC(SectionTypeENUM.TITLE)));
			// From 1, incremented by 1 every time a new section header is found - only h1 headers considered
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SECTION_ORDER_NUM", new DocumentSectionPositionC(SectionPositionENUM.ORDER_NUM)));
			// Percentage rounded to 2 decimals of (current_section / total_sect_number) - if the paper has no section always 50% is returned
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SECTION_ORDER_PERC", new DocumentSectionPositionC(SectionPositionENUM.ORDER_PERC)));

			// Adding agreement level for each annotation type
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("AGREEMENT_RHET", new AnnotatorAgreementC(AnnotationTypeENUM.RHET)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("AGREEMENT_ASPECT", new AnnotatorAgreementC(AnnotationTypeENUM.ASPECT)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("AGREEMENT_MAIN_CIT", new AnnotatorAgreementC(AnnotationTypeENUM.MAIN_CIT)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("AGREEMENT_SECONDARY_CIT", new AnnotatorAgreementC(AnnotationTypeENUM.SECONDARY_CIT)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("AGREEMENT_SUMMARY", new AnnotatorAgreementC(AnnotationTypeENUM.SUMMARY)));

			// Adding full original sentence text without any processing of its contents
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_rawSentence", new AnnotationTextC()));

			// Adding all the annotated texts of Tokens of the inputAS annotation set
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitToken", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "", "", false, "", false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitTokenNoPunctNoStopWords", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "kind", "word", false, "", true, true)));

			// Add all the annotated texts Tokens of the inputAS annotation set having an annotation of type category starting with "VB" / "N"
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitToken_Verb", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "category", "VB", true, "", true, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitToken_Noun", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "category", "N", true, "", true, true)));

			// Add the value of the lemma feature of Tokens of kind equal to word of the inputAS annotation set
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitLemma", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "kind", "word", false, "lemma", true, true)));

			// Add the value of the lemma feature of Tokens of the inputAS annotation set having an annotation of type category starting with "VB" / "N"
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitLemma_Verb", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "category", "VB", true, "lemma", true, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TEXT_spaceSplitLemma_Noun", new IntersectingAnnotationTextC(inputAS, inputTokenAN, "category", "N", true, "lemma", true, true)));


			// Bigrams, trigrams and skipgrams of tokens, considering the value of string feature of all token with kind = word
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("BIGRAM_token", new NgramsC(inputAS, inputTokenAN, "string", "kind", "word", false, 1, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TRIGRAM_token", new NgramsC(inputAS, inputTokenAN, "string", "kind", "word", false, 2, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_1_token", new SkipgramsC(inputAS, inputTokenAN, "string", "kind", "word", false, 1, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_2_token", new SkipgramsC(inputAS, inputTokenAN, "string", "kind", "word", false, 2, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_3_token", new SkipgramsC(inputAS, inputTokenAN, "string", "kind", "word", false, 3, false, true)));

			// Bigrams and skipgrams of lemmas
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("BIGRAM_lemma", new NgramsC(inputAS, inputTokenAN, "lemma", "kind", "word", false, 1, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("TRIGRAM_lemma", new NgramsC(inputAS, inputTokenAN, "lemma", "kind", "word", false, 2, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_1_lemma", new SkipgramsC(inputAS, inputTokenAN, "lemma", "kind", "word", false, 1, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_2_lemma", new SkipgramsC(inputAS, inputTokenAN, "lemma", "kind", "word", false, 2, false, true)));
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("SKIPGRAM_3_lemma", new SkipgramsC(inputAS, inputTokenAN, "lemma", "kind", "word", false, 3, false, true)));

			// Synsets
			/* NO FREELING ANALYSIS IS PERFORMED
					featSet.addFeature(new StringW<Annotation, DocumentCtx>("WN_synsetID_all", new IntersectingAnnotationTextC(inputFreelingAS, inputTokenAN, null, null, true, "senseID", false, true)));

					featSet.addFeature(new StringW<Annotation, DocumentCtx>("WN_synsetID_Adjective", new IntersectingAnnotationTextC(inputFreelingAS, inputTokenAN, "morpho", "J", true, "senseID", false, true)));
					featSet.addFeature(new StringW<Annotation, DocumentCtx>("WN_synsetID_Verb", new IntersectingAnnotationTextC(inputFreelingAS, inputTokenAN, "morpho", "V", true, "senseID", false, true)));
					featSet.addFeature(new StringW<Annotation, DocumentCtx>("WN_synsetID_Noun", new IntersectingAnnotationTextC(inputFreelingAS, inputTokenAN, "morpho", "N", true, "senseID", false, true)));
					featSet.addFeature(new StringW<Annotation, DocumentCtx>("WN_synsetID_Adverb", new IntersectingAnnotationTextC(inputFreelingAS, inputTokenAN, "morpho", "R", true, "senseID", false, true)));
			 */

			// Tokens of dependency relations
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("DEPREL_tokens", new TokenFromDependencyRelationsC()));

			// POS of first occurrence of VERB
			featSet.addFeature(new StringW<Annotation, DocumentCtx>("POSofFirstVerb", new FeatureValueOfFirstFilteredMatchC(inputAS, inputTokenAN, "category", "category", "V", true)));

			// Mean of positivity, negativity and objectivity scores
			/* NO FREELING ANALYSIS IS PERFORMED
					featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SENTIWN_ObjAvgWordValue", new IntersectingAnnotationStringMeanOfNumerifValuesC(inputFreelingAS, inputTokenAN, "sw_objScore", null, true, "sw_objScore")));
					featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SENTIWN_PosAvgWordValue", new IntersectingAnnotationStringMeanOfNumerifValuesC(inputFreelingAS, inputTokenAN, "sw_posScore", null, true, "sw_posScore")));
					featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SENTIWN_NegAvgWordValue", new IntersectingAnnotationStringMeanOfNumerifValuesC(inputFreelingAS, inputTokenAN, "sw_negScore", null, true, "sw_negScore")));
			 */

			// Depth and number of arks of the dependency tree
			// Percentage of dependency relations by type
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_maxDepth", new TreeMaxDepthOfDependencyRelationsC()));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_totalNumEdges", new NumberOfDependencyRelationsByTypeC(null, false, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_NMOD", new NumberOfDependencyRelationsByTypeC("NMOD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_P", new NumberOfDependencyRelationsByTypeC("P", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PMOD", new NumberOfDependencyRelationsByTypeC("PMOD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_SBJ", new NumberOfDependencyRelationsByTypeC("SBJ", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_OBJ", new NumberOfDependencyRelationsByTypeC("OBJ", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_ADV", new NumberOfDependencyRelationsByTypeC("ADV", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_NAME", new NumberOfDependencyRelationsByTypeC("NAME", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_VC", new NumberOfDependencyRelationsByTypeC("VC", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_COORD", new NumberOfDependencyRelationsByTypeC("COORD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_DEP", new NumberOfDependencyRelationsByTypeC("DEP", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_TMP", new NumberOfDependencyRelationsByTypeC("TMP", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_CONJ", new NumberOfDependencyRelationsByTypeC("CONJ", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_LOC", new NumberOfDependencyRelationsByTypeC("LOC", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_AMOD", new NumberOfDependencyRelationsByTypeC("AMOD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PRD", new NumberOfDependencyRelationsByTypeC("PRD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_APPO", new NumberOfDependencyRelationsByTypeC("APPO", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_IM", new NumberOfDependencyRelationsByTypeC("IM", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_HYPH", new NumberOfDependencyRelationsByTypeC("HYPH", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_HMOD", new NumberOfDependencyRelationsByTypeC("HMOD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_SUB", new NumberOfDependencyRelationsByTypeC("SUB", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_OPRD", new NumberOfDependencyRelationsByTypeC("OPRD", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_SUFFIX", new NumberOfDependencyRelationsByTypeC("SUFFIX", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_DIR", new NumberOfDependencyRelationsByTypeC("DIR", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_TITLE", new NumberOfDependencyRelationsByTypeC("TITLE", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_MNR", new NumberOfDependencyRelationsByTypeC("MNR", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_POSTHON", new NumberOfDependencyRelationsByTypeC("POSTHON", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PRP", new NumberOfDependencyRelationsByTypeC("PRP", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PRT", new NumberOfDependencyRelationsByTypeC("PRT", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_LGS", new NumberOfDependencyRelationsByTypeC("LGS", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_EXT", new NumberOfDependencyRelationsByTypeC("EXT", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PRN", new NumberOfDependencyRelationsByTypeC("PRN", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_EXTR", new NumberOfDependencyRelationsByTypeC("EXTR", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_DTV", new NumberOfDependencyRelationsByTypeC("DTV", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_PUT", new NumberOfDependencyRelationsByTypeC("PUT", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_BNF", new NumberOfDependencyRelationsByTypeC("BNF", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("DEPREL_numEdge_VOC", new NumberOfDependencyRelationsByTypeC("VOC", false, true)));

			// Number of tokens also included in citation spans
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("tokenNumWithInlineCit", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, null, null, false, false, false)));
			// Number of tokens not included in citation spans
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("tokenNumWithoutInlineCit", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, null, null, false, false, true)));

			// Sentence Length nominal: Short, Medium, Long with respect to be less than 20, from 20 to 40, more than 40 words
			Set<String> sentenceLengthValues = new HashSet<String>();
			sentenceLengthValues.add("Short");
			sentenceLengthValues.add("Medium");
			sentenceLengthValues.add("Long");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("sentLength_Nominal", sentenceLengthValues, new DERIV_SentLengthNominalC(inputAS, inputTokenAN, null, null, false, true)));

			// Position of the annotation with respect to the entire document
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relDocAnnPositionPercent", new AnnotationPositionInDocumentC(includeAbstractSentences, includeContentSentences, 0, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relDocAnnPosition10foldEqual", new AnnotationPositionInDocumentC(includeAbstractSentences, includeContentSentences, 10, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relDocAnnPosition20foldUnequal", new AnnotationPositionInDocumentC(includeAbstractSentences, includeContentSentences, 20, true)));

			// From 1, incremented by 1 every time a new section header is found - only h1 headers considered
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_sectionNumber_H1", new SectionNumberC(true)));
			// From 1, incremented by 1 every time a new section header is found - all headers considered
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_sectionNumber_ALL_HEADERS", new SectionNumberC(false)));

			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPositionPercent_H1", new AnnotationPositionInSectionC(0, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPosition5foldEqual_H1", new AnnotationPositionInSectionC(5, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPosition5foldUnequal_H1", new AnnotationPositionInSectionC(5, true, true)));

			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPositionPercent_ALL_HEADERS", new AnnotationPositionInSectionC(0, false, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPosition5foldEqual_ALL_HEADERS", new AnnotationPositionInSectionC(5, false, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_relSectAnnPosition5foldUnqual_ALL_HEADERS", new AnnotationPositionInSectionC(5, true, false)));

			// Number of citations
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitMarkerNum", new IntersectingAnnotationStringCountC(inputAS, inputInlineCitationMarkerAN, null, null, false, false, false)));
			// Number of citation spans with one or more citation markers in the sentence [1, 2, 3] or [Arg et al. 2000] or [ANDS2004, DJF2012, SDS2010]
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitSpanNum", new IntersectingAnnotationStringCountC(inputAS, inputInlineCitationAN, null, null, false, false, false)));

			// Marker and span in the revious and next sentence in the same section
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitMarkerNum_NextSent", new DERIV_CITSprevNextSentencesC(REF_SENT_AND_TYPE.NEXT_SENT_CIT_MARKER)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitSpanNum_NextSent", new DERIV_CITSprevNextSentencesC(REF_SENT_AND_TYPE.NEXT_SENT_CIT_SPAN)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitMarkerNum_PrevSent", new DERIV_CITSprevNextSentencesC(REF_SENT_AND_TYPE.PREVIOUS_SENT_CIT_MARKER)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_CitSpanNum_PrevSent", new DERIV_CITSprevNextSentencesC(REF_SENT_AND_TYPE.PREVIOUS_SENT_CIT_SPAN)));

			// Number of citation spans with more than one citation marker, in the sentence [1, 2, 3] or [Arg et al. 2000] or [ANDS2004, DJF2012, SDS2010]
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_SpansWithMoreCitMarkersNum", new IntersectingGroupsOfCitationCountC(inputAS, inputInlineCitationAN, inputInlineCitationMarkerAN, false)));			
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_SpansWithMoreCitMarkersBoolean", new IntersectingGroupsOfCitationCountC(inputAS, inputInlineCitationAN, inputInlineCitationMarkerAN, true)));
			// Number of syntactic citation
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_NumSyntacticCitSpans", new IntersectingAnnotationBooleanCountC(inputAS, inputInlineCitationAN, "syntactic", true)));
			// Relative position of first citation
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("CITA_RelPositionOfFirstCitSpan", new RelativePositionOfFirstMatchOfAnnotationC(inputAS, inputInlineCitationAN, null, null, true)));
			Set<String> positionsOfFirstCitSpan = new HashSet<String>();
			positionsOfFirstCitSpan.add("NoCit");
			positionsOfFirstCitSpan.add("Beginning");
			positionsOfFirstCitSpan.add("Middle");
			positionsOfFirstCitSpan.add("End");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("CITA_RelPositionOfFirstCitSpan_NOMINAL", positionsOfFirstCitSpan, new DERIV_RelativePositionOfFirstMatchOfAnnotationNominalC(inputAS, inputInlineCitationAN, null, null, true)));



			// TF-IDF cosine similarity with other sentences / title / section
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SIM_TITLE", new DERIV_TFIDFsimilarityC(TYPE_SIM.TITLE)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SIM_H1_SECT_TITLE", new DERIV_TFIDFsimilarityC(TYPE_SIM.H1)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SIM_PREV_SENT", new DERIV_TFIDFsimilarityC(TYPE_SIM.PREVIOUS_SENT)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("SIM_NEXT_SENT", new DERIV_TFIDFsimilarityC(TYPE_SIM.NEXT_SENT)));

			// POS frequency - PERCENTAGE
			// POS starting with: C D E F I J L M N P R S T U V W
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumC", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "C", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumD", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "D", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumE", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "E", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumF", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "F", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumI", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "I", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumJ", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "J", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumL", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "L", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumM", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "M", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumN", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "N", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumP", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "P", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumR", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "R", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumT", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "T", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumU", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "U", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumV", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "V", true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumW", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "W", true, true, true)));

			// Count of verb tenses - COUNT
			// List of verb tense POS: VB, VBD, VBG, VBN, VBP, VBZ
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VB", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VB", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VBD", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VBD", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VBG", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VBG", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VBN", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VBN", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VBP", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VBP", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumVerb_VBZ", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "VBZ", false, false, true)));

			// Comparative and superlative adjective presence
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumComparADJ_JJR", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "JJR", false, false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("POS_NumSuperlADJ_JJS", new IntersectingAnnotationStringCountC(inputAS, inputTokenAN, "category", "JJS", false, false, true)));

			Set<String> passive = new HashSet<String>();
			passive.add("NoVerb");
			passive.add("Passive");
			passive.add("Active");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("ROOTverb_PASSIVE_DERIV", passive, new DERIV_PassiveModalTenseC(inputAS, inputTokenAN, PASSIVE_MODAL_TENSE.PASSIVE)));

			Set<String> modal = new HashSet<String>();
			modal.add("NoVerb");
			modal.add("Modal");
			modal.add("NotModal");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("ROOTverb_MODAL_DERIV", modal, new DERIV_PassiveModalTenseC(inputAS, inputTokenAN, PASSIVE_MODAL_TENSE.MODAL)));

			Set<String> tense = new HashSet<String>();
			tense.add("NoVerb");
			tense.add("Present");
			tense.add("Past");
			tense.add("Future");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("ROOTverb_TENSE_DERIV", tense, new DERIV_PassiveModalTenseC(inputAS, inputTokenAN, PASSIVE_MODAL_TENSE.TENSE)));

			// First person pronouns boolean and counter - from: http://en.wikipedia.org/wiki/English_personal_pronouns
			// List: I,	me,	my,	mine, myself, we, us, our, ours, ourselves
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstFirstPersonPronoun", new RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfFirstPersonPronouns,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));
			Set<String> positionsOfFirstPronoun = new HashSet<String>();
			positionsOfFirstPronoun.add("None");
			positionsOfFirstPronoun.add("Beginning");
			positionsOfFirstPronoun.add("Middle");
			positionsOfFirstPronoun.add("End");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstFirstPersonPronoun_NOMINAL", positionsOfFirstPronoun, new DERIV_RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfFirstPersonPronouns,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_FirstPersonPronounsBoolean", new ContainWordsInListC(StaticLists.listOfFirstPersonPronouns,
					true, true, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_FirstPersonPronounsCounter", new ContainWordsInListC(StaticLists.listOfFirstPersonPronouns,
					true, false, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));

			// Third person pronouns boolean and counter - from: http://en.wikipedia.org/wiki/English_personal_pronouns
			// List: 
			// MASCULINE: he, him, his, himself, they, them, their, theirs, themselves, 
			// FEMININE: she, her, hers, herself, 
			// NON SPECIFIC: they, them, their, theirs, themself
			// NEUTER - EXCLUDED: it, its, itself
			// Relative sentence position of first match of third person pronoun in the sentence
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstThirdPersonPronoun", new RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfThirdPersonPronouns,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));
			Set<String> positionsOfThirdPronoun = new HashSet<String>();
			positionsOfThirdPronoun.add("None");
			positionsOfThirdPronoun.add("Beginning");
			positionsOfThirdPronoun.add("Middle");
			positionsOfThirdPronoun.add("End");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstThirdPersonPronoun_NOMINAL", positionsOfThirdPronoun, new DERIV_RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfThirdPersonPronouns,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));

			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ThirdPersonPronounsBoolean", new ContainWordsInListC(StaticLists.listOfThirdPersonPronouns,
					true, true, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ThirdPersonPronounsCounter", new ContainWordsInListC(StaticLists.listOfThirdPersonPronouns,
					true, false, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));

			// Relative sentence position of first match of determiner in the sentence
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstDeterminer", new RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfDeterminers,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));
			Set<String> positionsOfDeterminer = new HashSet<String>();
			positionsOfDeterminer.add("None");
			positionsOfDeterminer.add("Beginning");
			positionsOfDeterminer.add("Middle");
			positionsOfDeterminer.add("End");
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("OFFSET_RelSentPositionOfFirstDeterminer_NOMINAL", positionsOfDeterminer, new DERIV_RelativePositionOfFirstMatchOfWordsInListC(StaticLists.listOfThirdPersonPronouns,
					true, inputAS, inputTokenAN, "lemma", "kind", "word", false)));

			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_DeterminerBoolean", new ContainWordsInListC(StaticLists.listOfDeterminers,
					true, true, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_DeterminerCounter", new ContainWordsInListC(StaticLists.listOfDeterminers,
					true, false, inputAS, inputTokenAN, "lemma", "kind", "word", false, true)));


			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ContraryExpressionCounter", new ContainTextInListC(StaticLists.listOfContraryExpressions,
					true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ContraryExpressionBoolean", new ContainTextInListC(StaticLists.listOfContraryExpressions,
					true, true)));

			// From: http://delivery.acm.org/10.1145/2340000/2330739/coli_a_00126.pdf?ip=193.145.48.8&id=2330739&acc=OPEN&key=DD1EC5BCF38B3699%2EBD9BF0B02D94E6D5%2E4D4702B0C3E38B35%2E6D218144511F3437&CFID=449338353&CFTOKEN=82241200&__acm__=1415879980_d1cc62df2b034f17e3ce7d620752d12f
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_SpeculationCueCounter", new ContainTextInListC(StaticLists.listOfSpeculationCues,
					true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_SpeculationCueBoolean", new ContainTextInListC(StaticLists.listOfSpeculationCues,
					true, true)));

			// Return the sum of subjective words present in the sentence
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_SubjectivityCueCounter", new ContainSubjectivityCueC(false,
					inputAS, inputTokenAN,
					"lemma", "category",
					"kind", "word", false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_SubjectivitCueBoolean", new ContainSubjectivityCueC(true,
					inputAS, inputTokenAN,
					"lemma", "category",
					"kind", "word", false)));


			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ConjunctiveAdverbCounter", new ContainTextInListC(StaticLists.listOfConjunctiveAdverbs,
					true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_ConjunctiveAdverbBoolean", new ContainTextInListC(StaticLists.listOfConjunctiveAdverbs,
					true, true)));

			// Teufel: Concept Lexicon (CL)
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_NegationExpressionCounter", new ContainTextInListC(ConceptLexicon.coreMap.get("NEGATION"),
					true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_NegationExpressionBoolean", new ContainTextInListC(ConceptLexicon.coreMap.get("NEGATION"),
					true, true)));


			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_COMPARISON_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("COMPARISON_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_FUTURE_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("FUTURE_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_INTEREST_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("INTEREST_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_QUESTION_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("QUESTION_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_AWARE_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("AWARE_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_ARGUMENTATION_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("ARGUMENTATION_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_SIMILAR_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("SIMILAR_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_EARLIER_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("EARLIER_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_RESEARCH_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("RESEARCH_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_NEED_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("NEED_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_REFERENTIALExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("REFERENTIAL"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_QUESTIONExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("QUESTION"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_WORK_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("WORK_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_CHANGE_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("CHANGE_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_DISCIPLINEExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("DISCIPLINE"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_GIVENExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("GIVEN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_BAD_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("BAD_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_CONTRAST_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("CONTRAST_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_NEED_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("NEED_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_AIM_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("AIM_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_CONTRAST_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("CONTRAST_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_SOLUTION_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("SOLUTION_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_TRADITION_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("TRADITION_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_PROFESSIONALSExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("PROFESSIONALS"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_PROBLEM_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("PROBLEM_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_TEXT_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("TEXT_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_PROBLEM_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("PROBLEM_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_TRADITION_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("TRADITION_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_PRESENTATION_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("PRESENTATION_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_RESEARCH_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("RESEARCH_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_MAIN_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("MAIN_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_REFLEXIVEExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("REFLEXIVE"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_NED_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("NED_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_MANYExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("MANY"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_COMPARISON_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("COMPARISON_NOUN"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_GOOD_ADJExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("GOOD_ADJ"),
					true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_CL_CHANGE_NOUNExpressionBoolean_TEUFEL", new ContainTextInListC(ConceptLexicon.coreMap.get("CHANGE_NOUN"),
					true, true)));

			// Teufel: Action Lexicon (AL)
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_RESEARCHExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("RESEARCH"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_ARGUMENTATIONExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("ARGUMENTATION"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_AWAREExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("AWARE"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_USEExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("USE"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_PROBLEMExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("PROBLEM"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_SOLUTIONExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("SOLUTION"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_BETTER_SOLUTIONExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("BETTER_SOLUTION"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_TEXTSTRUCTUREExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("TEXTSTRUCTURE"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_INTERESTExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("INTEREST"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CONTINUEExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CONTINUE"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_FUTURE_INTERESTExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("FUTURE_INTEREST"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_NEEDExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("NEED"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_AFFECTExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("AFFECT"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_PRESENTATIONExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("PRESENTATION"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CONTRASTExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CONTRAST"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CHANGEExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CHANGE"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_COMPARISONExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("COMPARISON"),
					true, true, true)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_SIMILARExpressionBoolean_NEG_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("SIMILAR"),
					true, true, true)));

			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_RESEARCHExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("RESEARCH"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_ARGUMENTATIONExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("ARGUMENTATION"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_AWAREExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("AWARE"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_USEExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("USE"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_PROBLEMExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("PROBLEM"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_SOLUTIONExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("SOLUTION"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_BETTER_SOLUTIONExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("BETTER_SOLUTION"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_TEXTSTRUCTUREExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("TEXTSTRUCTURE"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_INTERESTExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("INTEREST"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CONTINUEExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CONTINUE"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_FUTURE_INTERESTExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("FUTURE_INTEREST"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_NEEDExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("NEED"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_AFFECTExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("AFFECT"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_PRESENTATIONExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("PRESENTATION"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CONTRASTExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CONTRAST"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_CHANGEExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("CHANGE"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_COMPARISONExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("COMPARISON"),
					true, true, false)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("MARKER_AL_SIMILARExpressionBoolean_TEUFEL", new DERIV_ContainTextInList_AL_negC(ActionLexicon.coreMap.get("SIMILAR"),
					true, true, false)));

			// Section type (Nominal)
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("sectionType", sectTypeValues, new SectionTypeC()));

			// Rhethorical class probabilities - return null if the sentence has not been tagged with a rhetorical class
			/*
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_Approach", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_Approach)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_Background", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_Background)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_Challenge", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_Challenge)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_FutureWork", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_FutureWork)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_Outcome", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_Outcome)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_DRI_Unspecified", new RhetoricalClassProbabilityC(RhethoricalClassENUM.DRI_Unspecified)));
			featSet.addFeature(new NumericW<Annotation, DocumentCtx>("prob_Sentence", new RhetoricalClassProbabilityC(RhethoricalClassENUM.Sentence)));
			*/

			// Adding previous class value (MEM)
			/*
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_sectBreak_1", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(1, true)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_sectBreak_2", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(2, true)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_sectBreak_3", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(3, true)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_sectBreak_4", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(4, true)));

			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_noBreak_1", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(1, false)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_noBreak_2", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(2, false)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_noBreak_3", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(3, false)));
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("prevClass_noBreak_4", rhetCatValuesPrevClass, new MEM_PreviousClassesValueC(4, false)));
			*/

			/* SENTENCE VECTOR DISABLED
			featSet.addFeature(new NumericArrayW<Annotation, DocumentCtx>("sentenceVect", 300, new SentenceVectorFeatureC()));
			 */

			// Adding classes (as last features)
			featSet.addFeature(new NominalW<Annotation, DocumentCtx>("Class_rhetorical", rhetCatValues, new RhetoricalClassGetterC()));
			

		} catch (FeatureException e1) {
			e1.printStackTrace();
		}

		return featSet;
	}

}
