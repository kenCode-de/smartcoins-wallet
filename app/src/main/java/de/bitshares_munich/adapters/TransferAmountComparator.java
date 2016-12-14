package de.bitshares_munich.adapters;

import com.luminiasoft.bitshares.models.HistoricalTransfer;

import java.util.Comparator;

/**
 * Created by nelson on 12/14/16.
 */

public class TransferAmountComparator implements Comparator<HistoricalTransfer> {

    @Override
    public int compare(HistoricalTransfer lhs, HistoricalTransfer rhs) {
        return lhs.getOperation().getTransferAmount().getAmount().compareTo(rhs.getOperation().getTransferAmount().getAmount());
    }
}
