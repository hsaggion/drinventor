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

/**
 * Section of a paper
 * 
 *
 */
public interface Section {
	
	/**
	 * Document-wide unambiguous section Id
	 * @return
	 */
	public Integer getId();
	
	/**
	 * Text of the section header
	 * @return
	 */
	public String getName();
	
	/**
	 * Text of the section level
	 * @return
	 */
	public Integer getLevel();
	
	/**
	 * Get the parent section
	 * @return
	 */
	public Section getParentSection();
	
	/**
	 * Get subsections
	 * @return
	 */
	public List<Section> getSubsections();
	
	/**
	 * Get the ordered list of sentences inside the section
	 * @return
	 */
	public List<Sentence> getSentences();
	
	/**
	 * Get the language of the section title
	 * 
	 * @return
	 */
	public LangENUM getLanguage();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
	
}
