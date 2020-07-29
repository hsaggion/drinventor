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

import org.apache.commons.lang3.StringUtils;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get the sentence relative position of the first match of a certain annotation
 * 
 *
 */
public class DERIV_RelativePositionOfFirstMatchOfAnnotationNominalC implements FeatCalculator<String, Annotation, DocumentCtx> {

	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";

	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;

	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation.
	 * 
	 * The three arguments that have a name starting with fratureFilter are devoted to filter by a feature name and value the annotations to consider.
	 * If featureFilterName is null or empty no filter is performed.
	 * If featureFilterName is not null or empty, only annotations having a feature with that name are considered.
	 * If also featureFilterValue is not null or empty, only annotations having a feature name equal to featureFilterName and
	 * a feature value equal to featureFilterValue are considered.
	 * If featureFilterStartsWith is true, it is only checked that the feature value starts with the featureFilterValue string.
	 * 
	 * @param caseInsensitiveMatch
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param featureFilterName
	 * @param featureFilterValue
	 * @param featureFilterStartsWith
	 */
	public DERIV_RelativePositionOfFirstMatchOfAnnotationNominalC(
			String tokenAnnotationSet, String tokenAnnotationName,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith) {
		super();
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureFilterName = featureFilterName;
		this.featureFilterValue = featureFilterValue;
		this.featureFilterStartsWith = featureFilterStartsWith;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {

		String returnValue = null;

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

					// I have matched an annotation with tokenAnnotationSet and tokenAnnotationName and passed the feature filter if any

					// Compute the relative position in the sentence of the annotaiton
					Long startOffsetOfSentenceAnnotation = obj.getStartNode().getOffset();
					Long endOffsetOfSentenceAnnotation = obj.getEndNode().getOffset();

					Long startOffsetOfInternalAnnotation = annotInt.getStartNode().getOffset();
					Long endOffsetOfInternalAnnotation = annotInt.getEndNode().getOffset();

					if(!foundFisrstOccurrence &&
							startOffsetOfSentenceAnnotation !=  null && startOffsetOfSentenceAnnotation >= 0l &&
							endOffsetOfSentenceAnnotation !=  null && endOffsetOfSentenceAnnotation >= 0l &&
							startOffsetOfInternalAnnotation !=  null && startOffsetOfInternalAnnotation >= 0l &&
							endOffsetOfInternalAnnotation !=  null && endOffsetOfInternalAnnotation >= 0l &&
							startOffsetOfSentenceAnnotation < endOffsetOfInternalAnnotation &&
							startOffsetOfInternalAnnotation < endOffsetOfInternalAnnotation && 
							startOffsetOfSentenceAnnotation <= startOffsetOfInternalAnnotation) {
						Double sentenceLength = endOffsetOfSentenceAnnotation.doubleValue() - startOffsetOfSentenceAnnotation.doubleValue();
						Double internalAnnotationMiddleOffset = (startOffsetOfInternalAnnotation.doubleValue() - startOffsetOfSentenceAnnotation.doubleValue()) + ( (endOffsetOfInternalAnnotation.doubleValue() - startOffsetOfInternalAnnotation.doubleValue()) / 2d );

						if(sentenceLength > 0d) {
							Double percPosition = round( ( internalAnnotationMiddleOffset / sentenceLength ), 4) * 100d;
							foundFisrstOccurrence = true;

							if(percPosition <= 20d) {
								returnValue = "Beginning";
							}
							else if(percPosition > 20d && percPosition < 80d) {
								returnValue = "Middle";
							}
							else if(percPosition >= 80d) {
								returnValue = "End";
							}
							
							break;
						}
					}
				}
			}
		}

		MyString nomRetValue = new MyString("NoCit");
		
		if(StringUtils.isNotBlank(returnValue)) {
			nomRetValue.setValue(returnValue);
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((nomRetValue.getValue() != null) ? nomRetValue.getValue() : "") + "");
		}

		return nomRetValue;
	}


	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
