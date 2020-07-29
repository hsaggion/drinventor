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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * Count the max depth of the dependency relation tree
 * 
 *
 */
public class TreeMaxDepthOfDependencyRelationsC implements FeatCalculator<Double, Annotation, DocumentCtx> {
	
	
	public TreeMaxDepthOfDependencyRelationsC() {
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		Integer maxDepth = 0;
		
		List<Annotation> tokensOrdered = gate.Utils.inDocumentOrder(doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(
				obj.getStartNode().getOffset(), obj.getEndNode().getOffset()));
		
		if(!CollectionUtils.isEmpty(tokensOrdered)) {
			for(Annotation token : tokensOrdered) {
				if(token != null) {
					String depFunct = (token.getFeatures().containsKey(MateParser.depKindFeat) && token.getFeatures().get(MateParser.depKindFeat) != null) ? (String) token.getFeatures().get(MateParser.depKindFeat) : "";
					Integer depTargetId = (token.getFeatures().containsKey(MateParser.depTargetIdFeat) && token.getFeatures().get(MateParser.depTargetIdFeat) != null) ? (Integer) token.getFeatures().get(MateParser.depTargetIdFeat): null;
					
					if(StringUtils.isNotBlank(depFunct) && depTargetId != null) {
						Set<Integer> visitTatgets = new HashSet<Integer>();
						visitTatgets.add(depTargetId);
						Integer branchDepth = getDepth(tokensOrdered, depTargetId, visitTatgets);
						if(branchDepth != null && branchDepth > maxDepth) {
							maxDepth = branchDepth;
						}
					}
					
				}
			}
		}
		
		// Increase maxDepth by 1 because the root node is not considered
		maxDepth = maxDepth + 1;
		
		MyDouble retValDouble = new MyDouble(maxDepth.doubleValue());
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValDouble.getValue() != null) ? retValDouble.getValue() : "") + "");
		}
		
		return retValDouble;
	}

	private Integer getDepth(List<Annotation> tokensOrdered, Integer depTarget, Set<Integer> visitedTargets) {
		Integer retVal = 1;
		
		if(!CollectionUtils.isEmpty(tokensOrdered)) {
			for(Annotation token : tokensOrdered) {
				if(token != null && token.getId().equals(depTarget)) {
					String depFunct = (token.getFeatures().containsKey(MateParser.depKindFeat) && token.getFeatures().get(MateParser.depKindFeat) != null) ? (String) token.getFeatures().get(MateParser.depKindFeat) : "";
					Integer depTargetId = (token.getFeatures().containsKey(MateParser.depTargetIdFeat) && token.getFeatures().get(MateParser.depTargetIdFeat) != null) ? (Integer) token.getFeatures().get(MateParser.depTargetIdFeat): null;
					if(StringUtils.isNotBlank(depFunct) && depTargetId != null && !visitedTargets.contains(depTargetId)) {
						visitedTargets.add(depTargetId);
						Integer branchDepth = getDepth(tokensOrdered, depTargetId, visitedTargets);
						retVal += branchDepth;
					}
					else if(visitedTargets.contains(depTargetId)) {
						System.out.println("Cyclic dep tree!");
					}
				}
			}
		}
		
		return retVal;
	}
	
}
