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
 * Sentence of a document together with its descriptive features.
 * 
 *
 */
public class TokenImpl extends BaseDocumentElem implements Token {

	private Integer id;
	private Integer inSentencePosition;
	private Integer containingSentence;
	private String word;
	private String lemma;
	private String POS;

	// Constructors
	public TokenImpl(DocCacheManager cacheManager, Integer sentenceId) {
		super(cacheManager);
		this.id = null;
		this.inSentencePosition = null;
		this.containingSentence = sentenceId;
		this.word = null;
		this.lemma = null;
		this.POS = null;
	}

	@Override
	public Integer getId() {
		return new Integer(this.id);
	}

	@Override
	public Integer getInSentencePosition() {
		return new Integer(this.inSentencePosition);
	}

	@Override
	public Integer getContainingSentence() {
		return new Integer(this.containingSentence);
	}

	@Override
	public String getWord() {
		return new String(this.word);
	}

	@Override
	public String getLemma() {
		return new String(this.lemma);
	}

	@Override
	public String getPOS() {
		return new String(this.POS);
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setInSentencePosition(Integer inSentencePosition) {
		this.inSentencePosition = inSentencePosition;
	}

	public void setContainingSentence(Integer containingSentence) {
		this.containingSentence = containingSentence;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public void setPOS(String pOS) {
		POS = pOS;
	}

	@Override
	public String asString(boolean compactOutput) {
		String tokenStr = "";

		tokenStr += "[TOKEN] ID: '" + ((this.id != null) ? this.id + "" : "-") + "'" +
				((this.inSentencePosition != null || !compactOutput) ? ", In sentence position: '" + ((this.inSentencePosition != null) ? this.inSentencePosition + "" : "-") + "'" : "") +
				((this.containingSentence != null || !compactOutput) ? ", Containing sentence ID: '" + ((this.containingSentence != null) ? this.containingSentence + "" : "-") + "'" : "") +
				((this.word != null || !compactOutput) ? ", Word: '" + StringUtils.defaultString(this.word, "-") + "'" : "") +
				((this.lemma != null || !compactOutput) ? ", Lemma: '" + StringUtils.defaultString(this.lemma, "-") + "'" : "") +
				((this.POS != null || !compactOutput) ? ", POS: '" + StringUtils.defaultString(this.POS, "-") + "'" : "") + "\n";

		return tokenStr;
	}

}
