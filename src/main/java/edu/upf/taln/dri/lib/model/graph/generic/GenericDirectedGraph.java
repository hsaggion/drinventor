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

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Graph interaction methods to interact with directed graphs
 * 
 *
 */
public interface GenericDirectedGraph {

	// Graph population
	/**
	 * Add a new node
	 * 
	 * @param nodeId if null the first available id will be chosen for this node (the ID of the node is unambiguous graph-wide)
	 * @param nodeName each node should have a name (leaving null or empty will not create the node)
	 * @return the id of the new node. If null the new node has not been added
	 */
	public Integer addNode(Integer nodeId, String nodeName);
	
	/**
	 * Set a new name of an existing node
	 * @param nodeId
	 * @param nodeName not null or empty
	 * @return
	 */
	public boolean changeNodeName(Integer nodeId, String nodeName);
	
	/**
	 * Remove a node together with all its incident edges
	 * 
	 * @param nodeId
	 * @return
	 */
	public boolean removeNode(Integer nodeId);
	
	/**
	 * Remove an edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public boolean removeEdge(Integer edgeId);

	/**
	 * Add a new edge (directed)
	 * 
	 * @param from the id of the starting node
	 * @param to the id of the ending node
	 * @param edgeName each node should have a name (leaving null or empty will not create the node)
	 * @return
	 */
	public Integer addEdge(Integer from, Integer to, String edgeName);

	/**
	 * Change the name of an existing edge - the new name should not be empty or null
	 * 
	 * @param edgeId
	 * @param edgeName
	 * @return
	 */
	public boolean changeEdgeName(Integer edgeId, String edgeName);
	
	/**
	 * Add the named feature to the node. If a node feature with this name is present, the feature is overwritten.
	 * 
	 * @param nodeId the id of the node
	 * @param featureName can't be null or empty
	 * @param featureValue
	 * @return true if the feature is added
	 */
	public boolean addNodeFeature(Integer nodeId, String featureName, Object featureValue);

	/**
	 * Add the named feature to the edge. If an edge feature with this name is present, the feature is overwritten.
	 * 
	 * @param nodeId the id of the node
	 * @param featureName can't be null or empty
	 * @param featureValue
	 * @return true if the feature is added
	 */
	public boolean addEdgeFeature(Integer edgeId, String featureName, Object featureValue);
	
	/**
	 * Remove all edges with a specific source node id / destination node id / name
	 * At least one of source node Id / destination node Id / name has not to be blank / empty.
	 * (AND OF source id, destination id and kind checks)
	 * 
	 * @param sourceId if not blank only the edges with this source id are removed
	 * @param destinationId if not blank only the edges with this destination id are removed
	 * @param kind if not blank only the edges of this kind are removed
	 * @return number of edges removed
	 */
	public int removeEdgesByNameSourceAndDestination(Integer sourceId, Integer destinationId, String name);

	// Graph browsing
	/**
	 * Get the set of nodes with with a specific name
	 * 
	 * @param name
	 * @return
	 */
	public Set<Integer> getNodesByName(String name);
	
	/**
	 * Map with id (key) and name of graph nodes
	 * 
	 * @param nodeId If null all the node names are retrieved. Otherwise only the name of the node with the specified id, if present. 
	 * @return
	 */
	public Map<Integer, String> getNodeNames(Integer nodeId);
	
	/**
	 * Name of graph node
	 * 
	 * @param nodeId
	 * @return
	 */
	public String getNodeName(Integer nodeId);
	
	/**
	 * Get the set of edges with with a specific name
	 * 
	 * @param name
	 * @return
	 */
	public Set<Integer> getEdgesByName(String name);
	
	/**
	 * Map with id (key) and name of graph edges
	 * 
	 * @param edgeId If null all the edge names are retrieved. Otherwise only the name of the edge with the specified id, if present. 
	 * @return
	 */
	public Map<Integer, String> getEdgeNames(Integer edgeId);
	
	/**
	 * Name of graph edges
	 * 
	 * @param edgeId
	 * @return
	 */
	public String getEdgeName(Integer edgeId);
	
	/**
	 * Number of nodes
	 * 
	 * @return
	 */
	public Integer getNodeCount();
	
	/**
	 * Number of edges
	 * 
	 * @return
	 */
	public Integer getEdgeCount();
	
	/**
	 * Get the ids of all the children of a node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getChildrenNodes(Integer nodeId);
	
	/**
	 * Get the number of children of the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Integer getChildrenNodesCount(Integer nodeId);
	
	/**
	 * Get the ids of all the parents of a node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getParentNodes(Integer nodeId);
	
	/**
	 * Get the number of parents of the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Integer getParentNodesCount(Integer nodeId);
	
	/**
	 * Get the ids of root nodes
	 * 
	 * @return
	 */
	public Set<Integer> getRoots();
	
	/**
	 * Get the set of edges entering in the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getIncidentEdges(Integer nodeId);
	
	/**
	 * Get the set of edges going out from the node
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getOutgoingEdges(Integer nodeId);
	
	/**
	 * Map with id (key) of edges and pair of id of the nodes each vertex connects 
	 * 
	 * @param edgeId
	 * @return
	 */
	public Map<Integer, Pair<Integer, Integer>> getAllEdgeConnections();
	
	/**
	 * Get the named node feature map
	 * 
	 * @param nodeId
	 * @return
	 */
	public Map<String, Object> getNodeFeatures(Integer nodeId);
	
	/**
	 * Get the named edge feature map
	 * 
	 * @param edgeId
	 * @return
	 */
	public Map<String, Object> getEdgeFeatures(Integer edgeId);
	
	/**
	 * Get edges by source node id / destination node id / name
	 * At least on of source node Id / destination node Id / name has not to be blank / empty.
	 * (AND OF source node id, destination node id and name checks)
	 * 
	 * @param edgeList list of edges to filter
	 * @param sourceId
	 * @param destinationId
	 * @param name
	 * @return filtered edges list
	 */
	public Set<Integer> getEdgesByKindSourceAndDestination(Integer sourceId, Integer destinationId, String name);
	
	/**
	 * Serialize the graph contents as a string
	 * 
	 * @param outputType
	 * @return
	 */
	public String graphAsString(GraphToStringENUM outputType);
	
}
