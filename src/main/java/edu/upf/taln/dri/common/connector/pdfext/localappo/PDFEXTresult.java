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
package edu.upf.taln.dri.common.connector.pdfext.localappo;

public class PDFEXTresult {
	
	private String resHTML = "";
	private String resHTML_divid = "";
	private String resXML = "";
	
	// Constructor
	public PDFEXTresult() {
		super();
		this.resHTML = "";
		this.resXML = "";
		this.resHTML_divid="";
	}
	
	public PDFEXTresult(String resHTML, String resXML) {
		super();
		this.resHTML = resHTML;
		this.resXML = resXML;
	}
	
	// Getters and setters
	public String getResHTML() {
		return resHTML;
	}

	public void setResHTML(String resHTML) {
		this.resHTML = resHTML;
	}

	public String getResXML() {
		return resXML;
	}

	public void setResXML(String resXML) {
		this.resXML = resXML;
	}
	
	
	public String getResHTML_divid() {
		return resHTML_divid;
	}

	public void setResHTML_divid(String resHTML_divid) {
		this.resHTML_divid = resHTML_divid;
	}

	
}
