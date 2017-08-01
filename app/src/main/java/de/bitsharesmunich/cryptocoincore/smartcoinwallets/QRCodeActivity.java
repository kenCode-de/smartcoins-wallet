package de.bitsharesmunich.cryptocoincore.smartcoinwallets;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.zxing.Result;

import de.bitshares_munich.smartcoinswallet.BaseActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 *
 * Created by Syed Muhammad Muzzammil on 5/10/16.
 */
public class QRCodeActivity extends BaseActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    int id;
    Coin coin;

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
        if (this.getIntent().getExtras().getString(getString(R.string.coin)) == null){
            this.coin = Coin.BITSHARE;
        } else {
            this.coin = Coin.valueOf(this.getIntent().getExtras().getString(getString(R.string.coin), "BITSHARE"));
        }
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        progressDialog = new ProgressDialog(this);
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
                if (id==0) {
                    finishWithResult(rawResult.toString());
                } else if(id==1) {
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
        conData.putSerializable("sResult",parseddata);
        Intent intent = new Intent();
        intent.putExtras(conData);
        intent.putExtra(getString(R.string.coin),coin.name());
        setResult(RESULT_OK, intent);
        finish();
    }
    private void StartWithfinishWithResult(String parseddata) {
        Bundle conData = new Bundle();
        conData.putSerializable("sResult",parseddata);
        Intent intent = new Intent(QRCodeActivity.this, SendScreen.class);
        intent.putExtras(conData);
        intent.putExtra("id",5);
        intent.putExtra(getString(R.string.coin),coin.name());
        startActivity(intent);
        finish();
    }
}
