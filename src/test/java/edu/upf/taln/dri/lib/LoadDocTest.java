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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import junit.framework.JUnit4TestAdapter;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoadDocTest {
	
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
	// @Test
	public void LoadPDFDocument() {
		try {
			URL resourceUrl = getClass().getResource("/paper3_PDFX.xml");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Document doc = Factory.createNewDocument(resourcePath.toString());
			assertNotNull("The PDF document should be correctly loaded from the PDF file", doc);
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void LoadJATSDocument() {
		try {
			URL resourceUrl = getClass().getResource("/JATSfiles/paper_1.xml");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Document doc = Factory.getJATSloader().parseJATS(resourcePath.toString());
			DocumentImpl docImpl = (DocumentImpl) doc;
			docImpl.parsingSentences(false);
			docImpl.parsingCitations_Spot(false);
			docImpl.parsingCitations_Link(false);
			assertNotNull("The JATS document should be correctly loaded from the JATS XML file", doc);
			
			try {
				// Store File
				String fileName = resourcePath.getFileName().toString().replace(".xml", "") + "_JATS.xml";
				File file = new File("/home/francesco/Desktop/DRILIB_EXP/TEST/" + fileName);

				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(doc.getXMLString());
				bw.flush();
				bw.close();
				
				
				System.out.println("File stored to: " + file.getAbsolutePath());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			resourceUrl = getClass().getResource("/JATSfiles/paper_2.xml");
			resourcePath = Paths.get(resourceUrl.toURI());
			doc = Factory.getJATSloader().parseJATS(resourcePath.toString());
			docImpl = (DocumentImpl) doc;
			docImpl.parsingSentences(false);
			docImpl.parsingCitations_Spot(false);
			docImpl.parsingCitations_Link(false);
			assertNotNull("The JATS document should be correctly loaded from the JATS XML file", doc);
			
			try {
				// Store File
				String fileName = resourcePath.getFileName().toString().replace(".xml", "") + "_JATS.xml";
				File file = new File("/home/francesco/Desktop/DRILIB_EXP/TEST/" + fileName);

				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(doc.getXMLString());
				bw.flush();
				bw.close();
				
				
				System.out.println("File stored to: " + file.getAbsolutePath());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(LoadDocTest.class);
	}

}
