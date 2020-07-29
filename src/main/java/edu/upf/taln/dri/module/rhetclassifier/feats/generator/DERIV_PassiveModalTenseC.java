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

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.parser.MateParser;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;


/**
 * Retrieve if the main verb of the sentence is active or passive
 * 
 *
 */
public class DERIV_PassiveModalTenseC implements FeatCalculator<String, Annotation, DocumentCtx> {

	public enum PASSIVE_MODAL_TENSE {
		PASSIVE, MODAL, TENSE;
	} 

	private PASSIVE_MODAL_TENSE type = null;
	private String tokenAnnotationSet = "";
	private String tokenAnnotationName = "Token";

	/**
	 * Token annotation set and name
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 */
	public DERIV_PassiveModalTenseC(String tokenAnnotationSet, String tokenAnnotationName, PASSIVE_MODAL_TENSE type) {
		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.type = type;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		AnnotationSet intersectingTokens = doc.getGateDoc().getAnnotations(tokenAnnotationSet).get(tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());

		MyString retValue = new MyString("NoVerb");

		if(intersectingTokens != null) {

			List<Annotation> intersectingTokensOrdered = gate.Utils.inDocumentOrder(intersectingTokens);

			// Look for root token
			for(Annotation intersectingToken : intersectingTokensOrdered) {
				if(intersectingToken != null && intersectingToken.getId() != null) {
					String root_depFunct = GateUtil.getStringFeature(intersectingToken, MateParser.depKindFeat).orElse(null);
					String root_lemma = GateUtil.getStringFeature(intersectingToken, MateParser.lemmaFeat).orElse(null);
					String root_POS = GateUtil.getStringFeature(intersectingToken, MateParser.posFeat).orElse(null);

					if(root_depFunct != null && root_depFunct.toLowerCase().trim().equals("root")) {
						Annotation VC_firstLevel = getDepVC(intersectingTokensOrdered, intersectingToken.getId());
						String VC_firstLevel_lemma = GateUtil.getStringFeature(VC_firstLevel, MateParser.lemmaFeat).orElse(null);
						String VC_firstLevel_POS = GateUtil.getStringFeature(VC_firstLevel, MateParser.posFeat).orElse(null);

						Annotation VC_secondLevel = (VC_firstLevel != null && VC_firstLevel.getId() != null) ? getDepVC(intersectingTokensOrdered, VC_firstLevel.getId()): null;
						String VC_secondLevel_lemma = GateUtil.getStringFeature(VC_secondLevel, MateParser.lemmaFeat).orElse(null);
						String VC_secondLevel_POS = GateUtil.getStringFeature(VC_secondLevel, MateParser.posFeat).orElse(null);

						switch(this.type) {
						case PASSIVE:
							if(root_lemma != null && root_lemma.equals("be") && VC_firstLevel_POS != null && VC_firstLevel_POS.equals("VBN")) {
								retValue.setValue("Passive");
							}
							else {
								retValue.setValue("Active");
							}
							break;
						case MODAL:
							if(root_POS != null && root_POS.equals("MD")) {
								retValue.setValue("Modal");
							}
							else {
								retValue.setValue("NotModal");
							}
							break;
						case TENSE:
							if(root_POS != null && (root_POS.equals("VBP") || root_POS.equals("VBZ"))) {
								retValue.setValue("Present");
							}
							else if(root_POS != null && root_POS.equals("VBD")) {
								retValue.setValue("Past");
							}
							else if(root_POS != null && root_POS.equals("MD") && root_lemma != null && root_lemma.equals("will")
									&& VC_firstLevel_POS != null && VC_firstLevel_POS.startsWith("VB")) {
								retValue.setValue("Future");
							}
							break;
						default:
							retValue.setValue("NoVerb");
						}
					}
				}
			}

		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}

		return retValue;
	}


	/**
	 * Get a child of the dependency tree with link type equal to VC
	 * @param intersectingTokensOrdered
	 * @param parentID
	 * @return
	 */
	private static Annotation getDepVC(List<Annotation> intersectingTokensOrdered, int parentID) {
		if(intersectingTokensOrdered != null) {
			for(Annotation intersectingToken : intersectingTokensOrdered) {
				if(intersectingToken != null) {

					Integer targetId = GateUtil.getIntegerFeature(intersectingToken, MateParser.depTargetIdFeat).orElse(null);
					String depFunct = GateUtil.getStringFeature(intersectingToken, MateParser.depKindFeat).orElse(null);

					if(targetId != null && targetId.equals(parentID) && depFunct != null && depFunct.equals("VC")) {
						return intersectingToken;
					}

				}
			}
		}

		return null;
	}

}
