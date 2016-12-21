package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.ArrayList;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Syed Muhammad Muzzammil on 5/10/16.
 */
public class QRCodeActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private static String TAG = "QRCodeActivity";
    private ZXingScannerView mScannerView;

    /* Pin pinDialog */
    private Dialog pinDialog;

    /* Internal attribute used to keep track of the activity state */
    private boolean mRestarting;

    /* TinyDB instance */
    private TinyDB tinyDB;

    int id;

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

        tinyDB = new TinyDB(getApplicationContext());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mRestarting = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        * Ask for pin number if this is not a restart (first time) or
        * if this is not we coming back from an internal move.
        */
        if(mRestarting){
            showDialogPin();
        }
    }

    // Block for pin
    private void showDialogPin() {
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        pinDialog = new Dialog(QRCodeActivity.this);
        pinDialog.setTitle(R.string.txt_6_digits_pin);
        pinDialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) pinDialog.findViewById(R.id.btnDone);
        final EditText etPin = (EditText) pinDialog.findViewById(R.id.etPin);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                            pinDialog.cancel();
                            break;
                        }else{
                            Toast.makeText(QRCodeActivity.this, getResources().getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        pinDialog.setCancelable(false);
        pinDialog.show();
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
        Intent intent = new Intent(QRCodeActivity.this, SendScreen.class);
        intent.putExtras(conData);
        intent.putExtra("id",5);
        startActivity(intent);
        finish();
    }
}
