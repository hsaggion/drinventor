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

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import au.com.bytecode.opencsv.CSVWriter;
import edu.upf.taln.dri.common.util.GateUtil;
import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.RhetoricalClassENUM;
import edu.upf.taln.dri.lib.model.ext.Section;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.graph.DependencyGraph;
import edu.upf.taln.dri.lib.model.graph.DocGraphTypeENUM;
import edu.upf.taln.dri.lib.model.graph.SentGraphTypeENUM;
import edu.upf.taln.dri.module.importer.ImporterBase;
import edu.upf.taln.dri.module.importer.SourceENUM;
import gate.Annotation;
import gate.Document;
import gate.util.InvalidOffsetException;

/**
 * Collection of utility methods extract specific data from document annotations
 * 
 *
 */
public class DocParse {

	private static Logger logger = Logger.getLogger(DocParse.class.getName());


	/**
	 *  Get the CSV representation of the sentence graphs of a DRI Document object by extracting from a document a CSV table
	 *  with the following column-tab-separated row format to support ROS population: <br/>
	 *  
	 *  SENTENCE_ID <br/>
	 *  START_NODE_ID START_NODE_WORD START_NODE_LEMMA START_NODE_POS START_NODE_CHAIN_ID START_NODE_COREF_WORD <br/>
	 *  END_NODE_ID END_NODE_WORD END_NODE_LEMMA END_NODE_POS END_NODE_CHAIN_ID START_NODE_COREF_WORD <br/>
	 *  EDGE_TYPE  <br/>
	 *  SENT_RHETL_CLASS SENT_SECTION_NAME SENT_ROOT_SECTION_NAME SENT_DOC_POSITION SENT_NUM_CITS <br/>
	 *  
	 *  The following features are equal for all the triples of a sentence:<br/>
	 *  - SENT_RHETL_CLASS: the rhetorical class assigned to the sentence<br/>
	 *  - SENT_SECTION_NAME: the name of the section the sentence belongs to<br/>
	 *  - SENT_ROOT_SECTION_NAME: the name of the root section the sentence belongs to (if any, in case SENT_SECTION_NAME is a sub-section name)<br/>
	 *  - SENT_DOC_POSITION: the position of the sentence inside the document - number in the interval [0,1]<br/>
	 *  - SENT_NUM_CITS: the number of citations that the sentence includes<br/><br/>
	 *  
	 * SENTENCE_ID, START_NODE_ID, END_NODE_ID and CHAIN_ID are CSV unique identifiers.<br/><br/><br/>
	 * 
	 * Important: you need to enable the following modules to correctly extract ROS graphs: GraphParsing, CoreferenceResolution, CausalityParsing<br/><br/>
	 * 
	 * 
	 * --- Coreferences ---<br/>
	 * The column useful to identify the coreference chain a node belongs to and the group of nodes that form together an element of the coreference chain are 4:<br/>
	 * START_NODE_CHAIN_ID, START_NODE_COREF_WORD, END_NODE_CHAIN_ID, START_NODE_COREF_WORD<br/><br/>
	 * Each triple / row of the CSV identifies the relation among two nodes by means of a specific property (SBJ, OBJ, CAUSE, etc.).<br/>
	 * Each element of a coreference chain can span over one or more nodes. As a consequence we can get an element of a corefernce chain that is: "the sunny sky",
	 * made of three nodes: the, sunny, sky.<br/>
	 * Each one of these three nodes (the, sunny, sky) have its own ID (NODE_ID column), independently of its role in a triple.<br/>
	 * All the nodes belonging to a coreference chain will have both the NODE_CHAIN_ID and the NODE_COREF_WORD columns with values different from 'NONE'.<br/>
	 * In particular for each node blonging to a coreference chain:<br/>
	 * - the column NODE_CHAIN_ID identifies with an integer ID the coreference chain the nodes belongs to. As a consequence, all the nodes belonging to the same coreference
	 * chain will share the same integer ID in their NODE_CHAIN_ID column;<br/>
	 * - the column NODE_COREF_WORD all the nodes that belong to a coreference chain have a value different from "NONE" for this column. This column is the real name of the
	 * node in the coreference chain, that usually includes surrounding words.<br/>
	 *
	 * For instance, if we have the following element of a coreference chain: "the sunny sky",
	 * such element will span over three nodes in the CSV: 'the', 'sunny', 'sky'. In this case, one of these three nodes ('sky' - the head node of the coreference chain element) 
	 * will have both its NODE_CHAIN_ID and its NODE_COREF_WORD values diferent from "NONE". In particular, the NODE_CHAIN_ID value of the 'sky' node will be equal to the
	 * integer that identifies all the nodes of that coreference chain. While the NODE_COREF_WORD of the 'sky' node will be equal to "the sunny sky", that is the complete
	 * name of that element of the coreference chain.<br/>
	 * The other two nodes, 'the' and 'sunny', (non head nodes of the coreference chain element) will both have their NODE_CHAIN_ID and NODE_COREF_WORD values equal to "NONE".<br/>
	 * <br/><br/><br/>
	 * Example 1:<br/>
	 * "1","18","Karla","karla","NNP","542","Karla","28","lived","live","VBD","NONE","NONE","SBJ","STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION","","","0.125","0"<br/>
	 * "3","80","that","that","WDT","NONE","NONE","82","had","have","VBD","NONE","NONE","SBJ","STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION","","","0.25","0"<br/>
	 * "3","52","she","she","PRP","542","she","54","saw","saw","VBD","NONE","NONE","SBJ","STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION","","","0.25","0"<br/><br/>
	 * The first row represents the triple "Karla" --> SBJ --> "lived". The node "Karla" (with id 18) belongs to the coreference chain with id "542".<br/>
	 * The third row represents the triple "she" --> SBJ --> "saw". The node "she" (with id 52) belongs to the coreference chain with id "542" that is the same
	 * coreference chain of the node Karla.<br/>
	 * Both nodes "Karla" (with id 18) and "she" (with id 52) are coreferent, refer to the smae entity and can be merged in the same node.<br/>
	 * <br/><br/><br/>
	 * Example 2:<br/>
	 * "11","173","hunter","hunter","NN","533","the hunter","175","wanted","want","VBD","NONE","NONE","SBJ","STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION","","","0.75","0"<br/><br/>
	 * This row represents the triple "hunter" --> SBJ --> "wanted". The node "hunter" (with id 173) belongs to the coreference chain with id "533".<br/>
	 * The actual name of this node in the coreference chain is "the hunter" (as we can see from the seventh column - START_NODE_COREF_WORD), 
	 * since the coreference chin element that has as head the node "hunter" span over this node and the node "the" that is included in the 
	 * coreference chain element name of the node "hunter".<br/>
	 * 
	 * 
	 * 
	 * @param doc
	 * @param sentenceSelector
	 * @param graphType
	 * @return
	 * @throws InternalProcessingException 
	 * @deprecated
	 * IMPORTANT: this method is deprecated in favor of the method {@link #getDocumentROSasCSVstring(edu.upf.taln.dri.lib.model.Document, SentenceSelectorENUM) getDocumentROSasCSVstring} that generates a more compact and useful ROS
	 */
	@Deprecated
	public static String getTokenROSasCSVstring(edu.upf.taln.dri.lib.model.Document doc, SentenceSelectorENUM sentenceSelector, DocGraphTypeENUM graphType) throws InternalProcessingException {

		SentGraphTypeENUM sentGraphType = null;
		if(graphType == null){
			sentGraphType = SentGraphTypeENUM.DEP;
		}
		else {
			switch (graphType) {
			case DEP:
				sentGraphType = SentGraphTypeENUM.DEP;
				break;
			case DEP_SBJ_VERB_OBJ_CAUSE:
				sentGraphType = SentGraphTypeENUM.DEP_SBJ_VERB_OBJ_CAUSE;
				break;
			default:
				sentGraphType = SentGraphTypeENUM.DEP;
			}
		}

		StringWriter csvStringWriter = new StringWriter();
		String resultString = null;
		try {
			CSVWriter writer = new CSVWriter(csvStringWriter);
			DecimalFormat df = new DecimalFormat("#.####");

			// Check document type
			if(doc.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
				sentenceSelector = SentenceSelectorENUM.ALL;
			}

			List<Sentence> sentenceList = doc.extractSentences(sentenceSelector);


			Set<Integer> abstractSentenceIds = new HashSet<Integer>();
			if(!doc.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) { // For plain text sources abstract sentences cannot be determined
				List<Sentence> abstractSentenceList = doc.extractSentences(SentenceSelectorENUM.ONLY_ABSTRACT);
				for(Sentence sent : abstractSentenceList) {
					abstractSentenceIds.add(sent.getId());
				}
			}

			if(!CollectionUtils.isEmpty(sentenceList)) {
				logger.info("Generating ROS CSV from " + sentenceList.size() + " sentences "
						+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + " and "
						+ "graph type: " + ((sentGraphType == null) ? "NULL" : sentGraphType.toString()) + ") ...");
				Integer sentenceCounter = 0;
				for(Sentence sent : sentenceList) {
					sentenceCounter++;
					if(sent != null && sent.getId() != null) {
						try {
							DependencyGraph depGraph = doc.extractSentenceGraph(sent.getId(), sentGraphType);
							Sentence sentence = doc.extractSentenceById(sent.getId());

							for(Entry<Integer, Pair<Integer, Integer>> edgeELem : depGraph.getAllEdges().entrySet()) {
								if(edgeELem.getKey() != null && edgeELem.getValue() != null && edgeELem.getValue().getRight() != null && edgeELem.getValue().getLeft() != null) {
									Integer edgeId = edgeELem.getKey();
									Integer sentenceId = sent.getId();


									/* FROM NODE */
									Integer fromNode = edgeELem.getValue().getLeft();
									String fromNodeWord = depGraph.getNodeName(fromNode).replace("\n", " ");
									String fromNodeLemma = depGraph.getNodeLemma(fromNode).replace("\n", " ");
									String fromNodePOS = depGraph.getNodePOS(fromNode).replace("\n", " ");

									// Generating to node coreference chain id list of the token
									Set<Integer> fromNodeCorefChainIDsOfToken = depGraph.getNodeCorefID(fromNode);
									String fromNodeCorefChainIDsStr = "";
									if(fromNodeCorefChainIDsOfToken != null && fromNodeCorefChainIDsOfToken.size() > 0) {
										for(Integer fromNodeCorefChainID : fromNodeCorefChainIDsOfToken) {
											if(fromNodeCorefChainID != null) {
												if(fromNodeCorefChainIDsStr.length() > 0) fromNodeCorefChainIDsStr += ",";
												fromNodeCorefChainIDsStr += fromNodeCorefChainID;
											}
										}
									}
									String fromNodeCorefChainID = (fromNodeCorefChainIDsStr != null && !fromNodeCorefChainIDsStr.equals("")) ? fromNodeCorefChainIDsStr : "NONE";

									// Generate the coreference words of the token
									String fromNodeCorefName = (depGraph.getNodeCorefName(fromNode) != null) ? depGraph.getNodeCorefName(fromNode).replace("\n", " ") : "NONE";


									/* TO NODE */
									Integer toNode = edgeELem.getValue().getRight();
									String toNodeWord = depGraph.getNodeName(toNode).replace("\n", " ");
									String toNodeLemma = depGraph.getNodeLemma(toNode).replace("\n", " ");
									String toNodePOS = depGraph.getNodePOS(toNode).replace("\n", " ");

									// Generating to node coreference chain id of the token
									Set<Integer> toNodeCorefChainIDsOfToken = depGraph.getNodeCorefID(toNode);
									String toNodeCorefChainIDsStr = "";
									if(toNodeCorefChainIDsOfToken != null && toNodeCorefChainIDsOfToken.size() > 0) {
										for(Integer toNodeCorefChainID : toNodeCorefChainIDsOfToken) {
											if(toNodeCorefChainID != null) {
												if(toNodeCorefChainIDsStr.length() > 0) toNodeCorefChainIDsStr += ",";
												toNodeCorefChainIDsStr += toNodeCorefChainID;
											}
										}
									}
									String toNodeCorefChainID = (toNodeCorefChainIDsStr != null && !toNodeCorefChainIDsStr.equals("")) ? toNodeCorefChainIDsStr : "NONE";

									// Generate the coreference words of the token
									String toNodeCorefName = (depGraph.getNodeCorefName(toNode) != null) ? depGraph.getNodeCorefName(toNode).replace("\n", " ") : "NONE";


									/* EDGE */
									String edgeName = depGraph.getEdgeName(edgeId);

									/* Sentence level features */
									String rhetoricalClass = (sentence.getRhetoricalClass() != null) ? sentence.getRhetoricalClass().toString() : RhetoricalClassENUM.STILL_NOT_EXECUTED_RHETORICAL_CLASSIFICATION.toString();
									String sectName = (sentence.getContainingSection() != null && sentence.getContainingSection().getName() != null) ? sentence.getContainingSection().getName().replace("\n", " ") : "";
									if(abstractSentenceIds.contains(sentence.getId())) {
										sectName = "ABSTRACT";
									}

									Section parentSection = sentence.getContainingSection();
									while((parentSection != null) && (parentSection.getParentSection() != null)) {
										parentSection = parentSection.getParentSection();
									}
									String parentSectName = (parentSection != null && parentSection != sentence.getContainingSection() && parentSection.getName() != null) ? parentSection.getName() : ""; 

									double totSentences = new Double(sentenceList.size());
									double sentPosition = new Double(sentenceCounter);
									String sentencePosition = df.format(sentPosition / totSentences);

									String numCitations = sentence.getCitationMarkers().size() + "";

									List<String> recordList = new ArrayList<String>(); 
									recordList.add(sentenceId.toString());
									recordList.add(fromNode.toString());

									// The from node word can be a multi-ẃord expression if more than one node of the dep tree has been merged
									// Since multi-ẃord expressions can not be managed by the ROS mapping, if the fromNodeWord is multi-word, the related lemma is used
									if(fromNodeWord.trim().contains(" ")) {
										// fromNodeWord is multi-word
										recordList.add(fromNodeLemma);
									}
									else {
										// fromNodeWord is not multi-word
										recordList.add(fromNodeWord);
									}

									recordList.add(fromNodeLemma);
									recordList.add(fromNodePOS);
									recordList.add(fromNodeCorefChainID);
									recordList.add(fromNodeCorefName);
									recordList.add(toNode.toString());

									// The to node word can be a multi-ẃord expression if more than one node of the dep tree has been merged
									// Since multi-ẃord expressions can not be managed by the ROS mapping, if the toNodeWord is multi-word, the related lemma is used
									if(toNodeWord.trim().contains(" ")) {
										// toNodeWord is multi-word
										recordList.add(toNodeLemma);
									}
									else {
										// fromNodeWord is not multi-word
										recordList.add(toNodeWord);
									}

									recordList.add(toNodeLemma);
									recordList.add(toNodePOS);
									recordList.add(toNodeCorefChainID);
									recordList.add(toNodeCorefName);
									recordList.add(edgeName);

									// Sentence level features
									recordList.add(rhetoricalClass);
									recordList.add(sectName);
									recordList.add(parentSectName);
									recordList.add(sentencePosition);
									recordList.add(numCitations);

									String[] recordArray = new String[recordList.size()];
									recordArray = recordList.toArray(recordArray);

									writer.writeNext(recordArray);
								}
							}

							if(sentenceCounter % 10 == 0) {
								logger.info("Processed " + sentenceCounter + " sentences over " + sentenceList.size() + "...");
							}
						}
						catch (Exception e) {
							Util.notifyException("Parsing sentence to generate graph / CSV", e, logger);
						}
					}
				}
			}

			resultString = csvStringWriter.toString();
			csvStringWriter.close();
			writer.close();
			logger.info("Generated ROS CSV from " + sentenceList.size() + " sentences " 
					+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + " and "
					+ "graph type: " + ((sentGraphType == null) ? "NULL" : sentGraphType.toString()) + ").");
		}
		catch (Exception e) {
			Util.notifyException("Converting Document to graph / CSV (token)", e, logger);
		}

		return resultString;
	}

