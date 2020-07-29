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
package edu.upf.taln.dri.lib.model.ext;

import java.util.List;
import java.util.Map;

/**
 * Interface to access the sentence of a document together with its descriptive features.
 * 
 *
 */
public interface Sentence {
	
	/**
	 * Document-wide unambiguous sentence Id
	 * @return
	 */
	public Integer getId();
	
	/**
	 * Return the ID of the abstract the sentence belongs to.
	 * For papers having only one abstract, if the sentence is included in the abstract, its abstract ID is equal to 1.
	 * For papers having more than one abstract (one per language for instance), if the sentence is included in the
	 * first abstract, its abstract ID is equal to 1, otherwise its abstract ID is equal to 2.
	 * Sentences that are not part of the abstract of the paper have the abstract ID value set to -1.
	 * 
	 * @return
	 */
	public Integer getAbstractId();
	
	/**
	 * Text of the sentence
	 * @return
	 */
	public String getText();

	
	/**
	 * Rhetorical class of the sentence
	 * @return
	 */
	public RhetoricalClassENUM getRhetoricalClass();
	
	/**
	 * Containing section
	 * @return
	 */
	public Section getContainingSection();
	
	/**
	 * List of citation markers included in the sentence
	 * @return
	 */
	public List<CitationMarker> getCitationMarkers();
	
	
	/**
	 * List of candidate terms included in the sentence
	 * 
	 * @return
	 */
	public List<CandidateTermOcc> getCandidateTerms();
	
	/**
	 * List of candidate Babelnet synset occurrences spotted in the sentence
	 * 
	 * @return
	 */
	public List<BabelSynsetOcc> getBabelSynsetsOcc();
	
	/**
	 * List of tokens included in the sentence
	 * 
	 * @return
	 */
	public List<Token> getTokens();
	
	
	/**
	 * Get the language of the sentence
	 * 
	 * @return
	 */
	public LangENUM getLanguage();
	
	
	/**
	 * Check if a sentence is an acknowledgment
	 * 
	 * @return
	 */
	public boolean isAcknowledgment();
	
	/**
	 * It the sentence is an acknowledgment
	 * (in the current version only acknowledgment sentences may contain spotted entities)
	 * 
	 * @return
	 */
	public Map<String, MetaEntityTypeENUM> getSpottedEntities();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
	
}
