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

/**
 * Just a dumb item to test whether or not LexRank actually works.
 * It simulates having a similarity function by just reading a value out of some
 * similarity matrix. If you're using multiple of these, it's probably best to
 * make sure they all point to the same matrix, or hilarity could ensue.
 */
public class DummyItem implements Similar<DummyItem> {
    public int id;
    double[][] similarityMatrix;
    public DummyItem(int id, double[][] similarityMatrix) {
        this.id = id;
        this.similarityMatrix = similarityMatrix;
    }
    public double similarity(DummyItem other) {
        return similarityMatrix[id][other.id];
    }
}