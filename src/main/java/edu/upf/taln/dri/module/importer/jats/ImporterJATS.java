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
package edu.upf.taln.dri.module.importer.jats;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.SourceENUM;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;

/**
 * From the JATS XML mark-up, this processing resource identifies all the textual contents,
 * split their sentences by exploiting a customized REGEXP Sentence Splitter of ANNIE and store the
 * new sentence annotations
 * 
 *
 */
@CreoleResource(name = "DRI Modules - JATS importer")
public class ImporterJATS extends ImporterBase {

	private static Logger logger = Logger.getLogger(ImporterJATS.class);	

	private static final long serialVersionUID = 1L;

	public static final String JATSannSet = "Original markups";
	public static final String JATSbibEntry = "ref";
	public static final String JATSbibEntry_IdFeat = "id";
	public static final String JATScitMarker = "xref";
	public static final String JATScitMarker_refTypeFeat = "ref-type";
	public static final String JATScitMarker_refIdFeat = "rid";
	public static final String JATScitElem_Container = "mixed-citation";
	public static final String JATScitElem_Year = "year";
	public static final String JATScitElem_Day = "day";
	public static final String JATScitElem_Month = "month";
	public static final String JATScitElem_Title = "article-title";
	public static final String JATScitElem_Source = "source";
	public static final String JATScitElem_Volume = "volume";
	public static final String JATScitElem_Issue = "issue";
	public static final String JATScitElem_Fpage = "fpage";
	public static final String JATScitElem_Lpage = "lpage";
	public static final String JATScitElem_Link = "ext-link";
	public static final String JATScitElem_PubId = "pub-id";
	public static final String JATScitElem_PubId_typeFeat = "pub-id-type";
	public static final String JATScitElem_AuthName = "name";
	public static final String JATScitElem_AuthName_Surmane = "surname";
	public static final String JATScitElem_AuthName_GivenNames = "given-names";
	public static final String JATScitElem_PublisherName = "publisher-name";
	public static final String JATScitElem_PublisherLoc = "publisher-loc";

	public static final String JATSabstract = "abstract";
	public static final String JATStitle = "title";
	public static final String JATSsection = "sec";
	public static final String JATSfigure = "fig";
	public static final String JATStable = "table-wrap";
	
	public static final String JATScontrib = "contrib";
	public static final String JATScontrib_authTypeFeat = "contrib-type";
	public static final String JATScontribName = "name";
	public static final String JATScontribGivenName = "given-names";
	public static final String JATScontribSurname = "surname";
	public static final String JATScontribXref = "xref";
	
	public static final String JATSaffiliation = "aff";
	public static final String JATSaffiliationAddressLine = "addr-line";
	public static final String JATSaffiliationAddressLine_INSTITUTION = "institution";
	public static final String JATSaffiliationAddressLine_CITY = "city";
	public static final String JATSaffiliationAddressLine_STATE = "state";
	public static final String JATSaffiliationAddressLine_EMAIL = "email";
	public static final String JATSaffiliationAddressLine_EXTLINK = "ext-link";
	public static final String JATSaffiliationAddressLine_URI = "uri";

	// Input and output annotation
	private String inputSentenceASname;
	private String inputSentenceAStype;

