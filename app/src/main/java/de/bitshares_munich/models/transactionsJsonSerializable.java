package de.bitshares_munich.models;

import android.content.Context;

/**
 * Created by developer on 6/14/16.
 */
public class transactionsJsonSerializable {
    public java.util.Date Date;
    public Boolean Sent; // false : if received
    public String To;
    public String From;
    public String Memo;
    public double Amount;
    public String assetSymbol;
    public double faitAmount;
    public String faitAssetSymbol;
    public String eReceipt;
    private Context context;
}
