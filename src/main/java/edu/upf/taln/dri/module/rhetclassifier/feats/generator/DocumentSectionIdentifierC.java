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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;

/**
 * Get the id and title of the document section that contains the sentence.
 * 
 *
 */
public class DocumentSectionIdentifierC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	public enum SectionTypeENUM {
        ID, TITLE;
    } 
	
	private SectionTypeENUM sectionType = SectionTypeENUM.ID;
	
	public DocumentSectionIdentifierC(SectionTypeENUM sectionType) {
		super();
		this.sectionType = (sectionType != null) ? sectionType : SectionTypeENUM.ID;
	}

	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		
		// Get section annotations
		Map<Long, Annotation> sectAnnotationsMap = new TreeMap<Long, Annotation>(Collections.reverseOrder());
		if(doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet) != null) {
			List<Annotation> h1Annotations = gate.Utils.inDocumentOrder(doc.getGateDoc().
					getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h1AnnType)); 
			/*
			List<Annotation> h2Annotations = gate.Utils.inDocumentOrder(doc.getGateDoc().
					getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h2AnnType)); 
			List<Annotation> h3Annotations = gate.Utils.inDocumentOrder(doc.getGateDoc().
					getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h3AnnType)); 
			List<Annotation> h4Annotations = gate.Utils.inDocumentOrder(doc.getGateDoc().
					getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h4AnnType)); 
			List<Annotation> h5Annotations = gate.Utils.inDocumentOrder(doc.getGateDoc().
					getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h5AnnType));
			*/
			
			
			if(h1Annotations != null && h1Annotations.size() > 0) {
				for(Annotation ann : h1Annotations) {
					if(ann != null) {
						sectAnnotationsMap.put(ann.getStartNode().getOffset(), ann);
					}
				}
			}

			/*
			if(h2Annotations != null && h2Annotations.size() > 0) {
				for(Annotation ann : h2Annotations) {
					if(ann != null) {
						annotationsMap.put(ann.getStartNode().getOffset(), ann);
					}
				}
			}


			if(h3Annotations != null && h3Annotations.size() > 0) {
				for(Annotation ann : h3Annotations) {
					if(ann != null) {
						annotationsMap.put(ann.getStartNode().getOffset(), ann);
					}
				}
			}

			if(h4Annotations != null && h4Annotations.size() > 0) {
				for(Annotation ann : h4Annotations) {
					if(ann != null) {
						annotationsMap.put(ann.getStartNode().getOffset(), ann);
					}
				}
			}

			if(h5Annotations != null && h5Annotations.size() > 0) {
				for(Annotation ann : h5Annotations) {
					if(ann != null) {
						annotationsMap.put(ann.getStartNode().getOffset(), ann);
					}
				}
			}
			*/
		}

		Map<Long, Annotation> sectAnnotationsMapOrdered = new TreeMap<Long, Annotation>(sectAnnotationsMap);
		
		// Sentence order
		Integer sectionOrderNumber = -1;
		String sectionName = "-";

		AnnotationSet documentSentences = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);
		if(documentSentences != null && documentSentences.size() > 0 ) {
			List<Annotation> documentSentencesList = gate.Utils.inDocumentOrder(documentSentences);

			for(Annotation docSentence : documentSentencesList) {

				if(docSentence.getStartNode().getOffset().compareTo(obj.getStartNode().getOffset()) == 0 &&
						docSentence.getEndNode().getOffset().compareTo(obj.getEndNode().getOffset()) == 0) {
					// docSentence is the sentence under analysis

					// Start offset of current section - START
					Long sentenceStartOffset = docSentence.getStartNode().getOffset();
					Long currentDifference = null;
					Integer currentSection = -1;
					String currentSectionName = "-";

					if(sectAnnotationsMapOrdered == null || sectAnnotationsMapOrdered.size() == 0) {
						currentSection = 1;
					}
					else {
						Integer sectionCount = 1;
						
						for(Entry<Long, Annotation> sectionElem : sectAnnotationsMapOrdered.entrySet()) {
							sectionCount++;

							Long sectionStartOffset = sectionElem.getKey();
							Annotation sectName = sectionElem.getValue();

							String sectText = "-";
							try {
								sectText = doc.getGateDoc().getContent().getContent(sectName.getStartNode().getOffset(), sectName.getEndNode().getOffset()).toString();
							}
							catch (Exception e) {
								sectText = "-";
							}

							if( sectionStartOffset < sentenceStartOffset && currentDifference == null ) {
								currentDifference = sentenceStartOffset - sectionStartOffset;
								currentSection = new Integer(sectionCount.intValue());
								currentSectionName = sectText;
							}
							else if( sectionStartOffset < sentenceStartOffset && (sentenceStartOffset - sectionStartOffset) < currentDifference ) {
								currentDifference = sentenceStartOffset - sectionStartOffset;
								currentSection = new Integer(sectionCount.intValue());
								currentSectionName = sectText;
							}
						}
						
						if(sectionCount == 1) {
							currentSection = 1;
						}
						
					}
					// Start offset of current section - END
					
					sectionOrderNumber = currentSection;

					currentSectionName = currentSectionName.replace(" ", "_");
					currentSectionName = currentSectionName.replace(":", "_");
					currentSectionName = currentSectionName.replace(";", "_");
					currentSectionName = currentSectionName.replace(",", "_");
					sectionName = currentSectionName;
				}
			}
		}

		MyString retValue = new MyString("");
		
		switch(sectionType) {
		case ID:
			retValue.setValue("SECT_" + sectionOrderNumber + "__" + sectionName);
			break;
		case TITLE:
			retValue.setValue(sectionName);
			break;
		default:
			
		}
		
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

	public static void main(String args[]) {
		Map<Long, String> mp = new HashMap<Long, String>();
		mp.put(100l, "");
		mp.put(104l, "");
		mp.put(107l, "");
		mp.put(102l, "");

		Map<Long, String> treeMap = new TreeMap<Long, String>(mp);

		for(Entry<Long, String> entry : treeMap.entrySet()) {
			System.out.println(" - " + entry.getKey());
		}

	}

}
