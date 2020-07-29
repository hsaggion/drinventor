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
package edu.upf.taln.dri.lib.model.graph.generic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.in_memory.InMemoryGrph;
import toools.set.IntSet;

/**
 * IMPORTANT: Never instantiate directly this class! <br/>
 * Generic graph implementation.
 * 
 *
 */
public class GenericDirectedGraphGRPHimpl extends InMemoryGrph implements GenericDirectedGraph {

	private static final long serialVersionUID = 1L;

	private Map<Integer, String> nodeNameMap;
	private Map<Integer, Map<String, Object>> nodeFeatureMap;

	private Map<Integer, String> edgeNameMap;
	private Map<Integer, Map<String, Object>> edgeFeatureMap;


	public GenericDirectedGraphGRPHimpl() {
		super();
		this.nodeNameMap = new HashMap<Integer, String>();
		this.nodeFeatureMap = new HashMap<Integer, Map<String, Object>>();
		this.edgeNameMap = new HashMap<Integer, String>();
		this.edgeFeatureMap = new HashMap<Integer, Map<String, Object>>();
	}

	public GenericDirectedGraphGRPHimpl(Map<Integer, String> nodeNameMap,
			Map<Integer, Map<String, Object>> nodeFeatureMap,
			Map<Integer, String> edgeNameMap,
			Map<Integer, Map<String, Object>> edgeFeatureMap) {
		super();
		this.nodeNameMap = nodeNameMap;
		this.nodeFeatureMap = nodeFeatureMap;
		this.edgeNameMap = edgeNameMap;
		this.edgeFeatureMap = edgeFeatureMap;
	}

	// **********************************************************
	// **********************************************************
	// **********************************************************
	// Graph population
	public Integer addNode(Integer nodeId, String nodeName) {
		Integer result = null;

		if(StringUtils.isBlank(nodeName)) {
			return result;
		}
		
		if(nodeId == null) {
			result = this.addVertex();
		}
		else if(!nodeNameMap.containsKey(nodeId)) {
			this.addVertex(nodeId.intValue());
			result = nodeId;
		}

		if(result != null) {
			// If the node has been added
			nodeNameMap.put(result, nodeName);
			nodeFeatureMap.put(result, new HashMap<String, Object>());
		}

		return result;
	}

	public boolean changeNodeName(Integer nodeId, String nodeName) {
		if(StringUtils.isNotBlank(nodeName) && nodeId != null && this.containsVertex(nodeId)) {
			nodeNameMap.put(nodeId, nodeName);
			return true;
		}
		return false;
	}
	
	public boolean removeNode(Integer nodeId) {
		boolean result = false;

		if(nodeId != null && this.containsVertex(nodeId)) {
			// Remove incident and outgoing edges
			Map<Integer, Pair<Integer, Integer>> incidentEdges = this.getIncidentEdges(nodeId);
			if(!CollectionUtils.isEmpty(incidentEdges)) {
				for(Entry<Integer, Pair<Integer, Integer>> incidentEdge : incidentEdges.entrySet()) {
					if(incidentEdge != null && incidentEdge.getKey() != null && this.containsEdge(incidentEdge.getKey())) {
						this.removeEdge(incidentEdge.getKey());
					}
				}
			}

			Map<Integer, Pair<Integer, Integer>> outgoingEdges = this.getOutgoingEdges(nodeId);
			if(!CollectionUtils.isEmpty(outgoingEdges)) {
				if(!CollectionUtils.isEmpty(outgoingEdges)) {
					for(Entry<Integer, Pair<Integer, Integer>> outgoingEdge : outgoingEdges.entrySet()) {
						if(outgoingEdge != null && outgoingEdge.getKey() != null && this.containsEdge(outgoingEdge.getKey())) {
							this.removeEdge(outgoingEdge.getKey());
						}
					}
				}
			}

			// Remove name and features map
			nodeNameMap.remove(nodeId);
			nodeFeatureMap.remove(nodeId);

			this.removeVertex(nodeId);

			result = true;
		}

		return result;
	}

