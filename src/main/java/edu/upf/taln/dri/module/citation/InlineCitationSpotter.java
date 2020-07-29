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
package edu.upf.taln.dri.module.citation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.SourceENUM;
import edu.upf.taln.dri.module.importer.jats.ImporterJATS;
import edu.upf.taln.dri.module.importer.pdf.ImporterGROBID;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFX;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;


/**
 * Validate the annotations of CandidateInlineCitation and CandidateInlineCitationMarker by generating the annotations
 * InlineCitation and InlineCitationMarker
 * 
 *
 */
@CreoleResource(name = "DRI Modules - In-line citation spotter")
public class InlineCitationSpotter  extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {
	
	private static Logger logger = Logger.getLogger(InlineCitationSpotter.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;
	
	// Input and output annotation
	private String inputCandidateCitationASname = ImporterBase.driAnnSet;
	private String inputCandidateInlineCitationAStype = "CandidateInlineCitation";
	private String inputCandidateInlineCitationMarkerAStype = "CandidateInlineCitationMarker";
	

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set to read citation annotations from")
	public void setInputCandidateCitationASname(String inputCandidateCitationASname) {
		this.inputCandidateCitationASname = inputCandidateCitationASname;
	}

	public String getInputCandidateInlineCitationAStype() {
		return inputCandidateInlineCitationAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "CandidateInlineCitation", comment = "The name of the candidate inline citation annotation type, to be searched in the input annotation set")
	public void setInputCandidateInlineCitationAStype(
			String inputCandidateInlineCitationAStype) {
		this.inputCandidateInlineCitationAStype = inputCandidateInlineCitationAStype;
	}

	public String getInputCandidateInlineCitationMarkerAStype() {
		return inputCandidateInlineCitationMarkerAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "CandidateInlineCitationMarker", comment = "The name of the candidate inline citation marker annotation type, to be searched in the input annotation set")
	public void setInputCandidateInlineCitationMarkerAStype(
			String inputCandidateInlineCitationMarkerAStype) {
		this.inputCandidateInlineCitationMarkerAStype = inputCandidateInlineCitationMarkerAStype;
	}
	
	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		// Get the document to process
		gate.Document doc = getDocument();

		// Normalize variables
		this.inputCandidateCitationASname = (StringUtils.isNotBlank(this.inputCandidateCitationASname)) ?
				this.inputCandidateCitationASname : "Analysis";
		this.inputCandidateInlineCitationAStype = (StringUtils.isNotBlank(this.inputCandidateInlineCitationAStype)) ?
				this.inputCandidateInlineCitationAStype : "CandidateInlineCitation";
		this.inputCandidateInlineCitationMarkerAStype = (StringUtils.isNotBlank(this.inputCandidateInlineCitationMarkerAStype)) ? 
				this.inputCandidateInlineCitationMarkerAStype : "CandidateInlineCitationMarker";
		
		// If the source document is JATS (XML) there is no need to dentify inline citations
		Optional<String> sourceType = GateUtil.getStringFeature(doc, "source");
		if(Util.strCompare(sourceType.orElse(null), SourceENUM.JATS.toString())) {
			// Transfer in-line citations from JATS doc
			GateUtil.transferAnnotations(this.document, "xref", ImporterBase.inlineCitationMarkerAnnType, ImporterJATS.JATSannSet, ImporterBase.driAnnSet, (Annotation ann) -> {
				if(ann != null && ann.getFeatures() != null && ann.getFeatures().containsKey("ref-type") && ann.getFeatures().get("ref-type") != null && ((String) ann.getFeatures().get("ref-type")).equals("bibr")) {
					return true;
				}
				return false;
			});
			logger.info("JATS source document: importing inline citations...");
			return;
		}
		else if(Util.strCompare(sourceType.orElse(null), SourceENUM.PDFX.toString())) {
			// Transfer in-line citations from PDFX doc
			GateUtil.transferAnnotations(this.document, "xref", ImporterBase.inlineCitationMarkerAnnType, ImporterPDFX.PDFXAnnSet, ImporterBase.driAnnSet, (Annotation ann) -> {
				if(ann != null && ann.getFeatures() != null && ann.getFeatures().containsKey("ref-type") && ann.getFeatures().get("ref-type") != null && ((String) ann.getFeatures().get("ref-type")).equals("bibr")) {
					return true;
				}
				return false;
			});
			logger.info("PDFX source document: importing inline citations...");
		}
		else if(Util.strCompare(sourceType.orElse(null), SourceENUM.GROBID.toString())) {
			// Transfer in-line citations from GROBID doc
			GateUtil.transferAnnotations(this.document, "ref", ImporterBase.inlineCitationMarkerAnnType, ImporterGROBID.GROBIDannSet, ImporterBase.driAnnSet, (Annotation ann) -> {
				if(ann != null && ann.getFeatures() != null && ann.getFeatures().containsKey("type") && ann.getFeatures().get("type") != null && ((String) ann.getFeatures().get("type")).equals("bibr")) {
					return true;
				}
				return false;
			});
						
			logger.info("GROBID source document: imported inline citations...");
		}

		// A) GENERATE STATS ON CITATION ANNOTATION RULES
		// 1) "ruleSet", "PAR" and "rule", "Parent_AuthorEtAl" or "rule", "Parent_AuthorEtAl_Syntactic"
		// 2) "ruleSet", "SB" and "rule", "SquareBrackets_SingleCit" or "rule", "SquareBrackets_MultipleCit"
		// 3) "ruleSet", "SBNL" and "rule", "SquareBrackets_NumLetter_SingleCit" or "rule", "SquareBrackets_NumLetter_MultipleCit"
		
		
		Integer counterPARinCentralPartOfDoc = 0;
		Integer counterSBinCentralPartOfDoc = 0;
		Integer counterSBNLinCentralPartOfDoc = 0;
		
		List<Annotation> candidateCitationsAS = gate.Utils.inDocumentOrder(doc.getAnnotations(ImporterBase.driAnnSet).get(this.inputCandidateInlineCitationAStype));
		

		// ****************************************
		// B) START HEURISTICS
		String chosenRULE = "";

		// HEURISTIC 1: if there are more than 4 InlineCitations that are at the beginning of a Sentence,
		// are placed in the last 30% of a paper and are identified by the same rule 
		// this rule set is the one of the citations of the paper.
		// These InlineCitations are thus part of the bibliography
		Integer counterPARatBeginningOfSentence = 0;
		Integer counterSBatBeginningOfSentence = 0;
		Integer counterSBNLatBeginningOfSentence = 0;
		Iterator<Annotation> candidateCitationsIter = candidateCitationsAS.iterator();
		while(candidateCitationsIter.hasNext()) {
			Annotation candidateCitation = candidateCitationsIter.next();

			if(candidateCitation != null && candidateCitation.getFeatures() != null) {
				Optional<String> ruleSet = GateUtil.getStringFeature(candidateCitation, "ruleSet");
				
				// Get sentences spanning over the candidateCitation
				AnnotationSet spanningSentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType).get(
						candidateCitation.getStartNode().getOffset(), candidateCitation.getEndNode().getOffset());

				if(spanningSentences != null && spanningSentences.size() > 0) {
					Double docLength = gate.Utils.lengthLong(doc) + 0d;
					Double minimumOffset = (docLength / 10d) * 7d;
					Long minimumOffsetLong = minimumOffset.longValue();

					Iterator<Annotation> spanningSentencesIter = spanningSentences.iterator();
					while(spanningSentencesIter.hasNext()) {
						Annotation spanningSent = spanningSentencesIter.next();

						if(spanningSent != null && spanningSent.getStartNode().getOffset() >= minimumOffsetLong) {
							if(candidateCitation.getStartNode().getOffset() >= (spanningSent.getStartNode().getOffset() -1l) &&
									candidateCitation.getStartNode().getOffset() <= (spanningSent.getStartNode().getOffset() + 1l) ) {
								if(ruleSet.isPresent() && ruleSet.equals("PAR")) {
									counterPARatBeginningOfSentence++;
								} else if(ruleSet.isPresent() && ruleSet.equals("SB")) {
									counterSBatBeginningOfSentence++;
								} else if(ruleSet.isPresent() && ruleSet.equals("SBNL")) {
									counterSBNLatBeginningOfSentence++;
								}

							}
						}
					}
				}
			}
		}

