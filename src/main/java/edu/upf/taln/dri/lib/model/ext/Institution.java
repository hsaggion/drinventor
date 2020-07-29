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

/**
 * Interface to access the metadata of an institution (university, institute, etc.).
 * 
 *
 */
public interface Institution {
	
	/**
	 * The full text of the institution
	 * @return
	 */
	public String getFullText();
	
	/**
	 * The name of the institution
	 * @return
	 */
	public String getName();
	
	/**
	 * The name of the department, laboratory, group
	 * @return
	 */
	public String getSubName();
	
	/**
	 * The name of the institution
	 * @return
	 */
	public String getAddress();
	
	/**
	 * The state of the institution
	 * @return
	 */
	public String getState();
	
	/**
	 * The city of the institution
	 * @return
	 */
	public String getCity();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
