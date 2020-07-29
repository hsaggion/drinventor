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

import java.util.List;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get the order number of the sentence inside the document (the number 1 is assigned to the first sentence of the abstract).
 * 
 *
 */
public class DocumentSentenceIdentifierC implements FeatCalculator<String, Annotation, DocumentCtx> {

	
	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		// Sentence order
		Integer sentenceOrderNumber = -1;
		
		AnnotationSet rhetClassifiedSentences = doc.getGateDoc().getAnnotations("Analysis").get("Sentence");
		Integer orderCount = 0;
		if(rhetClassifiedSentences != null && rhetClassifiedSentences.size() > 0 ) {
			List<Annotation> rhetClassifiedSentencesList = gate.Utils.inDocumentOrder(rhetClassifiedSentences);
			
			for(Annotation rhetClassifiedSentence : rhetClassifiedSentencesList) {
				orderCount++;
				
				if( rhetClassifiedSentence.getStartNode().getOffset().compareTo(obj.getStartNode().getOffset()) == 0 &&
						rhetClassifiedSentence.getEndNode().getOffset().compareTo(obj.getEndNode().getOffset()) == 0 ) {
					sentenceOrderNumber = orderCount;
					break;
				}
			}
		}
		
		MyString retValue = new MyString("");
		
		retValue.setValue("SENT_" + sentenceOrderNumber);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
