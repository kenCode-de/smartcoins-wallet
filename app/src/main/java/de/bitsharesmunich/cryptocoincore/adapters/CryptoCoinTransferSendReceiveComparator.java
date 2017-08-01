package de.bitsharesmunich.cryptocoincore.adapters;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * Created by henry on 12/14/16.
 */
public class CryptoCoinTransferSendReceiveComparator implements Comparator<TransactionLog> {
    private String TAG = this.getClass().getName();

    @Override
    public int compare(TransactionLog lhs, TransactionLog rhs) {
        switch (lhs.getType()){

            case TRANSACTION_TYPE_BITSHARE:
                TransferOperation lhsOperation = lhs.getBitshareTransactionLog().getHistoricalTransfer().getOperation();
                boolean isOutgoing = lhsOperation.getFrom().getObjectId().equals(lhs.getBitshareAccount().getObjectId());
                if(isOutgoing)
                    return -1;
                else
                    return 1;
            case TRANSACTION_TYPE_BITCOIN:
                if(lhs.getBitcoinTransactionLog().getAccountBalanceChange() < 0)
                    return -1;
                else
                    return 1;
        }

        return 0;
    }
}
