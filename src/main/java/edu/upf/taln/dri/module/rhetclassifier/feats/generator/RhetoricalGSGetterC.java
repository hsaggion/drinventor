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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;

/**
 * Get the GS rhetorical anntoation of the sentence
 * 
 *
 */
public class RhetoricalGSGetterC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	public enum CLASS_TYPE_RHET {DRI_Approach, DRI_Background, DRI_Challenge, DRI_FutureWork, DRI_Outcome, DRI_Unspecified, Sentence}

	private CLASS_TYPE_RHET classPerc = null;

	public RhetoricalGSGetterC(CLASS_TYPE_RHET classPerc) {
		super();
		this.classPerc = classPerc;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		Double percentage = 0d;
		String GS_value = "";
		String agreement = "";
		Map<String, Double> occMap = new HashMap<String, Double>();

		// Retrieve the gold standard value
		if(doc.getGateDoc() != null && doc.getGateDoc().getAnnotations("GoldStandard_RHETORICAL").size() > 0) {
			List<Annotation> goldStandardRhetorical = gate.Utils.inDocumentOrder(doc.getGateDoc().getAnnotations("GoldStandard_RHETORICAL").get(obj.getStartNode().getOffset(), obj.getEndNode().getOffset()));
			if(goldStandardRhetorical != null && goldStandardRhetorical.size() == 1) {
				Annotation goldStandardRhetAnnotation = goldStandardRhetorical.get(0);
				if(goldStandardRhetAnnotation != null) {
					GS_value = goldStandardRhetAnnotation.getType();

					// Get agreement values
					agreement = GateUtil.getStringFeature(goldStandardRhetAnnotation, "agreement").orElse(null);


					if(agreement.equals("All_Equal")) {
						occMap.put(GS_value, 3d);
					}
					else {
						try {
							for(Entry<Object, Object> feat : goldStandardRhetAnnotation.getFeatures().entrySet()) {
								if(feat != null && feat.getKey() != null) {
									String fName = (String) feat.getKey();
									if(fName.startsWith("ann") && feat.getValue() != null) {
										String fVal = (String) feat.getValue();
										if(fVal.startsWith("DRI_Outcome")) fVal = "DRI_Outcome";
										if(fVal.startsWith("DRI_Challenge")) fVal = "DRI_Challenge";
										
										if(occMap.containsKey(fVal)) {
											Double precValue = occMap.get(fVal);
											occMap.put(fVal, precValue + 1d);
										}
										else {
											occMap.put(fVal, 1d);
										}
									}
								}
							}
						}
						catch (Exception e) {
							System.out.println("Exception with GS rhet. class getter");
							/* Do nothing */
						}
					}
				}
			}
		}


		switch(this.classPerc) {
		case DRI_Approach:
			if(occMap.containsKey("DRI_Approach")) {
				Double frequenceVal = occMap.get("DRI_Approach");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case DRI_Background:
			if(occMap.containsKey("DRI_Background")) {
				Double frequenceVal = occMap.get("DRI_Background");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case DRI_Challenge:
			if(occMap.containsKey("DRI_Challenge")) {
				Double frequenceVal = occMap.get("DRI_Challenge");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case DRI_FutureWork:
			if(occMap.containsKey("DRI_FutureWork")) {
				Double frequenceVal = occMap.get("DRI_FutureWork");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case DRI_Outcome:
			if(occMap.containsKey("DRI_Outcome")) {
				Double frequenceVal = occMap.get("DRI_Outcome");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case DRI_Unspecified:
			if(occMap.containsKey("DRI_Unspecified")) {
				Double frequenceVal = occMap.get("DRI_Unspecified");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		case Sentence:
			if(occMap.containsKey("Sentence")) {
				Double frequenceVal = occMap.get("Sentence");
				if(frequenceVal != null) {
					percentage = frequenceVal / 3d;
				}
			}
			break;
		default:

		}

		MyDouble retValue = new MyDouble(percentage);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}

}