	/**
	 * This method generates "NODE -> SUBJECT -> VERB_NODE" and "NODE -> OBJECT -> VERB_NODE" and "CAUSE_NODE -> CAUSE -> EFFECT_NODE" triples.<br/>
	 * Because of coreference resolution, the non verb NODES (subjects and objects of SUBJECT / OBJECT triples) 
	 * can represent the aggregation, performed by means of a coreference resolutor, of several nodes coming from several sentence.<br/>
	 * In case a non verb NODE is the rsult of the aggregation of nodes by coreference resolutions, the text / name of the NODE 
	 * is the sequence of the texts / names of the nodes aggregated, separated by three underscores (name1___name2___name3). 
	 * For instance a node with text / name 'she___Karla' means that such node aggregated all nodes with text 'she'
	 * or 'Karla' by means of the coreference resolutor (both nodes 'she' and 'Karla' belongs to the same coreference chain, 
	 * thus they are aggregated in a single node).<br/><br/>
	 * 
	 * Important: you need to enable the following modules to correctly extract ROS graphs: GraphParsing, CoreferenceResolution, CausalityParsing.<br/><br/>
	 * 
	 * <br/><br/>
	 * <b>IMPORTANT</b>: this method tries to extend the verb node and the subject or object nodes that does not belong to any coreference chains
	 * by incorporating useful information.<br/>
	 * For instance, the sentence: "Karla tried to identify her friend." will generate the triples:<br/>
	 * - "Karla" --> "tried to identify" --> "her friend"<br/>
	 * where the three verbal tokens 'tried', 'to', 'identify' are merged as well as the two nominal tokens 'her' and 'friend'.
	 * <br/><br/>
	 * 
	 * Each line of the document ROS CSV generated by this method has the following columns:<br/>
	 * - EDGE_ID: integer, to identify the edge - unambiguous among the edge IDs of the document<br/>
	 * - FROM_NODE_ID: unambiguous from node ID<br/>
	 * - FROM_NODE_NAME: from node name<br/>
	 * - FROM_NODE_HEAD_WORD: the head word of the node. When the NODE_NAME is a multiword expression, this field contains the head word of the
	 * multiword. When the NODE_NAME is a single word, the head word is equal to NODE_NAME. In general, for non-coreference / merged nodes 
	 * the head word consists of a single token / word. In case of coreference nodes, this field contains a comma separated list of all the head words 
	 * of the merged nodes.<br/>
	 * - FROM_NODE_RHET_CLASSES: the rhetorical class of the sentence that includes the node. If a nodes is a coreference one, 
	 * thus resulting from the aggregations of various co-referring nodes, this field is equal to the list of comma-separated
	 * rhetorical classes each one belonging to at least one of the co-referring nodes (i.e. "DRI_Approach, DRI_FuturWork")<br/>
	 * - FROM_NODE_SENT_ID: the ID of the sentence this nodes belongs to (empty in case of coreference nodes, since they result from the 
	 * merging of nodes belonging to one or more sentences)<br/>
	 * - FROM_NODE_SENT_TOKENS: the position(s) of the tokens of this node - list of integer starting from 0 to point out the first token -, 
	 * relative to the sentence in which the same node occurs (empty in case of coreference nodes, resulting from the 
	 * merging of nodes belonging to different sentences)<br/>
	 * - TO_NODE_ID: unambiguous to node ID<br/>
	 * - TO_NODE_NAME: to node name<br/>
	 * - TO_NODE_HEAD_WORD: the head word of the node. When the NODE_NAME is a multiword expression, this field contains the head word of the
	 * multiword. When the NODE_NAME is a single word, the head word is equal to NODE_NAME. In general, for non-coreference / merged nodes 
	 * the head word consists of a single token / word. In case of coreference nodes, this field contains a comma separated list of all the head words 
	 * of the merged nodes.<br/>
	 * - TO_NODE_RHET_CLASSES: the rhetorical class of the sentence that includes the node. If a nodes is a coreference one, 
	 * thus resulting from the aggregations of various co-referring nodes, this field is equal to the list of comma-separated
	 * rhetorical classes each one belonging to at least one of the co-referring nodes (i.e. "DRI_Approach, DRI_FuturWork")<br/>
	 * - TO_NODE_SENT_ID: the ID of the sentence this nodes belongs to (empty in case of coreference nodes, since they result from the 
	 * merging of nodes belonging to one or more sentences)<br/>
	 * - TO_NODE_SENT_TOKENS: the positions of the tokens of this node - list of integer starting from 0 to point out the first token -,
	 * relative to the sentence in which the same node occurs (empty in case of coreference nodes, resulting from the 
	 * merging of nodes belonging to different sentences)<br/>
	 * - EDGE_TYPE: could be SBJ, OBJ or CAUSE<br/>
	 * 
	 * <br/><br/><br/>
	 * 
	 * EXAMPLE DOCUMENT TEXT: <br/>
	 * The proposed method considers the relationships among rigid body parts and is more general since
	 * it can handle motions of close interactions with / without tangles .<br/>
	 * <br/>
	 * GENERATED DOCUMENT ROS CSV: <br/>
	 * "0","117","it___The proposed method","method, it","DRI_Approach","","","25","is general","is","DRI_Approach","1","11, 13","SBJ"<br/>
	 * "1","117","it___The proposed method","method, it","DRI_Approach","","","9","considers","considers","DRI_Approach","1","3","SBJ"<br/>
	 * "2","13","relationships","relationships","DRI_Approach","1","5","9","considers","considers","DRI_Approach","1","3","OBJ"<br/>
	 * "3","39","motions","motions","DRI_Approach","1","18","35","can handle","can","DRI_Approach","1","16, 17","OBJ"<br/>
	 * "4","117","it___The proposed method","method, it","DRI_Approach","","","35","can handle","can","DRI_Approach","1","16, 17","SBJ"<br/>
	 * "5","35","can handle","can","DRI_Approach","1","16, 17","9","considers","considers","DRI_Approach","1","3","CAUSE"<br/>

	 * <br/>
	 * 
	 * GENERATED SENTNECE CSV: <br/>
	 * "1","The proposed method considers the relationships among rigid body parts and is more general since it can handle motions of close interactions with / without tangles ."<br/>
	 * <br/>
	 * 
	 * COMMENTS ON THE GENERATED DOCUMENT ROS CSV: <br/>
	 * The node with ID 117 is the result of the coreference of the nodes 'it' and 'The proposed method' - the name of this
	 * node separates the names of the merged nodes by three underscores (___).<br/>
	 * Since the node with ID 117 is the result of a coreference / merging of two nodes, it has NODE_SENT_ID and NODE_SENT_TOKENS
	 * fields empty.<br/>
	 * All the nodes belongs to sentences with rhetorical category equal to "DRI_Approach" (indeed all the nodes belong 
	 * to the only sentence of the document classified as DRI_Approach).<br/>
	 * The node with name "can handle" (ID equal to "35") is made of two tokens, at positions 16 and 17 (NODE_SENT_TOKENS) 
	 * of the sentence with id equal to 1 (NODE_SENT_ID).<br/>
	 * If we consider the tokens of the EXAMPLE DOCUMENT TEXT, we can find at position 16 and 17 respectively the tokens 'can' and 'handle'
	 * that are the two tokens of the node with name "can handle" (ID equal to "35"). Remember that the first token of the sentence has 
	 * position equal to 0.<br/>
	 * The head words of the with ID 117, resulting from the merging of the coreferent nodes 'it' and 'The proposed method', 
	 * are 'method' and 'it'.<br/>
	 * The head word of the node with ID 25 ("is general") is the word 'is'<br/>
	 * To get the association of a SENT_ID (ID of the sentence) to the actual contents of the sentence (list of tokens separated by space), 
	 * we can use the method of this class {@link #getSentencesCSVstring(edu.upf.taln.dri.lib.model.Document, SentenceSelectorENUM) getSentencesCSVstring}.<br/>
	 * This method will return a CSV with the first two columns respectively equal to: the first column is the ID of the sentence as referred 
	 * by the field NODE_SENT_ID of the ROS CSV and the second column is related the space separated list of tokens of the sentence.<br/>
	 * In the example above the CSV returned by the method {@link #getSentencesCSVstring(edu.upf.taln.dri.lib.model.Document, SentenceSelectorENUM) getSentencesCSVstring} is 
	 * (only one sentence with id equal to 1):<br/>
	 * "1","The proposed method considers the relationships among rigid body parts and is more general since it can handle motions of close interactions with / without tangles .","DRI_Approach","","","false"<br/><br/>
	 * <br/><br/>
	 * **********************
	 * <br/><br/>
	 * A more complex example:<br/><br/>
	 * EXAMPLE DOCUMENT TEXT:<br/>
	 * Since kinematic constraints can usually be represented by single equations, they can be easily embedded into optimization problems for motion synthesis.<br/>
	 * However, extension to motions involving character shapes seems difficult since relationships between rigid bodies or surfaces need to be encoded.<br/>
	 * Further, these methods cannot handle close interactions without any tangles.<br/>
	 * The proposed method considers the relationships among rigid body parts and is more general since it can handle motions of close interactions with/without tangles.<br/>
	 * 
	 * <br/>
	 * GENERATED DOCUMENT ROS CSV: <br/>
	 * "0","349","they___kinematic constraints","constraints, they","DRI_Approach","","","15","can be represented","can","DRI_Approach","1","3, 5, 6","SBJ"<br/>
	 * "1","15","can be represented","can","DRI_Approach","1","3, 5, 6","32","can be embedded","can","DRI_Approach","1","12, 13, 15","CAUSE"<br/>
	 * "2","349","they___kinematic constraints","constraints, they","DRI_Approach","","","32","can be embedded","can","DRI_Approach","1","12, 13, 15","SBJ"<br/>
	 * "3","57","extension","extension","DRI_Challenge","3","2","69","However seems difficult","seems","DRI_Challenge","3","0, 8, 9","SBJ"<br/>
	 * "4","67","shapes","shapes","DRI_Challenge","3","7","63","involving","involving","DRI_Challenge","3","5","OBJ"<br/>
	 * "5","87","need to be encoded","need","DRI_Challenge","3","17, 18, 19, 20","69","However seems difficult","seems","DRI_Challenge","3","0, 8, 9","CAUSE"<br/>
	 * "6","75","relationships","relationships","DRI_Challenge","3","11","87","need to be encoded","need","DRI_Challenge","3","17, 18, 19, 20","SBJ"<br/>
	 * "7","101","methods","methods","DRI_Approach","5","3","168","Further can not handle","can","DRI_Approach","5","0, 4, 5, 6","SBJ"<br/>
	 * "8","351","close interactions","interactions","DRI_Approach","","","168","Further can not handle","can","DRI_Approach","5","0, 4, 5, 6","OBJ"<br/>
	 * "9","353","it___The proposed method","method, it","DRI_Approach","","","140","is general","is","DRI_Approach","7","11, 13","SBJ"<br/>
	 * "10","353","it___The proposed method","method, it","DRI_Approach","","","124","considers","considers","DRI_Approach","7","3","SBJ"<br/>
	 * "11","128","relationships","relationships","DRI_Approach","7","5","124","considers","considers","DRI_Approach","7","3","OBJ"<br/>
	 * "12","154","motions","motions","DRI_Approach","7","18","150","can handle","can","DRI_Approach","7","16, 17","OBJ"<br/>
	 * "13","353","it___The proposed method","method, it","DRI_Approach","","","150","can handle","can","DRI_Approach","7","16, 17","SBJ"<br/>
	 * "14","150","can handle","can","DRI_Approach","7","16, 17","124","considers","considers","DRI_Approach","7","3","CAUSE"<br/>
	 * <br/>
	 * GENERATED SENTNECE CSV: <br/>
	 * "1","Since kinematic constraints can usually be represented by single equations , they can be easily embedded into optimization problems for motion synthesis .","DRI_Approach","","","false"<br/>
	 * "3","However , extension to motions involving character shapes seems difficult since relationships between rigid bodies or surfaces need to be encoded .","DRI_Challenge","","","false"<br/>
	 * "5","Further , these methods can not handle close interactions without any tangles .","DRI_Approach","","","false"<br/>
	 * "7","The proposed method considers the relationships among rigid body parts and is more general since it can handle motions of close interactions with / without tangles .","DRI_Approach","","","false"<br/>
	 * 
	 * 
	 * @param doc
	 * @param sentenceSelector
	 * @return
	 */
	public static String getDocumentROSasCSVstring(edu.upf.taln.dri.lib.model.Document doc, SentenceSelectorENUM sentenceSelector) {

		StringWriter csvStringWriter = new StringWriter();
		String resultString = null;
		try {
			CSVWriter writer = new CSVWriter(csvStringWriter);
			DecimalFormat df = new DecimalFormat("#.####");

			// Check document type
			if(doc.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
				sentenceSelector = SentenceSelectorENUM.ALL;
			}

			List<Sentence> sentenceList = doc.extractSentences(sentenceSelector);

			if(!CollectionUtils.isEmpty(sentenceList)) {
				logger.info("Generating document ROS CSV from " + sentenceList.size() + " sentences "
						+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ") ...");

				// Start popularing CSV with graph contents
				try {
					DependencyGraph depGraph = doc.extractDocumentGraph(sentenceSelector);

					for(Entry<Integer, Pair<Integer, Integer>> edgeELem : depGraph.getAllEdges().entrySet()) {
						if(edgeELem.getKey() != null && edgeELem.getValue() != null && edgeELem.getValue().getRight() != null && edgeELem.getValue().getLeft() != null) {
							Integer edgeId = edgeELem.getKey();

							Integer fromNode = edgeELem.getValue().getLeft();
							String fromNodeWord = depGraph.getNodeName(fromNode).replace("\n", " ");

							Integer toNode = edgeELem.getValue().getRight();
							String toNodeWord = depGraph.getNodeName(toNode).replace("\n", " ");

							String edgeName = depGraph.getEdgeName(edgeId);

							List<String> recordList = new ArrayList<String>(); 
							recordList.add(edgeId.toString());

							// FROM NODE
							recordList.add(fromNode.toString());
							recordList.add(fromNodeWord);

							Set<String> fromNodeHeadWords = depGraph.getHeadWordsSet(fromNode);
							if(fromNodeHeadWords != null && fromNodeHeadWords.size() > 0) {
								String headWordString = "";
								for(String headWord : fromNodeHeadWords) {
									if(headWord != null && !headWord.equals("")) {
										if(headWordString.length() > 0) {
											headWordString += ", ";
										}
										headWordString += headWord.replace(",", " ");
									}
								}
								recordList.add(headWordString);
							}
							else {
								recordList.add("");
							}

							Set<String> fromNodeRhetClasses = depGraph.getRhetoricalClassSet(fromNode);
							if(fromNodeRhetClasses != null && fromNodeRhetClasses.size() > 0) {
								String rhetClassString = "";
								for(String rhetClass : fromNodeRhetClasses) {
									if(rhetClass != null && !rhetClass.equals("")) {
										if(rhetClassString.length() > 0) {
											rhetClassString += ", ";
										}
										rhetClassString += rhetClass;
									}
								}
								recordList.add(rhetClassString);
							}
							else {
								recordList.add("");
							}

							Pair<Integer, List<String>> formNodeSentIDtokenPair = depGraph.getSentenceIDTokensPair(fromNode);
							if(formNodeSentIDtokenPair != null && formNodeSentIDtokenPair.getLeft() != null) {
								recordList.add(formNodeSentIDtokenPair.getLeft().toString());
							}
							else {
								recordList.add("");
							}

							Map<Integer, String> fromMergedNodeNameMap = depGraph.getMergedNameMap(fromNode);
							if(fromMergedNodeNameMap != null && fromMergedNodeNameMap.size() > 0) {
								String nodeTokenPositions = "";
								for(Entry<Integer, String> nodeTokenPosition : fromMergedNodeNameMap.entrySet()) {
									if(nodeTokenPosition != null && nodeTokenPosition.getKey() != null) {
										if(nodeTokenPositions.length() > 0) {
											nodeTokenPositions += ", ";
										}
										nodeTokenPositions += nodeTokenPosition.getKey();
									}
								}
								recordList.add(nodeTokenPositions);
							}
							else {
								recordList.add("");
							}

							// TO NODE
							recordList.add(toNode.toString());
							recordList.add(toNodeWord);

							Set<String> toNodeHeadWords = depGraph.getHeadWordsSet(toNode);
							if(toNodeHeadWords != null && toNodeHeadWords.size() > 0) {
								String headWordString = "";
								for(String headWord : toNodeHeadWords) {
									if(headWord != null && !headWord.equals("")) {
										if(headWordString.length() > 0) {
											headWordString += ", ";
										}
										headWordString += headWord.replace(",", " ");
									}
								}
								recordList.add(headWordString);
							}
							else {
								recordList.add("");
							}

							Set<String> toNodeRhetClasses = depGraph.getRhetoricalClassSet(toNode);
							if(toNodeRhetClasses != null && toNodeRhetClasses.size() > 0) {
								String rhetClassString = "";
								for(String rhetClass : toNodeRhetClasses) {
									if(rhetClass != null && !rhetClass.equals("")) {
										if(rhetClassString.length() > 0) {
											rhetClassString += ", ";
										}
										rhetClassString += rhetClass;
									}
								}
								recordList.add(rhetClassString);
							}
							else {
								recordList.add("");
							}

							Pair<Integer, List<String>> toNodeSentIDtokenPair = depGraph.getSentenceIDTokensPair(toNode);
							if(toNodeSentIDtokenPair != null && toNodeSentIDtokenPair.getLeft() != null) {
								recordList.add(toNodeSentIDtokenPair.getLeft().toString());
							}
							else {
								recordList.add("");
							}

							Map<Integer, String> toMergedNodeNameMap = depGraph.getMergedNameMap(toNode);
							if(toMergedNodeNameMap != null && toMergedNodeNameMap.size() > 0) {
								String nodeTokenPositions = "";
								for(Entry<Integer, String> nodeTokenPosition : toMergedNodeNameMap.entrySet()) {
									if(nodeTokenPosition != null && nodeTokenPosition.getKey() != null) {
										if(nodeTokenPositions.length() > 0) {
											nodeTokenPositions += ", ";
										}
										nodeTokenPositions += nodeTokenPosition.getKey();
									}
								}
								recordList.add(nodeTokenPositions);
							}
							else {
								recordList.add("");
							}


							// EDGE NAME
							recordList.add(edgeName);

							String[] recordArray = new String[recordList.size()];
							recordArray = recordList.toArray(recordArray);

							writer.writeNext(recordArray);
						}
					}

					writer.close();
				}
				catch (Exception e) {
					Util.notifyException("Parsing coreference graph to generate graph / CSV", e, logger);
				}
			}

			resultString = csvStringWriter.toString();
			csvStringWriter.close();
			logger.info("Generated ROS CSV from " + sentenceList.size() + " sentences " 
					+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ").");
		}
		catch (Exception e) {
			Util.notifyException("Converting Document to graph / CSV (coref, causality)", e, logger);
		}

		return resultString;
	}

