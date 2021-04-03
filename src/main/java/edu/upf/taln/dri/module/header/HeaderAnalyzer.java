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
package edu.upf.taln.dri.module.header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.connector.google.scholar.model.GoogleScholarResult;
import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.citation.BiblioEntryParser;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;


/**
 * Analyze the header of a paper to extract authors affiliations and e-mails
 * 
 */
@CreoleResource(name = "DRI Modules - Citation Aware MATE Parser")
public class HeaderAnalyzer  extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(HeaderAnalyzer.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private gate.Document originalDocument;

	// Input and output annotation
	private String inputTitleAS;
	private String inputTitleAStype;

	private String useGoogleScholar;
	private String useBibsonomy;

	public String getInputTitleAS() {
		return inputTitleAS;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set for the title")
	public void setInputTitleAS(String inputTitleAS) {
		this.inputTitleAS = inputTitleAS;
	}

	public String getInputTitleAStype() {
		return inputTitleAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Title", comment = "The name of the input annotation type for the title")
	public void setInputTitleAStype(String inputTitleAStype) {
		this.inputTitleAStype = inputTitleAStype;
	}

	public gate.Document getOriginalDocument() {
		return originalDocument;
	}

	@RunTime
	@CreoleParameter(comment = "The original GATE document to get data from")
	public void setOriginalDocument(gate.Document originalDocument) {
		this.originalDocument = originalDocument;
	}

	public String getUseGoogleScholar() {
		return useGoogleScholar;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set to true to parse biblio entries by Google Scholar")
	public void setUseGoogleScholar(String useGoogleScholar) {
		this.useGoogleScholar = useGoogleScholar;
	}

	public String getUseBibsonomy() {
		return useBibsonomy;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set to true to parse biblio entries by Bibsonomy")
	public void setUseBibsonomy(String useBibsonomy) {
		this.useBibsonomy = useBibsonomy;
	}

	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		// Normalize variables
		this.inputTitleAS = StringUtils.defaultString(this.inputTitleAS, ImporterBase.driAnnSet);
		this.inputTitleAStype = StringUtils.defaultIfBlank(this.inputTitleAStype, ImporterBase.titleAnnType);

		// Get title annotations
		List<Annotation> refAnnotations = gate.Utils.inDocumentOrder(this.originalDocument.getAnnotations(this.inputTitleAS).get(this.inputTitleAStype));
		if(!CollectionUtils.isEmpty(refAnnotations)) {

			Annotation titleAnn = refAnnotations.get(0);
			titleAnn.setFeatures((titleAnn.getFeatures() == null) ? Factory.newFeatureMap() : titleAnn.getFeatures());

			Optional<String> titleText = GateUtil.getAnnotationText(titleAnn, this.originalDocument);

			if(titleText.isPresent()) {
				
				BiblioEntryParser bep = new BiblioEntryParser();
				bep.setUseBibsonomy("true");
				bep.setUseGoogleScholar("true");
				bep.setDocument(this.originalDocument);
				
				// Add Bibsonomy parsing result as title annotation features 
				try {
					if(this.useBibsonomy != null && this.useBibsonomy.equalsIgnoreCase("true")) {
						Integer bibsonomyResult = bep.bibsonomyParsing.apply(titleAnn);
						logger.info("Retrieved " + ((bibsonomyResult != null) ? bibsonomyResult : "0") + " metadata item(s) from Bibsonomy.");
					}
				}
				catch (Exception e) {
					Util.notifyException("Parsing header (title) by Bibsonomy", e, logger);
				}

				// Add Google Scholar parsing result as title annotation features
				try {
					if(this.useGoogleScholar != null && this.useGoogleScholar.equalsIgnoreCase("true")) {
						String searchText = GateUtil.getAnnotationText(titleAnn, this.originalDocument).orElse("");
						
						if(this.document != null) {
							List<Annotation> headerSentences = GateUtil.getAnnInDocOrder(this.document, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Sentence);
							if(headerSentences != null && headerSentences.size() > 0) {
								for(int i = headerSentences.size() - 1; i >= 0; i--) {
									String headerSentenceText = GateUtil.getAnnotationText(headerSentences.get(i), this.document).orElse(null);
									if(StringUtils.isNotEmpty(headerSentenceText)) {
										searchText += " " + headerSentenceText.trim();
									}
									
									if(searchText.length() > 250) {
										break;
									}
								}
							}
						}
						
						GoogleScholarResult result = BiblioEntryParser.googleScholarExpansion(null, searchText, "");
						BiblioEntryParser.populateFmGoogleScholar(titleAnn.getFeatures(), result);
						Integer googleScholarResult = bep.bibsonomyParsing.apply(titleAnn);
						logger.info("Retrieved " + ((googleScholarResult != null) ? googleScholarResult : "0") + " metadata item(s) from Google Scholar.");
					}
				}
				catch (Exception e) {
					Util.notifyException("Parsing header (title expanded) by Google Scholar", e, logger);
				}

				// Mining header sentences - annotate header document
				parseHeaderDoc(this.document, this.originalDocument, titleAnn);

				// Add author names and affiliations name, affiliations names, cities and states, emails as title annotation features
				List<Annotation> authorList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author);
				int authorCount = 1;
				for(Annotation author : authorList) {
					Optional<String> authorName = GateUtil.getAnnotationText(author, this.document);
					if(authorName.isPresent()) {
						titleAnn.getFeatures().put("R_author_" + authorCount, authorName.get());
						int i = 1;
						while(true){
							Optional<String> affiliation = GateUtil.getStringFeature(author, "affiliation_" + i);
							if(affiliation.isPresent()) {
								titleAnn.getFeatures().put("R_author_" + authorCount + "_aff_" + i, affiliation.get());
								i++;
							}
							else {
								break;
							}
						}
						authorCount++;
					}
				}

				List<Annotation> affiliationList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation);
				int affilCount = 1;
				for(Annotation affiliation : affiliationList) {
					Optional<String> affiliationName = GateUtil.getAnnotationText(affiliation, this.document);
					if(affiliationName.isPresent()) {
						titleAnn.getFeatures().put("R_affilName_" + affilCount,  affiliationName);
						titleAnn.getFeatures().put("R_affilCity_" + affilCount,  GateUtil.getStringFeature(affiliation, "city").orElse("NO_CITY"));
						titleAnn.getFeatures().put("R_affilState_" + affilCount,  GateUtil.getStringFeature(affiliation, "state").orElse("NO_STATE"));
						affilCount++;
					}
				}

				List<Annotation> emailList = GateUtil.getAnnInDocOrder(this.document, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail);
				int emailCount = 1;
				for(Annotation email : emailList) {
					Optional<String> emailName = GateUtil.getAnnotationText(email, this.document);
					if(emailName.isPresent()) {
						titleAnn.getFeatures().put("R_email_" + emailCount,  emailName.get());
						emailCount++;
					}
				}

			}
		}
	}


	/**
	 * Parse the header of a document
	 * 
	 * @param headerDoc The parsed header document
	 * @param originalDoc The original document
	 * @param titleAnnotation The title annotations of the original document to be enriched with features
	 */
	public static void parseHeaderDoc(gate.Document headerDoc, gate.Document originalDoc, Annotation titleAnnotation) {

		if(headerDoc != null) {

			String originalHeader = "";
			originalHeader += "-------------------------------------------";
			List<Annotation> headerSentenceList = gate.Utils.inDocumentOrder(headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).get(ImporterBase.headerDOC_Sentence));
			if(!CollectionUtils.isEmpty(headerSentenceList)) {
				for(Annotation headerSentence : headerSentenceList) {
					Optional<String> sentText = GateUtil.getAnnotationText(headerSentence, headerDoc);
					if(sentText.isPresent()) {
						originalHeader += " > " + sentText + "\n";
					}
				}
			}
			originalHeader += "-------------------------------------------";
			logger.debug(originalHeader);

			// ADDING Author annotations
			addPersonNames(headerDoc, originalDoc, titleAnnotation);

			// ADDING Affiliation annotations
			addOrganizationNames(headerDoc, originalDoc, titleAnnotation);

			// ADDING FEATS refa_ and refb_ to Author and Affiliation
			extractRefOfAuthorAndAff(headerDoc, originalDoc, titleAnnotation);

			// ADDING FEATS add city and state features to Affiliation
			extractOrganizationAddresses(headerDoc, originalDoc, titleAnnotation);

			// ADDING FEATS match author and affiliations
			matchPersonOrganization(headerDoc, originalDoc, titleAnnotation);

		}
	}

	/**
	 * Add the header annotations to the original document
	 * 
	 * @param headerDoc
	 * @param originalDoc
	 * @param titleAnnotation
	 */
	public static void addPersonNames(Document headerDoc, Document originalDoc, Annotation titleAnnotation) {

		// Sanitize parameters
		Set<String> authorList = new HashSet<String>();

		// Populate authorList with Bibsonomy and Google Scholar disambiguation results
		if(titleAnnotation != null) {
			// Retrieve authors - first from google scholar, then bibsonomy
			Optional<String> firstAuthorName_GS = GateUtil.getStringFeature(titleAnnotation, "goos_authorName_1");
			Optional<String> firstAuthorName_BIB = GateUtil.getStringFeature(titleAnnotation, "b_authorName_1");
			if(firstAuthorName_GS.isPresent()) {
				authorList.add(firstAuthorName_GS.get());
				logger.debug("GS auth: " + firstAuthorName_GS.get());
				int counter = 2;
				while(true) {
					Optional<String> authorName_GS = GateUtil.getStringFeature(titleAnnotation, "goos_authorName_" + counter);
					counter++;
					if(authorName_GS.isPresent()) {
						authorList.add(authorName_GS.get());
						logger.debug("GS auth: " + authorName_GS.get());
					}
					else {
						break;
					}
				}
			}
			else if(firstAuthorName_BIB.isPresent()) {
				authorList.add(firstAuthorName_BIB.get());
				logger.debug("GS auth: " + firstAuthorName_BIB.get());
				int counter = 2;
				while(true) {
					Optional<String> authorName_BIB = GateUtil.getStringFeature(titleAnnotation, "b_authorName_" + counter);
					counter++;
					if(authorName_BIB.isPresent()) {
						authorList.add(authorName_BIB.get());
						logger.debug("GS auth: " + authorName_BIB.get());
					}
					else {
						break;
					}
				}
			}
		}

		// *****************************************************************************
		// 2) Annotate person names
		List<Annotation> headerSentenceList = gate.Utils.inDocumentOrder(headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).get(ImporterBase.headerDOC_Sentence));

		Set<String> sanitizedAuthorNamePartsList = new HashSet<String>();
		for(String author : authorList) {
			String autorNameSanitized = author.replace(",", " ").replace(".", " ").replace(";", " ").replace(":", " ").trim();
			String[] authorNameSanitizedSplit = autorNameSanitized.split(" ");
			for(String splitElem : authorNameSanitizedSplit) {
				if(StringUtils.isNotBlank(splitElem)) {
					sanitizedAuthorNamePartsList.add(splitElem.toLowerCase());
				}
			}
		}

		for(Annotation headerSentence : headerSentenceList) {
			Optional<String> sentText = GateUtil.getAnnotationText(headerSentence, headerDoc);

			if(sentText.isPresent()) {

				// A) Annotate header tokens equals to author name token by feature personElem equal to true
				List<Annotation> headerSentenceTokenList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, headerSentence);
				headerSentenceTokenList.stream().forEach((tokenAnn) -> {
					Optional<String> tokenText = GateUtil.getAnnotationText(tokenAnn, headerDoc);
					if(tokenText.isPresent() && sanitizedAuthorNamePartsList.contains(tokenText.get().toLowerCase())) {
						tokenAnn.setFeatures((tokenAnn.getFeatures() != null) ? tokenAnn.getFeatures() : Factory.newFeatureMap());
						tokenAnn.getFeatures().put("personElem", "true");
					}
				});

				// B) Identify Author annotations

				// B.1 - PATTERN: first_name / last_name OR first_name / part OR part / last_name
				headerSentenceTokenList.stream().forEach((token1) -> {
					if(token1 != null && headerSentenceTokenList.size() > (headerSentenceTokenList.indexOf(token1) + 1) ) {
						Annotation token2 = headerSentenceTokenList.get(headerSentenceTokenList.indexOf(token1) + 1);
						String token1MinorType = GateUtil.getStringFeature(token1, "minorType").orElse(null);
						String token1PersonElem = GateUtil.getStringFeature(token1, "personElem").orElse(null);
						String token2MinorType = GateUtil.getStringFeature(token2, "minorType").orElse(null);
						String token2PersonElem = GateUtil.getStringFeature(token2, "personElem").orElse(null);

						if(Util.strCompareTrimmedCI(token1MinorType, "first_name") && Util.strCompareTrimmedCI(token2MinorType, "last_name") ||
								Util.strCompareTrimmedCI(token1PersonElem, "true") && Util.strCompareTrimmedCI(token2MinorType, "last_name") ||
								Util.strCompareTrimmedCI(token1MinorType, "first_name") && Util.strCompareTrimmedCI(token2PersonElem, "true") ) {
							try {
								headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).add(token1.getStartNode().getOffset(), token2.getEndNode().getOffset(), ImporterBase.headerDOC_Author, Factory.newFeatureMap());
							} catch (Exception e) {
								Util.notifyException("Creating header author annotations", e, logger);
							}
						}
					}
				});

				// B.2 - PATTERN: mayorType = AUTHOR && minorType = full && not intersecting email or other authors
				List<Annotation> headerLookupList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Lookup, headerSentence);
				headerLookupList.stream().forEach((lookup) -> {
					String lookupMajorType = GateUtil.getStringFeature(lookup, "mayorType").orElse(null);
					String lookupMinorType = GateUtil.getStringFeature(lookup, "minorType").orElse(null);

					if(Util.strCompareTrimmedCI(lookupMajorType, "AUTHOR") && Util.strCompareTrimmedCI(lookupMinorType, "full")) {
						List<Annotation> intersectingEmail = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, lookup);
						List<Annotation> intersectingAuthor = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, lookup);

						if(CollectionUtils.isEmpty(intersectingEmail) && CollectionUtils.isEmpty(intersectingAuthor)) {
							try {
								headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).add(lookup.getStartNode().getOffset(), lookup.getEndNode().getOffset(), ImporterBase.headerDOC_Author, Factory.newFeatureMap());
							} catch (Exception e) {
								Util.notifyException("Creating header author annotations", e, logger);
							}
						}
					}
				});

				// B.3 - If there are two author annotations next one the other merge them
				List<Annotation> headerAuthorList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, headerSentence);
				Set<Annotation> toDel = new HashSet<Annotation>();
				headerAuthorList.stream().forEach((author1) -> {
					if(author1 != null && headerAuthorList.size() > (headerAuthorList.indexOf(author1) + 1) ) {
						Annotation author2 = headerAuthorList.get(headerAuthorList.indexOf(author1) + 1);
						if(author2 != null && author2.getStartNode().getOffset() >= author1.getEndNode().getOffset()) {
							Optional<String> textAuthor1 = GateUtil.getAnnotationText(author1, headerDoc);
							Optional<String> textAuthor2 = GateUtil.getAnnotationText(author2, headerDoc);
							Optional<String> textBetween = GateUtil.getDocumentText(headerDoc, author1.getEndNode().getOffset(), author2.getStartNode().getOffset());
							if(textAuthor1.isPresent() && textAuthor2.isPresent() && textBetween.isPresent()) {
								String trimmedText = textBetween.get().trim();
								if(!trimmedText.contains(",") && !trimmedText.contains(";") &&
										!trimmedText.contains(":") && !trimmedText.contains("-") && 
										!trimmedText.trim().equals(".") && !trimmedText.toLowerCase().contains("and") &&
										textAuthor1.get().split(" ").length <= 1 && textAuthor2.get().split(" ").length <= 1) {
									// Merge annotations
									try {
										headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).add(author1.getStartNode().getOffset(), author2.getEndNode().getOffset(), ImporterBase.headerDOC_Author, Factory.newFeatureMap());
										toDel.add(author1);
										toDel.add(author2);
									} catch (Exception e) {
										Util.notifyException("Merging header author annotations", e, logger);
									}
								}
							}
						}
					}
				});

				toDel.stream().forEach( (ann) -> { headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).remove(ann); } );
			}
		}
	}

	// Annotate as Affiliation, the names of the organizations in the header
	public static void addOrganizationNames(Document headerDoc, Document originalDoc, Annotation titleAnnotation) {

		List<Annotation> headerSentenceList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Sentence);

		for(Annotation headerSentence : headerSentenceList) {
			Optional<String> sentText = GateUtil.getAnnotationText(headerSentence, headerDoc);

			if(sentText.isPresent()) {
				List<Annotation> intersectingLookup = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Lookup, headerSentence);

				intersectingLookup.stream().forEach((lookup) -> {
					String lookupMajorType = GateUtil.getStringFeature(lookup, "mayorType").orElse(null);
					String lookupMinorType = GateUtil.getStringFeature(lookup, "minorType").orElse(null);

					List<Annotation> intersectingEmail = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, lookup);

					if(Util.strCompareTrimmedCI(lookupMajorType, "COMMON") && Util.strCompareTrimmedCI(lookupMinorType, "affiliation") &&
							CollectionUtils.isEmpty(intersectingEmail)) {
						Long startOffserNewOrg = lookup.getStartNode().getOffset();
						Long endOffserNewOrg = lookup.getEndNode().getOffset();

						List<Annotation> preceedingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, headerSentence.getStartNode().getOffset(), lookup.getStartNode().getOffset());
						List<Annotation> followingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, lookup.getEndNode().getOffset(), headerSentence.getEndNode().getOffset());

						// Add to the affiliation all the preceding tokens of the same line that are not:
						// number, symbol, punctuation, author
						for(int i = preceedingTokens.size() -1; i >= 0; i--) {
							Annotation preceedingToken = preceedingTokens.get(i);
							if(preceedingToken != null) {
								Optional<String> kindValue = GateUtil.getStringFeature(preceedingToken, "kind");
								List<Annotation> intersectingAuthor = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, preceedingToken);
								if(intersectingAuthor.size() == 0 && 
										kindValue.isPresent() && !kindValue.get().equals("number") && !kindValue.get().equals("punctuation") && !kindValue.get().equals("symbol")) {
									startOffserNewOrg = preceedingToken.getStartNode().getOffset();
								}
							}
						}

						// Add to the affiliation all the following tokens of the same line that are not:
						// number, symbol, punctuation, author
						for(int i = 0; i < followingTokens.size(); i++) {
							Annotation followingToken = followingTokens.get(i);
							if(followingToken != null) {
								Optional<String> kindValue = GateUtil.getStringFeature(followingToken, "kind");
								List<Annotation> intersectingAuthor = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, followingToken);
								if(intersectingAuthor.size() == 0 && 
										kindValue.isPresent() && !kindValue.get().equals("number") && !kindValue.get().equals("punctuation") && !kindValue.get().equals("symbol")) {
									endOffserNewOrg = followingToken.getEndNode().getOffset();
								}
							}
						}

						// Not intersecting email, person and organization
						List<Annotation> intersectingAffEmail = GateUtil.getAnnInDocOrderIntersectOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, startOffserNewOrg, endOffserNewOrg);
						List<Annotation> intersectingAffAuthor = GateUtil.getAnnInDocOrderIntersectOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, startOffserNewOrg, endOffserNewOrg);
						List<Annotation> intersectingAffAffiliation = GateUtil.getAnnInDocOrderIntersectOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, startOffserNewOrg, endOffserNewOrg);

						if(CollectionUtils.isEmpty(intersectingAffEmail) && CollectionUtils.isEmpty(intersectingAffAuthor) && CollectionUtils.isEmpty(intersectingAffAffiliation)) {
							try {
								headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).add(startOffserNewOrg, endOffserNewOrg, ImporterBase.headerDOC_Affiliation, Factory.newFeatureMap());
							} catch (Exception e) {
								Util.notifyException("Creating affiliation annotations", e, logger);
							}
						}

					}
				});


				// Merge subsequent affiliations if no alphanumeric text is not contained in between
				List<Annotation> headerAffiliationList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation, headerSentence);
				Set<Annotation> toDel = new HashSet<Annotation>();
				headerAffiliationList.stream().forEach((affil1) -> {
					if(affil1 != null && headerAffiliationList.size() > (headerAffiliationList.indexOf(affil1) + 1) ) {
						Annotation affil2 = headerAffiliationList.get(headerAffiliationList.indexOf(affil1) + 1);
						if(affil2 != null && affil2.getStartNode().getOffset() >= affil1.getEndNode().getOffset()) {
							Optional<String> textBetween = GateUtil.getDocumentText(headerDoc, affil1.getEndNode().getOffset(), affil2.getStartNode().getOffset());
							if(textBetween.isPresent()) {
								boolean isThereAlphanumeric = false;
								for(int y = 0; y < textBetween.get().length(); y++) {
									if(Character.isAlphabetic(textBetween.get().charAt(y)) || Character.isDigit(textBetween.get().charAt(y))) {
										isThereAlphanumeric = true;
									}
								}

								if(!isThereAlphanumeric) {
									// Merge annotations
									try {
										headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).add(affil1.getStartNode().getOffset(), affil2.getEndNode().getOffset(), ImporterBase.headerDOC_Author, Factory.newFeatureMap());
										toDel.add(affil1);
										toDel.add(affil2);
									} catch (Exception e) {
										Util.notifyException("Merging header author annotations", e, logger);
									}
								}
							}
						}
					}
				});

				toDel.stream().forEach( (ann) -> { headerDoc.getAnnotations(ImporterBase.headerDOC_AnnSet).remove(ann); } );

			}
		}

	}

	// Mark references
	private static void extractRefOfAuthorAndAff(Document headerDoc, Document originalDoc, Annotation titleAnnotation) {

		List<Annotation> headerSentenceList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Sentence);

		for(Annotation headerSentence : headerSentenceList) {

			List<Annotation> authorSentenceList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, headerSentence);
			List<Annotation> affilSentenceList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation, headerSentence);

			// Author
			authorSentenceList.stream().forEach((author) -> {
				Optional<String> personText = GateUtil.getAnnotationText(author, headerDoc);

				if(personText.isPresent()) {
					List<Annotation> precedingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, headerSentence.getStartNode().getOffset(), author.getStartNode().getOffset());
					List<Annotation> followingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, author.getEndNode().getOffset(), headerSentence.getEndNode().getOffset());

					// Check following reference
					for(int k = 0; k < followingTokens.size(); k++) {
						Annotation fToken = followingTokens.get(k);
						fToken.setFeatures((fToken.getFeatures() != null) ? fToken.getFeatures() : Factory.newFeatureMap());

						if(isReference(fToken)) {
							fToken.getFeatures().put("refa_0", GateUtil.getStringFeature(fToken, "string").get().trim());

							// Check for second reference
							if((k+2) < followingTokens.size() && 
									GateUtil.getStringFeature(followingTokens.get(k+1), "string").isPresent() &&
									GateUtil.getStringFeature(followingTokens.get(k+1), "string").get().equals(",")) {

								if(isReference(followingTokens.get(k+2))) {
									fToken.getFeatures().put("refa_1", GateUtil.getStringFeature(followingTokens.get(k+2), "string").get().trim());
								}
							}
						}
					}

					// Check previous reference
					for(int k = precedingTokens.size() - 1; k >= 0; k--) {
						Annotation pToken = precedingTokens.get(k);
						pToken.setFeatures((pToken.getFeatures() != null) ? pToken.getFeatures() : Factory.newFeatureMap());

						if(isReference(pToken)) {
							pToken.getFeatures().put("refb_0", GateUtil.getStringFeature(pToken, "string").get().trim());

							// Check for second reference
							if((k-2) >= 0 && 
									GateUtil.getStringFeature(precedingTokens.get(k-1), "string").isPresent() &&
									GateUtil.getStringFeature(precedingTokens.get(k-1), "string").get().equals(",")) {

								if(isReference(precedingTokens.get(k-2))) {
									pToken.getFeatures().put("refb_1", GateUtil.getStringFeature(precedingTokens.get(k-2), "string").get().trim());
								}
							}
						}
					}
				}
			});

			// Affiliation
			affilSentenceList.stream().forEach((affilAnn) -> {
				List<Annotation> precedingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, headerSentence.getStartNode().getOffset(), affilAnn.getStartNode().getOffset());
				List<Annotation> followingTokens = GateUtil.getAnnInDocOrderContainedOffset(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Token, affilAnn.getEndNode().getOffset(), headerSentence.getEndNode().getOffset());

				// Check following reference
				for(int k = 0; k < followingTokens.size(); k++) {
					Annotation fToken = followingTokens.get(k);
					fToken.setFeatures((fToken.getFeatures() != null) ? fToken.getFeatures() : Factory.newFeatureMap());

					if(isReference(fToken)) {
						fToken.getFeatures().put("refa_0", GateUtil.getStringFeature(fToken, "string").get().trim());

						// Check for second reference
						if((k+2) < followingTokens.size() && 
								GateUtil.getStringFeature(followingTokens.get(k+1), "string").isPresent() &&
								GateUtil.getStringFeature(followingTokens.get(k+1), "string").get().equals(",")) {

							if(isReference(followingTokens.get(k+2))) {
								fToken.getFeatures().put("refa_1", GateUtil.getStringFeature(followingTokens.get(k+2), "string").get().trim());
							}
						}
					}
				}

				// Check previous reference
				for(int k = precedingTokens.size() - 1; k >= 0; k--) {
					Annotation pToken = precedingTokens.get(k);
					pToken.setFeatures((pToken.getFeatures() != null) ? pToken.getFeatures() : Factory.newFeatureMap());

					if(isReference(pToken)) {
						pToken.getFeatures().put("refb_0", GateUtil.getStringFeature(pToken, "string").get().trim());

						// Check for second reference
						if((k-2) >= 0 && 
								GateUtil.getStringFeature(precedingTokens.get(k-1), "string").isPresent() &&
								GateUtil.getStringFeature(precedingTokens.get(k-1), "string").get().equals(",")) {

							if(isReference(precedingTokens.get(k-2))) {
								pToken.getFeatures().put("refb_1", GateUtil.getStringFeature(precedingTokens.get(k-2), "string").get().trim());
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Check if the token annotation can be a ref symbol
	 * 
	 * @param tokenAnn
	 * @return
	 */
	private static boolean isReference(Annotation tokenAnn) {
		boolean result = false;

		String kindValue = GateUtil.getStringFeature(tokenAnn, "kind").orElse(null);
		String lengthValue = GateUtil.getStringFeature(tokenAnn, "length").orElse(null);
		String stringValue = GateUtil.getStringFeature(tokenAnn, "string").orElse(null);

		if( (Util.strCompare(kindValue, "number") && (Util.strCompare(lengthValue, "1") || Util.strCompare(lengthValue, "2"))) ||
				(Util.strCompare(kindValue, "punctuation") && Util.strCompare(lengthValue, "1") && (!Util.strCompare(stringValue, ",") && !Util.strCompare(stringValue, ";"))) ||
				(Util.strCompare(kindValue, "symbol") && Util.strCompare(lengthValue, "1")) ||
				(Util.strCompare(kindValue, "word") && Util.strCompare(lengthValue, "1")) ) {
			result = true;
		}

		return result;
	}


	public static void extractOrganizationAddresses(Document headerDoc, Document originalDoc, Annotation titleAnnotation) {
		List<Annotation> lookupList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Lookup);

		lookupList.stream().forEach((lookup) -> {
			List<Annotation> intersectingEmailAddrs = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_JAPEemail, lookup);
			List<Annotation> intersectingAuthor = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author, lookup);
			List<Annotation> intersectingAffiliation = GateUtil.getAnnInDocOrderIntersectAnn(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation, lookup);

			Optional<String> lookupText = GateUtil.getAnnotationText(lookup, headerDoc);

			if(lookupText.isPresent() && CollectionUtils.isEmpty(intersectingEmailAddrs) && CollectionUtils.isEmpty(intersectingAuthor) && intersectingAffiliation.size() == 1) {
				String majorType = GateUtil.getStringFeature(lookup, "majorType").orElse(null);
				String minorType = GateUtil.getStringFeature(lookup, "minorType").orElse(null);

				intersectingAffiliation.get(0).setFeatures((intersectingAffiliation.get(0).getFeatures() != null) ? intersectingAffiliation.get(0).getFeatures() : Factory.newFeatureMap());

				if(Util.strCompare(majorType, "location")) {
					if(Util.strCompare(minorType, "city")) {
						intersectingAffiliation.get(0).getFeatures().put("city", lookupText.get());
					}
					else if(Util.strCompare(minorType, "country")) {
						intersectingAffiliation.get(0).getFeatures().put("state", lookupText.get());
					}
					else if(Util.strCompare(minorType, "province")) {
						intersectingAffiliation.get(0).getFeatures().put("state", lookupText.get());
					}
				}

				if(majorType.equals("DBPEDIA_SIMPLE")) {
					if(minorType.equals("city")) {
						intersectingAffiliation.get(0).getFeatures().put("city", lookupText.get());
					}
					else if(minorType.equals("state")) {
						intersectingAffiliation.get(0).getFeatures().put("state", lookupText.get());
					}
					else if(minorType.equals("region")) {
						intersectingAffiliation.get(0).getFeatures().put("state", lookupText.get());
					}
				}
			}
		});

	}

	public static void matchPersonOrganization(Document headerDoc, Document originalDoc, Annotation titleAnnotation) {

		Map<String, List<Annotation>> refAuthorMAP = new HashMap<String, List<Annotation>>();
		Map<String, List<Annotation>> refAffiliationMAP = new HashMap<String, List<Annotation>>();

		// Populate author reference map
		List<Annotation> authorAnnotationList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author);
		Integer refBefore_auth = 0;
		Map<String, List<Annotation>> authorAnn_BEF = new HashMap<String, List<Annotation>>();
		Integer refAfter_auth = 0;
		Map<String, List<Annotation>> authorAnn_AFT = new HashMap<String, List<Annotation>>();

		for(Annotation author : authorAnnotationList) {
			Optional<String> refBefore_0 = GateUtil.getStringFeature(author, "refb_0");
			Optional<String> refBefore_1 = GateUtil.getStringFeature(author, "refb_1");
			Optional<String> refAfter_0 = GateUtil.getStringFeature(author, "refa_0");
			Optional<String> refAfter_1 = GateUtil.getStringFeature(author, "refa_1");

			if(refBefore_0.isPresent()) {
				refBefore_auth++;
				List<Annotation> listC = authorAnn_BEF.get(refBefore_0.get().trim());
				listC = (listC != null) ? listC : new ArrayList<Annotation>();
				listC.add(author);
				authorAnn_BEF.put(refBefore_0.get().trim(), listC);

				if(refBefore_1.isPresent()) {
					List<Annotation> listD = authorAnn_BEF.get(refBefore_1.get().trim());
					listD = (listD != null) ? listD : new ArrayList<Annotation>();
					listD.add(author);
					authorAnn_BEF.put(refBefore_1.get().trim(), listD);
				}
			}

			if(refAfter_0.isPresent()) {
				refAfter_auth++;
				List<Annotation> listC = authorAnn_AFT.get(refAfter_0.get().trim());
				listC = (listC != null) ? listC : new ArrayList<Annotation>();
				listC.add(author);
				authorAnn_AFT.put(refAfter_0.get().trim(), listC);

				if(refAfter_1.isPresent()) {
					List<Annotation> listD = authorAnn_AFT.get(refAfter_1.get().trim());
					listD = (listD != null) ? listD : new ArrayList<Annotation>();
					listD.add(author);
					authorAnn_AFT.put(refAfter_1.get().trim(), listD);
				}
			}
		}
		refAuthorMAP = (refAfter_auth >= refBefore_auth) ? authorAnn_AFT : authorAnn_BEF;

		// Populate affiliation reference map
		List<Annotation> affiliationAnnotationList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation);
		Integer refBefore_affil = 0;
		Map<String, List<Annotation>> affiliationAnn_BEF = new HashMap<String, List<Annotation>>();
		Integer refAfter_affil = 0;
		Map<String, List<Annotation>> affiliationAnn_AFT = new HashMap<String, List<Annotation>>();

		for(Annotation affiliation : affiliationAnnotationList) {
			Optional<String> refBefore_0 = GateUtil.getStringFeature(affiliation, "refb_0");
			Optional<String> refBefore_1 = GateUtil.getStringFeature(affiliation, "refb_1");
			Optional<String> refAfter_0 = GateUtil.getStringFeature(affiliation, "refa_0");
			Optional<String> refAfter_1 = GateUtil.getStringFeature(affiliation, "refa_1");

			if(refBefore_0.isPresent()) {
				refBefore_affil++;
				List<Annotation> listC = affiliationAnn_BEF.get(refBefore_0.get().trim());
				listC = (listC != null) ? listC : new ArrayList<Annotation>();
				listC.add(affiliation);
				affiliationAnn_BEF.put(refBefore_0.get().trim(), listC);

				if(refBefore_1.isPresent()) {
					List<Annotation> listD = affiliationAnn_BEF.get(refBefore_1.get().trim());
					listD = (listD != null) ? listD : new ArrayList<Annotation>();
					listD.add(affiliation);
					affiliationAnn_BEF.put(refBefore_1.get().trim(), listD);
				}
			}

			if(refAfter_0.isPresent()) {
				refAfter_affil++;
				List<Annotation> listC = affiliationAnn_AFT.get(refAfter_0.get().trim());
				listC = (listC != null) ? listC : new ArrayList<Annotation>();
				listC.add(affiliation);
				affiliationAnn_AFT.put(refAfter_0.get().trim(), listC);

				if(refAfter_1.isPresent()) {
					List<Annotation> listD = affiliationAnn_AFT.get(refAfter_1.get().trim());
					listD = (listD != null) ? listD : new ArrayList<Annotation>();
					listD.add(affiliation);
					affiliationAnn_AFT.put(refAfter_1.get().trim(), listD);
				}
			}
		}
		refAffiliationMAP = (refAfter_affil >= refBefore_affil) ? affiliationAnn_AFT : affiliationAnn_BEF;

		// Match author and affiliation
		boolean matched = false;
		for(Entry<String, List<Annotation>> mapEntry : refAffiliationMAP.entrySet()) {
			if(mapEntry != null && mapEntry.getKey() != null && mapEntry.getValue() != null && mapEntry.getValue().size() > 0) {
				String referenceOfOrg = mapEntry.getKey();
				List<Annotation> referencedAffiliations = mapEntry.getValue();
				try {
					if(refAuthorMAP.containsKey(referenceOfOrg)) {
						List<Annotation> referencedPersons = refAuthorMAP.get(referenceOfOrg);
						if(referencedPersons != null && referencedPersons.size() > 0) {
							for(Annotation person : referencedPersons) {
								if(person != null) {
									int affNum = 1;
									for(Annotation referencedAffiliation : referencedAffiliations) {
										person.setFeatures((person.getFeatures() != null) ? person.getFeatures() : Factory.newFeatureMap());
										person.getFeatures().put("affiliation_" + affNum++, headerDoc.getContent().getContent(referencedAffiliation.getStartNode().getOffset(), referencedAffiliation.getEndNode().getOffset()).toString());
										matched = true;
									}
								}
							}
						}
					}
				} catch (Exception e) {
					Util.notifyException("Adding author affiliaiton match", e, logger);
				}
			}
		}

		// No match by reference and only one affiliation
		if(!matched) {
			List<Annotation> authorList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Author);
			List<Annotation> affiliationList = GateUtil.getAnnInDocOrder(headerDoc, ImporterBase.headerDOC_AnnSet, ImporterBase.headerDOC_Affiliation);

			if(affiliationList.size() == 1 && affiliationList.size() >= 1) {
				try {
					Annotation uniqueAffiliation = affiliationList.get(0);
					for(Annotation person : authorList) {
						int affNum = 1;
						if(person != null) {
							person.setFeatures((person.getFeatures() != null) ? person.getFeatures() : Factory.newFeatureMap());
							person.getFeatures().put("affiliation_" + affNum++, headerDoc.getContent().getContent(uniqueAffiliation.getStartNode().getOffset(), uniqueAffiliation.getEndNode().getOffset()).toString());
							matched = true;
						}
					}

				} catch (Exception e) {
					Util.notifyException("Adding author affiliaiton match", e, logger);
				}
			}
		}

	}
	
	/**
	 * Delete all the annotations created by this module
	 */
	private Consumer<Annotation> annReset = (ann) -> {
		try {
			FeatureMap fm = ann.getFeatures();
			if(fm != null) {
				Set<Object> featNameToRem = new HashSet<Object>();
				for(Entry<Object, Object> feature : fm.entrySet()) {
					if(feature.getKey() != null && feature.getKey() instanceof String && 
							( ((String) feature.getKey()).startsWith("b_") || ((String) feature.getKey()).startsWith("goos_") || 
									((String) feature.getKey()).startsWith("f_") || ((String) feature.getKey()).startsWith("x_") || ((String) feature.getKey()).startsWith("R_") ) 
							) {
						featNameToRem.add(feature.getKey());
					}
				}
				featNameToRem.stream().forEach((feat) -> fm.remove(feat) );
			}
		}
		catch (Exception e) {
			Util.notifyException("Resetting bibliographic entry parsing annotations", e, logger);
		}
	};

	@Override
	public boolean resetAnnotations() {
		if(!this.annotationReset) {
			// Normalize variables
			this.inputTitleAS = StringUtils.defaultString(this.inputTitleAS, ImporterBase.driAnnSet);
			this.inputTitleAStype = StringUtils.defaultIfBlank(this.inputTitleAStype, ImporterBase.titleAnnType);

			List<Annotation> refAnnotations = gate.Utils.inDocumentOrder(this.originalDocument.getAnnotations(this.inputTitleAS).get(this.inputTitleAStype));
			if(!CollectionUtils.isEmpty(refAnnotations)) {
				annReset.accept(refAnnotations.get(0));
			}
			
			this.annotationReset = true;
		}
		
		return true;
	}

}

