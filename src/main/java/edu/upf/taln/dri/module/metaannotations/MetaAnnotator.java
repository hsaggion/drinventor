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
package edu.upf.taln.dri.module.metaannotations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;

/**
 * Refine and unify Meta-annotations (projects, funding agencies, ontologies, etc.) added by the
 * related GATE Application.
 * 
 */
@CreoleResource(name = "DRI Modules - Meta annotator")
public class MetaAnnotator  extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(MetaAnnotator.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	public void execute() throws ExecutionException {
		this.annotationReset = false;

		// STEP 1: extract acknowledgment sentences
		List<Annotation> ackSentences_DRIannset = annotateAckSentences(ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		List<Annotation> ackSentences = annotateAckSentences(ImporterBase.metaAnnotator_AnnSet, ImporterBase.sentenceAnnType);

		// STEP 2: extract from Lookup / Gazetteer annotations intersecting acknowledgement sentences
		annotateFromLookup(ackSentences, "project");
		annotateFromLookup(ackSentences, "fundingAgency");

		// STEP 3: JAPE spotted annotations intersecting acknowledgement sentences
		/* spotting metaAnnotator_ProjectAnnType */
		annotateFromJAPE(ackSentences, "EU_PROJECT", ImporterBase.metaAnnotator_ProjectAnnType);

		/* spotting metaAnnotator_FundingAgencyAnnType */
		annotateFromJAPE(ackSentences, "RESEARCH_INST", ImporterBase.metaAnnotator_FundingAgencyAnnType);
		annotateFromJAPE(ackSentences, "FUNDING_FUND_PROJECT", ImporterBase.metaAnnotator_FundingAgencyAnnType);
		annotateFromJAPE(ackSentences, "FUNDING_EU_PROJECT", ImporterBase.metaAnnotator_FundingAgencyAnnType);
		annotateFromJAPE(ackSentences, "FUNDING_GRANT", ImporterBase.metaAnnotator_FundingAgencyAnnType);

		/* spotting metaAnnotator_OntologyAnnType */
		annotateFromJAPE(ackSentences, "ONTOLOGY", ImporterBase.metaAnnotator_OntologyAnnType);
		annotateFromJAPE(ackSentences, "related_ONTOLOGY", ImporterBase.metaAnnotator_OntologyAnnType);


		// STEP 4: remove overlapping annotations of type metaAnnotator_ProjectAnnType / metaAnnotator_FundingAgencyAnnType /metaAnnotator_OntologyAnnType
		removeOverlapping(ImporterBase.metaAnnotator_FundingAgencyAnnType);
		removeOverlapping(ImporterBase.metaAnnotator_ProjectAnnType);
		removeOverlapping(ImporterBase.metaAnnotator_OntologyAnnType);
		
		// STEP 5: remove superfluous annotations
		this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).removeAll(this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.sentenceAnnType));
		this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).removeAll(this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.tokenAnnType));
	}
	
	/**
	 * Group overlapping annotations of a specific type and keep only the longest one from the overlapping group
	 * 
	 * @param annType
	 */
	private void removeOverlapping(String annType) {

		try {
			List<Annotation> annList = gate.Utils.inDocumentOrder(this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(annType));
			
			boolean foundOverlap = true;
			while(foundOverlap) {
				
				foundOverlap = false;
				
				for(int i = 0; i < annList.size(); i++) {
					Annotation annConsidered = annList.get(i);
					List<Annotation> overlapList = new ArrayList<Annotation>();
					overlapList.add(annConsidered);
					
					// Find all overlapping annotations
					for(int k = 0; k < annList.size(); k++) {
						if(k == i) {
							continue;
						}
						
						Annotation annToCheckOverlap = annList.get(k);
						if(annConsidered.overlaps(annToCheckOverlap)) {
							overlapList.add(annToCheckOverlap);
						}
					}
					
					
					// Delete all overlapping
					if(overlapList.size() > 1) {
						foundOverlap = true;
						Annotation annToKeep = null;
						long lengthOfAnnToKeep = 0l;
						for(int p = 0; p < overlapList.size(); p++) {
							long lengthOfAnn = overlapList.get(p).getEndNode().getOffset() - overlapList.get(p).getStartNode().getOffset();
							if(annToKeep == null || lengthOfAnn > lengthOfAnnToKeep) {
								annToKeep = overlapList.get(p);
								lengthOfAnnToKeep = lengthOfAnn;
							}
						}
						
						// Delete all the overlapping annotations that are not equal to annToKeep
						for(int j = 0; j < overlapList.size(); j++) {
							if(overlapList.get(j).getId().equals(annToKeep.getId())) {
								continue;
							}
							
							this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).remove(overlapList.get(j));
						}
					}
				}
			}

		}
		catch (Exception e) {
			/* Do Nothing */
		}
	}

	/**
	 * Consider all the annotations of type annType in the ann set ImporterBase.metaAnnotator_AnnSet and if
	 * included in the list of sentencesToAnnotate, copy the annotation with type destinationAnnType 
	 * 
	 * @param sentencesToAnnotate
	 * @param annType
	 * @param destinationAnnType
	 * @return
	 */
	private List<Annotation> annotateFromJAPE(List<Annotation> sentencesToAnnotate, String annType, String destinationAnnType) {

		List<Annotation> returnAnnotations = new ArrayList<Annotation>();

		for(Annotation sentToAnn : sentencesToAnnotate) {
			try {
				if(sentToAnn != null && !GateUtil.getAnnotationText(sentToAnn, this.document).orElse("").equals("")) {

					AnnotationSet researchInstOfSentence = this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(annType);
					if(researchInstOfSentence != null && researchInstOfSentence.size() > 0) {
						List<Annotation> researchInstOfSentenceOrdered = gate.Utils.inDocumentOrder(researchInstOfSentence.get(sentToAnn.getStartNode().getOffset(), sentToAnn.getEndNode().getOffset()));
						if(researchInstOfSentenceOrdered != null && researchInstOfSentenceOrdered.size() > 0) {
							for(int w = 0; w < researchInstOfSentenceOrdered.size(); w++) {
								try {
									Integer newAnnId = this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).add(researchInstOfSentenceOrdered.get(w).getStartNode().getOffset(),
											researchInstOfSentenceOrdered.get(w).getEndNode().getOffset(), destinationAnnType, researchInstOfSentenceOrdered.get(w).getFeatures());
									returnAnnotations.add(this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(newAnnId));
								}
								catch (Exception e) {
									/* Do nothing */
								}
							}
						}
					}

				}
			}
			catch (Exception e) {
				/* Do Nothing */
			}
		}

		return returnAnnotations;
	}

	/**
	 * Get project 
	 * @param sentencesToAnnotate
	 * @return
	 */
	private List<Annotation> annotateFromLookup(List<Annotation> sentencesToAnnotate, String annType) {

		List<Annotation> returnAnnotations = new ArrayList<Annotation>();

		// Look for Lookups in Original markups
		for(Annotation ackSent : sentencesToAnnotate) {
			try {
				if(ackSent != null && !GateUtil.getAnnotationText(ackSent, this.document).equals("")) {

					// Project with something around
					AnnotationSet lookupOfSentence = this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.metaAnnotator_LookupAnnType);

					if(lookupOfSentence != null && lookupOfSentence.size() > 0) {
						List<Annotation> lookupOfSentenceOrdered = gate.Utils.inDocumentOrder(lookupOfSentence.get(ackSent.getStartNode().getOffset(),ackSent.getEndNode().getOffset()));
						if(lookupOfSentenceOrdered != null && lookupOfSentenceOrdered.size() > 0) {
							for(int w = 0; w < lookupOfSentenceOrdered.size(); w++) {
								try {
									String word = GateUtil.getStringFeature(lookupOfSentenceOrdered.get(w), "string").orElse(null);
									String majorT = GateUtil.getStringFeature(lookupOfSentenceOrdered.get(w), "majorType").orElse(null);
									String minorT = GateUtil.getStringFeature(lookupOfSentenceOrdered.get(w), "minorType").orElse(null);

									if( word != null && word.length() > 0 && majorT != null && majorT.length() > 0 ) {

										if(annType != null && annType.equals("project") && majorT.trim().equals("EUDATA")) {
											if(minorT.trim().equals("prjAcronym") || minorT.trim().equals("prjLongName")) {
												if(word.trim().length() > 5) {
													this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).add(lookupOfSentenceOrdered.get(w).getStartNode().getOffset(),
															lookupOfSentenceOrdered.get(w).getEndNode().getOffset(), ImporterBase.metaAnnotator_ProjectAnnType, lookupOfSentenceOrdered.get(w).getFeatures());
													returnAnnotations.add(lookupOfSentenceOrdered.get(w));
												}
											}
										}

										if(annType != null && annType.equals("fundingAgency") && (majorT.trim().equals("FUNDREF") || majorT.trim().equals("EUDATA"))) {
											if(minorT.trim().equals("institutionName")) {
												if(word.trim().length() > 5) {
													this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).add(lookupOfSentenceOrdered.get(w).getStartNode().getOffset(),
															lookupOfSentenceOrdered.get(w).getEndNode().getOffset(), ImporterBase.metaAnnotator_FundingAgencyAnnType, lookupOfSentenceOrdered.get(w).getFeatures());
													returnAnnotations.add(lookupOfSentenceOrdered.get(w));
												}
											}

											if(minorT.trim().equals("orgLongName") || minorT.trim().equals("orgShortName")) {
												if(word.trim().length() > 5) {
													this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).add(lookupOfSentenceOrdered.get(w).getStartNode().getOffset(),
															lookupOfSentenceOrdered.get(w).getEndNode().getOffset(), ImporterBase.metaAnnotator_FundingAgencyAnnType, lookupOfSentenceOrdered.get(w).getFeatures());
													returnAnnotations.add(lookupOfSentenceOrdered.get(w));
												}
											}
										}
									}
								}
								catch (Exception e) {
									/* Do nothing */
								}
							}
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return returnAnnotations;
	}

	private List<Annotation> annotateAckSentences(String sentAnnSet, String sentAnnType) {

		List<Annotation> acknowledgmentSentenceAnnotations = new ArrayList<Annotation>();

		Map<Long, Annotation> annotationsMap = new TreeMap<Long, Annotation>(Collections.reverseOrder());
		if(this.document.getAnnotations(ImporterBase.driAnnSet) != null) {
			// Acknowledgement sections can be mainlys H1
			AnnotationSet driAnnSet = this.document.getAnnotations(ImporterBase.driAnnSet);
			List<Annotation> h1Annotations = gate.Utils.inDocumentOrder(driAnnSet.get(ImporterBase.h1AnnType)); 
			/*
			List<Annotation> h2Annotations = gate.Utils.inDocumentOrder(driAnnSet.get(ImporterBase.h2AnnType)); 
			List<Annotation> h3Annotations = gate.Utils.inDocumentOrder(driAnnSet.get(ImporterBase.h3AnnType)); 
			List<Annotation> h4Annotations = gate.Utils.inDocumentOrder(driAnnSet.get(ImporterBase.h4AnnType)); 
			List<Annotation> h5Annotations = gate.Utils.inDocumentOrder(driAnnSet.get(ImporterBase.h5AnnType)); 
			 */

			if(h1Annotations != null && h1Annotations.size() > 0) {
				for(Annotation ann : h1Annotations) {
					if(ann != null) {
						annotationsMap.put(ann.getStartNode().getOffset(), ann);
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

		List<Annotation> sentenceAnnotations = gate.Utils.inDocumentOrder(this.document.getAnnotations(sentAnnSet).get(sentAnnType));
		if(sentenceAnnotations != null && sentenceAnnotations.size() > 0) {
			for(Annotation sentenceAnnotation : sentenceAnnotations) {
				if(sentenceAnnotation != null) {
					try {
						String selectedSectionText = "";
						Long maxDistance = Long.MAX_VALUE;
						for(Entry<Long, Annotation> entrySection : annotationsMap.entrySet()) {
							if(entrySection.getKey() != null && entrySection.getValue() != null) {
								try {
									if(entrySection.getValue().getStartNode().getOffset() <= sentenceAnnotation.getStartNode().getOffset() && 
											(sentenceAnnotation.getStartNode().getOffset() - entrySection.getValue().getStartNode().getOffset()) < maxDistance) {

										String text = this.document.getContent().getContent(
												entrySection.getValue().getStartNode().getOffset(), 
												entrySection.getValue().getEndNode().getOffset()).toString();
										selectedSectionText = text;
										maxDistance = sentenceAnnotation.getStartNode().getOffset() - entrySection.getValue().getStartNode().getOffset();
									}
								}
								catch (Exception e) {
									// Do nothing
								}
							}
						}

						String sentenceText = GateUtil.getAnnotationText(sentenceAnnotation, this.document).orElse(null);

						if(sentenceText != null && !sentenceText.equals("") &&
								selectedSectionText != null && !selectedSectionText.equals("")) {

							if(selectedSectionText.toLowerCase().contains("acknowledg")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("acknowledgment")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("we acknowledge")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work was supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work is supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work was partially supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work is partially supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work was in part supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work is in part supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("we thank")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("we are thankful")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("financial supports")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("would like to thank")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("grant from")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("funded by")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("research is supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("work is supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(sentenceText.toLowerCase().contains("project is supported")) {
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							} 
							else if(selectedSectionText.toLowerCase().contains("agradecimiento")) { // Spanish adaptation
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(selectedSectionText.toLowerCase().contains("colaboración")) { // Spanish adaptation
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							else if(selectedSectionText.toLowerCase().contains("colaboracion")) { // Spanish adaptation
								acknowledgmentSentenceAnnotations.add(sentenceAnnotation);
							}
							
							
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for(Annotation acknowledgmentSent : acknowledgmentSentenceAnnotations) {
			if(acknowledgmentSent != null) {
				acknowledgmentSent.setFeatures((acknowledgmentSent.getFeatures() != null) ? acknowledgmentSent.getFeatures() : Factory.newFeatureMap());
				acknowledgmentSent.getFeatures().put(ImporterBase.sentence_isAcknowledgement, "true");
			}
		}

		return acknowledgmentSentenceAnnotations;
	}

	@Override
	public boolean resetAnnotations() {
		if(!this.annotationReset) {
			// Remove sentence annotation of acknowledgement sentences
			List<Annotation> sentenceAnn = gate.Utils.inDocumentOrder(this.document.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType));
			for(Annotation sentAnn : sentenceAnn) {
				if(sentAnn != null && sentAnn.getFeatures() != null) {
					sentAnn.getFeatures().remove(ImporterBase.sentence_isAcknowledgement);
				}
			}
			
			// Remove JAPE_RUELS ann set
			this.document.removeAnnotationSet(ImporterBase.metaAnnotator_AnnSet);
			
			// Remove annotations of types: (commented because we removed the whole ImporterBase.metaAnnotator_AnnSet ann set)
			// - ImporterBase.metaAnnotator_FundingAgencyAnnType
			// - ImporterBase.metaAnnotator_ProjectAnnType
			// - ImporterBase.metaAnnotator_OntologyAnnType
			// this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).removeAll(
			// 		this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.metaAnnotator_FundingAgencyAnnType));
			// this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).removeAll(
			// 		this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.metaAnnotator_ProjectAnnType));
			// this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).removeAll(
			// 		this.document.getAnnotations(ImporterBase.metaAnnotator_AnnSet).get(ImporterBase.metaAnnotator_OntologyAnnType));
			
			this.annotationReset = true;
		}

		return false;
	}

}
