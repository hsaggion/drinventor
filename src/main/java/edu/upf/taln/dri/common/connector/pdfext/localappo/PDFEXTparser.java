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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/***************************
import org.grobid.core.*;
import org.grobid.core.data.*;
import org.grobid.core.factory.*;
import org.grobid.core.mock.*;
import org.grobid.core.utilities.*;
import org.grobid.core.engines.Engine;
***************************/

public class PDFEXTparser {

	
	
	private static boolean verbosity = false;
	
	private static Logger logger = Logger.getLogger(PDFEXTparser.class);
	private static Random rnd = new Random();
	
	
	private static String strPattern_TITLE= ""; //  h2 ";
	private static String strPattern_AUTHORS_AND_EMAIL=""; // h3 ";
	private static String strPattern_ADDRESS=""; //" h4 ";
	private static String strPattern_SECTION=" h5 "; // h5 ";
	private static String strPattern_TEXT=" h6 "; // h6 ";
	private static String strPattern_COPYRIGHT=""; // h7 ";
	private static String strPattern_FOOTNOTE_INDICATOR=""; // h8 ";
	private static String strPattern_PAGE_NUMBER=""; // ha ";
	private static String strPattern_TABLE_HEADER=""; // hc ";
	private static String strPattern_TABLE_ROW=""; // hd ";
	private static String strPattern_VERBATIM_ROW=""; // hf ";
	
	
	//Kind of different elements to parse in the scientific articles
	public enum State {	TITLE, 
						AUTHORS, 
						EMAIL, 
						ADDRESS, 
						ABSTRACT_TITLE, 
						ABSTRACT_TEXT, 
						CATEGORIES_TITLE, 
						CATEGORIES_TEXT, 
						GENERAL_TERMS_TITLE, 
						GENERAL_TERMS_TEXT, 
						KEYWORDS_TITLE, 
						KEYWORDS_TEXT,
						ACKNOWLEDGEMENTS_TITLE,
						ACKNOWLEDGEMENTS_TEXT,
						SECTION_TITLE, 
						SECTION_TEXT,
						REFERENCES_TITLE,
						SUBSECTION_TITLE, 
						SUBSECTION_TEXT,
						SUBSUBSECTION_TITLE, 
						SUBSUBSECTION_TEXT,
						ANNEX_TITLE,
						ANNEX_TEXT,
						AUTHORS_BIOGRAPHIES_TITLE,
						AUTHORS_BIOGRAPHIES_TEXT,
						SUPPORTING_INFORMATION_TITLE,
						SUPPORTING_INFORMATION_TEXT,
						REFERENCES_TEXT,
						FIGURE_CAPTION, 
						TABLE_CAPTION, 
						EQUATION, 
						FIGURE_IMAGE_CAPTION, 
						COPYRIGHT,
						FOOTNOTE_INDICATOR,
						PAGE_NUMBER, 
						TABLE_HEADER, 
						TABLE_ROW, 
						VERBATIM_ROW, 
						REFERENCE};
	
						
	private enum StateSection 
						{
							SECTION,
							SUBSECTION,
							SUBSUBSECTION,
						};

	private enum StateAnnexes
						{
							ANNEX,
							SUBANNEX,
							SUBSUBANNEX,
						};

											
						
	
	private static int iteratorGlobalElements =0;
	
	
	protected static  HyphenWordsDictionary hyphenWordsDictionaryEN;
	
	protected static AbstractDictionary jastTagsDictionary;
	protected static AbstractDictionary htmlArticlePartTypeColorsDictionary;
	
	
	
	
	protected static  RegexpMatcher regexpMatcher;
	
	protected static Map<String,Integer> mapCountsCSSHeights;
	protected static Map<String,Integer> mapCountsCSSFontSize;
	protected static Map<String,Integer> mapCountsCSSFontFamily;
	protected static Map<String,Integer> mapCountsCSSBottom;
	protected static Map<String,Integer> mapCountsCSSLeft;
	
	protected static Map<String,Double> mapValuesCSSHeights;
	protected static Map<String,Double> mapValuesCSSFontSize;
	protected static Map<String,Double> mapValuesCSSBottom;
	protected static Map<String,Double> mapValuesCSSLeft;
	
	
	protected static int yAxisAverageTextDiff;

	protected static double firstColumnXAxisValue;
	protected static double secondColumnXAxisValue;


	protected static int numColumns;

	
	protected static double bottomValueThresholdForTITLE;
	
	protected static double minThresholdReferenceDetectByFirstLineIndent;
	protected static double maxThresholdReferenceDetectByFirstLineIndent;
	protected static double maxThresholdReferenceWithinLineBottomDistance;
	
	
	protected static double  maxValuePagePxPercentThresholdFilterRunningHeads;
	protected static double  minValuePagePxPercentThresholdFilterRunningHeads;
	
	protected static double secondLineReferenceXAxisLeftValue;
	
	protected static double maxThresholdXAxisToIndentReference;
	
	
	protected static double maxThresholdYAxisFootnoteIndex;
	
	protected static double OFFSET_ColumnXAxisValue;
	
	protected static double OFFSET_yAxisAverageTextDiff;
	
	
	protected static double maxOFFSETxAxisIndexFootnote;
	
	
	protected static double biggestYAxis_OFFSET;
	protected static double smallestYAxis_OFFSET;
	
	
	/***************************
	//GROBID engige object
	protected static Engine engine;
	***************************/
	
	
	public PDFEXTparser()
	{
		
		
		hyphenWordsDictionaryEN= new HyphenWordsDictionary();
		jastTagsDictionary= new AbstractDictionary(1);
		htmlArticlePartTypeColorsDictionary= new AbstractDictionary(2);
		regexpMatcher=new RegexpMatcher();

		
		mapCountsCSSHeights = new HashMap<String,Integer>();
		mapCountsCSSFontSize = new HashMap<String,Integer>();
		mapCountsCSSFontFamily = new HashMap<String,Integer>();
		mapCountsCSSBottom = new HashMap<String,Integer>();
		mapCountsCSSLeft = new HashMap<String,Integer>();
		
		mapValuesCSSHeights = new HashMap<String,Double>();
		mapValuesCSSFontSize = new HashMap<String,Double>();
		mapValuesCSSBottom = new HashMap<String,Double>();
		mapValuesCSSLeft = new HashMap<String,Double>();

		
		
		firstColumnXAxisValue=0;
		secondColumnXAxisValue=0;
		yAxisAverageTextDiff=0;
		numColumns=0;
		
		bottomValueThresholdForTITLE=500;
		minThresholdReferenceDetectByFirstLineIndent=2;
		maxThresholdReferenceDetectByFirstLineIndent=20;
		maxThresholdReferenceWithinLineBottomDistance=10;
		maxThresholdXAxisToIndentReference=20;
		
		
		
		maxValuePagePxPercentThresholdFilterRunningHeads=0.92;
		minValuePagePxPercentThresholdFilterRunningHeads=0.06;
		
		
		OFFSET_ColumnXAxisValue=3;
		
		OFFSET_yAxisAverageTextDiff=2;
		
		
		maxThresholdYAxisFootnoteIndex=8;
		maxOFFSETxAxisIndexFootnote=20;
		
		
		biggestYAxis_OFFSET=10;
		smallestYAxis_OFFSET=10;
		
		
		
		// initialize the GROBID java api 
		// currently this gives an execution error
		/***************************
	    try {

	        MockContext.setInitialContext(Util.pGrobidHome, Util.pGrobidProperties);      
	        GrobidProperties.getInstance();
	        System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());
	        Engine engine = GrobidFactory.getInstance().createEngine();
	    } 
	    catch (Exception e) {
	        // If an exception is generated, print a stack trace
	        e.printStackTrace();
	    } 
	    finally {
	        try {
	            MockContext.destroyInitialContext();
	        } 
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		
		***************************/
	}
	
	
	// currently GROBID gives an execution error
	/***************************
	public static String extractHEADERS_GROBID(String PDFfilePath) 
	{
		
		BiblioItem resHeader=null;
	
	    try {
	        // Biblio object for the result
	        resHeader = new BiblioItem();
	        String tei = engine.processHeader(PDFfilePath, false, resHeader);
	       
	        System.out.println(">>>>>>>> GROBID_RESULTS="+resHeader.toString());
	        
	    } 
	    catch (Exception e) {
	        // If an exception is generated, print a stack trace
	        e.printStackTrace();
	    }
	    
	    return resHeader.toString();
	    
    }

	/***************************
	
	
	/**
	 * Parsing document method
	 * 
	 * @param doc
	 * @return
	 */
	public static PDFEXTresult parse(PDFEXTresult contentsToParse) {
		
		String infoXMLStr="";
		
		// TO IMPLEMENT THE XML DOCUMENT GENERATION
		org.jsoup.nodes.Document doc = Jsoup.parse(contentsToParse.getResHTML().replaceAll("&([^;]+?);", "**$1;"), "", Parser.xmlParser());
		
		
		infoXMLStr="<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n<root>";
		
		
		
		/** Generates an unique id identifier for each <div> tag in the document
		 */

		generateDivUniqueID(doc);
		
		
		//saves the HTML original with the unique id identifiers for each div tag
		//contentsToParse.setResHTML_divid(strDocHeader+doc.head());
		
		doc.outputSettings().prettyPrint(false);
		EscapeMode em= EscapeMode.extended;
		doc.outputSettings().escapeMode(em);

		contentsToParse.setResHTML_divid(doc.outerHtml().replaceAll("\\*\\*([^;]+?);", "&$1;"));
		
	    
		/* The first and last word of each textual sentence are extracted to 
		 * detect if extra space has to be added or a "hyphen has to be removed  
		*/
		
		String strTextPreviousSentenceLastWord=null;
		String strTextCurrentSentenceFirstWord;
		
		
		//STEP 1 counts the CSS Attribute labels  .y .h .fs .x labels in the <div> tags
		
		
		countsCSSAttributeLabels(doc);
		
		
		if (verbosity)
		{
			logger.info("HASHMAP Counts heights:"+mapCountsCSSHeights.toString()+"\n");  
			logger.info("HASHMAP Counts font size:"+mapCountsCSSFontSize.toString()+"\n");  
			logger.info("HASHMAP Counts font family:"+mapCountsCSSFontFamily.toString()+"\n");
			logger.info("HASHMAP Counts bottom:"+mapCountsCSSBottom.toString()+"\n");  
			logger.info("HASHMAP Counts left:"+mapCountsCSSLeft.toString()+"\n");  
		}

		
		//STEP 2 store CSS attribute values of the .y .h .fs .x labels in the <div> tags
		
		storesCSSAttributeValues(doc);	
		

		if (verbosity)
		{
			logger.info("HASHMAP value height:"+mapValuesCSSHeights.toString()+"\n");  
			logger.info("HASHMAP value font size:"+mapValuesCSSFontSize.toString()+"\n");  
			logger.info("HASHMAP value bottom:"+mapValuesCSSBottom.toString()+"\n");  
			logger.info("HASHMAP value left:"+mapValuesCSSLeft.toString()+"\n");  					
		}
		
		
		

		
		
		/** filters out (marks) the <div> tag that are running heads or page numbers
		 */

		filterOutRunningHeadsAndPageNumbers(doc);
		


		
		
		
		// STEP 2 B.    Detect the  left values (x-axis) of the text 
		//				X1, and X2 values (for 2 column papers)
		//				X1 for (1 column papers)
		
		

		double [] columnXAxisValues =detectColumnsXAxisValue();

		
		firstColumnXAxisValue = columnXAxisValues[0];
		secondColumnXAxisValue= columnXAxisValues[1];
		
		if (secondColumnXAxisValue==0.0)	{	numColumns=1;	}
		else								{	numColumns=2;	}

		
		if (verbosity)
		{
			logger.info("X-Axis VALUE (1 or 2 columns)  X1:"+firstColumnXAxisValue+" X2: "+secondColumnXAxisValue+"\n"); 
		}

		

		
		
		
		
		
		//STEP 3  -  relate the font height with the scientific article PART:  TITLE, AUTHORS, EMAIL, ADDRESS, SECTION, TEXT, COPYRIGHT,FOOTNOTE_INDICATOR,PAGE_NUMBER, TABLE_HEADER, TABLE_ROW, VERBATIM_ROW};
		
		
		
		//Heuristic 1.
		// The most used font size and font family in div tags is the font height of the TEXT part
		
	
		String strMostUsedFontSizeAttribute=getMostUsedFontSizeAttributeInDivTags();
		String strMostUsedFontFamilyAttribute=getMostUsedFontFamilyAttributeInDivTags();
		String strMostUsedHeightAttribute=getMostUsedHeightAttributeInDivTags();

		String fontFamilyText=strMostUsedFontFamilyAttribute;
		String fontSizeText=strMostUsedFontSizeAttribute;
		
		
		
		
		// Handling the case of documents without textual contents  
		// (special cases when the HTML uses images instead of HTML tags)
		// the parsing stops and returns an ERROR comment in the generated XML
		
		if (strMostUsedFontSizeAttribute==null)
		{
			infoXMLStr += "<!-- ERROR transforming PDF to XML --></root>\n";
			contentsToParse.setResXML(infoXMLStr);
			contentsToParse.setResHTML(doc.outerHtml().replaceAll("\\*\\*([^;]+?);", "&$1;"));
			return contentsToParse;
		}
		
		
		
		double fontSizeValueText = (double)mapValuesCSSFontSize.get(strMostUsedFontSizeAttribute);
		strPattern_TEXT=strMostUsedFontSizeAttribute;
		
		if (verbosity)
		{
			logger.info("Pattern TEXT detected:"+strPattern_TEXT+"\n");  
		}	
			
				
		
		
		

		filterOutRunningHeadsAndPageNumbers2(doc,fontFamilyText,fontSizeText);
		filterOutFootnotes(doc,numColumns,fontFamilyText,fontSizeText);
		
		
		filterOutTableContents(doc,numColumns,fontFamilyText,fontSizeText,strMostUsedHeightAttribute);
		
		
		// Heuristic 2  ==>  DETECT THE TITLE FONT PATTERN
		//  The greatest font size that appears in the first page in the upper part of the page. 
		    
		
		String currentBestFontSizeAttribute=detectTITLEFontSizeAttribute(doc);
		
		
		
		// Handling the case of documents without TITLE recognized in the first page, 
		// (special cases when the HTML uses images instead of HTML tags)
		// the parsing stops and returns an ERROR comment in the generated XML
		
		if (currentBestFontSizeAttribute==null)
		{
			infoXMLStr += "<!-- ERROR transforming PDF to XML --></root>\n";
			contentsToParse.setResXML(infoXMLStr);
			contentsToParse.setResHTML(doc.outerHtml().replaceAll("\\*\\*([^;]+?);", "&$1;"));
			return contentsToParse;
		}
		
		
		
		strPattern_TITLE= currentBestFontSizeAttribute;	
		double fontSizeValueTitle = (double)mapValuesCSSFontSize.get(currentBestFontSizeAttribute);
		
		if (verbosity)
		{
			logger.info("Pattern TITLE detected:"+strPattern_TITLE+"\n");  
		}	
		
		
		// STEP 4.   Detect the Average Y-axis distance between lines of text.
		

		yAxisAverageTextDiff=detectAverageTextYAxisDistance(doc,strPattern_TEXT);
		
		if (verbosity)
		{
			System.out.println("Y axis average text diff :"+yAxisAverageTextDiff);
		}
		
		
		
		//STEP 5. Detect if the article has Section titles without numberings
		//			if so, then detects the font size of the section titles
		
		
		//  TODO
		
		//double fontSizeSectionTitlesWithoutNumbering=detectFontSizeSectionTitlesWithoutNumbering(doc,fontSizeValueText,fontSizeValueTitle);
		
		
		
		
		//Heuristics to detect ABSTRACT, SECTION, REFERENCES, FIGURES, TABLES,....
		    

	    
		String strTitle="";
		String strFiguresCaption="";
		String strTablesCaption="";
		
		String strAbstractTitle="";
		String strAbstractText="";
		String strSections="";
		String strAnnexes="";
		
		
		String strCategoriesTitle="";
		String strCategoriesText="";
		
		String strGeneralTermsTitle="";
		String strGeneralTermsText="";
		
		String strKeywordsTitle="";
		String strKeywordsText="";
		
		
		String strAcknowledgementsTitle="";
		String strAcknowledgementsText="";

		
		
		
		String strReferencesTitle="";
		String strReferencesText="";
		
		
		State currentParsingState=null;
		State previousParsingState=null;
		
			
		double previousBottomValue=99999999;
		double previousFontSizeValue=0;
		
		Elements divtags2 = doc.getElementsByTag("div");
		
				
		String datapageno="0";

		double bottomValue=0;
		double fontSizeValue=0;
		
		double leftValue=0;
		
		String currentDivFontSize="";
		String currentDivFontFamily="";
		
		
		StateSection currentStateSection = null;
		StateAnnexes currentStateAnnexes=null;
		
		
		boolean flagReferenceStateHasHappenned=false;
		boolean flagReferencesTitleStateHasHappenned=false;
		boolean flagAppendixTitleStateHasHappenned=false;
		
		double firstLineReferenceXAxisLeftValue;
		String filteredAttributeInfoStr=null;
		
		boolean flagAbstractConsumed=false;
		
		boolean flagAuthorsBiographiesTitleConsumed=false;	  

		boolean flagSupportingInformationConsumed=false;
		
		boolean flagSectionTitleHasHappenedOnce=false;
		boolean flagTextDiv=false;
		
		
		Boolean flagParagraphTagOpen= new Boolean(false) ;
		boolean flagLastSectionClosedTags=false;
		
		
		boolean flagReferencesClosed=false;
		

		DivClassCSSProperties divClassPropertiesParentNode;
		double currentAttributeHeightValueParentNode=0;
		double currentAttributeBottomValueParentNode=0;
		double currentAttributeHeightValue=0;

		
		boolean flagCurrentLineIsNormalText=false;
		boolean flagPreviousLineIsNormalText=false;  
		  
		double bottomValueTitleLastLine=0;
		
		
		
		for(iteratorGlobalElements=0;iteratorGlobalElements<divtags2.size();iteratorGlobalElements++)
		{
			Element divtag=divtags2.get(iteratorGlobalElements);
			flagTextDiv=false;
			
		  flagCurrentLineIsNormalText=false;
		  
		  String classAttributeInfoStr = divtag.attr("class");
		  
		  String strDivTagText=divtag.text();
		  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	       
		  		   
		  
		  if (classAttributeInfoStr.startsWith("pf "))
		  {
			  datapageno=divtag.attr("data-page-no");
		  }
		  
		  
		  

		  if (classAttributeInfoStr.startsWith("t "))
		  {
			  
			  
			  Element divtagParent = divtag.parent();
			  String ParentClassAttributeInfoStr = divtagParent.attr("class");
			  
			  DivClassCSSProperties divClassPropertiesParent=new DivClassCSSProperties(ParentClassAttributeInfoStr);
			  currentAttributeHeightValueParentNode=mapValuesCSSHeights.get(divClassPropertiesParent.getHeight());
			  
			  if (divClassPropertiesParent.getBottom()!=null)
			  {
				  currentAttributeBottomValueParentNode=mapValuesCSSBottom.get(divClassPropertiesParent.getBottom());
			  }

			  
			  
			  
			  
			  bottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			  
			  currentAttributeHeightValue=(double)mapValuesCSSHeights.get(divClassProperties.getHeight());
			  
			  
			  //handling the case in which the bottom value is reference by the (parent) div tag with atribute class="c ..."
			  if (currentAttributeHeightValueParentNode<currentAttributeHeightValue)
			  {
				  bottomValue+=currentAttributeBottomValueParentNode;
			  }
			  
			  
			  fontSizeValue= (double)mapValuesCSSFontSize.get(divClassProperties.getFontsize());
			  leftValue= (double)mapValuesCSSLeft.get(divClassProperties.getLeft());
			  
				
			  currentDivFontSize=divClassProperties.getFontsize();
			  currentDivFontFamily=divClassProperties.getFontfamily();
			  
			  
			  if ( (currentDivFontSize.equals(fontSizeText))  && (currentDivFontFamily.equals(fontFamilyText)))
			  {
				  flagCurrentLineIsNormalText=true;
			  }		  
			  
			  
			  filteredAttributeInfoStr=divtag.attr("filtered"); 
			  
			  flagTextDiv=true;
		  }


		if (verbosity)
		{
		  
		  System.out.println("classAttributeInfoStr:"+classAttributeInfoStr+"\n");
		  System.out.println("previousBottomValue:"+previousBottomValue+"\n");
		  System.out.println("bottomValue:"+bottomValue+"\n");
		  System.out.println("fontsizeValue:"+fontSizeValue+"\n");
		  System.out.println("fontSizeValueTitle:"+fontSizeValueTitle+"\n");
		  System.out.println("fontSizeValueText:"+fontSizeValueText+"\n");
		  
		  System.out.println("currentDivFontSize:"+currentDivFontSize+"\n");
		  System.out.println("currentDivFontFamily:"+currentDivFontFamily+"\n");
		  System.out.println("fontSizeText:"+fontSizeText+"\n");
		  System.out.println("fontFamilyText:"+fontFamilyText+"\n");
		}  
		  
		  
		
		  
		  
		  double diffBottomValue=Math.abs(previousBottomValue-bottomValue);
		  
		  double diffBottomValueWithoutAbs=previousBottomValue-bottomValue;
		  
		  boolean flagDiffBottomValue=false;
		  
		  double yAxisValueComparison=yAxisAverageTextDiff+OFFSET_yAxisAverageTextDiff; 
		  
		  // checks if the distance between the current line and the previous one is
		  //greater than the average distance between 
		  if (diffBottomValue>yAxisValueComparison)
		  {
			  flagDiffBottomValue=true;
		  }
		  
		  
			if (verbosity)
			{
				System.out.println("z_classAttributeInfoStr:"+classAttributeInfoStr+"\n");
				System.out.println("z_diffBottomValue:"+diffBottomValue+"\n");
				System.out.println("z_flagDiffBottomValue:"+flagDiffBottomValue+"\n");
			}

		  
		  //flag that indicates if the bottom value difference
		  // between two consecutive diffs indicates that the two diffs
		  // are in the same text line.
		  boolean flagDiffBottomValueWithinLine=false;

		  if (diffBottomValue<maxThresholdReferenceWithinLineBottomDistance)
			 {
			  	flagDiffBottomValueWithinLine=true;
			 }
			 
		  
		  
		  
		  
		  // TITLE detection and extraction.	  
		  // HEURISTICS
		  // * appears in page 1,
		  // * the font detected was the biggest in the upper part of the page 1
		  
		  if ( datapageno.equals("1") && classAttributeInfoStr.startsWith("t") && classAttributeInfoStr.contains(strPattern_TITLE)
				  && (bottomValue> bottomValueThresholdForTITLE))
		  {
			  
			  //title first line consumption
			  if (previousParsingState==null)
			  {
				  strTitle="<div id=\""+divtag.attr("id")+"\">"+divtag.text()+"</div>";
				  markDivTagHTMLOutput(divtag, "TITLE");
				  
				  currentParsingState=State.TITLE;
				  
				  bottomValueTitleLastLine=bottomValue;
			  }
			  else
			  {
				  
				  //other lines of title
				  if (Math.abs(bottomValueTitleLastLine-bottomValue)<30)
				  {
					  strTitle += " <div id=\""+divtag.attr("id")+"\">"+divtag.text()+"</div>";
					  markDivTagHTMLOutput(divtag, "TITLE");
				  
					  currentParsingState=State.TITLE;
					  bottomValueTitleLastLine=bottomValue;
				  }	  
			  }
			  
			  
			  
		  }
		  
		  
		  
		  
		  //conditions to detect a FIGURE CAPTION
		  // flagDiffBottomValue is not necessary
		  if (regexpMatcher.findRegexp("FIGURE_CAPTION",strDivTagText) && flagTextDiv)
		  {
			  strFiguresCaption+=consume_CAPTION(divtag,divtags2,"FIGURE_CAPTION");
			  currentParsingState=State.FIGURE_CAPTION;
			  markDivTagHTMLOutput(divtag, "FIGURE_CAPTION");
			  
		  }
		  

		  //conditions to detect a TABLE CAPTION
		  // flagDiffBottomValue is not necessary.
		  if (regexpMatcher.findRegexp("TABLE_CAPTION",strDivTagText) && flagTextDiv)
		  {
			  strTablesCaption+=consume_CAPTION(divtag,divtags2,"TABLE_CAPTION");
			  currentParsingState=State.TABLE_CAPTION;
			  markDivTagHTMLOutput(divtag, "TABLE_CAPTION");
		  }
		  	  

		  
		  //conditions to detect the ACKNOWLEDGEMENTS TITLE
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("ACKNOWLEDGEMENTS_TITLE",strDivTagText) && flagTextDiv)
		  {
			  strAcknowledgementsTitle=consume_LINE(divtag,"ACKNOWLEDGEMENTS_TITLE");
			  currentParsingState=State.ACKNOWLEDGEMENTS_TITLE;
			  markDivTagHTMLOutput(divtag, "ACKNOWLEDGEMENTS_TITLE");
			  
			  if (((currentStateSection==StateSection.SECTION) ||
					  (currentStateSection==StateSection.SUBSECTION) ||
					  (currentStateSection==StateSection.SUBSUBSECTION)) && flagLastSectionClosedTags==false
					  )
			  {  
				  strSections+=closeStateSection(currentStateSection, flagParagraphTagOpen);
				  flagLastSectionClosedTags=true;
			  }
			  
			  
			  
		  }
		  

		  
 		  //  case Acknowledgement Title and Text in the same line.	
		  if (flagDiffBottomValue &&  regexpMatcher.findRegexp("ACKNOWLEDGEMENTS_TITLE_WITH_TEXT",strDivTagText) && flagTextDiv)
		  {		  
			  
			  strAcknowledgementsTitle=	"<"+jastTagsDictionary.getFieldValue("")+">";
			  strAcknowledgementsTitle+="";
			  strAcknowledgementsTitle+="</"+jastTagsDictionary.getFieldValue("ACKNOWLEDGEMENTS_TITLE")+">\n";
					  
			  markDivTagHTMLOutput(divtag, "ACKNOWLEDGEMENTS_TITLE");
			  
			  if (((currentStateSection==StateSection.SECTION) ||
					  (currentStateSection==StateSection.SUBSECTION) ||
					  (currentStateSection==StateSection.SUBSUBSECTION)) && flagLastSectionClosedTags==false
					  )
			  {  
				  strSections+=closeStateSection(currentStateSection,flagParagraphTagOpen);
				  flagLastSectionClosedTags=true;
			  }
			  
			  
			  strAcknowledgementsText=consume_CAPTION(divtag,divtags2,"ACKNOWLEDGEMENTS_TEXT");
			  currentParsingState=State.ACKNOWLEDGEMENTS_TEXT;
			  
			  
		  }

		  
		  
		  
		  
		  //conditions to detect the REFERENCES TITLE
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("REFERENCES_TITLE",strDivTagText) && flagTextDiv)
		  {
			  strReferencesTitle=consume_LINE(divtag,"REFERENCES_TITLE");
			  currentParsingState=State.REFERENCES_TITLE;
			  
			  markDivTagHTMLOutput(divtag, "REFERENCES_TITLE");
			  
			  if (((currentStateSection==StateSection.SECTION) ||
					  (currentStateSection==StateSection.SUBSECTION) ||
					  (currentStateSection==StateSection.SUBSUBSECTION)) && flagLastSectionClosedTags==false
					  )
			  {  
				  strSections+=closeStateSection(currentStateSection,flagParagraphTagOpen);
				  flagLastSectionClosedTags=true;
			  }
			  currentStateSection=null;
			  
			  strReferencesText=	"<"+jastTagsDictionary.getFieldValue("REFERENCES_TEXT")+">\n";
			  
			  flagReferencesTitleStateHasHappenned=true;
			  
			  
		  }
		  

