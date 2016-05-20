package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;

/**
 * Created by Syed Muhammad Muzzammil on 5/20/16.
 */
public class TransactionActivity implements BalancesDelegate {
    Context context;
    AssetDelegate assetDelegate;
    Application application = new Application();

    public TransactionActivity(Context c,String account_id , AssetDelegate instance){
        context = c;
        application.registerBalancesDelegate(this);
        get_relative_account_history(account_id,"8");
    }
    void get_relative_account_history(String account_id, String id) {
        int history_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_history));
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":["+history_id+",\"get_relative_account_history\",[\""+account_id+"\",0,10,0]]}";
        Application.webSocketG.send(getDetails);
    }
    @Override
    public void OnUpdate(String s,int id) {
        Log.i("opotytp",s);
//       Log.i("wert",returnParseArray(s,"result").toString());
        String result = returnParse(s,"result");
        Log.i("wert",result);
        HashMap<String,ArrayList<String>> arrayofOP = returnParseArray(result,"op");
       for(int i=0; i<arrayofOP.get("op").size();i++){
            Log.i("wert","1:"+arrayofOP.get("op").get(i));
        }
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
    String returnParse(String Json , String req){
        try {
            if(Json.contains(req)){
                JSONObject myJson = new JSONObject(Json);
                return  myJson.getString(req);}
        }catch (Exception e){}
        return "";
    }

}
