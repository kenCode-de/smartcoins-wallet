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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrecision() {
        return this.precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFiat() {
        return this.fiat;
    }

    public void setFiat(String fiat) {
        this.fiat = fiat;
    }

    public BalanceItem clone() {
        BalanceItem item = new BalanceItem(this.symbol, this.precision, this.amount);
        return item;
    }

    public TextView getSymbolTextView() {
        return this.symbolTextView;
    }

    public void setSymbolTextView(TextView symbolTextView) {
        this.symbolTextView = symbolTextView;
    }

    public TextView getAmountTextView() {
        return this.amountTextView;
    }

    public void setAmountTextView(TextView amountTextView) {
        this.amountTextView = amountTextView;
    }
}