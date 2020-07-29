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
package edu.upf.taln.dri.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

/**
 * Collection of utility methods to interact with GATE documents.
 * 
 *
 */
public class GateUtil {
	
	private static Logger logger = Logger.getLogger(GateUtil.class);	
	
	/**
	 * Return the value of the annotation feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<String> getStringFeature(Annotation ann, String featName) {
		String annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && ann != null && ann.getFeatures() != null && 
					ann.getFeatures().containsKey(featName) && ann.getFeatures().get(featName) != null) {
				annotationFeatValue = (String) ann.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the annotation feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<Boolean> getBooleanFeature(Annotation ann, String featName) {
		Boolean annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && ann != null && ann.getFeatures() != null && 
					ann.getFeatures().containsKey(featName) && ann.getFeatures().get(featName) != null) {
				annotationFeatValue = (Boolean) ann.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the annotation feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<Integer> getIntegerFeature(Annotation ann, String featName) {
		Integer annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && ann != null && ann.getFeatures() != null && 
					ann.getFeatures().containsKey(featName) && ann.getFeatures().get(featName) != null) {
				annotationFeatValue = (Integer) ann.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the annotation feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<Double> getDoubleFeature(Annotation ann, String featName) {
		Double annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && ann != null && ann.getFeatures() != null && 
					ann.getFeatures().containsKey(featName) && ann.getFeatures().get(featName) != null) {
				annotationFeatValue = (Double) ann.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the annotation feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<List<Integer>> getListIntegerFeature(Annotation ann, String featName) {
		List<Integer> annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && ann != null && ann.getFeatures() != null && 
					ann.getFeatures().containsKey(featName) && ann.getFeatures().get(featName) != null) {
				annotationFeatValue = (List<Integer>) ann.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the document feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<String> getStringFeature(Document doc, String featName) {
		String annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && doc != null && doc.getFeatures() != null && 
					doc.getFeatures().containsKey(featName) && doc.getFeatures().get(featName) != null) {
				annotationFeatValue = (String) doc.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Return the value of the document feature
	 * 
	 * @param ann
	 * @param featName
	 * @return
	 */
	public static Optional<Boolean> getBooleanFeature(Document doc, String featName) {
		Boolean annotationFeatValue = null;
		
		try {
			if(StringUtils.isNotBlank(featName) && doc != null && doc.getFeatures() != null && 
					doc.getFeatures().containsKey(featName) && doc.getFeatures().get(featName) != null) {
				annotationFeatValue = (Boolean) doc.getFeatures().get(featName);
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationFeatValue);
	}
	
	/**
	 * Check if an annotation is contained in another one.
	 * The contained annotation needs to have:
	 * - a start offset >= than the start offset of the container
	 * - a end offset <= than the end offset of the container
	 * 
	 * @param container
	 * @param contained
	 * @return
	 */
	public static boolean containedIn(Annotation container, Annotation contained) {
		boolean result = false;
		
		if(container != null && contained != null &&
				container.getStartNode().getOffset() <= contained.getStartNode().getOffset() &&
				container.getEndNode().getOffset() >= contained.getEndNode().getOffset()) {
			result = true;
		}
		
		return result;
	} 
	
	
	/**
	 * Return the text spotted by the annotation
	 * 
	 * @param ann
	 * @param doc
	 * @return
	 */
	public static Optional<String> getAnnotationText(Annotation ann, Document doc) {
		String annotationText = null;
		
		try {
			if(doc != null && ann != null) {
				annotationText = doc.getContent().getContent(ann.getStartNode().getOffset(), ann.getEndNode().getOffset()).toString();
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationText);
	}
	
	/**
	 * Return the text spotted by the annotation
	 * 
	 * @param ann
	 * @param doc
	 * @return
	 */
	public static Optional<String> getDocumentText(Document doc, Long startOffset, Long endOffset) {
		String annotationText = null;
		
		try {
			if(doc != null && startOffset != null && endOffset != null && startOffset <= endOffset && endOffset < gate.Utils.lengthLong(doc)) {
				annotationText = (startOffset < endOffset) ? doc.getContent().getContent(startOffset, endOffset).toString() : "";
			}
		}
		catch (Exception e) {
			/* Do nothing */
		}
		
		return Optional.ofNullable(annotationText);
	}
	
	/**
	 * Transfer from the source Annotation Set all the annotations of type equal to annType to the
	 * destination Annotation Set, renaming each annotation with the new annotation type.
	 * 
	 * @param annType
	 * @param newAnnType
	 * @param sourceAS
	 * @param destinationAS
	 * @param filterPred If not null, useful to filter the annotations to transfer
	 */
	public static void transferAnnotations(Document doc, String annType, String newAnnType, String sourceAS, String destinationAS, Predicate<Annotation> filterPred) {
		if(doc == null || StringUtils.isBlank(annType) || StringUtils.isBlank(newAnnType) && sourceAS == null && destinationAS == null) {
			return;
		}
		
		List<Annotation> annotationsToTransfer = gate.Utils.inDocumentOrder(doc.getAnnotations(sourceAS).get(annType));
		
		if(filterPred != null) {
			annotationsToTransfer = annotationsToTransfer.stream().filter(filterPred).collect(Collectors.toList());
		}
		
		annotationsToTransfer.stream().forEach((ann) -> {
			try {
				doc.getAnnotations(destinationAS).add(ann.getStartNode().getOffset(), ann.getEndNode().getOffset(), newAnnType, ann.getFeatures());
			}
			catch(Exception e) {
				/* Do nothing */
			}
		});
	}
	
	/**
	 * Get the ordered annotations from an annotation set and type 
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @return
	 */
	public static List<Annotation> getAnnInDocOrder(Document doc, String annSet, String annType) {
		List<Annotation> result = new ArrayList<Annotation>();
		
		if(annSet != null && StringUtils.isNotBlank(annType)) {
			result = gate.Utils.inDocumentOrder(doc.getAnnotations(annSet).get(annType));
		}
		
		return result;
	}
	
	/**
	 * Get the ordered annotations from an annotation set and type, included in a couple of offsets
	 * 
	 * NB: all annotations with start position >= startOffset and whose end position <= endOffset
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	public static List<Annotation> getAnnInDocOrderContainedOffset(Document doc, String annSet, String annType, Long startOffset, Long endOffset) {
		List<Annotation> result = new ArrayList<Annotation>();
		
		if(annSet != null && StringUtils.isNotBlank(annType) && startOffset != null && endOffset != null) {
			result = gate.Utils.inDocumentOrder(doc.getAnnotations(annSet).get(annType).getContained(startOffset, endOffset));
		}
		
		return result;
	}
	
	/**
	 * Get the ordered annotations from an annotation set and type, included in an annotation
	 * 
	 * NB: all annotations with start position >= startOffset and whose end position <= endOffset
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	public static List<Annotation> getAnnInDocOrderContainedAnn(Document doc, String annSet, String annType, Annotation includedInAnn) {
		List<Annotation> result = new ArrayList<Annotation>();
		
		if(annSet != null && StringUtils.isNotBlank(annType) && includedInAnn != null) {
			result = gate.Utils.inDocumentOrder(doc.getAnnotations(annSet).get(annType).getContained(includedInAnn.getStartNode().getOffset(), includedInAnn.getEndNode().getOffset()));
		}
		
		return result;
	}
	
	/**
	 * Get the ordered annotations from an annotation set and type, intersecting a couple of offsets
	 * 
	 * NB: all annotations with start position >= startOffset and whose end position <= endOffset
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @param startOffset
	 * @param intersectingAnn
	 * @return
	 */
	public static List<Annotation> getAnnInDocOrderIntersectOffset(Document doc, String annSet, String annType, Long startOffset, Long intersectingAnn) {
		List<Annotation> result = new ArrayList<Annotation>();
		
		if(annSet != null && StringUtils.isNotBlank(annType) && startOffset != null && intersectingAnn != null) {
			result = gate.Utils.inDocumentOrder(doc.getAnnotations(annSet).get(annType, startOffset, intersectingAnn));
		}
		
		return result;
	}
	
	/**
	 * Get the ordered annotations from an annotation set and type, intersecting an annotation
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	public static List<Annotation> getAnnInDocOrderIntersectAnn(Document doc, String annSet, String annType, Annotation intersectingAnn) {
		List<Annotation> result = new ArrayList<Annotation>();
		
		if(annSet != null && StringUtils.isNotBlank(annType) && intersectingAnn != null) {
			result = gate.Utils.inDocumentOrder(doc.getAnnotations(annSet).get(annType, intersectingAnn.getStartNode().getOffset(), intersectingAnn.getEndNode().getOffset()));
		}
		
		return result;
	}
	
	
	/**
	 * Creates a GATE document from a list of Strings.
	 * Each element of the list is annotated by the ImportBase.headerDOC_Sentence in the
	 * ImportBase.headerDOC_AnnSet annotation set
	 * 
	 * @param sentList
	 * @return
	 */
	public static gate.Document formListStrToGateDoc(List<String> sentList) {
		gate.Document gateDoc = null;
		
		if(!CollectionUtils.isEmpty(sentList)) {

			// Generate a GATE document
			String headerText = "";
			Map<Integer, String> lineText = new HashMap<Integer, String>();
			Map<Integer, Integer> lineStartOffset = new HashMap<Integer, Integer>();
			Map<Integer, Integer> lineEndOffset = new HashMap<Integer, Integer>();

			Integer lineId = 0;
			
			for(String sent : sentList) {
				if(StringUtils.isNotBlank(sent)) {
					if(!headerText.equals("")) {
						headerText += "\n";
					}
					
					// Separate with a space sequences of lowerCase/UpperCase chars
					for(int y = 0; y < 10; y++) {
						for(int z = 0; z < sent.length() - 1; z++) {
							char first = sent.charAt(z);
							char second = sent.charAt(z + 1);
							
							if(Character.isLowerCase(first) && Character.isAlphabetic(first) &&
							   Character.isUpperCase(second) && Character.isAlphabetic(second)) {
								sent = sent.substring(0, z + 1) + " " + sent.substring(z + 1);
								break;
							}
						}
					}
					
					// Adding line
					Integer initialOffset = headerText.length();
					headerText = headerText + sent;
					Integer finalOffset = headerText.length();

					lineText.put(lineId, sent);
					lineStartOffset.put(lineId, initialOffset);
					lineEndOffset.put(lineId, finalOffset);
					lineId++;
				}
			}
			
			// Populate GATE Document from sentence list
			try {
				gateDoc = Factory.newDocument(headerText);
				
				AnnotationSet mainAS = gateDoc.getAnnotations(ImporterBase.headerDOC_AnnSet);
				for(int k = 0; k < lineId; k++) {					
					try {
						mainAS.add(new Long(lineStartOffset.get(k)), new Long(lineEndOffset.get(k)), ImporterBase.headerDOC_Sentence, Factory.newFeatureMap());
					} catch (InvalidOffsetException e) {
						Util.notifyException("Adding sentence annotations to header", e, logger);
					}
				}
			} catch (ResourceInstantiationException e) {
				Util.notifyException("Creating header GATE document", e, logger);
			}
		}
		
		return gateDoc;
	}
	
	/**
	 * Get the first annotation in doc order of a specific set and type
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @return
	 */
	public static Optional<Annotation> getFirstAnnotationInDocOrder(Document doc, String annSet, String annType) {
		Annotation result = null;
		
		List<Annotation> docOrderedAnnList = GateUtil.getAnnInDocOrder(doc, annSet, annType);
		if(!CollectionUtils.isEmpty(docOrderedAnnList) && docOrderedAnnList.size() > 0) {
			result = docOrderedAnnList.get(0);
		}
		
		return Optional.ofNullable(result);
	}
	
	/**
	 * Get the last annotation in doc order of a specific set and type
	 * 
	 * @param doc
	 * @param annSet
	 * @param annType
	 * @return
	 */
	public static Optional<Annotation> getLastAnnotationInDocOrder(Document doc, String annSet, String annType) {
		Annotation result = null;
		
		List<Annotation> docOrderedAnnList = GateUtil.getAnnInDocOrder(doc, annSet, annType);
		if(!CollectionUtils.isEmpty(docOrderedAnnList) && docOrderedAnnList.size() > 0) {
			result = docOrderedAnnList.get(docOrderedAnnList.size() - 1);
		}
		
		return Optional.ofNullable(result);
	}
	
	/**
	 * Given a list of annotations return the list of annotation IDs
	 * @param annList
	 * @return
	 */
	public static List<Integer> fromAnnListToAnnIDlist(List<Annotation> annList) {
		List<Integer> retList = new ArrayList<Integer>();
		
		if(annList != null && annList.size() > 0) {
			for(Annotation ann : annList) {
				if(ann != null && ann.getId() != null) {
					retList.add(ann.getId());
				}
			}
		}
		
		return retList;
	}
	
	
}
