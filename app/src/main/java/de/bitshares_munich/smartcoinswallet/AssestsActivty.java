package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by afnan on 5/19/16.
 */
public class AssestsActivty  implements BalancesDelegate {
    BalancesLoad balancesLoad;
    ArrayList<String> ids;
    ArrayList<String> precisons;
    ArrayList<String> symbols;
    ArrayList<String> ammount;
    TinyDB tinyDB;
    Context context;
    AssetDelegate assetDelegate;

    public AssestsActivty(Context c,String account_name , AssetDelegate instance){
        context = c;
        ids = new ArrayList<>();
        precisons = new ArrayList<>();
        symbols = new ArrayList<>();
        balancesLoad = new BalancesLoad(context,this);
        assetDelegate = instance;
        balancesLoad.get_json_account_balances(account_name,"999");
    }

    HashMap<String, String> jsonToMap(String t) throws JSONException {

        HashMap<String, String> map = new HashMap<String, String>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.getString(key);
            if(key.equals("asset_id")){
                map.put(key, value);
            }
        }
        return map;
    }
    HashMap<String, ArrayList<String>> jsonArrayToMap(String t) throws JSONException {
        JSONArray myArray = new JSONArray(t);
        ArrayList<String> array = new ArrayList<>();
        HashMap<String, ArrayList<String>> pairs = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < myArray.length(); i++) {
            JSONObject j = myArray.optJSONObject(i);
            Iterator it = j.keys();
            while (it.hasNext()) {
                String n = (String) it.next();
                if(n.equals("account")){
                    ammount.add(j.getString(n));
                }
                if(n.equals("asset_id")){
                    array.add(j.getString(n));
                    pairs.put("asset_id",array);
                }
            }
        }
        return pairs;
    }
    void getJson(String s){
        HashMap<String, String> pair = new HashMap<String, String>();
        HashMap<String, ArrayList<String>>  pairs = new HashMap<String,ArrayList<String>>();
        try {
            Object json = new JSONTokener(s).nextValue();
            if (json instanceof JSONObject){
                pair = jsonToMap(s);
                if(pair.containsKey("asset_id"))
                    balancesLoad.get_asset(pair.get("asset_id"),"99");
            }
            else if(json instanceof JSONArray){
                pairs = jsonArrayToMap(s);
                if(pairs.containsKey("asset_id"))
                    balancesLoad.get_asset(pairs.get("asset_id"),"99");
            }
        }catch (Exception e){

        }
    }

    @Override
    public void OnUpdate(String s,int id){
        String convert;
        Log.i("anaml",s);
        try {
            if (id == 999) {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.has("result")) {
                    convert = jsonObject.getString("result");
                    getJson(convert);
                }
            }
        }catch (Exception e){

        }
        if(id==99) {
            String result = returnParse(s,"result");
            if(checkJsonStatus(result)==1) {
                ids = returnRootValues(result,"id");
                precisons = returnRootValues(result, "precision");
                symbols = returnRootValues(result, "symbol");
                AddinAssets();
            }

        }

    }
    String returnParse(String Json , String req){
        try {
            if(Json.contains(req)){
                JSONObject myJson = new JSONObject(Json);
                return  myJson.getString(req);}
        }catch (Exception e){}
        return "";
    }
    int checkJsonStatus(String Json){
        try {
            Object json = new JSONTokener(Json).nextValue();
//                return  myJson.getString(req);
            if (json instanceof JSONObject) {
                return 0;
            } else if (json instanceof JSONArray) {
                return 1;
            }
        }catch (Exception e){
            return -1;
        }
        return -1;
    }
    HashMap<String,ArrayList<String>> returnParseArray(String Json , String req){
        try {
            JSONArray myArray = new JSONArray(Json);
            ArrayList<String> array = new ArrayList<>();
            HashMap<String, ArrayList<String>> pairs = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < myArray.length(); i++) {
                JSONObject j = myArray.optJSONObject(i);
                Iterator it = j.keys();
                while (it.hasNext()) {
                    String n = (String) it.next();
                    if (n.equals(req)) {
                        array.add(j.getString(n));
                        pairs.put(req, array);
                    }
                }

            }
            return pairs;
        }catch (Exception e){

        }
        return null;
    }
    ArrayList<String> returnRootValues(String json , String key) {
        HashMap<String, ArrayList<String>> pairs = returnParseArray(json,key);
        return  pairs.get(key);
    }
    void AddinAssets() {                Log.i("frg","kajl");
        assetDelegate.isUpdate(ids,symbols,precisons,ammount);}
//        ArrayList<AccountDetails> accountDetails;
//        ArrayList<AccountAssets> accountAssets = new ArrayList<>();
//        for(int i = 0 ; i < ids.size() ;i++){
//            AccountAssets account1Assets = new AccountAssets();
//            account1Assets.id = ids.get(i);
//            account1Assets.precision = precisons.get(i);
//            account1Assets.symbol = symbols.get(i);
//            accountAssets.add(account1Assets);
//            Log.i("frg",ids.get(i));
//            Log.i("frg",precisons.get(i));
//            Log.i("frg",symbols.get(i));
//        }
//
//        accountDetails = tinyDB.getListObject(context.getString(R.string.pref_account_from_brainkey), AccountDetails.class);
//        Log.i("anaml", "2nd:" + accountDetails.toString() + "");
//       if(accountDetails.size()==1) {
//            accountDetails.get(0).isSelected = true;
//            accountDetails.get(0).AccountAssets = accountAssets;
//       }
//        tinyDB.putListObject(context.getString(R.string.pref_account_from_brainkey), accountDetails);
//        assetDelegate.isUpdate(true);
//
//        ArrayList<AccountDetails> accountDetails1 = tinyDB.getListObject(context.getString(R.string.pref_account_from_brainkey), AccountDetails.class);
//        Log.i("anaml", "2nd:" + accountDetails1.toString() + "");
//        if (accountDetails1.size() == 1) {
////            accountDetails1.get(0).isSelected = true;
////            accountDetails1.get(0).AccountAssets = accountAssets;
//            Log.i("anaml", "2nd:" + accountDetails1.get(0).isSelected + "");
//            Log.i("anaml", "2nd:" + accountDetails1.get(0).AccountAssets.get(0).account_id + "");
//            Log.i("anaml", "2nd:" + accountDetails1.get(0).AccountAssets.get(1).account_id + "");
//        }
//        }
}
