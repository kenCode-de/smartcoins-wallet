package de.bitshares_munich.models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by developer on 5/24/16.
 */
public class TransactionDetails {
    public java.util.Date Date;
    public Boolean Sent; // false : if received
    public String To;
    public String From;
    public String Memo;
    public float Amount;
    public String assetSymbol;
    public float faitAmount;
    public String faitAssetSymbol;
    public String eReceipt;

    public TransactionDetails(Date _date, Boolean _Sent, String _to, String _from, String _memo, float _Amount,
                              String _assetSymbol, float _faitAmount, String _faitAssetSymbol , String _eReceipt)
    {
        this.Date = _date;
        this.Sent = _Sent;
        this.To = _to;
        this.From = _from;
        this.Memo = _memo;
        this.Amount = _Amount;
        this.assetSymbol = _assetSymbol;
        this.faitAmount = _faitAmount;
        this.faitAssetSymbol = _faitAssetSymbol;
        eReceipt = _eReceipt;
    }

    public Date getDate()
    {
        //SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        //String formattedDate = df.format(Calendar.getInstance().getTime());
        return this.Date;//Calendar.getInstance().getTime();
    }

    public String getDateString()
    {
        SimpleDateFormat df = new SimpleDateFormat("dd MMM");
        String formattedDate = df.format(this.Date);
        return formattedDate;//Calendar.getInstance().getTime();
    }

    public String getTimeString()
    {
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String formattedDate = df.format(this.Date);
        return formattedDate;//Calendar.getInstance().getTime();
    }

    public String getTimeZone()
    {
        //SimpleDateFormat df = new SimpleDateFormat("HH:mm a");
        //String formattedDate = df.format(this.Date);
        return "UTC";//Calendar.getInstance().getTime();
    }

    public Boolean getSent ()
    {
        return this.Sent;
    }

    public String getDetailsTo()
    {
        return this.To;
    }

    public String getDetailsFrom()
    {
        return this.From;
    }

    public String getDetailsMemo()
    {
        return this.Memo;
    }

    public float getAmount ()
    {
        return this.Amount;
    }

    public String getAssetSymbol(){
        return this.assetSymbol;
    }

    public float getFaitAmount() {
        return this.faitAmount;
    }

    public String getFaitAssetSymbol() {
        return this.faitAssetSymbol;
    }
}
