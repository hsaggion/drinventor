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
package edu.upf.taln.dri.common.connector.pdfext.localappo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegexpMatcher {

	private static Logger logger = Logger.getLogger(HyphenWordsDictionary.class);

	public Map<String, Pattern> dictPatternRegexps;
	private static Matcher matcher;


	public RegexpMatcher()
	{
		loadFileRegexps();
	}

	private void loadFileRegexps() 
	{
		dictPatternRegexps = new HashMap<String,Pattern>();

		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(RegexpMatcher.class.getClassLoader().getResourceAsStream("pdfext/en/regexp_matches_en.dat"), "UTF-8"));
			String line;
			String patternType, regexp;
			StringTokenizer tokenizer;

			while((line=in.readLine())!=null) 
			{
				tokenizer=new StringTokenizer(line,"=\n");
				if(tokenizer.countTokens()==2) 
				{
					patternType=tokenizer.nextToken();
					regexp=tokenizer.nextToken();


					Pattern p=Pattern.compile(regexp);
					dictPatternRegexps.put(patternType, p);
				} 
				else {
					System.out.println("ERROR in file");
				}
			}
			in.close();

			logger.info("Loaded RegexpMatcher - " +
					((this.dictPatternRegexps != null) ? this.dictPatternRegexps.size() : "NULL") + " entries.");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}


		//System.out.println("DICT_REGEXPS:"+dictPatternRegexps+"\n");
	}



	boolean findRegexp(String patternType, String stringToCheck)
	{
		matcher = dictPatternRegexps.get(patternType).matcher(stringToCheck);
		return matcher.find();	
	}


	boolean matchRegexp(String patternType, String stringToCheck)
	{
		matcher = dictPatternRegexps.get(patternType).matcher(stringToCheck);
		return matcher.matches();	
	}


}
