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
 * Interface to access an citation marker of a paper, that is an inline references to a bibliography citation
 * 
 *
 */
public interface CitationMarker {

	/**
	 * Document-wide unambiguous citation marker Id
	 * @return document-wide unambiguous term Id
	 */
	public Integer getId();
	
	/**
	 * The document-wide unambiguous id that references the corresponding {@link edu.upf.taln.dri.lib.model.ext.Citation Citation} element
	 * @return null if this citation marker han not been associated to any bibliography citation entry
	 */
	public Integer getCitationId();
	
	/**
	 * The document-wide unambiguous id that references the {@link edu.upf.taln.dri.lib.model.ext.Sentence Sentence} of the document 
	 * that includes the citation marker 
	 * @return
	 */
	public Integer getSentenceId();
	
	/**
	 * The text of the citation marker
	 * @return
	 */
	public String getReferenceText();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
