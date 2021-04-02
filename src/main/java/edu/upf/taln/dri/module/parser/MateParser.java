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
package edu.upf.taln.dri.module.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.ResourceAccessException;
import edu.upf.taln.dri.lib.model.ext.LangENUM;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.languageDetector.LanguageDetector;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.SemanticRoleLabeler;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.pipeline.Pipeline;
import se.lth.cs.srl.pipeline.Reranker;
import se.lth.cs.srl.pipeline.Step;
import se.lth.cs.srl.preprocessor.Preprocessor;



/**
 * Citation-aware Mate-tools parser (pos tagger, lemmatizer, dep parser and semantic role labeller)
 * REF: https://code.google.com/p/mate-tools/
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Citation Aware MATE Parser")
public class MateParser extends AbstractLanguageAnalyser implements ProcessingResource, Serializable, DRIModule {

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static Logger logger = Logger.getLogger(MateParser.class);

	public static final String posFeat = "category";
	public static final String lemmaFeat = "lemma";
	public static final String gateIdFeat = "gateId";
	public static final String depKindFeat = "depFunct";
	public static final String depTargetIdFeat = "depTargetId";
	public static final String depInternalIdFeat = "depInternalId";
	public static final String SRLpartTagNFeat = "srlP_tag_";
	public static final String SRLpartSenseNFeat = "srlP_sense_";
	public static final String SRLpartRoodIdNFeat = "srlP_root_";
	public static final String SRLrootSenseFeat = "srlR_sense";

	private Preprocessor pp = null;
	private SemanticRoleLabeler srl = null;

	// URLs of the model for lemmatizer, POS tagger, morphological analyzer and tagger
	private String lemmaModelPath = null;
	private String postaggerModelPath = null;
	private String parserModelPath = null;
	private String srlModelPath = null;

	// Input set for annotation
	private String sentenceAnnotationSetToAnalyze = ImporterBase.driAnnSet;
	private String sentenceAnnotationTypeToAnalyze = ImporterBase.sentenceAnnType;
	private String tokenAnnotationSetToAnalyze = ImporterBase.driAnnSet;
	private String tokenAnnotationTypeToAnalyze = ImporterBase.tokenAnnType;
	private Set<String> sentenceIdsToAnalyze = new HashSet<String>();
	private Integer excludeThreshold;

	// Citances management
	private Boolean citancesEnabled = false;
	private String citeSpanAnnotationSetToExclude = ImporterBase.driAnnSet;
	private String citeSpanAnnotationTypeToExclude = ImporterBase.inlineCitationAnnType;

	public String getSentenceAnnotationSetToAnalyze() {
		return sentenceAnnotationSetToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "Set name of the annotation set where the sentences to parse are annotated")
	public void setSentenceAnnotationSetToAnalyze(
			String sentenceAnnotationSetToAnalyze) {
		this.sentenceAnnotationSetToAnalyze = sentenceAnnotationSetToAnalyze;
	}

	public String getSentenceAnnotationTypeToAnalyze() {
		return sentenceAnnotationTypeToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Sentence", comment = "The type of sentence annotations")
	public void setSentenceAnnotationTypeToAnalyze(
			String sentenceAnnotationTypeToAnalyze) {
		this.sentenceAnnotationTypeToAnalyze = sentenceAnnotationTypeToAnalyze;
	}

	public String getTokenAnnotationSetToAnalyze() {
		return tokenAnnotationSetToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "Set name of the annotation set where the token of the sentences to parse are annotated")
	public void setTokenAnnotationSetToAnalyze(String tokenAnnotationSetToAnalyze) {
		this.tokenAnnotationSetToAnalyze = tokenAnnotationSetToAnalyze;
	}

	public String getTokenAnnotationTypeToAnalyze() {
		return tokenAnnotationTypeToAnalyze;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Token", comment = "The type of token annotations")
	public void setTokenAnnotationTypeToAnalyze(String tokenAnnotationTypeToAnalyze) {
		this.tokenAnnotationTypeToAnalyze = tokenAnnotationTypeToAnalyze;
	}

	public Integer getExcludeThreshold() {
		return this.excludeThreshold;
	}

	@RunTime
	@CreoleParameter(defaultValue = "0", comment = "The value of exclude threshold of the parser.")
	public void setExcludeThreshold(Integer excludeThreshold) {
		this.excludeThreshold = excludeThreshold;
	}

	public Boolean getCitancesEnabled() {
		return citancesEnabled;
	}

	@CreoleParameter(defaultValue = "false", comment = "Make the parser aware of the presence of cite span annotations in order "
			+ "to properly manage while parsing sentences. If set to false, the parameters citeSpanAnnotationSetToExclude and citeSpanAnnotationTypeToExclude "
			+ "have no validity.")
	public void setCitancesEnabled(Boolean citancesEnabled) {
		this.citancesEnabled = citancesEnabled;
	}

	public String getCiteSpanAnnotationSetToExclude() {
		return citeSpanAnnotationSetToExclude;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the annotation set that includes cite span annotaitons. Valid only if citancesEnabled is true.")
	public void setCiteSpanAnnotationSetToExclude(
			String citeSpanAnnotationSetToExclude) {
		this.citeSpanAnnotationSetToExclude = citeSpanAnnotationSetToExclude;
	}

	public String getCiteSpanAnnotationTypeToExclude() {
		return citeSpanAnnotationTypeToExclude;
	}

	@RunTime
	@CreoleParameter(defaultValue = "CitSpan", comment = "The name of the annotation type of cite span annotaitons. Valid only if citancesEnabled is true.")
	public void setCiteSpanAnnotationTypeToExclude(
			String citeSpanAnnotationTypeToExclude) {
		this.citeSpanAnnotationTypeToExclude = citeSpanAnnotationTypeToExclude;
	}

	public String getLemmaModelPath() {
		return lemmaModelPath;
	}
	
	@RunTime
	@CreoleParameter(defaultValue = "", comment = "The ids of all the sentence type annotations to parse. If empty or null all annotations of sentence type will be parsed.")
	public void setSentenceIdsToAnalyze(Set<String> sentenceIdsToAnalyze) {
		this.sentenceIdsToAnalyze = sentenceIdsToAnalyze;
	}
	
	public Set<String> getSentenceIdsToAnalyze() {
		return sentenceIdsToAnalyze;
	}

	@RunTime
	@CreoleParameter(comment = "Full path to the lemmatizer model.")
	public void setLemmaModelPath(String lemmaModelPath) {
		this.lemmaModelPath = lemmaModelPath;
	}

	public String getPostaggerModelPath() {
		return postaggerModelPath;
	}

	@RunTime
	@CreoleParameter(comment = "Full path to the POS tagger model.")
	public void setPostaggerModelPath(String postaggerModelPath) {
		this.postaggerModelPath = postaggerModelPath;
	}

	public String getParserModelPath() {
		return parserModelPath;
	}

	@RunTime
	@CreoleParameter(comment = "Full path to the dep parser model.")
	public void setParserModelPath(String parserModelPath) {
		this.parserModelPath = parserModelPath;
	}

	public String getSrlModelPath() {
		return srlModelPath;
	}

	@RunTime
	@CreoleParameter(comment = "Full path to the semantic role labeller model.")
	public void setSrlModelPath(String srlModelPath) {
		this.srlModelPath = srlModelPath;
	}


	@Override
	public Resource init() {
		try {

			List<String> argumentList = new ArrayList<String>();
			argumentList.add("eng");

			if(lemmaModelPath != null && (new File(lemmaModelPath)).exists() && (new File(lemmaModelPath)).isFile()) {
				argumentList.add("-lemma");
				argumentList.add(lemmaModelPath);
			}
			else {
				logger.warn("Lemmatizer model file not provided or invalid");
			}

			if(postaggerModelPath != null && (new File(postaggerModelPath)).exists() && (new File(postaggerModelPath)).isFile()) {
				argumentList.add("-tagger");
				argumentList.add(postaggerModelPath);
			}
			else {
				logger.warn("POStagger model file not provided or invalid");
			}

			if(parserModelPath != null && (new File(parserModelPath)).exists() && (new File(parserModelPath)).isFile()) {
				argumentList.add("-parser");
				argumentList.add(parserModelPath);
			}
			else {
				logger.warn("Dep parser model file not provided or invalid");
			}

			if(srlModelPath != null && (new File(srlModelPath)).exists() && (new File(srlModelPath)).isFile()) {
				argumentList.add("-srl");
				argumentList.add(srlModelPath);
			}
			else {
				logger.warn("SRL model file not provided or invalid");
			}

			// Set options
			String[] arguments = argumentList.toArray(new String[argumentList.size()]);

			CompletePipelineCMDLineOptions options = new CompletePipelineCMDLineOptions();
			options.parseCmdLineArgs(arguments);

			pp = Language.getLanguage().getPreprocessor(options);
			Parse.parseOptions = options.getParseOptions();

			if (options.reranker) {
				srl = new Reranker(Parse.parseOptions);
			} else {
				ZipFile zipFile = new ZipFile(Parse.parseOptions.modelFile);
				if (Parse.parseOptions.skipPI) {
					srl = Pipeline.fromZipFile(zipFile, new Step[] { Step.pd, Step.ai, Step.ac });
				} else {
					srl = Pipeline.fromZipFile(zipFile);
				}
				zipFile.close();
			}

		} catch (Exception e) {
			Util.notifyException("Initializing Mate-tools", e, logger);
		}

		return this;
	}

	@Override
	public void execute() {

		int parsedSentences = 0;

		long t1 = System.currentTimeMillis();

		// Reference to the current document to parse
		Document doc = getDocument();
		logger.debug("   - Start parsing document: " + ((doc.getName() != null && doc.getName().length() > 0) ? doc.getName() : "NO_NAME") );

		int threshold = getExcludeThreshold();

		// Check input parameters
		sentenceAnnotationSetToAnalyze = StringUtils.defaultString(sentenceAnnotationSetToAnalyze, ImporterBase.driAnnSet);
		sentenceAnnotationTypeToAnalyze = StringUtils.defaultString(sentenceAnnotationTypeToAnalyze, ImporterBase.sentenceAnnType);
		tokenAnnotationSetToAnalyze = StringUtils.defaultString(tokenAnnotationSetToAnalyze, ImporterBase.driAnnSet);
		tokenAnnotationTypeToAnalyze = StringUtils.defaultString(tokenAnnotationTypeToAnalyze, ImporterBase.tokenAnnType);
		citeSpanAnnotationSetToExclude = StringUtils.defaultString(citeSpanAnnotationSetToExclude, ImporterBase.driAnnSet);
		citeSpanAnnotationTypeToExclude = StringUtils.defaultString(citeSpanAnnotationTypeToExclude, ImporterBase.inlineCitationAnnType);

		// Get all the sentence annotations (sentenceAnnotationSet) from the input annotation set (inputAnnotationSet)
		AnnotationSet inputAnnotationSet = document.getAnnotations(sentenceAnnotationSetToAnalyze);
		AnnotationSet sentenceAnnotationSet = inputAnnotationSet.get(sentenceAnnotationTypeToAnalyze);

		AnnotationSet citeAnnotationSet = document.getAnnotations(citeSpanAnnotationSetToExclude);
		
		// Sort sentences
		List<Annotation> sentencesSorted = sortSetenceList(sentenceAnnotationSet);
		
		if(sentenceIdsToAnalyze != null && sentenceIdsToAnalyze.size() > 0) {
			Set<Integer> sentenceIdsToAnalyzeInt = new HashSet<Integer>();
			for(String sentIdToAnalyzeString : sentenceIdsToAnalyze) {
				try {
					if(sentIdToAnalyzeString != null && sentIdToAnalyzeString.trim().length() > 0) {
						Integer sentIdToAnalyzeInt = Integer.valueOf(sentIdToAnalyzeString);
						sentenceIdsToAnalyzeInt.add(sentIdToAnalyzeInt);
					}
				}
				catch(Exception e) {
					/* Do nothing */
				}
			}
			
			int originalSentCount = sentencesSorted.size();
			int removedCount = 0;
			for (Iterator<Annotation> iter = sentencesSorted.iterator(); iter.hasNext(); ) {
				Annotation ann = iter.next();
			    if (ann != null & ann.getId() != null && !sentenceIdsToAnalyzeInt.contains(ann.getId())) {
			        iter.remove();
			        removedCount++;
			    }
			}
			
			logger.info("   - Enabled sentence ID filter --> sentences to parse: " + sentencesSorted.size() + " (num sentences filtered out: " + removedCount + " over " + originalSentCount + ")");
		}
		
		parsedSentences += annotateSentences(sentencesSorted, doc, threshold, citeAnnotationSet);
		
		long needed = System.currentTimeMillis() - t1;
		logger.debug("   - End parsing document: " + doc.getName());
		logger.debug("     in (seconds): " + (needed / 1000) + ", parsed: " + parsedSentences + ", unparsed: " + (sentencesSorted.size() - parsedSentences) );
		logger.debug("********************************************");
	}


	/**
	 * Annotate by means of the parser a set of sentences 
	 * 
	 * @param sentencesSorted list of sentences to annotate
	 * @param doc document the sentences belong to
	 * @param t threshold for the parser
	 * @return
	 */
	public int annotateSentences(List<Annotation> sentencesSorted, Document doc, int t, AnnotationSet citeAnnotationSet) {

		int parsedSentences = 0;

		for (Annotation actualSentence : sentencesSorted) {

			try {

				// References to the document (actualDoc) and the sentence (actualSentence) to parse
				Document actualDoc = doc;

				// Sentence parsing approach: if the sentence contains a cite, isCitingSentence is set to true and the actualDoc is substituted with a new
				// GATE document containing only the citing sentence to parse with the tokens included in the citation properly replaced. In particular:
				//    - if the citation is syntactic, only the first token included in the citation span is preserved as a sentence token and its
				//      string property is set equal to CITATION
				//    - if the citation is not syntactic, all the tokens included in the citation spans are removed
				boolean isCitingSentence = false;
				boolean isSyntacticCite = false;
				Map<Integer, Integer> newOldTokenIdMap = new HashMap<Integer, Integer>();

				// *******************************************************************
				// *******************************************************************
				// Properly modify the sentence tokens if the sentence contains a cite
				if(this.citancesEnabled && StringUtils.isNotBlank(this.citeSpanAnnotationTypeToExclude)) {

					// Define the set of cite spans that are included in the actualSentence under analysis
					List<Annotation> citationsOverlappingActualSentenceList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(actualDoc, this.citeSpanAnnotationSetToExclude,
																																	   this.citeSpanAnnotationTypeToExclude, actualSentence);

					// If there are overlapping cite spans, the sentence tokens should be modified - new actualDoc and actualSentence
					if(citationsOverlappingActualSentenceList.size() > 0) {
						isCitingSentence = true;

						// GENERATE NEW DOCUMENT AND SENTENCE - START
						// Get the initial offset ID of the sentence
						Long sentenceOffset = actualSentence.getStartNode().getOffset(); // To subtract to the get the offsets in the new GATE doc with a single sentence
						// Get the list of tokens included in the sentence
						List<Annotation> orderedSentenceTokens_TR = gate.Utils.inDocumentOrder(actualDoc.getAnnotations(tokenAnnotationSetToAnalyze).get(tokenAnnotationTypeToAnalyze).getContained(actualSentence.getStartNode().getOffset(), actualSentence.getEndNode().getOffset()));

						// Generate new actualDoc and actualSentence
						Optional<String> sentenceText = GateUtil.getAnnotationText(actualSentence, actualDoc);

						if(!sentenceText.isPresent()) {
							break;
						}

						logger.debug("Start processing citing sentence: " + sentenceText.get());

						try {
							actualDoc = Factory.newDocument(sentenceText.get());
						} catch (ResourceInstantiationException e) {
							e.printStackTrace();
						}

						// Generate sentence annotation of new Document
						Integer sentenceAnnInt = -1;
						try {
							sentenceAnnInt = actualDoc.getAnnotations(this.sentenceAnnotationSetToAnalyze).add(gate.Utils.start(actualDoc), gate.Utils.end(actualDoc), this.sentenceAnnotationTypeToAnalyze, Factory.newFeatureMap());
						} catch (InvalidOffsetException e) {
							e.printStackTrace();
						}

						actualSentence = actualDoc.getAnnotations(this.sentenceAnnotationSetToAnalyze).get(sentenceAnnInt);

						// Propagate token annotations on the new document
						for(Annotation originalToken : orderedSentenceTokens_TR) {
							if(originalToken != null) {
								try {
									Integer oldTokenId = originalToken.getId();
									FeatureMap fm = Factory.newFeatureMap();
									fm.put("originalTokenID", originalToken.getId());
									fm.put("string", originalToken.getFeatures().get("string"));
									Integer newTokenId = actualDoc.getAnnotations(this.tokenAnnotationSetToAnalyze).add(
											(originalToken.getStartNode().getOffset() - sentenceOffset),
											(originalToken.getEndNode().getOffset() - sentenceOffset),
											this.tokenAnnotationTypeToAnalyze, fm);
									// Store the correspondence of the new and old token IDs
									newOldTokenIdMap.put(newTokenId, oldTokenId);
								} catch (InvalidOffsetException e) {
									e.printStackTrace();
								}
							}
						}
						// GENERATE NEW DOCUMENT AND SENTENCE - END

						// For each citation, check:
						// - if single or multiple citation
						// - if syntactic
						for(Annotation cite : citationsOverlappingActualSentenceList) {
							if(cite != null) {
								// Overlapping tokens
								Optional<String> citeText = GateUtil.getDocumentText( actualDoc, (cite.getStartNode().getOffset() - sentenceOffset),
										(cite.getEndNode().getOffset() - sentenceOffset) );

								AnnotationSet citeTokens = actualDoc.getAnnotations(tokenAnnotationSetToAnalyze).get(tokenAnnotationTypeToAnalyze).getContained(
										(cite.getStartNode().getOffset() - sentenceOffset),
										(cite.getEndNode().getOffset() - sentenceOffset));
								if(citeText.isPresent() && citeTokens != null && citeTokens.size() > 0) {

									// *** CHECK IF SYNTACTIC CITE - START
									isSyntacticCite = false;

									if((cite.getStartNode().getOffset() - sentenceOffset) <= (actualSentence.getStartNode().getOffset() + 2l) ) {
										// The citation is the first word in the considered sentence
										isSyntacticCite = true;
										logger.debug("isSyntacticCite : The citation is the first word in the considered sentence");
									}
									else if(!citeText.get().trim().startsWith("(") && !citeText.get().trim().startsWith("[") && !citeText.get().trim().startsWith("{")) {
										// The citation does not start with a parenthesis
										isSyntacticCite = true;
										logger.debug("isSyntacticCite : The citation does not start with a parenthesis");
									}
									if(!isSyntacticCite) {
										// Check if the preceding token is a cue word
										List<Annotation> orderedSentenceTokens = gate.Utils.inDocumentOrder(actualDoc.getAnnotations(tokenAnnotationSetToAnalyze).get(tokenAnnotationTypeToAnalyze).getContained(actualSentence.getStartNode().getOffset(), actualSentence.getEndNode().getOffset()));
										Long greatestStartOffset = 0l;
										String preceedingTokenText = null;
										for(Annotation token : orderedSentenceTokens) {
											if(token != null && token.getEndNode().getOffset() <= (cite.getStartNode().getOffset() - sentenceOffset)
													&& token.getStartNode().getOffset() >= greatestStartOffset) {
												String tokenText = null;
												try {
													tokenText = actualDoc.getContent().getContent(token.getStartNode().getOffset(), token.getEndNode().getOffset()).toString();
												} catch (InvalidOffsetException e) {
													e.printStackTrace();
												}
												if(tokenText != null && !tokenText.equals("") && tokenText.length() > 1) {
													greatestStartOffset = token.getStartNode().getOffset();
													preceedingTokenText = tokenText;
												}
											}
										}

										if(preceedingTokenText != null && preceedingTokenText.length() > 1) {
											preceedingTokenText = preceedingTokenText.trim().toLowerCase();
											if(preceedingTokenText.equals("by")) {
												isSyntacticCite = true;
												logger.debug("isSyntacticCite : preceeded by: by");
											}
											else if(preceedingTokenText.equals("in")) {
												isSyntacticCite = true;
												logger.debug("isSyntacticCite : preceeded by: in");
											}
											else if(preceedingTokenText.equals("of")) {
												isSyntacticCite = true;
												logger.debug("isSyntacticCite : preceeded by: of");
											}
											else if(preceedingTokenText.equals("see")) {
												isSyntacticCite = true;
												logger.debug("isSyntacticCite : preceeded by: see");
											}
											else if(preceedingTokenText.equals("to")) {
												isSyntacticCite = true;
												logger.debug("isSyntacticCite : preceeded by: see");
											}
											// IMPORTANT: improve cue list!!!
										}

									}
									// *** CHECK IF SYNTACTIC CITE - END

									// If the cite sentence is not syntactic, remove all tokens from the sentence span,
									// if not remove all token except the first one and put the string property of this token equal to the word CITATION.
									List<Annotation> citeTokensOrdered = gate.Utils.inDocumentOrder(citeTokens);
									if(isSyntacticCite) {
										cite.getFeatures().put("syntactic", true);
										citeTokensOrdered.get(0).getFeatures().put("string", "CITATION");
										AnnotationSet tokenAnnSet = actualDoc.getAnnotations(this.tokenAnnotationSetToAnalyze);
										for(int i = 1; i < citeTokensOrdered.size(); i++) {
											tokenAnnSet.remove(citeTokensOrdered.get(i));
										}
									}
									else {
										cite.getFeatures().put("syntactic", false);
										AnnotationSet tokenAnnSet = actualDoc.getAnnotations(this.tokenAnnotationSetToAnalyze);
										for(int i = 0; i < citeTokensOrdered.size(); i++) {
											tokenAnnSet.remove(citeTokensOrdered.get(i));
										}
									}
								}
							}
						}
						// Finished to report citations

					}
				}
				// *******************************************************************
				// *******************************************************************

				// Generated sorted token list
				List<Annotation> sortedTokens = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(actualDoc, tokenAnnotationSetToAnalyze, tokenAnnotationTypeToAnalyze, actualSentence);

				if(sortedTokens == null || sortedTokens.size() < 1) {
					continue;
				}

				List<String> tokensToProcess = new ArrayList<String>();
				tokensToProcess.add("<root>");
				for (int i = 0; i < sortedTokens.size(); i++) {
					tokensToProcess.add(String.valueOf(sortedTokens.get(i).getFeatures().get("string")));
				}

				// **********************************************
				// PROCESS:
				Sentence s = null;

				if(tokensToProcess.size() > 0 && tokensToProcess.size() <= t) {

					String sentenceToParse = null;
					sentenceToParse = tokensToProcess.stream().collect(Collectors.joining(" "));
					logger.debug("Parsing sentence: " + sentenceToParse);

					sentenceToParse = sentenceToParse.replace("\n", " ").trim();

					if(StringUtils.isNotBlank(sentenceToParse)) {
						s = new Sentence(pp.preprocess(tokensToProcess.toArray(new String[tokensToProcess.size()])));
						srl.parseSentence(s);
						parsedSentences++;
					}
				}
				else {
					Optional<String> annText = GateUtil.getAnnotationText(actualSentence, actualDoc);
					logger.debug("Impossible to parse the sentence " + ((tokensToProcess.size() > t) ? "(token size " + tokensToProcess.size() + " greater than threshold t " + t + ")" : "" ) + ": " + annText.orElse("NOT_PRESENT"));
				}

				Annotation token = null;
				FeatureMap fm = null;
				for(int w = 0; w < sortedTokens.size(); w++) {
					token = sortedTokens.get(w);
					fm = token.getFeatures();

					if(s != null) {
						Word word = s.get(w+1);

						// The following two annotations are internal to the parser
						// fm.put("seq", w);
						fm.put(gateIdFeat, token.getId());

						// WORD FEATS
						fm.put(posFeat, StringUtils.defaultString(word.getPOS(), ""));
						fm.put(lemmaFeat, StringUtils.defaultString(word.getLemma(), ""));

						// DEP PARSER
						fm.put(depInternalIdFeat, word.getHeadId());
						
						String depRel = word.getDeprel();
						if(depRel != null && depRel.equals("sentence")) {
							depRel = "ROOT"; // sentence is the root dep rel in Spanish parsing
						}
						fm.put(depKindFeat, StringUtils.defaultString(depRel, ""));
						if(word.getHeadId() > 0) {
							fm.put(depTargetIdFeat, sortedTokens.get(word.getHeadId() - 1).getId());
						}

						// SRL
						List<Predicate> predicates = s.getPredicates();
						for (int j = 0; j < predicates.size(); ++j) {
							Predicate pred = predicates.get(j);
							String tag = pred.getArgumentTag(word);
							if (StringUtils.isNotBlank(tag)) {
								Integer SRLid = 1;
								while(fm.containsKey("srlA_tag_" + SRLid)) {
									SRLid++;
									if(SRLid > 30) break;
								}
								fm.put(SRLpartTagNFeat + SRLid, StringUtils.defaultString(tag, ""));
								fm.put(SRLpartRoodIdNFeat + SRLid, sortedTokens.get(pred.getIdx() - 1).getId());
								fm.put(SRLpartSenseNFeat + SRLid, StringUtils.defaultString(pred.getSense(), ""));
							}
							else if(pred.getIdx() == w+1) {
								fm.put(SRLrootSenseFeat, StringUtils.defaultString(pred.getSense(), ""));
							}
						}
					}
				}

				// ****************************

				// Report back parsing results to the original document	if the sentence analyzed contains a cite, since we have replaced to actualDoc and
				// actualSentence a new GATE document without the tokens included in the citation span.
				if(isCitingSentence) {
					for(int w = 0; w < sortedTokens.size(); w++) {
						Annotation tokenAnn = sortedTokens.get(w);
						Integer originalTokenId = (Integer) tokenAnn.getFeatures().get("originalTokenID");

						Annotation originalToken = doc.getAnnotations(this.tokenAnnotationSetToAnalyze).get(originalTokenId);
						if(originalToken != null) {
							FeatureMap originalTokenFm = originalToken.getFeatures();
							originalTokenFm.put(gateIdFeat, originalToken.getId());

							// Add features
							// WORD FEATS
							if(tokenAnn.getFeatures().get(posFeat) != null) {
								originalTokenFm.put(posFeat, tokenAnn.getFeatures().get(posFeat));
							}
							if(tokenAnn.getFeatures().get(lemmaFeat) != null) {
								originalTokenFm.put(lemmaFeat, tokenAnn.getFeatures().get(lemmaFeat));
							}

							// DEP PARSER
							if(tokenAnn.getFeatures().get(depInternalIdFeat) != null) {
								originalTokenFm.put(depInternalIdFeat, tokenAnn.getFeatures().get(depInternalIdFeat));
							}
							if(tokenAnn.getFeatures().get(depKindFeat) != null) {
								originalTokenFm.put(depKindFeat, tokenAnn.getFeatures().get(depKindFeat));
							}
							if(tokenAnn.getFeatures().containsKey(depTargetIdFeat) && newOldTokenIdMap.containsKey((Integer) tokenAnn.getFeatures().get(depTargetIdFeat))) {
								originalTokenFm.put(depTargetIdFeat, newOldTokenIdMap.get((Integer) tokenAnn.getFeatures().get(depTargetIdFeat)));
							}

							// SRL
							boolean foundSRL = true;
							Integer SRLid = 1;
							while(foundSRL) {
								if(tokenAnn.getFeatures().get(SRLpartTagNFeat + SRLid) != null) {
									originalTokenFm.put(SRLpartTagNFeat + SRLid, tokenAnn.getFeatures().get(SRLpartTagNFeat + SRLid));
								}
								else {
									foundSRL = false;
								}
								if(tokenAnn.getFeatures().containsKey(SRLpartRoodIdNFeat + SRLid) && newOldTokenIdMap.containsKey((Integer) tokenAnn.getFeatures().get(SRLpartRoodIdNFeat + SRLid))) {
									originalTokenFm.put(SRLpartRoodIdNFeat + SRLid, newOldTokenIdMap.get((Integer) tokenAnn.getFeatures().get(SRLpartRoodIdNFeat + SRLid)));
								}
								if(tokenAnn.getFeatures().get(SRLpartSenseNFeat + SRLid) != null) {
									originalTokenFm.put(SRLpartSenseNFeat + SRLid, tokenAnn.getFeatures().get(SRLpartSenseNFeat + SRLid));
								}
								SRLid++;
							}
							if(tokenAnn.getFeatures().get(SRLrootSenseFeat) != null) {
								originalTokenFm.put(SRLrootSenseFeat, tokenAnn.getFeatures().get(SRLrootSenseFeat));
							}

						}
						else {
							logger.debug("ATTENTION: cannot report back citation annotations to original token: " + ((originalTokenId != null)? originalTokenId : "NULL"));
						}
					}

					// Delete the temporary actual document built
					actualDoc.cleanup();
					Factory.deleteResource(actualDoc);
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("Error parsing sentence: " + ((actualSentence != null) ? actualSentence.toString() : "NULL"));
			}
			
		}

		return parsedSentences;
	}


	/**
	 * Given a {@link AnnotationSet} instance, returns a sorted list of its elements.
	 * Sorting is done by position (offset) in the document.
	 * 
	 * @param sentences {@link Annotation}
	 * 
	 * @return Sorted list of {@link Annotation} instances.
	 */
	public List<Annotation> sortSetenceList(AnnotationSet sentences) {
		List<Annotation> sentencesSorted = new ArrayList<Annotation>(sentences);
		Collections.sort(sentencesSorted, new OffsetComparator());
		return sentencesSorted;
	}
	
	/**
	 * Given a map of parsers instances for different languages, determine the majority language from the list of sentence annotations
	 * and parse all the sentence annotations by means of the parser for that language.
	 * 
	 * If the list of sentence annotations is null or empty all the annotation of the sentence type specified are parsed.
	 * 
	 * @param isLangAware if false, always the English parser will be used
	 * @param parsersLangMap
	 * @param doc
	 * @param sentenceAnnList
	 * @param sentAnnSet
	 * @param sentAnnType
	 * @param tokenAnnSet
	 * @param tokenAnnType
	 * @throws ResourceAccessException 
	 */
	public static void languageAwareAnnotationParsing(boolean isLangAware, Map<LangENUM, MateParser> parsersLangMap, Document doc, List<Annotation> sentenceAnnList,
			String sentAnnSet, String sentAnnType, String tokenAnnSet, String tokenAnnType) {
		
		String selectedMajorityLang = "";
		if(sentenceAnnList != null && sentenceAnnList.size() > 0) {
			selectedMajorityLang = LanguageDetector.getMajorityLanguage(sentenceAnnList, ImporterBase.langAnnFeat);
		}
		
		// Check if exactly one parse has been instantiated
		if(parsersLangMap != null && parsersLangMap.size() != 1) {
			logger.info("ATTENTION: Language-specific parsers map contains more than one instantiated parser!");
			return;
		}
		
				
		// Check if a parser is present for the languageToParse
		LangENUM langOfTheParser = parsersLangMap.keySet().iterator().next();
		if(isLangAware) {
			if((selectedMajorityLang.toLowerCase().equals("en") || selectedMajorityLang.toLowerCase().equals("")) && !langOfTheParser.equals(LangENUM.EN)) {
				logger.info("ATTENTION: Asking to parse " + selectedMajorityLang + " texts, but only " + langOfTheParser + " parser available.");
				return;
			}
			
			if(selectedMajorityLang.toLowerCase().equals("es") && !langOfTheParser.equals(LangENUM.ES)) {
				logger.info("ATTENTION: Asking to parse " + selectedMajorityLang + " texts, but only " + langOfTheParser + " parser available.");
				return;
			}
		}
		
		
		// Select parser on the basis of the majority language
		MateParser selectedParser = null;
		if(isLangAware) {
			selectedParser = (selectedMajorityLang != null && selectedMajorityLang.toLowerCase().equals("es")) ? parsersLangMap.get(LangENUM.ES) : parsersLangMap.get(LangENUM.EN);
		}
		else { // Use English parser
			selectedParser = parsersLangMap.get(LangENUM.EN);
			logger.warn("You're parsing a text by means of the English parser, even if the language tag of its text is " + selectedMajorityLang);
		}
		
		if(selectedParser == null) {
			selectedParser = (parsersLangMap.get(LangENUM.EN) != null) ? parsersLangMap.get(LangENUM.EN) : parsersLangMap.get(LangENUM.ES);
		}
		if(selectedParser == null) {
			logger.info("ATTENTION: Impossible to select an instantiated parser!!!");
			return;
		}
		
		// Store original setting of parser
		String original_sentenceAnnotationSet = new String(selectedParser.getSentenceAnnotationSetToAnalyze());
		String original_sentenceAnnotationType = new String(selectedParser.getSentenceAnnotationTypeToAnalyze());
		String original_tokenAnnotationSet = new String(selectedParser.getTokenAnnotationSetToAnalyze());
		String original_tokenAnnotationType = new String(selectedParser.getTokenAnnotationTypeToAnalyze());
		
		// Set sentence and token annotation types
		selectedParser.setSentenceAnnotationSetToAnalyze(sentAnnSet);
		selectedParser.setSentenceAnnotationTypeToAnalyze(sentAnnType);
		selectedParser.setTokenAnnotationSetToAnalyze(tokenAnnSet);
		selectedParser.setTokenAnnotationTypeToAnalyze(tokenAnnType);
		
		logger.info("Parsing " + ((sentenceAnnList != null) ? sentenceAnnList.size() : "NULL") + " sentences "
				+ "with language " + selectedMajorityLang);
		
		// Eventually limit the parsing only to a set of sentence IDs
		if(sentenceAnnList != null && sentenceAnnList.size() > 0) {
			List<Integer> sentenceAnnIDs = GateUtil.fromAnnListToAnnIDlist(sentenceAnnList);
			selectedParser.setSentenceIdsToAnalyze(Util.fromListIntToSetString(sentenceAnnIDs));
		}
		else {
			selectedParser.setSentenceIdsToAnalyze(null);
		}
		
		selectedParser.setDocument(doc);
		
		// Execute parsing
		// ATTENTION two parsers can't be instantiated at the same time!
		// Exclude Spanish sentence parsing --> Parse only English sentences
		selectedParser.execute();
		
		// Reset parser parameters
		selectedParser.setDocument(null);
		selectedParser.setSentenceIdsToAnalyze(null);
		
		selectedParser.setSentenceAnnotationSetToAnalyze(original_sentenceAnnotationSet);
		selectedParser.setSentenceAnnotationTypeToAnalyze(original_sentenceAnnotationType);
		selectedParser.setTokenAnnotationSetToAnalyze(original_tokenAnnotationSet);
		selectedParser.setTokenAnnotationTypeToAnalyze(original_tokenAnnotationType);
		
	}
	
	public static void languageAwareAnnotationParsing(boolean isLangAware, Map<LangENUM, MateParser> parsersLangMap, Document doc, Annotation sentenceAnn,
			String sentAnnSet, String sentAnnType, String tokenAnnSet, String tokenAnnType) {
		
		if(sentenceAnn != null) {
			List<Annotation> annList = new ArrayList<Annotation>();
			annList.add(sentenceAnn);
		
			languageAwareAnnotationParsing(isLangAware, parsersLangMap, doc, annList, sentAnnSet, sentAnnType, tokenAnnSet, tokenAnnType);
		}
		
	}
	

	// ************************************************************************************************
	// ************************************************************************************************
	// ************************************************************************************************

	// inputOurputTestDir: base path to read input file to parse from and to write paresed file to
	private static String inputOurputTestDir = "/home/francesco/Downloads/SEPLN/EXAMPLE_COLLECTION_v3/pdln54/";
	private static String urlGATEannotatedDocument = "file://" + inputOurputTestDir + "pdln54__RomanMGZ15/pdln54__RomanMGZ15_DRI_v1.xml"; 
	private static String urlGATEoutputDocument = inputOurputTestDir + "Apdln54__RomanMGZ15/pdln54__RomanMGZ15_DRI_v1" + "_DEPPARSED.xml";

	public static void main(String[] args) {

		// Set URL document
		URL urlDocument = null;
		try {
			urlDocument = new URL(args[0]);
		} catch (MalformedURLException murle) {
			logger.error(murle.getMessage());
			// murle.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		if(urlDocument == null) {
			try {
				urlDocument = new URL(urlGATEannotatedDocument);
			} catch (MalformedURLException murle) {
				logger.error(murle.getMessage());
			}
		} 

		// Initialize and execute parser
		try {
			Gate.setGateHome(new File("/home/francesco/Desktop/DRILIB_EXP/DRIresources-3.4/gate_home"));
			Gate.setPluginsHome(new File("/home/francesco/Desktop/DRILIB_EXP/DRIresources-3.4/gate_home/plugins"));
			Gate.setSiteConfigFile(new File("/home/francesco/Desktop/DRILIB_EXP/DRIresources-3.4/gate_home/gate_uc.xml"));
			Gate.setUserConfigFile(new File("/home/francesco/Desktop/DRILIB_EXP/DRIresources-3.4/gate_home/gate_uc.xml"));
			
			
			Gate.init();

			logger.debug("Loading document to parse: " + urlDocument);
			Document doc = Factory.newDocument(urlDocument);
			logger.debug("Loaded document: " + doc.getContent().toString());

			logger.debug("\n-----------------------------------------------\n");


			// Instantiate parser
			MateParser parser = new MateParser();

			// Set parser parameters

			String baseModelPath = "/home/francesco/Downloads/TRANSITION_PARSER/";
			parser.setLemmaModelPath(baseModelPath + "CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model");
			parser.setPostaggerModelPath(baseModelPath + "CoNLL2009-ST-English-ALL.anna-3.3.postagger.model");
			parser.setParserModelPath(baseModelPath + "CoNLL2009-ST-English-ALL.anna-3.3.parser.model");
			parser.setSrlModelPath(baseModelPath + "CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model");

			parser.setExcludeThreshold(70);

			parser.setCitancesEnabled(Boolean.TRUE);

			// Initialize parser
			parser.init();

			// Set document and start parsing
			parser.setDocument(doc);
			parser.execute();

			// Storing parsed document
			try {
				OutputStreamWriter os_pw = null;
				PrintWriter pw = null;
				try {
					File outFile = new File(urlGATEoutputDocument);
					if(!outFile.exists()) {
						outFile.createNewFile();
					}
					os_pw = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
					pw = new PrintWriter(os_pw, true);
				} catch (FileNotFoundException e1) {
					logger.error("Error opening output file --> " + e1.getMessage() );
					e1.printStackTrace();
				}

				pw.print(doc.toXml());
				pw.flush();
				pw.close();
			} catch (Exception e) {
				logger.error("ERROR, IMPOSSIBLE TO SAVE: " + doc.getName());
				e.printStackTrace();
			}

		} catch(GateException ge) {
			logger.error("ERROR (GateException): while executing parser " + ge.getMessage());
			ge.printStackTrace();
		}
	}

	@Override
	public boolean resetAnnotations() {
		
		if(!this.annotationReset) {
			// Check input parameters
			sentenceAnnotationSetToAnalyze = StringUtils.defaultString(sentenceAnnotationSetToAnalyze, ImporterBase.driAnnSet);
			sentenceAnnotationTypeToAnalyze = StringUtils.defaultString(sentenceAnnotationTypeToAnalyze, ImporterBase.sentenceAnnType);
			tokenAnnotationSetToAnalyze = StringUtils.defaultString(tokenAnnotationSetToAnalyze, ImporterBase.driAnnSet);
			tokenAnnotationTypeToAnalyze = StringUtils.defaultString(tokenAnnotationTypeToAnalyze, ImporterBase.tokenAnnType);
			citeSpanAnnotationSetToExclude = StringUtils.defaultString(citeSpanAnnotationSetToExclude, ImporterBase.driAnnSet);
			citeSpanAnnotationTypeToExclude = StringUtils.defaultString(citeSpanAnnotationTypeToExclude, ImporterBase.inlineCitationAnnType);

			// Remove from all token of sentences the annotation features added by the parser
			List<Annotation> sentenceList = GateUtil.getAnnInDocOrder(this.document, sentenceAnnotationSetToAnalyze, sentenceAnnotationTypeToAnalyze);
			if(sentenceList != null && sentenceList.size() > 0) {
				for(Annotation sentence : sentenceList) {
					List<Annotation> tokenList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, tokenAnnotationSetToAnalyze, tokenAnnotationTypeToAnalyze, sentence);
					
					if(tokenList != null && tokenList.size() > 0) {
						for(Annotation token : tokenList) {
							// Remove features: gateId, category, lemma, depSentInternal, func, dependency,
							// srlA_tag, srlA_root, srlA_sense, srlR_tag
							if(token != null && token.getFeatures() != null) {
								token.getFeatures().remove(gateIdFeat);
								token.getFeatures().remove(posFeat);
								token.getFeatures().remove(lemmaFeat);
								token.getFeatures().remove(depInternalIdFeat);
								token.getFeatures().remove(depKindFeat);
								token.getFeatures().remove(depTargetIdFeat);

								Integer SRLid = 1;
								while(true) {
									if(token.getFeatures().containsKey(SRLpartTagNFeat + SRLid)) {
										token.getFeatures().remove(SRLpartTagNFeat + SRLid);
										token.getFeatures().remove(SRLpartRoodIdNFeat + SRLid);
										token.getFeatures().remove(SRLpartSenseNFeat + SRLid);
									}
									else {
										break;
									}
									SRLid++;
								}
								token.getFeatures().remove(SRLrootSenseFeat);
							}
						}
					}
					
				}
			}
			
			this.annotationReset = true;
		}

		return true;
	}

}
