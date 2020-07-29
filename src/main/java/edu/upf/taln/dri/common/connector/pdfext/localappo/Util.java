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
package edu.upf.taln.dri.common.connector.pdfext.localappo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;


public class Util {

	private static Logger logger = Logger.getLogger(Util.class);
	
	/**
	 * Read a byte array from an input stream
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFully(InputStream stream) throws IOException {
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1)
		{
			baos.write(buffer, 0, bytesRead);
		}
		return baos.toByteArray();
	}

	/**
	 * Generate a string digest (MD5) from an array of bytes
	 * 
	 * @param byteArrayInput
	 * @return
	 */
	public static String computeStringDigest(byte[] byteArrayInput) {
		StringBuffer hexString = new StringBuffer();

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(byteArrayInput);
			byte[] hash = md.digest();

			for (int i = 0; i < hash.length; i++) {
				hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Digest computation error - " + e.getMessage());
			e.printStackTrace();
		}

		return hexString.toString();
	}

	/**
	 * Delete a folder together with its contents
	 * 
	 * @param folder
	 */
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();

		if(files != null) {
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}

		folder.delete();
	}

}