		  //conditions to detect the CATEGORIES TITLE
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("CATEGORIES_TITLE",strDivTagText) && flagTextDiv)
		  {
			  strCategoriesTitle=consume_LINE(divtag,"CATEGORIES_TITLE");
			  currentParsingState=State.CATEGORIES_TITLE;
			  markDivTagHTMLOutput(divtag, "CATEGORIES_TITLE");
		  }

		  

		  //conditions to detect the GENERAL TERMS TITLE
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("GENERAL_TERMS_TITLE",strDivTagText) && flagTextDiv)
		  {
			  strGeneralTermsTitle=consume_LINE(divtag,"GENERAL_TERMS_TITLE");
			  currentParsingState=State.GENERAL_TERMS_TITLE;
			  markDivTagHTMLOutput(divtag, "GENERAL_TERMS_TITLE");
		  }


		  
		  //conditions to detect the KEYWORDS TITLE
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("KEYWORDS_TITLE",strDivTagText) && flagTextDiv)
		  {
			  strKeywordsTitle=consume_LINE(divtag,"KEYWORDS_TITLE");
			  currentParsingState=State.KEYWORDS_TITLE;
			  markDivTagHTMLOutput(divtag, "KEYWORDS_TITLE");
		  }

		  
		  
 		  //  case Keywords  Title and Text in the same line.	
		  if (flagDiffBottomValue &&  regexpMatcher.findRegexp("KEYWORDS_TITLE_WITH_TEXT",strDivTagText) && flagTextDiv)
		  {		  
			  
			  strKeywordsTitle=	"<"+jastTagsDictionary.getFieldValue("KEYWORDS_TITLE")+">";
			  strKeywordsTitle+="";
			  strKeywordsTitle+="</"+jastTagsDictionary.getFieldValue("KEYWORDS_TITLE")+">\n";
					  
			  strKeywordsText=consume_CAPTION(divtag,divtags2,"KEYWORDS_TEXT");
			  currentParsingState=State.KEYWORDS_TEXT;
			  
			  markDivTagHTMLOutput(divtag, "KEYWORDS_TITLE");
			  
			  
		  }
		  
		  
		  
		  
		  
 		  //  case Categories  Title and Text in the same line.	
		  if (flagDiffBottomValue &&  regexpMatcher.findRegexp("CATEGORIES_TITLE_WITH_TEXT",strDivTagText) && flagTextDiv)
		  {		  
			  
			  strCategoriesTitle=	"<"+jastTagsDictionary.getFieldValue("CATEGORIES_TITLE")+">";
			  strCategoriesTitle+="";
			  strCategoriesTitle+="</"+jastTagsDictionary.getFieldValue("CATEGORIES_TITLE")+">\n";
					  
			  strCategoriesText=consume_CAPTION(divtag,divtags2,"CATEGORIES_TEXT");
			  currentParsingState=State.CATEGORIES_TEXT;
			  
			  markDivTagHTMLOutput(divtag, "CATEGORIES_TITLE");
			  
			  
		  }
		  


		  //conditions to detect the ABSTRACT TITLE
		  //ABSTRACT title has  1 or 2 or more lines
		  if (flagDiffBottomValue &&  regexpMatcher.matchRegexp("ABSTRACT_TITLE",strDivTagText) &&  flagTextDiv)
		  {
			  strAbstractTitle=consume_LINE(divtag,"ABSTRACT_TITLE");
			  currentParsingState=State.ABSTRACT_TITLE;
			  
			  markDivTagHTMLOutput(divtag, "ABSTRACT_TITLE");
			  
		  }


		  
		  
		  

		  
	    // SECTION TITLE detection and extraction
		  
		  // 	conditions
		  //   	* starts with a number or a letter (consult SECTION_TITLE REGEXP)
		  //   	* the section font size is greater than normal text font size	  
		  //  	* the section font size is smaller than the article's title font size
		  
		  //   	/// cURRENTLY CONDITION NOT APPLIED FOR CASES OF TITLES WITH SEVERAL LINES 
		  		////		* the next line after the section title is normal text
		  
		  //    * the previous line is at a distance greater than the normal text average distance.
		  //    * the current line has not been detected as an Abstract title,
		  //         categories title, general terms title, keyword title, acknowledgments title
		  //        or references title.        
		  
		  //	 *	authorsBiographies section has not appeared yet	  
		  //     *  references section not detected yet
		  //	 * 	Supporting Information section not detected yet 
		  //	 *  annexes (appendixes) sections not detected yet
		  
		  //     * abstract has been consumed
		
		  
		  if   ( (fontSizeValue<=fontSizeValueTitle) &&
				 (	(fontSizeValue>fontSizeValueText) ||
					 (fontSizeValue==fontSizeValueText && (fontFamilyText.equals(divClassProperties.getFontfamily())==false))	
				  ) &&
				  regexpMatcher.findRegexp("SECTION_TITLE",strDivTagText) &&
				  currentParsingState!=State.ABSTRACT_TITLE &&
				  currentParsingState!=State.CATEGORIES_TITLE &&
				  currentParsingState!=State.GENERAL_TERMS_TITLE &&
				  currentParsingState!=State.KEYWORDS_TITLE &&
				  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
				  currentParsingState!=State.REFERENCES_TITLE &&
				  flagDiffBottomValue && 
				  flagTextDiv &&
				  flagAbstractConsumed &&
					(flagAuthorsBiographiesTitleConsumed==false &&	  
					flagReferencesTitleStateHasHappenned==false &&
					flagSupportingInformationConsumed==false && 
					flagAppendixTitleStateHasHappenned==false
					)	
				  
				  //&& nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
				)  
				  
		  {
			  

			  
			  if (currentStateSection==StateSection.SUBSUBSECTION)
			  {
				  
				  
				  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
				  								flagParagraphTagOpen=false;
				  							}
				  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSUBSECTION_TEXT")+">\n";
				  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
				  strSections+="</"+jastTagsDictionary.getFieldValue("SECTION_TEXT")+">\n";
			  }  
			  else
			  {
				  if (currentStateSection==StateSection.SUBSECTION)
				  {
					  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
					  								flagParagraphTagOpen=false;
					  							}
					  
					  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
					  strSections+="</"+jastTagsDictionary.getFieldValue("SECTION_TEXT")+">\n";
				  } 
				  else
				  {
					  if ((strSections!="")) 
					  {
						  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
						  								flagParagraphTagOpen=false;
						  							}
						  
						  strSections+="</"+jastTagsDictionary.getFieldValue("SECTION_TEXT")+">\n";
					  }	
				  }  	
			  }
				  
			  
			  
			  
			  
			  strSections+=consume_TITLE(divtag,divtags2,"SECTION_TITLE");
			  strSections+="<"+jastTagsDictionary.getFieldValue("SECTION_TEXT")+">\n";
			  
			  currentParsingState=State.SECTION_TITLE;
			  currentStateSection=StateSection.SECTION;
			  
			  
			  if (flagSectionTitleHasHappenedOnce==false)
			  {
				  
				  flagSectionTitleHasHappenedOnce=true;
			  }
			  

			  
		  }		  

		 
		
		  
		  
		    // SUBSECTION TITLE detection and extraction
		  
			  // 	conditions
			  //   	* starts with a number dot number (consult SUBSECTION_TITLE regexp)
		  	  //   TODO --> 	* the subsection font size is equal or smaller than subsection font size	  
			  //   	* the subsection font size is greater than normal text font size	  
			
		   	//   NOT ALWAYS * the subsection font size is smaller than the article's title font size
		  			// in some cases the font type is different and the font size of the subsection is
		            //smaller than the text
		  			// for this reason a correction factor of +2 has been added to this condition
		  
		  
		  
			  //   	* the next line after the section title is normal text
			  //    * the previous line is at a distance greater than the normal text average distance.
			  //    * the current line has not been detected as an Abstract title,
			  //         categories title, general terms title, keyword title, acknowledgments title
			  //        or references title.        
			
			  
			  if   ( (fontSizeValue<fontSizeValueTitle) &&
						 (	(fontSizeValue>fontSizeValueText) ||
								 (fontSizeValue+2>fontSizeValueText && (fontFamilyText.equals(divClassProperties.getFontfamily())==false))	
							  ) &&
					  regexpMatcher.findRegexp("SUBSECTION_TITLE",strDivTagText) &&
					  currentParsingState!=State.ABSTRACT_TITLE &&
					  currentParsingState!=State.CATEGORIES_TITLE &&
					  currentParsingState!=State.GENERAL_TERMS_TITLE &&
					  currentParsingState!=State.KEYWORDS_TITLE &&
					  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
					  currentParsingState!=State.REFERENCES_TITLE &&
					  currentParsingState!=State.SECTION_TITLE &&
					  flagDiffBottomValue  && 
					  flagTextDiv &&
					  flagAbstractConsumed &&
						(flagAuthorsBiographiesTitleConsumed==false &&	  
						flagReferencesTitleStateHasHappenned==false &&
						flagSupportingInformationConsumed==false && 
						flagAppendixTitleStateHasHappenned==false
						)	
					  //nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
					)  
					  
			  {
				  
				  
				  

				  
				  
				  if ((strSections!="")) 
				  {

					  if (currentStateSection==StateSection.SUBSUBSECTION)
					  {
						  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
						  								flagParagraphTagOpen=false;
						  							}
						  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSUBSECTION_TEXT")+">\n";
						  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
					  }  

					  else
						  
					  {
						  
						  if (currentStateSection==StateSection.SUBSECTION)
					  
						  {
							  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
							  								flagParagraphTagOpen=false;
							  							}
							  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
						  }

						  else
						  {
							  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
							  								flagParagraphTagOpen=false;
							  							}
						  }
						  
					  }  
						  
				  }	
				  			  
				  strSections+=consume_TITLE(divtag,divtags2,"SUBSECTION_TITLE");
				  
				  strSections+="<"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
				  
				  currentParsingState=State.SUBSECTION_TITLE;
				  currentStateSection=StateSection.SUBSECTION;
				  
				  
				  
			  }	
		  
		  
			  
			  
			  
			    // SUBSUBSECTION  TITLE detection and extraction
			  
				  // 	conditions
				  //   	* starts with a number dot number dot number (consult SUBSflagSupportingInformationConsumed=true;	UBSECTION_TITLE regexp)
			  	  //   TODO --> 	* the subsection font size is equal or smaller than subsection font size	  
				  //   	NOT APPLIED HERE THIS RULE: the subsection font size is greater than normal text font size	  
				  //  	* the subsection font size is smaller than the article's title font size
				  //   	NOT APPLIED THIS RULE * the next line after the section title is normal text
				  //    * the previous line is at a distance greater than the normal text average distance.
				  //    * the current line has not been detected as an Abstract title,
				  //         categories title, general terms title, keyword title, acknowledgments title
				  //        or references title.        
				
				  
				  if   ( (fontSizeValue<fontSizeValueTitle) &&
						  regexpMatcher.findRegexp("SUBSUBSECTION_TITLE",strDivTagText) &&
						  currentParsingState!=State.ABSTRACT_TITLE &&
						  currentParsingState!=State.CATEGORIES_TITLE &&
						  currentParsingState!=State.GENERAL_TERMS_TITLE &&
						  currentParsingState!=State.KEYWORDS_TITLE &&
						  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
						  currentParsingState!=State.REFERENCES_TITLE &&
						  currentParsingState!=State.SECTION_TITLE &&
						  currentParsingState!=State.SUBSECTION_TITLE &&
						  flagDiffBottomValue &&
						  flagTextDiv && 
						  flagAbstractConsumed &&
							(flagAuthorsBiographiesTitleConsumed==false &&	  
							flagReferencesTitleStateHasHappenned==false &&
							flagSupportingInformationConsumed==false && 
							flagAppendixTitleStateHasHappenned==false
							)	
						  //nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
						)  
						  
				  {
					  
					  
					  if ((strSections!="")) 
					  {
						  if (currentStateSection==StateSection.SUBSUBSECTION)
						  {
							  
							  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
							  								flagParagraphTagOpen=false;
							  							}
							  strSections+="</"+jastTagsDictionary.getFieldValue("SUBSUBSECTION_TEXT")+">\n";
						  }  
						  
						  else
						  {
							  if (flagParagraphTagOpen)	 { strSections+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
							  									flagParagraphTagOpen=false;
							  								}
						  }
					  }	
					  
					  strSections+=consume_TITLE(divtag,divtags2,"SUBSUBSECTION_TITLE");
					  strSections+="<"+jastTagsDictionary.getFieldValue("SUBSUBSECTION_TEXT")+">\n";
					  
					  currentParsingState=State.SUBSUBSECTION_TITLE;
					  currentStateSection=StateSection.SUBSUBSECTION;
					  markDivTagHTMLOutput(divtag, "SUBSUBSECTION_TITLE");
					  
				  }	
			  
			  
			  
		
				    // ANNEX TITLE detection and extraction
				  
					  // 	conditions
					  //   	* starts with capital letters (consult aNNEX_TITLE regexp)
					  //   	* the annex font size is greater than normal text font size	  
					  //  	* the annex font size is smaller than the article's title font size
					  //   	// condicion NOT ACTIVE to detect titles  with several lines  -- * the next line after the section title is normal text
					  //    * the previous line is at a distance greater than the normal text average distance.
					  //    * the current line has not been detected as an Abstract title,
					  //         categories title, general terms title, keyword title, acknowledgments title
					  //        references title, section title, subsection title or subsubsection title.        
					
					  
					  if   ( (fontSizeValue<fontSizeValueTitle) &&
							  (fontSizeValue>fontSizeValueText) &&
							  regexpMatcher.findRegexp("ANNEX_TITLE",strDivTagText) &&
							  currentParsingState!=State.ABSTRACT_TITLE &&
							  currentParsingState!=State.CATEGORIES_TITLE &&
							  currentParsingState!=State.GENERAL_TERMS_TITLE &&
							  currentParsingState!=State.KEYWORDS_TITLE &&
							  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
							  currentParsingState!=State.REFERENCES_TITLE &&
							  currentParsingState!=State.SECTION_TITLE &&
							  currentParsingState!=State.SUBSECTION_TITLE &&
							  currentParsingState!=State.SUBSUBSECTION_TITLE &&
							  flagDiffBottomValue &&
							  flagTextDiv 
							  //nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
							)  
							  
					  {
						  
						  
						  if (((currentStateSection==StateSection.SECTION) ||
								  (currentStateSection==StateSection.SUBSECTION) ||
								  (currentStateSection==StateSection.SUBSUBSECTION)) && flagLastSectionClosedTags==false
								  )
						  {  
							  strSections+=closeStateSection(currentStateSection,flagParagraphTagOpen);
							  flagLastSectionClosedTags=true;
						  }
						  
						  
						  
							if (flagReferencesTitleStateHasHappenned==true && flagReferencesClosed==false)
							{
								strReferencesText+="</"+jastTagsDictionary.getFieldValue("REFERENCES_TEXT")+">";
								flagReferencesClosed=true;
							}	

						  
						  
						  if ((currentStateAnnexes==StateAnnexes.ANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
								  )
						  {  
							  strAnnexes+=closeStateAnnexes(currentStateAnnexes);
						  }
						  
						  
						  /**if ((strAnnexes!="")) 
						  {
							  if (currentStateAnnexes==StateAnnexes.ANNEX)
							  {
								  strAnnexes+="</"+jastTagsDictionary.getFieldValue("ANNEX_TEXT")+">";
							  }  
						  }	
						  
						  */
						  
						  
						  //strAnnexes+="\n<"+jastTagsDictionary.getFieldValue("ANNEX_TITLE")+">";
						  
						  strAnnexes+=consume_TITLE(divtag,divtags2,"ANNEX_TITLE");
						  
						  //strAnnexes+="<div id=\""+divtag.attr("id")+"\">"+divtag.text()+"</div>";
						  //strAnnexes+="</"+jastTagsDictionary.getFieldValue("ANNEX_TITLE")+">";
						  
						  strAnnexes+="\n<"+jastTagsDictionary.getFieldValue("ANNEX_TEXT")+">";
						  
						  currentParsingState=State.ANNEX_TITLE;
						  currentStateAnnexes=StateAnnexes.ANNEX;
						  currentStateSection=null;
						  //markDivTagHTMLOutput(divtag, "ANNEX_TITLE");
						  
						  flagAppendixTitleStateHasHappenned=true;
						  
						  
					  }	
				  
			  
			  
		
					  
					  //AUTHORS_BIOGRAPHIES_TITLE
					  
					  
					  if   ( (fontSizeValue<fontSizeValueTitle) &&
							  (fontSizeValue>fontSizeValueText) &&
							  regexpMatcher.matchRegexp("AUTHORS_BIOGRAPHIES_TITLE",strDivTagText) &&
							  currentParsingState!=State.ABSTRACT_TITLE &&
							  currentParsingState!=State.CATEGORIES_TITLE &&
							  currentParsingState!=State.GENERAL_TERMS_TITLE &&
							  currentParsingState!=State.KEYWORDS_TITLE &&
							  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
							  currentParsingState!=State.REFERENCES_TITLE &&
							  currentParsingState!=State.SECTION_TITLE &&
							  currentParsingState!=State.SUBSECTION_TITLE &&
							  currentParsingState!=State.SUBSUBSECTION_TITLE &&
							  flagDiffBottomValue &&
							  flagTextDiv &&
							  nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
							)  
							  
					  {
						  
							if (flagReferencesTitleStateHasHappenned==true && flagReferencesClosed==false)
							{
								strReferencesText+="</"+jastTagsDictionary.getFieldValue("REFERENCES_TEXT")+">";
								flagReferencesClosed=true;
							}	
							
							
						  
						  if (((currentStateSection==StateSection.SECTION) ||
								  (currentStateSection==StateSection.SUBSECTION) ||
								  (currentStateSection==StateSection.SUBSUBSECTION)) && flagLastSectionClosedTags==false
								  )
						  {  
							  strSections+=closeStateSection(currentStateSection,flagParagraphTagOpen);
							  flagLastSectionClosedTags=true;
						  }
						  
						  
						  
						  if ((currentStateAnnexes==StateAnnexes.ANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
								  )
						  {  
							  strAnnexes+=closeStateAnnexes(currentStateAnnexes);
						  }
						  
						  
						  if ((strAnnexes!="")) 
						  {
							  if (currentStateAnnexes==StateAnnexes.ANNEX)
							  {
								  strAnnexes+="</"+jastTagsDictionary.getFieldValue("ANNEX_TEXT")+">";
							  }  
						  }	
						
						  
						  currentParsingState=State.AUTHORS_BIOGRAPHIES_TITLE;
						  currentStateAnnexes=null;
						  
						  markDivTagHTMLOutput(divtag, "AUTHORS_BIOGRAPHIES_TITLE");
						  
						  flagAuthorsBiographiesTitleConsumed=true;	  

						  
					  }	
				  
		  
					  
					  
					  //SUPPORTING_INFORMATION_TITLE
					  
					  
					  if   ( (fontSizeValue<fontSizeValueTitle) &&
							  (fontSizeValue>fontSizeValueText) &&
							  regexpMatcher.matchRegexp("SUPPORTING_INFORMATION_TITLE",strDivTagText) &&
							  currentParsingState!=State.ABSTRACT_TITLE &&
							  currentParsingState!=State.CATEGORIES_TITLE &&
							  currentParsingState!=State.GENERAL_TERMS_TITLE &&
							  currentParsingState!=State.KEYWORDS_TITLE &&
							  currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE &&
							  currentParsingState!=State.REFERENCES_TITLE &&
							  currentParsingState!=State.SECTION_TITLE &&
							  currentParsingState!=State.SUBSECTION_TITLE &&
							  currentParsingState!=State.SUBSUBSECTION_TITLE &&
							  flagDiffBottomValue &&
							  flagTextDiv &&
							  nextLineIsNormalText(divtags2,iteratorGlobalElements,strPattern_TEXT)
							)  
							  
					  {
						  
							if (flagReferencesTitleStateHasHappenned==true && flagReferencesClosed==false)
							{
								strReferencesText+="</"+jastTagsDictionary.getFieldValue("REFERENCES_TEXT")+">";
								flagReferencesClosed=true;
							}	
						  
						  
						  if ((currentStateAnnexes==StateAnnexes.ANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBANNEX) ||
								  (currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
								  )
						  {  
							  strAnnexes+=closeStateAnnexes(currentStateAnnexes);
							  flagLastSectionClosedTags=true;
						  }
						  
						  
						  if ((strAnnexes!="")) 
						  {
							  if (currentStateAnnexes==StateAnnexes.ANNEX)
							  {
								  strAnnexes+="</"+jastTagsDictionary.getFieldValue("ANNEX_TEXT")+">";
							  }  
						  }	
						
						  
						  currentParsingState=State.SUPPORTING_INFORMATION_TITLE;
						  currentStateAnnexes=null;
						  
						  markDivTagHTMLOutput(divtag, "SUPPORTING_INFORMATION_TITLE");
						  
						  flagSupportingInformationConsumed=true;	  

						  
					  }			  
					  
		  
		  
		  
		  

					  
		  
		  
		  
		  //case ABSTRACT and abstact text in the same line.
		  
		  
		  
		  //conditions to detect ABSTRACT TEXT
		  if (classAttributeInfoStr.startsWith("t ")  && previousParsingState==State.ABSTRACT_TITLE)
		  {
			  strAbstractText=consume_ABSTRACT(divtag,divtags2,"ABSTRACT_TEXT");
			  currentParsingState=State.ABSTRACT_TEXT;
			  
			  flagAbstractConsumed=true;
		  }	
		  
			  
		  //conditions to detect CATEGORIES TEXT
		  if (classAttributeInfoStr.startsWith("t ")  && previousParsingState==State.CATEGORIES_TITLE)
		  {
			  strCategoriesText=consume_CAPTION(divtag,divtags2,"CATEGORIES_TEXT");
			  currentParsingState=State.CATEGORIES_TEXT;
		  }	
		  
		  //conditions to detect GENERAL TERMS TEXT
		  if (classAttributeInfoStr.startsWith("t ")  && previousParsingState==State.GENERAL_TERMS_TITLE)
		  {
			  strGeneralTermsText=consume_CAPTION(divtag,divtags2,"GENERAL_TERMS_TEXT");
			  currentParsingState=State.GENERAL_TERMS_TEXT;
		  }	

		  
		  //conditions to detect KEYWORDS TEXT
		  if (classAttributeInfoStr.startsWith("t ")  && previousParsingState==State.KEYWORDS_TITLE)
		  {
			  strKeywordsText=consume_CAPTION(divtag,divtags2,"KEYWORDS_TEXT");
			  currentParsingState=State.KEYWORDS_TEXT;
		  }	

		  
		  //conditions to detect ACKNOWLEDGEMENTS TEXT
		  if (classAttributeInfoStr.startsWith("t ")  && previousParsingState==State.ACKNOWLEDGEMENTS_TITLE)
		  {
			  strAcknowledgementsText=consume_ACKNOWLEDGEMENTS(divtag,divtags2,"ACKNOWLEDGEMENTS_TEXT");
			  currentParsingState=State.ACKNOWLEDGEMENTS_TEXT;
		  }	

		  
		  
		  
		  
		  
		  //conditions to detect REFERENCES TEXT
		  // * the previous Div was the References title 
		  //				OR 
		 //      the previous div was a reference   
		 //      OR  (in  case the previous div was not references title or references, that could happen
		  //      in a page brek, then one of the previous states had to be references title or reference. 
		  //
		  //  * there is an indentation in the first line of the reference.
		  //      OR 
		  //     there is a regular expression that captures the syntax of some kind of
		  //       references such as: ""[1]. A..."
		  //
		  //  * the reference ends when 
		  //      (the next line has a distance greater than the Y axis text average distance
		  //                 OR 
		  //      (the next line has an indentation such as a first line of reference.)             
		   
		  
		  
		  if (classAttributeInfoStr.startsWith("t ") && 
				  filteredAttributeInfoStr.equals("false") &&
			  flagReferencesClosed==false &&  	  
			  flagAuthorsBiographiesTitleConsumed==false &&	  
			  flagSupportingInformationConsumed==false && 
			  (previousParsingState==State.REFERENCES_TEXT || 
			  previousParsingState==State.REFERENCES_TITLE ||
			   ( (flagReferencesTitleStateHasHappenned==true && currentParsingState!=State.REFERENCES_TITLE) ||
			     flagReferenceStateHasHappenned==true)
  			  )
  			  && 
  			  (checkReferenceByFirstLineIndentation(divtag,divtags2,iteratorGlobalElements)==true
  			  	||
			  regexpMatcher.findRegexp("REFERENCE_INDEX",divtag.text())) )
			{
				 
			  
				if (verbosity)
				{
					System.out.println("REFERENCE detected 1:");
				}	
			  
			  	String divTagFirstLineClassAttributeInfoStr = divtag.attr("class");
			  	DivClassCSSProperties divTagFirstLineProperties=new DivClassCSSProperties(divTagFirstLineClassAttributeInfoStr);
			  	firstLineReferenceXAxisLeftValue=(double)mapValuesCSSLeft.get(divTagFirstLineProperties.getLeft());
			    
			  
			  	strReferencesText+=consume_REFERENCE(divtag,divtags2,"REFERENCE");
			  	currentParsingState=State.REFERENCES_TEXT;
			  	
			  	flagReferenceStateHasHappenned=true;
		
				if (verbosity)
				{
					System.out.println("detectedREFERENCE");
				}	
		 	}	
		  else
		  {
			  
			  //check the cases of a broken reference due to a page break or a column break.
			  
			  
			   if (classAttributeInfoStr.startsWith("t ") &&
					   flagReferencesClosed==false && 
					flagAuthorsBiographiesTitleConsumed==false &&
					flagSupportingInformationConsumed==false &&
					filteredAttributeInfoStr.equals("false") &&
					   ((flagReferencesTitleStateHasHappenned==true && currentParsingState!=State.REFERENCES_TITLE) ||
					     flagReferenceStateHasHappenned==true)
				   	&&
		   	   checkPartOfPreviousReferenceInFirstPosColumn(divtag,divtags2,iteratorGlobalElements))
			  {
				
				  if (verbosity)
				  { 
					  System.out.println("REFERENCE detected 2:");
				  }
				  
				  String strBrokenReference=consume_REFERENCE(divtag,divtags2,"REFERENCE");
				  currentParsingState=State.REFERENCES_TEXT;
				  
				  int beginIndex=jastTagsDictionary.getFieldValue("REFERENCE").length()+3;
				  int endIndex=strBrokenReference.length();
				  
				  String strBrokenReferenceWithoutInitialTag=strBrokenReference.substring(beginIndex,endIndex);
				  int endIndexStrReferences=strReferencesText.length()-jastTagsDictionary.getFieldValue("REFERENCE").length()-4;
				  strReferencesText=strReferencesText.substring(0,endIndexStrReferences)+strBrokenReferenceWithoutInitialTag;
			  }
			  
			   else
			   {
				   
				   
				   // case one line reference
				   
				   if (classAttributeInfoStr.startsWith("t ") &&
						 flagReferencesClosed==false && 
						flagAuthorsBiographiesTitleConsumed==false &&
						flagSupportingInformationConsumed==false &&
						filteredAttributeInfoStr.equals("false") &&
					   ((flagReferencesTitleStateHasHappenned==true && currentParsingState!=State.REFERENCES_TITLE) 							||
						     flagReferenceStateHasHappenned==true) )
				   {
					   
					   strReferencesText+="\n<"+jastTagsDictionary.getFieldValue("REFERENCE")+">"+"<div id=\""+divtag.attr("id")+"\">";
					   
					   strReferencesText+=divtag.text();
							   
					   strReferencesText+="</div></"+jastTagsDictionary.getFieldValue("REFERENCE")+">\n";
						
					   markDivTagHTMLOutput(divtag, "REFERENCE");
					
					   
				   }
				   
				   
				   
				   
				   
				   
				   
			   }
			   
			   
			   
			   
			  
			  
		  }
			 
					 

		  
		  

		  
		  
		  
		  
		  
		  
  
				// TEXT detection and extraction
				  // takes into account the hyphens at the end of line and the spaces 
				  
		  if (
				flagAbstractConsumed==true &&  
			   classAttributeInfoStr.startsWith("t ") && 
			  filteredAttributeInfoStr.equals("false") &&
			  flagReferencesTitleStateHasHappenned==false &&
		     (  classAttributeInfoStr.contains(strPattern_TEXT) || 
		    	childrenNodeHasTextFontAttribute(divtag,strPattern_TEXT) ||
		    	flagDiffBottomValueWithinLine==true)
				  && 
					  (currentParsingState!=State.FIGURE_CAPTION) &&
					  (currentParsingState!=State.TABLE_CAPTION) && 
					  (currentParsingState!=State.ABSTRACT_TITLE) &&
					  (currentParsingState!=State.ABSTRACT_TEXT) &&
					  (currentParsingState!=State.CATEGORIES_TITLE) &&
					  (currentParsingState!=State.CATEGORIES_TEXT) &&
					  (currentParsingState!=State.KEYWORDS_TITLE) &&
					  (currentParsingState!=State.KEYWORDS_TEXT) &&
					  (currentParsingState!=State.GENERAL_TERMS_TITLE) &&
					  (currentParsingState!=State.GENERAL_TERMS_TEXT) &&
					  (currentParsingState!=State.ACKNOWLEDGEMENTS_TITLE) &&
					  (currentParsingState!=State.ACKNOWLEDGEMENTS_TEXT) && 
					  (currentParsingState!=State.REFERENCES_TITLE) &&
					  (currentParsingState!=State.SECTION_TITLE) &&
					  (currentParsingState!=State.SUBSECTION_TITLE) &&
					  (currentParsingState!=State.SUBSUBSECTION_TITLE) &&
					  (currentParsingState!=State.REFERENCES_TEXT)
				 &&
				 flagSectionTitleHasHappenedOnce==true 
				 &&
					(flagAuthorsBiographiesTitleConsumed==false &&	  
					flagReferencesTitleStateHasHappenned==false &&
					flagSupportingInformationConsumed==false && 
					flagAppendixTitleStateHasHappenned==false
					)	
				 
					
					//** CONDITIONS NOT USED - FONT SIZE NOT USED because of equationss written in font sizes smaller than normal text were not recognized  
				    // && (fontSizeValueText==fontSizeValue)   // 
				//&& (currentDivFontSize.equals(fontSizeText) || currentDivFontFamily.equals(fontFamilyText))
				  ) 
				 
					  
			  {
				  
				  if (   (previousParsingState==State.SECTION_TITLE) ||
						  (previousParsingState==State.SUBSECTION_TITLE) ||
						  (previousParsingState==State.SUBSUBSECTION_TITLE))
						  
				  {
					  String divTagText=divtag.text();
					  StringTokenizer tokenizer=new StringTokenizer(divTagText);
					  String textWithoutLastWord="";
					  
					  int num_tokens= tokenizer.countTokens();
					 
					  int i;
					  for(i=0;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  strSections+="<"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
					  flagParagraphTagOpen=true;
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
						  
						  if (strLastWord.endsWith("-"))
						  {
							  strSections+="<div id=\""+divtag.attr("id")+"\">"+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strSections+="<div id=\""+divtag.attr("id")+"\">"+textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  }
					  else
					  {
						  
						  strSections+="<div id=\""+divtag.attr("id")+"\"></div>\n";
						  strTextPreviousSentenceLastWord="";					  
					  }
					  
					  
					  String typeDivTag="SECTION_TEXT";
					  
					  if (previousParsingState==State.SUBSECTION_TITLE)
					  {
						  typeDivTag="SUBSECTION_TEXT";
						  currentParsingState=State.SUBSECTION_TEXT;
					  }
					  else
					  {
						
						  if (previousParsingState==State.SUBSUBSECTION_TITLE)
						  {
							  typeDivTag="SUBSUBSECTION_TEXT";
							  currentParsingState=State.SUBSUBSECTION_TEXT;
						  }
						  else
						  {
							  currentParsingState=State.SECTION_TEXT;
							  
						  }
						  
						  
					  }
					  
					  
					  
					  
					  markDivTagHTMLOutput(divtag,typeDivTag);
					  
					  
				  }
				  else
				  {

					  
					  //check for new paragraph start
					  boolean flagParagraphStart=false;
					  
					  //case 1 column article
					  if (numColumns==1)
					  {

						  if ( (leftValue>firstColumnXAxisValue+OFFSET_ColumnXAxisValue)
								  //&& (flagDiffBottomValue) 
								  &&  (diffBottomValueWithoutAbs>0)
							  )	  
						  {
							  flagParagraphStart=true;
						  }
						  
						  
					  }
					  else
					  {
						  //case 2 columns article
						  if (numColumns==2)
						  {

							  if ((leftValue>firstColumnXAxisValue+OFFSET_ColumnXAxisValue) &&
								  (leftValue<secondColumnXAxisValue)
								  //&& (flagDiffBottomValue)
								  && flagPreviousLineIsNormalText
								  && flagCurrentLineIsNormalText
								  &&  (diffBottomValueWithoutAbs>0)
									  
									  )
								 {
								  	flagParagraphStart=true;
								 }
							  
							  if ((leftValue>firstColumnXAxisValue+OFFSET_ColumnXAxisValue) &&
								  (leftValue>secondColumnXAxisValue+OFFSET_ColumnXAxisValue)
								  && flagPreviousLineIsNormalText
								  && flagCurrentLineIsNormalText
								  
								  //&& (flagDiffBottomValue)
								  &&  (diffBottomValueWithoutAbs>0)
									  )
								 {
								  	flagParagraphStart=true;
								 }	
						  }
					  }
					  
					  
					  String strParagraphMark="";
					  
					  
					  
					  if (flagParagraphStart==true)
					  {

						  if (flagParagraphTagOpen==true)
						  {
							  strParagraphMark="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
						  }	  
							  
						  strParagraphMark+="<"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";
						  flagParagraphTagOpen=true;
					  }
					  
					  
					  
					  
					  
					  
					  String divTagText=divtag.text();
					  StringTokenizer tokenizer=new StringTokenizer(divTagText);
					  String textWithoutLastWord="";
					  
					  
					  int num_tokens= tokenizer.countTokens();
					  
					  if(num_tokens>0)
					  {
						  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
					  }
					  else
					  {
						  strTextCurrentSentenceFirstWord="";
					  }
	
					  
					  if (strTextPreviousSentenceLastWord!="")
					  {
						  
						  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
						  
						  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
						  {
							  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  else
						  {
							  
							  String strTextPreviousSentenceLasWordWithoutHyphen="";
							  
							  if (strTextPreviousSentenceLastWord!=null)
							  {
								  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
							  }
							  
							  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  
						  
					  }
					  else
					  {
						  textWithoutLastWord=""+strParagraphMark+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  
					  }
					  
					  int i;
					  for(i=1;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  //get last token
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strSections+=" "+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strSections+=textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  
					  }
					  else
					  {
						  strTextPreviousSentenceLastWord="";	
						  strSections+=textWithoutLastWord+"</div>";
					  	
					  }
					  
					  
					  String typeDivTag="SECTION_TEXT";
					  
						
					  
					  
					  if (previousParsingState==State.SUBSECTION_TEXT)
					  {
						  currentParsingState=State.SUBSECTION_TEXT;
						  typeDivTag="SUBSECTION_TEXT";
					  }
					  else
					  {
						  if (previousParsingState==State.SUBSUBSECTION_TEXT)
						  {
							  currentParsingState=State.SUBSUBSECTION_TEXT;
							  typeDivTag="SUBSUBSECTION_TEXT";
						  }
						  else
						  {
							  currentParsingState=State.SECTION_TEXT;
							  
						  }
					  }
					  
					  
					  
					  markDivTagHTMLOutput(divtag,typeDivTag);
					  
				  }
				  
				  
				  
			  }		  
		  
		 
		  
		  if (flagCurrentLineIsNormalText)
		  {
			  flagPreviousLineIsNormalText=true;  
		  }
		  
		  
		  previousParsingState=currentParsingState;
		  currentParsingState=null;
		  previousBottomValue=bottomValue;
		  previousFontSizeValue= fontSizeValue;
		  
		

		  
		  
		}
		  
		  //logger.info(classAttributeInfoStr+"\n");  
		
		
		if (flagReferencesTitleStateHasHappenned==true && flagReferencesClosed==false)
		{
			strReferencesText+="</"+jastTagsDictionary.getFieldValue("REFERENCES_TEXT")+">";
			flagReferencesClosed=true;
		}	

		
		if (flagLastSectionClosedTags==false)
		{
			strSections+=closeStateSection(currentStateSection,flagParagraphTagOpen);
		}
		
		strAnnexes+=closeStateAnnexes(currentStateAnnexes);
		
				
		
		
		infoXMLStr+="\n<"+jastTagsDictionary.getFieldValue("TITLE")+">"+strTitle+"</"+jastTagsDictionary.getFieldValue("TITLE")+">\n";
		
		infoXMLStr+=strAbstractTitle+"\n";
		infoXMLStr+=strAbstractText+"\n";
		
		
		infoXMLStr+=strCategoriesTitle+"\n";
		infoXMLStr+=strCategoriesText+"\n";
		
		infoXMLStr+=strGeneralTermsTitle+"\n";
		infoXMLStr+=strGeneralTermsText+"\n";
		
		infoXMLStr+=strKeywordsTitle+"\n";
		infoXMLStr+=strKeywordsText+"\n";
		

		
		
		
		infoXMLStr+=strSections;
		infoXMLStr+="\n<figures_caption>\n"+strFiguresCaption+"\n</figures_caption>\n";
		infoXMLStr+="\n<tables_caption>\n"+strTablesCaption+"\n</tables_caption>\n";
		
		infoXMLStr+=strAcknowledgementsTitle+"\n";
		infoXMLStr+=strAcknowledgementsText+"\n";
		
		infoXMLStr+=strReferencesTitle+"\n";
		infoXMLStr+=strReferencesText+"\n";
		
		
		
		infoXMLStr+=strAnnexes+"\n";
		
		infoXMLStr+="\n</root>";
		
		// #####doc.outputSettings().indentAmount(0);
		//######doc.outputSettings().outline(true);
		doc.outputSettings().prettyPrint(false);
		//doc.outputSettings().charset("UTF-8");
		//EscapeMode em= EscapeMode.extended;
		doc.outputSettings().escapeMode(em);

		
		
		contentsToParse.setResXML(infoXMLStr);
		
		
		
		
		
		contentsToParse.setResHTML(doc.outerHtml().replaceAll("\\*\\*([^;]+?);", "&$1;"));
		
		clearData();
		
		return contentsToParse;
	}

	

	
	
	
	public static boolean checkPartOfPreviousReferenceInFirstPosColumn(Element divtag, Elements divtags2, int current_index)
	{
	
		
		  
		  String divTagFirstLineClassAttributeInfoStr = divtag.attr("class");
		  DivClassCSSProperties divTagFirstLineProperties=new DivClassCSSProperties(divTagFirstLineClassAttributeInfoStr);
		  double leftValueFirstLineDivTag=(double)mapValuesCSSLeft.get(divTagFirstLineProperties.getLeft());
		  double bottomValueFirstLineDivTag =  (double)mapValuesCSSBottom.get(divTagFirstLineProperties.getBottom());
		  
		  //check if the previous divtag has some distance to the current divtag,
		  //to prevent that two divtags in the same line can indicate a false reference
		  
			
		  
		  if (verbosity)
		  {
			  System.out.println("BROKEN REFERENCE TEST: "+divtag.text());
		  }
		  
		  
			  if (numColumns==1)
			  {

				  if (firstColumnXAxisValue<leftValueFirstLineDivTag)
				  {
					  return true;
				  }
				  
			  }
			  
			  if (numColumns==2)
			  {
				  
				  double diffSecondColumnXAxisToFirstLine=Math.abs(secondColumnXAxisValue-leftValueFirstLineDivTag);
				  double diffFirstColumnXAxisToFirstLine=Math.abs(firstColumnXAxisValue-leftValueFirstLineDivTag);
				  
				  if (verbosity)
				  {
					  System.out.println("diffSecondColumnXAxisToFirstLine: "+diffSecondColumnXAxisToFirstLine);
					  System.out.println("diffFirstColumnXAxisToFirstLine: "+diffFirstColumnXAxisToFirstLine);
					  System.out.println("leftValueFirstLineDivTag: "+leftValueFirstLineDivTag);
					  System.out.println("secondColumnXAxisValue: "+secondColumnXAxisValue);
					  System.out.println("firstColumnXAxisValue: "+firstColumnXAxisValue);
					  System.out.println("maxThresholdXAxisToIndentReference: "+maxThresholdXAxisToIndentReference);
				  }
				  
				  
				  //case broken reference in 2nd column 
				  if ((secondColumnXAxisValue<leftValueFirstLineDivTag) &&
				      (diffSecondColumnXAxisToFirstLine<maxThresholdXAxisToIndentReference))
						  
				  {
					  return true;
				  }
				  
				  
				  //case broken reference in first column
				  
				  if ((secondColumnXAxisValue>leftValueFirstLineDivTag) &&
				      (firstColumnXAxisValue<leftValueFirstLineDivTag) &&
				      (diffFirstColumnXAxisToFirstLine<maxThresholdXAxisToIndentReference)
					)
				  {
					  return true;
				  }
			  }
			  
			  
			return false;
			  
	}
	

	
	
	// TODO one-line REFERENCE capture
	
	//
	// ALBERS, J. 1963. The interaction of color. Art news 62, 1.
	//
	
	// TODO CASA+ACL papers references exceptions
	
	
	// DONE special case when a reference has a page break or a column break.
	
	
	
	//checks if there is an indentation (within some thresholds  max>indentation>min)
	// in the second line (indicating that this is a reference). 
	
	
	public static boolean checkReferenceByFirstLineIndentation(Element divtag, Elements divtags,int current_index)
	{

		  int index=current_index+1; 
			
		  
		  String divTagFirstLineClassAttributeInfoStr = divtag.attr("class");
		  String divTagFilteredAttributeInfoStr= divtag.attr("filtered");
		  
		  DivClassCSSProperties divTagFirstLineProperties=new DivClassCSSProperties(divTagFirstLineClassAttributeInfoStr);
		  double leftValueFirstLineDivTag=(double)mapValuesCSSLeft.get(divTagFirstLineProperties.getLeft());
		  double bottomValueFirstLineDivTag =  (double)mapValuesCSSBottom.get(divTagFirstLineProperties.getBottom());
		  
		  
		  if (verbosity)
		  {
			  System.out.println("CHECK REFERENCE first:"+divTagFirstLineClassAttributeInfoStr);
		  }
		  
		  
		  //check if the previous divtag (another reference or reference title) 
		  // has some distance to the current divtag,
		  //to prevent that two divtags in the same line can indicate a false reference
	
		  
		  
		 if (divtags.size()>current_index-1)
		  {
			  Element previousDivTag= divtags.get(current_index-1);
			  String previousDivTagClassAttributeInfoStr = previousDivTag.attr("class");
			  String previousDivTagFilteredAttributeInfoStr =   previousDivTag.attr("filtered");

			  if (verbosity)
			  {
				  System.out.println("CHECK REFERENCE previous:"+previousDivTagClassAttributeInfoStr);
			  }
				  
			  if (previousDivTagClassAttributeInfoStr.startsWith("t ")
				  && previousDivTagFilteredAttributeInfoStr.equals("false"))
			  {
				  DivClassCSSProperties previousDivTagProperties=new DivClassCSSProperties(previousDivTagClassAttributeInfoStr);
				  double bottomValuePreviousDivTag =  (double)mapValuesCSSBottom.get(previousDivTagProperties.getBottom());
				  
				  if (Math.abs(bottomValuePreviousDivTag-bottomValueFirstLineDivTag)<maxThresholdReferenceWithinLineBottomDistance)
					 {
						 return false;
					 }
			  }
		  }
		  
		 		  
		  
		  
		  double bottomValueCurrentDivTag;
		  
		  boolean flagDiffBottomValueWithinLine=false;
		  boolean flagIsTextDiv=true;
		
		  while(divtag!=null&& divTagFirstLineClassAttributeInfoStr.startsWith("t ") && divTagFilteredAttributeInfoStr.equals("false") && divtags.size()>index)
		  {
			
			  //consume all the div tags corresponding to the first line,
			  // this case is rare to happen but we cover it.
			  // it could happen in sentences with accents where the accent creates a new div in the same
			  // first line. So we have to consume all the divs appearing in the first line
			  //  <div> Ralf Krestel.. Ren</div> <div>'</div><div>e Witte. 2008</div>
			  //
			  //   Ralf Krestel, Sabine Bergler, and René Witte. 2008.
			 // 			Minding the Source: Automatic Tagging of Re-ported Speech in Newspaper Articles. 
			  //In LREC,Marrakech, Morocco, May. European Language Re-sources Association (ELRA)
			  
			  // or this one 
			  //LÉVY, B., AND MALLET, J.-L. 1998. Non-distorted texture map-
			  // ping for sheared triangulated meshes. Proc. SIGGRAPH 98,
			  // 343–352.
			  //  
			  //
			  //
			  //
			  
			  
			  
			  while(divtags.size()>index&& (flagDiffBottomValueWithinLine==false) && (flagIsTextDiv))
			  {
				  Element nextDivTag= divtags.get(index);
				  String nextDivTagAttributeClass= nextDivTag.attr("class");
				  
				  String nextDivTagFilteredAttributeInfoStr=nextDivTag.attr("filtered");

				  if (verbosity)
				  {
					  System.out.println("CHECK REFERENCE next:"+nextDivTagAttributeClass);
				  }
				  
				  if (nextDivTagAttributeClass.startsWith("t ") &&
						  nextDivTagFilteredAttributeInfoStr.equals("false"))
				  {
				  
					  DivClassCSSProperties divTagSecondLineProperties=new DivClassCSSProperties(nextDivTagAttributeClass);
					  bottomValueCurrentDivTag =  (double)mapValuesCSSBottom.get(divTagSecondLineProperties.getBottom());
					
					 if (Math.abs(bottomValueCurrentDivTag-bottomValueFirstLineDivTag)>maxThresholdReferenceWithinLineBottomDistance)
					 {
						 flagDiffBottomValueWithinLine=true;
					 }
					 else
					 {
						 
						 index++;
					 }
				  }
				  else
				  {
					  
					  flagIsTextDiv=false;
				  }
				  
			  }
				  
				  
			  
			  Element nextDivTag= divtags.get(index);
			  String divTagSecondLineClassAttributeInfoStr = nextDivTag.attr("class");
			  String divTagSecondLineFilteredAttributeInfoStr = nextDivTag.attr("filtered");
			  
			  
			  if (divTagSecondLineClassAttributeInfoStr.startsWith("t ") &&
					  divTagSecondLineFilteredAttributeInfoStr.equals("false") )
			  {
				  DivClassCSSProperties divTagSecondLineProperties=new DivClassCSSProperties(divTagSecondLineClassAttributeInfoStr);
		  
				  
				  if (verbosity)
				  {
					  System.out.println("divTagFirstLineProperties: "+divTagFirstLineClassAttributeInfoStr);
					  System.out.println("divTagSecondLineProperties: "+divTagSecondLineClassAttributeInfoStr);
				  }
					  
				  double leftValueSecondLineDivTag=(double)mapValuesCSSLeft.get(divTagSecondLineProperties.getLeft());
				  double bottomValueSecondLineDivTag =  (double)mapValuesCSSBottom.get(divTagSecondLineProperties.getBottom());
					
				  secondLineReferenceXAxisLeftValue=leftValueSecondLineDivTag;
				  
				  if (verbosity)
				  {
					  System.out.println("leftValueFirstLineDivTag: "+leftValueFirstLineDivTag);
					  System.out.println("leftValueSecondLineDivTag: "+leftValueSecondLineDivTag);
				  }
					  
				  if ((leftValueFirstLineDivTag< leftValueSecondLineDivTag) && 
						 (leftValueSecondLineDivTag-leftValueFirstLineDivTag>minThresholdReferenceDetectByFirstLineIndent)  && 
						(leftValueSecondLineDivTag-leftValueFirstLineDivTag<maxThresholdReferenceDetectByFirstLineIndent) &&
						(Math.abs(bottomValueSecondLineDivTag-bottomValueFirstLineDivTag)>maxThresholdReferenceWithinLineBottomDistance))
				  {
					
					  if (verbosity)
					  {
						  System.out.println("leftValueSecondLineDivTagTRUE");
					  }  
						  
					  return true;
					  
				  }
				  else
				  {
					  return false;
				  }
				  
			  }
			  
			  
			  index++;
		  }
		
		return false;
	}

	
	
	  public  static void markDivTagHTMLOutput(Element divtag, String articlePartType)
	  {
		  
		  
		  String articlePartTypeColor=htmlArticlePartTypeColorsDictionary.getFieldValue(articlePartType);
		  divtag.attr("style", divtag.attr("style") + " color:"+articlePartTypeColor+";");
		  
	  }

	 
	 
	
	 public static String closeStateSection(StateSection currentStateSection, Boolean flagParagraphTagOpen)
	 {
	
		 String strSectionsClosing="";
		 
		 if (currentStateSection==StateSection.SECTION || currentStateSection==StateSection.SUBSECTION || currentStateSection==StateSection.SUBSUBSECTION)
		 {
			 if (flagParagraphTagOpen)	 { strSectionsClosing+="</"+jastTagsDictionary.getFieldValue("PARAGRAPH")+">\n";	
			 									flagParagraphTagOpen=false;
			 							  } 
			 
		 }	  
		 
		 if (currentStateSection==StateSection.SUBSUBSECTION)
		 {
			 strSectionsClosing+="</"+jastTagsDictionary.getFieldValue("SUBSUBSECTION_TEXT")+">\n";
		 } 
	
	
		 if (currentStateSection==StateSection.SUBSECTION || currentStateSection==StateSection.SUBSUBSECTION)
		 {
			 strSectionsClosing+="</"+jastTagsDictionary.getFieldValue("SUBSECTION_TEXT")+">\n";
		 }  

	
		 if (currentStateSection==StateSection.SECTION || currentStateSection==StateSection.SUBSECTION || currentStateSection==StateSection.SUBSUBSECTION)
		 {
			 
			 strSectionsClosing+="</"+jastTagsDictionary.getFieldValue("SECTION_TEXT")+">\n";
		 }  

		 
		 
		 return strSectionsClosing;
		 
	 }
	 
	 
	 
	 public static String closeStateAnnexes(StateAnnexes currentStateAnnexes)
	 {
	
		 String strAnnexesClosing="";
		 
		 if (currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
		 {
			 strAnnexesClosing+="</"+jastTagsDictionary.getFieldValue("SUBSUBANNEX_TEXT")+">\n";
		 } 
	
	
		 if (currentStateAnnexes==StateAnnexes.SUBANNEX || currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
		 {
			 strAnnexesClosing+="</"+jastTagsDictionary.getFieldValue("SUBANNEX_TEXT")+">";
		 }  

	
		 if (currentStateAnnexes==StateAnnexes.ANNEX || currentStateAnnexes==StateAnnexes.SUBANNEX || currentStateAnnexes==StateAnnexes.SUBSUBANNEX)
		 {
			 strAnnexesClosing+="</"+jastTagsDictionary.getFieldValue("ANNEX_TEXT")+">\n";
		 }  

		 return strAnnexesClosing;
		 
	 }

	
	
	  //checks if at least one of the children nodes of the current divtag has
	  // a TEXT font attribute (strPatter 
	  public static boolean childrenNodeHasTextFontAttribute(Element divtag,String strPattern_TEXT)
	  {
		   
		   for (Node divtagNode : divtag.childNodes())
		   {
			   	if (divtagNode.attr("class").contains(strPattern_TEXT))
			   	{
			   		return true;
			   	}
		   }
		   
		  return false;
	  }
	  
	  
	  
	  


	  //checks if the next line is or contains normal text
	  public static boolean  nextLineIsNormalText(Elements divtags,int current_index, String strPattern_TEXT) 
	  {
		   double bottomValueFirstDivTag=0;
		  
		  if (current_index<divtags.size()-1)
		  {
			  Element divtag = divtags.get(current_index+1);
			  String classAttributeInfoStr = divtag.attr("class");
			  String strDivTagText=divtag.text();
			  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
		  
			  bottomValueFirstDivTag=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			  
			  
			  if (classAttributeInfoStr.startsWith("t ") &&
				 (	
						 classAttributeInfoStr.contains(strPattern_TEXT) || 
						 childrenNodeHasTextFontAttribute(divtag,strPattern_TEXT)
				  ))
				  {
				  		return true;
				  }
		  }
		  
		  
		  double currentBottomValue= bottomValueFirstDivTag;
		  
		  current_index+=1;
		  
		  while ((currentBottomValue==bottomValueFirstDivTag) && (current_index<divtags.size()-1))
		  {

			  Element divtag = divtags.get(current_index+1);
			  String classAttributeInfoStr = divtag.attr("class");
			  String strDivTagText=divtag.text();
			  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
		  
			  currentBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			  
			  if (classAttributeInfoStr.startsWith("t ") &&
				 (	
						 classAttributeInfoStr.contains(strPattern_TEXT) || 
						 childrenNodeHasTextFontAttribute(divtag,strPattern_TEXT)
				  ))
				  {
				  		return true;
				  }
			
			  
			  current_index+=1;
		  }
		  
		  
		   
		  return false;
	  }
	  
	  

	  
		//When a Section title, subsection title, or subsubsection title is detected then this function 
	    // checks if this title has more lines and processes them

		private static String consume_TITLE(Element initialDivTag, Elements listDivTags,String titleType) 
		{
			
			State currentState= State.TITLE;
			
			//treat initialDivTag
			
			String strTitle="<"+jastTagsDictionary.getFieldValue(titleType)+">"+"<div id=\""+initialDivTag.attr("id")+"\">";
			
			String strTextPreviousSentenceLastWord=null;
			String strTextCurrentSentenceFirstWord;
			
			
		   String classAttributeInfoStr = initialDivTag.attr("class");
		   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	       
		    double bottomValue=0;
		   
			   
			
			
			String strDivTagInitialSentence=initialDivTag.text();
			
			StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
			String textWithoutLastWord="";
			  
			int num_tokens= tokenizer.countTokens();
			 
			int i;
			for(i=0;i<num_tokens-1;i++)
			{
			  textWithoutLastWord+=" "+tokenizer.nextToken();
			}

			  
			if (i<num_tokens)
			{
				//get last token
				String strLastWord=tokenizer.nextToken();
				  
				if (strLastWord.endsWith("-"))
				{
					strTitle+=textWithoutLastWord;
					strTextPreviousSentenceLastWord=strLastWord;
				}
			   else
				{
				   	  strTitle+=textWithoutLastWord+" "+strLastWord+"</div>\n";
					  strTextPreviousSentenceLastWord="";
				  }
			  }
			  else
			  {
				  
				  strTitle+="</div>\n";
				  strTextPreviousSentenceLastWord="";					  
			  }
			
			
			
			markDivTagHTMLOutput(initialDivTag,titleType);
			
			
			
	  	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			
	  	  String previousFontFamily=divClassProperties.getFontfamily();
	  	  String previousFontSize=divClassProperties.getFontsize();
	  	  String currentFontFamily=null;
	  	  String currentFontSize=null;
			  
	  	  Element divtag = null;
			
			iteratorGlobalElements++;
			  
			while ((currentState==State.TITLE) && (iteratorGlobalElements<listDivTags.size()))
			{
				
				divtag=listDivTags.get(iteratorGlobalElements);
				String classAttributeInfoStr2 = divtag.attr("class");
				DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			    
				
				
				if (initialDivTag.attr("class").startsWith("t "))
				  {
					bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	
					currentFontFamily=divClassProperties2.getFontfamily();
					currentFontSize=divClassProperties2.getFontsize();
					
				  }
				
				//The current div is not part of the caption.
				else
				{
					currentState=null;
					
				}
				
				
				
				
				double diffBottomValue=previousBottomValue-bottomValue;
				boolean flagDiffBottomValue=false;
				
				//The current div is not part of the caption.
				if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+7))
				  {
					  flagDiffBottomValue=true;
					  currentState=null;
					  
					  
					  
				  }
				
				//The current div is not part of the title
				if ((previousFontFamily.equals(currentFontFamily)==false) ||
					(previousFontSize.equals(currentFontSize)==false))
				  {
					  currentState=null;
				  }
				

			
				// the current div is NOT part of the figure caption.
				if (currentState==null)
				{
					
					if (strTextPreviousSentenceLastWord!="")
					{
						strTitle+=" "+strTextPreviousSentenceLastWord+"</div>";
					}
				}
				// the current div IS part of the figure caption.
				
				else
				{
					
						markDivTagHTMLOutput(divtag,titleType);
					
					  String divTagText=divtag.text();
					  tokenizer=new StringTokenizer(divTagText);
					  textWithoutLastWord="";
					  
					  
					  num_tokens= tokenizer.countTokens();
					  
					  if(num_tokens>0)
					  {
						  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
					  }
					  else
					  {
						  strTextCurrentSentenceFirstWord="";
					  }

					  
					  if (strTextPreviousSentenceLastWord!="")
					  {
						  
						  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
						  
						  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
						  {
							  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  else
						  {
							  
							  String strTextPreviousSentenceLasWordWithoutHyphen="";
							  
							  if (strTextPreviousSentenceLastWord!=null)
							  {
								  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
							  }
							  
							  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  
						  
					  }
					  else
					  {
						  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  
					  }
					  
					
					  for(i=1;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  //get last token
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strTitle+=" "+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strTitle+=textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  
					  }
					  else
					  {
						  String strLastWord=strTextCurrentSentenceFirstWord;
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strTitle+=" "+"<div id=\""+divtag.attr("id")+"\">";
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strTextPreviousSentenceLastWord="";	
							  strTitle+=textWithoutLastWord+"</div>";
						  }
						  
					  }
					  
					  
					  
					  
				  }

					
					
				previousBottomValue=bottomValue;
				previousFontFamily=currentFontFamily;
				previousFontSize=currentFontSize;
				iteratorGlobalElements++;
					
				}
				
			
			iteratorGlobalElements--;
			iteratorGlobalElements--;
			
			
			strTitle+="</"+jastTagsDictionary.getFieldValue(titleType)+">\n";
			
			return strTitle;
				
		
			
			
			
		}
	  
	  
	  
	  
	//When a caption (Figure or Table) is detected this function reads its content and stores it

	private static String consume_CAPTION(Element initialDivTag, Elements listDivTags,String captionType) 
	{
	
		State currentState= State.FIGURE_CAPTION;
		
		//treat initialDivTag
		
		String strCaption="<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+initialDivTag.attr("id")+"\">\n";
		
		String strTextPreviousSentenceLastWord=null;
		String strTextCurrentSentenceFirstWord;
		
		
	   String classAttributeInfoStr = initialDivTag.attr("class");
	   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
       
	    double bottomValue=0;
	   
		   
		
		
		String strDivTagInitialSentence=initialDivTag.text();
		
		StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
		String textWithoutLastWord="";
		  
		int num_tokens= tokenizer.countTokens();
		 
		int i;
		for(i=0;i<num_tokens-1;i++)
		{
		  textWithoutLastWord+=" "+tokenizer.nextToken();
		}

		  
		if (i<num_tokens)
		{
			//get last token
			String strLastWord=tokenizer.nextToken();
			  
			if (strLastWord.endsWith("-"))
			{
				strCaption+=textWithoutLastWord;
				strTextPreviousSentenceLastWord=strLastWord;
			}
		   else
			{
			   	  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
				  strTextPreviousSentenceLastWord="";
			  }
		  }
		  else
		  {
			  
			  strCaption+="</div>\n";
			  strTextPreviousSentenceLastWord="";					  
		  }
		
		
		
		markDivTagHTMLOutput(initialDivTag,captionType);
		
		
		
  	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
		

  	    
  	  String previousDivTagText = strDivTagInitialSentence;
		
		  
		  
  	  Element divtag = null;
		
		iteratorGlobalElements++;
		  
		while ((currentState==State.FIGURE_CAPTION) && (iteratorGlobalElements<listDivTags.size()))
		{
			
			divtag=listDivTags.get(iteratorGlobalElements);
			String classAttributeInfoStr2 = divtag.attr("class");
			DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			String filteredAttributeInfoStr2 = divtag.attr("filtered");
			
			
			
			if (divtag.attr("class").startsWith("t ") && filteredAttributeInfoStr2.equals("false"))
			  {
				bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	  
			  }
			
			//The current div is not part of the caption.
			else
			{
				currentState=null;
				
			}
			
			double diffBottomValue=previousBottomValue-bottomValue;
			boolean flagDiffBottomValue=false;
			
			//The current div is not part of the caption.
			if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+4) ||  previousDivTagText.equals(""))
			  {
				  flagDiffBottomValue=true;
				  currentState=null;
				  
				  
				  
			  }
			
			if (regexpMatcher.findRegexp("TABLE_CAPTION",divtag.text()) ||
				 regexpMatcher.findRegexp("FIGURE_CAPTION",divtag.text())	
				)
			{
				 currentState=null;
			}
			
			

		
			// the current div is NOT part of the figure caption.
			if (currentState==null)
			{
				
				if (strTextPreviousSentenceLastWord!="")
				{
					strCaption+=" "+strTextPreviousSentenceLastWord+"</div>";
				}
			}
			// the current div IS part of the figure caption.
			
			else
			{
				
				markDivTagHTMLOutput(divtag,captionType);
				
				  String divTagText=divtag.text();
				  tokenizer=new StringTokenizer(divTagText);
				  textWithoutLastWord="";
				  
				  
				  num_tokens= tokenizer.countTokens();
				  
				  if(num_tokens>0)
				  {
					  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
				  }
				  else
				  {
					  strTextCurrentSentenceFirstWord="";
				  }

				  
				  if (strTextPreviousSentenceLastWord!="")
				  {
					  
					  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
					  
					  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
					  {
						  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  }
					  else
					  {
						  
						  String strTextPreviousSentenceLasWordWithoutHyphen="";
						  
						  if (strTextPreviousSentenceLastWord!=null)
						  {
							  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
						  }
						  
						  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  }
					  
					  
				  }
				  else
				  {
					  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
				  
				  }
				  
				
				  for(i=1;i<num_tokens-1;i++)
				  {
					  textWithoutLastWord+=" "+tokenizer.nextToken();
				  }
		
				  //get last token
				  
				  if (i<num_tokens)
				  {
					  //get last token
					  String strLastWord=tokenizer.nextToken();
				  
					  if (strLastWord.endsWith("-"))
					  {
						  strCaption+=" "+textWithoutLastWord;
						  strTextPreviousSentenceLastWord=strLastWord;
					  }
					  else
					  {
						  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
						  strTextPreviousSentenceLastWord="";
					  }
				  
				  }
				  else
				  {
					  String strLastWord=strTextCurrentSentenceFirstWord;
				  
					  if (strLastWord.endsWith("-"))
					  {
						  strCaption+=" "+"<div id=\""+divtag.attr("id")+"\">";
						  strTextPreviousSentenceLastWord=strLastWord;
					  }
					  else
					  {
						  strTextPreviousSentenceLastWord="";	
						  strCaption+=textWithoutLastWord+"</div>";
					  }
					  
				  }
				  
				  
				  
				  
			  }

				
				
			previousBottomValue=bottomValue;
			previousDivTagText=divtag.text();
			iteratorGlobalElements++;
				
			}
			
		
		iteratorGlobalElements--;
		iteratorGlobalElements--;
		
		
		strCaption+="</"+jastTagsDictionary.getFieldValue(captionType)+">\n";
		
		return strCaption;
			

		
}
		

	
	
	
	
	
		// CONSUME THE ABSTRACT
		//
		// STOPS WHEN DETECTS
		// 		* KEYWORDS TITLE  OR CATEGORIES TITLE OR SECTION TITLE
	

		private static String consume_ABSTRACT(Element initialDivTag, Elements listDivTags,String captionType) 
		{
		
			State currentState= State.ABSTRACT_TEXT;
			
			//treat initialDivTag
			
			String strCaption="<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+initialDivTag.attr("id")+"\">\n";
			
			String strTextPreviousSentenceLastWord=null;
			String strTextCurrentSentenceFirstWord;
			
			
		   String classAttributeInfoStr = initialDivTag.attr("class");
		   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	       
		    double bottomValue=0;
		   
			   
			
			
			String strDivTagInitialSentence=initialDivTag.text();
			
			StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
			String textWithoutLastWord="";
			  
			int num_tokens= tokenizer.countTokens();
			 
			int i;
			for(i=0;i<num_tokens-1;i++)
			{
			  textWithoutLastWord+=" "+tokenizer.nextToken();
			}

			  
			if (i<num_tokens)
			{
				//get last token
				String strLastWord=tokenizer.nextToken();
				  
				if (strLastWord.endsWith("-"))
				{
					strCaption+=textWithoutLastWord;
					strTextPreviousSentenceLastWord=strLastWord;
				}
			   else
				{
				   	  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
					  strTextPreviousSentenceLastWord="";
				  }
			  }
			  else
			  {
				  
				  strCaption+="</div>\n";
				  strTextPreviousSentenceLastWord="";					  
			  }
			
			
			
			markDivTagHTMLOutput(initialDivTag,captionType);
			
			
			
	  	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			

			  
			  
	  	  Element divtag = null;
			
			iteratorGlobalElements++;
			  
			while ((currentState==State.ABSTRACT_TEXT) && (iteratorGlobalElements<listDivTags.size()))
			{
				
				divtag=listDivTags.get(iteratorGlobalElements);
				String classAttributeInfoStr2 = divtag.attr("class");
				DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			    
				String divTagText=divtag.text();
				
				
				if (divtag.attr("class").startsWith("t "))
				  {
					bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	  
				  }
				
				//The current div is not part of the caption.
				else
				{
					currentState=null;
					
				}
				
				double diffBottomValue=previousBottomValue-bottomValue;
				boolean flagDiffBottomValue=false;
				
				//The current div is not part of the caption.
				if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+2))
				  {
					  flagDiffBottomValue=true;
					 
					  
					  
					  
				  }
				
				
				if ((regexpMatcher.matchRegexp("CATEGORIES_TITLE",divTagText) ||
						regexpMatcher.findRegexp("CATEGORIES_TITLE_WITH_TEXT",divTagText) ||
				  regexpMatcher.matchRegexp("KEYWORDS_TITLE",divTagText) ||
				  regexpMatcher.findRegexp("KEYWORDS_TITLE_WITH_TEXT",divTagText) ||
				   regexpMatcher.findRegexp("SECTION_TITLE",divTagText)) &&
				  flagDiffBottomValue)
				{
					 currentState=null;
				}
				  
				
				
				

			
				// the current div is NOT part of the figure caption.
				if (currentState==null)
				{
					
					if (strTextPreviousSentenceLastWord!="")
					{
						strCaption+=" "+strTextPreviousSentenceLastWord+"</div>";
					}
				}
				// the current div IS part of the figure caption.
				
				else
				{
					
					markDivTagHTMLOutput(divtag,captionType);
					
					  
					  tokenizer=new StringTokenizer(divTagText);
					  textWithoutLastWord="";
					  
					  
					  num_tokens= tokenizer.countTokens();
					  
					  if(num_tokens>0)
					  {
						  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
					  }
					  else
					  {
						  strTextCurrentSentenceFirstWord="";
					  }

					  
					  if (strTextPreviousSentenceLastWord!="")
					  {
						  
						  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
						  
						  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
						  {
							  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  else
						  {
							  
							  String strTextPreviousSentenceLasWordWithoutHyphen="";
							  
							  if (strTextPreviousSentenceLastWord!=null)
							  {
								  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
							  }
							  
							  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  
						  
					  }
					  else
					  {
						  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  
					  }
					  
					
					  for(i=1;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  //get last token
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strCaption+=" "+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  
					  }
					  else
					  {
						  String strLastWord=strTextCurrentSentenceFirstWord;
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strCaption+=" "+"<div id=\""+divtag.attr("id")+"\">";
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strTextPreviousSentenceLastWord="";	
							  strCaption+=textWithoutLastWord+"</div>";
						  }
						  
					  }
					  
					  
					  
					  
				  }

					
					
				previousBottomValue=bottomValue;
				iteratorGlobalElements++;
					
				}
				
			
			iteratorGlobalElements--;
			iteratorGlobalElements--;
			
			
			strCaption+="</"+jastTagsDictionary.getFieldValue(captionType)+">\n";
			
			return strCaption;
				

			
	}
		
	
		
		// CONSUME THE ACKNOWLEDGMENTS
		//
		// STOPS WHEN DETECTS
		// 		* REFERENCES TITLE
	

		private static String consume_ACKNOWLEDGEMENTS(Element initialDivTag, Elements listDivTags,String captionType) 
		{
		
			State currentState= State.ACKNOWLEDGEMENTS_TEXT;
			
			//treat initialDivTag
			
			String strCaption="<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+initialDivTag.attr("id")+"\">\n";
			
			String strTextPreviousSentenceLastWord=null;
			String strTextCurrentSentenceFirstWord;
			
			
		   String classAttributeInfoStr = initialDivTag.attr("class");
		   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	       
		    double bottomValue=0;
		   
			   
			
			
			String strDivTagInitialSentence=initialDivTag.text();
			
			StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
			String textWithoutLastWord="";
			  
			int num_tokens= tokenizer.countTokens();
			 
			int i;
			for(i=0;i<num_tokens-1;i++)
			{
			  textWithoutLastWord+=" "+tokenizer.nextToken();
			}

			  
			if (i<num_tokens)
			{
				//get last token
				String strLastWord=tokenizer.nextToken();
				  
				if (strLastWord.endsWith("-"))
				{
					strCaption+=textWithoutLastWord;
					strTextPreviousSentenceLastWord=strLastWord;
				}
			   else
				{
				   	  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
					  strTextPreviousSentenceLastWord="";
				  }
			  }
			  else
			  {
				  
				  strCaption+="</div>\n";
				  strTextPreviousSentenceLastWord="";					  
			  }
			
			
			
			markDivTagHTMLOutput(initialDivTag,captionType);
			
			
			
	  	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			

			  
			  
	  	  Element divtag = null;
			
			iteratorGlobalElements++;
			  
			while ((currentState==State.ACKNOWLEDGEMENTS_TEXT) && (iteratorGlobalElements<listDivTags.size()))
			{
				
				divtag=listDivTags.get(iteratorGlobalElements);
				String classAttributeInfoStr2 = divtag.attr("class");
				String filteredAttributeInfoStr2 = divtag.attr("filtered");
				DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			    
				String divTagText=divtag.text();
				
				
				
				if (divtag.attr("class").startsWith("t ") && filteredAttributeInfoStr2.equals("false"))
				  {
					bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	  
				  }
				
				//The current div is not part of the caption.
				else
				{
					currentState=null;
					
				}
				
				double diffBottomValue=previousBottomValue-bottomValue;
				boolean flagDiffBottomValue=false;
				
				//The current div is not part of the caption.
				if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+2))
				  {
					  flagDiffBottomValue=true;
					 
					  
					  
					  
				  }
				
				
				if ((regexpMatcher.matchRegexp("REFERENCES_TITLE",divTagText))) 
						//flagDiffBottomValue)
				{
					 currentState=null;
				}
				  
				
				
				

			
				// the current div is NOT part of the figure caption.
				if (currentState==null)
				{
					
					if (strTextPreviousSentenceLastWord!="")
					{
						strCaption+=" "+strTextPreviousSentenceLastWord+"</div>";
					}
				}
				// the current div IS part of the figure caption.
				
				else
				{
					
					markDivTagHTMLOutput(divtag,captionType);
					
					  
					  tokenizer=new StringTokenizer(divTagText);
					  textWithoutLastWord="";
					  
					  
					  num_tokens= tokenizer.countTokens();
					  
					  if(num_tokens>0)
					  {
						  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
					  }
					  else
					  {
						  strTextCurrentSentenceFirstWord="";
					  }

					  
					  if (strTextPreviousSentenceLastWord!="")
					  {
						  
						  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
						  
						  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
						  {
							  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  else
						  {
							  
							  String strTextPreviousSentenceLasWordWithoutHyphen="";
							  
							  if (strTextPreviousSentenceLastWord!=null)
							  {
								  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
							  }
							  
							  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  
						  
					  }
					  else
					  {
						  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  
					  }
					  
					
					  for(i=1;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  //get last token
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strCaption+=" "+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  
					  }
					  else
					  {
						  String strLastWord=strTextCurrentSentenceFirstWord;
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strCaption+=" "+"<div id=\""+divtag.attr("id")+"\">";
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strTextPreviousSentenceLastWord="";	
							  strCaption+=textWithoutLastWord+"</div>";
						  }
						  
					  }
					  
					  
					  
					  
				  }

					
					
				previousBottomValue=bottomValue;
				iteratorGlobalElements++;
					
				}
				
			
			iteratorGlobalElements--;
			iteratorGlobalElements--;
			
			
			strCaption+="</"+jastTagsDictionary.getFieldValue(captionType)+">\n";
			
			return strCaption;
				

			
	}
			
		
		
		
		
	
	
	
	
	
	

	
	
	//Consume the text of a section, it can involve consuming text in several columns of the same page and other
	//following pages. 
	// it stops when the TITLE of the following SECTION is detected
	

	private static String consume_SECTION_TEXT(Element initialDivTag, Elements listDivTags,String captionType) 
	{
	
		State currentState= State.SECTION_TEXT;
		
		//treat initialDivTag
		
		String strCaption="<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+initialDivTag.attr("id")+"\">\n";
		
		String strTextPreviousSentenceLastWord=null;
		String strTextCurrentSentenceFirstWord;
		
		
	   String classAttributeInfoStr = initialDivTag.attr("class");
	   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
       
	    double bottomValue=0;
	   
		   
		
		
		String strDivTagInitialSentence=initialDivTag.text();
		
		StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
		String textWithoutLastWord="";
		  
		int num_tokens= tokenizer.countTokens();
		 
		int i;
		for(i=0;i<num_tokens-1;i++)
		{
		  textWithoutLastWord+=" "+tokenizer.nextToken();
		}

		  
		if (i<num_tokens)
		{
			//get last token
			String strLastWord=tokenizer.nextToken();
			  
			if (strLastWord.endsWith("-"))
			{
				strCaption+=textWithoutLastWord;
				strTextPreviousSentenceLastWord=strLastWord;
			}
		   else
			{
			   	  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
				  strTextPreviousSentenceLastWord="";
			  }
		  }
		  else
		  {
			  
			  strCaption+="</div>\n";
			  strTextPreviousSentenceLastWord="";					  
		  }
		
		
		
		
		
  	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
		

		  
		  
  	  Element divtag = null;
		
		iteratorGlobalElements++;
		  
		while ((currentState==State.SECTION_TEXT) && (iteratorGlobalElements<listDivTags.size()))
		{
			
			divtag=listDivTags.get(iteratorGlobalElements);
			String classAttributeInfoStr2 = divtag.attr("class");
			DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
		       
			
			if (initialDivTag.attr("class").startsWith("t "))
			  {
				bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	  
			  }
			
			//The current div is not part of the caption.
			else
			{
				currentState=null;
				
			}
			
			double diffBottomValue=previousBottomValue-bottomValue;
			boolean flagDiffBottomValue=false;
			
			//The current div is not part of the caption.
			if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+4))
			  {
				  flagDiffBottomValue=true;
				  currentState=null;
				  
				  
				  
			  }
			

		
			// the current div is NOT part of the figure caption.
			if (currentState==null)
			{
				
				if (strTextPreviousSentenceLastWord!="")
				{
					strCaption+=" "+strTextPreviousSentenceLastWord+"</div>";
				}
			}
			// the current div IS part of the figure caption.
			
			else
			{
				
				
				  String divTagText=divtag.text();
				  tokenizer=new StringTokenizer(divTagText);
				  textWithoutLastWord="";
				  
				  
				  num_tokens= tokenizer.countTokens();
				  
				  if(num_tokens>0)
				  {
					  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
				  }
				  else
				  {
					  strTextCurrentSentenceFirstWord="";
				  }

				  
				  if (strTextPreviousSentenceLastWord!="")
				  {
					  
					  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
					  
					  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
					  {
						  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  }
					  else
					  {
						  
						  String strTextPreviousSentenceLasWordWithoutHyphen="";
						  
						  if (strTextPreviousSentenceLastWord!=null)
						  {
							  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
						  }
						  
						  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  }
					  
					  
				  }
				  else
				  {
					  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
				  
				  }
				  
				
				  for(i=1;i<num_tokens-1;i++)
				  {
					  textWithoutLastWord+=" "+tokenizer.nextToken();
				  }
		
				  //get last token
				  
				  if (i<num_tokens)
				  {
					  //get last token
					  String strLastWord=tokenizer.nextToken();
				  
					  if (strLastWord.endsWith("-"))
					  {
						  strCaption+=" "+textWithoutLastWord;
						  strTextPreviousSentenceLastWord=strLastWord;
					  }
					  else
					  {
						  strCaption+=textWithoutLastWord+" "+strLastWord+"</div>\n";
						  strTextPreviousSentenceLastWord="";
					  }
				  
				  }
				  else
				  {
					  strTextPreviousSentenceLastWord="";	
					  strCaption+=textWithoutLastWord;
				  	
				  }
				  
				  
			  }

				
				
			previousBottomValue=bottomValue;
			iteratorGlobalElements++;
				
			}
			
		
		iteratorGlobalElements--;
		iteratorGlobalElements--;
		
		
		strCaption+="</"+jastTagsDictionary.getFieldValue(captionType)+">\n";
		
		return strCaption;
			

		
}
		

	
	
	
	
	
	
	//consumes a reference.

	private static String consume_REFERENCE(Element divTag,Elements listDivTags,String captionType) 
	{
	
		State currentState= State.REFERENCE;
		
		//treat initialDivTag
		
		String strReference="\n<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+divTag.attr("id")+"\">\n";
		
		String strTextPreviousSentenceLastWord=null;
		String strTextCurrentSentenceFirstWord;
		
		
	   String classAttributeInfoStr = divTag.attr("class");
	   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
       
	
	    double previousBottomValue =  previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
		
	   
	   markDivTagHTMLOutput(divTag,captionType);
		
		
		String strDivTagInitialSentence=divTag.text();
		
		StringTokenizer tokenizer=new StringTokenizer(strDivTagInitialSentence);
		String textWithoutLastWord="";
		  
		int num_tokens= tokenizer.countTokens();
		 
		int i;
		for(i=0;i<num_tokens-1;i++)
		{
		  textWithoutLastWord+=" "+tokenizer.nextToken();
		}

		  
		if (i<num_tokens)
		{
			//get last token
			String strLastWord=tokenizer.nextToken();
			  
			if (strLastWord.endsWith("-"))
			{
				strReference+=textWithoutLastWord;
				strTextPreviousSentenceLastWord=strLastWord;
			}
		   else
			{
			   strReference+=textWithoutLastWord+" "+strLastWord+"</div>\n";
				  strTextPreviousSentenceLastWord="";
			  }
		  }
		  else
		  {
			  
			  strReference+="</div>\n";
			  strTextPreviousSentenceLastWord="";					  
		  }
		
		
		

	  double currentBottomValue;
		  
  	  Element divtag = null;
		
		iteratorGlobalElements++;
		  
		while ((currentState==State.REFERENCE) && (iteratorGlobalElements<listDivTags.size()))
		{
			
			divtag=listDivTags.get(iteratorGlobalElements);
			String classAttributeInfoStr2 = divtag.attr("class");
			String filteredAttributeInfoStr  = divtag.attr("filtered");
			
			if (classAttributeInfoStr2.startsWith("t ") &&
				 filteredAttributeInfoStr.equals("false"))
					
			{
				DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			    currentBottomValue =  (double)mapValuesCSSBottom.get(divClassProperties2.getBottom());
				
				
			    
				double diffBottomValue=previousBottomValue-currentBottomValue;
				boolean flagDiffBottomValue=false;
				
				//The current div is not part of the caption.
				if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+4))
				  {
					  flagDiffBottomValue=true;
				  }
			    
			    
				
	
				 //Checks if the current line is another reference or is part of the 
				
				if ( regexpMatcher.findRegexp("REFERENCE_INDEX",divtag.text())
							||
					(checkReferenceByFirstLineIndentation(divtag,listDivTags,iteratorGlobalElements)==true
					)
							||
					flagDiffBottomValue==true)
				{
					  currentState=null;
					  
				 }
				
	
			
				// the current div is NOT part of the reference
				if (currentState==null)
				{
					
					if (strTextPreviousSentenceLastWord!="")
					{
						strReference+=" "+strTextPreviousSentenceLastWord+"</div>";
					}
				}
				// the current div IS part of the reference				
				else
				{
					
					
					markDivTagHTMLOutput(divtag,captionType);
					
					  String divTagText=divtag.text();
					  tokenizer=new StringTokenizer(divTagText);
					  textWithoutLastWord="";
					  
					  
					  num_tokens= tokenizer.countTokens();
					  
					  if(num_tokens>0)
					  {
						  strTextCurrentSentenceFirstWord=tokenizer.nextToken();
					  }
					  else
					  {
						  strTextCurrentSentenceFirstWord="";
					  }
	
					  
					  if (strTextPreviousSentenceLastWord!="")
					  {
						  
						  String strHyphenWordCandidate=strTextPreviousSentenceLastWord+strTextCurrentSentenceFirstWord;
						  
						  if (hyphenWordsDictionaryEN.checkExistsHyphenWord(strHyphenWordCandidate))
						  {
							  textWithoutLastWord=" "+strTextPreviousSentenceLastWord+"</div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  else
						  {
							  
							  String strTextPreviousSentenceLasWordWithoutHyphen="";
							  
							  if (strTextPreviousSentenceLastWord!=null)
							  {
								  strTextPreviousSentenceLasWordWithoutHyphen=strTextPreviousSentenceLastWord.substring(0, strTextPreviousSentenceLastWord.length()-1);
							  }
							  
							  textWithoutLastWord=" "+strTextPreviousSentenceLasWordWithoutHyphen+"<false-hyphen/></div>\n<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
						  }
						  
						  
					  }
					  else
					  {
						  
						  textWithoutLastWord=" "+"<div id=\""+divtag.attr("id")+"\">"+strTextCurrentSentenceFirstWord;
					  
					  }
					  
					
					  for(i=1;i<num_tokens-1;i++)
					  {
						  textWithoutLastWord+=" "+tokenizer.nextToken();
					  }
			
					  //get last token
					  
					  if (i<num_tokens)
					  {
						  //get last token
						  String strLastWord=tokenizer.nextToken();
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strReference+=" "+textWithoutLastWord;
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strReference+=textWithoutLastWord+" "+strLastWord+"</div>\n";
							  strTextPreviousSentenceLastWord="";
						  }
					  
					  }
					  else
					  {
						  String strLastWord=strTextCurrentSentenceFirstWord;
					  
						  if (strLastWord.endsWith("-"))
						  {
							  strReference+=" "+"<div id=\""+divtag.attr("id")+"\">";
							  strTextPreviousSentenceLastWord=strLastWord;
						  }
						  else
						  {
							  strTextPreviousSentenceLastWord="";	
							  strReference+=textWithoutLastWord+"</div>";
						  }
						  
					  }
					  
					  
				  }

				previousBottomValue=currentBottomValue;
				
				}	
			iteratorGlobalElements++;
			
			
			
			}
			
		
		iteratorGlobalElements--;
		iteratorGlobalElements--;
		
		
		strReference+="\n</"+jastTagsDictionary.getFieldValue(captionType)+">\n";
		
		return strReference;
		
}

	
	
	
	
	
	
	//consumes a line.

	private static String consume_LINE(Element divTag,String captionType) 
	{
	
		State currentState= State.FIGURE_CAPTION;
		
		//treat initialDivTag
		
		String strLine="<"+jastTagsDictionary.getFieldValue(captionType)+">"+"<div id=\""+divTag.attr("id")+"\">";
		strLine+=divTag.text();
		strLine+="</div></"+jastTagsDictionary.getFieldValue(captionType)+">";

		
		return strLine;
			

		
}






	public static void clearData()
	{
	
 	    mapCountsCSSHeights.clear();
 	    mapCountsCSSFontSize.clear();
 	    mapCountsCSSFontFamily.clear();
	    mapCountsCSSBottom.clear();
	    mapCountsCSSLeft.clear();
	    
	    mapValuesCSSHeights.clear();
	    mapValuesCSSFontSize.clear();
		mapValuesCSSBottom.clear();
	    mapValuesCSSLeft.clear();
				
	}





	public static String detectTITLEFontSizeAttribute(org.jsoup.nodes.Document doc )
		{
			
			String strPattern_Page="pf ";
			
			String currentBestFontSize=null;
			double currentValueBestFontSize=0;
			
			
			Elements divtagsPAGES = doc.getElementsByTag("div");
			String datapageno="0";
			
			
			for (Element divtag : divtagsPAGES) 
			{
			  
			   String classAttributeInfoStr = divtag.attr("class");
			   
			  
			 if (verbosity)
			  {
				  System.out.println("classAttributeInfoStr:"+divtag.attr("class"));
				  System.out.println("classAttributeInfoStr:--"+divtag.attr("data-page-no")+"--");
			  }
				  
				  
			  if (classAttributeInfoStr.startsWith("pf "))
			  {
				  
				  datapageno=divtag.attr("data-page-no");
			  }
			  
			 
			  // if the page class is detected then the divs inside this div are analyzed.
			  if ( datapageno.equals("1") && classAttributeInfoStr.startsWith("t "))
			  {
						
						DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
						
						
						if (verbosity)
						{
							System.out.println("BOTTOM: "+divClassProperties.getBottom());
							System.out.println("BOTTOMVALUE: "+(double)mapValuesCSSBottom.get(divClassProperties.getBottom()));
							System.out.println("FONTSIZEVALUE: "+(double)mapValuesCSSFontSize.get(divClassProperties.getFontsize()));
						}		
							
						double bottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
						double fontsizeValue=(double)mapValuesCSSFontSize.get(divClassProperties.getFontsize());
						
						if ((bottomValue> bottomValueThresholdForTITLE) &&  (fontsizeValue> currentValueBestFontSize))
						{
							currentBestFontSize=divClassProperties.getFontsize();
							currentValueBestFontSize=fontsizeValue;
							
							if (verbosity)
							{
								System.out.println("BESTVALUE: "+currentValueBestFontSize);
							}	
						}
			
			  }	
			  
				  
			}




			return currentBestFontSize;	

	
		}






		
		//STEP 1 counts some  CSS attribute labels (.h1, .h2... .ha ...)
	


	public static void countsCSSAttributeLabels(org.jsoup.nodes.Document doc )
		{
			
		
		
		Elements divtags = doc.getElementsByTag("div");
		for (Element divtag : divtags) 
		{
		  String classAttributeInfoStr = divtag.attr("class");
		 
		  
		  StringTokenizer tokenizerAttributeInfo=new StringTokenizer(classAttributeInfoStr);
		  
		  while(tokenizerAttributeInfo.hasMoreElements())
		  {
			  String attribute=tokenizerAttributeInfo.nextToken();
			  
			  if (attribute.startsWith("h"))
			  {
				  int count = mapCountsCSSHeights.containsKey(attribute) ?  mapCountsCSSHeights.get(attribute) : 0;
				  mapCountsCSSHeights.put(attribute, count+1);
			 }
			
			  if (attribute.startsWith("fs"))
			  {
				  int count = mapCountsCSSFontSize.containsKey(attribute) ?  mapCountsCSSFontSize.get(attribute) : 0;
				  mapCountsCSSFontSize.put(attribute, count+1);
			 }

			  if (attribute.startsWith("ff"))
			  {
				  int count = mapCountsCSSFontFamily.containsKey(attribute) ?  mapCountsCSSFontFamily.get(attribute) : 0;
				  mapCountsCSSFontFamily.put(attribute, count+1);
			 }
			  
			  
			 if (attribute.startsWith("y"))
			  {
				  int count = mapCountsCSSBottom.containsKey(attribute) ?  mapCountsCSSBottom.get(attribute) : 0;
				  mapCountsCSSBottom.put(attribute, count+1);
			 }
			  
			 if (attribute.startsWith("x"))
			  {
				  int count = mapCountsCSSLeft.containsKey(attribute) ?  mapCountsCSSLeft.get(attribute) : 0;
				  mapCountsCSSLeft.put(attribute, count+1);
			 }
			  
			
			 
			  
			  
		  }
		}
		
	  }




















	public static void storesCSSAttributeValues(org.jsoup.nodes.Document doc )
		{
			


		Elements styletags = doc.getElementsByTag("style");
		for (Element styletag : styletags) 
		{
		  String styletagText = styletag.text();
		 
		  
		  StringTokenizer tokenizerText=new StringTokenizer(styletagText);
		  
		  while(tokenizerText.hasMoreElements())
		  {
			  String line=tokenizerText.nextToken();
			  
			  
			  if (verbosity)
			  {
				  System.out.println("line:"+line);
			  }
			  
			  
			  //dected lines such as: 
			  // .h1{height:0.650000px;}
			  // .hb{height:468.650000px;}

			  // .fs7{font-size:31.880000px;}
			  //.fs3{font-size:35.864000px;}
			  
			  //dected lines such as: 
			  //.y9d{bottom:468.349700px;}
			  //.yfe{bottom:469.680900px;}
			  
			  if (line.startsWith(".h") || line.startsWith(".fs") || line.startsWith(".y") || line.startsWith(".x"))
			  {
				  //extract key (h1,hb.., fs7,... yfe.)
				 
				  StringTokenizer tokenizerKey=new StringTokenizer(line,"{");
				  String key=tokenizerKey.nextToken();
				  key=key.substring(1,key.length());
				  
				  //extract Value (0.650000, 468.65000,..)
				  
				  StringTokenizer tokenizerValue=new StringTokenizer(tokenizerKey.nextToken(),":");
				  
				  tokenizerValue.nextToken();
				  
				  String value=tokenizerValue.nextToken();
				  value=value.substring(0,value.length()-5);
				  
				  
				  if (line.startsWith(".h"))
				  {
					  
					  if (mapCountsCSSHeights.containsKey(key))
					  {
						  if (mapValuesCSSHeights.containsKey(key)==false)
						  {
							  mapValuesCSSHeights.put(key, Double.parseDouble(value));
						  }
					}
				  }

				  
				  
				  if (line.startsWith(".fs"))
				  {
				  
					  if (mapCountsCSSFontSize.containsKey(key))
					  {
						  if (mapValuesCSSFontSize.containsKey(key)==false)
						  {
							  mapValuesCSSFontSize.put(key, Double.parseDouble(value));
						  }
					}
					  
				  }
				  
				  
				  if (line.startsWith(".y"))
				  {
					  
					  if (mapValuesCSSBottom.containsKey(key)==false)
					  {
						  mapValuesCSSBottom.put(key, Double.parseDouble(value));
					  }
				  }

				  if (line.startsWith(".x"))
				  {
					  
					  if (mapValuesCSSLeft.containsKey(key)==false)
					  {
						  mapValuesCSSLeft.put(key, Double.parseDouble(value));
					  }
				  }
				  
					  
			  }
			  
		  }
		}





	}












	// STEP 2 B.    Detect the  left values (x-axis) of the text 
		//				X1, and X2 values (for 2 column papers)
		//				X1 for (1 column papers)
		




	public static double[] detectColumnsXAxisValue()
		{
		
	
			MapValueComparator comparatorCountsLeftValues = new MapValueComparator(mapCountsCSSLeft);

			Map<String, Integer> mapCountsCSSLeftSorted = new TreeMap<String, Integer>(comparatorCountsLeftValues);
			mapCountsCSSLeftSorted.putAll(mapCountsCSSLeft);
			
			if (verbosity)
			{
				System.out.println(mapCountsCSSLeftSorted);
			}
			
			
			Set setCountsLeft = mapCountsCSSLeftSorted.entrySet();
			Iterator iteratorCountsLeft = setCountsLeft.iterator();

			String firstAttributeLeft;
			int firstAttributeLeftCounts=0;
			double firstAttributeLeftValue = 0;
			    
			String secondAttributeLeft;
			int  secondAttributeLeftCounts=0;
			double secondAttributeLeftValue = 0;

			// get the most used left value (x-axis) 
			if (iteratorCountsLeft.hasNext()) 
			{
				Map.Entry mapEntryCounts = (Map.Entry)iteratorCountsLeft.next();
				firstAttributeLeft=(String) mapEntryCounts.getKey();
				firstAttributeLeftCounts= (int) mapEntryCounts.getValue();
				firstAttributeLeftValue= mapValuesCSSLeft.get(firstAttributeLeft);
	
			}	
			 
			// get the second most used left value (x-axis) - in case of 2 columns articles. 
			if (iteratorCountsLeft.hasNext()) 
			{
			    	Map.Entry mapEntryCounts = (Map.Entry)iteratorCountsLeft.next();
				secondAttributeLeft=(String) mapEntryCounts.getKey();
				secondAttributeLeftCounts= (int) mapEntryCounts.getValue();
				secondAttributeLeftValue= mapValuesCSSLeft.get(secondAttributeLeft);
			}	
			 		

			Double tmpfirstColumnXAxisValue;
			Double tmpsecondColumnXAxisValue=0.0;

			//check if we have a 1-column article or a 2-column article
			if (secondAttributeLeftCounts> (int)(firstAttributeLeftCounts/2))
			{
				if (secondAttributeLeftValue> firstAttributeLeftValue)
				{
					tmpfirstColumnXAxisValue=firstAttributeLeftValue;
					tmpsecondColumnXAxisValue=secondAttributeLeftValue;
				}
				else
				{
					tmpfirstColumnXAxisValue=secondAttributeLeftValue;
					tmpsecondColumnXAxisValue=firstAttributeLeftValue;
				}

			}
			//case 1 column only.
			else
			{
				tmpfirstColumnXAxisValue=firstAttributeLeftValue;
			}
		
		

			return new double[] {tmpfirstColumnXAxisValue, tmpsecondColumnXAxisValue};

		}










		// Detect the Average Y-axis distance between lines of text.



	public static int detectAverageTextYAxisDistance(org.jsoup.nodes.Document doc,String strPattern_TEXT )
		{
		

			int detectedyAxisAverageTextDiff=0;

			Map<String,Integer> mapDiffsCSSBottomValues = new HashMap<String,Integer>();

			double previousAttributeBottomValue=0;
		
			Elements divtags3 = doc.getElementsByTag("div");
			for (Element divtag : divtags3) 
			{
			  String classAttributeInfoStr = divtag.attr("class");
			  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
		       
			  
			  
			  if (classAttributeInfoStr.startsWith("t ") &&  classAttributeInfoStr.contains( strPattern_TEXT))
			  {
				  
				  double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
				  
				  if (previousAttributeBottomValue>0)
					 {
						 double  diffAttributeBottomValue=previousAttributeBottomValue-currentAttributeBottomValue;
						 
						 
						 
						 String strDiffAttributeBottomValue =  (new Long(Math.round(diffAttributeBottomValue))).toString();
						 int count = mapDiffsCSSBottomValues.containsKey(strDiffAttributeBottomValue) ?  mapDiffsCSSBottomValues.get(strDiffAttributeBottomValue) : 0;
						 mapDiffsCSSBottomValues.put(strDiffAttributeBottomValue, count+1);
					 }
				
					 previousAttributeBottomValue=currentAttributeBottomValue;
				  
				  
			  }
			  
		  
			  
			  
			  }
		

		
		  	MapValueComparator comparatorCountsLeftDiffsValues = new MapValueComparator(mapDiffsCSSBottomValues);

		    Map<String, Integer> mapCountsLeftDiffsValuesSorted = new TreeMap<String, Integer>(comparatorCountsLeftDiffsValues);
		    mapCountsLeftDiffsValuesSorted.putAll(mapDiffsCSSBottomValues);
		    
		    if (verbosity)
		    {
		    	System.out.println("Hash --- DIFFS LEFT VALUES SORTED :"+mapCountsLeftDiffsValuesSorted);
		    }
		
		
		    Set setLeftDiffsValues = mapCountsLeftDiffsValuesSorted.entrySet();
		    Iterator iteratorLeftDiffsValues= setLeftDiffsValues.iterator();

	    	String strYAxisAverageTextDiff;
	    	
	    	
		    // get the most used left value (x-axis) 
		    if (iteratorLeftDiffsValues.hasNext()) 
		{
			Map.Entry mapEntryValues = (Map.Entry)iteratorLeftDiffsValues.next();
			strYAxisAverageTextDiff=(String) mapEntryValues.getKey();
			detectedyAxisAverageTextDiff= Integer.parseInt(strYAxisAverageTextDiff);
		}	
		   


		return detectedyAxisAverageTextDiff;

	}






	public static String getMostUsedFontSizeAttributeInDivTags()
	{
		String strFontSizeAttribute=null;
		
	  	MapValueComparator comparatorCountsValues = new MapValueComparator(mapCountsCSSFontSize);

		Map<String, Integer> mapCountsCSSFontSizeSorted = new TreeMap<String, Integer>(comparatorCountsValues);
		mapCountsCSSFontSizeSorted.putAll(mapCountsCSSFontSize);
		
		if (verbosity)
		{
			System.out.println(mapCountsCSSFontSizeSorted);
		}
		
		Set setCounts = mapCountsCSSFontSizeSorted.entrySet();
		Iterator iteratorCounts = setCounts.iterator();
	        
		// get the most used font height
		if (iteratorCounts.hasNext()) 
			{
				Map.Entry mapEntryCounts = (Map.Entry)iteratorCounts.next();
				strFontSizeAttribute=(String) mapEntryCounts.getKey();
			}	

		
		return strFontSizeAttribute;

	}

	
	public static String getMostUsedFontFamilyAttributeInDivTags()
	{
		String strFontFamilyAttribute=null;
		
	  	MapValueComparator comparatorCountsValues = new MapValueComparator(mapCountsCSSFontFamily);

		Map<String, Integer> mapCountsCSSFontFamilySorted = new TreeMap<String, Integer>(comparatorCountsValues);
		mapCountsCSSFontFamilySorted.putAll(mapCountsCSSFontFamily);
		
		if (verbosity)
		{
			System.out.println(mapCountsCSSFontFamilySorted);
		}
		
		Set setCounts = mapCountsCSSFontFamilySorted.entrySet();
		Iterator iteratorCounts = setCounts.iterator();
	        
		// get the most used font height
		if (iteratorCounts.hasNext()) 
			{
				Map.Entry mapEntryCounts = (Map.Entry)iteratorCounts.next();
				strFontFamilyAttribute=(String) mapEntryCounts.getKey();
			
			}	

		
		return strFontFamilyAttribute;

	}
	
	public static String getMostUsedHeightAttributeInDivTags()
	{
		String strHeightAttribute=null;
		
	  	MapValueComparator comparatorCountsValues = new MapValueComparator(mapCountsCSSHeights);

		Map<String, Integer> mapCountsCSSHeightsSorted = new TreeMap<String, Integer>(comparatorCountsValues);
		mapCountsCSSHeightsSorted.putAll(mapCountsCSSHeights);
		
		if (verbosity)
		{
			System.out.println(mapCountsCSSHeightsSorted);
		}
		
		Set setCounts = mapCountsCSSHeightsSorted.entrySet();
		Iterator iteratorCounts = setCounts.iterator();
	        
		// get the most used font height
		if (iteratorCounts.hasNext()) 
			{
				Map.Entry mapEntryCounts = (Map.Entry)iteratorCounts.next();
				strHeightAttribute=(String) mapEntryCounts.getKey();
			
			}	

		
		return strHeightAttribute;

	}
	
	
	

	
	public static double detectFontSizeSectionTitlesWithoutNumbering(org.jsoup.nodes.Document doc, double fontSizeValueText, double fontSizeValueTitle)
	{
		double fontSizeSectionTitlesWithoutNumbering=0.0;

		

		int detectedyAxisAverageTextDiff=0;

		Map<String,Integer> mapDiffsCSSBottomValues = new HashMap<String,Integer>();

		double previousAttributeBottomValue=0;
	
		Elements divtags3 = doc.getElementsByTag("div");
		for (Element divtag : divtags3) 
		{
		  String classAttributeInfoStr = divtag.attr("class");
		  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	       
		  double fontsizeValue=(double)mapValuesCSSFontSize.get(divClassProperties.getFontsize());
		  
		  String currentDivTagText= divtag.text();
		  
		  if (classAttributeInfoStr.startsWith("t ") &&  fontsizeValue<fontSizeValueTitle && fontsizeValue>fontSizeValueText)
		  {
			  
			  double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
			  
			  if (previousAttributeBottomValue>0)
				 {
					 double  diffAttributeBottomValue=previousAttributeBottomValue-currentAttributeBottomValue;
					 
					 
					 
					 String strDiffAttributeBottomValue =  (new Long(Math.round(diffAttributeBottomValue))).toString();
					 int count = mapDiffsCSSBottomValues.containsKey(strDiffAttributeBottomValue) ?  mapDiffsCSSBottomValues.get(strDiffAttributeBottomValue) : 0;
					 mapDiffsCSSBottomValues.put(strDiffAttributeBottomValue, count+1);
				 }
			
				 previousAttributeBottomValue=currentAttributeBottomValue;
			  
			  
		  }

		}


		
		return fontSizeSectionTitlesWithoutNumbering;

	}
	
	public static void generateDivUniqueID(org.jsoup.nodes.Document doc)
	{
		int id=0;
		
		Elements divtags3 = doc.getElementsByTag("div");
		for (Element divtag : divtags3) 
		{
			divtag.attr("id",Integer.toString(id));
			id++;
		}
		
	}
	
	
	

	// we filter out (don't process) div tags with class "t " that
	
	//   * have a bottom value (y axis) smaller than < the smallest y axis of the normal text. 
	//   * have a bottom value (y axis) bigger than >  the biggest y axis of the normal text

	public static void filterOutRunningHeadsAndPageNumbers2(org.jsoup.nodes.Document doc,String fontFamilyText,String fontSizeText)
	{
		
		
		
		
		// STEP 1 find  1) the smallest y axis of the normal text and 
		//		        2) biggest y axis of the normal text
		
		

		
		
	  double currentAttributeHeightValue=0;
	  double smallestYAxisOfNormalText=99999999;
	  double biggestYAxisOfNormalText=0;
	  
		
		
	  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
				 
		
		for (Element divtag : divtagsCurrentPages )
		{
		
			String classAttributeInfoStr = divtag.attr("class");
			String filteredAttributeInfoStr = divtag.attr("filtered");
			
			if (classAttributeInfoStr.startsWith("t ") &&
				 filteredAttributeInfoStr.equals("false")) 
			  {
				  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);

				  
				  if (divClassProperties.getFontfamily().equals(fontFamilyText) &&
					  divClassProperties.getFontsize().equals(fontSizeText))
				  {

					  
						  double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
					  
					  if (currentAttributeBottomValue>biggestYAxisOfNormalText)
					  {
						  biggestYAxisOfNormalText=currentAttributeBottomValue;
					  }
					  
					  if (currentAttributeBottomValue<smallestYAxisOfNormalText)
					  {
						  smallestYAxisOfNormalText=currentAttributeBottomValue;
						  
					  }

				  }
	
			  }
		
		}
		
		
		if (verbosity)
		{
			System.out.println("biggestYAxisOfNormalText:"+biggestYAxisOfNormalText);
			System.out.println("smallestYAxisOfNormalText:"+smallestYAxisOfNormalText);
		}
		
		//STEP 2  - Mark the "div" tags of class "t " that appear before 
		// 						biggestYAxisOfNormalText
		// and below  smallestYAxisOfNormalText
		
		
		
		
		  DivClassCSSProperties divClassPropertiesParentNode;
		  double currentAttributeHeightValueParentNode=0;
		  double currentAttributeBottomValueParentNode=0;
	
		
		

		for (Element divtag : divtagsCurrentPages )
		{
		
			String classAttributeInfoStr = divtag.attr("class");
			String filteredAttributeInfoStr = divtag.attr("filtered");
			
			
			if (classAttributeInfoStr.equals("pf w0 h0"))
			{
			
				if (verbosity)
				{
					System.out.println("pf w0 h0: class: "+divtag.attr("class"));
				}
				
			   DivClassCSSProperties divClassPropertiesDivTagPage=new DivClassCSSProperties(divtag.attr("class"));

				if (verbosity)
				{
					System.out.println("pf w0 h0: getHeight: "+divClassPropertiesDivTagPage.getHeight());
				}
				
			   currentAttributeHeightValue=mapValuesCSSHeights.get(divClassPropertiesDivTagPage.getHeight());
			   
			   currentAttributeHeightValueParentNode=currentAttributeHeightValue;
			   

			}  
			
			
			
	
			
			
			
			  
			  if (classAttributeInfoStr.startsWith("t ") &&
				  filteredAttributeInfoStr.equals("false"))
			  {
				
				  
				  
				  Element divtagParent = divtag.parent();
				  String ParentClassAttributeInfoStr = divtagParent.attr("class");
				  
				  DivClassCSSProperties divClassPropertiesParent=new DivClassCSSProperties(ParentClassAttributeInfoStr);
				  currentAttributeHeightValueParentNode=mapValuesCSSHeights.get(divClassPropertiesParent.getHeight());
				  
				  if (divClassPropertiesParent.getBottom()!=null)
				  {
					  currentAttributeBottomValueParentNode=mapValuesCSSBottom.get(divClassPropertiesParent.getBottom());
				  }

				  
				  if (verbosity)
				  {
					  System.out.println("-------------------------------");
					  System.out.println("Node: "+classAttributeInfoStr );
					  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
					  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
					  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
				  }  
				  
				  
				  
				  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
				
				  double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
				  
				  
				  
				  //handling the case in which the bottom value is reference by the (parent) div tag with atribute class="c ..."
				  if ((currentAttributeHeightValueParentNode<600) && (currentAttributeHeightValueParentNode+1<currentAttributeHeightValue))
				  {
					  
					  
					  if (verbosity)
					  {
						  System.out.println("-----------------------------------\n");
						  System.out.println("classAtrr: "+classAttributeInfoStr);
						  System.out.println("currentAttributeHeightValue: "+currentAttributeHeightValue);
						  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode);
						  System.out.println("currentAttributeBottomValue: "+currentAttributeBottomValue);
						  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode);
						  System.out.println("-----------------------------------\n");
					  }
					  
					  currentAttributeBottomValue+=currentAttributeBottomValueParentNode;
				  }
				  

				  
				  
				  if (currentAttributeBottomValue>(biggestYAxisOfNormalText+biggestYAxis_OFFSET))
				  {
					  divtag.attr("filtered", "true1");
					  
					  if (verbosity)
					  {
						  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
						  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
						  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
					  }
					  
					  
				  }
				  else
				  {
					  if (currentAttributeBottomValue<(smallestYAxisOfNormalText-smallestYAxis_OFFSET))
					  {
						  divtag.attr("filtered","true2");
						
						  if (verbosity)
						  {
							  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
							  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
							  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
						  }
						  
					  }
					  else
					  {
						  divtag.attr("filtered","false");
						  
					  }
				  }

			  }

		}
		
		
		
		
		
		
		
		
}






