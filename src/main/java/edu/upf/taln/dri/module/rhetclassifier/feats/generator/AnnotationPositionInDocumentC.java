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

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get a Double from 0 to 100 that reflects the position of the annotation (central offset ID) with respect to the whole document.
 * 
 *
 */
public class AnnotationPositionInDocumentC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private boolean includeAbstractSentences = true;
	private boolean includeContentSentences = true;

	private Integer folderNumber = 0;
	private boolean unequal = false;

	/**
	 * If folderNumber is greater than 0, the document length is divided in folderNumber different folders of equal size and
	 * the id from 1 to folderNumber equal to the folder where the annotation is placed is returned
	 * 
	 * If unequal is true the document is divided into 20 equal sized folders (folderNumber = 20) and a number from 1 to 10
	 * is returned if (original number ---> returned number):
	 * 1 ---> 1
	 * 2 ---> 2
	 * 3 ---> 3
	 * 4 ---> 4
	 * 5 ---> 5
	 * 6 ---> 5
	 * 7 ---> 6
	 * 8 ---> 6
	 * 9 ---> 6
	 * 10 ---> 6
	 * 11 ---> 6
	 * 12 ---> 6
	 * 13 ---> 6
	 * 14 ---> 6
	 * 15 ---> 6
	 * 16 ---> 7
	 * 17 ---> 7
	 * 18 ---> 8
	 * 19 ---> 9
	 * 20 ---> 10
	 * (SEE pag. 180 of: http://www.cl.cam.ac.uk/~sht25/thesis/t.pdf)
	 * 
	 * @param folderNumber
	 * @param unequal
	 * @param includeAbstractSentences
	 * @param includeContentSentences
	 */
	public AnnotationPositionInDocumentC(boolean includeAbstractSentences, boolean includeContentSentences,
			Integer folderNumber, boolean unequal) {
		super();
		this.folderNumber = (folderNumber != null && folderNumber > 0) ? folderNumber : 0;
		this.unequal = unequal;
		
		this.includeAbstractSentences = includeAbstractSentences;
		this.includeContentSentences = includeContentSentences;
		
		if(this.unequal) {
			this.folderNumber = 20;
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
			Long totalDocStartOffset = (includeAbstractSentences) ? 0l : abstractEndOffset;
			Long totalDocEndOffset = (includeContentSentences) ? gate.Utils.lengthLong(doc.getGateDoc()) : abstractEndOffset;

			Long startOffsetOfAnnotation = obj.getStartNode().getOffset();
			Long endOffsetOfAnnotation = obj.getEndNode().getOffset();
			
			// If the sentence starts in the abstract and ends outside, temporarily set the end offset of the abstract equal to the end offset of the sentence
			if(startOffsetOfAnnotation < endOffsetOfAnnotation && startOffsetOfAnnotation < totalDocEndOffset && endOffsetOfAnnotation > totalDocEndOffset) {
				totalDocEndOffset = endOffsetOfAnnotation;
			}
			

			if(totalDocEndOffset != null && totalDocEndOffset > 0l && totalDocStartOffset != null && totalDocStartOffset >= 0l &&
					totalDocStartOffset < totalDocEndOffset &&
					startOffsetOfAnnotation != null && startOffsetOfAnnotation > 0l &&
					endOffsetOfAnnotation != null && endOffsetOfAnnotation > 0l &&
					startOffsetOfAnnotation < endOffsetOfAnnotation &&
					endOffsetOfAnnotation <= totalDocEndOffset) {
				Double middleOffset = startOffsetOfAnnotation.doubleValue() + ( (endOffsetOfAnnotation.doubleValue() - startOffsetOfAnnotation.doubleValue()) / 2d );
				middleOffset = middleOffset - totalDocStartOffset.doubleValue();
				doubleRetValue = round( (middleOffset / (totalDocEndOffset.doubleValue() - totalDocStartOffset.doubleValue())), 4) * 100d;

				if(folderNumber != null && folderNumber > 0) {
					doubleRetValue = ( middleOffset / (totalDocEndOffset.doubleValue() - totalDocStartOffset.doubleValue()) ) * (folderNumber.doubleValue());
					doubleRetValue = (double) (doubleRetValue.intValue() + (int) 1);
					
					// Generating unequal sizes over 20
					if(this.unequal) {
						if(doubleRetValue == 6d) {
							doubleRetValue = 5d;
						}
						else if(doubleRetValue >= 7d && doubleRetValue <= 15d) {
							doubleRetValue = 6d;
						}
						else if(doubleRetValue >= 16d && doubleRetValue <= 17d) {
							doubleRetValue = 7d;
						}
						else if(doubleRetValue == 18d) {
							doubleRetValue = 8d;
						}
						else if(doubleRetValue == 19d) {
							doubleRetValue = 9d;
						}
						else if(doubleRetValue == 20d) {
							doubleRetValue = 10d;
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
