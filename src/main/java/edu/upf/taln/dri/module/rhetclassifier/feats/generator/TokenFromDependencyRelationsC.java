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
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.parser.MateParser;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;

/**
 * Generate a string with the following info: DEPrelNAME_FROMlemma_TOlemma
 * 
 *
 */
public class TokenFromDependencyRelationsC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	
	public TokenFromDependencyRelationsC() {
		
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String retValue = "";
		
		List<Annotation> tokensOrdered = gate.Utils.inDocumentOrder(doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(
				obj.getStartNode().getOffset(), obj.getEndNode().getOffset()));
		
		if(!CollectionUtils.isEmpty(tokensOrdered)) {
			for(Annotation token : tokensOrdered) {
				if(token != null) {
					String depFunct = (token.getFeatures().containsKey(MateParser.depKindFeat) && token.getFeatures().get(MateParser.depKindFeat) != null) ? (String) token.getFeatures().get(MateParser.depKindFeat) : "";
					Integer depTargetId = (token.getFeatures().containsKey(MateParser.depTargetIdFeat) && token.getFeatures().get(MateParser.depTargetIdFeat) != null) ? (Integer) token.getFeatures().get(MateParser.depTargetIdFeat): null;
					
					if(StringUtils.isNotBlank(depFunct) && depTargetId != null) {
						for(Annotation targetToken : tokensOrdered) {
							if(targetToken != null && targetToken.getId().equals(depTargetId)) {
								
								String fromLemma = (token.getFeatures().containsKey(ImporterBase.token_LemmaFeat) && token.getFeatures().get(ImporterBase.token_LemmaFeat) != null) ? (String) token.getFeatures().get(ImporterBase.token_LemmaFeat) : "";
								String toLemma = (targetToken.getFeatures().containsKey(ImporterBase.token_LemmaFeat) && targetToken.getFeatures().get(ImporterBase.token_LemmaFeat) != null) ? (String) targetToken.getFeatures().get(ImporterBase.token_LemmaFeat) : "";
								
								if(StringUtils.isNotBlank(fromLemma) && StringUtils.isNotBlank(toLemma)) {
									if(retValue.length() > 0) {
										retValue += " ";
									}
									
									fromLemma = fromLemma.replace(" ", "_");
									toLemma = toLemma.replace(" ", "_");
									
									if(retValue.length() > 0) {
										retValue += " ";
									}
									retValue += depFunct + "___" + fromLemma + "___" + toLemma;
								}
							}
						}
					}
				}
			}
		}
		
		MyString retValString = new MyString(retValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValString.getValue() != null) ? retValString.getValue() : "") + "");
		}
		
		return retValString;
	}

}
