package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IExchangeRate;
import de.bitshares_munich.Interfaces.IRelativeHistory;
import de.bitshares_munich.Interfaces.OnClickListView;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.models.QrJson;
import de.bitshares_munich.models.TradeResponse;
import de.bitshares_munich.models.TransferResponse;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/6/16.
 */
public class SendScreen extends BaseActivity implements IExchangeRate, IAccount, IRelativeHistory , OnClickListView{
    Context context;
    Application application = new Application();
    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;
    AccountAssets selectedAccountAsset;
    AccountAssets loyaltyAsset;

    AccountAssets backupAssets;

    boolean validReceiver = false;
    boolean validAmount = false;
    boolean sendBtnPressed = false;
    boolean alwaysDonate = false;
    boolean validating = false;


    ProgressDialog progressDialog;
    Double exchangeRate, requiredAmount, backAssetRate, sellAmount;

    String backupAsset, receiverID, callbackURL;

    @Bind(R.id.llMemo)
    LinearLayout llMemo;

    @Bind(R.id.llLoyalty)
    LinearLayout llLoyalty;

    @Bind(R.id.llBackupAsset)
    LinearLayout llBackupAsset;

    @Bind(R.id.tvLoyaltyStatus)
    TextView tvLoyaltyStatus;

    @Bind(R.id.tvTotalStatus)
    TextView tvTotalStatus;

    @Bind(R.id.spAssets)
    Spinner spAssets;

    @Bind(R.id.tvLoyalty)
    TextView tvLoyalty;

    @Bind(R.id.tvBackupAsset)
    TextView tvBackupAsset;

    @Bind(R.id.tvBackupAssetBalanceValidate)
    TextView tvBackupAssetBalanceValidate;

    @Bind(R.id.webviewFrom)
    WebView webviewFrom;

    @Bind(R.id.webviewTo)
    WebView webviewTo;

    @Bind(R.id.etReceiverAccount)
    EditText etReceiverAccount;

    @Bind(R.id.tvErrorRecieverAccount)
    TextView tvErrorRecieverAccount;

    @Bind(R.id.tvAmountStatus)
    TextView tvAmountStatus;

    @Bind(R.id.cbAlwaysDonate)
    CheckBox cbAlwaysDonate;

    @Bind(R.id.etMemo)
    EditText etMemo;

    @Bind(R.id.etAmount)
    EditText etAmount;

    @Bind(R.id.etBackupAsset)
    EditText etBackupAsset;

    @Bind(R.id.spinnerFrom)
    Spinner spinnerFrom;

    @Bind(R.id.btnSend)
    LinearLayout btnSend;

    @Bind(R.id.sendicon)
    ImageView sendicon;

    @Bind(R.id.etLoyalty)
    EditText etLoyalty;

    @Bind(R.id.tvFrom)
    TextView tvFrom;

    int count = 0;

