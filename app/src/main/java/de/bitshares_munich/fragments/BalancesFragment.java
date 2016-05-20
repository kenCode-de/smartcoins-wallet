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
import android.widget.TextView;


import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.nostra13.universalimageloader.utils.L;

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

    ArrayList<AccountDetails> accountDetails;

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
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);


     LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        for (int i = 0; i < 5; i++) {
//            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
//            LinearLayout layout = (LinearLayout) customView;
//            LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
//            int count1 = layout1.getChildCount();
//            TextView textView = (TextView) layout1.getChildAt(0);
//            textView.setText("dfgfd");
//            llBalances.addView(customView);
//        }
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
        Log.i("uncle","aay1");
        ArrayList<AccountAssets> accountAssets = new ArrayList<>();
        for(int i = 0 ; i < ids.size() ;i++){
            AccountAssets accountAsset = new AccountAssets();
            Log.i("uncle","aay1");
            accountAsset.id = ids.get(i);
            accountAsset.precision = pre.get(i);
            accountAsset.symbol = sym.get(i);
            accountAsset.ammount = am.get(i);
            Log.i("uncle","aay1"+ids.get(i));
            Log.i("uncle","aay1"+pre.get(i));
            Log.i("uncle","aay1"+sym.get(i));
           // Log.i("uncle","aay1"+am.get(i));

            accountAssets.add(accountAsset);
        }
       if(accountDetails.size()==1) {
            accountDetails.get(0).isSelected = true;
            accountDetails.get(0).AccountAssets = accountAssets;
       } else {
           for(int i = 0 ; i < accountDetails.size() ; i++){
                if(accountDetails.get(i).isSelected){
                    accountDetails.get(0).AccountAssets = accountAssets;
                    break;
                }
           }
       }
        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        BalanceAssetsUpdate(sym,pre,am);
//        ArrayList<AccountDetails> accountDetails1 = tinyDB.getListObject(getString(R.string.pref_account_from_brainkey), AccountDetails.class);
//        if (accountDetails1.size() == 1) {
//           accountDetails1.get(0).AccountAssets = accountAssets;
//            Log.i("NAMA","22aay1"+accountDetails1.get(0).AccountAssets.get(0).id);
//            Log.i("NAMA","22aay1"+accountDetails1.get(0).AccountAssets.get(0).precision);
//            Log.i("NAMA","22aay1"+accountDetails1.get(0).AccountAssets.get(0).symbol);
//
//        }
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym ,final ArrayList<String> pre ,final ArrayList<String>  am){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               // for (int i = 0; i < 5; i++) {
                    View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
            LinearLayout layout = (LinearLayout) customView;
            LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
            TextView textView = (TextView) layout1.getChildAt(0);
            textView.setText(sym.get(0));
                TextView textView1 = (TextView) layout1.getChildAt(1);
                textView1.setText(returnFromPower(pre.get(0),am.get(0)));
                TextView textView2 = (TextView) layout1.getChildAt(2);
                    textView2.setText(sym.get(1));
                TextView textView3 = (TextView) layout1.getChildAt(3);
                textView3.setText(returnFromPower(pre.get(1),am.get(1)));
                llBalances.addView(customView);
              //  }
            }
        });











//        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        for (int i = 0; i < 5; i++) {
////            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
////            LinearLayout layout = (LinearLayout) customView;
////            LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
////            int count1 = layout1.getChildCount();
////            TextView textView = (TextView) layout1.getChildAt(0);
////            textView.setText("dfgsdfd");
//            View customView1 = layoutInflater.inflate(R.layout.items_rows_balances, null);
//            llBalances.addView(customView1);
//        }
    }
    String returnFromPower(String i,String str){
        int ok=1;
        Log.i("popu",i);
        Log.i("popu",str);
        for(int k = 0 ; k<Integer.parseInt(i) ; k++ ){
            ok = ok*10;
        }
        int value = Integer.parseInt(str);
        return  new Double(value/ok).toString();
    }
}


