package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * Created by henry on 13/02/2017.
 */

public class Txi {
    public String txid;
    public int version;
    public long locktime;
    public Vin[] vin;
    public Vout[] vout;
    public String blockhash;
    public long blockheight;
    public int confirmations;
    public long time;
    public long blocktime;
    public double valueOut;
    public int size;
    public double valueIn;
    public double fee;
}
