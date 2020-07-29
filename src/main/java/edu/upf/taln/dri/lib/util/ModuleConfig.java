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
package edu.upf.taln.dri.lib.util;

/**
 * Class to configure how a scientific publication will be processed by the Dr. Inventor Text Mining Framework.<br/>
 * IMPORTANT: this configuration should be set up before initializing the Dr. Inventor Text Mining Framework!<br/>
 * The Framework integrates a set of scientific text mining modules; each module is responsible to perform a set of analyses of scientific publications.<br/>
 * By means of this class it is possible to selectively enable only the module of interest for each particular use case, thus performing the analyses of scientific
 * publications that the user actually needs. The following modules of the Dr. Inventor Text Mining Framework can be selectively enabled or disabled:<br/>
 * - BibEntryParsing: enriches each bibliographic entry of the paper by retrieving cited paper's meta-data from Bibsonomy, CrossRef and FreeCite;<br/>
 * - HeaderParsing: parse the contents of the header of the paper by means of external Web Services;<br/>
 * - BabelNetParsing: disambiguate the textual contents of the paper by means of BabelNet;<br/>
 * - TerminologyParsing: spot candidate terms inside the paper;<br/>
 * - GraphParsing: perform the dependency parsing of the contents of the paper, thus enabling the extraction of Subject-Verb-Object graphs;<br/>
 * - RhetoricalClassification: identify for each sentence of the paper, its rhetorical category (Background, Approach, Outcome, FutureWirk, Challenge);<br/>
 * - CoreferenceResolution: perform co-reference resolution;<br/>
 * - CausalityParsing: extract causal relations from the paper;<br/>
 * - MetaAnnotationsParsing: extract <br/>
 * - MultiLangSupport (Experimental): for multi-language papers - detect the language of each text excerpt of the paper and analyze it only if English. Specific 
 * post-processing modules are available to analyze the textual contents of papers in languages different than english: see package 
 * {@link edu.upf.taln.dri.lib.postproc postproc} <br/>
 * 
 * 
 * <br/><br/>
 * 
 * See <a href="http://driframework.readthedocs.io/en/latest/Initialize/" target="_blank">http://driframework.readthedocs.io/en/latest/Initialize/</a>
 * to obtain more information on how to configure (enable / disable) the modules of the Dr. Inventor Text Mining Framework.
 * 
 * <br/><br/>
 * 
 * Remember that if you disable a specific module, the related information fields in the instances of the data model objects will be set to null.<br/>
 * For instance if you set enableRhetoricalClassification to false, the rhetorical class is not
 * identified and thus not assigned to the sentences of the paper and the related field of the 
 * {@link edu.upf.taln.dri.lib.model.ext.Sentence Sentence} class, retrieved by means of the method
 * {@link edu.upf.taln.dri.lib.model.ext.Sentence#getRhetoricalClass() Sentence#getRhetoricalClass()}  will return a null 
 * or STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION value.<br/><br/>
 * 
 * @author francesco
 *
 */
public class ModuleConfig {
	
	private boolean enableBibEntryParsing;
	private boolean enableHeaderParsing;
	private boolean enableBabelNetParsing;
	private boolean enableTerminologyParsing;
	private boolean enableGraphParsing;
	private boolean enableRhetoricalClassification;
	private boolean enableCoreferenceResolution;
	private boolean enableCausalityParsing;
	private boolean enableMetaAnnotationsParsing;
	private boolean enableMultiLangSupport;
	
	/**
	 * The constructor set by default all modules enabled.
	 */
	public ModuleConfig() {
		this.enableBibEntryParsing = true;
		this.enableHeaderParsing = true;
		this.enableBabelNetParsing = true;
		this.enableTerminologyParsing = true;
		this.enableGraphParsing = true;
		this.enableRhetoricalClassification = true;
		this.enableCoreferenceResolution = true;
		this.enableCausalityParsing = true;
		this.enableMetaAnnotationsParsing = true;
		this.enableMultiLangSupport = false;
	}

	public boolean isEnableBibEntryParsing() {
		return enableBibEntryParsing;
	}

	public void setEnableBibEntryParsing(boolean enableBibEntryParsing) {
		this.enableBibEntryParsing = enableBibEntryParsing;
	}

	public boolean isEnableHeaderParsing() {
		return enableHeaderParsing;
	}

	public void setEnableHeaderParsing(boolean enableHeaderParsing) {
		this.enableHeaderParsing = enableHeaderParsing;
	}

	public boolean isEnableBabelNetParsing() {
		return enableBabelNetParsing;
	}

	public void setEnableBabelNetParsing(boolean enableBabelNetParsing) {
		this.enableBabelNetParsing = enableBabelNetParsing;
	}

	public boolean isEnableTerminologyParsing() {
		return enableTerminologyParsing;
	}
	
