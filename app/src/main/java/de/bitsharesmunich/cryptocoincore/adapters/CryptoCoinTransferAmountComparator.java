package de.bitsharesmunich.cryptocoincore.adapters;

import com.google.common.primitives.UnsignedLong;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;

/**
 * Created by henry on 12/14/16.
 */

public class CryptoCoinTransferAmountComparator implements Comparator<GeneralTransaction> {

    @Override
    public int compare(GeneralTransaction lhs, GeneralTransaction rhs) {
        Double lhsAmount = lhs.getAccountBalanceChange();
        Double rhsAmount = rhs.getAccountBalanceChange();
        return lhsAmount.compareTo(rhsAmount);
    }
}