	public Integer addEdge(Integer from, Integer to, String edgeName) {
		Integer result = null;
		
		if(StringUtils.isBlank(edgeName)) {
			return result;
		}

		if(from != null && this.containsVertex(from) && to != null && this.containsVertex(to)) {
			result = this.addSimpleEdge(from, to, true);
		}

		if(result != null) {
			// If the edge has been added
			edgeNameMap.put(result, edgeName);
			edgeFeatureMap.put(result, new HashMap<String, Object>());
		}

		return result;
	}
	
	public boolean changeEdgeName(Integer edgeId, String edgeName) {
		if(StringUtils.isNotBlank(edgeName) && edgeId != null && this.containsEdge(edgeId)) {
			edgeNameMap.put(edgeId, edgeName);
			return true;
		}
		return false;
	}

	public boolean removeEdge(Integer edgeId) {
		boolean result = false;

		if(edgeId != null && this.containsEdge(edgeId)) {

			// Remove name and features map
			edgeNameMap.remove(edgeId);
			edgeFeatureMap.remove(edgeId);

			((InMemoryGrph) this).removeEdge(edgeId);

			result = true;
		}

		return result;
	}

	public boolean addNodeFeature(Integer nodeId, String featureName, Object featureValue) {
		boolean result = false;

		if(nodeId != null && StringUtils.isNotBlank(featureName) && nodeFeatureMap.containsKey(nodeId)) {
			nodeFeatureMap.get(nodeId).put(featureName, featureValue);
			result = true;
		}

		return result;
	}

	public boolean addEdgeFeature(Integer edgeId, String featureName, Object featureValue) {
		boolean result = false;

		if(edgeId != null && StringUtils.isNotBlank(featureName) && edgeFeatureMap.containsKey(edgeId)) {
			edgeFeatureMap.get(edgeId).put(featureName, featureValue);
			result = true;
		}

		return result;
	}

	public int removeEdgesByNameSourceAndDestination(Integer sourceId, Integer destinationId, String name) {
		int removedEdges = 0;

		if(sourceId != null || destinationId != null || StringUtils.isNotBlank(name)) {
			Map<Integer, Pair<Integer, Integer>> edgeConnections = this.getAllEdgeConnections();

			Set<Integer> edgesToRemove = new HashSet<Integer>();
			for(Entry<Integer, Pair<Integer, Integer>> edgeConnection : edgeConnections.entrySet()) {
				if( ( sourceId == null || edgeConnection.getValue().getLeft().equals(sourceId) ) &&
						( destinationId == null || edgeConnection.getValue().getRight().equals(destinationId) ) &&
						( StringUtils.isBlank(name) || this.edgeNameMap.get(edgeConnection.getKey()).equalsIgnoreCase(name)) ) {
					edgesToRemove.add(edgeConnection.getKey());
				}
			}

			for(Integer edgeToRemove : edgesToRemove) {
				this.removeEdge(edgeToRemove);
				removedEdges++;
			}
		}

		return removedEdges;
	}

	// **********************************************************
	// **********************************************************
	// **********************************************************

	// Graph browsing
	public Set<Integer> getNodesByName(String name) {
		Set<Integer> result = new HashSet<Integer>();

		if(StringUtils.isNotBlank(name)) {
			for(Entry<Integer, String> nodeNameMapEntry : this.nodeNameMap.entrySet()) {
				if(nodeNameMapEntry != null && nodeNameMapEntry.getValue() != null && nodeNameMapEntry.getValue().equals(name)) {
					result.add(nodeNameMapEntry.getKey());
				}
			}
		}

		return result;
	}

	public Map<Integer, String> getNodeNames(Integer nodeId) {
		Map<Integer, String> result = new HashMap<Integer, String>();

		if(nodeId != null && this.containsVertex(nodeId.intValue())) {
			result = new HashMap<Integer, String>();
			result.put(nodeId, (String) nodeNameMap.get(nodeId));
		}
		else {
			result = Collections.unmodifiableMap(this.nodeNameMap);
		}

		return result;
	}

