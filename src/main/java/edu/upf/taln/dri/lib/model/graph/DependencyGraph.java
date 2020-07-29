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
package edu.upf.taln.dri.lib.model.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import edu.upf.taln.dri.common.util.Util;
import edu.upf.taln.dri.lib.model.graph.generic.GenericDirectedGraph;
import edu.upf.taln.dri.lib.model.graph.generic.GenericDirectedGraphGRPHimpl;
import edu.upf.taln.dri.lib.model.graph.generic.GraphToStringENUM;

/**
 * Dependency Graph data structure and methods
 * 
 *
 */
public class DependencyGraph {

	private static final String nodePOSfeat = "POS"; // String
	private static final String nodeLemmafeat = "Lemma"; // String
	private static final String nodeCorefIDfeat = "CorefID"; // Set<Integer>
	private static final String nodeCorefNamefeat = "CorefName"; // String
	private static final String nodeSentenceOrderFeat = "SentOrder"; // Integer
	private static final String edgeSRLtagfeat = "SRLtag"; // String
	private static final String edgeSRLsensefeat = "SRLsense"; // String
	private static final String edgeSRLsentId = "SRLsentId"; // Integer
	
	private static final String causal_IDcauseNodesID = "causal_IDcauseNodesID"; // Map<Integer, Integer>
	private static final String causal_IDeffectNodesID = "causal_IDeffectNodesID"; // Map<Integer, Integer>
	private static final String causal_IDrole = "causal_IDrole"; // Map<Integer, String>
	
	private static final String node_mergedNameMap = "merged_nameMap";
	private static final String node_mergedIDmap = "merged_nodeIDmap";
	
	private static final String indipProp_rhetClass = "indipProp_rhetClass"; // Set<String>
	private static final String indipProp_headWord = "indipProp_headWord"; // Set<String>
	private static final String indipProp_sentenceIDTokensPair = "indipProp_sentenceTokens"; // Pair<Integer, List<String>>
	
	
	private GenericDirectedGraph graph;

	private Set<Integer> SRLedges;

	// Constructor
	public DependencyGraph() {
		this.graph = new GenericDirectedGraphGRPHimpl();
		this.SRLedges = new HashSet<Integer>();
	}

	// Methods
	/**
	 * Add a new node
	 * 
	 * @param nodeId
	 * @param word
	 * @param pos
	 * @param lemma
	 * @return
	 */
	public Integer addNode(Integer nodeId, String word, String pos, String lemma, Set<Integer> corefIDs, String corefName, Integer sentOrder) {
		Integer newNodeId = graph.addNode(nodeId, word);
		if(newNodeId != null) {
			Map<Integer, String> nodeNameMap = new TreeMap<Integer, String>();
			Map<Integer, String> nodeIDmap = new TreeMap<Integer, String>();
			Map<Integer, String> rhetClassMap = new TreeMap<Integer, String>();
			if(sentOrder != null && sentOrder >= 0) {
				graph.addNodeFeature(newNodeId, nodeSentenceOrderFeat, sentOrder.intValue());
				nodeNameMap.put(sentOrder.intValue(), word);
				nodeIDmap.put(newNodeId, word);
			}
			if(StringUtils.isNotBlank(pos)) graph.addNodeFeature(newNodeId, nodePOSfeat, pos);
			if(StringUtils.isNotBlank(lemma)) graph.addNodeFeature(newNodeId, nodeLemmafeat, lemma);
			if(corefIDs != null) {
				// The IDs of the coreference chains in which the node is the head of one of their elements
				// Usually it should be a one element List since each node can be head of one element of a specific coreference chain
				graph.addNodeFeature(newNodeId, nodeCorefIDfeat, corefIDs);
			}
			if(corefName != null) graph.addNodeFeature(newNodeId, nodeCorefNamefeat, corefName);
			graph.addNodeFeature(newNodeId, node_mergedNameMap, nodeNameMap);
			graph.addNodeFeature(newNodeId, node_mergedIDmap, nodeIDmap);
		}
		return newNodeId;
	}

	/**
	 * Get node name
	 * 
	 * @param nodeId
	 * @return
	 */
	public String getNodeName(Integer nodeId) {
		return graph.getNodeName(nodeId);
	}

	/**
	 * Change the name of the node - new name not null or empty
	 * 
	 * @param nodeId
	 * @param newNodeName
	 * @return
	 */
	public boolean changeNodeName(Integer nodeId, String newNodeName) {
		return graph.changeNodeName(nodeId, newNodeName);
	}

	/**
	 *  Set an SRL edge sense and tag and source sentence ID
	 *  
	 * @param edgeId
	 * @param tag
	 * @param sense
	 * @param sentenceId
	 * @return
	 */
	public Boolean setEdgeSRLsenseAndTag(Integer edgeId, String tag, String sense, Integer sentenceId) {
		boolean result = false;
		if(graph.getEdgeName(edgeId) != null && SRLedges.contains(edgeId)) {
			if(StringUtils.isNotBlank(tag)) graph.addEdgeFeature(edgeId, edgeSRLtagfeat, tag);
			if(StringUtils.isNotBlank(sense)) graph.addEdgeFeature(edgeId, edgeSRLsensefeat, sense);
			if(sentenceId != null) graph.addEdgeFeature(edgeId, edgeSRLsentId, sentenceId);
		}
		return result;
	}
	
	/**
	 *  Set a node as participating to a cross-sentence causal relation (the cause annotated in a sentence and the effect in another one)
	 *  A node annotated with these properties can be the cause or the effect of the causal relation.
	 *  
	 * @param nodeId the ID of the node that participates in the causal relation
	 * @param causalRelId the ID of the causal relation
	 * @param roleName the name of the role of the node in the causal relation
	 * @param causeNode the id of the node that identifies the cause of the causal relation
	 * @param effectNode the id of the node that identifies the effect in the causal relation
	 * @return
	 */
	public Boolean setNodeInCrossSentCausalRel(Integer nodeId, Integer causalRelId, String roleName, Integer causeNode, Integer effectNode) {
		boolean result = false;
		if(nodeId != null && graph.getNodeName(nodeId) != null && causalRelId != null && roleName != null && !roleName.equals("") &&
			causeNode != null && effectNode != null) {
			
			if(!graph.getNodeFeatures(nodeId).containsKey(DependencyGraph.causal_IDrole)) {
				graph.addNodeFeature(nodeId, DependencyGraph.causal_IDrole, new HashMap<Integer, String>());
			}
			((Map<Integer, String>) graph.getNodeFeatures(nodeId).get(DependencyGraph.causal_IDrole)).put(causalRelId, roleName);
			
			if(!graph.getNodeFeatures(nodeId).containsKey(DependencyGraph.causal_IDcauseNodesID)) {
				graph.addNodeFeature(nodeId, DependencyGraph.causal_IDcauseNodesID, new HashMap<Integer, Integer>());
			}
			((Map<Integer, Integer>) graph.getNodeFeatures(nodeId).get(DependencyGraph.causal_IDcauseNodesID)).put(causalRelId, causeNode);
			
			if(!graph.getNodeFeatures(nodeId).containsKey(DependencyGraph.causal_IDeffectNodesID)) {
				graph.addNodeFeature(nodeId, DependencyGraph.causal_IDeffectNodesID, new HashMap<Integer, Integer>());
			}
			((Map<Integer, Integer>) graph.getNodeFeatures(nodeId).get(DependencyGraph.causal_IDeffectNodesID)).put(causalRelId, effectNode);
			
		}
		return result;
	}
	
