package de.bitshares_munich.fragments;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.premnirmal.textcounter.CounterView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.ISound;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.AccountUpgrade;
import de.bitshares_munich.models.CCAssets;
import de.bitshares_munich.models.EquivalentComponentResponse;
import de.bitshares_munich.models.LtmFee;
import de.bitshares_munich.models.Smartcoin;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.models.Uia;
import de.bitshares_munich.models.transactionsJsonSerializable;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.AssetsSymbols;
import de.bitshares_munich.smartcoinswallet.ListViewActivity;
import de.bitshares_munich.smartcoinswallet.MediaService;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.TransactionActivity;
import de.bitshares_munich.smartcoinswallet.pdfTable;
import de.bitshares_munich.smartcoinswallet.popUpwindow;
import de.bitshares_munich.smartcoinswallet.qrcodeActivity;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.PermissionManager;
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
public class BalancesFragment extends Fragment implements AssetDelegate ,ISound{

    Application application = new Application();
    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");

    Boolean isLoading = false;
    public static Boolean onClicked = false;
    Handler myHandler = new Handler();

    String to = "";

    String wifkey = "";
    String finalFaitCurrency;

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
    static ArrayList<TransactionDetails> myTransactions;

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

    @Bind(R.id.ivMultiAccArrow)
    ImageView ivMultiAccArrow;

    ProgressDialog progressDialog;

    Boolean sentCallForTransactions = false;
    Boolean isSavedTransactions = false;


    Locale locale;
    NumberFormat format;
    String language;

    public static ISound iSound;
    // String ltmAmount="17611.7";