// we filter out (don't process) div tags with class "t " that
//   * have a bottom value (y axis) smaller than < minValueThresholdFilterRunningHeads
//   * have a bottom value (y axis) bigger than >  maxValueThresholdFilterRunningHeads

public static void filterOutRunningHeadsAndPageNumbers(org.jsoup.nodes.Document doc)
{
	
  double currentAttributeHeightValue=0;
  double currentMaxValuePxThresholdFilterRunningHeadsInPx=0;
  double currentMinValuePxThresholdFilterRunningHeadsInPx=0;
	
	
	
  Elements divtagsCurrentPages = doc.getElementsByTag("div");
  
  double currentAttributeHeightValueParentNode=0;
  double currentAttributeBottomValueParentNode=0;

  
  
  
	
	for (Element divtag : divtagsCurrentPages )
	{
		
		
		
	
		String classAttributeInfoStr = divtag.attr("class");
		
		if (classAttributeInfoStr.equals("pf w0 h0"))
		{
		
			
			if (verbosity)
			{
				System.out.println("pf w0 h0: class: "+divtag.attr("class"));
			}
			
			DivClassCSSProperties divClassPropertiesDivTagPage=new DivClassCSSProperties(divtag.attr("class"));

			if (verbosity)
			{			
				System.out.println("pf w0 h0: getHeight: "+divClassPropertiesDivTagPage.getHeight());
			} 
			
		   currentAttributeHeightValue=mapValuesCSSHeights.get(divClassPropertiesDivTagPage.getHeight());
		   
		   if (verbosity)
		   {
			   System.out.println("currentAttributeHeightValue:"+currentAttributeHeightValue);
		   }
			
			 currentMaxValuePxThresholdFilterRunningHeadsInPx=currentAttributeHeightValue*maxValuePagePxPercentThresholdFilterRunningHeads;
			
			if (verbosity)
			{
			 System.out.println("currentMaxValuePxThresholdFilterRunningHeadsInPx:"+currentMaxValuePxThresholdFilterRunningHeadsInPx);
			} 
			 currentMinValuePxThresholdFilterRunningHeadsInPx=currentAttributeHeightValue*minValuePagePxPercentThresholdFilterRunningHeads;
			
			if (verbosity)
			 {
				 System.out.println("currentMinValuePxThresholdFilterRunningHeadsInPx:"+currentMinValuePxThresholdFilterRunningHeadsInPx);
			 }
			
			
			
			currentAttributeHeightValueParentNode=currentAttributeHeightValue;
				 
			 
		}
		
		
	
		  
		  
		  if (classAttributeInfoStr.startsWith("t "))
		  {
			  
			  
			  Element divtagParent = divtag.parent();
			  String ParentClassAttributeInfoStr = divtagParent.attr("class");
			  
			  DivClassCSSProperties divClassPropertiesParent=new DivClassCSSProperties(ParentClassAttributeInfoStr);
			  currentAttributeHeightValueParentNode=mapValuesCSSHeights.get(divClassPropertiesParent.getHeight());
			  
			  if (divClassPropertiesParent.getBottom()!=null)
			  {
				  currentAttributeBottomValueParentNode=mapValuesCSSBottom.get(divClassPropertiesParent.getBottom());
			  }

			  if (verbosity)
			  {
				  System.out.println("------------------------------------");
				  System.out.println("Node: "+classAttributeInfoStr );
				  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
				  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
				  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
				  
			  }
			  
			  
			  
			  DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
			  
			  
			  
			  
			  
			
			  double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
			  
			  
			  
			  
			  
			  
			  //handling the case in which the bottom value is reference by the (parent) div tag with atribute class="c ..."
			  if ((currentAttributeHeightValueParentNode<600) && (currentAttributeHeightValueParentNode+1<currentAttributeHeightValue))
			  {
				  currentAttributeBottomValue+=currentAttributeBottomValueParentNode;
			  }
			  
			  
			  
			  
			  if (currentAttributeBottomValue>currentMaxValuePxThresholdFilterRunningHeadsInPx)
			  {
				  divtag.attr("filtered", "true3");
				  
					if (verbosity)
					{
						
						  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
						  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
						  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
						  
						System.out.println("filtered true3: "+classAttributeInfoStr);
					}	
			  }
			  else
			  {
				  if (currentAttributeBottomValue<currentMinValuePxThresholdFilterRunningHeadsInPx)
				  {
					  divtag.attr("filtered","true4");

					  if (verbosity)
						{	
							
							  System.out.println("ParentNode: "+ParentClassAttributeInfoStr );
							  System.out.println("currentAttributeHeightValueParentNode: "+currentAttributeHeightValueParentNode );
							  System.out.println("currentAttributeBottomValueParentNode: "+currentAttributeBottomValueParentNode );
							  
							System.out.println("filtered true4: "+classAttributeInfoStr);
						}
				  }
				  else
				  {
					  divtag.attr("filtered","false");
					  
				  }
			  }

		  }

	}
	
}




