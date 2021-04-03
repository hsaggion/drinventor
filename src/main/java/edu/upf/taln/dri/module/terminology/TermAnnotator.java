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
package edu.upf.taln.dri.module.terminology;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
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
 * Annotate sequences of tokens as terms on the basis ofthe following set of
 * POS sequence patterns that identifies these terms:
 * [JN]*N <-- Best one
 * [NV]*J?N+
 * J+N+
 * [JN]ID?N+
 * [JN].*?N
 * 
 */
@CreoleResource(name = "DRI Modules - Candidate Term Spotter")
public class TermAnnotator  extends AbstractLanguageAnalyser implements ProcessingResource, DRIModule {

	private static Logger logger = Logger.getLogger(TermAnnotator.class);	

	private static final long serialVersionUID = 1L;
	private boolean annotationReset = false;

	// List of POS patterns (regexp) to check for candidate terms
	private static List<String> POSpatterns;

	static {
		POSpatterns = new ArrayList<String>();
		POSpatterns.add("[JN]*N");
		POSpatterns.add("[NV]*J?N+");
		POSpatterns.add("J+N+");
		POSpatterns.add("[JN]ID?N+");
		POSpatterns.add("[JN].*?N");

		POSpatterns = Collections.unmodifiableList(POSpatterns);
	}

	// Input and output annotation
	private String inputSentenceASname;
	private String inputSentenceAStype;
	private String inputTokenASname;
	private String inputTokenAStype;
	private String outputTermASname;
	private String outputTermAStype;


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
	@CreoleParameter(defaultValue = "Sentence", comment = "The name of the annotation type to read input sentence annotations")
	public void setInputSentenceAStype(String inputSentenceAStype) {
		this.inputSentenceAStype = inputSentenceAStype;
	}