    @Bind(R.id.tvBlockNumberHead_send_screen_activity)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_send_screen_activity)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected_send_screen_activity)
    ImageView ivSocketConnected;

    private void startupTasks ()
    {
        runningSpinerForFirstTime = true;
        init();
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null)
        {
            if (res.containsKey("sResult") && res.containsKey("id"))
            {
                if (res.getInt("id") == 5) {
                    getJsonFromHash(res.getString("sResult"));
                }
            }
        }

        String basset = etBackupAsset.getText().toString();

        if ( !basset.isEmpty() )
        {
            backupAssetCHanged(basset);
        }

        loadWebView(webviewTo,39, Helper.hash("", Helper.SHA256));
    }


    Activity sendScreenActivity;
    webSocketCallHelper myWebSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);

        setBackButton(true);

        setTitle(getResources().getString(R.string.send_screen_name));

        sendScreenActivity = this;
        myWebSocketHelper = new webSocketCallHelper(getApplicationContext());

        context = getApplicationContext();
        ButterKnife.bind(this);
        application.registerExchangeRateCallback(this);
        application.registerCallback(this);
        application.registerRelativeHistoryCallback(this);

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));

        updateBlockNumberHead();

        tinyDB = new TinyDB(context);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        cbAlwaysDonate.setText(getString(R.string.checkbox_donate)+" BitShares Munich");


        startupTasks();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if(!validReceiver && !validating)
                {
                    if (etReceiverAccount.getText().length() > 0)
                    {
                        myLowerCaseTimer.cancel();
                        myAccountNameValidationTimer.cancel();
                        myLowerCaseTimer.start();
                        myAccountNameValidationTimer.start();
                    }
                }
                //Do something after 100ms
                if (validateSend() && validReceiver)
                {
                    btnSend.setEnabled(true);
                    btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(), R.color.redcolor));
                    sendicon.setImageDrawable(getDrawable(getApplicationContext(), R.mipmap.icon_send));

                } else {
                    btnSend.setEnabled(false);
                    btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(), R.color.gray));
                    sendicon.setImageDrawable(getDrawable(getApplicationContext(), R.drawable.sendicon2));
                }
                handler.postDelayed(this, 100);
            }
        }, 100);


        //getTrxBlock("160");
    }

    void init() {
        setCheckboxAvailabilty();
        setSpinner();
    }

    @OnTextChanged(R.id.etReceiverAccount)
    void onTextChangedTo(CharSequence text) {
        validReceiver = false;
        tvErrorRecieverAccount.setText("");
        if (!text.toString().equals(text.toString().trim())) {
            etReceiverAccount.setText(text.toString().trim());
        }

        if (spinnerFrom.getSelectedItem().toString().equals(etReceiverAccount.getText().toString())) {
            Toast.makeText(context, R.string.warning_msg_same_account, Toast.LENGTH_SHORT).show();
        }

        if (etReceiverAccount.getText().length() > 0) {
            validating = true;
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
        tvErrorRecieverAccount.setVisibility(View.GONE);
        loadWebView(webviewTo,34, Helper.hash(etReceiverAccount.getText().toString(), Helper.SHA256));
        // loadWebView(webviewTo, 34, Helper.hash(etReceiverAccount.getText().toString(), Helper.MD5));
    }

    @OnFocusChange(R.id.etReceiverAccount)
    public void onFocusChange(boolean hasFocus) {
        validReceiver = false;

        if (!hasFocus) {
            validating = true;
            tvErrorRecieverAccount.setText("");
            //  tvErrorRecieverAccount.setVisibility(View.VISIBLE);
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
    }

    //    @OnFocusChange(R.id.etAmount)
//    public void onFocus() {
//        if(validateSend() && validReceiver){
//            btnSend.setEnabled(true);
//            btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(),R.color.redcolor));
//            sendicon.setImageDrawable(getDrawable(getApplicationContext(),R.mipmap.icon_send));
//
//        }else {
//            btnSend.setEnabled(false);
//            btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(),R.color.gray));
//            sendicon.setImageDrawable(getDrawable(getApplicationContext(),R.drawable.sendicon2));
//        }
//    }
    @OnTextChanged(R.id.etAmount)
    void onAmountChanged(CharSequence text) {
        updateAmountStatus();
//        if(validateSend() && validReceiver){
//            btnSend.setEnabled(true);
//            btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(),R.color.redcolor));
//            sendicon.setImageDrawable(getDrawable(getApplicationContext(),R.mipmap.icon_send));
//
//        }else {
//            btnSend.setEnabled(false);
//            btnSend.setBackgroundColor(getColorWrapper(getApplicationContext(),R.color.gray));
//            sendicon.setImageDrawable(getDrawable(getApplicationContext(),R.drawable.sendicon2));
//        }
    }

    Boolean runningSpinerForFirstTime = true;

    @OnItemSelected(R.id.spinnerFrom)
    void onItemSelected(int position)
    {
        if (!runningSpinerForFirstTime)
        {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();

            for (int i = 0; i < accountDetails.size(); i++)
            {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount))
                {
                    accountDetail.isSelected = true;
                }
                else
                {
                    accountDetail.isSelected = false;
                }
            }

            startupTasks();

            /*
            if (requiredAmount == null)
            {
                populateAssetsSpinner();
            }
            else
            {
                updateAmountStatus();
                if (loyaltyAsset != null) {
                    String selectedAccount = spinnerFrom.getSelectedItem().toString();
                    for (int i = 0; i < accountDetails.size(); i++) {
                        AccountDetails accountDetail = accountDetails.get(i);
                        if (accountDetail.account_name.equals(selectedAccount)) {
                            for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                                AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                                if (tempAccountAsset.id.equals(loyaltyAsset.id)) {
                                    loyaltyAsset = accountDetail.AccountAssets.get(j);
                                    break;
                                }
                            }
                        }
                    }
                }
                onLoyaltyChanged(etAmount.getText());
            }
            */
        }
        else
        {
            this.runningSpinerForFirstTime = false;
        }

        //  loadWebView(webviewFrom, 34, Helper.hash(spinnerFrom.getSelectedItem().toString(), Helper.MD5));
        loadWebView(webviewFrom,34, Helper.hash(spinnerFrom.getSelectedItem().toString(), Helper.SHA256));
    }

    @OnItemSelected(R.id.spAssets)
    void onAssetsSelected(int position) {
        if (loyaltyAsset == null) {
            updateAmountStatus();
        }
        if (backupAsset != null && !backupAsset.isEmpty()) {
            getExchangeRate(200);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @OnTextChanged(R.id.etLoyalty)
    void onLoyaltyChanged(CharSequence text) {

        if (text.toString().equals("")) {
            text = "0";
        } else if (text.toString().equals(".")) {
            text = "0.";
        }
        Double loyaltyAmount = Double.parseDouble(text.toString());
        Double loyaltyBalance = Double.parseDouble(loyaltyAsset.ammount) / Math.pow(10, Integer.parseInt(loyaltyAsset.precision));
        if (loyaltyAmount > loyaltyBalance) {
             tvLoyaltyStatus.setText(String.format(getString(R.string.str_warning_only_available), loyaltyBalance.toString(), loyaltyAsset.symbol));

        } else {
            String remainingBalance = String.format(Locale.ENGLISH, "%.4f", (loyaltyBalance - loyaltyAmount));
            tvLoyaltyStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, loyaltyAsset.symbol));
        }

        setHyperlinkText(tvLoyaltyStatus,loyaltyBalance.toString(),etLoyalty , 0, loyaltyAsset.symbol, Color.BLACK);

        if (loyaltyAsset != null && backupAssets != null) {
            count = 1;
            updateTotalStatus();
        } else {
            if (exchangeRate != null) {
                String backupAssetAmount = etBackupAsset.getText().toString();

                if (backupAssetAmount.equals("")) {
                    backupAssetAmount = "0";
                }
                Double remainingAmount = requiredAmount - (loyaltyAmount * exchangeRate) - Double.valueOf(backupAssetAmount);
                etAmount.setText(remainingAmount.toString());
                updateTotalStatus();
            } else {
                getExchangeRate(100);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refresh your views here
        super.onConfigurationChanged(newConfig);
    }

    private void backupAssetCHanged (String text)
    {
        count = 1;
        if (text.toString().equals(""))
        {
            text = "0";
        } else if (text.toString().equals("."))
        {
            text = "0.";
        }

        Double backupAssetAmount = Double.parseDouble(text.toString());
        Double backupAssetBalance = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));

        String backupAssetSym;
        AssetsSymbols assetsSymbols = new AssetsSymbols(context);
        backupAssetSym = assetsSymbols.updateString(backupAssets.symbol);

        if (backupAssetAmount > backupAssetBalance)
        {
            tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_warning_only_available), backupAssetBalance.toString(), backupAssetSym));
        }
        else
        {
            String remainingBalance = String.format(Locale.ENGLISH, "%.5f", (backupAssetBalance - backupAssetAmount));
            tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_balance_available), remainingBalance, backupAssetSym));
        }

        setHyperlinkText(tvBackupAssetBalanceValidate,backupAssetBalance.toString(),etBackupAsset , 0,backupAssetSym, Color.BLACK);

        if (backAssetRate != null)
        {
            String loyaltyAmount = etLoyalty.getText().toString();

            if (loyaltyAmount.equals(""))
            {
                loyaltyAmount = "0";
            }

            if (requiredAmount != null)
            {
                Double remainingAmount = requiredAmount - (backupAssetAmount * backAssetRate) - Double.valueOf(loyaltyAmount);
                etAmount.setText(remainingAmount.toString());
            }
            updateTotalStatus();
        }
        else
        {
            getExchangeRate(200);
        }
    }

    @OnTextChanged(R.id.etBackupAsset)
    void onBackupAssetChanged(CharSequence text) {
        backupAssetCHanged(text.toString());
    }


    @OnClick(R.id.btnSend)
    public void setBtnSend(View view) {
        sendBtnPressed = true;
        if (validReceiver)
            validatingComplete();
        else {
            tvErrorRecieverAccount.setText("");
            tvErrorRecieverAccount.setVisibility(View.VISIBLE);
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
    }

    void validatingComplete() {
        if (validateSend()) {
            progressDialog = new ProgressDialog(this);
            if (!etBackupAsset.getText().toString().equals("") && Double.parseDouble(etBackupAsset.getText().toString()) != 0) {
                if (Helper.fetchBoolianSharePref(this, "require_pin")) {
                    showDialogPin(true);
                } else {
                    String transferFunds = this.getString(R.string.transfer_funds) + "...";
                    showDialog("", transferFunds);
                    tradeAsset();
                }
            } else {
                if (Helper.fetchBoolianSharePref(this, "require_pin")) {
                    showDialogPin(false);
                } else {
                    sendFunds(false);
                }
            }
        }
    }

//    void timer(){
//        final Handler handler = new Handler();
//
//        final Runnable updateTask = new Runnable() {
//            @Override
//            public void run() {
//                if ( validReceiver )
//                {
//                    validatingComplete();
//                }
//                else {
//                    timer();
//                }
//            }
//        };
//
//        handler.postDelayed(updateTask, 1000);
//    }

    public void sendFunds(boolean isTrade) {

        String transferFunds = this.getString(R.string.transfer_funds) + "...";
        showDialog("", transferFunds);
        if (isTrade) {
            Double tradeAmount = Double.parseDouble(etBackupAsset.getText().toString()) * backAssetRate;
            if (!etAmount.getText().toString().equals("") && Double.parseDouble(etAmount.getText().toString()) != 0) {
                tradeAmount += Double.parseDouble(etAmount.getText().toString());
            }
            String mainAsset = spAssets.getSelectedItem().toString();
            mainAsset = mainAsset.replace("bit","");
            transferAmount(String.format(Locale.ENGLISH, "%." + selectedAccountAsset.precision + "f", tradeAmount), mainAsset, etReceiverAccount.getText().toString());
        } else if (!etAmount.getText().toString().equals("") && Double.parseDouble(etAmount.getText().toString()) != 0) {
            String mainAmount = String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(etAmount.getText().toString()));
            String mainAsset = spAssets.getSelectedItem().toString();
            mainAsset = mainAsset.replace("bit","");
            transferAmount(mainAmount, mainAsset, etReceiverAccount.getText().toString());
        }
        if (!etLoyalty.getText().toString().equals("") && Double.parseDouble(etLoyalty.getText().toString()) != 0) {
            String loyaltyAmount = String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(etLoyalty.getText().toString()));
            String loyaltyAsset = tvLoyalty.getText().toString();
            transferAmount(loyaltyAmount, loyaltyAsset, etReceiverAccount.getText().toString());
        }
        if (alwaysDonate || cbAlwaysDonate.isChecked()) {
            transferAmount("2", "BTS", "bitshares-munich");
        }
