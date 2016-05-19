package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.bitshares_munich.Interfaces.BalancesDelegate;

public class MainActivity extends AppCompatActivity implements BalancesDelegate{
    Gson gson;
    BalancesLoad balancesLoad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gson = new Gson();
        balancesLoad = new BalancesLoad(this,this);
        balancesLoad.get_json_account_balances("mbilal-knysys","7");

//        decomposeAlgorithm.decompositionalgo("fd");
//
//        String r= "KqPVtdU4BtpnRTbJAwguTnSCYKCh1ZzQDeZzPXx8QEdUe1UiJbVZ3r6ktHgPgGUEDoRCtFVPcUWWhwbuwvcHrYb1QNan8ZbZayfct4SGN6eQvEgzYgPeGTC3Ei6js2JujQcSwFRWfQ64QTnxQSbnrbPJHTHGZW7Uz9nPLFZnA3ZM4RhNEkRwCkxmvLZT4LcBKayXpEaVfRZgp7LpHgpRtXAc9TkaGEGonCTym1KGobhyPJS4UuTEJJyXJRD7LuZP8ChtSuJGpbHgRvBAfSF7e9fmx3pmUmmhVQEmcwchcZzcGug33VJhoRcoxp57sG3V2CJCBVJTnkAHyzkFnQb4ppH9PHRoQf6jzGctpSFVf1rKhwebovKg8tqw9MZWQd9BYHeyLJZKDnYobrvK4DdA1YHejrpAmH6qTbqkeTKm9mmQHE54hQ5YXmvFK3qd2nzn5VxPNSVuNy4qsBz1b8BR5A5R25QR47yx4BxSv6a3DY6wK6UxQzc2N3TwZdqFXE8C6g2UKXswrkK5q4cKeY8VGJ";
//
//        RequestParams params = new RequestParams();
//        params.put("method","hashtostr");
//        params.put("hash",r);
//
//
//
//        Log.i("euro",Base58.decode(r).toString()+"");
//
//
//
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.addHeader("Content-Type","application/json");
//        client.get("http://188.166.147.110:9001", params , new AsyncHttpResponseHandler(){
//
//            @Override
//            public void onStart() {
//                Log.i("euro", "start");
//
//                // called before request is started
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] timeline) {
//                // called when response HTTP status is "200 OK"
//                try {
////                   JSONObject firstEvent = (JSONObject)timeline.get(0);
////                   String tweetText = firstEvent.getString("hash");
//                    byte[] bytes = timeline;
//
//                    System.out.println("Text [Byte Format] : " + bytes);
//                    System.out.println("Text [Byte Format] : " + bytes.toString());
//
//                    String s = new String(bytes);
//                    System.out.println("Text Decryted : " + s);
//
//                    Log.i("euro", s);
//                }catch (Exception j){
//                    Log.i("euro", j+"");
//
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                Log.i("euro", "failed");
//
//            }
//
//            @Override
//            public void onRetry(int retryNo) {
//                // called when request is retried
//                Log.i("euro", "retry");
//
//            }
//        });
//        String toPass= "2AfXpEf97W2EQBY73dQhUbPrhZQW78LwnaAvS7Rz9P4JMUaC8FB4wMa7dxEYe47uZGmKs3rLv5qd2WEdxYDnWnxPBpgE4imTx28mTXzXGtVKB7LShbs2h2MpC8JR9EVKpxixiRHFNndfpUdTZhkFz6Tca4vX14PodkiMHxnq792y5fi8M2imMk2w5DJ8uAWeUH7npgwujsTkHCv4hcraTq2KyArBpSWrnvrPixTc4bTu8DkM7rpAMsKF5tMusQX6eFvDsRtGntNsFPQknPAbkHKz56sirfA8JmkPLuduKjtT3HxQZR5UzXYTN1ejP7YhqfdAu8jDXXLbxsJnqYxMLYG2cjoMvUj799YPDuCsGxZxtPopqh5Gn2gdahE";
//        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data" + "/files/languageFolder";
//        final File dir = new File(dirPath);
//        dir.mkdirs(); //create folders where write files
//        final File languageFolder = new File(dir, "BlockForTest.txt");
//        Log.i("euro", dir+"");

//createfolder with path
//        File languageFolder;
//        languageFolder = new File(dirPath);
//        try{
//            if (!languageFolder.exists()) {
//                languageFolder.getParentFile().mkdirs();
//                languageFolder.createNewFile();
//                Log.i("euro", "hgg");
//            }}catch(Exception f){
//            Log.i("euro", "fail"+f);
//
//        }
//
//        try {
//            FileOutputStream fileOuputStream = new FileOutputStream(dirPath + "/BlockForTest.txt");
//            fileOuputStream.write(Base58.decode(toPass));
//            fileOuputStream.close();
//        } catch (Exception e){
//            Log.i("euro", "nahi hwa 1"+e);
//
//        }
//        try{
//            decomposeAlgorithm.decompositionalgo(dirPath + "/BlockForTest.txt");}
//        catch (Exception e){
//            Log.i("euro", "nahi hwa 2"+e);
//        }

    }
    //    Intent intent = new Intent(getApplicationContext(), qrcodeActivity.class);
//    startActivityForResult(intent,90);
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch(requestCode) {
//            case 90:
//                if (resultCode == RESULT_OK) {
//                    Bundle res = data.getExtras();
//                    String result1 = res.getString("sFormat");
//                    String result2 = res.getString("sResult");
//                    //Log.d("falcon", "sFormat:"+result1);
//                   // Log.d("falcon", "sResult:"+result2);
//                }
//                break;
//        }
//    }

public void settings(View v){
    Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
    startActivity(intent);
}
    public void sendscreen(View v){
        Intent intent = new Intent(getApplicationContext(), SendScreen.class);
        startActivity(intent);
    }
public void recieve(View v){
    Intent intent = new Intent(getApplicationContext(), RecieveActivity.class);
    startActivity(intent);
}
    public void request(View v){
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
        startActivity(intent);
    }public void payment(View v){
        Intent intent = new Intent(getApplicationContext(), PaymentRecieved.class);
        startActivity(intent);
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
        String convert;ArrayList<String> ids;ArrayList<String> precisons;ArrayList<String> symbols;
        try {
            if (id == 7) {
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
               for (int i = 0; i < ids.size(); i++) {
                    Log.i("falconBhai","found:id:"+ids.get(i));
                   Log.i("falconBhai","found:precisons:"+precisons.get(i));
                   Log.i("falconBhai","found:symbols:"+symbols.get(i));
               }
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
}
