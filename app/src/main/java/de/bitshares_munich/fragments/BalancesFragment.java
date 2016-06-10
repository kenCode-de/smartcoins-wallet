package de.bitshares_munich.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.TransactionActivity;
import de.bitshares_munich.smartcoinswallet.pdfTable;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.tableViewClickListener;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;

/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate {
    Application application = new Application();
    //private static ArrayList<AccountDetails> accountDetails;
    int accountDetailsId;
    String accountId = "";

    Boolean isLoading = false;

    Handler handler = new Handler();
    String to ="";

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
    TextView tv_account_name;

    @Bind(R.id.recievebtn)
    ImageView recievebtn;


    @Bind(R.id.sendbtn)
    ImageView sendbtn;

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

//        recievebtn.setImageBitmap(SupportMethods.highlightImage(20,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_receive)));
//        sendbtn.setImageBitmap(SupportMethods.highlightImage(20,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_send)));
//        qrCamera.setImageBitmap(SupportMethods.highlightImage(7,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_camera)));

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


//        tableViewparent.setVisibility(View.GONE);
//        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
//        if (accountDetails.size() == 1) {
//            accountDetails.get(0).isSelected = true;
//            to = accountDetails.get(0).account_name;
//            accountId = accountDetails.get(0).account_id;
//            wifkey = accountDetails.get(0).wif_key;
//        } else {
//            for (int i = 0; i < accountDetails.size(); i++) {
//                if (accountDetails.get(i).isSelected) {
//                    accountDetailsId = i;
//                    to = accountDetails.get(i).account_name;
//                    accountId = accountDetails.get(i).account_id;
//                    wifkey = accountDetails.get(i).wif_key;
//                    break;
//                }
//            }
//        }
//        isLifeTime(accountId,"15");
//        load_more_values.setVisibility(View.GONE);
//        new AssestsActivty(getContext(), to, this);
//        number_of_transactions_loaded = 0;
//        new TransactionActivity(getContext(), accountId, this, wifkey, number_of_transactions_loaded);
//        number_of_transactions_loaded = number_of_transactions_loaded + 25;
//        tv_account_name.setText(to);
//
//        //LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        tableView = (SortableTableView<TransactionDetails>) rootView.findViewById(R.id.tableView);
//        final View tableViewparent = rootView.findViewById(R.id.tableViewparent);
//
//        // replace myTransactions with actual data
//        myTransactions = new ArrayList<>();
//        updateSortTableView(tableView, myTransactions);
//
//
//        tableView.addDataClickListener(new tableViewClickListener(getContext()));
//
//        final Handler handler = new Handler();
//
//        final Runnable updateTask = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    View scrollViewBalances = rootView.findViewById(R.id.scrollViewBalances);
//                    int height1 = scrollViewBalances.getHeight();
//                    View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
//                    int height2 = transactionsExportHeader.getHeight();
//                    tableViewparent.setMinimumHeight(height1 - (height2));
//                } catch (Exception e) {
//
//                }
//            }
//        };
//        handler.postDelayed(updateTask, 2000);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);


//        recievebtn.setImageBitmap(SupportMethods.highlightImage(15,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_receive)));
//        sendbtn.setImageBitmap(SupportMethods.highlightImage(15,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_send)));
//        qrCamera.setImageBitmap(SupportMethods.highlightImage(7,BitmapFactory.decodeResource(getResources(), R.mipmap.icon_camera)));

        if(checkIfAccountNameChange()){loadBasic();}
//        tableViewparent.setVisibility(View.GONE);
//        load_more_values.setVisibility(View.GONE);
        //for(int i=0;i<3;i++){
       // final View rootView = layoutInflater.inflate(R.layout.fragment_balances, null, false);

//        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
//        if (accountDetails.size() == 1) {
//            accountDetails.get(0).isSelected = true;
//            to = accountDetails.get(0).account_name;
//            accountId = accountDetails.get(0).account_id;
//            wifkey = accountDetails.get(0).wif_key;
//        } else {
//            for (int i = 0; i < accountDetails.size(); i++) {
//                if (accountDetails.get(i).isSelected) {
//                    accountDetailsId = i;
//                    to = accountDetails.get(i).account_name;
//                    accountId = accountDetails.get(i).account_id;
//                    wifkey = accountDetails.get(i).wif_key;
//                    break;
//                }
//            }
//        }
//        Application.monitorAccountId = accountId;
//        tv_account_name.setText(to);
//        isLifeTime(accountId,"15");
//        get_full_accounts(accountId,"17");


//        myTransactions = new ArrayList<>();
//        updateSortTableView(tableView, myTransactions);
//
//        llBalances.removeAllViews();
//
//        tableView.addDataClickListener(new tableViewClickListener(getContext()));
//
//        progressBar1.setVisibility(View.VISIBLE);
//        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
//
//        new AssestsActivty(getContext(), to, this);
//        number_of_transactions_loaded = 0;
//        progressBar.setVisibility(View.VISIBLE);
//        new TransactionActivity(getContext(), accountId, this, wifkey, number_of_transactions_loaded);
//        number_of_transactions_loaded = number_of_transactions_loaded + 25;

        //LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        if(isLoading){
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
        }
        else Toast.makeText(getContext(),R.string.loading_msg,Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.qrCamera)
    public void QrCodeActivity() {
        if(isLoading){
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
        }
        else Toast.makeText(getContext(),R.string.loading_msg,Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.exportButton)
    public void onExportButton() {
        if(isLoading){
        TableDataAdapter myAdapter = tableView.getDataAdapter();
        List<TransactionDetails> det = myAdapter.getData();
        pdfTable myTable = new pdfTable(getContext(), getActivity(), "Transactions-scwall");
        myTable.createTable(det);}
        else Toast.makeText(getContext(),R.string.loading_msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        SupportMethods.testing("Assets","Assets views 1","Asset Activity");

        ArrayList<AccountAssets> accountAssets = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            AccountAssets accountAsset = new AccountAssets();

            accountAsset.id = ids.get(i);
            accountAsset.precision = pre.get(i);
            accountAsset.symbol = sym.get(i);
            accountAsset.ammount = am.get(i);

            // Log.i("uncle","aay1"+am.get(i));

            accountAssets.add(accountAsset);
        }

        SupportMethods.testing("Assets","Assets views 2","Asset Activity");

//       if(accountDetails.size()==1) {
//            accountDetails.get(0).isSelected = true;
//            accountDetails.get(0).AccountAssets = accountAssets;
//       } else {
        try{
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                accountDetails.get(i).AccountAssets = accountAssets;
                break;
            }
        }}catch (Exception w){
            SupportMethods.testing("Assets",w,"Asset Activity");

        }
        SupportMethods.testing("Assets","Assets views 3","Asset Activity");

