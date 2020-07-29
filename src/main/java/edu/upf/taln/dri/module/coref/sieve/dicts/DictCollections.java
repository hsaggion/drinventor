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
package edu.upf.taln.dri.module.coref.sieve.dicts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.lib.Factory;

/**
 * Co-reference mention spotting in-memory dictionaries
 * 
 *
 */
public class DictCollections {

	private static Logger logger = Logger.getLogger(DictCollections.class);

	private static boolean dictsInitialized = false;

	// DICTS VARS - START
	public static final Set<String> allPronouns = new HashSet<String>();
	public static final Set<String> personPronouns = new HashSet<String>();
	public static final Set<String> nonWords = new HashSet(Arrays.asList("mm", "hmm", "ahem", "um"));
	public static final Set<String> copulas = new HashSet(Arrays.asList("is","are","were", "was","be", "been","become","became","becomes","seem","seemed","seems","remain","remains","remained"));
	public static final Set<String> quantifiers = new HashSet(Arrays.asList("not","every","any","none","everything","anything","nothing","all","enough"));
	public static final Set<String> parts = new HashSet(Arrays.asList("half","one","two","three","four","five","six","seven","eight","nine","ten","hundred","thousand","million","billion","tens","dozens","hundreds","thousands","millions","billions","group","groups","bunch","number","numbers","pinch","amount","amount","total","all","mile","miles","pounds"));
	public static final Set<String> temporals = new HashSet(Arrays.asList(
			"second", "minute", "hour", "day", "week", "month", "year", "decade", "century", "millennium",
			"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "now",
			"yesterday", "tomorrow", "age", "time", "era", "epoch", "morning", "evening", "day", "night", "noon", "afternoon",
			"semester", "trimester", "quarter", "term", "winter", "spring", "summer", "fall", "autumn", "season",
			"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"));