	public boolean isEnableMetaAnnotationsParsing() {
		return enableMetaAnnotationsParsing;
	}
	
	/**
	 * If you enable meta-annotation extraction, also the graph parsing will be enabled if not since
	 * is a prerequisite for meta-annotation extraction
	 * @param enableMetaAnnotationsParsing
	 */
	public void setEnableMetaAnnotationsParsing(boolean enableMetaAnnotationsParsing) {
		if(enableMetaAnnotationsParsing) {
			this.enableGraphParsing = true;
		}
		this.enableMetaAnnotationsParsing = enableMetaAnnotationsParsing;
	}

	public void setEnableTerminologyParsing(boolean enableTerminologyParsing) {
		this.enableTerminologyParsing = enableTerminologyParsing;
	}
	
	public boolean isEnableGraphParsing() {
		return enableGraphParsing;
	}

	public boolean isEnableMultiLangSupport() {
		return enableMultiLangSupport;
	}

	public void setEnableMultiLangSupport(boolean enableSentenceLanguageDetection) {
		this.enableMultiLangSupport = enableSentenceLanguageDetection;
	}

	/**
	 * IMPORTANT: when you disable the graph parsing, you also disable at the same time
	 * the causality parsing, the coreference resolution and the rhetorical classification
	 * since they are all based on graph parsing results.
	 * @param enableGraphParsing
	 */
	public void setEnableGraphParsing(boolean enableGraphParsing) {
		if(!enableGraphParsing) {
			this.enableCausalityParsing = false;
			this.enableCoreferenceResolution = false;
			this.enableRhetoricalClassification = false;
			this.enableMetaAnnotationsParsing = false;
		}
		this.enableGraphParsing = enableGraphParsing;
	}

	public boolean isEnableRhetoricalClassification() {
		return enableRhetoricalClassification;
	}
	
	/**
	 * If you enable rhetorical classification, also the graph parsing will be enabled if not since
	 * is a prerequisite for rhetorical classification
	 * @param enableRhetoricalClassification
	 */
	public void setEnableRhetoricalClassification(
			boolean enableRhetoricalClassification) {
		if(enableRhetoricalClassification) {
			this.enableGraphParsing = true;
		}
		this.enableRhetoricalClassification = enableRhetoricalClassification;
	}

	public boolean isEnableCoreferenceResolution() {
		return enableCoreferenceResolution;
	}

	/**
	 * If you enable coreference resolution, also the graph parsing will be enabled if not since
	 * is a prerequisite for coreference resolution
	 * @param enableCoreferenceResolution
	 */
	public void setEnableCoreferenceResolution(boolean enableCoreferenceResolution) {
		if(enableCoreferenceResolution) {
			this.enableGraphParsing = true;
		}
		this.enableCoreferenceResolution = enableCoreferenceResolution;
	}

	public boolean isEnableCausalityParsing() {
		return enableCausalityParsing;
	}
	
	/**
	 * If you enable causality parsing, also the graph parsing will be enabled if not since
	 * is a prerequisite for causality parsing
	 * @param enableCausalityParsing
	 */
	public void setEnableCausalityParsing(boolean enableCausalityParsing) {
		if(enableCausalityParsing) {
			this.enableGraphParsing = true;
		}
		this.enableCausalityParsing = enableCausalityParsing;
	}
	
	
	@Override
	public String toString() {
		return "ModuleConfig [enableBibEntryParsing=" + enableBibEntryParsing
				+ ", enableHeaderParsing=" + enableHeaderParsing
				+ ", enableBabelNetParsing=" + enableBabelNetParsing
				+ ", enableTerminologyParsing=" + enableTerminologyParsing
				+ ", enableGraphParsing=" + enableGraphParsing
				+ ", enableRhetoricalClassification="
				+ enableRhetoricalClassification
				+ ", enableCoreferenceResolution="
				+ enableCoreferenceResolution + ", enableCausalityParsing="
				+ enableCausalityParsing + "]";
	}

	public static ModuleConfig dCopy(ModuleConfig input) {
		if(input != null) {
			ModuleConfig inputDcopy = new ModuleConfig();
			inputDcopy.setEnableBabelNetParsing(input.isEnableBabelNetParsing());
			inputDcopy.setEnableHeaderParsing(input.isEnableHeaderParsing());
			inputDcopy.setEnableGraphParsing(input.isEnableGraphParsing());
			inputDcopy.setEnableBibEntryParsing(input.isEnableBibEntryParsing());
			inputDcopy.setEnableCausalityParsing(input.isEnableCausalityParsing());
			inputDcopy.setEnableCoreferenceResolution(input.isEnableCoreferenceResolution());
			inputDcopy.setEnableRhetoricalClassification(input.isEnableRhetoricalClassification());
			inputDcopy.setEnableTerminologyParsing(input.isEnableTerminologyParsing());
			return inputDcopy;
		}
		
		return null;
	}
	
}
