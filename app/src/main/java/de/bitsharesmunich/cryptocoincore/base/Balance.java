package de.bitsharesmunich.cryptocoincore.base;

import java.util.Date;

/**
 * Created by henry on 05/02/2017.
 */

public class Balance {

    protected Coin type;
    protected Date date;

    protected double unconfirmedAmount;
    protected double confirmedAmount;
    protected int lessConfirmed;

    public Coin getType() {
        return type;
    }

    public void setType(Coin type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmmount() {
        return unconfirmedAmount + confirmedAmount;
    }

    public double getUnconfirmedAmount() {
        return unconfirmedAmount;
    }

    public void setUnconfirmedAmount(double unconfirmedAmount) {
        this.unconfirmedAmount = unconfirmedAmount;
    }

    public double getConfirmedAmount() {
        return confirmedAmount;
    }

    public void setConfirmedAmount(double confirmedAmount) {
        this.confirmedAmount = confirmedAmount;
    }

    public int getLessConfirmed() {
        return lessConfirmed;
    }

    public void setLessConfirmed(int lessConfirmed) {
        this.lessConfirmed = lessConfirmed;
    }
}
