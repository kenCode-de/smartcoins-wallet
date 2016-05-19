package de.bitshares_munich.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.smartcoinswallet.MainActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment {


    @Bind(R.id.llBalances)
    LinearLayout llBalances;

    @Bind(R.id.llTransactions)
    LinearLayout llTransactions;

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
        ButterKnife.bind(this, rootView);


        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 5; i++) {
            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);

            llBalances.addView(customView);

        }
        for (int j = 0; j < 50; j++) {
            View customView1 = layoutInflater.inflate(R.layout.items_rows_transactions, null);
            llTransactions.addView(customView1);
        }


        return rootView;
    }

    @OnClick(R.id.recievebtn)
    public void GoToRecieveActivity(){
        Intent intent = new Intent(getActivity(), RecieveActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.sendbtn)
    public void GoToSendActivity(){
        Intent intent = new Intent(getActivity(), SendScreen.class);
        startActivity(intent);
    }
    @OnClick(R.id.qrCamera)
    public void QrCodeActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}


