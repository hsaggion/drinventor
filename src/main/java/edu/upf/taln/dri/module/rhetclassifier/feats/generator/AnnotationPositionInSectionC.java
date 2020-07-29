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

import java.util.Comparator;
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
public class AnnotationPositionInSectionC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private Integer folderNumber = 0;
	private boolean considerOnlyTopLevelHeaders = true;
	private boolean unequal = false;
	
	/**
	 * If folderNumber is greater than 0, the section length is divided in folderNumber different folders of equal size and
	 * the id from 1 to folderNumber equal to the folder where the annotation is placed is returned
	 * 
	 * @param folderNumber
	 * @param considerOnlyTopLevelHeaders
	 */
	/**
	 * If folderNumber is greater than 0, the section length is divided in folderNumber different folders of equal size and
	 * the id from 1 to folderNumber equal to the folder where the annotation is placed is returned
	 * 
	 * If unequal is true the section is divided into 5 equal sized folders (folderNumber = 5) and a number from 1 to 5
	 * is returned if (original number ---> returned number):
	 * 1 ---> first sentence of the section
	 * 2 ---> sentences from the first fifth of the section, after the first one
	 * 3 ---> sentences from the second, third and fourth fifth of the section
	 * 4 ---> sentences from the fifth fifth of the section, excluding the last one
	 * 5 ---> last sentence of the section
	 * 
	 * @param folderNumber
	 * @param unequal
	 * @param considerOnlyTopLevelHeaders
	 */
	public AnnotationPositionInSectionC(Integer folderNumber, boolean unequal, boolean considerOnlyTopLevelHeaders) {
		super();
		this.folderNumber = (folderNumber != null && folderNumber > 0) ? folderNumber : 0;
		this.considerOnlyTopLevelHeaders = considerOnlyTopLevelHeaders;
		this.unequal = unequal;
		
		if(this.unequal) {
			this.folderNumber = 5;
		}
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		Double doubleRetValue = null;

		// Retrieve the end offset of the abstract - START
		Long abstractEndOffset = Long.MIN_VALUE;

		AnnotationSet abstractAnnotationSet = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.abstractAnnType);
		if(abstractAnnotationSet != null && abstractAnnotationSet.size() == 1) {
			Annotation abstractAnnotation = gate.Utils.inDocumentOrder(abstractAnnotationSet).get(0);
			if(abstractAnnotation.getEndNode() != null && abstractAnnotation.getEndNode().getOffset() != null) {
				abstractEndOffset = abstractAnnotation.getEndNode().getOffset();
			}
		}
		// Retrieve the end offset of the abstract - END

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

				treeMap = new TreeMap<Long, Annotation>(
						new Comparator<Long>() {

							@Override
							public int compare(Long o1, Long o2) {
								return o1.compareTo(o2);
							}

						});
				treeMap.putAll(orderedAnnotation);
			}

			if(treeMap != null && treeMap.size() > 0) {
				Long sectionStartOffset = 0l;
				Long sectionEndOffset = gate.Utils.lengthLong(doc.getGateDoc());

				Long distanceBefore = Long.MAX_VALUE;
				Long distanceAfter = Long.MAX_VALUE;
				for(Entry<Long, Annotation> entry : treeMap.entrySet()) {
					if(entry != null && entry.getValue() != null) {

						Long headerStartOffset = entry.getValue().getStartNode().getOffset();
						Long headerEndOffset = entry.getValue().getStartNode().getOffset();

						// Check if the header annotation is a new section start offset
						if(headerEndOffset != null && headerEndOffset <= obj.getStartNode().getOffset()) {
							Long newDistanceBefore = obj.getStartNode().getOffset() - headerEndOffset;
							if(newDistanceBefore < distanceBefore) {
								distanceBefore = newDistanceBefore;
								sectionStartOffset = headerEndOffset;
							}
						}

						// Check if the header annotation is a new section end offset
						if(headerStartOffset != null && headerStartOffset >= obj.getEndNode().getOffset()) {
							Long newDistanceAfter = headerStartOffset - obj.getEndNode().getOffset();
							if(newDistanceAfter < distanceAfter) {
								distanceAfter = newDistanceAfter;
								sectionEndOffset = headerStartOffset;
							}
						}
					}
				}

				// Here I have the sectionStartOffset and sectionEndOffset variables correctly set 
				// to the start and end offset of the section of the considered sentence
				if(sectionStartOffset != null && sectionEndOffset != null && sectionStartOffset < sectionEndOffset &&
						sectionStartOffset <= obj.getStartNode().getOffset() && sectionEndOffset >= obj.getEndNode().getOffset()) {

					Double middleOffset = obj.getStartNode().getOffset().doubleValue() + ( (obj.getEndNode().getOffset().doubleValue() - obj.getStartNode().getOffset().doubleValue()) / 2d );

					middleOffset = middleOffset - sectionStartOffset.doubleValue();

					doubleRetValue = round( ( middleOffset / (sectionEndOffset.doubleValue() - sectionStartOffset.doubleValue()) ), 4) * 100d;

					if(folderNumber != null && folderNumber > 0) {
						doubleRetValue = ( middleOffset / (sectionEndOffset.doubleValue() - sectionStartOffset.doubleValue()) ) * (folderNumber.doubleValue());
						doubleRetValue = (double) (doubleRetValue.intValue() + (int) 1);
					}
					
					// Generating unequal sized segments
					if(this.unequal) {
						if( (sectionStartOffset + 7l) >= obj.getStartNode().getOffset() ) { // In the first sentence of the section
							doubleRetValue = 1d;
						}
						else if( (sectionEndOffset - 7l) <= obj.getEndNode().getOffset() ) { // In the last sentence of the section
							doubleRetValue = 5d;
						}
						else if(doubleRetValue == 1d) {
							doubleRetValue = 2d;
						}
						else if(doubleRetValue == 5d) {
							doubleRetValue = 4d;
						}
						else {
							doubleRetValue = 3d;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(doubleRetValue == null) {
			System.out.println("NULL");
		}
		else {
			if(doubleRetValue < 0d) {
				doubleRetValue = null;
			}
		}

		MyDouble retValue = new MyDouble(doubleRetValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}

		return retValue;
	}
	
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

}
