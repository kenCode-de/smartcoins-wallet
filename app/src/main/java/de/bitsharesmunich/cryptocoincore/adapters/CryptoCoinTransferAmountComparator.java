package de.bitsharesmunich.cryptocoincore.adapters;

import com.google.common.primitives.UnsignedLong;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;

/**
 * Created by henry on 12/14/16.
 */

public class CryptoCoinTransferAmountComparator implements Comparator<TransactionLog> {

    public Double getAmountFromTransactionLog(TransactionLog tl){
        switch(tl.getType()){
            case TRANSACTION_TYPE_BITSHARE:
                return tl.getBitshareTransactionLog().getHistoricalTransfer().getOperation().getTransferAmount().getAmount().doubleValue();
            case TRANSACTION_TYPE_BITCOIN:
                return tl.getBitcoinTransactionLog().getAccountBalanceChange();
        }

        return 0.0;
    }

    @Override
    public int compare(TransactionLog lhs, TransactionLog rhs) {
        Double lhsAmount = this.getAmountFromTransactionLog(lhs);
        Double rhsAmount = this.getAmountFromTransactionLog(rhs);
        return lhsAmount.compareTo(rhsAmount);
    }
}
