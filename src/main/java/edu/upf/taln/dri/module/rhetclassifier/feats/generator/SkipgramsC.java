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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.InvalidOffsetException;
import weka.core.Stopwords;

/**
 * Generate skipgrams from the text or lemmatized text of a sentence
 * 
 *
 */
public class SkipgramsC implements FeatCalculator<String, Annotation, DocumentCtx> {
	
	private String tokenAnnotationSet;
	private String tokenAnnotationName;
	private String featureName;
	
	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;

	private Integer skipFactor;
	private boolean removeStopWords;
	private Set<String> stopWords;
	private Document gateDoc;
	private boolean excludeCitSpan = false;

	/**
	 * Generate a list of skipgram taking into account the document ordered sequence of tokens and getting the 
	 * token text as the value of a specific feature (like lemma or string)
	 * 
	 * The three arguments that have a name starting with fratureFilter are devoted to filter by a feature name and value the annotations to consider.
	 * If featureFilterName is null or empty no filter is performed.
	 * If featureFilterName is not null or empty, only annotations having a feature with that name are considered.
	 * If also featureFilterValue is not null or empty, only annotations having a feature name equal to featureFilterName and
	 * a feature value equal to featureFilterValue are considered.
	 * If featureFilterStartsWith is true, it is only checked that the feature value starts with the featureFilterValue string.
	 * If excludeCitSpan is true, the token included in an inline citation span are not considered.
	 * 
	 * @param tokenAnnotationSet
	 * @param tokenAnnotationName
	 * @param featureName
	 * @param featureFilterName
	 * @param featureFilterValue
	 * @param featureFilterStartsWith
	 * @param skipFactor
	 * @param removeStopWords
	 * @param removeStopWords
	 */
	public SkipgramsC(String tokenAnnotationSet, String tokenAnnotationName,
			String featureName,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith,
			Integer skipFactor, boolean removeStopWords, boolean excludeCitSpan) {

		if(skipFactor != null && skipFactor >= 0 && skipFactor < 5) {
			this.skipFactor = skipFactor;
		}
		this.removeStopWords = removeStopWords;

		// Stopword list statically loaded

		this.tokenAnnotationSet = tokenAnnotationSet;
		this.tokenAnnotationName = tokenAnnotationName;
		this.featureName = featureName;
		this.featureFilterName = featureFilterName;
		this.featureFilterValue = featureFilterValue;
		this.featureFilterStartsWith = featureFilterStartsWith;
		this.excludeCitSpan = excludeCitSpan;
	}


