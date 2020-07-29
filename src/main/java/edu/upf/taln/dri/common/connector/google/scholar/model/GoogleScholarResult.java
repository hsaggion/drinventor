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
package edu.upf.taln.dri.common.connector.google.scholar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * Google Scholar search result - data model
 * 
 *
 */
public class GoogleScholarResult {
	
	private String title = "";
	private String link = "";
	private String secondLine = "";
	private String year = "";
	private Map<String, String> authorName_LinkMap = new HashMap<String, String>();
	private String abstractSnippet = "";
	private Map<String, String> citationType_ContentMap = new HashMap<String, String>();
	private String refText = "";
	private List<String> refAuthorsList = new ArrayList<String>();
	private List<String> refTitleList = new ArrayList<String>();
	private List<String> refYearList = new ArrayList<String>();
	private List<String> refJournalList = new ArrayList<String>();
	
	
	// Getters and setters
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getSecondLine() {
		return secondLine;
	}

	public void setSecondLine(String secondLine) {
		this.secondLine = secondLine;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Map<String, String> getAuthorName_LinkMap() {
		return authorName_LinkMap;
	}

	public void setAuthorName_LinkMap(Map<String, String> authorName_LinkMap) {
		this.authorName_LinkMap = authorName_LinkMap;
	}

	public String getAbstractSnippet() {
		return abstractSnippet;
	}

	public void setAbstractSnippet(String abstractSnippet) {
		this.abstractSnippet = abstractSnippet;
	}

	public Map<String, String> getCitationType_ContentMap() {
		return citationType_ContentMap;
	}

	public void setCitationType_ContentMap(
			Map<String, String> citationType_ContentMap) {
		this.citationType_ContentMap = citationType_ContentMap;
	}

	public String getRefText() {
		return refText;
	}

	public void setRefText(String refText) {
		this.refText = refText;
	}

	public List<String> getRefAuthorsList() {
		return refAuthorsList;
	}

	public void setRefAuthorsList(List<String> refAuthorsList) {
		this.refAuthorsList = refAuthorsList;
	}

	public List<String> getRefTitleList() {
		return refTitleList;
	}

	public void setRefTitleList(List<String> refTitleList) {
		this.refTitleList = refTitleList;
	}

	public List<String> getRefYearList() {
		return refYearList;
	}

	public void setRefYearList(List<String> refYearList) {
		this.refYearList = refYearList;
	}

	public List<String> getRefJournalList() {
		return refJournalList;
	}

	public void setRefJournalList(List<String> refJournalList) {
		this.refJournalList = refJournalList;
	}
	
	
	@Override
	public String toString() {
		return "GoogleScholarResult ["
				+ "title=" + StringUtils.defaultIfBlank(title, "NULL")
				+ ", link=" + StringUtils.defaultIfBlank(link, "NULL")
				+ ", secondLine=" + StringUtils.defaultIfBlank(secondLine, "NULL")
				+ ", year=" + StringUtils.defaultIfBlank(year, "NULL")
				+ ", authorNameLinkMap=" + (!CollectionUtils.isEmpty(authorName_LinkMap) ? authorName_LinkMap : "EMPTY")
				+ ", abstractSnippet=" + StringUtils.defaultIfBlank(abstractSnippet, "NULL")
				+ ", citationTypeContentMap=" + (!CollectionUtils.isEmpty(citationType_ContentMap) ? citationType_ContentMap : "EMPTY")
				+ ", refText=" + StringUtils.defaultIfBlank(refText, "NULL")
				+ ", refAuthors=" + (!CollectionUtils.isEmpty(refAuthorsList) ? refAuthorsList : "EMPTY")
				+ ", refTitle=" + (!CollectionUtils.isEmpty(refTitleList) ? refTitleList : "EMPTY")
				+ ", refYear=" + (!CollectionUtils.isEmpty(refYearList) ? refYearList : "EMPTY")
				+ ", refJournal=" + (!CollectionUtils.isEmpty(refJournalList) ? refJournalList : "EMPTY")
				+ "]";
	}
	
}
