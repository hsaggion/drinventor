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
package edu.upf.taln.dri.common.connector.freecite.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * FreeCite search result - data model
 * 
 *
 */
public class FreeCiteResult {
	
	private String title = "";
	private List<String> authorNames = new ArrayList<String>();
	private String year = "";
	private String journal = "";
	private String pages = "";
	private String rawString = "";
	
	// Getters and setters
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public List<String> getAuthorNames() {
		return authorNames;
	}
	
	public void setAuthorNames(List<String> authorNames) {
		this.authorNames = authorNames;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public String getJournal() {
		return journal;
	}
	
	public void setJournal(String journal) {
		this.journal = journal;
	}
	
	public String getPages() {
		return pages;
	}
	
	public void setPages(String pages) {
		this.pages = pages;
	}
	
	public String getRawString() {
		return rawString;
	}

	public void setRawString(String rawString) {
		this.rawString = rawString;
	}
	

	@Override
	public String toString() {
		return "FreeCiteResult [title=" + StringUtils.defaultIfBlank(title, "NULL") 
				+ ", authorNames=" + (!CollectionUtils.isEmpty(authorNames) ? authorNames : "EMPTY") 
				+ ", year=" + StringUtils.defaultIfBlank(year, "NULL") 
				+ ", journal=" + StringUtils.defaultIfBlank(journal, "NULL")
				+ ", pages=" + StringUtils.defaultIfBlank(pages, "NULL")
				+ ", raw_string=" + StringUtils.defaultIfBlank(rawString, "NULL") + "]";
	}
	
}
