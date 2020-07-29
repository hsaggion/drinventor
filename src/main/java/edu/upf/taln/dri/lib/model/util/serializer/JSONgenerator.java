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
package edu.upf.taln.dri.lib.model.util.serializer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.bytecode.opencsv.CSVReader;
import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.BabelSynsetOcc;
import edu.upf.taln.dri.lib.model.ext.Header;
import edu.upf.taln.dri.lib.model.ext.MetaEntityTypeENUM;
import edu.upf.taln.dri.lib.model.ext.RhetoricalClassENUM;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.ext.SummaryTypeENUM;
import edu.upf.taln.dri.lib.model.util.DocParse;
import edu.upf.taln.dri.lib.model.util.ObjectGenerator;
import edu.upf.taln.dri.lib.model.util.serializer.model.DocumentJSON;
import edu.upf.taln.dri.lib.model.util.serializer.model.SentenceJSON;
import edu.upf.taln.dri.lib.model.util.serializer.model.TripleJSON;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.pdf.ImporterPDFEXT;
import gate.Annotation;

/**
 * This class is useful to generate JSON representations / serializations of the core
 * data stuctures that characterize a scientific document.
 * 
 *
 */
public class JSONgenerator {

	private static Logger logger = Logger.getLogger(JSONgenerator	.class);

	private static ObjectMapper mapper = new ObjectMapper();


	/**
	 * Return the JSON serialization of a set of sentences of the document.<br/>
	 * A list of sentence JSON object is returned; the order of the elements of this list reflects 
	 * the order of occurrence of each sentence in the document.<br/>
	 * Each sentence JSON object is compliant with the class the structure modeled 
	 * by the {@link edu.upf.taln.dri.lib.model.util.serializer.model.SentenceJSON SentenceJSON} class.
	 * 
	 * @param doc
	 * @param sentenceSel
	 * @param includeTokens true to include the list of tokens of each sentence, each one with lemma and POS
	 * @param includeBabelnetSynsets true to include the list of Babelnet sysets spotted inside each sentence
	 * @return
	 */
	public static String getSentencesJSON(Document doc, SentenceSelectorENUM sentenceSel, 
			boolean includeTokens, boolean includeBabelnetSynsets) {

		List<SentenceJSON> sentenceListJSON = getSentenceJSONlist(doc, sentenceSel, includeTokens, includeBabelnetSynsets);

		String retJson = "";
		try {
			retJson = mapper.writeValueAsString(sentenceListJSON);
		} catch (JsonProcessingException e) {
			Util.notifyException("Error generating sentence JSON", e, logger);
		}

		return retJson;
	}


	/**
	 * Return the JSON serialization of a sentence of the document (JSON object), identified by its id.<br/>
	 * The sentence JSON object is compliant to the structure modeled 
	 * by the {@link edu.upf.taln.dri.lib.model.util.serializer.model.SentenceJSON SentenceJSON} class.
	 * 
	 * @param doc
	 * @param sentenceId
	 * @param includeTokens true to include the list of tokens of each sentence, each one with lemma and POS
	 * @param includeBabelnetSynsets true to include the list of Babelnet sysets spotted inside each sentence
	 * @return
	 */
	public static String getSentenceJSON(Document doc, Integer sentenceId, 
			boolean includeTokens, boolean includeBabelnetSynsets) {
		if(doc == null || sentenceId == null) {
			return null;
		}

		List<SentenceJSON> sentenceListJSON = getSentenceJSONlist(doc, SentenceSelectorENUM.ALL, includeTokens, includeBabelnetSynsets);

		SentenceJSON selectedSentence = null;
		for(SentenceJSON sent : sentenceListJSON) {
			if(sent != null && sent.getId().equals(sentenceId)) {
				selectedSentence = sent;
			}
		}

		if(selectedSentence != null) {
			String retJson = "";
			try {
				retJson = mapper.writeValueAsString(selectedSentence);
			} catch (JsonProcessingException e) {
				Util.notifyException("Error generating sentence JSON", e, logger);
			}

			return retJson;
		}
		else {
			return "";
		}
	}


