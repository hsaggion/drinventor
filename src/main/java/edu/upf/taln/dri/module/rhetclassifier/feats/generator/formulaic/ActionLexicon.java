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

public class ActionLexicon {
	
	public static Map<String, Set<String>> coreMap = new HashMap<String, Set<String>>();
	
	static {
		Set<String> AFFECT = new HashSet<String>();
		coreMap.put("AFFECT", AFFECT);
		AFFECT.add("afford");
		AFFECT.add("believe");
		AFFECT.add("decide");
		AFFECT.add("feel");
		AFFECT.add("hope");
		AFFECT.add("imagine");
		AFFECT.add("regard");
		AFFECT.add("trust");
		AFFECT.add("think");
		
		Set<String> ARGUMENTATION = new HashSet<String>();
		coreMap.put("ARGUMENTATION", ARGUMENTATION);
		ARGUMENTATION.add("agree");
		ARGUMENTATION.add("accept");
		ARGUMENTATION.add("advocate");
		ARGUMENTATION.add("argue");
		ARGUMENTATION.add("claim");
		ARGUMENTATION.add("conclude");
		ARGUMENTATION.add("comment");
		ARGUMENTATION.add("defend");
		ARGUMENTATION.add("embrace");
		ARGUMENTATION.add("hypothesize");
		ARGUMENTATION.add("imply");
		ARGUMENTATION.add("insist");
		ARGUMENTATION.add("posit");
		ARGUMENTATION.add("postulate");
		ARGUMENTATION.add("reason");
		ARGUMENTATION.add("recommend");
		ARGUMENTATION.add("speculate");
		ARGUMENTATION.add("stipulate");
		ARGUMENTATION.add("suspect");
		
		Set<String> AWARE = new HashSet<String>();
		coreMap.put("AWARE", AWARE);
		AWARE.add("be unaware");
		AWARE.add("be familiar with");
		AWARE.add("be aware");
		AWARE.add("be not aware");
		AWARE.add("know of");
		
		Set<String> BETTER_SOLUTION = new HashSet<String>();
		coreMap.put("BETTER_SOLUTION", BETTER_SOLUTION);
		BETTER_SOLUTION.add("boost");
		BETTER_SOLUTION.add("enhance");
		BETTER_SOLUTION.add("defeat");
		BETTER_SOLUTION.add("improve");
		BETTER_SOLUTION.add("go beyond");
		BETTER_SOLUTION.add("perform better");
		BETTER_SOLUTION.add("outperform");
		BETTER_SOLUTION.add("outweigh");
		BETTER_SOLUTION.add("surpass");
		
		Set<String> CHANGE = new HashSet<String>();
		coreMap.put("CHANGE", CHANGE);
		CHANGE.add("adapt");
		CHANGE.add("adjust");
		CHANGE.add("augment");
		CHANGE.add("combine");
		CHANGE.add("change");
		CHANGE.add("decrease");
		CHANGE.add("elaborate");
		CHANGE.add("expand");
		CHANGE.add("extend");
		CHANGE.add("derive");
		CHANGE.add("incorporate");
		CHANGE.add("increase");
		CHANGE.add("manipulate");
		CHANGE.add("modify");
		CHANGE.add("optimize");
		CHANGE.add("optimise");
		CHANGE.add("refine");
		CHANGE.add("render");
		CHANGE.add("replace");
		CHANGE.add("revise");
		CHANGE.add("substitute");
		CHANGE.add("tailor");
		CHANGE.add("upgrade");
		
		Set<String> COMPARISON = new HashSet<String>();
		coreMap.put("COMPARISON", COMPARISON);
		COMPARISON.add("compare");
		COMPARISON.add("compete");
		COMPARISON.add("evaluate");
		COMPARISON.add("test");
		
		Set<String> CONTINUE = new HashSet<String>();
		coreMap.put("CONTINUE", CONTINUE);
		CONTINUE.add("adopt");
		CONTINUE.add("agree with CITE");
		CONTINUE.add("base");
		CONTINUE.add("be based on");
		CONTINUE.add("be derived from");
		CONTINUE.add("be originated in");
		CONTINUE.add("be inspired by");
		CONTINUE.add("borrow");
		CONTINUE.add("build on");
		CONTINUE.add("follow CITE");
		CONTINUE.add("originate from");
		CONTINUE.add("originate in");
		CONTINUE.add("side with");
		
		Set<String> CONTRAST = new HashSet<String>();
		coreMap.put("CONTRAST", CONTRAST);
		CONTRAST.add("be different from");
		CONTRAST.add("be distinct from");
		CONTRAST.add("conflict");
		CONTRAST.add("contrast");
		CONTRAST.add("clash");
		CONTRAST.add("differ from");
		CONTRAST.add("distinguish @RFX");
		CONTRAST.add("differentiate");
		CONTRAST.add("disagree");
		CONTRAST.add("disagreeing");
		CONTRAST.add("dissent");
		CONTRAST.add("oppose");
		
		Set<String> FUTURE_INTEREST = new HashSet<String>();
		coreMap.put("FUTURE_INTEREST", FUTURE_INTEREST);
		FUTURE_INTEREST.add("plan on");
		FUTURE_INTEREST.add("plan to");
		FUTURE_INTEREST.add("expect to");
		FUTURE_INTEREST.add("intend to");
		
		Set<String> INTEREST = new HashSet<String>();
		coreMap.put("INTEREST", INTEREST);
		INTEREST.add("aim");
		INTEREST.add("ask @SELF_RFX");
		INTEREST.add("ask @OTHERS_RFX");
		INTEREST.add("address");
		INTEREST.add("attempt");
		INTEREST.add("be concerned");
		INTEREST.add("be interested");
		INTEREST.add("be motivated");
		INTEREST.add("concern");
		INTEREST.add("concern @SELF_ACC");
		INTEREST.add("concern @OTHERS_ACC");
		INTEREST.add("consider");
		INTEREST.add("concentrate on");
		INTEREST.add("explore");
		INTEREST.add("focus");
		INTEREST.add("intend to");
		INTEREST.add("like to");
		INTEREST.add("look at how");
		INTEREST.add("motivate @SELF_ACC");
		INTEREST.add("motivate @OTHERS_ACC");
		INTEREST.add("pursue");
		INTEREST.add("seek");
		INTEREST.add("study");
		INTEREST.add("try");
		INTEREST.add("target");
		INTEREST.add("want");
		INTEREST.add("wish");
		INTEREST.add("wonder");
		
		Set<String> NEED = new HashSet<String>();
		coreMap.put("NEED", NEED);
		NEED.add("be dependent on");
		NEED.add("be reliant on");
		NEED.add("depend on");
		NEED.add("lack");
		NEED.add("need");
		NEED.add("necessitate");
		NEED.add("require");
		NEED.add("rely on");
		
		Set<String> PRESENTATION = new HashSet<String>();
		coreMap.put("PRESENTATION", PRESENTATION);
		PRESENTATION.add("describe");
		PRESENTATION.add("discuss");
		PRESENTATION.add("give");
		PRESENTATION.add("introduce");
		PRESENTATION.add("note");
		PRESENTATION.add("notice");
		PRESENTATION.add("point out");
		PRESENTATION.add("present");
		PRESENTATION.add("propose");
		PRESENTATION.add("put forward");
		PRESENTATION.add("recapitulate");
		PRESENTATION.add("remark");
		PRESENTATION.add("report");
		PRESENTATION.add("say");
		PRESENTATION.add("show");
		PRESENTATION.add("sketch");
		PRESENTATION.add("state");
		PRESENTATION.add("suggest");
		PRESENTATION.add("talk about");
		
		Set<String> PROBLEM = new HashSet<String>();
		coreMap.put("PROBLEM", PROBLEM);
		PROBLEM.add("abound");
		PROBLEM.add("aggravate");
		PROBLEM.add("arise");
		PROBLEM.add("be cursed");
		PROBLEM.add("be incapable of");
		PROBLEM.add("be forced to");
		PROBLEM.add("be limited to");
		PROBLEM.add("be problematic");
		PROBLEM.add("be restricted to");
		PROBLEM.add("be troubled");
		PROBLEM.add("be unable to");
		PROBLEM.add("contradict");
		PROBLEM.add("damage");
		PROBLEM.add("degrade");
		PROBLEM.add("degenerate");
		PROBLEM.add("fail");
		PROBLEM.add("fall prey");
		PROBLEM.add("fall short");
		PROBLEM.add("force @SELF_ACC");
		PROBLEM.add("force @OTHERS_ACC");
		PROBLEM.add("hinder");
		PROBLEM.add("impair");
		PROBLEM.add("impede");
		PROBLEM.add("inhibit");
		PROBLEM.add("misclassify");
		PROBLEM.add("misjudge");
		PROBLEM.add("mistake");
		PROBLEM.add("misuse");
		PROBLEM.add("neglect");
		PROBLEM.add("obscure");
		PROBLEM.add("overestimate");
		PROBLEM.add("over-estimate");
		PROBLEM.add("overfit");
		PROBLEM.add("over-fit");
		PROBLEM.add("overgeneralize");
		PROBLEM.add("over-generalize");
		PROBLEM.add("overgeneralise");
		PROBLEM.add("over-generalise");
		PROBLEM.add("overgenerate");
		PROBLEM.add("over-generate");
		PROBLEM.add("overlook");
		PROBLEM.add("pose");
		PROBLEM.add("plague");
		PROBLEM.add("preclude");
		PROBLEM.add("prevent");
		PROBLEM.add("remain");
		PROBLEM.add("resort to");
		PROBLEM.add("restrain");
		PROBLEM.add("run into");
		PROBLEM.add("settle for");
		PROBLEM.add("spoil");
		PROBLEM.add("suffer from");
		PROBLEM.add("threaten");
		PROBLEM.add("thwart");
		PROBLEM.add("underestimate");
		PROBLEM.add("under-estimate");
		PROBLEM.add("undergenerate");
		PROBLEM.add("under-generate");
		PROBLEM.add("violate");
		PROBLEM.add("waste");
		PROBLEM.add("worsen");
		
		Set<String> RESEARCH = new HashSet<String>();
		coreMap.put("RESEARCH", RESEARCH);
		RESEARCH.add("apply");
		RESEARCH.add("analyze");
		RESEARCH.add("analyse");
		RESEARCH.add("build");
		RESEARCH.add("calculate");
		RESEARCH.add("categorize");
		RESEARCH.add("categorise");
		RESEARCH.add("characterize");
		RESEARCH.add("characterise");
		RESEARCH.add("choose");
		RESEARCH.add("check");
		RESEARCH.add("classify");
		RESEARCH.add("collect");
		RESEARCH.add("compose");
		RESEARCH.add("compute");
		RESEARCH.add("conduct");
		RESEARCH.add("confirm");
		RESEARCH.add("construct");
		RESEARCH.add("count");
		RESEARCH.add("define");
		RESEARCH.add("delineate");
		RESEARCH.add("detect");
		RESEARCH.add("determine");
		RESEARCH.add("equate");
		RESEARCH.add("estimate");
		RESEARCH.add("examine");
		RESEARCH.add("expect");
		RESEARCH.add("formalize");
		RESEARCH.add("formalise");
		RESEARCH.add("formulate");
		RESEARCH.add("gather");
		RESEARCH.add("identify");
		RESEARCH.add("implement");
		RESEARCH.add("indicate");
		RESEARCH.add("inspect");
		RESEARCH.add("integrate");
		RESEARCH.add("interpret");
		RESEARCH.add("investigate");
		RESEARCH.add("isolate");
		RESEARCH.add("maximize");
		RESEARCH.add("maximise");
		RESEARCH.add("measure");
		RESEARCH.add("minimize");
		RESEARCH.add("minimise");
		RESEARCH.add("observe");
		RESEARCH.add("predict");
		RESEARCH.add("realize");
		RESEARCH.add("realise");
		RESEARCH.add("reconfirm");
		RESEARCH.add("simulate");
		RESEARCH.add("select");
		RESEARCH.add("specify");
		RESEARCH.add("test");
		RESEARCH.add("verify");
		
		Set<String> SIMILAR = new HashSet<String>();
		coreMap.put("SIMILAR", SIMILAR);
		SIMILAR.add("bear comparison");
		SIMILAR.add("be analogous to");
		SIMILAR.add("be alike");
		SIMILAR.add("be related to");
		SIMILAR.add("be closely related to");
		SIMILAR.add("be reminiscent of");
		SIMILAR.add("be the same as");
		SIMILAR.add("be similar to");
		SIMILAR.add("be in a similar vein to");
		SIMILAR.add("have much in common with");
		SIMILAR.add("have a lot in common with");
		SIMILAR.add("pattern with");
		SIMILAR.add("resemble");
		
		Set<String> SOLUTION = new HashSet<String>();
		coreMap.put("SOLUTION", SOLUTION);
		SOLUTION.add("accomplish");
		SOLUTION.add("account for");
		SOLUTION.add("achieve");
		SOLUTION.add("apply to");
		SOLUTION.add("answer");
		SOLUTION.add("alleviate");
		SOLUTION.add("allow for");
		SOLUTION.add("allow @SELF_ACC");
		SOLUTION.add("allow @OTHERS_ACC");
		SOLUTION.add("avoid");
		SOLUTION.add("benefit");
		SOLUTION.add("capture");
		SOLUTION.add("clarify");
		SOLUTION.add("circumvent");
		SOLUTION.add("contribute");
		SOLUTION.add("cope with");
		SOLUTION.add("cover");
		SOLUTION.add("cure");
		SOLUTION.add("deal with");
		SOLUTION.add("demonstrate");
		SOLUTION.add("develop");
		SOLUTION.add("devise");
		SOLUTION.add("discover");
		SOLUTION.add("elucidate");
		SOLUTION.add("escape");
		SOLUTION.add("explain");
		SOLUTION.add("fix");
		SOLUTION.add("gain");
		SOLUTION.add("go a long way");
		SOLUTION.add("guarantee");
		SOLUTION.add("handle");
		SOLUTION.add("help");
		SOLUTION.add("implement");
		SOLUTION.add("justify");
		SOLUTION.add("lend itself");
		SOLUTION.add("make progress");
		SOLUTION.add("manage");
		SOLUTION.add("mend");
		SOLUTION.add("mitigate");
		SOLUTION.add("model");
		SOLUTION.add("obtain");
		SOLUTION.add("offer");
		SOLUTION.add("overcome");
		SOLUTION.add("perform");
		SOLUTION.add("preserve");
		SOLUTION.add("prove");
		SOLUTION.add("provide");
		SOLUTION.add("realize");
		SOLUTION.add("realise");
		SOLUTION.add("rectify");
		SOLUTION.add("refrain from");
		SOLUTION.add("remedy");
		SOLUTION.add("resolve");
		SOLUTION.add("reveal");
		SOLUTION.add("scale up");
		SOLUTION.add("sidestep");
		SOLUTION.add("solve");
		SOLUTION.add("succeed");
		SOLUTION.add("tackle");
		SOLUTION.add("take care of");
		SOLUTION.add("take into account");
		SOLUTION.add("treat");
		SOLUTION.add("warrant");
		SOLUTION.add("work well");
		SOLUTION.add("yield");
		
		Set<String> TEXTSTRUCTURE = new HashSet<String>();
		coreMap.put("TEXTSTRUCTURE", TEXTSTRUCTURE);
		TEXTSTRUCTURE.add("begin by");
		TEXTSTRUCTURE.add("illustrate");
		TEXTSTRUCTURE.add("conclude by");
		TEXTSTRUCTURE.add("organize");
		TEXTSTRUCTURE.add("organise");
		TEXTSTRUCTURE.add("outline");
		TEXTSTRUCTURE.add("return to");
		TEXTSTRUCTURE.add("review");
		TEXTSTRUCTURE.add("start by");
		TEXTSTRUCTURE.add("structure");
		TEXTSTRUCTURE.add("summarize");
		TEXTSTRUCTURE.add("summarise");
		TEXTSTRUCTURE.add("turn to");
		
		Set<String> USE = new HashSet<String>();
		coreMap.put("USE", USE);
		USE.add("apply");
		USE.add("employ");
		USE.add("use");
		USE.add("make");
		USE.add("use");
		USE.add("utilize");
	}
	
	
	public static void main(String[] args) {
		
		System.out.println("***************************************************************");
		System.out.println("*********************** TEUFEL ACTION LEXICON *****************");
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
