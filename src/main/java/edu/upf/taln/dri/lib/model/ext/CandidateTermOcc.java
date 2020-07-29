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


/**
 * Interface to access a candidate term mined from the document.
 * 
 *
 */
public interface CandidateTermOcc {
	
	/**
	 * Document-wide unambiguous candidate term occurrence Id
	 * 
	 * @return document-wide unambiguous term Id
	 */
	public Integer getId();
	
	/**
	 * The candidate term occurrence text
	 * 
	 * @return term text
	 */
	public String getText();
	
	/**
	 * Get the id of the sentence that includes the candidate term occurrence
	 * @return
	 */
	public Integer getSentenceIdWithTerm();
	
	/**
	 * The POS pattern regular expression used to spot this candidate term occurrence
	 * 
	 * @return
	 */
	public String getRegexPattern();
	
	/**
	 * The actual POS pattern that characterizes the candidate term occurrence
	 * 
	 * @return
	 */
	public String getMatchedPattern();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
