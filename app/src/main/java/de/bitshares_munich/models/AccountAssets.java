package de.bitshares_munich.models;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Syed Muhammad Muzzammil on 5/19/16.
 */


public class AccountAssets {
    public String id;
    public String precision;
    public String account_id;
    public String symbol;
    public String account;
    public String ammount;
    private String TAG = "AccountAssets";

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize id");
        }
        try {
            obj.put("precision", precision);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize precision");
        }
        try {
            obj.put("account_id", account_id);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize account_id");
        }
        try {
            obj.put("symbol", symbol);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize symbol");
        }
        try {
            obj.put("account", account);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize account");
        }
        try {
            obj.put("ammount", ammount);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while trying to serialize ammount");
        }
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}