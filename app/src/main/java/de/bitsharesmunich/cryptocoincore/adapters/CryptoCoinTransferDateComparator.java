package de.bitsharesmunich.cryptocoincore.adapters;

import java.util.Comparator;
import java.util.Date;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;

/**
 * Created by henry on 12/14/16.
 */
public class CryptoCoinTransferDateComparator implements Comparator<TransactionLog> {

    public long getTimestampFromTransactionLog(TransactionLog tl){
        switch (tl.getType()){
            case TRANSACTION_TYPE_BITSHARE:
                return tl.getBitshareTransactionLog().getTimestamp();
            case TRANSACTION_TYPE_BITCOIN:
                return tl.getBitcoinTransactionLog().getDate().getTime();
        }

        return 0;
    }

    @Override
    public int compare(TransactionLog lhs, TransactionLog rhs) {
        long firstTimestamp = getTimestampFromTransactionLog(lhs);
        long secondTimestamp = getTimestampFromTransactionLog(rhs);

        if ((firstTimestamp - secondTimestamp) < 0){
            return -1;
        } else if (firstTimestamp == secondTimestamp){
            return 0;
        } else {
            return 1;
        }
    }
}