	/**
	 * This method will return a CSV with a number of rows equal to the number of sentences in the document.<br/>
	 * The first CSV column is the document-unambiguous ID of the sentence as referred by the field NODE_SENT_ID of the ROS CSV of the ROS CSV 
	 * generated by the method getDocumentROSasCSVstring(edu.upf.taln.dri.lib.model.Document doc, SentenceSelectorENUM sentenceSelector) of this class.<br/>
	 * The second column is the space separated list of tokens of the sentence.<br/>
	 * The third column is the Rhetorical Class of the sentence.<br/>
	 * The fourth column is the name of the section that contains the sentence (if any).<br/>
	 * The fifth column is the nesting level of the section that contains the sentence (if any). 1 is a first-level section, 2 is a section nested in a first level section and so on.<br/>
	 * The sixth column is "true" if the sentence contains one or more citations, otherwise "false".<br/><br/>
	 * If the processed document would be made of one sentence, the CSV returned would be (only one sentence with id equal to 1):<br/><br/>
	 * "1","The proposed method considers the relationships among rigid body parts and is more general since it can handle motions of close interactions with / without tangles .","DRI_Approach","","","false"<br/>
	 * @param doc
	 * @param sentenceSelector
	 * @return
	 */
	public static String getSentencesCSVstring(edu.upf.taln.dri.lib.model.Document doc, SentenceSelectorENUM sentenceSelector) {

		StringWriter csvStringWriter = new StringWriter();
		String resultString = null;

		try {
			CSVWriter writer = new CSVWriter(csvStringWriter);
			DecimalFormat df = new DecimalFormat("#.####");

			// Check document type
			if(doc.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
				sentenceSelector = SentenceSelectorENUM.ALL;
			}

			List<Sentence> sentenceList = doc.extractSentences(sentenceSelector);

			if(!CollectionUtils.isEmpty(sentenceList)) {
				logger.info("Generating document sentences CSV from " + sentenceList.size() + " sentences "
						+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ") ...");

				// Start popularing CSV with graph contents
				try {
					DependencyGraph depGraph = doc.extractDocumentGraph(sentenceSelector);

					Map<Integer, String> sentIDtextMap = new HashMap<Integer, String>();

					if(depGraph != null && depGraph.getAllEdges()!= null && depGraph.getAllEdges().size() > 0) {
						for(Entry<Integer, Pair<Integer, Integer>> edgeOfDocGraph : depGraph.getAllEdges().entrySet()) {
							if(edgeOfDocGraph != null && edgeOfDocGraph.getKey() != null && edgeOfDocGraph.getValue() != null) {
								Integer fromNode = edgeOfDocGraph.getValue().getRight();
								if(fromNode != null && depGraph.getNodeName(fromNode) != null && depGraph.getSentenceIDTokensPair(fromNode) != null &&
										depGraph.getSentenceIDTokensPair(fromNode).getLeft() != null && depGraph.getSentenceIDTokensPair(fromNode).getRight() != null) {

									String sentText = "";
									for(String sentToken : depGraph.getSentenceIDTokensPair(fromNode).getRight()) {
										if(sentText.length() > 0) {
											sentText += " ";
										}

										if(sentToken != null && !depGraph.getSentenceIDTokensPair(fromNode).getRight().equals("")) {
											sentText += sentToken;
										}
									}

									if(sentText != null && !sentText.equals("")) {
										sentIDtextMap.put(depGraph.getSentenceIDTokensPair(fromNode).getLeft(), sentText);
									}
								}

								Integer toNode = edgeOfDocGraph.getValue().getLeft();
								if(toNode != null && depGraph.getNodeName(toNode) != null && depGraph.getSentenceIDTokensPair(toNode) != null &&
										depGraph.getSentenceIDTokensPair(toNode).getLeft() != null && depGraph.getSentenceIDTokensPair(toNode).getRight() != null) {

									String sentText = "";
									for(String sentToken : depGraph.getSentenceIDTokensPair(toNode).getRight()) {
										if(sentText.length() > 0) {
											sentText += " ";
										}

										if(sentToken != null && !depGraph.getSentenceIDTokensPair(toNode).getRight().equals("")) {
											sentText += sentToken;
										}
									}

									if(sentText != null && !sentText.equals("")) {
										sentIDtextMap.put(depGraph.getSentenceIDTokensPair(toNode).getLeft(), sentText);
									}
								}
							}
						}
					}

					// Here got populated the map sentIDtextMap

					for(Entry<Integer, String> sentIdTextEntry : sentIDtextMap.entrySet()) {
						if(sentIdTextEntry != null && sentIdTextEntry.getKey() != null && sentIdTextEntry.getValue() != null && !sentIdTextEntry.getValue().trim().equals("")) {
							List<String> recordList = new ArrayList<String>(); 

							// Retrieve sentence annotation
							Sentence sentenceObject = ObjectGenerator.getSentenceFromId(sentIdTextEntry.getKey(), ((DocumentImpl) doc).cacheManager);

							// Adding CSV row
							recordList.add(sentIdTextEntry.getKey().toString());
							recordList.add(sentIdTextEntry.getValue());
							recordList.add(((sentenceObject != null && sentenceObject.getRhetoricalClass() != null) ? "" + sentenceObject.getRhetoricalClass().toString() : ""));
							recordList.add(((sentenceObject != null && sentenceObject.getContainingSection() != null && sentenceObject.getContainingSection().getName() != null) ? "" + sentenceObject.getContainingSection().getName() : ""));
							recordList.add(((sentenceObject != null && sentenceObject.getContainingSection() != null && sentenceObject.getContainingSection().getLevel() != null) ? "" + sentenceObject.getContainingSection().getLevel() : ""));
							recordList.add(((sentenceObject != null && sentenceObject.getCitationMarkers() != null && sentenceObject.getCitationMarkers().size() > 0) ? "true" : "false"));

							String[] recordArray = new String[recordList.size()];
							recordArray = recordList.toArray(recordArray);

							writer.writeNext(recordArray);
						}
					}

				}
				catch (Exception e) {
					Util.notifyException("Parsing coreference graph to generate sentence / CSV", e, logger);
				}
			}

			resultString = csvStringWriter.toString();
			csvStringWriter.close();
			logger.info("Generated Sentence CSV from " + sentenceList.size() + " sentences " 
					+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ").");
		}
		catch (Exception e) {
			Util.notifyException("Converting Document to sentence / CSV", e, logger);
		}

		return resultString;
	}

