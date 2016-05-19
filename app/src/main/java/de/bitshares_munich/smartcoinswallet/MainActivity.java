package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            jsonToMap("{\"json\":\"{\\\"to\\\":\\\"srk\\\",\\\"to_label\\\":\\\"srk\\\",\\\"currency\\\":\\\"BTS\\\",\\\"memo\\\":\\\"Order: 8f04a475-4c1a-4bb5-a548-b7e1fafaefa7 #sapos\\\",\\\"ruia\\\":\\\"1.3.541\\\",\\\"line_items\\\":[{\\\"label\\\":\\\"Your Purchase\\\",\\\"quantity\\\":1,\\\"price\\\":\\\"1.6977125632925415E8\\\"},{\\\"label\\\":\\\"Donation fee\\\",\\\"quantity\\\":1,\\\"price\\\":\\\"848859.0826970935\\\"}],\\\"note\\\":\\\"\\\",\\\"callback\\\":\\\"http://188.166.147.110:8000/transaction/1.2.88346/8f04a475-4c1a-4bb5-a548-b7e1fafaefa7\\\"}\",\"status\":\"success\"}");
        }catch (Exception e){

        }

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
    public static void jsonToMap(String t) throws JSONException {

        HashMap<String, String> map = new HashMap<String, String>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.getString(key);
            map.put(key, value);

        }

        System.out.println("json : "+jObject);
        System.out.println("map : "+map);
    }
}
