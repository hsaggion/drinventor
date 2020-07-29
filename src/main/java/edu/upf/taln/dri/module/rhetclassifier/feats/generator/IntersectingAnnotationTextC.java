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

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;
import gate.util.InvalidOffsetException;
import weka.core.Stopwords;

/**
 * Get the text of intersecting annotations (separated by a space) from a given annotation set, of a given type
 * and eventually in a specific feature with a specific String value.
 * 
 *
 */
public class IntersectingAnnotationTextC implements FeatCalculator<String, Annotation, DocumentCtx> {

	private String tokenAnnotationSet = ImporterBase.driAnnSet;
	private String tokenAnnotationName = ImporterBase.tokenAnnType;
	private String featureName = "";
	private String featureValue = "";
	private boolean startsWith = false;
	private String addFeatureValue = "";
	private boolean removeStopwords = false;
	private boolean excludeCitSpan = false;
	
	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation
	 * If feature name is not null or empty:
	 *  - if feature value is not null or empty, all annotation with that feature name and value are considered in the text output building
	 *  - if feature value is empty, all the annotation having a feature with that name are considered in the count
	 * The text is composed of the main annotation text if the addFeatureValue argument is null or empty, if not it is composed of the value of 
	 * the feature with name addFeatureValue.
	 * If excludeCitSpan is true, the token included in an inline citation span are not considered.
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param filterFeatureName
	 * @param filterFeatureValue
	 * @param startsWith
	 * @param addFeatureValue
	 * @param excludeCitSpan
	 */
	public IntersectingAnnotationTextC(String tokenAnnotationSet, String tokenAnnotationName, String filterFeatureName, String filterFeatureValue, boolean startsWith, String addFeatureValue, boolean removeStopwords, boolean excludeCitSpan) {
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = filterFeatureName;
		this.featureValue = filterFeatureValue;
		this.startsWith = startsWith;
		this.addFeatureValue = addFeatureValue;
		this.removeStopwords = removeStopwords;
		this.excludeCitSpan = excludeCitSpan;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		AnnotationSet intersectingCitSpans = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		List<Annotation> intersectingCitSpansOrdered  = gate.Utils.inDocumentOrder(intersectingCitSpans);
		
		AnnotationSet intersectTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		List<Annotation> intersectingTokensOrdered  = gate.Utils.inDocumentOrder(intersectTokens);
		
		MyString retValue = new MyString("");

		// Add annotation text or feature value
		boolean addFeatureValue = (StringUtils.isNotBlank(this.addFeatureValue)) ? true : false; 
		
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
					

					String annotIntText = "";
					try {
						annotIntText = doc.getGateDoc().getContent().getContent(annotInt.getStartNode().getOffset(), annotInt.getEndNode().getOffset()).toString();
					} catch (InvalidOffsetException e) {
						e.printStackTrace();
					}

					// Check if to filter or not also by feature name and value
					if( StringUtils.isBlank(this.featureName) ) {
						
						if(!addFeatureValue) {
							if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(annotIntText)) ) {
								retValue.setValue(retValue.getValue() + annotIntText + " ");
							}
						}
						else {
							String featVal = (annotInt.getFeatures().containsKey(this.addFeatureValue) && !((String) annotInt.getFeatures().get(this.addFeatureValue)).equals("")) ? ((String) annotInt.getFeatures().get(this.addFeatureValue)) + " " : " ";
							if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(featVal)) ) {
								retValue.setValue(retValue.getValue() + featVal);
							}
						}
						
					}
					else {
						boolean checkAlsoValue = false;
						if( this.featureValue != null || !this.featureValue.equals("") ) {
							checkAlsoValue = true;
						}

						if(annotInt.getFeatures().containsKey(this.featureName)) {
							if(checkAlsoValue) {
								String featureValue = (String) annotInt.getFeatures().get(this.featureName);
								
								if(featureValue != null && this.startsWith && featureValue.startsWith(this.featureValue)) {
									
									if(!addFeatureValue) {
										if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(annotIntText)) ) {
											retValue.setValue(retValue.getValue() + annotIntText + " ");
										}
									}
									else {
										String featVal = (annotInt.getFeatures().containsKey(this.addFeatureValue) && !((String) annotInt.getFeatures().get(this.addFeatureValue)).equals("")) ? ((String) annotInt.getFeatures().get(this.addFeatureValue)) + " " : " ";
										if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(featVal)) ) {
											retValue.setValue(retValue.getValue() + featVal);
										}
									}
									
								}
								else if(featureValue != null && featureValue.equals(this.featureValue)) {
									
									if(!addFeatureValue) {
										if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(annotIntText)) ) {
											retValue.setValue(retValue.getValue() + annotIntText + " ");
										}
									}
									else {
										String featVal = (annotInt.getFeatures().containsKey(this.addFeatureValue) && !((String) annotInt.getFeatures().get(this.addFeatureValue)).equals("")) ? ((String) annotInt.getFeatures().get(this.addFeatureValue)) + " " : " ";
										if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(featVal)) ) {
											retValue.setValue(retValue.getValue() + featVal);
										}
									}
									
								}
							}
							else {
								
								if(!addFeatureValue) {
									if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(annotIntText)) ) {
										retValue.setValue(retValue.getValue() + annotIntText + " ");
									}
								}
								else {
									String featVal = (annotInt.getFeatures().containsKey(this.addFeatureValue) && !((String) annotInt.getFeatures().get(this.addFeatureValue)).equals("")) ? ((String) annotInt.getFeatures().get(this.addFeatureValue)) + " " : " ";
									if( !this.removeStopwords || (this.removeStopwords && !Stopwords.isStopword(featVal)) ) {
										retValue.setValue(retValue.getValue() + featVal);
									}
								}
								
							}
						}
					}
				}
			}
		}

		retValue.setValue(retValue.getValue().trim());
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}

}
