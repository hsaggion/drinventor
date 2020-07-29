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
package edu.upf.taln.dri.module.languageDetector;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.module.DRIModule;
import edu.upf.taln.dri.module.importer.ImporterBase;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

/**
 * Detect language of annotations of set and type defined in the input settings
 * 
 *
 */
@CreoleResource(name = "DRI Modules - Language Detector")
public class LanguageDetector extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(LanguageDetector.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	private static com.optimaize.langdetect.LanguageDetector languageDetect = null;

	// Input and output annotation
	private String inputDetectionASname;
	private String inputDetectionAStype;
	private String outputLangFeatureName;
	private String enableLangDetect;
	private String enableMajorityVoting;

	public String getInputDetectionASname() {
		return inputDetectionASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set to detect language of annotations")
	public void setInputDetectionASname(String inputSentenceASname) {
		this.inputDetectionASname = inputSentenceASname;
	}

	public String getInputDetectionAStype() {
		return inputDetectionAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Sentence", comment = "The name of the annotation type to detect language of")
	public void setInputDetectionAStype(String inputSentenceAStype) {
		this.inputDetectionAStype = inputSentenceAStype;
	}

	public String getOutputLangFeatureName() {
		return outputLangFeatureName;
	}

	@RunTime
	@CreoleParameter(defaultValue = "lang", comment = "The name of the feature in which to store the language detected")
	public void setOutputLangFeatureName(String outputLangFeatureName) {
		this.outputLangFeatureName = outputLangFeatureName;
	}

	public String getEnableLangDetect() {
		return enableLangDetect;
	}

	@RunTime
	@CreoleParameter(defaultValue = "false", comment = "Set to true to enable langaugae detection. If false, the language features is set to an empty string")
	public void setEnableLangDetect(String enableLangDetect) {
		this.enableLangDetect = enableLangDetect;
	}

	public String getEnableMajorityVoting() {
		return enableMajorityVoting;
	}

	@RunTime
	@CreoleParameter(defaultValue = "false", comment = "Set to true to set the language of all annotations equal to the language"
			+ " detected for the majority of these annotations. If false, the language features is set to the language detected in each individual annotation")
	public void setEnableMajorityVoting(String enableMajorityVoting) {
		this.enableMajorityVoting = enableMajorityVoting;
	}



	@Override
	public Resource init() {
		try {
			//load all languages:
			List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

			//build language detector:
			languageDetect = LanguageDetectorBuilder.create(NgramExtractors.standard())
					.withProfiles(languageProfiles)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while loading language detector - " + e.getMessage());
		}
		return this;
	}

	public void execute() throws ExecutionException {
		this.annotationReset = false;


		// Normalize variables
		String inputDetectASnameAppo = (this.inputDetectionASname != null) ? this.inputDetectionASname : ImporterBase.driAnnSet;
		String inputDetectAStypeAppo = (this.inputDetectionAStype != null && this.inputDetectionAStype.length() > 0) ? this.inputDetectionAStype : ImporterBase.sentenceAnnType;
		String outputLangFeatureNameAppo = (this.outputLangFeatureName != null && this.outputLangFeatureName.length() > 0) ? this.outputLangFeatureName : ImporterBase.langAnnFeat;

		// Get the annotation set of the annotations to detect language of
		List<Annotation> langDetectAnnotationList = GateUtil.getAnnInDocOrder(this.document, inputDetectASnameAppo, inputDetectAStypeAppo);

		detectLanguage(this.document, langDetectAnnotationList, outputLangFeatureNameAppo, 
				((this.getEnableLangDetect() != null && this.getEnableLangDetect().toLowerCase().equals("true")) ? true : false),
				((this.getEnableMajorityVoting() != null && this.getEnableMajorityVoting().toLowerCase().equals("true")) ? true : false));

	}

	public static void detectLanguage(Document doc, List<Annotation> langDetectAnnotationList, String langFeatureName, boolean enableLangDetect, boolean enableMajorityVoting) {
		for(Annotation langDetectAnn : langDetectAnnotationList) {
			String lang = "";

			if(enableLangDetect) {
				String annText = GateUtil.getAnnotationText(langDetectAnn, doc).orElse(null);
				if(annText != null && annText.length() > 0) {
					try {
						TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
						TextObject textObject = textObjectFactory.forText(annText);
						Optional<LdLocale> result = languageDetect.detect(textObject);
						lang = (result.isPresent() && result.get().getLanguage() != null && result.get().getLanguage().length() > 0) ? result.get().getLanguage().toString() : "";
					}
					catch(Exception e) {
						/* Do nothing */
					}
				}
			}
			
			lang = (lang != null) ? lang : "";
			
			FeatureMap fm = (langDetectAnn.getFeatures() != null) ? langDetectAnn.getFeatures() : Factory.newFeatureMap();
			fm.put(langFeatureName, lang);
		}

		if(enableLangDetect && enableMajorityVoting) {
			/*
			String selectedMajorityLang = getMajorityLanguage(langDetectAnnotationList, langFeatureName);
			*/
			
			// Create a single string out of all annotations
			String mergedAnnTexts = "";
			for(Annotation langDetectAnn : langDetectAnnotationList) {
				String annText = GateUtil.getAnnotationText(langDetectAnn, doc).orElse(null);
				if(annText != null && annText.length() > 0) {
					mergedAnnTexts += (mergedAnnTexts.length() == 0) ? annText : " " + annText;
				}
			}
			
			String selectedMajorityLang = "";
			try {
				TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
				TextObject textObject = textObjectFactory.forText(mergedAnnTexts);
				Optional<LdLocale> result = languageDetect.detect(textObject);
				selectedMajorityLang = (result.isPresent() && result.get().getLanguage() != null && result.get().getLanguage().length() > 0) ? result.get().getLanguage().toString() : "";
			}
			catch(Exception e) {
				/* Do nothing */
			}
			
			if(selectedMajorityLang != null) {
				for(Annotation langDetectAnn : langDetectAnnotationList) {
					FeatureMap fm = (langDetectAnn.getFeatures() != null) ? langDetectAnn.getFeatures() : Factory.newFeatureMap();
					if(fm.containsKey(langFeatureName) && fm.get(langFeatureName) != null) {
						fm.put(langFeatureName + "_orig", fm.get(langFeatureName));
					}
					fm.put(langFeatureName, selectedMajorityLang);
				}
			}
		}
	}
	
	
	public static String getMajorityLanguage(List<Annotation> langDetectAnnotationList, String langFeatureName) {
		String majorityLang = "";
		
		if(langDetectAnnotationList != null && langDetectAnnotationList.size() > 0 && langFeatureName != null && langFeatureName.length() > 0) {
			Map<String, Integer> languageCountMap = new HashMap<String, Integer>();
			for(Annotation ann : langDetectAnnotationList) {
				if(ann != null && ann.getFeatures() != null && ann.getFeatures().containsKey(langFeatureName) && 
						ann.getFeatures().get(langFeatureName) != null && ann.getFeatures().get(langFeatureName) instanceof String) {
					String langName = (String) ann.getFeatures().get(langFeatureName);
					if(languageCountMap.containsKey(langName)) {
						languageCountMap.put(langName, languageCountMap.get(langName) + 1);
					}
					else {
						languageCountMap.put(langName, 1);
					}
				}
			}
			
			languageCountMap = Util.sortByValueDec(languageCountMap);
			majorityLang = "";
			for(Entry<String, Integer> languageCountMapEntry : languageCountMap.entrySet()) {
				if(languageCountMapEntry != null && languageCountMapEntry.getKey() != null && !languageCountMapEntry.getKey().equals("")) {
					majorityLang = languageCountMapEntry.getKey();
				}
			}
		}
		
		return majorityLang;
	}

	@Override
	public boolean resetAnnotations() {
		if(!this.annotationReset) {
			// Normalize variables
			String inputDetectASnameAppo = (this.inputDetectionASname != null) ? this.inputDetectionASname : ImporterBase.driAnnSet;
			String inputDetectAStypeAppo = (this.inputDetectionAStype != null && this.inputDetectionAStype.length() > 0) ? this.inputDetectionAStype : ImporterBase.sentenceAnnType;
			String outputLangFeatureNameAppo = (this.outputLangFeatureName != null && this.outputLangFeatureName.length() > 0) ? this.outputLangFeatureName : ImporterBase.langAnnFeat;

			// Get the annotation set of the annotations to detect language of and remove the feature named outputLangFeatureNameAppo
			List<Annotation> langDetectAnnotationList = GateUtil.getAnnInDocOrder(this.document, inputDetectASnameAppo, inputDetectAStypeAppo);
			for(Annotation langDetectAnn : langDetectAnnotationList) {
				if(langDetectAnn != null && langDetectAnn.getFeatures() != null) {
					langDetectAnn.getFeatures().remove(outputLangFeatureNameAppo);
				}
			}

			this.annotationReset = true;
		}

		return false;
	}

}
