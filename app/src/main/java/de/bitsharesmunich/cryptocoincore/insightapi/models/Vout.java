package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Represents a Transasction output
 */

public class Vout {
    /**
     * The amount of coin
     */
    public double value;
    /**
     * the order of this transaciton output on the transaction
     */
    public int n;
    /**
     * The script public key
     */
    public ScriptPubKey scriptPubKey;
    /**
     * If this transaciton output was spent what txid it belongs
     */
    public String spentTxId;
    /**
     * The index on the transaction that this transaction was spent
     */
    public String spentIndex;
    /**
     * The block height of the transaction this output was spent
     */
    public String spentHeight;
}
