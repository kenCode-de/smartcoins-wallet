package de.bitshares_munich.models;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import de.bitshares_munich.smartcoinswallet.MerchantEmailActivity;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/30/16.
 */
public class MerchantEmail {
    HashMap<String,String> merchantEmail = new HashMap<String,String>();
    TinyDB tinyDB;
    Context context;

    public MerchantEmail(Context _context){
        context = _context;
        tinyDB = new TinyDB(_context);
    }

    public void saveMerchantEmail(String accountName,String email){
        merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
        merchantEmail.put(accountName,email);
        tinyDB.putHashmapObject("Merchant_Email",merchantEmail);
        saveInFile(false);
    }

    public String getMerchantEmail(String accountName){
        merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
        if(merchantEmail.containsKey(accountName))
        return merchantEmail.get(accountName);
        else return "";
    }
    public void saveInFile(Boolean isMerchant) {
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission== PackageManager.PERMISSION_GRANTED) {
            merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(getPath()));
                out.write(merchantEmail.toString());
                out.close();
                if (MerchantEmailActivity.backupBinDelegate != null && isMerchant) {
                    MerchantEmailActivity.backupBinDelegate.backupComplete(true);
                }
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }
    public void readFromFile(String path) {
        try {
            Boolean isImport = false;
            merchantEmail = (HashMap<String, String>) tinyDB.getHashmap("Merchant_Email");
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String string;
            while ((string = reader.readLine()) != null) {
                stringBuilder.append(string);
            }
            string = stringBuilder.toString();
            if(string.contains("}") && string.contains("{")){
                string = string.replace("}","");
                string = string.replace("{","");
                if(string.contains(",")){
                    String list[] = string.split(",");
                    for(String li : list){
                        if(li.contains("=")) {
                            String split[] = li.split("=");
                            merchantEmail.put(split[0].replaceAll(" ",""), split[1].replaceAll(" ",""));
                            isImport = true;
                        }
                    }
                }else {
                    if(string.contains("=")) {
                        String split[] = string.split("=");
                        merchantEmail.put(split[0].replaceAll(" ",""), split[1].replaceAll(" ",""));
                        isImport = true;
                    }
                }
            }
            reader.close();
            if(isImport) {
                tinyDB.putHashmapObject("Merchant_Email", merchantEmail);
                saveInFile(true);
            }else {
                if(MerchantEmailActivity.backupBinDelegate!=null) {
                    MerchantEmailActivity.backupBinDelegate.backupComplete(false);
                }
            }
        }catch(Exception i) {
            if(MerchantEmailActivity.backupBinDelegate!=null) {
                MerchantEmailActivity.backupBinDelegate.backupComplete(false);
            }
            i.printStackTrace();
            return;
        }
    }
    public static String getPath(){
        try {
            String folder = Environment.getExternalStorageDirectory() + File.separator + "SmartcoinsWallet";
            File folderPath = new File(folder);
            if (folderPath.exists()) {
                return folder + File.separator + "merchantemail" + ".txt";
            } else {
                if (folderPath.mkdir())
                    return folder + File.separator + "merchantemail" + ".txt";
                else return "";
            }
        }catch (Exception e){
                return "";
        }
    }
}

