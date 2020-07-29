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

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.Header;
import junit.framework.JUnit4TestAdapter;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParseHeaderTest {
	
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
	public void parseHeader() {
		try {
			URL resourceUrl = getClass().getResource("/PDFfiles/paper3.pdf");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			Document doc = Factory.getPDFloader().parsePDF(resourcePath.toString());
			
			DocumentImpl docImpl = (DocumentImpl) doc;
			Header head = docImpl.extractHeader();
			
			// assertTrue("Header is null", (head != null));
			// assertTrue("Header text is empty", StringUtils.isNotBlank(head.getPlainText()));
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ParseHeaderTest.class);
	}

}
