package de.bitsharesmunich.cryptocoincore.base;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * Created by Henry Varona on 19/3/2017.
 */
public class TransactionLog {

    public enum TransactionType {TRANSACTION_TYPE_BITSHARE, TRANSACTION_TYPE_BITCOIN};

    public TransactionType getType() {
        return type;
    }

    public TransactionType type;

    Object transaction;
    Object account;

    public TransactionLog(GeneralTransaction transaction, GeneralCoinAccount account){
        this.type = TransactionType.TRANSACTION_TYPE_BITCOIN;
        this.transaction = transaction;
        this.account = account;
    }

    public TransactionLog(HistoricalTransferEntry transaction, UserAccount userAccount){
        this.type = TransactionType.TRANSACTION_TYPE_BITSHARE;
        this.transaction = transaction;
        this.account = userAccount;
    }

    public GeneralTransaction getBitcoinTransactionLog(){
        return (GeneralTransaction) this.transaction;
    }

    public HistoricalTransferEntry getBitshareTransactionLog(){
        return (HistoricalTransferEntry) this.transaction;
    }

    public GeneralCoinAccount getBitcoinAccount(){
        return (GeneralCoinAccount) this.account;
    }

    public UserAccount getBitshareAccount(){
        return (UserAccount) this.account;
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
                return this.getBitshareTransactionLog().equals(objTransactionLog.getBitshareTransactionLog());
            case TRANSACTION_TYPE_BITCOIN:
                return this.getBitcoinTransactionLog().equals(objTransactionLog.getBitcoinTransactionLog());
            default:
                return false;
        }
    }

}