//       }
        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

        SupportMethods.testing("Assets","Assets views 4","Asset Activity");

        BalanceAssetsUpdate(sym, pre, am);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                SupportMethods.testing("Assets","Assets views ","Asset Activity");

                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                llBalances.removeAllViews();
                for (int i = 0; i < sym.size(); i += 2) {
                    int counter = 1;
                    int op = sym.size();
                    int pr;
                    if ((op - i) > 2) {
                        pr = 2;
                    } else pr = op - i;
                    View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
                    LinearLayout layout = (LinearLayout) customView;
                    LinearLayout layout1 = (LinearLayout) layout.getChildAt(0);
                    for (int l = i; l < i + pr; l++) {
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
                progressBar1.setVisibility(View.GONE);
//                tvBalances.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
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

    public void updateSortTableView(SortableTableView<TransactionDetails> tableView, List<TransactionDetails> myTransactions) {
        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), getContext().getString(R.string.date), getContext().getString(R.string.all),getContext().getString(R.string.to_from), getContext().getString(R.string.amount));
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
//                        Handler ha = new Handler();
//                        ha.postDelayed(new Runnable() {
//                         @Override
//                            public void run() {
//                             scrollViewBalances.fullScroll(View.FOCUS_UP);
//                             scrollViewBalances.pageScroll(View.FOCUS_UP);
//                               }}, 30000);
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
            //    {"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}

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
        }catch (Exception e){
            
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
                        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_full_accounts\",[[\""+name_id+"\"],true]]}";
                        SupportMethods.testing("get_full_accounts", getDetails, "getDetails");
                        Application.webSocketG.send(getDetails);
                    } else {
                        get_full_accounts(name_id, id);

                    }
                }
            };

            handler.postDelayed(updateTask, 1000);
        }catch (Exception e){
            SupportMethods.testing("get_full_accounts", e, "exception");

        }
    }
    @Override
    public void getLifetime(String s,int id){
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String result = SupportMethods.ParseJsonObject(s,"result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result,0);
        String expiration = SupportMethods.ParseJsonObject(nameObject,"membership_expiration_date");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(expiration);
            Date date2 = dateFormat.parse("1969-12-31T23:59:59");
            if (date2.after(date1)) {
                SupportMethods.testing("getLifetime","true","s");
                //accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                if(accountDetails.size()>accountDetailsId) accountDetails.get(accountDetailsId).isLifeTime = true;
                else if(accountDetails.size() == 1) {
                    accountDetails.get(0).isLifeTime = true;
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

            } else {

                SupportMethods.testing("getLifetime","false","s");

            }
        }catch (Exception e){
            SupportMethods.testing("getLifetime",e,"Exception");

        }

    }
    void startAnimation(){
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
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        if (visible){

            if(qrCamera!=null && backLine!=null) {
                startAnimation();
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(qrCamera!=null && backLine!=null) {
                            startAnimation();
                        }else handler.postDelayed(this, 333);
                    }
                }, 333);
            }
        }
    }

    @Override
    public void loadAll(){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
        loadViews();
            }
        });
    }

    void loadViews(){
        tableViewparent.setVisibility(View.GONE);
        load_more_values.setVisibility(View.GONE);

        myTransactions = new ArrayList<>();
        updateSortTableView(tableView, myTransactions);

        llBalances.removeAllViews();

        tableView.addDataClickListener(new tableViewClickListener(getContext()));

        progressBar.setVisibility(View.VISIBLE);
        progressBar1.setVisibility(View.VISIBLE);
        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);

        new AssestsActivty(getContext(), to, this);
        number_of_transactions_loaded = 0;
        new TransactionActivity(getContext(), accountId, this, wifkey, number_of_transactions_loaded);
        number_of_transactions_loaded = number_of_transactions_loaded + 25;
//        Handler ha = new Handler();
//        ha.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                loadViews();
//            }}, 30000);
    }
    void loadBasic(){
        isLoading = false;
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        //accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        if (accountDetails.size() == 1) {
            accountDetailsId = 0;
            accountDetails.get(0).isSelected = true;
            to = accountDetails.get(0).account_name;
            accountId = accountDetails.get(0).account_id;
            wifkey = accountDetails.get(0).wif_key;
            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetailsId = i;
                    to = accountDetails.get(i).account_name;
                    accountId = accountDetails.get(i).account_id;
                    wifkey = accountDetails.get(i).wif_key;
                    break;
                }
            }
        }
        Application.monitorAccountId = accountId;
        tv_account_name.setText(to);
        isLifeTime(accountId,"15");
        get_full_accounts(accountId,"17");

        loadViews();
    }
    Boolean checkIfAccountNameChange(){
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String checkAccountName="";
        if (accountDetails.size() == 1) {
            checkAccountName = accountDetails.get(0).account_name;
        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    checkAccountName= accountDetails.get(i).account_name;
                    break;
                }
            }
        }
        return !checkAccountName.equals(to);
    }

}


