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
package edu.upf.taln.dri.common.connector.bibsonomy.model;

import org.bibsonomy.model.BibTex;

/**
 * Extension of class that represent a BibTeX entry
 * 
 *
 */
public class BibTexWrap extends BibTex {
	
	private static final long serialVersionUID = 1L;
	
	private String authorList = "";
	private String editorList = "";
	
	// Constructor
	public BibTexWrap() {
		super();
		this.authorList = null;
		this.editorList = null;
	}
	
	public BibTexWrap(String authorString, String editorString) {
		super();
		this.authorList = authorString;
		this.editorList = editorString;
	}

	// Setters and getters
	public String getAuthorList() {
		return authorList;
	}

	public void setAuthorList(String authorString) {
		this.authorList = authorString;
	}

	public String getEditorList() {
		return editorList;
	}

	public void setEditorList(String editorString) {
		this.editorList = editorString;
	}
	
}
