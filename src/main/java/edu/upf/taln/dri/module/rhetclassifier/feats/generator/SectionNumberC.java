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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get a Double from 0 to 100 that reflects the position of the annotation (central offset ID) with respect to the section the sentence belongs to.
 * A section is the set of sentences between two consecutive main headers (h1).
 * 
 *
 */
public class SectionNumberC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private boolean considerOnlyTopLevelHeaders = true;
	
	/**
	 * If folderNumber is greater than 0, the section length is divided in folderNumber different folders of equal size and
	 * the id from 1 to folderNumber equal to the folder where the annotation is placed is returned
	 * 
	 * @param folderNumber
	 * @param considerOnlyTopLevelHeaders
	 */
	/**
	 * If considerOnlyTopLevelHeaders is true, only h1 headers are considered in the section number count
	 * 
	 * @param considerOnlyTopLevelHeaders
	 */
	public SectionNumberC(boolean considerOnlyTopLevelHeaders) {
		super();
		this.considerOnlyTopLevelHeaders = considerOnlyTopLevelHeaders;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		Double doubleRetValue = null;

		try {

			// Get the start and end offsets of the section the sentence belongs to (between two h1)
			AnnotationSet originalMK = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet);
			Map<Long, Annotation> treeMap = null;
			if(originalMK != null && originalMK.size() > 0) {
				AnnotationSet originalMK_h1 = originalMK.get(ImporterBase.h1AnnType);
				AnnotationSet originalMK_h2 = originalMK.get(ImporterBase.h2AnnType);
				AnnotationSet originalMK_h3 = originalMK.get(ImporterBase.h3AnnType);
				AnnotationSet originalMK_h4 = originalMK.get(ImporterBase.h4AnnType);
				AnnotationSet originalMK_h5 = originalMK.get(ImporterBase.h5AnnType);
				
				Map<Long, Annotation> orderedAnnotation = new HashMap<Long, Annotation>();
				// Add h1 to map
				Iterator<Annotation> originalMK_h1_ITER = originalMK_h1.iterator();
				while(originalMK_h1_ITER.hasNext()) {
					Annotation ann = originalMK_h1_ITER.next();

					if(ann != null) {
						orderedAnnotation.put(ann.getStartNode().getOffset(), ann);
					}
				}

				if(!this.considerOnlyTopLevelHeaders) {
					// Add h2 to map
					Iterator<Annotation> originalMK_h2_ITER = originalMK_h2.iterator();
					while(originalMK_h2_ITER.hasNext()) {
						Annotation ann = originalMK_h2_ITER.next();

						if(ann != null) {
							orderedAnnotation.put(ann.getStartNode().getOffset(), ann);
						}
					}

					// Add h3 to map
					Iterator<Annotation> originalMK_h3_ITER = originalMK_h3.iterator();
					while(originalMK_h3_ITER.hasNext()) {
						Annotation ann = originalMK_h3_ITER.next();

						if(ann != null) {
							orderedAnnotation.put(ann.getStartNode().getOffset(), ann);
						}
					}
					
					// Add h4 to map
					Iterator<Annotation> originalMK_h4_ITER = originalMK_h4.iterator();
					while(originalMK_h4_ITER.hasNext()) {
						Annotation ann = originalMK_h4_ITER.next();

						if(ann != null) {
							orderedAnnotation.put(ann.getStartNode().getOffset(), ann);
						}
					}
					
					// Add h5 to map
					Iterator<Annotation> originalMK_h5_ITER = originalMK_h5.iterator();
					while(originalMK_h5_ITER.hasNext()) {
						Annotation ann = originalMK_h5_ITER.next();

						if(ann != null) {
							orderedAnnotation.put(ann.getStartNode().getOffset(), ann);
						}
					}
				}

				treeMap = new TreeMap<Long, Annotation>(orderedAnnotation);
			}

			if(treeMap != null && treeMap.size() > 0) {
				
				doubleRetValue = 1d;
				for(Entry<Long, Annotation> entry : treeMap.entrySet()) {
					if(entry != null && entry.getValue() != null) {
						Long sectionStartOffset = entry.getKey();
						if(sectionStartOffset > obj.getStartNode().getOffset() ) {
							break;
						}
						doubleRetValue += 1d;
					}
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(doubleRetValue == null || doubleRetValue < 0d) {
			doubleRetValue = 1d;
		}

		MyDouble retValue = new MyDouble(doubleRetValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}
	
	
	public static void main(String[] args) {
		
		Map<Long, String> mapN = new HashMap<Long, String>();
		mapN.put(1l, "");
		mapN.put(5l, "");
		mapN.put(3l, "");
		mapN.put(4l, "");
		
		TreeMap<Long, String> treeMap = new TreeMap<Long, String>(mapN);
		
		for(Entry<Long, String> treeMapEntry : treeMap.entrySet()) {
			System.out.println(treeMapEntry.getKey() + " --> " + treeMapEntry.getValue());
		}
		
	}

}
