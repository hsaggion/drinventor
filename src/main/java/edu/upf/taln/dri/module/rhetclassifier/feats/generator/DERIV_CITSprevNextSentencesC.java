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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;


/**
 * Retrieve if the main verb of the sentence is active or passive
 * 
 *
 */
public class DERIV_CITSprevNextSentencesC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	public enum REF_SENT_AND_TYPE {
		PREVIOUS_SENT_CIT_SPAN, PREVIOUS_SENT_CIT_MARKER, NEXT_SENT_CIT_SPAN, NEXT_SENT_CIT_MARKER;
	} 

	private REF_SENT_AND_TYPE type = null;

	/**
	 * Token annotation set and name
	 * 
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 */
	public DERIV_CITSprevNextSentencesC(REF_SENT_AND_TYPE type) {
		this.type = type;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		MyDouble retValue = new MyDouble(0d);
		
		
		// Get the H1 section of the sentence - START
		Annotation H1header = null;
		Map<Long, Annotation> unorderedAnnotation = new HashMap<Long, Annotation>();

		// Add h1 to map
		List<Annotation> H1_annList = GateUtil.getAnnInDocOrder(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h1AnnType);
		Iterator<Annotation> H1_annList_Iter = H1_annList.iterator();
		while(H1_annList_Iter.hasNext()) {
			Annotation ann = H1_annList_Iter.next();

			if(ann != null) {
				unorderedAnnotation.put(ann.getStartNode().getOffset(), ann);
			}
		}

		Map<Long, Annotation> orderedAnnotation = new TreeMap<Long, Annotation>(
				new Comparator<Long>() {

					@Override
					public int compare(Long o1, Long o2) {
						return o1.compareTo(o2);
					}

				});
		orderedAnnotation.putAll(unorderedAnnotation);

		Long sectionStartOffset = 0l;
		Long sectionEndOffset = gate.Utils.lengthLong(doc.getGateDoc());

		if(orderedAnnotation != null && orderedAnnotation.size() > 0) {

			Long distanceBefore = Long.MAX_VALUE;
			Long distanceAfter = Long.MAX_VALUE;
			for(Entry<Long, Annotation> entry : orderedAnnotation.entrySet()) {
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

			if(sectionStartOffset != null && orderedAnnotation.containsKey(sectionStartOffset) && orderedAnnotation.get(sectionStartOffset) != null) {
				H1header = orderedAnnotation.get(sectionStartOffset);
			}

		}

		// Here I have the sectionStartOffset and sectionEndOffset variables correctly set 
		// to the start and end offset of the section of the considered sentence

		// Get the H1 section of the sentence - END

		// Get previous and next sentence
		Annotation sentBeforeInSection = null;
		Annotation sentenceAfterInSection = null;
		List<Annotation> sentenceList = GateUtil.getAnnInDocOrder(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		if(sentenceList != null && sentenceList.size() > 0) {
			for(int i = 0; i < sentenceList.size(); i++) {
				Annotation sentenceListElem = sentenceList.get(i);
				if(sentenceListElem != null && sentenceListElem.getId().equals(obj.getId())) {

					// Get sent before in section
					if(i > 0 && i < sentenceList.size()) {
						Annotation sentenceBefore = sentenceList.get(i - 1);
						if(sentenceBefore != null && sentenceBefore.getStartNode().getOffset() >= sectionStartOffset) {
							sentBeforeInSection = sentenceBefore;
						}
					}


					// Get sent after in section
					if(i >= 0 && i < (sentenceList.size() - 1)) {
						Annotation sentenceAfter = sentenceList.get(i + 1);
						if(sentenceAfter != null && sentenceAfter.getEndNode().getOffset() <= sectionEndOffset) {
							sentenceAfterInSection = sentenceAfter;
						}
					}
				}
			}
		}


		Double countResult = null;

		switch(this.type) {
		case PREVIOUS_SENT_CIT_SPAN:
			if(sentBeforeInSection != null) {
				List<Annotation> citSpanPrevious = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType, sentBeforeInSection);
				if(citSpanPrevious != null && citSpanPrevious.size() > 0) {
					countResult = (double) citSpanPrevious.size();
				}
			}
			break;
		case PREVIOUS_SENT_CIT_MARKER:
			if(sentBeforeInSection != null) {
				List<Annotation> citMarkerPrevious = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType, sentBeforeInSection);
				if(citMarkerPrevious != null && citMarkerPrevious.size() > 0) {
					countResult = (double) citMarkerPrevious.size();
				}
			}
			break;
		case NEXT_SENT_CIT_SPAN:
			if(sentenceAfterInSection != null) {
				List<Annotation> citSpanAfter = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType, sentenceAfterInSection);
				if(citSpanAfter != null && citSpanAfter.size() > 0) {
					countResult = (double) citSpanAfter.size();
				}
			}
			break;
		case NEXT_SENT_CIT_MARKER:
			if(sentenceAfterInSection != null) {
				List<Annotation> citMarkerAfter = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(doc.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType, sentenceAfterInSection);
				if(citMarkerAfter != null && citMarkerAfter.size() > 0) {
					countResult = (double) citMarkerAfter.size();
				}
			}
			break;
		default:

		}

		if(countResult != null && countResult > 0d) {
			retValue.setValue(countResult);
		}
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}

		return retValue;
	}

}
