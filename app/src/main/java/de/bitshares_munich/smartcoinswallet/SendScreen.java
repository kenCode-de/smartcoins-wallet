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
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.common.primitives.UnsignedLong;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.security.SecureRandom;
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
import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.interfaces.IExchangeRate;
import de.bitshares_munich.interfaces.IRelativeHistory;
import de.bitshares_munich.interfaces.ContactSelectionListener;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.BlockData;
import de.bitsharesmunich.graphenej.Invoice;
import de.bitsharesmunich.graphenej.LineItem;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.TransferTransactionBuilder;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.api.GetAccountByName;
import de.bitsharesmunich.graphenej.api.TransactionBroadcastSequence;
import de.bitsharesmunich.graphenej.crypto.SecureRandomGenerator;
import de.bitsharesmunich.graphenej.errors.MalformedTransactionException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.HistoricalTransfer;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.objects.Memo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/6/16.
 */
public class SendScreen extends BaseActivity implements IExchangeRate, ContactSelectionListener {
    private static final String TAG = "SendScreen";

    private final Asset CORE_ASSET = new Asset("1.3.0");
    private final Asset FEE_ASSET = CORE_ASSET;

    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;
    AccountAssets selectedAccountAsset;
    AccountAssets loyaltyAsset;
    AccountAssets backupAssets;

//    boolean validReceiver = false;
    boolean validAmount = false;
    boolean sendBtnPressed = false;
    boolean alwaysDonate = false;
    boolean validating = false;

    ProgressDialog progressDialog;
    Double exchangeRate, requiredAmount, backAssetRate, sellAmount;
    String backupAsset, callbackURL;

    private boolean mSendAttemptFail = false;

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
    Button btnSend;

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

    /**
     * Handler and delay
     */
    private int REQUEST_TRANSFER_HISTORY_DELAY = 1800;

    /**
     * Instance of the database interface
     */
    private SCWallDatabase database;

    /* Donation account and amount */
    private UserAccount bitsharesMunich = new UserAccount("1.2.90200");
    private AssetAmount donationAmount = new AssetAmount(UnsignedLong.valueOf(200000), new Asset("1.3.0"));

    /* Destination account */
    private UserAccount destinationAccount;

    /* Sender account */
    private UserAccount sourceAccount;

    /* Constant used to fix the number of historical transfers to fetch from the network in one batch */
    private int HISTORICAL_TRANSFER_BATCH_SIZE = 50;

    /* Websocket threads */
    private WebsocketWorkerThread transferHistoryThread;
    private WebsocketWorkerThread donationBroadcaster;
    private WebsocketWorkerThread transferBroadcaster;
    private WebsocketWorkerThread getAccountByName;

    /* This is one of the of the recipient account's public key, it will be used for memo encoding */
    private PublicKey destinationPublicKey;

