package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by qasim on 5/19/16.
 */
public class SplashActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tinyDB = new TinyDB(getApplicationContext());

        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if (arrayList.size() > 0) {
            moveToMainScreen();
        } else {
            moveToAccountScreen();
        }

    }

    private void moveToMainScreen() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, TabActivity.class);
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    private void moveToAccountScreen() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, AccountActivity.class);
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);
    }


}