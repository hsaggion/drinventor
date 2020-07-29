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
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;


/**
 * Get the number of intersecting annotations from a given annotation set, of a given type and eventyally evin a specific feature with a specific String value.
 * 
 *
 */
public class IntersectingGroupsOfCitationCountC implements FeatCalculator<Double, Annotation, DocumentCtx> {
	
	private String citationAnnotationSet = "";
	private String inlineCitationAnnotationName = "";
	private String inlineCitationMarkerAnnotationName = "";
	private boolean booleanOutput = false;
	
	/**
	 * Return the count of annotations of type inlineCitationAnnotationName overlapping more than one inlineCitationMarkerAnnotationName
	 * If booleanOutput is set to true, the output is equal to 1 if there is at least one match of a word, if not 0
	 * 
	 * @param citationAnnotationSet
	 * @param inlineCitationAnnotationName
	 * @param inlineCitationMarkerAnnotationName
	 */
	public IntersectingGroupsOfCitationCountC(String citationAnnotationSet, String inlineCitationAnnotationName, 
			String inlineCitationMarkerAnnotationName, boolean booleanOutput) {
		this.citationAnnotationSet = citationAnnotationSet;
		this.inlineCitationAnnotationName = inlineCitationAnnotationName;
		this.inlineCitationMarkerAnnotationName = inlineCitationMarkerAnnotationName;
		this.booleanOutput = booleanOutput;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		
		Double retValue = 0d;
		
		AnnotationSet intersectingInlineCitations = doc.getGateDoc().getAnnotations(citationAnnotationSet).get(inlineCitationAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		
		if(intersectingInlineCitations != null && intersectingInlineCitations.size() > 0) {
			for(Annotation inlineCitationAnn : intersectingInlineCitations) {
				try {
					if(inlineCitationAnn != null) {
						AnnotationSet intersectingInlineCitationMarkers = doc.getGateDoc().getAnnotations(citationAnnotationSet).get(
								inlineCitationMarkerAnnotationName).getContained(inlineCitationAnn.getStartNode().getOffset(), inlineCitationAnn.getEndNode().getOffset());
						if(intersectingInlineCitationMarkers != null && intersectingInlineCitationMarkers.size() > 1) {
							retValue += 1d;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if(booleanOutput && retValue > 0d) {
			retValue = 1d;
		}
		
		MyDouble retValDouble = new MyDouble(retValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValDouble.getValue() != null) ? retValDouble.getValue() : "") + "");
		}
		
		return retValDouble;
	}
	
}
