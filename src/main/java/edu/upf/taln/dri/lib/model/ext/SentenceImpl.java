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
package edu.upf.taln.dri.lib.model.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Sentence of a document together with its descriptive features.
 * 
 *
 */
public class SentenceImpl extends BaseDocumentElem implements Sentence {

	private Integer id;
	private String text;
	private Integer abstractId;
	private RhetoricalClassENUM rhetoricalClass;
	private Section containingSection;
	private List<Token> tokens;
	private List<CitationMarker> citationMarkers;
	private List<CandidateTermOcc> candidateTerms;
	private List<BabelSynsetOcc> babelSynsetsOcc;
	private LangENUM language;
	private boolean isAck;
	private Map<String, MetaEntityTypeENUM> metaEntityMap;

	// Constructor
	public SentenceImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.text = null;
		this.abstractId = null;
		this.rhetoricalClass = RhetoricalClassENUM.STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION;
		this.containingSection = null;
		this.citationMarkers = new ArrayList<CitationMarker>();
		this.candidateTerms = new ArrayList<CandidateTermOcc>();
		this.language = LangENUM.UNSPECIFIED;
		this.isAck = false;
		this.metaEntityMap = new HashMap<String, MetaEntityTypeENUM>();
	}

	// Setters and getters
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id) : null;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return (this.text != null) ? new String(this.text) : null;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getAbstractId() {
		return abstractId;
	}

	public void setAbstractId(Integer abstractId) {
		this.abstractId = abstractId;
	}

	public void setContainingSection(Section containingSection) {
		this.containingSection = containingSection;
	}

	public RhetoricalClassENUM getRhetoricalClass() {
		return RhetoricalClassENUM.valueOf(rhetoricalClass.toString());
	}

	public void setRhetoricalClass(RhetoricalClassENUM rhetClass) {
		this.rhetoricalClass = rhetClass;
	}

	public Section getContainingSection() {
		return containingSection;
	}

	public List<CitationMarker> getCitationMarkers() {
		return (this.citationMarkers != null) ? Collections.unmodifiableList(this.citationMarkers) : null;
	}

	public void addCitationMarker(CitationMarker citationMarker) {
		if(this.citationMarkers == null) {
			this.citationMarkers = new ArrayList<CitationMarker>();
		}

		if(citationMarker != null) {
			this.citationMarkers.add(citationMarker);
		}
	}

	public List<CandidateTermOcc> getCandidateTerms() {
		return (this.candidateTerms != null) ? Collections.unmodifiableList(this.candidateTerms) : null;
	}

	public void addCandidateTerm(CandidateTermOcc candidateTerm) {
		if(this.candidateTerms == null) {
			this.candidateTerms = new ArrayList<CandidateTermOcc>();
		}

		if(candidateTerm != null) {
			this.candidateTerms.add(candidateTerm);
		}
	}
	
	public boolean isAck() {
		return isAck;
	}

	public void setAck(boolean isAck) {
		this.isAck = isAck;
	}

	public Map<String, MetaEntityTypeENUM> getMetaEntityMap() {
		return metaEntityMap;
	}

	public void setMetaEntityMap(Map<String, MetaEntityTypeENUM> metaEntityMap) {
		this.metaEntityMap = metaEntityMap;
	}
	
	@Override
	public List<BabelSynsetOcc> getBabelSynsetsOcc() {
		return (this.babelSynsetsOcc != null) ? Collections.unmodifiableList(this.babelSynsetsOcc) : null;
	}

	public void addBabelSynsetOcc(BabelSynsetOcc babelSynsetOcc) {
		if(this.babelSynsetsOcc == null) {
			this.babelSynsetsOcc = new ArrayList<BabelSynsetOcc>();
		}

		if(babelSynsetOcc != null) {
			this.babelSynsetsOcc.add(babelSynsetOcc);
		}
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	@Override
	public List<Token> getTokens() {
		if(this.tokens == null || this.tokens.size() == 0) {
			return ObjectGenerator.getTokensFromSentenceId(this.getId(), cacheManager);
		}
		else {
			return Collections.unmodifiableList(this.tokens); 
		}
	}
	
	public void setLanguage(LangENUM languge) {
		this.language = languge;
	}

	@Override
	public LangENUM getLanguage() {
		switch(this.language) {
		case ES:
			return LangENUM.ES;
		case EN:
			return LangENUM.EN;
		default:
			return LangENUM.UNSPECIFIED;
		} 
	}

	@Override
	public String asString(boolean compactOutput) {
		String sentenceStr = "";

		sentenceStr += "[SENTENCE] ID: '" + ((this.id != null) ? this.id + "" : "-") + "'" +
				((this.text != null || !compactOutput) ? ", Text: '" + StringUtils.defaultString(this.text, "-") + "'" : "") +
				((this.rhetoricalClass != null || !compactOutput) ? ", Rhetorical class: '" + ((this.rhetoricalClass != null) ? this.rhetoricalClass + "" : "-") + "'" : "") +
				((this.containingSection != null || !compactOutput) ? ", Section: '" + ((this.containingSection != null) ? this.containingSection.asString(compactOutput) : "-") + "'" : "") + 
				"Is ack: '" + this.isAck + "'" + ", Language: '" + ((this.language != null) ? this.language + "" : "-") + "'" + ", "
						+ "Meta-entities: '" + ((this.metaEntityMap != null) ? this.metaEntityMap : "-") + "'\n";

		if(this.tokens != null && this.tokens.size() > 0) {
			sentenceStr += "   " + this.tokens.size() + " TOKENS ASSOCIATED\n";
			/*
			for(Token token : this.tokens) {
				sentenceStr += "   " + token.asString(compactOutput);
			}
			*/
		}
		else {
			sentenceStr += "   NO TOKENS ASSOCIATED\n";
		}

		if(this.citationMarkers != null && this.citationMarkers.size() > 0) {
			for(CitationMarker citMarker : this.citationMarkers) {
				sentenceStr += "   " + citMarker.asString(compactOutput);
			}
		}
		else {
			sentenceStr += "   NO CITATION MARKERS ASSOCIATED\n";
		}

		if(this.candidateTerms != null && this.candidateTerms.size() > 0) {
			for(CandidateTermOcc candidTerm : this.candidateTerms) {
				sentenceStr += "   " + candidTerm.asString(compactOutput);
			}
		}
		else {
			sentenceStr += "   NO CANDIDATE TERMS ASSOCIATED\n";
		}

		if(this.babelSynsetsOcc != null && this.babelSynsetsOcc.size() > 0) {
			for(BabelSynsetOcc babelSynOcc : this.babelSynsetsOcc) {
				sentenceStr += "   " + babelSynOcc.asString(compactOutput);
			}
		}
		else {
			sentenceStr += "   NO BABELNET SYNTET OCCURRENCES ASSOCIATED\n";
		}

		return sentenceStr;
	}
	

	@Override
	public boolean isAcknowledgment() {
		return this.isAck;
	}

	@Override
	public Map<String, MetaEntityTypeENUM> getSpottedEntities() {
		return Collections.unmodifiableMap(this.metaEntityMap); 
	}

}
