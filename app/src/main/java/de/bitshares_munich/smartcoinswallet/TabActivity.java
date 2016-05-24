package de.bitshares_munich.smartcoinswallet;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.utils.Application;

public class TabActivity extends AppCompatActivity {


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

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.mipmap.btslogo);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        updateBlockNumberHead();

    }

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
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(updateTask, 2000);
    }

    @OnClick(R.id.OnClickSettings_TabActivity)
    void OnClickSettings(){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

}
