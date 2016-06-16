package de.bitshares_munich.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.premnirmal.textcounter.CounterView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.utils.L;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.AccountUpgrade;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.TransactionActivity;
import de.bitshares_munich.smartcoinswallet.pdfTable;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.tableViewClickListener;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate {

    Application application = new Application();
    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");

    Boolean isLoading = false;

    Handler handler = new Handler();

    String to = "";

    String wifkey = "";

    @Bind(R.id.load_more_values)
    Button load_more_values;

    @Bind(R.id.scrollViewBalances)
    ScrollView scrollViewBalances;

    @Bind(R.id.backLine)
    View backLine;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @Bind(R.id.progressBar1)
    ProgressBar progressBar1;

    @Bind(R.id.qrCamera)
    ImageView qrCamera;

    @Bind(R.id.tvBalances)
    TextView tvBalances;

    @Bind(R.id.tvUpgradeLtm)
    TextView tvUpgradeLtm;

    @Bind(R.id.llBalances)
    LinearLayout llBalances;
    int number_of_transactions_loaded;

    @Bind(R.id.whiteSpaceAfterBalances)
    LinearLayout whiteSpaceAfterBalances;

    private SortableTableView<TransactionDetails> tableView;
    static List<TransactionDetails> myTransactions;

    TinyDB tinyDB;

    @Bind(R.id.tableViewparent)
    LinearLayout tableViewparent;

    @Bind(R.id.account_name)
    TextView tvAccountName;

    @Bind(R.id.recievebtn)
    ImageView recievebtn;

    @Bind(R.id.sendbtn)
    ImageView sendbtn;

    @Bind(R.id.ivLifeTime)
    ImageView ivLifeTime;

    @Bind(R.id.ivHelp)
    ImageView ivHelp;

    ProgressDialog progressDialog;

    public BalancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
        application.registerAssetDelegate(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        tvUpgradeLtm.setPaintFlags(tvUpgradeLtm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressDialog = new ProgressDialog(getActivity());
        //recievebtn.setImageBitmap(SupportMethods.highlightImage(20,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_receive)));
        //sendbtn.setImageBitmap(SupportMethods.highlightImage(20,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_send)));
        //qrCamera.setImageBitmap(SupportMethods.highlightImage(7,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_camera)));
        tableView = (SortableTableView<TransactionDetails>) rootView.findViewById(R.id.tableView);
        final View tableViewparent = rootView.findViewById(R.id.tableViewparent);

        // replace myTransactions with actual data
        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    View scrollViewBalances = rootView.findViewById(R.id.scrollViewBalances);
                    int height1 = scrollViewBalances.getHeight();
                    View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
                    int height2 = transactionsExportHeader.getHeight();
                    tableViewparent.setMinimumHeight(height1 - (height2));
                } catch (Exception e) {

                }
            }
        };
        handler.postDelayed(updateTask, 2000);

        loadBasic();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);

        if(checkIfAccountNameChange()){loadBasic();}
    }

    @OnClick(R.id.recievebtn)
    public void GoToRecieveActivity() {
        final Intent intent = new Intent(getActivity(), RecieveActivity.class);
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), accountId);
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        recievebtn.startAnimation(coinAnimation);
    }

    @OnClick(R.id.sendbtn)
    public void GoToSendActivity() {
        if (isLoading) {
            final Intent intent = new Intent(getActivity(), SendScreen.class);
            Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
            coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }
            });
            sendbtn.startAnimation(coinAnimation);
        } else Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.ivHelp)
    public void help() {
        showDialogHelp();
    }

    @OnClick(R.id.tvUpgradeLtm)
    public void updateLtm() {

        final boolean[] balanceValid = {true};
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.txt_confirmation));
        alert.setMessage(getString(R.string.txt_confirmation_msg));
        alert.setPositiveButton(getString(R.string.txt_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


                //Check Balance
                ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);;
                try {
                    for (int i = 0; i < accountDetails.size(); i++) {
                        if (accountDetails.get(i).isSelected) {
                            ArrayList<AccountAssets> arrayListAccountAssets = accountDetails.get(i).AccountAssets;
                            for (int j = 0; j < arrayListAccountAssets.size(); j++) {
                                AccountAssets accountAssets = arrayListAccountAssets.get(j);
                                if (accountAssets.symbol.equalsIgnoreCase("BTS")) {
                                    Double amount = Double.valueOf(SupportMethods.ConvertValueintoPrecision(accountAssets.precision, accountAssets.ammount));
                                    if (amount < 18000) {
                                        balanceValid[0] = false;
                                        Toast.makeText(getActivity(), getString(R.string.insufficient_amount), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                }
                if (balanceValid[0]) {
                    showDialog("", getString(R.string.upgrading));
                    getAccountUpgradeInfo(getActivity());
                }
            }
        });
        alert.setNegativeButton(getString(R.string.txt_no), null);
        alert.show();
    }

    @OnClick(R.id.qrCamera)
    public void QrCodeActivity() {
        if (isLoading) {
            final Intent intent = new Intent(getContext(), qrcodeActivity.class);
            intent.putExtra("id", 1);
            Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
            coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }
            });
            qrCamera.startAnimation(coinAnimation);
        } else Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.exportButton)
    public void onExportButton() {
        if (isLoading) {
            TableDataAdapter myAdapter = tableView.getDataAdapter();
            List<TransactionDetails> det = myAdapter.getData();
            pdfTable myTable = new pdfTable(getContext(), getActivity(), "Transactions-scwall");
            myTable.createTable(det);
        } else Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<AccountAssets> accountAssets = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            AccountAssets accountAsset = new AccountAssets();

            accountAsset.id = ids.get(i);
            if(pre.size()>i) accountAsset.precision = pre.get(i);
            if(sym.size()>i) accountAsset.symbol = sym.get(i);
            if(am.size()>i) accountAsset.ammount = am.get(i);

            SupportMethods.testing("floatDoubleIssue",Float.parseFloat(returnFromPower(pre.get(i), am.get(i))),"txtamount");

            // Log.i("uncle","aay1"+am.get(i));
            accountAssets.add(accountAsset);
        }
        try {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetails.get(i).AccountAssets = accountAssets;
                    break;
                }
            }
        } catch (Exception w) {
            SupportMethods.testing("Assets", w, "Asset Activity");
        }

        SupportMethods.testing("Assets", "Assets views 3", "Asset Activity");

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        SupportMethods.testing("Assets", "Assets views 4", "Asset Activity");
        BalanceAssetsUpdate(sym, pre, am,false);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp)
    {
        int count = llBalances.getChildCount();

        if(count<=0) BalanceAssetsLoad(sym, pre, am);
        if(count>0) BalanceAssetsUpdate(sym, pre, am);
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am)
    {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                SupportMethods.testing("Assets", "Assets views ", "Asset Activity");
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                llBalances.removeAllViews();

                for (int i = 0; i < sym.size(); i += 2) {
                    int counter = 1;
                    int op = sym.size();
                    int pr;

                    if ((op - i) > 2) {
                        pr = 2;
                    } else {
                        pr = op - i;
                    }

                    View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
                    LinearLayout layout = (LinearLayout) customView;
                    LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
                    for (int l = i; l < i + pr; l++) {
                        if (counter == 1) {
                            TextView textView = (TextView) layout1.getChildAt(0);
                            textView.setText(sym.get(l));
                            CounterView textView1 = (CounterView) layout1.getChildAt(1);
                            //inializeCounter(textView1);
                           // textView1.setText(returnFromPower(pre.get(l), am.get(i)));
                           // String r = returnFromPower(pre.get(l), am.get(i));
                            float b = powerInFloat(pre.get(l), am.get(i));
//                            SupportMethods.testing("floatDoubleIssue",Float.parseFloat(r),"3");
//                            Float value = Float.parseFloat(r);
                            setCounter(textView1,0f,0f);

                            setCounter(textView1,b,b);
                        }

                        if (counter == 2) {
                            TextView textView2 = (TextView) layout1.getChildAt(2);
                            textView2.setText(sym.get(l));
                            CounterView textView3 = (CounterView) layout1.getChildAt(3);
                            String r = returnFromPower(pre.get(l), am.get(l));
                           // textView3.setText();
                            setCounter(textView3,0f,0f);
                            setCounter(textView3,Float.parseFloat(r),Float.parseFloat(r));
                            llBalances.addView(customView);
                        }
                        if (counter == 1 && i == sym.size() - 1)
                        {
                            TextView textView2 = (TextView) layout1.getChildAt(2);
                            textView2.setText("");
                            CounterView textView3 = (CounterView) layout1.getChildAt(3);
                            textView3.setVisibility(View.GONE);
                            llBalances.addView(customView);
                        }

                        if (counter == 1) {
                            counter = 2;
                        } else counter = 1;
                    }
                }
                progressBar1.setVisibility(View.GONE);
                whiteSpaceAfterBalances.setVisibility(View.GONE);
                isLoading = true;
            }
        });
    }

