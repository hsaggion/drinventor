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

import org.apache.log4j.Logger;


public class AbstractDictionary {
	
	private static Logger logger = Logger.getLogger(AbstractDictionary.class);
	
    public Map<String,String> dictionary;
       
    public  AbstractDictionary(int numDict)
	{
    	 this.dictionary=new HashMap<String,String>();
    	 loadDictionary(numDict);     	
	}

    
    /** Loads a file with the following format  
     * 
     *  Format of the file:
     *  
     *   
     *   TABLE_CAPTION=table_caption
     *   FIGURE_CAPTION=figure
     *   TITLE=title
     *   TEXT=text
     *   
     *   ..
     *   
     * 
     * 
     * */
    public void loadDictionary(int numDict) 
    {
        try {
            BufferedReader in = null;
            if(numDict == 1) in = new BufferedReader(new InputStreamReader(AbstractDictionary.class.getClassLoader().getResourceAsStream("pdfext/en/jast_tags_dictionary.dat"), "UTF-8"));
            if(numDict == 2) in = new BufferedReader(new InputStreamReader(AbstractDictionary.class.getClassLoader().getResourceAsStream("pdfext/en/html_output_colors_dictionary.dat"), "UTF-8"));
            String line;
            String field, tag;
            StringTokenizer tokenizer;
            
            while((line=in.readLine())!=null) {
            	tokenizer=new StringTokenizer(line,"=\n");
                if(tokenizer.countTokens()==2) {
                    field=tokenizer.nextToken();
                    tag=tokenizer.nextToken();
                    this.dictionary.put(field,tag);
                } 
                
            }
            in.close();
            
            logger.info("Loaded AbstractDictionary from file " +
            		((this.dictionary != null) ? this.dictionary.size() : "NULL") + " entries.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    	

    
    /** Gets the value of an entry */
    public String getFieldValue(String strField) 
    {
        return this.dictionary.get(strField);
    }
    
    
    
    
}

