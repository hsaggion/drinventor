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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;

/**
 * Get the name of the document (or a random integer if no name can be retrieved)
 * 
 *
 */
public class DocumentIdentifierC implements FeatCalculator<String, Annotation, DocumentCtx> {

	private static Random rnd = new Random();
	
	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String stringText = null;
		if(doc != null && doc.getGateDoc() != null && doc.getGateDoc().getName() != null) {
			stringText = doc.getGateDoc().getName();
		}
		
		MyString retValue = new MyString("");
		
		if(StringUtils.isNotBlank(stringText)) {
			retValue.setValue(stringText.trim());
		}
		else {
			retValue.setValue("DOCUMENT_" + rnd.nextInt());
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
