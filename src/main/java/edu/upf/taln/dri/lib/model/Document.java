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

import java.io.File;
import java.util.List;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.ext.CandidateTermOcc;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.Header;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.ext.SummaryTypeENUM;
import edu.upf.taln.dri.lib.model.graph.DependencyGraph;
import edu.upf.taln.dri.lib.model.graph.SentGraphTypeENUM;
import edu.upf.taln.dri.module.importer.SourceENUM;

/** 
 * Interface to access a Document processed by Dr Inventor.<br/><br/>
 * To get an instance of a Document by the {@link edu.upf.taln.dri.lib.model.Document Document interface}, you have always to use one of the
 * {@link edu.upf.taln.dri.lib.Factory Factory} methods:  <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument() Factory.createNewDocument()} <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument(String absoluteFilePath) Factory.createNewDocument(String absoluteFilePath)} <br/>
 * - {@link edu.upf.taln.dri.lib.Factory#createNewDocument(File file) Factory.createNewDocument(File file)} <br/>
 * 
 *
 */
public interface Document {
	
	/**
	 * Get the name of the document
	 * 
	 * @return
	 * @throws InternalProcessingException 
	 */
	public String getName() throws InternalProcessingException;


	/**
	 * Get the XML string-serialized contents of the document, as a string (UTF-8 char encoding)
	 * 
	 * @return the String representing the contents of the Document
	 * @throws InternalProcessingException 
	 */
	public String getXMLString() throws InternalProcessingException;
	
	
	/**
	 * Get the contents of the document as an instance of org.w3c.dom.Document
	 * 
	 * @return the document as an instance of the class org.w3c.dom.Document
	 * @throws InternalProcessingException 
	 */
	public org.w3c.dom.Document getXMLDocument() throws InternalProcessingException;
	
	
	/**
	 * Load the XML string-serialized contents of the document (UTF-8) from a file, by specifying the file's absolute path
	 * 
	 * @param absoluteFilePath the absolute path of the file with the XML string-serialized contents of the document to load
	 * @throws DRIexception 
	 */
	public void loadXML(String absoluteFilePath) throws DRIexception;
	
	
	/**
	 * Load the XML string-serialized contents of the document (UTF-8) from a file
	 * 
	 * @param file the file with the XML string-serialized contents of the document to load
	 * @return
	 * @throws DRIexception 
	 */
	public void loadXML(File file) throws DRIexception;
	
	
	/**
	 * Load the XML string-serialized contents of the document from a string (UTF-8 char encoding)
	 * 
	 * @param XMLStringContents the String with the XML serialized contents to load
	 * @throws InternalProcessingException
	 */
	public void loadXMLString(String XMLStringContents) throws DRIexception;
	
	
	/**
	 * Get the raw text of the document (UTF-8 encoded)
	 * 
	 * @return the UTF-8 encoded text of the document
	 * @throws InternalProcessingException 
	 */
	public String getRawText() throws InternalProcessingException;

	
	/**
	 * Pre-compute the text analysis of the document in order to speed-up the execution of
	 * the extract-methods.
	 * 
	 * @throws InternalProcessingException
	 */
	public void preprocess() throws InternalProcessingException;
	
	/**
	 * Extract the information retrieved by parsing the header of the paper
	 * 
	 * @return
	 * @throws InternalProcessingException 
	 */
	public Header extractHeader() throws InternalProcessingException;
	
	/**
	 * Get the sections (or a subset of the sections) of the document
	 * 
	 * @param onlyRoot if equal to true, extract only the top level sections (h1)
	 * @return
	 * @throws InternalProcessingException
	 */
	public List<Section> extractSections(Boolean onlyRoot) throws InternalProcessingException;
	
	/* If sentences have not been rhetorically annotated, the first time this method is executed the rhetorical category of each sentence is determined. */
	/**
	 * Load the list of sentences of the document, ordered by their occurrence in the document.
	 * If sentences have not been extracted, the first time this method is executed the document text is split into sentences.
	 * 
	 * 
	 * @param sentenceSel the type of sentence to select
	 * @return the set of sentences in document order
	 * @throws InternalProcessingException
	 */
	public List<Sentence> extractSentences(SentenceSelectorENUM sentenceSel) throws InternalProcessingException;
	