	public String getNodeName(Integer nodeId) {
		String result = null;

		if(nodeId != null && this.containsVertex(nodeId.intValue())) {
			result = nodeNameMap.get(nodeId);
		}

		return result;
	}

	@Override
	public Set<Integer> getEdgesByName(String name) {
		Set<Integer> result = new HashSet<Integer>();

		if(name != null && !name.equals("")) {
			for(Entry<Integer, String> edgeNameMapEntry : this.edgeNameMap.entrySet()) {
				if(edgeNameMapEntry != null && edgeNameMapEntry.getValue() != null && edgeNameMapEntry.getValue().endsWith(name)) {
					result.add(edgeNameMapEntry.getKey());
				}
			}
		}

		return result;
	}

	public Map<Integer, String> getEdgeNames(Integer edgeId) {
		Map<Integer, String> result = new HashMap<Integer, String>();

		if(edgeId != null && this.containsEdge(edgeId.intValue())) {
			result = new HashMap<Integer, String>();
			result.put(edgeId, (String) edgeNameMap.get(edgeId));
		}
		else {
			result = Collections.unmodifiableMap(this.edgeNameMap);
		}

		return result;
	}

	public String getEdgeName(Integer edgeId) {
		String result = null;

		if(edgeId != null && this.containsEdge(edgeId.intValue())) {
			result = edgeNameMap.get(edgeId);
		}

		return result;
	}

	public Integer getNodeCount() {
		if(this.nodeNameMap != null) {
			return new Integer(this.nodeNameMap.size());
		}
		else {
			return 0;
		}
	}

	public Integer getEdgeCount() {
		if(this.edgeNameMap != null) {
			return new Integer(this.edgeNameMap.size());
		}
		else {
			return 0;
		}
	}

	public Set<Integer> getChildrenNodes(Integer nodeId) {
		Set<Integer> result = new HashSet<Integer>();

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet outEdges = this.getOutEdges(nodeId);
			for (IntCursor c : outEdges) {
				int e = c.value;
				int v1 = getOneVertex(e);
				int v2 = getTheOtherVertex(e, v1);
				result.add(v2);
			}
		}

