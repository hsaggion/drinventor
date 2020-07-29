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

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.util.InvalidOffsetException;

/**
 * Get the text of the annotation.
 * 
 *
 */
public class AnnotationTextC implements FeatCalculator<String, Annotation, DocumentCtx> {

	
	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String stringText = null;
		try {
			stringText = doc.getGateDoc().getContent().getContent(obj.getStartNode().getOffset(), obj.getEndNode().getOffset()).toString();
		} catch (InvalidOffsetException e) {
			e.printStackTrace();
		}
		MyString retValue = new MyString("");
		
		if(stringText != null) {
			retValue.setValue(stringText.trim());
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