	@Override
	public MyString calculateFeature(Annotation obj, DocumentCtx doc, String featName) {
		String retText = "";

		String skipGramString = "";

		this.gateDoc = doc.getGateDoc();
		
		AnnotationSet intersectingCitSpans = doc.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.inlineCitationAnnType).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset());
		List<Annotation> intersectingCitSpansOrdered  = gate.Utils.inDocumentOrder(intersectingCitSpans);

		List<Annotation> tokenListWithCitSpan = gate.Utils.inDocumentOrder(doc.getGateDoc().getAnnotations(this.tokenAnnotationSet).get(this.tokenAnnotationName).getContained(obj.getStartNode().getOffset(), obj.getEndNode().getOffset()));

		List<Annotation> tokenListWithoutCitSpan = new ArrayList<Annotation>();
		// Do not consider the token if inside a citation span (Analysis --> CitSpan annotation)
		// From the coplete token list (tokenListWithCitSpan) generate the token list without citation spans tokenListWithoutCitSpan
		if(this.excludeCitSpan) {
			for(Annotation tokenAnn : tokenListWithCitSpan) {
				boolean tokenInsideCitSpan = false;
				if(intersectingCitSpansOrdered != null && intersectingCitSpansOrdered.size() > 0) {
					for(Annotation intersectingCitSpan : intersectingCitSpansOrdered) {
						if(intersectingCitSpan != null && 
								tokenAnn.getStartNode().getOffset() >= intersectingCitSpan.getStartNode().getOffset() && tokenAnn.getEndNode().getOffset() <= intersectingCitSpan.getEndNode().getOffset()) {
							tokenInsideCitSpan = true;
							break;
						}
					}
				}

				if(!tokenInsideCitSpan) {
					tokenListWithoutCitSpan.add(tokenAnn);
				}
			}
		}
		else {
			tokenListWithoutCitSpan = tokenListWithCitSpan;
		}
		
		
		List<Annotation> tokenListFiltered = new ArrayList<Annotation>();

		for(Annotation ann : tokenListWithoutCitSpan) {
			boolean excludeAnnotation = false;
			if(this.featureFilterName != null && !featureFilterName.equals("")) {
				if(ann.getFeatures() == null || ann.getFeatures().size() <= 0 || 
						!ann.getFeatures().containsKey(this.featureFilterName)) {
					excludeAnnotation = true;
				}
				else if(this.featureFilterValue != null && !featureFilterValue.equals("")) {
					if(this.featureFilterStartsWith && (ann.getFeatures().get(this.featureFilterName) == null ||
							!((String) ann.getFeatures().get(this.featureFilterName)).startsWith(this.featureFilterValue)) ) {
						excludeAnnotation = true;
					}
					if(!this.featureFilterStartsWith && (ann.getFeatures().get(this.featureFilterName) == null ||
							!((String) ann.getFeatures().get(this.featureFilterName)).equals(this.featureFilterValue)) ) {
						excludeAnnotation = true;
					}
				}
			}
			
			if(!excludeAnnotation) {
				tokenListFiltered.add(ann);
			}
			else {
				/* DEBUG: 
				try {
					System.out.println("NOT ADDED STRING OF ANNOTATION: " + doc.getCtxObject().getContent().getContent(
							ann.getStartNode().getOffset(), 
							ann.getEndNode().getOffset()));
				} catch (InvalidOffsetException e) {
					e.printStackTrace();
				}
				*/
			}
		}
		
		Set<Integer> idOfTokenListFiltered = new HashSet<Integer>();
		for(Annotation ann : tokenListFiltered) {
			idOfTokenListFiltered.add(ann.getId());
		}
		

		// In tokenListFiltered and idOfTokenListFiltered I get all the annotation filtered by featureFilterName, featureFilterValue, featureFilterStartsWith
		
		// Retrieve the list of feature values for each token in tokenList - NOT FILTERED, in order to consider puncutation
		Map<Integer, Annotation> tokenMap = new HashMap<Integer, Annotation>();
		Integer positionInSentence = 0;
		for(Annotation ann : tokenListWithoutCitSpan) {
			tokenMap.put(positionInSentence++, ann);
		}
		
		// After getting the list of feature values for the sentence, generate the related skipgrams
		if(tokenMap != null && tokenMap.size() > 0) {
			List<String> skipGrams = skipGrams(tokenMap, this.skipFactor, idOfTokenListFiltered);

			// Populate skipGramString
			if(skipGrams != null && skipGrams.size() > 0) {
				for(String sg : skipGrams) {
					if(sg != null && !sg.equals("")) {
						if(!skipGramString.equals("")) {
							skipGramString = skipGramString + " ";
						}
						skipGramString = skipGramString + sg;
					}
				}
			}
		}
		
		retText = skipGramString;
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retText != null) ? retText : "") + "");
		}
		
		return new MyString(retText);
	}

	// Utility methods
	private List<String> skipGrams(Map<Integer, Annotation> orderedAnnotations, Integer skipFactor, Set<Integer> idOfTokenListFiltered) {
		List<String> retSkipGrams = new ArrayList<String>();

		if(orderedAnnotations != null && orderedAnnotations.size() > 0 && skipFactor != null && skipFactor >= 0) {
			for(int i = 0; i < orderedAnnotations.size(); i++) {
				Integer startToken = i;
				Integer endToken = i + skipFactor + 1;
				if(endToken < orderedAnnotations.size()) {
					Annotation startAnnotation = orderedAnnotations.get(startToken);
					Annotation endAnnotation = orderedAnnotations.get(endToken);
					
					// Do not consider annotations because they do not pass the filter
					if(!idOfTokenListFiltered.contains(startAnnotation.getId()) || !idOfTokenListFiltered.contains(endAnnotation.getId())) {
						continue;
					}
					
					String tokenText_start = "";
					try {
						tokenText_start = gateDoc.getContent().getContent(startAnnotation.getStartNode().getOffset(), startAnnotation.getEndNode().getOffset()).toString();
					} catch (InvalidOffsetException e) {
						e.printStackTrace();
					}
					
					String tokenText_end = "";
					try {
						tokenText_end = gateDoc.getContent().getContent(endAnnotation.getStartNode().getOffset(), endAnnotation.getEndNode().getOffset()).toString();
					} catch (InvalidOffsetException e) {
						e.printStackTrace();
					}
					
					String startAnnotationString = (startAnnotation.getFeatures() != null && startAnnotation.getFeatures().containsKey(this.featureName) && 
							!((String) startAnnotation.getFeatures().get(this.featureName)).equals("")
							) ? ( (this.removeStopWords && Stopwords.isStopword(((String) startAnnotation.getFeatures().get(this.featureName)).trim().toLowerCase())) 
									? ""
									: (String) startAnnotation.getFeatures().get(this.featureName)
							    ) 
							  : tokenText_start;
					String endAnnotationString = (endAnnotation.getFeatures() != null && endAnnotation.getFeatures().containsKey(this.featureName) && 
							!((String) endAnnotation.getFeatures().get(this.featureName)).equals("")
							) ? ( (this.removeStopWords && Stopwords.isStopword(((String) endAnnotation.getFeatures().get(this.featureName)).trim().toLowerCase())) 
							        ? ""
							        : (String) endAnnotation.getFeatures().get(this.featureName) 
							    ) 
							  : tokenText_end;
							
							// DEBUG: System.out.println("Skip " + this.skipFactor + " from: '" + startAnnotationString + "' to: '" + endAnnotationString + "'");
					
					// Check if in skipgrams with skipFactor >= 1 the intermediate tokens to skip have a string value (are not punctuation)
					boolean skipAdditionOfSkipgram = false;
					if(this.skipFactor > 0 && startToken + 1 < endToken) {
						for(int k = startToken + 1; k < endToken; k++) {
							Annotation tokenToSkipAnnotation = orderedAnnotations.get(k);
							
							/* DEBUG: 
							try {
								System.out.println(" > Intermediate token: '" + this.gateDoc.getContent().getContent(
										tokenToSkipAnnotation.getStartNode().getOffset(), 
										tokenToSkipAnnotation.getEndNode().getOffset()) + "'");
							} catch (InvalidOffsetException e) {
								e.printStackTrace();
							}
							*/
							
							if( !idOfTokenListFiltered.contains(tokenToSkipAnnotation.getId()) ) {
								skipAdditionOfSkipgram = true;
							}
							
							String tokenText_skip = "";
							try {
								tokenText_skip = gateDoc.getContent().getContent(tokenToSkipAnnotation.getStartNode().getOffset(), tokenToSkipAnnotation.getEndNode().getOffset()).toString();
							} catch (InvalidOffsetException e) {
								e.printStackTrace();
							}
							
							String tokenToSkipAnnotationString = (tokenToSkipAnnotation.getFeatures() != null && tokenToSkipAnnotation.getFeatures().containsKey(this.featureName) && 
									!((String) tokenToSkipAnnotation.getFeatures().get(this.featureName)).equals("")
									) ? ( (this.removeStopWords && this.stopWords.contains(((String) tokenToSkipAnnotation.getFeatures().get(this.featureName)).trim().toLowerCase())) 
									        ? ""
									        : (String) tokenToSkipAnnotation.getFeatures().get(this.featureName) 
									    ) 
									  : tokenText_skip;
							
							if(tokenToSkipAnnotationString.equals("")) {
								skipAdditionOfSkipgram = true;
							}
						}
					}
					
					if(skipAdditionOfSkipgram) {
						// DEBUG: System.out.println("   >>> SKIPPED <<<");
						continue;
					}
							
					if(!startAnnotationString.trim().equals("") && !endAnnotationString.trim().equals("")) {
						retSkipGrams.add(startAnnotationString.trim().replace(" ", "_") + "___" + endAnnotationString.trim().replace(" ", "_"));
					}
				}
			}
		}
		
		return retSkipGrams;
	}

}