		return result;
	}

	public Integer getChildrenNodesCount(Integer nodeId) {
		Integer result = null;

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet outEdges = this.getOutEdges(nodeId);
			result = outEdges.size();
		}

		return result;
	}

	public Set<Integer> getParentNodes(Integer nodeId) {
		Set<Integer> result = new HashSet<Integer>();

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet inEdges = this.getInEdges(nodeId);
			for (IntCursor c : inEdges) {
				int e = c.value;
				int v1 = getOneVertex(e);
				// int v2 = getTheOtherVertex(e, v1);
				result.add(v1);
			}
		}

		return result;
	}

	public Integer getParentNodesCount(Integer nodeId) {
		Integer result = null;

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet inEdges = this.getInEdges(nodeId);
			result = inEdges.size();
		}

		return result;
	}

	public Set<Integer> getRoots() {
		Set<Integer> result = new HashSet<Integer>();

		IntSet verticesIds = this.getVertices();
		for (IntCursor c : verticesIds) {
			int e = c.value;
			if(this.getEdgesIncidentTo(e).size() <= 0) {
				result.add(e);
			}
		}

		return result;
	}

	public Map<Integer, Pair<Integer, Integer>> getIncidentEdges(Integer nodeId) {		
		Map<Integer, Pair<Integer, Integer>> result = new HashMap<Integer, Pair<Integer, Integer>>();

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet inEdges = this.getInEdges(nodeId);
			for (IntCursor c : inEdges) {
				int e = c.value;
				int v1 = getOneVertex(e);
				int v2 = getTheOtherVertex(e, v1);
				result.put(e, Pair.of(v1, v2));
			}
		}

		return result;
	}

	public Map<Integer, Pair<Integer, Integer>> getOutgoingEdges(Integer nodeId) {		
		Map<Integer, Pair<Integer, Integer>> result = new HashMap<Integer, Pair<Integer, Integer>>();

		if(nodeId != null && this.containsVertex(nodeId)) {
			IntSet outEdges = this.getOutEdges(nodeId);
			for (IntCursor c : outEdges) {
				int e = c.value;
				int v1 = getOneVertex(e);
				int v2 = getTheOtherVertex(e, v1);
				result.put(e, Pair.of(v1, v2));
			}
		}

		return result;
	}

	public Map<Integer, Pair<Integer, Integer>> getAllEdgeConnections() {
		if (getNumberOfHyperEdges() > 0)
			throw new IllegalStateException("this graph has hyperedges");

		Map<Integer, Pair<Integer, Integer>> result = new HashMap<Integer, Pair<Integer, Integer>>();

		if(this.edgeNameMap != null) {
			for (IntCursor c : this.getEdges()) {
				int e = c.value;
				int v1 = getOneVertex(e);
				int v2 = getTheOtherVertex(e, v1);
				result.put(e, Pair.of(v1, v2));
			}
		}

		return result;
	}

	public Map<String, Object> getNodeFeatures(Integer nodeId) {
		if(nodeId != null && this.nodeFeatureMap != null && 
				this.nodeFeatureMap.containsKey(nodeId) && this.nodeFeatureMap.get(nodeId) != null) {
			return Collections.unmodifiableMap(this.nodeFeatureMap.get(nodeId));
		}
		else {
			return new HashMap<String, Object>();
		}
	}

	public Map<String, Object> getEdgeFeatures(Integer edgeId) {
		if(edgeId != null && this.edgeFeatureMap != null && 
				this.edgeFeatureMap.containsKey(edgeId) && this.edgeFeatureMap.get(edgeId) != null) {
			return Collections.unmodifiableMap(this.edgeFeatureMap.get(edgeId));
		}
		else {
			return new HashMap<String, Object>();
		}
	}

	public Set<Integer> getEdgesByKindSourceAndDestination(Integer sourceId, Integer destinationId, String name) {
		Set<Integer> filteredEdges = new HashSet<Integer>();

		if(sourceId != null || destinationId != null || StringUtils.isNotBlank(name)) {
			Map<Integer, Pair<Integer, Integer>> edgeConnections = this.getAllEdgeConnections();

			for(Entry<Integer, Pair<Integer, Integer>> edgeConnection : edgeConnections.entrySet()) {
				if( ( sourceId == null || edgeConnection.getValue().getLeft().equals(sourceId) ) &&
						( destinationId == null || edgeConnection.getValue().getRight().equals(destinationId) ) &&
						( StringUtils.isBlank(name) || this.edgeNameMap.get(edgeConnection.getKey()).equalsIgnoreCase(name)) ) {
					filteredEdges.add(edgeConnection.getKey());
				}
			}
		}

		return filteredEdges;
	}

	public String graphAsString(GraphToStringENUM outputType) {
		String returnGraphString = "";

		outputType = (outputType != null) ? outputType : GraphToStringENUM.TREE;

		switch(outputType) {
		case NODE_LIST:
			returnGraphString += "NODE LIST:\n";
			for(Entry<Integer, String> nodeEntry : this.getNodeNames(null).entrySet()) {
				if(nodeEntry != null && nodeEntry.getKey() != null && nodeEntry.getValue() != null) {
					Integer nodeFeaturesNumber = this.getNodeFeatures(nodeEntry.getKey()).size();
					returnGraphString += "\n\n   > name: '" + nodeEntry.getValue() + "' (id:" + nodeEntry.getKey() + ", number of features: " + nodeFeaturesNumber + ((nodeFeaturesNumber > 0) ? " value: " + this.getNodeFeatures(nodeEntry.getKey()).keySet() : "") + ")\n";

					int edgeAdded = 0;
					returnGraphString += "      *> INCIDENT EDGES:\n";
					for(Entry<Integer, Pair<Integer, Integer>> edgeEntry : this.getIncidentEdges(nodeEntry.getKey()).entrySet()) {
						if(edgeEntry != null) {
							String edgeType = StringUtils.defaultIfBlank(this.getEdgeName(edgeEntry.getKey()), "NO_EDGE_NAME_DEFINED");
							String originName = StringUtils.defaultIfBlank(this.getNodeName(edgeEntry.getValue().getLeft()), "NO_ORIGIN_NODE_NAME_DEFINED");
							Integer originFeaturesNumber = this.getNodeFeatures(edgeEntry.getValue().getLeft()).size();
							returnGraphString += "       |--- " + originName + " ---> '" + edgeType + "' --> " + nodeEntry.getValue() + " (C) (incident id:" + edgeEntry.getValue().getLeft() + ", features: " + originFeaturesNumber + ((originFeaturesNumber > 0) ? " value: " + this.getNodeFeatures(edgeEntry.getValue().getLeft()).keySet() : "") + ")\n";
							edgeAdded++;
						}
					}

					returnGraphString += "      *> OUTGOING EDGES:\n";
					for(Entry<Integer, Pair<Integer, Integer>> edgeEntry : this.getOutgoingEdges(nodeEntry.getKey()).entrySet()) {
						if(edgeEntry != null) {
							String edgeType = StringUtils.defaultIfBlank(this.getEdgeName(edgeEntry.getKey()), "NO_EDGE_NAME_DEFINED");
							String targetName = StringUtils.defaultIfBlank(this.getNodeName(edgeEntry.getValue().getRight()), "NO_DESTINATION_NODE_NAME_DEFINED");
							Integer targeFeaturesNumber = this.getNodeFeatures(edgeEntry.getValue().getRight()).size();
							returnGraphString += "       |--- " + nodeEntry.getValue() + " (C) ---> '" + edgeType + "' ---> " + targetName + " (id:" + edgeEntry.getValue().getRight() + ", features: " + targeFeaturesNumber + ((targeFeaturesNumber > 0) ? " value: " + this.getNodeFeatures(edgeEntry.getValue().getRight()).keySet() : "") + ")\n";
							edgeAdded++;
						}
					}

					if(edgeAdded == 0) {
						returnGraphString += "      *> NO EDGES INCIDENT OR OUTGOING FROM THIS NODE\n";	
					}
				}
			}

			break;

		case TREE:
			returnGraphString += "TREE:\n";
			Set<Integer> rootNodes = this.getRoots();
			returnGraphString += "Number of root nodes: " + rootNodes.size() + "\n";

			for(Integer rootNode : rootNodes) {
				Integer nodeFeaturesNumber = this.getNodeFeatures(rootNode).size();
				returnGraphString += "ROOT NODE: '" + StringUtils.defaultIfEmpty(this.getNodeName(rootNode), "NO_NODE_NAME_DEFINED") + "' (id:" + rootNode + ", features: " + nodeFeaturesNumber + ")\n";
				returnGraphString += printSubtree(rootNode, 1);
			}
			break;
		}

		return returnGraphString;
	}

	private String printSubtree(Integer rootId, int treeDepth) {
		String retSubtreeString = "";

		if(treeDepth < 10 && rootId != null && this.getNodeNames(rootId).get(rootId) != null) {

			for(Entry<Integer, Pair<Integer, Integer>> outgoingEdge : this.getOutgoingEdges(rootId).entrySet()) {
				if(outgoingEdge != null && outgoingEdge.getKey() != null && outgoingEdge.getValue() != null) {
					Integer childId = outgoingEdge.getValue().getRight();
					Integer nodeFeaturesNumber = this.getNodeFeatures(childId).size();
					String nodeName = StringUtils.defaultIfEmpty(this.getNodeName(childId), "NO_NODE_NAME_DEFINED");
					String linkName = StringUtils.defaultIfEmpty(this.getEdgeName(outgoingEdge.getKey()), "NO_EDGE_NAME_DEFINED");
					String padding = "";
					for(int k = 0; k <= treeDepth; k++) {
						padding += "   ";
					}

					retSubtreeString += padding + "|-> " + linkName + " --> " + nodeName + "' (id:" + childId + ", features: " + nodeFeaturesNumber + ")\n";
					retSubtreeString += printSubtree(childId, new Integer(treeDepth) + 1);
				}
			}
		}

		return retSubtreeString;
	}

}
