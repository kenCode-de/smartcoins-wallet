package de.bitshares_munich.smartcoinswallet;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabHost;

public class BalancesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balances_activity);

        populateTabs();

    }

    public void populateTabs() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        TabHost.TabSpec tab1 = tabHost.newTabSpec("BALANCES");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("CONTACTS");
        tab1.setIndicator("Tab1");
        tab1.setContent(new Intent(this, BalancesActivity.class));
        tab2.setIndicator("Tab2");
        tab2.setContent(new Intent(this, BalancesActivity.class));
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
    }
}
