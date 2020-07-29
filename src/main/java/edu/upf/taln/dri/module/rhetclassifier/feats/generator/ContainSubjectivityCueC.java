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

import java.util.ArrayList;
import java.util.List;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder.PosENUM;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder.SubjectivityElem;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder.SubjectivityReader;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder.SubjectivityTypeENUM;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Check if the text contains any subjectivity cue
 * 
 *
 */
public class ContainSubjectivityCueC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private boolean booleanOutput = false;
	private String tokenAnnotationSet;
	private String tokenAnnotationName;
	private String featureName;
	private String posFeature;
	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;

	/**
	 * The list of words to match is matched against the whole text taken from the sentence annotation.
	 * 
	 * @param listOfWordsToMathc
	 * @param caseInsensitiveMatch
	 * @param booleanOutput
	 */
	public ContainSubjectivityCueC(boolean booleanOutput,
			String tokenAnnotationSet, String tokenAnnotationName,
			String featureName, String posFeature,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith) {
		super();
		this.booleanOutput = booleanOutput;

		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = featureName;
		this.posFeature = posFeature;
		this.featureFilterName = featureFilterName;
		this.featureFilterValue = featureFilterValue;
		this.featureFilterStartsWith = featureFilterStartsWith;

		SubjectivityReader.init();
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {

		Double returnValue = 0d;

		try {
			// Get all tokens
			AnnotationSet intersectTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
			List<Annotation> intersectingTokensOrdered  = gate.Utils.inDocumentOrder(intersectTokens);

			if(intersectingTokensOrdered != null && intersectingTokensOrdered.size() > 0) {

				for(Annotation annotInt : intersectingTokensOrdered) {
					if(annotInt != null) {
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
						
						
						String pos = (annotInt.getFeatures() != null && annotInt.getFeatures().containsKey(this.posFeature) &&
								annotInt.getFeatures().get(this.posFeature) != null &&
								!((String) annotInt.getFeatures().get(this.posFeature)).equals("") ) ? (String) annotInt.getFeatures().get(this.posFeature) : "";
						String text = (annotInt.getFeatures() != null && annotInt.getFeatures().containsKey(this.featureName) &&
								annotInt.getFeatures().get(this.featureName) != null &&
								!((String) annotInt.getFeatures().get(this.featureName)).equals("") ) ? (String) annotInt.getFeatures().get(this.featureName) : "";
						
						if(!pos.equals("") && !text.equals("")) {
							
							// Retrieve subjectivity cues with the pos of the analyzed token
							List<SubjectivityElem> subjectivityClues = new ArrayList<SubjectivityElem>();
							if(pos.trim().toLowerCase().startsWith("v")) {
								subjectivityClues = SubjectivityReader.getSubjectivityElemOfTerm(text.trim().toLowerCase(), PosENUM.verb);
							}
							else if(pos.trim().toLowerCase().startsWith("nn")) {
								subjectivityClues = SubjectivityReader.getSubjectivityElemOfTerm(text.trim().toLowerCase(), PosENUM.noun);
							}
							else if(pos.trim().toLowerCase().startsWith("rb")) {
								subjectivityClues = SubjectivityReader.getSubjectivityElemOfTerm(text.trim().toLowerCase(), PosENUM.adverb);
							}
							else if(pos.trim().toLowerCase().startsWith("jj")) {
								subjectivityClues = SubjectivityReader.getSubjectivityElemOfTerm(text.trim().toLowerCase(), PosENUM.adj);
							}
							else {
								subjectivityClues = SubjectivityReader.getSubjectivityElemOfTerm(text.trim().toLowerCase(), null);
							}
							
							if(subjectivityClues != null && subjectivityClues.size() > 0) {
								for(SubjectivityElem sbjElem : subjectivityClues) {
									if(sbjElem != null) {
										/*
										System.out.println("Analyzing text: " + text + " with pos: " + pos);
										System.out.println("Subj cue found: " + sbjElem.toString());
										*/
										returnValue += (sbjElem.getSubjType() != null && sbjElem.getSubjType().equals(SubjectivityTypeENUM.STRONG_SUBJ)) ? 1d : 0.5d;
									}
								}
							}
						}
					}
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
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
