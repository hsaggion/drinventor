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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;


/**
 * Link InlineCitation with the corresponding bibliographic entry
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Inline citations linker")
public class CitationLinker  extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static Logger logger = Logger.getLogger(CitationLinker.class);

	private Integer bibEntryCounter;

	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		bibEntryCounter = 1;

		// Get the document to process
		gate.Document doc = getDocument();

		// For each source type specific pre-processing actions can be performed
		Optional<String> sourceType = GateUtil.getStringFeature(doc, "source");

		// PDFX bibliographic entry linking pre-processing
		/*
		 * If the Document Source Type is PDFX, it is possible to rely on its associations
		 * of bibliographic entries to in-line citations and then refine this information by means
		 * of the standard in-line citations-bibliographic entry linking procedure
		 */
		if(Util.strCompare(sourceType.orElse(null), SourceENUM.PDFX.toString())) {
			List<Annotation> bibEntryAnnotations = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);

			List<Annotation> invalidMarkers = new ArrayList<Annotation>();

			bibEntryAnnotations.stream().forEach((bibEntryAnn) -> {
				try {
					Optional<String> rText = GateUtil.getAnnotationText(bibEntryAnn, doc);
					Optional<String> rID = GateUtil.getStringFeature(bibEntryAnn, ImporterPDFX.PDFXbibEntry_IdFeat);

					if(rText.isPresent() && rID.isPresent()) {
						List<Annotation> citMarkerAnn = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);

						List<Annotation> selectedCitMarkers = citMarkerAnn.stream().filter((markerAnn) -> {
							String markerID = GateUtil.getStringFeature(markerAnn, ImporterPDFX.PDFXcitMarker_IdFeat).orElse(null);

							if(Util.strCompareTrimmed(markerID, rID.get())) {
								// Check if the markerAnn is inside a valid document sentence, if not ignore it
								AnnotationSet sentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);
								if(sentences != null) {
									AnnotationSet coveringSentences = sentences.getCovering(ImporterBase.sentenceAnnType, markerAnn.getStartNode().getOffset(), markerAnn.getEndNode().getOffset());
									if(coveringSentences != null && coveringSentences.size() > 0) {
										return true;
									}
									else {
										invalidMarkers.add(markerAnn);
									}
								}
							}

							return false;
						}).collect(Collectors.toList());

						if(!CollectionUtils.isEmpty(selectedCitMarkers)) {
							// The citation marker has been validated since it overlaps a valid sentence
							bibEntryAnn.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							for(Annotation selectedCitMarker : selectedCitMarkers) {
								selectedCitMarker.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							}
							bibEntryCounter++;
						}
					}

				}
				catch (Exception e) {
					Util.notifyException("Parsing bibliographic entry", e, logger);
				}
			});

		}
		else if(Util.strCompare(sourceType.orElse(null), SourceENUM.JATS.toString())) {
			List<Annotation> bibEntryAnnotations = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);

			List<Annotation> invalidMarkers = new ArrayList<Annotation>();

			bibEntryAnnotations.stream().forEach((bibEntryAnn) -> {
				try {
					Optional<String> rText = GateUtil.getAnnotationText(bibEntryAnn, this.document);
					Optional<String> rID = GateUtil.getStringFeature(bibEntryAnn, ImporterJATS.JATSbibEntry_IdFeat);

					if(rText.isPresent() && rID.isPresent()) {
						List<Annotation> citMarkerAnn = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);

						List<Annotation> selectedCitMarkers = citMarkerAnn.stream().filter((markerAnn) -> {
							String markerID = GateUtil.getStringFeature(markerAnn, ImporterJATS.JATScitMarker_refIdFeat).orElse(null);

							if(Util.strCompareTrimmed(markerID, rID.get())) {
								// Check if the markerAnn is inside a valid document sentence, if not ignore it
								AnnotationSet sentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);
								if(sentences != null) {
									AnnotationSet coveringSentences = sentences.getCovering(ImporterBase.sentenceAnnType, markerAnn.getStartNode().getOffset(), markerAnn.getEndNode().getOffset());
									if(coveringSentences != null && coveringSentences.size() > 0) {
										return true;
									}
									else {
										invalidMarkers.add(markerAnn);
									}
								}
							}

							return false;
						}).collect(Collectors.toList());

						if(!CollectionUtils.isEmpty(selectedCitMarkers)) {
							// The citation marker has been validated since it overlaps a valid sentence
							bibEntryAnn.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							for(Annotation selectedCitMarker : selectedCitMarkers) {
								selectedCitMarker.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							}
							bibEntryCounter++;
						}
					}

				}
				catch (Exception e) {
					Util.notifyException("Parsing bibliographic entry", e, logger);
				}
			});
		}
		else if(Util.strCompare(sourceType.orElse(null), SourceENUM.GROBID.toString())) {
			List<Annotation> bibEntryAnnotations = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);

			List<Annotation> invalidMarkers = new ArrayList<Annotation>();

			bibEntryAnnotations.stream().forEach((bibEntryAnn) -> {
				try {
					Optional<String> rText = GateUtil.getAnnotationText(bibEntryAnn, this.document);
					Optional<String> rID = GateUtil.getStringFeature(bibEntryAnn, ImporterGROBID.GROBIDbibEntry_IdFeat);

					if(rText.isPresent() && rID.isPresent()) {
						List<Annotation> citMarkerAnn = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);

						List<Annotation> selectedCitMarkers = citMarkerAnn.stream().filter((markerAnn) -> {
							String markerID = GateUtil.getStringFeature(markerAnn, ImporterGROBID.GROBIDcitMarker_refIdFeat).orElse(null);
							markerID = (markerID != null && markerID.trim().startsWith("#") && markerID.length() > 1) ? markerID.substring(1) : markerID;
							
							if(Util.strCompareTrimmed(markerID, rID.get())) {
								// Check if the markerAnn is inside a valid document sentence, if not ignore it
								AnnotationSet sentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);
								if(sentences != null) {
									AnnotationSet coveringSentences = sentences.getCovering(ImporterBase.sentenceAnnType, markerAnn.getStartNode().getOffset(), markerAnn.getEndNode().getOffset());
									if(coveringSentences != null && coveringSentences.size() > 0) {
										return true;
									}
									else {
										invalidMarkers.add(markerAnn);
									}
								}
							}

							return false;
						}).collect(Collectors.toList());

						if(!CollectionUtils.isEmpty(selectedCitMarkers)) {
							// The citation marker has been validated since it overlaps a valid sentence
							bibEntryAnn.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							for(Annotation selectedCitMarker : selectedCitMarkers) {
								selectedCitMarker.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter);
							}
							bibEntryCounter++;
						}
					}

				}
				catch (Exception e) {
					Util.notifyException("Parsing bibliographic entry", e, logger);
				}
			});
		}

		// ****************************************************************
		// ********** SOURCE-INDEPENDENT CITATION LINKER CODE *************
		// Go through all the in-line citation markers that have not been linked to a bibliographic entry by means of PDFX and try to guess the match
		List<Annotation> citMarkerAnnotations = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);

		citMarkerAnnotations.stream().filter((citMarkAnn) -> {
			// Filter out in-line citation markers that already have a bibliographic entry associated
			if(citMarkAnn != null && !GateUtil.getStringFeature(citMarkAnn, ImporterBase.bibEntry_IdAnnFeat).isPresent()) {
				return true;
			}
			return false;
		}).forEach((citMarkerAnn) -> {
			Optional<String> citMarkerText = GateUtil.getAnnotationText(citMarkerAnn, this.document);

			if(citMarkerText.isPresent()) {
				// For each in-line citation marker not associated to a bibliographic entry, try to see if there is a bibliographic entry that can match
				List<Annotation> bibEntryAnnotations = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);

				bibEntryAnnotations.stream().forEach((bibEntryAnn) -> {
					try {
						Optional<String> rText = GateUtil.getAnnotationText(bibEntryAnn, this.document);
						if(rText.isPresent() && isTextInCitation(citMarkerText.get(), rText.get())) {
							// The in-line citation marker matches the reference text and does not have a bibliographic entry associated

							// Check if the citMarkerAnn is inside a valid document sentence, if not ignore it
							AnnotationSet sentences = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);
							if(sentences != null) {
								AnnotationSet coveringSentences = sentences.getCovering(ImporterBase.sentenceAnnType, citMarkerAnn.getStartNode().getOffset(), citMarkerAnn.getEndNode().getOffset());
								if(coveringSentences != null && coveringSentences.size() > 0) {
									// Add the reference id of the bibliographic entry (bibEntryAnn) to the in-line citation marker (citMarkerAnn)

									// Get the reference id from the bibliographic entry (bibEntryAnn) or add a new one if not present
									Optional<String> rID = GateUtil.getStringFeature(bibEntryAnn, ImporterBase.bibEntry_IdAnnFeat);
									if(!rID.isPresent()) {
										bibEntryAnn.setFeatures((bibEntryAnn.getFeatures() == null) ? Factory.newFeatureMap() : bibEntryAnn.getFeatures());
										bibEntryAnn.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, "bib_" + bibEntryCounter++);
										rID = GateUtil.getStringFeature(bibEntryAnn, ImporterBase.bibEntry_IdAnnFeat);
									}

									// Add the reference id - feature (ImportBase.bibEntryIdAnnFeat) - to the in-line citation marker annotation 
									citMarkerAnn.setFeatures(citMarkerAnn.getFeatures() != null ? citMarkerAnn.getFeatures() : Factory.newFeatureMap());
									citMarkerAnn.getFeatures().put(ImporterBase.bibEntry_IdAnnFeat, rID.get().toString());
								}
							}
						}
					}
					catch (Exception e) {
						Util.notifyException("Parsing bibliographic entry", e, logger);
					}
				});
			}

		});


	}


	/**
	 * Heuristics to verify if the in-line citation marker can be linked to the bibliographic entry
	 * 
	 * @param inlineMarker In-line citation marker
	 * @param biblioEntry Bibliographic entry text
	 * @return
	 */
	private boolean isTextInCitation(String inlineMarker, String biblioEntry) {
		boolean retValue = false;

		if(inlineMarker != null && !inlineMarker.equals("") && biblioEntry != null && !biblioEntry.equals("")) {
			String inlineInit = (inlineMarker.length() > 15) ? inlineMarker.substring(0,15): inlineMarker;
			String citInit = (biblioEntry.length() > 15) ? biblioEntry.substring(0,15): biblioEntry;

			// PATCHES - start
			// Second char comma ---> delete
			if(inlineInit.length() > 1 && inlineInit.charAt(1) == ',' && inlineInit.length() > 3) {
				inlineInit = inlineInit.substring(0, 1) + inlineInit.substring(2);
			}

			if(citInit.length() > 1 && citInit.charAt(1) == ',' && citInit.length() > 3) {
				citInit = citInit.substring(0, 1) + citInit.substring(2);
			}

			// Second char space ---> delete (ACM SIGGRAPH)
			if(inlineInit.length() > 1 && inlineInit.charAt(1) == ' ' && inlineInit.length() > 3) {
				inlineInit = inlineInit.substring(0, 1) + inlineInit.substring(2);
			}

			if(citInit.length() > 1 && citInit.charAt(1) == ' ' && citInit.length() > 3) {
				citInit = citInit.substring(0, 1) + citInit.substring(2);
			}
			// PATCHES - end

			// Check if the in-line citation marker is numeric
			boolean inlineInitIsNum = false;
			try {
				@SuppressWarnings("unused")
				Long inlineInitLong = Long.valueOf(inlineInit.trim());
				inlineInitIsNum = true;
			}
			catch (NumberFormatException nfe) {

			}

			if(inlineInitIsNum) {
				String inlineInit_1 = "[" + inlineInit.trim() + "]";
				String inlineInit_2 = "(" + inlineInit.trim() + ")";
				String inlineInit_3 = "" + inlineInit.trim() + ".";
				String inlineInit_4 = "[ " + inlineInit.trim() + " ]";
				String inlineInit_5 = "( " + inlineInit.trim() + " )";
				String inlineInit_6 = "" + inlineInit.trim() + " .";

				if(citInit.contains(inlineInit_1) || citInit.contains(inlineInit_2) || 
						citInit.contains(inlineInit_3) || citInit.contains(inlineInit_4) ||
						citInit.contains(inlineInit_5) || citInit.contains(inlineInit_6)) {
					retValue = true;
				}
			}
			else {
				// The in-line citation marker is not numeric
				String[] splitInlineInit = inlineInit.split(" ");
				String[] splitCitInit = citInit.split(" ");

				if(splitCitInit.length > 0 && 
						splitInlineInit.length > 0 && splitInlineInit[0] != null && splitInlineInit[0].length() > 2) {
					splitInlineInit[0] = splitInlineInit[0].replace(".", "");
					splitInlineInit[0] = splitInlineInit[0].replace(",", "");
					splitInlineInit[0] = splitInlineInit[0].replace(";", "");
					splitInlineInit[0] = splitInlineInit[0].replace(":", "");


					if(splitInlineInit[0].length() > 2 && splitCitInit[0] != null) {
						splitCitInit[0] = splitCitInit[0].replace(".", "");
						splitCitInit[0] = splitCitInit[0].replace(",", "");
						splitCitInit[0] = splitCitInit[0].replace(";", "");
						splitCitInit[0] = splitCitInit[0].replace(":", "");
						if(splitCitInit[0].length() > 2 && splitCitInit[0].toLowerCase().trim().equals(splitInlineInit[0].toLowerCase().trim())) {
							retValue = true;
						}
					}
					if(splitInlineInit[0].length() > 2 && splitCitInit.length > 1 && splitCitInit[1] != null) {
						splitCitInit[1] = splitCitInit[1].replace(".", "");
						splitCitInit[1] = splitCitInit[1].replace(",", "");
						splitCitInit[1] = splitCitInit[1].replace(";", "");
						splitCitInit[1] = splitCitInit[1].replace(":", "");
						if(splitCitInit[1].length() > 2  && splitCitInit[1].toLowerCase().trim().equals(splitInlineInit[0].toLowerCase().trim())) {
							retValue = true;
						}
					}					
				}
			}
		}

		return retValue;
	}

	@Override
	public boolean resetAnnotations() {
		
		if(!this.annotationReset) {
			// Get the document to process
			gate.Document doc = getDocument();

			AnnotationSet driAnnotationSet = doc.getAnnotations(ImporterBase.driAnnSet);

			// Remove all the ImporterBase.bibEntry_IdAnnFeat features from each inline citation marker
			List<Annotation> citMarkerAnnotations = gate.Utils.inDocumentOrder(driAnnotationSet.get(ImporterBase.inlineCitationMarkerAnnType));

			for(Annotation citMarkerAnnotation : citMarkerAnnotations) {
				String rid = GateUtil.getStringFeature(citMarkerAnnotation, ImporterBase.bibEntry_IdAnnFeat).orElse(null);
				if(rid != null) {
					citMarkerAnnotation.getFeatures().remove(ImporterBase.bibEntry_IdAnnFeat);
				}
			}
			
			this.annotationReset = true;
		}

		return true;
	}
}

