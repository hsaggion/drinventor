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

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get the type of the section this sentence is in
 * 
 *
 */
public class SectionTypeC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String class_REF = "NO_SECTION_TYPE";
		
		// Get all the h1 annotations preceding the section
		AnnotationSet originalMK = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet);
		if(originalMK != null && originalMK.size() > 0) {
			// Get all h1 annotations
			AnnotationSet originalMK_h1 = originalMK.get(ImporterBase.h1AnnType);
			
			Long startOffset_CurrentSentence = obj.getStartNode().getOffset();
			Long offsetDistance_REF = Long.MAX_VALUE;
			
			Iterator<Annotation> originalMK_h1_ITER = originalMK_h1.iterator();
			while(originalMK_h1_ITER.hasNext()) {
				Annotation annH1 = originalMK_h1_ITER.next();
				if(annH1 != null) {
					if(annH1.getEndNode().getOffset() <= startOffset_CurrentSentence) {
						Long currentOffsetDistance = startOffset_CurrentSentence - annH1.getEndNode().getOffset();
						
						if(currentOffsetDistance < offsetDistance_REF) {
							// Reset the section type
							class_REF = "NO_SECTION_TYPE";
							
							String annH1_text = null;
							try {
								annH1_text = doc.getGateDoc().getContent().getContent(annH1.getStartNode().getOffset(), annH1.getEndNode().getOffset()).toString();	
							}
							catch (Exception e) {
								// Do nothing
							}
							
							
							if(annH1_text != null && !annH1_text.equals("")) {
								annH1_text = annH1_text.trim().toLowerCase();
								
								// Determine the section class / category by analyzing the contents of the header (annH1_text)
								if(annH1_text.contains("abstract")) {
									class_REF = "ABSTRACT";
									offsetDistance_REF = currentOffsetDistance;									
								}
								else if(annH1_text.contains("introduction") || annH1_text.contains("overview")) {
									class_REF = "INTRO";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("background")) {
									class_REF = "BACKGROUND";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("implement") || annH1_text.contains("optimization")) {
									class_REF = "IMPLEMENTATION";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("method") || annH1_text.contains("algorithm")) {
									class_REF = "METHOD_ALGORITHM";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("result") || annH1_text.contains("evaluation")) {
									class_REF = "RESULT_EVALUTAITON";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("experiment")) {
									class_REF = "EXPERIMENT";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("discussion") || annH1_text.contains("conclusion") || annH1_text.contains("future work")) {
									class_REF = "DISCUSSION_CONCLUSION_FUTURE_WORK";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("model")) {
									class_REF = "MODEL_DESCRIPTION";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.contains("acknowledg")) {
									class_REF = "ACKNOLWEDGMENT";
									offsetDistance_REF = currentOffsetDistance;			
								}
								else if(annH1_text.equals("reference") || annH1_text.equals("references") || annH1_text.equals("bibliography")) {
									class_REF = "REFERENCES";
									offsetDistance_REF = currentOffsetDistance;			
								}
							}
						}
					}
				}
			}
		}
		
		MyString retValue = new MyString(class_REF.trim());
		
		// DEBUG: System.out.println("   > SECTION TYPE: " + class_REF);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
}
