package de.bitshares_munich.models;

import android.content.Context;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.bitshares_munich.smartcoinswallet.BaseActivity;
import de.bitshares_munich.utils.Helper;

/**
 * Created by developer on 5/24/16.
 */
public class TransactionDetails
{
    public String id;
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

    public void updateContext(Context _context)
    {
        context = _context;
    }

    public TransactionDetails()
    {

    }

    public TransactionDetails(String _id,Date _date, Boolean _Sent, String _to, String _from, String _memo, double _Amount,
                              String _assetSymbol, double _faitAmount, String _faitAssetSymbol , String _eReceipt)
    {
        this.id = _id;
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
        /*SimpleDateFormat df = new SimpleDateFormat("dd MMM");
        String formattedDate = df.format(this.Date);
        return formattedDate;//Calendar.getInstance().getTime();*/
        return Helper.convertDateToGMT(this.Date,context);

    }

    public String getDateStringWithYear()
    {
        return Helper.convertDateToGMTWithYear(this.Date,context);

    }

    public String getTimeString()
    {
     /*   SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String formattedDate = df.format(this.Date);
        return formattedDate;//Calendar.getInstance().getTime();*/
        return Helper.convertTimeToGMT(this.Date,context);
    }

    public String getTimeZone()
    {
       /* //SimpleDateFormat df = new SimpleDateFormat("HH:mm a");
        //String formattedDate = df.format(this.Date);
        return "UTC";//Calendar.getInstance().getTime();*/
        return Helper.convertTimeZoneToGMT(this.Date,context);
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

    public double getAmount ()
    {
        return this.Amount;
    }

    public String getAssetSymbol(){
        return this.assetSymbol;
    }

    public double getFaitAmount() {
        return this.faitAmount;
    }

    public String getFaitAssetSymbol() {
        return this.faitAssetSymbol;
    }
}
