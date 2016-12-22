package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by qasim on 5/19/16.
 */
public class SplashActivity extends Activity {
    private String TAG = "SplashActivity";

    /*
    * Key used to store a flag value in the Bundle passed with the Intent that will open
    * up the TabActivity. This activity is the main entry point for this app, and since it
    * is an instance of LockableActivity, it will look for this flag in order to decide
    * wheter or not to display the pin dialog at the onStart life-cycle method.
    */
    public static final String KEY_ASK_FOR_PIN = "ask_for_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkWhereToGo();
            }
        }, 1000);
    }

    private void checkWhereToGo() {
        TinyDB tinyDB = new TinyDB(getApplicationContext());
        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if (arrayList != null && arrayList.size() > 0) {
            Log.d(TAG, "we have at least one account!");
            if (Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_backup_bin_exist))) {
                Log.d(TAG,"backup bin exists");
                moveToMainScreen(tinyDB);
            } else {
                Log.d(TAG,"backup bin does not exist");
                moveToBackupBrainKey();
            }
        } else {
            moveToAccountScreen();
        }
    }

    private void moveToMainScreen(TinyDB tinyDB) {
        String pin = "";
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                pin = accountDetails.get(i).pinCode;
                break;
            }
        }
        Intent i = new Intent(SplashActivity.this, TabActivity.class);
        if (pin != null && !pin.isEmpty()) {
            i.putExtra(KEY_ASK_FOR_PIN, true);
        } else {
            i.putExtra(KEY_ASK_FOR_PIN, false);
        }

        startActivity(i);
        finish();
    }

    private void moveToAccountScreen() {
        Intent i = new Intent(SplashActivity.this, AccountActivity.class);
        i.putExtra(KEY_ASK_FOR_PIN, false);
        startActivity(i);
        finish();
    }

    private void moveToBackupBrainKey() {
        Intent i = new Intent(SplashActivity.this, BackupBrainkeyActivity.class);
        i.putExtra(KEY_ASK_FOR_PIN, false);
        startActivity(i);
        finish();
    }
}
