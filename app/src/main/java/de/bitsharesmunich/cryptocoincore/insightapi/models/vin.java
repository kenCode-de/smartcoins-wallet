package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * This represents a transaction input
 */

public class Vin {
    /**
     * The original transaction id where this transaction is an output
     */
    public String txid;
    /**
     *
     */
    public int vout;
    /**
     * Sequence fo the transaction
     */
    public long sequence;
    /**
     * Order of the transasction input on the transasction
     */
    public int n;
    /**
     * The script signature
     */
    public ScriptSig scriptSig;
    /**
     * The addr of this transaction
     */
    public String addr;
    /**
     * Value in satoshi
     */
    public long valueSat;
    /**
     * Calue of this transaction
     */
    public double value;
    /**
     *
     */
    public String doubleSpentTxID;
}
