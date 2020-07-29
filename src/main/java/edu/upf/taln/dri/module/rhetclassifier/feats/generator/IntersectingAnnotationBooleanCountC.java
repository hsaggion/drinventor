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

import java.util.Iterator;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;


/**
 * Get the number of intersecting annotations from a given annotation set, of a given type and if feature name is not null, with a given value for a boolean feature
 * 
 *
 */
public class IntersectingAnnotationBooleanCountC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";
	private String featureName = "";
	private boolean featureValue = false;

	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation
	 * If feature name is not null or empty:
	 *  - both feature name and the feature boolean value have to match to consider the annotation in the count
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param filterFeatureName
	 * @param filterFeatureValue
	 */
	public IntersectingAnnotationBooleanCountC(String tokenAnnotationSet, String tokenAnnotationName, String filterFeatureName, boolean featureValue) {
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = filterFeatureName;
		this.featureValue = featureValue;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		AnnotationSet intersectingTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		MyDouble retValue = new MyDouble(0d);

		if(intersectingTokens != null) {
			// Check if to filter or not also by feature name and value
			if( this.featureName == null || this.featureName.equals("") ) {
				retValue.setValue(new Double(intersectingTokens.size()));
				return retValue;
			}
			else {
				Iterator<Annotation> intersectingTokensIter = intersectingTokens.iterator();
				Integer countAnn = 0;
				while(intersectingTokensIter.hasNext()) {
					Annotation annotInt = intersectingTokensIter.next();

					if(annotInt.getFeatures().containsKey(this.featureName)) {
						boolean featureValue = (boolean) annotInt.getFeatures().get(this.featureName);

						if(featureValue == this.featureValue) {
							countAnn++;
						}
					}
				}
				retValue.setValue(new Double(countAnn));
				return retValue;
			}
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}

}
