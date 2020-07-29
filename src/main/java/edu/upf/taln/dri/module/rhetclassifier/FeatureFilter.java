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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class FeatureFilter {

	public static List<Filter> setFilterChain_Rhetorical_1() {
		// SET FILTER CHAIN
		List<Filter> filterList = new ArrayList<Filter>();

		try {
			Remove removeFilter_FIRST = new Remove();
			removeFilter_FIRST.setOptions(weka.core.Utils.splitOptions("-R 1-16"));
			filterList.add(removeFilter_FIRST);

			StringToWordVector stringToWordVectorFilter_UNI_LEMMA = new StringToWordVector();
			stringToWordVectorFilter_UNI_LEMMA.setOptions(weka.core.Utils.splitOptions("-R 1 -P UNI_LEMMA_ -W 300 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_UNI_LEMMA);

			StringToWordVector stringToWordVectorFilter_UNI_LEM_VERB = new StringToWordVector();
			stringToWordVectorFilter_UNI_LEM_VERB.setOptions(weka.core.Utils.splitOptions("-R 1 -P UNI_LEM_VERB_ -W 100 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_UNI_LEM_VERB);

			StringToWordVector stringToWordVectorFilter_UNI_LEM_NOUN = new StringToWordVector();
			stringToWordVectorFilter_UNI_LEM_NOUN.setOptions(weka.core.Utils.splitOptions("-R 1 -P UNI_LEM_NOUN_ -W 100 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_UNI_LEM_NOUN);


			Remove removeFilter_SECOND = new Remove();
			removeFilter_SECOND.setOptions(weka.core.Utils.splitOptions("-R 1-5"));
			filterList.add(removeFilter_SECOND);


			StringToWordVector stringToWordVectorFilter_BIG_LEM = new StringToWordVector();
			stringToWordVectorFilter_BIG_LEM.setOptions(weka.core.Utils.splitOptions("-R 1 -P BIG_LEM_ -W 200 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_BIG_LEM);

			StringToWordVector stringToWordVectorFilter_TRIG_LEM = new StringToWordVector();
			stringToWordVectorFilter_TRIG_LEM.setOptions(weka.core.Utils.splitOptions("-R 1 -P TRIG_LEM_ -W 200 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_TRIG_LEM);

			StringToWordVector stringToWordVectorFilter_SK1_LEM = new StringToWordVector();
			stringToWordVectorFilter_SK1_LEM.setOptions(weka.core.Utils.splitOptions("-R 1 -P SK1_LEM_ -W 200 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_SK1_LEM);

			StringToWordVector stringToWordVectorFilter_SK2_LEM = new StringToWordVector();
			stringToWordVectorFilter_SK2_LEM.setOptions(weka.core.Utils.splitOptions("-R 1 -P SK2_LEM_ -W 200 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_SK2_LEM);

			StringToWordVector stringToWordVectorFilter_SK3_LEM = new StringToWordVector();
			stringToWordVectorFilter_SK3_LEM.setOptions(weka.core.Utils.splitOptions("-R 1 -P SK3_LEM_ -W 200 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_SK3_LEM);

			StringToWordVector stringToWordVectorFilter_DEP_REL = new StringToWordVector();
			stringToWordVectorFilter_DEP_REL.setOptions(weka.core.Utils.splitOptions("-R 1 -P DEP_REL_ -W 400 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\""));
			filterList.add(stringToWordVectorFilter_DEP_REL);


			StringToNominal stringToNominal_FIRST = new StringToNominal();
			stringToNominal_FIRST.setOptions(weka.core.Utils.splitOptions("-R 1"));
			filterList.add(stringToNominal_FIRST);


			Remove removeFilter_THIRD = new Remove();
			removeFilter_THIRD.setOptions(weka.core.Utils.splitOptions("-R 40,43,45,48,50,51,53,60,63,95,98,99,102,103,106,107,109,111,113,115"));
			filterList.add(removeFilter_THIRD);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return filterList;
	}

	public static Instances setClassAsLastAttr_Rhetorical_1(Instances wekaInst, String attributeName) {

		if(StringUtils.isEmpty(attributeName)) {
			return wekaInst;
		}

		// Search attribute with name "Class_rhetorical"
		int classRhetIndex = -1;
		for(int attIndex = 0; attIndex < wekaInst.numAttributes(); attIndex++) {
			if(wekaInst.attribute(attIndex).name().equals(attributeName.trim())) {
				classRhetIndex = attIndex;
				break;
			}
		}

		// System.out.println("CLASS RHETORICAL INDEX: " + classRhetIndex + " over " + wekaInst.numAttributes() + " attributes.");
		try {
			if(classRhetIndex == 0 && wekaInst.numAttributes() > 1) {
				Reorder reorderFilter = new Reorder();
				reorderFilter.setInputFormat(wekaInst);
				reorderFilter.setOptions(weka.core.Utils.splitOptions("-R 2-last,first"));
				return Filter.useFilter(wekaInst, reorderFilter);
			}
			if(classRhetIndex > 0 && classRhetIndex < (wekaInst.numAttributes() - 1)) {
				Reorder reorderFilter = new Reorder();
				reorderFilter.setInputFormat(wekaInst);
				if(classRhetIndex == 1) {
					reorderFilter.setOptions(weka.core.Utils.splitOptions("-R first,3-last,2"));
				}
				else if(classRhetIndex == (wekaInst.numAttributes() - 2)) {
					reorderFilter.setOptions(weka.core.Utils.splitOptions("-R first-" + (wekaInst.numAttributes() - 2) + ",last," + (wekaInst.numAttributes() - 1)));
				}
				else {
					reorderFilter.setOptions(weka.core.Utils.splitOptions("-R first-" + (classRhetIndex) + "," + (classRhetIndex + 2) + "-last," + (classRhetIndex + 1)));
				}
				return Filter.useFilter(wekaInst, reorderFilter);
			}
			else {
				return wekaInst;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


}
