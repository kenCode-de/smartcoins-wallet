package de.bitshares_munich.Interfaces;

import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.models.TransactionDetails;

/**
 * Created by Syed Muhammad Muzzammil on 5/19/16.
 */
public interface AssetDelegate {
    void isUpdate(ArrayList<String> id,ArrayList<String> sym , ArrayList<String>  pre,ArrayList<String>  am);
    void TransactionUpdate(List<TransactionDetails> transactionDetails,int nos);

}
