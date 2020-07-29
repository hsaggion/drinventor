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
package edu.upf.taln.dri.common.connector.bibsonomy;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.connector.bibsonomy.model.BibTexWrap;


/**
 * Example of bibliographic entry search by Bibsonomy
 * REF: http://www.bibsonomy.org/help/doc/api.html
 * 
 *
 */
public class ExampleBibsonomy {
	
	private static Logger logger = Logger.getLogger(ExampleBibsonomy.class.getName());

	public static void main(String[] args) {

		String s = "Moses: and open";
		System.out.println("NEW STRING: " + s.replace(":", ""));
		
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		/*
		PropertyManager.setPropertyFilePath("/home/francesco/Desktop/DRILIB_EXP/DRIconfig.properties");
		try {
			PropertyManager.loadProperties();
		} catch (InternalProcessingException e) {
			e.printStackTrace();
		}
		List<BibTexWrap> list = BibsonomyStandaloneConn.getBibTexWrap("Neural machine translation by jointly learning to align and translate. arXiv preprint arXiv:1409.0473",
				PropertyManager.getProperty("connector.bibsonomy.userid"), PropertyManager.getProperty("connector.bibsonomy.apykey"), 15);
		*/
		List<BibTexWrap> list = BibsonomyStandaloneConn.getBibTexWrap("DBpedia: a nucleus for a web of open data", "francesco82", "de8ce60b98de517ef533df6e860dedd6", 15);
		
		for (BibTexWrap post : list) {
			logger.info(post.getBibtexKey());
			logger.info(post.getTitle());
			logger.info(post.getAuthorList());
			logger.info(post.getInstitution());
		}
	}
}
