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
package edu.upf.taln.dri.lib.postproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.ext.LangENUM;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import edu.upf.taln.dri.module.parser.MateParser;
import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.SimpleFeatureMapImpl;

/**
 * 
 * EXPERIMENTAL!!!
 * 
 *
 */
public class SpanishParser {

	private static Logger logger = Logger.getLogger(SpanishParser.class);

	private static final String GATEModelsFolder = "models";
	private static final String GATEhomeFolder = "gate_home";
	private static final String GATEpluginsFolder = "plugins";
	private static String baseModelPath = File.separator + GATEModelsFolder;

	private static String sentenceAnnotationSetToAnalyze_allLang_MateParser = ImporterBase.driAnnSet;
	private static String sentenceAnnotationTypeToAnalyze_allLang_MateParser = ImporterBase.sentenceAnnType;
	private static String tokenAnnotationSetToAnalyze_allLang_MateParser = ImporterBase.driAnnSet;
	private static String tokenAnnotationTypeToAnalyze_allLang_MateParser = ImporterBase.tokenAnnType;
	private static Integer excludeThreshold_allLang_MateParser = 120; // Originally set equal to 80
	private static Boolean citancesEnabled_allLang = true;
	private static String citeSpanAnnotationSetToExclude_allLang = ImporterBase.driAnnSet;
	private static String citeSpanAnnotationTypeToExclude_allLang = ImporterBase.inlineCitationAnnType;

	private static String lemmatizerModelURL_ES = baseModelPath + File.separator + "CoNLL2009-ST-Spanish-ALL.anna-3.3.lemmatizer.model";
	private static String POStaggerModelURL_ES = baseModelPath + File.separator + "CoNLL2009-ST-Spanish-ALL.anna-3.3.postagger.model";
	private static String parserModelURL_ES = baseModelPath + File.separator + "CoNLL2009-ST-Spanish-ALL.anna-3.3.parser.model";
	private static String srlModelURL_ES = baseModelPath + File.separator + "CoNLL2009-ST-Spanish-ALL.anna-3.3.srl-4.21.srl-rr.model";

	protected static Map<LangENUM, MateParser> MateParsersLang_Resource = new HashMap<LangENUM, MateParser>();

	private static String arg_localFolderPathToProcess = null;
	private static String arg_DRIresFolderPath = null;
	private static String arg_fileNameStartsWith = null;
	
