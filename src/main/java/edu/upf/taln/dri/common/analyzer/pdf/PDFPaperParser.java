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
package edu.upf.taln.dri.common.analyzer.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Collection of utilities to mine data from PDF documents 
 * 
 *
 */
public class PDFPaperParser {

	/**
	 * Extract from the PDF, a list of paragraph by converting the PDF file to HTML
	 * 
	 * @param PDFInputStream
	 * @param onlyHeaderParagraph
	 * @return
	 */
	public static List<String> getHeaderSentences(InputStream PDFInputStream, boolean onlyHeaderParagraph) {
		// Get PDF and parse contents
		PDDocument pdoc = null;

		// logger.info("Start parsing doc " + PDF_URL.toString());

		// Load PDF document
		try {
			pdoc = PDDocument.load(PDFInputStream);
		} catch (IOException e) {
			return null;
		}

		return extractLineList(pdoc, onlyHeaderParagraph);
	}


	/**
	 * Extract from the PDF, a list of paragraph by converting the PDF file to HTML
	 * 
	 * @param hrefStr
	 * @param onlyHeaderParagraph set to true to extract only header paragraph
	 * @return
	 */
	public static List<String> getHeaderSentences(URL PDF_URL, boolean onlyHeaderParagraph) {

		// Get PDF and parse contents
		PDDocument pdoc = null;

		// logger.info("Start parsing doc " + PDF_URL.toString());

		// Load PDF document
		try {
			pdoc = PDDocument.load(PDF_URL);
		} catch (IOException e) {
			return null;
		}

		return extractLineList(pdoc, onlyHeaderParagraph);
	}

	private static List<String> extractLineList(PDDocument pdoc, boolean onlyHeaderParagraph) {
		List<String> retList = new ArrayList<String>();

		if(pdoc != null) {
			try {
				PDFTextStripper ts_HTML = new PDFText2HTML("UTF-8");
				ts_HTML.setAddMoreFormatting(true);
				String HTMLtext = ts_HTML.getText(pdoc);

				// ************************************************
				// Parse HTML version

				// Retrieve the list of P elements in the first page of the PDF document
				List<String> pList = new ArrayList<String>();
				Document doc;
				try {
					doc = Jsoup.parse(HTMLtext);

					// Retrieve p children
					Elements divDocElemPchildren = doc.getElementsByTag("p");
					if(divDocElemPchildren != null && divDocElemPchildren.size() >= 1) {
						for(int g = 0; g < divDocElemPchildren.size(); g++) {
							Element divDocElemPchild = divDocElemPchildren.get(g);

							if(divDocElemPchild != null && divDocElemPchild.text() != null && !divDocElemPchild.text().trim().equals("")) {
								String textOfLine = divDocElemPchild.text();
								pList.add(textOfLine);
							}
						}
					}

				} catch (Exception e) {
					// Do nothing
				}

				retList = pList;

				// Identify P HEADER LINES (before abstract)
				if(onlyHeaderParagraph) {
					List<String> pListHeader = new ArrayList<String>();
					for(int i = 0; i < pList.size(); i++) {
						String pListElem = pList.get(i);
						if(pListElem != null) {
							if(i <= 1 || !pListElem.toLowerCase().contains("abstract")) {
								pListHeader.add(pListElem);
							}
							else {
								break;
							}
						}

						if(i > 14) {
							break;
						}
					}
					
					retList = pListHeader;
				}

			} catch (Exception e) {
				// Do nothing
			}
			finally {
				try {
					pdoc.close();	
				}
				catch (Exception e) {
					// Do nothing
				}
			}
		}

		return retList;
	}

}
