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
 * Interface to access the metadata of the header of the paper.
 * 
 *
 */
public interface Header {
	
	/**
	 * The plain text of the paper header
	 * @return
	 */
	public String getPlainText();
	
	/**
	 * The titles of the paper (each one with its own language)
	 * Useful in case of papers with multiple titles in multiple languages.
	 * 
	 * @return
	 */
	public Map<LangENUM, String> getTitles();
	
	/**
	 * Get the language specific list of keywords
	 * @return
	 */
	public Map<LangENUM, List<String>> getKeywords();
	
	/**
	 * The authors of the paper
	 * @return
	 */
	public List<Author> getAuthorList();
	
	/**
	 * The year of the paper
	 * @return
	 */
	public String getYear();
	
	/**
	 * The pages
	 * @return
	 */
	public String getPages();
	
	/**
	 * The first page
	 * @return
	 */
	public String getFirstPage();
	
	/**
	 * The last page
	 * @return
	 */
	public String getLastPage();
	
	/**
	 * The open URL of the paper
	 * @return
	 */
	public String getOpenURL();
	
	/**
	 * The specific ID of the paper, if any
	 * @return
	 */
	public String getPubID(PubIdENUM pubIDtype);
	
	/**
	 * The bibsonomy URL of the paper
	 * @return
	 */
	public String getBibsonomyURL();
	
	/**
	 * The chapter of the paper
	 * @return
	 */
	public String getChapter();
	
	/**
	 * The volume of the paper
	 * @return
	 */
	public String getVolume();
	
	/**
	 * The issue of the paper
	 * @return
	 */
	public String getIssue();
	
	/**
	 * The series of the paper
	 * @return
	 */
	public String getSeries();
	
	/**
	 * The publisher of the paper
	 * @return
	 */
	public String getPublisher();
	
	/**
	 * The location of the publisher of the paper
	 * @return
	 */
	public String getPublisherLoc();
	
	/**
	 * The journal of the paper
	 * @return
	 */
	public String getJournal();
	
	/**
	 * The edition of the paper
	 * @return
	 */
	public String getEdition();
	
	/**
	 * The list of editors of the paper
	 * @return
	 */
	public List<Author> getEditorList();
	
	
	/**
	 * Get all the institutions mentioned in the header
	 * @return
	 */
	public List<Institution> getInstitutions();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
