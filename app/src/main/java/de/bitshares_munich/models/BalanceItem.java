package de.bitshares_munich.models;

import android.widget.TextView;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItem {
    String symbol;
    String precision;
    String ammount;
    TextView symbolTextView;
    TextView ammountTextView;

    public BalanceItem(String symbol, String precision, String ammount){
        this.symbol = symbol;
        this.precision = precision;
        this.ammount = ammount;
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

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public void setPrecision(String precision){
        this.precision = precision;
    }

    public void setAmmount(String ammount){
        this.ammount = ammount;
    }

    public BalanceItem clone(){
        BalanceItem item = new BalanceItem(this.symbol, this.precision, this.ammount);
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