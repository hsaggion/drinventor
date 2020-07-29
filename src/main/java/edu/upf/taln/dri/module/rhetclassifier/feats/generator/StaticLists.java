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

import java.util.HashSet;
import java.util.Set;

public class StaticLists {
	// First person pronouns boolean and counter - from: http://en.wikipedia.org/wiki/English_personal_pronouns
	// List: I,	me,	my,	mine, myself, we, us, our, ours, ourselves
	public static Set<String> listOfFirstPersonPronouns = new HashSet<String>();

	// Third person pronouns boolean and counter - from: http://en.wikipedia.org/wiki/English_personal_pronouns
	// List: 
	// MASCULINE: he, him, his, himself, they, them, their, theirs, themselves, 
	// FEMININE: she, her, hers, herself, 
	// NON SPECIFIC: they, them, their, theirs, themself
	// NEUTER - EXCLUDED: it, its, itself
	public static Set<String> listOfThirdPersonPronouns = new HashSet<String>();
	
	public static Set<String> listOfDeterminers = new HashSet<String>();
	
	// Variation Across Speech and Writing By Douglas Biber
	// Form: http://books.google.es/books?id=CVTPaSSYEroC&pg=PR9&source=gbs_selected_pages&cad=3#v=onepage&q=contrary&f=false
	public static Set<String> listOfContraryExpressions = new HashSet<String>();
	
	public static Set<String> listOfNegationExpressions = new HashSet<String>();
	
	// From: http://delivery.acm.org/10.1145/2340000/2330739/coli_a_00126.pdf?ip=193.145.48.8&id=2330739&acc=OPEN&key=DD1EC5BCF38B3699%2EBD9BF0B02D94E6D5%2E4D4702B0C3E38B35%2E6D218144511F3437&CFID=449338353&CFTOKEN=82241200&__acm__=1415879980_d1cc62df2b034f17e3ce7d620752d12f
	public static Set<String> listOfSpeculationCues = new HashSet<String>();
	
