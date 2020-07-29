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
 * Interface to access the tokens of a sentence of a document together with their descriptive features.
 * 
 *
 */
public interface Token {
	
	/**
	 * The document unambiguous ID of the token
	 * 
	 * @return
	 */
	public Integer getId();
	
	/**
	 * The position of the token inside the sentence
	 * 
	 * @return
	 */
	public Integer getInSentencePosition();
	
	/**
	 * The document-wide unambiguous id that references the {@link edu.upf.taln.dri.lib.model.ext.Sentence Sentence} of the document 
	 * that includes the citation marker 
	 * 
	 * @return
	 */
	public Integer getContainingSentence();

	/**
	 * The token text
	 * 
	 * @return
	 */
	public String getWord();

	/**
	 * The token lemma
	 * 
	 * @return
	 */
	public String getLemma();

	/**
	 * The token Part-Of-Speech
	 * @return
	 */
	public String getPOS();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