    public BalancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
        application.registerAssetDelegate(this);
        iSound=this;
        updateEquivalentAmount = new Handler();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        language = Helper.fetchStringSharePref(getActivity(), getString(R.string.pref_language));
        locale = new Locale(language);
        format = NumberFormat.getInstance(locale);
        tvUpgradeLtm.setPaintFlags(tvUpgradeLtm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressDialog = new ProgressDialog(getActivity());
        tableView = (SortableTableView<TransactionDetails>) rootView.findViewById(R.id.tableView);
        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.getAssetsFromServer();
       // getAssetsFromServer();
        final Handler handler = new Handler();
        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setSortableTableViewHeight(rootView, handler, this);
                    }
                });


            }
        };

        final Runnable createFolder = new Runnable() {
            @Override
            public void run() {
                createFolder();
            }
        };


        loadBasic(false,true,false);
        loadBalancesFromSharedPref();
        TransactionUpdateOnStartUp();

        handler.postDelayed(updateTask, 2000);

        handler.postDelayed(createFolder, 5000);
        if (!Helper.containKeySharePref(getActivity(), "ltmAmount")) {
            Helper.storeStringSharePref(getActivity(), "ltmAmount", "17611.7");
        }
        getLtmPrice(getActivity(), tvAccountName.getText().toString());
        return rootView;
    }

    private void setSortableTableViewHeight(View rootView, Handler handler, Runnable task) {
        try {
            View scrollViewBalances = rootView.findViewById(R.id.scrollViewBalances);
            int height1 = scrollViewBalances.getHeight();

            if (height1 == 0) {
                handler.postDelayed(task, 2000);
                return;
            }

            Log.d("setSortableHeight", "Scroll Heght : " + Long.toString(height1));
            View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
            int height2 = transactionsExportHeader.getHeight();
            Log.d("setSortableHeight", "Scroll Header Heght : " + Long.toString(height2));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tableView.getLayoutParams();
            params.height = height1 - height2;
            Log.d("setSortableHeight", "View Heght : " + Long.toString(params.height));
            tableViewparent.setLayoutParams(params);
            Log.d("setSortableHeight", "View Heght Set");
        } catch (Exception e) {
            Log.d("List Height", e.getMessage());
            handler.postDelayed(task, 2000);
        }
    }

    private void createFolder() {
        try {
            PermissionManager manager = new PermissionManager();
            manager.verifyStoragePermissions(getActivity());

            final File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.folder_name));

            boolean success = false;

            if (!folder.exists()) {
                success = folder.mkdir();
            }

            if (success) {
                // Do something on success
                Toast.makeText(getContext(), getResources().getString(R.string.txt_folder_created) + " : " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file2 = new File(folder.getAbsolutePath(), "Woohoo.wav");

                        if (!file2.exists()) {
                            FileOutputStream save = new FileOutputStream(file2);

                            byte[] buffer = null;
                            InputStream fIn = getResources().openRawResource(R.raw.woohoo);
                            int size = 0;

                            try {
                                size = fIn.available();
                                buffer = new byte[size];
                                fIn.read(buffer);
                                fIn.close();
                                save.write(buffer);
                                //save.flush();
                                //save.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                            }

                            save.flush();
                            save.close();
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }catch (Exception e){}
    }

    @Override
    public void onResume() {
        super.onResume();
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);
        onClicked = false;
        final String hide_donations_isChanged = "hide_donations_isChanged";
        Boolean isHideDonationsChanged = false;
        if (Helper.containKeySharePref(getContext(), hide_donations_isChanged)) {
            if (Helper.fetchBoolianSharePref(getContext(), hide_donations_isChanged)) {
                isHideDonationsChanged = true;
                Helper.storeBoolianSharePref(getContext(), hide_donations_isChanged, false);
            }
        }
        Boolean isCheckedTimeZone=false;
        isCheckedTimeZone=Helper.fetchBoolianSharePref(getActivity(),getString(R.string.pre_ischecked_timezone));
        Boolean accountNameChange = checkIfAccountNameChange();

        if(accountNameChange || (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency)) )
            llBalances.removeAllViews();

        if (isCheckedTimeZone || isHideDonationsChanged || accountNameChange || (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency)) )
        {
            if (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency) )
            {
                loadBasic(true,accountNameChange,true);
            }
            else
            {
                loadBasic(true,accountNameChange,false);
            }

        }
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

    @OnClick(R.id.tvUpgradeLtm)
    public void updateLtm() {

        final boolean[] balanceValid = {true};
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.alert_delete_dialog);
        final Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final TextView alertMsg = (TextView) dialog.findViewById(R.id.alertMsg);
        alertMsg.setText(getString(R.string.help_message));
        final Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setBackgroundColor(Color.RED);
        btnCancel.setText(getString(R.string.txt_no));
        btnDone.setText(getString(R.string.next));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View v) {
                String ltmAmount=Helper.fetchStringSharePref(getActivity(),"ltmAmount");
                //Check Balance
                if (btnDone.getText().equals(getString(R.string.next))) {
                    alertMsg.setText("Upgrade to LTM now? " + ltmAmount + " BTS will be deducted from " + tvAccountName.getText().toString() + " account.");
                    btnDone.setText(getString(R.string.txt_yes));
                    btnCancel.setText(getString(R.string.txt_back));
                } else {
                    dialog.cancel();
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

                    try {
                        for (int i = 0; i < accountDetails.size(); i++) {
                            if (accountDetails.get(i).isSelected) {
                                ArrayList<AccountAssets> arrayListAccountAssets = accountDetails.get(i).AccountAssets;
                                for (int j = 0; j < arrayListAccountAssets.size(); j++) {
                                    AccountAssets accountAssets = arrayListAccountAssets.get(j);
                                    if (accountAssets.symbol.equalsIgnoreCase("BTS")) {
                                        Double amount = Double.valueOf(SupportMethods.ConvertValueintoPrecision(accountAssets.precision, accountAssets.ammount));
                                        if (amount < Double.parseDouble(ltmAmount)) {
                                            balanceValid[0] = false;
                                            Toast.makeText(getActivity(), getString(R.string.insufficient_funds), Toast.LENGTH_LONG).show();
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
                        getAccountUpgradeInfo(getActivity(), tvAccountName.getText().toString());
                    }

                }
            }

        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCancel.getText().equals(getString(R.string.txt_back))) {
                    alertMsg.setText(getString(R.string.help_message));
                    btnCancel.setText(getString(R.string.txt_no));
                    btnDone.setText(getString(R.string.next));
                } else {
                    dialog.cancel();
                }
            }
        });
        dialog.show();
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

    public void loadBalancesFromSharedPref() {
        try {
            ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

            if (accountDetails.size() > 1) {
                ivMultiAccArrow.setVisibility(View.VISIBLE);
            } else {
                ivMultiAccArrow.setVisibility(View.GONE);
            }


            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected)
                {
                    ArrayList<AccountAssets> accountAsset = accountDetails.get(i).AccountAssets;

                    if ((accountAsset != null) && (accountAsset.size() > 0)) {
                        ArrayList<String> sym = new ArrayList<>();
                        ArrayList<String> pre = new ArrayList<>();
                        ArrayList<String> am = new ArrayList<>();

                        for (int j = 0; j < accountAsset.size(); j++) {
                            pre.add(j, accountAsset.get(j).precision);
                            sym.add(j, accountAsset.get(j).symbol);
                            am.add(j, accountAsset.get(j).ammount);
                        }

               //         getEquivalentComponents(accountAsset);


                        BalanceAssetsUpdate(sym, pre, am, true);
                    }

                    break;
                }
            }
        } catch (Exception e) {

        }


    }

    Handler updateEquivalentAmount;

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<AccountAssets> accountAssets = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++)
        {
            long amount = Long.parseLong(am.get(i));

            if ( amount != 0 )
            {
                AccountAssets accountAsset = new AccountAssets();

                accountAsset.id = ids.get(i);
                if (pre.size() > i) accountAsset.precision = pre.get(i);
                if (sym.size() > i) accountAsset.symbol = sym.get(i);
                if (am.size() > i) accountAsset.ammount = am.get(i);

                accountAssets.add(accountAsset);
            }
        }

        try
        {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetails.get(i).AccountAssets = accountAssets;
                    getEquivalentComponents(accountAssets);
                    break;
                }
            }
        }
        catch (Exception w)
        {
            SupportMethods.testing("Assets", w, "Asset Activity");
        }

        SupportMethods.testing("Assets", "Assets views 3", "Asset Activity");

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        SupportMethods.testing("Assets", "Assets views 4", "Asset Activity");
        BalanceAssetsUpdate(sym, pre, am, false);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {
        int count = llBalances.getChildCount();

        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        ArrayList<String> symbols = assetsSymbols.updatedList(sym);


        if (count <= 0) BalanceAssetsLoad(symbols, pre, am, onStartUp);
        if (count > 0) BalanceAssetsUpdate(symbols, pre, am);

        /*
        Handler zeroBalanceHandler = new Handler();

        Runnable zeroKardo = new Runnable() {
            @Override
            public void run() {

                am.set(0,"0");

                BalanceAssetsUpdate(sym, pre, am, false);
            }
        };

        zeroBalanceHandler.postDelayed(zeroKardo,10000);
        */

    }


    private void getEquivalentComponents(final ArrayList<AccountAssets> accountAssets)
    {

        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());

        final Runnable getEquivalentCompRunnable = new Runnable() {
            @Override
            public void run() {
                getEquivalentComponents(accountAssets);
            }
        };

        String faitCurrency = Helper.getFadeCurrency(getContext());

        if (faitCurrency.isEmpty())
        {
            faitCurrency = "EUR";
        }

        final String fc = faitCurrency;

        final List<String> pairs = new ArrayList<>();
        String values = "";

        for (int i = 0; i < accountAssets.size(); i++) {
            AccountAssets accountAsset = accountAssets.get(i);
            if (!accountAsset.symbol.equals(faitCurrency)) {
                values += accountAsset.symbol + ":" + faitCurrency + ",";
                pairs.add(accountAsset.symbol + ":" + faitCurrency);
            }
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "equivalent_component");
        hashMap.put("values", values.substring(0, values.length() - 1));

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);
        finalFaitCurrency = faitCurrency;
        postingService.enqueue(new Callback<EquivalentComponentResponse>() {
            @Override
            public void onResponse(Response<EquivalentComponentResponse> response)
            {
                if (response.isSuccess())
                {
                    EquivalentComponentResponse resp = response.body();
                    if (resp.status.equals("success"))
                    {
                        try
                        {
                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            HashMap hm = new HashMap();

                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                hm.put(key.split(":")[0], rates.get(key));

                                if ( pairs.contains(key) )
                                {
                                    pairs.remove(key);
                                }
                            }


                            if ( pairs.size() > 0 )
                            {
                                getEquivalentComponentsIndirect(pairs,fc);
                            }

                            for (int i = 0; i < llBalances.getChildCount(); i++)
                            {
                                LinearLayout llRow = (LinearLayout) llBalances.getChildAt(i);

                                for (int j = 1; j <= 2; j++) {

                                    TextView tvAsset;
                                    TextView tvAmount;
                                    TextView tvFaitAmount;

                                    if (j == 1)
                                    {
                                        tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_one);
                                        tvAmount = (TextView) llRow.findViewById(R.id.amount_child_one);
                                        tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_one);
                                    }
                                    else
                                    {
                                        tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_two);
                                        tvAmount = (TextView) llRow.findViewById(R.id.amount_child_two);
                                        tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_two);
                                    }

                                    if (tvAsset == null || tvAmount == null || tvFaitAmount == null)
                                    {
                                        updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                                        return;
                                    }
                                    String asset = tvAsset.getText().toString();
                                    String amount = tvAmount.getText().toString();
                                    asset = asset.replace("bit","");
                                    //amount = android.text.Html.fromHtml(amount).toString();

                                    if (amount.isEmpty())
                                    {
                                        amount = "0.0";
                                    }

                                    if (!amount.isEmpty() && hm.containsKey(asset))
                                    {
                                        Currency currency = Currency.getInstance(finalFaitCurrency);

                                        try
                                        {
                                            double d = convertLocalizeStringToDouble(amount);
                                            Double eqAmount = d * convertLocalizeStringToDouble(hm.get(asset).toString());

                                            if ( Helper.isRTL(locale,currency.getSymbol()) )
                                            {
                                                tvFaitAmount.setText(String.format(locale, "%.2f %s", eqAmount,currency.getSymbol()));
                                            }
                                            else
                                            {
                                                tvFaitAmount.setText(String.format(locale, "%s %.2f", currency.getSymbol(),eqAmount));
                                            }

                                            tvFaitAmount.setVisibility(View.VISIBLE);

                                        }
                                        catch (Exception e)
                                        {
                                            tvFaitAmount.setVisibility(View.GONE);
                                        }
                                    }
                                    else
                                    {
                                        tvFaitAmount.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                        catch (JSONException e)
                        {
                            //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                            e.printStackTrace();
                        }
                    }
                    /*
                    else
                    {
                        //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                        //Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }
                    */
                }
                else
                {
                    hideDialog();
                    Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                //Toast.makeText(getActivity(), getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
            }
        });
    }

    private void getEquivalentComponentsIndirect(final List<String> leftOvers, final String faitCurrency)
    {
        try {
            final Runnable getEquivalentCompIndirectRunnable = new Runnable() {
                @Override
                public void run() {
                    getEquivalentComponentsIndirect(leftOvers, faitCurrency);
                }
            };

            List<String> newPairs = new ArrayList<>();

            for (String pair : leftOvers) {
                String firstHalf = pair.split(":")[0];
                newPairs.add(firstHalf + ":" + "BTS");
            }

            newPairs.add("BTS" + ":" + faitCurrency);

            String values = "";

            for (String pair : newPairs) {
                values += pair + ",";
            }

            values = values.substring(0, values.length() - 1);

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("method", "equivalent_component");
            hashMap.put("values", values);

            ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
            IWebService service = sg.getService(IWebService.class);
            final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);

            postingService.enqueue(new Callback<EquivalentComponentResponse>() {
                @Override
                public void onResponse(Response<EquivalentComponentResponse> response) {
                    if (response.isSuccess()) {
                        EquivalentComponentResponse resp = response.body();
                        if (resp.status.equals("success")) {
                            try {
                                JSONObject rates = new JSONObject(resp.rates);
                                Iterator<String> keys = rates.keys();
                                String btsToFait = "";

                                while (keys.hasNext()) {
                                    String key = keys.next();

                                    if (key.equals("BTS:" + faitCurrency)) {
                                        btsToFait = rates.get("BTS:" + faitCurrency).toString();
                                        break;
                                    }
                                }

                                HashMap hm = new HashMap();

                                if (!btsToFait.isEmpty()) {
                                    keys = rates.keys();


                                    while (keys.hasNext()) {
                                        String key = keys.next();

                                        if (!key.equals("BTS:" + faitCurrency)) {
                                            String asset = key.split(":")[0];

                                            String assetConversionToBTS = rates.get(key).toString();

                                            double newConversionRate = convertLocalizeStringToDouble(assetConversionToBTS) * convertLocalizeStringToDouble(btsToFait);

                                            String assetToFaitConversion = Double.toString(newConversionRate);

                                            hm.put(asset, assetToFaitConversion);
                                        }
                                    }
                                }


                                for (int i = 0; i < llBalances.getChildCount(); i++) {
                                    LinearLayout llRow = (LinearLayout) llBalances.getChildAt(i);

                                    for (int j = 1; j <= 2; j++) {

                                        TextView tvAsset;
                                        TextView tvAmount;
                                        TextView tvFaitAmount;

                                        if (j == 1) {
                                            tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_one);
                                            tvAmount = (TextView) llRow.findViewById(R.id.amount_child_one);
                                            tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_one);
                                        } else {
                                            tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_two);
                                            tvAmount = (TextView) llRow.findViewById(R.id.amount_child_two);
                                            tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_two);
                                        }

                                        if (tvAsset == null || tvAmount == null || tvFaitAmount == null) {
                                            updateEquivalentAmount.postDelayed(getEquivalentCompIndirectRunnable, 500);
                                            return;
                                        }

                                        String asset = tvAsset.getText().toString();
                                        String amount = tvAmount.getText().toString();
                                        asset = asset.replace("bit","");

                                        if (!amount.isEmpty() && hm.containsKey(asset)) {
                                            Currency currency = Currency.getInstance(faitCurrency);

                                            try {
                                                double d = convertLocalizeStringToDouble(amount);
                                                Double eqAmount = d * convertLocalizeStringToDouble(hm.get(asset).toString());

                                                if (Helper.isRTL(locale,currency.getSymbol())) {
                                                    tvFaitAmount.setText(String.format(locale, "%.2f %s", eqAmount, currency.getSymbol()));
                                                } else {
                                                    tvFaitAmount.setText(String.format(locale, "%s %.2f", currency.getSymbol(), eqAmount));
                                                }

                                                tvFaitAmount.setVisibility(View.VISIBLE);

                                            } catch (Exception e) {
                                                tvFaitAmount.setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                }


                            } catch (JSONException e) {
                                //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                                e.printStackTrace();
                            }
                        }
                    /*
                    else
                    {
                        //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                        //Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }
                    */
                    } else {
                        hideDialog();
                        Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                        //updateEquivalentAmount.postDelayed(getEquivalentCompRunnable,500);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    hideDialog();
                    //Toast.makeText(getActivity(), getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                    updateEquivalentAmount.postDelayed(getEquivalentCompIndirectRunnable, 500);
                }
            });
        }
        catch (Exception e)
        {

        }

    }

    ArrayList<String> symbolsArray;
    ArrayList<String> precisionsArray;
    ArrayList<String> amountsArray;

    private void updateBalanceArrays (final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am)
    {
        try
        {
            symbolsArray = new ArrayList<>();
            precisionsArray = new ArrayList<>();
            amountsArray = new ArrayList<>();

            for( int i = 0 ; i < sym.size() ; i++ )
            {
                Long _amount = Long.parseLong(am.get(i));

                // remove balances which are zero
                if ( _amount != 0 )
                {
                    amountsArray.add(am.get(i));
                    precisionsArray.add(pre.get(i));
                    symbolsArray.add(sym.get(i));
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {

        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());


        updateBalanceArrays( sym,pre,am );

        sym.clear();
        sym.addAll(symbolsArray);

        pre.clear();
        pre.addAll(precisionsArray);

        am.clear();
        am.addAll(amountsArray);


        getActivity().runOnUiThread(new Runnable()
        {
            public void run()
            {
                SupportMethods.testing("Assets", "Assets views ", "Asset Activity");
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                llBalances.removeAllViews();

                for (int i = 0; i < sym.size(); i += 2)
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
                    for (int l = i; l < i + pr; l++)
                    {
                        if (counter == 1)
                        {
                            TextView textView = (TextView) customView.findViewById(R.id.symbol_child_one);
                            textView.setText(sym.get(l));
                            TextView textView1 = (TextView) customView.findViewById(R.id.amount_child_one);

                            float b = powerInFloat(pre.get(l), am.get(i));

                            if(assetsSymbols.isUiaSymbol(sym.get(l))) textView1.setText(String.format(locale, "%.4f", b));
                            else if(assetsSymbols.isSmartCoinSymbol(sym.get(l))) textView1.setText(String.format(locale, "%.2f", b));
                        }

                        if (counter == 2)
                        {
                            TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                            textView2.setText(sym.get(l));
                            TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                            String r = returnFromPower(pre.get(l), am.get(l));

                            if(assetsSymbols.isUiaSymbol(sym.get(l))) textView3.setText(String.format(locale, "%.4f",Float.parseFloat(r)));
                            else if(assetsSymbols.isSmartCoinSymbol(sym.get(l))) textView3.setText(String.format(locale, "%.2f", Float.parseFloat(r)));

                            llBalances.addView(customView);
                        }

                        if (counter == 1 && i == sym.size() - 1)
                        {
                            TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                            textView2.setText("");
                            TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                            textView3.setVisibility(View.GONE);
                            llBalances.addView(customView);
                        }

                        if (counter == 1)
                        {
                            counter = 2;
                        }
                        else counter = 1;
                    }
                }

                if (!onStartUp)
                {
                    progressBar1.setVisibility(View.GONE);
                    isLoading = true;
                }
                else
                {
                    try
                    {
                        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                        for (int i = 0; i < accountDetails.size(); i++)
                        {
                            if (accountDetails.get(i).isSelected)
                            {
                                getEquivalentComponents(accountDetails.get(i).AccountAssets);
                                break;
                            }
                        }
                    } catch (Exception w) {
                        SupportMethods.testing("Assets", w, "Asset Activity");
                    }
                }

                whiteSpaceAfterBalances.setVisibility(View.GONE);
            }
        });
    }

  /*  public void setCounter(CounterView counterView, float sValue, float eValue) {
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
            counterView.setTextLocale(locale);
        }
    }*/

    private void rotateRecieveButton() {
        ImageView rcvBtn = (ImageView) getActivity().findViewById(R.id.recievebtn);
        final Animation rotAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate360);
        rcvBtn.startAnimation(rotAnim);
    }

    public void playSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.woohoo);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateText(final TextView tvCounter, float startValue, float endValue) {
        ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(startValue, endValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animateValue = Float.parseFloat(String.valueOf(animation.getAnimatedValue()));
                tvCounter.setText(Helper.setLocaleNumberFormat(locale, animateValue));
            }
        });
        animator.setEvaluator(new TypeEvaluator<Float>() {
            public Float evaluate(float fraction, Float startValue, Float endValue) {
                return startValue + (endValue - startValue) * fraction;
            }
        });
        animator.setDuration(2000);
        animator.start();
    }

    public void removeZeroedBalanceViews ()
    {

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {

                    for (int i = 0; i < llBalances.getChildCount(); i++)
                    {

                        View row = llBalances.getChildAt(i);

                        TextView tvSymOne = (TextView) row.findViewById(R.id.symbol_child_one);
                        TextView tvAmOne = (TextView) row.findViewById(R.id.amount_child_one);
                        TextView tvfaitOne = (TextView) row.findViewById(R.id.fait_child_one);

                        TextView tvSymtwo = (TextView) row.findViewById(R.id.symbol_child_two);
                        TextView tvAmtwo = (TextView) row.findViewById(R.id.amount_child_two);
                        TextView tvFaitTwo = (TextView) row.findViewById(R.id.fait_child_two);

                        // If first balance in row is zeroed then update it
                        if (tvSymOne.getText().toString().equals("")) {
                            // shift balances from next child here
                            String symbol = "";
                            String amount = "";
                            String fait = "";

                            // Get next non-zero balance
                            if (tvSymtwo.getText().toString().isEmpty()) {
                                // if second balance in row is also empty then get next non-zero balance
                                for (int j = i+1; j < llBalances.getChildCount(); j++)
                                {
                                    View nextrow = llBalances.getChildAt(j);

                                    TextView tvSymOnenextrow = (TextView) nextrow.findViewById(R.id.symbol_child_one);
                                    TextView tvAmOnenextrow = (TextView) nextrow.findViewById(R.id.amount_child_one);
                                    TextView tvfaitOnenextrow = (TextView) nextrow.findViewById(R.id.fait_child_one);

                                    if (!tvSymOnenextrow.getText().toString().isEmpty()) {
                                        symbol = tvSymOnenextrow.getText().toString();
                                        amount = tvAmOnenextrow.getText().toString();
                                        fait = tvfaitOnenextrow.getText().toString();
                                        tvSymOnenextrow.setText("");
                                        tvAmOnenextrow.setText("");
                                        tvfaitOnenextrow.setText("");
                                        break;
                                    }

                                    TextView tvSymtwonextrow = (TextView) nextrow.findViewById(R.id.symbol_child_two);
                                    TextView tvAmtwonextrow = (TextView) nextrow.findViewById(R.id.amount_child_two);
                                    TextView tvFaitTwonextrow = (TextView) nextrow.findViewById(R.id.fait_child_two);

                                    if (!tvSymtwonextrow.getText().toString().isEmpty()) {
                                        symbol = tvSymtwonextrow.getText().toString();
                                        amount = tvAmtwonextrow.getText().toString();
                                        fait = tvFaitTwonextrow.getText().toString();
                                        tvSymtwonextrow.setText("");
                                        tvAmtwonextrow.setText("");
                                        tvFaitTwonextrow.setText("");
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                // if second balance is row is non-empty then move it to first balance
                                symbol = tvSymtwo.getText().toString();
                                amount = tvAmtwo.getText().toString();
                                fait = tvFaitTwo.getText().toString();
                                tvSymtwo.setText("");
                                tvAmtwo.setText("");
                                tvFaitTwo.setText("");
                            }

                            // update first balance amount
                            AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                            if(assetsSymbols.isUiaSymbol(symbol)) tvAmOne.setText(String.format(locale, "%.4f",Float.parseFloat(amount)));
                            else if(assetsSymbols.isSmartCoinSymbol(symbol)) tvAmOne.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));
                            tvSymOne.setText(symbol);
                          //  tvAmOne.setText(amount);
                            tvfaitOne.setText(fait);

                            if (fait.isEmpty())
                            {
                                tvfaitOne.setVisibility(View.GONE);
                            }
                            else
                            {
                                tvfaitOne.setVisibility(View.VISIBLE);
                            }
                        }

                        if (tvSymtwo.getText().toString().isEmpty()) {
                            String symbol = "";
                            String amount = "";
                            String fait = "";

                            // Get next non-zero balance
                            for (int j = i+1; j < llBalances.getChildCount(); j++) {
                                View nextrow = llBalances.getChildAt(j);

                                TextView tvSymOnenextrow = (TextView) nextrow.findViewById(R.id.symbol_child_one);
                                TextView tvAmOnenextrow = (TextView) nextrow.findViewById(R.id.amount_child_one);
                                TextView tvfaitOnenextrow = (TextView) nextrow.findViewById(R.id.fait_child_one);

                                if (!tvSymOnenextrow.getText().toString().isEmpty()) {
                                    symbol = tvSymOnenextrow.getText().toString();
                                    amount = tvAmOnenextrow.getText().toString();
                                    fait = tvfaitOnenextrow.getText().toString();
                                    tvSymOnenextrow.setText("");
                                    tvAmOnenextrow.setText("");
                                    tvfaitOnenextrow.setText("");
                                    break;
                                }

                                TextView tvSymtwonextrow = (TextView) nextrow.findViewById(R.id.symbol_child_two);
                                TextView tvAmtwonextrow = (TextView) nextrow.findViewById(R.id.amount_child_two);
                                TextView tvFaitTwonextrow = (TextView) nextrow.findViewById(R.id.fait_child_two);

                                if (!tvSymtwonextrow.getText().toString().isEmpty()) {
                                    symbol = tvSymtwonextrow.getText().toString();
                                    amount = tvAmtwonextrow.getText().toString();
                                    fait = tvFaitTwonextrow.getText().toString();
                                    tvSymtwonextrow.setText("");
                                    tvAmtwonextrow.setText("");
                                    tvFaitTwonextrow.setText("");
                                    break;
                                }
                            }

                            AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                            if(assetsSymbols.isUiaSymbol(symbol)) tvAmtwo.setText(String.format(locale, "%.4f",Float.parseFloat(amount)));
                            else if(assetsSymbols.isSmartCoinSymbol(symbol)) tvAmtwo.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));

                            tvSymtwo.setText(symbol);
                          //  tvAmtwo.setText(amount);
                            tvFaitTwo.setText(fait);

                            if (fait.isEmpty())
                            {
                                tvFaitTwo.setVisibility(View.GONE);
                            }
                            else
                            {
                                tvFaitTwo.setVisibility(View.VISIBLE);
                            }
                        }


                    }

                    // remove empty rows
                    for (int i = 0; i < llBalances.getChildCount(); i++) {
                        View row = llBalances.getChildAt(i);

                        TextView tvSymOne = (TextView) row.findViewById(R.id.symbol_child_one);
                        TextView tvSymtwo = (TextView) row.findViewById(R.id.symbol_child_two);

                        if (tvSymOne.getText().toString().isEmpty() && tvSymtwo.getText().toString().isEmpty()) {
                            llBalances.removeView(row);
                        }
                    }

                    if (llBalances.getChildCount() == 0) {
                        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
                    }
                }
                catch (Exception e)
                {}
            }
        });
    }

    Handler animateNsoundHandler = new Handler();

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am)
    {
        final Runnable reloadBalances = new Runnable() {
            @Override
            public void run() {
                removeZeroedBalanceViews();
            }
        };

        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                try
                {
                    // remove zero balances not in previously loaded balances
                    List<Integer> indexesToRemove = new ArrayList<>();

                    for( int i = 0 ; i < sym.size() ; i++ )
                    {
                        Long _amount = Long.parseLong(am.get(i));

                        if ( _amount == 0 )
                        {
                            Boolean matchFound = symbolsArray.contains(sym.get(i));

                            if ( !matchFound )
                            {
                                indexesToRemove.add(i);
                                sym.remove(i);
                                am.remove(i);
                                pre.remove(i);

                                sym.trimToSize();
                                am.trimToSize();
                                pre.trimToSize();

                                i--;
                            }
                        }
                    }
                }
                catch (Exception e)
                {

                }


                try {

                    LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    int count = llBalances.getChildCount();
                    int m = 0;

                    try {

                        Log.d("Balances Update", "Start");
                        Boolean animateOnce = true;

                        for (int i = 0; i < count; i++)
                        {

                            // count == number of row
                            // m == number of child in the row
                            // Get balances row
                            LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            TextView tvSymOne = (TextView) linearLayout.findViewById(R.id.symbol_child_one);
                            TextView tvAmOne = (TextView) linearLayout.findViewById(R.id.amount_child_one);
                            TextView tvfaitOne = (TextView) linearLayout.findViewById(R.id.fait_child_one);

                            TextView tvSymtwo = (TextView) linearLayout.findViewById(R.id.symbol_child_two);
                            TextView tvAmtwo = (TextView) linearLayout.findViewById(R.id.amount_child_two);
                            TextView tvFaitTwo = (TextView) linearLayout.findViewById(R.id.fait_child_two);

                            // First child updation
                            if (sym.size() > m)
                            {

                                Log.d("Balances Update", "sym size 1 : " + Long.toString(m));

                                String symbol = sym.get(m);

                                Log.d("Balances Update", "symbol : " + symbol);

                                String amount = "";


                                if (pre.size() > m && am.size() > m) {
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                }

                                Log.d("Balances Update", "amount : " + symbol);

                                String amountInInt = am.get(m);

                                Log.d("Balances Update", "amount in int : " + amountInInt);

                                String txtSymbol = "";// tvSymOne.getText().toString();
                                String txtAmount = "";//tvAmOne.getText().toString();

                                if (symbolsArray.size() > m) {
                                    txtSymbol = symbolsArray.get(m);// tvSymOne.getText().toString();
                                    txtAmount = amountsArray.get(m);//tvAmOne.getText().toString();
                                }

                                Log.d("Balances Update", "old symbol : " + txtSymbol);

                                Log.d("Balances Update", "old amount : " + txtAmount);

                                if (!symbol.equals(txtSymbol))
                                {
                                    tvSymOne.setText(symbol);
                                }

                                if (!amountInInt.equals(txtAmount)) {
                                    // previous amount
                                    //float txtAmount_d = convertLocalizeStringToFloat(txtAmount);

                                    if ( txtAmount.isEmpty() )
                                    {
                                        txtAmount = "0";
                                    }

                                    Long txtAmount_d = Long.parseLong(txtAmount);

                                    // New amount
                                    //float amount_d = convertLocalizeStringToFloat(amount);
                                    Long amount_d = Long.parseLong(amountInInt);

                                    // Balance is sent
                                    if (txtAmount_d > amount_d) {

                                        Log.d("Balances Update", "Balance sent");

                                        SupportMethods.testing("float", txtAmount_d, "txtamount");
                                        SupportMethods.testing("float", amount_d, "amount");
                                        tvAmOne.setTypeface(null, Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.red));

                                        animateText(tvAmOne, convertLocalizeStringToFloat(tvAmOne.getText().toString()), convertLocalizeStringToFloat(amount));

                                        final TextView cView = tvAmOne;
                                        final TextView aView = tvSymOne;
                                        final TextView bView = tvfaitOne;
                                        //final Handler handler = new Handler();

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                cView.setTypeface(null, Typeface.NORMAL);
                                                cView.setTextColor(getResources().getColor(R.color.green));
                                            }
                                        };
                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    cView.setText("");
                                                    aView.setText("");
                                                    bView.setText("");
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }

                                        Log.d("Balances Update", "Animation initiated");
                                    }
                                    // Balance is rcvd
                                    else if (amount_d > txtAmount_d) {

                                        Log.d("Balances Update", "Balance received");

                                        tvAmOne.setTypeface(null, Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.green));

                                        // run animation
                                        if (animateOnce) {
                                            getActivity().startService(new Intent(getActivity(), MediaService.class));
                                           /* final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    playSound();
                                                }
                                            };*/

                                            final Runnable rotateTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                rotateRecieveButton();
                                                            }
                                                        });

                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                           // animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }

                                        animateText(tvAmOne, convertLocalizeStringToFloat(tvAmOne.getText().toString()), convertLocalizeStringToFloat(amount));

                                        Log.d("Balances Update", "Text Animated");

                                        final TextView cView = tvAmOne;
                                        final TextView aView = tvSymOne;
                                        final TextView bView = tvfaitOne;
                                        //final Handler handler = new Handler();

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                cView.setTypeface(null, Typeface.NORMAL);
                                                cView.setTextColor(getResources().getColor(R.color.green));
                                            }
                                        };
                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    cView.setText("");
                                                    aView.setText("");
                                                    bView.setText("");
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }
                                        Log.d("Balances Update", "Rcv done");
                                    }
                                }
                                m++;
                                Log.d("Balances Update", "m++");
                            }
                            else
                            {
                                Log.d("Balances Update", "linearLayout.removeAllViews");
                                linearLayout.removeAllViews();
                            }

                            // Second child updation
                            if (sym.size() > m)
                            {
                                Log.d("Balances Update", "sym size 2 : " + Long.toString(m));

                                String symbol = sym.get(m);
                                String amount = "";

                                Log.d("Balances Update", "symbol : " + symbol);

                                if (pre.size() > m && am.size() > m) {
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                }

                                Log.d("Balances Update", "amount : " + amount);

                                String amountInInt = am.get(m);

                                Log.d("Balances Update", "amount in int : " + amountInInt);

                                String txtSymbol = "";
                                String txtAmount = "";
                                if (symbolsArray.size() > m) {
                                    txtSymbol = symbolsArray.get(m);// tvSymOne.getText().toString();
                                    txtAmount = amountsArray.get(m);//tvAmOne.getText().toString();
                                }

                                Log.d("Balances Update", "old symbol : " + txtSymbol);
                                Log.d("Balances Update", "old amount : " + txtAmount);

                                if ( txtAmount.isEmpty() )
                                {
                                    txtAmount = "0";
                                }

                                //float txtAmount_d = convertLocalizeStringToFloat(txtAmount);
                                Long txtAmount_d = Long.parseLong(txtAmount);

                                //float amount_d = convertLocalizeStringToFloat(amount);
                                Long amount_d = Long.parseLong(amountInInt);

                                if (!symbol.equals(txtSymbol)) {
                                    tvSymtwo.setText(symbol);
                                }

                                if (!amountInInt.equals(txtAmount))
                                {
                                    tvAmtwo.setVisibility(View.VISIBLE);

                                    // balance is sent
                                    if (txtAmount_d > amount_d)
                                    {
                                        Log.d("Balances Update", "Balance sent");
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.red));
                                        tvAmtwo.setTypeface(null, Typeface.BOLD);

                                        animateText(tvAmtwo, convertLocalizeStringToFloat(tvAmtwo.getText().toString()), convertLocalizeStringToFloat(amount));
                                        Log.d("Balances Update", "Text animated");

                                        final TextView cView = tvAmtwo;
                                        final TextView aView = tvSymtwo;
                                        final TextView bView = tvFaitTwo;

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                cView.setTypeface(null, Typeface.NORMAL);
                                                cView.setTextColor(getResources().getColor(R.color.green));
                                            }
                                        };

                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    cView.setText("");
                                                    aView.setText("");
                                                    bView.setText("");
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }

                                        Log.d("Balances Update","Animation done");

                                    }
                                    // Balance is recieved
                                    else if (amount_d > txtAmount_d)
                                    {
                                        Log.d("Balances Update","Balance is received");
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.green));
                                        tvAmtwo.setTypeface(null, Typeface.BOLD);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    playSound();
                                                }
                                            };

                                            final Runnable rotateTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                rotateRecieveButton();
                                                            }
                                                        });

                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }

                                        animateText(tvAmtwo, convertLocalizeStringToFloat(tvAmtwo.getText().toString()), convertLocalizeStringToFloat(amount));
                                        Log.d("Balances Update","Text animated");

                                        final TextView cView = tvAmtwo;
                                        final TextView aView = tvSymtwo;
                                        final TextView bView = tvFaitTwo;

                                        //final Handler handler = new Handler();

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                cView.setTypeface(null, Typeface.NORMAL);
                                                cView.setTextColor(getResources().getColor(R.color.green));
                                            }
                                        };

                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    cView.setText("");
                                                    aView.setText("");
                                                    bView.setText("");
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }
                                        Log.d("Balances Update","rcv done");
                                    }
                                }
                                m++;
                                Log.d("Balances Update","m updated");
                            }
                            else
                            {
                                Log.d("Balances Update","else when sym > m");
                                // i == number of row
                                if (i == (count - 1) ) // if its the last row
                                {
                                    if (sym.size() > m) // if number of balances is more than traversed
                                        m--;            // then minus 1 from m
                                }
                            }
                        }


                        // Calculate m : number of balances loaded in ui
                        m = 0;
                        for ( int i = 0 ; i < llBalances.getChildCount(); i++ )
                        {
                            LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            TextView tvSymOne = (TextView) linearLayout.findViewById(R.id.symbol_child_one);
                            TextView tvSymtwo = (TextView) linearLayout.findViewById(R.id.symbol_child_two);

                            if ( !tvSymOne.getText().toString().isEmpty() )
                            {
                                m++;
                            }

                            if ( !tvSymtwo.getText().toString().isEmpty() )
                            {
                                m++;
                            }
                        }


                        Log.d("Balances Update","Number of balances loaded : " + Long.toString(m));

                        // Insert/remove balance objects if updated
                        Log.d("Balances Update","Insert or remove balance objects if needed");

                        int loop = sym.size() - m; // number of extra balances to be loaded

                        if (loop > 0)
                        {
                            Log.d("Balances Update","Yes updation required : " + Long.toString(loop));

                            for (int i = m; i < sym.size(); i += 2)
                            {
                                int counter = 1;
                                int totalNumberOfBalances = sym.size(); // total number of balances 6
                                int pr;

                                if ( (totalNumberOfBalances - i) > 2 )
                                {
                                    pr = 2;
                                }
                                else
                                {
                                    pr = totalNumberOfBalances - i;
                                }

                                View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);

                                for (int l = i; l < (i + pr); l++)
                                {
                                    if (counter == 1)
                                    {
                                        TextView textView = (TextView) customView.findViewById(R.id.symbol_child_one);
                                        textView.setText(sym.get(l));
                                        TextView textView1 = (TextView) customView.findViewById(R.id.amount_child_one);

                                        if ( (pre.size() > l) && (am.size() > i) )
                                        {
                                            String r = returnFromPower(pre.get(l), am.get(i));
                                            textView1.setText(r);
                                            // setCounter(textView1, 0f, 0f);
                                            textView1.setText(String.format(locale, "%.4f", Float.parseFloat(r)));
                                            //setCounter(textView1, Float.parseFloat(r), Float.parseFloat(r));
                                        }
                                        else textView1.setText("");
                                    }

                                    if (counter == 2)
                                    {
                                        TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                                        textView2.setText(sym.get(l));
                                        TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                                        if ( (pre.size() > l) && (am.size() > l) )
                                        {
                                            String r = returnFromPower(pre.get(l), am.get(l));
                                            textView3.setText(String.format(locale, "%.4f", Float.parseFloat(r)));
                                            //setCounter(textView3, 0f, 0f);
                                            // setCounter(textView3, Float.parseFloat(r), Float.parseFloat(r));
                                        }

                                        llBalances.addView(customView);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    playSound();
                                                }
                                            };

                                            final Runnable rotateTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                rotateRecieveButton();
                                                            }
                                                        });

                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }
                                    }

                                    if ( (counter == 1) && ( i == (sym.size() - 1) ) )
                                    {
                                        llBalances.addView(customView);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    playSound();
                                                }
                                            };

                                            final Runnable rotateTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                rotateRecieveButton();
                                                            }
                                                        });

                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }
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
                    catch (Exception e)
                    {
                        Log.d("Balances Update", e.getMessage());
                    }
                }
                catch (Exception e)
                {
                    Log.d("Balances Load", e.getMessage());
                }

                progressBar1.setVisibility(View.GONE);
                whiteSpaceAfterBalances.setVisibility(View.GONE);
                isLoading = true;

                updateBalanceArrays( sym,pre,am );
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
        float value = Float.parseFloat(str);
        for (int k = 0; k < pre; k++) {
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

        //tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), myTransactions));

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

    @Override
    public void soundFinish() {
        getActivity().stopService(new Intent(getActivity(), MediaService.class));
    }

    private static class TransactionsDateComparator implements Comparator<TransactionDetails>
    {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getDate().compareTo(two.getDate());
        }
    }

    private static class TransactionsSendRecieveComparator implements Comparator<TransactionDetails>
    {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getSent().compareTo(two.getSent());
        }
    }

    private static int compareFloats(float change1, float change2)
    {
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


    private void saveTransactions(List<TransactionDetails> transactionDetails) {
        tinyDB.putTransactions(getActivity(), getContext(), getResources().getString(R.string.pref_local_transactions), new ArrayList<>(transactionDetails));
    }

    private List<TransactionDetails> getTransactionsFromSharedPref() {
        ArrayList<TransactionDetails> mySavedList = tinyDB.getTransactions(getResources().getString(R.string.pref_local_transactions), TransactionDetails.class);

        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.updatedTransactionDetails(mySavedList);

        for (TransactionDetails td : mySavedList) {
            td.updateContext(getContext());
        }

        return mySavedList;
    }

    public void TransactionUpdateOnStartUp() {
        final List<TransactionDetails> localTransactionDetails = getTransactionsFromSharedPref();

        if (localTransactionDetails != null && localTransactionDetails.size() > 0) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    isSavedTransactions = true;
                    tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), localTransactionDetails));
                //    load_more_values.setVisibility(View.VISIBLE);
                //    load_more_values.setEnabled(true);
                    tableViewparent.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {
        sentCallForTransactions = true;
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                    if(isSavedTransactions) {
                        myTransactions.clear();
                        isSavedTransactions = false;
                        myTransactions = new ArrayList<>();
                    //    updateSortTableView(tableView, myTransactions);
                    }

                   if (number_of_transactions_in_queue == 0) {
                        load_more_values.setVisibility(View.GONE);
                    } else {
                        load_more_values.setVisibility(View.VISIBLE);
                        load_more_values.setEnabled(true);
                    }

                    if (myTransactions.size() == 0) {
                        saveTransactions(transactionDetails);
                    }

                    myTransactions.addAll(transactionDetails);

                    AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                    myTransactions = assetsSymbols.updatedTransactionDetails(myTransactions);

                    tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), myTransactions));
                    progressBar.setVisibility(View.GONE);
                    tableViewparent.setVisibility(View.VISIBLE);
                    if(number_of_transactions_loaded<20){
                        loadTransactions(getContext(), accountId, wifkey, number_of_transactions_loaded, 5);
                        number_of_transactions_loaded = number_of_transactions_loaded + 5;
                        load_more_values.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            SupportMethods.testing("TransactionUpdate", e, "try/catch");
        }
    }

    @OnClick(R.id.load_more_values)
    public void Load_more_Values() {
        load_more_values.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, 5);
        number_of_transactions_loaded = number_of_transactions_loaded + 5;
    }

    void isLifeTime(final String name_id, final String id) {
        try {
            final int db_id = Helper.fetchIntSharePref(getContext(), getContext().getString(R.string.sharePref_database));
            //{"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}

            final Handler handler = new Handler();

            final Runnable updateTask = new Runnable() {
                @Override
                public void run() {

                    try {
                        if (Application.webSocketG != null && (Application.webSocketG.isOpen()) && (Application.isReady)) {
                            String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
                            SupportMethods.testing("getLifetime", getDetails, "getDetails");
                            Application.webSocketG.send(getDetails);
                        } else {
                            isLifeTime(name_id, id);
                        }
                    }
                    catch (Exception e)
                    {}
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
                    if (Application.webSocketG != null && (Application.webSocketG.isOpen()) && (Application.isReady) ) {
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
        SupportMethods.testing("getLifetime", s, "s");

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        SupportMethods.testing("getAccountID", s, "s");

        String result = SupportMethods.ParseJsonObject(s, "result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(expiration);
            Date date2 = dateFormat.parse("1969-12-31T23:59:59");
            if (date2.getTime() >= date1.getTime()) {
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
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (qrCamera != null && backLine != null) {
                            startAnimation();
                        } else myHandler.postDelayed(this, 333);
                    }
                }, 333);
            }
        }
    }

    Handler loadOndemand = new Handler();

    private void loadOnDemand(final Activity _activity)
    {
        loadOndemand.removeCallbacksAndMessages(null);

        Runnable loadOnDemandRunnable = new Runnable() {
            @Override
            public void run() {
                _activity.runOnUiThread(new Runnable() {
                    public void run() {
                        sentCallForTransactions = false;
                        isSavedTransactions = true;
                        loadViews(false,true,false);
                    }
                });
            }
        };
        loadOndemand.removeCallbacks(loadOnDemandRunnable);
        loadOndemand.postDelayed(loadOnDemandRunnable,1000);
    }

    @Override
    public void loadAll() {
        loadOnDemand(getActivity());
//        getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                loadViews();
//            }
//        });
    }

    AssestsActivty myAssetsActivity;

    void loadViews(Boolean onResume,Boolean accountNameChanged,boolean faitCurrencyChanged) {

        load_more_values.setVisibility(View.GONE);

        if(!isSavedTransactions) {
            tableViewparent.setVisibility(View.GONE);
            myTransactions = new ArrayList<>();
            updateSortTableView(tableView, myTransactions);
        }

        // llBalances.removeAllViews();

        tableView.addDataClickListener(new tableViewClickListener(getContext()));

        progressBar.setVisibility(View.VISIBLE);
        //progressBar1.setVisibility(View.VISIBLE);
        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);

        if (myAssetsActivity == null) {
            myAssetsActivity = new AssestsActivty(getContext(), to, this, application);
            myAssetsActivity.registerDelegate();
        }

        if ( !onResume || accountNameChanged || faitCurrencyChanged) {
            progressBar1.setVisibility(View.VISIBLE);
            myAssetsActivity.loadBalances(to);
        }
        number_of_transactions_loaded = 0;
        if(!sentCallForTransactions) {
            sentCallForTransactions=true;
            loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, 5);
            number_of_transactions_loaded = number_of_transactions_loaded + 5;
        }
    }

    void loadBasic(boolean onResume,boolean accountNameChanged, boolean faitCurrencyChanged) {

        if ( !onResume )
        {
            isLoading = false;
        }

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
                    showHideLifeTime(accountDetails.get(i).isLifeTime);
                    break;
                }
            }
        }
        Application.monitorAccountId = accountId;
        tvAccountName.setText(to);
        isLifeTime(accountId, "15");
        get_full_accounts(accountId, "17");

        loadViews(onResume,accountNameChanged, faitCurrencyChanged);
    }

    Boolean checkIfAccountNameChange() {
        //ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String checkAccountName = "";
        if (accountDetails.size() == 1) {
            checkAccountName = accountDetails.get(0).account_name;
            ivMultiAccArrow.setVisibility(View.GONE);
        } else {
            ivMultiAccArrow.setVisibility(View.VISIBLE);
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    checkAccountName = accountDetails.get(i).account_name;
                    break;
                }
            }
        }
        return !checkAccountName.equals(to);
    }

    void onChangedAccount(){
        final ArrayList<AccountDetails> accountDetailsList;

        accountDetailsList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        List<String> accountlist = new ArrayList<String>();

        for (int i = 0; i < accountDetailsList.size(); i++) {
            accountlist.add(accountDetailsList.get(i).account_name);
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());

        builderSingle.setTitle(getString(R.string.imported_created_accounts));


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_list_item_1, accountlist);

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        for (int i = 0; i < accountDetailsList.size(); i++) {

                            if (strName.equals(accountDetailsList.get(i).account_name)) {
                                accountDetailsList.get(i).isSelected = true;
                            } else {
                                accountDetailsList.get(i).isSelected = false;
                            }

                        }
                        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetailsList);
                        Helper.storeStringSharePref(getContext(), getString(R.string.pref_account_name), strName);

                        onResume();
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    @OnClick(R.id.ivMultiAccArrow)
    public void ivOnChangedAccount(View view){
        onChangedAccount();
    }

    @OnClick(R.id.account_name)
    public void tvOnChangedAccount(View view){
        onChangedAccount();
    }

    private void showHideLifeTime(final Boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ivLifeTime.setVisibility(View.VISIBLE);
                    tvUpgradeLtm.setVisibility(View.GONE);

                } else {
                    ivLifeTime.setVisibility(View.GONE);
                    tvUpgradeLtm.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void getAccountUpgradeInfo(final Activity activity, final String accountName) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account");
        hashMap.put("account", accountName);
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
                        updateLifeTimeModel(accountName);
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

    public void getLtmPrice(final Activity activity, final String accountName) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account_fees");
        hashMap.put("account", accountName);
        try {
            hashMap.put("wifkey", Crypt.getInstance().decrypt_string(wifkey));
        } catch (Exception e) {
        }

        final Call<LtmFee> postingService = service.getLtmFee(hashMap);
        postingService.enqueue(new Callback<LtmFee>() {
            @Override
            public void onResponse(Response<LtmFee> response) {
                if (response.isSuccess()) {
                    hideDialog();
                    LtmFee ltmFee = response.body();
                    if (ltmFee.status.equals("success")) {
                        try {
                            JSONObject jsonObject = new JSONObject(ltmFee.transaction);
                            JSONObject jsonObject1 = jsonObject.getJSONArray("operations").getJSONArray(0).getJSONObject(1);
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("fee");
                            String amount = jsonObject2.getString("amount");
                            //String asset_id = jsonObject2.getString("asset_id");
                            String temp = SupportMethods.ConvertValueintoPrecision("5", amount);
                            Helper.storeStringSharePref(getActivity(), "ltmAmount", temp);
                        } catch (Exception e) {
                        }
                    }
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
        try {

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
        catch (Exception e)
        {}
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

    private void updateLifeTimeModel(String accountName) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        try {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).account_name.equals(accountName)) {
                    accountDetails.get(i).isLifeTime = true;
                    break;
                }
            }
        } catch (Exception e) {
        }

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        showHideLifeTime(true);
    }

    private float convertLocalizeStringToFloat(String text) {
        float txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.floatValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.floatValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    private double convertLocalizeStringToDouble(String text) {
        double txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.doubleValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.doubleValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    private int convertLocalizeStringToInt(String text) {
        int txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.intValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.intValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }


    void loadTransactions(final Context context,final String id,final AssetDelegate in ,final String wkey,final int loaded,final int toLoad)
    {
        new TransactionActivity(context, id ,in , wkey , loaded , toLoad);
    }
    void loadTransactions(final Context context,final String id ,final String wkey,final int loaded,final int toLoad)
    {
        new TransactionActivity(context, id ,this , wkey , loaded , toLoad);
    }
//        final Handler handlerTransactions = new Handler();
//        handlerTransactions.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                if(!rcvdCallForTransactions && sentCallForTransactions) {
//                    new TransactionActivity(context, id ,in , wkey , loaded , toLoad);
//                    handlerTransactions.postDelayed(this, 20000);
//                }
//
//                if(!sentCallForTransactions){
//                    sentCallForTransactions = true;
//                    new TransactionActivity(context, id ,in , wkey , loaded , toLoad);
//                    handlerTransactions.postDelayed(this, 20000);
//                }
//
//                if(rcvdCallForTransactions){
//                    handlerTransactions.removeCallbacks(this);
//                    handlerTransactions.removeCallbacksAndMessages(this);
//                }
//
//            }
//        }, 100);
//    }



}