	public static final Set<String> femalePronouns = new HashSet(Arrays.asList(new String[]{ "her", "hers", "herself", "she" }));
	public static final Set<String> malePronouns = new HashSet(Arrays.asList(new String[]{ "he", "him", "himself", "his" }));
	public static final Set<String> neutralPronouns = new HashSet(Arrays.asList(new String[]{ "it", "its", "itself", "where", "here", "there", "which" }));
	public static final Set<String> possessivePronouns = new HashSet(Arrays.asList(new String[]{ "my", "your", "his", "her", "its","our","their","whose" }));
	public static final Set<String> otherPronouns = new HashSet(Arrays.asList(new String[]{ "who", "whom", "whose", "where", "when", "which" }));
	public static final Set<String> thirdPersonPronouns = new HashSet(Arrays.asList(new String[]{ "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves" }));
	public static final Set<String> secondPersonPronouns = new HashSet(Arrays.asList(new String[]{ "you", "yourself", "yours", "your", "yourselves" }));
	public static final Set<String> firstPersonPronouns = new HashSet(Arrays.asList(new String[]{ "i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our" }));
	public static final Set<String> moneyPercentNumberPronouns = new HashSet(Arrays.asList(new String[]{ "it", "its" }));
	public static final Set<String> dateTimePronouns = new HashSet(Arrays.asList(new String[]{ "when" }));
	public static final Set<String> organizationPronouns = new HashSet(Arrays.asList(new String[]{ "it", "its", "they", "their", "them", "which"}));
	public static final Set<String> locationPronouns = new HashSet(Arrays.asList(new String[]{ "it", "its", "where", "here", "there" }));
	public static final Set<String> inanimatePronouns = new HashSet(Arrays.asList(new String[]{ "it", "itself", "its", "where", "when" }));
	public static final Set<String> animatePronouns = new HashSet(Arrays.asList(new String[]{ "i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our", "you", "yourself", "yours", "your", "yourselves", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves", "who", "whom", "whose" }));
	public static final Set<String> indefinitePronouns = new HashSet(Arrays.asList(new String[]{"another", "anybody", "anyone", "anything", "each", "either", "enough", "everybody", "everyone", "everything", "less", "little", "much", "neither", "no one", "nobody", "nothing", "one", "other", "plenty", "somebody", "someone", "something", "both", "few", "fewer", "many", "others", "several", "all", "any", "more", "most", "none", "some", "such"}));
	public static final Set<String> relativePronouns = new HashSet(Arrays.asList(new String[]{"that","who","which","whom","where","whose"}));
	public static final Set<String> GPEPronouns = new HashSet(Arrays.asList(new String[]{ "it", "itself", "its", "they","where" }));
	public static final Set<String> pluralPronouns = new HashSet(Arrays.asList(new String[]{ "we", "us", "ourself", "ourselves", "ours", "our", "yourself", "yourselves", "they", "them", "themself", "themselves", "theirs", "their" }));
	public static final Set<String> singularPronouns = new HashSet(Arrays.asList(new String[]{ "i", "me", "myself", "mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's" }));
	public static final Set<String> facilityVehicleWeaponPronouns = new HashSet(Arrays.asList(new String[]{ "it", "itself", "its", "they", "where" }));
	public static final Set<String> miscPronouns = new HashSet(Arrays.asList(new String[]{"it", "itself", "its", "they", "where" }));
	public static final Set<String> reflexivePronouns = new HashSet(Arrays.asList(new String[]{"myself", "yourself", "yourselves", "himself", "herself", "itself", "ourselves", "themselves", "oneself"}));
	public static final Set<String> transparentNouns = new HashSet(Arrays.asList(new String[]{"bunch", "group",
			"breed", "class", "ilk", "kind", "half", "segment", "top", "bottom", "glass", "bottle",
			"box", "cup", "gem", "idiot", "unit", "part", "stage", "name", "division", "label", "group", "figure",
			"series", "member", "members", "first", "version", "site", "side", "role", "largest", "title", "fourth",
			"third", "second", "number", "place", "trio", "two", "one", "longest", "highest", "shortest",
			"head", "resident", "collection", "result", "last"
	}));

	public static final Set<String> maleWords = new HashSet<String>();
	public static final Set<String> femaleWords = new HashSet<String>();
	public static final Set<String> neutralWords = new HashSet<String>();

	public static final Set<String> animateWords = new HashSet<String>();
	public static final Set<String> inanimateWords = new HashSet<String>();
	// DICTS VARS - STOP

	public static void initDictionaries() {
		if(!dictsInitialized) {
			for(String s: animatePronouns){
				personPronouns.add(s);
			}

			allPronouns.addAll(firstPersonPronouns);
			allPronouns.addAll(secondPersonPronouns);
			allPronouns.addAll(thirdPersonPronouns);
			allPronouns.addAll(otherPronouns);

			String dictListFolder = Factory.getResourceFolderFullPath() + File.separator + "lexicons" + File.separator + "Coreference" + File.separator + "DICT_LISTS" + File.separator;

			// Male nouns list
			File maleNounFile = new File(dictListFolder + "male.unigrams.txt");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(maleNounFile), "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					if(StringUtils.isNotBlank(str)) {
						maleWords.add(str);
					}
				}
				in.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				logger.error("Error while loading male names list.");
			} catch (IOException e) {
				logger.error("Error while loading male names list.");
			}

			// Female nouns list
			File femaleNounFile = new File(dictListFolder + "female.unigrams.txt");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(femaleNounFile), "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					if(StringUtils.isNotBlank(str)) {
						femaleWords.add(str);
					}
				}
				in.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				logger.error("Error while loading female names list.");
			} catch (IOException e) {
				logger.error("Error while loading female names list.");
			}

			// Neutral nouns list
			File neutralNounFile = new File(dictListFolder + "neutral.unigrams.txt");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(neutralNounFile), "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					if(StringUtils.isNotBlank(str)) {
						neutralWords.add(str);
					}
				}
				in.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				logger.error("Error while loading female names list.");
			} catch (IOException e) {
				logger.error("Error while loading female names list.");
			}

			// Animate words list
			File animateWordsFile = new File(dictListFolder + "animate.unigrams.txt");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(animateWordsFile), "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					if(StringUtils.isNotBlank(str)) {
						animateWords.add(str);
					}
				}
				in.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				logger.error("Error while loading animate words list.");
			} catch (IOException e) {
				logger.error("Error while loading animate words list.");
			}

			// Animate words list
			File inanimateWordsFile = new File(dictListFolder + "inanimate.unigrams.txt");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inanimateWordsFile), "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					if(StringUtils.isNotBlank(str)) {
						inanimateWords.add(str);
					}
				}
				in.close();
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				logger.error("Error while loading inanimate words list.");
			} catch (IOException e) {
				logger.error("Error while loading inanimate words list.");
			}

			dictsInitialized = true;
		}


	}

}