	/**
	 * EXPERIMENTAL!
	 * 
	 * This is main class that useful to parse all the Spanish contents of the papers processed by the Dr. Inventor Text Mining Framework and stored as
	 * an XML file.<br/><br/>
	 * 
	 * Suppose you have papers (in PDF or JATS XML format) with multi-lingual contents and in particular papers including both English and Spanish text excerpts.<br/>
	 * In order to analyze these texts you should:<br/>
	 * 1) Enable the Dr. Inventor library to deal with multi-lingual contents by setting to true the MultiLangSupport. This action can be performed by setting to true
	 * the related configuration flag of the class {@link edu.upf.taln.dri.lib.util.ModuleConfig ModuleConfig};<br/>
	 * 2) Analyze the multi-lingual papers by means of the Dr. Inventor library and store the contents as an XML file: 
	 * {@link edu.upf.taln.dri.lib.Factory#getXMLString() Factory.getXMLString()} <br/>
	 * 3) Execute the current main class by passing the following program arguments:<br/>
	 * [0]: the full local path of the folder to process; this directory and subdirectories will be visited and all XML files processed by means fo the Spanish parser<br/>
	 * [1]: the full local path of the DRI resource folder<br/>
	 * [2]: OPTIONAL - string to select files to parse. If not empty only files starting with this string will be parsed<br/><br/>
	 * 
	 * All .xml files in the full local path of the folder to process and its subfolder will be processed by means of the Spanish parser
	 * and results will be written in an XML files with the same name of the original one but ending in '_ESpars.xml'.
	 * 
	 * @param args
	 * @throws InternalProcessingException
	 */
	public static void main(String[] args) throws InternalProcessingException {


		if(args == null || args.length < 2) {
			System.out.println("Argument specification error!");
			System.out.println("You have to specify the following arguments:");
			System.out.println("     [0] --> the full local path of the folder to process; this directory and subdirectories will be visited and all XML files processed by means fo the Spanish parser.");
			System.out.println("     [1] --> the full local path of the DRI resource folder.");
			System.out.println("     [2] --> OPTIONAL - string to select files to parse. If not empty only files starting with this string will be parsed.");
			return;
		}

		// INPUT ARGUMENT: Check full local path of the folder to process argument
		if(args.length > 0 && args[0] != null && !args[0].trim().equals("")) {
			File localFolderToProcessFile = new File(args[0].trim());

			if(localFolderToProcessFile != null && localFolderToProcessFile.exists() && localFolderToProcessFile.isDirectory()) {
				arg_localFolderPathToProcess = (args[0].trim().endsWith(File.separator)) ? args[0].trim() : args[0].trim() + File.separator;
			}
			else {
				System.out.println("Argument specification error!");
				System.out.println("The full local path of the folder to process is invalid.");
				return;
			}
		}
		else {
			System.out.println("Argument specification error!");
			System.out.println("The full local path of the folder to process is not specified.");
			return;
		}

		// INPUT ARGUMENT: Check full local path of the DRI property file argument
		if(args.length > 1 && args[1] != null && !args[1].trim().equals("")) {
			File DRIconfigFile = new File(args[1].trim());

			if(DRIconfigFile != null && DRIconfigFile.exists() && DRIconfigFile.isFile()) {
				arg_DRIresFolderPath = args[1].trim();
			}
			else {
				System.out.println("Argument specification error!");
				System.out.println("The full local path of the DRI property file is invalid.");
				return;
			}
		}
		else {
			System.out.println("Argument specification error!");
			System.out.println("The full local path of the DRI property file is not spedcified.");
			return;
		}

		// INPUT ARGUMENT: Start with string
		if(args.length > 2 && args[2] != null && !args[2].trim().equals("")) {
			arg_fileNameStartsWith = args[2].trim();
		}
		

		// Print program arguments
		System.out.println("*******************************************************");
		System.out.println(" - Full local path of the folder to process (arg 0): " + arg_localFolderPathToProcess);
		System.out.println(" - Full local path of the DRI resource folder (arg 1): " + arg_DRIresFolderPath);
		if(arg_fileNameStartsWith != null && !arg_fileNameStartsWith.equals("")) {
			System.out.println(" - Processing phase (arg 3): " + ((arg_fileNameStartsWith != null) ? arg_fileNameStartsWith : "-------"));
		}
		System.out.println("*******************************************************");


		if(arg_DRIresFolderPath.endsWith(File.separator)) {
			arg_DRIresFolderPath = arg_DRIresFolderPath.substring(0, arg_DRIresFolderPath.length() - 1);
		}

		// Init GATE
		System.out.println("INIT: Initializing GATE...");
		File GATEHome_Folder = new File(arg_DRIresFolderPath + File.separator + GATEhomeFolder);
		File GATEPlugin_Folder = new File(arg_DRIresFolderPath + File.separator + GATEhomeFolder  + File.separator + GATEpluginsFolder);
		File GATEuserConfig_File = new File(arg_DRIresFolderPath + File.separator + GATEhomeFolder + File.separator + "gate_uc.xml");
		System.out.println("INIT: GATE home set to: " + GATEHome_Folder.getAbsolutePath());
		System.out.println("INIT: GATE plugin home set to: " + GATEPlugin_Folder.getAbsolutePath());
		System.out.println("INIT: GATE user config file set to: " + GATEuserConfig_File.getAbsolutePath());
		Gate.setGateHome(GATEHome_Folder);
		Gate.setPluginsHome(GATEPlugin_Folder);
		Gate.setSiteConfigFile(GATEuserConfig_File);
		Gate.setUserConfigFile(GATEuserConfig_File);

		try {
			Gate.init();
			Gate.getCreoleRegister().registerComponent(MateParser.class);
		} catch (GateException e3) {
			e3.printStackTrace();
		}

		System.out.println("INIT: GATE initialized.");

		// Init parser Spanish
		System.out.println("INIT: initializing parser (Spanish)...");
		File Mate_lemmModel_ES = new File(arg_DRIresFolderPath + lemmatizerModelURL_ES);
		File Mate_POSModel_ES = new File(arg_DRIresFolderPath + POStaggerModelURL_ES);
		File Mate_parsModel_ES = new File(arg_DRIresFolderPath + parserModelURL_ES);
		File Mate_srlModel_ES = new File(arg_DRIresFolderPath + srlModelURL_ES);

		if(!Mate_lemmModel_ES.exists() || !Mate_lemmModel_ES.isFile()) {
			throw new InternalProcessingException("Error while initializing Parser: "
					+ "cannot retrieve lemmatizer model at: '" + 
					arg_DRIresFolderPath + lemmatizerModelURL_ES + "'");
		}
		else if(!Mate_POSModel_ES.exists() || !Mate_POSModel_ES.isFile()) {
			throw new InternalProcessingException("Error while initializing Parser: "
					+ "cannot retrieve POS-tagger model at: '" + 
					arg_DRIresFolderPath + POStaggerModelURL_ES + "'");
		}
		else if(!Mate_parsModel_ES.exists() || !Mate_parsModel_ES.isFile()) {
			throw new InternalProcessingException("Error while initializing Parser: "
					+ "cannot retrieve parser model at: '" + 
					arg_DRIresFolderPath + parserModelURL_ES + "'");
		}
		else if(!Mate_srlModel_ES.exists() || !Mate_srlModel_ES.isFile()) {
			throw new InternalProcessingException("Error while initializing Semantic Role Labeller: "
					+ "cannot retrieve srl model at: '" + 
					arg_DRIresFolderPath + srlModelURL_ES + "'");
		}

		FeatureMap features_MateParser_ES = gate.Factory.newFeatureMap();
		FeatureMap features_MateParser_allLang = new SimpleFeatureMapImpl();
		features_MateParser_allLang.put("sentenceAnnotationSetToAnalyze", sentenceAnnotationSetToAnalyze_allLang_MateParser);
		features_MateParser_allLang.put("sentenceAnnotationTypeToAnalyze", sentenceAnnotationTypeToAnalyze_allLang_MateParser);
		features_MateParser_allLang.put("tokenAnnotationSetToAnalyze", tokenAnnotationSetToAnalyze_allLang_MateParser);
		features_MateParser_allLang.put("tokenAnnotationTypeToAnalyze", tokenAnnotationTypeToAnalyze_allLang_MateParser);
		features_MateParser_allLang.put("excludeThreshold", excludeThreshold_allLang_MateParser);
		features_MateParser_allLang.put("citancesEnabled", citancesEnabled_allLang);
		features_MateParser_allLang.put("citeSpanAnnotationSetToExclude", citeSpanAnnotationSetToExclude_allLang);
		features_MateParser_allLang.put("citeSpanAnnotationTypeToExclude", citeSpanAnnotationTypeToExclude_allLang);
		features_MateParser_ES.putAll(features_MateParser_allLang);
		features_MateParser_ES.put("lemmaModelPath", arg_DRIresFolderPath + lemmatizerModelURL_ES);
		features_MateParser_ES.put("postaggerModelPath", arg_DRIresFolderPath + POStaggerModelURL_ES);
		features_MateParser_ES.put("parserModelPath", arg_DRIresFolderPath + parserModelURL_ES);
		features_MateParser_ES.put("srlModelPath", arg_DRIresFolderPath + srlModelURL_ES);
		MateParser MateParser_ES = null;
		try {
			MateParser_ES = (MateParser) gate.Factory.createResource(MateParser.class.getName(), features_MateParser_ES);
		} catch (ResourceInstantiationException e2) {
			e2.printStackTrace();
		}
		MateParsersLang_Resource.put(LangENUM.ES, MateParser_ES);
		System.out.println("INIT: Parser (Spanish) initialized.");

		File dirToProcess = new File(arg_localFolderPathToProcess);
		Collection<File> fileToProcess = FileUtils.listFiles(dirToProcess, FileFilterUtils.suffixFileFilter("v4.xml"), TrueFileFilter.INSTANCE);

		System.out.println("");
		System.out.println("******************************************************************************");
		System.out.println(" - Directory to process: " + dirToProcess.getAbsolutePath());
		System.out.println(" - Number of DRI XML files in directory: " + fileToProcess.size());
		System.out.println("******************************************************************************");
		System.out.println("");
		System.out.println("");

		for(File fileToProc : fileToProcess) {
			if(fileToProc != null && fileToProc.exists() && fileToProc.isFile() && fileToProc.getName().endsWith(".xml")) {
				try {

					if(arg_fileNameStartsWith != null && arg_fileNameStartsWith.length() > 0) {
						if(!fileToProc.getName().startsWith(arg_fileNameStartsWith)) {
							System.out.println("Skipped file since it does not start with '" + arg_fileNameStartsWith + "'.");
							continue;
						}
					}

					System.out.println("Start analyzing file: '" + fileToProc.getName() + "'...");

					gate.Document driDoc = gate.Factory.newDocument(fileToProc.toURI().toURL(), "UTF-8");

					// Process document
					// STEP 0: Parse Spanish texts
					long startProcess = System.currentTimeMillis();

					try {

						boolean isLangAware = true;

						// STEP 1: Abstract(s) sentence parsing
						AnnotationSet abstractsAnnSet = driDoc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.abstractAnnType);
						long abstractEndOffset = 0l;
						for(Iterator<Annotation> iter = abstractsAnnSet.iterator(); iter.hasNext(); ) {
							Annotation abstractAnn = iter.next();
							if(abstractAnn != null) {
								if(abstractAnn.getEndNode().getOffset() > abstractEndOffset) {
									abstractEndOffset = abstractAnn.getEndNode().getOffset();
								}

								List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrderIntersectAnn(driDoc, ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractAnn);

								System.out.println("Start parsing " + sentenceAnnList.size() + " sentences from abstract...");
								MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, driDoc, sentenceAnnList, 
										ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);

							}

						}


						// STEP 2: Main body sentence parsing
						List<Annotation> mainBodyAnnotationList = GateUtil.getAnnInDocOrderIntersectOffset(driDoc, 
								ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, abstractEndOffset, gate.Utils.lengthLong(driDoc));
						System.out.println("Start parsing " + mainBodyAnnotationList.size() + " sentences from main body...");
						MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, driDoc, mainBodyAnnotationList, 
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
							List<Annotation> selectedAnnotations = GateUtil.getAnnInDocOrder(driDoc, ImporterBase.driAnnSet, headerAnnType);
							for(Annotation selectedAnn : selectedAnnotations) {
								System.out.println("Start parsing " + mainBodyAnnotationList.size() + " sentences from " + headerAnnType + "...");
								MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, driDoc, selectedAnn, 
										ImporterBase.driAnnSet, headerAnnType, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
							}
						}

						List<Annotation> keywordListAnnotations = GateUtil.getAnnInDocOrder(driDoc, ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTkeywordsText);
						if(keywordListAnnotations != null && keywordListAnnotations.size() > 0) {
							System.out.println("Start parsing " + keywordListAnnotations.size() + " keywords from " + ImporterPDFEXT.PDFEXTAnnSet + "/" + ImporterPDFEXT.PDFEXTkeywordsText + "...");
							for(Annotation keywordAnn : keywordListAnnotations) {
								MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, driDoc, keywordAnn, 
										ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTkeywordsText, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
							}
						}

						List<Annotation> affiliationListAnnotations = GateUtil.getAnnInDocOrder(driDoc, ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTaffiliation);
						if(affiliationListAnnotations != null && affiliationListAnnotations.size() > 0) {
							System.out.println("Start parsing " + affiliationListAnnotations.size() + " affiliations from " + ImporterPDFEXT.PDFEXTAnnSet + "/" + ImporterPDFEXT.PDFEXTaffiliation + "...");
							for(Annotation affilAnn : affiliationListAnnotations) {
								MateParser.languageAwareAnnotationParsing(isLangAware, MateParsersLang_Resource, driDoc, affilAnn, 
										ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTaffiliation, ImporterBase.driAnnSet, ImporterBase.tokenAnnType);
							}
						}


						System.out.println("Extract Graph - executed in " + (System.currentTimeMillis() - startProcess) + " ms.");
					} catch (Exception e) {
						logger.warn("Exception: " + e.getMessage());
						e.printStackTrace();
						throw new InternalProcessingException("Error while extracting sentence graph (parser)");
					}

					// Store document
					Writer out = null;
					try {
						out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(fileToProc.getParentFile().getAbsolutePath() + File.separator + fileToProc.getName().replace(".xml", "_ESpars.xml")), "UTF-8"));
						out.write(driDoc.toXml());
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						if(out != null) {
							out.close();
						}
					}

					// Clean results
					driDoc.cleanup();

					System.gc();

				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Error while processing document " + ((fileToProc != null) ? fileToProc.getAbsolutePath() : "NULL_FILE"));
				}
			}
			else {
				System.out.println("Skipped file: " + fileToProc.getName() + " - it is not a .xml file.");
			}

		}

	}

}
