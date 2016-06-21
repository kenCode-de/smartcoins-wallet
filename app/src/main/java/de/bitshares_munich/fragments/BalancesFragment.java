package de.bitshares_munich.fragments;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.premnirmal.textcounter.CounterView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.AccountUpgrade;
import de.bitshares_munich.models.EquivalentComponentResponse;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.models.transactionsJsonSerializable;
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
public class BalancesFragment extends Fragment implements AssetDelegate {

    Application application = new Application();
    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");

    Boolean isLoading = false;

    Handler handler = new Handler();

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

    @Bind(R.id.ivMultiAccArrow)
    ImageView ivMultiAccArrow;

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
        tableView = (SortableTableView<TransactionDetails>) rootView.findViewById(R.id.tableView);

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


        loadBasic();
        loadBalancesFromSharedPref();
        TransactionUpdateOnStartUp();

        handler.postDelayed(updateTask, 2000);

        handler.postDelayed(createFolder, 5000);

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

            Log.d("setSortableHeight", "Scroll Heght : " + Integer.toString(height1));
            View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
            int height2 = transactionsExportHeader.getHeight();
            Log.d("setSortableHeight", "Scroll Header Heght : " + Integer.toString(height2));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tableView.getLayoutParams();
            params.height = height1 - height2;
            Log.d("setSortableHeight", "View Heght : " + Integer.toString(params.height));
            tableViewparent.setLayoutParams(params);
            Log.d("setSortableHeight", "View Heght Set");
        } catch (Exception e) {
            Log.d("List Height", e.getMessage());
            handler.postDelayed(task, 2000);
        }
    }

    private void createFolder() {
        PermissionManager manager = new PermissionManager();
        manager.verifyStoragePermissions(getActivity());

        final File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.txt_folder_name));

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
                    File file2 = new File(folder.getAbsolutePath(), "ch_ching.wav");

                    if (!file2.exists()) {
                        FileOutputStream save = new FileOutputStream(file2);

                        byte[] buffer = null;
                        InputStream fIn = getResources().openRawResource(R.raw.ch_ching);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);


        if (checkIfAccountNameChange() || (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency))) {
            loadBasic();
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
        btnCancel.setText(getString(R.string.txt_back));
        btnDone.setText(getString(R.string.next));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check Balance
                if (btnDone.getText().equals(getString(R.string.next))) {
                    alertMsg.setText(String.format(getString(R.string.txt_confirmation_msg), tvAccountName.getText().toString()));
                    btnDone.setText(getString(R.string.txt_yes));
                    btnCancel.setText(getString(R.string.txt_no));
                } else {
                    dialog.cancel();
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    ;
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
                        getAccountUpgradeInfo(getActivity(), tvAccountName.getText().toString());
                    }

                }
            }

        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
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
                if (accountDetails.get(i).isSelected) {
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

                        BalanceAssetsUpdate(sym, pre, am, true);
                    }

                    break;
                }
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<AccountAssets> accountAssets = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            AccountAssets accountAsset = new AccountAssets();

            accountAsset.id = ids.get(i);
            if (pre.size() > i) accountAsset.precision = pre.get(i);
            if (sym.size() > i) accountAsset.symbol = sym.get(i);
            if (am.size() > i) accountAsset.ammount = am.get(i);

            SupportMethods.testing("floatDoubleIssue", Float.parseFloat(returnFromPower(pre.get(i), am.get(i))), "txtamount");

            // Log.i("uncle","aay1"+am.get(i));
            accountAssets.add(accountAsset);
        }
        try {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetails.get(i).AccountAssets = accountAssets;
                    getEquivalentComponents(accountAssets);
                    break;
                }
            }
        } catch (Exception w) {
            SupportMethods.testing("Assets", w, "Asset Activity");
        }

        SupportMethods.testing("Assets", "Assets views 3", "Asset Activity");

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        SupportMethods.testing("Assets", "Assets views 4", "Asset Activity");
        BalanceAssetsUpdate(sym, pre, am, false);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {
        int count = llBalances.getChildCount();

        if (count <= 0) BalanceAssetsLoad(sym, pre, am, onStartUp);
        if (count > 0) BalanceAssetsUpdate(sym, pre, am);
    }

    /*public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am,final Boolean onStartUp)
    {
        SupportMethods.testing("Assets", "Assets views 4", "Asset Activity");
        BalanceAssetsUpdate(sym, pre, am, false);
    }
    */

    private void getEquivalentComponents(ArrayList<AccountAssets> accountAssets) {
        String faitCurrency = Helper.getFadeCurrency(getContext());
        if (faitCurrency.isEmpty()) {
            faitCurrency = application.getString(R.string.default_currency);
        }
        String values = "";
        for (int i = 0; i < accountAssets.size(); i++) {
            AccountAssets accountAsset = accountAssets.get(i);
            if (!accountAsset.symbol.equals(faitCurrency)) {
                values += accountAsset.symbol + ":" + faitCurrency + ",";
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
            public void onResponse(Response<EquivalentComponentResponse> response) {
                if (response.isSuccess()) {
                    EquivalentComponentResponse resp = response.body();
                    if (resp.status.equals("success")) {
                        try {
                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            HashMap hm = new HashMap();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                hm.put(key.split(":")[0], rates.get(key));
                            }
                            for (int i = 0; i < llBalances.getChildCount(); i++) {
                                LinearLayout llRow = (LinearLayout) llBalances.getChildAt(i);
                                LinearLayout llRowInner = (LinearLayout) llRow.getChildAt(0);
                                for (int j = 0; j < llRowInner.getChildCount(); j++) {
                                    TextView tvAsset = (TextView) llRowInner.getChildAt(j);
                                    TextView tvAmount = (TextView) llRowInner.getChildAt(j + 1);
                                    String asset = tvAsset.getText().toString();
                                    String amount = tvAmount.getText().toString().split("\\[")[0];
                                    amount = android.text.Html.fromHtml(amount).toString();
                                    if (!amount.isEmpty() && hm.containsKey(asset)) {
                                        Currency currency = Currency.getInstance(finalFaitCurrency);
                                        Double eqAmount = Double.parseDouble(amount) * Double.parseDouble(hm.get(asset).toString());
                                        tvAmount.setText(amount);
                                        tvAmount.append(Html.fromHtml("<br><small>[" + currency.getSymbol() + String.format("%.4f", eqAmount) + "]</small>"));
                                    }
                                    j++;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Toast.makeText(getActivity(), getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    hideDialog();
                    Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(getActivity(), getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {
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
                            setCounter(textView1, 0f, 0f);

                            setCounter(textView1, b, b);
                        }

                        if (counter == 2) {
                            TextView textView2 = (TextView) layout1.getChildAt(2);
                            textView2.setText(sym.get(l));
                            CounterView textView3 = (CounterView) layout1.getChildAt(3);
                            String r = returnFromPower(pre.get(l), am.get(l));
                            // textView3.setText();
                            setCounter(textView3, 0f, 0f);
                            setCounter(textView3, Float.parseFloat(r), Float.parseFloat(r));
                            llBalances.addView(customView);
                        }
                        if (counter == 1 && i == sym.size() - 1) {
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

                if (!onStartUp) {
                    progressBar1.setVisibility(View.GONE);
                    isLoading = true;
                }

                whiteSpaceAfterBalances.setVisibility(View.GONE);
            }
        });
    }

    public void setCounter(CounterView counterView, float sValue, float eValue) {
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

    private void rotateRecieveButton() {
        ImageView rcvBtn = (ImageView) getActivity().findViewById(R.id.recievebtn);
        final Animation rotAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate360);
        rcvBtn.startAnimation(rotAnim);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                try {

                    LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    int count = llBalances.getChildCount();

                    if (count > 0) {
                        int m = 0;
                        for (int i = 0; i < count; i++) {
                            LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            LinearLayout child = (LinearLayout) linearLayout.getChildAt(0);
                            TextView tvSymOne = (TextView) child.getChildAt(0);
                            CounterView tvAmOne = (CounterView) child.getChildAt(1);
                            TextView tvSymtwo = (TextView) child.getChildAt(2);
                            CounterView tvAmtwo = (CounterView) child.getChildAt(3);

                            if (sym.size() > m) {

                                String symbol = sym.get(m);
                                String amount = "";
                                if (pre.size() > m && am.size() > m)
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                String txtSymbol = tvSymOne.getText().toString();
                                String txtAmount = tvAmOne.getText().toString().split("\\[")[0];
                                ;

                                if (!symbol.equals(txtSymbol))
                                    tvSymOne.setText(symbol);
                                if (!amount.equals(txtAmount)) {

                                    if (Float.parseFloat(txtAmount) > Float.parseFloat(amount)) {
                                        SupportMethods.testing("float", Float.parseFloat(txtAmount), "txtamount");
                                        SupportMethods.testing("float", Float.parseFloat(amount), "amount");
                                        tvAmOne.setTypeface(null, Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.red));
                                    }

                                    if (Float.parseFloat(amount) > Float.parseFloat(txtAmount)) {
                                        tvAmOne.setTypeface(null, Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.green));

                                        // run animation
                                        final Runnable rotateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        rotateRecieveButton();
                                                    }
                                                });
                                            }
                                        };

                                        handler.postDelayed(rotateTask, 200);
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
                            } else {
                                linearLayout.removeAllViews();
                            }

                            if (sym.size() > m) {
                                String symbol = sym.get(m);
                                String amount = "";
                                if (pre.size() > m && am.size() > m)
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                String txtSymbol = tvSymtwo.getText().toString();
                                String txtAmount = tvAmtwo.getText().toString().split("\\[")[0];
                                ;

                                if (!symbol.equals(txtSymbol))
                                    tvSymtwo.setText(symbol);

                                if (!amount.equals(txtAmount)) {
                                    tvAmtwo.setVisibility(View.VISIBLE);

                                    if (Float.parseFloat(txtAmount) > Float.parseFloat(amount)) {
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.red));
                                        tvAmtwo.setTypeface(null, Typeface.BOLD);

                                    }
                                    if (Float.parseFloat(amount) > Float.parseFloat(txtAmount)) {
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.green));
                                        tvAmtwo.setTypeface(null, Typeface.BOLD);


                                    }


                                    setCounter(tvAmtwo, Float.parseFloat(txtAmount), Float.parseFloat(amount));
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
                                }
                                m++;
                            } else {
                                if (i == count - 1) {
                                    if (sym.size() > m)
                                        m--;
                                }
                            }
                        }

                        int loop = sym.size() - m;
                        if (loop > 0) {
                            for (int i = m; i < sym.size(); i += 2) {
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
                                        if (pre.size() > l && am.size() > i) {
                                            String r = returnFromPower(pre.get(l), am.get(i));
                                            setCounter(textView1, 0f, 0f);
                                            setCounter(textView1, Float.parseFloat(r), Float.parseFloat(r));
                                        } else textView1.setText("");
                                    }

                                    if (counter == 2) {
                                        TextView textView2 = (TextView) layout1.getChildAt(2);
                                        textView2.setText(sym.get(l));
                                        CounterView textView3 = (CounterView) layout1.getChildAt(3);
                                        if (pre.size() > l && am.size() > l) {
                                            String r = returnFromPower(pre.get(l), am.get(l));
                                            setCounter(textView3, 0f, 0f);
                                            setCounter(textView3, Float.parseFloat(r), Float.parseFloat(r));
                                        }

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
                    }
                    progressBar1.setVisibility(View.GONE);
                    whiteSpaceAfterBalances.setVisibility(View.GONE);
                    isLoading = true;

                } catch (Exception e) {
                    Log.d("Balances Load", e.getMessage());

                }
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


    private void saveTransactions(List<TransactionDetails> transactionDetails) {
        tinyDB.putTransactions(getActivity(), getContext(), getResources().getString(R.string.pref_local_transactions), new ArrayList<>(transactionDetails));
    }

    private List<TransactionDetails> getTransactionsFromSharedPref() {
        List<TransactionDetails> mySavedList = tinyDB.getTransactions(getResources().getString(R.string.pref_local_transactions), TransactionDetails.class);

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
                    tableView.setDataAdapter(new TransactionsTableAdapter(getContext(), localTransactionDetails));
                    load_more_values.setVisibility(View.VISIBLE);
                    load_more_values.setEnabled(true);
                    tableViewparent.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (myTransactions.size() == 0) {
                    saveTransactions(transactionDetails);
                    //myTransactions.clear();
                }

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
        SupportMethods.testing("getLifetime", s, "s");

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        SupportMethods.testing("getAccountID", s, "s");

        String result = SupportMethods.ParseJsonObject(s, "result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");
//    public void getLifetime(String s, int id) {
//        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
//        String result = SupportMethods.ParseJsonObject(s, "result");
//        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
//        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");
//>>>>>>> c3d1af2a48bc7e3cc754165a8c98f3397cc8df59

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


}


