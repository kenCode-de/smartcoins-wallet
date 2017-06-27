package de.bitshares_munich.adapters;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.operations.TransferOperation;

/**
 * Created by nelson on 12/14/16.
 */
public class TransferSendReceiveComparator implements Comparator<HistoricalTransferEntry> {
    private String TAG = this.getClass().getName();

    private UserAccount me;

    public TransferSendReceiveComparator(UserAccount userAccount) {
        this.me = userAccount;
    }

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        TransferOperation lhsOperation = lhs.getHistoricalTransfer().getOperation();
        boolean isOutgoing = lhsOperation.getFrom().getObjectId().equals(me.getObjectId());
        if (isOutgoing)
            return -1;
        else
            return 1;
    }
}
