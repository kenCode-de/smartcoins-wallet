package de.bitsharesmunich.cryptocoincore.base;

import java.io.Serializable;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by henry on 05/02/2017.
 */

public enum Coin implements Serializable{
    BITCOIN("BTC",8,6,R.mipmap.coin_icon_btc), BITCOIN_TEST("BTC",8,6,R.mipmap.coin_icon_btc), LITECOIN("LTC",8,6,R.mipmap.coin_icon_litecoin), DASH("DASH",8,6,R.mipmap.coin_icon_dash), DOGECOIN("DOGE",8,6,R.mipmap.coin_icon_doge), BITSHARE("BTS",8,6,R.mipmap.coin_icon_bitshare);

    protected String label;
    protected int precision;
    protected int confirmationsNeeded;
    protected int icon;

    Coin(String label, int precision, int confirmationsNeeded, int icon){
        this.label = label;
        this.precision = precision;
        this.confirmationsNeeded = confirmationsNeeded;
        this.icon = icon;
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
    public int getIcon(){
        return this.icon;
    }
}
