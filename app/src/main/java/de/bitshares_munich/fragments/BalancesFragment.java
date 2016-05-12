package de.bitshares_munich.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment {

    public BalancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        LinearLayout llBalances = (LinearLayout) rootView.findViewById(R.id.llBalances);
        LinearLayout llTransactions = (LinearLayout) rootView.findViewById(R.id.llTransactions);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 5; i++) {
            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);

            llBalances.addView(customView);

        }
        for (int j = 0; j < 50; j++) {
            View customView1 = layoutInflater.inflate(R.layout.items_rows_balances, null);
            llTransactions.addView(customView1);
        }


        return rootView;
    }

}


