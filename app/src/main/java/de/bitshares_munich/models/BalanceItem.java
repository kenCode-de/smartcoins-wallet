package de.bitshares_munich.models;

import android.widget.TextView;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItem {
    String symbol;
    String precision;
    String amount;
    String fiat;
    TextView symbolTextView;
    TextView amountTextView;

    public BalanceItem(String symbol, String precision, String amount) {
        this.symbol = symbol;
        this.precision = precision;
        this.amount = amount;
        this.fiat = "";
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getPrecision() {
        return this.precision;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getFiat() {
        return this.fiat;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setFiat(String fiat) {
        this.fiat = fiat;
    }

    public BalanceItem clone() {
        BalanceItem item = new BalanceItem(this.symbol, this.precision, this.amount);
        return item;
    }

    public void setSymbolTextView(TextView symbolTextView) {
        this.symbolTextView = symbolTextView;
    }

    public void setAmountTextView(TextView amountTextView) {
        this.amountTextView = amountTextView;
    }

    public TextView getSymbolTextView() {
        return this.symbolTextView;
    }

    public TextView getAmountTextView() {
        return this.amountTextView;
    }
}