//    public void setCounter(CounterView counterView,float sValue , float eValue){
//        if (counterView != null) {
//            counterView.setStartValue(sValue);
//            counterView.setEndValue(eValue);
//            counterView.start();
//        }
//    }
     public void setCounter(CounterView counterView,float sValue , float eValue){
        if (counterView != null) {
            counterView.setAutoStart(false);
            counterView.setAutoFormat(false);
            counterView.setStartValue(sValue);
            counterView.setEndValue(eValue);
            counterView.setIncrement(5f); // the amount the number increments at each time interval
            counterView.setTimeInterval(5); // the time interval (ms) at which the text changes
            counterView.setPrefix("");
            counterView.setSuffix("");
            counterView.start();
        }
    }
    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am)
    {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                int count = llBalances.getChildCount();

//                am.add(0,"9858753");
//                am.add(1,"900009");
//               // am.add(3,"9");
//
//               // am.add(2,"9");
//                am.add("90");
//                pre.add("2");
//                sym.add("KPS");
//                sym.add("yth");
//                sym.add("kolth");
//                sym.add("ythol");
//                sym.add("ythth");

                if(count>0){
                    int m = 0 ;
                    for(int i = 0 ; i < count ; i++){
                        LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            LinearLayout child = (LinearLayout) linearLayout.getChildAt(0);
                            TextView tvSymOne = (TextView)child.getChildAt(0);
                            CounterView tvAmOne = (CounterView)child.getChildAt(1);
                            TextView tvSymtwo = (TextView)child.getChildAt(2);
                            CounterView tvAmtwo = (CounterView) child.getChildAt(3);

                        if(sym.size()>m){

                            String symbol = sym.get(m);
                            String amount = "";
                            if(pre.size()>m && am.size()>m)
                            amount  = returnFromPower(pre.get(m), am.get(m));
                            String txtSymbol = tvSymOne.getText().toString();
                            String txtAmount = tvAmOne.getText().toString();

                            if(!symbol.equals(txtSymbol))
                                tvSymOne.setText(symbol);
                            if(!amount.equals(txtAmount)) {

                                if (Float.parseFloat(txtAmount) > Float.parseFloat(amount)) {
                                    SupportMethods.testing("float",Float.parseFloat(txtAmount),"txtamount");
                                    SupportMethods.testing("float", Float.parseFloat(amount),"amount");
                                    tvAmOne.setTypeface(null, Typeface.BOLD);
                                    tvAmOne.setTextColor(getResources().getColor(R.color.red));
                                }

                                if (Float.parseFloat(amount) > Float.parseFloat(txtAmount)){
                                    tvAmOne.setTypeface(null, Typeface.BOLD);
                                    tvAmOne.setTextColor(getResources().getColor(R.color.green));
                                }

                                setCounter(tvAmOne, Float.parseFloat(txtAmount), Float.parseFloat(amount));
                                final CounterView cView = tvAmOne;
                                final Handler handler = new Handler();

                                final Runnable updateTask = new Runnable() {
                                    @Override
                                    public void run() {
                                        cView.setTypeface(null, Typeface.NORMAL);
                                        cView.setTextColor(getResources().getColor(R.color.green));
                                    }
                                };

                                handler.postDelayed(updateTask, 4000);

                            }
                            m++;
                        }else{
                            linearLayout.removeAllViews();
                        }
                        if(sym.size()>m) {

                            String symbol = sym.get(m);
                            String amount  = "";
                            if(pre.size()>m && am.size()>m)
                            amount  = returnFromPower(pre.get(m), am.get(m));
                            String txtSymbol = tvSymtwo.getText().toString();
                            String txtAmount = tvAmtwo.getText().toString();

                            if(!symbol.equals(txtSymbol))
                                tvSymtwo.setText(symbol);

                            if(!amount.equals(txtAmount)) {
                                //tvAmtwo.setText(amount);
                                tvAmtwo.setVisibility(View.VISIBLE);

                                if(Float.parseFloat(txtAmount)>Float.parseFloat(amount)) {
                                    tvAmtwo.setTextColor(getResources().getColor(R.color.red));
                                    tvAmtwo.setTypeface(null, Typeface.BOLD);

                                }
                                if(Float.parseFloat(amount)>Float.parseFloat(txtAmount))
                                {
                                    tvAmtwo.setTextColor(getResources().getColor(R.color.green));
                                    tvAmtwo.setTypeface(null, Typeface.BOLD);

                                }

                                    setCounter(tvAmtwo,Float.parseFloat(txtAmount),Float.parseFloat(amount));
                                final CounterView cView = tvAmtwo;
                                final Handler handler = new Handler();

                                final Runnable updateTask = new Runnable() {
                                    @Override
                                    public void run() {
                                        cView.setTypeface(null, Typeface.NORMAL);
                                        cView.setTextColor(getResources().getColor(R.color.green));
                                    }
                                };

                                handler.postDelayed(updateTask, 4000);

//                                if(txtAmount.equals("")){
//                                    setCounter(tvAmtwo,Float.parseFloat(amount),Float.parseFloat(amount));
//                                }
                            }

                            m++;
                        }else{
                            if(i == count-1){
                                if(sym.size()>m)
                                    m--;
                            }
                        }
                        }

                    int loop = sym.size()-m;
                    if(loop>0)
                    {
                        for (int i = m; i < sym.size(); i += 2)
                        {
                            int counter = 1;
                            int op = sym.size();
                            int pr;

                            if ((op - i) > 2)
                            {
                                pr = 2;
                            }
                            else
                            {
                                pr = op - i;
                            }

                            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
                            LinearLayout layout = (LinearLayout) customView;
                            LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
                            for (int l = i; l < i + pr; l++)
                            {
                                if (counter == 1)
                                {
                                    TextView textView = (TextView) layout1.getChildAt(0);
                                    textView.setText(sym.get(l));
                                    CounterView textView1 = (CounterView) layout1.getChildAt(1);
                                    if(pre.size()>l && am.size()>i){
//                                        textView1.setText(returnFromPower(pre.get(l), am.get(i)));
                                    String r = returnFromPower(pre.get(l), am.get(i));
                                        setCounter(textView1,0f,0f);
                                    setCounter(textView1,Float.parseFloat(r),Float.parseFloat(r));}
                                    else textView1.setText("");
                                }

                                if (counter == 2)
                                {
                                    TextView textView2 = (TextView) layout1.getChildAt(2);
                                    textView2.setText(sym.get(l));
                                    CounterView textView3 = (CounterView) layout1.getChildAt(3);
                                    if(pre.size()>l && am.size()>l){
                                        String r = returnFromPower(pre.get(l), am.get(l));
                                        setCounter(textView3,0f,0f);
                                    setCounter(textView3,Float.parseFloat(r),Float.parseFloat(r));}

                                    llBalances.addView(customView);
                                }

                                if (counter == 1 && i == sym.size() - 1)
                                {
                                    llBalances.addView(customView);
                                }

                                if (counter == 1)
                                {
                                    counter = 2;
                                }
                                else counter = 1;
                            }
                        }


                    }
                }
                progressBar1.setVisibility(View.GONE);
                whiteSpaceAfterBalances.setVisibility(View.GONE);
                isLoading = true;

            }

        });
    }


    String returnFromPower(String i, String str) {
        Double ok = 1.0;
        Double pre = Double.valueOf(i);
        Double value = Double.valueOf(str);
        for (int k = 0; k < pre; k++) {
            ok = ok * 10;
        }
        return Double.toString(value / ok);
    }
    float powerInFloat(String i, String str) {
        float ok = 1.0f;
        float pre = Float.parseFloat(i);
        float value =  Float.parseFloat(str);
        for (int k = 0; k < pre; k++)
        {
            ok = ok * 10;
        }
        return (value / ok);
    }
    public void updateSortTableView(SortableTableView<TransactionDetails> tableView, List<TransactionDetails> myTransactions) {
        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), getContext().getString(R.string.date), getContext().getString(R.string.all), getContext().getString(R.string.to_from), getContext().getString(R.string.amount));
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
        if (change1 < change2) {
            return -1;
        } else if (change1 == change2) {
            return 0; // Fails on NaN however, not sure what you want
        } else if (change2 > change2) {
            return 1;
        } else {
            return 1;
        }
    }

    private static class TransactionsAmountComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return compareFloats(one.getAmount(), two.getAmount());
        }
    }

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (number_of_transactions_in_queue < 25) {
                    load_more_values.setVisibility(View.GONE);
                } else {
                    load_more_values.setVisibility(View.VISIBLE);
                    load_more_values.setEnabled(true);
                }
                myTransactions.addAll(transactionDetails);
                tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), myTransactions));
                progressBar.setVisibility(View.GONE);
                tableViewparent.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.load_more_values)
    public void Load_more_Values() {
        load_more_values.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        new TransactionActivity(getContext(), accountId, this, wifkey, number_of_transactions_loaded);
        number_of_transactions_loaded = number_of_transactions_loaded + 25;
    }

    void isLifeTime(final String name_id, final String id) {
        try {
            final int db_id = Helper.fetchIntSharePref(getContext(), getContext().getString(R.string.sharePref_database));
            //{"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}

            final Handler handler = new Handler();

            final Runnable updateTask = new Runnable() {
                @Override
                public void run() {
                    if (Application.webSocketG != null && (Application.webSocketG.isOpen())) {
                        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
                        SupportMethods.testing("getLifetime", getDetails, "getDetails");
                        Application.webSocketG.send(getDetails);
                    } else {
                        isLifeTime(name_id, id);

                    }
                }
            };

            handler.postDelayed(updateTask, 1000);
        } catch (Exception e) {

        }
    }

    void get_full_accounts(final String name_id, final String id) {
        try {
            final int db_id = Helper.fetchIntSharePref(getContext(), getContext().getString(R.string.sharePref_database));
            //    {"id":4,"method":"call","params":[2,"get_full_accounts",[["1.2.101520"],true]]}

            final Handler handler = new Handler();

            final Runnable updateTask = new Runnable() {
                @Override
                public void run() {
                    if (Application.webSocketG != null && (Application.webSocketG.isOpen())) {
                        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_full_accounts\",[[\"" + name_id + "\"],true]]}";
                        SupportMethods.testing("get_full_accounts", getDetails, "getDetails");
                        Application.webSocketG.send(getDetails);
                    } else {
                        get_full_accounts(name_id, id);
                    }
                }
            };

            handler.postDelayed(updateTask, 1000);
        } catch (Exception e) {
            SupportMethods.testing("get_full_accounts", e, "exception");
        }
    }

    @Override
    public void getLifetime(String s, int id) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String result = SupportMethods.ParseJsonObject(s, "result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(expiration);
            Date date2 = dateFormat.parse("1969-12-31T23:59:59");
            if (date2.after(date1)) {
                SupportMethods.testing("getLifetime", "true", "s");
                //accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                if (accountDetails.size() > accountDetailsId) {
                    accountDetails.get(accountDetailsId).isLifeTime = true;
                    showHideLifeTime(true);
                } else if (accountDetails.size() == 1) {
                    accountDetails.get(0).isLifeTime = true;
                    showHideLifeTime(true);
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

            } else {
                SupportMethods.testing("getLifetime", "false", "s");
            }
        } catch (Exception e) {
            SupportMethods.testing("getLifetime", e, "Exception");

        }

    }

    void startAnimation() {
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);
        qrCamera.setVisibility(View.INVISIBLE);
        backLine.setVisibility(View.INVISIBLE);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        final Animation animationRigthtoLeft = AnimationUtils.loadAnimation(getContext(), R.anim.home_anim);
        animationRigthtoLeft.setInterpolator(new AccelerateDecelerateInterpolator());
        qrCamera.postDelayed(new Runnable() {
            public void run() {
                qrCamera.startAnimation(animationRigthtoLeft);
                qrCamera.setVisibility(View.VISIBLE);
            }
        }, 333);


        backLine.postDelayed(new Runnable() {
            public void run() {
                backLine.setVisibility(View.VISIBLE);
                backLine.startAnimation(animationFadeIn);
            }
        }, 999);

    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {

            if (qrCamera != null && backLine != null) {
                startAnimation();
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (qrCamera != null && backLine != null) {
                            startAnimation();
                        } else handler.postDelayed(this, 333);
                    }
                }, 333);
            }
        }
    }

    @Override
    public void loadAll() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                loadViews();
            }
        });
    }

    void loadViews() {
        tableViewparent.setVisibility(View.GONE);
        load_more_values.setVisibility(View.GONE);

        myTransactions = new ArrayList<>();
        updateSortTableView(tableView, myTransactions);

       // llBalances.removeAllViews();

        tableView.addDataClickListener(new tableViewClickListener(getContext()));

        progressBar.setVisibility(View.VISIBLE);
        progressBar1.setVisibility(View.VISIBLE);
        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);

        new AssestsActivty(getContext(), to, this);
        number_of_transactions_loaded = 0;
        new TransactionActivity(getContext(), accountId, this, wifkey, number_of_transactions_loaded);
        number_of_transactions_loaded = number_of_transactions_loaded + 25;
    }

    void loadBasic() {
        isLoading = false;
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        if (accountDetails.size() == 1) {
            accountDetailsId = 0;
            accountDetails.get(0).isSelected = true;
            to = accountDetails.get(0).account_name;
            accountId = accountDetails.get(0).account_id;
            wifkey = accountDetails.get(0).wif_key;
            showHideLifeTime(accountDetails.get(0).isLifeTime);

            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetailsId = i;
                    to = accountDetails.get(i).account_name;
                    accountId = accountDetails.get(i).account_id;
                    wifkey = accountDetails.get(i).wif_key;
                    showHideLifeTime(accountDetails.get(0).isLifeTime);
                    break;
                }
            }
        }
        Application.monitorAccountId = accountId;
        tvAccountName.setText(to);
        isLifeTime(accountId, "15");
        get_full_accounts(accountId, "17");

        loadViews();
    }

    Boolean checkIfAccountNameChange() {
        //ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String checkAccountName = "";
        if (accountDetails.size() == 1) {
            checkAccountName = accountDetails.get(0).account_name;
        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    checkAccountName = accountDetails.get(i).account_name;
                    break;
                }
            }
        }
        return !checkAccountName.equals(to);
    }

    private void showDialogHelp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.help_title));
        alert.setMessage(getString(R.string.help_message));
        alert.setPositiveButton(getString(R.string.got_it), null);
        alert.show();
    }

    private void showHideLifeTime(final Boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ivLifeTime.setVisibility(View.VISIBLE);
                    ivHelp.setVisibility(View.GONE);
                    tvUpgradeLtm.setVisibility(View.GONE);

                } else {
                    ivLifeTime.setVisibility(View.GONE);
                    ivHelp.setVisibility(View.VISIBLE);
                    tvUpgradeLtm.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void getAccountUpgradeInfo(final Activity activity) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_upgrade_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account");
        hashMap.put("account", tvAccountName.getText().toString());
        try {
            hashMap.put("wifkey", Crypt.getInstance().decrypt_string(wifkey));
        } catch (Exception e) {
        }

        final Call<AccountUpgrade> postingService = service.getAccountUpgrade(hashMap);
        postingService.enqueue(new Callback<AccountUpgrade>() {
            @Override
            public void onResponse(Response<AccountUpgrade> response) {
                if (response.isSuccess()) {
                    AccountUpgrade accountDetails = response.body();
                    if (accountDetails.status.equals("success")) {
                        loadBasic();
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                    } else {
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideDialog() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                }
            }
        });
    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }


}


