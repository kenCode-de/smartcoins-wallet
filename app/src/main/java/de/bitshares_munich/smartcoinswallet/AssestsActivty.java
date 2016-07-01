package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.SupportMethods;

/**
 * Created by Syed Muhammad Muzzammil on 5/19/16.
 */
public class AssestsActivty  implements IBalancesDelegate {
    //BalancesLoad balancesLoad;
    ArrayList<String> ids;
    ArrayList<String> precisons;
    ArrayList<String> symbols;
    ArrayList<String> ammount;
    Context context;
    AssetDelegate assetDelegate;
    Application application;

    public AssestsActivty(Context c,String account_name , AssetDelegate instance, Application app){
        context = c;
        application = app;
        ids = new ArrayList<>();
        precisons = new ArrayList<>();
        symbols = new ArrayList<>();
        ammount = new ArrayList<>();
        assetDelegate = instance;

        /*
        balancesLoad = new BalancesLoad(context,this);
        assetDelegate = instance;
        balancesLoad.get_json_account_balances(account_name,"999");
        */
    }

    public void registerDelegate ()
    {
        application.registerBalancesDelegateAssets(this);
    }

    public void loadBalances(String account_name)
    {
        get_json_account_balances(account_name,"999");
    }

    final Handler handler = new Handler();

    Boolean sentCallForBalances = false;
    void get_json_account_balances(final String account_name,final String id)
    {
        Log.d("Assets Activity", "Sending call for assets");

        final Runnable updateTask2 = new Runnable() {
            @Override
            public void run() {
                if ( sentCallForBalances ) // if balances are not returned in one second
                {
                    sentCallForBalances = false;

                    // if websocket is connected
                    if ( Application.webSocketG != null && Application.webSocketG.isOpen() )
                    {
                        Application.webSocketG.close();
                        get_json_account_balances(account_name, id);
                    }
                    // if websocket was disconnected
                    else
                    {
                        get_json_account_balances(account_name, id);
                    }
                }
            }
        };


        final Runnable updateTask = new Runnable() {
            @Override
            public void run()
            {
                if (Application.webSocketG != null && (Application.webSocketG.isOpen()) && Application.isReady )
                {
                    String getDetails = "{\"id\":" + id + ",\"method\":\"get_named_account_balances\",\"params\":[\"" + account_name + "\",[]]}";
                    Application.webSocketG.send(getDetails);
                    sentCallForBalances = true;
                    handler.removeCallbacks(updateTask2);
                    handler.postDelayed(updateTask2, 2000);
                    Log.d("Assets Activity", "Sent call for assets");
                }
                else
                {
                    get_json_account_balances(account_name,"999");
                }
            }
        };

        handler.removeCallbacks(updateTask);
        handler.postDelayed(updateTask, 1000);
    }

    void get_asset(String asset, String id) {
        String getDetails ="{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+asset+"\"]]}";
        Application.webSocketG.send(getDetails);
    }
    void get_asset(ArrayList<String> asset, String id) {
        //{"id":1,"method":"get_assets","params":[["1.3.0","1.3.120"]]}
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ;i<asset.size();i++){
            stringBuilder.append(asset.get(i));
            if((i+1)<asset.size())
                stringBuilder.append("\",\"");
        }
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+stringBuilder.toString()+"\"]]}";
        Application.webSocketG.send(getDetails);
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
                if(n.equals("amount")){
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
                    get_asset(pair.get("asset_id"),"99");
            }
            else if(json instanceof JSONArray){
                pairs = jsonArrayToMap(s);
                if(pairs.containsKey("asset_id"))
                    get_asset(pairs.get("asset_id"),"99");
            }
        }catch (Exception e){

        }
    }


    @Override
    public void OnUpdate(String s,int id){
        SupportMethods.testing("assests",s,"ids");
        Log.d("Assets Activity", "Call received");

        String convert;
        try
        {
            if (id == 999)
            {
                sentCallForBalances = false;

                ids = new ArrayList<>();
                precisons = new ArrayList<>();
                symbols = new ArrayList<>();
                ammount = new ArrayList<>();

                Log.d("Assets Activity", "Balances received");
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.has("result"))
                {
                    convert = jsonObject.getString("result");
                    getJson(convert);
                }
            }
        }
        catch (Exception e)
        {
            Log.d("Assets Activity",e.getMessage());
        }

        if(id==99)
        {
            Log.d("Assets Activity", "Assets received");
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
    void AddinAssets() {
        SupportMethods.testing("assests",ids,"ids");
        SupportMethods.testing("assests",symbols,"ids");
        SupportMethods.testing("assests",precisons,"ids");
        SupportMethods.testing("assests",ammount,"ids");

        Log.d("Assets Activity", "Updating Assets..............");

        assetDelegate.isUpdate(ids,symbols,precisons,ammount);
    }
}
