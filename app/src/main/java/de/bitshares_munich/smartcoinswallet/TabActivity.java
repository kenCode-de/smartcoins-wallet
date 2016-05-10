package de.bitshares_munich.smartcoinswallet;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;

public class TabActivity extends AppCompatActivity {


    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;


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

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BalancesFragment(), getString(R.string.balances));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        viewPager.setAdapter(adapter);
    }

}