	// List from: http://en.wikipedia.org/wiki/Conjunctive_adverb
	public static Set<String> listOfConjunctiveAdverbs = new HashSet<String>();
	
	
	static {

		listOfFirstPersonPronouns.add("i");
		listOfFirstPersonPronouns.add("me");
		listOfFirstPersonPronouns.add("my");
		listOfFirstPersonPronouns.add("mine");
		listOfFirstPersonPronouns.add("myself");
		listOfFirstPersonPronouns.add("we");
		listOfFirstPersonPronouns.add("us");
		listOfFirstPersonPronouns.add("our");
		listOfFirstPersonPronouns.add("ours");
		listOfFirstPersonPronouns.add("ourselves");
		
		
		listOfThirdPersonPronouns.add("he");
		listOfThirdPersonPronouns.add("him");
		listOfThirdPersonPronouns.add("his");
		listOfThirdPersonPronouns.add("himself");
		listOfThirdPersonPronouns.add("they");
		listOfThirdPersonPronouns.add("them");
		listOfThirdPersonPronouns.add("their");
		listOfThirdPersonPronouns.add("theirs");
		listOfThirdPersonPronouns.add("themselves");
		listOfThirdPersonPronouns.add("she");
		listOfThirdPersonPronouns.add("her");
		listOfThirdPersonPronouns.add("hers");
		listOfThirdPersonPronouns.add("herself");
		listOfThirdPersonPronouns.add("they");
		listOfThirdPersonPronouns.add("them");
		listOfThirdPersonPronouns.add("their");
		listOfThirdPersonPronouns.add("theirs");
		listOfThirdPersonPronouns.add("themself");
		
		
		listOfDeterminers.add("this");
		listOfDeterminers.add("that");
		listOfDeterminers.add("these");
		listOfDeterminers.add("those");
		listOfDeterminers.add("which");
		listOfDeterminers.add("another");
		listOfDeterminers.add("other");
		
		
		listOfContraryExpressions.add("alternatively");
		listOfContraryExpressions.add("altogether");
		listOfContraryExpressions.add("consequently");
		listOfContraryExpressions.add("conversely");
		listOfContraryExpressions.add("eg");
		listOfContraryExpressions.add("e.g.");
		listOfContraryExpressions.add("else");
		listOfContraryExpressions.add("futhermore");
		listOfContraryExpressions.add("hence");
		listOfContraryExpressions.add("however");
		listOfContraryExpressions.add("i.e.");
		listOfContraryExpressions.add("instead");
		listOfContraryExpressions.add("likewise");
		listOfContraryExpressions.add("moreover");
		listOfContraryExpressions.add("namely");
		listOfContraryExpressions.add("neverthless");
		listOfContraryExpressions.add("nonethless");
		listOfContraryExpressions.add("notwithstanding");
		listOfContraryExpressions.add("otherwise");
		listOfContraryExpressions.add("rather");
		listOfContraryExpressions.add("similarly");
		listOfContraryExpressions.add("therefore");
		listOfContraryExpressions.add("thus");
		listOfContraryExpressions.add("viz");
		listOfContraryExpressions.add("in comparison");
		listOfContraryExpressions.add("in contrast");
		listOfContraryExpressions.add("in particular");
		listOfContraryExpressions.add("in addition");
		listOfContraryExpressions.add("in conclusion");
		listOfContraryExpressions.add("in consequence");
		listOfContraryExpressions.add("in summ");
		listOfContraryExpressions.add("in summary");
		listOfContraryExpressions.add("in any event");
		listOfContraryExpressions.add("in any case");
		listOfContraryExpressions.add("in other words");
		listOfContraryExpressions.add("for example");
		listOfContraryExpressions.add("for instance");
		listOfContraryExpressions.add("by contrast");
		listOfContraryExpressions.add("by comparison");
		listOfContraryExpressions.add("as a result");
		listOfContraryExpressions.add("as a consequence");
		listOfContraryExpressions.add("on the contrary");
		listOfContraryExpressions.add("on the other end");
		listOfContraryExpressions.add("rather than");
		
		
		listOfNegationExpressions.add("not");
		listOfNegationExpressions.add("n't");
		listOfNegationExpressions.add("n’t");
		listOfNegationExpressions.add("no");
		listOfNegationExpressions.add("not");
		listOfNegationExpressions.add("nor");
		listOfNegationExpressions.add("non");
		listOfNegationExpressions.add("neither");
		listOfNegationExpressions.add("none");
		listOfNegationExpressions.add("never");
		listOfNegationExpressions.add("aren't");
		listOfNegationExpressions.add("aren’t");
		listOfNegationExpressions.add("can't");
		listOfNegationExpressions.add("can’t");
		listOfNegationExpressions.add("cannot");
		listOfNegationExpressions.add("hadn't");
		listOfNegationExpressions.add("hadn’t");
		listOfNegationExpressions.add("hasn't");
		listOfNegationExpressions.add("hasn’t");
		listOfNegationExpressions.add("haven't");
		listOfNegationExpressions.add("haven’t");
		listOfNegationExpressions.add("isn't");
		listOfNegationExpressions.add("isn’t");
		listOfNegationExpressions.add("didn't");
		listOfNegationExpressions.add("didn’t");
		listOfNegationExpressions.add("don't");
		listOfNegationExpressions.add("don’t");
		listOfNegationExpressions.add("doesn't");
		listOfNegationExpressions.add("doesn’t");
		listOfNegationExpressions.add("wasn't");
		listOfNegationExpressions.add("wasn’t");
		listOfNegationExpressions.add("weren't");
		listOfNegationExpressions.add("weren’t");
		listOfNegationExpressions.add("nothing");
		listOfNegationExpressions.add("nobody");
		listOfNegationExpressions.add("less");
		listOfNegationExpressions.add("least");
		listOfNegationExpressions.add("little");
		listOfNegationExpressions.add("scant");
		listOfNegationExpressions.add("scarcely");
		listOfNegationExpressions.add("rarely");
		listOfNegationExpressions.add("hardly");
		listOfNegationExpressions.add("few");
		listOfNegationExpressions.add("rare");
		listOfNegationExpressions.add("unlikely");
		
		
		listOfSpeculationCues.add("cannot be excluded");
		listOfSpeculationCues.add("can't be excluded");
		listOfSpeculationCues.add("can be excluded");
		listOfSpeculationCues.add("can exclude");
		listOfSpeculationCues.add("cannot exclude");
		listOfSpeculationCues.add("can't exclude");
		listOfSpeculationCues.add("may");
		listOfSpeculationCues.add("may not");
		listOfSpeculationCues.add("indicate that");
		listOfSpeculationCues.add("indicates that");
		listOfSpeculationCues.add("no evidence");
		listOfSpeculationCues.add("no proof");
		listOfSpeculationCues.add("no guarantee");
		listOfSpeculationCues.add("any evidence");
		listOfSpeculationCues.add("any proof");
		listOfSpeculationCues.add("any guarantee");
		listOfSpeculationCues.add("not clear");
		listOfSpeculationCues.add("not evident");
		listOfSpeculationCues.add("not understood");
		listOfSpeculationCues.add("not excluded");
		listOfSpeculationCues.add("not exclude");
		listOfSpeculationCues.add("rise the possibility");
		listOfSpeculationCues.add("rise the question");
		listOfSpeculationCues.add("rise the issue");
		listOfSpeculationCues.add("rise the hypothesis");
		listOfSpeculationCues.add("wether or not");
		
		
		listOfConjunctiveAdverbs.add("certainly");
		listOfConjunctiveAdverbs.add("comparatively");
		listOfConjunctiveAdverbs.add("consequently");
		listOfConjunctiveAdverbs.add("contrarily");
		listOfConjunctiveAdverbs.add("conversely");
		listOfConjunctiveAdverbs.add("elsewhere");
		listOfConjunctiveAdverbs.add("equally");
		listOfConjunctiveAdverbs.add("eventually");
		listOfConjunctiveAdverbs.add("finally");
		listOfConjunctiveAdverbs.add("further");
		listOfConjunctiveAdverbs.add("furthermore");
		listOfConjunctiveAdverbs.add("hence");
		listOfConjunctiveAdverbs.add("henceforth");
		listOfConjunctiveAdverbs.add("however");
		listOfConjunctiveAdverbs.add("in addition");
		listOfConjunctiveAdverbs.add("in comparison");
		listOfConjunctiveAdverbs.add("in contrast");
		listOfConjunctiveAdverbs.add("in fact");
		listOfConjunctiveAdverbs.add("incidentally");
		listOfConjunctiveAdverbs.add("indeed");
		listOfConjunctiveAdverbs.add("instead");
		listOfConjunctiveAdverbs.add("just as");
		listOfConjunctiveAdverbs.add("likewise");
		listOfConjunctiveAdverbs.add("meanwhile");
		listOfConjunctiveAdverbs.add("moreover");
		listOfConjunctiveAdverbs.add("namely");
		listOfConjunctiveAdverbs.add("nevertheless");
		listOfConjunctiveAdverbs.add("next");
		listOfConjunctiveAdverbs.add("nonetheless");
		listOfConjunctiveAdverbs.add("notably");
		listOfConjunctiveAdverbs.add("now");
		listOfConjunctiveAdverbs.add("otherwise");
		listOfConjunctiveAdverbs.add("rather");
		listOfConjunctiveAdverbs.add("similarly");
		listOfConjunctiveAdverbs.add("still");
		listOfConjunctiveAdverbs.add("subsequently");
		listOfConjunctiveAdverbs.add("that is");
		listOfConjunctiveAdverbs.add("then");
		listOfConjunctiveAdverbs.add("thereafter");
		listOfConjunctiveAdverbs.add("therefore");
		listOfConjunctiveAdverbs.add("thus");
		listOfConjunctiveAdverbs.add("undoubtedly");
		listOfConjunctiveAdverbs.add("uniquely");
		listOfConjunctiveAdverbs.add("whereas");
		listOfConjunctiveAdverbs.add("on the other hand");

	}

}