    /**
     * Callback fired when we get a response from the network with the transaction details.
     */
    private WitnessResponseListener mTransferHistoryListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG, "mTransferHistoryListener.onSuccess");
            List<HistoricalTransfer> transferList = (List<HistoricalTransfer>) response.result;
            Log.d(TAG, String.format("Got %d new transactions!", transferList.size()));
            ArrayList<HistoricalTransferEntry> transferEntries = new ArrayList<>();
            for(HistoricalTransfer historicalTransfer : transferList){
                HistoricalTransferEntry entry = new HistoricalTransferEntry();
                entry.setHistoricalTransfer(historicalTransfer);

                UserAccount from = database.fillUserDetails(historicalTransfer.getOperation().getFrom());
                UserAccount to = database.fillUserDetails(historicalTransfer.getOperation().getTo());
                String id = historicalTransfer.getId();
                long value = historicalTransfer.getOperation().getTransferAmount().getAmount().longValue();
                Log.d(TAG,String.format("transferred %d from %s -> %s, id: %s", value, from.getAccountName(), to.getAccountName(), id));
            }
            database.putTransactions(transferEntries);
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "mTransferHistoryListener.onError. Msg: "+error.message);
        }
    };

    /**
     * Callback fired when we get a response from the transaction broadcast
     * sequence for the donation button.
     */
    private WitnessResponseListener donationTransactionListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG, "donation.onSuccess");
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "donation.onError. Msg: "+error.message);
        }
    };

    /**
     * Callback that is fired when we get a response from a transaction broadcast sequence.
     */
    private WitnessResponseListener broadcastTransactionListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG, "send.onSuccess");
            mSendAttemptFail = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideDialog();
                    etAmount.setText("");
                    Toast.makeText(SendScreen.this, getResources().getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "send.onError. msg: "+error.message);
            //Try to pay free with asset instead of BTS (second try)
            if(mSendAttemptFail){
                Log.d(TAG, "Second send attempt using current asset");
                sendFunds(false);
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_transfer_funds), Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    };

    webSocketCallHelper myWebSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);

        setBackButton(true);

        setTitle(getResources().getString(R.string.send_screen_name));

        myWebSocketHelper = new webSocketCallHelper(getApplicationContext());

        ButterKnife.bind(this);
        Application.registerExchangeRateCallback(this);

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));

        updateBlockNumberHead();

        tinyDB = new TinyDB(this);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        cbAlwaysDonate.setText(getString(R.string.checkbox_donate) + " BitShares Munich");

        database = new SCWallDatabase(this);
    }

    void init() {
        setCheckboxAvailabilty();
        setSpinner();
    }

    private void startupTasks() {
        runningSpinerForFirstTime = true;
        init();
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null) {
            if (res.containsKey("sResult") && res.containsKey("id")) {
                if (res.getInt("id") == 5) {
                    decodeInvoiceData(res.getString("sResult"));
                }
            }
        }

        String basset = etBackupAsset.getText().toString();

        if (!basset.isEmpty()) {
            backupAssetCHanged(basset);
        }

        loadWebView(webviewTo, 39, Helper.hash("", Helper.SHA256));
    }

    @OnTextChanged(R.id.etReceiverAccount)
    void onTextChangedTo(CharSequence text) {
        tvErrorRecieverAccount.setText("");

        if (text != null) {
            if (!text.toString().equals(text.toString().trim())) {
                etReceiverAccount.setText(text.toString().trim());
            }
        }

        if (spinnerFrom.getSelectedItem().toString().equals(etReceiverAccount.getText().toString())) {
            Toast.makeText(this, R.string.warning_msg_same_account, Toast.LENGTH_SHORT).show();
        }

        if (etReceiverAccount.getText().length() > 0) {
            validating = true;
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
        tvErrorRecieverAccount.setVisibility(View.GONE);
        loadWebView(webviewTo, 34, Helper.hash(etReceiverAccount.getText().toString(), Helper.SHA256));
    }

    @OnFocusChange(R.id.etReceiverAccount)
    public void onFocusChange(boolean hasFocus) {

        if (!hasFocus) {
            validating = true;
            tvErrorRecieverAccount.setText("");
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
    }
    @OnTextChanged(R.id.etAmount)
    void onAmountChanged(CharSequence text) {
        onTransferAmountUpdated();
    }

    Boolean runningSpinerForFirstTime = true;

    @OnItemSelected(R.id.spinnerFrom)
    void onItemSelected(int position) {
        Log.d(TAG,"onItemSelected. position: "+position);
        if (!runningSpinerForFirstTime) {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();

            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    accountDetail.isSelected = true;
                } else {
                    accountDetail.isSelected = false;
                }
            }
//            startupTasks();
        } else {
            this.runningSpinerForFirstTime = false;
        }

        loadWebView(webviewFrom, 34, Helper.hash(spinnerFrom.getSelectedItem().toString(), Helper.SHA256));
    }

    @OnItemSelected(R.id.spAssets)
    void onAssetsSelected(int position) {
        if (loyaltyAsset == null) {
            onTransferAmountUpdated();
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

        try {

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

            setHyperlinkText(tvLoyaltyStatus, loyaltyBalance.toString(), etLoyalty, 0, loyaltyAsset.symbol, Color.BLACK);

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
        } catch (Exception e) {
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refresh your views here
        super.onConfigurationChanged(newConfig);
    }

    private void backupAssetCHanged(String text) {
        try {
            count = 1;
            if (text.toString().equals("")) {
                text = "0";
            } else if (text.toString().equals(".")) {
                text = "0.";
            }

            Double backupAssetAmount = Double.parseDouble(text.toString());
            Double backupAssetBalance = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));

            String backupAssetSym;
            AssetsSymbols assetsSymbols = new AssetsSymbols(this);
            backupAssetSym = assetsSymbols.updateString(backupAssets.symbol);

            if (backupAssetAmount > backupAssetBalance) {
                tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_warning_only_available), backupAssetBalance.toString(), backupAssetSym));
            } else {
                String remainingBalance = String.format(Locale.ENGLISH, "%.5f", (backupAssetBalance - backupAssetAmount));
                tvBackupAssetBalanceValidate.setText(String.format(getString(R.string.str_balance_available), remainingBalance, backupAssetSym));
            }

            setHyperlinkText(tvBackupAssetBalanceValidate, backupAssetBalance.toString(), etBackupAsset, 0, backupAssetSym, Color.BLACK);

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
        } catch (Exception e) {
        }
    }

    @OnTextChanged(R.id.etBackupAsset)
    void onBackupAssetChanged(CharSequence text) {
        backupAssetCHanged(text.toString());
    }


    @OnClick(R.id.btnSend)
    public void onSendButtonClicked(View view) {
        sendBtnPressed = true;
        if (destinationAccount != null) {
            validatingComplete();
        } else {
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

    public void sendFunds(boolean isTrade) {
        Log.d(TAG, "sendFunds: "+isTrade);
        String transferFunds = this.getString(R.string.transfer_funds) + "...";
        showDialog("", transferFunds);
        if (isTrade) {
            Double tradeAmount = Double.parseDouble(etBackupAsset.getText().toString()) * backAssetRate;
            if (!etAmount.getText().toString().equals("") && Double.parseDouble(etAmount.getText().toString()) != 0) {
                tradeAmount += Double.parseDouble(etAmount.getText().toString());
            }
            String mainAsset = spAssets.getSelectedItem().toString();
            mainAsset = mainAsset.replace("bit", "");
            transferAmount(String.format(Locale.ENGLISH, "%." + selectedAccountAsset.precision + "f", tradeAmount), mainAsset, etReceiverAccount.getText().toString());
        } else if (!etAmount.getText().toString().equals("") && Double.parseDouble(etAmount.getText().toString()) != 0) {
            String mainAsset = spAssets.getSelectedItem().toString();
            mainAsset = mainAsset.replace("bit", "");
            String mainAmount;
            if (selectedAccountAsset != null && selectedAccountAsset.precision != null && !selectedAccountAsset.precision.isEmpty()) {
                mainAmount = String.format(Locale.ENGLISH, "%." + selectedAccountAsset.precision + "f", Double.parseDouble(etAmount.getText().toString()));
            } else {
                mainAmount = String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(etAmount.getText().toString()));
            }
            transferAmount(mainAmount, mainAsset, etReceiverAccount.getText().toString());
        }
        if (!etLoyalty.getText().toString().equals("") && Double.parseDouble(etLoyalty.getText().toString()) != 0) {
            String loyaltyAmount;
            String loyaltyAsset = tvLoyalty.getText().toString();
            if (loyaltyAsset.equalsIgnoreCase("YASHA")) {
                loyaltyAmount = String.format(Locale.ENGLISH, "%.3f", Double.parseDouble(etLoyalty.getText().toString()));
            } else {
                loyaltyAmount = String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(etLoyalty.getText().toString()));
            }

            transferAmount(loyaltyAmount, loyaltyAsset, etReceiverAccount.getText().toString());
        }
    }

    private void selectedAccountAsset() {
        try {
            String selectedAsset = spAssets.getSelectedItem().toString();
            selectedAsset = selectedAsset.replace("bit", "");
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

    public void onTransferAmountUpdated() {
        Log.d(TAG,"onTransferAmountUpdated");
        try {
            tvAmountStatus.setTextColor(tvTotalStatus.getTextColors());
            String selectedAsset;
            if (spAssets.getChildCount() > 0) {
                selectedAsset = spAssets.getSelectedItem().toString();
            } else selectedAsset = "";
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
                        validAmount = false;
                        tvAmountStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        tvAmountStatus.setText(String.format(getString(R.string.str_warning_only_available), selectedBalance.toString(), selectedAsset));
                    } else {
                        validAmount = true;

                        remainingBalance = String.format(Locale.ENGLISH, "%.4f", (selectedBalance - enteredAmount));
                        tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, selectedAsset));
                        setHyperlinkText(tvAmountStatus, availableBalance, etAmount, 0, selectedAsset, Color.BLACK); //shayan
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
                setHyperlinkText(tvAmountStatus, availableBalance, etAmount, 0, selectedAsset, Color.BLACK); //shayan
            }

            //Crashing
            setHyperlinkText(tvAmountStatus, availableBalance, etAmount, 14, selectedAsset, Color.RED);
            tvAmountStatus.setTextColor(Color.RED); //shayan
            updateTotalStatus();
        } catch (Exception e) {
            Log.e(TAG,"Exception. Msg: "+e.getMessage());
        }

        // Enabling or disabling the send button depending on
        // the result of the validation.
        if(validateSend()){
            btnSend.setEnabled(true);
        }else{
            btnSend.setEnabled(false);
        }

    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
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

    private void lookupAccounts() {
        this.getAccountByName = new WebsocketWorkerThread(new GetAccountByName(etReceiverAccount.getText().toString(), mAccountByNameListener));
        this.getAccountByName.start();
        validating = true;
    }

    /**
     * Callback that obtains the response from the get_account_by_name API call.
     * Here we're just interested in get one of the public keys from the recipient account
     * in order to use it for memo encryption.
     */
    private WitnessResponseListener mAccountByNameListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            Log.d(TAG,"mAccountByNameListener. onSuccess");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AccountProperties accountProperties = ((WitnessResponse<AccountProperties>) response).result;
                    if(accountProperties != null){
                        Log.d(TAG,"Found account with name");
                        Log.d(TAG,accountProperties.name);
                        destinationPublicKey = accountProperties.active.getKeyAuths().keySet().iterator().next();
                        destinationAccount = new UserAccount(accountProperties.id);
                    }else{
                        Log.i(TAG, "No account with that name");
                        destinationPublicKey = null;
                        destinationAccount = null;
                    }

                    validating = false;

                    // Check whether we can enable the "send" button or not
                    if(validateSend()){
                        btnSend.setEnabled(true);
                    }else{
                        btnSend.setEnabled(false);
                    }
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.d(TAG,"mAccountByNameListener.onError. Msg: "+error.message);
        }
    };

    public void createBitShareAN(boolean focused) {
        if (!focused) {
            if (etReceiverAccount.getText().length() > 2) {
                tvErrorRecieverAccount.setText("");
                tvErrorRecieverAccount.setVisibility(View.GONE);
                lookupAccounts();
            } else {
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

    void setBackUpAsset() {
        try {
//            backupAsset = Helper.fetchStringSharePref(this, getString(R.string.pref_backup_symbol));
            backupAsset = "";
            if (backupAsset.isEmpty()) {
                llBackupAsset.setVisibility(View.GONE);
            } else {
                llBackupAsset.setVisibility(View.VISIBLE);
            }

            String sym;
            AssetsSymbols assetsSymbols = new AssetsSymbols(this);
            sym = assetsSymbols.updateString(backupAsset);

            tvBackupAsset.setText(sym);
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
                    try {
                        for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                            AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                            if (tempAccountAsset.symbol.toLowerCase().equals(backupAsset.toLowerCase())) {
                                backupAssets = accountDetail.AccountAssets.get(j);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        backupAssets = null;
                    }
                }
            }
        } catch (Exception e) {
            Log.d("getBackupAsset", e.getMessage());
        }
    }

    @OnClick(R.id.scanning)
    void OnScanning() {
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("id", 0);
        startActivityForResult(intent, 90);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult");
        switch (requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    decodeInvoiceData(res.getString("sResult"));
                }
                break;
        }
    }

    /**
     * Setups the correct fields with invoice data obtained from the QR-Code reader.
     * @param invoice: Invoice data read from the QR-Code in the JSON format.
     */
    void onScanResult(Invoice invoice) {
        try {
            callbackURL = invoice.getCallback();
            if(!callbackURL.equals("") && !callbackURL.endsWith("/")){
                callbackURL = callbackURL + "/";
            }
            etReceiverAccount.setText(invoice.getTo());
            spAssets.setSelection(getSpinnerIndex(spAssets, invoice.getCurrency()));
            if (invoice.getMemo() == null) {
                llMemo.setVisibility(View.GONE);
            } else {
                etMemo.setText(invoice.getMemo());
                llMemo.setVisibility(View.VISIBLE);
            }

            double totalAmount = 0.0;
            for (LineItem item : invoice.getLineItems()) {
                totalAmount += item.getQuantity() * item.getPrice();
            }
            requiredAmount = totalAmount;
            DecimalFormat df = new DecimalFormat("####.####");
            df.setRoundingMode(RoundingMode.CEILING);
            df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
            if (totalAmount != 0) {
                etAmount.setText(df.format(totalAmount));
//                etAmount.setEnabled(false);
            }
            String loyaltypoints = null;
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
                    setHyperlinkText(tvLoyaltyStatus, loyaltyBalance.toString(), etLoyalty, 0, loyaltyAsset.symbol, Color.BLACK);
                    llLoyalty.setVisibility(View.VISIBLE);
                    tvLoyaltyStatus.setVisibility(View.VISIBLE);
                }
            } else {
                llLoyalty.setVisibility(View.GONE);
                tvLoyaltyStatus.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG,"Exception. Msg: "+e.getMessage());
        }
    }

    public void createSpinner(List<String> spinnerArray, Spinner spinner) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_black, spinnerArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void populateAccountsSpinner() {
        List<String> spinnerArray = new ArrayList<String>();
        String accountname = "";
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            tvFrom.setText(accountDetail.account_name);
            if (accountDetail.isSelected)
                accountname = accountDetail.account_name;

            spinnerArray.add(accountDetail.account_name);
        }

        if (accountDetails.size() > 1) {
            spinnerFrom.setVisibility(View.VISIBLE);
            tvFrom.setVisibility(View.GONE);
        } else {
            loadWebView(webviewFrom, 34, Helper.hash(tvFrom.getText().toString(), Helper.SHA256));
            spinnerFrom.setVisibility(View.GONE);
            tvFrom.setVisibility(View.VISIBLE);
        }

        createSpinner(spinnerArray, spinnerFrom);

        if (accountname.isEmpty()) {
            spinnerFrom.setSelection(0);
        } else {
            spinnerFrom.setSelection(spinnerArray.indexOf(accountname));
        }
    }

    public void populateAssetsSpinner() {
        try {
            String selectedAccount = spinnerFrom.getSelectedItem().toString();
            ArrayList<String> spinnerArray = new ArrayList<String>();
            for (int i = 0; i < accountDetails.size(); i++) {
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)) {
                    try {
                        for (int j = 0; j < accountDetail.AccountAssets.size(); j++) {
                            selectedAccountAsset = accountDetail.AccountAssets.get(j);
                            spinnerArray.add(selectedAccountAsset.symbol);
                        }
                    } catch (Exception r) {
                        selectedAccountAsset = null;
                    }
                }
            }

            AssetsSymbols assetsSymbols = new AssetsSymbols(getApplicationContext());
            spinnerArray = assetsSymbols.updatedList(spinnerArray);
            createSpinner(spinnerArray, spAssets);
        } catch (Exception e) {
        }
    }

    void setSpinner() {
        Log.d(TAG,"setSpinner");
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
        try {
            String amount = etAmount.getText().toString();

            if (amount.isEmpty()) {
                return true;
            }

            Double am = Double.parseDouble(amount);

            if (am <= 0.0) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return true;
        }
    }


    public boolean validateSend() {
        if (spinnerFrom.getSelectedItem().toString().equals("")) {
            return false;
        } else if (spinnerFrom.getSelectedItem().toString().equals(etReceiverAccount.getText().toString())) {
            return false;
        } else if (!validAmount) {
            return false;
        } else if (etAmount.getText().toString().equals(".")) {
            return false;
        } else if (checkLastIndex()) {
            return false;
        } else if (checkIfZero()) {
            return false;
        } else if(destinationPublicKey == null){
            return false;
        }
        return true;
    }

    public void transferAmount(String amount, String symbol, String toAccount) {
        String senderID = null;
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        String wifKey = "";
        for (int i = 0; i < accountDetails.size(); i++) {
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)) {
                senderID = accountDetail.account_id;
                try {
                    wifKey = Crypt.getInstance().decrypt_string(accountDetail.wif_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        String memoMessage = null;
        if (!etMemo.getText().toString().isEmpty()){
            memoMessage = etMemo.getText().toString();
        }else{
            memoMessage = null;
        }

        try{
            long baseAmount = (long) (Double.valueOf(amount) * (long) Math.pow(10, Long.valueOf(selectedAccountAsset.precision)));
            String assetId = selectedAccountAsset.id;
            Asset transferAsset = new Asset(assetId);
            long expirationTime = Application.blockTime + 30;
            ECKey currentPrivKey = ECKey.fromPrivate(DumpedPrivateKey.fromBase58(null, wifKey).getKey().getPrivKeyBytes());

            sourceAccount = new UserAccount(senderID);


            if(mSendAttemptFail){
                mSendAttemptFail = false;
                TransferTransactionBuilder builder = new TransferTransactionBuilder()
                        .setSource(sourceAccount)
                        .setDestination(destinationAccount)
                        .setAmount(new AssetAmount(UnsignedLong.valueOf(baseAmount), transferAsset))
                        .setBlockData(new BlockData(Application.refBlockNum, Application.refBlockPrefix, expirationTime))
                        .setPrivateKey(currentPrivKey);
                if(memoMessage != null) {
                    SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
                    long nonce = secureRandom.nextLong();
                    byte[] encryptedMemo = Memo.encryptMessage(currentPrivKey, destinationPublicKey, nonce, memoMessage);
                    Address from = new Address(ECKey.fromPublicOnly(currentPrivKey.getPubKey()));
                    Address to = new Address(destinationPublicKey.getKey());
                    Memo memo = new Memo(from, to, nonce, encryptedMemo);
                    builder.setMemo(memo);
                }

                Transaction transaction = builder.build();

                transferBroadcaster = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, transferAsset, broadcastTransactionListener));
                transferBroadcaster.start();

                if (alwaysDonate || cbAlwaysDonate.isChecked()) {
                    TransferTransactionBuilder donationBuilder = new TransferTransactionBuilder()
                            .setSource(new UserAccount(senderID))
                            .setDestination(bitsharesMunich)
                            .setAmount(donationAmount)
                            .setFee(new AssetAmount(UnsignedLong.valueOf(264174), transferAsset))
                            .setBlockData(new BlockData(Application.refBlockNum, Application.refBlockPrefix, expirationTime))
                            .setPrivateKey(currentPrivKey);

                    Transaction donationTransaction = donationBuilder.build();
                    donationBroadcaster = new WebsocketWorkerThread(new TransactionBroadcastSequence(donationTransaction, transferAsset, donationTransactionListener));
                    donationBroadcaster.start();
                    Log.d(TAG, "started a donation message broadcast");
                }
            }
            else{
                mSendAttemptFail = true;
                TransferTransactionBuilder builder = new TransferTransactionBuilder()
                        .setSource(sourceAccount)
                        .setDestination(destinationAccount)
                        .setAmount(new AssetAmount(UnsignedLong.valueOf(baseAmount), transferAsset))
                        .setFee(new AssetAmount(UnsignedLong.valueOf(264174), FEE_ASSET))
                        .setBlockData(new BlockData(Application.refBlockNum, Application.refBlockPrefix, expirationTime))
                        .setPrivateKey(currentPrivKey);
                if(memoMessage != null) {
                    SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
                    long nonce = secureRandom.nextLong();
                    byte[] encryptedMemo = Memo.encryptMessage(currentPrivKey, destinationPublicKey, nonce, memoMessage);
                    Address from = new Address(ECKey.fromPublicOnly(currentPrivKey.getPubKey()));
                    Address to = new Address(destinationPublicKey.getKey());
                    Memo memo = new Memo(from, to, nonce, encryptedMemo);
                    builder.setMemo(memo);
                }
                Transaction transaction = builder.build();

                transferBroadcaster = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, FEE_ASSET, broadcastTransactionListener));
                transferBroadcaster.start();
                Log.d(TAG, "started a funds transfer donation broadcast");

                if (alwaysDonate || cbAlwaysDonate.isChecked()) {
                    TransferTransactionBuilder donationBuilder = new TransferTransactionBuilder()
                            .setSource(new UserAccount(senderID))
                            .setDestination(bitsharesMunich)
                            .setAmount(donationAmount)
                            .setFee(new AssetAmount(UnsignedLong.valueOf(264174), FEE_ASSET))
                            .setBlockData(new BlockData(Application.refBlockNum, Application.refBlockPrefix, expirationTime))
                            .setPrivateKey(currentPrivKey);

                    Transaction donationTransaction = donationBuilder.build();
                    donationBroadcaster = new WebsocketWorkerThread(new TransactionBroadcastSequence(donationTransaction, FEE_ASSET, donationTransactionListener));
                    donationBroadcaster.start();
                    Log.d(TAG, "started a donation message broadcast");
                }
            }

        } catch (MalformedTransactionException e) {
            hideDialog();
            e.printStackTrace();
            Log.e(TAG, "MalformedTransactionException. Msg: " + e.getMessage());
        }
    }

    private int getSpinnerIndex(Spinner spinner, String assetSymbol) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            String spString = spinner.getItemAtPosition(i).toString();
            spString = spString.replace("bit", "");
            if (spString.equalsIgnoreCase(assetSymbol)) {
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
            if(backupAssets!=null){
            Double baseWithPrecision = Double.parseDouble(base_amount) / Math.pow(10, Double.parseDouble(backupAssets.precision));
            if (id == 200) {
                Double quoteWithPrecision = Double.parseDouble(quote_amount) / Math.pow(10, Double.parseDouble(selectedAccountAsset.precision));
                backAssetRate = quoteWithPrecision / baseWithPrecision;
            } else if (id == 100) {
                Double quoteWithPrecision = Double.parseDouble(quote_amount) / Math.pow(10, Double.parseDouble(loyaltyAsset.precision));
                exchangeRate = quoteWithPrecision / baseWithPrecision;
            }}
            runOnUiThread(new Runnable() {
                public void run() {
                    updateTotalStatus();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SendScreen.this, R.string.str_trading_pair_not_exist, Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private void showDialog(String title, String msg)
    {
        try
        {
            if (progressDialog != null) {
                if (!progressDialog.isShowing()) {
                    progressDialog.setTitle(title);
                    progressDialog.setMessage(msg);
                    progressDialog.show();
                }
            }
        }
        catch (Exception e)
        {

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
            Double backupAssetsBalance = 0.0;
            if (backupAssets != null) {
                backupAssetsBalance = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));
            }
            String selectedAmount = Helper.padString(etAmount.getText().toString());
            Double enteredAmount = Double.parseDouble(selectedAmount);
            String loyaltyAmount = Helper.padString(etLoyalty.getText().toString());
            String backupAssetAmount = Helper.padString(etBackupAsset.getText().toString());
            if ((enteredAmount > selectedBalance) | (enteredAmount < 0)) {
                selectedAmount = String.valueOf(selectedBalance);
                if ((backupAssetsBalance) - (Double.parseDouble(String.valueOf(enteredAmount - selectedBalance)) / backAssetRate) > 0) {
                    backupAssetAmount = String.format(Locale.ENGLISH, "%.4f", ((enteredAmount - selectedBalance) / backAssetRate));

                }
            }
            // Send screen work end

            String selectedAccountAssetSym;
            AssetsSymbols assetsSymbols = new AssetsSymbols(this);
            selectedAccountAssetSym = assetsSymbols.updateString(selectedAccountAsset.symbol);


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

            } else if (backupAssets != null && backAssetRate != null) {
                Double totalAmount = Double.parseDouble(selectedAmount) + (Double.parseDouble(backupAssetAmount) * backAssetRate);
                String backupAssetSym;
                backupAssetSym = assetsSymbols.updateString(backupAssets.symbol);
                tvTotalStatus.setText(String.format(getString(R.string.str_total_status),
                        selectedAmount, selectedAccountAssetSym, backupAssetAmount,
                        backupAssetSym, String.format(Locale.ENGLISH, "%.4f", totalAmount), selectedAccountAssetSym));

                setHyperlinkTextDouble(tvTotalStatus, selectedAmount.toString(), backupAssetAmount.toString(), etAmount, etBackupAsset, 1, selectedAccountAssetSym, backupAssetSym);

            }

            tvTotalStatus.setVisibility(View.VISIBLE);
            if (backupAssets == null) {
                tvTotalStatus.setVisibility(View.GONE);
            }
            //shayan Friday night
            if (!etBackupAsset.getText().toString().isEmpty()) {
                Double backupEnteredAmount = Double.parseDouble(etBackupAsset.getText().toString());
                Double backAssAmount = Double.parseDouble(backupAssets.ammount) / Math.pow(10, Integer.parseInt(backupAssets.precision));
                Double backupAvailableAmount = backAssAmount;
                if (backupEnteredAmount > backupAvailableAmount)
                    validAmount = false;
            }
            // shayan friday night

        } catch (Exception e) {
        }
    }

    private void findExchangeRate(int id) {
        if (Application.isReady) {
            String loyalOrBackupAssets = "";
            if (id == 200) {
                loyalOrBackupAssets = backupAssets.id;
            } else if (id == 100) {
                loyalOrBackupAssets = loyaltyAsset.id;
            }

            String params = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
            String params2 = ",\"get_limit_orders\",[\"" + loyalOrBackupAssets + "\",\"" + selectedAccountAsset.id + "\",1]]}";
            myWebSocketHelper.make_websocket_call(params, params2, webSocketCallHelper.api_identifier.database);
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
        Double buyAmount;
        if (backAssetRate != null) {
            buyAmount = sellAmount * backAssetRate;
        } else {
            buyAmount = sellAmount;
        }
        HashMap<String, String> hm = new HashMap<>();
        hm.put("method", "trade");
        hm.put("wifkey", privateKey);
        hm.put("account", spinnerFrom.getSelectedItem().toString());
        hm.put("sell_amount", String.format(Locale.ENGLISH, "%.4f", sellAmount)); //shayan
        hm.put("sell_symbol", backupAsset);
        hm.put("buy_amount", String.format(Locale.ENGLISH, "%.4f", buyAmount));
        hm.put("buy_symbol", selectedAccountAsset.symbol);
        //Toast.makeText(context, R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        //TODO evaluate removal
        /*ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
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
                        hideDialog();
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
        });*/
    }

    private void decodeInvoiceData(String encoded) {
        Invoice invoice = Invoice.fromQrCode(encoded);
        saveMerchantEmail(invoice.toJsonString());
        onScanResult(invoice);
    }

    public void saveMerchantEmail(String string) {
        String accountName = SupportMethods.ParseJsonObject(string, "to");
        String note = SupportMethods.ParseJsonObject(string, "note");
        if (!note.isEmpty())
        {
            String email = SupportMethods.ParseJsonObject(note,"merchant_email");
            if (email.length() > 0)
            {
                if (SupportMethods.isEmailValid(email))
                {
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
                if (Application.isConnected()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
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

    @OnClick(R.id.contactButton)
    void OnClickContactBtn(View view) {

        ArrayList<ContactListAdapter.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ContactListAdapter.ListviewContactItem.class);

        if (contacts.size() > 0) {
            contactListDialog = new Dialog(this);
            contactListDialog.setTitle(getString(R.string.contacts));
            contactListDialog.setContentView(R.layout.contacts_list_send_screen);
            ListView listView = (ListView) contactListDialog.findViewById(R.id.contactsListSendScreen);
            listView.setAdapter(new ContactListDialogAdapter(this, this));
            int childCount = listView.getAdapter().getCount();
            setListViewHeightBasedOnChildren(listView, Math.min(5, childCount));
            contactListDialog.show();
        } else {
            Toast.makeText(this, R.string.empty_list, Toast.LENGTH_SHORT).show();
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView, int childCount) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            int px = listItem.getMeasuredHeight();
            totalHeight += px * 1.2;
        }
        Log.d(TAG, String.format("total height: %d", totalHeight));
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (5 - 1));
        listView.setLayoutParams(params);
    }


    @Override
    public void onResume() {
        super.onResume();
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        startupTasks();
    }

    private void showDialogPin(final Boolean fundTransfer) {
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        final Dialog dialog = new Dialog(SendScreen.this);
        dialog.setTitle(R.string.title_prompt_pin);
        dialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        btnDone.setVisibility(View.VISIBLE);
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
    public void onContactSelected(String contactName) {
        etReceiverAccount.setText(contactName);
        if (contactListDialog != null)
            contactListDialog.dismiss();
        createBitShareAN(false);
    }

    void setHyperlinkText(TextView textView, final String balances, final EditText editText, int UnderlineStartingIndex, String symbol, final int color) {
        String text = textView.getText().toString();
        String available = new String(symbol);

        int index = text.indexOf(available);

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
        ss.setSpan(clickableSpan, UnderlineStartingIndex, index - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setTextColor(Color.BLACK);
    }

    void setHyperlinkTextDouble(TextView textView, final String balances, final String balances2, final EditText editText, final EditText editText2, int UnderlineStartingIndex, String symbol, String symbol2) //shayan
    {
        String text = textView.getText().toString();
        String available = new String(symbol2);

        int index = text.indexOf(available);

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
        ss.setSpan(clickableSpan, UnderlineStartingIndex, index - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setTextColor(Color.BLACK);
    }
}