package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by henry on 05/02/2017.
 */

public enum Coin {
    BITCOIN("BTC",8,6), BITCOIN_TEST("BTC",8,6), LITECOIN("LTC",8,6), DASH("DASH",8,6), DOGECOIN("DOGE",8,6), BITSHARE("BTS",8,6);

    protected String label;
    protected int precision;
    protected int confirmationsNeeded;

    Coin(String label, int precision, int confirmationsNeeded){
        this.label = label;
        this.precision = precision;
        this.confirmationsNeeded = confirmationsNeeded;
    }

    public String getLabel(){
        return this.label;
    }
    public int getPrecision(){
        return this.precision;
    }
    public int getConfirmationsNeeded(){
        return this.confirmationsNeeded;
    }
}