	public String getInputSentenceASname() {
		return inputSentenceASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set to read sentence annotations from (sentence annotations previously added by sentence splitter execution)")
	public void setInputSentenceASname(String inputSentenceASname) {
		this.inputSentenceASname = inputSentenceASname;
	}

	public String getInputSentenceAStype() {
		return inputSentenceAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Sentence", comment = "The name of the input annotation type to read sentence annotations (sentence annotations previously added by sentence splitter execution)")
	public void setInputSentenceAStype(String inputSentenceAStype) {
		this.inputSentenceAStype = inputSentenceAStype;
	}

	/**
	 * Internal utility function to add a sentence annotation of type outputAStypeAppo to the annotation sets outputAs
	 * and outputAsOriginal, starting at startNode, ending at endNode and with features map equal to fm.
	 * 
	 * This method, before adding a new sentence annotation, performs the following steps:
	 *    - removes the header of section eventually included in the sentence
	 *    - left and right trims the sentence span
	 *    - check for duplicated sentence annotations
	 * 
	 * @param doc
	 * @param pdfx
	 * @param outputAs
	 * @param outputAsOriginal
	 * @param outputAStypeAppo
	 * @param startNode
	 * @param endNode
	 * @param fm
	 * @return
	 */
	private Integer addSentence(Document doc, AnnotationSet jats, AnnotationSet outputAs, String outputAStypeAppo, Long startNode, Long endNode, FeatureMap fm) {
		Integer newSentId = null;

		try {
			// Check if not header in sentence - if header in sentence, remove it
			List<String> headersAnnName = new ArrayList<String>();
			headersAnnName.add(ImporterBase.h1AnnType);
			headersAnnName.add(ImporterBase.h2AnnType);
			headersAnnName.add(ImporterBase.h3AnnType);
			headersAnnName.add(ImporterBase.h4AnnType);
			headersAnnName.add(ImporterBase.h5AnnType);

			for(String headName : headersAnnName) {
				AnnotationSet headersAnns = outputAs.get(headName, startNode, endNode);
				if(headersAnns != null && headersAnns.size() > 0) {
					Iterator<Annotation> headersAnnsIter = headersAnns.iterator();
					while(headersAnnsIter.hasNext()) {
						Annotation headersAnnsElem = headersAnnsIter.next();
						if(headersAnnsElem != null) {
							Long endOffsetHeadersAnnsElem = headersAnnsElem.getEndNode().getOffset();
							if(endOffsetHeadersAnnsElem != null && endOffsetHeadersAnnsElem > startNode && endOffsetHeadersAnnsElem < endNode) {
								startNode = endOffsetHeadersAnnsElem;
							}

							Long startOffsetHeadersAnnsElem = headersAnnsElem.getStartNode().getOffset();
							if(startOffsetHeadersAnnsElem != null && startOffsetHeadersAnnsElem > startNode && startOffsetHeadersAnnsElem < endNode) {
								endNode = startOffsetHeadersAnnsElem;
							}
						}
					}
				}
			}

			// Trim the new sentence annotation
			try {
				String sentContent = doc.getContent().getContent(startNode, endNode).toString();
				if(sentContent != null && sentContent.length() > 0) {
					for(int i = 0; i < sentContent.length(); i++) {
						char ch = sentContent.charAt(i);
						if(ch == ' ' || ch == '\n' || ch == '\t') {
							if(startNode < (endNode - 1)) {
								startNode = startNode + 1;
							}
						}
						else {
							break;
						}
					}

					for(int i = (sentContent.length() - 1); i >= 0; i--) {
						char ch = sentContent.charAt(i);
						if(ch == ' ' || ch == '\n' || ch == '\t') {
							if(startNode < (endNode - 1)) {
								endNode = endNode - 1;
							}
						}
						else {
							break;
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			// Check if the sentence is at least 10 chars long
			boolean tooFewChars = false;
			if( (endNode - startNode) < 10l) {
				tooFewChars = true;
			}

			// Check if the same sentence annotation was not already present, in order not to duplicate it
			boolean alreadyCreatedSentence = false;
			AnnotationSet asCheck = outputAs.getCovering(outputAStypeAppo, startNode, endNode);
			if(asCheck != null && asCheck.size() > 0) {
				alreadyCreatedSentence = true;
			}

			if(!tooFewChars && !alreadyCreatedSentence) {	
				newSentId = outputAs.add(startNode, endNode, outputAStypeAppo, fm);
			}

		} catch (InvalidOffsetException e) {
			logger.error("ERROR, InvalidOffsetException - " + e.getLocalizedMessage());
			e.printStackTrace();
		}

		return newSentId;
	}


	public void execute() throws ExecutionException {

		// Get the document to process
		gate.Document doc = getDocument();

		// Normalize variables
		String inputSentenceASnameAppo = (this.inputSentenceASname != null) ? this.inputSentenceASname : "";
		String inputSentenceAStypeAppo = (StringUtils.isNotBlank(this.inputSentenceAStype)) ? this.inputSentenceAStype : "Sentence";

		// Set PDFX as input type
		doc.setFeatures((doc.getFeatures() != null) ? doc.getFeatures() : Factory.newFeatureMap());
		doc.getFeatures().put("source", SourceENUM.JATS.toString());

		// Import from JATS annotations
		GateUtil.transferAnnotations(this.document, "abstract", ImporterBase.abstractAnnType, JATSannSet, ImporterBase.driAnnSet, null);
		GateUtil.transferAnnotations(this.document, JATSbibEntry, ImporterBase.bibEntryAnnType, JATSannSet, ImporterBase.driAnnSet, null);
		GateUtil.transferAnnotations(this.document, "article-title", ImporterBase.titleAnnType, JATSannSet, ImporterBase.driAnnSet, null);
		GateUtil.transferAnnotations(this.document, "caption", ImporterBase.captionAnnType, JATSannSet, ImporterBase.driAnnSet, null);

		// Inline cits: GateUtil.transferAnnotations(this.document, "xref", ImporterBase.inlineCitationMarkerAnnType, JATSannSet, ImporterBase.driAnnSet, getInlineRefs);

		// Make JATS bib entry properties explicit
		List<Annotation> bibEntryAnnList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);
		if(bibEntryAnnList != null && bibEntryAnnList.size() > 0) {
			for(Annotation bibEntry : bibEntryAnnList) {
				JATSparsing.apply(bibEntry);
			}
		}

		// Determine section headers
		List<Annotation> titleAnnList = GateUtil.getAnnInDocOrder(this.document, JATSannSet, JATStitle);
		titleAnnList.forEach((titleAnn) -> {
			if(titleAnn != null) {
				List<Annotation> intersectingSectionList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, JATSannSet, JATSsection, titleAnn);
				List<Annotation> intersectingFigureList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, JATSannSet, JATSfigure, titleAnn);
				List<Annotation> intersectingTableList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, JATSannSet, JATStable, titleAnn);

				if(intersectingFigureList.size() > 0 || intersectingTableList.size() > 0) {
					return;
				}

				try {
					if(intersectingSectionList.size() == 1) {
						doc.getAnnotations(ImporterBase.driAnnSet).add(titleAnn.getStartNode().getOffset(), titleAnn.getEndNode().getOffset(), ImporterBase.h1AnnType, Factory.newFeatureMap());
					}
					else if(intersectingSectionList.size() == 2) {
						doc.getAnnotations(ImporterBase.driAnnSet).add(titleAnn.getStartNode().getOffset(), titleAnn.getEndNode().getOffset(), ImporterBase.h2AnnType, Factory.newFeatureMap());
					}
					else if(intersectingSectionList.size() == 3) {
						doc.getAnnotations(ImporterBase.driAnnSet).add(titleAnn.getStartNode().getOffset(), titleAnn.getEndNode().getOffset(), ImporterBase.h3AnnType, Factory.newFeatureMap());
					}
					else if(intersectingSectionList.size() == 4) {
						doc.getAnnotations(ImporterBase.driAnnSet).add(titleAnn.getStartNode().getOffset(), titleAnn.getEndNode().getOffset(), ImporterBase.h4AnnType, Factory.newFeatureMap());
					}
					else if(intersectingSectionList.size() == 5) {
						doc.getAnnotations(ImporterBase.driAnnSet).add(titleAnn.getStartNode().getOffset(), titleAnn.getEndNode().getOffset(), ImporterBase.h5AnnType, Factory.newFeatureMap());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Create reference and new annotation sets
		AnnotationSet sentenceMarkup = ((inputSentenceASnameAppo != null && !inputSentenceASnameAppo.equals(""))? doc.getAnnotations(inputSentenceASnameAppo) : doc.getAnnotations());
		AnnotationSet jatsMarkup = doc.getAnnotations(JATSannSet);
		AnnotationSet outputMarkup = doc.getAnnotations(ImporterBase.driAnnSet);

		// Copy all the (inputSentenceAStypeAppo) sentences as annotations of type (inputSentenceAStypeAppo + "_OLD") and delete the original annotations
		Set<Integer> annIdOfOldSentences = new HashSet<Integer>();
		Set<Integer> annIdToDelete = new HashSet<Integer>();
		List<Annotation> inputSentenceAnnotations = gate.Utils.inDocumentOrder(sentenceMarkup.get(inputSentenceAStypeAppo));
		inputSentenceAnnotations.stream().forEach((ann) -> {
			if(ann != null) {
				ann.getFeatures().put("OLD_SENTENCE", "TO_DELETE");

				// Generate a copy of the sentence with type sufficed by "_OLD"
				try {
					Integer annToDeleteId = sentenceMarkup.add(ann.getStartNode().getOffset(), ann.getEndNode().getOffset(), inputSentenceAStypeAppo + "_OLD", ann.getFeatures());
					annIdOfOldSentences.add(annToDeleteId);
				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}

				// Delete the original annotation
				annIdToDelete.add(ann.getId());
			}
		});

		if(annIdToDelete != null && annIdToDelete.size() > 0) {
			for(Integer annIdToDel : annIdToDelete) {
				if(annIdToDel != null) {
					Annotation sentenceAnn = sentenceMarkup.get(inputSentenceAStypeAppo).get(annIdToDel);
					if(sentenceAnn != null) {
						sentenceMarkup.remove(sentenceAnn);
					}
				}
			}
		}


		// Adding sentences from the JATS abstract annotation / XML element
		List<Annotation> abstractAnnList = GateUtil.getAnnInDocOrder(this.document, JATSannSet, JATSabstract);
		if(abstractAnnList.size() > 0) {
			for(Annotation abstractAnn : abstractAnnList) {
				// Go through tokens overlapping annotation and add as sentences
				List<Annotation> includedSentAnnList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ((inputSentenceASnameAppo != null)? inputSentenceASnameAppo : ""),
						inputSentenceAStypeAppo + "_OLD", abstractAnn);

				Long offsetFirstSentence = Long.MAX_VALUE;
				for(Annotation sentenceAnn : includedSentAnnList) {
					if(sentenceAnn.getStartNode().getOffset() < offsetFirstSentence) {
						offsetFirstSentence = sentenceAnn.getStartNode().getOffset();
					}
					FeatureMap fm = new SimpleFeatureMapImpl();
					fm.put("JATS_from", JATSabstract);

					// Import JATS features in the new sentence annotation (names prefixed by 'JATS_')
					for(Map.Entry<Object, Object> entry : abstractAnn.getFeatures().entrySet()) {
						try {
							String featName = (String) entry.getKey();
							fm.put("JATS__" + featName, entry.getValue());
						}
						catch (Exception e) {

						}
					}

					Integer newSentenceId = addSentence(doc, jatsMarkup, outputMarkup, ImporterBase.sentenceAnnType, sentenceAnn.getStartNode().getOffset(), sentenceAnn.getEndNode().getOffset(), fm);
					fm.put("gateID", newSentenceId);
				}

				if(offsetFirstSentence > abstractAnn.getStartNode().getOffset() + 10l) {
					FeatureMap fm = new SimpleFeatureMapImpl();
					fm.put("JATS_from", JATSabstract);
					Integer newSentenceId = addSentence(doc, jatsMarkup, outputMarkup, ImporterBase.sentenceAnnType, abstractAnn.getStartNode().getOffset(), offsetFirstSentence, fm);
					fm.put("gateID", newSentenceId);
				}
			}
		}


		// Adding sentences from the JATS section annotations / XML elements
		List<Annotation> sectionAnnList = GateUtil.getAnnInDocOrder(this.document, JATSannSet, JATSsection);
		for(Annotation sectAnn : sectionAnnList) {
			if(sectAnn != null) {
				List<Annotation> includedSentAnnList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ((inputSentenceASnameAppo != null)? inputSentenceASnameAppo : ""),
						inputSentenceAStypeAppo + "_OLD", sectAnn);

				for(Annotation sentenceAnn : includedSentAnnList) {

					// Check if not part of figure or table
					List<Annotation> intersectingFigureList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, JATSannSet, JATSfigure, sentenceAnn);
					List<Annotation> intersectingTableList = GateUtil.getAnnInDocOrderIntersectAnn(this.document, JATSannSet, JATStable, sentenceAnn);

					if(intersectingFigureList.size() > 0 || intersectingTableList.size() > 0) {
						continue;
					}

					FeatureMap fm = new SimpleFeatureMapImpl();
					fm.put("JATS_from", JATSsection);

					// Import JATS features in the new sentence annotation (names prefixed by 'JATS_')
					for(Map.Entry<Object, Object> entry : sectAnn.getFeatures().entrySet()) {
						try {
							String featName = (String) entry.getKey();
							fm.put("JATS__" + featName, entry.getValue());
						}
						catch (Exception e) {

						}
					}

					Integer newSentenceId = addSentence(doc, jatsMarkup, outputMarkup, ImporterBase.sentenceAnnType, sentenceAnn.getStartNode().getOffset(), sentenceAnn.getEndNode().getOffset(), fm);
					fm.put("gateID", newSentenceId);
				}

			}
		}

		// Remove all the annotation ids of sentences to remove - with type inputSentenceAStypeAppo + "_OLD"
		if(annIdOfOldSentences != null && annIdOfOldSentences.size() > 0) {
			for(Integer annIdOfOldSent : annIdOfOldSentences) {
				if(annIdOfOldSent != null) {
					Annotation sentenceAnn = sentenceMarkup.get(inputSentenceAStypeAppo + "_OLD").get(annIdOfOldSent);
					if(sentenceAnn != null) {
						sentenceMarkup.remove(sentenceAnn);
					}
				}
			}
		}
	}

	// Make JATS properties explicit
	public Function<Annotation, Boolean> JATSparsing = (citation) -> {
		if(citation != null) {
			
			if(citation.getFeatures() == null) {
				citation.setFeatures(Factory.newFeatureMap());
			}

			FeatureMap fm = citation.getFeatures();
			
			List<Annotation> names = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_AuthName, citation);
			if(names != null && names.size() > 0) {
				Integer authCount = 1;
				for(Annotation name : names) {
					String nameStr = GateUtil.getAnnotationText(name, this.document).orElse(null);
					if(nameStr != null) {
						fm.put("jats_author_" + authCount, nameStr.trim().replace("\n", " ").replaceAll(" +", " "));

						List<Annotation> givenNameList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_AuthName_GivenNames, name);
						if(givenNameList != null && givenNameList.size() > 0 && GateUtil.getAnnotationText(givenNameList.get(0), this.document).orElse(null) != null) {
							fm.put("jats_author_" + authCount + "_givenName", GateUtil.getAnnotationText(givenNameList.get(0), this.document).orElse(null).trim().replace("\n", " ").replaceAll(" +", " "));
						}

						List<Annotation> surnameList = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_AuthName_Surmane, name);
						if(surnameList != null && surnameList.size() > 0 && GateUtil.getAnnotationText(surnameList.get(0), this.document).orElse(null) != null) {
							fm.put("jats_author_" + authCount + "_surname", GateUtil.getAnnotationText(surnameList.get(0), this.document).orElse(null).trim().replace("\n", " ").replaceAll(" +", " "));
						}

						authCount++;
					}
				}
			}

			List<Annotation> title = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Title, citation);
			if(title != null && title.size() > 0 && GateUtil.getAnnotationText(title.get(0), this.document).orElse(null) != null) {
				fm.put("jats_title", GateUtil.getAnnotationText(title.get(0), this.document).orElse(null));
			}

			List<Annotation> source = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Source, citation);
			if(source != null && source.size() > 0 && GateUtil.getAnnotationText(source.get(0), this.document).orElse(null) != null) {
				fm.put("jats_source", GateUtil.getAnnotationText(source.get(0), this.document).orElse(null));
			}

			List<Annotation> link = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Link, citation);
			if(link != null && link.size() > 0 && GateUtil.getAnnotationText(link.get(0), this.document).orElse(null) != null) {
				fm.put("jats_link", GateUtil.getAnnotationText(link.get(0), this.document).orElse(null));
			}

