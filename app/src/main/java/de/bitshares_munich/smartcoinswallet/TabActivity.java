package de.bitshares_munich.smartcoinswallet;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;

public class TabActivity extends BaseActivity {


    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.tvBlockNumberHead_TabActivity)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_TabActivity)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected_TabActivity)
    ImageView ivSocketConnected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.mipmap.btslogo);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();

        showDialogPin();
    }

 /*   @Override
    protected void onResume()
    {
        super.onResume();
        Application.setCurrentActivity(this);
    }*/

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BalancesFragment(), getString(R.string.balances));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        viewPager.setAdapter(adapter);
    }

    private String prevBlockNumber = "";
    private int counterBlockCheck = 0;

    private Boolean isBlockUpdated()
    {
        if ( Application.blockHead != prevBlockNumber )
        {
            prevBlockNumber = Application.blockHead;
            counterBlockCheck = 0;
            return true;
        }
        else if ( counterBlockCheck++ >= 3 )
        {
            return false;
        }

        return true;
    }

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.webSocketG != null)
                {
                    if (Application.webSocketG.isOpen() && (isBlockUpdated()))
                    {
                        boolean paused = Application.webSocketG.isPaused();
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                    }
                    else
                    {
                        ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                        Application.webSocketG.close();
                        Application.webSocketConnection();
                    }
                }
                else
                {
                    Application.webSocketConnection();
                }
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(updateTask, 5000);
    }

    @OnClick(R.id.OnClickSettings_TabActivity)
    void OnClickSettings(){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }


    // Block for pin
    private void showDialogPin() {
        if (Helper.containKeySharePref(getApplicationContext(), getApplicationContext().getString(R.string.txt_pin))) {
            final Dialog dialog = new Dialog(TabActivity.this);
            //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
            dialog.setTitle(R.string.pin_verification);
            dialog.setContentView(R.layout.activity_alert_pin_dialog);
            Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
            final EditText etPin = (EditText) dialog.findViewById(R.id.etPin);
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String savedPIN = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.txt_pin));
                    if (etPin.getText().toString().equals(savedPIN)) {
                        dialog.cancel();
                    } else {
                       // Toast.makeText(getApplicationContext(), "Wrong PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.setCancelable(false);

            dialog.show();
        }
    }
    //////

}
