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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder;

public class SubjectivityElem {
	
	private SubjectivityTypeENUM subjType;
	private Integer numWords;
	private String text;
	private PosENUM pos;
	private Boolean stemmed;
	private PriorPolarityENUM priorPlarity;
	
	// Constructor
	public SubjectivityElem() {
		super();
		this.subjType = null;
		this.numWords = null;
		this.text = null;
		this.pos = null;
		this.stemmed = null;
		this.priorPlarity = null;
	}
	
	public SubjectivityElem(SubjectivityTypeENUM subjType, Integer numWords,
			String text, PosENUM pos, Boolean stemmed,
			PriorPolarityENUM priorPlarity) {
		super();
		this.subjType = subjType;
		this.numWords = numWords;
		this.text = text;
		this.pos = pos;
		this.stemmed = stemmed;
		this.priorPlarity = priorPlarity;
	}

	
	// Setters and getters
	public SubjectivityTypeENUM getSubjType() {
		return subjType;
	}

	public void setSubjType(SubjectivityTypeENUM subjType) {
		this.subjType = subjType;
	}

	public Integer getNumWords() {
		return numWords;
	}

	public void setNumWords(Integer numWords) {
		this.numWords = numWords;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public PosENUM getPos() {
		return pos;
	}

	public void setPos(PosENUM pos) {
		this.pos = pos;
	}

	public Boolean getStemmed() {
		return stemmed;
	}

	public void setStemmed(Boolean stemmed) {
		this.stemmed = stemmed;
	}

	public PriorPolarityENUM getPriorPlarity() {
		return priorPlarity;
	}

	public void setPriorPlarity(PriorPolarityENUM priorPlarity) {
		this.priorPlarity = priorPlarity;
	}

	@Override
	public String toString() {
		return "SubjectivityElem [subjType=" + ((subjType != null) ? subjType : "NULL") +
				", numWords=" + ((numWords != null) ? numWords : "NULL") + 
				", text=" + ((text != null) ? text : "NULL") + 
				", pos=" + ((pos != null) ? pos : "NULL") + 
				", stemmed=" + ((stemmed != null) ? stemmed : "NULL") + 
				", priorPlarity=" + ((priorPlarity != null) ? priorPlarity : "NULL") + "]";
	}
	
	
	
	
	
}
