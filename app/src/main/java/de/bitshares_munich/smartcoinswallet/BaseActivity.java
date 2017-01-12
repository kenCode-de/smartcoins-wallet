package de.bitshares_munich.smartcoinswallet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import java.util.Locale;

import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;

/**
 * Created by qasim on 5/9/16.
 */
public class BaseActivity extends LockableActivity {
    public final String TAG = "BaseActivity";

    public static final long DISCONNECT_TIMEOUT = (3*60*1000);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language), "");
        if(!language.equals(""))
        Helper.setLocale(language,getResources());
        else {
            language = Locale.getDefault().getLanguage();
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_language) , language);
            Helper.setLocale(language,getResources());
        }
    }

    public void setBackButton(Boolean isBackButton) {
        if (isBackButton) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler disconnectHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run()
        {
            String close_bitshare = "close_bitshare";
            Boolean cb = Helper.fetchBoolianSharePref(getApplicationContext(), close_bitshare);
            if (cb)
            {
                finishAffinity();
            }
            else
            {
                resetDisconnectTimer();
            }
        }
    };

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.setCurrentActivity(this);
        resetDisconnectTimer();

        Application.send(getString(R.string.subscribe_callback));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.send(getString(R.string.cancel_subscriptions));
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }
}
