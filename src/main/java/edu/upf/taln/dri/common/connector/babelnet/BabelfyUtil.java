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
package edu.upf.taln.dri.common.connector.babelnet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.util.PropertyManager;
import it.uniroma1.lcl.babelfy.commons.BabelfyConfiguration;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyToken;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

/**
 * Collection of utility methods to access Babelfy Disambiguation service.
 * 
 *
 */
public class BabelfyUtil {

	private static Logger logger = Logger.getLogger(BabelfyUtil.class);

	/**
	 * Disambiguate a list of tokens belonging to the same or different sentences. Each sentence is constituted by a list of tokens of type
	 * {@link it.uniroma1.lcl.babelfy.commons.BabelfyToken BabelfyToken}.<br/>
	 * 
	 * @param apiKey
	 * @param sentTokens
	 * @return
	 */
	public static List<SemanticAnnotation> disambiguateTokenList(String apiKey, List<BabelfyToken> sentTokens, Language lang) {

		List<SemanticAnnotation> retList = new ArrayList<SemanticAnnotation>();

		if(StringUtils.isBlank(apiKey)) {
			logger.error("API key empty.");
			return retList;
		}
		else if(sentTokens == null || sentTokens.size() == 0) {
			logger.error("Sentence list empty.");
			return retList;
		}

		BabelfyConfiguration bconfig = it.uniroma1.lcl.babelfy.commons.BabelfyConfiguration.getInstance();

		try {
			bconfig.setConfigurationFile( new File(PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) + File.separator + "conf" + File.separator + "babelfy.properties") );
		} catch (Exception e) {
			logger.error("Impossible to set the proper configuration file of BabelNet");
		}

		bconfig.setRFkey(apiKey);

		// Instantiate BabelNet connection class
		BabelfyParameters bParam = new BabelfyParameters();
		bParam.setMultiTokenExpression(true);
		Babelfy bfy = new Babelfy(bParam);

		for(int i = 0; i < sentTokens.size(); i++) {
			logger.debug("Token " + i + " " + sentTokens.get(i).getWord());
		}

		List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(sentTokens, lang);

		return bfyAnnotations;
	}

	
	public static final String DRIconfigPathPropertyName = "DRIconf";
	
	public static void main(String[] args) throws InternalProcessingException {

		// Get DRIconfig.properties path
		String DRIconfigFileFullPath = null;
		String DRIresourceFolderFullPath = null;
		try {
			System.out.println("INIT: Reading system property '" + DRIconfigPathPropertyName + "'...");
			DRIconfigFileFullPath = (System.getProperty(DRIconfigPathPropertyName) != null) ? System.getProperty(DRIconfigPathPropertyName) : DRIconfigFileFullPath;
			System.out.println("INIT: The system property '" + DRIconfigPathPropertyName + "' is set equal to: " + DRIconfigFileFullPath);
			
			PropertyManager.setPropertyFilePath(DRIconfigFileFullPath);
			DRIresourceFolderFullPath = (PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) != null) ? PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) : null; 
			if(StringUtils.isBlank(DRIresourceFolderFullPath) || !(new File(DRIresourceFolderFullPath)).exists() || !(new File(DRIresourceFolderFullPath)).isDirectory()) {
				throw new InternalProcessingException("DRI INITIALIZATION ERROR: In DRI proeprty file, the property named '" + PropertyManager.resourceFolder_fullPath
						+ "' is not defined or points to an incorrect local path to the Resource folder - property value: " + ((DRIresourceFolderFullPath != null) ? DRIresourceFolderFullPath : "NULL"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new InternalProcessingException("DRI INITIALIZATION ERROR: In DRI proeprty file, does the property property named '" + PropertyManager.resourceFolder_fullPath
					+ "' has been correctly defined with the value of the local path to the Resource folder? - Exception: " + e.getMessage());
		}

		System.out.println("INIT: property '" + PropertyManager.resourceFolder_fullPath + "' pointing at: '" + DRIresourceFolderFullPath + "' folder.");
		

		try {
			System.out.println("Waiting 30 seconds before invoking Babelfy...");
			Thread.sleep(10000l);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			System.out.println("Waiting 20 seconds before invoking Babelfy...");
			Thread.sleep(10000l);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			System.out.println("Waiting 10 seconds before invoking Babelfy...");
			Thread.sleep(10000l);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		BabelfyConfiguration bconfig = it.uniroma1.lcl.babelfy.commons.BabelfyConfiguration.getInstance();

		bconfig.setRFkey("72f9a677-054b-4d6c-a58b-8ab74988a615");
		try {
			bconfig.setConfigurationFile( new File(PropertyManager.getProperty(PropertyManager.resourceFolder_fullPath) + File.separator + "conf" + File.separator + "babelfy.properties") );
		} catch (Exception e) {
			logger.error("Impossible to set the proper configuration file of BabelNet");
		}

		// Instantiate BabelNet connection class
		System.out.println("Invoking Babelfy...");
		System.out.println("Disambiguating sentence: '" + "The president Barak Obama approved the new act of the Parliament of USA." + "'");
		BabelfyParameters bParam = new BabelfyParameters();
		bParam.setMultiTokenExpression(true);
		Babelfy bfy = new Babelfy(bParam);

		List<SemanticAnnotation> semaAnn = bfy.babelfy("The president Barak Obama approved the new act of the Parliament of USA.", Language.EN);

		for(SemanticAnnotation sa : semaAnn) {
			System.out.println("Sem Ann: " + sa.getBabelSynsetID());
			System.out.println(" > " + sa.getSource());
		}

	}

}