//        if (alwaysDonate) {
//            transferAmount("2", "BTS", "bitshares-munich");
//        }
    }

    private void selectedAccountAsset() {
        try {
            String selectedAsset = spAssets.getSelectedItem().toString();
            selectedAsset = selectedAsset.replace("bit","");
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                        AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                        if (tempAccountAsset.symbol.toLowerCase().equals(selectedAsset.toLowerCase())) {
                            selectedAccountAsset = accountDetail.AccountAssets.get(j);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            selectedAccountAsset = null;
        }
    }

    public void updateAmountStatus() {
        try {
            tvAmountStatus.setTextColor(tvTotalStatus.getTextColors());
            String selectedAsset;
            if(spAssets.getChildCount()>0) {
                selectedAsset = spAssets.getSelectedItem().toString();
            }else selectedAsset = "";
            selectedAccountAsset();
            Double selectedBalance = Double.parseDouble(selectedAccountAsset.ammount) / Math.pow(10, Integer.parseInt(selectedAccountAsset.precision));
            String availableBalance = selectedBalance.toString();

            if (etAmount.getText().length() > 0) {
                String enteredAmountStr = etAmount.getText().toString();

                if (enteredAmountStr.equals(".")) {
                    enteredAmountStr = "0.";
                }
                Double enteredAmount = Double.parseDouble(enteredAmountStr);
                if (enteredAmount != 0) {
                    String remainingBalance = "0";

                    if (enteredAmount > selectedBalance | enteredAmount < 0) {
                        //etAmount.setText(selectedBalance.toString());
                        validAmount = false;
                        tvAmountStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                        tvAmountStatus.setText(String.format(getString(R.string.str_warning_only_available), selectedBalance.toString(), selectedAsset));
                    }

                    else {
                        validAmount = true;

                        remainingBalance = String.format(Locale.ENGLISH, "%.4f", (selectedBalance - enteredAmount));
                        tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, selectedAsset));
                        setHyperlinkText(tvAmountStatus, availableBalance , etAmount , 0,selectedAsset, Color.BLACK); //shayan
                        updateTotalStatus(); // shayan
                    }

                } else {
                    if (!etLoyalty.getText().toString().equals("")) {
                        validAmount = true;
                    }
                    tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));
                }




            } else {
                etBackupAsset.setText(""); //shayan
                validAmount = false;
                tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));
                setHyperlinkText(tvAmountStatus, availableBalance , etAmount , 0,selectedAsset, Color.BLACK); //shayan
            }

            /*
            if ( !etBackupAsset.getText().toString().isEmpty() )
            {
                Double backupEnteredAMount = Double.parseDouble(etBackupAsset.getText().toString());
               // Double backupAvailableAmount = backu
            }
            */

            setHyperlinkText(tvAmountStatus, availableBalance , etAmount , 14,selectedAsset, Color.RED);
            tvAmountStatus.setTextColor(Color.RED); //shayan
            updateTotalStatus();
        }
        catch (Exception e){

        }
    }
    private void loadWebView(WebView webView,int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

   /* private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }*/

    CountDownTimer myLowerCaseTimer = new CountDownTimer(500, 500) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            if (!etReceiverAccount.getText().toString().equals(etReceiverAccount.getText().toString().toLowerCase())) {
                etReceiverAccount.setText(etReceiverAccount.getText().toString().toLowerCase());
                etReceiverAccount.setSelection(etReceiverAccount.getText().toString().length());
            }
        }
    };

    CountDownTimer myAccountNameValidationTimer = new CountDownTimer(1000, 1000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            createBitShareAN(false);
        }
    };

    private void lookupAccounts()
    {
        String socketText = getString(R.string.lookup_account_a);
        String scketText2 = getString(R.string.lookup_account_b) + "\"" + etReceiverAccount.getText().toString() + "\"" + ",50]],\"id\": 6}";
        myWebSocketHelper.make_websocket_call(socketText,scketText2, webSocketCallHelper.api_identifier.database);

        /*
        if ( Application.isReady )
        {
            String databaseIdentifier = Integer.toString(Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database)));
            String socketText = getString(R.string.lookup_account_a) + databaseIdentifier + getString(R.string.lookup_account_b) + "\"" + etReceiverAccount.getText().toString() + "\"" + ",50]],\"id\": 6}";
            Application.webSocketG.send(socketText);
        }
        else
        {
            Runnable toAccountValidation = new Runnable() {
                @Override
                public void run()
                {
                    sendScreenActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            createBitShareAN(false);
                        }
                    });
                }
            };

            reloadToAccountValidation.postDelayed(toAccountValidation,500);
        }
        */
    }

    Handler reloadToAccountValidation = new Handler();
    public void createBitShareAN(boolean focused) {
        if (!focused)
        {
            if (etReceiverAccount.getText().length() > 2)
            {
                tvErrorRecieverAccount.setText("");
                tvErrorRecieverAccount.setVisibility(View.GONE);

                lookupAccounts();
                /*
                if ( Application.isReady )
                {
                    String databaseIdentifier = Integer.toString(Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database)));
                    String socketText = getString(R.string.lookup_account_a) + databaseIdentifier + getString(R.string.lookup_account_b) + "\"" + etReceiverAccount.getText().toString() + "\"" + ",50]],\"id\": 6}";
                    Application.webSocketG.send(socketText);
                }
                else
                {
                    Runnable toAccountValidation = new Runnable() {
                        @Override
                        public void run()
                        {
                            sendScreenActivity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    createBitShareAN(false);
                                }
                            });
                        }
                    };

                    reloadToAccountValidation.postDelayed(toAccountValidation,500);
                }
                */
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void setCheckboxAvailabilty() {
        cbAlwaysDonate.setChecked(true);
        alwaysDonate = cbAlwaysDonate.isChecked();
    }

    @OnCheckedChanged(R.id.cbAlwaysDonate)
    public void cbAlwaysDonate() {

        alwaysDonate = cbAlwaysDonate.isChecked();
        String text = etMemo.getText().toString();
        text = text.replaceAll("\\s+", " ").trim();
        etMemo.setText(text);
        etMemo.setSelection(etMemo.getText().length());


    }

    @OnFocusChange(R.id.etMemo)
    public void onFocusChanged() {
        String text = etMemo.getText().toString();
        text = text.replaceAll("\\s+", " ").trim();
        etMemo.setText(text);
        etMemo.setSelection(etMemo.getText().length());
    }
//=======
//    public void cbAlwaysDonate() {
//        alwaysDonate = cbAlwaysDonate.isChecked();
////        if (Helper.fetchBoolianSharePref(this, getString(R.string.pref_always_donate))) {
////            Helper.storeBoolianSharePref(this,getString(R.string.pref_always_donate),false);
////            alwaysDonate = false;
////        }else {
////            alwaysDonate = true;
//        //   Helper.storeBoolianSharePref(this,getString(R.string.pref_always_donate),alwaysDonate);
//        // }
//    }
//>>>>>>> a20b337683a1729f357e22873fdc18bd14b02424

    void setBackUpAsset() {
        try {
            backupAsset = Helper.fetchStringSharePref(this, getString(R.string.pref_backup_symbol));

            if (backupAsset.isEmpty()) {
                backupAsset = "BTS";
            }

            String sym;
            AssetsSymbols assetsSymbols = new AssetsSymbols(context);
            sym = assetsSymbols.updateString(backupAsset);

            llBackupAsset.setVisibility(View.VISIBLE);
            tvBackupAsset.setText(sym);
            getBackupAsset();
            getExchangeRate(200);

        } catch (Exception e) {
            Log.d("setBackUpAsset", e.getMessage());
        }
    }

    private void getBackupAsset()
    {
        try
        {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            for (int i = 0; i < accountDetails.size(); i++)
            {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount))
                {
                    try {
                        for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                            AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                            if (tempAccountAsset.symbol.toLowerCase().equals(backupAsset.toLowerCase())) {
                                backupAssets = accountDetail.AccountAssets.get(j);
                                break;
                            }
                        }
                    }
                    catch (Exception e){
                        backupAssets = null;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("getBackupAsset", e.getMessage());
        }
    }

    @OnClick(R.id.scanning)
    void OnScanning() {
        Intent intent = new Intent(context, qrcodeActivity.class);
        intent.putExtra("id", 0);
        startActivityForResult(intent, 90);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    getJsonFromHash(res.getString("sResult"));
                }
                break;
        }
    }

    void onScanResult(String result) {
        try {
            JSONObject resJson = new JSONObject(result);
            callbackURL = resJson.get("callback").toString();
            if (!callbackURL.substring(callbackURL.length() - 1).equals("/")) {
                callbackURL = callbackURL + "/";
            }
            //  Toast.makeText(context,"Callback identified",Toast.LENGTH_LONG).show();
            etReceiverAccount.setText(resJson.get("to").toString());
            //validReceiver = true;

            spAssets.setSelection(getSpinnerIndex(spAssets, resJson.get("currency").toString()));
            spAssets.setClickable(false);
            if (resJson.get("memo") != null) {
                llMemo.setVisibility(View.GONE);
                etMemo.setText(resJson.get("memo").toString());
            } else llMemo.setVisibility(View.VISIBLE);
            JSONArray lineItems = new JSONArray(resJson.get("line_items").toString());
            Double totalAmount = 0.0;
            for (int i = 0; i < lineItems.length(); i++) {
                JSONObject lineItem = (JSONObject) lineItems.get(i);
                totalAmount += (Double.parseDouble(lineItem.get("quantity").toString()) * Double.parseDouble(lineItem.get("price").toString()));
            }
            requiredAmount = totalAmount;
            DecimalFormat df = new DecimalFormat("####.####");
            df.setRoundingMode(RoundingMode.CEILING);
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
            if (totalAmount != 0) {
                etAmount.setText(df.format(totalAmount));
                etAmount.setEnabled(false);
                spAssets.setEnabled(false);
            }
//        selectBTSAmount.setText(hash.get("currency"));
            String loyaltypoints = null;
            if (resJson.has("ruia")) {
                loyaltypoints = resJson.get("ruia").toString();
            }
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            if (loyaltypoints != null) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    AccountDetails accountDetail = accountDetails.get(i);
                    if (accountDetail.account_name.equals(selectedAccount)) {
                        for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                            AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                            if (tempAccountAsset.id.equals(loyaltypoints)) {
                                loyaltyAsset = accountDetail.AccountAssets.get(j);
                                break;
                            }
                        }
                    }
                }
                if (loyaltyAsset != null) {
                    getExchangeRate(100);
                    Double loyaltyBalance = Double.parseDouble(loyaltyAsset.ammount) / Math.pow(10, Integer.parseInt(loyaltyAsset.precision));
                    tvLoyalty.setText(loyaltyAsset.symbol);
                    tvLoyaltyStatus.setText(String.format(getString(R.string.str_balance_available), loyaltyBalance.toString(), loyaltyAsset.symbol));
                    setHyperlinkText(tvLoyaltyStatus,loyaltyBalance.toString(),etLoyalty ,0,loyaltyAsset.symbol, Color.BLACK );
                    llLoyalty.setVisibility(View.VISIBLE);
                    tvLoyaltyStatus.setVisibility(View.VISIBLE);
                }
            } else {
                llLoyalty.setVisibility(View.GONE);
                tvLoyaltyStatus.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSpinner(List<String> spinnerArray, Spinner spinner) {


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_black, spinnerArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void populateAccountsSpinner()
    {
        List<String> spinnerArray = new ArrayList<String>();

        String accountname="";
        for (int i = 0; i < accountDetails.size(); i++)
        {
            AccountDetails accountDetail = accountDetails.get(i);
            tvFrom.setText(accountDetail.account_name);
            if (accountDetail.isSelected)
                accountname = accountDetail.account_name;

            spinnerArray.add(accountDetail.account_name);
        }

        if (accountDetails.size() > 1)
        {
            spinnerFrom.setVisibility(View.VISIBLE);
            tvFrom.setVisibility(View.GONE);
        }
        else
        {
            loadWebView(webviewFrom,34, Helper.hash(tvFrom.getText().toString(), Helper.SHA256));
            // loadWebView(webviewFrom, 34, Helper.hash(tvFrom.getText().toString(), Helper.MD5));
            spinnerFrom.setVisibility(View.GONE);
            tvFrom.setVisibility(View.VISIBLE);
        }

        createSpinner(spinnerArray, spinnerFrom);

        if (accountname.isEmpty())
        {
            spinnerFrom.setSelection(0);
        }
        else
        {
            spinnerFrom.setSelection(spinnerArray.indexOf(accountname));
        }
    }

    public void populateAssetsSpinner()
    {
        try
        {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            ArrayList<String> spinnerArray = new ArrayList<String>();
            for (int i = 0; i < accountDetails.size(); i++)
            {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount))
                {
                    try {
                        for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                            selectedAccountAsset = accountDetail.AccountAssets.get(j);
                            spinnerArray.add(selectedAccountAsset.symbol);
                        }
                    }
                    catch (Exception r){
                        selectedAccountAsset = null;
                    }
                }
            }

            AssetsSymbols assetsSymbols = new AssetsSymbols(getApplicationContext());
            spinnerArray = assetsSymbols.updatedList(spinnerArray);

            createSpinner(spinnerArray, spAssets);
        }
        catch (Exception e)
        {
        }
    }

    void setSpinner()
    {
        populateAccountsSpinner();
        populateAssetsSpinner();
        setBackUpAsset();
    }

    public void getExchangeRate(int id) {
        //id 200 for exchange rate
        selectedAccountAsset();
        if (backupAssets != null && id == 200) {
            if (selectedAccountAsset.id.equals(backupAssets.id)) {
                backAssetRate = 1.0;
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateTotalStatus();
                    }
                });
            } else {
                findExchangeRate(id);
            }
        } else {
            findExchangeRate(id);
        }
    }



    Boolean checkLastIndex() {
        String name = etAmount.getText().toString();
        if (name.length() >
                0) {
            String lastWord = String.valueOf(name.charAt(name.length() - 1));
            return lastWord.equals(".");
        }
        return false;
    }

    Boolean checkIfZero() {
        try
        {
            String amount = etAmount.getText().toString();

            if ( amount.isEmpty() )
            {
                return true;
            }

            Double am = Double.parseDouble(amount);

            if (am <= 0.0)
            {
                return true;
            }
            
            return false;
        }
        catch (Exception e)
        {
            return true;
        }
    }

    public boolean validateSend() {
        if (spinnerFrom.getSelectedItem().toString().equals("")) {
            return false;
//        } else if (!validReceiver) {
//            Toast.makeText(context, R.string.str_invalid_receiver, Toast.LENGTH_SHORT).show();
//            return false;
        } else if (spinnerFrom.getSelectedItem().toString().equals(etReceiverAccount.getText().toString())) {
            return false;
        } else if (!validAmount) {
            //   Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        } else if (etAmount.getText().toString().equals(".")) {
            //   Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        } else if (checkLastIndex()) {
            //   Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        } else if (checkIfZero()) {
            //   Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void transferAmount(String amount, String symbol, String toAccount) {
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        String privateKey = "";
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)) {
                try {
                    privateKey = Crypt.getInstance().decrypt_string(accountDetail.wif_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String memo = etMemo.getText().toString();
        if (toAccount.equals("bitshares-munich")) {
            memo = "Donation";
        }
        HashMap<String,String> hm = new HashMap<>();
        hm.put("method", "transfer");
        hm.put("wifkey", privateKey);
        hm.put("from_account", spinnerFrom.getSelectedItem().toString());
        hm.put("to_account", toAccount);
        hm.put("amount", amount);
        hm.put("asset_symbol", symbol);
        hm.put("memo", memo);

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<TransferResponse> postingService = service.getTransferResponse(hm);
        postingService.enqueue(new Callback<TransferResponse>() {
            @Override
            public void onResponse(Response<TransferResponse> response) {
                if (response.isSuccess()) {
                    TransferResponse resp = response.body();
                    if (resp.status.equals("success")) {
                        if (callbackURL != null) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getTrxBlock("160");
                                }
                            }, 5000);
                        }
                        if (!isFinishing()) {
                            //Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                            //startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(context, R.string.str_transaction_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.unable_to_send_amount), Toast.LENGTH_SHORT).show();
                }
                hideDialog();
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.unable_to_send_amount), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            String spString = spinner.getItemAtPosition(i).toString();
            spString = spString.replace("bit","");
            if (spString.equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void callback_exchange_rate(JSONObject result, int id) throws JSONException {
        myWebSocketHelper.cleanUpTransactionsHandler();

        if (result.length() > 0) {

            JSONObject sell_price = (JSONObject) result.get("sell_price");
            JSONObject base = (JSONObject) sell_price.get("quote");
            String base_amount = base.get("amount").toString();
            JSONObject quote = (JSONObject) sell_price.get("base");
            String quote_amount = quote.get("amount").toString();
            Double baseWithPrecision = Double.parseDouble(base_amount) / Math.pow(10, Double.parseDouble(backupAssets.precision));
            if (id == 200) {
                Double quoteWithPrecision = Double.parseDouble(quote_amount) / Math.pow(10, Double.parseDouble(selectedAccountAsset.precision));
                backAssetRate = quoteWithPrecision / baseWithPrecision;
            } else if (id == 100) {
                Double quoteWithPrecision = Double.parseDouble(quote_amount) / Math.pow(10, Double.parseDouble(loyaltyAsset.precision));
                exchangeRate = quoteWithPrecision / baseWithPrecision;
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    updateTotalStatus();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, R.string.str_trading_pair_not_exist, Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    @Override
    public void checkAccount(JSONObject jsonObject) {

        myWebSocketHelper.cleanUpTransactionsHandler();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            boolean found = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                final String temp = jsonArray.getJSONArray(i).getString(0);
                if (temp.equals(etReceiverAccount.getText().toString())) {
                    found = true;
                    validReceiver = true;
                    receiverID = jsonArray.getJSONArray(i).getString(1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (sendBtnPressed) {
                                validatingComplete();
                            }
                        }
                    });
                    sendBtnPressed = false;
                    validating = false;
                }
            }
            if (!found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validReceiver = false;
                        sendBtnPressed = false;
                        validating = false;
                        // This code works correct, donot edit if it shows red underline
                        String format = String.format(getResources().getString(R.string.account_name_not_exist), etReceiverAccount.getText());
                        format = format.replaceAll("\\s+", " ").trim();
                        tvErrorRecieverAccount.setText(format);
                        tvErrorRecieverAccount.setVisibility(View.VISIBLE);
                    }
                });
            }
        } catch (Exception e) {
            sendBtnPressed = false;
        }
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

    private void hideDialog() {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }

    }

    public void updateTotalStatus() {

        try {
            // Send screen work start
            Double selectedBalance = Double.parseDouble(selectedAccountAsset.ammount) / Math.pow(10, Integer.parseInt(selectedAccountAsset.precision));
            Double backupAssetsBalance = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));

            String selectedAmount = Helper.padString(etAmount.getText().toString());
            Double enteredAmount = Double.parseDouble(selectedAmount);
            String loyaltyAmount = Helper.padString(etLoyalty.getText().toString());
            String backupAssetAmount = Helper.padString(etBackupAsset.getText().toString());
            if ((enteredAmount > selectedBalance) | (enteredAmount < 0)) {
                selectedAmount = String.valueOf(selectedBalance);
                if ((backupAssetsBalance / backAssetRate) - (Double.parseDouble(String.valueOf(enteredAmount - selectedBalance)) / backAssetRate) > 0) {
                    backupAssetAmount = String.format(Locale.ENGLISH, "%.4f", ((enteredAmount - selectedBalance) / backAssetRate));

                }
            }
            // Send screen work end

            String selectedAccountAssetSym;
            AssetsSymbols assetsSymbols = new AssetsSymbols(context);
            selectedAccountAssetSym = assetsSymbols.updateString(selectedAccountAsset.symbol);
