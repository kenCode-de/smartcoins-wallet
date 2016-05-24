package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
    int id_in_work;
    int id_total_size;
    int names_total_size;
    int names_in_work;
    int assets_id_in_work;
    int assets_id_total_size;

    HashMap<String,HashMap<String,String>> arrayof_Amount_AssetId = new HashMap<>();
    ArrayList<String> blocks;
    HashMap<String,String> timestamp;
    BalancesDelegate balancesDelegate;
    String accountid;
    List<String> ofNames = new ArrayList<>();
    List<String> asset_ids = new ArrayList<>();
    HashMap<String,String> Names_from_Api = new HashMap<>();
    HashMap<String,HashMap<String,String>> Symbols_Precisions = new HashMap<>();

    public TransactionActivity(Context c,String account_id , AssetDelegate instance){
        context = c;
        balancesDelegate = this;
        application.registerBalancesDelegate(this);
        accountid = account_id;
        timestamp = new HashMap<>();
        if(account_id!=null)
        get_relative_account_history(account_id,"8");
    }
    void get_relative_account_history(String account_id, String id) {
        int history_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_history));
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":["+history_id+",\"get_relative_account_history\",[\""+account_id+"\",0,10,0]]}";
        Application.webSocketG.send(getDetails);
    }
    @Override
    public void OnUpdate(String s,int id) {
       // Log.i("kpk",""+ s+"");
//        Log.i("kpk", "2:id_in_work:"+ id_in_work+"");
//        Log.i("kpk","1:id_total_size:"+ id_total_size+"");
//        Log.i("kpk","1:id_total_size:"+ s+"");

//       Log.i("wert",returnParseArray(s,"result").toString());
        if(id==8){
            onFirstCall(s);
        }
        if(id==9){
            if(id_in_work<id_total_size) {
                String result = returnParse(s,"result");
                String time = returnParse(result,"timestamp");
                timestamp.put(Integer.toString(id_in_work),time);
                if(id_in_work==(id_total_size-1)){
                    names_in_work=0;
                    get_names(ofNames.get(names_in_work),"10");
                }
                id_in_work++;
                if(id_in_work<blocks.size())
                get_Time(blocks.get(id_in_work),"9");
            }
        }
        if(id==10) {
            if (names_in_work < names_total_size) {
                String result = returnParse(s,"result");
                String nameObject = returnArrayObj(result,0);
                String name = returnParse(nameObject,"name");
                Names_from_Api.put(ofNames.get(names_in_work),name);
                if(names_in_work==(names_total_size-1)){
                    assets_id_in_work=0;
                    get_asset(asset_ids.get(assets_id_in_work),"11");
                }
                names_in_work++;
                if(names_in_work<ofNames.size())
                get_names(ofNames.get(names_in_work),"10");
            }
        }
        if(id==11) {
            if (assets_id_in_work < assets_id_total_size) {
               String result = returnParse(s,"result");
                String assetObject = returnArrayObj(result,0);
                String symbol = returnParse(assetObject,"symbol");
                String precision = returnParse(assetObject,"precision");
                HashMap<String,String> de = new HashMap<>();
                de.put("symbol",symbol);
                de.put("precision",precision);
                Symbols_Precisions.put(asset_ids.get(assets_id_in_work),de);
                if(assets_id_in_work==(assets_id_total_size-1)){
                    Log.i("qubee","a:"+Symbols_Precisions);
                }
                assets_id_in_work++;
                if(assets_id_in_work<asset_ids.size()) get_asset(asset_ids.get(assets_id_in_work),"11");
            }
        }
    }
    void onFirstCall(String s){
        String result = returnParse(s,"result");
        HashMap<String,ArrayList<String>> arrayofOP = returnParseArray(result,"op");
        HashMap<String,ArrayList<String>> arrayofblock_num = returnParseArray(result,"block_num");
        blocks = arrayofblock_num.get("block_num");
        for(int i=0; i<arrayofOP.get("op").size();i++){
            String breakArray = returnArrayObj(arrayofOP.get("op").get(i),1);
            HashMap<String,String> mapof_All= new HashMap<>();
            String parseOfamount = returnParse(breakArray,"amount");
            String from = returnParse(breakArray,"from");
            String to = returnParse(breakArray,"to");
            String amount = returnParse(parseOfamount,"amount");
            String asset_id = returnParse(parseOfamount,"asset_id");
            mapof_All.put("amount",amount);
            mapof_All.put("asset_id",asset_id);
            mapof_All.put("from",from);
            mapof_All.put("to",to);
            ofNames.add(from);
            ofNames.add(to);
            asset_ids.add(asset_id);
            if(breakArray.contains("memo")) {
                String memo = returnParse(breakArray, "memo");
                String message = returnParse(memo, "message");
                mapof_All.put("message", message);
            }
            arrayof_Amount_AssetId.put(Integer.toString(i),mapof_All);
        }

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(ofNames);
        ofNames.clear();
        ofNames.addAll(hashSet);

        names_total_size = ofNames.size();

        assets_id_total_size = asset_ids.size();

        id_total_size = blocks.size();

//        if(arrayof_Amount_AssetId.size()==blocks.size())
//        id_total_size = arrayof_Amount_AssetId.size();
//        else id_total_size = Math.min(arrayof_Amount_AssetId.size(),blocks.size());

        id_in_work = 0;
        get_Time(blocks.get(id_in_work),"9");
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
    String returnArrayObj(String Json , int position){
        try {
            JSONArray myArray = new JSONArray(Json);
            if(myArray.length()>=position){
                return  myArray.get(position).toString();
            }
        }catch (Exception e){}
        return "";
    }

    void get_Time(String block_num,String id){
        int db_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
      //  {"id":4,"method":"call","params":[2,"get_block_header",[6356159]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_block_header\",[ " + block_num + "]]}";
        Application.webSocketG.send(getDetails);
    }
    void get_names(String name_id,String id){
        int db_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
        //    {"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        Application.webSocketG.send(getDetails);
    }
    void get_asset(String asset, String id) {
        //{"id":1,"method":"get_assets","params":[["1.3.0","1.3.120"]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+asset+"\"]]}";
        Application.webSocketG.send(getDetails);
    }
    void testing(String msg , Object obj){
        Log.i("Saiyed_Testing","=> Msg : "+ msg + " :Object Testing :" + obj);
    }

}