	/**
	 * Get one of the sentences of the document by id
	 * 
	 * @param sentenceId
	 * @return null if the sentence id is null or not a valid id
	 * @throws InternalProcessingException
	 */
	public Sentence extractSentenceById(int sentenceId) throws InternalProcessingException;
	
	/**
	 * Load the list of terms extracted from the document.
	 * If the terminology has not been extracted from the document, the first time this method is executed relevant terms are extracted from the document.
	 * 
	 * @return the set of sentences in document order
	 */
	public List<CandidateTermOcc> extractTerminology() throws DRIexception;
	
	/**
	 * Generate a summary of the paper by selecting a relevant set of sentences. Sentences are ordered by their relevance in descending order.
	 * 
	 * @param sentNumber from 1 to 30
	 * @param summaryType
	 * @return
	 * @throws InternalProcessingException
	 */
	public List<Sentence> extractSummary(int sentNumber, SummaryTypeENUM summaryType) throws InternalProcessingException;

	/**
	 * Get the graph representing a sentence. The id of the sentence can be retrieved by the method 
	 * {@link edu.upf.taln.dri.lib.model.Document#extractSentences() extractSentences()}
	 * 
	 * NB: experimental sentence graphs merging approach implemented
	 * 
	 * @param sentenceId
	 * @param graphType
	 * @return
	 * @throws DRIexception
	 */
	public DependencyGraph extractSentenceGraph(int sentenceId, SentGraphTypeENUM graphType) throws DRIexception;
	
	/**
	 * Get the graph representing a portion of a document. The nodes of the graph are merged by relying
	 * on co-reference chains. 
	 * 
	 * @param sentenceSel
	 * @return
	 * @throws DRIexception
	 */
	public DependencyGraph extractDocumentGraph(SentenceSelectorENUM sentenceSel) throws DRIexception;
	
	
	/**
	 * Get the list of citations extracted from the document.
	 * 
	 * @return
	 */
	public List<Citation> extractCitations() throws InternalProcessingException;

	
	/**
	 * This method deletes all the data extracted from the original document including sentences, terminology, citations, etc.
	 * After calling this method on a {@link edu.upf.taln.dri.lib.model.Document Document} object, the next time sentences,
	 * terminology, citations, etc. from the document are accessed, they are extracted again and not read from the output of
	 * a previous extraction process execution.
	 * @throws InternalProcessingException 
	 *  
	 */
	public void resetDocumentExtractionData() throws InternalProcessingException;
	
	/**
	 * Get the original document type from which the {@link edu.upf.taln.dri.lib.model.Document Document} instance has been created. 
	 * The set of document types are the values of {@link edu.upf.taln.dri.module.importer.SourceENUM SourceENUM}. 
	 * 
	 * @return
	 * @throws InternalProcessingException
	 */
	public SourceENUM getSourceDocumentType() throws InternalProcessingException;
	
	/**
	 * Call this method only WHEN YOU ARE SURE YOU WILL NOT USE THE DOCUMENT NO MORE IN YOUR DATA.
	 * This method will clean all the document data structures made the memory occupied by these data ready for garbage collection.
	 * Note that, if you try to access / call methods of the document after calling this method an exception will be raised 
	 * to state that the resource has been already closed and its data cleaned.
	 * 
	 * @throws InternalProcessingException
	 */
	public void cleanUp() throws InternalProcessingException;
	
	/**
	 * Check if the document data structures has been cleaned by calling the cleanUp() method. 
	 * A cleaned up document cannot be used no more; if you try to access / call methods of the document after calling this method an Exception will be raised 
	 * to state that the resource has been already closed and its data cleaned.
	 * 
	 * @return true if the document data structures has been cleaned.
	 * @throws InternalProcessingException
	 */
	public boolean isCleanUp() throws InternalProcessingException;
	
}
