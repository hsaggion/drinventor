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
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;

/**
 * Count the number of dependency relations eventually by type. If the depRelType is not null or empty the total number of
 * dependency relations of that type is divided by the total number of dependency relations of the sentence.
 * 
 *
 */
public class NumberOfDependencyRelationsByTypeC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private String depRelType = "";
	private boolean startsWith = false;
	private boolean percentage = false;
	
	/**
	 * 
	 *  @param depRelType if not null or empty only the dependency relations of that type are considered and the number is divided by the total number of dependency relaiton in the sentence
	 * @param startsWith
	 * @param percentage
	 */
	public NumberOfDependencyRelationsByTypeC(String depRelType, boolean startsWith, boolean percentage) {
		this.depRelType = depRelType;
		this.startsWith = startsWith;
		this.percentage = percentage;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		Double retValue = 0d;
		
		Double totalDepRels = 0d;
		Double matchingDepRels = 0d;
		
		List<Annotation> tokensOrdered = gate.Utils.inDocumentOrder(doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(
				obj.getStartNode().getOffset(), obj.getEndNode().getOffset()));
		
		if(!CollectionUtils.isEmpty(tokensOrdered)) {
			for(Annotation token : tokensOrdered) {
				if(token != null) {
					String depFunct = (token.getFeatures().containsKey(MateParser.depKindFeat) && token.getFeatures().get(MateParser.depKindFeat) != null) ? (String) token.getFeatures().get(MateParser.depKindFeat) : "";
					Integer depTargetId = (token.getFeatures().containsKey(MateParser.depTargetIdFeat) && token.getFeatures().get(MateParser.depTargetIdFeat) != null) ? (Integer) token.getFeatures().get(MateParser.depTargetIdFeat): null;
					
					if(StringUtils.isNotBlank(depFunct) && depTargetId != null) {
						totalDepRels++;
						
						if(this.depRelType == null || this.depRelType.equals("")) {
							matchingDepRels += 1d;
						}
						else if(this.startsWith && depFunct.startsWith(this.depRelType)) {
							matchingDepRels += 1d;
						}
						else if(!this.startsWith && depFunct.equals(this.depRelType)) {
							matchingDepRels += 1d;
						}
					}
				}
			}
		}
		
		if(percentage) {
			retValue = (totalDepRels > 0d) ? (matchingDepRels / totalDepRels) : 0d;
			retValue = round(retValue, 4) * 100d;
		}
		else {
			retValue = matchingDepRels;
		}
		
		
		MyDouble retValDouble = new MyDouble(retValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValDouble.getValue() != null) ? retValDouble.getValue() : "") + "");
		}
		
		return retValDouble;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

}
