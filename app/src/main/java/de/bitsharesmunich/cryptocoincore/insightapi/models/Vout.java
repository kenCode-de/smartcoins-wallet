package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Created by henry on 13/02/2017.
 */

public class Vout {
    public double value;
    public int n;
    public ScriptPubKey scriptPubKey;
    public String spentTxId;
    public String spentIndex;
    public String spentHeight;
}