//
//            String selectedAccountAssetSym;
//            AssetsSymbols assetsSymbols = new AssetsSymbols(context);
//            selectedAccountAssetSym = assetsSymbols.updateString(selectedAccountAsset.symbol);


            if (loyaltyAsset != null && backupAssets != null && exchangeRate != null && backAssetRate != null) {
                if (count == 1) {
                    Double totalAmountLoyalty = (Double.parseDouble(loyaltyAmount) * exchangeRate);
                    Double totalAmountBackupAssets = (Double.parseDouble(backupAssetAmount) * backAssetRate);
                    Double amount = requiredAmount - totalAmountBackupAssets - totalAmountLoyalty;
                    String temp = etAmount.getText().toString();
                    if (!temp.equals(String.format(Locale.ENGLISH, "%.4f", amount))) {
                        etAmount.setText(String.format(Locale.ENGLISH, "%.4f", amount));
                    }
                    count = 0;
                    String backupAssetSym;
                    backupAssetSym = assetsSymbols.updateString(backupAssets.symbol);

                    Double total = amount + totalAmountBackupAssets + totalAmountLoyalty;
                    tvTotalStatus.setText(String.format(getString(R.string.str_total_status),
                            String.format(Locale.ENGLISH, "%.4f", amount), selectedAccountAssetSym, backupAssetAmount + backupAssetSym + " + ", loyaltyAmount + loyaltyAsset.symbol
                            , String.format(Locale.ENGLISH, "%.4f", total), selectedAccountAssetSym));
                }
                count++;
            } else if (loyaltyAsset != null && exchangeRate != null) {
                Double totalAmount = Double.parseDouble(selectedAmount) + (Double.parseDouble(loyaltyAmount) * exchangeRate);
                tvTotalStatus.setText(String.format(getString(R.string.str_total_status), selectedAmount, selectedAccountAssetSym, loyaltyAmount, loyaltyAsset.symbol, String.format(Locale.ENGLISH, "%.4f", totalAmount), selectedAccountAssetSym));
            } else if (backupAssets != null && backAssetRate != null)
            {
                Double totalAmount = Double.parseDouble(selectedAmount) + (Double.parseDouble(backupAssetAmount) * backAssetRate);
                String backupAssetSym;
                backupAssetSym = assetsSymbols.updateString(backupAssets.symbol);
                tvTotalStatus.setText(String.format(getString(R.string.str_total_status),
                        selectedAmount, selectedAccountAssetSym, backupAssetAmount,
                        backupAssetSym, String.format(Locale.ENGLISH, "%.4f", totalAmount), selectedAccountAssetSym));

                setHyperlinkTextDouble(tvTotalStatus,selectedAmount.toString(),backupAssetAmount.toString(),etAmount, etBackupAsset,1,selectedAccountAssetSym, backupAssetSym); //

            }
            tvTotalStatus.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.unable_to_process + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void findExchangeRate(int id)
    {
        if (Application.isReady)
        {
            //int db_identifier = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database));
            String loyalOrBackupAssets = "";
            if (id == 200) {
                loyalOrBackupAssets = backupAssets.id;
            } else if (id == 100) {
                loyalOrBackupAssets = loyaltyAsset.id;
            }

            String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
            String params2 = ",\"get_limit_orders\",[\"" + loyalOrBackupAssets + "\",\"" + selectedAccountAsset.id + "\",1]]}";
            myWebSocketHelper.make_websocket_call(params,params2, webSocketCallHelper.api_identifier.database);
            //Application.webSocketG.send(params);
        }
    }

    //********Get trx block****************Start
    Handler reTryGetTrxBlock = new Handler();
    int reTryTimeGetTrxBlock = 1000;
    boolean callInProgressForGettingTrx = false;
    boolean callReceivedForGettingTrx = true;

    public void getTrxBlock(final String id)
    {

        String selectedAccountId = "";
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)) {
                selectedAccountId = accountDetail.account_id;
            }
        }
        //int historyIdentifier = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_history));
        String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String params2 = ",\"get_relative_account_history\",[\"" + selectedAccountId + "\",0,10,0]]}";
        myWebSocketHelper.make_websocket_call(params,params2, webSocketCallHelper.api_identifier.history);


        /*
        reTryGetTrxBlock.removeCallbacksAndMessages(null);

        final Runnable checkifRecieved = new Runnable() {
            @Override
            public void run() {
                if ( callInProgressForGettingTrx && !callReceivedForGettingTrx) // if balances are not returned in one second
                {
                    if ( Application.isReady )
                    {
                        Application.webSocketG.close();
                        getTrxBlock(id);
                    }
                    else
                    {
                        getTrxBlock(id);
                    }
                }
            }
        };


        final Runnable initiateTransactions = new Runnable() {
            @Override
            public void run()
            {
                if ( Application.isReady )
                {
                    String selectedAccountId = "";
                    String selectedAccount = spinnerFrom.getSelectedItem().toString();
                    for (int i = 0; i < accountDetails.size(); i++) {
                        AccountDetails accountDetail = accountDetails.get(i);
                        if (accountDetail.account_name.equals(selectedAccount)) {
                            selectedAccountId = accountDetail.account_id;
                        }
                    }
                    int historyIdentifier = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_history));
                    String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + historyIdentifier + ",\"get_relative_account_history\",[\"" + selectedAccountId + "\",0,10,0]]}";
                    Application.webSocketG.send(params);

                    callInProgressForGettingTrx = true;
                    callReceivedForGettingTrx = false;
                    reTryGetTrxBlock.postDelayed(checkifRecieved, reTryTimeGetTrxBlock);
                }
                else
                {
                    getTrxBlock(id);
                }
            }
        };

        reTryGetTrxBlock.postDelayed(initiateTransactions,0);
        */

        /*
        if (Application.isReady)
        {
            String selectedAccountId = "";
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    selectedAccountId = accountDetail.account_id;
                }
            }
            int historyIdentifier = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_history));
            String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + historyIdentifier + ",\"get_relative_account_history\",[\"" + selectedAccountId + "\",0,10,0]]}";
            Application.webSocketG.send(params);
        }
        */
    }



    private void fetchTrxBlockAndUpdateServer(final JSONArray jsonArray)
    {
        try
        {
            final Runnable reTry = new Runnable() {
                @Override
                public void run() {
                    fetchTrxBlockAndUpdateServer(jsonArray);
                }
            };

            for (int i = 0; i < 2; i++)
            {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                JSONArray opArray = (JSONArray) jsonObject.get("op");
                JSONObject operation = (JSONObject) opArray.get(1);

                if (operation.get("to").toString().equals(receiverID))
                {
                    ServiceGenerator sg = new ServiceGenerator(callbackURL);
                    IWebService service = sg.getService(IWebService.class);

                    if ( jsonObject.has("block_num") && jsonObject.has("trx_in_block") )
                    {

                        final Call<Void> postingService = service.sendCallback(callbackURL, jsonObject.get("block_num").toString(), jsonObject.get("trx_in_block").toString());
                        postingService.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Response<Void> response) {
                                if (response.isSuccess()) {

                                } else {
                                    reTryGetTrxBlock.postDelayed(reTry, 100);
                                    //Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                reTryGetTrxBlock.postDelayed(reTry, 100);
                            }
                        });
                        break;
                    }
                    else
                    {
                        getTrxBlock("160");
                    }
                }
                else
                {
                    getTrxBlock("160");
                }
            }
        }
        catch (Exception e)
        {
            //getTrxBlock("160");
        }
    }

    //********Get trx block****************End

    @Override
    public void relativeHistoryCallback(JSONObject msg) {
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONArray jsonArray = (JSONArray) msg.get("result");
            boolean found = false;
            if (msg.get("id").equals(160))
            {
                callInProgressForGettingTrx = true;
                callReceivedForGettingTrx = false;
                reTryGetTrxBlock.removeCallbacksAndMessages(null);

                fetchTrxBlockAndUpdateServer(jsonArray);

                /*
                for (int i = 0; i < 2; i++)
                {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    JSONArray opArray = (JSONArray) jsonObject.get("op");
                    JSONObject operation = (JSONObject) opArray.get(1);
                    if (operation.get("to").toString().equals(receiverID))
                    {
                        found = true;
                        ServiceGenerator sg = new ServiceGenerator(callbackURL);
                        IWebService service = sg.getService(IWebService.class);
                        final Call<Void> postingService = service.sendCallback(callbackURL, jsonObject.get("block_num").toString(), jsonObject.get("trx_in_block").toString());
                        postingService.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Response<Void> response)
                            {
                                if (response.isSuccess())
                                {

                                }
                                else
                                {
                                //Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Throwable t) {
                            }
                        });
                        break;
                    }
                }
                */
            }
            if (msg.get("id").equals(161)) {
                for (int i = 0; i < 2; i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    JSONArray opArray = (JSONArray) jsonObject.get("op");
                    if (opArray.get(0).equals(4)) {
                        JSONObject operation = (JSONObject) opArray.get(1);
                        JSONObject pays = (JSONObject) operation.get("pays");
                        if (pays.get("asset_id").equals(backupAssets.id) && Double.parseDouble(pays.get("amount").toString()) == Math.round(sellAmount * Math.pow(10, Double.parseDouble(backupAssets.precision)))) {
                            found = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideDialog();
                                    sendFunds(true);
                                }
                            });
                            break;
                        }
                    }
                }
                if (!found) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideDialog();
                            Toast.makeText(context, R.string.str_trade_not_available, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tradeAsset() {
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        String privateKey = "";
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)) {
                try {
                    privateKey = Crypt.getInstance().decrypt_string(accountDetail.wif_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        sellAmount = Double.parseDouble(etBackupAsset.getText().toString());
        sellAmount = sellAmount + (sellAmount * 0.5 / 100);
        Double buyAmount = sellAmount * backAssetRate;
        HashMap<String,String> hm = new HashMap<>();
        hm.put("method", "trade");
        hm.put("wifkey", privateKey);
        hm.put("account", spinnerFrom.getSelectedItem().toString());
        hm.put("sell_amount", String.format(Locale.ENGLISH, "%.4f", sellAmount)); //shayan
        hm.put("sell_symbol", backupAsset);
        hm.put("buy_amount", String.format(Locale.ENGLISH, "%.4f", buyAmount));
        hm.put("buy_symbol", selectedAccountAsset.symbol);
        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<TradeResponse> postingService = service.getTradeResponse(hm);
        postingService.enqueue(new Callback<TradeResponse>() {
            @Override
            public void onResponse(Response<TradeResponse> response) {
                if (response.isSuccess()) {
                    TradeResponse resp = response.body();
                    if (resp.status.equals("success")) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getTrxBlock("161");
                            }
                        }, 15000);
                    } else {
                        Toast.makeText(context, R.string.str_transaction_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.unable_to_trade_bAssets), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.unable_to_trade_bAssets), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getJsonFromHash(String hash) {
        ServiceGenerator sg = new ServiceGenerator(getString(R.string.hash_server_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<QrJson> postingService = service.getJson(hash);
        postingService.enqueue(new Callback<QrJson>() {
            @Override
            public void onResponse(Response<QrJson> response) {
                if (response.isSuccess()) {
                    QrJson resp = response.body();
                    if (resp.status.equals("success")) {
                        saveMerchantEmail(resp.json);
                        onScanResult(resp.json);
                    } else {
                        Toast.makeText(context, R.string.str_transaction_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.unable_to_decode_QR), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.unable_to_decode_QR), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveMerchantEmail(String string) {
        String json = SupportMethods.ParseJsonObject(string, "json");
        String accountName = SupportMethods.ParseJsonObject(json, "to");
        String note = SupportMethods.ParseJsonObject(json, "note");
        if (!note.equals("")) {
            String email = note.replace("merchant_email:\"", "");
            if (email.length() > 0) {
                email = email.substring(0, email.length() - 1);
                if (SupportMethods.isEmailValid(email)) {
                    MerchantEmail merchantEmail = new MerchantEmail(getApplicationContext());
                    merchantEmail.saveMerchantEmail(accountName, email);
                }
            }
        }
    }

    /// Updating Block Number and status
    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.webSocketG != null)
                {
                    if (Application.webSocketG.isOpen()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
                    } else {
                        ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                        ivSocketConnected.startAnimation(myFadeInAnimation);
                    }
                } else {
                    ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                    ivSocketConnected.startAnimation(myFadeInAnimation);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTask, 1000);
    }

    @OnClick(R.id.OnClickSettings_send_screen_activity)
    void OnClickSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>= API 21
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    Dialog contactListDialog;

    @OnClick(R.id.contactActivity)
    void OnClickContactBtn(View view) {
//        List<String> contactlist = new ArrayList<String>();
//
//        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
//        for (int i = 0; i < contacts.size(); i++) {
//            contactlist.add(contacts.get(i).GetAccount() + "::" + Integer.toString(i));
//        }
//
//        Collections.sort(contactlist, new Comparator<String>() {
//            @Override
//            public int compare(String s1, String s2) {
//                return s1.compareToIgnoreCase(s2);
//            }
//        });
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, contactlist) {
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//
//                // setting the ID and text for every items in the list
//                String item = getItem(position);
//                String[] itemArr = item.split("::");
//                String text = itemArr[0];
//                String id = itemArr[1];
//
//                // visual settings for the list item
//                TextView listItem = new TextView(context);
//                listItem.setTextColor(Color.BLACK);
//                listItem.setGravity(Gravity.START);
//                listItem.setText(text);
//                listItem.setTag(id);
//                listItem.setTextSize(20);
//                listItem.setPadding(10, 15, 15, 10);
//
//                return listItem;
//            }
//        };
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);

        if(contacts.size()>0) {
            contactListDialog = new Dialog(SendScreen.this);
            contactListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            contactListDialog.setContentView(R.layout.contacts_list_send_screen);
            ListView listView = (ListView) contactListDialog.findViewById(R.id.contactsListSendScreen);
            listView.setAdapter(new SendScreenListViewActivity(context, this));
            int size = listView.getAdapter().getCount();
            if (size > 5) {
                setListViewHeightBasedOnChildren(listView, 5);
            } else setListViewHeightBasedOnChildren(listView, size);
            //     listView.setOnItemClickListener(new DropdownOnItemClickListener());
            contactListDialog.show();
        }else {
            Toast.makeText(context, R.string.empty_list, Toast.LENGTH_SHORT).show();
        }
    }

    //    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
////            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
////            fadeInAnimation.setDuration(10);
////            v.startAnimation(fadeInAnimation);
//            contactListDialog.dismiss();
//            TextView selectedItemText = (TextView) v.findViewById(R.id.accountname);
//            etReceiverAccount.setText(selectedItemText.getText());
//            createBitShareAN(false);
//        }
//    }
    public static void setListViewHeightBasedOnChildren(ListView listView,int size) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < size; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (5 - 1));
        listView.setLayoutParams(params);
    }


    @Override
    public void onResume() {
        super.onResume();
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        init();
        populateAccountsSpinner();
    }

    private void showDialogPin(final Boolean fundTransfer) {
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        final Dialog dialog = new Dialog(SendScreen.this);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
        dialog.setTitle(R.string.pin_verification);
        dialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final EditText etPin = (EditText) dialog.findViewById(R.id.etPin);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                            dialog.cancel();
                            if (fundTransfer) {
                                String transferFunds = getString(R.string.transfer_funds) + "...";
                                showDialog("", transferFunds);
                                tradeAsset();
                            } else {
                                sendFunds(false);
                            }
                            break;
                        }
                    }
                }

            }
        });
        dialog.setCancelable(true);

        dialog.show();
    }

    @Override
    public void isClicked(String s){
        etReceiverAccount.setText(s);
        if(contactListDialog!=null)
            contactListDialog.dismiss();
        createBitShareAN(false);
    }

    void setHyperlinkText(TextView textView , final String balances , final EditText editText , int UnderlineStartingIndex, String symbol, final int color){
        String text = textView.getText().toString();
        String available = new String(symbol);

        int index = text.indexOf( available );

        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                editText.setText(balances);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                    ds.setColor(color);
            }
        };
        ss.setSpan(clickableSpan, UnderlineStartingIndex , index-1 , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setTextColor(Color.BLACK);
    }

    void setHyperlinkTextDouble(TextView textView, final String balances , final String balances2, final EditText editText , final EditText editText2 ,int UnderlineStartingIndex, String symbol, String symbol2) //shayan
    {
        String text = textView.getText().toString();
        String available = new String(symbol2);

        int index = text.indexOf( available );

        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                editText.setText(balances);
                editText2.setText(balances2);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.BLACK);
            }
        };
        ss.setSpan(clickableSpan, UnderlineStartingIndex , index-1 , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setTextColor(Color.BLACK);
    }
}