package de.bitsharesmunich.cryptocoincore.adapters;

import java.util.Comparator;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;

/**
 * Created by henry on 12/14/16.
 */
public class CryptoCoinTransferDateComparator implements Comparator<GeneralTransaction> {

    @Override
    public int compare(GeneralTransaction lhs, GeneralTransaction rhs) {
        return lhs.getDate().compareTo(rhs.getDate());
    }
}
