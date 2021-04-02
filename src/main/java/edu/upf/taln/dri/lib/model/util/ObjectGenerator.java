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
package edu.upf.taln.dri.lib.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import edu.upf.taln.dri.lib.model.ext.Author;
import edu.upf.taln.dri.lib.model.util.extractor.Extractor;
import edu.upf.taln.dri.lib.model.util.extractor.ExtractorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.model.DocCacheManager;
import edu.upf.taln.dri.lib.model.ext.AuthorImpl;
import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.BabelSynsetOccImpl;
import edu.upf.taln.dri.lib.model.ext.CandidateTermOcc;
import edu.upf.taln.dri.lib.model.ext.CandidateTermOccImpl;
import edu.upf.taln.dri.lib.model.ext.Citation;
import edu.upf.taln.dri.lib.model.ext.CitationImpl;
import edu.upf.taln.dri.lib.model.ext.CitationMarkerImpl;
import edu.upf.taln.dri.lib.model.ext.CitationSourceENUM;
import edu.upf.taln.dri.lib.model.ext.Header;
import edu.upf.taln.dri.lib.model.ext.HeaderImpl;
import edu.upf.taln.dri.lib.model.ext.Institution;
import edu.upf.taln.dri.lib.model.ext.InstitutionImpl;
import edu.upf.taln.dri.lib.model.ext.LangENUM;
import edu.upf.taln.dri.lib.model.ext.MetaEntityTypeENUM;
import edu.upf.taln.dri.lib.model.ext.PubIdENUM;
import edu.upf.taln.dri.lib.model.ext.RhetoricalClassENUM;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.SectionImpl;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceImpl;
import edu.upf.taln.dri.lib.model.ext.Token;
import edu.upf.taln.dri.lib.model.ext.TokenImpl;
import edu.upf.taln.dri.lib.model.graph.DependencyGraph;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.SourceENUM;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import edu.upf.taln.dri.module.parser.MateParser;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;

/**
 * Generates objects of the library starting from the original GATE document annotations
 * 
 *
 */
public class ObjectGenerator {

	private static Logger logger = Logger.getLogger(ObjectGenerator	.class);