public static void filterOutFootnotes(org.jsoup.nodes.Document doc,int numColumns,String fontFamilyText,String fontSizeText)
{
	
	  double fontSizeTextValue= mapValuesCSSFontSize.get(fontSizeText);
	
	  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
	  int i=0;	 
		
	  while(i<divtagsCurrentPages.size())
		{
			Element divtag=divtagsCurrentPages.get(i);
				
			if (i+1<divtagsCurrentPages.size())
			{
				Element divtagNext=divtagsCurrentPages.get(i+1);
			
				
				//if a footnote is detected then read all the footnote lines and
				//filter it
				if (detectFootNoteStart(divtag,divtagNext,fontSizeTextValue))
					{
					
						if (verbosity)
						{
							System.out.println("detected footnote: "+divtag.text()+ ".." + divtagNext.text());
						}
						
						
						//consume footnote and modify current index with the divs processed
					
						i=consume_FOOTNOTE(divtag,divtagNext,divtagsCurrentPages,fontSizeTextValue,i);
					
					}
				else
					{
						i++;
					}
				
			}
			else
			{
				i++;
			}
		}
			
		
}




public static boolean detectFootNoteStart(Element divtagIndexNumber, Element divtagTextFirstLine,double fontSizeTextValue)
{
	
	String classAttributeInfoStr = divtagIndexNumber.attr("class");
	String filteredAttributeInfoStr = divtagIndexNumber.attr("filtered");
	
	if (classAttributeInfoStr.startsWith("t ") &&
		 filteredAttributeInfoStr.equals("false"))
	{

		String classAttributeInfoStr2 = divtagTextFirstLine.attr("class");
		String filteredAttributeInfoStr2 = divtagTextFirstLine.attr("filtered");
			
		
		if (classAttributeInfoStr2.startsWith("t ") &&
			 filteredAttributeInfoStr2.equals("false")) 
		{
			
			DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
			DivClassCSSProperties divClassPropertiesNext=new DivClassCSSProperties(classAttributeInfoStr2);
			
			double currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
			double currentAttributeBottomValueNext=mapValuesCSSBottom.get(divClassPropertiesNext.getBottom());
			
			double xAxisDivTagIndexNumber=mapValuesCSSLeft.get(divClassProperties.getLeft());
			
			double currentAttributeFontSizeValue=mapValuesCSSFontSize.get(divClassProperties.getFontsize());
			double currentAttributeFontSizeValueNext=mapValuesCSSFontSize.get(divClassPropertiesNext.getFontsize());
			
			String textDivTag=divtagIndexNumber.text();
			
			double diffYAxisIndexNumberFirstLine=Math.abs(currentAttributeBottomValue-currentAttributeBottomValueNext);
			
			//conditions to detect a footnote
			
			//* first <div>  is a number with 
			//*  first div has font size smaller than the  font size of main text
			// * second <div> is in the same line of the first div (there is a threshold )
			//  because first div is a super index.
			//*  second div has font size smaller than the  font size of main text
			
			//first <div> Left CSS value (X Axis) is (almost) the same of the first column Xaxis or the secong colum Xaxis 

			// if the conditions match then we read until we detect the next footnote.
			
			
			
			
			
			
			
			if (verbosity)
			   {
					System.out.println("textDivTag:--"+textDivTag+"--");
			   }
			
			if ( textDivTag.matches("^\\d+$") &&
				  (
				    (Math.abs(firstColumnXAxisValue-xAxisDivTagIndexNumber)<maxOFFSETxAxisIndexFootnote) ||
				 	(Math.abs(secondColumnXAxisValue-xAxisDivTagIndexNumber)<maxOFFSETxAxisIndexFootnote)
				 	)  &&
				 currentAttributeFontSizeValue< fontSizeTextValue &&
				 diffYAxisIndexNumberFirstLine< maxThresholdYAxisFootnoteIndex &&
				 currentAttributeFontSizeValueNext< fontSizeTextValue)
			{
				
				if (verbosity)
				{
						System.out.println("firstColumnXAxisValue: "+firstColumnXAxisValue );
				
						System.out.println("secondColumnXAxisValue: "+secondColumnXAxisValue );
						System.out.println("xAxisDivTagIndexNumber: "+xAxisDivTagIndexNumber );
				}
				
				return true;
			}
					
		}
	}
	
	return false;
	
	
	
	
}



