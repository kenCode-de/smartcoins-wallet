package de.bitshares_munich.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.TabActivity;

/**
 * Created by developer on 6/28/16.
 */
public class BinHelper {

    private int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public ArrayList<Integer> getBytesFromBinFile(String filePath) {
        try {
            File file = new File(filePath);
            //byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));

            ArrayList<Integer> result = new ArrayList<>();


            for ( int i = 0 ; i < file.length() ; i++ )
            {
                int val = unsignedToBytes(dis.readByte());
                result.add(val);
            }

            dis.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }


    public void addWallet(AccountDetails accountDetail, String brainKey, String pinCode, Context context, Activity activity) {
        TinyDB tinyDB = new TinyDB(context);
        ArrayList<AccountDetails> accountDetailsList = tinyDB.getListObject(context.getString(R.string.pref_wallet_accounts), AccountDetails.class);
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.wif_key = accountDetail.wif_key;
        accountDetails.pinCode = pinCode;
        accountDetails.account_name = accountDetail.account_name;
        accountDetails.pub_key = accountDetail.pub_key;
        accountDetails.brain_key = brainKey;
        accountDetails.isSelected = true;
        accountDetails.status = "success";
        accountDetails.account_id = accountDetail.account_id;


        for (int i = 0; i < accountDetailsList.size(); i++) {
            if (accountDetailsList.get(i).account_name.equals(accountDetails.account_name)) {
                accountDetailsList.remove(i);
            }
        }

        for (int i = 0; i < accountDetailsList.size(); i++) {
            accountDetailsList.get(i).isSelected = false;
        }
        accountDetailsList.add(accountDetails);

        tinyDB.putListObject(context.getString(R.string.pref_wallet_accounts), accountDetailsList);

        List<TransactionDetails> emptyTransactions = new ArrayList<>();
        tinyDB.putTransactions(activity, context, context.getString(R.string.pref_local_transactions), new ArrayList<>(emptyTransactions));

    }

    public boolean saveBinFile ( String filePath , List<Integer> content, Activity _activity )
    {
        boolean success = false;
        try
        {
            PermissionManager Manager = new PermissionManager();
            Manager.verifyStoragePermissions(_activity);

            File file = new File(filePath);
            byte[] fileData = new byte[content.size()];

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            for ( int i = 0 ; i < content.size() ; i++ )
            {
                fileData[i] = content.get(i).byteValue();
            }

            bos.write(fileData);
            bos.flush();
            bos.close();

            success = true;
        }
        catch (Exception e)
        {

        }

        return success;
    }


}
