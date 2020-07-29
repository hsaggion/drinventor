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
package edu.upf.taln.dri.lib.model;

import java.util.HashMap;
import java.util.Map;

import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.CandidateTermOcc;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.Token;

public class DocCacheManager {
	
	private gate.Document gateDoc;
	
	private Map<Integer, Sentence> sentenceCache = new HashMap<Integer, Sentence>();
	private Map<Integer, Token> tokenCache = new HashMap<Integer, Token>();
	private Map<Integer, Section> sectionCache = new HashMap<Integer, Section>();
	private Map<Integer, CandidateTermOcc> termCacheById = new HashMap<Integer, CandidateTermOcc>();
	private Map<Integer, BabelSynsetOcc> babelSynCacheById = new HashMap<Integer, BabelSynsetOcc>();
	private Map<Integer, Citation> citationCacheById = new HashMap<Integer, Citation>();
	
	// Constructor
	public DocCacheManager(gate.Document gateDoc) throws InternalProcessingException {
		super();
		if(gateDoc == null) {
			throw new InternalProcessingException("Loading null document");
		}
		this.gateDoc = gateDoc;
		this.sentenceCache = new HashMap<Integer, Sentence>();
		this.tokenCache = new HashMap<Integer, Token>();
		this.sectionCache = new HashMap<Integer, Section>();
		this.termCacheById = new HashMap<Integer, CandidateTermOcc>();
		this.babelSynCacheById = new HashMap<Integer, BabelSynsetOcc>();
		this.citationCacheById = new HashMap<Integer, Citation>();
	}
	
	// Getters and setters
	public gate.Document getGateDoc() {
		return gateDoc;
	}
	
	/**
	 * Add a sentence to the cache / replace an existing one
	 * 
	 * @param sent
	 * @return
	 */
	public boolean cacheSentence(Sentence sent) {
		if(sent != null && sent.getId() != null) {
			sentenceCache.put(sent.getId(), sent);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a token to the cache / replace an existing one
	 * 
	 * @param tok
	 * @return
	 */
	public boolean cacheToken(Token tok) {
		if(tok != null && tok.getId() != null) {
			tokenCache.put(tok.getId(), tok);
			return true;
		}
		return false;
	}

	/**
	 * Add a section to the cache / replace an existing one
	 * 
	 * @param sect
	 * @return
	 */
	public boolean cacheSection(Section sect) {
		if(sect != null && sect.getId() != null) {
			sectionCache.put(sect.getId(), sect);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a candidate term to the cache / replace an existing one
	 * 
	 * @param sect
	 * @return
	 */
	public boolean cacheCandidateTerm(CandidateTermOcc cTerm) {
		if(cTerm != null && cTerm.getId() != null) {
			termCacheById.put(cTerm.getId(), cTerm);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a Babelnet synset occurrence to the cache / replace an existing one
	 * 
	 * @param sect
	 * @return
	 */
	public boolean cacheBabelSynsetOcc(BabelSynsetOcc babelSynOcc) {
		if(babelSynOcc != null && babelSynOcc.getId() != null) {
			babelSynCacheById.put(babelSynOcc.getId(), babelSynOcc);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a candidate term to the cache / replace an existing one
	 * 
	 * @param sect
	 * @return
	 */
	public boolean cacheCitation(Citation cit) {
		if(cit != null && cit.getId() != null) {
			citationCacheById.put(cit.getId(), cit);
			return true;
		}
		return false;
	}
	
	
	// Other methods
	/**
	 * Get cached sentence by id; null if the sentence does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public Sentence getCachedSentence(Integer id) {
		if(id != null && sentenceCache.containsKey(id) && sentenceCache.get(id) != null) {
			return sentenceCache.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get cached token by id; null if the token does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public Token getCachedToken(Integer id) {
		if(id != null && tokenCache.containsKey(id) && tokenCache.get(id) != null) {
			return tokenCache.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get cached section by id; null if the section does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public Section getCachedSection(Integer id) {
		if(id != null && sectionCache.containsKey(id) && sectionCache.get(id) != null) {
			return sectionCache.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get cached candidate term by id; null if the candidate term does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public CandidateTermOcc getCachedCandidateTerm(Integer id) {
		if(id != null && termCacheById.containsKey(id) && termCacheById.get(id) != null) {
			return termCacheById.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get cached Babelnet synset occurrence by id; null if the Babelnet synset occurrence does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public BabelSynsetOcc getCachedBabelSynsetOcc(Integer id) {
		if(id != null && babelSynCacheById.containsKey(id) && babelSynCacheById.get(id) != null) {
			return babelSynCacheById.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get cached citation by id; null if the citation does not exist in cache
	 * 
	 * @param id
	 * @return
	 */
	public Citation getCachedCitation(Integer id) {
		if(id != null && citationCacheById.containsKey(id) && citationCacheById.get(id) != null) {
			return citationCacheById.get(id);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Clear all cached document elements
	 */
	public void clearCache() {
		this.sentenceCache = new HashMap<Integer, Sentence>();
		this.tokenCache = new HashMap<Integer, Token>();
		this.sectionCache = new HashMap<Integer, Section>();
		this.termCacheById = new HashMap<Integer, CandidateTermOcc>();
		this.babelSynCacheById = new HashMap<Integer, BabelSynsetOcc>();
		this.citationCacheById = new HashMap<Integer, Citation>();
	}
	
	/**
	 * Clear all object pointers
	 */
	public void cleanUp() {
		
		this.gateDoc.cleanup();
		gate.Factory.deleteResource(this.gateDoc);
		
		clearCache();
		
		this.gateDoc = null;
	}
	
}
