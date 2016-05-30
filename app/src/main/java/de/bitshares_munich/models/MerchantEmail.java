package de.bitshares_munich.models;

import android.content.Context;

import java.util.HashMap;

import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/30/16.
 */
public class MerchantEmail {
    HashMap<String,String> merchantEmail = new HashMap<String,String>();
    TinyDB tinyDB;

    public MerchantEmail(Context _context){
        tinyDB = new TinyDB(_context);
    }

    public void saveMerchantEmail(String accountName,String email){
        merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
        merchantEmail.put(accountName,email);
        tinyDB.putHashmapObject("Merchant_Email",merchantEmail);
    }

    public String getMerchantEmail(String accountName){
        merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
        if(merchantEmail.containsKey(accountName))
        return merchantEmail.get(accountName);
        else return "";
    }
}
