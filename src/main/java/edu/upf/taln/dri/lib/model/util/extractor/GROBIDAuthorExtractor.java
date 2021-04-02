package edu.upf.taln.dri.lib.model.util.extractor;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.Author;
import edu.upf.taln.dri.lib.model.ext.AuthorImpl;
import edu.upf.taln.dri.lib.model.ext.InstitutionImpl;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;
import edu.upf.taln.dri.module.importer.pdf.ImporterGROBID;
import gate.Annotation;
import gate.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GROBIDAuthorExtractor implements Extractor{
	public List<Author> extract(DocCacheManager cacheManager) {
		ArrayList<Author> authorList = new ArrayList<>();

		// Retrieve author information from GROBID analysis
		Document gateDocument = cacheManager.getGateDoc();
		List<Annotation> authorAnnotationList = GateUtil.getAnnInDocOrder(gateDocument, ImporterGROBID.GROBIDannSet, ImporterGROBID.GROBIDauthor);

		Long abstractStartOffset = null;
		Optional<Annotation> abstractAnnotation = GateUtil.getFirstAnnotationInDocOrder(gateDocument, ImporterGROBID.GROBIDannSet, ImporterGROBID.GROBIDabstract);
		if(abstractAnnotation.isPresent()) {
			abstractAnnotation.get();
			abstractStartOffset = abstractAnnotation.get().getStartNode().getOffset();
		}

		if(authorAnnotationList != null && authorAnnotationList.size() > 0) {
			for(Annotation authorAnn : authorAnnotationList) {
				if(authorAnn != null && (abstractStartOffset == null || (abstractStartOffset != null && authorAnn.getEndNode().getOffset() <= abstractStartOffset)) ) {
					String authorName = null;
					List<Annotation> persNameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "persName", authorAnn);
					if(persNameList != null && persNameList.size() > 0) {
						Optional<String> authNameOpt = GateUtil.getAnnotationText(persNameList.get(0), gateDocument);
						if(authNameOpt.isPresent()) {
							if (!authNameOpt.get().equals("")) {
								authorName = authNameOpt.get();
							}
						}
					}

					if(authorName != null && !authorName.trim().equals("")) {
						AuthorImpl newAuthor = new AuthorImpl(cacheManager, ObjectGenerator.normalizeText(authorName).trim(), null, null);

						// Forename
						List<Annotation> forenameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "forename", authorAnn);
						if(forenameList != null && forenameList.size() > 0) {
							Optional<String> forenameOpt = GateUtil.getAnnotationText(forenameList.get(0), gateDocument);
							if(forenameOpt.isPresent()) {
								if (!forenameOpt.get().equals("")) {
									newAuthor.setFirstName(ObjectGenerator.normalizeText(forenameOpt.get()).trim());
								}
							}
						}

						// Surname
						List<Annotation> surnameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "surname", authorAnn);
						if(surnameList != null && surnameList.size() > 0) {
							Optional<String> surnameOpt = GateUtil.getAnnotationText(surnameList.get(0), gateDocument);
							if(surnameOpt.isPresent()) {
								if (!surnameOpt.get().equals("")) {
									newAuthor.setSurname(ObjectGenerator.normalizeText(surnameOpt.get()).trim());
								}
							}
						}

						// Email
						List<Annotation> emailList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "email", authorAnn);
						if(emailList != null && emailList.size() > 0) {
							Optional<String> emailOpt = GateUtil.getAnnotationText(emailList.get(0), gateDocument);
							if(emailOpt.isPresent()) {
								if (!emailOpt.get().equals("")) {
									newAuthor.setEmail(ObjectGenerator.normalizeText(emailOpt.get()).trim());
								}
							}
						}

						// Affiliation
						List<Annotation> affiliationList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "affiliation", authorAnn);
						if(affiliationList != null && affiliationList.size() > 0) {
							for(Annotation affil : affiliationList) {
								if(affil != null) {
									InstitutionImpl newAffiliation = new InstitutionImpl(cacheManager);

									// Affiliation fields:
									// ADDED: orgName, address, country
									// TO ADD: marker, URL, labs, instits. addr, region, settlement, acronym 

									// Name
									List<Annotation> orgNameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "orgName", affil);
									if(orgNameList != null && orgNameList.size() > 0) {
										Optional<String> orgNameOpt = GateUtil.getAnnotationText(orgNameList.get(0), gateDocument);
										if(orgNameOpt.isPresent()) {
											if (!orgNameOpt.get().equals("")) {
												newAffiliation.setName(ObjectGenerator.normalizeText(orgNameOpt.get()).trim());
											}
										}
									}

									// Address
									List<Annotation> addressList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "address", affil);
									if(addressList != null && addressList.size() > 0) {
										Optional<String> addressOpt = GateUtil.getAnnotationText(addressList.get(0), gateDocument);
										if(addressOpt.isPresent()) {
											if (!addressOpt.get().equals("")) {
												newAffiliation.setAddress(ObjectGenerator.normalizeText(addressOpt.get()).trim());
											}
										}
									}

									// Country
									List<Annotation> countryList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(gateDocument, ImporterGROBID.GROBIDannSet, "country", affil);
									if(countryList != null && countryList.size() > 0) {
										Optional<String> countryOpt = GateUtil.getAnnotationText(countryList.get(0), gateDocument);
										if(countryOpt.isPresent()) {
											if (!countryOpt.get().equals("")) {
												newAffiliation.setState(ObjectGenerator.normalizeText(countryOpt.get()).trim());
											}
										}
									}

									if(newAffiliation.getName() != null && !newAffiliation.getName().equals("")) {
										newAuthor.addAffiliation(newAffiliation);
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
