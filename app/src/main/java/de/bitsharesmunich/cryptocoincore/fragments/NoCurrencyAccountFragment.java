package de.bitsharesmunich.cryptocoincore.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import de.bitsharesmunich.cryptocoincore.models.Coin;


/**
 * Created by qasim on 5/10/16.
 */
public class NoCurrencyAccountFragment extends Fragment {
    public NoCurrencyAccountFragment() {
        // Required empty public constructor
    }

    public static NoCurrencyAccountFragment newInstance(Coin coin) {
        NoCurrencyAccountFragment balancesFragment = new NoCurrencyAccountFragment();

        Bundle args = new Bundle();
        args.putString("coin",coin.toString());
        balancesFragment.setArguments(args);

        return balancesFragment;
    }
}