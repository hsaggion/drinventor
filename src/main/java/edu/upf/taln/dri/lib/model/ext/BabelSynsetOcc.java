package edu.upf.taln.dri.lib.model.ext;

/**
 * Interface to access a Babelnet synset occurrence mined from the document.
 * 
 *
 */
public interface BabelSynsetOcc {
	/**
	 * Document-wide unambiguous Babelnet synset occurrence Id
	 * 
	 * @return document-wide unambiguous term Id
	 */
	public Integer getId();
	
	/**
	 * The Babelnet synset occurrence text
	 * 
	 * @return term text
	 */
	public String getText();
	
	/**
	 * Get the id of the sentence that includes the Babelnet synset occurrence
	 * @return
	 */
	public Integer getSentenceIdWithTerm();
	
	/**
	 * The BabelNet URL associated to this Babelnet synset occurrence
	 * @return
	 */
	public String getBabelURL();
	
	/**
	 * The Babelnet Synset ID associated to this Babelnet synset occurrence
	 * @return
	 */
	public String getSynsetID();
	
	/**
	 * The DBpedia URL associated to this Babelnet synset occurrence
	 * @return
	 */
	public String getDbpediaURL();
	
	/**
	 * The global score associated to this Babelnet synset occurrence
	 * @return
	 */
	public Double getGolbalScore();
	
	/**
	 * The coherence score asscoiated to this Babelnet synset occurrence
	 * @return
	 */
	public Double getCoherenceScore();
	
	/**
	 * The score associated to this Babelnet synset occurrence
	 * @return
	 */
	public Double getScore();
	
	/**
	 * The source associated to this Babelnet synset occurrence
	 * @return
	 */
	public String getSource();
	
	/**
	 * The number of tokens covered by this Babelnet synset occurrence
	 * @return
	 */
	public Integer getNumTokens();
	
	/**
	 * String representation of the object
	 * @return
	 */
	public String asString(boolean compactOutput);
}
