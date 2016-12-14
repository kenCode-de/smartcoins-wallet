package de.bitshares_munich.adapters;

import com.luminiasoft.bitshares.models.HistoricalTransfer;

import java.util.Comparator;

/**
 * Created by nelson on 12/14/16.
 */
public class TransferDateComparator implements Comparator<HistoricalTransfer> {

    @Override
    public int compare(HistoricalTransfer lhs, HistoricalTransfer rhs) {
        return (int) (lhs.getTimestamp() - rhs.getTimestamp());
    }
}
