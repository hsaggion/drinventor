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
package edu.upf.taln.dri.module.rhetclassifier.feats.ctx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.parser.MateParser;
import edu.upf.taln.dri.module.rhetclassifier.feats.StopWords;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

/**
 * Hold sentence-context for rhetorical sentence classification.
 * 
 *
 */
public class DocumentCtx {

	private Document gateDoc;

	private String classNameRhet;
	private String classNameAspect;
	private String classNameMainCit;
	private String classNameSecondaryCit;
	private Double classNameSummary;

	private String agreementRhet;
	private String agreementAspect;
	private String agreementMainCit;
	private String agreementSecondaryCit;
	private String agreementSummary;

	private Double instanceWeight = 1d;

	private Map<String, Double> rawTF_map = new HashMap<String, Double>();
	private Double maxTFvalue = 0d;
	private Map<String, Double> term_DF_map = new HashMap<String, Double>();

	// Consturctor
	public DocumentCtx(Document gateDoc, String classNameRhet,
			String classNameAspect, String classNameMainCit,
			String classNameSecondaryCit, Double classNameSummary,
			String agreementRhet, String agreementAspect,
			String agreementMainCit, String agreementSecondaryCit,
			String agreementSummary,
			Map<String, Double> rawTF_map,
			Double maxTFvalue,
			Map<String, Double> term_DF_map) {
		super();
		this.gateDoc = gateDoc;
		this.classNameRhet = classNameRhet;
		this.classNameAspect = classNameAspect;
		this.classNameMainCit = classNameMainCit;
		this.classNameSecondaryCit = classNameSecondaryCit;
		this.classNameSummary = classNameSummary;
		this.agreementRhet = agreementRhet;
		this.agreementAspect = agreementAspect;
		this.agreementMainCit = agreementMainCit;
		this.agreementSecondaryCit = agreementSecondaryCit;
		this.agreementSummary = agreementSummary;
		this.rawTF_map = rawTF_map;
		this.maxTFvalue = maxTFvalue;
		this.term_DF_map = term_DF_map;
	}


	public Document getGateDoc() {
		return gateDoc;
	}

	public String getClassNameRhet() {
		return classNameRhet;
	}

	public String getClassNameAspect() {
		return classNameAspect;
	}

	public String getClassNameMainCit() {
		return classNameMainCit;
	}

	public String getClassNameSecondaryCit() {
		return classNameSecondaryCit;
	}

	public Double getClassNameSummary() {
		return classNameSummary;
	}

	public String getAgreementRhet() {
		return agreementRhet;
	}

	public String getAgreementAspect() {
		return agreementAspect;
	}

	public String getAgreementMainCit() {
		return agreementMainCit;
	}

	public String getAgreementSecondaryCit() {
		return agreementSecondaryCit;
	}

	public String getAgreementSummary() {
		return agreementSummary;
	}

	public Map<String, Double> getRawTF_map() {
		return rawTF_map;
	}

	public Double getMaxTFvalue() {
		return maxTFvalue;
	}

	public Map<String, Double> getTerm_DF_map() {
		return term_DF_map;
	}

	public Double getInstanceWeight() {
		return instanceWeight;
	}

	public void setInstanceWeight(Double instanceWeight) {
		this.instanceWeight = instanceWeight;
	}

	public static Double computeAverageTFIDFofTerms(DocumentCtx ctx, Annotation sentence) {
		double retDouble = 0d;
		double termCount = 0d;

		AnnotationSet intersectingTokens = ctx.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
		List<Annotation> intersectingTokensOrderedList = gate.Utils.inDocumentOrder(intersectingTokens);
		if(intersectingTokensOrderedList != null && intersectingTokensOrderedList.size() > 0) {
			for(Annotation intersectingToken : intersectingTokensOrderedList) {
				if(intersectingToken != null) {
					String lemma = GateUtil.getStringFeature(intersectingToken, MateParser.lemmaFeat).orElse(null);
					if(lemma != null && !lemma.equals("") && !StopWords.isStopWord(lemma.toLowerCase()) &&
							ctx.getTerm_DF_map().containsKey(lemma) && ctx.getTerm_DF_map().get(lemma) != null &&
							ctx.getRawTF_map().containsKey(lemma) && ctx.getRawTF_map().get(lemma) != null &&
							ctx.getMaxTFvalue() != null && ctx.getMaxTFvalue() > 0d) {

						try {
							// Compute term frequency - double normalization 0.5
							double tf = 0.5d + 0.5d * ( ctx.getRawTF_map().get(lemma) / ctx.getMaxTFvalue() );
							// Compute inverse document frequency
							double idf = Math.log( 41 / ctx.getTerm_DF_map().get(lemma) ); // The total number of document is 40 from DRI Corpus and 1 the current document under analysis

							retDouble += (tf * idf);
							termCount += 1d;
						}
						catch (Exception e) {
							System.out.println("Error while calculating TF-IDF!");
						}

					}
				}
			}
		}



		return (termCount > 0d) ? (retDouble / termCount) : 0d;
	}

