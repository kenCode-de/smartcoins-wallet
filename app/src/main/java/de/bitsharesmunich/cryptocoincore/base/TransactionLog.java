package de.bitsharesmunich.cryptocoincore.base;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * Created by Henry Varona on 19/3/2017.
 */

/**
 * Unification of HistoricalTransferEntry and GeneralTransaction in one class.
 */
public class TransactionLog {

    /**
     * The types of transactions supported
     */
    public enum TransactionType {TRANSACTION_TYPE_BITSHARE, TRANSACTION_TYPE_BITCOIN};

    /**
     * the type of transaction
     */
    public TransactionType type;
    /**
     * the transaction object. Could be a HistoricalTransferEntry or a GeneralTransaction
     */
    Object mTransaction;
    /**
     * the account object. Could be a UserAccount or a GeneralCoinAccount
     */
    Object mAccount;

    public TransactionLog(GeneralTransaction transaction, GeneralCoinAccount account){
        this.type = TransactionType.TRANSACTION_TYPE_BITCOIN;
        this.mTransaction = transaction;
        this.mAccount = account;
    }

    public TransactionLog(HistoricalTransferEntry transaction, UserAccount userAccount){
        this.type = TransactionType.TRANSACTION_TYPE_BITSHARE;
        this.mTransaction = transaction;
        this.mAccount = userAccount;
    }

    /**
     *
     * @return The transaction object as a GeneralTransaction
     */
    public GeneralTransaction getBitcoinTransactionLog(){
        return (GeneralTransaction) this.mTransaction;
    }

    /**
     *
     * @return The transaction object as a HistoricalTransferEntry
     */
    public HistoricalTransferEntry getBitshareTransactionLog(){
        return (HistoricalTransferEntry) this.mTransaction;
    }

    public TransactionType getType() {
        return type;
    }

    /**
     *
     * @return The account object as a GeneralCoinAccount
     */
    public GeneralCoinAccount getBitcoinAccount(){
        return (GeneralCoinAccount) this.mAccount;
    }

    /**
     *
     * @return The account object as a UserAccount
     */
    public UserAccount getBitshareAccount(){
        return (UserAccount) this.mAccount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())){
            return false;
        }
        TransactionLog objTransactionLog = (TransactionLog)obj;
        if (this.type != objTransactionLog.type) {
            return false;
        }

        switch(this.type){
            case TRANSACTION_TYPE_BITSHARE:
                return this.getBitshareTransactionLog().equals(objTransactionLog
                        .getBitshareTransactionLog());
            case TRANSACTION_TYPE_BITCOIN:
                return this.getBitcoinTransactionLog().equals(objTransactionLog
                        .getBitcoinTransactionLog());
            default:
                return false;
        }
    }

}
