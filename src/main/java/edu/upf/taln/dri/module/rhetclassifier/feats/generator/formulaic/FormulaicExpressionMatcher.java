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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator.formulaic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FormulaicExpressionMatcher {

	public static Map<String, Set<String>> coreMap = new HashMap<String, Set<String>>();

	static {
		Set<String> GENERAL_FORMULAIC = new HashSet<String>();
		coreMap.put("GENERAL_FORMULAIC", GENERAL_FORMULAIC);
		GENERAL_FORMULAIC.add("in @TRADITION_ADJ JJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @TRADITION_ADJ used @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @TRADITION_ADJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @MANYJJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @MANY @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @BEFORE_ADJ JJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in @BEFORE_ADJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in other JJ @WORK_NOUN");
		GENERAL_FORMULAIC.add("in other @WORK_NOUN");
		GENERAL_FORMULAIC.add("in such @WORK_NOUN");
		
		
	}








}
