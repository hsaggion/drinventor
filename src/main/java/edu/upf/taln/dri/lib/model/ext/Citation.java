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
import java.util.Set;

/**
 * Interface to access a citation of a paper
 * 
 *
 */
public interface Citation {
	
	/**
	 * Document-wide unambiguous citation Id
	 * @return document-wide unambiguous term Id
	 */
	public Integer getId();
	
	/**
	 * The sources from which the citation data have been retrieved
	 * 
	 * @return
	 */
	public Set<CitationSourceENUM> getSource();
	
	/**
	 * The whole text of the citation, usually part of the bibliography of a paper
	 * @return
	 */
	public String getText();
	
	/**
	 * The title of the paper
	 * @return
	 */
	public String getTitle();
	
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
	 * The specific ID of the paper, if any
	 * @return
	 */
	public String getPubID(PubIdENUM pubIDtype);
	
	/**
	 * The open URL of the paper
	 * @return
	 */
	public String getOpenURL();
	
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
	 * The institution of the paper
	 * @return
	 */
	public String getInstitution();
	
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
	 * A map of citation styles as key and complete citation strings as values
	 * @return
	 */
	public Map<String, String> getCitationString();
	
	
	/**
	 * The list of {@link edu.upf.taln.dri.lib.model.ext.CitationMarker CitationMarkers}, 
	 * that are the inline references to this citation
	 * 
	 * An empty list is returned if no {@link edu.upf.taln.dri.lib.model.ext.CitationMarker CitationMarkers} 
	 * have been associated to the considered citation
	 * @return
	 */
	public List<CitationMarker> getCitaitonMarkers();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
	
}