	public static Double computeTFIDFsimilarityOfSentences(DocumentCtx ctx, Annotation sentence1, Annotation sentence2) {
		double retDouble = 0d;

		// Sentence 1
		Map<String, Double> sent1Map = new HashMap<String, Double>();
		AnnotationSet intersectingTokens_sent1 = ctx.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(sentence1.getStartNode().getOffset(), sentence1.getEndNode().getOffset());
		List<Annotation> intersectingTokensOrderedList_sent1 = gate.Utils.inDocumentOrder(intersectingTokens_sent1);

		if(intersectingTokensOrderedList_sent1 != null && intersectingTokensOrderedList_sent1.size() > 0) {
			for(Annotation intersectingToken : intersectingTokensOrderedList_sent1) {
				if(intersectingToken != null) {
					String lemma = GateUtil.getStringFeature(intersectingToken, MateParser.lemmaFeat).orElse(null);
					if(lemma != null && !lemma.equals("") && !StopWords.isStopWord(lemma.toLowerCase()) &&
							ctx.getTerm_DF_map().containsKey(lemma) && ctx.getTerm_DF_map().get(lemma) != null &&
							ctx.getRawTF_map().containsKey(lemma) && ctx.getRawTF_map().get(lemma) != null &&
							ctx.getMaxTFvalue() != null && ctx.getMaxTFvalue() > 0d) {


						try {
							// Compute term frequency - double normalization 0.5
							double tf = 0.5d + 0.5d * ( ctx.getRawTF_map().get(lemma) / ctx.getMaxTFvalue() );
							// Compute inverse document frequency
							double idf = Math.log( 41 / ctx.getTerm_DF_map().get(lemma) ); // The total number of document is 40 from DRI Corpus and 1 the current document under analysis

							double termTfIdf = tf * idf;
							sent1Map.put(lemma, termTfIdf);
						}
						catch (Exception e) {
							System.out.println("Error while calculating TF-IDF!");
						}

					}
				}
			}
		}


		// Sentence 2
		Map<String, Double> sent2Map = new HashMap<String, Double>();
		AnnotationSet intersectingTokens_sent2 = ctx.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.tokenAnnType).getContained(sentence2.getStartNode().getOffset(), sentence2.getEndNode().getOffset());
		List<Annotation> intersectingTokensOrderedList_sent2 = gate.Utils.inDocumentOrder(intersectingTokens_sent2);

		if(intersectingTokensOrderedList_sent2 != null && intersectingTokensOrderedList_sent2.size() > 0) {
			for(Annotation intersectingToken : intersectingTokensOrderedList_sent2) {
				if(intersectingToken != null) {
					String lemma = GateUtil.getStringFeature(intersectingToken, MateParser.lemmaFeat).orElse(null);
					if(lemma != null && !lemma.equals("") && !StopWords.isStopWord(lemma.toLowerCase()) &&
							ctx.getTerm_DF_map().containsKey(lemma) && ctx.getTerm_DF_map().get(lemma) != null &&
							ctx.getRawTF_map().containsKey(lemma) && ctx.getRawTF_map().get(lemma) != null &&
							ctx.getMaxTFvalue() != null && ctx.getMaxTFvalue() > 0d) {


						try {
							// Compute term frequency - double normalization 0.5
							double tf = 0.5d + 0.5d * ( ctx.getRawTF_map().get(lemma) / ctx.getMaxTFvalue() );
							// Compute inverse document frequency
							double idf = Math.log( 41 / ctx.getTerm_DF_map().get(lemma) ); // The total number of document is 40 from DRI Corpus and 1 the current document under analysis

							double termTfIdf = tf * idf;
							sent2Map.put(lemma, termTfIdf);
						}
						catch (Exception e) {
							System.out.println("Error while calculating TF-IDF!");
						}

					}
				}
			}
		}


		// In sent1Map and sent2Map got the map of terms of the sentence and their TF-IDF

		// Cosine similarity of sentence term vectors

		// NUMERATOR:
		double sumOfProducts = 0d;
		for(Entry<String, Double> sent1entry : sent1Map.entrySet()) {
			if(sent1entry != null && sent1entry.getKey() != null && sent1entry.getValue() != null) {
				if(sent2Map.containsKey(sent1entry.getKey()) && sent2Map.get(sent1entry.getKey()) != null) {
					double product = sent1entry.getValue() * sent2Map.get(sent1entry.getKey());
					sumOfProducts += product;
				}
			} 
		}


		// DENOMINATOR:
		double sum1ofcomp = 0d;
		for(Entry<String, Double> sent1entry : sent1Map.entrySet()) {
			if(sent1entry != null && sent1entry.getKey() != null && sent1entry.getValue() != null) {
				sum1ofcomp += (sent1entry.getValue() * sent1entry.getValue());
			}
		}
		double sum2ofcomp = 0d;
		for(Entry<String, Double> sent2entry : sent2Map.entrySet()) {
			if(sent2entry != null && sent2entry.getKey() != null && sent2entry.getValue() != null) {
				sum2ofcomp += (sent2entry.getValue() * sent2entry.getValue());
			}
		}

		double denomin = Math.sqrt(sum1ofcomp) * Math.sqrt(sum2ofcomp);

		retDouble = (denomin > 0d) ? (sumOfProducts / denomin) : 0d;


		return retDouble;
	}

}