	/**
	 * Sentence Generator method
	 * 
	 * @param annotationId
	 * @param cacheManager
	 * @return
	 */
	public static Sentence getSentenceFromId(Integer annotationId, DocCacheManager cacheManager) {
		if(annotationId == null || cacheManager == null) {
			return null;
		}

		Sentence result = null;

		Sentence cachedSent = cacheManager.getCachedSentence(annotationId);
		if(cachedSent != null) {
			return cachedSent;
		}

		try {
			List<Annotation> abstractAnnotationList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.abstractAnnType);

			
			Annotation sentenceAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annotationId);
			if(sentenceAnn != null && sentenceAnn.getType().equals(ImporterBase.sentenceAnnType)) {
				SentenceImpl resultImpl = new SentenceImpl(cacheManager);

				resultImpl.setId(sentenceAnn.getId());
				
				// Set abstract ID
				resultImpl.setAbstractId(-1);
				for(int i = 0; i < abstractAnnotationList.size(); i++) {
					if(abstractAnnotationList.get(i) != null && abstractAnnotationList.get(i).overlaps(sentenceAnn)) {
						resultImpl.setAbstractId(i + 1);
						break;
					}
				}

				// Set the text
				resultImpl.setText(GateUtil.getAnnotationText(sentenceAnn, cacheManager.getGateDoc()).orElse(null));

				// Set the rhetorical class
				String rhetoricalClass = GateUtil.getStringFeature(sentenceAnn, ImporterBase.sentence_RhetoricalAnnFeat).orElse(null);
				if(rhetoricalClass != null) {
					try {
						resultImpl.setRhetoricalClass(RhetoricalClassENUM.valueOf(rhetoricalClass));
					}
					catch (Exception e) {
						Util.notifyException("Creating sentence element - rhetorical class value " + rhetoricalClass, e, logger);
					}
				}

				// Set containing section
				resultImpl.setContainingSection(ObjectGenerator.getSectionContainingSentenceId(annotationId, cacheManager));

				// Set token list
				resultImpl.setTokens(getTokensFromSentenceId(sentenceAnn.getId(), cacheManager));

				// Set citation markers
				List<Annotation> citMarkerList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.driAnnSet,
																										  ImporterBase.inlineCitationMarkerAnnType, sentenceAnn);
				for(Annotation citMarkerAnn : citMarkerList) {
					if(citMarkerAnn != null) {
						CitationMarkerImpl newCitMarker = new CitationMarkerImpl(cacheManager);
						newCitMarker.setId(citMarkerAnn.getId());

						String citMarkerId = GateUtil.getStringFeature(citMarkerAnn, ImporterBase.bibEntry_IdAnnFeat).orElse(null);
						boolean foundBibEntry = false;
						List<Annotation> bibEntryList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType);
						for(Annotation bibEntryAnn : bibEntryList) {
							String bibEntryId = GateUtil.getStringFeature(bibEntryAnn, ImporterBase.bibEntry_IdAnnFeat).orElse(null);
							if(bibEntryId != null && citMarkerId != null && citMarkerId.equals(bibEntryId)) {
								newCitMarker.setCitationId(bibEntryAnn.getId());
								foundBibEntry = true;
							}
						}

						newCitMarker.setReferenceText(GateUtil.getAnnotationText(citMarkerAnn, cacheManager.getGateDoc()).orElse(null));
						newCitMarker.setSentenceId(sentenceAnn.getId());

						if(foundBibEntry) {
							resultImpl.addCitationMarker(newCitMarker);
						}
					}
				}

				// Set candidate terms
				List<Annotation> candidateTermList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.term_AnnSet,
																											  ImporterBase.term_CandOcc, sentenceAnn);
				for(Annotation candidateTerm : candidateTermList) {
					if(candidateTerm != null) {
						CandidateTermOcc newCandidateTerm = getCandidateTermOccFromId(candidateTerm.getId(), cacheManager);
						if(newCandidateTerm != null) {
							resultImpl.addCandidateTerm(newCandidateTerm);
						}
					}
				}

				// Set Babelnet synset occurrences
				List<Annotation> babelnetSynsetOccList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.babelnet_AnnSet,
																												  ImporterBase.babelnet_DisItem, sentenceAnn);
				for(Annotation babelnetSynsetOcc : babelnetSynsetOccList) {
					if(babelnetSynsetOcc != null) {

						// Filter babelnet synsets
						Integer numTokensFeat = GateUtil.getIntegerFeature(babelnetSynsetOcc, ImporterBase.babelnet_DisItem_numTokensFeat).orElse(null);
						Double scoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOcc, ImporterBase.babelnet_DisItem_scoreFeat).orElse(null);
						Double globalScoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOcc, ImporterBase.babelnet_DisItem_golbalScoreFeat).orElse(null);
						Double coherenceScoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOcc, ImporterBase.babelnet_DisItem_coherenceScoreFeat).orElse(null);

						boolean addSynset = false;
						if(numTokensFeat != null && numTokensFeat > 1) {
							addSynset = true;
						}
						else if(scoreFeat != null && scoreFeat > 0d && globalScoreFeat != null && globalScoreFeat > 0d && coherenceScoreFeat != null && coherenceScoreFeat > 0d) {
							addSynset = true;
						}

						// Add filters babelnet entity
						if(addSynset) {
							BabelSynsetOcc newBabelnetSynsetOcc = getBabelnetSynsetOccFromId(babelnetSynsetOcc.getId(), cacheManager);
							if(newBabelnetSynsetOcc != null) {
								resultImpl.addBabelSynsetOcc(newBabelnetSynsetOcc);
							}
						}
					}
				}
				
				// Set if is acknowledgement
				String ackFeature = GateUtil.getStringFeature(sentenceAnn, ImporterBase.sentence_isAcknowledgement).orElse(null);
				if(ackFeature != null && ackFeature.toLowerCase().equals("true")) {
					resultImpl.setAck(true);
				}
				
				// Set metaEntityMap: funding agencies, projects
				Map<String, MetaEntityTypeENUM> metaEntityMap = new HashMap<String, MetaEntityTypeENUM>();
				List<Annotation> fundigAgencyAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.metaAnnotator_AnnSet, ImporterBase.metaAnnotator_FundingAgencyAnnType);
				if(fundigAgencyAnnList != null && fundigAgencyAnnList.size() > 0) {
					for(Annotation fundigAgencyAnn : fundigAgencyAnnList) {
						String fundigAgencyAnnText = GateUtil.getAnnotationText(fundigAgencyAnn, cacheManager.getGateDoc()).orElse(null);
						if(fundigAgencyAnn != null && fundigAgencyAnnText != null && !fundigAgencyAnnText.trim().equals("")) {
							metaEntityMap.put(fundigAgencyAnnText.trim(), MetaEntityTypeENUM.FundingAgency);
						}
					}
				}
				
				List<Annotation> projectsAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.metaAnnotator_AnnSet, ImporterBase.metaAnnotator_ProjectAnnType);
				if(projectsAnnList != null && projectsAnnList.size() > 0) {
					for(Annotation projectAnn : projectsAnnList) {
						String fundigAgencyAnnText = GateUtil.getAnnotationText(projectAnn, cacheManager.getGateDoc()).orElse(null);
						if(projectAnn != null && fundigAgencyAnnText != null && !fundigAgencyAnnText.trim().equals("")) {
							metaEntityMap.put(fundigAgencyAnnText.trim(), MetaEntityTypeENUM.Project);
						}
					}
				}
				resultImpl.setMetaEntityMap(metaEntityMap);
				
				// Set language
				String sentLang = GateUtil.getStringFeature(sentenceAnn, ImporterBase.langAnnFeat).orElse(null);
				if(sentLang == null) {
					resultImpl.setLanguage(LangENUM.UNSPECIFIED);
				}
				else if(sentLang.trim().toLowerCase().equals("es")) {
					resultImpl.setLanguage(LangENUM.ES);
				}
				else {
					resultImpl.setLanguage(LangENUM.EN);
				}
				
				// Add to cache
				cacheManager.cacheSentence(resultImpl);
				result = resultImpl;
			}
		}
		catch (Exception e) {
			Util.notifyException("Creating sentence element", e, logger);
		}

		return result;
	}

	/**
	 * Return the list of tokens given a sentence ID
	 * @param annotationId
	 * @param cacheManager
	 * @return
	 */
	public static List<Token> getTokensFromSentenceId(Integer sentenceId, DocCacheManager cacheManager) {
		if(sentenceId == null || cacheManager == null) {
			return null;
		}

		List<Token> result = new ArrayList<Token>();

		try {
			Annotation sentenceAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(sentenceId);
			if(sentenceAnn != null && sentenceAnn.getType().equals(ImporterBase.sentenceAnnType)) {

				List<Annotation> tokenAnnotationList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(),
																												ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sentenceAnn);

				Integer tokenPositionInSentence = 0;
				for(Annotation tokenAnn : tokenAnnotationList) {
					if(tokenAnn != null) {


						// Check if present in cache, therwise create and cache token
						if(cacheManager.getCachedToken(tokenAnn.getId()) != null) {
							result.add(cacheManager.getCachedToken(tokenAnn.getId()));
						}
						else {
							try {
								tokenPositionInSentence++;
								TokenImpl newToken = new TokenImpl(cacheManager, sentenceAnn.getId());

								newToken.setId(tokenAnn.getId());
								newToken.setContainingSentence(sentenceAnn.getId());
								newToken.setInSentencePosition(tokenPositionInSentence);
								newToken.setWord(GateUtil.getAnnotationText(tokenAnn, cacheManager.getGateDoc()).orElse(""));
								newToken.setLemma(GateUtil.getStringFeature(tokenAnn, ImporterBase.token_LemmaFeat).orElse(""));
								newToken.setPOS(GateUtil.getStringFeature(tokenAnn, ImporterBase.token_POSfeat).orElse(""));

								// Add to cache
								cacheManager.cacheToken(newToken);
								result.add(newToken);
							}
							catch (Exception e) {
								Util.notifyException("Creating token element", e, logger);
							}
						}

					}
				}
			}
		}
		catch (Exception e) {
			Util.notifyException("Creating token element list", e, logger);
		}

		return result;
	}

	/**
	 * Section Generator method
	 * 
	 * @param annotationId
	 * @param baseDoc
	 * @return
	 */
	public static Section getSectionFromId(Integer annotationId, DocCacheManager cacheManager) {
		if(annotationId == null || cacheManager == null) {
			return null;
		}

		Section result = null;

		Section cachedSect = cacheManager.getCachedSection(annotationId);
		if(cachedSect != null) {
			return cachedSect;
		}

		try {
			Annotation sectionAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annotationId);
			if(sectionAnn != null && 
					sectionAnn.getType().equals(ImporterBase.h1AnnType) ||
					sectionAnn.getType().equals(ImporterBase.h2AnnType) ||
					sectionAnn.getType().equals(ImporterBase.h3AnnType) ||
					sectionAnn.getType().equals(ImporterBase.h4AnnType) ||
					sectionAnn.getType().equals(ImporterBase.h5AnnType) ) {

				SectionImpl resultImpl = new SectionImpl(cacheManager);
				resultImpl.setId(sectionAnn.getId());
				resultImpl.setName(GateUtil.getAnnotationText(sectionAnn, cacheManager.getGateDoc()).orElse(null));

				// Retrieve level
				String sentenceLevelAnnType = "";
				switch(sectionAnn.getType()) {
				case ImporterBase.h1AnnType:
					resultImpl.setLevel(1);
					sentenceLevelAnnType = ImporterBase.h1AnnType;
					break;
				case ImporterBase.h2AnnType:
					resultImpl.setLevel(2);
					sentenceLevelAnnType = ImporterBase.h2AnnType;
					break;
				case ImporterBase.h3AnnType:
					resultImpl.setLevel(3);
					sentenceLevelAnnType = ImporterBase.h3AnnType;
					break;
				case ImporterBase.h4AnnType:
					resultImpl.setLevel(4);
					sentenceLevelAnnType = ImporterBase.h4AnnType;
					break;
				case ImporterBase.h5AnnType:
					resultImpl.setLevel(5);
					sentenceLevelAnnType = ImporterBase.h5AnnType;
					break;
				default:
					resultImpl.setLevel(-1);
				}

				// Retrieve parent ID (the immediately preceding annotation of parent level)
				if(resultImpl.getLevel() <= 1) {
					resultImpl.setParentSectionId(null);
				}
				else {
					String parentLevel = "";
					if(resultImpl.getLevel() == 2) {
						parentLevel = ImporterBase.h1AnnType;
					} 
					else if(resultImpl.getLevel() == 3) {
						parentLevel = ImporterBase.h2AnnType;
					}
					else if(resultImpl.getLevel() == 4) {
						parentLevel = ImporterBase.h3AnnType;
					}
					else if(resultImpl.getLevel() == 5) {
						parentLevel = ImporterBase.h4AnnType;
					}

					if(StringUtils.isNotBlank(parentLevel)) {
						List<Annotation> orderedHigherLevelSections = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, parentLevel);
						for(int i = 0; i < orderedHigherLevelSections.size(); i++) {
							if(orderedHigherLevelSections.get(i) != null && orderedHigherLevelSections.get(i).getEndNode().getOffset() <= sectionAnn.getStartNode().getOffset() ) {
								if( (i + 1 == orderedHigherLevelSections.size()) || 
										(orderedHigherLevelSections.get(i + 1) != null && orderedHigherLevelSections.get(i + 1).getStartNode().getOffset() >= sectionAnn.getEndNode().getOffset())) {
									resultImpl.setParentSectionId(orderedHigherLevelSections.get(i).getId());
									break;
								}
							}
						}
					}
				}

				// Retrieve the start offset of the next section annotation of the same or superior levels, if any
				long nextSameLevelAnnStartOffset = gate.Utils.lengthLong(cacheManager.getGateDoc());
				List<Annotation> orderedSameLevelSections = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, sentenceLevelAnnType);
				for(int i = 0; i < resultImpl.getLevel(); i++) {
					if(i == 1) {
						List<Annotation> orderedSameLevelSectionsToAdd = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h1AnnType);
						orderedSameLevelSections.addAll(orderedSameLevelSectionsToAdd);
					}
					else if(i == 2) {
						List<Annotation> orderedSameLevelSectionsToAdd = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h2AnnType);
						orderedSameLevelSections.addAll(orderedSameLevelSectionsToAdd);
					}
					else if(i == 3) {
						List<Annotation> orderedSameLevelSectionsToAdd = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h3AnnType);
						orderedSameLevelSections.addAll(orderedSameLevelSectionsToAdd);
					}
					else if(i == 4) {
						List<Annotation> orderedSameLevelSectionsToAdd = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h4AnnType);
						orderedSameLevelSections.addAll(orderedSameLevelSectionsToAdd);
					}
					else if(i == 5) {
						List<Annotation> orderedSameLevelSectionsToAdd = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h5AnnType);
						orderedSameLevelSections.addAll(orderedSameLevelSectionsToAdd);
					}
				}
				for(int i = 0; i < orderedSameLevelSections.size(); i++) {
					if(orderedSameLevelSections.get(i) != null && orderedSameLevelSections.get(i).getStartNode().getOffset() > sectionAnn.getEndNode().getOffset() && 
							orderedSameLevelSections.get(i).getStartNode().getOffset() < nextSameLevelAnnStartOffset) {
						nextSameLevelAnnStartOffset = orderedSameLevelSections.get(i).getStartNode().getOffset();
					}
				}


				// Retrieve children IDs (children sections annotations with id greater than the end offset of the current section
				// and lower than the start offset of the following section of the same level)
				if(resultImpl.getLevel() < 1 || resultImpl.getLevel() >= 5) {
					resultImpl.setParentSectionId(null);
				}
				else {
					String childrenLevel = "";
					if(resultImpl.getLevel() == 1) {
						childrenLevel = ImporterBase.h2AnnType;
					} 
					else if(resultImpl.getLevel() == 2) {
						childrenLevel = ImporterBase.h3AnnType;
					}
					else if(resultImpl.getLevel() == 3) {
						childrenLevel = ImporterBase.h4AnnType;
					}
					else if(resultImpl.getLevel() == 4) {
						childrenLevel = ImporterBase.h5AnnType;
					}

					if(StringUtils.isNotBlank(childrenLevel)) {
						List<Annotation> childrenLevelAnnotations = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, childrenLevel);
						for(int i = 0; i < childrenLevelAnnotations.size(); i++) {
							if(childrenLevelAnnotations.get(i) != null && 
									childrenLevelAnnotations.get(i).getStartNode().getOffset() >= sectionAnn.getEndNode().getOffset() &&
									childrenLevelAnnotations.get(i).getEndNode().getOffset() <= nextSameLevelAnnStartOffset ) {
								resultImpl.addChildSectionId(childrenLevelAnnotations.get(i).getId());
							}
						}

						// Account for the case of one level missing
						if(resultImpl.getChildrenSectionId() == null || resultImpl.getChildrenSectionId().size() == 0) {
							childrenLevel = "";
							if(resultImpl.getLevel() == 1) {
								childrenLevel = ImporterBase.h3AnnType;
							} 
							else if(resultImpl.getLevel() == 2) {
								childrenLevel = ImporterBase.h4AnnType;
							}
							else if(resultImpl.getLevel() == 3) {
								childrenLevel = ImporterBase.h5AnnType;
							}
							childrenLevelAnnotations = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, childrenLevel);
							for(int i = 0; i < childrenLevelAnnotations.size(); i++) {
								if(childrenLevelAnnotations.get(i) != null && 
										childrenLevelAnnotations.get(i).getStartNode().getOffset() >= sectionAnn.getEndNode().getOffset() &&
										childrenLevelAnnotations.get(i).getEndNode().getOffset() <= nextSameLevelAnnStartOffset ) {
									resultImpl.addChildSectionId(childrenLevelAnnotations.get(i).getId());
								}
							}
						}
					}
				}

				// Retrieve sentences IDs (all the sentence IDs with offset contained between the end of the section annotation and the start of the 
				// section annotation of the same level, if any - if not up to the end of the document)
				List<Annotation> sentenceAnnotations = GateUtil.getAnnInDocOrderContainedOffset(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, 
						sectionAnn.getEndNode().getOffset(), nextSameLevelAnnStartOffset);
				List<Integer> sentenceIds = new ArrayList<Integer>();
				for(Annotation sentenceAnnotation : sentenceAnnotations) {
					if(sentenceAnnotation != null) {
						sentenceIds.add(sentenceAnnotation.getId());
					}
				}
				resultImpl.setSentencesId(sentenceIds);
				
				// Set language
				String sentLang = GateUtil.getStringFeature(sectionAnn, ImporterBase.langAnnFeat).orElse(null);
				if(sentLang == null) {
					resultImpl.setLanguage(LangENUM.UNSPECIFIED);
				}
				else if(sentLang.trim().toLowerCase().equals("es")) {
					resultImpl.setLanguage(LangENUM.ES);
				}
				else {
					resultImpl.setLanguage(LangENUM.EN);
				}

				// Add to cache
				cacheManager.cacheSection(resultImpl);
				result = resultImpl;
			}
		}
		catch (Exception e) {
			Util.notifyException("Creating section element", e, logger);
		}

		return result;
	}

	/**
	 * Get the Section element that contains the sentence with the given Id
	 * 
	 * @param sentenceId
	 * @param cacheManager
	 * @return
	 */
	public static Section getSectionContainingSentenceId(Integer sentenceId, DocCacheManager cacheManager) {
		Section result = null;

		if(sentenceId != null && cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(sentenceId) != null) {
			Annotation sentAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(sentenceId);

			List<Annotation> allSectList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h1AnnType);
			allSectList.addAll(GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h2AnnType));
			allSectList.addAll(GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h3AnnType));
			allSectList.addAll(GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h4AnnType));
			allSectList.addAll(GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.h5AnnType));

			Annotation selectedSectAnn = null;
			for(Annotation sectAnn : allSectList) {
				if(sectAnn != null && sectAnn.getEndNode().getOffset() <= sentAnn.getStartNode().getOffset()) {
					if(selectedSectAnn == null || selectedSectAnn.getStartNode().getOffset() < sectAnn.getStartNode().getOffset()) {
						selectedSectAnn = sectAnn;
					}
				}
			}

			if(selectedSectAnn != null) {
				result = getSectionFromId(selectedSectAnn.getId(), cacheManager);
			}
		}

		return result;
	}

	/**
	 * Candidate term Generator method
	 * 
	 * @param annotationId
	 * @param baseDoc
	 * @return
	 */
	public static CandidateTermOcc getCandidateTermOccFromId(Integer annotationId, DocCacheManager cacheManager) {
		if(annotationId == null || cacheManager == null) {
			return null;
		}

		CandidateTermOcc result = null;

		CandidateTermOcc cachedCandTerm = cacheManager.getCachedCandidateTerm(annotationId);
		if(cachedCandTerm != null) {
			return cachedCandTerm;
		}

		try {
			Annotation candidateTermOccAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.term_AnnSet).get(annotationId);
			if(candidateTermOccAnn != null && candidateTermOccAnn.getType().equals(ImporterBase.term_CandOcc)) {
				CandidateTermOccImpl resultImpl = new CandidateTermOccImpl(cacheManager);

				resultImpl.setId(candidateTermOccAnn.getId());

				// Text
				resultImpl.setText(GateUtil.getAnnotationText(candidateTermOccAnn, cacheManager.getGateDoc()).orElse(null));
				if(StringUtils.isBlank(resultImpl.getText())) {
					return null;
				}

				// Sentence Id
				List<Annotation> sentencesOverlappingCandTermOcc = GateUtil.getAnnInDocOrderIntersectAnn(cacheManager.getGateDoc(), 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, candidateTermOccAnn);
				if(sentencesOverlappingCandTermOcc.size() == 1) {
					resultImpl.setInSentenceId(sentencesOverlappingCandTermOcc.get(0).getId());
				}

				// Regex pattern
				String regexPatternFeat = GateUtil.getStringFeature(candidateTermOccAnn, ImporterBase.term_CandOcc_regexPOSFeat).orElse(null);
				if(StringUtils.isNotBlank(regexPatternFeat)) {
					resultImpl.setRegexPattern(regexPatternFeat);
				}
				// Matched pattern
				String matchedPatternFeat = GateUtil.getStringFeature(candidateTermOccAnn, ImporterBase.term_CandOcc_actualPOSFeat).orElse(null);
				if(StringUtils.isNotBlank(matchedPatternFeat)) {
					resultImpl.setMatchedPattern(matchedPatternFeat);
				}

				// Add to cache
				cacheManager.cacheCandidateTerm(resultImpl);
				result = resultImpl;
			}
		}
		catch (Exception e) {
			Util.notifyException("Creating candidate term occurrence element", e, logger);
		}

		return result;
	}


	/**
	 * Babelnet synset occurrence Generator method
	 * 
	 * @param annotationId
	 * @param baseDoc
	 * @return
	 */
	public static BabelSynsetOcc getBabelnetSynsetOccFromId(Integer annotationId, DocCacheManager cacheManager) {
		if(annotationId == null || cacheManager == null) {
			return null;
		}

		BabelSynsetOcc result = null;

		BabelSynsetOcc cachedBabelnetSynsetOcc = cacheManager.getCachedBabelSynsetOcc(annotationId);
		if(cachedBabelnetSynsetOcc != null) {
			return cachedBabelnetSynsetOcc;
		}

		try {
			Annotation babelnetSynsetOccAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.babelnet_AnnSet).get(annotationId);
			if(babelnetSynsetOccAnn != null && babelnetSynsetOccAnn.getType().equals(ImporterBase.babelnet_DisItem)) {
				BabelSynsetOccImpl resultImpl = new BabelSynsetOccImpl(cacheManager);

				resultImpl.setId(babelnetSynsetOccAnn.getId());

				// Text
				resultImpl.setText(GateUtil.getAnnotationText(babelnetSynsetOccAnn, cacheManager.getGateDoc()).orElse(null));
				if(StringUtils.isBlank(resultImpl.getText())) {
					return null;
				}

				// Sentence Id
				List<Annotation> sentencesOverlappingBabelnetSynsetOcc = GateUtil.getAnnInDocOrderIntersectAnn(cacheManager.getGateDoc(), 
						ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, babelnetSynsetOccAnn);
				if(sentencesOverlappingBabelnetSynsetOcc.size() == 1) {
					resultImpl.setInSentenceId(sentencesOverlappingBabelnetSynsetOcc.get(0).getId());
				}

				// Babelnet URL
				String babelnetURLfeat = GateUtil.getStringFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_babelnetURLfeat).orElse(null);
				if(!StringUtils.isBlank(babelnetURLfeat)) {
					resultImpl.setBabelURL(babelnetURLfeat);
				}

				// DBpedia URL
				String dbpediaURLfeat = GateUtil.getStringFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_dbpediaURLfeat).orElse(null);
				if(!StringUtils.isBlank(dbpediaURLfeat)) {
					resultImpl.setDbpediaURL(dbpediaURLfeat);
				}

				// Synset ID
				String synsetIDfeat = GateUtil.getStringFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_synsetIDfeat).orElse(null);
				if(!StringUtils.isBlank(synsetIDfeat)) {
					resultImpl.setSynsetID(synsetIDfeat);
				}

				// Source
				String sourceFeat = GateUtil.getStringFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_sourceFeat).orElse(null);
				if(!StringUtils.isBlank(sourceFeat)) {
					resultImpl.setSource(synsetIDfeat);
				}

				// Num tokens
				Integer numTokensFeat = GateUtil.getIntegerFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_numTokensFeat).orElse(null);
				Integer scoreMultiplier = 0;
				if(numTokensFeat != null) {
					resultImpl.setNumTokens(numTokensFeat);
					if(numTokensFeat == 2) {
						scoreMultiplier = 4;
					}
					else if(numTokensFeat == 2) {
						scoreMultiplier = 8;
					}
				}

				// Global score
				Double globalScoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_golbalScoreFeat).orElse(null);
				if(globalScoreFeat != null) {
					if(globalScoreFeat == 0d && numTokensFeat != null && numTokensFeat > 1) globalScoreFeat = 0.1d;  
					resultImpl.setGolbalScore(globalScoreFeat);
					for(int i = 0; i < scoreMultiplier; i++) resultImpl.setGolbalScore(resultImpl.getGolbalScore() + globalScoreFeat);
				}

				// Score
				Double scoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_scoreFeat).orElse(null);
				if(scoreFeat != null) {
					if(scoreFeat == 0d && numTokensFeat != null && numTokensFeat > 1) scoreFeat = 0.1d;
					resultImpl.setScore(scoreFeat);
					for(int i = 1; i < scoreMultiplier; i++) resultImpl.setGolbalScore(resultImpl.getScore() + scoreFeat);
				}

				// Coherence score
				Double coherenceScoreFeat = GateUtil.getDoubleFeature(babelnetSynsetOccAnn, ImporterBase.babelnet_DisItem_coherenceScoreFeat).orElse(null);
				if(coherenceScoreFeat != null) {
					if(coherenceScoreFeat == 0d && numTokensFeat != null && numTokensFeat > 1) coherenceScoreFeat = 0.1d;
					resultImpl.setCoherenceScore(coherenceScoreFeat);
					for(int i = 1; i < scoreMultiplier; i++) resultImpl.setCoherenceScore(resultImpl.getScore() + coherenceScoreFeat);
				}

				// Add to cache
				cacheManager.cacheBabelSynsetOcc(resultImpl);
				result = resultImpl;
			}
		}
		catch (Exception e) {
			Util.notifyException("Creating Babelnet synset occurrence element", e, logger);
		}

		return result;
	}


	/**
	 * Dependency Graph of sentence Generator method
	 * 
	 * @param annotationId
	 * @param cacheManager
	 * @param mergeNodes
	 * @return
	 */
	public static DependencyGraph getDepGraphFromSentId(Integer annotationId, DocCacheManager cacheManager, boolean mergeNodes) {
		if(annotationId == null || cacheManager == null) {
			return null;
		}

		DependencyGraph result = new DependencyGraph();

		try {
			Annotation sentenceAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType).get(annotationId);

			if(sentenceAnn != null) {
				List<Annotation> tokenSentence = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(),
																										  ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sentenceAnn);

				// Generate node list to be added as node feature
				List<String> nodesList = new ArrayList<String>();
				for(Annotation token : tokenSentence) {
					if(token != null && token.getId() != null && result.getNodeName(token.getId()) == null) {
						nodesList.add(GateUtil.getAnnotationText(token, cacheManager.getGateDoc()).orElse(""));
					}
				}

				// Add nodes with their features
				int sentOrder = 0;
				for(Annotation token : tokenSentence) {
					if(token != null && token.getId() != null && result.getNodeName(token.getId()) == null) {
						// Populate the list of IDs of the coreference chains in which the node is the head of one of their elements
						// Usually it should be a one element List since each node can be head of one element of a specific coreference chain
						Set<Integer> coreferenceChainIDs = new HashSet<Integer>();

						// Retrieve the co-reference node name (aggregates all the tokens of the coreference chain element)
						String coreferenceNodeName = "";

						Set<String> corefChainNames = cacheManager.getGateDoc().getAnnotations(ImporterBase.coref_ChainAnnSet).getAllTypes();
						for(String corefChainName : corefChainNames) {
							if(corefChainName != null && !corefChainName.equals("")) {
								List<Annotation> corefChainElements = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.coref_ChainAnnSet, corefChainName);

								// The coreference chain id is the ID of the first element of the coreference chain
								Integer corefChainID = null;

								if(corefChainElements != null && corefChainElements.size() > 0) {
									for(Annotation corefChainElement : corefChainElements) {
										if(corefChainElement != null) {

											if(corefChainID == null) {
												corefChainID = corefChainElement.getId();
											}

											Integer headIDofCorefElem = GateUtil.getIntegerFeature(corefChainElement, "headID").orElse(null);
											// If the token is head of the current coreference chain element (corefChainElement)
											if(corefChainID != null && headIDofCorefElem != null && headIDofCorefElem.equals(token.getId())) {

												// Add coref chain ID
												coreferenceChainIDs.add(corefChainID);

												// Add coref chain text
												String textOfCorefElem = GateUtil.getAnnotationText(corefChainElement, cacheManager.getGateDoc()).orElse(null);
												if(textOfCorefElem != null) {
													if(coreferenceNodeName.equals("")) {
														coreferenceNodeName += textOfCorefElem;
													}
													else {
														coreferenceNodeName += "___" + textOfCorefElem;
													}
												}

												break; // Since each token can be the head of only one coreference chain element per coreference chain
											}
										}
									}
								}

							}
						}

						if(coreferenceNodeName.equals("")) {
							if(coreferenceChainIDs != null && coreferenceChainIDs.size() > 0) {
								String tokenText = GateUtil.getAnnotationText(token, cacheManager.getGateDoc()).orElse(null);
								logger.debug("ATTNETION: not possible to determine the coreference node name of token " + ((tokenText != null) ? tokenText : "_NO_TOKEN_TEXT_") 
										+ " with id " + ((token.getId() != null) ? token.getId() : "_NO_TOKEN_ID_") + " - the token belongs to the following coreference cains: "
										+ ((coreferenceChainIDs != null) ? coreferenceChainIDs : "_NONE_"));
							}
							coreferenceNodeName = null;
						}

						Integer newNodeId = result.addNode(token.getId(), GateUtil.getAnnotationText(token, cacheManager.getGateDoc()).orElse(null), 
								GateUtil.getStringFeature(token, MateParser.posFeat).orElse(null),
								GateUtil.getStringFeature(token, MateParser.lemmaFeat).orElse(null),
								coreferenceChainIDs,
								coreferenceNodeName,
								sentOrder++);

						// Set independent properties of the node:
						//   - indipProp_rhetClass
						if(cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annotationId) != null) {
							String rhetoricalClass = GateUtil.getStringFeature(cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annotationId), ImporterBase.sentence_RhetoricalAnnFeat).orElse(null);
							result.addRhetoricalClass(newNodeId, rhetoricalClass, true);
						}
						//    - sentence ID / node list pair
						if(nodesList != null) {
							result.setSentenceIDTokensPair(newNodeId, annotationId, nodesList);
						}
					}
				}

				// Add dependency edges
				for(Annotation token : tokenSentence) {
					if(token != null && token.getId() != null && GateUtil.getIntegerFeature(token, MateParser.depTargetIdFeat).orElse(null) != null) {
						result.addEdge(GateUtil.getStringFeature(token, MateParser.depKindFeat).orElse(null), token.getId(), GateUtil.getIntegerFeature(token, MateParser.depTargetIdFeat).orElse(null), false);
					}
				}

				// Add SRL edges
				for(Annotation token : tokenSentence) {
					if(token != null && token.getId() != null && result.getNodeName(token.getId()) != null) {
						boolean SRLedgeAdded = true;
						int SRLindex = 1;
						while(SRLedgeAdded) {
							String SRL_sense = GateUtil.getStringFeature(token, MateParser.SRLpartSenseNFeat + SRLindex).orElse(null);
							String SRL_tag = GateUtil.getStringFeature(token, MateParser.SRLpartTagNFeat + SRLindex).orElse(null);
							Integer SRL_root = GateUtil.getIntegerFeature(token, MateParser.SRLpartRoodIdNFeat + SRLindex).orElse(null);

							if(SRL_root != null && SRL_sense != null && SRL_tag != null && result.getNodeName(SRL_root) != null) {
								String edgeName = SRL_sense + "___" + SRL_tag;
								Integer edgeId = result.addEdge(edgeName, token.getId(), SRL_root, true);
								if(edgeId != null) {
									result.setEdgeSRLsenseAndTag(edgeId, SRL_tag, SRL_sense, annotationId);
								}
							}
							else {
								SRLedgeAdded = false;
							}
							SRLindex++;

							if(SRLindex > 30) break;
						}
					}
				}

				// Merging nodes - START
				if(mergeNodes) {
					// Merge nodes of the sentence graph
					int mergedNodeCount = 0;
					for(Integer graphEdgeID : result.getAllOrderedDepthFirstEdges()) {
						if(graphEdgeID != null) {

							String sourceNodePOS_APPO = result.getNodePOS(result.getEdgeFromNode(graphEdgeID));
							String destinationNodePOS_APPO = result.getNodePOS(result.getEdgeToNode(graphEdgeID));
							logger.debug("Analyzing edge: " + result.getNodeName(result.getEdgeFromNode(graphEdgeID)) + " (" + sourceNodePOS_APPO + ") --> " + result.getEdgeName(graphEdgeID)
									+ " --> " + result.getNodeName(result.getEdgeToNode(graphEdgeID)) + " (" + destinationNodePOS_APPO + ")");

							// Skip merging nodes of the same sentence if any of them belongs to a coreference chain - has at least one chain ID associated
							Set<Integer> fromNodeCorefChainIds = result.getNodeCorefID(result.getEdgeFromNode(graphEdgeID));
							Set<Integer> toNodeCorefChainIds = result.getNodeCorefID(result.getEdgeToNode(graphEdgeID));

							if((fromNodeCorefChainIds != null && fromNodeCorefChainIds.size() > 0) || (toNodeCorefChainIds != null && toNodeCorefChainIds.size() > 0)) {
								logger.debug("SKIPPED - coref.");
								continue;
							}

							String edgeName = result.getEdgeName(graphEdgeID);

							// Merge the origin with the destination of IM edges: give --> IM --> to ==> to give
							// Where the origin has POS equal to VB (verb base form) and the destination has POS equal to TO
							if(edgeName != null && edgeName.equals("IM")) {
								String sourceNodePOS = result.getNodePOS(result.getEdgeFromNode(graphEdgeID));
								String destinationNodePOS = result.getNodePOS(result.getEdgeToNode(graphEdgeID));
								if(sourceNodePOS != null && destinationNodePOS != null && sourceNodePOS.equals("VB") && destinationNodePOS.equals("TO")) {
									result.mergeNodes(result.getEdgeToNode(graphEdgeID), result.getEdgeFromNode(graphEdgeID), null);
									mergedNodeCount++;
									logger.debug("MERGED");
								}
							}

							// Merge the origin with the destination of VC edges: not --> ADV --> will ==> will not
							// Where the origin has POS starting with RB (adverb. comparative and superlative) or is equal to RP (particle)
							if(edgeName != null && edgeName.equals("ADV")) {
								String sourceNodePOS = result.getNodePOS(result.getEdgeFromNode(graphEdgeID));
								if(sourceNodePOS != null && (sourceNodePOS.startsWith("RB") || sourceNodePOS.equals("RP"))) {
									result.mergeNodes(result.getEdgeToNode(graphEdgeID), result.getEdgeFromNode(graphEdgeID), null);
									mergedNodeCount++;
									logger.debug("MERGED");
								}
							}

							// Merge the origin with the destination of VC edges: affect --> VC --> will ==> will affect
							if(edgeName != null && edgeName.equals("VC")) {
								result.mergeNodes(result.getEdgeToNode(graphEdgeID), result.getEdgeFromNode(graphEdgeID), null);
								mergedNodeCount++;
								logger.debug("MERGED");
							}

							// Merge the origin with the destination of PRD edges: harmless --> PRD --> are ==> are harmless
							// Where the origin has POS starting with J (adjective, comparative and superlative) or is equal to VBN (verb, past participle)
							// Where the destination has POS starting with V (verb)
							if(edgeName != null && edgeName.equals("PRD")) {
								String sourceNodePOS = result.getNodePOS(result.getEdgeFromNode(graphEdgeID));
								String destinationNodePOS = result.getNodePOS(result.getEdgeToNode(graphEdgeID));
								if(sourceNodePOS != null && destinationNodePOS != null && (sourceNodePOS.startsWith("J") || sourceNodePOS.equals("VBN")) && destinationNodePOS.startsWith("V")) {
									result.mergeNodes(result.getEdgeToNode(graphEdgeID), result.getEdgeFromNode(graphEdgeID), null);
									mergedNodeCount++;
									logger.debug("MERGED");
								}
							}

							// Merge the origin with the destination of IM edges: to --> OPRD --> offered ==> offered to
							// Where the origin has POS equal to TO and the destination has POS starting with V (verb)
							if(edgeName != null && (edgeName.equals("PRD") || edgeName.equals("OPRD"))) {
								String sourceNodePOS = result.getNodePOS(result.getEdgeFromNode(graphEdgeID));
								String destinationNodePOS = result.getNodePOS(result.getEdgeToNode(graphEdgeID));
								if(sourceNodePOS != null && destinationNodePOS != null && sourceNodePOS.equals("TO") && destinationNodePOS.startsWith("V")) {
									result.mergeNodes(result.getEdgeToNode(graphEdgeID), result.getEdgeFromNode(graphEdgeID), null);
									mergedNodeCount++;
									logger.debug("MERGED");
								}
							}
						}
					}
					logger.info("Sentence: " + annotationId + ", number of merged nodes: " + mergedNodeCount);
				}
				// Merging nodes - END


				// Set the head word of each node (after eventually merging all nodes) - START
				Set<Integer> nodeIDs = result.getNodesByNameRegExp(".*");
				for(Integer nodeID : nodeIDs) {
					if(nodeID != null && result.getNodeName(nodeID) != null) {
						// This map has as keys all the GATE Token IDs of the GATE Tokens merged in the node
						Map<Integer, String> mergedNodeIDmap = result.getMergedIDmap(nodeID);

						if(mergedNodeIDmap != null) {

							if(mergedNodeIDmap.size() == 1) { // No merged nodes
								String headWord = "";
								for(Entry<Integer, String> mergedNodeIDentry : mergedNodeIDmap.entrySet()) {
									if(mergedNodeIDentry != null && mergedNodeIDentry.getValue() != null) {
										headWord = mergedNodeIDentry.getValue();
									}
								}

								result.addHeadWord(nodeID, headWord, true);
							}
							else { // The node has been derived by merging more single token sentence nodes,
								// as a consequence we need to find which token is the head work of this node
								List<Annotation> candidateHeadWordTokenAnnList = new ArrayList<Annotation>();

								AnnotationSet driAnnSet = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet);
								AnnotationSet tokenAnnSet = (driAnnSet != null) ? driAnnSet.get(ImporterBase.tokenAnnType) : null;
								for(Entry<Integer, String> mergedNodeIDentry : mergedNodeIDmap.entrySet()) {
									if(mergedNodeIDentry != null && mergedNodeIDentry.getKey() != null && tokenAnnSet != null) {
										Annotation tokenAnn = tokenAnnSet.get(mergedNodeIDentry.getKey());
										if(tokenAnn != null) {
											candidateHeadWordTokenAnnList.add(tokenAnn);
										}
									}
								}

								// Got all the candidate tokens that could represent the head word in candidateHeadWordTokenAnnList
								Annotation headWordToken = null;
								if(candidateHeadWordTokenAnnList != null && candidateHeadWordTokenAnnList.size() > 0) {
									for(Annotation candidateHeadWordTokenAnn : candidateHeadWordTokenAnnList) {
										if(candidateHeadWordTokenAnn != null) {

											String posOfToken = GateUtil.getStringFeature(candidateHeadWordTokenAnn, ImporterBase.token_POSfeat).orElse(null);
											String depFunctOfToken = GateUtil.getStringFeature(candidateHeadWordTokenAnn, MateParser.depKindFeat).orElse(null);
											Integer depTargetIdOfToken = GateUtil.getIntegerFeature(candidateHeadWordTokenAnn, MateParser.depTargetIdFeat).orElse(null);

											// 1) A head word could be the ROOT one among the candidateHeadWordTokenAnnList
											if(depFunctOfToken != null && depFunctOfToken.trim().equals("ROOT")) {
												headWordToken = candidateHeadWordTokenAnn;
												break;
											}

											// 2) A head word could be a token that depends on a token that is external with respect to the candidateHeadWordTokenAnnList
											boolean headTokenDependingOnExternalToken = true;
											if(depTargetIdOfToken != null) {
												for(Annotation candidateHeadWordTokenAnnInt : candidateHeadWordTokenAnnList) {
													if(candidateHeadWordTokenAnnInt != null && candidateHeadWordTokenAnnInt.getId().equals(depTargetIdOfToken)) {
														headTokenDependingOnExternalToken = false;
													}
												}
											}
											if(headTokenDependingOnExternalToken) {
												headWordToken = candidateHeadWordTokenAnn;
												break;
											}

										}
									}
								}

								// Here, if any, I got the head word token among the tokens associated to the node

								if(headWordToken != null && GateUtil.getAnnotationText(headWordToken, cacheManager.getGateDoc()).orElse(null) != null &&
										!GateUtil.getAnnotationText(headWordToken, cacheManager.getGateDoc()).orElse(null).equals("")) {
									result.addHeadWord(nodeID, GateUtil.getAnnotationText(headWordToken, cacheManager.getGateDoc()).orElse(""), true);
								}
							}
						}

					}
				}
				// Set the head word of each node (after eventually merging all nodes) - END


				// Add causality relations - START
				//    - every (cause -> effect) pair has an id (map key) equal to the lower annotation id of all its annotations
				AnnotationSet causalityAnnSet = cacheManager.getGateDoc().getAnnotations(ImporterBase.causality_AnnSet);
				if(causalityAnnSet != null && causalityAnnSet.size() > 0) {
					List<Annotation> causalityAnnotationList = gate.Utils.inDocumentOrder(causalityAnnSet);

					// Group the annotations of each (cause -> effect) pair - the id of the group is the lower id of
					// all the annotations of the same group
					Map<Integer, List<Annotation>> causalityGroupsMap = new HashMap<Integer, List<Annotation>>();

					List<Annotation> causalityAnnGroupAnns = null;
					Set<String> causalityAnnGroupTypes = null;
					Integer causalityAnnGroupId = null;
					for(Annotation causalityAnn : causalityAnnotationList) {
						if(causalityAnn != null && causalityAnn.getType() != null && !causalityAnn.getType().equals("")) {

							boolean beginningOfNewGroupDetected = false;
							if( ( causalityAnnGroupTypes != null && causalityAnnGroupId != null && causalityAnnGroupAnns != null &&
									causalityAnnGroupTypes.contains(causalityAnn.getType()) ) ) {
								// Store old group
								causalityGroupsMap.put(causalityAnnGroupId, causalityAnnGroupAnns);
								beginningOfNewGroupDetected = true;
							}

							// If the beginning of a new group has been detected or I'm on the first iteration
							if( beginningOfNewGroupDetected || causalityAnnGroupTypes == null || 
									causalityAnnGroupId == null || causalityAnnGroupAnns == null ) {
								// Reset grouping variables
								causalityAnnGroupTypes = new HashSet<String>();
								causalityAnnGroupTypes.add(causalityAnn.getType());
								causalityAnnGroupAnns = new ArrayList<Annotation>();
								causalityAnnGroupAnns.add(causalityAnn);
								causalityAnnGroupId = causalityAnn.getId();
							}
							else { // If an existing group has to be extended with the current causalityAnn
								causalityAnnGroupTypes.add(causalityAnn.getType());
								causalityAnnGroupAnns.add(causalityAnn);
								causalityAnnGroupId = (causalityAnnGroupId > causalityAnn.getId()) ? causalityAnn.getId() : causalityAnnGroupId;
							}
						}
					}

					if(causalityAnnGroupAnns != null && causalityAnnGroupAnns.size() > 0 && causalityAnnGroupId != null) {
						causalityGroupsMap.put(causalityAnnGroupId, causalityAnnGroupAnns);
					}

					// Here got the causality groups in the map causalityGroupsMap

					// Select causality groups that intersect the current sentence in at least one annotation
					Set<Integer> causalityGroupsIntersectingSent = new HashSet<Integer>();
					for(Entry<Integer, List<Annotation>> causalityGroupsMapEntry : causalityGroupsMap.entrySet()) {
						if(causalityGroupsMapEntry.getKey() != null && causalityGroupsMapEntry.getValue() != null && causalityGroupsMapEntry.getValue().size() > 0) {
							boolean intersectingSentence = false;
							for(Annotation causalityAnnOfGroup : causalityGroupsMapEntry.getValue()) {
								if(causalityAnnOfGroup != null && 
										( ( causalityAnnOfGroup.getStartNode().getOffset() > sentenceAnn.getStartNode().getOffset() && causalityAnnOfGroup.getStartNode().getOffset() < sentenceAnn.getEndNode().getOffset() ) || 
												( causalityAnnOfGroup.getEndNode().getOffset() > sentenceAnn.getStartNode().getOffset() && causalityAnnOfGroup.getEndNode().getOffset() < sentenceAnn.getEndNode().getOffset() ) )
										) {
									intersectingSentence = true;
									break;
								}
							}

							if(intersectingSentence) {
								causalityGroupsIntersectingSent.add(causalityGroupsMapEntry.getKey());
							}
						}
					}

					// Here got the causality groups that intersect the sentence under analysis in the map causalityGroupsIntersectingSent

					// Check if some node of the sentence is in a cause / effect annotation
					// 1) Get nodes of dependency graph
					Set<Integer> resultGraphNodeIDs = new HashSet<Integer>();
					for(Entry<Integer, Pair<Integer, Integer>> edgeElement : result.getAllEdges().entrySet()) {
						if(edgeElement != null && edgeElement.getValue() != null &&
								edgeElement.getValue().getLeft() != null && edgeElement.getValue().getRight() != null) {
							resultGraphNodeIDs.add(edgeElement.getValue().getLeft());
							resultGraphNodeIDs.add(edgeElement.getValue().getRight());
						}
					}

					// 2) For each group of causal relations that intersects the sentence, go through all the elements (cause, effect) and mark the overlapping graph nodes
					if(causalityGroupsIntersectingSent != null && causalityGroupsIntersectingSent.size() > 0) {
						for(Integer causalityGroupID : causalityGroupsIntersectingSent) {
							if(causalityGroupID != null && causalityGroupsMap != null && causalityGroupsMap.containsKey(causalityGroupID) && 
									causalityGroupsMap.get(causalityGroupID) != null && causalityGroupsMap.get(causalityGroupID).size() > 1) {
								List<Annotation> causalityGroupMapAnn = causalityGroupsMap.get(causalityGroupID);

								// Get the ID of token that constitutes the representative node of the cause
								Integer causeID = null;
								for(Annotation causalityAnn : causalityGroupMapAnn) {
									if(causalityAnn != null && causalityAnn.getType() != null && !causalityAnn.getType().equals("")) {
										if(causalityAnn.getType().trim().equals("CAUSE") || causalityAnn.getType().trim().equals("CAUSE_antecedent") || causalityAnn.getType().trim().equals("CAUSE_MOTIVATION")) {

											// Check for cause head annotations
											Annotation causeHead = null;
											for(Annotation causalityAnnInt : causalityGroupMapAnn) {
												if(causalityAnnInt != null && causalityAnnInt.getType() != null && causalityAnnInt.getType().equals("CAUSE_head")) {
													causeHead = causalityAnnInt;
													break;
												}
											}

											// Found head of the cause, set as causeID node
											if(causeHead != null) {
												// From cause head ID to the corresponding token ID (we suppose that the cause head annotations corresponds to one token)
												List<Annotation> tokensInCausalityAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.tokenAnnType, causeHead);

												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0) {
													Annotation causeAnn = tokensInCausalityAnnList.iterator().next(); 
													if(causeAnn != null) {
														causeID = causeAnn.getId();
													}
												}
											}
											else {
												// Search for the right verb token of the causalityAnn as causeVerb
												Annotation causeVerb = null;
												List<Annotation> tokensInCausalityAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.tokenAnnType, causalityAnn);
												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0) {
													for(Annotation tokensInCausalityAnn : tokensInCausalityAnnList) {
														if(tokensInCausalityAnn != null) {
															String posOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, ImporterBase.token_POSfeat).orElse(null);
															String depFunctOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, MateParser.depKindFeat).orElse(null);
															Integer depTargetIdOfToken = GateUtil.getIntegerFeature(tokensInCausalityAnn, MateParser.depTargetIdFeat).orElse(null);
															if(posOfToken != null && posOfToken.startsWith("V")) {
																// 1) A cause verb could be the ROOT one of the causalityAnn where it occurs
																if(depFunctOfToken != null && depFunctOfToken.trim().equals("ROOT")) {
																	causeVerb = tokensInCausalityAnn;
																	break;
																}

																// 2) A cause verb could be a verb that depends on a token that is external with respect to the causalityAnn tokens
																boolean verbDependingOnExternalToken = true;
																if(depTargetIdOfToken != null) {
																	for(Annotation tokensInCausalityAnnInt : tokensInCausalityAnnList) {
																		if(tokensInCausalityAnnInt != null && tokensInCausalityAnnInt.getId().equals(depTargetIdOfToken)) {
																			verbDependingOnExternalToken = false;
																		}
																	}
																}
																if(verbDependingOnExternalToken) {
																	causeVerb = tokensInCausalityAnn;
																	break;
																}
															}
														}
													}
												}

												// If no causeVerb has been identified, get the first verb token intersecting causalityAnn
												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0 && causeVerb == null) {
													for(Annotation tokensInCausalityAnn : tokensInCausalityAnnList) {
														if(tokensInCausalityAnn != null) {
															String posOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, ImporterBase.token_POSfeat).orElse(null);
															if(posOfToken != null && posOfToken.startsWith("V")) {
																causeVerb = tokensInCausalityAnn;
																break;
															}
														}
													}
												}

												// Here got the causeVerb

												if(causeVerb != null && causeVerb.getId() != null) {
													causeID = causeVerb.getId();
												}
											}

										}
									}
								}

								// Get the ID of token that constitutes the representative node of the effect
								Integer effectID = null;
								for(Annotation causalityAnn : causalityGroupMapAnn) {
									if(causalityAnn != null && causalityAnn.getType() != null && !causalityAnn.getType().equals("")) {
										if(causalityAnn.getType().trim().equals("EFFECT") || causalityAnn.getType().trim().equals("EFFECT_antecedent") || causalityAnn.getType().trim().equals("EFFECT_MOTIVATION")) {

											// Check for effect head annotations
											Annotation effectHead = null;
											for(Annotation causalityAnnInt : causalityGroupMapAnn) {
												if(causalityAnnInt != null && causalityAnnInt.getType() != null && causalityAnnInt.getType().equals("EFFECT_head")) {
													effectHead = causalityAnnInt;
													break;
												}
											}

											// Found head of the cause, set as causeID node
											if(effectHead != null) {
												// From effect head ID to the corresponding token ID (we suppose that the effect head annotations corresponds to one token)
												List<Annotation> tokensInCausalityAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.tokenAnnType, effectHead);

												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0) {
													Annotation effectAnn = tokensInCausalityAnnList.iterator().next();
													if(effectAnn != null) {
														effectID = effectAnn.getId();
													}
												}
											}
											else {
												// Search for the right verb token of the causalityAnn as causeVerb
												Annotation effectVerb = null;
												List<Annotation> tokensInCausalityAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.tokenAnnType, causalityAnn);
												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0) {
													for(Annotation tokensInCausalityAnn : tokensInCausalityAnnList) {
														if(tokensInCausalityAnn != null) {
															String posOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, ImporterBase.token_POSfeat).orElse(null);
															String depFunctOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, MateParser.depKindFeat).orElse(null);
															Integer depTargetIdOfToken = GateUtil.getIntegerFeature(tokensInCausalityAnn, MateParser.depTargetIdFeat).orElse(null);
															if(posOfToken != null && posOfToken.startsWith("V")) {
																// 1) A cause verb could be the ROOT one of the causalityAnn where it occurs
																if(depFunctOfToken != null && depFunctOfToken.trim().equals("ROOT")) {
																	effectVerb = tokensInCausalityAnn;
																	break;
																}

																// 2) A cause verb could be a verb that depends on a token that is external with respect to the causalityAnn tokens
																boolean verbDependingOnExternalToken = true;
																if(depTargetIdOfToken != null) {
																	for(Annotation tokensInCausalityAnnInt : tokensInCausalityAnnList) {
																		if(tokensInCausalityAnnInt != null && tokensInCausalityAnnInt.getId().equals(depTargetIdOfToken)) {
																			verbDependingOnExternalToken = false;
																		}
																	}
																}
																if(verbDependingOnExternalToken) {
																	effectVerb = tokensInCausalityAnn;
																	break;
																}
															}
														}
													}
												}

												// If no causeVerb has been identified, get the first verb token intersecting causalityAnn
												if(tokensInCausalityAnnList != null && tokensInCausalityAnnList.size() > 0 && effectVerb == null) {
													for(Annotation tokensInCausalityAnn : tokensInCausalityAnnList) {
														if(tokensInCausalityAnn != null) {
															String posOfToken = GateUtil.getStringFeature(tokensInCausalityAnn, ImporterBase.token_POSfeat).orElse(null);
															if(posOfToken != null && posOfToken.startsWith("V")) {
																effectVerb = tokensInCausalityAnn;
																break;
															}
														}
													}
												}

												// Here got the causeVerb

												if(effectVerb != null && effectVerb.getId() != null) {
													effectID = effectVerb.getId();
												}
											}

										}
									}
								}


								// Here got the IDs of both cause (causeID) and effect (effectID)
								// causalityGroupID: Id of the causality group 
								// resultGraphNodeIDs: all the IDs of the nodes of the dependency graph
								if(causeID != null && effectID != null && causalityGroupID != null) {
									// Here found the causal relation with ID causalityGroupID, cause expressed by the node causeID 
									// and effect expressed by the effectID

									// Add causal relation information to cause and effect nodes

									// CAUSE NODE
									Integer realCauseNodeID = null;
									boolean causeNodeFromAnotherSentence = false;
									if(resultGraphNodeIDs.contains(causeID)) {
										realCauseNodeID = causeID;
									}
									else { // The causeID node (GATE XML Token ID) has been merged with other nodes and there is no more a node
										// of the dependency graph with that ID

										// Search for all the nodes of the current dependency graph, which ones derives from
										// the merging of the causeID node with other ones
										for(Integer resultGraphNodeID : resultGraphNodeIDs) {
											if(result.getNodeName(resultGraphNodeID) != null) {
												Map<Integer, String> mergedNodesInResultGraphNodeIDmap = result.getMergedIDmap(resultGraphNodeID);

												if(mergedNodesInResultGraphNodeIDmap != null && mergedNodesInResultGraphNodeIDmap.size() > 0) {
													for(Entry<Integer, String> mergedNodeElem : mergedNodesInResultGraphNodeIDmap.entrySet()) {
														if(mergedNodeElem != null && mergedNodeElem.getKey() != null && mergedNodeElem.getKey().equals(causeID)) {
															realCauseNodeID = resultGraphNodeID;
															break;
														}
													}
												}
											}

											if(realCauseNodeID != null) {
												break;
											}
										}

									}

									if(realCauseNodeID == null) {
										logger.debug("Cause node from another sentence.");
										causeNodeFromAnotherSentence = true;
										realCauseNodeID = causeID;
									}


									// EFFECT NODE
									Integer realEffectNodeID = null;
									boolean effectNodeFromAnotherSentence = false;
									if(resultGraphNodeIDs.contains(effectID)) {
										realEffectNodeID = effectID;
									}
									else { // The effectID node (GATE XML Token ID) has been merged with other nodes and there is no more a node
										// of the dependency graph with that ID

										// Search for all the nodes of the current dependency graph, which ones derives from
										// the merging of the effectID node with other ones
										for(Integer resultGraphNodeID : resultGraphNodeIDs) {
											if(result.getNodeName(resultGraphNodeID) != null) {
												Map<Integer, String> mergedNodesInResultGraphNodeIDmap = result.getMergedIDmap(resultGraphNodeID);

												if(mergedNodesInResultGraphNodeIDmap != null && mergedNodesInResultGraphNodeIDmap.size() > 0) {
													for(Entry<Integer, String> mergedNodeElem : mergedNodesInResultGraphNodeIDmap.entrySet()) {
														if(mergedNodeElem != null && mergedNodeElem.getKey() != null && mergedNodeElem.getKey().equals(effectID)) {
															realEffectNodeID = resultGraphNodeID;
															break;
														}
													}
												}
											}

											if(realEffectNodeID != null) {
												break;
											}
										}

									}

									if(realEffectNodeID == null) {
										logger.debug("Effect node from another sentence.");
										effectNodeFromAnotherSentence = true;
										realEffectNodeID = effectID;
									}

									// Here found the real dependency graph ID of the cause and effect node if they are in the same sentence

									// If the cause and effect nodes are in the same sentence, simply add a "CAUSE" arc from the cause node to the effect node
									if(realCauseNodeID != null && result.getNodeName(realCauseNodeID) != null && realEffectNodeID != null && result.getNodeName(realEffectNodeID) != null) {
										// Adding arc
										result.addEdge("CAUSE", realCauseNodeID, realEffectNodeID, false);
									}
									else { // The cause or effect node is in another sentence --> for the nodes that are in the sentence
										// use the causal node attributes to store this information about cross-sentence causal relation

										if(!causeNodeFromAnotherSentence) { // If the cause node is from the sentence of the dependency graph that is being built
											result.setNodeInCrossSentCausalRel(realCauseNodeID, causalityGroupID, "CAUSE", realCauseNodeID, realEffectNodeID);
										}
										if(!effectNodeFromAnotherSentence) { // If the effect node is from the sentence of the dependency graph that is being built
											result.setNodeInCrossSentCausalRel(realCauseNodeID, causalityGroupID, "EFFECT", realCauseNodeID, realEffectNodeID);
										}
									}


									// Only logging
									if(realCauseNodeID != null && realEffectNodeID != null) {
										String relationType = "in-sentence";
										if(causeNodeFromAnotherSentence || effectNodeFromAnotherSentence) {
											relationType = "cross-sentence";
										}

										if(relationType.equals("in-sentence")) {
											logger.info("Added " + relationType + " causal relation:\n" + 
													"Sentence: '" + GateUtil.getAnnotationText(cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(annotationId), cacheManager.getGateDoc()).orElse("NOT_RETRIEVED")  + "'\n" + 
													" CAUSE NODE: " + ((!causeNodeFromAnotherSentence && result.getNodeName(realCauseNodeID) != null) ? result.getNodeName(realCauseNodeID) : "NULL - FROM ANOTHER SENTENCE" ) + " ID: " + realCauseNodeID + "\n" +
													" EFFECT NODE: " + ((!effectNodeFromAnotherSentence && result.getNodeName(realEffectNodeID) != null) ? result.getNodeName(realEffectNodeID) : "NULL - FROM ANOTHER SENTENCE" ) + " ID: " + realEffectNodeID	);
										}
										else {
											logger.info("Deferred cross-sentence causal relation.");
										}


									}
								}
							}
						}
					}

				}
				// Add causality relations - END

			}
		}
		catch (Exception e) {
			Util.notifyException("Creating sentence graph element", e, logger);
		}

		return result;
	}

	/**
	 * Get a map with all the set of node IDs that are equivalent (i.e. that belong to the same co-reference chain).	 * 
	 * KEY: identifier of coref chain (integer)
	 * VALUE: List of elements of the coreference chain, each element is a Map with:
	 *      - KEY: unambiguous id of the coreference chain element
	 *      - VALUE: set of token IDs (integer) that the coreference chain element includes.
	 * 
	 * @param cacheManager
	 * @return
	 */
	public static Map<Integer, List<Map<Integer, Set<Integer>>>> getCorefTokenIDsets(DocCacheManager cacheManager) {
		Map<Integer, List<Map<Integer, Set<Integer>>>> returnMap = new HashMap<Integer, List<Map<Integer, Set<Integer>>>>();

		Set<String> corefChainAnnTypes = cacheManager.getGateDoc().getAnnotations(ImporterBase.coref_ChainAnnSet).getAllTypes();
		if(corefChainAnnTypes != null && corefChainAnnTypes.size() > 0) {
			Set<String> corefChainAnnTypesOrdered = new TreeSet<String>();
			corefChainAnnTypesOrdered.addAll(corefChainAnnTypes);
			Integer corefId = -1;
			for(String corefChainAnnType : corefChainAnnTypesOrdered) {
				List<Map<Integer, Set<Integer>>> listOfTokensSetOfCoref = new ArrayList<Map<Integer, Set<Integer>>>();

				List<Annotation> corefChainAnnotationList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.coref_ChainAnnSet, corefChainAnnType);
				for(Annotation corefChainAnnotation : corefChainAnnotationList) {
					if(corefId == -1) {
						corefId = corefChainAnnotation.getId();
					}

					Integer headTokenID = GateUtil.getIntegerFeature(corefChainAnnotation, "headID").orElse(null);
					if(headTokenID != null) {
						Map<Integer, Set<Integer>> corefElemIdTokenIdSetMap = new HashMap<Integer, Set<Integer>>();

						// Gather info concerning the coreference chain annotation under analysis
						Annotation headAnn = cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(headTokenID);
						String headString = GateUtil.getAnnotationText(headAnn, cacheManager.getGateDoc()).orElse(null);
						String corefMentionType = GateUtil.getStringFeature(corefChainAnnotation, "type").orElse(null);
						boolean isPronoun = (corefMentionType != null && corefMentionType.equals("PRONOMINAL")) ? true : false;

						// Gather all the tokens of the coreference chain annotation under analysis
						List<Annotation> tokenOfCorefChainAnnotation = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(cacheManager.getGateDoc(),
																																ImporterBase.driAnnSet, ImporterBase.tokenAnnType, corefChainAnnotation);

						// Filter out co-reference mentions that are not correct:
						// - empty string
						// - not pronominal and with length 1
						if(tokenOfCorefChainAnnotation != null && tokenOfCorefChainAnnotation.size() > 0 &&
								headString != null && headString.length() > 0 && 
								( isPronoun || (!isPronoun && headString.length() > 1) )) {
							Set<Integer> tokensSetOfCorefMention = new HashSet<Integer>();

							for(Annotation tokenOfCorefMention : tokenOfCorefChainAnnotation) {
								if(tokenOfCorefMention != null) {
									tokensSetOfCorefMention.add(tokenOfCorefMention.getId());
								}
							}

							if(tokensSetOfCorefMention.size() > 0) {
								corefElemIdTokenIdSetMap.put(corefChainAnnotation.getId(), tokensSetOfCorefMention);
								listOfTokensSetOfCoref.add(corefElemIdTokenIdSetMap);
							}
						}
					}	
				}

				if(listOfTokensSetOfCoref.size() > 1) {
					returnMap.put(corefId.intValue(), listOfTokensSetOfCoref);
				}

				corefId = -1;
			}
		}

		return returnMap;
	}


	/**
	 * Header of document Generator method
	 * 
	 * @param annotationId
	 * @param cacheManager
	 * @return
	 */
	public static Header getHeaderFromDocument(Document gateDoc, DocCacheManager cacheManager) {
		if(gateDoc == null || cacheManager == null) {
			return null;
		}

		// Retrieve title annotation to add as features all the metadata extracted from the header
		List<Annotation> titlesAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.titleAnnType);

		if(titlesAnnList == null || titlesAnnList.size() == 0) {
			Util.notifyException("Impossible to parse header, no document title identified", new Exception("No document title identified"), logger);
			return null;
		}
		else {
			HeaderImpl resultImpl = new HeaderImpl(cacheManager);
			
			try {
				for(Annotation titleAnn : titlesAnnList) {
					if(titleAnn != null) {
						titleAnn.setFeatures((titleAnn.getFeatures() != null) ? titleAnn.getFeatures() : Factory.newFeatureMap());
					}
				}
				
				
				// *****
				// Title
				for(Annotation titleAnn : titlesAnnList) {
					if(titleAnn != null) {
						String titleText = GateUtil.getAnnotationText(titleAnn, cacheManager.getGateDoc()).orElse(null);
						String titleLang = GateUtil.getStringFeature(titleAnn, ImporterBase.langAnnFeat).orElse(null);
						
						if(titleText == null || titleText.trim().equals("")) {
							continue;
						}
						
						if(titleLang == null) {
							resultImpl.addTitle(LangENUM.UNSPECIFIED, titleText);
						}
						else if(titleLang.trim().toLowerCase().equals("es")) {
							resultImpl.addTitle(LangENUM.ES, titleText);
						}
						else {
							resultImpl.addTitle(LangENUM.EN, titleText);
						}
					}
				}
				

				// *****************
				// Header plain text
				Optional<String> headerGATEdocStr = GateUtil.getStringFeature(cacheManager.getGateDoc(), ImporterBase.headerDOC_OrigDocFeat);
				if(headerGATEdocStr.isPresent()) {
					String headerStr = headerGATEdocStr.get();
					resultImpl.setPlainText(headerStr.replace("<NL>", "\n"));
				}
				
				// ******************************************************
				// List of authors with name, surname, email, affiliation
				String sourceDocFeature = GateUtil.getStringFeature(cacheManager.getGateDoc(), "source").orElse(null);
				try {
					SourceENUM sourceType = SourceENUM.valueOf(sourceDocFeature);
					Extractor authorExtractor = ExtractorFactory.getExtractor(sourceType);
					List<Author> authorList = authorExtractor.extract(cacheManager);
					resultImpl.addAuthors(authorList);
				}
				catch (Exception e) {
					System.out.println("Could not use an Extractor for the authors");
				}


				Annotation titleAnn = GateUtil.getFirstAnnotationInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.titleAnnType).orElse(null);
				
				if(resultImpl != null && (resultImpl.getAuthorList() == null || resultImpl.getAuthorList().size() == 0)) {
					// Retrieve author information from Bibsonomy, CrossRef, FreeCite and GoogleScholar
					int authorCount = 1;
					String authroName_googleScholar = GateUtil.getStringFeature(titleAnn, "goos_authorName_" + authorCount).orElse(null);
					String authroName_bib = GateUtil.getStringFeature(titleAnn, "b_authorName_" + authorCount).orElse(null);
					String authorFullName_ruleExtract = GateUtil.getStringFeature(titleAnn, "R_author_" + authorCount).orElse(null);

					if(StringUtils.isNotEmpty(authroName_googleScholar)) {
						// Authors from Google Scholar
						boolean foundAuthor = true;
						while(foundAuthor) {
							authroName_googleScholar = GateUtil.getStringFeature(titleAnn, "goos_authorName_" + authorCount).orElse(null);
							if(authroName_googleScholar != null) {
								String personalPageURL = GateUtil.getStringFeature(titleAnn, "goos_authorURL_" + authorCount).orElse(null);
								AuthorImpl newAuthor = new AuthorImpl(cacheManager, authroName_googleScholar, null, null);
								newAuthor.setPersonalPageURL(personalPageURL);
								resultImpl.addAuthor(newAuthor);
							}
							else {
								foundAuthor = false;
							}
							authorCount++;
						}
						
					}
					else if (StringUtils.isNotEmpty(authroName_bib)) {
						// Authors from bibsonomy
						boolean foundAuthor = true;
						while(foundAuthor) {
							authroName_bib = GateUtil.getStringFeature(titleAnn, "b_authorName_" + authorCount).orElse(null);
							if(authroName_bib != null) {
								String firstName = GateUtil.getStringFeature(titleAnn, "b_authorFirstName_" + authorCount).orElse(null);
								String surname = GateUtil.getStringFeature(titleAnn, "b_authorLastName_" + authorCount).orElse(null);

								AuthorImpl newAuthor = new AuthorImpl(cacheManager, authroName_bib, firstName, surname);
								resultImpl.addAuthor(newAuthor);
							}
							else {
								foundAuthor = false;
							}
							authorCount++;
						}
						
					}
					else if (StringUtils.isNotEmpty(authorFullName_ruleExtract)) {
						boolean foundAuthor = true;
						while(foundAuthor) {
							authorFullName_ruleExtract = GateUtil.getStringFeature(titleAnn, "R_author_" + authorCount).orElse(null);

							if(authorFullName_ruleExtract != null) {
								AuthorImpl newAuthor = new AuthorImpl(cacheManager);
								newAuthor.setFullName(authorFullName_ruleExtract);
								resultImpl.addAuthor(newAuthor);

								int authorAffCount = 1;
								boolean foundAuthorAff = true;
								while(foundAuthorAff) {
									String authorAffiliationName = GateUtil.getStringFeature(titleAnn, "R_author_" + authorCount + "_aff_" + authorAffCount).orElse(null);
									if(authorAffiliationName != null) {
										if(!CollectionUtils.isEmpty(resultImpl.getInstitutions())) {
											for(Institution inst : resultImpl.getInstitutions()) {
												if(inst != null && Util.strCompareCI(inst.getName(), authorAffiliationName)) {
													newAuthor.addAffiliation(inst);
												}
											}
										}
										else {
											// Create new affiliation
											InstitutionImpl newAffil = new InstitutionImpl(cacheManager);
											newAffil.setName(authorAffiliationName);
											resultImpl.addInstitution(newAffil);
											newAuthor.addAffiliation(newAffil);
										}
										authorAffCount++;
									}
									else {
										foundAuthorAff = false;
									}
								}
								authorCount++;
							}
							else {
								foundAuthor = false;
							}
						}
					}
					
				}
				
				
				// ****************************************************************************
				// Info other than authors from Bibsnomy, CrossRef, FreeCite and Google Scholar
				int authorCount = 1;
				String authroName_googleScholar = GateUtil.getStringFeature(titleAnn, "goos_authorName_" + authorCount).orElse(null);
				String authroName_bib = GateUtil.getStringFeature(titleAnn, "b_authorName_" + authorCount).orElse(null);
				
				/* IMPORTANT: parse year, journal, etc. info from JATS XML
				if(sourceDocFeature != null && sourceDocFeature.equals(SourceENUM.JATS.toString())) {
					
				}
				else */
				if(StringUtils.isNotEmpty(authroName_googleScholar)) {
					// Year from Google Scholar
					String year = GateUtil.getStringFeature(titleAnn, "goos_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// URL from Google Scholar
					String paparURL = GateUtil.getStringFeature(titleAnn, "goos_link").orElse(null);
					if(paparURL != null) resultImpl.setOpenURL(paparURL);

					// Journal from Google Scholar
					String journalName = "";
					int journalCount = 1;
					boolean foundJournal = true;
					while(foundJournal) {
						String journalNameElem = GateUtil.getStringFeature(titleAnn, "goos_journalName_" + journalCount).orElse(null);
						if(journalNameElem != null) {
							journalName += ((journalName.length() > 0) ? " " : "") + journalNameElem;
						}
						else {
							foundJournal = false;
						}
						journalCount++;
					}
					resultImpl.setJournal(journalName);
				}
				else if (StringUtils.isNotEmpty(authroName_bib)) {
					// Year from Bibsonomy
					String year = GateUtil.getStringFeature(titleAnn, "b_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// Pages from Bibsonomy
					String pages = GateUtil.getStringFeature(titleAnn, "b_pages").orElse(null);
					if(pages != null) resultImpl.setPages(pages);

					// OpenURL from Bibsonomy
					String openURL = GateUtil.getStringFeature(titleAnn, "b_openURL").orElse(null);
					if(openURL != null) resultImpl.setOpenURL(openURL);

					// URL from Bibsonomy
					String bibURL = GateUtil.getStringFeature(titleAnn, "b_url").orElse(null);
					if(bibURL != null) resultImpl.setBibsonomyURL(bibURL);

					// Chapter from Bibsonomy
					String chapter = GateUtil.getStringFeature(titleAnn, "b_chapter").orElse(null);
					if(chapter != null) resultImpl.setChapter(chapter);

					// Volume from Bibsonomy
					String volume = GateUtil.getStringFeature(titleAnn, "b_volume").orElse(null);
					if(volume != null) resultImpl.setVolume(volume);

					// Series from Bibsonomy
					String series = GateUtil.getStringFeature(titleAnn, "b_series").orElse(null);
					if(series != null) resultImpl.setSeries(series);

					// Publisher from Bibsonomy
					String publisher = GateUtil.getStringFeature(titleAnn, "b_publisher").orElse(null);
					if(publisher != null) resultImpl.setPages(publisher);

					// Edition from Bibsonomy
					String edition = GateUtil.getStringFeature(titleAnn, "b_edition").orElse(null);
					if(edition != null) resultImpl.setEdition(edition);

					// Journal from Bibsonomy
					String journal = GateUtil.getStringFeature(titleAnn, "b_journal").orElse(null);
					if(journal != null) resultImpl.setJournal(journal);

					// Institution from Bibsonomy -  custom institution parsing have been implemented
					// String instituion = GateUtil.getStringFeature(titleAnn, "b_institution").orElse(null);
					// if(instituion != null) resultImpl.setInstitution(instituion);

				}
				
				// Set keyword - taken from original markups
				List<Annotation> keywordAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet, ImporterPDFEXT.PDFEXTkeywordsText);
				if(keywordAnnList != null && keywordAnnList.size() > 0) {
					for(Annotation keywordAnn : keywordAnnList) {
						if(keywordAnn != null) {
							String keywordString = GateUtil.getAnnotationText(keywordAnn, cacheManager.getGateDoc()).orElse(null);
							String keywordLang = GateUtil.getStringFeature(keywordAnn, "lang").orElse(null);
							
							if(keywordString != null && !keywordString.equals("")) {
								keywordString = keywordString.toLowerCase();
								if(keywordString.contains("palabras clave")) {
									keywordLang = "es";
								}
								else if(keywordString.contains("keywords")) {
									keywordLang = "en";
								}
								else if(keywordLang.toLowerCase().contains("es") && !keywordLang.toLowerCase().contains("en")) {
									keywordLang = "es";
								}
								else if(!keywordLang.toLowerCase().contains("es")) {
									keywordLang = "en";
								}
								
								keywordString = keywordString.replace("palabras clave:", "").replace("keywords:", "").replace("palabras clave :", "").replace("keywords :", "");
								String[] keywordsCommaSep = keywordString.split(",");
								String[] keywordsSemicolonSep = keywordString.split(";");
								
								String[] keywordsList = null;
								if(keywordsCommaSep.length > keywordsSemicolonSep.length) {
									keywordsList = keywordsCommaSep;
								}
								else {
									keywordsList = keywordsSemicolonSep;
								}
								
								List<String> keywordList = Arrays.asList(keywordsList);
								
								if(keywordLang == null || keywordLang.equals("")) {
									resultImpl.setKeyword(LangENUM.UNSPECIFIED, keywordList);
								}
								else if(keywordLang.toLowerCase().trim().equals("es")) {
									resultImpl.setKeyword(LangENUM.ES, keywordList);
								}
								else {
									resultImpl.setKeyword(LangENUM.EN, keywordList);
								}
							}
						}
					}
				}
				
				
			}
			catch (Exception e) {
				Util.notifyException("Creating header element", e, logger);
			}
			
			return resultImpl;
		}
		
	}


	/**
	 * Citations of document Generator method
	 * 
	 * @param annotationId
	 * @param cacheManager
	 * @return
	 */
	public static Citation getCitationFromBibEntry(Annotation bibEntryAnn, DocCacheManager cacheManager) {
		if(bibEntryAnn == null || cacheManager == null) {
			return null;
		}

		Citation result = null;

		Citation cachedCitation = cacheManager.getCachedCitation(bibEntryAnn.getId());
		if(cachedCitation != null) {
			return cachedCitation;
		}

		try {
			CitationImpl resultImpl = new CitationImpl(cacheManager);

			String bibEntryText = GateUtil.getAnnotationText(bibEntryAnn, cacheManager.getGateDoc()).orElse(null);

			if(bibEntryText != null) {

				resultImpl.setId(bibEntryAnn.getId());
				resultImpl.setText(bibEntryText);

				// Title
				String JATStitle = GateUtil.getStringFeature(bibEntryAnn, "jats_title").orElse(null);
				String JATSsource = GateUtil.getStringFeature(bibEntryAnn, "jats_source").orElse(null);
				String bibsonomyTitle = GateUtil.getStringFeature(bibEntryAnn, "b_title").orElse(null);
				String googleScholarTitle = GateUtil.getStringFeature(bibEntryAnn, "goos_title").orElse(null);
				String crossRefTitle = GateUtil.getStringFeature(bibEntryAnn, "x_title").orElse(null);
				String freeCiteTitle = GateUtil.getStringFeature(bibEntryAnn, "f_title").orElse(null);

				if(JATStitle != null || JATSsource != null) {
					resultImpl.addSource(CitationSourceENUM.JATS);

					resultImpl.setTitle((JATStitle != null && JATStitle.trim().length() > 0) ? JATStitle : JATSsource);

					// JATS authors
					Integer authCount = 0;
					while(GateUtil.getStringFeature(bibEntryAnn, "jats_author_" + (authCount + 1)).orElse(null) != null && 
							!GateUtil.getStringFeature(bibEntryAnn, "jats_author_" + (authCount + 1)).orElse(null).equals("")) {
						authCount++;

						if(authCount > 50) {
							break;
						}

						AuthorImpl newAuthor = new AuthorImpl(cacheManager, 
								GateUtil.getStringFeature(bibEntryAnn, "jats_author_" + authCount).orElse(null), 
								GateUtil.getStringFeature(bibEntryAnn, "jats_author_" + authCount + "_givenName").orElse(null), 
								GateUtil.getStringFeature(bibEntryAnn, "jats_author_" + authCount + "_surname").orElse(null));

						resultImpl.setAuthor(newAuthor);
					}

					resultImpl.setOpenURL(GateUtil.getStringFeature(bibEntryAnn, "jats_link").orElse(null));

					// JATS pub ID
					Integer pubIDcount = 0;
					while(GateUtil.getStringFeature(bibEntryAnn, "jats_pubID_" + (pubIDcount + 1)).orElse(null) != null && 
							!GateUtil.getStringFeature(bibEntryAnn, "jats_pubID_" + (pubIDcount + 1)).orElse(null).equals("")) {
						pubIDcount++;

						if(pubIDcount > 50) {
							break;
						}

						String pubIDvalue = GateUtil.getStringFeature(bibEntryAnn, "jats_pubID_" + (pubIDcount + 1)).orElse(null);
						String pubIDtype = GateUtil.getStringFeature(bibEntryAnn, "jats_pubID_" + (pubIDcount + 1) + "_type").orElse(null);

						if(pubIDtype != null && pubIDtype.trim().equals("doi")) {
							resultImpl.setPubID(PubIdENUM.DOI, pubIDvalue);
						}
						else if(pubIDtype != null && pubIDtype.trim().equals("pmid")) {
							resultImpl.setPubID(PubIdENUM.PMID, pubIDvalue);
						}
					}

					resultImpl.setYear(GateUtil.getStringFeature(bibEntryAnn, "jats_year").orElse(null));

					resultImpl.setVolume(GateUtil.getStringFeature(bibEntryAnn, "jats_volume").orElse(null));

					resultImpl.setIssue(GateUtil.getStringFeature(bibEntryAnn, "jats_issue").orElse(null));

					resultImpl.setPublisher(GateUtil.getStringFeature(bibEntryAnn, "jats_publisherName").orElse(null));

					resultImpl.setPublisherLoc(GateUtil.getStringFeature(bibEntryAnn, "jats_publisherLoc").orElse(null));

					resultImpl.setFirstPage(GateUtil.getStringFeature(bibEntryAnn, "jats_firstPage").orElse(null));

					resultImpl.setLastPage(GateUtil.getStringFeature(bibEntryAnn, "jats_lastPage").orElse(null));

					if(GateUtil.getStringFeature(bibEntryAnn, "jats_firstPage").orElse(null) != null && GateUtil.getStringFeature(bibEntryAnn, "jats_lastPage").orElse(null) != null) {
						resultImpl.setPages(GateUtil.getStringFeature(bibEntryAnn, "jats_firstPage").orElse(null) + " - " + GateUtil.getStringFeature(bibEntryAnn, "jats_lastPage").orElse(null));
					}

				}
				else if(bibsonomyTitle != null) {
					resultImpl.addSource(CitationSourceENUM.Bibsonomy);

					resultImpl.setTitle(bibsonomyTitle);
					
					// Add DOI from CrossRef if any
					if(crossRefTitle != null) {
						Integer distance = Util.computeLevenshteinDistance(bibsonomyTitle.trim().toLowerCase(), crossRefTitle.trim().toLowerCase());
						if(distance != null && distance < 15) {
							String DOI = GateUtil.getStringFeature(bibEntryAnn, "x_doi").orElse(null);
							if(DOI != null) resultImpl.setPubID(PubIdENUM.DOI, DOI);
						}
					}
					
					// Authors from bibsonomy
					int authorCount = 1;
					boolean foundAuthor = true;
					while(foundAuthor) {
						String authroName = GateUtil.getStringFeature(bibEntryAnn, "b_authorName_" + authorCount).orElse(null);
						if(authroName != null) {
							String firstName = GateUtil.getStringFeature(bibEntryAnn, "b_authorFirstName_" + authorCount).orElse(null);
							String surname = GateUtil.getStringFeature(bibEntryAnn, "b_authorLastName_" + authorCount).orElse(null);

							AuthorImpl newAuthor = new AuthorImpl(cacheManager, authroName, firstName, surname);
							resultImpl.setAuthor(newAuthor);
						}
						else {
							foundAuthor = false;
						}
						authorCount++;
					}

					// Editor from bibsonomy
					int editorCount = 1;
					boolean foundEditor = true;
					while(foundEditor) {
						String authroName = GateUtil.getStringFeature(bibEntryAnn, "b_editorName_" + editorCount).orElse(null);
						if(authroName != null) {
							String firstName = GateUtil.getStringFeature(bibEntryAnn, "b_editorFirstName_" + editorCount).orElse(null);
							String surname = GateUtil.getStringFeature(bibEntryAnn, "b_editorLastName_" + editorCount).orElse(null);

							AuthorImpl newEditor = new AuthorImpl(cacheManager, authroName, firstName, surname);
							resultImpl.setEditor(newEditor);
						}
						else {
							foundEditor = false;
						}
						editorCount++;
					}

					// Year from Bibsonomy
					String year = GateUtil.getStringFeature(bibEntryAnn, "b_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// Pages from Bibsonomy
					String pages = GateUtil.getStringFeature(bibEntryAnn, "b_pages").orElse(null);
					if(pages != null) resultImpl.setPages(pages);

					// OpenURL from Bibsonomy
					String openURL = GateUtil.getStringFeature(bibEntryAnn, "b_openURL").orElse(null);
					if(openURL != null) resultImpl.setOpenURL(openURL);

					// URL from Bibsonomy
					String bibURL = GateUtil.getStringFeature(bibEntryAnn, "b_url").orElse(null);
					if(bibURL != null) resultImpl.setBibsonomyURL(bibURL);

					// Chapter from Bibsonomy
					String chapter = GateUtil.getStringFeature(bibEntryAnn, "b_chapter").orElse(null);
					if(chapter != null) resultImpl.setChapter(chapter);

					// Volume from Bibsonomy
					String volume = GateUtil.getStringFeature(bibEntryAnn, "b_volume").orElse(null);
					if(volume != null) resultImpl.setVolume(volume);

					// Series from Bibsonomy
					String series = GateUtil.getStringFeature(bibEntryAnn, "b_series").orElse(null);
					if(series != null) resultImpl.setSeries(series);

					// Publisher from Bibsonomy
					String publisher = GateUtil.getStringFeature(bibEntryAnn, "b_publisher").orElse(null);
					if(publisher != null) resultImpl.setPages(publisher);

					// Edition from Bibsonomy
					String edition = GateUtil.getStringFeature(bibEntryAnn, "b_edition").orElse(null);
					if(edition != null) resultImpl.setEdition(edition);

					// Journal from Bibsonomy
					String journal = GateUtil.getStringFeature(bibEntryAnn, "b_journal").orElse(null);
					if(journal != null) resultImpl.setJournal(journal);

					// Institution from Bibsonomy
					String instituion = GateUtil.getStringFeature(bibEntryAnn, "b_institution").orElse(null);
					if(instituion != null) resultImpl.setInstitution(instituion);


				}
				else if(googleScholarTitle != null) {
					resultImpl.addSource(CitationSourceENUM.GoogleScholar);

					resultImpl.setTitle(googleScholarTitle);
					
					// Add DOI from CrossRef if any
					if(crossRefTitle != null) {
						Integer distance = Util.computeLevenshteinDistance(googleScholarTitle.trim().toLowerCase(), crossRefTitle.trim().toLowerCase());
						if(distance != null && distance < 15) {
							String DOI = GateUtil.getStringFeature(bibEntryAnn, "x_doi").orElse(null);
							if(DOI != null) resultImpl.setPubID(PubIdENUM.DOI, DOI);
						}
					}

					// Authors from Google Scholar
					int authorCount = 1;
					boolean foundAuthor = true;
					while(foundAuthor) {
						String authroName = GateUtil.getStringFeature(bibEntryAnn, "goos_authorName_" + authorCount).orElse(null);
						if(authroName != null) {
							String personalPageURL = GateUtil.getStringFeature(bibEntryAnn, "goos_authorURL_" + authorCount).orElse(null);
							AuthorImpl newAuthor = new AuthorImpl(cacheManager, authroName, null, null);
							newAuthor.setPersonalPageURL(personalPageURL);
							resultImpl.setAuthor(newAuthor);
						}
						else {
							foundAuthor = false;
						}
						authorCount++;
					}

					// Year from Google Scholar
					String year = GateUtil.getStringFeature(bibEntryAnn, "goos_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// URL from Google Scholar
					String paparURL = GateUtil.getStringFeature(bibEntryAnn, "goos_link").orElse(null);
					if(paparURL != null) resultImpl.setOpenURL(paparURL);

					// Journal from Google Scholar
					String journalName = "";
					int journalCount = 1;
					boolean foundJournal = true;
					while(foundJournal) {
						String journalNameElem = GateUtil.getStringFeature(bibEntryAnn, "goos_journalName_" + journalCount).orElse(null);
						if(journalNameElem != null) {
							journalName += ((journalName.length() > 0) ? " " : "") + journalNameElem;
						}
						else {
							foundJournal = false;
						}
						journalCount++;
					}
					resultImpl.setJournal(journalName);

					// Citation style contents
					int citStyleCount = 1;
					boolean foundCitStyle = true;
					while(foundCitStyle) {
						String citStyleID = GateUtil.getStringFeature(bibEntryAnn, "goos_citStyle_" + citStyleCount).orElse(null);
						if(citStyleID != null) {
							String citStyleContent = GateUtil.getStringFeature(bibEntryAnn, "goos_citContentsr_" + citStyleCount).orElse(null);
							resultImpl.setCitationStringEntry(citStyleID, citStyleContent);
						}
						else {
							foundCitStyle = false;
						}
						citStyleCount++;
					}

				}
				else if(crossRefTitle != null) {
					resultImpl.addSource(CitationSourceENUM.CrossRef);

					resultImpl.setTitle(crossRefTitle);

					// Year from CrossRef
					String year = GateUtil.getStringFeature(bibEntryAnn, "x_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// DOI from CrossRef
					String DOI = GateUtil.getStringFeature(bibEntryAnn, "x_doi").orElse(null);
					if(DOI != null) resultImpl.setPubID(PubIdENUM.DOI, DOI);

				}
				else if(freeCiteTitle != null) {
					resultImpl.addSource(CitationSourceENUM.FreeCite);

					resultImpl.setTitle(freeCiteTitle);

					// Authors from FreeCite
					int authorCount = 1;
					boolean foundAuthor = true;
					while(foundAuthor) {
						String authroName = GateUtil.getStringFeature(bibEntryAnn, "f_authorName_" + authorCount).orElse(null);
						if(authroName != null) {
							AuthorImpl newAuthor = new AuthorImpl(cacheManager, authroName, null, null);
							resultImpl.setAuthor(newAuthor);
						}
						else {
							foundAuthor = false;
						}
						authorCount++;
					}

					// Year from FreeCite
					String year = GateUtil.getStringFeature(bibEntryAnn, "f_year").orElse(null);
					if(year != null) resultImpl.setYear(year);

					// Pages from FreeCite
					String pages = GateUtil.getStringFeature(bibEntryAnn, "f_pages").orElse(null);
					if(pages != null) resultImpl.setPages(pages);

					// Journal from FreeCite
					String journal = GateUtil.getStringFeature(bibEntryAnn, "f_journal").orElse(null);
					if(journal != null) resultImpl.setJournal(journal);


				}

				// Add citation markers
				String bibEntry_rid = GateUtil.getStringFeature(bibEntryAnn, ImporterBase.bibEntry_IdAnnFeat).orElse("");
				List<Annotation> citMarkersAnnList = GateUtil.getAnnInDocOrder(cacheManager.getGateDoc(), ImporterBase.driAnnSet, ImporterBase.inlineCitationMarkerAnnType);
				for(Annotation citMarkerAnn : citMarkersAnnList) {
					String rid = GateUtil.getStringFeature(citMarkerAnn, ImporterBase.bibEntry_IdAnnFeat).orElse(null);
					String text = GateUtil.getAnnotationText(citMarkerAnn, cacheManager.getGateDoc()).orElse(null);
					if(rid != null && text != null && rid.equals(bibEntry_rid)) {
						CitationMarkerImpl newCitMarker = new CitationMarkerImpl(cacheManager);
						newCitMarker.setId(citMarkerAnn.getId());
						newCitMarker.setCitationId(bibEntryAnn.getId());
						newCitMarker.setReferenceText(text);

						List<Annotation> getOverlappingSentences = GateUtil.getAnnInDocOrderIntersectAnn(cacheManager.getGateDoc(), 
								ImporterBase.driAnnSet, ImporterBase.sentenceAnnType, citMarkerAnn);
						if(getOverlappingSentences.size() == 1) {
							newCitMarker.setSentenceId(getOverlappingSentences.get(0).getId());
						}

						resultImpl.setNewCitationMarker(newCitMarker);
					}
				}


				// Add to cache
				cacheManager.cacheCitation(resultImpl);
				result = resultImpl;
			}

		}
		catch (Exception e) {
			Util.notifyException("Creating citation element", e, logger);
		}

		return result;
	}
	
	
	public static String normalizeText(String inputText) {
		if(inputText != null) {
			inputText = inputText.replaceAll("\t", " ");
			inputText = inputText.replaceAll("\\s+", " ");
			inputText = inputText.trim();
		}
		return inputText;
	}

}

