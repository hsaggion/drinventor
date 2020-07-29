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
package edu.upf.taln.dri.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.loader.GROBIDloaderImpl;
import edu.upf.taln.dri.lib.loader.JATSloader;
import edu.upf.taln.dri.lib.loader.JATSloaderImpl;
import edu.upf.taln.dri.lib.loader.PDFXloaderImpl;
import edu.upf.taln.dri.lib.loader.PDFloader;
import edu.upf.taln.dri.lib.loader.PlainTextLoader;
import edu.upf.taln.dri.lib.loader.PlainTextLoaderImpl;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.LangENUM;
import edu.upf.taln.dri.lib.util.ModuleConfig;
import edu.upf.taln.dri.lib.util.PDFtoTextConvMethod;
import edu.upf.taln.dri.lib.util.PropertyManager;
import edu.upf.taln.dri.module.babelnet.BabelnetAnnotator;
import edu.upf.taln.dri.module.citation.BiblioEntryParser;
import edu.upf.taln.dri.module.citation.CitationLinker;
import edu.upf.taln.dri.module.citation.InlineCitationSpotter;
import edu.upf.taln.dri.module.coref.CorefChainBuilder;
import edu.upf.taln.dri.module.header.HeaderAnalyzer;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.jats.ImporterJATS;
import edu.upf.taln.dri.module.importer.pdf.ImporterGROBID;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFX;
import edu.upf.taln.dri.module.languageDetector.LanguageDetector;
import edu.upf.taln.dri.module.metaannotations.MetaAnnotator;
import edu.upf.taln.dri.module.parser.MateParser;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder.SubjectivityReader;
import edu.upf.taln.dri.module.summary.lexrank.LexRankSummarizer;
import edu.upf.taln.dri.module.summary.titlesim.TitleSimSummarizer;
import edu.upf.taln.dri.module.summary.util.similarity.StopWordList;
import edu.upf.taln.dri.module.summary.util.similarity.WikipediaLemmaPOSfFrequency;
import edu.upf.taln.dri.module.terminology.TermAnnotator;
import gate.CorpusController;
import gate.FeatureMap;
import gate.Gate;
import gate.util.GateException;
import gate.util.SimpleFeatureMapImpl;

/**
 * Factory class to get the instance of PDFimporter by the interface {@link edu.upf.taln.dri.lib.loader.PDFloader PDFimporter} and 
 * to get instances of new Documents by the interface {@link edu.upf.taln.dri.lib.model.Document Document}s.
 * 
 *
 */
public class Factory {

	private static Logger logger = Logger.getLogger(Factory.class);

	private static PDFloader PDFloaderObj = null;
	private static JATSloader JATSloaderObj = null;
	private static PlainTextLoader plainTextLoaderObj = null;
	
	private static PDFtoTextConvMethod PDFtoTextConverter = PDFtoTextConvMethod.PDFX;
	private static boolean enableSentenceParsing = true;
	private static ModuleConfig currentModuleConfig = new ModuleConfig();

	// Management of resources
	private static boolean isProductionProp = false;
	private static String versionProp = null;
	private static boolean initialized = false;
	public static final String DRIconfigPathPropertyName = "DRIconf";
	private static String DRIconfigFileFullPath = null;
	private static String DRIresourceFolderFullPath = null;
	private static final String GATEhomeFolder = "gate_home";
	private static final String GATEpluginsFolder = "plugins";
	private static final String GATELexiconsFolder = "lexicons";
	private static final String GATEModelsFolder = "models";
	private static final String GATEConvertersFolder = "converters";
	private static final String GATETextSimFolder = "textsim";
	
	public static String GROBIDhome = null;
	public static String GROBIDproperties = null;
	
	private static String baseModelPath = File.separator + GATEModelsFolder;

