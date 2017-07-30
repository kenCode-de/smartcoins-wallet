package de.bitsharesmunich.cryptocoincore.base;

import java.io.Serializable;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Enum of the Coin type, defines several parameters of each coin
 */

public enum Coin implements Serializable{
    /**
     * Bitcoin main net
     */
    BITCOIN("BTC",8,6,R.mipmap.coin_icon_btc),
    /**
     * Bitcoin test net
     */
    BITCOIN_TEST("BTC",8,6,R.mipmap.coin_icon_btc),
    /**
     * Litecoin main net
     */
    LITECOIN("LTC",8,6,R.mipmap.coin_icon_litecoin),
    /**
     * Dash main net
     */
    DASH("DASH",8,6,R.mipmap.coin_icon_dash),
    /**
     * Dogecoin main net
     */
    DOGECOIN("DOGE",8,6,R.mipmap.coin_icon_doge),
    /**
     * Bitshaers
     */
    BITSHARE("BTS",8,6,R.mipmap.coin_icon_bitshare);

    /**
     * A label that define the coin
     */
    protected String mLabel;
    /**
     * The scale of the precision
     */
    protected int mPrecision;
    /**
     * Confirmations needed for verified transaction
     */
    protected int mConfirmationsNeeded;
    /**
     * the icon to use
     */
    protected int mIcon;


    Coin(String label, int precision, int confirmationsNeeded, int icon){
        this.mLabel = label;
        this.mPrecision = precision;
        this.mConfirmationsNeeded = confirmationsNeeded;
        this.mIcon = icon;
    }

    public String getLabel(){
        return this.mLabel;
    }
    public int getPrecision(){
        return this.mPrecision;
    }
    public int getConfirmationsNeeded(){
        return this.mConfirmationsNeeded;
    }
    public int getIcon(){
        return this.mIcon;
    }




}
