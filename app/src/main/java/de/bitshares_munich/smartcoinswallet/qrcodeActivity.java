package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.zxing.Result;

import de.bitshares_munich.utils.SupportMethods;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Syed Muhammad Muzzammil on 5/10/16.
 */
public class qrcodeActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private static String TAG = "qrcodeActivity";
    private ZXingScannerView mScannerView;
    int id;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
   // private GoogleApiClient client;

    // Camera Permissions
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    public static void verifyCameraPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA_PERMISSION
            );
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setBackButton(true);
        setTitle(getResources().getString(R.string.qr_code_activity_name));

        verifyCameraPermissions(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("id",-1);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        progressDialog = new ProgressDialog(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
     //   client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    ProgressDialog progressDialog;

    @Override
    public void handleResult(final Result rawResult)
    {
        showDialog("","");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mScannerView.stopCamera();
                if (id==0)
                {
                    finishWithResult(rawResult.toString());
                }
                else if(id==1)
                {
                    StartWithfinishWithResult(rawResult.toString());
                }
            }
        });


    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }

    private void hideDialog() {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }

    }

    private void finishWithResult(String parseddata) {
        Bundle conData = new Bundle();
        SupportMethods.testing("merchantEmail",parseddata,"Object");
        conData.putSerializable("sResult",parseddata);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }
    private void StartWithfinishWithResult(String parseddata) {
        Log.d(TAG, "StartWithfinishWithResult");
        Log.d(TAG, "parsed data: "+parseddata);
        Bundle conData = new Bundle();
        SupportMethods.testing("merchantEmail",parseddata,"Object");
        conData.putSerializable("sResult",parseddata);
        Intent intent = new Intent(qrcodeActivity.this, SendScreen.class);
        intent.putExtras(conData);
        intent.putExtra("id",5);
        startActivity(intent);
        finish();
    }
}
