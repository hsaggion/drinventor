package edu.upf.taln.dri.lib.model.ext;

import org.apache.commons.lang.StringUtils;

import edu.upf.taln.dri.lib.model.BaseDocumentElem;
import edu.upf.taln.dri.lib.model.DocCacheManager;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * A Babelnet synset occurrence mined from the document.
 * 
 *
 */
public class BabelSynsetOccImpl extends BaseDocumentElem implements BabelSynsetOcc {
	private Integer id;
	private String text;
	private Integer inSentenceId;
	private String babelURL;
	private String synsetID;
	private String dbpediaURL;
	private Double golbalScore;
	private Double coherenceScore;
	private Double score;
	private String source;
	private Integer numTokens;

	// Constructors
	public BabelSynsetOccImpl(DocCacheManager cacheManager) {
		super(cacheManager);
		this.id = null;
		this.text = null;
		this.inSentenceId = null;
		this.babelURL = null;
		this.synsetID = null;
		this.dbpediaURL = null;
		this.golbalScore = null;
		this.coherenceScore = null;
		this.score = null;
		this.source = null;
		this.numTokens = null;
	}

	public BabelSynsetOccImpl(DocCacheManager cacheManager, Integer id) {
		super(cacheManager);
		this.id = id;
		this.text = null;
		this.inSentenceId = null;
		this.babelURL = null;
		this.synsetID = null;
		this.dbpediaURL = null;
		this.golbalScore = null;
		this.coherenceScore = null;
		this.score = null;
		this.source = null;
		this.numTokens = null;
	}

	public BabelSynsetOccImpl(DocCacheManager cacheManager, Integer id, String text, Integer sentenceId) {
		super(cacheManager);
		this.id = id;
		this.text = text;
		this.inSentenceId = sentenceId;
		this.babelURL = null;
		this.synsetID = null;
		this.dbpediaURL = null;
		this.golbalScore = null;
		this.coherenceScore = null;
		this.score = null;
		this.source = null;
		this.numTokens = null;
	}

	// Setters and getters
	@Override
	public Integer getId() {
		return (this.id != null) ? new Integer(this.id): this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getText() {
		return (this.text != null) ? new String(this.text): this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getInSentenceId() {
		return (this.inSentenceId != null) ? new Integer(this.inSentenceId): this.inSentenceId;
	}

	public void setInSentenceId(Integer inSentenceId) {
		this.inSentenceId = inSentenceId;
	}

	@Override
	public String getBabelURL() {
		return (this.babelURL != null) ? new String(this.babelURL): this.babelURL;
	}

	public void setBabelURL(String babelURL) {
		this.babelURL = babelURL;
	}

	@Override
	public String getSynsetID() {
		return (this.synsetID != null) ? new String(this.synsetID): this.synsetID;
	}

	public void setSynsetID(String synsetID) {
		this.synsetID = synsetID;
	}

	@Override
	public String getDbpediaURL() {
		return (this.dbpediaURL != null) ? new String(this.dbpediaURL): this.dbpediaURL;
	}

	public void setDbpediaURL(String dbpediaURL) {
		this.dbpediaURL = dbpediaURL;
	}

	@Override
	public Double getGolbalScore() {
		return (this.golbalScore != null) ? new Double(this.golbalScore): this.golbalScore;
	}

	public void setGolbalScore(Double golbalScore) {
		this.golbalScore = golbalScore;
	}

	@Override
	public Double getCoherenceScore() {
		return (this.coherenceScore != null) ? new Double(this.coherenceScore): this.coherenceScore;
	}

	public void setCoherenceScore(Double coherenceScore) {
		this.coherenceScore = coherenceScore;
	}

	@Override
	public Double getScore() {
		return (this.score != null) ? new Double(this.score): this.score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	@Override
	public String getSource() {
		return (this.source != null) ? new String(this.source): this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public Integer getNumTokens() {
		return (this.numTokens != null) ? new Integer(this.numTokens): this.numTokens;
	}

	public void setNumTokens(Integer numTokens) {
		this.numTokens = numTokens;
	}


	// Other interface implementations
	@Override
	public Integer getSentenceIdWithTerm() {
		if(this.inSentenceId != null) {
			return this.inSentenceId;
		}

		return null;
	}

	@Override
	public String asString(boolean compactOutput) {
		String babelSynStr = "";

		babelSynStr += "[BABELNET SYNSET] ID: '" + ((this.id != null) ? this.inSentenceId + "" : "-") + "'" +
				((this.text != null || !compactOutput) ? ", Text: '" + StringUtils.defaultString(this.text, "NULL") + "'" : "") +
				((this.text != null || !compactOutput) ? ", In-sentence ID: '" + ((this.inSentenceId != null) ? this.inSentenceId + "" : "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Babel URL: '" + StringUtils.defaultString(this.babelURL, "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Synset ID: '" + StringUtils.defaultString(this.synsetID, "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", DBpedia URL: '" + StringUtils.defaultString(this.dbpediaURL, "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Global score: '" + ((this.golbalScore != null) ? this.golbalScore + "" : "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Coherence score: '" + ((this.coherenceScore != null) ? this.coherenceScore + "" : "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Score: '" + ((this.score != null) ? this.score + "" : "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Source: '" + StringUtils.defaultString(this.source, "-") + "'" : "") +
				((this.text != null || !compactOutput) ? ", Num tokens: '" + ((this.score != null) ? this.numTokens + "" : "-") + "'" : "") + "\n";

		return babelSynStr;
	}

}
