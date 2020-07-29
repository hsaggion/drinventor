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
package edu.upf.taln.dri.lib.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.exception.InvalidParameterException;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.ext.CandidateTermOcc;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.Header;
import edu.upf.taln.dri.lib.model.ext.LangENUM;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.ext.SummaryTypeENUM;
import edu.upf.taln.dri.lib.model.graph.DependencyGraph;
import edu.upf.taln.dri.lib.model.graph.SentGraphTypeENUM;
import edu.upf.taln.dri.lib.model.util.DocParse;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;
import edu.upf.taln.dri.module.babelnet.BabelnetAnnotator;
import edu.upf.taln.dri.module.citation.BiblioEntryParser;
import edu.upf.taln.dri.module.citation.CitationLinker;
import edu.upf.taln.dri.module.citation.InlineCitationSpotter;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.header.HeaderAnalyzer;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.SourceENUM;
import edu.upf.taln.dri.module.importer.jats.ImporterJATS;
import edu.upf.taln.dri.module.importer.pdf.ImporterGROBID;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFX;
import edu.upf.taln.dri.module.languageDetector.LanguageDetector;
import edu.upf.taln.dri.module.metaannotations.MetaAnnotator;
import edu.upf.taln.dri.module.parser.MateParser;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.summary.lexrank.LexRankSummarizer;
import edu.upf.taln.dri.module.summary.titlesim.TitleSimSummarizer;
import edu.upf.taln.dri.module.terminology.TermAnnotator;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Factory;
import gate.corpora.CorpusImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.OffsetComparator;

/**
 * IMPORTANT: Never instantiate directly this class! <br/><br/>
 * To get an instance of a Document by the {@link edu.upf.taln.dri.lib.model.Document Document interface}, you have always to use one of the
 * {@link edu.upf.taln.dri.lib.Factory Factory} methods:  <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument() Factory.createNewDocument()} <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument(String absoluteFilePath) Factory.createNewDocument(String absoluteFilePath)} <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument(File file) Factory.createNewDocument(File file)} <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#getEmptyDocument() Factory.getEmptyDocument()} <br/>
 * 
 *
 */
public class DocumentImpl implements Document {

	private static Logger logger = Logger.getLogger(DocumentImpl.class);

	// TODO: change to private
	public DocCacheManager cacheManager = null;

	// CleanUp boolean
	private boolean cleanedUp = false;

	// Synchronization elements
	private static final Object LOCK_PDFXimporter_Resource = new Object();
	private static final Object LOCK_PDFEXTimporter_Resource = new Object();
	private static final Object LOCK_GROBIDimporter_Resource = new Object();
	private static final Object LOCK_JATSimporter_Resource = new Object();
	private static final Object LOCK_MateParser_Resource = new Object();
	private static final Object LOCK_RhetoricalClassifier_Resource = new Object();
	private static final Object LOCK_TermAnnotator_Resource = new Object();
	private static final Object LOCK_LanguageDetector_Resource = new Object();
	private static final Object LOCK_MetaAnnotator_Resource = new Object();
	private static final Object LOCK_CitationSanitizer_Resource = new Object();
	private static final Object LOCK_CitationLinker_Resource = new Object();
	private static final Object LOCK_CitationExpander_Resource = new Object();
	private static final Object LOCK_HeaderAnalyzer_Resource = new Object();
	private static final Object LOCK_CorefChainBuilder_Resource = new Object();
	private static final Object LOCK_BabelnetAnnotator_Resource = new Object();
	private static final Object LOCK_corpusController_preprocess_XGAPPpreprocStep1 = new Object();
	private static final Object LOCK_corpusController_preprocess_XGAPPpreprocStep2 = new Object();
	private static final Object LOCK_corpusController_XGAPPheader = new Object();
	private static final Object LOCK_corpusController_XGAPPcitMarker = new Object();
	private static final Object LOCK_corpusController_XGAPPcorefMentionSpot = new Object();
	private static final Object LOCK_corpusController_XGAPPcausality = new Object();
	private static final Object LOCK_corpusController_XGAPPmetaAnnotator = new Object();
	private static final Object LOCK_corpusController_LexRanksumm = new Object();
	private static final Object LOCK_corpusController_TitleSimsumm = new Object();

	// GATE module references
	private static ImporterPDFX PDFXimporter_Resource = null;
	private static ImporterPDFEXT PDFEXTimporter_Resource = null;
	private static ImporterGROBID GROBIDimporter_Resource = null;
	private static ImporterJATS JATSimporter_Resource = null;
	private static Map<LangENUM, MateParser> MateParsersLang_Resource = null;
	private static RhetoricalClassifier RhetoricalClassifier_Resource = null;
	private static LanguageDetector LanguageDetector_Resource = null;
	private static TermAnnotator TermAnnotator_Resource = null;
	private static MetaAnnotator MetaAnnotator_Resource = null;
	private static InlineCitationSpotter CitationSanitizer_Resource = null;
	private static CitationLinker CitationLinker_Resource = null;
	private static BiblioEntryParser CitationExpander_Resource = null;
	private static HeaderAnalyzer HeaderAnalyzer_Resource = null;
	private static CorefChainBuilder CorefChainBuilder_Resource = null;
	private static BabelnetAnnotator BabelnetAnnotator_Resource = null;
	protected static LexRankSummarizer LexRankSummarizer_Resource = null;
	protected static TitleSimSummarizer TitleSimSummarizer_Resource = null;
	protected static CorpusController corpusController_preprocess_XGAPPpreprocStep1 = null;
	protected static CorpusController corpusController_preprocess_XGAPPpreprocStep2 = null;
	protected static CorpusController corpusController_XGAPPheader = null;
	protected static CorpusController corpusController_XGAPPcitMarker = null;
	protected static CorpusController corpusController_XGAPPcorefMentionSpot = null;
	protected static CorpusController corpusController_XGAPPcausality = null;
	protected static CorpusController corpusController_XGAPPmetaAnnotator = null;

	public static void initDocPointers(
			ImporterPDFX ref1,
			ImporterPDFEXT ref2,
			ImporterGROBID ref3,
			ImporterJATS ref4,
			Map<LangENUM, MateParser> ref5,
			RhetoricalClassifier ref6,
			TermAnnotator ref7,
			MetaAnnotator ref8,
			LanguageDetector ref9,
			InlineCitationSpotter ref10,
			CitationLinker ref11,
			BiblioEntryParser ref12,
			HeaderAnalyzer ref13,
			CorefChainBuilder ref14,
			BabelnetAnnotator ref15,
			LexRankSummarizer ref16,
			TitleSimSummarizer ref17,
			CorpusController ref18,
			CorpusController ref19,
			CorpusController ref20,
			CorpusController ref21,
			CorpusController ref22,
			CorpusController ref23,
			CorpusController ref24) {

		PDFXimporter_Resource = ref1;
		PDFEXTimporter_Resource = ref2;
		GROBIDimporter_Resource = ref3;
		JATSimporter_Resource = ref4;
		MateParsersLang_Resource = ref5;
		RhetoricalClassifier_Resource = ref6;
		TermAnnotator_Resource = ref7;
		MetaAnnotator_Resource = ref8;
		LanguageDetector_Resource = ref9;
		CitationSanitizer_Resource = ref10;
		CitationLinker_Resource = ref11;
		CitationExpander_Resource = ref12;
		HeaderAnalyzer_Resource = ref13;
		CorefChainBuilder_Resource = ref14;
		BabelnetAnnotator_Resource = ref15;
		LexRankSummarizer_Resource = ref16;
		TitleSimSummarizer_Resource = ref17;
		corpusController_preprocess_XGAPPpreprocStep1 = ref18;
		corpusController_preprocess_XGAPPpreprocStep2 = ref19;
		corpusController_XGAPPheader = ref20;
		corpusController_XGAPPcitMarker = ref21;
		corpusController_XGAPPcorefMentionSpot = ref22;
		corpusController_XGAPPcausality = ref23;
		corpusController_XGAPPmetaAnnotator = ref24;
	}

	// Constructor
	public DocumentImpl() {
		super();
		this.cacheManager = null;
	}

	public DocumentImpl(gate.Document gateDoc) throws InternalProcessingException {
		super();
		this.cacheManager = new DocCacheManager(gateDoc);
	}

