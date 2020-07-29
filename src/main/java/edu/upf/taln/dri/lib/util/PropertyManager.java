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
package edu.upf.taln.dri.lib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;

/**
 * Classes to manage the configuration orpoerties of the Dr. Inventor Text Mining Framework.
 * 
 *
 */
public class PropertyManager {
	
	public static final String resourceFolder_fullPath = "resourceFolder.fullPath";
	
	private static Logger logger = Logger.getLogger(PropertyManager.class.getName());
	
	private static String propertyPath;
	private static Properties holder = null; 
	
	/**
	 * Load the library property file.
	 * The path of the library property file is specified as a local absolute (without trailing slash, for instance /home/mydir/DRIresources/DRIconfig.properties)
	 * 
	 * @return
	 * @throws InternalProcessingException
	 */
	public static boolean loadProperties() throws InternalProcessingException {
		
		FileInputStream input;
		try {
			input = new FileInputStream(propertyPath);
		} catch (FileNotFoundException e) {
			throw new InternalProcessingException("PROPERTY FILE INITIALIZATION ERROR: property file '" + propertyPath + "' cannot be found");
		}
		
		try {
			holder = new Properties();
			holder.load(input);
		} catch (IOException e) {
			throw new InternalProcessingException("PROPERTY FILE INITIALIZATION ERROR: property file '" + propertyPath + "' cannot be read (" +  e.getMessage() + ")");
		}
		
		return false;
	}
	
	/**
	 * Set the property file path
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean setPropertyFilePath(String filePath) {
		if(filePath != null) {
			File propFile = new File(filePath);
			if(propFile.exists() && propFile.isFile()) {
				propertyPath = filePath;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieve a property from the library property file.
	 * The path of the library property file is specified as a local absolute (without trailing slash, for instance /home/mydir/DRIresources/DRIconfig.properties)
	 * 
	 * @param propertyName
	 * @return
	 * @throws InternalProcessingException
	 */
	public static String getProperty(String propertyName) {
		if(StringUtils.isNotBlank(propertyName)) {
			if(holder == null) {
				try {
					loadProperties();
				} catch (InternalProcessingException e) {
					Util.notifyException("Property file not correctly loaded", e, logger);
				}
			}
			
			return holder.getProperty(propertyName);
		}
		else {
			return null;
		}
	}
	
}
