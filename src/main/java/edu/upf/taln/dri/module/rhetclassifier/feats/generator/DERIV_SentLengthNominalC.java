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
import java.util.List;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;


/**
 * Get the number of intersecting annotations from a given annotation set, of a given type and eventyally evin a specific feature with a specific String value.
 * 
 *
 */
public class DERIV_SentLengthNominalC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";
	private String featureName = "";
	private String featureValue = "";
	private boolean startsWith = false;
	private boolean excludeCitSpan = false;
	
	/**
	 * The annotation set and the annotation name to check for intersection with the main annotation
	 * If feature name is not null or empty:
	 *  - if feature value is null, all annotatio with that feature name are considered in the count
	 *  - if feature value is not null or empty, all annotation with that feature name and value are considered in the count
	 *  - if feature value is empty, all the annotation having a feature with that name are considered in the count
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param filterFeatureName
	 * @param filterFeatureValue
	 * @param excludeCitSpan
	 */
	public DERIV_SentLengthNominalC(String tokenAnnotationSet, String tokenAnnotationName, String filterFeatureName, String filterFeatureValue, 
			boolean startsWith, boolean excludeCitSpan) {
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = filterFeatureName;
		this.featureValue = filterFeatureValue;
		this.startsWith = startsWith;
		this.excludeCitSpan = excludeCitSpan;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		AnnotationSet intersectingCitSpans = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		List<Annotation> intersectingCitSpansOrdered  = gate.Utils.inDocumentOrder(intersectingCitSpans);
		
		AnnotationSet intersectingTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		MyString retValue = new MyString("Short");
		
		Double totalIntersecting = 0d;
		if(intersectingTokens != null) {
			// Check if to filter or not also by feature name and value
			if( this.featureName == null || this.featureName.equals("") ) {
				totalIntersecting = new Double(intersectingTokens.size());
			}
			else {
				boolean checkAlsoValue = false;
				if( this.featureValue != null || !this.featureValue.equals("") ) {
					checkAlsoValue = true;
				}
				
				Iterator<Annotation> intersectingTokensIter = intersectingTokens.iterator();
				while(intersectingTokensIter.hasNext()) {
					Annotation annotInt = intersectingTokensIter.next();
					
					if(annotInt == null) {
						continue;
					}
					
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
					
					if(this.featureName == null || this.featureName.equals("")) {
						totalIntersecting = totalIntersecting + 1d;
					}
					else {
						if(annotInt.getFeatures().containsKey(this.featureName)) {
							if(checkAlsoValue) {
								String featureValue = (String) annotInt.getFeatures().get(this.featureName);
								
								if(featureValue != null && this.startsWith && featureValue.startsWith(this.featureValue)) {
									totalIntersecting = totalIntersecting + 1d;
								}
								else if(featureValue != null && featureValue.equals(this.featureValue)) {
									totalIntersecting = totalIntersecting + 1d;
								}
							}
							else {
								totalIntersecting = totalIntersecting + 1d;
							}
						}
					}
					
				}
			}
			
			if(totalIntersecting <= 15d) {
				retValue.setValue("Short");
			}
			else if(totalIntersecting > 15d && totalIntersecting <= 30d) {
				retValue.setValue("Medium");
			}
			else if(totalIntersecting > 30d) {
				retValue.setValue("Long");
			}
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
