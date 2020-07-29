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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Sentence of a document together with its descriptive features.
 * 
 *
 */
public class AuthorImpl extends BaseDocumentElem implements Author {

	private String fullName;
	private String firstName;
	private String surname;
	private String email;
	private String personalPageURL;
	private List<Institution> affiliations;

	public AuthorImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.fullName = null;
		this.firstName = null;
		this.surname = null;
		this.email = null;
		this.affiliations = new ArrayList<Institution>();
	}

	public AuthorImpl(DocCacheManager cacheManager, String fullName, String firstName, String surname) {
		super(cacheManager);
		this.fullName = fullName;
		this.firstName = firstName;
		this.surname = surname;
		this.email = null;
		this.affiliations  = new ArrayList<Institution>();
	}

	public AuthorImpl(DocCacheManager cacheManager, String fullName, String firstName, String surname, String email, List<Institution> affiliations) {
		super(cacheManager);
		this.fullName = fullName;
		this.firstName = firstName;
		this.surname = surname;
		this.email = email;
		this.affiliations = affiliations;
	}


	@Override
	public String getFullName() {
		return (this.fullName != null) ? new String(this.fullName): this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public String getFirstName() {
		return (this.firstName != null) ? new String(this.firstName): this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getSurname() {
		return (this.surname != null) ? new String(this.surname): this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return (this.email != null) ? new String(this.email): this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void addAffiliation(Institution institution) {
		this.affiliations.add(institution);
	}

	@Override
	public String getPersonalPageURL() {
		return (this.personalPageURL != null) ? new String(this.personalPageURL): this.personalPageURL;
	}

	public void setPersonalPageURL(String personalPageURL) {
		this.personalPageURL = personalPageURL;
	}

	@Override
	public List<Institution> getAffiliations() {
		return (this.affiliations != null) ? Collections.unmodifiableList(this.affiliations) : null;
	}

	@Override
	public String asString(boolean compactOutput) {
		String authorStr = "";

		authorStr += "[AUTHOR] Full name: '" + StringUtils.defaultString(this.fullName, "-") + "'" +
				((this.firstName != null || !compactOutput) ? ", First name: '" + StringUtils.defaultString(this.firstName, "-") + "'" : "") +
				((this.surname != null || !compactOutput) ? ", Surname: '" + StringUtils.defaultString(this.surname, "-") + "'" : "") +
				((this.email != null || !compactOutput) ? ", Email: '" + StringUtils.defaultString(this.email, "-") + "'" : "") +
				((this.personalPageURL != null || !compactOutput) ? ", Personal page URL: '" + StringUtils.defaultString(this.personalPageURL, "-") + "'" : "") + "\n";
		if(this.affiliations != null && this.affiliations.size() > 0) {
			for(Institution inst : this.affiliations) {
				authorStr += "   " + inst.asString(compactOutput);
			}
		}
		else {
			authorStr += "   NO AFFILIATIONS ASSOCIATED\n";
		}

		return authorStr;
	}

}
