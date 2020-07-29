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
 * Institution representing an institution, usually mentioned in the header of the document as the affiliation of the authors
 * 
 *
 */
public class InstitutionImpl extends BaseDocumentElem implements Institution {

	private String fullText;
	private String name;
	private String subName;
	private String address;
	private String state;
	private String city;
	private String URL;

	public InstitutionImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.fullText = null;
		this.name = null;
		this.subName = null;
		this.address = null;
		this.state = null;
		this.city = null;
		this.URL = null;
	}

	public InstitutionImpl(DocCacheManager cacheManager, String fullText, String name, String subName, String address, String state, String city, String URL) {
		super(cacheManager);
		this.fullText = fullText;
		this.name = name;
		this.subName = subName;
		this.address = address;
		this.state = state;
		this.city = city;
		this.URL = URL;
	}
	

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	@Override
	public String getFullText() {
		return (this.fullText != null) ? new String(this.fullText) : null;
	}

	@Override
	public String getName() {
		return (this.name != null) ? new String(this.name) : null;
	}

	@Override
	public String getSubName() {
		return (this.subName != null) ? new String(this.subName) : null;
	}
	
	public String getAddress() {
		return (this.address != null) ? new String(this.address) : null;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String getState() {
		return (this.state != null) ? new String(this.state) : null;
	}

	@Override
	public String getCity() {
		return (this.city != null) ? new String(this.city) : null;
	}

	public String getURL() {
		return (this.URL != null) ? new String(this.URL) : null;
	}

	public void setURL(String URL) {
		this.URL = URL;
	}

	@Override
	public String asString(boolean compactOutput) {
		String institutionStr = "";

		institutionStr += "[INSTITUTION] Name: '" + StringUtils.defaultString(this.name, "-") + "'" +
				((this.subName != null || !compactOutput) ? ", Subname: '" + StringUtils.defaultString(this.subName, "-") + "'" : "") +
				((this.address != null || !compactOutput) ? ", Address: '" + StringUtils.defaultString(this.address, "-") + "'" : "") +
				((this.state != null || !compactOutput) ? ", State: '" + StringUtils.defaultString(this.state, "-") + "'" : "") +
				((this.city != null || !compactOutput) ? ", City: '" + StringUtils.defaultString(this.city, "-") + "'" : "") +
				((this.URL != null || !compactOutput) ? ", URL: '" + StringUtils.defaultString(this.URL, "-") + "'" : "") + "\n";

		return institutionStr;	
	}

}