public static int consume_FOOTNOTE(Element divtagIndexNumber, Element divtagTextFirstLine,Elements divtagsPages, double fontSizeTextValue,int current_index)
{
	int new_index=current_index+2;
	
	
	divtagIndexNumber.attr("filtered","filtered_footnote");
	divtagTextFirstLine.attr("filtered","filtered_footnote");
	
	String classAttributeInfoStrFirstLine = divtagTextFirstLine.attr("class");
	DivClassCSSProperties divClassPropertiesFirstLine=new DivClassCSSProperties(classAttributeInfoStrFirstLine);
	
	double currentAttributeFontSizeValueFirstLine=mapValuesCSSFontSize.get(divClassPropertiesFirstLine.getFontsize());
	
	
	
	while( new_index<divtagsPages.size()-2)
		{
		
			//conditions to detect a new footnote or the end of the footnotes
		
			// * the conditions of the method detectFootNoteStart(...)
			// * or the font size detected is equal to the first line of the footnote
		
			Element divtagNewFootnote1=divtagsPages.get(new_index);
			Element divtagNewFootnote2=divtagsPages.get(new_index+1);
			
			
			
			String classAttributeInfoStr= divtagNewFootnote1.attr("class");
			
			DivClassCSSProperties divClassProperties1=new DivClassCSSProperties(classAttributeInfoStr);
			
			String filteredAttributeInfoStr=divtagNewFootnote1.attr("filtered");
			
			if (classAttributeInfoStr.startsWith("t ") &&
					 filteredAttributeInfoStr.equals("false")) 
			{
				double currentAttributeFontSizeValue1=mapValuesCSSFontSize.get(divClassProperties1.getFontsize());
				
			
				if (detectFootNoteStart(divtagNewFootnote1,divtagNewFootnote2,fontSizeTextValue))
				{
					return new_index;
				}
				
				
				if  (currentAttributeFontSizeValueFirstLine!=currentAttributeFontSizeValue1)
				{
					return new_index;
				}
				
				//divtag detected as a part of the current footnote
				divtagNewFootnote1.attr("filtered","true6");

				
			}
			
				
			new_index++;
		}
	


	return new_index;
}



