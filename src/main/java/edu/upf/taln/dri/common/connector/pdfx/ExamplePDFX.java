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
package edu.upf.taln.dri.common.connector.pdfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.upf.taln.dri.common.util.Util;

/**
 * PDFX connector example
 * 
 *
 */
public class ExamplePDFX {

	private static Logger logger = Logger.getLogger(ExamplePDFX.class.getName());

	public static void main(String[] args) {

		Logger.getRootLogger().setLevel(Level.INFO);

		// PDFXConn.convertFilesAndStore("/home/francesco/Desktop/DRILIB_EXP/TEST", true, 0.2f, true, 400);

		String filename = "Full";

		/*
		try {
			Document doc = Factory.getPDFimporter().parsePDF("/home/francesco/Desktop/DRILIB_EXP/PDF_TEST/" + filename + ".pdf");

			FileOutputStream fos = new FileOutputStream("/home/francesco/Desktop/DRILIB_EXP/PDF_TEST/" + filename + "_COMPRESSED_1.xml");
			fos.write(doc.getXMLString().getBytes(Charset.forName("UTF-8")));
			fos.close();
		} catch (DRIexception e1) {
			Util.notifyException("Compressing PDF file", e1, logger);
		} catch (IOException e) {
			Util.notifyException("Compressing PDF file", e, logger);
		}
		 */

		/*
		Path filePath = Paths.get("/home/francesco/Downloads/" + filename + ".pdf");
		byte[] originalPDF = {};
		try {
			originalPDF = Files.readAllBytes(filePath);
		} catch (IOException e) {
			Util.notifyException("Reading PDF file", e, logger);
		}
		byte[] compressedPDF = null;

		try {
			compressedPDF = PDFXConn.pdfCompress(originalPDF, 0.01f, true);
		}
		catch(Exception e) {
			Util.notifyException("Compressing PDF file", e, logger);
		}

		logger.info("Compressed from " + originalPDF.length + " bytes to " + compressedPDF.length + " bytes");

		try {
			FileOutputStream fos = new FileOutputStream("/home/francesco/Downloads/" + filename + "_COMPRESSED.pdf");
			fos.write(compressedPDF);
			fos.close();
		}
		catch(Exception e) {
			Util.notifyException("Compressing PDF file", e, logger);
		}

		logger.info("Conversion completed");

		// By PDFX
		try {
			PDDocument document = PDDocument.load("/home/francesco/Downloads/" + filename + ".pdf");

			if (document.isEncrypted()) {
				document.decrypt("");
			}

			PDDocumentCatalog catalog = document.getDocumentCatalog();
			for (Object pageObj :  catalog.getAllPages()) {
				PDPage page = (PDPage) pageObj;
				PDResources resources = page.findResources();
				resources.getImages().clear();
			}

			document.save("/home/francesco/Downloads/" + filename + "_COMPRESSED_PDFBOX.pdf");
		}
		catch(Exception e) {
			Util.notifyException("Compressing PDF file", e, logger);
		}
		 */

		File rootDir = new File("/home/francesco/Desktop/DRILIB_EXP/SIGGRAPH_2002_2014/SIG_2002/EXPANDED");
		analyzeChildren(rootDir);
		System.out.println("------------------------------------");
		System.out.println("PDF files analyzed: " + totFiles);
		System.out.println("PDF files smaller than 5Mb: " + (totFiles - greaterThanFiles));
		System.out.println("PDF files smaller than 5Mb with reading errors: " + readingPDFerrorsSmallerThan5);
		System.out.println("PDF files greater than 5Mb: " + greaterThanFiles + " of which:");
		System.out.println("   > PDF files greater than 5Mb with reading errors: " + readingPDFerrorsGreaterThan5);
		System.out.println("   > PDF files greater than 5Mb without reading errors: " + (greaterThanFiles - readingPDFerrorsGreaterThan5) + " of which: ");
		System.out.println("       --- PDF files compressed to a size smaller or equal than 5Mb: " + correctlyCompressedToLess5Files);
		System.out.println("       --- PDF files compressed to a size greater than 5Mb: " + correctlyCompressedToMore5Files);
		System.out.println("\n\nList of PDF files that has not possible to compress to a size <= 5Mb:");
		for(String uncompressedPaper : uncompressedPapersSet) {
			System.out.println("   > " + uncompressedPaper);
		}
		System.out.println("\n\nList of PDF files with reading errors: " + (readingPDFerrorsGreaterThan5 + readingPDFerrorsSmallerThan5) + " in list:");
		for(String errorElem : errorSet) {
			System.out.println("   ERROR: " + errorElem);
		}
	}