	@Override
	public String getName() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		return GateUtil.getStringFeature(cacheManager.getGateDoc(), "name").orElse(null);
	}

	@Override
	public void loadXML(String absoluteFilePath) throws DRIexception {

		if(absoluteFilePath == null || absoluteFilePath.length() == 0) {
			throw new InvalidParameterException("Invalid XML file absolute path (null or empty String)");
		}

		File inputXML = new File(absoluteFilePath);
		if(!inputXML.exists()) {
			throw new ResourceAccessException("The file at:'" + absoluteFilePath + "' does not exist.");
		}

		gate.Document gateDoc = null;
		try {
			URL documentUrl = inputXML.toURI().toURL();
			gateDoc = Factory.newDocument(documentUrl, "UTF-8");
			gateDoc = DocParse.sanitize(gateDoc);
		} catch (ResourceInstantiationException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
			throw new InternalProcessingException("Errors while loading the file at:'" + absoluteFilePath + "'.");
		} catch (MalformedURLException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
			throw new InternalProcessingException("Errors while loading the file at:'" + absoluteFilePath + "'.");
		} 

		if(gateDoc != null) {
			this.cacheManager = new DocCacheManager(gateDoc);
		}
		else {
			throw new InternalProcessingException("Errors while loading the file at:'" + absoluteFilePath + "'.");
		}
	}

	@Override
	public void loadXMLString(String XMLStringContents) throws DRIexception {
		if(XMLStringContents == null || XMLStringContents.length() == 0) {
			throw new InvalidParameterException("Invalid XML String contents (UTF-8)");
		}

		gate.Document gateDoc = null;
		try {
			gateDoc = Factory.newDocument(XMLStringContents);
			gateDoc = DocParse.sanitize(gateDoc);
		} catch (ResourceInstantiationException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
			throw new InternalProcessingException("Errors while loading the file from the string '" + XMLStringContents + "'.");
		} 

		if(gateDoc != null) {
			this.cacheManager = new DocCacheManager(gateDoc);
		}
		else {
			throw new InternalProcessingException("Errors while loading the file from the string '" + XMLStringContents + "'.");
		}
	}

	@Override
	public void loadXML(File file) throws DRIexception {

		if(file == null) {
			throw new InvalidParameterException("Invalid File object (null)");
		}

		if(!file.exists()) {
			throw new ResourceAccessException("Invalid File object (does not exist)");
		}

		String absoluteFilePath = file.getAbsolutePath();

		loadXML(absoluteFilePath);
	}

	@Override
	public String getXMLString() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		this.cacheManager.getGateDoc().setFeatures((this.cacheManager.getGateDoc().getFeatures() != null) ? this.cacheManager.getGateDoc().getFeatures() : Factory.newFeatureMap());
		this.cacheManager.getGateDoc().getFeatures().put("libVersion", (edu.upf.taln.dri.lib.Factory.getVersion() != null) ? edu.upf.taln.dri.lib.Factory.getVersion() : "UNSPECIFIED");

		return this.cacheManager.getGateDoc().toXml();
	}

	@Override
	public org.w3c.dom.Document getXMLDocument() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		org.w3c.dom.Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new ByteArrayInputStream(this.cacheManager.getGateDoc().toXml().getBytes()));
		} catch (ParserConfigurationException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.warn("Exception: " + e.getMessage());
			e.printStackTrace();
		}

		if(doc == null) {
			throw new InternalProcessingException("Not possible to generate the XML object including the contents of the document");
		}
		else {
			return doc;
		}
	}

	@Override
	public String getRawText() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		String rawText = GateUtil.getDocumentText(this.cacheManager.getGateDoc(), 0l, gate.Utils.lengthLong(this.cacheManager.getGateDoc()) - 1l).orElse(null);

		if(rawText == null) {
			throw new InternalProcessingException("Not possible to retrieve the raw text of the document");
		}
		else {
			return rawText;
		}
	}

	@Override
	public void preprocess() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		this.resetDocumentExtractionData();
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingHeader(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingCoref(false);
		this.parsingCausality(false);
		this.parsingTerminology(false);
		this.parsingRhetoricalClass(false);
		this.parsingBabelNet(false);
		this.parsingSummary(false);
	}

	@Override
	public List<Section> extractSections(Boolean onlyRoot) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingBabelNet(false);
		this.parsingRhetoricalClass(false);
		this.parsingCoref(false);
		this.parsingCausality(false);

		List<Annotation> h1DocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h1AnnType);
		List<Annotation> allhAnn = new ArrayList<Annotation>();
		allhAnn.addAll(h1DocOrdered);

		if(onlyRoot != null && !onlyRoot) {
			List<Annotation> h2DocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h2AnnType);
			List<Annotation> h3DocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h3AnnType);
			List<Annotation> h4DocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h4AnnType);
			List<Annotation> h5DocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h5AnnType);
			allhAnn.addAll(h2DocOrdered);
			allhAnn.addAll(h3DocOrdered);
			allhAnn.addAll(h4DocOrdered);
			allhAnn.addAll(h5DocOrdered);
		}

		Collections.sort(allhAnn, new OffsetComparator());

		List<Section> retSectionList = new ArrayList<Section>();
		allhAnn.stream().forEach((sectAnn) -> {
			Section newSection = ObjectGenerator.getSectionFromId(sectAnn.getId(), this.cacheManager);
			if(newSection != null) {
				retSectionList.add(newSection);
			}
		});

		return retSectionList;
	}

	@Override
	public List<Sentence> extractSentences(SentenceSelectorENUM sentenceSel) throws InternalProcessingException {

		sentenceSel = (sentenceSel != null) ? sentenceSel : SentenceSelectorENUM.ALL; 

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(this.cacheManager.getGateDoc().getFeatures() == null) {
			this.cacheManager.getGateDoc().setFeatures(gate.Factory.newFeatureMap());
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingBabelNet(false);
		this.parsingRhetoricalClass(false);
		this.parsingCoref(false);
		this.parsingCausality(false);

		List<Annotation> sentDocOrdered = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		List<Annotation> abstractAnnotationList = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.abstractAnnType);

		List<Sentence> retSentenceList = new ArrayList<Sentence>();
		for(Annotation sentAnn : sentDocOrdered) {
			Sentence newSentence = ObjectGenerator.getSentenceFromId(sentAnn.getId(), this.cacheManager);
			if(newSentence != null) {
				boolean inAbstract = false;
				for(Annotation abstractAnn : abstractAnnotationList) {
					if(abstractAnn != null && abstractAnn.overlaps(sentAnn)) {
						inAbstract = true;
						break;
					}
				}

				if(sentenceSel.equals(SentenceSelectorENUM.ALL) ||
						(sentenceSel.equals(SentenceSelectorENUM.ONLY_ABSTRACT) && inAbstract) ||
						(sentenceSel.equals(SentenceSelectorENUM.ALL_EXCEPT_ABSTRACT) && !inAbstract) ) {
					retSentenceList.add(newSentence);
				}
			}
		}

		return retSentenceList;
	}

	@Override
	public Sentence extractSentenceById(int sentenceId) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(this.cacheManager.getGateDoc().getFeatures() == null) {
			this.cacheManager.getGateDoc().setFeatures(gate.Factory.newFeatureMap());
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingBabelNet(false);
		this.parsingRhetoricalClass(false);
		this.parsingCoref(false);
		this.parsingCausality(false);

		return ObjectGenerator.getSentenceFromId(sentenceId, this.cacheManager);
	}

	@Override
	public List<CandidateTermOcc> extractTerminology() throws DRIexception {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingBabelNet(false);
		this.parsingCoref(false);
		this.parsingCausality(false);
		this.parsingTerminology(false);

		List<Annotation> candidateTermAnnList = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.term_AnnSet, ImporterBase.term_CandOcc);

		List<CandidateTermOcc> retCandTermOccList = new ArrayList<CandidateTermOcc>();
		for(Annotation candidateTermAnn : candidateTermAnnList) {
			CandidateTermOcc newCandidateTerm = ObjectGenerator.getCandidateTermOccFromId(candidateTermAnn.getId(), this.cacheManager);
			if(newCandidateTerm != null) {
				retCandTermOccList.add(newCandidateTerm);
			}
		}

		return retCandTermOccList;
	}

	@Override
	public List<Sentence> extractSummary(int sentNumber, SummaryTypeENUM summaryType) throws InternalProcessingException {
		sentNumber = (sentNumber > 0 && sentNumber <= 120) ? sentNumber : 10;
		summaryType = (summaryType != null) ? summaryType : SummaryTypeENUM.TITILE_SIM;
		
		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		// this.parsingMetaAnnotations(false); - Disabled becuse the summarized doesn't use these annotations
		// this.parsingBabelNet(false); - Disabled becuse the summarized doesn't use these annotations
		// this.parsingCoref(false); - Disabled becuse the summarized doesn't use these annotations
		// this.parsingCausality(false); - Disabled becuse the summarized doesn't use these annotations
		// this.parsingRhetoricalClass(false); - Disabled becuse the summarized doesn't use these annotations
		this.parsingSummary(false);

		Map<Annotation, Double> rankedSentencesMap = new HashMap<Annotation, Double>();

		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(this.cacheManager.getGateDoc());

		List<Annotation> allSentList = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
		switch(summaryType) {
		case TITILE_SIM:
			for(Annotation allSentElem : allSentList) {
				if(allSentElem != null && allSentElem.getFeatures() != null && allSentElem.getFeatures().containsKey(ImporterBase.sentence_titleSimScore) &&
						allSentElem.getFeatures().get(ImporterBase.sentence_titleSimScore) != null) {
					rankedSentencesMap.put(allSentElem, (Double) allSentElem.getFeatures().get(ImporterBase.sentence_titleSimScore));
				}
			}
			
			break;
		case LEX_RANK:
			for(Annotation allSentElem : allSentList) {
				if(allSentElem != null && allSentElem.getFeatures() != null && allSentElem.getFeatures().containsKey(ImporterBase.sentence_lexRankScore) &&
						allSentElem.getFeatures().get(ImporterBase.sentence_lexRankScore) != null) {
					rankedSentencesMap.put(allSentElem, (Double) allSentElem.getFeatures().get(ImporterBase.sentence_lexRankScore));
				}
			}
			
			break;
		}

		corpusToProcess.clear();
		corpusToProcess.cleanup();
		
		rankedSentencesMap = Util.sortByValueDec(rankedSentencesMap);

		List<Sentence> summarySentences = new ArrayList<Sentence>();

		int count = 0;
		for(Entry<Annotation, Double> entry : rankedSentencesMap.entrySet()) {
			if(entry.getValue() != null) {
				try {
					List<Annotation> sentenceList = GateUtil.getAnnInDocOrderContainedOffset(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType,
							entry.getKey().getStartNode().getOffset(), entry.getKey().getEndNode().getOffset());
					if(sentenceList.size() == 1) {
						summarySentences.add(ObjectGenerator.getSentenceFromId(sentenceList.get(0).getId(), this.cacheManager));
						count++;

						if(count >= sentNumber) {
							break;
						}
					}
				}
				catch (Exception e) {
					/* Do nothing */
				}

			}
		}

		return summarySentences;
	}

	@Override
	public DependencyGraph extractSentenceGraph(int sentenceId, SentGraphTypeENUM graphType) throws DRIexception {

		DependencyGraph retGraph = null;

		if(this.cacheManager.getGateDoc() == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingBabelNet(false);
		this.parsingRhetoricalClass(false);
		this.parsingCoref(false);
		this.parsingCausality(false);


		List<String> regexps = new ArrayList<String>();
		regexps.add("SBJ");
		regexps.add("OBJ");
		regexps.add("CAUSE");

		switch(graphType) {
		case DEP:
			// Do not merge nodes
			retGraph = ObjectGenerator.getDepGraphFromSentId(sentenceId, this.cacheManager, true);
			break;

		case DEP_SBJ_VERB_OBJ_CAUSE:
			// Do not merge nodes
			retGraph = ObjectGenerator.getDepGraphFromSentId(sentenceId, this.cacheManager, true);
			retGraph.deleteEdgesByNameRegExp(regexps, false);
			break;

		case COMPACT:
			// Merge nodes
			retGraph = ObjectGenerator.getDepGraphFromSentId(sentenceId, this.cacheManager, true);
			break;

		case COMPACT_SBJ_VERB_OBJ_CAUSE:
			// Merge nodes
			retGraph = ObjectGenerator.getDepGraphFromSentId(sentenceId, this.cacheManager, true);
			retGraph.deleteEdgesByNameRegExp(regexps, false);
			break;
		}

		return retGraph;
	}

	@Override
	public DependencyGraph extractDocumentGraph(SentenceSelectorENUM sentenceSel) throws DRIexception {

		DependencyGraph retGraph = null;

		if(this.cacheManager.getGateDoc() == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);
		this.parsingDep(false);
		this.parsingMetaAnnotations(false);
		this.parsingBabelNet(false);
		this.parsingCoref(false);
		this.parsingCausality(false);
		this.parsingRhetoricalClass(false);

		List<Sentence> graphSentences = extractSentences(sentenceSel);

		retGraph = new DependencyGraph();

		Map<Sentence, DependencyGraph> processedSentenceMap = new HashMap<Sentence, DependencyGraph>();
		for(Sentence graphSentence : graphSentences) {
			if(graphSentence != null) {
				DependencyGraph sentGraph = ObjectGenerator.getDepGraphFromSentId(graphSentence.getId(), this.cacheManager, true);
				if(sentGraph != null) {

					processedSentenceMap.put(graphSentence, sentGraph);

					// System.out.println("SENTENCE " + graphSentence.getId() + " WITH " + sentGraph.getAllEdges().size() + " EDGES.");
					for(Entry<Integer, Pair<Integer, Integer>> graphEdge : sentGraph.getAllEdges().entrySet()) {
						if(graphEdge != null && graphEdge.getKey() != null && graphEdge.getValue() != null) {

							// Check if the edge is a SBJ or OBJ one
							String edgeName = sentGraph.getEdgeName(graphEdge.getKey());
							boolean addEdge = false;
							Set<Integer> coordinateNames = new HashSet<Integer>();
							Set<Integer> coordinateVerbs = new HashSet<Integer>();
							if(edgeName != null && (edgeName.equals("SBJ") || edgeName.equals("OBJ") || edgeName.equals("CAUSE"))) {

								/* FOR DEBUG PURPOSES
								String sourceNode = sentGraph.getNodeName(sentGraph.getEdgeFromNode(graphEdge.getKey()));
								String sourceNodeC = sentGraph.getNodeCorefName(sentGraph.getEdgeFromNode(graphEdge.getKey()));
								String targetNode = sentGraph.getNodeName(sentGraph.getEdgeToNode(graphEdge.getKey()));
								String targetNodeC = sentGraph.getNodeCorefName(sentGraph.getEdgeToNode(graphEdge.getKey()));
								 */

								// Check for coordinations among nouns acting as subject or object: main_noun_node <-- COORD <-- 'and' <-- CONJ <-- noun_node
								coordinateNames = getNodeCoordinate(sentGraph, graphEdge.getValue().getLeft(), "N");
								coordinateNames.add(graphEdge.getValue().getLeft());

								// Check for coordinations among verbs: main_verb_node <-- COORD <-- 'and' <-- CONJ <-- verb_node
								if(edgeName.equals("SBJ")) { // Only for SBJ triples we connect each coordinate verb with each coordinate subject
									coordinateVerbs = getNodeCoordinate(sentGraph, graphEdge.getValue().getRight(), "V");
								}
								coordinateVerbs.add(graphEdge.getValue().getRight());

								// In coordinateNames got all the IDs of name nodes that are coordinates
								// In coordinateVerbs got all the IDs of verb nodes that are coordinates

								addEdge = true;
							}

							if(addEdge) {
								// Generate a link between each pair of coordinate names and coordinate verb
								if(coordinateNames != null && coordinateNames.size() > 0) {
									for(Integer coordinateNodeID : coordinateNames) {
										if(coordinateVerbs != null && coordinateVerbs.size() > 0) {

											for(Integer coordinateVerbID : coordinateVerbs) {

												if(retGraph != null && sentGraph != null && coordinateNodeID != null && coordinateVerbID != null &&
														edgeName != null && !edgeName.equals("")) {

													Integer finalSourceNodeId = addDocumentGraphNode(retGraph, sentGraph, coordinateNodeID);
													// System.out.println("Number of nodes: " + retGraph.getNodeCount());
													Integer finalTargetNodeId = addDocumentGraphNode(retGraph, sentGraph, coordinateVerbID);
													// System.out.println("Number of nodes: " + retGraph.getNodeCount());

													addDocumentGraphEdge(retGraph, finalSourceNodeId, finalTargetNodeId, edgeName);

												}
												else {
													logger.debug("Error adding Document graph edge.");
												}

											}

										}
									}
								}
							}
						}
					}
					// System.out.println("TOTAL EDGES IN FINAL GRAPH AFTER SENTENCE " + graphSentence.getId() + ": " + retGraph.getAllEdges().size() + " EDGES.");
				}
			}
		}


		// Adding cross-sentence causal edges - START
		for(Entry<Sentence, DependencyGraph> graphSentenceMapEntry : processedSentenceMap.entrySet()) {
			if(graphSentenceMapEntry != null && graphSentenceMapEntry.getKey() != null && graphSentenceMapEntry.getValue() != null) {

				// Get all node IDs of the sentence dependency graph
				Set<Integer> sentenceGraphNodeIDs = new HashSet<Integer>();
				for(Entry<Integer, Pair<Integer, Integer>> edgeElement : graphSentenceMapEntry.getValue().getAllEdges().entrySet()) {
					if(edgeElement != null && edgeElement.getValue() != null &&
							edgeElement.getValue().getLeft() != null && edgeElement.getValue().getRight() != null) {
						sentenceGraphNodeIDs.add(edgeElement.getValue().getLeft());
						sentenceGraphNodeIDs.add(edgeElement.getValue().getRight());
					}
				}

				// For each node check if it takes part in one or more cross-sentence causal relations
				// For each cross-sentence causal relation the node is part of, the same node should include as 
				// features the role in the causal relation, the id of the cause node and the id of the effect node.
				// The id of one among the cause or effect node should be equal to the id of the node, instead the id of
				// the other cause / effect node should be the id of a node in another sentence

				for(Integer resultGraphNodeID : sentenceGraphNodeIDs) {
					if(resultGraphNodeID != null && graphSentenceMapEntry.getValue().getCausalRoleNameMap(resultGraphNodeID) != null && 
							graphSentenceMapEntry.getValue().getCausalCauseIDmap(resultGraphNodeID) != null &&
							graphSentenceMapEntry.getValue().getCausalEffectIDmap(resultGraphNodeID) != null) {

						for(Entry<Integer, String> causalRelRole : graphSentenceMapEntry.getValue().getCausalRoleNameMap(resultGraphNodeID).entrySet()) {
							if(causalRelRole != null && causalRelRole.getKey() != null && causalRelRole.getValue() != null && 
									causalRelRole.getValue().equals("CAUSE") && graphSentenceMapEntry.getValue().getCausalEffectIDmap(resultGraphNodeID).containsKey(causalRelRole.getKey()) &&
									graphSentenceMapEntry.getValue().getCausalEffectIDmap(resultGraphNodeID).get(causalRelRole.getKey()) != null) {
								// Add causal edge from the current node with ID equal to resultGraphNodeID to the node graphSentenceMapEntry.getValue().getCausalEffectIDmap(resultGraphNodeID)

								// Get cause node of the current dependency graph
								Integer causeNodeIDinSentGraph = resultGraphNodeID; 

								// Retrieve effect node from the sentence graph of another sentence (since it belongs to a sentence different to the actual one)
								Integer effectNodeID = graphSentenceMapEntry.getValue().getCausalEffectIDmap(resultGraphNodeID).get(causalRelRole.getKey());
								for(Entry<Sentence, DependencyGraph> graphSentenceMapEntryInt : processedSentenceMap.entrySet()) {
									if(graphSentenceMapEntryInt != null && graphSentenceMapEntryInt.getKey() != null && graphSentenceMapEntryInt.getValue() != null) {

										// 1) Get all node IDs of the internal sentence dependency graph
										Set<Integer> sentenceGraphNodeIDsInt = new HashSet<Integer>();
										for(Entry<Integer, Pair<Integer, Integer>> edgeElement : graphSentenceMapEntryInt.getValue().getAllEdges().entrySet()) {
											if(edgeElement != null && edgeElement.getValue() != null &&
													edgeElement.getValue().getLeft() != null && edgeElement.getValue().getRight() != null) {
												sentenceGraphNodeIDsInt.add(edgeElement.getValue().getLeft());
												sentenceGraphNodeIDsInt.add(edgeElement.getValue().getRight());
											}
										}

										// 2) Check if for some of these nodes there is a merged node id with the id of the effect node (effectNodeID)
										for(Integer sentenceGraphNodeIDsIntElem : sentenceGraphNodeIDsInt) {
											if(sentenceGraphNodeIDsIntElem != null) {
												Map<Integer, String> elemNodeIDmap = graphSentenceMapEntryInt.getValue().getMergedIDmap(sentenceGraphNodeIDsIntElem);
												if(elemNodeIDmap != null && elemNodeIDmap.containsKey(effectNodeID)) {

													// The node with ID sentenceGraphNodeIDsIntElem is the effect node of the causal relation under analysis
													// that has a cause node in the sentence under analysis

													Integer effectNodeIDinSentGraph = sentenceGraphNodeIDsIntElem;

													// Look for the document graph (retGraph) node ID corresponding to this effect node

													Integer causeNodeIDinDocGraph = addDocumentGraphNode(retGraph, graphSentenceMapEntry.getValue(), causeNodeIDinSentGraph);
													Integer effectNodeIDinDocGraph = addDocumentGraphNode(retGraph, graphSentenceMapEntryInt.getValue(), effectNodeIDinSentGraph);

													if(causeNodeIDinDocGraph != null && effectNodeIDinDocGraph != null) {
														// Add edge
														addDocumentGraphEdge(retGraph, causeNodeIDinDocGraph, effectNodeIDinDocGraph, "CAUSE");

														logger.info("Added cross-sentence causal relation:\n" + 
																" CAUSE NODE: " + ((retGraph.getNodeName(causeNodeIDinDocGraph) != null) ? retGraph.getNodeName(causeNodeIDinDocGraph) : "NULL" ) + " ID: " + causeNodeIDinDocGraph + "\n" +
																" EFFECT NODE: " + ((retGraph.getNodeName(effectNodeIDinDocGraph) != null) ? retGraph.getNodeName(effectNodeIDinDocGraph) : "NULL" ) + " ID: " + effectNodeIDinDocGraph	);

														break;
													}
													else {
														logger.info("Error while adding cross-sentence causal relation");
													}
												}
											}
										}

									}
								}

							}
						}

					}
				}


			}
		}
		// Adding cross-sentence causal edges - END

		return retGraph;
	}

	/**
	 * Given a node ID, returns the IDs of all coordinate nodes
	 * 
	 * @param sentGraph
	 * @param nodeID
	 * @return
	 */
	private static Set<Integer> getNodeCoordinate(DependencyGraph sentGraph, Integer nodeID, String POSstartsWith) {
		Set<Integer> retCoordinateNodes = new HashSet<Integer>();
		Map<Integer, Pair<Integer, Integer>> incomingCOORDedges = sentGraph.getIncidentEdges(nodeID, "COORD");
		for(Entry<Integer, Pair<Integer, Integer>> elem : incomingCOORDedges.entrySet()) {
			if(elem != null && elem.getValue().getLeft() != null && sentGraph.getNodePOS(elem.getValue().getLeft()) != null) {
				if(sentGraph.getNodePOS(elem.getValue().getLeft()).equals("CC")) {
					// This is a direct coordination: node <-- COORD <-- 'and'
					// Looking for incoming CONJ nodes
					Map<Integer, Pair<Integer, Integer>> incomingCONJedges = sentGraph.getIncidentEdges(elem.getValue().getLeft(), "CONJ");
					for(Entry<Integer, Pair<Integer, Integer>> elemCONJ : incomingCONJedges.entrySet()) {
						if(elemCONJ != null && elemCONJ.getValue().getLeft() != null && sentGraph.getNodePOS(elemCONJ.getValue().getLeft()) != null) {
							if (sentGraph.getNodePOS(elemCONJ.getValue().getLeft()).startsWith(POSstartsWith)) {
								// This is a 'and' based coordination: 'and' <-- CONJ <-- NOUN_NODE
								retCoordinateNodes.add(elemCONJ.getValue().getLeft());
								retCoordinateNodes.addAll(getNodeCoordinate(sentGraph, elemCONJ.getValue().getLeft(), POSstartsWith));
							}
						}
					}

				}
				else if (sentGraph.getNodePOS(elem.getValue().getLeft()).startsWith("N")) {
					// This is a direct coordination: node <-- COORD <-- NOUN_NODE
					retCoordinateNodes.add(elem.getValue().getLeft());
					retCoordinateNodes.addAll(getNodeCoordinate(sentGraph, elem.getValue().getLeft(), POSstartsWith));
				}
			}
		}

		return retCoordinateNodes;
	}

	private static void addDocumentGraphEdge(DependencyGraph docGraph, Integer fromSentGraphNodeID, Integer toSentGraphNodeID, String edgeName) {

		if(docGraph != null && fromSentGraphNodeID != null && toSentGraphNodeID != null) {
			// Check if the edge exists
			boolean edgeExists = false;
			for(Entry<Integer, Pair<Integer, Integer>> edge : docGraph.getAllEdges().entrySet()) {
				if( edge.getKey() != null && edge.getValue() != null && edge.getValue().getLeft() == fromSentGraphNodeID &&
						edge.getValue().getRight() == toSentGraphNodeID && docGraph.getEdgeName(edge.getKey()).equals(edgeName) ) {
					edgeExists = true;
				}
			}

			if(!edgeExists) {
				Integer newEdgeID = docGraph.addEdge(edgeName, fromSentGraphNodeID, toSentGraphNodeID, false);
				// System.out.println("ADDED EDGE FROM: " + finalSourceNodeId + " TO " + finalTargetNodeId +
				//		" WITH ID: " + ((newEdgeID != null) ? newEdgeID : "NULL"));
			}
			else {
				// System.out.println("ALREADY EXISTING EDGE FROM: " + finalSourceNodeId + " TO " + finalTargetNodeId);
			}
		}
		else {
			logger.debug("Error adding Document graph edge.");
		}

	}

	private Integer addDocumentGraphNode(DependencyGraph docGraph, DependencyGraph sentGraph, Integer sentGraphNodeID) {

		Integer returnNodeID = null;

		Integer sentNodeId = sentGraphNodeID;
		Set<Integer> fromNodeCorefChainIDs = sentGraph.getNodeCorefID(sentNodeId);

		// System.out.println("Number of nodes: " + retGraph.getNodeCount());
		if(fromNodeCorefChainIDs == null || fromNodeCorefChainIDs.size() == 0) {
			String nodeName = docGraph.getNodeName(sentNodeId);
			if(nodeName == null) {
				returnNodeID = docGraph.addNode(sentNodeId, sentGraph.getNodeName(sentNodeId), "-", "-", null, null, -1);
				// System.out.println("ADDED SOURCE NODE: " + ((finalSourceNodeId != null) ? finalSourceNodeId : "null"));

				// Set merged node name and merged node ID
				Map<Integer, String> mergedIdMap = sentGraph.getMergedIDmap(sentNodeId);
				if(mergedIdMap != null && mergedIdMap.size() > 0) {
					for(Entry<Integer, String> mergedIdMapEntry : mergedIdMap.entrySet()) {
						if(mergedIdMapEntry != null && mergedIdMapEntry.getKey() != null && mergedIdMapEntry.getValue() != null) {
							docGraph.addToMergedIDmap(returnNodeID, mergedIdMapEntry.getKey(), mergedIdMapEntry.getValue(), false);
						}
					}
				}

				Map<Integer, String> mergedNameMap = sentGraph.getMergedNameMap(sentNodeId);
				if(mergedNameMap != null && mergedNameMap.size() > 0) {
					for(Entry<Integer, String> mergedNameMapEntry : mergedNameMap.entrySet()) {
						if(mergedNameMapEntry != null && mergedNameMapEntry.getKey() != null && mergedNameMapEntry.getValue() != null) {
							docGraph.addToMergedNameMap(returnNodeID, mergedNameMapEntry.getKey(), mergedNameMapEntry.getValue(), false);
						}
					}
				}

				// Set the head word of the document graph node
				Set<String> sentNodeHeadWordSet = sentGraph.getHeadWordsSet(sentNodeId);
				if(sentNodeHeadWordSet != null && sentNodeHeadWordSet.size() > 0) {
					for(String headWord : sentNodeHeadWordSet) {
						if(headWord != null && !headWord.equals("")) {
							docGraph.addHeadWord(returnNodeID, headWord, false);
						}
					}
				}

				// Transfer independent properties of the node:
				//   - indipProp_rhetClass
				if(sentGraph.getRhetoricalClassSet(sentNodeId) != null) {
					for(String rhetClass : sentGraph.getRhetoricalClassSet(sentNodeId)) {
						docGraph.addRhetoricalClass(returnNodeID, new String(rhetClass), true);
					}
				}
				//	 - sentence ID / node list pair
				if(sentGraph.getSentenceIDTokensPair(sentNodeId) != null) {
					docGraph.setSentenceIDTokensPair(returnNodeID, sentGraph.getSentenceIDTokensPair(sentNodeId).getLeft(), sentGraph.getSentenceIDTokensPair(sentNodeId).getRight());
				}
			}
			else {
				returnNodeID = sentNodeId;
				// System.out.println("EXISTING SOURCE NODE: " + ((finalSourceNodeId != null) ? finalSourceNodeId : "null"));
			}
		}
		else {

			for(Integer coreferenceChainIDofToken : fromNodeCorefChainIDs) { // Should be a one element list, since each node is head of only one coreference chain element
				String[] nodeNames = null; // Get all the different names of the node coming from the coreference chain
				if(sentGraph.getNodeCorefName(sentNodeId) != null) {
					nodeNames = sentGraph.getNodeCorefName(sentNodeId).split("___");
				}
				else {
					nodeNames = new String[1];
					nodeNames[0] = sentGraph.getNodeName(sentNodeId);
				}

				String existingWord = docGraph.getNodeName(coreferenceChainIDofToken);
				if(existingWord != null) {
					String[] existingWordSplit = existingWord.split("___");

					Set<String> notFoundNodeNames = new HashSet<String>();

					for(int u = 0; u < nodeNames.length; u++) {
						boolean found = false;
						for(int i = 0; i < existingWordSplit.length; i++) {
							if(existingWordSplit[i].equals(nodeNames[u])) {
								found = true;
								break;
							}
						}
						if(!found) {
							notFoundNodeNames.add(nodeNames[u]);
						}
					}

					if(notFoundNodeNames.size() > 0) {
						for(String notFoundNodeName : notFoundNodeNames) {
							existingWord = docGraph.getNodeName(coreferenceChainIDofToken);
							notFoundNodeName += "___" + existingWord;
							docGraph.changeNodeName(coreferenceChainIDofToken, notFoundNodeName);
						}
					}

					returnNodeID = coreferenceChainIDofToken;
					// System.out.println("EXISTING SOURCE NODE (COREF): " + ((finalSourceNodeId != null) ? finalSourceNodeId : "null"));
				}
				else {
					returnNodeID = docGraph.addNode(coreferenceChainIDofToken, sentGraph.getNodeCorefName(sentNodeId), 
							"-", "-", null, null, -1);
					// System.out.println("ADDED SOURCE NODE (COREF): " + ((finalSourceNodeId != null) ? finalSourceNodeId : "null"));

					// Set head words of the return node - a list of the head words of all the coreference chain elements
					AnnotationSet corefChainAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.coref_ChainAnnSet);
					if(corefChainAnnSet != null) {
						Annotation firstAnnOfCorefChain = corefChainAnnSet.get(coreferenceChainIDofToken);
						if(firstAnnOfCorefChain != null && firstAnnOfCorefChain.getType() != null && !firstAnnOfCorefChain.getType().equals("")) {
							List<Annotation> corefChainAnnotations = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.coref_ChainAnnSet, firstAnnOfCorefChain.getType());
							for(Annotation corefChainAnnotation : corefChainAnnotations) {
								if(corefChainAnnotation != null) {
									Integer headIDfeat = GateUtil.getIntegerFeature(corefChainAnnotation, "headID").orElse(null);
									if(headIDfeat != null) {
										AnnotationSet driAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet);
										if(driAnnSet != null) {
											AnnotationSet tokenAnnSet = driAnnSet.get(ImporterBase.tokenAnnType);
											if(tokenAnnSet != null) {
												Annotation tokenHeadAnn = tokenAnnSet.get(headIDfeat);
												if(tokenHeadAnn != null && 
														GateUtil.getAnnotationText(tokenHeadAnn, this.cacheManager.getGateDoc()).orElse(null) != null &&
														!GateUtil.getAnnotationText(tokenHeadAnn, this.cacheManager.getGateDoc()).orElse(null).equals("")) {
													docGraph.addHeadWord(returnNodeID, GateUtil.getAnnotationText(tokenHeadAnn, this.cacheManager.getGateDoc()).orElse(""), false);
												}
											}
										}
									}
								}
							}
						}
					}

				}

				// Transfer independent properties of the node (in case the coref node already exists, add new values for some of these properties):
				//   - indipProp_rhetClass
				if(sentGraph.getRhetoricalClassSet(sentNodeId) != null) {
					for(String rhetClass : sentGraph.getRhetoricalClassSet(sentNodeId)) {
						docGraph.addRhetoricalClass(returnNodeID, new String(rhetClass), false);
					}
				}
			}

		}

		return returnNodeID;
	}

	@Override
	public Header extractHeader() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Prerequisites
		this.parsingHeader(false);

		return ObjectGenerator.getHeaderFromDocument(cacheManager.getGateDoc(), cacheManager);
	}

	@Override
	public List<Citation> extractCitations() throws InternalProcessingException {

		if(this.cacheManager.getGateDoc() == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Check if already extracted sentences, performed dep parsing and rhetorically annotated sentences
		if(this.cacheManager.getGateDoc().getFeatures() == null) {
			this.cacheManager.getGateDoc().setFeatures(gate.Factory.newFeatureMap());
		}

		// Prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingCitations_Enrich(false);

		List<Citation> retCitationList = new ArrayList<Citation>();

		List<Annotation> bibEntryList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);

		for(Annotation bibEntry : bibEntryList) {
			Citation cit = ObjectGenerator.getCitationFromBibEntry(bibEntry, cacheManager);

			if(cit != null) {
				retCitationList.add(cit);
			}
		}

		return retCitationList;
	}

	// Utility methods
	public void parsingHeader(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Checking doc type
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
			logger.info("It is not possible to parse the header of a plain text.");
			return;
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableHeaderParsing()) {
			logger.info("Parsing Header module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String headerAnalysisFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.headerAnalysisFlagKey).orElse(null);
		if(!force && headerAnalysisFlagCHECK != null && headerAnalysisFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Parsing Header - START...");

		// Reset dependent annotations and flags:
		this.resetHeader();

		// Check prerequisites: NONE
		this.parsingSentences(false);

		gate.Document headerDoc = null;

		// Get plain text header contents
		Optional<String> headerGATEdocStr = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), ImporterBase.headerDOC_OrigDocFeat);

		if(headerGATEdocStr.isPresent()) {
			long startProcess = System.currentTimeMillis();
			String headerStr = headerGATEdocStr.get();
			String[] headerStrSplit = headerStr.split("<NL>");
			List<String> headerStrList = Arrays.asList(headerStrSplit);
			headerDoc = GateUtil.formListStrToGateDoc(headerStrList);

			Corpus corpusToProcess = new CorpusImpl();

			corpusToProcess.add(headerDoc);

			synchronized(LOCK_corpusController_XGAPPheader) {
				startProcess = System.currentTimeMillis();
				corpusController_XGAPPheader.setCorpus(corpusToProcess);
				try {
					corpusController_XGAPPheader.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while procesing header");
				}
				corpusController_XGAPPheader.setCorpus(null);
			}

			synchronized(LOCK_HeaderAnalyzer_Resource) {
				HeaderAnalyzer_Resource.setDocument(headerDoc);
				HeaderAnalyzer_Resource.setOriginalDocument(this.cacheManager.getGateDoc());
				try {
					HeaderAnalyzer_Resource.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while analyzing header");
				}
				HeaderAnalyzer_Resource.setDocument(null);
				HeaderAnalyzer_Resource.setOriginalDocument(null);
			}
			logger.info("Parsing Header - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");

			headerDoc.cleanup();
			Factory.deleteResource(headerDoc);

			corpusToProcess.clear();
			corpusToProcess.cleanup();

		}
		else {
			logger.info("ParsingHeader - NO HEADER SECTION IDENTIFIED");
		}

		System.gc();

		// Set headerAnalysisFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.headerAnalysisFlagKey, "true");

		logger.info("Parsing Header - END.");
	}

	public void resetHeader() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_HeaderAnalyzer_Resource) {
				HeaderAnalyzer_Resource.setOriginalDocument(this.cacheManager.getGateDoc());
				HeaderAnalyzer_Resource.resetAnnotations();
				HeaderAnalyzer_Resource.setOriginalDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Header annotations", e, logger);
		}

		System.gc();

		// Reset headerAnalysisFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.headerAnalysisFlagKey, "false");

		logger.debug("Reset Header parsing results");
	}

	public void parsingSentences(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		String sentenceExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.sentenceExtracionFlagKey).orElse(null);
		if(!force && sentenceExtracionFlagCHECK != null && sentenceExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract Sentences - START...");

		// Reset dependent annotations and flags:
		// The DOCUMENT RESET of STEP 1 delete all annotations of driAnnSet
		this.resetDocumentExtractionData();

		// Check prerequisites: NONE

		Corpus corpusToProcess = new CorpusImpl();

		corpusToProcess.add(this.cacheManager.getGateDoc());

		long startProcess = System.currentTimeMillis();
		synchronized(LOCK_corpusController_preprocess_XGAPPpreprocStep1) {
			startProcess = System.currentTimeMillis();
			corpusController_preprocess_XGAPPpreprocStep1.setCorpus(corpusToProcess);
			try {
				corpusController_preprocess_XGAPPpreprocStep1.execute();
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting sentences (step 1)");
			}
			corpusController_preprocess_XGAPPpreprocStep1.setCorpus(null);
		}

		AnnotationSet inputPDFX = this.cacheManager.getGateDoc().getAnnotations(ImporterPDFX.PDFXAnnSet);
		AnnotationSet inputPDFEXT = this.cacheManager.getGateDoc().getAnnotations(ImporterPDFEXT.PDFEXTAnnSet);
		AnnotationSet inputGROBID = this.cacheManager.getGateDoc().getAnnotations(ImporterGROBID.GROBIDannSet);
		AnnotationSet inputJATS = this.cacheManager.getGateDoc().getAnnotations(ImporterJATS.JATSannSet);

		if(inputPDFX != null && (inputPDFX.get("pdfx") != null && inputPDFX.get("pdfx").size() > 0)) {
			synchronized(LOCK_PDFXimporter_Resource) {
				PDFXimporter_Resource.setDocument(this.cacheManager.getGateDoc());
				try {
					PDFXimporter_Resource.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while extracting sentences (PDFX sanitizing)");
				}
				PDFXimporter_Resource.setDocument(null);
			}
		}
		else if (inputPDFEXT != null && 
				(inputPDFEXT.get("root") != null && inputPDFEXT.get("root").size() > 0) &&
				(
						(inputPDFEXT.get("title") != null && inputPDFEXT.get("title").size() > 0) ||
						(inputPDFEXT.get("abstract_text") != null && inputPDFEXT.get("abstract_text").size() > 0) ||
						(inputPDFEXT.get("reference") != null && inputPDFEXT.get("reference").size() > 0))
				) {
			synchronized(LOCK_PDFEXTimporter_Resource) {
				PDFEXTimporter_Resource.setDocument(this.cacheManager.getGateDoc());
				try {
					PDFEXTimporter_Resource.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while extracting sentences (PDFEXT sanitizing)");
				}
				PDFEXTimporter_Resource.setDocument(null);
			}
		}
		else if (inputGROBID != null && 
				(inputGROBID.get("TEI") != null && inputJATS.get("TEI").size() > 0)) {
			synchronized(LOCK_GROBIDimporter_Resource) {
				GROBIDimporter_Resource.setDocument(this.cacheManager.getGateDoc());
				try {
					GROBIDimporter_Resource.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while extracting sentences (GROBID sanitizing)");
				}
				GROBIDimporter_Resource.setDocument(null);
			}
		}
		else if (inputJATS != null && 
				(inputJATS.get("article-title") != null && inputJATS.get("article-title").size() > 0) &&
				(inputJATS.get("article") != null && inputJATS.get("article").size() > 0)) {
			synchronized(LOCK_JATSimporter_Resource) {
				JATSimporter_Resource.setDocument(this.cacheManager.getGateDoc());
				try {
					JATSimporter_Resource.execute();
				} catch (ExecutionException e) {
					logger.warn("Exception: " + e.getMessage());
					e.printStackTrace();
					throw new InternalProcessingException("Error while extracting sentences (JATS sanitizing)");
				}
				JATSimporter_Resource.setDocument(null);
			}
		}
		else { 
			// Plain text
			this.cacheManager.getGateDoc().setFeatures((this.cacheManager.getGateDoc().getFeatures() != null) ? this.cacheManager.getGateDoc().getFeatures() : Factory.newFeatureMap());
			this.cacheManager.getGateDoc().getFeatures().put("source", SourceENUM.PLAIN_TEXT.toString());
		}

		synchronized(LOCK_corpusController_preprocess_XGAPPpreprocStep2) {
			corpusController_preprocess_XGAPPpreprocStep2.setCorpus(corpusToProcess);
			try {
				corpusController_preprocess_XGAPPpreprocStep2.execute();
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting sentences (step 2)");
			}
			corpusController_preprocess_XGAPPpreprocStep2.setCorpus(null);
		}

		synchronized(LOCK_LanguageDetector_Resource) {
			if(edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableMultiLangSupport()) {
				LanguageDetector_Resource.setEnableLangDetect("true");
			}
			else {
				LanguageDetector_Resource.setEnableLangDetect("false");
			}

			LanguageDetector_Resource.setEnableMajorityVoting("true");

			boolean enableLangDetect = (LanguageDetector_Resource.getEnableLangDetect() != null && LanguageDetector_Resource.getEnableLangDetect().toLowerCase().equals("true")) ? true : false;

			// Detect language of title(s) - one language tag assigned independently to each title
			try {
				AnnotationSet titleAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.titleAnnType);

				AnnotationSet H1AnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h1AnnType);
				AnnotationSet H2AnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h2AnnType);
				AnnotationSet H3AnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h3AnnType);
				AnnotationSet H4AnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h4AnnType);
				AnnotationSet H5AnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.h5AnnType);

				AnnotationSet captionAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.captionAnnType);
				AnnotationSet contribGroupAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get("contrib-group");

				AnnotationSet keywordPDFEXTAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterPDFEXT.PDFEXTAnnSet).get(ImporterPDFEXT.PDFEXTkeywordsText);
				AnnotationSet affiliationsPDFEXTAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterPDFEXT.PDFEXTAnnSet).get(ImporterPDFEXT.PDFEXTaffiliation);

				List<Annotation> annotationToDetectLang = new ArrayList<Annotation>();
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(titleAnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(H1AnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(H2AnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(H3AnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(H4AnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(H5AnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(captionAnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(contribGroupAnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(keywordPDFEXTAnnSet));
				annotationToDetectLang.addAll(gate.Utils.inDocumentOrder(affiliationsPDFEXTAnnSet));

				if(annotationToDetectLang != null && annotationToDetectLang.size() > 0) {
					for(Annotation annToDetLang : annotationToDetectLang) {
						if(annToDetLang != null) {
							List<Annotation> annToDetectLang = new ArrayList<Annotation>();
							annToDetectLang.add(annToDetLang);

							try {
								LanguageDetector.detectLanguage(this.cacheManager.getGateDoc(), annToDetectLang, LanguageDetector_Resource.getOutputLangFeatureName(),
										enableLangDetect, false);
							}
							catch(Exception e) {
								logger.warn("Exception: " + e.getMessage());
								e.printStackTrace();
								throw new InternalProcessingException("Error while detecting language of title / header");
							}
						}
					}
				}

			}
			catch(Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while detecting language of titles / headers.");
			}

			// Detect language of abstract(s) - the same language tag (majority) assigned to all sentences of abstract
			long lastAbstractId = 0l;
			try {
				AnnotationSet abstractsAnn = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.abstractAnnType);
				if(abstractsAnn != null && abstractsAnn.size() > 0) {
					Iterator<Annotation> abstractsAnnIter = abstractsAnn.iterator();
					while(abstractsAnnIter.hasNext()) {
						Annotation abstractAnn = abstractsAnnIter.next();
						if(abstractAnn != null) {
							if(lastAbstractId < abstractAnn.getEndNode().getOffset()) {
								lastAbstractId = abstractAnn.getEndNode().getOffset();
							}

							List<Annotation> abstractSentenceList = GateUtil.getAnnInDocOrderIntersectAnn(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractAnn);
							
							try {
								LanguageDetector.detectLanguage(this.cacheManager.getGateDoc(), abstractSentenceList, LanguageDetector_Resource.getOutputLangFeatureName(),
										enableLangDetect, true);
							}
							catch(Exception e) {
								logger.warn("Exception: " + e.getMessage());
								e.printStackTrace();
								throw new InternalProcessingException("Error while detecting language of abstract");
							}
						}
					}
				}

			}
			catch(Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while detecting language of abstracts");
			}

			// Detect language of main body - the same language tag (majority) assigned to all sentences of abstract
			List<Annotation> bodySentenceList = GateUtil.getAnnInDocOrderIntersectOffset(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, lastAbstractId, gate.Utils.lengthLong(this.cacheManager.getGateDoc()));
			try {
				LanguageDetector.detectLanguage(this.cacheManager.getGateDoc(), bodySentenceList, LanguageDetector_Resource.getOutputLangFeatureName(),
						enableLangDetect, true);
			}
			catch(Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while detecting language of main body");
			}
		}

		logger.info("Extract Sentences - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set sentenceExtracionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.sentenceExtracionFlagKey, "true");

		logger.info("Extract Sentences - END.");
	}

	public void parsingCitations_Spot(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Checking doc type
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
			logger.info("It is not possible to extract citations from a plain text (citation spot).");
			return;
		}

		String citationExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.citationSpotFlagKey).orElse(null);
		if(!force && citationExtracionFlagCHECK != null && citationExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Analyze Citations (Spot) - START...");

		// Reset dependent annotations and flags:
		this.resetCitations_Spot();
		this.resetCitations_Link();
		this.resetDep();
		this.resetCoref();
		this.resetCausality();
		this.resetBabelNet();
		this.resetTerminology();
		this.resetRhetoricalClass();

		// Check prerequisites:
		this.parsingSentences(false);

		Corpus corpusToProcess = new CorpusImpl();

		corpusToProcess.add(this.cacheManager.getGateDoc());

		long startProcess = System.currentTimeMillis();
		synchronized(LOCK_corpusController_XGAPPcitMarker) {
			startProcess = System.currentTimeMillis();
			corpusController_XGAPPcitMarker.setCorpus(corpusToProcess);
			try {
				corpusController_XGAPPcitMarker.execute();
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting candidate citations");
			}
			corpusController_XGAPPcitMarker.setCorpus(null);
		}

		synchronized(LOCK_CitationSanitizer_Resource) {
			CitationSanitizer_Resource.setDocument(this.cacheManager.getGateDoc());
			try {
				CitationSanitizer_Resource.execute();
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while sanitizing citations");
			}
			CitationSanitizer_Resource.setDocument(null);
		}
		logger.info("Analyze Citations (Spot) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set citationExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationSpotFlagKey, "true");

		logger.info("Analyze Citations (Spot) - END.");
	}

	public void resetCitations_Spot() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			// Reset: MarkerCitaiton, CandidateInlineCitaiton and CandidateInlineCitaitonMarker
			// Delete annotations imported from PDFX
			List<String> annTypesToRemove = new ArrayList<String>();
			annTypesToRemove.add("MarkerCitaiton");
			annTypesToRemove.add(edu.upf.taln.dri.lib.Factory.inputCandidateInlineCitationAStype_citationSanitizerResource);
			annTypesToRemove.add(edu.upf.taln.dri.lib.Factory.inputCandidateInlineCitationMarkerAStype_citationSanitizerResource);

			for(String annTypeToRemove : annTypesToRemove) {
				List<Annotation> annListToRem = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, annTypeToRemove);

				if(annListToRem != null && annListToRem.size() > 0) {
					for(Annotation annToRem : annListToRem) {
						if(annToRem != null) {
							this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
						}
					}
				}
			}

			synchronized(LOCK_CitationSanitizer_Resource) {
				CitationSanitizer_Resource.setDocument(this.cacheManager.getGateDoc());
				CitationSanitizer_Resource.resetAnnotations();
				CitationSanitizer_Resource.setDocument(null);
			}

		} catch (Exception e) {
			Util.notifyException("Resetting Citation (Spot) annotations", e, logger);
		}

		System.gc();

		// Reset citationSpotAndLinkFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationSpotFlagKey, "false");

		logger.debug("Reset Citations (Spot) parsing results");
	}

	public void parsingCitations_Link(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Checking doc type
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
			logger.info("It is not possible to extract citations from a plain text (citation link).");
			return;
		}

		String citationExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.citationLinkFlagKey).orElse(null);
		if(!force && citationExtracionFlagCHECK != null && citationExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Analyze Citations (Link) - START...");

		// Reset dependent annotations and flags:
		this.resetCitations_Link();

		// Check prerequisites:
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);

		Corpus corpusToProcess = new CorpusImpl();

		corpusToProcess.add(this.cacheManager.getGateDoc());

		synchronized(LOCK_CitationLinker_Resource) {
			long startProcess = System.currentTimeMillis();
			CitationLinker_Resource.setDocument(this.cacheManager.getGateDoc());
			try {
				CitationLinker_Resource.execute();
				logger.info("Analyze Citations (Link) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while linking citation to inline markers");
			}
			CitationLinker_Resource.setDocument(null);
		}

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set citationExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationLinkFlagKey, "true");

		logger.info("Analyze Citations (Link) - END.");
	}

	public void resetCitations_Link() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_CitationLinker_Resource) {
				CitationLinker_Resource.setDocument(this.cacheManager.getGateDoc());
				CitationLinker_Resource.resetAnnotations();
				CitationLinker_Resource.setDocument(null);
			}

		} catch (Exception e) {
			Util.notifyException("Resetting Citation (Link and Spot) annotations", e, logger);
		}

		System.gc();

		// Reset citationSpotAndLinkFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationLinkFlagKey, "false");

		logger.debug("Reset Citations (Link) parsing results");
	}

	public void parsingCitations_Enrich(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Checking doc type
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
			logger.info("It is not possible to extract citations from a plain text (citation enrichment).");
			return;
		}

		// If JATS source, citations are not analyzed / enriched
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.JATS)) {
			logger.info("Citations elements already identified in JATS documents (citation enrich).");
			return;
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableBibEntryParsing()) {
			logger.info("Analyze Citations (Enrich) module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String citationExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.citationEnrichFlagKey).orElse(null);
		if(!force && citationExtracionFlagCHECK != null && citationExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Analyze Citations (Enrich) - START...");

		// Reset dependent annotations and flags:
		this.resetCitations_Enrich();

		// Check prerequisites:
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);

		synchronized(LOCK_CitationExpander_Resource) {
			long startProcess = System.currentTimeMillis();
			CitationExpander_Resource.setDocument(this.cacheManager.getGateDoc());
			try {
				CitationExpander_Resource.execute();
				logger.info("Analyze Citations (Enrich) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while parsing citation info");
			}
			CitationExpander_Resource.setDocument(null);
		}

		System.gc();

		// Set citationExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationEnrichFlagKey, "true");

		logger.info("Analyze Citations (Enrich) - END.");

	}

	public void resetCitations_Enrich() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_CitationExpander_Resource) {
				CitationExpander_Resource.setDocument(this.cacheManager.getGateDoc());
				CitationExpander_Resource.resetAnnotations();
				CitationExpander_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Citation (Enrich) annotations", e, logger);
		}

		System.gc();

		// Reset citationSpotAndLinkFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationEnrichFlagKey, "false");

		logger.debug("Reset Citations (Enrich) parsing results");
	}

	public void parsingDep(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableGraphParsing()) {
			logger.info("Extract Graph module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String depExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.graphExtractionFlagKey).orElse(null);
		if(!force && depExtracionFlagCHECK != null && depExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract Graph - START...");

		// Reset dependent annotations and flags:
		this.resetDep();
		this.resetRhetoricalClass();
		this.resetCoref();
		this.resetCausality();
		this.resetBabelNet();
		this.resetTerminology();

		// Check prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		// this.parsingCitations_Enrich(false); - NOT A PREREQUISITE

		synchronized(LOCK_MateParser_Resource) {
			long startProcess = System.currentTimeMillis();

			try {

				boolean isLangAware = edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableMultiLangSupport();

				// STEP 1: Abstract(s) sentence parsing
				AnnotationSet abstractsAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.abstractAnnType);
				long abstractEndOffset = 0l;
				for(Iterator<Annotation> iter = abstractsAnnSet.iterator(); iter.hasNext(); ) {
					Annotation abstractAnn = iter.next();
					if(abstractAnn != null) {
						if(abstractAnn.getEndNode().getOffset() > abstractEndOffset) {
							abstractEndOffset = abstractAnn.getEndNode().getOffset();
						}

						List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrderIntersectAnn(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractAnn);

						logger.info("Start parsing " + sentenceAnnList.size() + " sentences from abstract...");
						MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, this.cacheManager.getGateDoc(), sentenceAnnList, 
								ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);

					}

				}


				// STEP 2: Main body sentence parsing
				List<Annotation> mainBodyAnnotationList = GateUtil.getAnnInDocOrderIntersectOffset(this.cacheManager.getGateDoc(), 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractEndOffset, gate.Utils.lengthLong(this.cacheManager.getGateDoc()));
				logger.info("Start parsing " + mainBodyAnnotationList.size() + " sentences from main body...");
				MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, this.cacheManager.getGateDoc(), mainBodyAnnotationList, 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);


				// STEP 3: parse title, section headers, captions and "contrib-group"
				List<String> headerAnnTypes = new ArrayList<String>();
				headerAnnTypes.add(ImporterBase.titleAnnType);
				headerAnnTypes.add(ImporterBase.h1AnnType);
				headerAnnTypes.add(ImporterBase.h2AnnType);
				headerAnnTypes.add(ImporterBase.h3AnnType);
				headerAnnTypes.add(ImporterBase.h4AnnType);
				headerAnnTypes.add(ImporterBase.h5AnnType);
				headerAnnTypes.add(ImporterBase.captionAnnType);
				headerAnnTypes.add("contrib-group");
				for(String headerAnnType : headerAnnTypes) {
					List<Annotation> selectedAnnotations = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, headerAnnType);
					for(Annotation selectedAnn : selectedAnnotations) {
						logger.info("Start parsing " + mainBodyAnnotationList.size() + " sentences from " + headerAnnType + "...");
						MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, this.cacheManager.getGateDoc(), selectedAnn, 
								ImporterBase.driAnnSet, headerAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
					}
				}

				List<Annotation> keywordListAnnotations = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTkeywordsText);
				if(keywordListAnnotations != null && keywordListAnnotations.size() > 0) {
					logger.info("Start parsing " + keywordListAnnotations.size() + " keywords from " + ImporterPDFEXT.PDFEXTAnnSet + "/" + ImporterPDFEXT.PDFEXTkeywordsText + "...");
					for(Annotation keywordAnn : keywordListAnnotations) {
						MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, this.cacheManager.getGateDoc(), keywordAnn, 
								ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTkeywordsText, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
					}
				}

				List<Annotation> affiliationListAnnotations = GateUtil.getAnnInDocOrder(this.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTaffiliation);
				if(affiliationListAnnotations != null && affiliationListAnnotations.size() > 0) {
					logger.info("Start parsing " + affiliationListAnnotations.size() + " affiliations from " + ImporterPDFEXT.PDFEXTAnnSet + "/" + ImporterPDFEXT.PDFEXTaffiliation + "...");
					for(Annotation affilAnn : affiliationListAnnotations) {
						MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, this.cacheManager.getGateDoc(), affilAnn, 
								ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTaffiliation, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
					}
				}


				logger.info("Extract Graph - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting sentence graph (parser)");
			}

		}

		System.gc();

		// Set graphExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.graphExtractionFlagKey, "true");

		logger.info("Extract Graph - END.");
	}

	public void resetDep() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_MateParser_Resource) {
				// Reset is equal independently from the parsing language
				MateParsersLang_Resource.get(LangENUM.EN).setDocument(this.cacheManager.getGateDoc());
				MateParsersLang_Resource.get(LangENUM.EN).resetAnnotations();
				MateParsersLang_Resource.get(LangENUM.EN).setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Graph annotations", e, logger);
		}

		System.gc();

		// Reset graphExtractionFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.graphExtractionFlagKey, "false");

		logger.debug("Reset Graph parsing results");
	}

	public void parsingCoref(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableCoreferenceResolution()) {
			logger.info("Extract Coreference module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String corefExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.coreferenceAnalysisFlagKey).orElse(null);
		if(!force && corefExtracionFlagCHECK != null && corefExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract Coreference - START...");

		// Reset dependent annotations and flags:
		this.resetCoref();
		this.resetCausality();

		// Check prerequisites
		this.parsingDep(false);

		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(this.cacheManager.getGateDoc());

		long startProcess = System.currentTimeMillis();
		synchronized(LOCK_corpusController_XGAPPcorefMentionSpot) {
			startProcess = System.currentTimeMillis();
			try {
				corpusController_XGAPPcorefMentionSpot.setCorpus(corpusToProcess);
				corpusController_XGAPPcorefMentionSpot.execute();
				corpusController_XGAPPcorefMentionSpot.setCorpus(null);
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting sentence graph (coref - mention spot)");
			}
		}

		synchronized(LOCK_CorefChainBuilder_Resource) {
			try {
				CorefChainBuilder_Resource.setDocument(this.cacheManager.getGateDoc());
				CorefChainBuilder_Resource.execute();
				CorefChainBuilder_Resource.setDocument(null);

			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting sentence graph (coref - chain build)");
			}
		}
		logger.info("Extract Coreference - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set graphExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.coreferenceAnalysisFlagKey, "true");

		logger.info("Extract Coreference - END.");
	}

	public void resetCoref() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_CorefChainBuilder_Resource) {
				CorefChainBuilder_Resource.setDocument(this.cacheManager.getGateDoc());
				CorefChainBuilder_Resource.resetAnnotations();
				CorefChainBuilder_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Coreference annotations", e, logger);
		}

		System.gc();

		// Reset coreferenceAnalysisFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.coreferenceAnalysisFlagKey, "false");

		logger.debug("Reset Coreference parsing results");
	}

	public void parsingCausality(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableCausalityParsing()) {
			logger.info("Extract Causality module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String causailtyExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.causalityAnalysisFlagKey).orElse(null);
		if(!force && causailtyExtracionFlagCHECK != null && causailtyExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract Causality - START...");

		// Reset dependent annotations and flags:
		this.resetCausality();

		// Check prerequisites
		this.parsingDep(false);
		this.parsingCoref(false);

		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(this.cacheManager.getGateDoc());

		synchronized(LOCK_corpusController_XGAPPcausality) {
			long startProcess = System.currentTimeMillis();
			corpusController_XGAPPcausality.setCorpus(corpusToProcess);
			try {
				corpusController_XGAPPcausality.execute();
				logger.info("Extract Causality - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting causality");
			}
			corpusController_XGAPPcausality.setCorpus(null);
		}

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set graphExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.causalityAnalysisFlagKey, "true");

		logger.info("Extract Causality - END.");
	}

	public void resetCausality() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			// Delete annotation set
			this.cacheManager.getGateDoc().removeAnnotationSet(ImporterBase.causality_AnnSet);

		} catch (Exception e) {
			Util.notifyException("Resetting Causality annotations", e, logger);
		}

		System.gc();

		// Reset citationSpotAndLinkFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.causalityAnalysisFlagKey, "false");

		logger.debug("Reset Causality parsing results");
	}

	public void parsingBabelNet(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableBabelNetParsing()) {
			logger.info("Disambiguating by BabelNet module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String babelNetExtracionFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.babelNetAnalysisFlagKey).orElse(null);
		if(!force && babelNetExtracionFlagCHECK != null && babelNetExtracionFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Disambiguating by BabelNet - START...");

		// Reset dependent annotations and flags:
		this.resetBabelNet();

		// Check prerequisites
		this.parsingDep(false);

		synchronized(LOCK_BabelnetAnnotator_Resource) {
			try {
				long startProcess = System.currentTimeMillis();
				BabelnetAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());

				boolean isLangAware = edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableMultiLangSupport();

				// STEP 1: Abstract(s) sentence parsing
				AnnotationSet abstractsAnnSet = this.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.abstractAnnType);
				long abstractEndOffset = 0l;
				for(Iterator<Annotation> iter = abstractsAnnSet.iterator(); iter.hasNext(); ) {
					Annotation abstractAnn = iter.next();
					if(abstractAnn != null) {
						if(abstractAnn.getEndNode().getOffset() > abstractEndOffset) {
							abstractEndOffset = abstractAnn.getEndNode().getOffset();
						}

						List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrderIntersectAnn(this.cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractAnn);

						BabelnetAnnotator.languageAwareDisambiguation(isLangAware, BabelnetAnnotator_Resource, this.cacheManager.getGateDoc(),
								sentenceAnnList, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);

					}
				}

				// STEP 2: Main body sentence parsing
				List<Annotation> mainBodyAnnotationList = GateUtil.getAnnInDocOrderIntersectOffset(this.cacheManager.getGateDoc(), 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractEndOffset, gate.Utils.lengthLong(this.cacheManager.getGateDoc()));
				BabelnetAnnotator.languageAwareDisambiguation(isLangAware, BabelnetAnnotator_Resource, this.cacheManager.getGateDoc(), mainBodyAnnotationList, 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);

				logger.info("Disambiguating by BabelNet - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while disambiguating by BabelNet");
			}
		}

		System.gc();

		// Set graphExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.babelNetAnalysisFlagKey, "true");

		logger.info("Disambiguating by BabelNet - END.");
	}

	public void resetBabelNet() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_BabelnetAnnotator_Resource) {
				BabelnetAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());
				BabelnetAnnotator_Resource.resetAnnotations();
				BabelnetAnnotator_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Babelnet annotations", e, logger);
		}

		System.gc();

		// Reset babelNetAnalysisFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.babelNetAnalysisFlagKey, "false");

		logger.debug("Reset Babelnet parsing results");
	}

	public void parsingRhetoricalClass(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		// Checking doc type
		/* ENABLED THE EXTRACTION OF THE RHETORICAL CLASS FROM PLAIN TEXT
		if(this.getSourceDocumentType() != null && this.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
			logger.info("It is not possible to execute rhetorical sentence classification of a plain text.");
			return;
		}
		 */

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableRhetoricalClassification()) {
			logger.info("Sentence rhetorical classification module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String sentenceRhetoricalClassificationFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.sentenceRhetoricalAnnotationFlagKey).orElse(null);
		if(!force && sentenceRhetoricalClassificationFlagCHECK != null && sentenceRhetoricalClassificationFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Sentence rhetorical classification - START...");

		// Reset dependent annotations and flags:
		this.resetRhetoricalClass();

		// Check prerequisites
		this.parsingDep(false);

		synchronized(LOCK_RhetoricalClassifier_Resource) {
			long startProcess = System.currentTimeMillis();
			RhetoricalClassifier_Resource.setDocument(this.cacheManager.getGateDoc());

			try {
				RhetoricalClassifier_Resource.execute();
				logger.info("Sentence rhetorical classification - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while rhetorically classifying sentences");
			}
			RhetoricalClassifier_Resource.setDocument(null);
		}

		System.gc();

		// Set sentenceRhetoricalAnnotationFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.sentenceRhetoricalAnnotationFlagKey, "true");

		logger.info("Sentence rhetorical classification - END.");
	}

	public void resetRhetoricalClass() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_RhetoricalClassifier_Resource) {
				RhetoricalClassifier_Resource.setDocument(this.cacheManager.getGateDoc());
				RhetoricalClassifier_Resource.resetAnnotations();
				RhetoricalClassifier_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting rhetorical classification annotations", e, logger);
		}

		System.gc();

		// Reset sentenceRhetoricalAnnotationFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.sentenceRhetoricalAnnotationFlagKey, "false");

		logger.debug("Reset rhetorical classification parsing results");
	}

	public void parsingTerminology(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableTerminologyParsing()) {
			logger.info("Extract Terminology (candidate terms) module is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String terminologyExtractionFlagCheck= GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.terminologyExtractionFlagKey).orElse(null);
		if(!force && terminologyExtractionFlagCheck != null && terminologyExtractionFlagCheck.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract Terminology (candidate terms) - START...");

		// Reset dependent annotations and flags:
		this.resetTerminology();

		// Check prerequisites
		this.parsingDep(false);

		synchronized(LOCK_TermAnnotator_Resource) {
			long startProcess = System.currentTimeMillis();
			TermAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());

			try {
				TermAnnotator_Resource.execute();
				logger.info("Extract Terminology (candidate terms) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while extracting terminology (candidate terms)");
			}
			TermAnnotator_Resource.setDocument(null);
		}

		System.gc();

		// Set terminologyExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.terminologyExtractionFlagKey, "true");

		logger.info("Extract Terminology (candidate terms) - END.");
	}

	public void resetTerminology() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_TermAnnotator_Resource) {
				TermAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());
				TermAnnotator_Resource.resetAnnotations();
				TermAnnotator_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Terminology annotations", e, logger);
		}

		System.gc();

		// Reset terminologyExtractionFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.terminologyExtractionFlagKey, "false");

		logger.debug("Reset Terminology parsing results");
	}

	public void parsingMetaAnnotations(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		if(!edu.upf.taln.dri.lib.Factory.getModuleConfig().isEnableMetaAnnotationsParsing()) {
			logger.info("Meta-annotations module (projects, funding agencies, ontologies, etc.) is disabled, thus not executed. Change the module configuration of the library to enable it.");
			return;
		}

		String metaannotationsEctractionFlagCheck= GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.metaannotationsExtractionFlagKey).orElse(null);
		if(!force && metaannotationsEctractionFlagCheck != null && metaannotationsEctractionFlagCheck.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Extract (spot and sanitize) Meta-annotations (projects, funding agencies, ontologies, etc.) - START...");

		// Reset dependent annotations and flags:
		this.resetMetaAnnotations();

		// Check prerequisites
		this.parsingSentences(false);
		this.parsingCitations_Spot(false);
		this.parsingCitations_Link(false);
		this.parsingDep(false);

		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(this.cacheManager.getGateDoc());

		synchronized(LOCK_corpusController_XGAPPmetaAnnotator) {
			long startProcess = System.currentTimeMillis();
			corpusController_XGAPPmetaAnnotator.setCorpus(corpusToProcess);
			try {
				corpusController_XGAPPmetaAnnotator.execute();
				logger.info("Spot Meta-annotations (projects, funding agencies, ontologies, etc.) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (ExecutionException e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while spotting Meta-annotations (projects, funding agencies, ontologies, etc.)");
			}
			corpusController_XGAPPmetaAnnotator.setCorpus(null);
		}

		synchronized(LOCK_MetaAnnotator_Resource) {
			long startProcess = System.currentTimeMillis();
			MetaAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());

			try {
				MetaAnnotator_Resource.execute();
				logger.info("Sanitize Meta-annotations (projects, funding agencies, ontologies, etc.) - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while sanitizing Meta-annotations (projects, funding agencies, ontologies, etc.)");
			}
			MetaAnnotator_Resource.setDocument(null);
		}

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set terminologyExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.metaannotationsExtractionFlagKey, "true");

		logger.info("Extract (spot and sanitize) Meta-annotations (projects, funding agencies, ontologies, etc.) - END.");
	}

	public void resetMetaAnnotations() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		try {
			synchronized(LOCK_MetaAnnotator_Resource) {
				MetaAnnotator_Resource.setDocument(this.cacheManager.getGateDoc());
				MetaAnnotator_Resource.resetAnnotations();
				MetaAnnotator_Resource.setDocument(null);
			}
		} catch (Exception e) {
			Util.notifyException("Resetting Meta-annotations (projects, funding agencies, ontologies, etc.)", e, logger);
		}

		System.gc();

		// Reset terminologyExtractionFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.metaannotationsExtractionFlagKey, "false");

		logger.debug("Reset Meta-annotations (projects, funding agencies, ontologies, etc.) parsing results");
	}

	// Utility methods
	public void parsingSummary(boolean force) throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		String summaryAnalysisFlagCHECK = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), edu.upf.taln.dri.lib.Factory.summaryAnalysisFlagKey).orElse(null);
		if(!force && summaryAnalysisFlagCHECK != null && summaryAnalysisFlagCHECK.equalsIgnoreCase("true")) {
			// Not forced and processing results already present in GATE document
			return;
		}

		logger.info("Generating summaries - START...");

		// Reset dependent annotations and flags:
		this.resetSummary();

		// Check prerequisites
		this.parsingDep(false);


		Corpus corpusToProcess = new CorpusImpl();
		corpusToProcess.add(this.cacheManager.getGateDoc());

		synchronized(LOCK_corpusController_LexRanksumm) {
			long startProcess = System.currentTimeMillis();
			LexRankSummarizer_Resource.setDocument(this.cacheManager.getGateDoc());

			try {
				LexRankSummarizer_Resource.execute();
				logger.info("LexRank summary - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Error while generating LexRank summary");
			}
			LexRankSummarizer_Resource.setDocument(null);
		}

		synchronized(LOCK_corpusController_TitleSimsumm) {
			long startProcess = System.currentTimeMillis();
			TitleSimSummarizer_Resource.setDocument(this.cacheManager.getGateDoc());

			try {
				TitleSimSummarizer_Resource.execute();
				logger.info("Title Similarity summary - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
			} catch (Exception e) {
				logger.warn("Exception: " + e.getMessage());
				e.printStackTrace();
				throw new InternalProcessingException("Title Similarity summary");
			}
			TitleSimSummarizer_Resource.setDocument(null);
		}

		corpusToProcess.clear();
		corpusToProcess.cleanup();

		System.gc();

		// Set terminologyExtractionFlag
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.summaryAnalysisFlagKey, "true");

		logger.info("Generating summaries - END.");
	}

	public void resetSummary() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		synchronized(LOCK_corpusController_LexRanksumm) {
			LexRankSummarizer_Resource.setDocument(this.cacheManager.getGateDoc());
			try {
				LexRankSummarizer_Resource.resetAnnotations();
			} catch (Exception e) {
				Util.notifyException("Resetting LexRank summary annotations", e, logger);
			}
			LexRankSummarizer_Resource.setDocument(null);
		}

		synchronized(LOCK_corpusController_TitleSimsumm) {
			TitleSimSummarizer_Resource.setDocument(this.cacheManager.getGateDoc());
			try {
				TitleSimSummarizer_Resource.execute();
			} catch (Exception e) {
				Util.notifyException("Resetting TitleSimilarity summary annotations", e, logger);
			}
			TitleSimSummarizer_Resource.setDocument(null);
		}
		
		System.gc();

		// Reset summaryAnalysisFlagKey
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.summaryAnalysisFlagKey, "false");

		logger.debug("Reset summary results");
	}

	@Override
	public void resetDocumentExtractionData() throws InternalProcessingException {

		if(this.cacheManager == null) {
			throw new InternalProcessingException("No document loaded (data cleande up: " + cleanedUp + ")");
		}

		logger.info("Reset Document Extraction Data - START");

		try {
			// Reset parsing sentences - delete Sentence and token annotations in Analysis
			List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType);
			Set<Integer> annIdToDel = new HashSet<Integer>();
			for(Annotation sentenceAnn : sentenceAnnList) {
				if(sentenceAnn != null) {
					List<Annotation> tokenOfSentence = GateUtil.getAnnInDocOrderContainedAnn(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sentenceAnn);
					for(Annotation tokenAnn : tokenOfSentence) {
						if(tokenAnn != null) {
							annIdToDel.add(tokenAnn.getId());
						}
					}
					annIdToDel.add(sentenceAnn.getId());
				}
			}
			annIdToDel.stream().forEach((annId) -> {
				Annotation annToDel = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annId);
				if(annToDel != null) {
					cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).remove(annToDel);
				}
			});
		} catch (Exception e) {
			Util.notifyException("Resetting Sentence annotations", e, logger);
		}

		// Remove citation annotations
		this.resetCitations_Spot();
		this.resetCitations_Link();
		this.resetCitations_Enrich();

		// Remove rhetorical classifier annotations
		this.resetRhetoricalClass();

		// Remove parser annotations
		this.resetDep();

		// Remove co-reference chain builder annotations
		this.resetCoref();

		// Remove causality
		this.resetCausality();

		// Remove BabelNet annotations
		this.resetBabelNet();

		// Remove candidate term annotations
		this.resetTerminology();

		// Remove header annotations
		this.resetHeader();

		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.sentenceExtracionFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationSpotFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.citationEnrichFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.graphExtractionFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.sentenceRhetoricalAnnotationFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.terminologyExtractionFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.headerAnalysisFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.coreferenceAnalysisFlagKey, "false");
		this.cacheManager.getGateDoc().getFeatures().put(edu.upf.taln.dri.lib.Factory.babelNetAnalysisFlagKey, "false");

		this.cacheManager.clearCache();

		logger.info("Reset Document Extraction Data - STOP");
	}

	@Override
	public SourceENUM getSourceDocumentType() throws InternalProcessingException {
		String sourceDocFeature = GateUtil.getStringFeature(this.cacheManager.getGateDoc(), "source").orElse(null);
		if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.PLAIN_TEXT.toString())) {
			return SourceENUM.PLAIN_TEXT;
		}
		else if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.PDFX.toString())) {
			return SourceENUM.PDFX;
		}
		else if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.PDFEXT.toString())) {
			return SourceENUM.PDFEXT;
		}
		else if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.GROBID.toString())) {
			return SourceENUM.GROBID;
		}
		else if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.JATS.toString())) {
			return SourceENUM.JATS;
		}
		else {
			return SourceENUM.UNDEFINED;
		}
	}

	@Override
	public void cleanUp() throws InternalProcessingException {
		String docTitle = (this.cacheManager != null && this.cacheManager.getGateDoc() != null && this.cacheManager.getGateDoc().getName() != null) ? new String(this.cacheManager.getGateDoc().getName()) : "---";

		// Cleanup code
		this.cacheManager.cleanUp();
		this.cacheManager = null;

		System.gc();

		cleanedUp = true;

		logger.info("Cleaned up document: " + docTitle);
	}

	@Override
	public boolean isCleanUp() throws InternalProcessingException {
		return new Boolean(cleanedUp);
	}

}
