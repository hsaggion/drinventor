package edu.upf.taln.dri.lib.model.util.extractor;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.Author;
import edu.upf.taln.dri.lib.model.ext.AuthorImpl;
import edu.upf.taln.dri.lib.model.ext.InstitutionImpl;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import gate.Annotation;
import gate.Document;

import java.util.ArrayList;
import java.util.List;

public class PDFEXTAuthorExtractor extends Extractor{

	public List<Author> extract(DocCacheManager cacheManager) {
		ArrayList<Author> authorList = new ArrayList<>();

		// Retrieve author information from JATS tags
		Document gateDoc = cacheManager.getGateDoc();
		List<Annotation> authorAnnotationList = GateUtil.getAnnInDocOrder(gateDoc, ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTauthor);

		List<Annotation> affiliationAnnotationList = GateUtil.getAnnInDocOrder(gateDoc, ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTaffiliation);
		List<Annotation> emailAnnotationList = GateUtil.getAnnInDocOrder(gateDoc, ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTemail);

		if (authorAnnotationList != null && authorAnnotationList.size() > 0) {
			for (Annotation authorAnn : authorAnnotationList) {
				if (authorAnn != null) {
					String authorFullName = GateUtil.getAnnotationText(authorAnn, gateDoc).orElse(null);
					String authorAffiId = GateUtil.getStringFeature(authorAnn, "refaff").orElse(null);
					String authorEmailId = GateUtil.getStringFeature(authorAnn, "refemail").orElse(null);

					if (authorFullName != null && !authorFullName.trim().equals("")) {
						AuthorImpl newAuthor = new AuthorImpl(cacheManager, normalizeText(authorFullName).trim(), null, null);

						if (authorAffiId != null && affiliationAnnotationList != null) {
							for (Annotation affiliation : affiliationAnnotationList) {
								if (affiliation != null) {
									String affiliationText = GateUtil.getAnnotationText(affiliation, gateDoc).orElse(null);
									String affiliationID = GateUtil.getStringFeature(affiliation, "id").orElse(null);
									if (affiliationText != null && affiliationID != null && affiliationID.trim().equals(authorAffiId) && !affiliationText.trim().equals("")) {
										InstitutionImpl institution = new InstitutionImpl(cacheManager);
										institution.setFullText(normalizeText(affiliationText).trim());
										newAuthor.addAffiliation(institution);
									}
								}
							}
						}

						if (authorEmailId != null && emailAnnotationList != null) {
							for (Annotation email : emailAnnotationList) {
								if (email != null) {
									String emailText = GateUtil.getAnnotationText(email, gateDoc).orElse(null);
									String emailID = GateUtil.getStringFeature(email, "id").orElse(null);
									if (emailText != null && emailID != null && emailID.trim().equals(authorEmailId) && !emailText.trim().equals("")) {
										newAuthor.setEmail(normalizeText(emailText).trim());
									}
								}
							}
						}

						authorList.add(newAuthor);
					}
				}
			}
		}
		return authorList;
	}
}
