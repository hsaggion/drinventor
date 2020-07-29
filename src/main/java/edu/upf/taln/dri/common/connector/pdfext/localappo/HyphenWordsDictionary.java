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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


public class HyphenWordsDictionary {
	
	private static Logger logger = Logger.getLogger(HyphenWordsDictionary.class);
	
    public List<String> hyphenWordsDictionary;
       
    public  HyphenWordsDictionary()
	{
    	 this.hyphenWordsDictionary=new ArrayList<String>();
    	 loadHyphenWordsDictionary();     	
	}

    
    /** Loads a file with hyphenated compound words in memory 
     * 
     *  Format of the file:
     *  
     *   Word-withhyphens1\n
     *   Word-withhyphens2\n
     *   ..
     *   Word-withhyphensN\n
     * 
     * 
     * */
    public void loadHyphenWordsDictionary() 
    {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(HyphenWordsDictionary.class.getClassLoader().getResourceAsStream("pdfext/en/hyphenated_words_freeling3.1_en.dat"), "UTF-8"));
            
            String line;
            String word;
            StringTokenizer tokenizer;
            
            while((line=in.readLine())!=null) {
            	tokenizer=new StringTokenizer(line,"\n");
                if(tokenizer.countTokens()==1) {
                    word=tokenizer.nextToken();
                    this.hyphenWordsDictionary.add(word);
                } 
                
            }
            in.close();
            
            logger.info("Loaded HyphenWordsDictionary - " +
            		((this.hyphenWordsDictionary != null) ? this.hyphenWordsDictionary.size() : "NULL") + " entries.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    	

    
    /** Gets the frequency of a word */
    public boolean checkExistsHyphenWord(String hypenWordCandidate) 
    {
        return this.hyphenWordsDictionary.contains(hypenWordCandidate);
    }
    
    
    
    
}

