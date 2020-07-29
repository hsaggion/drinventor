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

import java.util.Set;

import edu.upf.taln.dri.module.rhetclassifier.RhetoricalClassifier;
import edu.upf.taln.dri.module.rhetclassifier.feats.ctx.DocumentCtx;
import edu.upf.taln.ml.feat.base.FeatCalculator;
import edu.upf.taln.ml.feat.base.MyDouble;
import gate.Annotation;

/**
 * Match the words in the annotation against a list provided by the constructor and return the number of words of the list matched
 * 
 *
 */
public class ContainTextInListC implements FeatCalculator<Double, Annotation, DocumentCtx> {

	private Set<String> listOfWordsToMathc;
	private boolean caseInsensitiveMatch = false;
	private boolean booleanOutput = false;


	/**
	 * The list of words to match is matched against the whole text taken from the sentence annotation.
	 * 
	 * @param listOfWordsToMathc
	 * @param caseInsensitiveMatch
	 * @param booleanOutput
	 */
	public ContainTextInListC(Set<String> listOfWordsToMathc,
			boolean caseInsensitiveMatch,
			boolean booleanOutput) {
		super();
		this.listOfWordsToMathc = listOfWordsToMathc;
		this.caseInsensitiveMatch = caseInsensitiveMatch;
		this.booleanOutput = booleanOutput;
	}

	@Override
	public MyDouble calculateFeature(Annotation obj, DocumentCtx doc, String featName) {

		Double returnValue = 0d;

		if(listOfWordsToMathc != null && listOfWordsToMathc.size() > 0) {

			try {
				String textOfSentence = doc.getGateDoc().getContent().getContent(
						obj.getStartNode().getOffset(),
						obj.getEndNode().getOffset()).toString();
				
				for(String wordToMathc : listOfWordsToMathc) {
					if( wordToMathc != null && !wordToMathc.equals("") ) {
						
						if(wordToMathc.contains(" ")) {
							if(this.caseInsensitiveMatch) {
								returnValue += (textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase())) ? 1d : 0d;
							}
							else {
								returnValue += (textOfSentence.contains(wordToMathc)) ? 1d : 0d;
							}
						}
						else {
							if(this.caseInsensitiveMatch) {
								returnValue += (textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + " ") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + ".") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + ",") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + ";") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + ")") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + "]") ||
												textOfSentence.toLowerCase().contains(wordToMathc.toLowerCase() + ":") ||
												textOfSentence.toLowerCase().contains(" " + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains("." + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains("," + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains(";" + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains(":" + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains("(" + wordToMathc.toLowerCase()) ||
												textOfSentence.toLowerCase().contains("[" + wordToMathc.toLowerCase()) ) ? 1d : 0d;
							}
							else {
								returnValue += (textOfSentence.contains(wordToMathc + " ") ||
										textOfSentence.contains(wordToMathc + ".") ||
										textOfSentence.contains(wordToMathc + ",") ||
										textOfSentence.contains(wordToMathc + ";") ||
										textOfSentence.contains(wordToMathc + ")") ||
										textOfSentence.contains(wordToMathc + "]") ||
										textOfSentence.contains(wordToMathc + ":") ||
										textOfSentence.contains(" " + wordToMathc) ||
										textOfSentence.contains("." + wordToMathc) ||
										textOfSentence.contains("," + wordToMathc) ||
										textOfSentence.contains(";" + wordToMathc) ||
										textOfSentence.contains(":" + wordToMathc) ||
										textOfSentence.contains("(" + wordToMathc) ||
										textOfSentence.contains("[" + wordToMathc) ) ? 1d : 0d;
							}
						}
						
						
					}
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

		if(booleanOutput && returnValue > 0d) {
			returnValue = 1d;
		}

		MyDouble retValue = new MyDouble(returnValue);
		
		if(RhetoricalClassifier.storeClassificationFeatures) {
			obj.getFeatures().put("FEAT_" + featName, ((retValue.getValue() != null) ? retValue.getValue() : "") + "");
		}
		
		return retValue;
	}

}
