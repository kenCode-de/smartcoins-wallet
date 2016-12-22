package de.bitshares_munich.adapters;

import com.google.common.primitives.UnsignedLong;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;

/**
 * Created by nelson on 12/14/16.
 */

public class TransferAmountComparator implements Comparator<HistoricalTransferEntry> {

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        UnsignedLong lhsAmount = lhs.getHistoricalTransfer().getOperation().getTransferAmount().getAmount();
        UnsignedLong rhsAmount = rhs.getHistoricalTransfer().getOperation().getTransferAmount().getAmount();
        return lhsAmount.compareTo(rhsAmount);
    }
}
