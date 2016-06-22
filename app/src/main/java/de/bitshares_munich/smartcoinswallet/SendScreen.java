package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/6/16.
 */
public class SendScreen extends BaseActivity implements IExchangeRate, IAccount, IRelativeHistory {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);

        setBackButton(true);

        setTitle(getResources().getString(R.string.send_screen_name));

        context = getApplicationContext();
        ButterKnife.bind(this);
        application.registerExchangeRateCallback(this);
        application.registerCallback(this);
        application.registerRelativeHistoryCallback(this);

        tinyDB = new TinyDB(context);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);


        init();
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null) {
            if (res.containsKey("sResult") && res.containsKey("id")) {
                if (res.getInt("id") == 5) {
                    getJsonFromHash(res.getString("sResult"));
                }
            }
        }


        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        //loadWebView(webviewFrom ,34 , accountDetails.);
        //webviewFrom.setVisibility(View.VISIBLE);
        loadWebView(webviewTo, 34, Helper.md5(""));
        updateBlockNumberHead();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if (validateSend() && validReceiver) {
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
    }

    void init() {
        cbAlwaysDonate.setText(getString(R.string.checkbox_donate)+" BitShares Munich");
        setCheckboxAvailabilty();
        setSpinner();
    }

    @OnTextChanged(R.id.etReceiverAccount)
    void onTextChangedTo(CharSequence text) {
        validReceiver = false;
        tvErrorRecieverAccount.setText("");

        if (etReceiverAccount.getText().length() > 0) {
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
        tvErrorRecieverAccount.setVisibility(View.GONE);

        loadWebView(webviewTo, 34, Helper.md5(etReceiverAccount.getText().toString()));
    }

    @OnFocusChange(R.id.etReceiverAccount)
    public void onFocusChange(boolean hasFocus) {
        validReceiver = false;

        if (!hasFocus) {
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
    void onItemSelected(int position) {
        if (!runningSpinerForFirstTime) {
            if (requiredAmount == null) {
                populateAssetsSpinner();
            } else {
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
        } else {
            this.runningSpinerForFirstTime = false;
        }

        loadWebView(webviewFrom, 34, Helper.md5(spinnerFrom.getSelectedItem().toString()));

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

    @OnTextChanged(R.id.etBackupAsset)
    void onBackupAssetChanged(CharSequence text) {
        count = 1;
        if (text.toString().equals("")) {
            text = "0";
        } else if (text.toString().equals(".")) {
            text = "0.";
        }
        Double backupAssetAmount = Double.parseDouble(text.toString());
        Double backupAssetBalance = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));
        if (backupAssetAmount > backupAssetBalance) {
            tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_warning_only_available), backupAssetBalance.toString(), backupAssets.symbol));
        } else {
            String remainingBalance = String.format(Locale.ENGLISH, "%.4f", (backupAssetBalance - backupAssetAmount));
            tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_balance_available), remainingBalance, backupAssets.symbol));
        }
        if (backAssetRate != null) {
            String loyaltyAmount = etLoyalty.getText().toString();
            if (loyaltyAmount.equals("")) {
                loyaltyAmount = "0";
            }
            if (requiredAmount != null) {
                Double remainingAmount = requiredAmount - (backupAssetAmount * backAssetRate) - Double.valueOf(loyaltyAmount);
                etAmount.setText(remainingAmount.toString());
            }
            updateTotalStatus();
        } else {
            getExchangeRate(200);
        }
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
            transferAmount(String.format(Locale.ENGLISH, "%." + selectedAccountAsset.precision + "f", tradeAmount), mainAsset, etReceiverAccount.getText().toString());
        } else if (!etAmount.getText().toString().equals("") && Double.parseDouble(etAmount.getText().toString()) != 0) {
            String mainAmount = String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(etAmount.getText().toString()));
            String mainAsset = spAssets.getSelectedItem().toString();
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
        String selectedAsset = spAssets.getSelectedItem().toString();
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
    }

    public void updateAmountStatus() {

        String selectedAsset = spAssets.getSelectedItem().toString();
        selectedAccountAsset();
        Double selectedBalance = Double.parseDouble(selectedAccountAsset.ammount) / Math.pow(10, Integer.parseInt(selectedAccountAsset.precision));
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
                    tvAmountStatus.setText(String.format(getString(R.string.str_warning_only_available), selectedBalance.toString(), selectedAsset));
                } else {
                    validAmount = true;

                    remainingBalance = String.format(Locale.ENGLISH, "%.4f", (selectedBalance - enteredAmount));
                    tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, selectedAsset));

                }

            } else {
                if (!etLoyalty.getText().toString().equals("")) {
                    validAmount = true;
                }
                tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));

            }
        } else {
            validAmount = false;
            tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));
        }
        updateTotalStatus();

    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

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

    public void createBitShareAN(boolean focused) {
        if (!focused) {

            if (etReceiverAccount.getText().length() > 2) {
                tvErrorRecieverAccount.setText("");
                tvErrorRecieverAccount.setVisibility(View.GONE);
                if (Application.webSocketG.isOpen()) {
                    String socketText = getString(R.string.lookup_account_a) + "\"" + etReceiverAccount.getText().toString() + "\"" + ",50]],\"id\": 6}";
                    Application.webSocketG.send(socketText);

                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void setCheckboxAvailabilty() {
//        if(!Helper.containKeySharePref(this, getString(R.string.pref_always_donate))){
//            Helper.storeBoolianSharePref(this,getString(R.string.pref_always_donate),false);
//        }
//        if (Helper.fetchBoolianSharePref(this, getString(R.string.pref_always_donate))) {
//            cbAlwaysDonate.setChecked(true);
//            alwaysDonate = true;
//        } else {
//            cbAlwaysDonate.setChecked(false);
//            alwaysDonate = false;
//        }
//
//
//        cbAlwaysDonate.setVisibility(View.VISIBLE);

        cbAlwaysDonate.setChecked(true);
        alwaysDonate = cbAlwaysDonate.isChecked();
        etMemo.setText(getString(R.string.donation));
    }

    @OnCheckedChanged(R.id.cbAlwaysDonate)
    public void cbAlwaysDonate(){

        alwaysDonate = cbAlwaysDonate.isChecked();
        String text = etMemo.getText().toString();

        if(alwaysDonate){
            if(!text.contains(getString(R.string.donation))){
                text = getString(R.string.donation) + " " + text;
            }
        }else {
            if(text.contains(getString(R.string.donation))){
                text = text.replace(getString(R.string.donation),"");
            }
        }
        text=text.replaceAll("\\s+", " ").trim();
        etMemo.setText(text);
        etMemo.setSelection(etMemo.getText().length());


    }
    @OnFocusChange(R.id.etMemo)
    public void onFocusChanged(){
        String text = etMemo.getText().toString();
        if(alwaysDonate){
            if(!text.contains(getString(R.string.donation))){
                text = getString(R.string.donation) + " " + text;
            }
        }else {
            if(text.contains(getString(R.string.donation))){
                text = text.replace(getString(R.string.donation),"");
            }
        }
        text=text.replaceAll("\\s+", " ").trim();
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
            llBackupAsset.setVisibility(View.VISIBLE);
            tvBackupAsset.setText(backupAsset);
            getBackupAsset();
            getExchangeRate(200);

        } catch (Exception e) {
            Log.d("setBackUpAsset", e.getMessage());
        }
    }

    private void getBackupAsset() {
        try {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                        AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                        if (tempAccountAsset.symbol.toLowerCase().equals(backupAsset.toLowerCase())) {
                            backupAssets = accountDetail.AccountAssets.get(j);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
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
            etAmount.setText(totalAmount.toString());
            etAmount.setEnabled(false);
            spAssets.setEnabled(false);

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
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void populateAccountsSpinner() {
        List<String> spinnerArray = new ArrayList<String>();
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            tvFrom.setText(accountDetail.account_name);
            spinnerArray.add(accountDetail.account_name);
        }
        if (accountDetails.size() > 1) {
            spinnerFrom.setVisibility(View.VISIBLE);
            tvFrom.setVisibility(View.GONE);
        } else {
            loadWebView(webviewFrom, 34, Helper.md5(tvFrom.getText().toString()));
            spinnerFrom.setVisibility(View.GONE);
            tvFrom.setVisibility(View.VISIBLE);
        }
        createSpinner(spinnerArray, spinnerFrom);
    }

    public void populateAssetsSpinner() {
        try {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            List<String> spinnerArray = new ArrayList<String>();
            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                        selectedAccountAsset = accountDetail.AccountAssets.get(j);
                        spinnerArray.add(selectedAccountAsset.symbol);
                    }
                }
            }
            createSpinner(spinnerArray, spAssets);
        } catch (Exception e) {
        }
    }

    void setSpinner() {
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

    private void findExchangeRate(int id) {
        if (application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database));
            String loyalOrBackupAssets = "";
            if (id == 200) {
                loyalOrBackupAssets = backupAssets.id;
            } else if (id == 100) {
                loyalOrBackupAssets = loyaltyAsset.id;
            }
            String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_identifier + ",\"get_limit_orders\",[\"" + loyalOrBackupAssets + "\",\"" + selectedAccountAsset.id + "\",1]]}";
            application.webSocketG.send(params);
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

    public boolean validateSend() {
        if (spinnerFrom.getSelectedItem().toString().equals("")) {
            return false;
//        } else if (!validReceiver) {
//            Toast.makeText(context, R.string.str_invalid_receiver, Toast.LENGTH_SHORT).show();
//            return false;
        } else if (spinnerFrom.getSelectedItem().toString().equals(etReceiverAccount.getText().toString())) {
            //  Toast.makeText(context, R.string.str_invalid_receiver, Toast.LENGTH_SHORT).show();
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
        } else if (etAmount.getText().toString().equals("0")) {
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
        HashMap hm = new HashMap();
        hm.put("method", "transfer");
        hm.put("wifkey", privateKey);
        hm.put("from_account", spinnerFrom.getSelectedItem().toString());
        hm.put("to_account", toAccount);
        hm.put("amount", amount);
        hm.put("asset_symbol", symbol);
        hm.put("memo", etMemo.getText().toString());

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.transfer_server_url));
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
                    Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                hideDialog();
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void callback_exchange_rate(JSONObject result, int id) throws JSONException {


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
                }
            }
            if (!found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validReceiver = false;
                        sendBtnPressed = false;
                        // This code works correct, donot edit if it shows red underline
                        String format = String.format(getResources().getString(R.string.account_name_not_exist), etReceiverAccount.getText());
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

            String selectedAmount = Helper.padString(etAmount.getText().toString());
            String loyaltyAmount = Helper.padString(etLoyalty.getText().toString());
            String backupAssetAmount = Helper.padString(etBackupAsset.getText().toString());
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
                    Double total = amount + totalAmountBackupAssets + totalAmountLoyalty;
                    tvTotalStatus.setText(String.format(getString(R.string.str_total_status),
                            String.format(Locale.ENGLISH, "%.4f", amount), selectedAccountAsset.symbol, backupAssetAmount + backupAssets.symbol + " + ", loyaltyAmount + loyaltyAsset.symbol
                            , String.format(Locale.ENGLISH, "%.4f", total), selectedAccountAsset.symbol));
                }
                count++;
            } else if (loyaltyAsset != null && exchangeRate != null) {
                Double totalAmount = Double.parseDouble(selectedAmount) + (Double.parseDouble(loyaltyAmount) * exchangeRate);
                tvTotalStatus.setText(String.format(getString(R.string.str_total_status), selectedAmount, selectedAccountAsset.symbol, loyaltyAmount, loyaltyAsset.symbol, String.format(Locale.ENGLISH, "%.4f", totalAmount), selectedAccountAsset.symbol));
            } else if (backupAssets != null && backAssetRate != null) {
                Double totalAmount = Double.parseDouble(selectedAmount) + (Double.parseDouble(backupAssetAmount) * backAssetRate);
                tvTotalStatus.setText(String.format(getString(R.string.str_total_status),
                        selectedAmount, selectedAccountAsset.symbol, backupAssetAmount,
                        backupAssets.symbol, String.format(Locale.ENGLISH, "%.4f", totalAmount), selectedAccountAsset.symbol));
            }
            tvTotalStatus.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.unable_to_process + " : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void getTrxBlock(String id) {
        if (application.webSocketG.isOpen()) {
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
            application.webSocketG.send(params);
        }
    }

    @Override
    public void relativeHistoryCallback(JSONObject msg) {
        try {
            JSONArray jsonArray = (JSONArray) msg.get("result");
            boolean found = false;
            if (msg.get("id").equals(160)) {
                for (int i = 0; i < 2; i++) {
                    if (!found) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        JSONArray opArray = (JSONArray) jsonObject.get("op");
                        JSONObject operation = (JSONObject) opArray.get(1);
                        if (operation.get("to").toString().equals(receiverID)) {
                            found = true;
                            ServiceGenerator sg = new ServiceGenerator(callbackURL);
                            IWebService service = sg.getService(IWebService.class);
                            final Call<Void> postingService = service.sendCallback(callbackURL, jsonObject.get("block_num").toString(), jsonObject.get("trx_in_block").toString());
                            postingService.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Response<Void> response) {
                                    if (response.isSuccess()) {

                                    } else {
//                            Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onFailure(Throwable t) {
//                if (progressDialog.isShowing())
//                    progressDialog.dismiss();
                                }
                            });
                            break;
                        }
                    }
                }
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
        HashMap hm = new HashMap();
        hm.put("method", "trade");
        hm.put("wifkey", privateKey);
        hm.put("account", spinnerFrom.getSelectedItem().toString());
        hm.put("sell_amount", sellAmount.toString());
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
                    Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
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
                if (Application.webSocketG != null) {
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

    @OnClick(R.id.contactActivity)
    void OnClickContactBtn(View view) {
        List<String> contactlist = new ArrayList<String>();

        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        for (int i = 0; i < contacts.size(); i++) {
            contactlist.add(contacts.get(i).GetAccount() + "::" + Integer.toString(i));
        }

        if (!contactlist.isEmpty()) {
            popUpwindow p = new popUpwindow(this, etReceiverAccount, contactlist);
            p.show(view);
        } else Toast.makeText(context, R.string.empty_list, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void showDialogPin(final Boolean fundTransfer) {
        if (Helper.containKeySharePref(getApplicationContext(), getApplicationContext().getString(R.string.txt_pin))) {
            final Dialog dialog = new Dialog(SendScreen.this);
            //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
            dialog.setTitle(R.string.pin_verification);
            dialog.setContentView(R.layout.activity_alert_pin_dialog);
            Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
            final EditText etPin = (EditText) dialog.findViewById(R.id.etPin);
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String savedPIN = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.txt_pin));
                    if (etPin.getText().toString().equals(savedPIN)) {
                        dialog.cancel();
                        if (fundTransfer) {
                            String transferFunds = getString(R.string.transfer_funds) + "...";
                            showDialog("", transferFunds);
                            tradeAsset();
                        } else {
                            sendFunds(false);
                        }


                    } else {
                        // Toast.makeText(getApplicationContext(), "Wrong PIN", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.setCancelable(true);

            dialog.show();
        }
    }

}