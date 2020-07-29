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
 * CitationMarkerImpl represents an inline reference to a citation in a paper
 * 
 *
 */
public class CitationMarkerImpl extends BaseDocumentElem implements CitationMarker {

	private Integer id;
	private Integer citationId;
	private Integer sentenceId;
	private String referenceText;


	public CitationMarkerImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.citationId = null;
		this.sentenceId = null;
		this.referenceText = null;
	}

	public CitationMarkerImpl(DocCacheManager cacheManager, Integer id, Integer citationId,
			Integer sentenceId, String referenceText) {
		super(cacheManager);
		this.id = id;
		this.citationId = citationId;
		this.sentenceId = sentenceId;
		this.referenceText = referenceText;
	}


	@Override
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id): this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getCitationId() {
		return (this.citationId != null) ? new Integer(this.citationId): this.citationId;
	}

	public void setCitationId(Integer citationId) {
		this.citationId = citationId;
	}

	@Override
	public Integer getSentenceId() {
		return (this.sentenceId != null) ? new Integer(this.sentenceId): this.sentenceId;
	}

	public void setSentenceId(Integer sentenceId) {
		this.sentenceId = sentenceId;
	}

	@Override
	public String getReferenceText() {
		return (this.referenceText != null) ? new String(this.referenceText): this.referenceText;
	}

	public void setReferenceText(String referenceText) {
		this.referenceText = referenceText;
	}

	@Override
	public String asString(boolean compactOutput) {
		String citMarkerStr = "";

		citMarkerStr += "[CIT MARKER] ID: '" + ((this.id != null) ? this.id + "" : "-") + "'" +
				((this.citationId != null || !compactOutput) ? ", Citation ID: '" + ((this.citationId != null) ? this.citationId + "" : "-") + "'" : "") +
				((this.sentenceId != null || !compactOutput) ? ", Sentence ID: '" + ((this.sentenceId != null) ? this.sentenceId + "" : "-") + "'" : "") +
				((this.referenceText != null || !compactOutput) ? ", Reference text: '" + StringUtils.defaultString(this.referenceText, "-") + "'" : "") + "\n";

		return citMarkerStr;
	}

}
