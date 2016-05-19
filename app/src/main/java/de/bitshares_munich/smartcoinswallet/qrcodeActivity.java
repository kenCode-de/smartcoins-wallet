package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;
import com.loopj.android.http.*;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.Result;
import com.koushikdutta.async.http.body.StringBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.*;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Syed Muhammad Muzzammil on 5/10/16.
 */
public class qrcodeActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    String sResult;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
 //       final StringBuilder sResult = new StringBuilder();
        mScannerView.stopCamera();
        workingQrcode(rawResult.toString());
//        String toGet = "http://188.166.147.110:9002/get_json_for_hash?hash="+rawResult;
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.addHeader("Content-Type","application/json");
//            client.get(toGet, null , new AsyncHttpResponseHandler(){
//                @Override
//            public void onStart() {
//                Log.i("euro", "start");
//            }
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] timeline) {
//                try {
//                    byte[] bytes = timeline;
//                    String s = new String(bytes);
//                    sResult.append(s);
//                    Log.i("euro", s);
//                }catch (Exception j){
//                    Log.i("euro", j+"");
//                }
//            }
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                Log.i("euro", "failed");
//            }
//            @Override
//            public void onRetry(int retryNo) {
//                // called when request is retried
//                Log.i("euro", "retry");
//
//            }
//        });
//        parseStringtoJson(sResult.toString());
//        finishWithResult();
    }

    HashMap<String,String> parseStringtoJson(String myJson){
        try {
            HashMap<String,String> parsedData = new HashMap<>();
            String getOne = returnParse(myJson,"json");
            parsedData.put("to",returnParse(getOne,"to"));
            parsedData.put("to_label",returnParse(getOne,"to_label"));
            parsedData.put("currency",returnParse(getOne,"currency"));
            parsedData.put("memo",returnParse(getOne,"memo"));
            parsedData.put("ruia",returnParse(getOne,"ruia"));
            parsedData.put("note",returnParse(getOne,"note"));
            parsedData.put("callback",returnParse(getOne,"callback"));

            String line_items = returnParse(getOne,"line_items");
            JSONArray jsonRootObject = new JSONArray(line_items);

            for(int i=0;i<jsonRootObject.length();i++) {
                String jArray = jsonRootObject.get(i).toString();
                parsedData.put("label"+i, returnParse(jArray, "label"));
                parsedData.put("quantity"+i, returnParse(jArray, "quantity"));
                parsedData.put("price"+i, returnParse(jArray, "price"));
            }


            return parsedData;


        }
        catch (JSONException j){
            Log.i("kopi","error: " + j +"");

        }
        return null;
    }
    String returnParse(String Json , String req) throws JSONException{
        if(Json.contains(req)){
        JSONObject myJson = new JSONObject(Json);
        return  myJson.getString(req);}
        else return "null";
    }


    private void finishWithResult(HashMap<String,String> parseddata) {
        Bundle conData = new Bundle();
        conData.putSerializable("sResult",parseddata);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }
    void workingQrcode(String rawResult){
        String toGet = "http://188.166.147.110:9002/get_json_for_hash?hash="+rawResult;
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Content-Type","application/json");
        client.get(toGet, null , new AsyncHttpResponseHandler(){
            @Override
            public void onStart() {
                Log.i("euro", "start");
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] timeline) {
                try {
                    byte[] bytes = timeline;
                    String s = new String(bytes);
                    finishWithResult(parseStringtoJson(s));

                    Log.i("euro", sResult);
                }catch (Exception j){
                    Log.i("euro", j+"");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("euro", "failed");
            }
            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Log.i("euro", "retry");

            }
        });

    }

}
    //called in another activity by this code

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
//                    Log.d("falcon", "sFormat:"+result1);
//                    Log.d("falcon", "sResult:"+result2);
//                }
//                break;
//        }
//    }