	/**
	 * Add a rhetorical class to the set of rhetorical classes associated to the node
	 * 
	 * @param nodeId
	 * @param rhetoricalClass if null or empty, no rhetorical class is added (to empty the rhetorical class set, set to null and deletePreexisting to true)
	 * @param deletePreexisting if true, all pre-existing rhetorical classes are deleted
	 * @return
	 */
	public Boolean addRhetoricalClass(Integer nodeId, String rhetoricalClass, boolean deletePreexisting) {
		boolean result = false;
		if(nodeId != null && graph.getNodeName(nodeId) != null) {
			if(deletePreexisting || !graph.getNodeFeatures(nodeId).containsKey(DependencyGraph.indipProp_rhetClass)) {
				graph.addNodeFeature(nodeId, DependencyGraph.indipProp_rhetClass, new HashSet<String>());
			}
			
			if(rhetoricalClass != null && !rhetoricalClass.equals("")) {
				((Set<String>) graph.getNodeFeatures(nodeId).get(DependencyGraph.indipProp_rhetClass)).add(rhetoricalClass);
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Get the set of rhetorical classes associated to the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<String> getRhetoricalClassSet(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(indipProp_rhetClass)) {
			Set<String> rhetoricalClassSet_node = (Set<String>) featureMap_node.get(indipProp_rhetClass);
			return Collections.unmodifiableSet(rhetoricalClassSet_node);
		}
		else {
			return Collections.unmodifiableSet(new HashSet<String>());
		}
	}
	
	/**
	 * Add a head word to the set of head words associated to the node.
	 * Usually, each node not derived from the merging of coreferents has only a head word.
	 * 
	 * @param nodeId
	 * @param headWord if null or empty, no head word is added (to empty the head words set, set to null and deletePreexisting to true)
	 * @param deletePreexisting if true, all pre-existing head words are deleted
	 * @return
	 */
	public Boolean addHeadWord(Integer nodeId, String headWord, boolean deletePreexisting) {
		boolean result = false;
		if(nodeId != null && graph.getNodeName(nodeId) != null) {
			if(deletePreexisting || !graph.getNodeFeatures(nodeId).containsKey(DependencyGraph.indipProp_headWord)) {
				graph.addNodeFeature(nodeId, DependencyGraph.indipProp_headWord, new HashSet<String>());
			}
			
			if(headWord != null && !headWord.equals("")) {
				((Set<String>) graph.getNodeFeatures(nodeId).get(DependencyGraph.indipProp_headWord)).add(headWord);
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Get the set of head words associated to the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<String> getHeadWordsSet(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(indipProp_headWord)) {
			Set<String> headWordSet_node = (Set<String>) featureMap_node.get(indipProp_headWord);
			return Collections.unmodifiableSet(headWordSet_node);
		}
		else {
			return Collections.unmodifiableSet(new HashSet<String>());
		}
	}
	
	/**
	 * Set the pair of sentence ID and list of Tokens for a specific node (list of sentence words separated by space)
	 * 
	 * @param nodeId
	 * @param sentenceID
	 * @param sentenceTokenList
	 * @return
	 */
	public Boolean setSentenceIDTokensPair(Integer nodeId, Integer sentenceID, List<String> sentenceTokenList) {
		boolean result = false;
		if(nodeId != null && graph.getNodeName(nodeId) != null && sentenceID != null && sentenceTokenList != null && sentenceTokenList.size() > 0) {
			Pair<Integer, List<String>> sentIDTokensPair = Pair.of(sentenceID, sentenceTokenList);
			graph.addNodeFeature(nodeId, DependencyGraph.indipProp_sentenceIDTokensPair, sentIDTokensPair);
			result = true;
		}
		
		return result;
	}
	
	/**
	 * Get the pair of sentence ID and list of Tokens of a specific node (list of sentence words separated by space)
	 * 
	 * @param nodeId
	 * @return
	 */
	public Pair<Integer, List<String>> getSentenceIDTokensPair(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(indipProp_sentenceIDTokensPair)) {
			return (Pair<Integer, List<String>>) featureMap_node.get(indipProp_sentenceIDTokensPair);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Add edge
	 * 
	 * @param name
	 * @param from
	 * @param to
	 * @param isSRL
	 * @return
	 */
	public Integer addEdge(String name, Integer from, Integer to, boolean isSRL) {
		if(StringUtils.isNotBlank(name) && from != null && to != null && !from.equals(to)) {
			Integer edgeId = null;
			
			// Check if the edge already exists
			for(Entry<Integer, Pair<Integer, Integer>> edge : getAllEdges().entrySet()) {
				if(edge != null && edge.getKey() != null && edge.getValue() != null &&
					edge.getValue().getLeft().equals(from) && edge.getValue().getRight().equals(to) &&
					getEdgeName(edge.getKey()) != null && getEdgeName(edge.getKey()).equals(name) &&
					( (isSRL && SRLedges.contains(edge.getKey())) || (!isSRL && !SRLedges.contains(edge.getKey())) )
				   ) {
					edgeId = edge.getKey();
				}
			}
			
			if(edgeId == null) { // The edge does not exist
				edgeId = graph.addEdge(from, to, name);
				if(isSRL && edgeId != null) SRLedges.add(edgeId);
			}
			
			if(edgeId != null) {
				// Check for duplicated edges and merge them
				Map<Integer, Pair<Integer, Integer>> parallelEdges = getOutgoingEdges(from, null);
				Set<Integer> equalEdgeIds = new HashSet<Integer>();
				for(Entry<Integer, Pair<Integer, Integer>> parallelEdge : parallelEdges.entrySet()) {
					if(parallelEdge.getKey() != edgeId && parallelEdge.getValue() != null && 
						parallelEdge.getValue().getLeft() != null && parallelEdge.getValue().getLeft().equals(getEdgeFromNode(edgeId)) &&
						parallelEdge.getValue().getRight() != null && parallelEdge.getValue().getRight().equals(getEdgeToNode(edgeId)) &&
						!SRLedges.contains(parallelEdge.getKey()) ) {
						equalEdgeIds.add(parallelEdge.getKey());
					}
				}

				if(equalEdgeIds.size() > 0) {
					for(Integer edgeToMergeId : equalEdgeIds) {
						edgeId = mergeParallEdges(edgeId, edgeToMergeId);
					}
				}
			}
			
			return edgeId;
		}
		return null;
	}

	/**
	 * The edges will be merged if they have the same start and end nodes, the same type (SRL or not SRL), but different edge names
	 * A a new edge with name: nameOfEdge1 + "___" + nameOfEdge2 and merged features is created.
	 * 
	 * @param edgeId1
	 * @param edgeId2
	 * @return
	 */
	private Integer mergeParallEdges(Integer edgeId1, Integer edgeId2) {

		if(getEdgeName(edgeId1) != null && getEdgeName(edgeId2) != null && !edgeId1.equals(edgeId2) &&
				!getEdgeName(edgeId1).equals(getEdgeName(edgeId2)) &&
				getEdgeFromNode(edgeId1).equals(getEdgeFromNode(edgeId2)) && getEdgeToNode(edgeId1).equals(getEdgeToNode(edgeId2)) &&
				( (SRLedges.contains(edgeId1) && SRLedges.contains(edgeId2)) || (!SRLedges.contains(edgeId1) && !SRLedges.contains(edgeId2)) )) {
			String newEdgeName = getEdgeName(edgeId1) + "___" + getEdgeName(edgeId2);

			Map<String, Object> newEdgeFeatures = new HashMap<String, Object>();
			for(Entry<String, Object> edge1Feats : graph.getEdgeFeatures(edgeId1).entrySet()) {
				newEdgeFeatures.put(edge1Feats.getKey(), edge1Feats.getValue());
			}
			for(Entry<String, Object> edge2Feats : graph.getEdgeFeatures(edgeId2).entrySet()) {
				newEdgeFeatures.put(edge2Feats.getKey(), edge2Feats.getValue());
			}

			Integer newEdgeId = graph.addEdge(getEdgeFromNode(edgeId1), getEdgeToNode(edgeId2), newEdgeName);
			
			if(newEdgeId != null) {
				if(SRLedges.contains(edgeId1) && newEdgeId != null) SRLedges.add(newEdgeId);
				
				for(Entry<String, Object> edgeFeature : newEdgeFeatures.entrySet()) {
					graph.addEdgeFeature(newEdgeId, edgeFeature.getKey(), edgeFeature.getValue());
				}

				SRLedges.remove(edgeId1); graph.removeEdge(edgeId1);
				SRLedges.remove(edgeId2); graph.removeEdge(edgeId2);

				return newEdgeId;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Get edge name
	 * 
	 * @param edgeId
	 * @return
	 */
	public String getEdgeName(Integer edgeId) {
		return graph.getEdgeName(edgeId);
	}

	/**
	 * Change the name of the edge - new name not null or empty
	 * 
	 * @param edgeId
	 * @param newEdgeName
	 * @return
	 */
	public boolean changeEdgeName(Integer edgeId, String newEdgeName) {
		return graph.changeEdgeName(edgeId, newEdgeName);
	}

	/**
	 * Get all edges of the dependency graph ordered by depth first visit
	 * 
	 * @param edgeId ordered list of edge ids
	 * @return
	 */
	public List<Integer> getAllOrderedDepthFirstEdges() {
		Set<Integer> rootNodeIds = this.getRootNodeIds();
		
		List<Integer> edgeIdList = new ArrayList<Integer>();
		for(Integer rootNodeId : rootNodeIds) {
			if(rootNodeId != null) {
				addAllOrderedDepthFirstEdges(rootNodeId, edgeIdList);
			}
		}
		return edgeIdList;
	}
	
	
	private void addAllOrderedDepthFirstEdges(Integer nodeId, List<Integer> edgesIdList) {
		
		if(nodeId != null && graph.getNodeName(nodeId) != null) {
			Map<Integer, Pair<Integer, Integer>> nodeIncidentEdges = graph.getIncidentEdges(nodeId);
			
			//Order incident edges: first SBJ type, then the others ordered by source node document occurrence - START
			List<Integer> orderedEdgesIDs = new ArrayList<Integer>();
			
			// Add in the map all the edges ID ordered by the id of the source token, 
			// assuming that the id of the source tokens are document ordered
			Map<Integer, Integer> sourceTokenIdEdgeIdMap = new TreeMap<Integer, Integer>();
			for(Entry<Integer, Pair<Integer, Integer>> nodeIncidentEdge : nodeIncidentEdges.entrySet()) {
				if(nodeIncidentEdge != null && nodeIncidentEdge.getKey() != null) {
					String edgeName = graph.getEdgeName(nodeIncidentEdge.getKey());
					if(edgeName.equals("SBJ")) {
						if(!SRLedges.contains(nodeIncidentEdge.getKey())) {
							orderedEdgesIDs.add(nodeIncidentEdge.getKey());
						}
					}
					else {
						if(!SRLedges.contains(nodeIncidentEdge.getKey())) {
							sourceTokenIdEdgeIdMap.put(nodeIncidentEdge.getValue().getLeft(), nodeIncidentEdge.getKey());
						}
					}
				}
			}
			
			for(Entry<Integer, Integer> sourceTokenIdEdgeIdMapElem : sourceTokenIdEdgeIdMap.entrySet()) {
				if(sourceTokenIdEdgeIdMapElem != null && sourceTokenIdEdgeIdMapElem.getValue() != null) {
					orderedEdgesIDs.add(sourceTokenIdEdgeIdMapElem.getValue());
				}
			}
			//Order incident edges: first SBJ type, then the others ordered by source node document occurrence - END
			
			for(Integer orderedEdgesID : orderedEdgesIDs) {
				if(orderedEdgesID != null) {
					addAllOrderedDepthFirstEdges(this.getEdgeFromNode(orderedEdgesID), edgesIdList);
					edgesIdList.add(orderedEdgesID);
				}
			}
		}
	}
	
	/**
	 * Get all edges of the dependency graph
	 * 
	 * @param edgeId
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getAllEdges() {
		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		Map<Integer, Pair<Integer, Integer>> nonSRLedges = new HashMap<Integer, Pair<Integer, Integer>>();
		
		// Order edge by tree occurrence
		Map<Integer, Pair<Integer, Integer>> orderedEdges = new TreeMap<Integer, Pair<Integer, Integer>>();
		for(Entry<Integer, Pair<Integer, Integer>> edgeEntry : allEdges.entrySet()) {
			if(!SRLedges.contains(edgeEntry.getKey())) {
				nonSRLedges.put(edgeEntry.getKey(), edgeEntry.getValue());
			}
		}
		return nonSRLedges;
	}
	
	/**
	 * Get the start node of the edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public Integer getEdgeFromNode(Integer edgeId) {
		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		return (edgeId != null && allEdges != null && allEdges.get(edgeId) != null) ? allEdges.get(edgeId).getLeft() : null;
	}

	/**
	 * Get the end node of the edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public Integer getEdgeToNode(Integer edgeId) {
		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		return (edgeId != null && allEdges != null && allEdges.get(edgeId) != null) ? allEdges.get(edgeId).getRight() : null;
	}

	/**
	 * Get the SRL tag of the edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public String getEdgeSRLtag(Integer edgeId) {
		return (graph.getEdgeFeatures(edgeId).containsKey(edgeSRLtagfeat)) ? (String) graph.getEdgeFeatures(edgeId).get(edgeSRLtagfeat) : null;
	}

	/**
	 * Get the SRL sense of the edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public String getEdgeSRLsensefeat(Integer edgeId) {
		return (graph.getEdgeFeatures(edgeId).containsKey(edgeSRLsensefeat)) ? (String) graph.getEdgeFeatures(edgeId).get(edgeSRLsensefeat) : null;
	}

	/**
	 * Get the SRL sent ID of the edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public Integer getEdgeSRLsentId(Integer edgeId) {
		return (graph.getEdgeFeatures(edgeId).containsKey(edgeSRLsentId)) ? (Integer) graph.getEdgeFeatures(edgeId).get(edgeSRLsentId) : null;
	}

	/**
	 * Get node POS
	 * 
	 * @param nodeId
	 * @return
	 */
	public String getNodePOS(Integer nodeId) {
		return (graph.getNodeFeatures(nodeId).containsKey(nodePOSfeat)) ? (String) graph.getNodeFeatures(nodeId).get(nodePOSfeat) : null;
	}

	/**
	 * Get node POS
	 * 
	 * @param nodeId
	 * @return
	 */
	public String getNodeLemma(Integer nodeId) {
		return (graph.getNodeFeatures(nodeId).containsKey(nodeLemmafeat)) ? (String) graph.getNodeFeatures(nodeId).get(nodeLemmafeat) : null;
	}
	
	/**
	 * The IDs of the coreference chains in which the node is the head of one of their elements.
	 * Usually it should be a one element List since each node can be head of one element of a specific coreference chain.
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getNodeCorefID(Integer nodeId) {
		return (graph.getNodeFeatures(nodeId).containsKey(nodeCorefIDfeat)) ? (Set<Integer>) graph.getNodeFeatures(nodeId).get(nodeCorefIDfeat) : null;
	}
	
	/**
	 * Get co-reference node name (aggregates all the tokens of the coreference chain element).
	 * 
	 * @param nodeId
	 * @return
	 */
	public String getNodeCorefName(Integer nodeId) {
		return (graph.getNodeFeatures(nodeId).containsKey(nodeCorefNamefeat)) ? (String) graph.getNodeFeatures(nodeId).get(nodeCorefNamefeat) : null;
	}

	/**
	 * Get node sentence order
	 * 
	 * @param nodeId
	 * @return
	 */
	public Integer getNodeSentOrder(Integer nodeId) {
		return (graph.getNodeFeatures(nodeId).containsKey(nodeSentenceOrderFeat)) ? (Integer) graph.getNodeFeatures(nodeId).get(nodeSentenceOrderFeat) : null;
	}
	
	/**
	 * Number of nodes in the graph
	 * 
	 * @return
	 */
	public int getNodeCount() {
		return this.graph.getNodeCount().intValue();
	}

	/**
	 * Get IDs of root nodes
	 * 
	 * @return
	 */
	public Set<Integer> getRootNodeIds() {
		// Roots have no parents
		Set<Integer> rootNodesId = new HashSet<Integer>();

		Map<Integer, String> nodesIdNameMap = graph.getNodeNames(null);
		for(Entry<Integer, String> nodesIdNameElem : nodesIdNameMap.entrySet()) {
			if(getOutgoingEdges(nodesIdNameElem.getKey(), null).size() == 0) {
				rootNodesId.add(nodesIdNameElem.getKey());
			}
		}

		return rootNodesId;
	}

	/**
	 * Get edges by source node id / destination node id / name
	 * At least on of source node Id / destination node Id / name has not to be blank / empty.
	 * (AND OF source node id, destination node id and name checks)
	 * 
	 * @param edgeList list of edges to filter
	 * @param sourceId
	 * @param destinationId
	 * @param name
	 * @return filtered edge ids list
	 */
	public Set<Integer> getEdgesByNameSourceAndDestination(Integer sourceId, Integer destinationId, String name) {
		Set<Integer> allGraphEdges = graph.getEdgesByKindSourceAndDestination(sourceId, destinationId, name);

		Set<Integer> result = new HashSet<Integer>();
		for(Integer graphEdge : allGraphEdges) {
			if(!SRLedges.contains(graphEdge)) {
				result.add(graphEdge);
			}
		}

		return result;
	}

	/**
	 * Get the id of the edges by regexp match on edge name
	 * 
	 * @param nameRegExp
	 * @return
	 */
	public Set<Integer> getEdgesByNameRegExp(String nameRegExp) {
		Set<Integer> result = new HashSet<Integer>();

		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		for(Entry<Integer, Pair<Integer, Integer>> edge : allEdges.entrySet()) {
			String edgeName = getEdgeName(edge.getKey());
			if(edgeName != null && edgeName.matches(nameRegExp)) {
				result.add(edge.getKey());
			}
		}

		for(Entry<Integer, Pair<Integer, Integer>> graphEdge : allEdges.entrySet()) {
			if(!SRLedges.contains(graphEdge.getKey())) {
				result.add(graphEdge.getKey());
			}
		}

		return result;
	}

	/**
	 * Get the id of the nodes by regexp match on node name
	 * 
	 * @param nameRegExp
	 * @return
	 */
	public Set<Integer> getNodesByNameRegExp(String nameRegExp) {
		Set<Integer> result = new HashSet<Integer>();

		Map<Integer, String> nodeIds = graph.getNodeNames(null);
		for(Entry<Integer, String> nodeEntry : nodeIds.entrySet()) {
			String nodeName = nodeEntry.getValue();
			if(nodeName != null && nodeName.matches(nameRegExp)) {
				result.add(nodeEntry.getKey());
			}
		}

		return result;
	}


	/**
	 * The id(s) of the parent node(s)
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getParentNodeIds(Integer nodeId) {		
		// Each edge connects a child (from) with its parent (to)
		Map<Integer, Pair<Integer, Integer>> outgoingEdges = getOutgoingEdges(nodeId, null);

		Set<Integer> result = new HashSet<Integer>();
		for(Entry<Integer, Pair<Integer, Integer>> outgoingEdge : outgoingEdges.entrySet()) {
			if(outgoingEdge.getValue() != null && outgoingEdge.getValue().getRight() != null) {
				result.add(outgoingEdge.getValue().getRight());
			}
		}

		return result;
	}

	/**
	 * The id(s) of the children node(s)
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getChildrenNodes(Integer nodeId) {
		// Each edge connects a child (from) with its parent (to)
		Map<Integer, Pair<Integer, Integer>> incidentEdges = getIncidentEdges(nodeId, null);

		Set<Integer> result = new HashSet<Integer>();
		for(Entry<Integer, Pair<Integer, Integer>> incidentEdge : incidentEdges.entrySet()) {
			if(incidentEdge.getValue() != null && incidentEdge.getValue().getLeft() != null) {
				result.add(incidentEdge.getValue().getLeft());
			}
		}

		return result;
	}
	
	/**
	 * Get the set of edges entering in the node.
	 * If name is not null or empty, only edges with that name are returned
	 * 
	 * @param nodeId
	 * @param name
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getIncidentEdges(Integer nodeId, String name) {
		Map<Integer, Pair<Integer, Integer>> incidentEdges = graph.getIncidentEdges(nodeId);

		Map<Integer, Pair<Integer, Integer>> result = new HashMap<Integer, Pair<Integer, Integer>>();
		for(Entry<Integer, Pair<Integer, Integer>> incidentEdge : incidentEdges.entrySet()) {
			if(!SRLedges.contains(incidentEdge.getKey()) &&
				(name == null || name.equals("")) || (name != null && name.equals(graph.getEdgeName(incidentEdge.getKey())) ) ) {
				result.put(incidentEdge.getKey(), incidentEdge.getValue());
			}
		}

		return Collections.unmodifiableMap(result);
	}

	/**
	 * Get the set of edges going out from the node.
	 * If name is not null or empty, only edges with that name are returned
	 * 
	 * @param nodeId
	 * @param name
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getOutgoingEdges(Integer nodeId, String name) {
		Map<Integer, Pair<Integer, Integer>> outgoingEdges = graph.getOutgoingEdges(nodeId);

		Map<Integer, Pair<Integer, Integer>> result = new HashMap<Integer, Pair<Integer, Integer>>();
		for(Entry<Integer, Pair<Integer, Integer>> outgoingEdge : outgoingEdges.entrySet()) {
			if(!SRLedges.contains(outgoingEdge.getKey()) &&
				(name == null || name.equals("")) || (name != null && name.equals(graph.getEdgeName(outgoingEdge.getKey())) ) )  {
				result.put(outgoingEdge.getKey(), outgoingEdge.getValue());
			}
		}

		return Collections.unmodifiableMap(result);
	}

	/**
	 * Get a map of node ID (key) / set of sense by considering only nodes that are roots of SRL frames
	 * The set of senses is the set of different frame the node participates in as root.
	 *  
	 * @return
	 */
	public Map<Integer, Set<String>> getSRLframeRoots() {
		Map<Integer, Set<String>> result = new HashMap<Integer, Set<String>>();

		Map<Integer, Pair<Integer, Integer>> edgeConnectionsMap = graph.getAllEdgeConnections();
		for(Entry<Integer, Pair<Integer, Integer>> edgeEntry : edgeConnectionsMap.entrySet()) {
			if(edgeEntry != null && edgeEntry.getValue() != null && getEdgeSRLsensefeat(edgeEntry.getKey()) != null) {
				String SRLsense = getEdgeSRLsensefeat(edgeEntry.getKey());
				if(result.containsKey(edgeEntry.getValue().getRight())) {
					result.get(edgeEntry.getValue().getRight()).add(SRLsense);
				}
				else {
					Set<String> nodeSenses = new HashSet<String>();
					nodeSenses.add(SRLsense);
					result.put(edgeEntry.getValue().getRight(), nodeSenses);
				}
			}
		}

		return result;
	}

	/**
	 * Get a map of predicates the node specified by ID is root of.
	 * Each element of the map has as key an unambiguous id of the SRL frame (ID of the sentence that contains the SRL frame) and 
	 * as value the set of ids of the edges participating in the frame
	 * 
	 * @param rootNodeId
	 * @return
	 */
	public Map<Integer, Set<Integer>> getSRLframeParticipantTags(Integer rootNodeId) {
		Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();

		if(rootNodeId == null || getNodeName(rootNodeId) == null) {
			return result;
		}

		Map<Integer, Pair<Integer, Integer>> rootIncidentEdges = graph.getIncidentEdges(rootNodeId);
		for(Entry<Integer, Pair<Integer, Integer>> incidentEdgeEntry : rootIncidentEdges.entrySet()) {
			if(incidentEdgeEntry.getValue() != null && incidentEdgeEntry.getValue().getLeft() != null && 
					getEdgeSRLsensefeat(incidentEdgeEntry.getKey()) != null) {
				// String senseFrat = getEdgeSRLsensefeat(outgoingEdgeEntry.getKey());
				// String senseTag = getEdgeSRLtag(outgoingEdgeEntry.getKey());
				Integer sentId = getEdgeSRLsentId(incidentEdgeEntry.getKey());

				if(result.containsKey(sentId)) {
					Set<Integer> edgeSet = result.get(sentId);
					edgeSet.add(incidentEdgeEntry.getKey());
					result.put(sentId, edgeSet);
				}
				else {
					Set<Integer> edgeSet = new HashSet<Integer>();
					edgeSet.add(incidentEdgeEntry.getKey());
					result.put(sentId, edgeSet);
				}
			}
		}

		return result;
	}

	/**
	 * Get a string representation of the graph
	 * 
	 * @param formatType
	 * @return
	 */
	public String graphAsString(GraphToStringENUM outputType) {
		String returnGraphString = "";

		outputType = (outputType != null) ? outputType : GraphToStringENUM.TREE;

		switch(outputType) {
		case NODE_LIST:
			returnGraphString = graph.graphAsString(outputType);
			break;

		case TREE:
			returnGraphString += "TREE:\n";
			Set<Integer> rootNodes = getRootNodeIds();
			returnGraphString += "Number of root nodes: " + rootNodes.size() + "\n";


			for(Integer rootNode : rootNodes) {
				String nodePOS = getNodePOS(rootNode);
				String nodeLemma = getNodeLemma(rootNode);
				returnGraphString += "ROOT NODE: '" + StringUtils.defaultIfEmpty(getNodeName(rootNode), "NO_NODE_NAME_DEFINED") + "' "
						+ "(id:" + rootNode + ", pos: " + StringUtils.defaultIfEmpty(nodePOS, "NO_POS") + ", "
						+ "lemma: " + StringUtils.defaultIfEmpty(nodeLemma, "NO_LEMMA") + ")\n";
				returnGraphString += printSubtree(rootNode, 1);
			}


			Map<Integer, Set<String>> SRLframes = getSRLframeRoots();
			returnGraphString += "SRL frames: " + SRLframes.size() + "\n";
			for(Entry<Integer, Set<String>> SRLframe : SRLframes.entrySet()) {
				returnGraphString += "     > ROOT NODE " + getNodeName(SRLframe.getKey()) + " (node id: " + SRLframe.getKey() + ", senses: " + SRLframe.getValue() + "):\n";
				Map<Integer, Set<Integer>> framesAndParticipants = getSRLframeParticipantTags(SRLframe.getKey());
				Integer participantId = 1;
				for(Entry<Integer, Set<Integer>> framesAndParticipant : framesAndParticipants.entrySet()) {
					if(framesAndParticipant.getKey() != null && framesAndParticipant.getValue() != null && framesAndParticipant.getValue().size() > 0) {
						returnGraphString += "        |-> FRAME ID " + framesAndParticipant.getKey() + ":\n";
						for(Integer frameEdgeId : framesAndParticipant.getValue()) {
							returnGraphString += "           |-> edge: " + getEdgeName(frameEdgeId) + " (edge id: " + frameEdgeId + ") -> " + this.getNodeName(this.getEdgeFromNode(frameEdgeId)) + " (node id: " + this.getEdgeFromNode(frameEdgeId) + ")\n";
						}
						participantId++;
					}
				}
			} 

			break;
		}

		return returnGraphString;
	}

	private String printSubtree(Integer rootId, int treeDepth) {
		String retSubtreeString = "";

		if(treeDepth < 10 && rootId != null && getNodeName(rootId) != null) {

			for(Entry<Integer, Pair<Integer, Integer>> incidenteEdge : this.getIncidentEdges(rootId, null).entrySet()) {
				if(incidenteEdge.getKey() != null && !SRLedges.contains(incidenteEdge.getKey()) && incidenteEdge.getValue() != null) {
					Integer childId = incidenteEdge.getValue().getLeft();
					String nodePOS = getNodePOS(childId);
					String nodeLemma = getNodeLemma(childId);
					String nodeName = StringUtils.defaultIfEmpty(this.getNodeName(childId), "NO_NODE_NAME_DEFINED");
					String linkName = StringUtils.defaultIfEmpty(getEdgeName(incidenteEdge.getKey()), "NO_EDGE_NAME_DEFINED");
					String padding = "";
					for(int k = 0; k <= treeDepth; k++) {
						padding += "   ";
					}

					retSubtreeString += padding + "|-> " + linkName + " --> " + nodeName + "' (id:" + childId + ", pos: " + StringUtils.defaultIfEmpty(nodePOS, "NO_POS") + ", "
							+ "lemma: " + StringUtils.defaultIfEmpty(nodeLemma, "NO_LEMMA") + ")\n";
					retSubtreeString += printSubtree(childId, new Integer(treeDepth) + 1);
				}
			}
		}

		return retSubtreeString;
	}

	/**
	 *  Delete all the edges with name that matches one of the reg exps
	 *  
	 * @param regexpList
	 * @param deleteMatching if false delete the edges that doesn't match
	 * @return
	 */
	public int deleteEdgesByNameRegExp(List<String> regexpList, boolean deleteMatching) {
		int deletedEdges = 0;

		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		Set<Integer> edgesToRemove = new HashSet<Integer>();
		for(Entry<Integer, Pair<Integer, Integer>> edge : allEdges.entrySet()) {
			String edgeName = getEdgeName(edge.getKey());
			if(edgeName != null) {
				Integer matchCount = 0;
				for(String regexp : regexpList) {
					boolean matchCheck = edgeName.matches(regexp);
					if(matchCheck) {
						matchCount++;
					}
				}
				
				if((matchCount == 0 && !deleteMatching) || (matchCount > 0 && deleteMatching)) {
					edgesToRemove.add(edge.getKey());
					deletedEdges++;
				}
			}
		}

		edgesToRemove.forEach((edgeId) -> { SRLedges.remove(edgeId); graph.removeEdge(edgeId); } );

		return deletedEdges;
	}

	/**
	 * Merge the second node with the first.
	 * The properties of the first node are preserved. In-going and outgoing edges of the second node
	 * are moved to the first one.
	 * 
	 * NB: the new name of a merged pair of nodes is consistent only if both nodes are in the same sentence. Since the 
	 * sentence specific order is exploited to determine the order of the names of the merged nodes. 
	 * 
	 * @param nodeId1
	 * @param nodeId2
	 * @param newNodeName if not null, the node 1 will be renamed
	 * @return
	 */
	public boolean mergeNodes(Integer nodeId1, Integer nodeId2, String newNodeName) {
		if(nodeId1 == null || getNodeName(nodeId1) == null || nodeId2 == null || getNodeName(nodeId2) == null) {
			return false;
		}

		Set<Integer> edgesToDelete = new HashSet<Integer>();

		// Transfer incident edges
		Map<Integer, Pair<Integer, Integer>> incidentEdges = graph.getIncidentEdges(nodeId2);
		for(Entry<Integer, Pair<Integer, Integer>> edge : incidentEdges.entrySet()) {
			if(!edge.getValue().getLeft().equals(nodeId1)) {
				Map<String, Object> edgeFeatures = graph.getEdgeFeatures(edge.getKey());
				Integer newEdgeId = graph.addEdge(edge.getValue().getLeft(), nodeId1, getEdgeName(edge.getKey()));
				for(Entry<String, Object> edgeFeature : edgeFeatures.entrySet()) {
					graph.addEdgeFeature(newEdgeId, edgeFeature.getKey(), edgeFeature.getValue());
				}
				if(SRLedges.contains(edge.getKey())) {
					SRLedges.add(newEdgeId);
				}
			}
			edgesToDelete.add(edge.getKey());
		}

		// Transfer outgoing edges
		Map<Integer, Pair<Integer, Integer>> outgoingEdges = graph.getOutgoingEdges(nodeId2);
		for(Entry<Integer, Pair<Integer, Integer>> edge : outgoingEdges.entrySet()) {
			if(!edge.getValue().getRight().equals(nodeId1)) {
				Map<String, Object> edgeFeatures = graph.getEdgeFeatures(edge.getKey());
				Integer newEdgeId = graph.addEdge(nodeId1, edge.getValue().getRight(), getEdgeName(edge.getKey()));
				for(Entry<String, Object> edgeFeature : edgeFeatures.entrySet()) {
					graph.addEdgeFeature(newEdgeId, edgeFeature.getKey(), edgeFeature.getValue());
				}
				if(SRLedges.contains(edge.getKey())) {
					SRLedges.add(newEdgeId);
				}
			}
			edgesToDelete.add(edge.getKey());
		}

		edgesToDelete.stream().forEach((edgeId) -> {graph.removeEdge(edgeId); SRLedges.remove(edgeId);});

		// Transferring merged node map from node 2 to node 1
		Map<String, Object> featureMap_node1 = graph.getNodeFeatures(nodeId1);
		Map<Integer, String> mergedNodeMap_node1 = new TreeMap<Integer, String>(); 
		if(featureMap_node1 != null && featureMap_node1.containsKey(node_mergedNameMap)) {
			mergedNodeMap_node1 = (Map<Integer, String>) featureMap_node1.get(node_mergedNameMap);
		}
		else {
			graph.addNodeFeature(nodeId1, node_mergedNameMap, mergedNodeMap_node1);
		}
		
		Map<String, Object> featureMap_node2 = graph.getNodeFeatures(nodeId2);
		if(featureMap_node2 != null && featureMap_node2.containsKey(node_mergedNameMap)) {
			Map<Integer, String> mergedNodeMap_node2 = (Map<Integer, String>) featureMap_node2.get(node_mergedNameMap);
			if(mergedNodeMap_node2 != null && mergedNodeMap_node2.size() > 0) {
				for(Entry<Integer, String> mapEntry : mergedNodeMap_node2.entrySet()) {
					if(mapEntry.getKey() != null && mapEntry.getValue() != null) {
						mergedNodeMap_node1.put(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
		}
		
		// Merge original node IDs
		Map<Integer, String> mergedNodeIDmap_node1 = new HashMap<Integer, String>(); 
		if(featureMap_node1 != null && featureMap_node1.containsKey(node_mergedIDmap)) {
			mergedNodeIDmap_node1 = (Map<Integer, String>) featureMap_node1.get(node_mergedIDmap);
		}
		else {
			graph.addNodeFeature(nodeId1, node_mergedIDmap, mergedNodeIDmap_node1);
		}
		
		if(featureMap_node2 != null && featureMap_node2.containsKey(node_mergedIDmap)) {
			Map<Integer, String> mergedNodeIDset_node2 = (Map<Integer, String>) featureMap_node2.get(node_mergedIDmap);
			if(mergedNodeIDset_node2 != null && mergedNodeIDset_node2.size() > 0) {
				for(Entry<Integer, String> mapEntry : mergedNodeIDset_node2.entrySet()) {
					if(mapEntry.getKey() != null && mapEntry.getValue() != null) {
						mergedNodeIDmap_node1.put(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
		}
		
		// Rename node1
		if(StringUtils.isNotBlank(newNodeName)) {
			graph.changeNodeName(nodeId1, newNodeName);
		}
		else {
			String updatedNodeName = "";
			for(Entry<Integer, String> nodeNamePart : mergedNodeMap_node1.entrySet()) {
				if(updatedNodeName.length() > 0) {
					updatedNodeName += " ";
				}
				updatedNodeName += nodeNamePart.getValue();
			}
			if(StringUtils.isNotBlank(updatedNodeName)) {
				graph.changeNodeName(nodeId1, updatedNodeName);
			}
		}

		// Remove node2
		graph.removeNode(nodeId2);

		return true;
	}
	
	/**
	 * Get the map of original nodeID and lexicalization of the nodes merged in the current node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, String> getMergedIDmap(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(node_mergedIDmap)) {
			Map<Integer, String> mergedNodeIDmap_node = (Map<Integer, String>) featureMap_node.get(node_mergedIDmap);
			return Collections.unmodifiableMap(mergedNodeIDmap_node);
		}
		else {
			return Collections.unmodifiableMap(new HashMap<Integer, String>());
		}
	}
	
	/**
	 * Add an element to the node_mergedNameMap of the node
	 * 
	 * @param nodeId
	 * @param nodeName
	 * @param emptyMap
	 * @return
	 */
	public boolean addToMergedNameMap(Integer nodeId, Integer mergedNodeId, String mergedNodeName, boolean emptyMap) {
		if(nodeId == null || getNodeName(nodeId) == null || mergedNodeId == null || mergedNodeName == null || mergedNodeName.equals("")) {
			return false;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(emptyMap || !featureMap_node.containsKey(node_mergedNameMap)) {
			featureMap_node.put(node_mergedNameMap, new HashMap<Integer, String>());
		}
		
		((Map<Integer, String>) featureMap_node.get(node_mergedNameMap)).put(mergedNodeId, mergedNodeName);
		
		return true;
	}
	
	/**
	 * Add an element to the node_mergedIDmap of the node
	 * 
	 * @param nodeId
	 * @param nodeName
	 * @param emptyMap
	 * @return
	 */
	public boolean addToMergedIDmap(Integer nodeId, Integer mergedNodeId, String mergedNodeName, boolean emptyMap) {
		if(nodeId == null || getNodeName(nodeId) == null || mergedNodeId == null || mergedNodeName == null || mergedNodeName.equals("")) {
			return false;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(emptyMap || !featureMap_node.containsKey(node_mergedIDmap)) {
			featureMap_node.put(node_mergedIDmap, new HashMap<Integer, String>());
		}
		
		((Map<Integer, String>) featureMap_node.get(node_mergedIDmap)).put(mergedNodeId, mergedNodeName);
		
		return true;
	}
	
	
	/**
	 * Get the map of sentence order ID of node and lexicalization of the sentence nodes merged in the current node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, String> getMergedNameMap(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(node_mergedNameMap)) {
			Map<Integer, String> mergedNodeNameMap_node = (Map<Integer, String>) featureMap_node.get(node_mergedNameMap);
			return Collections.unmodifiableMap(mergedNodeNameMap_node);
		}
		else {
			return Collections.unmodifiableMap(new HashMap<Integer, String>());
		}
	}
	
	/**
	 * Get the map of causal relations the nodes is part of. The keys are the ID of each causal relation, 
	 * while the values are the role of the node in each causal relation (CAUSE or EFFECT).
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, String> getCausalRoleNameMap(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(causal_IDrole)) {
			Map<Integer, String> mergedNodeNameMap_node = (Map<Integer, String>) featureMap_node.get(causal_IDrole);
			return Collections.unmodifiableMap(mergedNodeNameMap_node);
		}
		else {
			return Collections.unmodifiableMap(new HashMap<Integer, String>());
		}
	}

	/**
	 * Get the map of causal relations the nodes is part of. The keys are the ID of each causal relation, 
	 * while the values are the ID of the cause node in each causal relation.
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, Integer> getCausalCauseIDmap(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(causal_IDcauseNodesID)) {
			Map<Integer, Integer> mergedNodeNameMap_node = (Map<Integer, Integer>) featureMap_node.get(causal_IDcauseNodesID);
			return Collections.unmodifiableMap(mergedNodeNameMap_node);
		}
		else {
			return Collections.unmodifiableMap(new HashMap<Integer, Integer>());
		}
	}
	
	/**
	 * Get the map of causal relations the nodes is part of. The keys are the ID of each causal relation, 
	 * while the values are the ID of the cause node in each causal relation.
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, Integer> getCausalEffectIDmap(Integer nodeId) {
		if(nodeId == null || getNodeName(nodeId) == null) {
			return null;
		}
		
		Map<String, Object> featureMap_node = graph.getNodeFeatures(nodeId);
		if(featureMap_node != null && featureMap_node.containsKey(causal_IDeffectNodesID)) {
			Map<Integer, Integer> mergedNodeNameMap_node = (Map<Integer, Integer>) featureMap_node.get(causal_IDeffectNodesID);
			return Collections.unmodifiableMap(mergedNodeNameMap_node);
		}
		else {
			return Collections.unmodifiableMap(new HashMap<Integer, Integer>());
		}
	}
	
	/**
	 * Check and remove self loops edges and merges duplicated / non-SRL edges
	 */
	public void sanitizeGraph() {
		// Self loops
		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();
		Set<Integer> edgesToRemove = new HashSet<Integer>(); 
		for(Entry<Integer, Pair<Integer, Integer>> edge : allEdges.entrySet()) {
			if(edge.getValue() != null && edge.getValue().getLeft().equals(edge.getValue().getRight())) {
				edgesToRemove.add(edge.getKey());
			}
		}
		edgesToRemove.stream().forEach( (edgeId) -> { 
			SRLedges.remove(edgeId);
			graph.removeEdge(edgeId);
		});

		// Duplicated edges
		Map<Integer, String> nodesIdNameMap = graph.getNodeNames(null);
		for(Entry<Integer, String> nodesIdNameElem : nodesIdNameMap.entrySet()) {
			Map<Integer, Pair<Integer, Integer>> outgoingEdges = getOutgoingEdges(nodesIdNameElem.getKey(), null);

			Map<Integer, Set<Integer>> incidentNodeIdDuplicatedEdges = new HashMap<Integer, Set<Integer>>();
			for(Entry<Integer, Pair<Integer, Integer>> outgoingEdge : outgoingEdges.entrySet()) {
				if(SRLedges.contains(outgoingEdge.getKey())) {
					continue;
				}

				Integer outgoingNodeId = outgoingEdge.getValue().getRight();
				if(incidentNodeIdDuplicatedEdges.containsKey(outgoingNodeId)) {
					incidentNodeIdDuplicatedEdges.get(outgoingNodeId).add(outgoingEdge.getKey());
				}
				else {
					Set<Integer> edgeList = new HashSet<Integer>();
					edgeList.add(outgoingEdge.getKey());
					incidentNodeIdDuplicatedEdges.put(outgoingNodeId, edgeList);
				}
			}

			for(Entry<Integer, Set<Integer>> incidentEdgesCount : incidentNodeIdDuplicatedEdges.entrySet()) {
				if(incidentEdgesCount.getValue().size() > 1) {
					// Set of edges to merge
					List<Integer> edgeIdsToMergeList = new ArrayList<Integer>();
					edgeIdsToMergeList.addAll(incidentEdgesCount.getValue());

					if(edgeIdsToMergeList.size() == 2) {
						mergeParallEdges(edgeIdsToMergeList.get(0), edgeIdsToMergeList.get(1));
					}
					else {
						Integer newEdgeId = mergeParallEdges(edgeIdsToMergeList.get(0), edgeIdsToMergeList.get(1));
						for(int i = 2; i < edgeIdsToMergeList.size(); i++) {
							if(newEdgeId != null) {
								newEdgeId = mergeParallEdges(newEdgeId, edgeIdsToMergeList.get(i));
							}
						}
					}
				}
			}

		}
	}


	/**
	 * Tentative version of node collapsing heuristics over a dependency graph
	 * 
	 * @return
	 */
	public Integer compactNodes() {
		Map<Integer, Pair<Integer, Integer>> allEdges = graph.getAllEdgeConnections();

		Set<Integer> rootNodes = this.getRootNodeIds();

		boolean collapseOccurred = true;
		Integer mergeCount = 0;
		while(collapseOccurred) {
			collapseOccurred = false;

			for(Entry<Integer, Pair<Integer, Integer>> edge : allEdges.entrySet()) {

				if(edge.getValue() != null && edge.getValue().getLeft() != null && getIncidentEdges(edge.getValue().getLeft(), null).size() == 0) {
					String edgeName = getEdgeName(edge.getKey());
					String fromNodeName = getNodeName(edge.getValue().getLeft());
					String fromNodePOS = getNodePOS(edge.getValue().getLeft());

					// NMOD collapse
					if(Util.strCompare(edgeName, "NMOD") && edge.getValue() != null) {
						if(fromNodePOS != null &&
								( fromNodePOS.startsWith("N") || fromNodePOS.startsWith("J") || fromNodePOS.startsWith("R") || 
										fromNodePOS.startsWith("VBG") || fromNodePOS.startsWith("VBN") ) ){
							collapseOccurred = true;
						}
						else if (fromNodeName != null && fromNodeName.equals("no")) {
							collapseOccurred = true;
						}
						else if(fromNodePOS.startsWith("MD")) {
							// Keep noun modifier
						}
						else if(fromNodeName != null && fromNodeName.equals("to")) {
							// Keep noun modifier
						}
						else {
							collapseOccurred = true;
						}
					}
					else if(Util.strCompare(edgeName, "IM") && (fromNodePOS != null && fromNodePOS.startsWith("V") ) ) {
						collapseOccurred = true;
					}
					else if(Util.strCompare(edgeName, "VC") && (fromNodePOS != null && fromNodePOS.startsWith("V") ) ) {
						collapseOccurred = true;
					}
					else if(Util.strCompare(edgeName, "PMOD") && (fromNodePOS != null && fromNodePOS.startsWith("N") ) ) {
						collapseOccurred = true;
					}
					else if(Util.strCompare(edgeName, "AMOD") && (fromNodePOS != null && fromNodePOS.startsWith("RB") ) ) {
						collapseOccurred = true;
					}
					else if(Util.strCompare(edgeName, "P")) {
						collapseOccurred = true;
					}

					if(collapseOccurred && !rootNodes.contains(edge.getValue().getRight())) {
						collapseOccurred = mergeNodes(edge.getValue().getRight(), edge.getValue().getLeft(), null);
					}
					else {
						collapseOccurred = false;
					}
				}
			}
		}

		sanitizeGraph();

		return mergeCount;
	}

	public static void main(String[] args) {
		List<String> regexp = new ArrayList<String>();
		regexp.add(".*");
		regexp.add("OB");

		System.out.println("srlfaf".matches(regexp.get(0)));
		System.out.println("depe".matches(regexp.get(0)));
	}
}
