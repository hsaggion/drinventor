package edu.upf.taln.dri.lib.model.util.serializer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.Header;

public class DocumentJSON {

	private Header header;

	private List<SentenceJSON> abstractSentences;

	private List<SentenceJSON> bodySentences;

	private List<Citation> citations;

	private List<TripleJSON> abstractGraph;

	private List<TripleJSON> contentGraph;

	private Map<String, String> bibTex;

	// Experimental (SEPLN): affiliation disambiguation data - googlemaps
	private List<Map<String, String>> affil_gmaps;

	// Experimental (SEPLN): affiliation disambiguation data - spotlight
	private List<Map<String, String>> affil_spotlight;

	// Experimental (SEPLN): affiliation disambiguation data - synsets
	private List<BabelSynsetOcc> affil_synsets;

	// Experimental (SEPLN): keyword disambiguation data - synset
	private List<BabelSynsetOcc> keyword_synsets;

	// Constructor
	public DocumentJSON() {
		super();
		this.abstractSentences = new ArrayList<SentenceJSON>();
		this.bodySentences = new ArrayList<SentenceJSON>();
		this.citations = new ArrayList<Citation>();
		this.abstractGraph = new ArrayList<TripleJSON>();
		this.contentGraph = new ArrayList<TripleJSON>();
		
		this.affil_gmaps = new ArrayList<Map<String, String>>();
		this.affil_spotlight = new ArrayList<Map<String, String>>();
		this.affil_synsets = new ArrayList<BabelSynsetOcc>();
		this.keyword_synsets = new ArrayList<BabelSynsetOcc>();
	}


	// Setters and getters
	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public List<SentenceJSON> getAbstractSentences() {
		return abstractSentences;
	}

	public void setAbstractSentences(List<SentenceJSON> abstractSentences) {
		this.abstractSentences = abstractSentences;
	}

	public List<SentenceJSON> getBodySentences() {
		return bodySentences;
	}

	public void setBodySentences(List<SentenceJSON> bodySentences) {
		this.bodySentences = bodySentences;
	}

	public List<Citation> getCitations() {
		return citations;
	}

	public void setCitations(List<Citation> citations) {
		this.citations = citations;
	}

	public List<TripleJSON> getAbstractGraph() {
		return abstractGraph;
	}

	public void setAbstractGraph(List<TripleJSON> abstractGraph) {
		this.abstractGraph = abstractGraph;
	}

	public List<TripleJSON> getContentGraph() {
		return contentGraph;
	}

	public void setContentGraph(List<TripleJSON> contentGraph) {
		this.contentGraph = contentGraph;
	}

	public Map<String, String> getBibTex() {
		return bibTex;
	}

	public void setBibTex(Map<String, String> bibTex) {
		this.bibTex = bibTex;
	}
	

	public List<Map<String, String>> getAffil_gmaps() {
		return affil_gmaps;
	}


	public void setAffil_gmaps(List<Map<String, String>> affil_gmaps) {
		this.affil_gmaps = affil_gmaps;
	}


	public List<Map<String, String>> getAffil_spotlight() {
		return affil_spotlight;
	}


	public void setAffil_spotlight(List<Map<String, String>> affil_spotlight) {
		this.affil_spotlight = affil_spotlight;
	}


	public List<BabelSynsetOcc> getAffil_synsets() {
		return affil_synsets;
	}


	public void setAffil_synsets(List<BabelSynsetOcc> affil_synsets) {
		this.affil_synsets = affil_synsets;
	}


	public List<BabelSynsetOcc> getKeyword_synsets() {
		return keyword_synsets;
	}


	public void setKeyword_synsets(List<BabelSynsetOcc> keyword_synsets) {
		this.keyword_synsets = keyword_synsets;
	}
	
}
