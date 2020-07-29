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
import java.util.Set;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;
import gate.util.InvalidOffsetException;

/**
 * Match the words in the annotation against a list provided by the constructor and return the number of tokens that matched one word in the list
 * 
 *
 */
public class ContainWordsInListC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private Set<String> listOfWordsToMathc;
	private boolean caseInsensitiveMatch = false;
	private boolean booleanOutput = false;
	
	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";
	private String featureName = "";
	
	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;
	private boolean excludeCitSpan = false;
	

	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation.
	 * If feature name is null or empty the match against the words is performed considering the annotation text.
	 * If feature name is not null or empty all annotation with that feature name and a value that matches against the words are considered in the match count.
	 * If booleanOutput is set to true, the output is equal to 1 if there is at least one match of a word, if not 0
	 * 
	 * The three arguments that have a name starting with fratureFilter are devoted to filter by a feature name and value the annotations to consider.
	 * If featureFilterName is null or empty no filter is performed.
	 * If featureFilterName is not null or empty, only annotations having a feature with that name are considered in the count.
	 * If also featureFilterValue is not null or empty, only annotations having a feature name equal to featureFilterName and
	 * a feature value equal to featureFilterValue are considered in the count.
	 * If featureFilterStartsWith is true, it is only checked that the feature value starts with the featureFilterValue string.
	 * 
	 * @param listOfWordsToMathc
	 * @param caseInsensitiveMatch
	 * @param booleanOutput
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param featureName
	 * @param featureFilterName
	 * @param featureFilterValue
	 * @param featureFilterStartsWith
	 * @param excludeCitSpan
	 */
	public ContainWordsInListC(Set<String> listOfWordsToMathc,
			boolean caseInsensitiveMatch,
			boolean booleanOutput,
			String tokenAnnotationSet, String tokenAnnotationName,
			String featureName,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith, boolean excludeCitSpan) {
		super();
		this.listOfWordsToMathc = listOfWordsToMathc;
		this.caseInsensitiveMatch = caseInsensitiveMatch;
		this.booleanOutput = booleanOutput;
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = featureName;
		this.featureFilterName = featureFilterName;
		this.featureFilterValue = featureFilterValue;
		this.featureFilterStartsWith = featureFilterStartsWith;
		this.excludeCitSpan = excludeCitSpan;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		
		Double returnValue = 0d;
		
		if(listOfWordsToMathc != null && listOfWordsToMathc.size() > 0) {
			
			AnnotationSet intersectingCitSpans = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
			List<Annotation> intersectingCitSpansOrdered  = gate.Utils.inDocumentOrder(intersectingCitSpans);
			
			AnnotationSet intersectTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
			List<Annotation> intersectingTokensOrdered  = gate.Utils.inDocumentOrder(intersectTokens);
			
			if(intersectingTokensOrdered != null && intersectingTokensOrdered.size() > 0) {
				
				for(Annotation annotInt : intersectingTokensOrdered) {
					if(annotInt != null) {
						
						// Do not consider the token if inside a citation span (Analysis --> CitSpan annotation)
						if(this.excludeCitSpan) {
							boolean tokenInsideCitSpan = false;
							if(intersectingCitSpansOrdered != null && intersectingCitSpansOrdered.size() > 0) {
								for(Annotation intersectingCitSpan : intersectingCitSpansOrdered) {
									if(intersectingCitSpan != null && 
											annotInt.getStartNode().getOffset() >= intersectingCitSpan.getStartNode().getOffset() && annotInt.getEndNode().getOffset() <= intersectingCitSpan.getEndNode().getOffset()) {
										tokenInsideCitSpan = true;
										break;
									}
								}
							}
							
							if(tokenInsideCitSpan) {
								continue;
							}
						}
						
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
						
						
						String textToMatch = "";
						try {
							if(featureName == null || featureName.equals("")) {
								textToMatch = doc.getGateDoc().getContent().getContent(annotInt.getStartNode().getOffset(), annotInt.getEndNode().getOffset()).toString();
							}
							else if(annotInt.getFeatures() != null && annotInt.getFeatures().containsKey(featureName) &&
									annotInt.getFeatures().get(featureName) != null &&
									!((String) annotInt.getFeatures().get(featureName)).equals("") ) {
								textToMatch = (String) annotInt.getFeatures().get(featureName);
							}
							
						} catch (InvalidOffsetException e) {
							e.printStackTrace();
						}

						// In textToMatch there is the text we want to match
						if(textToMatch != null && !textToMatch.equals("")) {
							textToMatch = (caseInsensitiveMatch) ? textToMatch.trim().toLowerCase() : textToMatch.trim();
							
							for(String wordToMatch : listOfWordsToMathc) {
								if(textToMatch.equals(wordToMatch)) {
									returnValue += 1d;
								}
							}
						}
						
					}
				}
			}
		}
		
		if(booleanOutput && returnValue > 0d) {
			returnValue = 1d;
		}
		
		MyDouble retValue = new MyDouble(returnValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
