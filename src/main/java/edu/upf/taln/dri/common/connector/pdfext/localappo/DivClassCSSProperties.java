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

import java.util.StringTokenizer;


/**
 * Captures some class properties of a div tag
 *  * bottom (y..)
 *  * font-size (fs..)
 *  * font family (ff..)
 *  * height (h..)
 *  * left (x..)
 * 
 * <div class="t m0 x1 h2 y1 ff1 fs0 fc0 sc0 ls0 ws0">
 * 
 * @author upf
 *
 */

public class DivClassCSSProperties {
	
	public String height;
	public String fontsize;				  
 	public String bottom;				  
 	public String left;
 	public String fontfamily;
 	
	public String strDivClassValue="";
	
	public DivClassCSSProperties(String strDivClassValue)
	{
		this.strDivClassValue=strDivClassValue;
		
		StringTokenizer tokenizerDivClassValue=new StringTokenizer(strDivClassValue," ");
		  
		while(tokenizerDivClassValue.hasMoreElements())
		  {
			  String property=tokenizerDivClassValue.nextToken();
			  
			  
			  
			  if (property.startsWith("h"))
			  {
				 this.height=property;
			  }
			  if (property.startsWith("fs"))
			  {
				  this.fontsize=property;				  
			  }

			  if (property.startsWith("ff"))
			  {
				  this.fontfamily=property;				  
			  }

			  
			  if (property.startsWith("y"))
			  {
				  this.bottom=property;				  
			  }
			  
			  if (property.startsWith("x"))
			  {
				  this.left=property;				  
			  }
			  
		  }	  
	}
		
		
	public String getBottom()
	{
		return bottom;
	}

	
	public String getFontsize()
	{
		return fontsize;
	}
	
	public String getFontfamily()
	{
		return fontfamily;
	}
	
	public String getHeight()
	{
		return height;
	}
	
	public String getLeft()
	{
		return left;
	}

}