	/**
	 * Get the JSON serialization of the information contained in the document
	 * 
	 * @param doc
	 * @param includeTokens true to include the list of tokens of each sentence, each one with lemma and POS
	 * @param includeBabelnetSynsets true to include the list of Babelnet sysets spotted inside each sentence
	 * @return
	 */
	public static String getDocumentJSON(Document doc, boolean includeTokens, boolean includeBabelnetSynsets) {
		if(doc == null) {
			return null;
		}

		DocumentJSON documentJSON = new DocumentJSON();

		try {
			Header docHeader = doc.extractHeader();
			if(docHeader != null) {
				documentJSON.setHeader(docHeader);
				documentJSON.setAbstractSentences(getSentenceJSONlist(doc, SentenceSelectorENUM.ONLY_ABSTRACT, includeTokens, includeBabelnetSynsets));
				documentJSON.setBodySentences(getSentenceJSONlist(doc, SentenceSelectorENUM.ALL_EXCEPT_ABSTRACT, includeTokens, includeBabelnetSynsets));
				documentJSON.setCitations(doc.extractCitations());

				// Experimental (SEPLN): summary rank
				try {
					Double documentSentences = (double) documentJSON.getBodySentences().size();

					// 10% - TITILE_SIM
					Double tenPercSummary = documentSentences * 0.10d;
					Integer tenPercSummaryInt = tenPercSummary.intValue();

					List<Sentence> summarySentences_10TIT = doc.extractSummary(tenPercSummaryInt, SummaryTypeENUM.TITILE_SIM);
					List<Integer> summarySentencesID_10TIT = new ArrayList<Integer>();
					for(Sentence summarySentence : summarySentences_10TIT) {
						if(summarySentence != null && summarySentence.getId() != null) {
							summarySentencesID_10TIT.add(summarySentence.getId());
						}
					}

					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						if(summarySentencesID_10TIT.contains(sent.getId())) {
							sent.getSummaryRank().put("TITILE_SIM_10perc", "true");
						}
						else {
							sent.getSummaryRank().put("TITILE_SIM_10perc", "false");
						}
					}

					// 30% - TITILE_SIM
					Double thirtyPercSummary = documentSentences * 0.30d;
					Integer thirtyPercSummaryInt = thirtyPercSummary.intValue();

					List<Sentence> summarySentences_30TIT = doc.extractSummary(thirtyPercSummaryInt, SummaryTypeENUM.TITILE_SIM);
					List<Integer> summarySentencesID_30TIT = new ArrayList<Integer>();
					for(Sentence summarySentence : summarySentences_30TIT) {
						if(summarySentence != null && summarySentence.getId() != null) {
							summarySentencesID_30TIT.add(summarySentence.getId());
						}
					}

					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						if(summarySentencesID_30TIT.contains(sent.getId())) {
							sent.getSummaryRank().put("TITILE_SIM_30perc", "true");
						}
						else {
							sent.getSummaryRank().put("TITILE_SIM_30perc", "false");
						}
					}
					
					// 10% - LEXRANK
					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						sent.getSummaryRank().put("LEXRANK_10perc", "unavailabe");
					}
					
					List<Sentence> summarySentences_10LR = doc.extractSummary(tenPercSummaryInt, SummaryTypeENUM.LEX_RANK);
					List<Integer> summarySentencesID_10LR = new ArrayList<Integer>();
					for(Sentence summarySentence : summarySentences_10LR) {
						if(summarySentence != null && summarySentence.getId() != null) {
							summarySentencesID_10LR.add(summarySentence.getId());
						}
					}

					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						if(summarySentencesID_10LR.contains(sent.getId())) {
							sent.getSummaryRank().put("LEXRANK_10perc", "true");
						}
						else {
							sent.getSummaryRank().put("LEXRANK_10perc", "false");
						}
					}
					

