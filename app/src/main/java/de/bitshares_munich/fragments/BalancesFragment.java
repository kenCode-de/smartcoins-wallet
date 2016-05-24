package de.bitshares_munich.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.BalancesLoad;
import de.bitshares_munich.smartcoinswallet.MainActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.TransactionActivity;
import de.bitshares_munich.smartcoinswallet.pdfTable;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.TinyDB;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate {

    ArrayList<AccountDetails> accountDetails;
    String accountId = "";
    String to ="";
    @Bind(R.id.llBalances)
    LinearLayout llBalances;

    private SortableTableView<TransactionDetails> tableView;

    TinyDB tinyDB;

    @Bind(R.id.account_name)
    TextView tv_account_name;

    public BalancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if (accountDetails.size() == 1) {
            accountDetails.get(0).isSelected = true;
            to = accountDetails.get(0).account_name;
            accountId = accountDetails.get(0).account_id;

        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    to = accountDetails.get(i).account_name;
                    accountId = accountDetails.get(i).account_id;
                    break;
                }
            }
        }

        new AssestsActivty(getContext(),to , this);
        new TransactionActivity(getContext(),accountId , this);
        tv_account_name.setText(to);

        //LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tableView = (SortableTableView<TransactionDetails>) rootView.findViewById(R.id.tableView);
        final View tableViewparent = rootView.findViewById(R.id.tableViewparent);

        // replace myTrabsactions with actual data
        List<TransactionDetails> myTransactions = new ArrayList<>();
        updateSortTableView(tableView,myTransactions);

        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    View scrollViewBalances = rootView.findViewById(R.id.scrollViewBalances);
                    int height1 = scrollViewBalances.getHeight();


                    View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
                    int height2 = transactionsExportHeader.getHeight();

                    tableViewparent.setMinimumHeight(height1 - (height2) );

//                    View childView = tableView.getDataAdapter().getView(0, null, tableView);
//                    childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//                    float height3 = childView.getMeasuredHeight();
//                    if ( height3 > 0 ) {
//                        tableViewparent.setMinimumHeight((int) (height * 5));
//                    }
                }
                catch (Exception e)
                {

                }
            }
        };
        handler.postDelayed(updateTask, 2000);

        // To generate pdf use following cmds
        //TableDataAdapter myAdapter = tableView.getDataAdapter();
        //List<TransactionDetails> det =  myAdapter.getData();
        //pdfTable myTable = new pdfTable(getContext(),getActivity(),"Transactions-scwall");
        //myTable.createTable(det);

        return rootView;
    }

    @OnClick(R.id.recievebtn)
    public void GoToRecieveActivity() {
        Intent intent = new Intent(getActivity(), RecieveActivity.class);
        intent.putExtra(getString(R.string.to),to);
        intent.putExtra(getString(R.string.account_id),accountId);
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

    @OnClick(R.id.exportButton)
    public void onExportButton() {
        TableDataAdapter myAdapter = tableView.getDataAdapter();
        List<TransactionDetails> det =  myAdapter.getData();
        pdfTable myTable = new pdfTable(getContext(),getActivity(),"Transactions-scwall");
        myTable.createTable(det);
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
    }
    public void BalanceAssetsUpdate(final ArrayList<String> sym ,final ArrayList<String> pre ,final ArrayList<String>  am){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                for (int i = 0; i < sym.size(); i+=2) {
                    int counter = 1;
                    int op = sym.size();
                    int pr;
                    if((op-i)>2){
                        pr=2;
                    }else pr=op-i;
                View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
                LinearLayout layout = (LinearLayout) customView;
                LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
                    for(int l = i ; l<i+pr; l++) {
                        if (counter == 1) {
                            TextView textView = (TextView) layout1.getChildAt(0);
                            textView.setText(sym.get(l));
                            TextView textView1 = (TextView) layout1.getChildAt(1);
                            textView1.setText(returnFromPower(pre.get(l), am.get(i)));
                        }
                        if (counter == 2) {
                                TextView textView2 = (TextView) layout1.getChildAt(2);
                                textView2.setText(sym.get(l));
                                TextView textView3 = (TextView) layout1.getChildAt(3);
                                textView3.setText(returnFromPower(pre.get(l), am.get(l)));
                            llBalances.addView(customView);
                        }
                        if (counter == 1 && i == sym.size() - 1) {
                            llBalances.addView(customView);
                        }

                        if (counter == 1) {
                            counter = 2;
                        } else counter = 1;

                    }
                }
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
        Double ok = 1.0;
        Double pre = Double.valueOf(i);
        Double value = Double.valueOf(str);
        for(int k = 0 ; k<pre ; k++ ){
            ok = ok*10;
        }
        return  Double.toString(value/ok);
    }

    public void updateSortTableView (SortableTableView<TransactionDetails> tableView, List<TransactionDetails> myTransactions)
    {
        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), "Date", "S/R", "Details", "Amount");
        simpleTableHeaderAdapter.setPaddingLeft(getResources().getDimensionPixelSize(R.dimen.transactionsheaderpading));
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);

        tableView.setHeaderSortStateViewProvider(SortStateViewProviders.darkArrows());
        tableView.setColumnWeight(0, 17);
        tableView.setColumnWeight(1, 12);
        tableView.setColumnWeight(2, 30);
        tableView.setColumnWeight(3, 20);
        tableView.setColumnComparator(0, new TransactionsDateComparator());
        tableView.setColumnComparator(1, new TransactionsSendRecieveComparator());
        tableView.setColumnComparator(3, new TransactionsAmountComparator());

        tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), myTransactions));

        /*
        TableDataAdapter myAdapter = tableView.getDataAdapter();
        List<TransactionDetails> det =  myAdapter.getData();
        float height = tableView.getHeight();


        for(int l=0; l<=30; l++){

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2016);
            cal.set(Calendar.MONTH, 3);
            cal.set(Calendar.DATE, l);
            cal.set(Calendar.HOUR_OF_DAY, 14);
            cal.set(Calendar.MINUTE, 33);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date myDate = cal.getTime();

            myTransactions.add(new TransactionDetails(myDate,true,"yasir-ibrahim","yasir-mobile","#scwal",(float)l,"OBITS",(float)3.33,"USD"));
        }
        */

    }

    private static class TransactionsDateComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getDate().compareTo(two.getDate());
        }
    }

    private static class TransactionsSendRecieveComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getSent().compareTo(two.getSent());
        }
    }

    private static int compareFloats(float change1, float change2) {
        if (change1 < change2) return -1;
        if (change1 == change2) return 0; // Fails on NaN however, not sure what you want
        if (change2 > change2) return 1;
        return 0;
    }

    private static class TransactionsAmountComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return compareFloats(one.getAmount(),two.getAmount());
        }
    }

}


