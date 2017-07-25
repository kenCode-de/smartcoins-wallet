package de.bitsharesmunich.cryptocoincore.base;

/**
 * Interface to interact with the balance change of a coin
 */
public interface ChangeBalanceListener {
    /**
     * This metods is invoke when a balance of a coin is changed, see {@link Balance}
     */
    void balanceChange(Balance balance);
}
