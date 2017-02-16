package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by henry on 05/02/2017.
 */

public enum Coin {
    BITCOIN(8), BITCOIN_TEST(8), LITECOIN(8), DASH(8), DOGECOIN(8), BITSHARE(8);

    protected int precision;

    Coin(int precision){
        this.precision = precision;
    }

    public int getPrecision(){
        return this.precision;
    }
}