	public String getInputTokenASname() {
		return inputTokenASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Analysis", comment = "The name of the input annotation set to read token annotations from")
	public void setInputTokenASname(String inputTokenASname) {
		this.inputTokenASname = inputTokenASname;
	}

	public String getInputTokenAStype() {
		return inputTokenAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Token", comment = "The name of the annotation type to read input token annotations")
	public void setInputTokenAStype(String inputTokenAStype) {
		this.inputTokenAStype = inputTokenAStype;
	}

	public String getOutputTermASname() {
		return outputTermASname;
	}

	@RunTime
	@CreoleParameter(defaultValue = "", comment = "The name of the output annotation set to write term annotations to")
	public void setOutputTermASname(String outputTermASname) {
		this.outputTermASname = outputTermASname;
	}

	public String getOutputTermAStype() {
		return outputTermAStype;
	}

	@RunTime
	@CreoleParameter(defaultValue = "Term", comment = "The name of the annotation type of term annotations")
	public void setOutputTermAStype(String outputTermAStype) {
		this.outputTermAStype = outputTermAStype;
	}

	public void execute() throws ExecutionException {
		this.annotationReset = false;
		
		// Get the document to process
		gate.Document doc = getDocument();

		// Normalize variables
		String inputSentenceASnameAppo = (this.inputSentenceASname != null) ? this.inputSentenceASname : ImporterBase.driAnnSet;
		String inputSentenceAStypeAppo = (this.inputSentenceAStype != null && this.inputSentenceAStype.length() > 0) ? this.inputSentenceAStype : ImporterBase.sentenceAnnType;

		String inputTokenASnameAppo = (this.inputTokenASname != null) ? this.inputTokenASname : ImporterBase.driAnnSet;
		String inputTokenAStypeAppo = (this.inputTokenAStype != null && this.inputTokenAStype.length() > 0) ? this.inputTokenAStype : ImporterBase.tokenAnnType;

		String outputTermASnameAppo = (this.outputTermASname != null) ? this.outputTermASname : ImporterBase.term_AnnSet;
		String outputTermAStypeAppo = (this.outputTermAStype != null && this.outputTermAStype.length() > 0) ? this.outputTermAStype : ImporterBase.term_CandOcc;

		// Get the annotation set of the sentences to analyze
		List<Annotation> sentenceAnnotationList = GateUtil.getAnnInDocOrder(this.document, inputSentenceASnameAppo, inputSentenceAStypeAppo);

		AnnotationSet termAnnotationSet = doc.getAnnotations(outputTermASnameAppo);

		// Browse all sentences of the documents
		for(Annotation sentenceAnn : sentenceAnnotationList) {
			if(sentenceAnn != null) {
				List<Annotation> tokenOfSentenceList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(this.document, inputTokenASnameAppo, inputTokenAStypeAppo, sentenceAnn);

				// POS sequence patterns
				/*
					[JN]*N <-- Best one
					[NV]*J?N+
					J+N+
					[JN]ID?N+
					[JN].*?N
				 */

				// Get the pos pattern that characterize the sentence
				// NOTE: every token annotation has the "category" features that contains the POS of tge token 
				String POSpatternOfSentence = "";
				try {
					for(Annotation tokenAnn : tokenOfSentenceList) {
						boolean insertedTokenPOS = false;
						String tokenPOS = GateUtil.getStringFeature(tokenAnn, ImporterBase.token_POSfeat).orElse(null);
						if(tokenPOS != null && tokenPOS.length() > 0) {
							POSpatternOfSentence += tokenPOS.substring(0, 1);
							insertedTokenPOS = true;
						}

						if(!insertedTokenPOS) {
							POSpatternOfSentence += "^";
						}
					}

					// Add to the sentence a feature with the POS pattern characterizing the same sentence
					sentenceAnn.setFeatures( (sentenceAnn.getFeatures() != null) ? sentenceAnn.getFeatures() : Factory.newFeatureMap() );
					sentenceAnn.getFeatures().put(ImporterBase.sentence_POSpatternFeat, POSpatternOfSentence);

				} catch (Exception e) {
					Util.notifyException("Retrieving sentence token POSs", e, logger);
				}


				if(POSpatternOfSentence != null && !POSpatternOfSentence.equals("")) {

					// For every POS pattern for candidate term identification, check for a possible match
					for(String patternForTerm : POSpatterns) {
						Pattern pattern = Pattern.compile(patternForTerm);
						Matcher matcher = pattern.matcher(POSpatternOfSentence);
						// check all occurrences
						while (matcher.find()) {
							try {
								// logger.debug(matchCount + " > Start index: " + matcher.start());
								// logger.debug(matchCount + " > End index: " + matcher.end() + " ");
								// logger.debug(matchCount + " > GROUP: " + matcher.group());

								Long startOffset = tokenOfSentenceList.get(matcher.start()).getStartNode().getOffset();
								Long endOffset = tokenOfSentenceList.get(matcher.end() - 1).getEndNode().getOffset();

								// logger.debug(matchCount + " > String: " + doc.getContent().getContent(startOffset, endOffset).toString());

								// Create candidate term annotation
								FeatureMap fm = Factory.newFeatureMap();
								fm.put(ImporterBase.term_CandOcc_regexPOSFeat, patternForTerm);
								fm.put(ImporterBase.term_CandOcc_actualPOSFeat, matcher.group());
								termAnnotationSet.add(startOffset, endOffset, ImporterBase.term_CandOcc, fm); // OLD TERM ANN TYPE: outputTermAStypeAppo + patternForTerm
							} catch (Exception e) {
								Util.notifyException("Creating candidate term occurrence annotation", e, logger);
							}
						}
					}

				}
			}
		}

	}

	@Override
	public boolean resetAnnotations() {
		if(!this.annotationReset) {
			// Normalize variables
			String outputTermASnameAppo = (this.outputTermASname != null) ? this.outputTermASname : ImporterBase.term_AnnSet;
			String outputTermAStypeAppo = (this.outputTermAStype != null && this.outputTermAStype.length() > 0) ? this.outputTermAStype : ImporterBase.term_CandOcc;
			
			List<Annotation> candidateTermAnnotations = GateUtil.getAnnInDocOrder(this.document, outputTermASnameAppo, outputTermAStypeAppo);
			
			Set<Integer> candidateTermAnnToDel = candidateTermAnnotations.stream().map((ann) -> ann.getId()).collect(Collectors.toSet());
			candidateTermAnnToDel.forEach((annId) -> {
				Annotation candidateTermAnn = this.document.getAnnotations(outputTermASnameAppo).get(annId);
				if(candidateTermAnn != null) {
					this.document.getAnnotations(outputTermASnameAppo).remove(candidateTermAnn);
				}
			});
			
			this.annotationReset = true;
		}
		
		return false;
	}

}
