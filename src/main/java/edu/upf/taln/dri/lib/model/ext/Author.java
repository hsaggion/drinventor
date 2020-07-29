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

import java.util.List;

/**
 * Author of a paper
 * 
 *
 */
public interface Author {
	
	/**
	 * The full name of the author
	 * @return
	 */
	public String getFullName();
		
	/**
	 * The first name of the author
	 * @return
	 */
	public String getFirstName();
	
	/**
	 * The surname of the author
	 * @return
	 */
	public String getSurname();
	
	/**
	 * The email of the author
	 * @return
	 */
	public String getEmail();
	
	/**
	 * The URL of the personal Web page of the author
	 * 
	 * @return
	 */
	public String getPersonalPageURL();
	
	/**
	 * The affiliations of the author
	 * @return
	 */
	public List<Institution> getAffiliations();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
