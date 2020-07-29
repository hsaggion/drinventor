/*
 * ******************************************************************************************************
 * Dr. Inventor Text Mining Framework Java Library
 * 
 * This code has been developed by the Natural Language Processing Group of the
 * Universitat Pompeu Fabra in the context of the FP7 European Project Dr. Inventor
 * Call: FP7-ICT-2013.8.1 - Agreement No: 611383
 * 
 * Dr. Inventor Text Mining Framework Java Library is available under an open licence, GPLv3, for non-commercial applications.
 * 
 * Dr. Inventor Text Mining Framework Java Library is available under an open licence, GPLv3, for non-commercial applications.
 * ******************************************************************************************************
 */
package edu.upf.taln.dri.common.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility in-memory constant
 * 
 *
 */
public class Const {

	public static Set<String> stopWords = Collections.unmodifiableSet(new HashSet<String>());

	static {
		// List from: http://99webtools.com/blog/list-of-english-stop-words/
		stopWords.add("a"); stopWords.add("able"); stopWords.add("about"); stopWords.add("across"); stopWords.add("after"); 
		stopWords.add("all"); stopWords.add("almost"); stopWords.add("also"); stopWords.add("am"); stopWords.add("among"); 
		stopWords.add("an"); stopWords.add("and"); stopWords.add("any"); stopWords.add("are"); stopWords.add("as"); 
		stopWords.add("at"); stopWords.add("be"); stopWords.add("because"); stopWords.add("been"); stopWords.add("but"); 
		stopWords.add("by"); stopWords.add("can"); stopWords.add("cannot"); stopWords.add("could"); stopWords.add("dear"); 
		stopWords.add("did"); stopWords.add("do"); stopWords.add("does"); stopWords.add("either"); stopWords.add("else"); 
		stopWords.add("ever"); stopWords.add("every"); stopWords.add("for"); stopWords.add("from"); stopWords.add("get"); 
		stopWords.add("got"); stopWords.add("had"); stopWords.add("has"); stopWords.add("have"); stopWords.add("he"); 
		stopWords.add("her"); stopWords.add("hers"); stopWords.add("him"); stopWords.add("his"); stopWords.add("how"); 
		stopWords.add("however"); stopWords.add("i"); stopWords.add("if"); stopWords.add("in"); stopWords.add("into"); 
		stopWords.add("is"); stopWords.add("it"); stopWords.add("its"); stopWords.add("just"); stopWords.add("least"); 
		stopWords.add("let"); stopWords.add("like"); stopWords.add("likely"); stopWords.add("may"); stopWords.add("me"); 
		stopWords.add("might"); stopWords.add("most"); stopWords.add("must"); stopWords.add("my"); stopWords.add("neither"); 
		stopWords.add("no"); stopWords.add("nor"); stopWords.add("not"); stopWords.add("of"); stopWords.add("off"); 
		stopWords.add("often"); stopWords.add("on"); stopWords.add("only"); stopWords.add("or"); stopWords.add("other"); 
		stopWords.add("our"); stopWords.add("own"); stopWords.add("rather"); stopWords.add("said"); stopWords.add("say"); 
		stopWords.add("says"); stopWords.add("she"); stopWords.add("should"); stopWords.add("since"); stopWords.add("so"); 
		stopWords.add("some"); stopWords.add("than"); stopWords.add("that"); stopWords.add("the"); stopWords.add("their"); 
		stopWords.add("them"); stopWords.add("then"); stopWords.add("there"); stopWords.add("these"); stopWords.add("they"); 
		stopWords.add("this"); stopWords.add("tis"); stopWords.add("to"); stopWords.add("too"); stopWords.add("twas"); 
		stopWords.add("us"); stopWords.add("wants"); stopWords.add("was"); stopWords.add("we"); stopWords.add("were"); 
		stopWords.add("what"); stopWords.add("when"); stopWords.add("where"); stopWords.add("which"); stopWords.add("while"); 
		stopWords.add("who"); stopWords.add("whom"); stopWords.add("why"); stopWords.add("will"); stopWords.add("with"); 
		stopWords.add("would"); stopWords.add("yet"); stopWords.add("you"); stopWords.add("your"); stopWords.add("ain't"); 
		stopWords.add("aren't"); stopWords.add("can't"); stopWords.add("could've"); stopWords.add("couldn't"); stopWords.add("didn't"); 
		stopWords.add("doesn't"); stopWords.add("don't"); stopWords.add("hasn't"); stopWords.add("he'd"); stopWords.add("he'll"); 
		stopWords.add("he's"); stopWords.add("how'd"); stopWords.add("how'll"); stopWords.add("how's"); stopWords.add("i'd"); 
		stopWords.add("i'll"); stopWords.add("i'm"); stopWords.add("i've"); stopWords.add("isn't"); stopWords.add("it's"); 
		stopWords.add("might've"); stopWords.add("mightn't"); stopWords.add("must've"); stopWords.add("mustn't"); stopWords.add("shan't"); 
		stopWords.add("she'd"); stopWords.add("she'll"); stopWords.add("she's"); stopWords.add("should've"); stopWords.add("shouldn't"); 
		stopWords.add("that'll"); stopWords.add("that's"); stopWords.add("there's"); stopWords.add("they'd"); stopWords.add("they'll"); 
		stopWords.add("they're"); stopWords.add("they've"); stopWords.add("wasn't"); stopWords.add("we'd"); stopWords.add("we'll"); 
		stopWords.add("we're"); stopWords.add("weren't"); stopWords.add("what'd"); stopWords.add("what's"); stopWords.add("when'd"); 
		stopWords.add("when'll"); stopWords.add("when's"); stopWords.add("where'd"); stopWords.add("where'll"); stopWords.add("where's"); 
		stopWords.add("who'd"); stopWords.add("who'll"); stopWords.add("who's"); stopWords.add("why'd"); stopWords.add("why'll"); 
		stopWords.add("why's"); stopWords.add("won't"); stopWords.add("would've"); stopWords.add("wouldn't"); stopWords.add("you'd"); 
		stopWords.add("you'll"); stopWords.add("you're"); stopWords.add("you've");
	}
}
