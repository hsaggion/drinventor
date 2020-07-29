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
package edu.upf.taln.dri.module.rhetclassifier.feats.generator.opinionfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


public class SubjectivityReader {
	private static Logger logger = Logger.getLogger(SubjectivityReader.class.getName());

	private static Map<String, SubjectivityElem> subjectiveClauseMap = null;

	// Constructor
	public static void init() {
		
		if(subjectiveClauseMap != null && subjectiveClauseMap.size() > 0) {
			// logger.info("Already initialized subjectivity clauses list with " + subjectiveClauseMap.size() + " subjectivity clauses from Opinion Finder.");
			return;
		}
		
		
		subjectiveClauseMap = new HashMap<String, SubjectivityElem>();

		logger.info("Initializing Opinion Finder subjectivity clause list...");

		// Read file and populate map
		InputStream subjCueFileIS = SubjectivityReader.class.getResourceAsStream("/opinionfinder/subjcues/subjclueslen1-HLTEMNLP05.tff");

		String str = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(subjCueFileIS));
			if(subjCueFileIS != null) {                         
				while ((str = reader.readLine()) != null) { 
					str = str.trim();
					if(!str.startsWith("#")) {
						try {
							String[] splittedSubjEntry = str.split(" ");
							String[] splittedSubjEntryNorm = Arrays.copyOf(splittedSubjEntry, 6);

							// Structure of a line: type=weaksubj len=1 word1=abandoned pos1=adj stemmed1=n priorpolarity=negative
							if(splittedSubjEntryNorm.length == 6) {
								SubjectivityTypeENUM subjType = null;
								if(splittedSubjEntryNorm[0].trim().startsWith("type=")) {
									String type = splittedSubjEntryNorm[0].trim().replace("type=", "");
									
									// strongsubj or weaksubj  
									if(type.trim().toLowerCase().equals("strongsubj")) {
										subjType = SubjectivityTypeENUM.STRONG_SUBJ;
									} else if (type.trim().toLowerCase().equals("weaksubj")) {
										subjType = SubjectivityTypeENUM.WEAK_SUBJ;
									}
								}
								
								Integer numWords = null;
								if(splittedSubjEntryNorm[1].trim().startsWith("len=")) {
									String numW = splittedSubjEntryNorm[1].trim().replace("len=", "");
									
									try {
										Integer numWint = Integer.valueOf(numW);
										numWords = numWint;
									}
									catch (NumberFormatException nfe) {
										nfe.printStackTrace();
									}
								}
								
								String text = null;
								if(splittedSubjEntryNorm[2].trim().startsWith("word1=")) {
									String textRet = splittedSubjEntryNorm[2].trim().replace("word1=", "");
									
									if(textRet != null && !textRet.equals("")) {
										text = textRet.trim();
									}
								}
								
								PosENUM pos = null;
								if(splittedSubjEntryNorm[3].trim().startsWith("pos1=")) {
									String posStr = splittedSubjEntryNorm[3].trim().replace("pos1=", "");
									
									// anypos, verb, noun, adverb, adj
									if(posStr.trim().toLowerCase().equals("anypos")) {
										pos = PosENUM.anypos;
									} else if (posStr.trim().toLowerCase().equals("verb")) {
										pos = PosENUM.verb;
									} else if (posStr.trim().toLowerCase().equals("noun")) {
										pos = PosENUM.noun;
									} else if (posStr.trim().toLowerCase().equals("adverb")) {
										pos = PosENUM.adverb;
									} else if (posStr.trim().toLowerCase().equals("adj")) {
										pos = PosENUM.adj;
									}
								}
								
								
								Boolean stemmed = null;
								if(splittedSubjEntryNorm[4].trim().startsWith("stemmed1=")) {
									String stemmedStr = splittedSubjEntryNorm[4].trim().replace("stemmed1=", "");
									
									if(stemmedStr != null && !stemmedStr.equals("")) {
										if (stemmedStr.trim().toLowerCase().equals("y")) {
											stemmed = true;
										}
										else if (stemmedStr.trim().toLowerCase().equals("n")) {
											stemmed = false;
										}
									}
								}
								
								PriorPolarityENUM priorPlarity = null;
								if(splittedSubjEntryNorm[5].trim().startsWith("priorpolarity=")) {
									String priorPolarityStr = splittedSubjEntryNorm[5].trim().replace("priorpolarity=", "");
									
									// positive, negative, both, neutral
									if(priorPolarityStr.trim().toLowerCase().equals("positive")) {
										priorPlarity = PriorPolarityENUM.positive;
									} else if (priorPolarityStr.trim().toLowerCase().equals("negative")) {
										priorPlarity = PriorPolarityENUM.negative;
									} else if (priorPolarityStr.trim().toLowerCase().equals("both")) {
										priorPlarity = PriorPolarityENUM.both;
									} else if (priorPolarityStr.trim().toLowerCase().equals("neutral")) {
										priorPlarity = PriorPolarityENUM.neutral;
									}
								}
								
								// Populate all vars from file values
								if(subjType != null && numWords != null && text != null && pos != null && 
										stemmed != null && priorPlarity != null) {
									SubjectivityElem newSubjElem = new SubjectivityElem(subjType, numWords, text, pos, stemmed, priorPlarity);
									subjectiveClauseMap.put(text, newSubjElem);
								}
								else {
									logger.warning("Impossible to parse Opinion Finder Subjectivity cue list line: " + str);
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}	                

				}               
			}

			logger.info("Added " + subjectiveClauseMap.size() + " subjectivity clauses from Opinion Finder Subjectivity Clause List.");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { subjCueFileIS.close(); } catch (Throwable ignore) {}
		}
	}

	/**
	 * Look forSubjectivityElem by exact matching term and pos 
	 * 
	 * @param term
	 * @param pos if pos is null all exact matching terms are considered
	 * @return
	 */
	public static List<SubjectivityElem> getSubjectivityElemOfTerm(String term, PosENUM pos) {
		List<SubjectivityElem> matchingSubjElem = new ArrayList<SubjectivityElem>();
		
		if(term == null || term.equals("") || pos == null) {
			return matchingSubjElem;
		}
		
		if(subjectiveClauseMap == null) {
			SubjectivityReader.init();
		}
		
		for(Entry<String, SubjectivityElem> entry : subjectiveClauseMap.entrySet()) {
			if(entry != null && entry.getKey() != null && entry.getValue() != null) {
				String text = entry.getKey();
				SubjectivityElem subjElem = entry.getValue();
				if(text.equals(term.toLowerCase().trim())) {
					if(pos == null || (pos != null && subjElem.getPos().equals(pos)) ) {
						matchingSubjElem.add(subjElem);
					}
				}
			}
		}
		
		return matchingSubjElem;
	}
	

	public static void main(String[] args) {
		SubjectivityReader.init();
	}
}
