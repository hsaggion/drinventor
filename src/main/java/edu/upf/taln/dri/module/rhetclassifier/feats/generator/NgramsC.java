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
import edu.upf.taln.dri.module.rhetclassifier.feats.StopWords;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyString;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.InvalidOffsetException;

/**
 * Generate skipgrams from the text or lemmatized text of a sentence
 * 
 *
 */
public class NgramsC implements FeatCalculator<String, Annotation, DocumentCtx> {

	private String tokenAnnotationSet;
	private String tokenAnnotationName;
	private String featureName;

	private String featureFilterName;
	private String featureFilterValue;
	private boolean featureFilterStartsWith;

	private Integer ngramFactor;
	private boolean removeStopWords;
	private Document gateDoc;
	private boolean excludeCitSpan = false;

	/**
	 * Generate a list of ngram taking into account the document ordered sequence of tokens and getting the 
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
	 * @param ngramFactor
	 * @param removeStopWords
	 * @param excludeCitSpan
	 */
	public NgramsC(String tokenAnnotationSet, String tokenAnnotationName,
			String featureName,
			String featureFilterName, String featureFilterValue, boolean featureFilterStartsWith,
			Integer ngramFactor, boolean removeStopWords, boolean excludeCitSpan) {

		this.ngramFactor = 1;
		if(ngramFactor != null && ngramFactor > 0 && ngramFactor < 5) {
			this.ngramFactor = ngramFactor;
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

		String nGramString = "";

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

		// Retrieve the list of feature values for each token in tokenList - NOT FILTERED, in order to consider punctuation
		Map<Integer, Annotation> tokenMap = new HashMap<Integer, Annotation>();
		Integer positionInSentence = 0;
		for(Annotation ann : tokenListWithoutCitSpan) {
			tokenMap.put(positionInSentence++, ann);
		}

		// After getting the list of feature values for the sentence, generate the related ngrams
		if(tokenMap != null && tokenMap.size() > 0) {
			List<String> nGramsList = nGrams(tokenMap, this.ngramFactor, idOfTokenListFiltered);

			// Populate skipGramString
			if(nGramsList != null && nGramsList.size() > 0) {
				for(String ng : nGramsList) {
					if(ng != null && !ng.equals("")) {
						if(!nGramString.equals("")) {
							nGramString = nGramString + " ";
						}
						nGramString = nGramString + ng;
					}
				}
			}
		}

		retText = nGramString;
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retText != null) ? retText : "") + "");
		}
		
		return new MyString(retText);
	}

	// Utility methods
	private List<String> nGrams(Map<Integer, Annotation> orderedAnnotations, Integer ngFactor, Set<Integer> idOfTokenListFiltered) {
		List<String> retNGrams = new ArrayList<String>();

		if(orderedAnnotations != null && orderedAnnotations.size() > 0 && ngFactor != null && ngFactor >= 0) {
			for(int i = 0; i < orderedAnnotations.size(); i++) {
				Integer startToken = i;
				Integer endToken = i + ngFactor;
				if(endToken < orderedAnnotations.size()) {

					// Get all the annotations between start and end tokens
					List<Annotation> ngramTokenAnnotationList = new ArrayList<Annotation>();
					for(int t = startToken; t <= endToken; t++) {
						ngramTokenAnnotationList.add(orderedAnnotations.get(t));
					}

					// Do not consider annotations because they do not pass the filter
					boolean allNGramAnnotationPassedFilter = true;
					for(Annotation annToCheck : ngramTokenAnnotationList) {
						if(!idOfTokenListFiltered.contains(annToCheck.getId())) {
							allNGramAnnotationPassedFilter = false;
						}
					}

					if(!allNGramAnnotationPassedFilter) {
						// Not all tokens that would be part of the ngram passed the filter list (have their ID contained in idOfTokenListFiltered)
						continue;
					}

					// Check if all the tokens of the ngram have a string value that is not empty (are not punctuation)
					boolean skipAdditionOfNgram = false;
					List<String> tokenStringList = new ArrayList<String>();
					for(Annotation annToCheck : ngramTokenAnnotationList) {

						/* DEBUG: 
						try {
							System.out.println(" > Intermediate token: '" + this.gateDoc.getContent().getContent(
									tokenToSkipAnnotation.getStartNode().getOffset(), 
									tokenToSkipAnnotation.getEndNode().getOffset()) + "'");
						} catch (InvalidOffsetException e) {
							e.printStackTrace();
						}
						 */

						if( !idOfTokenListFiltered.contains(annToCheck.getId()) ) {
							skipAdditionOfNgram = true;
						}

						String tokenText = "";
						try {
							tokenText = gateDoc.getContent().getContent(annToCheck.getStartNode().getOffset(), annToCheck.getEndNode().getOffset()).toString();
						} catch (InvalidOffsetException e) {
							e.printStackTrace();
						}

						String tokenOfNgramAnnotationString = (annToCheck.getFeatures() != null && annToCheck.getFeatures().containsKey(this.featureName) && 
								!((String) annToCheck.getFeatures().get(this.featureName)).equals("")
								) ? ( (this.removeStopWords && StopWords.isStopWord(((String) annToCheck.getFeatures().get(this.featureName)).trim().toLowerCase()) ) 
										? ""
												: (String) annToCheck.getFeatures().get(this.featureName) 
										) 
										: tokenText;

								if(tokenOfNgramAnnotationString.equals("")) {
									skipAdditionOfNgram = true;
								}

								tokenStringList.add(tokenOfNgramAnnotationString);

					}

					if(skipAdditionOfNgram) {
						// DEBUG: System.out.println("   >>> SKIPPED <<<");
						continue;
					}

					// Populate nGramString
					if(tokenStringList != null && tokenStringList.size() == (this.ngramFactor + 1)) {
						String nGramString = "";
						Integer ngramIndex = -1;
						for(String token : tokenStringList) {
							ngramIndex++;

							nGramString += (ngramIndex > 0) ? "___" : "";

							nGramString += token.toLowerCase().trim();
						}

						if(nGramString != null && nGramString.length() > 0) {
							retNGrams.add(nGramString);
						}
					}

				}
			}
		}

		return retNGrams;
	}

}