		if(counterPARatBeginningOfSentence >= 4 && counterSBatBeginningOfSentence < 4 && counterSBNLatBeginningOfSentence < 4) {
			chosenRULE = "PAR";
		} else if(counterSBatBeginningOfSentence >= 4 && counterPARatBeginningOfSentence < 4 && counterSBNLatBeginningOfSentence < 4) {
			chosenRULE = "SB";
		} else if(counterSBNLatBeginningOfSentence >= 4 && counterPARatBeginningOfSentence < 4 && counterSBatBeginningOfSentence < 4) {
			chosenRULE = "SBNL";
		} else {
			// HEURISTIC 2: get the most frequent rule in the first half of the paper
			Integer counterPARinFirstHalf = 0;
			Integer counterSBinFirstHalf = 0;
			Integer counterSBNLinFirstHalf = 0;
			Iterator<Annotation> candidateCitationsDirstHalfIter = candidateCitationsAS.iterator();
			while(candidateCitationsDirstHalfIter.hasNext()) {
				Annotation candidateCitationFirstHalf = candidateCitationsDirstHalfIter.next();

				Optional<String> ruleSet = GateUtil.getStringFeature(candidateCitationFirstHalf, "ruleSet");

				if(candidateCitationFirstHalf != null && candidateCitationFirstHalf.getFeatures() != null) {
					// Get sentences spanning over the candidateCitation
					AnnotationSet spanningSentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType).get(
							candidateCitationFirstHalf.getStartNode().getOffset(), candidateCitationFirstHalf.getEndNode().getOffset());

					if(spanningSentences != null && spanningSentences.size() > 0) {
						Double docLength = gate.Utils.lengthLong(doc) + 0d;
						Double minimumOffset = (docLength / 10d) * 4d;
						Long maximumOffsetLong = minimumOffset.longValue();

						Iterator<Annotation> spanningSentencesIter = spanningSentences.iterator();
						while(spanningSentencesIter.hasNext()) {
							Annotation spanningSent = spanningSentencesIter.next();

							if(spanningSent != null && spanningSent.getStartNode().getOffset() <= maximumOffsetLong) {
								if(ruleSet.isPresent() && ruleSet.equals("PAR")) {
									counterPARinFirstHalf++;
								} else if(ruleSet.isPresent() && ruleSet.equals("SB")) {
									counterSBinFirstHalf++;
								} else if(ruleSet.isPresent() && ruleSet.equals("SBNL")) {
									counterSBNLinFirstHalf++;
								}
							}
						}
					}
				}
			}