	/**
	 * This method will return a CSV with 
	 * @param doc
	 * @param sentenceSelector
	 * @return
	 */
	public static String getTokenExpressionCSVstring(edu.upf.taln.dri.lib.model.Document doc, SentenceSelectorENUM sentenceSelector) {

		StringWriter csvStringWriter = new StringWriter();
		String resultString = null;

		try {
			CSVWriter writer = new CSVWriter(csvStringWriter);
			DecimalFormat df = new DecimalFormat("#.####");

			// Check document type
			if(doc.getSourceDocumentType().equals(SourceENUM.PLAIN_TEXT)) {
				sentenceSelector = SentenceSelectorENUM.ALL;
			}

			List<Sentence> sentenceList = doc.extractSentences(sentenceSelector);

			if(!CollectionUtils.isEmpty(sentenceList)) {
				logger.info("Generating document token expressions CSV from " + sentenceList.size() + " sentences "
						+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ") ...");

				// Start popularing CSV with token expression contents
				try {
					for(Sentence sent : sentenceList) {
						if(sent != null) {
							String sentID = (sent.getId() != null) ? "" + sent.getId() : "NULL";
							try {
								DependencyGraph depGraph = ObjectGenerator.getDepGraphFromSentId(sent.getId(), ((DocumentImpl) doc).cacheManager, true);

								Map<Integer, Pair<Integer, Integer>>  graphEdges = depGraph.getAllEdges();

								Set<Integer> nodeIDs = depGraph.getNodesByNameRegExp(".*");
								for(Integer nodeID : nodeIDs) {
									if(nodeID != null && depGraph.getNodePOS(nodeID) != null) {
										try {
											// ***************** NOUN *****************
											if(depGraph.getNodePOS(nodeID).trim().toLowerCase().startsWith("n")) {

												// System.out.println("OLD NODE NAME: " + depGraph.getNodeName(nodeID));

												// Get all children
												Set<Integer> childNodes = depGraph.getChildrenNodes(nodeID);

												if(childNodes != null && childNodes.size() > 0) {
													for(Integer childNode : childNodes) {
														if(childNode != null) {
															for(Entry<Integer, Pair<Integer, Integer>> graphEdge : graphEdges.entrySet()) {
																if(graphEdge != null && graphEdge.getKey() != null && graphEdge.getValue() != null && 
																		graphEdge.getValue().getLeft().equals(childNode) && graphEdge.getValue().getRight().equals(nodeID)) {
																	Integer edgeID = graphEdge.getKey();
																	String edgeName = depGraph.getEdgeName(edgeID);

																	String childNodePOS = depGraph.getNodePOS(childNode);
																	Set<Integer> childNodeChildList = depGraph.getChildrenNodes(childNode);

																	if(edgeName.equals("NMOD") && childNodeChildList != null && childNodeChildList.size() == 0 && 
																			(childNodePOS.startsWith("D") || childNodePOS.startsWith("J") || childNodePOS.startsWith("N"))) {
																		depGraph.mergeNodes(nodeID, childNode, null);
																	}

																	if(edgeName.equals("NMOD") && childNodeChildList != null && childNodeChildList.size() > 0 && childNodePOS.startsWith("I")) {
																		mergeWitSubtree(childNode, depGraph);
																		depGraph.mergeNodes(nodeID, childNode, null);
																	}
																}
															}
														}
													}
												}

												// System.out.println("NEW NODE NAME: " + depGraph.getNodeName(nodeID).replace("_", " "));

												List<String> recordList = new ArrayList<String>(); 
												recordList.add("NOUN");
												recordList.add(nodeID + "");
												recordList.add(depGraph.getNodeName(nodeID).replace("_", " "));

												String[] recordArray = new String[recordList.size()];
												recordArray = recordList.toArray(recordArray);

												writer.writeNext(recordArray);

											}
											// ***************** VERB *****************
											else if(depGraph.getNodePOS(nodeID).trim().toLowerCase().startsWith("v")) {
												// System.out.println("VERB NODE NAME: " + depGraph.getNodeName(nodeID).replace("_", " "));

												List<String> recordList = new ArrayList<String>(); 
												recordList.add("VERB");
												recordList.add(nodeID + "");
												recordList.add(depGraph.getNodeName(nodeID).replace("_", " "));

												String[] recordArray = new String[recordList.size()];
												recordArray = recordList.toArray(recordArray);

												writer.writeNext(recordArray);
											}

										}
										catch (Exception e) {
											Util.notifyException("Parsing coreference graph to generate token espressions / CSV (node with id " + nodeID + " of sentence with id " + sentID + ")", e, logger);
										}

									}
								}
							}
							catch (Exception e) {
								Util.notifyException("Parsing coreference graph to generate token espressions / CSV (sentence with id " + sentID + ")", e, logger);
							}
						}
					}

				}
				catch (Exception e) {
					Util.notifyException("Parsing coreference graph to generate token espressions / CSV", e, logger);
				}
			}

			resultString = csvStringWriter.toString();
			csvStringWriter.close();
			logger.info("Generated Sentence CSV from " + sentenceList.size() + " sentences " 
					+ "(sentence selector: " + ((sentenceSelector == null) ? "NULL" : sentenceSelector.toString()) + ").");
		}
		catch (Exception e) {
			Util.notifyException("Converting Document to sentence / CSV", e, logger);
		}

		return resultString;
	}

