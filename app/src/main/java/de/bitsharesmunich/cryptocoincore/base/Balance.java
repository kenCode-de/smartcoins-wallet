package de.bitsharesmunich.cryptocoincore.base;

import java.util.Date;

/**
 * Calculates a balance of an asset of a coin type. Normally the asset is the same coin.
 */

public class Balance {
    /**
     * The coin type
     */
    protected Coin mType;
    /**
     * The date of the balance
     */
    protected Date date;
    /**
     * The uncofirmed amount if applies
     */
    protected double unconfirmedAmount;
    /**
     * The confirmed amount
     */
    protected double confirmedAmount;
    /**
     * The amount of min confirmation this balance has
     */
    protected int lessConfirmed;

    /**
     * Gets the coin type
     */
    public Coin getType() {
        return mType;
    }

    /**
     * Setter of the coin type
     */
    public void setType(Coin type) {
        this.mType = type;
    }

    /**
     * Getter of the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter of the date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets the current avaible amount
     * @return The current avaible amount
     */
    public double getAmmount() {
        return unconfirmedAmount + confirmedAmount;
    }

    /**
     * Getter of unconfirmed amount
     */
    public double getUnconfirmedAmount() {
        return unconfirmedAmount;
    }

    /**
     * Setter of  uncofirmed Amount
     */
    public void setUnconfirmedAmount(double unconfirmedAmount) {
        this.unconfirmedAmount = unconfirmedAmount;
    }

    /**
     * Getter of confirmed amount
     */
    public double getConfirmedAmount() {
        return confirmedAmount;
    }

    /**
     * Setter of confirmed Amount
     */
    public void setConfirmedAmount(double confirmedAmount) {
        this.confirmedAmount = confirmedAmount;
    }

    public int getLessConfirmed() {
        return lessConfirmed;
    }

    /**
     * Setter of less confirmed
     * this is the amount of the less confirmed transaction
     */
    public void setLessConfirmed(int lessConfirmed) {
        this.lessConfirmed = lessConfirmed;
    }
}
