package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Created by henry on 13/02/2017.
 */

public class Vin {
    public String txid;
    public int vout;
    public long sequence;
    public int n;
    public ScriptSig scriptSig;
    public String addr;
    public long valueSat;
    public double value;
    public String doubleSpentTxID;
}
