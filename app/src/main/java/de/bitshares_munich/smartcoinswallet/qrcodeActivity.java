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

import cz.msebera.android.httpclient.*;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Syed Muhammad Muzzammil on 5/10/16.
 */
public class qrcodeActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
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
//        Log.i("falcon", rawResult.getText()); // Prints scan results
//        Log.i("falcon", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String sResult = rawResult.getText();
        String sFormat = rawResult.getBarcodeFormat().toString();
        mScannerView.stopCamera();
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
                    Log.i("euro", s);
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
        finishWithResult(sFormat, sResult);
    }

    private void finishWithResult(String sFormat, String sResult) {
        Bundle conData = new Bundle();
        conData.putString("sFormat", sFormat);
        conData.putString("sResult", sResult);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
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
