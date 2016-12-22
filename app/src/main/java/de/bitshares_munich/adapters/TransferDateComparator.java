package de.bitshares_munich.adapters;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;

/**
 * Created by nelson on 12/14/16.
 */
public class TransferDateComparator implements Comparator<HistoricalTransferEntry> {

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        return (int) (lhs.getTimestamp() - rhs.getTimestamp());
    }
}
