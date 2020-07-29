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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator.formulaic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConceptLexicon {
	public static Map<String, Set<String>> coreMap = new HashMap<String, Set<String>>();

	static {
		Set<String> NEGATION = new HashSet<String>();
		coreMap.put("NEGATION", NEGATION);
		NEGATION.add("no");
		NEGATION.add("not");
		NEGATION.add("nor");
		NEGATION.add("non");
		NEGATION.add("neither");
		NEGATION.add("none");
		NEGATION.add("never");
		NEGATION.add("aren’t");
		NEGATION.add("can’t");
		NEGATION.add("cannot");
		NEGATION.add("hadn’t");
		NEGATION.add("hasn’t");
		NEGATION.add("haven’t");
		NEGATION.add("isn’t");
		NEGATION.add("didn’t");
		NEGATION.add("don’t");
		NEGATION.add("doesn’t");
		NEGATION.add("n’t");
		NEGATION.add("wasn’t");
		NEGATION.add("weren’t");
		NEGATION.add("nothing");
		NEGATION.add("nobody");
		NEGATION.add("less");
		NEGATION.add("least");
		NEGATION.add("little");
		NEGATION.add("scant");
		NEGATION.add("scarcely");
		NEGATION.add("rarely");
		NEGATION.add("hardly");
		NEGATION.add("few");
		NEGATION.add("rare");
		NEGATION.add("unlikely");

		Set<String> THIRD_PRON = new HashSet<String>();
		coreMap.put("THIRD_PRON", THIRD_PRON);
		THIRD_PRON.add("they");
		THIRD_PRON.add("he");
		THIRD_PRON.add("she");
		THIRD_PRON.add("theirs");
		THIRD_PRON.add("hers");
		THIRD_PRON.add("his");
		THIRD_PRON.add("her");
		THIRD_PRON.add("him");
		THIRD_PRON.add("them");
		THIRD_PRON.add("their");
		THIRD_PRON.add("his");
		THIRD_PRON.add("her");
		THIRD_PRON.add("themselves");
		THIRD_PRON.add("himself");
		THIRD_PRON.add("herself");

		Set<String> FIRST_PRON = new HashSet<String>();
		coreMap.put("FIRST_PRON", FIRST_PRON);
		FIRST_PRON.add("we");
		FIRST_PRON.add("i");
		FIRST_PRON.add("ours");
		FIRST_PRON.add("mine");
		FIRST_PRON.add("us");
		FIRST_PRON.add("me");
		FIRST_PRON.add("my");
		FIRST_PRON.add("our");
		FIRST_PRON.add("ourselves");
		FIRST_PRON.add("myself");

		Set<String> REFERENTIAL = new HashSet<String>();
		coreMap.put("REFERENTIAL", REFERENTIAL);
		REFERENTIAL.add("this");
		REFERENTIAL.add("that");
		REFERENTIAL.add("those");
		REFERENTIAL.add("these");

		Set<String> REFLEXIVE = new HashSet<String>();
		coreMap.put("REFLEXIVE", REFLEXIVE);
		REFLEXIVE.add("itself");
		REFLEXIVE.add("ourselves");
		REFLEXIVE.add("myself");
		REFLEXIVE.add("themselves");
		REFLEXIVE.add("himself");
		REFLEXIVE.add("herself");

		Set<String> QUESTION = new HashSet<String>();
		coreMap.put("QUESTION", QUESTION);
		QUESTION.add("?");
		QUESTION.add("how");
		QUESTION.add("why");
		QUESTION.add("whether");
		QUESTION.add("wonder");

		Set<String> GIVEN = new HashSet<String>();
		coreMap.put("GIVEN", GIVEN);
		GIVEN.add("noted");
		GIVEN.add("mentioned");
		GIVEN.add("addressed");
		GIVEN.add("illustrated");
		GIVEN.add("described");
		GIVEN.add("discussed");
		GIVEN.add("given");
		GIVEN.add("outlined");
		GIVEN.add("presented");
		GIVEN.add("proposed");
		GIVEN.add("reported");
		GIVEN.add("shown");
		GIVEN.add("taken");

		Set<String> PROFESSIONALS = new HashSet<String>();
		coreMap.put("PROFESSIONALS", PROFESSIONALS);
		PROFESSIONALS.add("collegues");
		PROFESSIONALS.add("community");
		PROFESSIONALS.add("computer scientists");
		PROFESSIONALS.add("computational linguists");
		PROFESSIONALS.add("discourse analysts");
		PROFESSIONALS.add("expert");
		PROFESSIONALS.add("investigators");
		PROFESSIONALS.add("linguists");
		PROFESSIONALS.add("logicians");
		PROFESSIONALS.add("philosophers");
		PROFESSIONALS.add("psycholinguists");
		PROFESSIONALS.add("psychologists");
		PROFESSIONALS.add("researchers");
		PROFESSIONALS.add("scholars");
		PROFESSIONALS.add("semanticists");
		PROFESSIONALS.add("scientists");

		Set<String> DISCIPLINE = new HashSet<String>();
		coreMap.put("DISCIPLINE", DISCIPLINE);
		DISCIPLINE.add("computer science");
		DISCIPLINE.add("computer linguistics");
		DISCIPLINE.add("computational linguistics");
		DISCIPLINE.add("discourse analysis");
		DISCIPLINE.add("logics");
		DISCIPLINE.add("linguistics");
		DISCIPLINE.add("psychology");
		DISCIPLINE.add("psycholinguistics");
		DISCIPLINE.add("philosophy");
		DISCIPLINE.add("semantics");
		DISCIPLINE.add("several disciplines");
		DISCIPLINE.add("various disciplines");

		Set<String> TEXT_NOUN = new HashSet<String>();
		coreMap.put("TEXT_NOUN", TEXT_NOUN);
		TEXT_NOUN.add("paragraph");
		TEXT_NOUN.add("section");
		TEXT_NOUN.add("subsection");
		TEXT_NOUN.add("chapter");

		Set<String> SIMILAR_NOUN = new HashSet<String>();
		coreMap.put("SIMILAR_NOUN", SIMILAR_NOUN);
		SIMILAR_NOUN.add("analogy");
		SIMILAR_NOUN.add("similarity");

		Set<String> COMPARISON_NOUN = new HashSet<String>();
		coreMap.put("COMPARISON_NOUN", COMPARISON_NOUN);
		COMPARISON_NOUN.add("accuracy");
		COMPARISON_NOUN.add("baseline");
		COMPARISON_NOUN.add("comparison");
		COMPARISON_NOUN.add("competition");
		COMPARISON_NOUN.add("evaluation");
		COMPARISON_NOUN.add("inferiority");
		COMPARISON_NOUN.add("measure");
		COMPARISON_NOUN.add("measurement");
		COMPARISON_NOUN.add("performance");
		COMPARISON_NOUN.add("precision");
		COMPARISON_NOUN.add("optimum");
		COMPARISON_NOUN.add("recall");
		COMPARISON_NOUN.add("superiority");

		Set<String> CONTRAST_NOUN = new HashSet<String>();
		coreMap.put("CONTRAST_NOUN", CONTRAST_NOUN);
		CONTRAST_NOUN.add("contrast");
		CONTRAST_NOUN.add("conflict");
		CONTRAST_NOUN.add("clash");
		CONTRAST_NOUN.add("clashes");
		CONTRAST_NOUN.add("difference");
		CONTRAST_NOUN.add("point of departure");

		Set<String> AIM_NOUN = new HashSet<String>();
		coreMap.put("AIM_NOUN", AIM_NOUN);
		AIM_NOUN.add("aim");
		AIM_NOUN.add("goal");
		AIM_NOUN.add("intention");
		AIM_NOUN.add("objective");
		AIM_NOUN.add("purpose");
		AIM_NOUN.add("task");
		AIM_NOUN.add("theme");
		AIM_NOUN.add("topic");

		Set<String> ARGUMENTATION_NOUN = new HashSet<String>();
		coreMap.put("ARGUMENTATION_NOUN", ARGUMENTATION_NOUN);
		ARGUMENTATION_NOUN.add("assumption");
		ARGUMENTATION_NOUN.add("belief");
		ARGUMENTATION_NOUN.add("hypothesis");
		ARGUMENTATION_NOUN.add("hypotheses");
		ARGUMENTATION_NOUN.add("claim");
		ARGUMENTATION_NOUN.add("conclusion");
		ARGUMENTATION_NOUN.add("confirmation");
		ARGUMENTATION_NOUN.add("opinion");
		ARGUMENTATION_NOUN.add("recommendation");
		ARGUMENTATION_NOUN.add("stipulation");
		ARGUMENTATION_NOUN.add("view");

		Set<String> PROBLEM_NOUN = new HashSet<String>();
		coreMap.put("PROBLEM_NOUN", PROBLEM_NOUN);
		PROBLEM_NOUN.add("Achilles heel");
		PROBLEM_NOUN.add("caveat");
		PROBLEM_NOUN.add("challenge");
		PROBLEM_NOUN.add("complication");
		PROBLEM_NOUN.add("contradiction");
		PROBLEM_NOUN.add("damage");
		PROBLEM_NOUN.add("danger");
		PROBLEM_NOUN.add("deadlock");
		PROBLEM_NOUN.add("defect");
		PROBLEM_NOUN.add("detriment");
		PROBLEM_NOUN.add("difficulty");
		PROBLEM_NOUN.add("dilemma");
		PROBLEM_NOUN.add("disadvantage");
		PROBLEM_NOUN.add("disregard");
		PROBLEM_NOUN.add("doubt");
		PROBLEM_NOUN.add("downside");
		PROBLEM_NOUN.add("drawback");
		PROBLEM_NOUN.add("error");
		PROBLEM_NOUN.add("failure");
		PROBLEM_NOUN.add("fault");
		PROBLEM_NOUN.add("foil");
		PROBLEM_NOUN.add("flaw");
		PROBLEM_NOUN.add("handicap");
		PROBLEM_NOUN.add("hindrance");
		PROBLEM_NOUN.add("hurdle");
		PROBLEM_NOUN.add("ill");
		PROBLEM_NOUN.add("inflexibility");
		PROBLEM_NOUN.add("impediment");
		PROBLEM_NOUN.add("imperfection");
		PROBLEM_NOUN.add("intractability");
		PROBLEM_NOUN.add("inefficiency");
		PROBLEM_NOUN.add("inadequacy");
		PROBLEM_NOUN.add("inability");
		PROBLEM_NOUN.add("lapse");
		PROBLEM_NOUN.add("limitation");
		PROBLEM_NOUN.add("malheur");
		PROBLEM_NOUN.add("mishap");
		PROBLEM_NOUN.add("mischance");
		PROBLEM_NOUN.add("mistake");
		PROBLEM_NOUN.add("obstacle");
		PROBLEM_NOUN.add("oversight");
		PROBLEM_NOUN.add("pitfall");
		PROBLEM_NOUN.add("problem");
		PROBLEM_NOUN.add("shortcoming");
		PROBLEM_NOUN.add("threat");
		PROBLEM_NOUN.add("trouble");
		PROBLEM_NOUN.add("vulnerability");
		PROBLEM_NOUN.add("absence");
		PROBLEM_NOUN.add("dearth");
		PROBLEM_NOUN.add("deprivation");
		PROBLEM_NOUN.add("lack");
		PROBLEM_NOUN.add("loss");
		PROBLEM_NOUN.add("fraught");
		PROBLEM_NOUN.add("proliferation");
		PROBLEM_NOUN.add("spate");

		Set<String> QUESTION_NOUN = new HashSet<String>();
		coreMap.put("QUESTION_NOUN", QUESTION_NOUN);
		QUESTION_NOUN.add("question");
		QUESTION_NOUN.add("conundrum");
		QUESTION_NOUN.add("enigma");
		QUESTION_NOUN.add("paradox");
		QUESTION_NOUN.add("phenomena");
		QUESTION_NOUN.add("phenomenon");
		QUESTION_NOUN.add("puzzle");
		QUESTION_NOUN.add("riddle");

		Set<String> SOLUTION_NOUN = new HashSet<String>();
		coreMap.put("SOLUTION_NOUN", SOLUTION_NOUN);
		SOLUTION_NOUN.add("answer");
		SOLUTION_NOUN.add("accomplishment");
		SOLUTION_NOUN.add("achievement");
		SOLUTION_NOUN.add("advantage");
		SOLUTION_NOUN.add("benefit");
		SOLUTION_NOUN.add("break-through");
		SOLUTION_NOUN.add("contribution");
		SOLUTION_NOUN.add("explanation");
		SOLUTION_NOUN.add("idea");
		SOLUTION_NOUN.add("improvement");
		SOLUTION_NOUN.add("innovation");
		SOLUTION_NOUN.add("insight");
		SOLUTION_NOUN.add("justification");
		SOLUTION_NOUN.add("proposal");
		SOLUTION_NOUN.add("proof");
		SOLUTION_NOUN.add("remedy");
		SOLUTION_NOUN.add("solution");
		SOLUTION_NOUN.add("success");
		SOLUTION_NOUN.add("triumph");
		SOLUTION_NOUN.add("verification");
		SOLUTION_NOUN.add("victory");

		Set<String> INTEREST_NOUN = new HashSet<String>();
		coreMap.put("INTEREST_NOUN", INTEREST_NOUN);
		INTEREST_NOUN.add("attention");
		INTEREST_NOUN.add("quest");

		Set<String> RESEARCH_NOUN = new HashSet<String>();
		coreMap.put("RESEARCH_NOUN", RESEARCH_NOUN);
		RESEARCH_NOUN.add("evidence");
		RESEARCH_NOUN.add("experiment");
		RESEARCH_NOUN.add("finding");
		RESEARCH_NOUN.add("progress");
		RESEARCH_NOUN.add("observation");
		RESEARCH_NOUN.add("outcome");
		RESEARCH_NOUN.add("result");

		Set<String> CHANGE_NOUN = new HashSet<String>();
		coreMap.put("CHANGE_NOUN", CHANGE_NOUN);
		CHANGE_NOUN.add("alternative");
		CHANGE_NOUN.add("adaptation");
		CHANGE_NOUN.add("extension");
		CHANGE_NOUN.add("development");
		CHANGE_NOUN.add("modification");
		CHANGE_NOUN.add("refinement");
		CHANGE_NOUN.add("version");
		CHANGE_NOUN.add("variant");
		CHANGE_NOUN.add("variation");

		Set<String> PRESENTATION_NOUN = new HashSet<String>();
		coreMap.put("PRESENTATION_NOUN", PRESENTATION_NOUN);
		PRESENTATION_NOUN.add("article");
		PRESENTATION_NOUN.add("draft");
		PRESENTATION_NOUN.add("paper");
		PRESENTATION_NOUN.add("project");
		PRESENTATION_NOUN.add("report");
		PRESENTATION_NOUN.add("study");

		Set<String> NEED_NOUN = new HashSet<String>();
		coreMap.put("NEED_NOUN", NEED_NOUN);
		NEED_NOUN.add("necessity");
		NEED_NOUN.add("motivation");		

		Set<String> WORK_NOUN = new HashSet<String>();
		coreMap.put("WORK_NOUN", WORK_NOUN);
		WORK_NOUN.add("account");
		WORK_NOUN.add("algorithm");
		WORK_NOUN.add("analysis");
		WORK_NOUN.add("analyses");
		WORK_NOUN.add("approach");
		WORK_NOUN.add("approaches");
		WORK_NOUN.add("application");
		WORK_NOUN.add("architecture");
		WORK_NOUN.add("characterization");
		WORK_NOUN.add("characterisation");
		WORK_NOUN.add("component");
		WORK_NOUN.add("design");
		WORK_NOUN.add("extension");
		WORK_NOUN.add("formalism");
		WORK_NOUN.add("formalization");
		WORK_NOUN.add("formalisation");
		WORK_NOUN.add("framework");
		WORK_NOUN.add("implementation");
		WORK_NOUN.add("investigation");
		WORK_NOUN.add("machinery");
		WORK_NOUN.add("method");
		WORK_NOUN.add("methodology");
		WORK_NOUN.add("model");
		WORK_NOUN.add("module");
		WORK_NOUN.add("moduls");
		WORK_NOUN.add("process");
		WORK_NOUN.add("procedure");
		WORK_NOUN.add("program");
		WORK_NOUN.add("prototype");
		WORK_NOUN.add("research");
		WORK_NOUN.add("researches");
		WORK_NOUN.add("strategy");
		WORK_NOUN.add("system");
		WORK_NOUN.add("technique");
		WORK_NOUN.add("theory");
		WORK_NOUN.add("tool");
		WORK_NOUN.add("treatment");
		WORK_NOUN.add("work");
		
		Set<String> TRADITION_NOUN = new HashSet<String>();
		coreMap.put("TRADITION_NOUN", TRADITION_NOUN);
		TRADITION_NOUN.add("acceptance");
		TRADITION_NOUN.add("community");
		TRADITION_NOUN.add("convention");
		TRADITION_NOUN.add("disciples");
		TRADITION_NOUN.add("disciplines");
		TRADITION_NOUN.add("folklore");
		TRADITION_NOUN.add("literature");
		TRADITION_NOUN.add("mainstream");
		TRADITION_NOUN.add("school");
		TRADITION_NOUN.add("tradition");
		TRADITION_NOUN.add("textbook");

		Set<String> CHANGE_ADJ = new HashSet<String>();
		coreMap.put("CHANGE_ADJ", CHANGE_ADJ);
		CHANGE_ADJ.add("alternate");
		CHANGE_ADJ.add("alternative");
		
		Set<String> GOOD_ADJ = new HashSet<String>();
		coreMap.put("GOOD_ADJ", GOOD_ADJ);
		GOOD_ADJ.add("adequate");
		GOOD_ADJ.add("advantageous");
		GOOD_ADJ.add("appealing");
		GOOD_ADJ.add("appropriate");
		GOOD_ADJ.add("attractive");
		GOOD_ADJ.add("automatic");
		GOOD_ADJ.add("beneficial");
		GOOD_ADJ.add("capable");
		GOOD_ADJ.add("cheerful");
		GOOD_ADJ.add("clean");
		GOOD_ADJ.add("clear");
		GOOD_ADJ.add("compact");
		GOOD_ADJ.add("compelling");
		GOOD_ADJ.add("competitive");
		GOOD_ADJ.add("comprehensive");
		GOOD_ADJ.add("consistent");
		GOOD_ADJ.add("convenient");
		GOOD_ADJ.add("convincing");
		GOOD_ADJ.add("constructive");
		GOOD_ADJ.add("correct");
		GOOD_ADJ.add("desirable");
		GOOD_ADJ.add("distinctive");
		GOOD_ADJ.add("efficient");
		GOOD_ADJ.add("elegant");
		GOOD_ADJ.add("encouraging");
		GOOD_ADJ.add("exact");
		GOOD_ADJ.add("faultless");
		GOOD_ADJ.add("favourable");
		GOOD_ADJ.add("feasible");
		GOOD_ADJ.add("flawless");
		GOOD_ADJ.add("good");
		GOOD_ADJ.add("helpful");
		GOOD_ADJ.add("impeccable");
		GOOD_ADJ.add("innovative");
		GOOD_ADJ.add("insightful");
		GOOD_ADJ.add("intensive");
		GOOD_ADJ.add("meaningful");
		GOOD_ADJ.add("neat");
		GOOD_ADJ.add("perfect");
		GOOD_ADJ.add("plausible");
		GOOD_ADJ.add("positive");
		GOOD_ADJ.add("polynomial");
		GOOD_ADJ.add("powerful");
		GOOD_ADJ.add("practical");
		GOOD_ADJ.add("preferable");
		GOOD_ADJ.add("precise");
		GOOD_ADJ.add("principled");
		GOOD_ADJ.add("promising");
		GOOD_ADJ.add("pure");
		GOOD_ADJ.add("realistic");
		GOOD_ADJ.add("reasonable");
		GOOD_ADJ.add("reliable");
		GOOD_ADJ.add("right");
		GOOD_ADJ.add("robust");
		GOOD_ADJ.add("satisfactory");
		GOOD_ADJ.add("simple");
		GOOD_ADJ.add("sound");
		GOOD_ADJ.add("successful");
		GOOD_ADJ.add("sufficient");
		GOOD_ADJ.add("systematic");
		GOOD_ADJ.add("tractable");
		GOOD_ADJ.add("usable");
		GOOD_ADJ.add("useful");
		GOOD_ADJ.add("valid");
		GOOD_ADJ.add("unlimited");
		GOOD_ADJ.add("well worked out");
		GOOD_ADJ.add("well");
		GOOD_ADJ.add("enough");
		
		Set<String> BAD_ADJ = new HashSet<String>();
		coreMap.put("BAD_ADJ", BAD_ADJ);
		BAD_ADJ.add("absent");
		BAD_ADJ.add("ad-hoc");
		BAD_ADJ.add("adhoc");
		BAD_ADJ.add("ad hoc");
		BAD_ADJ.add("annoying");
		BAD_ADJ.add("ambiguous");
		BAD_ADJ.add("arbitrary");
		BAD_ADJ.add("awkward");
		BAD_ADJ.add("bad");
		BAD_ADJ.add("brittle");
		BAD_ADJ.add("brute-force");
		BAD_ADJ.add("brute force");
		BAD_ADJ.add("careless");
		BAD_ADJ.add("confounding");
		BAD_ADJ.add("contradictory");
		BAD_ADJ.add("defect");
		BAD_ADJ.add("defunct");
		BAD_ADJ.add("disturbing");
		BAD_ADJ.add("elusive");
		BAD_ADJ.add("erraneous");
		BAD_ADJ.add("expensive");
		BAD_ADJ.add("exponential");
		BAD_ADJ.add("false");
		BAD_ADJ.add("fallacious");
		BAD_ADJ.add("frustrating");
		BAD_ADJ.add("haphazard");
		BAD_ADJ.add("ill-defined");
		BAD_ADJ.add("imperfect");
		BAD_ADJ.add("impossible");
		BAD_ADJ.add("impractical");
		BAD_ADJ.add("imprecise");
		BAD_ADJ.add("inaccurate");
		BAD_ADJ.add("inadequate");
		BAD_ADJ.add("inappropriate");
		BAD_ADJ.add("incomplete");
		BAD_ADJ.add("incomprehensible");
		BAD_ADJ.add("inconclusive");
		BAD_ADJ.add("incorrect");
		BAD_ADJ.add("inelegant");
		BAD_ADJ.add("inefficient");
		BAD_ADJ.add("inexact");
		BAD_ADJ.add("infeasible");
		BAD_ADJ.add("infelicitous");
		BAD_ADJ.add("inflexible");
		BAD_ADJ.add("implausible");
		BAD_ADJ.add("inpracticable");
		BAD_ADJ.add("improper");
		BAD_ADJ.add("insufficient");
		BAD_ADJ.add("intractable");
		BAD_ADJ.add("invalid");
		BAD_ADJ.add("irrelevant");
		BAD_ADJ.add("labour-intensive");
		BAD_ADJ.add("labor-intensive");
		BAD_ADJ.add("labour intensive");
		BAD_ADJ.add("labor intensive");
		BAD_ADJ.add("limited-coverage");
		BAD_ADJ.add("limited coverage");
		BAD_ADJ.add("limited");
		BAD_ADJ.add("limiting");
		BAD_ADJ.add("meaningless");
		BAD_ADJ.add("modest");
		BAD_ADJ.add("misguided");
		BAD_ADJ.add("misleading");
		BAD_ADJ.add("non-existent");
		BAD_ADJ.add("NP-hard");
		BAD_ADJ.add("NP-complete");
		BAD_ADJ.add("NP hard");
		BAD_ADJ.add("NP complete");
		BAD_ADJ.add("questionable");
		BAD_ADJ.add("pathological");
		BAD_ADJ.add("poor");
		BAD_ADJ.add("prone");
		BAD_ADJ.add("protracted");
		BAD_ADJ.add("restricted");
		BAD_ADJ.add("scarce");
		BAD_ADJ.add("simplistic");
		BAD_ADJ.add("suspect");
		BAD_ADJ.add("time-consuming");
		BAD_ADJ.add("time consuming");
		BAD_ADJ.add("toy");
		BAD_ADJ.add("unacceptable");
		BAD_ADJ.add("unaccounted for");
		BAD_ADJ.add("unaccounted-for");
		BAD_ADJ.add("unaccounted");
		BAD_ADJ.add("unattractive");
		BAD_ADJ.add("unavailable");
		BAD_ADJ.add("unavoidable");
		BAD_ADJ.add("unclear");
		BAD_ADJ.add("uncomfortable");
		BAD_ADJ.add("unexplained");
		BAD_ADJ.add("undecidable");
		BAD_ADJ.add("undesirable");
		BAD_ADJ.add("unfortunate");
		BAD_ADJ.add("uninnovative");
		BAD_ADJ.add("uninterpretable");
		BAD_ADJ.add("unjustified");
		BAD_ADJ.add("unmotivated");
		BAD_ADJ.add("unnatural");
		BAD_ADJ.add("unnecessary");
		BAD_ADJ.add("unorthodox");
		BAD_ADJ.add("unpleasant");
		BAD_ADJ.add("unpractical");
		BAD_ADJ.add("unprincipled");
		BAD_ADJ.add("unreliable");
		BAD_ADJ.add("unsatisfactory");
		BAD_ADJ.add("unsound");
		BAD_ADJ.add("unsuccessful");
		BAD_ADJ.add("unsuited");
		BAD_ADJ.add("unsystematic");
		BAD_ADJ.add("untractable");
		BAD_ADJ.add("unwanted");
		BAD_ADJ.add("unwelcome");
		BAD_ADJ.add("useless");
		BAD_ADJ.add("vulnerable");
		BAD_ADJ.add("weak");
		BAD_ADJ.add("wrong");
		BAD_ADJ.add("too");
		BAD_ADJ.add("overly");
		BAD_ADJ.add("only");
		
		Set<String> EARLIER_ADJ = new HashSet<String>();
		coreMap.put("EARLIER_ADJ", EARLIER_ADJ);
		EARLIER_ADJ.add("earlier");
		EARLIER_ADJ.add("past");
		EARLIER_ADJ.add("previous");
		EARLIER_ADJ.add("prior");
		
		Set<String> CONTRAST_ADJ = new HashSet<String>();
		coreMap.put("CONTRAST_ADJ", CONTRAST_ADJ);
		CONTRAST_ADJ.add("different");
		CONTRAST_ADJ.add("distinguishing");
		CONTRAST_ADJ.add("contrary");
		CONTRAST_ADJ.add("competing");
		CONTRAST_ADJ.add("rival");
		
		Set<String> TRADITION_ADJ = new HashSet<String>();
		coreMap.put("TRADITION_ADJ", TRADITION_ADJ);
		TRADITION_ADJ.add("better known");
		TRADITION_ADJ.add("better-known");
		TRADITION_ADJ.add("cited");
		TRADITION_ADJ.add("classic");
		TRADITION_ADJ.add("common");
		TRADITION_ADJ.add("conventional");
		TRADITION_ADJ.add("current");
		TRADITION_ADJ.add("customary");
		TRADITION_ADJ.add("established");
		TRADITION_ADJ.add("existing");
		TRADITION_ADJ.add("extant");
		TRADITION_ADJ.add("available");
		TRADITION_ADJ.add("favourite");
		TRADITION_ADJ.add("fashionable");
		TRADITION_ADJ.add("general");
		TRADITION_ADJ.add("obvious");
		TRADITION_ADJ.add("long-standing");
		TRADITION_ADJ.add("mainstream");
		TRADITION_ADJ.add("modern");
		TRADITION_ADJ.add("naive");
		TRADITION_ADJ.add("orthodox");
		TRADITION_ADJ.add("popular");
		TRADITION_ADJ.add("prevailing");
		TRADITION_ADJ.add("prevalent");
		TRADITION_ADJ.add("published");
		TRADITION_ADJ.add("quoted");
		TRADITION_ADJ.add("seminal");
		TRADITION_ADJ.add("standard");
		TRADITION_ADJ.add("textbook");
		TRADITION_ADJ.add("traditional");
		TRADITION_ADJ.add("trivial");
		TRADITION_ADJ.add("typical");
		TRADITION_ADJ.add("well-established");
		TRADITION_ADJ.add("well-known");
		TRADITION_ADJ.add("widely-assumed");
		TRADITION_ADJ.add("unanimous");
		TRADITION_ADJ.add("usual");
		
		Set<String> MANY = new HashSet<String>();
		coreMap.put("MANY", MANY);
		MANY.add("a number of");
		MANY.add("a body of");
		MANY.add("a substantial number of");
		MANY.add("a substantial body of");
		MANY.add("most");
		MANY.add("many");
		MANY.add("several");
		MANY.add("various");
		
		Set<String> COMPARISON_ADJ = new HashSet<String>();
		coreMap.put("COMPARISON_ADJ", COMPARISON_ADJ);
		COMPARISON_ADJ.add("evaluative");
		COMPARISON_ADJ.add("superior");
		COMPARISON_ADJ.add("inferior");
		COMPARISON_ADJ.add("optimal");
		COMPARISON_ADJ.add("better");
		COMPARISON_ADJ.add("best");
		COMPARISON_ADJ.add("worse");
		COMPARISON_ADJ.add("worst");
		COMPARISON_ADJ.add("greater");
		COMPARISON_ADJ.add("larger");
		COMPARISON_ADJ.add("faster");
		COMPARISON_ADJ.add("weaker");
		COMPARISON_ADJ.add("stronger");

		Set<String> PROBLEM_ADJ = new HashSet<String>();
		coreMap.put("PROBLEM_ADJ", PROBLEM_ADJ);
		PROBLEM_ADJ.add("demanding");
		PROBLEM_ADJ.add("difficult");
		PROBLEM_ADJ.add("hard");
		PROBLEM_ADJ.add("non-trivial");
		PROBLEM_ADJ.add("nontrivial");
		
		Set<String> RESEARCH_ADJ = new HashSet<String>();
		coreMap.put("RESEARCH_ADJ", RESEARCH_ADJ);
		RESEARCH_ADJ.add("empirical");
		RESEARCH_ADJ.add("experimental");
		RESEARCH_ADJ.add("exploratory");
		RESEARCH_ADJ.add("ongoing");
		RESEARCH_ADJ.add("quantitative");
		RESEARCH_ADJ.add("qualitative");
		RESEARCH_ADJ.add("preliminary");
		RESEARCH_ADJ.add("statistical");
		RESEARCH_ADJ.add("underway");
		
		Set<String> AWARE_ADJ = new HashSet<String>();
		coreMap.put("AWARE_ADJ", AWARE_ADJ);
		AWARE_ADJ.add("unnoticed");
		AWARE_ADJ.add("understood");
		AWARE_ADJ.add("unexplored");
		
		Set<String> NEED_ADJ = new HashSet<String>();
		coreMap.put("NEED_ADJ", NEED_ADJ);
		NEED_ADJ.add("necessary");
		
		Set<String> NED_ADJ = new HashSet<String>();
		coreMap.put("NED_ADJ", NED_ADJ);
		NED_ADJ.add("new");
		NED_ADJ.add("novel");
		NED_ADJ.add("state-of-the-art");
		NED_ADJ.add("state of the art");
		NED_ADJ.add("leading-edge");
		NED_ADJ.add("leading edge");
		NED_ADJ.add("enhanced");
		
		Set<String> FUTURE_ADJ = new HashSet<String>();
		coreMap.put("FUTURE_ADJ", FUTURE_ADJ);
		FUTURE_ADJ.add("further");
		FUTURE_ADJ.add("future");
		
		Set<String> MAIN_ADJ = new HashSet<String>();
		coreMap.put("MAIN_ADJ", MAIN_ADJ);
		MAIN_ADJ.add("main");
		MAIN_ADJ.add("key");
		MAIN_ADJ.add("basic");
		MAIN_ADJ.add("central");
		MAIN_ADJ.add("crucial");
		MAIN_ADJ.add("essential");
		MAIN_ADJ.add("eventual");
		MAIN_ADJ.add("fundamental");
		MAIN_ADJ.add("great");
		MAIN_ADJ.add("important");
		MAIN_ADJ.add("key");
		MAIN_ADJ.add("largest");
		MAIN_ADJ.add("main");
		MAIN_ADJ.add("major");
		MAIN_ADJ.add("overall");
		MAIN_ADJ.add("primary");
		MAIN_ADJ.add("principle");
		MAIN_ADJ.add("serious");
		MAIN_ADJ.add("substantial");
		MAIN_ADJ.add("ultimate");

	}
	
	
	public static void main(String[] args) {
		
		System.out.println("***************************************************************");
		System.out.println("*********************** TEUFEL CONCEPT LEXICON ****************");
		System.out.println("***************************************************************");
		
		for(Entry<String, Set<String>> coreMapEntry : coreMap.entrySet()) {
			
			// System.out.println("\n*********\n");
			// System.out.println("   > " + coreMapEntry.getKey());
			System.out.println(coreMapEntry.getKey());
			/*
			for(String elem : coreMapEntry.getValue()) {
				System.out.println(elem);
			}
			*/
			
			
		}
		
	}
}






