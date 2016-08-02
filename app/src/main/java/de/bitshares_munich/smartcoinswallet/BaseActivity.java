package de.bitshares_munich.smartcoinswallet;

//import android.app.Dialog;
//import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
//import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
//import android.view.View;
//import android.webkit.WebView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;

import java.util.Locale;

import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
//import de.bitshares_munich.utils.SupportMethods;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

/**
 * Created by qasim on 5/9/16.
 */
public class BaseActivity extends AppCompatActivity {

    public static final long DISCONNECT_TIMEOUT = (3*60*1000);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fabric.with(this, new Crashlytics());

        String language = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
//                SupportMethods.setLocale(getApplicationContext(),language);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

    /*
    protected void setToolbarAndNavigationBar(String heading, Boolean navigationBar) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //
        //        toolbar_title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        //        // wb = (WebView) findViewById(R.id.wb);
        //        toolbar_title.setText(heading);
        //        //toolbar_title.setText(heading);
        //        //toolbar.setTitle(heading);
        //        //toolbar.setTitleTextColor(Color.WHITE);
        //        setSupportActionBar(toolbar);

    }
    */
}