public static void filterOutTableContents(org.jsoup.nodes.Document doc,int numColumns,String fontFamilyText,String fontSizeText,String strMostUsedHeightAttribute)
{
	
	  double fontSizeTextValue= mapValuesCSSFontSize.get(fontSizeText);
	
	  
	  
	  // computes the average number of chars in a section text line.
	  
	  
	  double averageNumCharsSectionTextLine=computeAverageNumCharsSectionTextLine(doc, numColumns, fontFamilyText, fontSizeText);
	  
	  if (verbosity)
	   {
		  System.out.println("averageNumCharsSectionTextLine:"+averageNumCharsSectionTextLine);
	   }
	
	  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
	  int i=0;	 
		
	  while(i<divtagsCurrentPages.size())
		{
			Element divtag=divtagsCurrentPages.get(i);
			String classAttributeInfoStr = divtag.attr("class");
			DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr);
		    
			
			//Table caption detected
			if (classAttributeInfoStr.startsWith("t ") &&
					(
							regexpMatcher.findRegexp("TABLE_CAPTION",divtag.text())
											||
							regexpMatcher.findRegexp("FIGURE_CAPTION",divtag.text())
					)
					
				)
			  {
					
					
				  if (verbosity)
				  {
					  	System.out.println("TABLE caption text: "+ divtag.text());
				  }
				
					//now we have to 
					// 1. detect the table contents below or above the table caption and  mark the filtered attribute
				
					
					if (checkTableContentsAboveTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine, strMostUsedHeightAttribute))
					{
						
						filterTableContentsAboveTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine, strMostUsedHeightAttribute);
						
						if (verbosity)
						   {
								System.out.println("filterTableContentsAboveTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine))");
						   }
					}	
					
					
					else
					{
					
						if (checkTableContentsBelowTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine, strMostUsedHeightAttribute))
						{
							
							filterTableContentsBelowTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine, strMostUsedHeightAttribute);
							
							
							if (verbosity)
							   {
									System.out.println("filterTableContentsBelowTableCaption(doc, i, numColumns, fontFamilyText, fontSizeText,averageNumCharsSectionTextLine))");
							   }
							   
						}
					
					}		
						
					// TODO 2. mark the table contents as filtered in the HTML output with colors
					
					// 3. actualize the index i
					
				
				
					i++;
				
				
				
					
			  }
			else
			{
				i++;
			}
	
		}
			
		
}


	private static double computeAverageNumCharsSectionTextLine(org.jsoup.nodes.Document doc, int numColumns, String fontFamilyText, String fontSizeText)
	{
		
		
			double averageNumCharsSectionTextLine=0;
			double numCharsLine=0;
			double numLinesText=0;
		
			
			  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
			  int i=0;	 
				
			  while(i<divtagsCurrentPages.size())
				{
					Element divtag=divtagsCurrentPages.get(i);
					String classAttributeInfoStr = divtag.attr("class");
					String filteredAttributeInfoStr = divtag.attr("filtered");
					DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
				    
					if (classAttributeInfoStr.startsWith("t ") && 
								filteredAttributeInfoStr.equals("false") &&
								divClassProperties.getFontsize().equals(fontSizeText) &&
								divClassProperties.getFontfamily().equals(fontFamilyText))
					  {	
							
							numCharsLine += (double) divtag.text().length();
							numLinesText+=1;
						
							i++;
					  }
					else
					{
						i++;
					}
			
				}

			  
			  averageNumCharsSectionTextLine=numCharsLine/numLinesText;
			  
			 return averageNumCharsSectionTextLine;

	}
	
	

	private static void filterTableContentsBelowTableCaption(org.jsoup.nodes.Document doc, int currentIndexDiv, int numColumns, String fontFamilyText, String fontSizeText, double averageNumCharsSectionTextLine, String strMostUsedHeightAttribute)
	{
		
			double numCharsLine=0;
			
			  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
			  boolean flagEvent=false;


				  

			  //it is necessary to consume the complete TABLE or FIGURE CAPTION to detect when to star to check for normal content.						 
			  int indexLastDivTagCaption  = consume_CAPTION_forTableContentsFiltering(doc, currentIndexDiv ); 
			  
			  int j=indexLastDivTagCaption+1;

			  
			  Element divtagBelowCaption=divtagsCurrentPages.get(j);
			  String classAttributeInfoCaptionStr = divtagBelowCaption.attr("class");
			  String filteredAttributeInfoCaptionStr = divtagBelowCaption.attr("filtered");
			  DivClassCSSProperties divClassPropertiesCaption=new DivClassCSSProperties(classAttributeInfoCaptionStr);
			  double previousAttributeBottomValue=0;
			  
			  

			  
			  
				if (classAttributeInfoCaptionStr.startsWith("t ") && 
						filteredAttributeInfoCaptionStr.equals("false")
						)
				{
						previousAttributeBottomValue=mapValuesCSSBottom.get(divClassPropertiesCaption.getBottom());
						
						
						 numCharsLine=(double)divtagBelowCaption.text().length();
						 
							//case next line is a text line. then we STOP to filter
							if	(divClassPropertiesCaption.getFontsize().equals(fontSizeText) &&
									divClassPropertiesCaption.getFontfamily().equals(fontFamilyText) &&
								  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<6
								  			||
							  			strMostUsedHeightAttribute.equals(divClassPropertiesCaption.getHeight())
								  	)			
								)

								  {	
										if (verbosity)
										   {
												System.out.println("filter above true:"+divtagBelowCaption.text());
										   }	
										flagEvent=true;
						  		}
							else
							{
								divtagBelowCaption.attr("filtered","filtered_table_contents");
							}		

				}
				
				else
				{
					
					return;
				}

			  

			  
			  j++;
			  
			  double currentAttributeBottomValue=0;
			  
			  //we look 
			  while(j<divtagsCurrentPages.size() && flagEvent==false)
				{
					Element divtag=divtagsCurrentPages.get(j);
					String classAttributeInfoStr = divtag.attr("class");
					String filteredAttributeInfoStr = divtag.attr("filtered");

					DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
				    
					  

				    
					if (classAttributeInfoStr.startsWith("t ") && 
							filteredAttributeInfoStr.equals("false")
							)
					{
							

							currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
						
							numCharsLine=(double)divtag.text().length();
											
							
							//case next line is a text line. then we STOP to filter
							if	(divClassProperties.getFontsize().equals(fontSizeText) &&
								 divClassProperties.getFontfamily().equals(fontFamilyText) &&
								  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<5
								  			||
							  			strMostUsedHeightAttribute.equals(divClassProperties.getHeight())
								  	)			
								)
								  {	
										
										if (verbosity)
										   {
												System.out.println("filter above true:"+divtag.text());
										   }	
										flagEvent=true;
						  		}
							
							
							// case next line is a page break or column break. then STOP to filter
							// TODO  this condition needs revision 		
							if (previousAttributeBottomValue<currentAttributeBottomValue+1)
							{
									flagEvent=true;
							}
							  
							
							  
							if (Math.abs(previousAttributeBottomValue-currentAttributeBottomValue)>30)
							{
									flagEvent=true;
							}

							

						}
					else
					{
						flagEvent=true;
						
					}
					
					//case div tag that is not textual
					if (flagEvent==false)
					{
						divtag.attr("filtered","filtered_table_contents");
					}

				
					j++;	
					
					previousAttributeBottomValue=currentAttributeBottomValue;
					
				}


	}
	
	
	

	private static void filterTableContentsAboveTableCaption(org.jsoup.nodes.Document doc, int currentIndexDiv,int numColumns, String fontFamilyText, String fontSizeText, double averageNumCharsSectionTextLine, String strMostUsedHeightAttribute)
	
	{
		
			double numCharsLine=0;
			
			  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
			  int j=currentIndexDiv-1;
			  boolean flagEvent=false;
			  boolean flagTableContentsAboveTableCaption=false;

			  Element divtagAboveCaption=divtagsCurrentPages.get(j);
			  String classAttributeInfoCaptionStr = divtagAboveCaption.attr("class");
			  String filteredAttributeInfoCaptionStr = divtagAboveCaption.attr("filtered");
			  DivClassCSSProperties divClassPropertiesCaption=new DivClassCSSProperties(classAttributeInfoCaptionStr);
			  double previousAttributeBottomValue=0;
			  
			  
			    
				if (classAttributeInfoCaptionStr.startsWith("t ") && 
						filteredAttributeInfoCaptionStr.equals("false")
						)
				{
					 previousAttributeBottomValue=mapValuesCSSBottom.get(divClassPropertiesCaption.getBottom());
					 
					 numCharsLine=(double)divtagAboveCaption.text().length();
					 
						//case next line is a text line. then we STOP to filter
						if	(divClassPropertiesCaption.getFontsize().equals(fontSizeText) &&
								divClassPropertiesCaption.getFontfamily().equals(fontFamilyText) &&
							  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<6
							  			||
						  			strMostUsedHeightAttribute.equals(divClassPropertiesCaption.getHeight())
							  	)			
							)

							  {	
									if (verbosity)
									   {
											System.out.println("filter above true:"+divtagAboveCaption.text());
									   }	
									flagEvent=true;
					  		}
						else
						{
								divtagAboveCaption.attr("filtered","filtered_table_contents");
						}		
				}
				
				else
				{
					
					return;
				}


				
			  double currentAttributeBottomValue=0;
			  
			  j--;
			  //we look 
			  while(j<divtagsCurrentPages.size() && flagEvent==false)
				{
				  	
					Element divtag=divtagsCurrentPages.get(j);
					String classAttributeInfoStr = divtag.attr("class");
					String filteredAttributeInfoStr = divtag.attr("filtered");

					DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
				    
					  

				    
					if (classAttributeInfoStr.startsWith("t ") && 
							filteredAttributeInfoStr.equals("false")
							)
					{
							
						
							currentAttributeBottomValue=mapValuesCSSBottom.get(divClassProperties.getBottom());
													
							numCharsLine=(double)divtag.text().length();
											
							
							//case next line is a text line. then we STOP to filter
							if	(divClassProperties.getFontsize().equals(fontSizeText) &&
								 divClassProperties.getFontfamily().equals(fontFamilyText) &&
								  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<6
								  			||
							  			strMostUsedHeightAttribute.equals(divClassProperties.getHeight())
								  	)			
								)

								  {	
										if (verbosity)
										   {
												System.out.println("filter above true:"+divtag.text());
										   }	
										flagEvent=true;
						  		}
							
							
							// case next line is a page break or column break. then STOP to filter
							// TODO this condition needs revision
							  
							if (previousAttributeBottomValue>currentAttributeBottomValue+1)
							{
									flagEvent=true;
								
							}
							  
							
							// case next line is a a certain threshold distance 
							  
							if (Math.abs(previousAttributeBottomValue-currentAttributeBottomValue)>30)
							{
									flagEvent=true;
							}


							
						 }
					
					
					//case div tag that is not textual
					else
					{
						flagEvent=true;
					}
					
					
					if (flagEvent==false)
					{
						divtag.attr("filtered","filtered_table_contents");
					}
					
					
					previousAttributeBottomValue=currentAttributeBottomValue;
				
					j--;	
					
				}


	}
	
	
	
	
	
	
		
	
	
	

	private static boolean checkTableContentsAboveTableCaption(org.jsoup.nodes.Document doc, int currentIndexDiv,int numColumns, String fontFamilyText, String fontSizeText, double averageNumCharsSectionTextLine, String strMostUsedHeightAttribute)
	{
		
			double numCharsLine=0;
			
			  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
			  
			 
			  boolean flagEvent=false;
			  int numExploredDivsBelow=0;
			  boolean flagTableContentsAboveTableCaption=false;
			  
			  
			  
			  //it is necessary to consume the complete TABLE or FIGURE CAPTION to detect when to star to check for normal content.
			  
			  
			  int indexLastDivTagCaption  = consume_CAPTION_forTableContentsFiltering(doc, currentIndexDiv ); 
				
			  
			  
			  int j=indexLastDivTagCaption+1;
			  
				
			  //we look 5 divs above and below the caption 
			  // if one of the 5 divs above is a line of text then whe considere that the table contents is below
			  while(j<divtagsCurrentPages.size() && (numExploredDivsBelow<5 ) && flagEvent==false)
				{
					Element divtag=divtagsCurrentPages.get(j);
					String classAttributeInfoStr = divtag.attr("class");
					String filteredAttributeInfoStr = divtag.attr("filtered");
					DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
					    
						if (classAttributeInfoStr.startsWith("t ") && 
							filteredAttributeInfoStr.equals("false")
							)
						{
							
							numExploredDivsBelow++;
							
							numCharsLine=(double)divtag.text().length();
							
							if (verbosity)
							{
								System.out.println("numcharslin-avgnumcharssection:"+numCharsLine+" -- "+averageNumCharsSectionTextLine);
							}
											
							if	(divClassProperties.getFontsize().equals(fontSizeText) &&
								 divClassProperties.getFontfamily().equals(fontFamilyText) &&
								  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<5
								  			||
							  			strMostUsedHeightAttribute.equals(divClassProperties.getHeight())
								  	)			
								)

								  {	
										flagTableContentsAboveTableCaption=true;
										
										if (verbosity)
										   {
												System.out.println("contents above true:"+divtag.text());
										   }	
										flagEvent=true;
						  		}
						  }
					j++;	
				}

			  
			  return flagTableContentsAboveTableCaption;

	}
	
	

	

	private static boolean checkTableContentsBelowTableCaption(org.jsoup.nodes.Document doc, int currentIndexDiv,int numColumns, String fontFamilyText, String fontSizeText, double averageNumCharsSectionTextLine, String strMostUsedHeightAttribute)
	{
		
			double numCharsLine=0;
			
			  Elements divtagsCurrentPages = doc.getElementsByTag("div");
				
			  int i=currentIndexDiv-1;
			  boolean flagEvent=false;
			  int numExploredDivsAbove=0;
			  boolean flagTableContentsBelowTableCaption=false;
				
			  //we look 5 divs above and below the caption 
			  // if one of the 5 divs above is a line of text then whe considere that the table contents is below
			  while(i>0 && (numExploredDivsAbove<5) && flagEvent==false)
				{
					Element divtag=divtagsCurrentPages.get(i);
					String classAttributeInfoStr = divtag.attr("class");
					String filteredAttributeInfoStr = divtag.attr("filtered");
					DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
				    
					if (classAttributeInfoStr.startsWith("t ") && 
						filteredAttributeInfoStr.equals("false")
						)
					{
						
						numExploredDivsAbove++;
						
						numCharsLine=(double)divtag.text().length();
										
						if	(divClassProperties.getFontsize().equals(fontSizeText) &&
							 divClassProperties.getFontfamily().equals(fontFamilyText) &&
							  	( Math.abs(numCharsLine-averageNumCharsSectionTextLine)<5
							  			||
						  			strMostUsedHeightAttribute.equals(divClassProperties.getHeight())
							  	)			
							)

							  {	
									flagTableContentsBelowTableCaption=true;
									
									if (verbosity)
									   {
											System.out.println("contents below true:"+divtag.text());
									   }
									flagEvent=true;
					  		}
					  }
						i--;
						
						
						
			
				}

			  
			  return flagTableContentsBelowTableCaption;

	}
	
	
	
	
	
	//Consumes a caption for the filtering contents of FIGURES and TABLES

	private static int consume_CAPTION_forTableContentsFiltering(org.jsoup.nodes.Document doc, int currentIndexDiv) 
	{
	
		
		int indexLastDivTagCaption=currentIndexDiv;
		
		State currentState = State.FIGURE_CAPTION;
		
		Elements divtagsCurrentPages = doc.getElementsByTag("div");
		
		
		Element initialDivTag=divtagsCurrentPages.get(indexLastDivTagCaption);
		
	   String classAttributeInfoStr = initialDivTag.attr("class");
	   String filteredAttributeInfoStr = initialDivTag.attr("filtered");
	   DivClassCSSProperties divClassProperties=new DivClassCSSProperties(classAttributeInfoStr);
	   double previousBottomValue=0;

		  
			if (classAttributeInfoStr.startsWith("t ") && 
					filteredAttributeInfoStr.equals("false")
					)
			{
				previousBottomValue=(double)mapValuesCSSBottom.get(divClassProperties.getBottom());
			}
			
			else
			{
				return indexLastDivTagCaption;
			}

	   
	   
		String strDivTagInitialSentence=initialDivTag.text();
  	    
		

  	    
  	  String previousDivTagText = strDivTagInitialSentence;
		
	  double bottomValue=0;	  
		  
  	  Element divtag = null;
		
  	  	indexLastDivTagCaption++;
		  
		while ((currentState==State.FIGURE_CAPTION) && (indexLastDivTagCaption<divtagsCurrentPages.size()))
		{
			
			
			divtag=divtagsCurrentPages.get(indexLastDivTagCaption);
			
			
			String classAttributeInfoStr2 = divtag.attr("class");
			DivClassCSSProperties divClassProperties2=new DivClassCSSProperties(classAttributeInfoStr2);
			String filteredAttributeInfoStr2 = divtag.attr("filtered");
			
			
			
			if (divtag.attr("class").startsWith("t ") && filteredAttributeInfoStr2.equals("false"))
			  {
				bottomValue=(double)mapValuesCSSBottom.get(divClassProperties2.getBottom());	  
			  }
			
			//The current div is not part of the caption.
			else
			{
				currentState=null;
				
			}
			
			double diffBottomValue=previousBottomValue-bottomValue;
			boolean flagDiffBottomValue=false;
			
			//The current div is not part of the caption.
			if (Math.abs(diffBottomValue)>(yAxisAverageTextDiff+4))
			  {
				  flagDiffBottomValue=true;
				  currentState=null;
				  
				  
				  
			  }
			
			
			// the current div is NOT part of the figure caption.
			if (regexpMatcher.findRegexp("TABLE_CAPTION",divtag.text()) ||
				 regexpMatcher.findRegexp("FIGURE_CAPTION",divtag.text())	
				)
			{
				 currentState=null;
			}

				
			previousBottomValue=bottomValue;
			indexLastDivTagCaption++;
				
		}
			

		
	
		return indexLastDivTagCaption;
			

		
}
	
	
	
	
	
}

