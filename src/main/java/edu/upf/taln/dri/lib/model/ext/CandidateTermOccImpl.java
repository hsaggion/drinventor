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

import org.apache.commons.lang.StringUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * A term mined from the document.
 * 
 *
 */
public class CandidateTermOccImpl extends BaseDocumentElem implements CandidateTermOcc {
	private Integer id;
	private String text;
	private Integer inSentenceId;
	private String regexPattern;
	private String matchedPattern;

	// Constructor
	public CandidateTermOccImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.text = null;
		this.inSentenceId = null;
		this.regexPattern = null;
		this.matchedPattern = null;
	}

	public CandidateTermOccImpl(DocCacheManager cacheManager, Integer id) {
		super(cacheManager);
		this.id = id;
		this.text = null;
		this.inSentenceId = null;
		this.matchedPattern = null;
		this.matchedPattern = null;
	}

	public CandidateTermOccImpl(DocCacheManager cacheManager, Integer id, String text, Integer sentenceId) {
		super(cacheManager);
		this.id = id;
		this.text = text;
		this.inSentenceId = sentenceId;
		this.matchedPattern = null;
		this.matchedPattern = null;
	}


	// Setters and getters
	@Override
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id): this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getText() {
		return (this.text != null) ? new String(this.text): this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getInSentenceId() {
		return (this.inSentenceId != null) ? new Integer(this.inSentenceId): this.inSentenceId;
	}

	public void setInSentenceId(Integer inSentenceId) {
		this.inSentenceId = inSentenceId;
	}

	public String getRegexPattern() {
		return (this.regexPattern != null) ? new String(this.regexPattern): this.regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
	}

	public String getMatchedPattern() {
		return (this.matchedPattern != null) ? new String(this.matchedPattern): this.matchedPattern;
	}

	public void setMatchedPattern(String matchedPattern) {
		this.matchedPattern = matchedPattern;
	}

	// Other interface implementations
	@Override
	public Integer getSentenceIdWithTerm() {
		if(this.inSentenceId != null) {
			return this.inSentenceId;
		}

		return null;
	}

	@Override
	public String asString(boolean compactOutput) {
		String candidateTermOccStr = "";

		candidateTermOccStr += "[CANDIDATE TERM] ID: '" + ((this.id != null) ? this.inSentenceId + "" : "-") + "'" +
				((this.text != null || !compactOutput) ? ", Text: '" + StringUtils.defaultString(this.text, "-") + "'" : "") +
				((this.inSentenceId != null || !compactOutput) ? ", In-sentence ID: '" + ((this.inSentenceId != null) ? this.inSentenceId + "" : "-") + "'" : "") +
				((this.regexPattern != null || !compactOutput) ? ", Regex pattern: '" + StringUtils.defaultString(this.regexPattern, "-") + "'" : "") +
				((this.matchedPattern != null || !compactOutput) ? ", Matched pattern: '" + StringUtils.defaultString(this.matchedPattern, "-") + "'" : "") + "'\n";

				return candidateTermOccStr;
	}



}
