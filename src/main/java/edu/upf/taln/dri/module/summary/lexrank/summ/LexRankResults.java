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
package edu.upf.taln.dri.module.summary.lexrank.summ;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;

/** A dumb container class that holds results from the LexRank algorithm. */
public class LexRankResults<T> {
    /** The results, sorted in order of LexRank score */
    public List<T> rankedResults;
    /** A mapping from each element to its LexRank score */
    public Map<T, Double> scores;
    /**
     * A mapping from each element to its neighbors in the thresholded
     * connectivity graph.
     */
    public Map<T, List<T>> neighbors;

    public LexRankResults() {
        rankedResults = new ArrayList<T>();
        scores = new HashMap<T, Double>();
        neighbors = new HashMap<T, List<T>>();
    }

}