package de.bitsharesmunich.cryptocoincore.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.adapters.ViewPagerAdapter;
import de.bitsharesmunich.cryptocoincore.models.Coin;


/**
 * Created by hvarona on 05/02/17.
 */
public class NoCurrencyAccountFragment extends Fragment {
    private Coin coin;
    private ViewPager pager;

    public NoCurrencyAccountFragment() {
        // Required empty public constructor
    }

    public static NoCurrencyAccountFragment newInstance(Coin coin) {
        NoCurrencyAccountFragment noCurrencyAccountFragment = new NoCurrencyAccountFragment();

        Bundle args = new Bundle();
        args.putString("coin",coin.toString());
        noCurrencyAccountFragment.setArguments(args);

        return noCurrencyAccountFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_no_currency_account, container, false);
        final Fragment f = this;

        this.coin = Coin.valueOf(getArguments().getString("coin", "BITSHARE"));

        TextView text = (TextView) v.findViewById(R.id.noCurrencyAccountText);
        text.setText(getString(R.string.no_currency_account,this.coin.toString()));

        if (container instanceof ViewPager) {
            pager = (ViewPager) container;
        }

        Button importButton = (Button) v.findViewById(R.id.importCurrencyAccount);
        Button createButton = (Button) v.findViewById(R.id.createCurrencyAccount);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager != null){
                    ((ViewPagerAdapter)pager.getAdapter()).changeBitcoinFragment();
                    //pager.getAdapter().notifyDataSetChanged();
                }

            }
        });

        return v;
    }
}