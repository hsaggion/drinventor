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
package edu.upf.taln.dri.common.util;


import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import info.debatty.java.stringsimilarity.MetricLCS;

/**
 * Collection of comparison and sotr utility methods.
 * 
 *
 */
public class Util {

	/**
	 * Compare two string - true if both not null and equal
	 * @return
	 */
	public static boolean strCompare(String str1, String str2) {
		return (str1 != null && str2 != null && str1.equals(str2));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareCI(String str1, String str2) {
		return (str1 != null && str2 != null && str1.equalsIgnoreCase(str2));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareTrimmed(String str1, String str2) {
		return (str1 != null && str2 != null && str1.trim().equals(str2.trim()));
	}

	/**
	 * Compare two string case insensitive - true if both not null and equal
	 * @return
	 */
	public static boolean strCompareTrimmedCI(String str1, String str2) {
		return (str1 != null && str2 != null && str1.trim().equalsIgnoreCase(str2.trim()));
	}

	/**
	 * Compare two Integers
	 * 
	 * @param int1
	 * @param int2
	 * @return
	 */
	public static boolean intCompare(Integer int1, Integer int2) {
		return (int1 != null && int2 != null && int1.intValue() == int2.intValue());
	}

	/**
	 * Notify exception
	 * 
	 * @param localMsg
	 * @param e
	 * @param l
	 */
	public static void notifyException(String localMsg, Exception e, Logger l){
		l.warn(localMsg + " (" + e.getClass().getName() + ")" + ((e.getMessage() != null) ? " - " + e.getMessage() : ""));

		try {
			if(l.isDebugEnabled()) {
				e.printStackTrace();
			}
			else {
				l.warn("   >>> Stack trace: " + ExceptionUtils.getStackTrace(e).replace("\n", " ^^ "));
			}
		}
		catch (Exception exc) {
			// DO NOTHING
		}
	}

	/**
	 * Sort a map by increasing value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueInc( Map<K, V> map ) {
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		st.sorted(Comparator.comparing(e -> e.getValue())).forEach(e ->result.put(e.getKey(),e.getValue()));
		return result;
	}

	/**
	 * Sort a map by decreasing value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDec( Map<K, V> map ) {
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		st.sorted(new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}).forEach(e ->result.put(e.getKey(),e.getValue()));
		return result;
	}

	private static int minimum(int a, int b, int c) {                            
		return Math.min(Math.min(a, b), c);                                      
	}                                                                            

	/**
	 * Levenshtein Distance computation
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {      
		int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        

		for (int i = 0; i <= lhs.length(); i++)                                 
			distance[i][0] = i;                                                  
		for (int j = 1; j <= rhs.length(); j++)                                 
			distance[0][j] = j;                                                  

		for (int i = 1; i <= lhs.length(); i++)                                 
			for (int j = 1; j <= rhs.length(); j++)                             
				distance[i][j] = minimum(                                        
						distance[i - 1][j] + 1,                                  
						distance[i][j - 1] + 1,                                  
						distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

		return distance[lhs.length()][rhs.length()];                           
	}

	/**
	 * Compute the Metric Longest Common Subsequence over normalized strings
	 * 
	 * Equal to 0 means that the strings are identical 
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static double computeMetricLCS(String lhs, String rhs) {

		if(lhs == null || rhs == null) {
			return 1d;
		}

		// Normalize
		lhs = lhs.trim().toLowerCase();
		rhs = rhs.trim().toLowerCase();

		// Replace
		lhs = lhs.replace("\n", " ");
		rhs = rhs.replace("\n", " ");

		lhs = lhs.replace("\t", " ");
		rhs = rhs.replace("\t", " ");

		lhs = lhs.replace(":", " ");
		rhs = rhs.replace(":", " ");

		lhs = lhs.replace(";", " ");
		rhs = rhs.replace(";", " ");

		lhs = lhs.replace(".", " ");
		rhs = rhs.replace(".", " ");

		lhs = lhs.replace(",", " ");
		rhs = rhs.replace(",", " ");

		lhs = lhs.replace("-", " ");
		rhs = rhs.replace("-", " ");

		lhs = lhs.replace("_", " ");
		rhs = rhs.replace("_", " ");

		lhs = lhs.replace("(", " ");
		rhs = rhs.replace("(", " ");

		lhs = lhs.replace(")", " ");
		rhs = rhs.replace(")", " ");

		lhs = lhs.replace("[", " ");
		rhs = rhs.replace("[", " ");

		lhs = lhs.replace("]", " ");
		rhs = rhs.replace("]", " ");

		lhs = lhs.replace("{", " ");
		rhs = rhs.replace("{", " ");

		lhs = lhs.replace("}", " ");
		rhs = rhs.replace("}", " ");

		lhs = lhs.replace("`", " ");
		rhs = rhs.replace("`", " ");

		lhs = lhs.replace("'", " ");
		rhs = rhs.replace("'", " ");

		lhs = lhs.replace("\"", " ");
		rhs = rhs.replace("\"", " ");

		lhs = lhs.replace("?", " ");
		rhs = rhs.replace("?", " ");

		lhs = lhs.replace("!", " ");
		rhs = rhs.replace("!", " ");

		lhs = lhs.replace("\\", " ");
		rhs = rhs.replace("\\", " ");

		lhs = lhs.replace("/", " ");
		rhs = rhs.replace("/", " ");

		lhs = lhs.replace("=", " ");
		rhs = rhs.replace("=", " ");

		lhs = lhs.replace("*", " ");
		rhs = rhs.replace("*", " ");

		lhs = lhs.replace("+", " ");
		rhs = rhs.replace("+", " ");

		lhs = lhs.replace("^", " ");
		rhs = rhs.replace("^", " ");

		// Trim
		int contrInt = 0;
		while(true) {
			int lhsPreLength = lhs.length();
			int rhsPreLength = rhs.length();
			lhs = lhs.replace("  ", " ");
			rhs = rhs.replace("  ", " ");

			if((lhsPreLength == lhs.length()) && (rhsPreLength == rhs.length())) {
				break;
			}

			if(++contrInt > 100) {
				break;
			}
		}


		MetricLCS lcs = new MetricLCS();

		return lcs.distance(lhs, rhs);
	}
	
	/**
	 * Convert a list of integer in a list of string
	 * 
	 * @param inputList
	 * @return
	 */
	public static Set<String> fromListIntToSetString(List<Integer> inputList) {
		Set<String> strRetSet = new HashSet<String>();
		
		if(inputList != null && inputList.size() > 0) {
			for(Integer inputListElem : inputList) {
				if(inputListElem != null) {
					strRetSet.add(inputListElem.toString());
				}
			}
		}
		
		return strRetSet;
	}


	public static void main(String[] args) {
		String bibsonomyTitle = "Hao Zhang, Liang Huang, Kai Zhao, and Ryan McDonald. 2013. Online learning for inexact hypergraph search. In Proc. of EMNLP, pages 908? 913. Association for Computational Linguistics.";
		String annText = "Deep Neural Networks for Anatomical Brain Segmentation";
		System.out.println("MLCS: " + computeMetricLCS(bibsonomyTitle , annText));


		double titleBibEntryProp = new Integer(bibsonomyTitle.length()).doubleValue() / new Integer(annText.length()).doubleValue();
		System.out.println("Rapporto: " + titleBibEntryProp);
	}
}