	private static void mergeWitSubtree(Integer nodeID, DependencyGraph depGraph) {

		if(depGraph != null && nodeID != null && depGraph.getNodeName(nodeID) != null) {

			// Get all children
			Set<Integer> childNodes = depGraph.getChildrenNodes(nodeID);

			for(Integer childNode : childNodes) {
				if(childNode != null) {
					if(depGraph.getChildrenNodes(childNode) != null && depGraph.getChildrenNodes(childNode).size() > 0) {
						mergeWitSubtree(childNode, depGraph);
					}
					depGraph.mergeNodes(nodeID, childNode, null);
				}
			}
		}

	}




	public static Document sanitize(Document doc) {
		if(doc != null) {
			Optional<String> sourceType = GateUtil.getStringFeature(doc, "source");

			// 1) PDFX document sanitizing (replicated in ImporterPDFX)
			if(Util.strCompare(sourceType.orElse(null), SourceENUM.PDFX.toString())) {
				// Delete all sentences and header annotations that are after the first bibliographic entry or the section headers that have more than 14 tokens
				Annotation firstBibEntryAnn = GateUtil.getFirstAnnotationInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.bibEntryAnnType).orElse(null);
				if(firstBibEntryAnn != null) {
					Long firstBibEntryStartOffset = firstBibEntryAnn.getStartNode().getOffset();
					if(firstBibEntryStartOffset != null) {
						Set<Integer> annIdToRemove = new HashSet<Integer>();

						List<String> annTypesToRemove = new ArrayList<String>();
						annTypesToRemove.add(ImporterBase.sentenceAnnType);
						annTypesToRemove.add(ImporterBase.h1AnnType);
						annTypesToRemove.add(ImporterBase.h2AnnType);
						annTypesToRemove.add(ImporterBase.h3AnnType);
						annTypesToRemove.add(ImporterBase.h4AnnType);
						annTypesToRemove.add(ImporterBase.h5AnnType);

						for(String annTypeToRem : annTypesToRemove) {
							List<Annotation> sentenceAnnList = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, annTypeToRem);
							for(Annotation sent : sentenceAnnList) {
								if(sent != null && sent.getStartNode().getOffset() >= firstBibEntryStartOffset) {
									annIdToRemove.add(sent.getId());
								}

								// Remove incorrect headers
								if(sent.getType().equals(ImporterBase.h1AnnType) || sent.getType().equals(ImporterBase.h2AnnType) || 
										sent.getType().equals(ImporterBase.h3AnnType) || sent.getType().equals(ImporterBase.h4AnnType) || sent.getType().equals(ImporterBase.h5AnnType)) {

									List<Annotation> intersectingTokenAnnList = GateUtil.getAnnotationInDocumentOrderContainedAnnotation(doc, ImporterBase.driAnnSet, ImporterBase.tokenAnnType, sent);
									if(intersectingTokenAnnList.size() > 14) {
										annIdToRemove.add(sent.getId());
									}

									String sectionHeaderText = GateUtil.getAnnotationText(sent, doc).orElse(null);
									if(sectionHeaderText != null && (sectionHeaderText.trim().startsWith(",") || sectionHeaderText.trim().startsWith(".") ||
											sectionHeaderText.trim().startsWith(":") || sectionHeaderText.trim().startsWith(";"))) {
										annIdToRemove.add(sent.getId());
									}
								}
							}
						}

						if(annIdToRemove.size() > 0) {
							for(Integer annIdToRem : annIdToRemove) {
								Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRem);
								if(annToRem != null) {
									doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
								}
							}
						}

					}
				}