			List<Annotation> pubIds = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_PubId, citation);
			if(pubIds != null && pubIds.size() > 0) {
				Integer pubIDcount = 1;
				for(Annotation pubId : pubIds) {
					String pubIDstr = GateUtil.getAnnotationText(pubId, this.document).orElse(null);
					if(pubIDstr != null) {
						fm.put("jats_pubID_" + pubIDcount, pubIDstr);

						List<Annotation> pubIDtypes = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_PubId_typeFeat, pubId);
						if(pubIDtypes != null && pubIDtypes.size() > 0 && GateUtil.getAnnotationText(pubIDtypes.get(0), this.document).orElse(null) != null) {
							fm.put("jats_pubID_" + pubIDcount + "_type", GateUtil.getAnnotationText(pubIDtypes.get(0), this.document).orElse(null));
						}

						pubIDcount++;
					}
				}
			}

			List<Annotation> issue = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Issue, citation);
			if(issue != null && issue.size() > 0 && GateUtil.getAnnotationText(issue.get(0), this.document).orElse(null) != null) {
				fm.put("jats_issue", GateUtil.getAnnotationText(issue.get(0), this.document).orElse(null));
			}

			List<Annotation> volume = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Volume, citation);
			if(volume != null && volume.size() > 0 && GateUtil.getAnnotationText(volume.get(0), this.document).orElse(null) != null) {
				fm.put("jats_volume", GateUtil.getAnnotationText(volume.get(0), this.document).orElse(null));
			}

			List<Annotation> year = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Year, citation);
			if(year != null && year.size() > 0 && GateUtil.getAnnotationText(year.get(0), this.document).orElse(null) != null) {
				fm.put("jats_year", GateUtil.getAnnotationText(year.get(0), this.document).orElse(null));
			}

			List<Annotation> publisherName = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_PublisherName, citation);
			if(publisherName != null && publisherName.size() > 0 && GateUtil.getAnnotationText(publisherName.get(0), this.document).orElse(null) != null) {
				fm.put("jats_publisherName", GateUtil.getAnnotationText(publisherName.get(0), this.document).orElse(null));
			}

			List<Annotation> publisherLoc = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_PublisherLoc, citation);
			if(publisherLoc != null && publisherLoc.size() > 0 && GateUtil.getAnnotationText(publisherLoc.get(0), this.document).orElse(null) != null) {
				fm.put("jats_publisherLoc", GateUtil.getAnnotationText(publisherLoc.get(0), this.document).orElse(null));
			}

			List<Annotation> fPage = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Fpage, citation);
			if(fPage != null && fPage.size() > 0 && GateUtil.getAnnotationText(fPage.get(0), this.document).orElse(null) != null) {
				fm.put("jats_firstPage", GateUtil.getAnnotationText(fPage.get(0), this.document).orElse(null));
			}

			List<Annotation> lPage = GateUtil.getAnnInDocOrderContainedAnn(this.document, ImporterJATS.JATSannSet, ImporterJATS.JATScitElem_Lpage, citation);
			if(lPage != null && lPage.size() > 0 && GateUtil.getAnnotationText(lPage.get(0), this.document).orElse(null) != null) {
				fm.put("jats_lastPage", GateUtil.getAnnotationText(lPage.get(0), this.document).orElse(null));
			}

		}
		
		return true;
	};

	@Override
	public boolean resetAnnotations() {
		// Delete JATS as input type
		if(this.document.getFeatures() != null) {
			this.document.getFeatures().remove("source");
		}

		// Delete annotations imported from PDFX
		List<String> annTypesToRemove = new ArrayList<String>();
		annTypesToRemove.add(ImporterBase.abstractAnnType);
		annTypesToRemove.add(ImporterBase.bibEntryAnnType);
		annTypesToRemove.add(ImporterBase.h1AnnType);
		annTypesToRemove.add(ImporterBase.h2AnnType);
		annTypesToRemove.add(ImporterBase.h3AnnType);
		annTypesToRemove.add(ImporterBase.h4AnnType);
		annTypesToRemove.add(ImporterBase.h5AnnType);
		annTypesToRemove.add(ImporterBase.titleAnnType);
		annTypesToRemove.add(ImporterBase.captionAnnType);
		annTypesToRemove.add(ImporterBase.sentenceAnnType);

		for(String annTypeToRemove : annTypesToRemove) {
			List<Annotation> annListToRem = GateUtil.getAnnInDocOrder(this.document, ImporterBase.driAnnSet, annTypeToRemove);

			if(annListToRem != null && annListToRem.size() > 0) {
				for(Annotation annToRem : annListToRem) {
					if(annToRem != null) {
						this.document.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
					}
				}
			}
		}

		return true;
	}
}