					// 30% - LEXRANK
					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						sent.getSummaryRank().put("LEXRANK_30perc", "unavailabe");
					}
					
					List<Sentence> summarySentences_30LR = doc.extractSummary(thirtyPercSummaryInt, SummaryTypeENUM.LEX_RANK);
					List<Integer> summarySentencesID_30LR = new ArrayList<Integer>();
					for(Sentence summarySentence : summarySentences_30LR) {
						if(summarySentence != null && summarySentence.getId() != null) {
							summarySentencesID_30LR.add(summarySentence.getId());
						}
					}

					for(SentenceJSON sent : documentJSON.getBodySentences()) {
						if(summarySentencesID_30LR.contains(sent.getId())) {
							sent.getSummaryRank().put("LEXRANK_30perc", "true");
						}
						else {
							sent.getSummaryRank().put("LEXRANK_30perc", "false");
						}
					}
					
				}
				catch(Exception e) {
					/* DO NOTHING */
				}

				// Experimental (SEPLN): affiliation disambiguation data - synsets
				try {
					DocumentImpl docImpl = (DocumentImpl) doc;

					List<Annotation> affilList = GateUtil.getAnnInDocOrder(docImpl.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet,
							ImporterPDFEXT.PDFEXTaffiliation);

					for(Annotation affil : affilList) {
						if(affil != null) {
							try {
								// Set Babelnet synset occurrences
								List<Annotation> babelnetSynsetOccList = GateUtil.getAnnInDocOrderContainedAnn(docImpl.cacheManager.getGateDoc(), ImporterBase.babelnet_AnnSet,
										ImporterBase.babelnet_DisItem, affil);
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
											BabelSynsetOcc newBabelnetSynsetOcc = ObjectGenerator.getBabelnetSynsetOccFromId(babelnetSynsetOcc.getId(), docImpl.cacheManager);
											if(newBabelnetSynsetOcc != null) {
												documentJSON.getAffil_synsets().add(newBabelnetSynsetOcc);
											}
										}
									}
								}
							}
							catch(Exception e) {
								/* DO NOTHING */
							}
						}
					}
				}
				catch(Exception e) {
					/* DO NOTHING */
				}


				// Experimental (SEPLN): keyword disambiguation data - synset
				try {
					DocumentImpl docImpl = (DocumentImpl) doc;

					List<Annotation> keywordList = GateUtil.getAnnInDocOrder(docImpl.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet,
							ImporterPDFEXT.PDFEXTkeywordsText);

					for(Annotation keyw : keywordList) {
						if(keyw != null) {
							try {
								// Set Babelnet synset occurrences
								List<Annotation> babelnetSynsetOccList = GateUtil.getAnnInDocOrderContainedAnn(docImpl.cacheManager.getGateDoc(), ImporterBase.babelnet_AnnSet,
										ImporterBase.babelnet_DisItem, keyw);
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
											BabelSynsetOcc newBabelnetSynsetOcc = ObjectGenerator.getBabelnetSynsetOccFromId(babelnetSynsetOcc.getId(), docImpl.cacheManager);
											if(newBabelnetSynsetOcc != null) {
												documentJSON.getKeyword_synsets().add(newBabelnetSynsetOcc);
											}
										}
									}
								}
							}
							catch(Exception e) {
								/* DO NOTHING */
							}
						}
					}
				}
				catch(Exception e) {
					/* DO NOTHING */
				}


				// Experimental (SEPLN): affiliation disambiguation data - googlemaps
				try {
					DocumentImpl docImpl = (DocumentImpl) doc;

					List<Annotation> affilList = GateUtil.getAnnInDocOrder(docImpl.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet,
							ImporterPDFEXT.PDFEXTaffiliation);

					for(Annotation affil : affilList) {
						if(affil != null && affil.getFeatures() != null && affil.getFeatures().size() > 0) {
							
							Map<String, String> documentJSONmap = new HashMap<String, String>();
							documentJSON.getAffil_gmaps().add(documentJSONmap);
							
							String affilText = GateUtil.getAnnotationText(affil, docImpl.cacheManager.getGateDoc()).orElse(null);
							if(affilText != null) {
								documentJSONmap.put("fullText", affilText);
							}
							
							for(Entry<Object, Object> featEntry : affil.getFeatures().entrySet()) {
								try {
									if(featEntry.getKey() != null && ((String) featEntry.getKey()).startsWith("gmaps_") && featEntry.getValue() != null) {
										documentJSONmap.put(((String) featEntry.getKey()).replace("gmaps_", ""), (String) featEntry.getValue());
									}
								}
								catch(Exception e) {
									/* DO NOTHING */
								}
							}
						}
					}
				}
				catch(Exception e) {
					/* DO NOTHING */
				}
				
				// Experimental (SEPLN): affiliation disambiguation data - spotlight
				try {
					DocumentImpl docImpl = (DocumentImpl) doc;

					List<Annotation> affilList = GateUtil.getAnnInDocOrder(docImpl.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet,
							ImporterPDFEXT.PDFEXTaffiliation);

					for(Annotation affil : affilList) {
						if(affil != null && affil.getFeatures() != null && affil.getFeatures().size() > 0) {
							
							Map<String, String> documentJSONmap = new HashMap<String, String>();
							documentJSON.getAffil_spotlight().add(documentJSONmap);
							
							String affilText = GateUtil.getAnnotationText(affil, docImpl.cacheManager.getGateDoc()).orElse(null);
							if(affilText != null) {
								documentJSONmap.put("fullText", affilText);
							}
							
							for(Entry<Object, Object> featEntry : affil.getFeatures().entrySet()) {
								try {
									if(featEntry.getKey() != null && ((String) featEntry.getKey()).startsWith("slight_") && featEntry.getValue() != null) {
										documentJSONmap.put(((String) featEntry.getKey()).replace("slight_", ""), (String) featEntry.getValue());
									}
								}
								catch(Exception e) {
									/* DO NOTHING */
								}
							}
						}
					}
				}
				catch(Exception e) {
					/* DO NOTHING */
				}

			}

			// Abstract graph
			String abstractROS_CSV = DocParse.getDocumentROSasCSVstring(doc, SentenceSelectorENUM.ONLY_ABSTRACT);
			CSVReader abstractROS_CSVreader = new CSVReader(new StringReader(abstractROS_CSV));
			String [] nextLine;
			try {
				while ((nextLine = abstractROS_CSVreader.readNext()) != null) {
					if(nextLine != null && nextLine.length == 6) {
						// "707","44475","Each participant","15735","was given","SBJ"
						if(nextLine[0] == null || nextLine[1] == null || nextLine[2] == null || nextLine[3] == null || nextLine[4] == null || nextLine[5] == null) {
							continue;
						}
						try {
							TripleJSON newTriple = new TripleJSON(nextLine[5]);
							newTriple.setId(Integer.valueOf(nextLine[0]));
							newTriple.setFromId(Integer.valueOf(nextLine[1]));
							newTriple.setToId(Integer.valueOf(nextLine[3]));
							newTriple.setFromName(nextLine[2]);
							newTriple.setToName(nextLine[4]);
							documentJSON.getAbstractGraph().add(newTriple);
						}
						catch (Exception e) {
							logger.error("Error adding triple (content graph).");
						}
					}
				}
			} catch (IOException e) {
				logger.error("Error generating abstract graph.");
			}

			// Content graph
			String contentROS_CSV = DocParse.getDocumentROSasCSVstring(doc, SentenceSelectorENUM.ALL_EXCEPT_ABSTRACT);
			CSVReader contentROS_CSVreader = new CSVReader(new StringReader(contentROS_CSV));
			try {
				while ((nextLine = contentROS_CSVreader.readNext()) != null) {
					if(nextLine != null && nextLine.length == 6) {
						// "707","44475","Each participant","15735","was given","SBJ"
						if(nextLine[0] == null || nextLine[1] == null || nextLine[2] == null || nextLine[3] == null || nextLine[4] == null || nextLine[5] == null) {
							continue;
						}
						try {
							TripleJSON newTriple = new TripleJSON(nextLine[5]);
							newTriple.setId(Integer.valueOf(nextLine[0]));
							newTriple.setFromId(Integer.valueOf(nextLine[1]));
							newTriple.setToId(Integer.valueOf(nextLine[3]));
							newTriple.setFromName(nextLine[2]);
							newTriple.setToName(nextLine[4]);
							documentJSON.getContentGraph().add(newTriple);
						}
						catch (Exception e) {
							logger.error("Error adding triple (abstract graph).");
						}
					}
				}
			} catch (IOException e) {
				logger.error("Error generating content graph.");
			}

		} catch (InternalProcessingException e) {
			e.printStackTrace();
		}

		String retJson = "";
		try {
			retJson = mapper.writeValueAsString(documentJSON);
		} catch (JsonProcessingException e) {
			Util.notifyException("Error generating document JSON", e, logger);
		}

		return retJson;
	}


	/**
	 * Get the ordered list of sentences of a document, selected with respect to the value of the second parameter (sentenceSel)
	 * 
	 * @param doc
	 * @param sentenceSel
	 * @param includeTokens true to include the list of tokens of each sentence, each one with lemma and POS
	 * @param includeBabelnetSynsets true to include the list of Babelnet sysets spotted inside each sentence
	 * @return
	 */
	private static List<SentenceJSON> getSentenceJSONlist(Document doc, SentenceSelectorENUM sentenceSel,
			boolean includeTokens, boolean includeBabelnetSynsets) {

		List<SentenceJSON> sentenceListJSON = new ArrayList<SentenceJSON>();

		if(doc != null) {
			sentenceSel = (sentenceSel != null) ? sentenceSel : SentenceSelectorENUM.ALL;

			List<Sentence> sentenceList = new ArrayList<Sentence>();
			List<Sentence> abstractSentenceList = new ArrayList<Sentence>();
			Set<Integer> abstractSentenceIds = new HashSet<Integer>();
			try {
				sentenceList = doc.extractSentences(sentenceSel);

				abstractSentenceList = doc.extractSentences(SentenceSelectorENUM.ONLY_ABSTRACT);
				for(Sentence sent : abstractSentenceList) {
					abstractSentenceIds.add(sent.getId());
				}
			} catch (InternalProcessingException e) {
				e.printStackTrace();
			}

			Integer orderNum = 0;
			for(Sentence sent: sentenceList) {
				if(sent != null) {
					orderNum++;
					SentenceJSON sentJSON = new SentenceJSON();

					sentJSON.setId(sent.getId());
					sentJSON.setText(sent.getText());
					sentJSON.setGlobalOrderNumber(new Integer(orderNum));

					if(abstractSentenceIds.contains(sent.getId())) {
						sentJSON.setAbstract(true);
						sentJSON.setSect_name("ABSTRACT");
					}
					else {
						sentJSON.setAbstract(false);
					}

					sentJSON.setAbstractId(sent.getAbstractId());

					sentJSON.setLanguage((sent.getLanguage() != null) ? sent.getLanguage() + "" : "");

					if(sent.getContainingSection() != null) {
						sentJSON.setSect_id(sent.getContainingSection().getId());

						String sectName = (sent.getContainingSection() != null && sent.getContainingSection().getName() != null) ? sent.getContainingSection().getName() : "";
						if(abstractSentenceIds.contains(sent.getId())) {
							sectName = "ABSTRACT";
						}

						sentJSON.setSect_name(sectName);

						sentJSON.setSect_nastingLevel(sent.getContainingSection().getLevel());

						// Look for root section
						Section currentSection = sent.getContainingSection();
						while(currentSection.getParentSection() != null) {
							currentSection = currentSection.getParentSection();
						}
						sentJSON.setSect_rootName(currentSection.getName());

						sentJSON.setSect_language((currentSection.getLanguage() != null) ? currentSection.getLanguage() + "" : "");
					}

					sentJSON.setRhetoricalClass((sent.getRhetoricalClass() != null) ? sent.getRhetoricalClass().toString() : RhetoricalClassENUM.STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION.toString());

					if(includeTokens) {
						sentJSON.setTokens(sent.getTokens());
					}

					if(includeBabelnetSynsets) {
						sentJSON.setBabelSynsetsOcc(sent.getBabelSynsetsOcc());
					}

					sentJSON.setCitationMarkers(sent.getCitationMarkers());

					sentJSON.setIsAcknowledgment(((sent.isAcknowledgment()) ? "true" : "false"));

					Map<String, String> metaEntityMap = new HashMap<String, String>();
					if(sent.getSpottedEntities() != null && sent.getSpottedEntities().size() > 0) {
						for(Entry<String, MetaEntityTypeENUM> spottedEntity : sent.getSpottedEntities().entrySet()) {
							if(spottedEntity != null && spottedEntity.getKey() != null && !spottedEntity.getKey().equals("") && spottedEntity.getValue() != null) {
								metaEntityMap.put(spottedEntity.getKey(), spottedEntity.getValue().toString());
							}
						}
					}
					sentJSON.setMetaEntityNameTypeMap(metaEntityMap);

					// Experimental (SEPLN): get list of DIV IDs
					try {
						DocumentImpl docImpl = (DocumentImpl) doc;
						Integer sentID = sent.getId();

						Annotation sentAnn = docImpl.cacheManager.getGateDoc().getAnnotations(ImporterBase.driAnnSet).get(ImporterBase.sentenceAnnType).get(sentID);
						List<Annotation> overlappingDivs = GateUtil.getAnnInDocOrderIntersectAnn(docImpl.cacheManager.getGateDoc(), ImporterPDFEXT.PDFEXTAnnSet, "div", sentAnn);

						if(overlappingDivs != null && overlappingDivs.size() > 0) {
							for(Annotation overlappingDiv : overlappingDivs) {
								if(overlappingDiv != null && overlappingDiv.getFeatures() != null && overlappingDiv.getFeatures().containsKey("id") &&
										overlappingDiv.getFeatures().get("id") != null) {
									try {
										Integer divID = Integer.valueOf((String) overlappingDiv.getFeatures().get("id"));
										sentJSON.getPDFEXTdivIds().add(divID);
									}
									catch(Exception e) {
										/* Do nothing */
									}

								}
							}
						}

					}
					catch(Exception e) {
						/* DO NOTHING */
					}

					// Add sentence JSON to ret list
					sentenceListJSON.add(sentJSON);
				}
			}
		}

		return sentenceListJSON;
	}

}