	public static int totFiles = 0;
	public static int greaterThanFiles = 0;
	public static int correctlyCompressedToLess5Files = 0;
	public static int correctlyCompressedToMore5Files = 0;
	public static int readingPDFerrorsGreaterThan5 = 0;
	public static int readingPDFerrorsSmallerThan5 = 0;
	public static Set<String> errorSet = new HashSet<String>();
	public static Set<String> uncompressedPapersSet = new HashSet<String>();

	public static void analyzeChildren(File rootDir) {
		for(File childFile : rootDir.listFiles()) {
			if(childFile.isFile()) {
				if(childFile.getName().endsWith(".pdf")) {
					System.out.println("\nFILE: '" + childFile.getName() + "' IN DIRECTORY: '" + childFile.getParent() + "':");
					totFiles++;
				} 
				if(childFile.getName().endsWith(".pdf") && childFile.length() > 5242800l) {
					System.out.println("     HAS LENGTH > 5 Mb (" + childFile.length() + ")");
					greaterThanFiles++;

					byte[] originalPDF = new byte[(int) childFile.length()];
					
					try {
						Path path = Paths.get(childFile.getAbsolutePath());
						originalPDF = Files.readAllBytes(path);
					} catch (IOException e) {
						Util.notifyException("Reading PDF file", e, logger);
					}
					byte[] compressedPDF = null;

					try {
						compressedPDF = PDFXConn.pdfCompress(originalPDF, 0.01f, true);
					}
					catch(Exception e) {
						Util.notifyException("Compressing PDF file", e, logger);
					}
					
					System.out.println("     > compressed from " + ((originalPDF != null) ? originalPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes to " + ((compressedPDF != null) ? compressedPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes");

					if(compressedPDF != null && compressedPDF.length > 0l && compressedPDF.length < 5242800l) {
						correctlyCompressedToLess5Files++;
						System.out.println("     > CORRECTLY COMPRESSED UNDER 5 Mb");
					}
					else if(compressedPDF != null && compressedPDF.length > 5242800l) {
						correctlyCompressedToMore5Files++;
						System.out.println("     > ERROR: COMPRESSED TO A SIZE LARGER THAN 5 Mb");
						uncompressedPapersSet.add("FILE: '" + childFile.getName() + "' IN DIRECTORY: '" + childFile.getParent() + "', compressed from " + ((originalPDF != null) ? originalPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes to " + ((compressedPDF != null) ? compressedPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes");
					}
					else {
						System.out.println("     > ERROR: SOME ERROR OCCURRED DURING THE COMPRESSION PROCESS");
						errorSet.add("FILE: '" + childFile.getName() + "' IN DIRECTORY: '" + childFile.getParent() + "'");
						readingPDFerrorsGreaterThan5++;
					}

				}
				else if(childFile.getName().endsWith(".pdf") && childFile.length() <= 5242800l) {
					
					byte[] originalPDF = new byte[(int) childFile.length()];
					
					try {
						Path path = Paths.get(childFile.getAbsolutePath());
						originalPDF = Files.readAllBytes(path);
					} catch (IOException e) {
						Util.notifyException("Reading PDF file", e, logger);
					}
					byte[] compressedPDF = null;

					try {
						compressedPDF = PDFXConn.pdfCompress(originalPDF, 0.01f, true);
					}
					catch(Exception e) {
						Util.notifyException("Compressing PDF file", e, logger);
					}
					
					System.out.println("     > compressed from " + ((originalPDF != null) ? originalPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes to " + ((compressedPDF != null) ? compressedPDF.length : "_NO_LENGTH_COMPUTED_") + " bytes");

					if(compressedPDF != null && compressedPDF.length > 0l && compressedPDF.length < 5242800l) {
						
					}
					else if(compressedPDF != null && compressedPDF.length > 5242800l) {
						
					}
					else {
						System.out.println("     > ERROR: SOME ERROR OCCURRED DURING THE READ / COMPRESSION PROCESS");
						errorSet.add("FILE: '" + childFile.getName() + "' IN DIRECTORY: '" + childFile.getParent() + "'");
						readingPDFerrorsSmallerThan5++;
					}
					
					System.out.println("     HAS LENGTH < 5 Mb (" + childFile.length() + ")");
				}

			}
			else if(childFile.isDirectory()) {
				analyzeChildren(childFile);
			}
		}
	} 

}
