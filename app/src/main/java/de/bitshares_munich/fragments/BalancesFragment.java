package de.bitshares_munich.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.BalancesLoad;
import de.bitshares_munich.smartcoinswallet.MainActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate {


    @Bind(R.id.llBalances)
    LinearLayout llBalances;

    @Bind(R.id.llTransactions)
    LinearLayout llTransactions;

    TinyDB tinyDB;

    public BalancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        new AssestsActivty(getContext(),"mbilal-knysys" , this);

//        AccountAssets account1Assets = new AccountAssets();
//        account1Assets.account_id = "034";
//        AccountAssets account2Assets = new AccountAssets();
//        account2Assets.account_id = "0344546";
//        ArrayList<AccountAssets> accountAssets = new ArrayList<>();
//        accountAssets.add(account1Assets);
//        accountAssets.add(account2Assets);
//
//        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_account_from_brainkey), AccountDetails.class);
//        if (accountDetails.size() == 1) {
//            accountDetails.get(0).isSelected = true;
//            accountDetails.get(0).AccountAssets = accountAssets;
//            Log.i("opo", accountDetails.get(0).isSelected + "");
//            Log.i("opo", accountDetails.get(0).AccountAssets.get(0).account_id + "");
//        }
//        tinyDB.putListObject(getString(R.string.pref_account_from_brainkey), accountDetails);



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
    public void GoToRecieveActivity() {
        Intent intent = new Intent(getActivity(), RecieveActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.sendbtn)
    public void GoToSendActivity() {
        Intent intent = new Intent(getActivity(), SendScreen.class);
        startActivity(intent);
    }

    @OnClick(R.id.qrCamera)
    public void QrCodeActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void isUpdate(ArrayList<String> ids , ArrayList<String> sym ,ArrayList<String> pre , ArrayList<String>  am){
        Log.i("anaml", "2nd:" + ids + "");
        ArrayList<AccountDetails> accountDetails;
        ArrayList<AccountAssets> accountAssets = new ArrayList<>();
        for(int i = 0 ; i < ids.size() ;i++){
            AccountAssets account1Assets = new AccountAssets();
            account1Assets.id = ids.get(i);
            account1Assets.precision = pre.get(i);
            account1Assets.symbol = sym.get(i);
            account1Assets.ammount = am.get(i);

            accountAssets.add(account1Assets);

        }

        accountDetails = tinyDB.getListObject(getString(R.string.pref_account_from_brainkey), AccountDetails.class);
        Log.i("anaml", "2nd:" + accountDetails.toString() + "");
       if(accountDetails.size()==1) {
            accountDetails.get(0).isSelected = true;
            accountDetails.get(0).AccountAssets = accountAssets;
       }
        tinyDB.putListObject(getString(R.string.pref_account_from_brainkey), accountDetails);

        ArrayList<AccountDetails> accountDetails1 = tinyDB.getListObject(getString(R.string.pref_account_from_brainkey), AccountDetails.class);
        Log.i("anaml", "2nd:" + accountDetails1.toString() + "");
        if (accountDetails1.size() == 1) {
           accountDetails1.get(0).AccountAssets = accountAssets;
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).isSelected + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(0).id + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(1).id + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(0).precision + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(1).precision + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(0).symbol + "");
            Log.i("donyahoo", "2nd:" + accountDetails1.get(0).AccountAssets.get(1).symbol + "");

        }
        }

}


