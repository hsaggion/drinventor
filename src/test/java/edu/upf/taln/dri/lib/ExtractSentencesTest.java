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
package edu.upf.taln.dri.lib;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import junit.framework.JUnit4TestAdapter;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtractSentencesTest {
	
	// Before and after methods
	@Before
	public void methodToExecuteBeforeAnyTest() {
		// Init Library
		try {
			URL resourceUrl = getClass().getResource("/DRIconfig.properties");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Factory.setDRIPropertyFilePath(resourcePath.toString());
			System.out.println(Factory.checkConfig());
			Factory.initFramework();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (DRIexception e) {
			e.printStackTrace();
		}
	}

	@After
	public void methodToExecuteAfterAnyTest() {

	}

	// ***************************************************
	// TEST METHODS:
	@Test
	public void ExtractSentences() {
		try {
			URL resourceUrl = getClass().getResource("/paper3_PDFX.xml");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Document doc = Factory.createNewDocument(resourcePath.toString());
			assertNotNull("XML doc not loaded by Dr Inventor Lib", doc);
			
			List<Sentence> sents = doc.extractSentences(SentenceSelectorENUM.ALL);
			
			assertTrue("Sentence List empty", sents != null && sents.size() > 0);
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void ExtractRhetClass() {
		try {
			URL resourceUrl = getClass().getResource("/paper3_PDFX.xml");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Document doc = Factory.createNewDocument(resourcePath.toString());
			assertNotNull("XML doc not loaded by Dr Inventor Lib", doc);
			
			List<Sentence> sents = doc.extractSentences(SentenceSelectorENUM.ALL);
			
			Set<String> rhetClasses = new HashSet<String>();
			for(Sentence sent : sents) {
				if(sent != null && sent.getRhetoricalClass() != null) {
					rhetClasses.add(sent.getRhetoricalClass().toString());
				}
			}
			
			assertTrue("Rhetorical class not extracted", sents != null && sents.size() > 0 && rhetClasses.size() > 1);
			
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ExtractSentencesTest.class);
	}

}
