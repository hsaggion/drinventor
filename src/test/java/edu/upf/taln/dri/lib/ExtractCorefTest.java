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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.graph.DocGraphTypeENUM;
import edu.upf.taln.dri.lib.model.util.DocParse;
import junit.framework.JUnit4TestAdapter;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtractCorefTest {
	
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
	public void ExtractCorefs() {
		try {
			Logger.getRootLogger().setLevel(Level.DEBUG);
			URL resourceUrl = getClass().getResource("/paper3_PDFX_SENT_CITS_DEPS_RHET.xml");
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			DocumentImpl doc = (DocumentImpl) Factory.createNewDocument(resourcePath.toString());
			assertNotNull("XML doc not loaded by Dr Inventor Lib", doc);
			
			// DocumentImpl docImpl = (DocumentImpl) doc;
			DocumentImpl docImpl_1 = (DocumentImpl) Factory.getPlainTextLoader().parseString("Karla tried to identify her friend. "
					+ "Karla is tired of her friend. "
					+ "Karla and her friend are tired of her friend. "
					+ "Karla is ready to go. "
					+ "The things are harmless. "
					+ "Karla, an old hawk, lived at the top of a tall oak tree. "
					+ "One afternoon, she saw a hunter on the ground with a bow and some crude arrows that had no feathers. "
					+ "The hunter and his friend took aim and shot at the hawk but missed. "
					+ "The hunter and the child killed and tortured the pig, the cat and the bird. "
					+ "The man killed the dog and the butterfly. "
					+ "Karla knew the hunter wanted her feathers so she glided down to the hunter and offered to give him a few. "
					+ "The hunter was so grateful that he pledged never to shoot at a hawk again. "
					+ "He went off and shot a deer instead. ", "TEST doc");
			
			
			DocumentImpl docImpl_2 = (DocumentImpl) Factory.getPlainTextLoader().parseString("Suppose  you  are  a  doctor  faced  "
					+ "with  a  patient  who  has  a  malignant  tumor  in  his  stomach  .  "
					+ "It  is  impossible  to  operate  on  the  patient  ,  but  unless  the  tumor  is  destroyed  the  patient  will  die  ."
					+ "  There  is  a  kind  of  ray  that  can  be  used  to  destroy  the  tumor  .  "
					+ "If  the  rays  reach  the  tumor  all  at  once  at  a  sufficiently  high  intensity  ,  "
					+ "the  tumor  will  be  destroyed  .  Unfortunately  ,  at  this  intensity  the  healthy  tissue  that  "
					+ "the  rays  pass  through  on  the  way  to  the  tumor  will  also  be  destroyed  .  "
					+ "At  lower  intensities  the  rays  are  harmless  to  healthy  tissue  ,  but  they  will  not  affect  the  tumor  "
					+ "either  .  What  type  of  procedure  might  be  used  to  destroy  the  tumor  with  the  rays  ,  "
					+ "and  at  the  same  time  avoid  destroying  the  healthy  tissue  ? ", "TEST doc");
			
			
			// DocumentImpl docImpl_3 = (DocumentImpl) Factory.getPlainTextLoader().parseString("Karla, an old hawk, lived at the top of a tall oak tree.", "TEST doc");
					/*
					+ "The result was a blazing inferno that consumed an enormous quantity of oil each day. After initial efforts to extinguish it failed, famed "
					+ "firefighter Red Adair was called in. Red knew that the fire could be put out if a huge amount of fire retardant foam could be dumped "
					+ "on the base of the well. There was enough foam available at the site to do the job. However, there was no hose large enough to put "
					+ "all the foam on the fire fast enough. The small hoses that were available could not shoot the foam quickly enough to do any good. "
					+ "It looked like there would have to be a costly delay before a serious attempt could be made. However, Red Adair knew just what to do. "
					+ "He stationed men in a circle all around the fire, with all of the available small hoses. When everyone was ready all of the hoses were "
					+ "opened up and foam was directed at the fire from all directions. In this way a huge amount of foam quickly struck the source of the fire. "
					+ "The blaze was extinguished, and the Saudis were satisfied that Red had earned his three million dollar fee.", "TEST doc");
					*/
			
			DocumentImpl docImpl_3 = (DocumentImpl) Factory.getPlainTextLoader().parseString("Once there was an eagle named Zardia who nested on a rocky cliff.", "TEST doc");
			
			DocumentImpl docImpl_4 = (DocumentImpl) Factory.getPlainTextLoader().parseString("Once  there  was  a  small  country  called  Zardia  that  learned  to  make  the  world  's  smartest  computer  .  One  day  Zardia  was  attacked  by  its  warlike  neighbor  ,  Gagrach  .  But  the  missiles  were  badly  aimed  and  the  attack  failed  .  The  Zardian  government  realized  that  Gagrach  wanted  Zardian  computers  so  it  offered  to  sell  some  of  its  computers  to  the  country  .  The  government  of  Gagrach  was  very  pleased  .  It  promised  never  to  attack  Zardia  again  . ", "TEST doc"); 
			
			doc.parsingSentences(false);
			doc.parsingDep(false);
			doc.parsingCoref(false);
			doc.parsingBabelNet(false);
			/*
			DependencyGraph abstractGraph = docImpl.extractDocumentGraph(SentenceSelectorENUM.ONLY_ABSTRACT, DocGraphTypeENUM.DEP);
			System.out.println(abstractGraph.graphAsString(GraphToStringENUM.NODE_LIST));
			System.out.println(abstractGraph.graphAsString(GraphToStringENUM.TREE));
			
			
			System.out.println(DocParse.getROSasCSVstring(docImpl, SentenceSelectorENUM.ALL, DocGraphTypeENUM.DEP));
			
			System.out.println(JSONgenerator.getSentencesJSON(docImpl, SentenceSelectorENUM.ONLY_ABSTRACT));
			*/
			
			System.out.println(DocParse.getTokenROSasCSVstring(doc, SentenceSelectorENUM.ALL, DocGraphTypeENUM.DEP));
			System.out.println(DocParse.getDocumentROSasCSVstring(doc, SentenceSelectorENUM.ALL));
			try {
				// Store File
				String fileName = resourcePath.getFileName().toString().replace(".xml", "") + "_COREF_CHAIN.xml";
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
			
			// TODO: test condition
			assertTrue("Sentence List empty", doc != null);
			
			
		} catch (DRIexception e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ExtractCorefTest.class);
	}

}
