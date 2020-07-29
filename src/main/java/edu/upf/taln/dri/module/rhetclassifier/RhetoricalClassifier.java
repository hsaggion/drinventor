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
package edu.upf.taln.dri.module.rhetclassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.feats.FeatG;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.FeatUtil;
import edu.upf.taln.ml.feat.FeatureSet;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.misc.InputMappedClassifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;



/**
 * Associate to each sentence a rhetorical class
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Rhetorical sentence classifier")
public class RhetoricalClassifier extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(RhetoricalClassifier.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	public static boolean storeClassificationFeatures = false;
	private static Instances headerModel = null;

	private static Map<String, Double> term_DF_map_DRI = new HashMap<String, Double>();

	// Input and output annotation
	private String inputSentenceASname = ImporterBase.driAnnSet;
	private String inputSentenceAStype = ImporterBase.sentenceAnnType;
	private String outputSentenceRhetoricalFeature = ImporterBase.sentence_RhetoricalAnnFeat;

	private URL classifierModelURL;
	private URL classifierStructureURL;
	
	private String sentenceLanguageFilter = null;

	private static FeatureSet<Annotation, DocumentCtx> featSet = null;
	private static InputMappedClassifier classif;

	public String getInputSentenceASname() {
		return inputSentenceASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set to read sentence annotations from")
	public void setInputSentenceASname(String inputSentenceASname) {
		this.inputSentenceASname = inputSentenceASname;
	}

	public String getInputSentenceAStype() {
		return inputSentenceAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Sentence", comment = "The name of the input annotation type to read sentence annotations")
	public void setInputSentenceAStype(String inputSentenceAStype) {
		this.inputSentenceAStype = inputSentenceAStype;
	}

	public String getOutputSentenceRhetoricalFeature() {
		return outputSentenceRhetoricalFeature;
	}

	@RunTime
	@CreoleParameter(defaultValue = "rhetorical_class", comment = "The name of the feature of the input sentence annotations to store the rhetorical class to")
	public void setOutputSentenceRhetoricalFeature(
			String outputSentenceRhetoricalFeature) {
		this.outputSentenceRhetoricalFeature = outputSentenceRhetoricalFeature;
	}

	public URL getClassifierModelURL() {
		return classifierModelURL;
	}

	@CreoleParameter(defaultValue = "", comment = "The URL of the classifier model")
	public void setClassifierModelURL(URL classifierModelURL) {
		this.classifierModelURL = classifierModelURL;
	}


	public URL getClassifierStructureURL() {
		return classifierStructureURL;
	}

	@CreoleParameter(defaultValue = "", comment = "The URL of the classifier structure")
	public void setClassifierStructureURL(URL classifierStructureURL) {
		this.classifierStructureURL = classifierStructureURL;
	}

	public String getSentenceLanguageFilter() {
		return sentenceLanguageFilter;
	}
	
	@RunTime
	@CreoleParameter(defaultValue = "", comment = "The language of the sentences to classify")
	public void setSentenceLanguageFilter(String sentenceLanguageFilter) {
		this.sentenceLanguageFilter = sentenceLanguageFilter;
	}

	@Override
	public Resource init() {
		logger.debug("Instantiating classifier...");

		classif = null;

		/* Load model from file */
		try {
			logger.debug("Loading classifier new...");
			/* OLD CODE
				SMO cModel = (SMO) weka.core.SerializationHelper.read(new File(this.getClassifierModelURL().toURI()).getAbsolutePath());
				DataSource source = new DataSource(new File(this.getClassifierStructureURL().toURI()).getAbsolutePath());
				Instances dataModel = source.getDataSet();
				dataModel.setClassIndex(dataModel.numAttributes() - 1);
				imc.setModelHeader(dataModel);
				imc.setClassifier(cModel);
			 */
			
			boolean modelLoaded = this.loadClassificationModel();
			
			if(modelLoaded) {
				logger.debug("Classifiers loaded.");
			}
			else {
				Util.notifyException("Impossible to load the classifier", new Exception("Error while loading classifier"), logger);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception while loading classifier - " + ex.getMessage());
			return this;
		}

		/* Load the document frequency map of the DRI Corpus */
		try {
			term_DF_map_DRI = new HashMap<String, Double>();
			InputStream documentFrequencyMap = RhetoricalClassifier.class.getResourceAsStream("/corpus/DRIcorpusFrequencies.list");
			BufferedReader documentFrequencyMapReader = new BufferedReader(new InputStreamReader(documentFrequencyMap));

			String documentFrequencyMapLine = documentFrequencyMapReader.readLine();
			while(documentFrequencyMapLine != null){
				documentFrequencyMapLine = documentFrequencyMapLine.trim();
				String[] documentFrequencyMapLineSplit = documentFrequencyMapLine.split("\t");
				if(documentFrequencyMapLineSplit != null && documentFrequencyMapLineSplit.length == 2) {
					try {
						term_DF_map_DRI.put(documentFrequencyMapLineSplit[0].trim(), Double.valueOf(documentFrequencyMapLineSplit[1].trim()));
					}
					catch (Exception ex) {
						/* DO NOTHING */
					}
				}

				documentFrequencyMapLine = documentFrequencyMapReader.readLine();
			}
		} catch (Exception ex) {
			logger.error("Exception instantiating filters - " + ex.getMessage());
			return this;
		}
		term_DF_map_DRI = Collections.unmodifiableMap(term_DF_map_DRI);
		logger.debug("DRI doc. frequency map: " + term_DF_map_DRI.size() + " entries.");

		return this;
	}


	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		try {
			DocCacheManager cacheManager = new DocCacheManager(this.document);

			// Get the document to process
			gate.Document doc = getDocument();

			// Normalize variables
			String inputSentenceASnameAppo = StringUtils.defaultString(inputSentenceASname, ImporterBase.driAnnSet);
			String inputSentenceAStypeAppo = StringUtils.isNotBlank(inputSentenceAStype) ? inputSentenceAStype : ImporterBase.sentenceAnnType;
			String outputASfeatureNameAppo = StringUtils.isNotBlank(outputSentenceRhetoricalFeature) ? outputSentenceRhetoricalFeature : ImporterBase.sentenceAnnType;

			// --- Compute TF-IDF of words - START
			// 1) rawTF_map: map with key the term and value the raw frequency inside the document
			Map<String, Double> rawTF_map = new HashMap<String, Double>();
			// 2) maxTFvalue: the maximum term frequency value for the document
			Double maxTFvalue = 0d;
			// 3) term_DF_map: in how many documents each term occur? --> KEY: the term / VALUE: the number of documents of the corpus where the term occurs at least once
			Map<String, Double> term_DF_map = new HashMap<String, Double>();

			AnnotationSet sentenceAnnotations = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType);

			if(sentenceAnnotations != null && sentenceAnnotations.size() > 0 ) {

				List<Annotation> sentenceAnnotationList = gate.Utils.inDocumentOrder(sentenceAnnotations);

				int totalSentenceAnnotations = sentenceAnnotationList.size();

				// Include title annotation
				List<Annotation> titleAnn = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.titleAnnType);
				if(titleAnn != null && titleAnn.size() > 0 && titleAnn.get(0) != null) {
					sentenceAnnotationList.add(titleAnn.get(0));
				}

				// Include h1 section annotations
				List<Annotation> h1Ann = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.h1AnnType);
				if(h1Ann != null && h1Ann.size() > 0) {
					for(Annotation h1 : h1Ann) {
						if(h1 != null) {
							sentenceAnnotationList.add(h1);
						}
					}
				}
				
				// Remove 
				if(sentenceLanguageFilter != null && sentenceLanguageFilter.length() > 0) {
					int originalSentCount = sentenceAnnotationList.size();
					
					int removedCount = 0;
					for (Iterator<Annotation> iter = sentenceAnnotationList.iterator(); iter.hasNext(); ) {
						Annotation ann = iter.next();
					    if (ann != null & ann.getId() != null) {
					    	String sentLang = GateUtil.getStringFeature(ann, ImporterBase.langAnnFeat).orElse(null);
					    	
					    	// Not English rhetorical annotator with English language
					    	if(!sentenceLanguageFilter.toLowerCase().equals("english") &&
					    			(sentLang == null || sentLang.equals("") || sentLang.trim().toLowerCase().equals("en"))) {
					    		iter.remove();
						        removedCount++;
					    	} // Not Spanish rhetorical annotator with Spanish language
					    	else if(!sentenceLanguageFilter.toLowerCase().equals("spanish") &&
					    			(sentLang != null && sentLang.trim().toLowerCase().equals("es"))) {
					    		iter.remove();
						        removedCount++;
					    	}
					    }
					}
					
					logger.info("   - Enabled sentence language filter --> sentences to annotate rhetorically: " + sentenceAnnotationList.size() + " (num sentences filtered out: " + removedCount + " over " + originalSentCount + ")");
				}
				
				int sentenceCount = 0;
				for(Annotation sentenceAnn : sentenceAnnotationList) {
					sentenceCount++;
					if(sentenceAnn != null) {

						// Populate TFIDF map by excluding tokens inside citing spans
						AnnotationSet intersectingCitSpans = doc.getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).getContained(sentenceAnn.getStartNode().getOffset(), sentenceAnn.getEndNode().getOffset());
						List<Annotation> intersectingCitSpansOrdered  = gate.Utils.inDocumentOrder(intersectingCitSpans);

						List<Annotation> sentenceTokenAnn = GateUtil.getAnnInDocOrderContainedAnn(doc, ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sentenceAnn);

						// For every token in the sentence, modify / add one to the counter of the document term frequency
						for(Annotation token : sentenceTokenAnn) {

							boolean tokenInsideCitSpan = false;
							if(intersectingCitSpansOrdered != null && intersectingCitSpansOrdered.size() > 0) {
								for(Annotation intersectingCitSpan : intersectingCitSpansOrdered) {
									if(intersectingCitSpan != null && 
											token.getStartNode().getOffset() >= intersectingCitSpan.getStartNode().getOffset() && token.getEndNode().getOffset() <= intersectingCitSpan.getEndNode().getOffset()) {
										tokenInsideCitSpan = true;
										break;
									}
								}
							}

							if(tokenInsideCitSpan) {
								continue;
							}

							String lemma = GateUtil.getStringFeature(token, "lemma").orElse(null);
							String tokenKind = GateUtil.getStringFeature(token, "kind").orElse(null);
							if(StringUtils.isNotBlank(lemma) && !edu.upf.taln.dri.module.rhetclassifier.feats.StopWords.isStopWord(lemma) && tokenKind.toLowerCase().trim().equals("word")) {
								// Update rawTF_map
								if(rawTF_map.containsKey(lemma) && rawTF_map.get(lemma) != null) {
									rawTF_map.put(lemma, rawTF_map.get(lemma) + 1d);
								}
								else {
									rawTF_map.put(lemma, 1d);
								}
							}
						}

					}
				}

			}

			// Once populated the rawTF_map with the entry for the document under analysis,
			// it is possible to determine the maximum frequency of term in the document and add it to maxTFvalue
			if(rawTF_map != null) {
				for(Entry<String, Double> rawTF_mapEntry : rawTF_map.entrySet()) {
					if(rawTF_mapEntry != null && rawTF_mapEntry.getKey() != null && rawTF_mapEntry.getValue() != null && rawTF_mapEntry.getValue() > maxTFvalue) {
						maxTFvalue = new Double(rawTF_mapEntry.getValue());
					}
				}
			}

			// Compute the document frequency map enriching the DRI corpus one
			for(Entry<String, Double> term_DF_map_DRIentry : term_DF_map_DRI.entrySet()) {
				term_DF_map.put(new String(term_DF_map_DRIentry.getKey()), new Double(term_DF_map_DRIentry.getValue()));
			}

			if(rawTF_map != null) {
				for(Entry<String, Double> rawTF_mapEntry : rawTF_map.entrySet()) {
					if(rawTF_mapEntry != null && rawTF_mapEntry.getKey() != null && rawTF_mapEntry.getValue() != null) {
						if(term_DF_map.containsKey(rawTF_mapEntry.getKey())) {
							term_DF_map.put(rawTF_mapEntry.getKey(), term_DF_map.get(rawTF_mapEntry.getKey()) + 1d);
						}
						else {
							term_DF_map.put(rawTF_mapEntry.getKey(), 1d);
						}
					}
				}
			}
			// --- Compute TF-IDF of words - END


			List<Annotation> sentencesToClassify = gate.Utils.inDocumentOrder(doc.getAnnotations(inputSentenceASnameAppo).get(inputSentenceAStypeAppo));
			
			// Remove 
			if(sentenceLanguageFilter != null && sentenceLanguageFilter.length() > 0) {
				int originalSentCount = sentencesToClassify.size();
				
				int removedCount = 0;
				for (Iterator<Annotation> iter = sentencesToClassify.iterator(); iter.hasNext(); ) {
					Annotation ann = iter.next();
				    if (ann != null & ann.getId() != null) {
				    	String sentLang = GateUtil.getStringFeature(ann, ImporterBase.langAnnFeat).orElse(null);
				    	
				    	// Not English rhetorical annotator with English language
				    	if(!sentenceLanguageFilter.toLowerCase().equals("english") &&
				    			(sentLang == null || sentLang.equals("") || sentLang.toLowerCase().equals("en"))) {
				    		iter.remove();
					        removedCount++;
				    	} // Not Spanish rhetorical annotator with Spanish language
				    	else if(!sentenceLanguageFilter.toLowerCase().equals("spanish") &&
				    			(sentLang != null && sentLang.toLowerCase().equals("es"))) {
				    		iter.remove();
					        removedCount++;
				    	}
				    }
				}
				
				logger.info("   - Enabled sentence language filter --> sentences to annotate rhetorically: " + sentencesToClassify.size() + " (num sentences filtered out: " + removedCount + " over " + originalSentCount + ")");
			}
			
			if(!CollectionUtils.isEmpty(sentencesToClassify)) {
				for(Annotation sentenceToClassify : sentencesToClassify) {
					if(sentenceToClassify != null) {
						
						// Feature schema header
						// The object is a citing sentence annotaiton and the context is the document the sentence belongs to
						logger.debug("Instantiating feature schema header...");
						featSet = FeatG.generateFeatSet();

						boolean correctlyAdded = false;
						try {correctlyAdded = featSet.addElement(sentenceToClassify, new DocumentCtx(cacheManager.getGateDoc(), "Sentence", 
								"", "", "", 1d, "", "", "", "", "",
								rawTF_map,
								maxTFvalue,
								term_DF_map));
						}
						catch (Exception e) {
							Util.notifyException("Rhetorical classification of sentence: " + GateUtil.getAnnotationText(sentenceToClassify, doc).orElse("NOT_PRESENT"), e, logger);
						}

						if(correctlyAdded) {
							Instances wekaInstPreFilter = null;
							try {
								wekaInstPreFilter = FeatUtil.wekaInstanceGeneration(featSet, "relation");

								if(wekaInstPreFilter != null) {
									
									// Print attribute values before any filter - START
									/*
									for(int attrIndx = 0; attrIndx  < wekaInstPreFilter.instance(0).numAttributes(); attrIndx++) {
										if(wekaInstPreFilter.instance(0).isMissing(attrIndx)) {
											System.out.println("ATT: " + attrIndx + " MISSING -> " + wekaInstPreFilter.instance(0).attribute(attrIndx).name() + " *** MISSING ***");
										}
										else if(wekaInstPreFilter.instance(0).attribute(attrIndx).isNumeric()) {
											System.out.println("ATT: " + attrIndx + " NUMERIC -> " + wekaInstPreFilter.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInstPreFilter.instance(0).value(attrIndx));
										}
										else if(wekaInstPreFilter.instance(0).attribute(attrIndx).isNominal()) {
											System.out.println("ATT: " + attrIndx + " NOMINAL -> " + wekaInstPreFilter.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInstPreFilter.instance(0).attribute(attrIndx).value( (new Double(wekaInstPreFilter.instance(0).value(attrIndx)).intValue())) );
										}
										else if(wekaInstPreFilter.instance(0).attribute(attrIndx).isString()) {
											System.out.println("ATT: " + attrIndx + " STRING -> " + wekaInstPreFilter.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInstPreFilter.instance(0).attribute(attrIndx).value( (new Double(wekaInstPreFilter.instance(0).value(attrIndx)).intValue())) );
										}
										else {
											System.out.println("ATT: " + attrIndx + " OTHER -> " + wekaInstPreFilter.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInstPreFilter.instance(0).value(attrIndx));
										}
									}
									*/
									// Print attribute values before any filter - START
									
									MultiFilter multiFilter = new MultiFilter();

									Filter[] filtersArray = new Filter[FeatureFilter.setFilterChain_Rhetorical_1().size()];
									filtersArray = FeatureFilter.setFilterChain_Rhetorical_1().toArray(filtersArray);
									multiFilter.setFilters(filtersArray);

									// Set filter and classifier
									multiFilter.setInputFormat(wekaInstPreFilter);
									Instances wekaInst = Filter.useFilter(wekaInstPreFilter, multiFilter);

									// Set the Class_rhetorical attribute as the last one
									wekaInst = FeatureFilter.setClassAsLastAttr_Rhetorical_1(wekaInst, "Class_rhetorical");

									// Check the position of the Class_rhetorical attribute - START
									/*
									int classRhetIndex = -1;
									for(int attIndex = 0; attIndex < wekaInst.numAttributes(); attIndex++) {
										System.out.print("ID: " + attIndex + " : " + wekaInst.attribute(attIndex).name() + " - ");
										if(wekaInst.attribute(attIndex).name().equals("Class_rhetorical")) {
											classRhetIndex = attIndex;
										}
										if(attIndex % 10 == 0) {
											System.out.print("\n");
										}
									}
									System.out.println("CLASS RHETORICAL INDEX: " + (classRhetIndex + 1) + " over " + wekaInst.numAttributes() + " attributes.");
									 */
									// Check the position of the Class_rhetorical attribute - END

									// Alert if more than one instance / sentence is present in the dataset
									if(wekaInst.numInstances() > 1) {
										logger.error("There is more than one instance / sentence to classify!!!");
									}

									wekaInst.setClassIndex(wekaInst.numAttributes() - 1);

									// PRINT ATTRIBUTES NAMES AND VALUES BEFORE AND AFTER MAPPING - START
									/*									
									for(int attrIndx = 0; attrIndx  < wekaInst.instance(0).numAttributes(); attrIndx++) {
										if(wekaInst.instance(0).isMissing(attrIndx)) {
											System.out.println("ATT: " + attrIndx + " MISSING -> " + wekaInst.instance(0).attribute(attrIndx).name() + " *** MISSING ***");
										}
										else if(wekaInst.instance(0).attribute(attrIndx).isNumeric()) {
											System.out.println("ATT: " + attrIndx + " NUMERIC -> " + wekaInst.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInst.instance(0).value(attrIndx));
										}
										else if(wekaInst.instance(0).attribute(attrIndx).isNominal()) {
											System.out.println("ATT: " + attrIndx + " NOMINAL -> " + wekaInst.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInst.instance(0).attribute(attrIndx).value( (new Double(wekaInst.instance(0).value(attrIndx)).intValue())) );
										}
										else if(wekaInst.instance(0).attribute(attrIndx).isString()) {
											System.out.println("ATT: " + attrIndx + " STRING -> " + wekaInst.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInst.instance(0).attribute(attrIndx).value( (new Double(wekaInst.instance(0).value(attrIndx)).intValue())) );
										}
										else {
											System.out.println("ATT: " + attrIndx + " OTHER -> " + wekaInst.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInst.instance(0).value(attrIndx));
										}
									}
									
									// Print mapping details
									classif.setSuppressMappingReport(false);			
									classif.setDebug(true);
									Instance mappedInstance = classif.constructMappedInstance(wekaInst.instance(0));
									classif.setSuppressMappingReport(true);
									classif.setDebug(false);
									
									for(int attrIndx = 0; attrIndx  < mappedInstance.numAttributes(); attrIndx++) {
										if(wekaInst.instance(0).isMissing(attrIndx)) {
											System.out.println("MAPPED ATT: " + attrIndx + " MISSING -> " + mappedInstance.attribute(attrIndx).name() + " *** MISSING ***");
										}
										else if(mappedInstance.attribute(attrIndx).isNumeric() && !(Double.valueOf(mappedInstance.value(attrIndx)).equals("NaN")) ) {
											System.out.println("MAPPED ATT: " + attrIndx + " NUMERIC -> " + mappedInstance.attribute(attrIndx).name() + " VALUE: " + mappedInstance.value(attrIndx));
										}
										else if(mappedInstance.attribute(attrIndx).isNominal()) {
											System.out.println("MAPPED ATT: " + attrIndx + " NOMINAL -> " + mappedInstance.attribute(attrIndx).name() + " VALUE: " + mappedInstance.attribute(attrIndx).value( (new Double(mappedInstance.value(attrIndx)).intValue())) );
										}
										else if(mappedInstance.attribute(attrIndx).isString()) {
											System.out.println("MAPPED ATT: " + attrIndx + " STRING -> " + mappedInstance.attribute(attrIndx).name() + " VALUE: " + mappedInstance.attribute(attrIndx).value( (new Double(mappedInstance.value(attrIndx)).intValue())) );
										}
										else {
											System.out.println("ATT: " + attrIndx + " OTHER -> " + wekaInst.instance(0).attribute(attrIndx).name() + " VALUE: " + wekaInst.instance(0).value(attrIndx));
										}
									}
									*/
									// PRINT ATTRIBUTES NAMES AND VALUES BEFORE AND AFTER MAPPING - END

									// Classify instance
									Instance inst = wekaInst.instance(0);
									Double classInst = null;
									double[] classDistibInst = null;
									String annotationType = null;
									
									/* REDIRECTING STD OUT AND ERR - START */
									PrintStream out = System.out;
									PrintStream err = System.err;
									System.setOut(new PrintStream(new OutputStream() {
										@Override public void write(int b) throws IOException {}
									}));
									System.setErr(new PrintStream(new OutputStream() {
										@Override public void write(int b) throws IOException {}
									}));
									
									try {
										/* original code start */
										classInst = classif.classifyInstance(inst);
										annotationType = headerModel.attribute(headerModel.numAttributes() -1).value((int) classInst.intValue());
										logger.debug("\n---\n* Instance classified as: " + annotationType);
										classDistibInst = classif.distributionForInstance(inst);
										/* original code end */
									} finally {
										System.setOut(out);
										System.setErr(err);
									}
									/* REDIRECTING STD OUT AND ERR - END */
									
									FeatureMap fm = Factory.newFeatureMap();
									Map<String, Double> classProbabilityMap = new HashMap<String, Double>();
									for(int i = 0; i < classDistibInst.length; i++) {
										logger.debug("    -> Instance: " + headerModel.attribute(headerModel.numAttributes() -1).value((int) i) + " (" + i + ") --> " + classDistibInst[i]);
										fm.put(headerModel.attribute(headerModel.numAttributes() -1).value((int) i), classDistibInst[i]);
										classProbabilityMap.put(headerModel.attribute(headerModel.numAttributes() -1).value((int) i), classDistibInst[i]);
										// Add to sentence feature set the probability assigned to each class
										sentenceToClassify.getFeatures().put("PROB_" + headerModel.attribute(headerModel.numAttributes() -1).value((int) i), classDistibInst[i]);
									}

									// CORRECTIONS TO ANNOTAITON - TO IMPROVE CLASSIFIER - START
									String sentence = GateUtil.getAnnotationText(sentenceToClassify, this.document).orElse(null);
									if(sentence.toLowerCase().contains("future work") || sentence.toLowerCase().contains("future venue") ||
											sentence.toLowerCase().contains("future research") ||
											sentence.toLowerCase().contains("in future") || sentence.toLowerCase().contains("future investigation") ) {
										annotationType = "DRI_FutureWork";
									}	
									// CORRECTIONS TO ANNOTAITON - TO IMPROVE CLASSIFIER - END

									// GENERATE ANNOTATION
									sentenceToClassify.getFeatures().put(outputASfeatureNameAppo, annotationType);
								}
							} catch (Exception e) {
								e.printStackTrace();
								Util.notifyException("Rhetorical classification of sentence: " + GateUtil.getAnnotationText(sentenceToClassify, doc).orElse("NOT_PRESENT"), e, logger);
							}
						}
						else {
							Util.notifyException("Rhetorical classification of sentence: " + GateUtil.getAnnotationText(sentenceToClassify, doc).orElse("NOT_PRESENT"), new Exception("Errors when generating features"), logger);
						}
					}
				}
			}

		} catch (InternalProcessingException e1) {
			Util.notifyException("Executing rhetorical classifier", e1, logger);
		}
	}

	@Override
	public boolean resetAnnotations() {
		
		if(!this.annotationReset) {
			// Normalize variables
			String inputSentenceASnameAppo = StringUtils.defaultString(inputSentenceASname, ImporterBase.driAnnSet);
			String inputSentenceAStypeAppo = StringUtils.isNotBlank(inputSentenceAStype) ? inputSentenceAStype : ImporterBase.sentenceAnnType;
			String outputASfeatureNameAppo = StringUtils.isNotBlank(outputSentenceRhetoricalFeature) ? outputSentenceRhetoricalFeature : ImporterBase.sentenceAnnType;

			List<Annotation> classifiedSentences = GateUtil.getAnnInDocOrder(this.document, inputSentenceASnameAppo, inputSentenceAStypeAppo);

			classifiedSentences.stream().forEach((sentAnn) -> {
				if(sentAnn != null && sentAnn.getFeatures() != null) {
					sentAnn.getFeatures().remove(outputASfeatureNameAppo);
				}
			});
			
			this.annotationReset = true;
		}
		
		return true;
	}

	public boolean loadClassificationModel() throws Exception {
		// Check if the classifier model and the data structure file can be correctly loaded
		if(this.getClassifierModelURL() == null || this.getClassifierModelURL().equals("")) {
			Util.notifyException("No classification model specified", new Exception("Error while loading classification model"), logger);
			return false;
		}
		if(this.getClassifierStructureURL() == null || this.getClassifierStructureURL().equals("")) {
			Util.notifyException("No classification data structure specified", new Exception("Error while loading classification data structure"), logger);
			return false;
		}

		File classifierModel = new File(this.getClassifierModelURL().toURI());
		File classifierDataStructure = new File(this.getClassifierStructureURL().toURI());
		
		// logger.info("Classifier model file: " + classifierModel.getAbsolutePath());
		// logger.info("Classifier structure file: " + classifierDataStructure.getAbsolutePath());
		
		if(classifierModel == null || !classifierModel.exists()) {
			Util.notifyException("Impossible to load classifier model file: " + ((this.getClassifierModelURL() != null) ? this.getClassifierModelURL() : "NULL"), new Exception("Error while loading classification model"), logger);
			return false;
		}

		if(classifierDataStructure == null || !classifierDataStructure.exists()) {
			Util.notifyException("Impossible to load classifier data structure file: " + ((this.getClassifierStructureURL() != null) ? this.getClassifierStructureURL() : "NULL"), new Exception("Error while loading classification data structure"), logger);
			return false;
		}
		
		
		headerModel = null;
		classif = null;
		
		// Load classifier
		SerializedClassifier coreClassifier = new SerializedClassifier();
		coreClassifier.setModelFile(classifierModel);
		coreClassifier.setDebug(false);
		
		// Load InputMappedClassifier and set the just loaded model as classifier
		classif = new InputMappedClassifier();
		classif.setClassifier(coreClassifier);
		
		// DataSource source = new DataSource(classifierDataStructure.getAbsolutePath());
		// headerModel = source.getDataSet();
		
		BufferedReader reader = new BufferedReader(new FileReader(classifierDataStructure.getAbsolutePath()));
		headerModel = new Instances(reader);
		headerModel.setClassIndex(headerModel.numAttributes() - 1);
		classif.setModelHeader(headerModel);
		
		classif.setDebug(false);
		classif.setSuppressMappingReport(true);			
		classif.setTrim(true);
		classif.setIgnoreCaseForNames(false);

		return true;
	}


	// MAIN PROGRAM TO GENERATE THE CLASSIFIER MODEL FROM AN INPUT ARFF (OR VALIDATE IT)

	public static void main(String[] args) {

		File dir = new File("/home/francesco/Desktop/DRI_Pipeline_DATA/EXPERIMENT_EXTENDED_FEATURES/ONE_DOC_OUT_RHETORICAL/FILTERED");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if(child != null && child.exists() && child.isFile() && child.getName().endsWith(".arff")) {
					
					System.out.println("\n---\nGENERATING MODEL FROM FILE: " + child.getAbsolutePath());
					
					// Train model
					LibLINEAR cls = new LibLINEAR();
					try {
						String[] options = weka.core.Utils.splitOptions("-S 7 -C 1.0 -E 0.01 -B 1.0 -P");
						cls.setOptions(options);
						System.out.println("Classifier type: " + cls.getSVMType());
						System.out.println("Classifier cost: " + cls.getCost());
						System.out.println("Classifier epsilon: " + cls.getEps());
						System.out.println("Classifier bias: " + cls.getBias());
						System.out.println("Classifier probability: " + cls.getProbabilityEstimates());
					}
					catch(Exception e) {
						e.printStackTrace();
						System.out.println("ERROR WHILE INSTANTIATING CLASSIFIER...");
					}

					try {
						Instances inst = new Instances(new BufferedReader(new FileReader(child.getAbsolutePath())));
						inst.setClassIndex(inst.numAttributes() - 1);

						System.out.println("Num instances: " + inst.numInstances());
						System.out.println("Num attributes: " + inst.numAttributes());

						// *** Evaluate
						// System.out.println("Start evaluation...");
						// Evaluation eval = new Evaluation(inst);
						// eval.crossValidateModel(cls, inst, 10, new Random(1));
						// System.out.println("Estimated Accuracy: "+ Double.toString(eval.weightedFMeasure()));
						// System.out.println(eval.toSummaryString("=== SUMMARY STATS ===\n", false));
						// System.out.println(eval.toMatrixString("=== Confusion matrix ===\n"));
						// System.out.println(eval.toClassDetailsString());

						// *** Build and store
						System.out.println("Start building classifier...");
						cls.buildClassifier(inst);
						weka.core.SerializationHelper.write(child.getAbsolutePath().replace(".arff", ".model"), cls);
						System.out.println("Classifier model stored to: " + child.getAbsolutePath().replace(".arff", ".model"));
					}
					catch(Exception e) {
						e.printStackTrace();
						System.out.println("ERROR WHILE TRAINING CLASSIFIER...");
					}
				}
			}
		}
		
	}

}