	// Flags of processing actions
	public static final String sentenceExtracionFlagKey = "FL_sentenceExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.sentenceExtracted");
	public static final String citationSpotFlagKey = "FL_citationSpotExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.citationSpotExtracted");
	public static final String citationLinkFlagKey = "FL_citationLinkExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.citationLinkExtracted");
	public static final String citationEnrichFlagKey = "FL_citationEnrichExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.citationEnrichExtracted");
	public static final String graphExtractionFlagKey = "FL_dependencyTreeExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.dependencyTreeExtracted");
	public static final String sentenceRhetoricalAnnotationFlagKey = "FL_sentenceRhetoricalAnnotationExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.sentenceRhetoricalAnnotationExtracted");
	public static final String terminologyExtractionFlagKey = "FL_terminologyExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.terminologyExtracted");
	public static final String metaannotationsExtractionFlagKey = "FL_metaannotationsExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.metaannotationsExtracted");
	public static final String headerAnalysisFlagKey = "FL_headerInfoExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.headerInfoExtracted");
	public static final String coreferenceAnalysisFlagKey = "FL_coreferenceInfoExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.coreferenceInfoExtracted");
	public static final String causalityAnalysisFlagKey = "FL_causalityInfoExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.causalityInfoExtracted");
	public static final String babelNetAnalysisFlagKey = "FL_babelNetInfoExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.babelNetInfoExtracted");
	public static final String summaryAnalysisFlagKey = "FL_summaryExtracted_" + edu.upf.taln.dri.lib.Factory.getFlagProperty("version.summaryExtracted");
	
	// GATE plugins initialization parameters
	private static String inputASname_PDFXimporter = ImporterBase.driAnnSet;
	private static String inputAStype_PDFXimporter = "Sentence";

	private static String inputASname_PDFEXTimporter = ImporterBase.driAnnSet;
	private static String inputAStype_PDFEXTimporter = "Sentence";
	
	private static String inputASname_GROBIDimporter = ImporterBase.driAnnSet;
	private static String inputAStype_GROBIDimporter = "Sentence";

	private static String inputASname_JATSimporter = "Analysis";
	private static String inputAStype_JATSimporter = "Sentence";

	private static String sentenceAnnotationSetToAnalyze_allLang_MateParser = ImporterBase.driAnnSet;
	private static String sentenceAnnotationTypeToAnalyze_allLang_MateParser = ImporterBase.sentenceAnnType;
	private static String tokenAnnotationSetToAnalyze_allLang_MateParser = ImporterBase.driAnnSet;
	private static String tokenAnnotationTypeToAnalyze_allLang_MateParser = ImporterBase.tokenAnnType;
	private static Integer excludeThreshold_allLang_MateParser = 120; // Originally set equal to 80
	private static Boolean citancesEnabled_allLang = true;
	private static String citeSpanAnnotationSetToExclude_allLang = ImporterBase.driAnnSet;
	private static String citeSpanAnnotationTypeToExclude_allLang = ImporterBase.inlineCitationAnnType;

	private static String inputSentenceASname_RhetoricalClassifier = ImporterBase.driAnnSet;
	private static String inputSentenceAStype_RhetoricalClassifier = ImporterBase.sentenceAnnType;
	private static String outputSentenceRhetoricalFeature_RhetoricalClassifier = ImporterBase.sentence_RhetoricalAnnFeat;
	private static String sentenceLanguageFilter_RhetoricalClassifier = "english";
	private static String classifierModelURL_RhetoricalClassifier = baseModelPath + File.separator + "classifier_model_4.model";
	private static String classifierStructureURL_RhetoricalClassifier = baseModelPath + File.separator + "classifier_structure_4.arff";

	private static String inputSentenceASname_TermAnnotator = ImporterBase.driAnnSet;
	private static String inputSentenceAStype_TermAnnotator = ImporterBase.sentenceAnnType;
	private static String inputTokenASname_TermAnnotator = ImporterBase.driAnnSet;
	private static String inputTokenAStype_TermAnnotator = ImporterBase.tokenAnnType;
	private static String outputTermASname_TermAnnotator = ImporterBase.term_AnnSet;
	private static String outputTermAStype_TermAnnotator = ImporterBase.term_CandOcc;
	
	private static String inputDetectionASname_LanguageDetector = ImporterBase.driAnnSet;
	private static String inputDetectionAStype_LanguageDetector = ImporterBase.sentenceAnnType;
	private static String outputLangFeatureName_LanguageDetector = ImporterBase.langAnnFeat;

	private static String inputCandidateCitationASname_citationSanitizerResource = ImporterBase.driAnnSet;
	public static String inputCandidateInlineCitationAStype_citationSanitizerResource = "CandidateInlineCitation";
	public static String inputCandidateInlineCitationMarkerAStype_citationSanitizerResource = "CandidateInlineCitationMarker";

	private static String inputCitationAS_biblioEntryParserResource = ImporterBase.driAnnSet;
	private static String inputCitationAStype_biblioEntryParserResource = ImporterBase.bibEntryAnnType;
	private static String useGoogleScholar_biblioEntryParserResource = "true";
	private static String useBibsonomy_biblioEntryParserResource = "true";
	private static String useCrossRef_biblioEntryParserResource = "true";
	private static String useFreeCite_biblioEntryParserResource = "true";

	private static String inputTitleAS_headerAnalyzer = ImporterBase.driAnnSet;
	private static String inputTitleAStype_headerAnalyzer = ImporterBase.titleAnnType;
	private static String useGoogleScholar_headerAnalyzer = "true";
	private static String useBibsonomy_headerAnalyzer = "true";

	private static String outputCorefAS_corefChainBuilder = ImporterBase.coref_ChainAnnSet;

	private static String babelnetLanguage_babelnetAnnotator = "english";

	private static String lemmatizerModelURL_EN = baseModelPath + File.separator + "CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model";
	private static String POStaggerModelURL_EN = baseModelPath + File.separator + "CoNLL2009-ST-English-ALL.anna-3.3.postagger.model";
	private static String parserModelURL_EN = baseModelPath + File.separator + "CoNLL2009-ST-English-ALL.anna-3.3.parser.model";
	private static String srlModelURL_EN = baseModelPath + File.separator + "CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model";
	
	private static String baseLexiconPath = File.separator + GATELexiconsFolder;
	private static String appPath_XGAPPpreprocessStep1 = baseLexiconPath + File.separator + "DRI_Preprocessing_app_step_1.xgapp"; 
	private static String appPath_XGAPPpreprocessStep2 = baseLexiconPath + File.separator + "DRI_Preprocessing_app_step_2.xgapp"; 
	private static String appPath_XGAPPheader = baseLexiconPath + File.separator + "DRI_Header_preprocessing_app.xgapp";
	private static String appPath_XGAPPcitationMarker = baseLexiconPath + File.separator + "DRI_Citation_marker_app.xgapp"; 
	private static String appPath_XGAPPcorefMentionSpotter = baseLexiconPath + File.separator + "DRI_Coref_mention_spotter_app.xgapp";
	private static String appPath_XGAPPcausality = baseLexiconPath + File.separator + "DRI_Causality_app.xgapp"; 
	private static String appPath_XGAPPmetaAnnotator = baseLexiconPath + File.separator + "CustomMetadataAnnotation" + File.separator + "DRI_META_ANNOTATOR.xgapp";
	
	// GATE plugins singletons
	protected static ImporterPDFX PDFXimporter_Resource = null;
	protected static ImporterPDFEXT PDFEXTimporter_Resource = null;
	protected static ImporterGROBID GROBIDimporter_Resource = null;
	protected static ImporterJATS JATSimporter_Resource = null;
	protected static Map<LangENUM, MateParser> MateParsersLang_Resource = null;
	protected static RhetoricalClassifier RhetoricalClassifier_Resource = null;
	protected static TermAnnotator TermAnnotator_Resource = null;
	protected static MetaAnnotator MetaAnnotator_Resource = null;
	protected static LanguageDetector LanguageDetector_Resource = null;
	protected static InlineCitationSpotter CitationSanitizer_Resource = null;
	protected static CitationLinker CitationLinker_Resource = null;
	protected static BiblioEntryParser BiblioEntryParser_Resource = null;
	protected static HeaderAnalyzer HeaderAnalyzer_Resource = null;
	protected static CorefChainBuilder CorefChainBuilder_Resource = null;
	public static BabelnetAnnotator BabelnetAnnotator_Resource = null;
	protected static LexRankSummarizer LexRankSummarizer_Resource = null;
	protected static TitleSimSummarizer TitleSimSummarizer_Resource = null;
	protected static CorpusController corpusController_preprocess_XGAPPpreprocStep1 = null;
	protected static CorpusController corpusController_preprocess_XGAPPpreprocStep2 = null;
	protected static CorpusController corpusController_XGAPPheader = null;
	protected static CorpusController corpusController_XGAPPcitMarker = null;
	protected static CorpusController corpusController_XGAPPcorefMentionSpot = null;
	protected static CorpusController corpusController_XGAPPcausality = null;
	protected static CorpusController corpusController_XGAPPmetaAnnotator = null;
	
	/**
	 * This method returns a String with the status check results of the configuration settings 
	 * and the current status of the resources of Dr. Inventor Text Mining Framework
	 */
	public static String checkConfig() {
		String configString = "";
		String alertString = "";

		configString += "\n###########################################################################" + "\n";
		configString += "######## CHECK DR. INVENTOR TEXT MINING FRAMEWOR CONFIGURATION ############" + "\n\n";

		// Java Virtual Machine settings
		configString += " >>> Java Virtual Machine cofiguration check" + "\n";
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();
		long maxMemoryMb = runtime.maxMemory() / mb;
		configString += "Max Memory [MB]:" + maxMemoryMb + "\n";
		configString += "Used Memory [MB]:" + ((runtime.totalMemory() - runtime.freeMemory()) / mb) + "\n";
		configString += "Free Memory [MB]:" + (runtime.freeMemory() / mb) + "\n";
		configString += "Total Memory [MB]:" + (runtime.totalMemory() / mb) + "\n";

		if(maxMemoryMb <= 3000) {
			alertString += "The Max Memory of your JVM is less than 3Gb (" + maxMemoryMb + " Mb). In order to "
					+ "properly run Dr. Inventor Text Mining Framework you need at least 3Gb of "
					+ "Java max heap space (4Gb suggested).\n If you have not done so, please, start your program by specifying the "
					+ " JVM argument: -Xmx###m where ### should be replaced by the number of MB of "
					+ "maximum heap space (at least 3000, better 4096)" + "\n";
		}
		configString += "\n";

		// Check resource folder paths
		configString += " >>> dri.lib.config.properties check" + "\n";
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = logger.getClass().getResourceAsStream("/dri.lib.config.properties");

			// load a properties file
			prop.load(input);

			configString += "OK: config.property file correctly accessible" + "\n";

			// Check production property
			String isProd = prop.getProperty("production");
			if(isProd == null) {
				configString += "Can't access production property." + "\n";
			}
			else {
				configString += "OK: production: " + isProd + "\n";
			}

			// Check version property
			String version = prop.getProperty("version");
			if(version == null) {
				configString += "Can't access version property." + "\n";
			}
			else {
				configString += "OK: library version: " + version + "\n";
			}
		} catch (IOException ex) {
			configString += "Can't access dri.lib.config.properties file." + "\n";
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		configString += "\n";


		configString += " >>>  " + DRIconfigPathPropertyName + " system property check" + "\n";
		try {
			String systemPropDRIconfigPath = System.getProperty(DRIconfigPathPropertyName);

			if(StringUtils.isBlank(systemPropDRIconfigPath)) {
				configString += "The system property " + DRIconfigPathPropertyName + " is not defined.\n";

				if(StringUtils.isNotBlank(DRIconfigFileFullPath)) {
					configString += "DRI full local path to DRI property file programmatically set to " + DRIconfigFileFullPath + ".\n";
				}
				else {
					configString += "ERROR: full local path to DRI property file not set or incorrect. Set it by the system property named '" + DRIconfigPathPropertyName
							+ "' or ptogrammatically by the proper edu.upf.taln.dri.lib.FactorysetDRIPropertyFilePath method.";
				}
			}
			else {
				DRIconfigFileFullPath = systemPropDRIconfigPath;
				configString += "Full local path to DRI property file read from the value of the system property " + DRIconfigPathPropertyName + ".\n";
			}

			if(!StringUtils.isNotBlank(DRIconfigFileFullPath)) {
				boolean isStPropertyFilePath = PropertyManager.setPropertyFilePath(DRIconfigFileFullPath);

				if(isStPropertyFilePath) {
					DRIresourceFolderFullPath = (PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) != null) ? PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) : DRIconfigFileFullPath;
					File resourceFolder = new File(DRIresourceFolderFullPath);
					if(DRIresourceFolderFullPath == null || !resourceFolder.exists() || !resourceFolder.isDirectory()) {
						configString += "ERROR: the property named '" + PropertyManager.resourceFolder_fullPath
								+ "' (specified in the DRI property file) refers to an unexisting resource folder: '" + ( (DRIresourceFolderFullPath != null) ? DRIresourceFolderFullPath : "NULL") + "'.";
					}
					else {
						configString += "OK: '" + PropertyManager.resourceFolder_fullPath + "' property pointing at DRI resource folder: '" + DRIresourceFolderFullPath + "'";

					}
				}
				else {
					configString += "ERROR: the DRI property file with path '" + DRIconfigFileFullPath + " can't be accessed.";
				}

			}
		}
		catch (SecurityException se) {
			se.printStackTrace();
			configString += "CONFIGURATION ERROR: you have not rights to access the system property '" + DRIconfigPathPropertyName + "' (including full local path to DRI proeprty file)";
		}
		catch (Exception e) {
			e.printStackTrace();
			configString += "CONFIGURATION ERROR: Have you defined a system property named '" + DRIconfigPathPropertyName
					+ "' with the value of the full local path to DRI proeprty file or specified this path programmatically?";
		}

		configString += "\n";

		if(alertString != null && alertString.length() > 0) {
			configString += "\n ***** ATTTENTION: **** \n" + alertString;
		}

		configString += "\n###########################################################################";
		configString += "\n###########################################################################" + "\n";

		return configString;
	}

	/**
	 * Set programmatically the value of the full local path to DRI property file.
	 * If the value of this path is specified by the system property named 'DRIconf', the system property value has priority
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean setDRIPropertyFilePath(String filePath) {
		if( filePath != null && (new File(filePath)).exists() && (new File(filePath)).isFile() ) {
			DRIconfigFileFullPath = filePath;
			return true;
		}
		return false;
	}


	/**
	 * This method should be invoked in order to initialize the resources of Dr. Inventor Text Mining Framework.
	 * After the first call, subsequent calls will check if Dr. Inventor Text Mining Framework has already
	 * been initialized and, if so, the initialization process is simply skipped.
	 * 
	 * @throws DRIexception
	 */
	public static void initFramework() throws DRIexception {
		logger.info("INIT: checking if the Dr. Inventor Scientific Text Mining Java Library " + ((Factory.getVersion() != null) ? "version " + Factory.getVersion() : "") + " is initialized...");

		if(!initialized) {
			
			long startInitializationTime = System.currentTimeMillis();
			
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream("log4j.properties");
			PropertyConfigurator.configure(is);
			
			logger.info("INIT: initializing Dr. Inventor Scientific Text Mining Java Library " + ((Factory.getVersion() != null) ? "version " + Factory.getVersion() : "") + "...");
			
			Properties prop = new Properties();
			InputStream input = null;

			try {
				input = logger.getClass().getResourceAsStream("/dri.lib.config.properties");

				// load a properties file
				prop.load(input);

				String isProd = prop.getProperty("production");
				if(isProd != null && isProd.trim().toLowerCase().equals("true")) {
					isProductionProp = true;
				}

				String version = prop.getProperty("version");
				if(version != null) {
					versionProp = version;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				logger.info("Can't read config.properties file, setting configuration properties to their default value.");
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				DRIconfigFileFullPath = (System.getProperty(DRIconfigPathPropertyName) != null) ? System.getProperty(DRIconfigPathPropertyName) : DRIconfigFileFullPath;
				PropertyManager.setPropertyFilePath(DRIconfigFileFullPath);
				DRIresourceFolderFullPath = (PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) != null) ? PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) : null; 
				if(StringUtils.isBlank(DRIresourceFolderFullPath) || !(new File(DRIresourceFolderFullPath)).exists() || !(new File(DRIresourceFolderFullPath)).isDirectory()) {
					throw new InternalProcessingException("DRI INITIALIZATION ERROR: In DRI proeprty file, the property named '" + PropertyManager.resourceFolder_fullPath
							+ "' is not defined or points to an incorrect local path to the Resource folder - property value: " + ((DRIresourceFolderFullPath != null) ? DRIresourceFolderFullPath : "NULL"));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new InternalProcessingException("DRI INITIALIZATION ERROR: In DRI proeprty file, does the property property named '" + PropertyManager.resourceFolder_fullPath
						+ "' has been correctly defined with the value of the local path to the Resource folder? - Exception: " + e.getMessage());
			}

			logger.info("INIT: property '" + PropertyManager.resourceFolder_fullPath + "' pointing at: '" + DRIresourceFolderFullPath + "' folder.");
			
			// Init path for stop word lists and wikipedia IDF
			StopWordList.setResourcePath(DRIresourceFolderFullPath + File.separator + GATELexiconsFolder + File.separator + GATETextSimFolder);
			WikipediaLemmaPOSfFrequency.setResourcePath(DRIresourceFolderFullPath + File.separator + GATELexiconsFolder + File.separator + GATETextSimFolder);
			
			GROBIDhome = DRIresourceFolderFullPath + File.separator + GATEConvertersFolder + File.separator + "grobid-home_0_4_1";
			GROBIDproperties = DRIresourceFolderFullPath + File.separator + GATEConvertersFolder + File.separator + "grobid-home_0_4_1" +  File.separator + "config" + File.separator + "grobid.properties";
			logger.info("INIT: GROBID home set to: " + ((GROBIDhome != null) ? GROBIDhome : "NULL"));
			
			try {
				File GATEHome_Folder = new File(DRIresourceFolderFullPath + File.separator + GATEhomeFolder);
				File GATEPlugin_Folder = new File(DRIresourceFolderFullPath + File.separator + GATEhomeFolder  + File.separator + GATEpluginsFolder);
				File GATEuserConfig_File = new File(DRIresourceFolderFullPath + File.separator + GATEhomeFolder + File.separator + "gate_uc.xml");

				if(!GATEHome_Folder.exists() || !GATEHome_Folder.isDirectory()) {
					initialized = false;
					throw new InternalProcessingException("Error while initializing GATE framework: it is not possible to "
							+ "access the GATE home directory: '" + 
							DRIresourceFolderFullPath + File.separator + GATEhomeFolder + "'");
				}
				else if(!GATEPlugin_Folder.exists() || !GATEPlugin_Folder.isDirectory()) {
					initialized = false;
					throw new InternalProcessingException("Error while initializing GATE framework: it is not possible to "
							+ "access the GATE plugin home directory: '" + 
							DRIresourceFolderFullPath + File.separator + GATEhomeFolder + File.separator + GATEpluginsFolder + "'");
				}
				else if(!GATEuserConfig_File.exists() || !GATEuserConfig_File.isFile()) {
					initialized = false;
					throw new InternalProcessingException("Error while initializing GATE framework: it is not possible to "
							+ "access the GATE user config file: '" + 
							DRIresourceFolderFullPath + File.separator + GATEhomeFolder + File.separator + "gate_uc.xml" + "'");
				}
				else {
					logger.info("INIT: GATE home set to: " + GATEHome_Folder.getAbsolutePath());
					logger.info("INIT: GATE plugin home set to: " + GATEPlugin_Folder.getAbsolutePath());
					logger.info("INIT: GATE user config file set to: " + GATEuserConfig_File.getAbsolutePath());
					Gate.setGateHome(GATEHome_Folder);
					Gate.setPluginsHome(GATEPlugin_Folder);
					Gate.setSiteConfigFile(GATEuserConfig_File);
					Gate.setUserConfigFile(GATEuserConfig_File);

					Gate.init();
					initialized = true;
					logger.info("INIT: GATE initialized.");

					// Initialize GATE components
					logger.info("INIT: initializing GATE plugins...");

					if(isProductionProp) {
						PrintStream out = System.out;
						PrintStream err = System.err;
						System.setOut(new PrintStream(new OutputStream() {
							@Override public void write(int b) throws IOException {}
						}));
						System.setErr(new PrintStream(new OutputStream() {
							@Override public void write(int b) throws IOException {}
						}));
						try {
							initGATEcomponents();
						} finally {
							System.setOut(out);
							System.setErr(err);
						}
					}
					else {
						initGATEcomponents();
					}

					// Init DocumentImpl
					DocumentImpl.initDocPointers(PDFXimporter_Resource,
							PDFEXTimporter_Resource,
							GROBIDimporter_Resource,
							JATSimporter_Resource,
							MateParsersLang_Resource,
							RhetoricalClassifier_Resource,
							TermAnnotator_Resource,
							MetaAnnotator_Resource,
							LanguageDetector_Resource,
							CitationSanitizer_Resource,
							CitationLinker_Resource,
							BiblioEntryParser_Resource,
							HeaderAnalyzer_Resource,
							CorefChainBuilder_Resource,
							BabelnetAnnotator_Resource,
							LexRankSummarizer_Resource,
							TitleSimSummarizer_Resource,
							corpusController_preprocess_XGAPPpreprocStep1,
							corpusController_preprocess_XGAPPpreprocStep2,
							corpusController_XGAPPheader,
							corpusController_XGAPPcitMarker,
							corpusController_XGAPPcorefMentionSpot,
							corpusController_XGAPPcausality,
							corpusController_XGAPPmetaAnnotator);

					logger.info("INIT: initialization complete in " + (System.currentTimeMillis() - startInitializationTime) + " ms.");
				}

			} catch (GateException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing GATE framework: " + e.getMessage());
			}
		}
		else {
			logger.info("INIT: the Dr. Inventor Scientific Text Mining Java Library version " + ((Factory.getVersion() != null) ? Factory.getVersion() : "NULL") + " is already initialized.");
		}

	}

	private static void initGATEcomponents() throws InternalProcessingException {

		try {
			Gate.getCreoleRegister().registerComponent(ImporterPDFX.class);
			Gate.getCreoleRegister().registerComponent(ImporterPDFEXT.class);
			Gate.getCreoleRegister().registerComponent(ImporterGROBID.class);
			Gate.getCreoleRegister().registerComponent(ImporterJATS.class);
			Gate.getCreoleRegister().registerComponent(MateParser.class);
			Gate.getCreoleRegister().registerComponent(RhetoricalClassifier.class);
			Gate.getCreoleRegister().registerComponent(TermAnnotator.class);
			Gate.getCreoleRegister().registerComponent(LanguageDetector.class);
			Gate.getCreoleRegister().registerComponent(MetaAnnotator.class);
			Gate.getCreoleRegister().registerComponent(InlineCitationSpotter.class);
			Gate.getCreoleRegister().registerComponent(CitationLinker.class);
			Gate.getCreoleRegister().registerComponent(BiblioEntryParser.class);
			Gate.getCreoleRegister().registerComponent(HeaderAnalyzer.class);
			Gate.getCreoleRegister().registerComponent(CorefChainBuilder.class);
			Gate.getCreoleRegister().registerComponent(BabelnetAnnotator.class);
			Gate.getCreoleRegister().registerComponent(LexRankSummarizer.class);
			Gate.getCreoleRegister().registerComponent(TitleSimSummarizer.class);

			logger.info("INIT: initializing PDFX importer...");
			FeatureMap features_PDFXimporter = new SimpleFeatureMapImpl();
			features_PDFXimporter.put("inputSentenceASname", inputASname_PDFXimporter);
			features_PDFXimporter.put("inputSentenceAStype", inputAStype_PDFXimporter);
			PDFXimporter_Resource = (ImporterPDFX) gate.Factory.createResource(ImporterPDFX.class.getName(), features_PDFXimporter);

			logger.info("INIT: initializing PDFEXT importer...");
			FeatureMap features_PDFEXTimporter = new SimpleFeatureMapImpl();
			features_PDFEXTimporter.put("inputSentenceASname", inputASname_PDFEXTimporter);
			features_PDFEXTimporter.put("inputSentenceAStype", inputAStype_PDFEXTimporter);
			PDFEXTimporter_Resource = (ImporterPDFEXT) gate.Factory.createResource(ImporterPDFEXT.class.getName(), features_PDFEXTimporter);
			
			logger.info("INIT: initializing GROBID importer...");
			FeatureMap features_GROBIDimporter = new SimpleFeatureMapImpl();
			features_GROBIDimporter.put("inputSentenceASname", inputASname_GROBIDimporter);
			features_GROBIDimporter.put("inputSentenceAStype", inputAStype_GROBIDimporter);
			GROBIDimporter_Resource = (ImporterGROBID) gate.Factory.createResource(ImporterGROBID.class.getName(), features_PDFEXTimporter);

			logger.info("INIT: initializing JATS importer...");
			FeatureMap features_JATSimporter = new SimpleFeatureMapImpl();
			features_JATSimporter.put("inputSentenceASname", inputASname_JATSimporter);
			features_JATSimporter.put("inputSentenceAStype", inputAStype_JATSimporter);
			JATSimporter_Resource = (ImporterJATS) gate.Factory.createResource(ImporterJATS.class.getName(), features_JATSimporter);

			logger.info("INIT: initializing parser (English)...");
			File Mate_lemmModel_EN = new File(DRIresourceFolderFullPath + lemmatizerModelURL_EN);
			File Mate_POSModel_EN = new File(DRIresourceFolderFullPath + POStaggerModelURL_EN);
			File Mate_parsModel_EN = new File(DRIresourceFolderFullPath + parserModelURL_EN);
			File Mate_srlModel_EN = new File(DRIresourceFolderFullPath + srlModelURL_EN);

			if(!Mate_lemmModel_EN.exists() || !Mate_lemmModel_EN.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Parser: "
						+ "cannot retrieve lemmatizer model at: '" + 
						DRIresourceFolderFullPath + lemmatizerModelURL_EN + "'");
			}
			else if(!Mate_POSModel_EN.exists() || !Mate_POSModel_EN.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Parser: "
						+ "cannot retrieve POS-tagger model at: '" + 
						DRIresourceFolderFullPath + POStaggerModelURL_EN + "'");
			}
			else if(!Mate_parsModel_EN.exists() || !Mate_parsModel_EN.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Parser: "
						+ "cannot retrieve parser model at: '" + 
						DRIresourceFolderFullPath + parserModelURL_EN + "'");
			}
			else if(!Mate_srlModel_EN.exists() || !Mate_srlModel_EN.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Semantic Role Labeller: "
						+ "cannot retrieve srl model at: '" + 
						DRIresourceFolderFullPath + srlModelURL_EN + "'");
			}

			FeatureMap features_MateParser_allLang = new SimpleFeatureMapImpl();
			features_MateParser_allLang.put("sentenceAnnotationSetToAnalyze", sentenceAnnotationSetToAnalyze_allLang_MateParser);
			features_MateParser_allLang.put("sentenceAnnotationTypeToAnalyze", sentenceAnnotationTypeToAnalyze_allLang_MateParser);
			features_MateParser_allLang.put("tokenAnnotationSetToAnalyze", tokenAnnotationSetToAnalyze_allLang_MateParser);
			features_MateParser_allLang.put("tokenAnnotationTypeToAnalyze", tokenAnnotationTypeToAnalyze_allLang_MateParser);
			features_MateParser_allLang.put("excludeThreshold", excludeThreshold_allLang_MateParser);
			features_MateParser_allLang.put("citancesEnabled", citancesEnabled_allLang);
			features_MateParser_allLang.put("citeSpanAnnotationSetToExclude", citeSpanAnnotationSetToExclude_allLang);
			features_MateParser_allLang.put("citeSpanAnnotationTypeToExclude", citeSpanAnnotationTypeToExclude_allLang);
			if(enableSentenceParsing) {
				MateParsersLang_Resource = new HashMap<LangENUM, MateParser>(); 
				
				FeatureMap features_MateParser_EN = gate.Factory.newFeatureMap();
				features_MateParser_EN.putAll(features_MateParser_allLang);
				features_MateParser_EN.put("lemmaModelPath", DRIresourceFolderFullPath + lemmatizerModelURL_EN);
				features_MateParser_EN.put("postaggerModelPath", DRIresourceFolderFullPath + POStaggerModelURL_EN);
				features_MateParser_EN.put("parserModelPath", DRIresourceFolderFullPath + parserModelURL_EN);
				features_MateParser_EN.put("srlModelPath", DRIresourceFolderFullPath + srlModelURL_EN);
				MateParser MateParser_EN = (MateParser) gate.Factory.createResource(MateParser.class.getName(), features_MateParser_EN);
				MateParsersLang_Resource.put(LangENUM.EN, MateParser_EN);
			}

			logger.info("INIT: initializing rhetorical classifier...");
			File RhetoricalClassifier_classifierModel = new File(DRIresourceFolderFullPath + classifierModelURL_RhetoricalClassifier);
			File RhetoricalClassifier_classifierStructure = new File(DRIresourceFolderFullPath + classifierStructureURL_RhetoricalClassifier);

			if(!RhetoricalClassifier_classifierModel.exists() || !RhetoricalClassifier_classifierModel.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Rhetorical Classifier: "
						+ "cannot retrieve classifier model at: '" + 
						DRIresourceFolderFullPath + classifierModelURL_RhetoricalClassifier + "'");
			} else if (!RhetoricalClassifier_classifierStructure.exists() || !RhetoricalClassifier_classifierStructure.isFile()) {
				initialized = false;
				throw new InternalProcessingException("Error while initializing Rhetorical Classifier: "
						+ "cannot retrieve classifier structure at: '" + 
						DRIresourceFolderFullPath + classifierStructureURL_RhetoricalClassifier + "'");
			} 

			FeatureMap features_RhetoricalClassifier = new SimpleFeatureMapImpl();
			features_RhetoricalClassifier.put("inputSentenceASname", inputSentenceASname_RhetoricalClassifier);
			features_RhetoricalClassifier.put("inputSentenceAStype", inputSentenceAStype_RhetoricalClassifier);
			features_RhetoricalClassifier.put("outputSentenceRhetoricalFeature", outputSentenceRhetoricalFeature_RhetoricalClassifier);
			features_RhetoricalClassifier.put("sentenceLanguageFilter", sentenceLanguageFilter_RhetoricalClassifier);
			try {
				features_RhetoricalClassifier.put("classifierModelURL", RhetoricalClassifier_classifierModel.toURI().toURL());
				features_RhetoricalClassifier.put("classifierStructureURL", RhetoricalClassifier_classifierStructure.toURI().toURL());
				SubjectivityReader.init();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing Rhetorical Classifier: "
						+ e.getMessage());
			}
			RhetoricalClassifier_Resource = (RhetoricalClassifier) gate.Factory.createResource(RhetoricalClassifier.class.getName(), features_RhetoricalClassifier);


			logger.info("INIT: initializing term extractor...");
			FeatureMap features_TermAnnotator = new SimpleFeatureMapImpl();
			features_TermAnnotator.put("inputSentenceASname", inputSentenceASname_TermAnnotator);
			features_TermAnnotator.put("inputSentenceAStype", inputSentenceAStype_TermAnnotator);
			features_TermAnnotator.put("inputTokenASname", inputTokenASname_TermAnnotator);
			features_TermAnnotator.put("inputTokenAStype", inputTokenAStype_TermAnnotator);
			features_TermAnnotator.put("outputTermASname", outputTermASname_TermAnnotator);
			features_TermAnnotator.put("outputTermAStype", outputTermAStype_TermAnnotator);
			TermAnnotator_Resource = (TermAnnotator) gate.Factory.createResource(TermAnnotator.class.getName(), features_TermAnnotator);
			
			logger.info("INIT: initializing meta annotator...");
			FeatureMap features_MetaAnnotator = new SimpleFeatureMapImpl();
			MetaAnnotator_Resource = (MetaAnnotator) gate.Factory.createResource(MetaAnnotator.class.getName(), features_MetaAnnotator);
			
			logger.info("INIT: initializing language detector...");
			FeatureMap features_LanguageDetector = new SimpleFeatureMapImpl();
			features_LanguageDetector.put("inputDetectionASname", inputDetectionASname_LanguageDetector);
			features_LanguageDetector.put("inputDetectionAStype", inputDetectionAStype_LanguageDetector);
			features_LanguageDetector.put("outputLangFeatureName", outputLangFeatureName_LanguageDetector);
			LanguageDetector_Resource = (LanguageDetector) gate.Factory.createResource(LanguageDetector.class.getName(), features_LanguageDetector);
			
			logger.info("INIT: initializing citation sanitizer...");
			FeatureMap features_CitationSanitizer = new SimpleFeatureMapImpl();
			features_CitationSanitizer.put("inputCandidateCitationASname", inputCandidateCitationASname_citationSanitizerResource);
			features_CitationSanitizer.put("inputCandidateInlineCitationAStype", inputCandidateInlineCitationAStype_citationSanitizerResource);
			features_CitationSanitizer.put("inputCandidateInlineCitationMarkerAStype", inputCandidateInlineCitationMarkerAStype_citationSanitizerResource);
			CitationSanitizer_Resource = (InlineCitationSpotter) gate.Factory.createResource(InlineCitationSpotter.class.getName(), features_CitationSanitizer);


			logger.info("INIT: initializing citation linker...");
			FeatureMap features_CitationLinker = new SimpleFeatureMapImpl();
			CitationLinker_Resource = (CitationLinker) gate.Factory.createResource(CitationLinker.class.getName(), features_CitationLinker);


			logger.info("INIT: initializing citation expander...");
			FeatureMap features_BiblioEntryParser = new SimpleFeatureMapImpl();
			features_BiblioEntryParser.put("inputBiblioEntryAS", inputCitationAS_biblioEntryParserResource);
			features_BiblioEntryParser.put("inputBiblioEntryAStype", inputCitationAStype_biblioEntryParserResource);
			features_BiblioEntryParser.put("useGoogleScholar", useGoogleScholar_biblioEntryParserResource);
			features_BiblioEntryParser.put("useBibsonomy", useBibsonomy_biblioEntryParserResource);
			features_BiblioEntryParser.put("useFreeCite", useFreeCite_biblioEntryParserResource);
			features_BiblioEntryParser.put("useCrossRef", useCrossRef_biblioEntryParserResource);
			BiblioEntryParser_Resource = (BiblioEntryParser) gate.Factory.createResource(BiblioEntryParser.class.getName(), features_BiblioEntryParser);


			logger.info("INIT: initializing header analyzer...");
			FeatureMap features_HeaderAnalyzer = new SimpleFeatureMapImpl();
			features_HeaderAnalyzer.put("inputTitleAS", inputTitleAS_headerAnalyzer);
			features_HeaderAnalyzer.put("inputTitleAStype", inputTitleAStype_headerAnalyzer);
			features_BiblioEntryParser.put("useGoogleScholar", useGoogleScholar_headerAnalyzer);
			features_BiblioEntryParser.put("useBibsonomy", useBibsonomy_headerAnalyzer);
			HeaderAnalyzer_Resource = (HeaderAnalyzer) gate.Factory.createResource(HeaderAnalyzer.class.getName(), features_HeaderAnalyzer);


			logger.info("INIT: initializing coreference chain builder...");
			FeatureMap features_CorefChainBuilder = new SimpleFeatureMapImpl();
			features_CorefChainBuilder.put("outputCorefAS", outputCorefAS_corefChainBuilder);
			CorefChainBuilder_Resource = (CorefChainBuilder) gate.Factory.createResource(CorefChainBuilder.class.getName(), features_CorefChainBuilder);


			logger.info("INIT: initializing BabelNet annotator...");
			FeatureMap features_BabelnetAnnotator = new SimpleFeatureMapImpl();
			features_BabelnetAnnotator.put("babelnetAPIkey", PropertyManager.getProperty("babelnet.APIkey"));
			features_BabelnetAnnotator.put("babelnetLanguage", babelnetLanguage_babelnetAnnotator);
			BabelnetAnnotator_Resource = (BabelnetAnnotator) gate.Factory.createResource(BabelnetAnnotator.class.getName(), features_BabelnetAnnotator);
			
			
			logger.info("INIT: initializing TitleSim summarizer...");
			TitleSimSummarizer_Resource = (TitleSimSummarizer) gate.Factory.createResource(TitleSimSummarizer.class.getName(), gate.Factory.newFeatureMap());
			
			logger.info("INIT: initializing LexRank summarizer...");
			LexRankSummarizer_Resource = (LexRankSummarizer) gate.Factory.createResource(LexRankSummarizer.class.getName(), gate.Factory.newFeatureMap());
			
			
			logger.info("INIT: initializing XGAPP-based components...");
			File XGAPP_preprocStep1 = new File(DRIresourceFolderFullPath + appPath_XGAPPpreprocessStep1);
			File XGAPP_preprocStep2 = new File(DRIresourceFolderFullPath + appPath_XGAPPpreprocessStep2);
			File XGAPP_header = new File(DRIresourceFolderFullPath + appPath_XGAPPheader);
			File XGAPP_citMarker = new File(DRIresourceFolderFullPath + appPath_XGAPPcitationMarker);
			File XGAPP_corefMentSpot = new File(DRIresourceFolderFullPath + appPath_XGAPPcorefMentionSpotter);
			File XGAPP_causality = new File(DRIresourceFolderFullPath + appPath_XGAPPcausality);
			File XGAPP_metaAnnotator = new File(DRIresourceFolderFullPath + appPath_XGAPPmetaAnnotator);
			

			try {
				logger.info("INIT: initializing XGAPP-based document preprocessor...");
				corpusController_preprocess_XGAPPpreprocStep1 = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_preprocStep1);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Pre-processing step 1: "
						+ e.getMessage());
			}

			try {
				logger.info("INIT: initializing XGAPP-based document sanitizer...");
				corpusController_preprocess_XGAPPpreprocStep2 = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_preprocStep2);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Pre-processing step 2: "
						+ e.getMessage());
			}

			try {
				logger.info("INIT: initializing XGAPP-based header parser...");
				corpusController_XGAPPheader = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_header);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Header: "
						+ e.getMessage());
			}

			try {
				logger.info("INIT: initializing XGAPP-based citation marker...");
				corpusController_XGAPPcitMarker = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_citMarker);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Citation Marker: "
						+ e.getMessage());
			}

			try {
				logger.info("INIT: initializing XGAPP-based candidate coreference mention spotter...");
				corpusController_XGAPPcorefMentionSpot = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_corefMentSpot);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Coreference Mention Spotter: "
						+ e.getMessage());
			}

			try {
				logger.info("INIT: initializing XGAPP-based causality spotter...");
				corpusController_XGAPPcausality = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_causality);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP Causality Spotter: "
						+ e.getMessage());
			}
			
			try {
				logger.info("INIT: initializing XGAPP-based metadata annotator...");
				corpusController_XGAPPmetaAnnotator = (CorpusController) gate.util.persistence.PersistenceManager.loadObjectFromFile(XGAPP_metaAnnotator);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalProcessingException("Error while initializing XGAPP meta annotator: "
						+ e.getMessage());
			}

		} catch (GateException e) {
			e.printStackTrace();
			throw new InternalProcessingException("Error while initializing GATE framework: " + e.getMessage());
		}
	}

	/**
	 * Get the PDF loader (singleton) by the interface {@link edu.upf.taln.dri.lib.loader.PDFloader PDFloader} to invoke proper methods to generate {@link edu.upf.taln.dri.lib.model.Document Document}s from PDF files
	 * 
	 * @return
	 * @throws DRIexception 
	 */
	public static PDFloader getPDFloader() throws DRIexception {
		initFramework();
		
		if(PDFloaderObj == null) {
			switch(Factory.PDFtoTextConverter) {
			case PDFX:
				PDFloaderObj = new PDFXloaderImpl();
				break;
			case GROBID:
				PDFloaderObj = new GROBIDloaderImpl();
				break;
			/* TO ADD PDFEXT
			case PDFEXT:
				PDFloaderObj = new PDFEXTloaderImpl();
				break;
			*/
			default:
				PDFloaderObj = new PDFXloaderImpl();
				break;
			}
		}
		else {
			if(!(PDFloaderObj instanceof PDFXloaderImpl) && Factory.PDFtoTextConverter.equals(PDFtoTextConvMethod.PDFX)) {
				PDFloaderObj = new PDFXloaderImpl();
			}
			else if(!(PDFloaderObj instanceof GROBIDloaderImpl) && Factory.PDFtoTextConverter.equals(PDFtoTextConvMethod.GROBID)) {
				PDFloaderObj = new GROBIDloaderImpl();
			}
			/* TO ADD PDFEXT
			else if(!(PDFloaderObj instanceof PDFEXTloaderImpl) && Factory.PDFtoTextConverter.equals(PDFtoTextConvMethod.PDFEXT)) {
				PDFloaderObj = new PDFEXTloaderImpl();
			}
			*/
		}

		return PDFloaderObj;
	}

	/**
	 * Get the JATS loader (singleton) by the interface {@link edu.upf.taln.dri.lib.loader.JATSloader JATSloader} to invoke proper methods to generate {@link edu.upf.taln.dri.lib.model.Document Document}s from JATS XML files
	 * 
	 * @return
	 * @throws DRIexception 
	 */
	public static JATSloader getJATSloader() throws DRIexception {
		initFramework();

		if(JATSloaderObj == null) {
			JATSloaderObj = new JATSloaderImpl();
		}

		return JATSloaderObj;
	}

	/**
	 * Get the plain text loader (singleton) by the interface {@link edu.upf.taln.dri.lib.loader.PlainTextLoader PlainTextLoader} to invoke proper methods to generate {@link edu.upf.taln.dri.lib.model.Document Document}s from plain text
	 * 
	 * @return
	 * @throws DRIexception 
	 */
	public static PlainTextLoader getPlainTextLoader() throws DRIexception {
		initFramework();

		if(plainTextLoaderObj == null) {
			plainTextLoaderObj = new PlainTextLoaderImpl();
		}

		return plainTextLoaderObj;
	}

	/**
	 * Instantiates a new {@link edu.upf.taln.dri.lib.model.Document Document}, without any data loaded.
	 * 
	 * @return the new {@link edu.upf.taln.dri.lib.model.Document Document} instance
	 * @throws DRIexception 
	 */
	public static Document createNewDocument() throws DRIexception {
		initFramework();

		Document doc = new DocumentImpl();

		return doc;
	}

	/**
	 * Instantiates a new {@link edu.upf.taln.dri.lib.model.Document Document}, populating it with the XML string-serialized contents read from a file.
	 * 
	 * @param absoluteFilePath the absolute path of the file with the XML string-serialized contents of the document to load
	 * @return the new {@link edu.upf.taln.dri.lib.model.Document Document} instance populated with the XML string-serialized contents
	 * @throws DRIexception 
	 */
	public static Document createNewDocument(String absoluteFilePath) throws DRIexception {
		initFramework();

		Document doc = new DocumentImpl();

		doc.loadXML(absoluteFilePath);

		return doc;
	}

	/**
	 * Instantiates a new {@link edu.upf.taln.dri.lib.model.Document Document}, populating it with the XML string-serialized contents read from a file.
	 * 
	 * @param file the file with the XML string-serialized contents of the document to load
	 * @return the new {@link edu.upf.taln.dri.lib.model.Document Document} instance populated with the XML string-serialized contents
	 * @throws DRIexception 
	 */
	public static Document createNewDocument(File file) throws DRIexception {
		initFramework();

		Document doc = new DocumentImpl();

		doc.loadXML(file);

		return doc;
	}
	
	/**
	 * Check if the sentence parser is enabled.
	 * The parsing of sentences should be enabled for the library to properly work.
	 * 
	 * @return
	 */
	public static boolean isEnableMate() {
		return enableSentenceParsing;
	}

	/**
	 * Enable or disable the parsing of sentences.
	 * The parsing of sentences should be enabled for the library to properly work.
	 * 
	 * @param enableSentenceParsing
	 */
	public static void setEnableMate(boolean enableSentenceParsing) {
		Factory.enableSentenceParsing = enableSentenceParsing;
	}

	/**
	 * Get the PDF to text converter currently used by the library.
	 * 
	 * @return
	 */
	public static PDFtoTextConvMethod checkPDFtoTextConverter() {
		return PDFtoTextConverter;
	}

	/**
	 * Set the PDF to text converter that the library should use.
	 * 
	 * @param enableSentenceParsing
	 */
	public static void setPDFtoTextConverter(PDFtoTextConvMethod converter) {
		if(converter != null) {
			Factory.PDFtoTextConverter = converter;
		}
	}

	/**
	 * Get resource folder full path
	 * 
	 * @return
	 */
	public static String getResourceFolderFullPath() {
		return (DRIresourceFolderFullPath != null) ? new String(DRIresourceFolderFullPath) : null;
	}

	/**
	 * Get the version of the library
	 * 
	 * @return
	 */
	public static String getVersion() {
		if(versionProp != null) {
			return new String(versionProp);
		}
		return null;
	}
	
	/**
	 * Set the module configuration by passing an instance of the 
	 * {@link edu.upf.taln.dri.lib.util.ModuleConfig ModuleConfig} class that
	 * specifies which modules of the library are enabled.
	 * 
	 * @param configurationObject
	 */
	public static void setModuleConfig(ModuleConfig configurationObject) {
		if(configurationObject != null) {
			currentModuleConfig = ModuleConfig.dCopy(configurationObject);
			logger.info("Module configuration set to: " + ((currentModuleConfig != null) ? currentModuleConfig.toString() : "NULL"));
		}
		else {
			logger.info("Invalid module configuration object. Module configuration set to: " + ((currentModuleConfig != null) ? currentModuleConfig.toString() : "NULL"));
		}
	}
	
	/**
	 * Get a copy of the current module configuration of the library.
	 * IMPORTANT; changes to the boolean flags of the returned {@link edu.upf.taln.dri.lib.util.ModuleConfig ModuleConfig} instance
	 * do not change any module configuration (enable or disable any module). 
	 * To change the module configuration, provide a new instance of {@link edu.upf.taln.dri.lib.util.ModuleConfig ModuleConfig}
	 * by the the {@link #setModuleConfig(ModuleConfig) setModuleConfig(ModuleConfig configurationObject)} method.
	 * @return
	 */
	public static ModuleConfig getModuleConfig() {
		if(currentModuleConfig != null) {
			return ModuleConfig.dCopy(currentModuleConfig);
		}
		else {
			return null;
		}
	}

	private static String getFlagProperty(String propertyName) {
		if(propertyName != null) {
			Properties prop = new Properties();
			InputStream input = null;

			try {
				input = logger.getClass().getResourceAsStream("/dri.lib.config.properties");

				// load a properties file
				prop.load(input);

				return prop.getProperty(propertyName);
			} catch (IOException ex) {
				ex.printStackTrace();
				logger.info("Can't read config.properties file, setting configuration properties to their default value.");
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

}
