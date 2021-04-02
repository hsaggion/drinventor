package edu.upf.taln.dri.lib.model.util;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.Author;
import edu.upf.taln.dri.lib.model.ext.AuthorImpl;
import edu.upf.taln.dri.lib.model.ext.InstitutionImpl;
import edu.upf.taln.dri.module.importer.jats.ImporterJATS;
import gate.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JATSAuthorExtractor implements Extractor{
	public List<Author> extract(DocCacheManager cacheManager) {
		ArrayList<Author> authorList = new ArrayList<>();

		// Retrieve author information from JATS tags
		List<Annotation> authorAnnotationList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATScontrib);

		Long abstractStartOffset = null;
		Optional<Annotation> abstractAnnotation = GateUtil.getFirstAnnotationInDocOrder(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSabstract);
		if(abstractAnnotation.isPresent()) {
			abstractStartOffset = abstractAnnotation.get().getStartNode().getOffset();
		}

		if(authorAnnotationList != null && authorAnnotationList.size() > 0) {
			for(Annotation authorAnn : authorAnnotationList) {
				if(authorAnn != null && GateUtil.getStringFeature(authorAnn, ImporterJATS.JATScontrib_authTypeFeat).orElse(null) != null &&
				   GateUtil.getStringFeature(authorAnn, ImporterJATS.JATScontrib_authTypeFeat).get().equals("author") &&
				   (abstractStartOffset == null || (abstractStartOffset != null && authorAnn.getEndNode().getOffset() <= abstractStartOffset)) ) {

					// Full name
					String authorName = null;
					List<Annotation> persNameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATScontribName, authorAnn);
					if(persNameList != null && persNameList.size() > 0) {
						Optional<String> authNameOpt = GateUtil.getAnnotationText(persNameList.get(0), cacheManager.getGateDoc());
						if(authNameOpt.isPresent()) {
							if (!authNameOpt.get().equals("")) {
								authorName = authNameOpt.get();
							}
						}
					}

					if(authorName != null && !authorName.trim().equals("")) {
						AuthorImpl newAuthor = new AuthorImpl(cacheManager, ObjectGenerator.normalizeText(authorName).trim(), null, null);

						// Forename
						List<Annotation> forenameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATScontribGivenName, authorAnn);
						if(forenameList != null && forenameList.size() > 0) {
							Optional<String> forenameOpt = GateUtil.getAnnotationText(forenameList.get(0), cacheManager.getGateDoc());
							if(forenameOpt.isPresent()) {
								if (!forenameOpt.get().equals("")) {
									newAuthor.setFirstName(ObjectGenerator.normalizeText(forenameOpt.get()).trim());
								}
							}
						}

						// Surname
						List<Annotation> surnameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATScontribSurname, authorAnn);
						if(surnameList != null && surnameList.size() > 0) {
							Optional<String> surnameOpt = GateUtil.getAnnotationText(surnameList.get(0), cacheManager.getGateDoc());
							if(surnameOpt.isPresent()) {
								if (!surnameOpt.get().equals("")) {
									newAuthor.setSurname(ObjectGenerator.normalizeText(surnameOpt.get()).trim());
								}
							}
						}

						// Xref
						List<String> xrefIDs = new ArrayList<String>();
						List<Annotation> xrefList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATScontribXref, authorAnn);
						if(xrefList != null && xrefList.size() > 0) {
							for(Annotation xrefAnn : xrefList) {
								Optional<String> xrefOpt = GateUtil.getAnnotationText(xrefAnn, cacheManager.getGateDoc());
								if(xrefOpt.isPresent()) {
									if (!xrefOpt.get().equals("")) {
										xrefIDs.add(ObjectGenerator.normalizeText(xrefOpt.get()).trim());
									}
								}
							}
						}

						// Affiliation
						List<Annotation> affiliationList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliation, authorAnn);
						if(affiliationList != null && affiliationList.size() > 0) {
							for(Annotation affil : affiliationList) {
								if(affil != null && GateUtil.getStringFeature(affil, "id").orElse(null) != null &&
								   xrefIDs.contains(GateUtil.getStringFeature(affil, "id").orElse(null))) {

									InstitutionImpl newAffiliation = new InstitutionImpl(cacheManager);

									// Affiliation fields:

									// Full text
									newAffiliation.setFullText( (GateUtil.getAnnotationText(affil, cacheManager.getGateDoc()).orElse(null) != null) ?
																ObjectGenerator.normalizeText(GateUtil.getAnnotationText(affil, cacheManager.getGateDoc()).orElse(null).trim()) : null);

									// Name
									List<Annotation> orgNameList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine_INSTITUTION, affil);
									if(orgNameList != null && orgNameList.size() > 0) {
										Optional<String> orgNameOpt = GateUtil.getAnnotationText(orgNameList.get(0), cacheManager.getGateDoc());
										if(orgNameOpt.isPresent()) {
											if (!orgNameOpt.get().equals("")) {
												newAffiliation.setName(ObjectGenerator.normalizeText(orgNameOpt.get()).trim());
											}
										}
									}

									// Address
									List<Annotation> addressList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine, affil);
									if(addressList != null && addressList.size() > 0) {
										Optional<String> addressOpt = GateUtil.getAnnotationText(addressList.get(0), cacheManager.getGateDoc());
										if(addressOpt.isPresent()) {
											if (!addressOpt.get().equals("")) {
												newAffiliation.setAddress(ObjectGenerator.normalizeText(addressOpt.get()).trim());
											}
										}
									}

									// Country
									List<Annotation> countryList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine_STATE, affil);
									if(countryList != null && countryList.size() > 0) {
										Optional<String> countryOpt = GateUtil.getAnnotationText(countryList.get(0), cacheManager.getGateDoc());
										if(countryOpt.isPresent()) {
											if (!countryOpt.get().equals("")) {
												newAffiliation.setState(ObjectGenerator.normalizeText(countryOpt.get()).trim());
											}
										}
									}

									// URL / ext-link
									List<Annotation> uriList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine_URI, affil);
									List<Annotation> extLinkList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine_EXTLINK, affil);
									if(uriList != null && uriList.size() > 0) {
										Optional<String> uriOpt = GateUtil.getAnnotationText(uriList.get(0), cacheManager.getGateDoc());
										if(uriOpt.isPresent()) {
											if (!uriOpt.get().equals("")) {
												newAffiliation.setURL(ObjectGenerator.normalizeText(uriOpt.get()).trim());
											}
										}
									}
									else if(extLinkList != null && extLinkList.size() > 0) {
										Optional<String> extLinkOpt = GateUtil.getAnnotationText(extLinkList.get(0), cacheManager.getGateDoc());
										if(extLinkOpt.isPresent()) {
											if (!extLinkOpt.get().equals("")) {
												newAffiliation.setURL(ObjectGenerator.normalizeText(extLinkOpt.get()).trim());
											}
										}
									}

									// Email of author
									List<Annotation> emailList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterJATS.JATSannSet, ImporterJATS.JATSaffiliationAddressLine_EMAIL, affil);
									if(emailList != null && emailList.size() > 0) {
										Optional<String> emailOpt = GateUtil.getAnnotationText(emailList.get(0), cacheManager.getGateDoc());
										if(emailOpt.isPresent()) {
											if (!emailOpt.get().equals("")) {
												newAuthor.setEmail(ObjectGenerator.normalizeText(emailOpt.get()).trim());
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
