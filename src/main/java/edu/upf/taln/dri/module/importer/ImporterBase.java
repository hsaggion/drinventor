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
package edu.upf.taln.dri.module.importer;

import edu.upf.taln.dri.module.DRIModule;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

/**
 * Base GATE processing resource to build importers from different data sources (PDF, XML schemas, etc.)
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Base importer")
public abstract class ImporterBase extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static final long serialVersionUID = 1L;
	
	// Unified paper annotation names (GATE)
	public static final String driAnnSet = "Analysis";
	
	public static final String titleAnnType = "Title";
	public static final String headerAnnType = "Head";
	public static final String headerAuthorAnnType = "Head-author";
	public static final String headerAffilAnnType = "Head-affiliation";
	public static final String emailAnnType = "Email";
	public static final String abstractAnnType = "Abstract";
	public static final String figureAnnType = "Figure";
	public static final String tableAnnType = "Table";
	public static final String captionAnnType = "Caption";
	public static final String sentenceAnnType = "Sentence";
	public static final String sentence_POSpatternFeat = "POSpattern";
	public static final String sentence_RhetoricalAnnFeat = "rhetorical_class";
	public static final String sentence_isAcknowledgement = "ackSentence";
	public static final String sentence_lexRankScore = "lexRank_score";
	public static final String sentence_titleSimScore = "titleSim_score";
	public static final String langAnnFeat = "aLang";
	public static final String tokenAnnType = "Token";
	public static final String token_POSfeat = "category";
	public static final String token_LemmaFeat = "lemma";
	public static final String bibEntryAnnType = "BibEntry";
	public static final String bibEntry_IdAnnFeat = "bibEntryID";
	public static final String h1AnnType = "H1";
	public static final String h2AnnType = "H2";
	public static final String h3AnnType = "H3";
	public static final String h4AnnType = "H4";
	public static final String h5AnnType = "H5";
	public static final String inlineCitationAnnType = "CitSpan";
	public static final String inlineCitationMarkerAnnType = "CitMarker";
	public static final String outlayerAnnType = "Outlayer";
	
	public static final String coref_SpotAnnSet = "CorefSpot";
	public static final String coref_ChainAnnSet = "CorefChains";
	public static final String coref_Candidate = "CorefMention";
	public static final String coref_Coreference = "CorefChain";
	
	public static final String babelnet_AnnSet = "Babelnet";
	public static final String babelnet_DisItem = "Entity";
	public static final String babelnet_DisItem_babelnetURLfeat = "babelnetURL";
	public static final String babelnet_DisItem_synsetIDfeat = "synsetID";
	public static final String babelnet_DisItem_dbpediaURLfeat = "dbpediaURL";
	public static final String babelnet_DisItem_golbalScoreFeat = "globalScore";
	public static final String babelnet_DisItem_coherenceScoreFeat = "coherenceScore";
	public static final String babelnet_DisItem_scoreFeat = "score";
	public static final String babelnet_DisItem_sourceFeat = "source";
	public static final String babelnet_DisItem_numTokensFeat = "numTokens";
	
	public static final String term_AnnSet = "Terminology";
	public static final String term_CandOcc = "Term";
	public static final String term_CandOcc_actualPOSFeat = "matchedPattern";
	public static final String term_CandOcc_regexPOSFeat = "regexpPattern";
	
	public static final String headerDOC_OrigDocFeat = "HeaderDoc";
	public static final String headerDOC_AnnSet = "HeaderAnalysis";
	public static final String headerDOC_Sentence = "Sentence";
	public static final String headerDOC_Token = "Token";
	public static final String headerDOC_Lookup = "Lookup";
	public static final String headerDOC_JAPEemail = "emailAddress";
	public static final String headerDOC_Author = "Author";
	public static final String headerDOC_Affiliation = "Author";
	
	public static final String causality_AnnSet = "Causality";
	
	public static final String metaAnnotator_AnnSet = "JAPE_RULES";
	public static final String metaAnnotator_LookupAnnType = "Lookup";
	public static final String metaAnnotator_ProjectAnnType = "Project";
	public static final String metaAnnotator_FundingAgencyAnnType = "FundingBody";
	public static final String metaAnnotator_OntologyAnnType = "Ontology";
	
	private String inputASname;
	private String outputASname;

	
	public String getInputASname() {
		return inputASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Original markups", comment = "The name of the input annotation set where to retireve annotations to import")
	public void setInputASname(String inputASname) {
		this.inputASname = inputASname;
	}

	public String getOutputASname() {
		return outputASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "", comment = "The name of the output annotation set where to store imported annotations")
	public void setOutputASname(String outputASname) {
		this.outputASname = outputASname;
	}
	
}
