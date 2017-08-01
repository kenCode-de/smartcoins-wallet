package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Represents one transaction input of the insight API
 */

public class Txi {
    /**
     * The id of this transaction
     */
    public String txid;
    /**
     * the version number of this transaction
     */
    public int version;
    /**
     * Time to hold this transaction
     */
    public long locktime;
    /**
     * The array of the transaction inputs
     */
    public Vin[] vin;
    /**
     * the array of the transactions outputs
     */
    public Vout[] vout;
    /**
     * this block hash
     */
    public String blockhash;
    /**
     * The blockheight where this transaction belongs, if 0 this transactions hasn't be included in any block yet
     */
    public int blockheight;
    /**
     * Number of confirmations
     */
    public int confirmations;
    /**
     * The time of the first broadcast fo this transaction
     */
    public long time;
    /**
     * The time which this transaction was included
     */
    public long blocktime;
    /**
     * Total value to transactions outputs
     */
    public double valueOut;
    /**
     * The size in bytes
     */
    public int size;
    /**
     * Total value of transactions inputs
     */
    public double valueIn;
    /**
     * Fee of this transaction has to be valueIn - valueOut
     */
    public double fee;
    /**
     * This is only for dash, is the instantsend state
     */
    public boolean txlock=false;

}
