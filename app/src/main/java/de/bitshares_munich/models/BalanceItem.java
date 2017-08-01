package de.bitshares_munich.models;

import android.widget.TextView;

import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItem {
    String symbol;
    String precision;
    String ammount;
    Coin coin;

    int confirmations;
    String fait;
    TextView symbolTextView;
    TextView ammountTextView;

    public BalanceItem(Coin coin, String symbol, String precision, String ammount){
        this.symbol = symbol;
        this.precision = precision;
        this.ammount = ammount;
        this.confirmations = -1;
        this.fait = "";
        this.coin = coin;
    }

    public BalanceItem(Coin coin, String symbol, String precision, String ammount, int confirmations){
        this.symbol = symbol;
        this.precision = precision;
        this.ammount = ammount;
        this.confirmations = confirmations;
        this.fait = "";
        this.coin = coin;
    }

    public Coin getCoin(){
        return this.coin;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public String getPrecision(){
        return this.precision;
    }

    public String getAmmount(){
        return this.ammount;
    }

    public String getFait(){
        return this.fait;
    }

    public void setCoin(Coin coin){
        this.coin = coin;
    }

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public void setPrecision(String precision){
        this.precision = precision;
    }

    public void setAmmount(String ammount){
        this.ammount = ammount;
    }

    public void setFait(String fait){
        this.fait = fait;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public BalanceItem clone(){
        BalanceItem item = new BalanceItem(this.coin, this.symbol, this.precision, this.ammount);
        return item;
    }

    public void setSymbolTextView(TextView symbolTextView){
        this.symbolTextView = symbolTextView;
    }

    public void setAmmountTextView(TextView ammountTextView){
        this.ammountTextView = ammountTextView;
    }

    public TextView getSymbolTextView(){
        return this.symbolTextView;
    }

    public TextView getAmmountTextView(){
        return this.ammountTextView;
    }
}