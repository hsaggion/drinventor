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

import java.util.Comparator;
import java.util.Map;


public class MapValueComparator implements Comparator<Object> {

		private Map base;

		public MapValueComparator(Map<String, Integer> base) { this.base = base; }

		
		public int compare(Object a, Object b) 
		{

			if((Integer)base.get(a) < (Integer)base.get(b)) 
			{
			  return 1;
			} 
			else 
			{	
				if((Integer)base.get(a) == (Integer)base.get(b)) 
				{
					return -1;
				} 
				else 
				{
					return -1;
				}
			} 
		}
				
}

