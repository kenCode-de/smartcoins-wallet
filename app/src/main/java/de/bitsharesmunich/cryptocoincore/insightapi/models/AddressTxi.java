package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Represents the address txi of a insishgt api response
 */
public class AddressTxi {
    /**
     * The total number of items
     */
    public int totalItems;
    /**
     * The start index of the current txi
     */
    public int from;
    /**
     * the last index of the current txi
     */
    public int to;
    /**
     * The arrays of txi of this response
     */
    public Txi[] items;
    
}
