package de.bitshares_munich.models;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by qasim on 5/18/16.
 */
public class AccountDetails {
    private final String TAG = "AccountDetails";
    public static final int PRE_SECURITY_UPDATE = 0;
    public static final int POST_SECURITY_UPDATE = 1;

    public String status;
    public String brain_key;
    public String address;
    public String account_id;
    public String pub_key;
    public String wif_key;
    public String msg;
    public String pinCode;
    public int posBackupAsset;
    public ArrayList<AccountAssets> AccountAssets;
    public Boolean isSelected;
    public Boolean isLifeTime=false;
    public String account_name;
    public int securityUpdateFlag;

    @Override
    public String toString(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("status", status);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize status");
        }
        try {
            obj.put("brain_key", brain_key);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize brain_key");
        }
        try {
            obj.put("address", address);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize address");
        }
        try {
            obj.put("account_id", account_id);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize account_id");
        }
        try {
            obj.put("pub_key", pub_key);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize pub_key");
        }
        try {
            obj.put("wif_key", wif_key);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize wif_key");
        }
        try {
            obj.put("msg", msg);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize msg");
        }
        try {
            obj.put("pinCode", pinCode);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize pinCode");
        }
        try {
            obj.put("posBackupAsset", posBackupAsset);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize posBackupAsset");
        }
        JSONArray array = new JSONArray();
        if(AccountAssets != null){
            for(AccountAssets asset : AccountAssets){
                array.put(asset);
            }
            try {
                obj.put("AccountAssets", array);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException while trying to serialize AccountAssets");
            }
        }
        try {
            obj.put("isSelected", isSelected);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize isSelected");
        }
        try {
            obj.put("isLifeTime", isLifeTime);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize isLifeTime");
        }
        try {
            obj.put("account_name", account_name);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize account_name");
        }
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
