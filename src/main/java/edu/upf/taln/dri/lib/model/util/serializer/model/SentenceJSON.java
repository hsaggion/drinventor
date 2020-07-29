package edu.upf.taln.dri.lib.model.util.serializer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.CitationMarker;
import edu.upf.taln.dri.lib.model.ext.RhetoricalClassENUM;
import edu.upf.taln.dri.lib.model.ext.Token;

public class SentenceJSON {

	private Integer id;
	private boolean isAbstract;
	private Integer abstractId;
	private Integer globalOrderNumber;
	
	// PLAIN TEXT:
	private String text;
	
	private String language;
	
	// SECTION:
	private Integer sect_id;
	private String sect_name;
	private String sect_rootName;
	private Integer sect_nastingLevel;
	private String sect_language;
	
	// TOKEN IDs:
	private List<Token> tokens;
	
	// RHETORICAL CLASSIFICATION:
	private String rhetoricalClass;
	
	// BABELNET SYNSTETS:
	private List<BabelSynsetOcc> babelSynsetsOcc;
	
	// CITATIONS:
	private List<CitationMarker> citationMarkers;
	
	// ACKNOWLEDGEMENT
	private String isAcknowledgment;
	
	// META ENTITIES
	private Map<String, String> metaEntityNameTypeMap;
	
	// List of PDFEXT div IDs
	private List<Integer> PDFEXTdivIds;
	
	// Experimental (SEPLN): Key: summarization approach identifier, Value: rank
	private Map<String, String> summaryRank;
	
	// Constructor
	public SentenceJSON() {
		super();
		this.tokens = new ArrayList<Token>();
		this.babelSynsetsOcc = new ArrayList<BabelSynsetOcc>();
		this.citationMarkers = new ArrayList<CitationMarker>();
		
		this.rhetoricalClass = RhetoricalClassENUM.STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION.toString();
		
		this.metaEntityNameTypeMap = new HashMap<String, String>();
		
		this.PDFEXTdivIds = new ArrayList<Integer>();
		
		this.summaryRank = new HashMap<String, String>();
	}

	
	// Setters and getters
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}
	
	
	public Integer getAbstractId() {
		return abstractId;
	}


	public void setAbstractId(Integer abstractId) {
		this.abstractId = abstractId;
	}
	


	public boolean isAbstract() {
		return isAbstract;
	}


	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}


	public Integer getGlobalOrderNumber() {
		return globalOrderNumber;
	}


	public void setGlobalOrderNumber(Integer globalOrderNumber) {
		this.globalOrderNumber = globalOrderNumber;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}


	public Integer getSect_id() {
		return sect_id;
	}


	public void setSect_id(Integer sect_id) {
		this.sect_id = sect_id;
	}


	public String getSect_name() {
		return sect_name;
	}


	public void setSect_name(String sect_name) {
		this.sect_name = sect_name;
	}


	public String getSect_rootName() {
		return sect_rootName;
	}


	public void setSect_rootName(String sect_rootName) {
		this.sect_rootName = sect_rootName;
	}


	public Integer getSect_nastingLevel() {
		return sect_nastingLevel;
	}


	public void setSect_nastingLevel(Integer sect_nastingLevel) {
		this.sect_nastingLevel = sect_nastingLevel;
	}
	

	public String getSect_language() {
		return sect_language;
	}


	public void setSect_language(String sect_language) {
		this.sect_language = sect_language;
	}


	public List<Token> getTokens() {
		return tokens;
	}


	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}


	public String getRhetoricalClass() {
		return rhetoricalClass;
	}


	public void setRhetoricalClass(String rhetoricalClass) {
		this.rhetoricalClass = rhetoricalClass;
	}


	public List<BabelSynsetOcc> getBabelSynsetsOcc() {
		return babelSynsetsOcc;
	}


	public void setBabelSynsetsOcc(List<BabelSynsetOcc> babelSynsetsOcc) {
		this.babelSynsetsOcc = babelSynsetsOcc;
	}


	public List<CitationMarker> getCitationMarkers() {
		return citationMarkers;
	}


	public void setCitationMarkers(List<CitationMarker> citationMarkers) {
		this.citationMarkers = citationMarkers;
	}


	public String getIsAcknowledgment() {
		return isAcknowledgment;
	}


	public void setIsAcknowledgment(String isAcknowledgment) {
		this.isAcknowledgment = isAcknowledgment;
	}
	

	public Map<String, String> getMetaEntityNameTypeMap() {
		return metaEntityNameTypeMap;
	}
	

	public void setMetaEntityNameTypeMap(Map<String, String> metaEntityNameTypeMap) {
		this.metaEntityNameTypeMap = metaEntityNameTypeMap;
	}


	public List<Integer> getPDFEXTdivIds() {
		return PDFEXTdivIds;
	}


	public void setPDFEXTdivIds(List<Integer> pDFEXTdivIds) {
		PDFEXTdivIds = pDFEXTdivIds;
	}


	public Map<String, String> getSummaryRank() {
		return summaryRank;
	}


	public void setSummaryRank(Map<String, String> summaryRank) {
		this.summaryRank = summaryRank;
	}
	
}
