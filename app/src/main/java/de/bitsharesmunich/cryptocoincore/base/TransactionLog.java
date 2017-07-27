package de.bitsharesmunich.cryptocoincore.base;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * Created by Henry Varona on 19/3/2017.
 */
public class TransactionLog {

    /**
     * The types of transactions supported
     */
    public enum TransactionType {TRANSACTION_TYPE_BITSHARE, TRANSACTION_TYPE_BITCOIN};

    /**
     * The type of transasction
     */
    public TransactionType type;
    /**
     *  
     */
    Object mTransaction;
    /**
     *
     */
    Object mAccount;

    /**
     *
     * @param transaction
     * @param account
     */
    public TransactionLog(GeneralTransaction transaction, GeneralCoinAccount account){
        this.type = TransactionType.TRANSACTION_TYPE_BITCOIN;
        this.mTransaction = transaction;
        this.mAccount = account;
    }

    /**
     *
     * @param transaction
     * @param userAccount
     */
    public TransactionLog(HistoricalTransferEntry transaction, UserAccount userAccount){
        this.type = TransactionType.TRANSACTION_TYPE_BITSHARE;
        this.mTransaction = transaction;
        this.mAccount = userAccount;
    }

    /**
     *
     * @return
     */
    public GeneralTransaction getBitcoinTransactionLog(){
        return (GeneralTransaction) this.mTransaction;
    }

    /**
     *
     * @return
     */
    public HistoricalTransferEntry getBitshareTransactionLog(){
        return (HistoricalTransferEntry) this.mTransaction;
    }

    /**
     * Getter of the type of transaction
     */
    public TransactionType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public GeneralCoinAccount getBitcoinAccount(){
        return (GeneralCoinAccount) this.mAccount;
    }

    /**
     *
     * @return
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
