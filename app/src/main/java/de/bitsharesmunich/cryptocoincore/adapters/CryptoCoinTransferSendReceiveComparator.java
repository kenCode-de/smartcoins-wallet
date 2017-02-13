package de.bitsharesmunich.cryptocoincore.adapters;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * Created by henry on 12/14/16.
 */
public class CryptoCoinTransferSendReceiveComparator implements Comparator<GeneralTransaction> {
    private String TAG = this.getClass().getName();

    @Override
    public int compare(GeneralTransaction lhs, GeneralTransaction rhs) {
        if(lhs.getAccountBalanceChange() < 0)
            return -1;
        else
            return 1;
    }
}
