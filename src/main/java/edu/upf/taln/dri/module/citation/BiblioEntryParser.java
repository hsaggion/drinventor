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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bibsonomy.model.PersonName;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.connector.bibsonomy.BibsonomyStandaloneConn;
import edu.upf.taln.dri.common.connector.bibsonomy.model.BibTexWrap;
import edu.upf.taln.dri.common.connector.crossref.CrossRefConn;
import edu.upf.taln.dri.common.connector.crossref.model.CrossRefResult;
import edu.upf.taln.dri.common.connector.freecite.FreeCiteConn;
import edu.upf.taln.dri.common.connector.freecite.model.FreeCiteResult;
import edu.upf.taln.dri.common.connector.google.scholar.GoogleScholarConn;
import edu.upf.taln.dri.common.connector.google.scholar.model.GoogleScholarResult;
import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.util.PropertyManager;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
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

/**
 * Parse the contents of bibliographic entries by querying Bibsonomy, FreeCite, CrossRef and GoogleScholar
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Bibliographic Entries Parser")
public class BiblioEntryParser extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static Logger logger = Logger.getLogger(BiblioEntryParser.class);	

	private static String bibUserId = PropertyManager.getProperty("connector.bibsonomy.userid");
	private static String bibAPIkey = PropertyManager.getProperty("connector.bibsonomy.apykey");

	private String inputBiblioEntryAS;
	private String inputBiblioEntryAStype;

	private String useGoogleScholar;
	private String useBibsonomy;
	private String useFreeCite;
	private String useCrossRef;


	public String getInputBiblioEntryAS() {
		return inputBiblioEntryAS;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set for Bibliographic Entries")
	public void setInputBiblioEntryAS(String inputCitationAS) {
		this.inputBiblioEntryAS = inputCitationAS;
	}

	public String getInputBiblioEntryAStype() {
		return inputBiblioEntryAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "ref", comment = "The name of the input annotation type for Bibliographic Entries")
	public void setInputBiblioEntryAStype(String inputCitationAStype) {
		this.inputBiblioEntryAStype = inputCitationAStype;
	}

	public String getUseGoogleScholar() {
		return useGoogleScholar;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set true to parse biblio entries by Google Scholar")
	public void setUseGoogleScholar(String useGoogleScholar) {
		this.useGoogleScholar = useGoogleScholar;
	}

	public String getUseBibsonomy() {
		return useBibsonomy;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set true to parse biblio entries by Bibsonomy")
	public void setUseBibsonomy(String useBibsonomy) {
		this.useBibsonomy = useBibsonomy;
	}

	public String getUseFreeCite() {
		return useFreeCite;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set true to parse biblio entries by FreeCite")
	public void setUseFreeCite(String useFreeCite) {
		this.useFreeCite = useFreeCite;
	}

	public String getUseCrossRef() {
		return useCrossRef;
	}

	@RunTime
	@CreoleParameter(defaultValue = "true", comment = "Set true to parse biblio entries by CrossRef")
	public void setUseCrossRef(String useCrossRef) {
		this.useCrossRef = useCrossRef;
	}

	private Function<Annotation, String> textOfAnnotation = (ann) -> {
		String annText = null;
		try {
			annText = this.getDocument().getContent().getContent(ann.getStartNode().getOffset(), ann.getEndNode().getOffset()).toString();
		}
		catch (Exception e) {
			/* Do nothing */
		}
		return annText;
	};

	public void execute() throws ExecutionException {
		this.annotationReset = false;

		// Get the document to process
		gate.Document doc = getDocument();

		// Check variables
		this.inputBiblioEntryAS = StringUtils.defaultIfBlank(this.inputBiblioEntryAS, ImporterBase.driAnnSet);
		this.inputBiblioEntryAStype = StringUtils.defaultIfBlank(this.inputBiblioEntryAStype, ImporterBase.bibEntryAnnType);

		AnnotationSet refAnnotationSet = doc.getAnnotations(this.inputBiblioEntryAS);

		List<Annotation> bibEntryAnnotations = gate.Utils.inDocumentOrder(refAnnotationSet.get(this.inputBiblioEntryAStype));

		bibEntryAnnotations.stream().forEach(parseCitation);
	}
	
	/*** FreeCite ***/
	public Function<Annotation, Integer> freeCiteParsing = (ann) -> {
		try {
			String annText = textOfAnnotation.apply(ann);
			if(StringUtils.equalsIgnoreCase(this.useFreeCite, "true") && StringUtils.isNotBlank(annText)) {
				logger.info("FreeCite parsing: '" + normalizeText(annText) + "'");
				freeCiteAnalysis(ann, annText);
				
				return countFeatsStartingWith(ann, "f_");
			}
			else {
				logger.info("FreeCite parsing: SKIPPED");
			}
		}
		catch (Exception e) {
			Util.notifyException("FreeCite parsing", e, logger);
		}
		return 0;
	};

	/**
	 * Parse a bibliographic entry by FreeCite and store results as annotation features
	 * 
	 * @param ref Biblio entry annotation
	 * @param refTxt
	 */
	public void freeCiteAnalysis(Annotation ref, String refTxt) {
		if(ref.getFeatures() == null) {
			ref.setFeatures(Factory.newFeatureMap());
		}

		FeatureMap fm = ref.getFeatures();

		List<String> citations = new ArrayList<String>();
		citations.add(normalizeText(refTxt));

		List<FreeCiteResult> parsingResults = FreeCiteConn.parseCitations(citations, 15);
		logger.debug("FreeCite parsed entry: " + (parsingResults.size() > 0));

		if(!CollectionUtils.isEmpty(parsingResults)) {
			parsingResults.stream().forEach((result) -> {
				if(result != null) {
					fm.put("f_title", StringUtils.defaultIfBlank(result.getTitle(), ""));
					Integer authCount = 1;
					for(String author : result.getAuthorNames()) {
						if(StringUtils.isNotBlank(author)) {
							fm.put("f_authorName_" + authCount, author);
							authCount++;
						}
					}
					fm.put("f_journal", StringUtils.defaultIfBlank(result.getJournal(), ""));
					fm.put("f_pages", StringUtils.defaultIfBlank(result.getPages(), ""));
					fm.put("f_year", StringUtils.defaultIfBlank(result.getYear(), ""));
				}
			});
		}
	}


	/*** CrossRef ***/
	public Function<Annotation, Integer> crossRefParsing = (ann) -> {
		try {
			String annText = textOfAnnotation.apply(ann);
			if(StringUtils.equalsIgnoreCase(this.useCrossRef, "true") && StringUtils.isNotBlank(annText)) {
				logger.info("CrossRef parsing: '" + normalizeText(annText) + "'");
				crossRefAnalysis(ann, annText);
				return countFeatsStartingWith(ann, "x_");
			}
			else {
				logger.info("CrossRef parsing: SKIPPED");
			}
		}
		catch (Exception e) {
			Util.notifyException("CorssRef parsing", e, logger);
		}
		return 0;
	};

	/**
	 * Parse a bibliographic entry by CrossRef and store results as annotation features
	 * 
	 * @param ref Biblio entry annotation
	 * @param refTxt
	 */
	public void crossRefAnalysis(Annotation ref, String refTxt) {
		if(ref.getFeatures() == null) {
			ref.setFeatures(Factory.newFeatureMap());
		}

		FeatureMap fm = ref.getFeatures();

		// Clean citation text
		if(refTxt.indexOf("]") != -1 && refTxt.indexOf("]") < 10) {
			refTxt = refTxt.substring(refTxt.indexOf("]") + 1);
		}
		else if(refTxt.indexOf(")") != -1 && refTxt.indexOf(")") < 10) {
			refTxt = refTxt.substring(refTxt.indexOf(")") + 1);
		}

		CrossRefResult parsingResults = CrossRefConn.parseCitations(normalizeText(refTxt), 15);
		logger.debug("CrossRef parsed entry: " + (parsingResults != null));

		if(parsingResults != null) {
			fm.put("x_score", StringUtils.defaultIfBlank(parsingResults.getScore(), ""));	
			fm.put("x_normalizedScore", StringUtils.defaultIfBlank(parsingResults.getNormalizedScore(), ""));	
			fm.put("x_title", StringUtils.defaultIfBlank(parsingResults.getTitle(), ""));	
			fm.put("x_fullCitation", StringUtils.defaultIfBlank(parsingResults.getFullCitation(), ""));	
			fm.put("x_year", StringUtils.defaultIfBlank(parsingResults.getYear(), ""));	
			fm.put("x_coins", StringUtils.defaultIfBlank(parsingResults.getNormalizedScore(), ""));
			fm.put("x_doi", StringUtils.defaultIfBlank(parsingResults.getDoi(), ""));	
		}

	}


	/*** Bibsonomy ***/
	public Function<Annotation, Integer> bibsonomyParsing = (ann) -> {
		try {
			String annText = textOfAnnotation.apply(ann);
			if(StringUtils.equalsIgnoreCase(this.useBibsonomy, "true") && StringUtils.isNotBlank(annText)) {
				logger.info("Bibsonomy parsing: '" + normalizeText(annText) + "'");
				bibsonomyAnalysis(ann, annText);
				return countFeatsStartingWith(ann, "b_");
			}
			else {
				logger.info("Bibsonomy parsing: SKIPPED");
			}
		}
		catch (Exception e) {
			Util.notifyException("Bibsonomy parsing", e, logger);
		}
		return 0;
	};


	/**
	 * Parse a bibliographic entry by Bibsonomy and store results as annotation features
	 * 
	 * @param ref Biblio entry annotation
	 * @param refTxt
	 */
	public void bibsonomyAnalysis(Annotation ref, String refTxt) {
		if(ref.getFeatures() == null) {
			ref.setFeatures(Factory.newFeatureMap());
		}

		FeatureMap fm = ref.getFeatures();

		String title_FC = (fm.containsKey("f_title") && fm.get("f_title") != null) ? (String) fm.get("f_title") : "";
		String firstAuthor_FC = (fm.containsKey("f_authorName_1") && fm.get("f_authorName_1") != null) ? (String) fm.get("f_authorName_1") : "";
		String title_XR = (fm.containsKey("x_title") && fm.get("x_title") != null) ? (String) fm.get("x_title") : "";
		String paperTitlePars = refTxt;

		BibTexWrap result = null;
		if(title_XR != null && !title_XR.equals("")) {
			result = bibsonomyExpansion(ref, title_XR, firstAuthor_FC);
			logger.debug("Bibsonomy search (CrossRef title) " + title_XR);
		}
		
		// If there are no results or Levenshtein distance of the retrieved title and the FreeCite title is > than the shorter of both,
		// query again Bibsonomy with FreeCite title text
		if(result == null || result.getTitle() == null || 
				(result != null && result.getTitle() != null && title_FC != null && 
				edu.upf.taln.dri.common.util.Util.computeLevenshteinDistance(result.getTitle(), title_FC) > 
				((result.getTitle().length() > title_FC.length()) ? result.getTitle().length() : title_FC.length()) ) ) {
			
			if(title_FC != null && !title_FC.equals("")) {
				result = bibsonomyExpansion(ref, title_FC, firstAuthor_FC);
				logger.debug("Bibsonomy search (FreeCite title) " + title_FC);
			}
			else if(StringUtils.isNotBlank(paperTitlePars)) {
				result = bibsonomyExpansion(ref, paperTitlePars, firstAuthor_FC);
				logger.debug("Bibsonomy search (original title) " + paperTitlePars);
			}
			else {
				logger.debug("Bibsonomy search: NO RESULTS");
			}
		}
		
		logger.debug("Bibsonomy parsed entry: " + (result != null));

		if(result != null) {
			populateFmBibtex(fm, result);
		}

	}

	/**
	 * Given the bibliographic entry GATE annotation, its text, a {@org.bibsonomy.model.BibTex BibTex} entry is retrieved from Bibsonomy.
	 * The disambiguationString provides a text to guess the right bibsonomy result if multiple matching entry are found for a bibliography entry.
	 * 
	 * @param ref
	 * @param refTxt
	 * @param disambiguationString
	 * @return
	 */
	public static BibTexWrap bibsonomyExpansion(Annotation ref, String refTxt, String disambiguationString) {
		BibTexWrap result = null;

		if(StringUtils.isNotBlank(refTxt)) {
			try {
				List<BibTexWrap> bibList = BibsonomyStandaloneConn.getBibTexWrap(normalizeText(refTxt), bibUserId, bibAPIkey, 15);

				if(bibList != null && bibList.size() > 0) {
					BibTexWrap resultEntry = bibList.get(0);

					// If more than one matching Bibsonomy record is returned for the bibliographic entry
					// choose the correct one by counting the number of matching words of the disambiguation string
					if(bibList.size() > 1) {

						logger.debug("Bibsonomy result size > 1: " + bibList.size());
						
						// Order by MLCS Distance with bibliographic entry text
						Map<Double, List<BibTexWrap>> distancesMap = new TreeMap<Double, List<BibTexWrap>>(); 
						int counrBibTex = 0;
						logger.debug("REF TEXT > " + refTxt);
						
						for(BibTexWrap bibTexItem : bibList) {
							Double titleDistance = 1d;
							
							counrBibTex++;
							logger.debug(counrBibTex + " > " + bibTexItem.getTitle());
							
							if(bibTexItem != null && bibTexItem.getTitle() != null) {
								String title = bibTexItem.getTitle();
								Double distWithRefText = edu.upf.taln.dri.common.util.Util.computeMetricLCS(title, refTxt);
								if(distWithRefText != null && distWithRefText >= 0) {
									titleDistance = distWithRefText;
								}
							}
							
							if(distancesMap.containsKey(titleDistance)) {
								distancesMap.get(titleDistance).add(bibTexItem);
							}
							else {
								List<BibTexWrap> newBibTexWrapList = new ArrayList<BibTexWrap>();
								newBibTexWrapList.add(bibTexItem);;
								distancesMap.put(titleDistance, newBibTexWrapList);
							}
						}
						
						List<BibTexWrap> lowerMLCSDistanceBibTextList = new ArrayList<BibTexWrap>();
						Double lowerDistValue = 1.1d;
						for(Entry<Double, List<BibTexWrap>> entryDist : distancesMap.entrySet()) {
							if(entryDist.getKey() <= lowerDistValue && entryDist.getValue() != null && entryDist.getValue().size() > 0) {
								lowerMLCSDistanceBibTextList = entryDist.getValue();
								lowerDistValue = entryDist.getKey();
							}
						}
						
						if((lowerMLCSDistanceBibTextList.size() == 1 || (lowerMLCSDistanceBibTextList.size() > 1 && lowerDistValue < 0.3d)) && lowerMLCSDistanceBibTextList.get(0) != null) {
							resultEntry = lowerMLCSDistanceBibTextList.get(0);
							logger.debug("CHOSEN > " + resultEntry.getTitle());
						}
						else if(StringUtils.isNotBlank(disambiguationString)) {
							// Array of matching words to search for
							String[] disambigFeatureStrings = disambiguationString.split(",");
							List<String> disambigFeatureList = Arrays.asList(disambigFeatureStrings);

							// Group Bibsonomy entries / results by number of matching words
							Map<Integer, List<BibTexWrap>> occurrencesMap = bibList.stream().collect(Collectors.groupingBy((bibTexItem) -> {
								Integer count = 0;
								
								String author = bibTexItem.getAuthorList();
								if(StringUtils.isNotEmpty(author)) {
									String[] authorSplit = author.split(" ");
									for(String disFeat : disambigFeatureList) {
										if(disFeat.length() > 1) {
											for(String authorSplitElem : authorSplit) {
												if(authorSplitElem.trim().toLowerCase().contains(disFeat.trim().toLowerCase())) {
													count++;
												}
											}
										} 
									}
								}
								
								return count;
							}) );

							// Choose Bibsonomy entry with the highest number of ranked words
							Integer selectedFeatNumber = -1;
							for(Entry<Integer, List<BibTexWrap>> mentry : occurrencesMap.entrySet()) {
								if(mentry.getKey() > selectedFeatNumber && !CollectionUtils.isEmpty(mentry.getValue())) {
									resultEntry = mentry.getValue().get(0);
									selectedFeatNumber = mentry.getKey();
								}
							}
						}

					}

					logger.debug("Bibsonomy result: " + resultEntry.toString());
					result = resultEntry;
				}
			}
			catch (Exception e) {
				Util.notifyException("Connecting to Bibsonomy", e, logger);
			}
		}
		return result;
	}

	/**
	 * Add the Bibsonomy bibliographic entry parsing results as features of a feature map
	 *  
	 * @param fm
	 * @param resultEntry 
	 */
	public static void populateFmBibtex(FeatureMap fm, BibTexWrap resultEntry) {
		if(fm != null && resultEntry != null) {
			if(StringUtils.isNotBlank(resultEntry.getTitle())) {
				fm.put("b_title", resultEntry.getTitle());
			}

			if(StringUtils.isNotBlank(resultEntry.getBooktitle())) {
				fm.put("b_bookTitle", resultEntry.getBooktitle());
			}

			if(StringUtils.isNotBlank(resultEntry.getAuthorList())) {
				fm.put("b_authorList", resultEntry.getAuthorList());
			}

			if(!CollectionUtils.isEmpty(resultEntry.getAuthor())) {
				Integer authorCount = 1;
				for(PersonName pn : resultEntry.getAuthor()) {
					if(pn != null) {
						if(StringUtils.isNotBlank(pn.getFirstName()) && StringUtils.isNotBlank(pn.getLastName())) {
							fm.put("b_authorName_" + authorCount, pn.getFirstName() + " " + pn.getLastName());
						}

						if(StringUtils.isNotBlank(pn.getFirstName())) {
							fm.put("b_authorFirstName_" + authorCount, pn.getFirstName());
						}

						if(StringUtils.isNotBlank(pn.getLastName())) {
							fm.put("b_authorLastName_" + authorCount, pn.getLastName());
						}
						authorCount++;
					}
				}
			}

			if(StringUtils.isNotBlank(resultEntry.getEditorList())) {
				fm.put("b_editorList", resultEntry.getEditor());
			}

			if(!CollectionUtils.isEmpty(resultEntry.getEditor())) {
				Integer editorCount = 1;
				for(PersonName pn : resultEntry.getEditor()) {
					if(pn != null) {
						if(StringUtils.isNotBlank(pn.getFirstName()) && StringUtils.isNotBlank(pn.getLastName())) {
							fm.put("b_editorName_" + editorCount, pn.getFirstName() + " " + pn.getLastName());
						}

						if(StringUtils.isNotBlank(pn.getFirstName())) {
							fm.put("b_editorFirstName_" + editorCount, pn.getFirstName());
						}

						if(StringUtils.isNotBlank(pn.getLastName())) {
							fm.put("b_editorLastName_" + editorCount, pn.getLastName());
						}
						editorCount++;
					}
				}
			}

			if(StringUtils.isNotBlank(resultEntry.getEdition())) {
				fm.put("b_edition", resultEntry.getEdition());
			}

			if(StringUtils.isNotBlank(resultEntry.getChapter())) {
				fm.put("b_chapter", resultEntry.getChapter());
			}

			if(StringUtils.isNotBlank(resultEntry.getHowpublished())) {
				fm.put("b_howPublished", resultEntry.getHowpublished());
			}

			if(StringUtils.isNotBlank(resultEntry.getBibtexKey())) {
				fm.put("b_bibtexKey", resultEntry.getBibtexKey());
			}

			if(StringUtils.isNotBlank(resultEntry.getYear())) {
				fm.put("b_year", resultEntry.getYear());
			}

			if(StringUtils.isNotBlank(resultEntry.getMonth())) {
				fm.put("b_month", resultEntry.getMonth());
			}

			if(StringUtils.isNotBlank(resultEntry.getDay())) {
				fm.put("b_day", resultEntry.getDay());
			}

			if(resultEntry.getCount() >= 0) {
				fm.put("b_count", "" + resultEntry.getCount());
			}

			if(StringUtils.isNotBlank(resultEntry.getOpenURL())) {
				fm.put("b_openURL", resultEntry.getOpenURL());
			}

			if(StringUtils.isNotBlank(resultEntry.getVolume())) {
				fm.put("b_volume", resultEntry.getVolume());
			}

			if(StringUtils.isNotBlank(resultEntry.getSeries())) {
				fm.put("b_series", resultEntry.getSeries());
			}

			if(StringUtils.isNotBlank(resultEntry.getEntrytype())) {
				fm.put("b_entryType", resultEntry.getEntrytype());
			}

			if(StringUtils.isNotBlank(resultEntry.getPublisher())) {
				fm.put("b_publisher", resultEntry.getPublisher());
			}

			if(StringUtils.isNotBlank(resultEntry.getInstitution())) {
				fm.put("b_institution", resultEntry.getInstitution());
			}

			if(StringUtils.isNotBlank(resultEntry.getJournal())) {
				fm.put("b_journal", resultEntry.getJournal());
			}

			if(StringUtils.isNotBlank(resultEntry.getUrl())) {
				fm.put("b_url", resultEntry.getUrl());
			}

			if(StringUtils.isNotBlank(resultEntry.getSchool())) {
				fm.put("b_school", resultEntry.getSchool());
			}

			if(StringUtils.isNotBlank(resultEntry.getPages())) {
				fm.put("b_pages", resultEntry.getPages());
			}

			if(StringUtils.isNotBlank(resultEntry.getPrivnote())) {
				fm.put("b_privNote", resultEntry.getPrivnote());
			}
		}
	}


	/*** Google Scholar ***/
	public Function<Annotation, Integer> googleScholarParsingTitleAnn = (ann) -> {
		try {
			String annText = textOfAnnotation.apply(ann);
			if(StringUtils.equalsIgnoreCase(this.useGoogleScholar, "true") && StringUtils.isNotBlank(annText)) {
				logger.info("Google Scholar parsing: '" + normalizeText(annText) + "'");
				googleScholarAnalysis(ann, annText);
				return countFeatsStartingWith(ann, "goos_");
			}
			else {
				logger.info("Google Scholar parsing: SKIPPED");
			}
		}
		catch (Exception e) {
			Util.notifyException("Google Scholar parsing", e, logger);
		}
		return 0;
	};

	/**
	 * Parse a bibliographic entry by Bibsonomy and store results as annotation features
	 * 
	 * @param ref Biblio entry annotation
	 * @param refTxt
	 */
	public void googleScholarAnalysis(Annotation ref, String refTxt) {
		if(ref.getFeatures() == null) {
			ref.setFeatures(Factory.newFeatureMap());
		}

		FeatureMap fm = ref.getFeatures();

		String firstAuthor_BIB = (fm.containsKey("b_authorName_1") && fm.get("b_authorName_1") != null) ? (String) fm.get("b_authorName_1") : "";
		String firstAuthor_FC = (fm.containsKey("f_authorName_1") && fm.get("f_authorName_1") != null) ? (String) fm.get("f_authorName_1") : "";
		String firstAuthor = (StringUtils.isNotBlank(firstAuthor_BIB)) ? firstAuthor_BIB : firstAuthor_FC;
		String title_FC = (fm.containsKey("f_title") && fm.get("f_title") != null) ? (String) fm.get("f_title") : "";
		String title_XR = (fm.containsKey("x_title") && fm.get("x_title") != null) ? (String) fm.get("x_title") : "";
		String paperTitlePars = refTxt;

		GoogleScholarResult result = null;
		if(paperTitlePars != null && !paperTitlePars.equals("")) {
			result = googleScholarExpansion(ref, paperTitlePars, firstAuthor);
			logger.debug("Google Scholar search (original bibliographic entry):  result retrieved: " + ((result != null && result.getTitle() != null) ? "TRUE" : "FALSE") + " - text: " + paperTitlePars);
		}
		else if(title_XR != null && !title_XR.equals("")) {
			result = googleScholarExpansion(ref, title_XR, firstAuthor);
			logger.debug("Google Scholar search (CrossRef title):  result retrieved: " + ((result != null && result.getTitle() != null) ? "TRUE" : "FALSE") + " - text: " + title_XR);
		}
		else if(title_FC != null && !title_FC.equals("")) {
			result = googleScholarExpansion(ref, title_FC, firstAuthor);
			logger.debug("Google Scholar search (FreeCite title):  result retrieved: " + ((result != null && result.getTitle() != null) ? "TRUE" : "FALSE") + " - text: " + title_FC);
		}
		else {
			logger.debug("Google Scholar search: NO RESULTS");
		}

		logger.debug("Google Scholar parsed entry - result retrieved: " + ((result != null && result.getTitle() != null) ? "TRUE" : "FALSE"));

		if(result != null) {
			populateFmGoogleScholar(fm, result);
		}

	}

	/**
	 * Given the bibliographic entry GATE annotation, its text, a Google Scholar search result is retrieved.
	 * The disambiguationString provides a text to guess the right Google Scholar search result if multiple matching entry are found for a bibliography entry.
	 * 
	 * @param ref
	 * @param refTxt
	 * @param disambiguationString
	 * @return
	 */
	public static GoogleScholarResult googleScholarExpansion(Annotation ref, String refTxt, String disambiguationString) {
		GoogleScholarResult result = null;

		if(StringUtils.isNotBlank(refTxt)) {
			try {
				GoogleScholarConn.maxSleepTimeInSec = 25;
				GoogleScholarConn.useProxy = false;
				List<GoogleScholarResult> searchResults = GoogleScholarConn.parseAddress(normalizeText(refTxt), 1);
				GoogleScholarConn.maxSleepTimeInSec = 30;

				if(searchResults != null && searchResults.size() > 0) {

					result = searchResults.get(0);

					if(searchResults.size() > 1) {

						logger.debug("Google Scholar result size > 1: " + searchResults.size());
						
						// Order by MLCS Distance with bibliographic entry text
						Map<Double, List<GoogleScholarResult>> distancesMap = new TreeMap<Double, List<GoogleScholarResult>>(); 
						int counrBibTex = 0;
						logger.debug("REF TEXT > " + refTxt);
						for(GoogleScholarResult googleScholarResult : searchResults) {
							Double titleDistance = 1d;
							
							counrBibTex++;
							logger.debug(counrBibTex + " > " + googleScholarResult.getTitle());
							
							if(googleScholarResult != null && googleScholarResult.getTitle() != null) {
								String title = googleScholarResult.getTitle();
								Double distWithRefText = edu.upf.taln.dri.common.util.Util.computeMetricLCS(title, refTxt);
								if(distWithRefText != null && distWithRefText >= 0) {
									titleDistance = distWithRefText;
								}
							}
							
							if(distancesMap.containsKey(titleDistance)) {
								distancesMap.get(titleDistance).add(googleScholarResult);
							}
							else {
								List<GoogleScholarResult> newBibTexWrapList = new ArrayList<GoogleScholarResult>();
								newBibTexWrapList.add(googleScholarResult);;
								distancesMap.put(titleDistance, newBibTexWrapList);
							}
						}
						
						List<GoogleScholarResult> lowerMLCSDistanceGoogleSchList = new ArrayList<GoogleScholarResult>();
						Double lowerDistValue = 1.1d;
						for(Entry<Double, List<GoogleScholarResult>> entryDist : distancesMap.entrySet()) {
							if(entryDist.getKey() <= lowerDistValue && entryDist.getValue() != null && entryDist.getValue().size() > 0) {
								lowerMLCSDistanceGoogleSchList = entryDist.getValue();
								lowerDistValue = entryDist.getKey();
							}
						}
						
						if((lowerMLCSDistanceGoogleSchList.size() == 1 || (lowerMLCSDistanceGoogleSchList.size() > 1 && lowerDistValue < 0.3d)) && lowerMLCSDistanceGoogleSchList.get(0) != null) {
							result = lowerMLCSDistanceGoogleSchList.get(0);
							logger.debug("CHOSEN > " + result.getTitle());
						}
						else if(StringUtils.isNotBlank(disambiguationString)) {
							// Array of matching words to search for
							String[] disambigFeatureStrings = disambiguationString.split(",");
							List<String> disambigFeatureList = Arrays.asList(disambigFeatureStrings);

							// Group Bibsonomy entries / results by number of matching words
							Map<Integer, List<GoogleScholarResult>> occurrencesMap = searchResults.stream().collect(Collectors.groupingBy((googleSchRes) -> {
								Integer count = 0;
								
								Map<String, String> author = googleSchRes.getAuthorName_LinkMap();
								for(String disFeat : disambigFeatureList) {
									for(Entry<String, String> authorEntry : author.entrySet()) {
										if(authorEntry.getKey() != null && authorEntry.getKey().trim().toLowerCase().contains(disFeat.toLowerCase())) {
											count++;
										}
									}
								}
								return count;
							}) );

							// Google Scholar search result with the highest number of ranked words
							Integer selectedFeatNumber = -1;
							for(Entry<Integer, List<GoogleScholarResult>> mentry : occurrencesMap.entrySet()) {
								if(mentry.getKey() > selectedFeatNumber && !CollectionUtils.isEmpty(mentry.getValue())) {
									result = mentry.getValue().get(0);
									selectedFeatNumber = mentry.getKey();
								}
							}
						}

						logger.debug("Google Scholar result: " + result.toString());
					}
				}

			}
			catch (Exception e) {
				Util.notifyException("Connecting to Google Scholar", e, logger);
			}
		}

		return result;
	}

	/**
	 * Add the Google Scholar bibliographic entry parsing results as features of a feature map
	 *  
	 * @param fm
	 * @param resultEntry 
	 */
	public static void populateFmGoogleScholar(FeatureMap fm, GoogleScholarResult resultEntry) {
		// If any, add selected entry features
		if(resultEntry != null) {

			// Skipped: goos_totalRetrieved

			if(StringUtils.isNotBlank(resultEntry.getTitle())) {
				fm.put("goos_title", resultEntry.getTitle());
			}

			if(StringUtils.isNotBlank(resultEntry.getLink())) {
				fm.put("goos_link", resultEntry.getLink());
			}

			if(StringUtils.isNotBlank(resultEntry.getSecondLine())) {
				fm.put("goos_secondLine", resultEntry.getSecondLine());
			}

			if(StringUtils.isNotBlank(resultEntry.getYear())) {
				fm.put("goos_year", resultEntry.getYear());
			}

			if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
				fm.put("goos_snippet", resultEntry.getAbstractSnippet());
			}

			Integer authorIndex = 1;
			boolean addedAuthors = false;
			if(!CollectionUtils.isEmpty(resultEntry.getRefAuthorsList())) {
				for(String author : resultEntry.getRefAuthorsList()) {
					if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
						fm.put("goos_authorName_" + authorIndex, author);
					}
					if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
						fm.put("goos_authorURL_" + authorIndex, "NO_URL");
					}
					authorIndex++;
					addedAuthors = true;
				}
			}
			if(!addedAuthors && resultEntry.getAuthorName_LinkMap() != null && resultEntry.getAuthorName_LinkMap().size() > 0) {
				for(Entry<String, String> authorEntry :resultEntry.getAuthorName_LinkMap().entrySet()) {
					if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
						fm.put("goos_authorName_" + authorIndex, authorEntry.getKey());
					}
					if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
						fm.put("goos_authorURL_" + authorIndex, authorEntry.getValue());
					}
					authorIndex++;
				}
			}

			Integer citIndex = 1;
			for(Entry<String, String> citEntry : resultEntry.getCitationType_ContentMap().entrySet()) {
				if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
					fm.put("goos_citStyle_" + citIndex, citEntry.getKey());
				}
				if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
					fm.put("goos_citContent_" + citIndex, citEntry.getValue());
				}
				citIndex++;
			}

			Integer journalIndex = 1;
			if(!CollectionUtils.isEmpty(resultEntry.getRefJournalList())) {
				for(String journal : resultEntry.getRefJournalList()) {
					if(StringUtils.isNotBlank(resultEntry.getAbstractSnippet())) {
						fm.put("goos_journalName_" + journalIndex, journal);
					}
					journalIndex++;
				}
			}

		}
	}

	/**
	 * Trigger the invocation of different services and tools to parse bibliographic entries.
	 * The results are added as GATE annotation features.
	 */
	private Consumer<Annotation> parseCitation = (ann) -> {
		try {
			String annText = textOfAnnotation.apply(ann);
			if(StringUtils.isNotBlank(annText)) {
				logger.info("Start parsing citation text: '" + normalizeText(annText) + "'");
				
				Integer freeCiteResult = freeCiteParsing.apply(ann);
				logger.info("Retrieved " + ((freeCiteResult != null) ? freeCiteResult : "0") + " metadata item(s) from FreeCite.");
				Integer crossRefResult = crossRefParsing.apply(ann);
				logger.info("Retrieved " + ((crossRefResult != null) ? crossRefResult : "0") + " metadata item(s) from CrossRef.");
				Integer bibsonomyResult = bibsonomyParsing.apply(ann);
				logger.info("Retrieved " + ((bibsonomyResult != null) ? bibsonomyResult : "0") + " metadata item(s) from Bibsonomy.");
				
				// Use or not Google Scholar
				String bibsonomyTitle = GateUtil.getStringFeature(ann, "b_title").orElse(null);
				Double MLCSmetric = 1d;
				double titleBibEntryProp = 20d;
				if(bibsonomyTitle != null && StringUtils.isNotEmpty(annText) ) {
					MLCSmetric = edu.upf.taln.dri.common.util.Util.computeMetricLCS(bibsonomyTitle, annText);
					titleBibEntryProp = new Integer(bibsonomyTitle.length()).doubleValue() / new Integer(annText.length()).doubleValue();
				}
				
				
				logger.debug("Biblio entry text: " + annText);
				logger.debug("Bibsonomy title: " + ((bibsonomyTitle != null) ? bibsonomyTitle : "NONE"));
				logger.debug("MLCSmetric: " + MLCSmetric + " - Prop: " + titleBibEntryProp);
				if(StringUtils.isEmpty(bibsonomyTitle) && 
					( 	(MLCSmetric > 0.75d && titleBibEntryProp >= 0 && titleBibEntryProp < 4) || 
						(MLCSmetric > 0.8d && titleBibEntryProp >= 4 && titleBibEntryProp < 10) || 
						(MLCSmetric > 0.85d && titleBibEntryProp >= 10 && titleBibEntryProp < 15) || 
						(MLCSmetric > 0.95d && titleBibEntryProp >= 15 && titleBibEntryProp < 30) )) {
					logger.info("Biblio search results invalid -> Google Scholar search...");
					Integer googleScholarResult = googleScholarParsingTitleAnn.apply(ann);
					logger.info("Retrieved " + ((googleScholarResult != null) ? googleScholarResult : "0") + " metadata item(s) from Google Scholar.");
				}
				
				logger.debug("Parsed: " + GateUtil.getAnnotationText(ann, this.document).orElse("NONE"));
				for(Entry<Object, Object> feat : ann.getFeatures().entrySet()) {
					if(feat != null && feat.getKey() != null && feat.getValue() != null) {
						logger.debug("       > " + feat.getKey() + " --> " + feat.getValue());
					}
				}
			}
			else {
				logger.debug("Skipped bibliographic entry!");
			}
		}
		catch (Exception e) {
			Util.notifyException("Global bibliographic entry parsing", e, logger);
		}
	};
	
	/*
	 * Count number of annotation features with name starting with string that have values that are not blank String and not null Integer
	 */
	private static Integer countFeatsStartingWith(Annotation ann, String startWithStr) {
		Integer featuresAdded = 0;
		
		if(ann != null && ann.getFeatures() != null && startWithStr != null && !startWithStr.trim().equals("")) {
			for(Map.Entry<Object, Object> featureEntry : ann.getFeatures().entrySet()) {
				if( featureEntry != null && featureEntry.getKey() != null && featureEntry.getKey() instanceof String &&
					((String) featureEntry.getKey()).startsWith(startWithStr) && featureEntry.getValue() != null &&
					(
						(featureEntry.getValue() instanceof String && !((String) featureEntry.getValue()).trim().equals("") ) ||
						(featureEntry.getValue() instanceof Integer) 
					) ) {
					featuresAdded++;
				}
			}
		}
		
		return featuresAdded;
	}
	
	private static String normalizeText(String inputText) {
		if(inputText != null) {
			inputText = inputText.replaceAll("\t", " ");
			inputText = inputText.replaceAll("\\s+", " ");
			inputText = inputText.trim();
		}
		return inputText;
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
							( ((String) feature.getKey()).startsWith("b_") || ((String) feature.getKey()).startsWith("goos_") || ((String) feature.getKey()).startsWith("f_") || ((String) feature.getKey()).startsWith("x_") ) 
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
			// Get the document to process
			gate.Document doc = getDocument();

			// Check variables
			this.inputBiblioEntryAS = StringUtils.defaultIfBlank(this.inputBiblioEntryAS, ImporterBase.driAnnSet);
			this.inputBiblioEntryAStype = StringUtils.defaultIfBlank(this.inputBiblioEntryAStype, ImporterBase.bibEntryAnnType);

			AnnotationSet refAnnotationSet = doc.getAnnotations(this.inputBiblioEntryAS);

			List<Annotation> bibEntryAnnotations = gate.Utils.inDocumentOrder(refAnnotationSet.get(this.inputBiblioEntryAStype));

			bibEntryAnnotations.stream().forEach(annReset);
			
			this.annotationReset = true;
		}

		return true;
	}

}

