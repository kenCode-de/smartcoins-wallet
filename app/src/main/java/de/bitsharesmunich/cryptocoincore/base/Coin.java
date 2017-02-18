package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by henry on 05/02/2017.
 */

public enum Coin {
    BITCOIN(8,6), BITCOIN_TEST(8,6), LITECOIN(8,6), DASH(8,6), DOGECOIN(8,6), BITSHARE(8,6);

    protected int precision;
    protected int confirmationsNeeded;

    Coin(int precision, int confirmationsNeeded){
        this.precision = precision;
        this.confirmationsNeeded = confirmationsNeeded;
    }

    public int getPrecision(){
        return this.precision;
    }
    public int getConfirmationsNeeded(){
        return this.confirmationsNeeded;
    }
}
