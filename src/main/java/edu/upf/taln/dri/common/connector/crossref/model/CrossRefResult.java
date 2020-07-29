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
package edu.upf.taln.dri.common.connector.crossref.model;

import org.apache.commons.lang.StringUtils;


/**
 * CrossRef search result - data model
 * 
 *
 */
public class CrossRefResult {
	
	private String originalText = "";
	private String score = "";
	private String normalizedScore = "";
	
	private String doi = "";
	private String title = "";
	private String year = "";
	private String fullCitation = "";
	private String coins = "";
	
	
	// Getters and setters
	public String getOriginalText() {
		return originalText;
	}
	
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}
	
	public String getScore() {
		return score;
	}
	
	public void setScore(String score) {
		this.score = score;
	}
	
	public String getNormalizedScore() {
		return normalizedScore;
	}
	
	public void setNormalizedScore(String normalizedScore) {
		this.normalizedScore = normalizedScore;
	}
	
	public String getDoi() {
		return doi;
	}
	
	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public String getFullCitation() {
		return fullCitation;
	}
	
	public void setFullCitation(String fullCitation) {
		this.fullCitation = fullCitation;
	}
	
	public String getCoins() {
		return coins;
	}

	public void setCoins(String coins) {
		this.coins = coins;
	}
	

	@Override
	public String toString() {
		return "CrossRefResult [originalText=" + StringUtils.defaultIfBlank(originalText, "NULL")  
				+ ", score=" + StringUtils.defaultIfBlank(score, "NULL")  
				+ ", normalizedScore=" + StringUtils.defaultIfBlank(normalizedScore, "NULL")  
				+ ", doi=" + StringUtils.defaultIfBlank(doi, "NULL")  
				+ ", title=" + StringUtils.defaultIfBlank(title, "NULL")  
				+ ", year=" + StringUtils.defaultIfBlank(year, "NULL") 
				+ ", coins=" + StringUtils.defaultIfBlank(coins, "NULL") 
				+ ", fullCitation=" + StringUtils.defaultIfBlank(fullCitation, "NULL")  + "]";
	}
	
}
