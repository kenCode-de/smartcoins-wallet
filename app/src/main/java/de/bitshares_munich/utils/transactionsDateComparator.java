package de.bitshares_munich.utils;

import java.util.Comparator;

import de.bitshares_munich.models.TransactionDetails;

/**
 * Created by developer on 8/9/16.
 */
public class transactionsDateComparator implements Comparator<TransactionDetails> {
    @Override
    public int compare(TransactionDetails o1, TransactionDetails o2) {
        return o2.Date.compareTo(o1.Date);
    }
}
