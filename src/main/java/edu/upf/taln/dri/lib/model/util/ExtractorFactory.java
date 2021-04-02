package edu.upf.taln.dri.lib.model.util;

import edu.upf.taln.dri.module.importer.SourceENUM;

public class ExtractorFactory {

	public static Extractor getExtractor(SourceENUM sourceType) {
		if (sourceType.equals(SourceENUM.PDFEXT)) {
			return new PDFEXTAuthorExtractor();
		}
		else if (sourceType.equals(SourceENUM.GROBID)) {
			return new GROBIDAuthorExtractor();
		}
		else if (sourceType.equals(SourceENUM.JATS)) {
			return new JATSAuthorExtractor();
		}
		else {
			throw new IllegalArgumentException("source type not supported");
		}
	}


}
