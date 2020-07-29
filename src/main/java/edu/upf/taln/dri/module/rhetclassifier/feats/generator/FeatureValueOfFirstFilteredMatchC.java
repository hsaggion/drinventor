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
 * Get a feature value of the feature with name equal to featureName of the first annotation that matches 
 * the filters (name, filterName, filterValue and filterStartsWith) of the annotation
 * 
 *
 */
public class FeatureValueOfFirstFilteredMatchC implements FeatCalculator<String, Annotation, DocumentCtx> {

	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";
	private String featureName = "";

	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;


	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation.
	 * Feature name is the feature to return the value of.
	 * 
	 * The three arguments that have a name starting with fratureFilter are devoted to filter by a feature name and value the annotations to consider.
	 * If featureFilterName is null or empty no filter is performed.
	 * If featureFilterName is not null or empty, only annotations having a feature with that name are considered.
	 * If also featureFilterValue is not null or empty, only annotations having a feature name equal to featureFilterName and
	 * a feature value equal to featureFilterValue are considered.
	 * If featureFilterStartsWith is true, it is only checked that the feature value starts with the featureFilterValue string.
	 * 
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param featureName
	 * @param featureFilterName
	 * @param featureFilterValue
	 * @param featureFilterStartsWith
	 */
	public FeatureValueOfFirstFilteredMatchC(String tokenAnnotationSet, String tokenAnnotationName,
			String featureName,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith) {
		super();
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = featureName;
		this.featureFilterName = featureFilterName;
		this.featureFilterValue = featureFilterValue;
		this.featureFilterStartsWith = featureFilterStartsWith;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {

		String returnValue = "";

		boolean foundFisrstOccurrence = false;

		AnnotationSet intersectTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		List<Annotation> intersectingTokensOrdered  = gate.Utils.inDocumentOrder(intersectTokens);

		if(intersectingTokensOrdered != null && intersectingTokensOrdered.size() > 0) {

			for(Annotation annotInt : intersectingTokensOrdered) {
				if(!foundFisrstOccurrence && annotInt != null) {

					// Check if to include the annotations by applying the featureFilter
					boolean excludeAnnotation = false;
					if(this.featureFilterName != null && !featureFilterName.equals("")) {
						if(annotInt.getFeatures() == null || annotInt.getFeatures().size() <= 0 || 
								!annotInt.getFeatures().containsKey(this.featureFilterName)) {
							excludeAnnotation = true;
						}
						else if(this.featureFilterValue != null && !featureFilterValue.equals("")) {
							if(this.featureFilterStartsWith && (annotInt.getFeatures().get(this.featureFilterName) == null ||
									!((String) annotInt.getFeatures().get(this.featureFilterName)).startsWith(this.featureFilterValue)) ) {
								excludeAnnotation = true;
							}
							if(!this.featureFilterStartsWith && (annotInt.getFeatures().get(this.featureFilterName) == null ||
									!((String) annotInt.getFeatures().get(this.featureFilterName)).equals(this.featureFilterValue)) ) {
								excludeAnnotation = true;
							}
						}
					}
					
					if(excludeAnnotation) {
						continue;
					}
					else {
						// Found first annotation that passed the filter by feature name, value and startsWith
						// Check if the annotation has a not null String featureName and set this as the value ofthis feature
						try {
							if(annotInt.getFeatures() != null && annotInt.getFeatures().containsKey(this.featureName) &&
									!((String) annotInt.getFeatures().get(this.featureName)).equals("")) {
								returnValue = (String) annotInt.getFeatures().get(this.featureName);
								foundFisrstOccurrence = true;
								break;
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		MyString retValue = new MyString(returnValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}

}
