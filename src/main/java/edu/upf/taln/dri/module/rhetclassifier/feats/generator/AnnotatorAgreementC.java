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

/**
 * Get the agreement type of the specific kind of annotaiton.
 * 
 *
 */
public class AnnotatorAgreementC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	public enum AnnotationTypeENUM {
        RHET, ASPECT, MAIN_CIT, SECONDARY_CIT, SUMMARY;
    }
	
	private AnnotationTypeENUM annType = AnnotationTypeENUM.RHET;
	
	public AnnotatorAgreementC(AnnotationTypeENUM annType) {
		super();
		this.annType = annType;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String stringText = "";
		
		if(doc != null) {
			switch(annType) {
			case RHET:
				if(doc.getAgreementRhet() != null) stringText = doc.getAgreementRhet();
				break;
			case ASPECT:
				if(doc.getAgreementAspect() != null) stringText = doc.getAgreementAspect();
				break;
			case MAIN_CIT:
				if(doc.getAgreementMainCit() != null) stringText = doc.getAgreementMainCit();
				break;
			case SECONDARY_CIT:
				if(doc.getAgreementSecondaryCit() != null) stringText = doc.getAgreementSecondaryCit();
				break;
			case SUMMARY:
				if(doc.getAgreementSummary() != null) stringText = doc.getAgreementSummary();
				break;
			default:
				stringText = "";
			}
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