			if(counterPARinFirstHalf > (counterSBinFirstHalf + 3) && counterPARinFirstHalf > (counterSBNLinFirstHalf + 3)) {
				chosenRULE = "PAR";
			} else if(counterSBinFirstHalf > (counterPARinFirstHalf + 3) && counterSBinFirstHalf > (counterSBNLinFirstHalf + 3)) {
				chosenRULE = "SB";
			} else if(counterSBNLinFirstHalf > (counterPARinFirstHalf + 3) && counterSBNLinFirstHalf > (counterSBinFirstHalf + 3)) {
				chosenRULE = "SBNL";
			} else {
				// HEURISTIC 3: get the most frequent rule in the central part of the document (from 10% to 70%)
				Integer counterPAR = 0;
				Integer counterSB = 0;
				Integer counterSBNL = 0;
				
				Long documentLenght = gate.Utils.lengthLong(doc);
				Double docOffset_10_percent = documentLenght.doubleValue() / 10d;
				Double docOffset_70_percent = (documentLenght.doubleValue() / 10d) * 7d;
				
				candidateCitationsIter = candidateCitationsAS.iterator();
				while(candidateCitationsIter.hasNext()) {
					Annotation candidateCitation = candidateCitationsIter.next();
					if(candidateCitation != null && candidateCitation.getFeatures() != null) {
						Optional<String> ruleSet = GateUtil.getStringFeature(candidateCitation, "ruleSet");
						Optional<String> rule = GateUtil.getStringFeature(candidateCitation, "rule");

						if(ruleSet.isPresent() && rule.isPresent()) {
							if(ruleSet.equals("PAR")) {
								counterPAR++;
								if(candidateCitation.getStartNode().getOffset() >= docOffset_10_percent && candidateCitation.getStartNode().getOffset() <= docOffset_70_percent) {
									counterPARinCentralPartOfDoc++;
								}
							} else if(ruleSet.equals("SB")) {
								counterSB++;
								if(candidateCitation.getStartNode().getOffset() >= docOffset_10_percent && candidateCitation.getStartNode().getOffset() <= docOffset_70_percent) {
									counterSBinCentralPartOfDoc++;
								}
							} else if(ruleSet.equals("SBNL")) {
								counterSBNL++;
								if(candidateCitation.getStartNode().getOffset() >= docOffset_10_percent && candidateCitation.getStartNode().getOffset() <= docOffset_70_percent) {
									counterSBNLinCentralPartOfDoc++;
								}
							}
						}
					}
				}
				
				if(counterPAR > counterSB && counterPAR > counterSBNL) {
					chosenRULE = "PAR";
				} else if (counterSB > counterPAR && counterSB > counterSBNL) {
					chosenRULE = "SB";
				} else if (counterSBNL > counterPAR && counterSBNL > counterSB) {
					chosenRULE = "SBNL";
				}

			}
		}
		

		// Specific rules START
		if(counterPARinCentralPartOfDoc > 8) {
			chosenRULE = "PAR";
		} else if (counterSBNLinCentralPartOfDoc > 8) {
			chosenRULE = "SBNL";
		}
		// Specific rules END

		// END HEURISTICS
		// ****************************************


		// C) GENERATE STABLE CITATION ANNOTATIONS
		AnnotationSet outputAnnotationSet = doc.getAnnotations(ImporterBase.driAnnSet);

		Iterator<Annotation> candidateCitationsFinalIter = candidateCitationsAS.iterator();
		while(candidateCitationsFinalIter.hasNext()) {
			Annotation citationAnn = candidateCitationsFinalIter.next();

			if( citationAnn != null && ( chosenRULE == null || chosenRULE.equals("") || GateUtil.getStringFeature(citationAnn, "ruleSet").isPresent() &&
					GateUtil.getStringFeature(citationAnn, "ruleSet").get().equals(chosenRULE) ) ) {
				try {
					// Add InlineCitation annotation
					outputAnnotationSet.add(citationAnn.getStartNode().getOffset(), citationAnn.getEndNode().getOffset(),
							ImporterBase.inlineCitationAnnType, citationAnn.getFeatures());

					// Get all the citation markers contained in citationAnn
					List<Annotation> markersInInlineCitation = gate.Utils.inDocumentOrder(doc.getAnnotations(this.inputCandidateCitationASname).get(
							this.inputCandidateInlineCitationMarkerAStype).getContained(citationAnn.getStartNode().getOffset(), 
									citationAnn.getEndNode().getOffset()));

					if(!CollectionUtils.isEmpty(markersInInlineCitation)) {
						Iterator<Annotation> markersInInlineCitationIter = markersInInlineCitation.iterator();
						while(markersInInlineCitationIter.hasNext()) {
							Annotation markerAnn = markersInInlineCitationIter.next();
							
							// Check if the in-line citation marker to add (markerAnn) is not already present
							// (overlapping an existing cit marker or overlapped by an existing cit marker)
							boolean citMarkerAlreadyPresent = false;
							List<Annotation> alreadyAddedCitMarkersList = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);
							for(Annotation alreadyAddedCitMarker : alreadyAddedCitMarkersList) {
								if(alreadyAddedCitMarker.withinSpanOf(markerAnn) || markerAnn.withinSpanOf(alreadyAddedCitMarker)) {
									citMarkerAlreadyPresent = true;
								}
							}
							
							if(!citMarkerAlreadyPresent) {
								outputAnnotationSet.add(markerAnn.getStartNode().getOffset(), markerAnn.getEndNode().getOffset(),
										ImporterBase.inlineCitationMarkerAnnType, markerAnn.getFeatures());
							}
						}
					}

				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}
			}
		}

		// D) IF chosenRULE is "SB" or "SBNL" remove incorrect parenthesis
		if(chosenRULE.equals("SB") || chosenRULE.equals("SBNL")) {
			Iterator<Annotation> inlineCitationIter = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).iterator();

			Set<Integer> annotationIdWithRoundBrackets = new HashSet<Integer>();
			Set<Integer> annotationIdWithSquareBrackets = new HashSet<Integer>();
			while(inlineCitationIter.hasNext()) {
				Annotation ann  = inlineCitationIter.next();

				if(ann != null) {
					try {
						Optional<String> text = GateUtil.getAnnotationText(ann, doc);

						if(text.isPresent()) {
							if(text.get().contains("[") && text.get().contains("]")) {
								annotationIdWithSquareBrackets.add(ann.getId());
							} else if (text.get().contains("(") && text.get().contains(")")) {
								annotationIdWithRoundBrackets.add(ann.getId());
							}
						} 
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if(annotationIdWithRoundBrackets.size() > annotationIdWithSquareBrackets.size()) {
				for(Integer annIdToRemove : annotationIdWithSquareBrackets) {
					AnnotationSet containedInlineCitationMarkers = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationMarkerAnnType).getContained(
							doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove).getStartNode().getOffset(),
							doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove).getEndNode().getOffset());

					Set<Integer> containedCitMarkersToRemove = new HashSet<Integer>();
					if(!CollectionUtils.isEmpty(containedInlineCitationMarkers)) {
						Iterator<Annotation> containedInlineCitationMarkersIter = containedInlineCitationMarkers.iterator();
						while(containedInlineCitationMarkersIter.hasNext()) {
							Annotation containedInlineCitationMarkersAnn = containedInlineCitationMarkersIter.next();
							containedCitMarkersToRemove.add(containedInlineCitationMarkersAnn.getId());
						}
					}

					if(containedCitMarkersToRemove != null && containedCitMarkersToRemove.size() > 0) {
						for(Integer citMarkerAnnId : containedCitMarkersToRemove) {
							Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(citMarkerAnnId);
							doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
						}
					}

					Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove);
					doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
				}
			}
			else {
				for(Integer annIdToRemove : annotationIdWithRoundBrackets) {
					AnnotationSet containedInlineCitationMarkers = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationMarkerAnnType).getContained(
							doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove).getStartNode().getOffset(),
							doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove).getEndNode().getOffset());

					Set<Integer> containedCitMarkersToRemove = new HashSet<Integer>();
					if(containedInlineCitationMarkers != null && containedInlineCitationMarkers.size() > 0) {
						Iterator<Annotation> containedInlineCitationMarkersIter = containedInlineCitationMarkers.iterator();
						while(containedInlineCitationMarkersIter.hasNext()) {
							Annotation containedInlineCitationMarkersAnn = containedInlineCitationMarkersIter.next();
							containedCitMarkersToRemove.add(containedInlineCitationMarkersAnn.getId());
						}
					}

					if(containedCitMarkersToRemove != null && containedCitMarkersToRemove.size() > 0) {
						for(Integer citMarkerAnnId : containedCitMarkersToRemove) {
							Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(citMarkerAnnId);
							doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
						}
					}

					Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRemove);
					doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
				}
			}
		}


		// E) MERGE InlineCitation ANNOTATIONS
		boolean mergeOccurring = true;
		Set<Integer> annotationsIdToRemove = new HashSet<Integer>();
		while(mergeOccurring) {
			mergeOccurring = false;
			
			annotationsIdToRemove = new HashSet<Integer>();

			List<Annotation> inlineCitsToCheckForMerge = gate.Utils.inDocumentOrder(doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType));

			for(int i = 0; i < (inlineCitsToCheckForMerge.size() - 1); i++) {
				Annotation firstAnn = inlineCitsToCheckForMerge.get(i);
				Annotation secondAnn = inlineCitsToCheckForMerge.get(i + 1);

				// Check if firstAnn and secondAnn should be merged
				boolean mergeCitations = false;
				if(firstAnn != null && secondAnn != null) {
					
					if(firstAnn.getEndNode().getOffset() >= (secondAnn.getStartNode().getOffset() - 1l)) {
						mergeCitations = true;
					}
					
					// See if the text between the citations to merge is ok or not with merging
					String textBetweenCitations = "";
					if(firstAnn.getEndNode().getOffset() < secondAnn.getStartNode().getOffset()) {
						try {
							textBetweenCitations = doc.getContent().getContent(firstAnn.getEndNode().getOffset(), secondAnn.getStartNode().getOffset()).toString();
							textBetweenCitations = textBetweenCitations.trim();
							if(textBetweenCitations != null && textBetweenCitations.length() < 4) {
								if(textBetweenCitations.equals("")) {
									mergeCitations = true;
								} else if(textBetweenCitations.equals(",")) {
									mergeCitations = true;
								} else if(textBetweenCitations.toLowerCase().equals("and")) {
									mergeCitations = true;
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}	
					
					
				if(mergeCitations) {	
					FeatureMap fm = Factory.newFeatureMap();
					if(firstAnn.getFeatures() != null && firstAnn.getFeatures().containsKey("kind")) {
						fm.put("kind", firstAnn.getFeatures().get("kind"));
					}
					if(firstAnn.getFeatures() != null && firstAnn.getFeatures().containsKey("rule")) {
						fm.put("rule", firstAnn.getFeatures().get("rule"));
					}
					if(firstAnn.getFeatures() != null && firstAnn.getFeatures().containsKey("ruleSet")) {
						fm.put("ruleSet", firstAnn.getFeatures().get("ruleSet"));
					}
					
					try {
						Optional<String> firstAnnIdList = GateUtil.getStringFeature(firstAnn, "idList");
						Optional<String> secondAnnIdList = GateUtil.getStringFeature(secondAnn, "idList");
								
						String mergedIdList = "";
						if(firstAnnIdList.isPresent() && !firstAnnIdList.equals("")) {
							mergedIdList = firstAnnIdList.get();
						}
						if(secondAnnIdList.isPresent() && !secondAnnIdList.equals("")) {
							if(mergedIdList.length() > 0) {
								mergedIdList += ", " + secondAnnIdList;
							}
							else {
								mergedIdList = secondAnnIdList.get();
							}
						}
						
						fm.put("idList", mergedIdList);
						
						// Create new merged annotation
						Integer annotationId = doc.getAnnotations(ImporterBase.driAnnSet).add(firstAnn.getStartNode().getOffset(), secondAnn.getEndNode().getOffset(),
								ImporterBase.inlineCitationAnnType, fm);
						fm.put("GATEid", annotationId);
						
						// Delete old annotations
						annotationsIdToRemove.add(firstAnn.getId());
						annotationsIdToRemove.add(secondAnn.getId());
						
						// Break
						mergeOccurring = true;
						break;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			annotationsIdToRemove.stream().forEach((annId) -> {
				try {
					Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(annId);
					doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
				}
				catch (Exception e) {
					/* Do nothing */
				}
			});
		}
		
		// F) Remove overlapping with bibliographic entries
		List<Annotation> biblioEntryList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);
		Set<Annotation> citMarkerToRemoveSet = new HashSet<Annotation>();
		for(Annotation bibEntry : biblioEntryList) {
			List<Annotation> containedCitMarkerList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType, bibEntry);
			for(Annotation containedCitMarker : containedCitMarkerList) {
				citMarkerToRemoveSet.add(containedCitMarker);
			}
			List<Annotation> containedCitSpanList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType, bibEntry);
			for(Annotation containedCitSpan : containedCitSpanList) {
				citMarkerToRemoveSet.add(containedCitSpan);
			}
		}
		citMarkerToRemoveSet.stream().forEach((citMarkerAnnToRem) -> {
			this.document.getAnnotations(ImporterBase.driAnnSet).remove(citMarkerAnnToRem);
		});
		
	}
	
	@Override
	public boolean resetAnnotations() {

		if(!this.annotationReset) {
			// Remove all inlineCitation and inlineCitationMarker annotations
			List<Annotation> inlineCitation = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationAnnType);
			List<Annotation> inlineCitationMarker = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);
			
			inlineCitation.addAll(inlineCitationMarker);
			
			Set<Integer> annotaitonsIds = inlineCitation.stream().map((ann) -> { return ann.getId(); }).collect(Collectors.toSet());
			annotaitonsIds.forEach((annId) -> {
				Annotation ann = this.document.getAnnotations(ImporterBase.driAnnSet).get(annId);
				if(ann != null)  this.document.getAnnotations(ImporterBase.driAnnSet).remove(ann);
			});
			
			this.annotationReset = true;
		}
		
		return true;
	}
	
}