				// Delete all the section headers of type h1 before the first h1 section that contains the word Introduction and is in the first 30% of the document
				List<Annotation> rootSectionList = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, ImporterBase.h1AnnType);
				if(rootSectionList != null && rootSectionList.size() > 0) {
					long deleteAllSectBefore = Long.MIN_VALUE;

					Annotation introRootSectionAnn = null;
					for(Annotation rootSect : rootSectionList) {
						String rootSectText = GateUtil.getAnnotationText(rootSect, doc).orElse(null);
						if(rootSect != null && rootSectText != null && rootSectText.toLowerCase().trim().contains("introduction")) {
							introRootSectionAnn = rootSect;
							break;
						}
					}

					// Check if the first h1 section that contains the word Introduction is in the first 30% of the document
					if(introRootSectionAnn != null) {
						long introRootSectionAnnStartOffset = introRootSectionAnn.getStartNode().getOffset();
						long docLength = gate.Utils.lengthLong(doc);

						double positionOfIntroRooSectionAnn = 1d;
						if(docLength > 0 && docLength > introRootSectionAnnStartOffset) {
							positionOfIntroRooSectionAnn = new Double(introRootSectionAnnStartOffset) / new Double(docLength);
						}
						if(positionOfIntroRooSectionAnn <= 0.3d) {
							deleteAllSectBefore = introRootSectionAnnStartOffset;
						}
					}

					if(deleteAllSectBefore > 0d) {
						List<Annotation> sectAnnToDel = new ArrayList<Annotation>();
						for(Annotation rootSect : rootSectionList) {
							if(rootSect != null && rootSect.getStartNode().getOffset() < deleteAllSectBefore) {
								sectAnnToDel.add(rootSect);
							}
						}

						for(Annotation sectAnnToD : sectAnnToDel) {
							if(sectAnnToD != null) {
								doc.getAnnotations(ImporterBase.driAnnSet).remove(sectAnnToD);
							}
						}
					}
				}

			}

			// 2) The TermAnnotator doesn't use term annotations for all terms, change it
			List<Annotation> termAnnList = gate.Utils.inDocumentOrder(doc.getAnnotations(ImporterBase.term_AnnSet));
			if(termAnnList != null) {
				Set<Integer> annIdToRemove = new HashSet<Integer>();

				for(Annotation termAnn : termAnnList) {
					if(termAnn != null && !termAnn.getType().equals(ImporterBase.term_CandOcc)) {
						try {
							doc.getAnnotations(ImporterBase.term_AnnSet).add(termAnn.getStartNode().getOffset(), termAnn.getEndNode().getOffset(),
									ImporterBase.term_CandOcc, termAnn.getFeatures());
						} catch (InvalidOffsetException e) {
							/* Do nothing */
						}
						annIdToRemove.add(termAnn.getId());
					}
				}

				if(annIdToRemove.size() > 0) {
					for(Integer annIdToRem : annIdToRemove) {
						Annotation annToRem = doc.getAnnotations(ImporterBase.term_AnnSet).get(annIdToRem);
						if(annToRem != null) {
							doc.getAnnotations(ImporterBase.term_AnnSet).remove(annToRem);
						}
					}
				}
			}

			// 3) Delete all the coreference chain and coreference annotations in the 
			boolean corefInfoExtracted = false;
			if(doc.getFeatures() != null && doc.getFeatures().containsKey(Factory.coreferenceAnalysisFlagKey) && doc.getFeatures().get(Factory.coreferenceAnalysisFlagKey) != null &&
					((String) doc.getFeatures().get(Factory.coreferenceAnalysisFlagKey)).equals("true")) {
				corefInfoExtracted = true;
			}

			if(!corefInfoExtracted) {
				// Delete all CorefChains annotaitons
				doc.removeAnnotationSet(ImporterBase.coref_ChainAnnSet);

				// Delete all the annotations in the annotation set Analysis that starts with COREF_ or are SPURIOUSCOREFm or CorefMention
				Set<Integer> annIdToRemove = new HashSet<Integer>();

				List<String> annTypesToRemove = new ArrayList<String>();
				Set<String> analysisAnnotationTypes = doc.getAnnotations(ImporterBase.driAnnSet).getAllTypes();
				for(String analysisAnnotationType : analysisAnnotationTypes) {
					if(analysisAnnotationType != null && 
							(analysisAnnotationType.startsWith("COREF_") || analysisAnnotationType.equals("SPURIOUSCOREFm") || analysisAnnotationType.equals("CorefMention"))) {
						annTypesToRemove.add(analysisAnnotationType);
					}
				}

				for(String annTypeToRem : annTypesToRemove) {
					List<Annotation> toRemoveAnnList = GateUtil.getAnnInDocOrder(doc, ImporterBase.driAnnSet, annTypeToRem);
					for(Annotation toRemoveAnn : toRemoveAnnList) {
						if(toRemoveAnn != null) {
							annIdToRemove.add(toRemoveAnn.getId());
						}
					}
				}

				if(annIdToRemove.size() > 0) {
					for(Integer annIdToRem : annIdToRemove) {
						Annotation annToRem = doc.getAnnotations(ImporterBase.driAnnSet).get(annIdToRem);
						if(annToRem != null) {
							doc.getAnnotations(ImporterBase.driAnnSet).remove(annToRem);
						}
					}
				}

			}

			// 4) If FL_citationSpotAndLinkExtracted_1 is true, set FL_citationSpotExtracted_1 equal to true but the FL_citationLinkExtracted_1 equal to false
			if(doc.getFeatures() != null && doc.getFeatures().containsKey("FL_citationSpotAndLinkExtracted_1")) {
				for(Entry<Object, Object> featEntry : doc.getFeatures().entrySet()) {
					if(featEntry != null && featEntry.getKey() != null && featEntry.getKey() instanceof String && 
							featEntry.getValue() != null && featEntry.getValue() instanceof String) {
						String featureName = (String) featEntry.getKey();
						String featureValue = (String) featEntry.getValue();
						if(featureName.equals("FL_citationSpotAndLinkExtracted_1") && featureValue.toLowerCase().equals("true")) {
							doc.getFeatures().put("FL_citationSpotExtracted_1", "true");
							doc.getFeatures().put("FL_citationLinkExtracted_1", "false");
							doc.getFeatures().remove("FL_citationSpotAndLinkExtracted_1");
						}
					}
				}
			}

		}

		return doc;
	}

}
