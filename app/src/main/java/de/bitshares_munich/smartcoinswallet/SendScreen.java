package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IExchangeRate;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransferResponse;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/6/16.
 */
public class SendScreen extends Activity implements IExchangeRate, IAccount {
    Context context;
    final String always_donate = "always_donate";
    final String backup_asset = "backup_asset";
    Application application = new Application();
    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;
    AccountAssets selectedAccountAsset;
    AccountAssets loyaltyAsset;
    boolean validReceiver = false;
    boolean validAmount = false;
    ProgressDialog progressDialog;


    @Bind(R.id.FirstChild)
    LinearLayout FirstChild;

    @Bind(R.id.SecChild)
    LinearLayout SecChild;

    @Bind(R.id.ThirdChild)
    LinearLayout ThirdChild;

    @Bind(R.id.llMemo)
    LinearLayout llMemo;

    @Bind(R.id.llLoyalty)
    LinearLayout llLoyalty;

    @Bind(R.id.tvLoyaltyStatus)
    TextView tvLoyaltyStatus;

    @Bind(R.id.StatusFourth)
    TextView StatusFourth;

    @Bind(R.id.spAssets)
    Spinner spAssets;

    @Bind(R.id.selectBTSAsset)
    TextView selectBTSAsset;

    @Bind(R.id.tvLoyalty)
    TextView tvLoyalty;

    @Bind(R.id.webviewFrom)
    WebView webviewFrom;

    @Bind(R.id.webviewTo)
    WebView webviewTo;


//    @Bind(R.id.editTextFrom)
//    TextView editTextFrom;

    @Bind(R.id.etReceiverAccount)
    EditText etReceiverAccount;

    @Bind(R.id.tvErrorRecieverAccount)
    TextView tvErrorRecieverAccount;

    @Bind(R.id.tvAmountStatus)
    TextView tvAmountStatus;

    @Bind(R.id.checkbox_donate)
    CheckBox checkbox_donate;

    @Bind(R.id.etMemo)
    EditText etMemo;

    @Bind(R.id.etAmount)
    EditText etAmount;

    @Bind(R.id.editTextAsset)
    EditText editTextAsset;

    @Bind(R.id.spinnerFrom)
    Spinner spinnerFrom;

    @Bind(R.id.btnSend)
    LinearLayout btnSend;

    @Bind(R.id.etLoyalty)
    EditText etLoyalty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);
        context = getApplicationContext();
        ButterKnife.bind(this);
        application.registerExchangeRateCallback(this);
        application.registerCallback(this);

        tinyDB = new TinyDB(context);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        init();
    }
    void init(){
        setCheckboxAvailabilty();
        setBackUpAsset();
        setSpinner();
    }
    void screenTwo(){
        llLoyalty.setVisibility(View.GONE);
        tvLoyaltyStatus.setVisibility(View.GONE);
       // StatusFourth.setVisibility(View.GONE);
    }
    void screenThree(){
        llMemo.setVisibility(View.GONE);
    }
//    @OnTextChanged(R.id.editTextFrom)
//    void onTextChangedFrom(CharSequence text) {
//        if (editTextFrom.getText().length() > 0) {
//            loadWebView(webviewFrom , 34, Helper.md5(editTextFrom.getText().toString()));
//        }
//    }

    @OnTextChanged(R.id.etReceiverAccount)
    void onTextChangedTo(CharSequence text) {
        if (etReceiverAccount.getText().length() > 0) {
            loadWebView(webviewTo , 34, Helper.md5(etReceiverAccount.getText().toString()));
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
    }
    @OnTextChanged(R.id.etAmount)
    void onAmountChanged(CharSequence text) {
        updateAmountStatus();
    }
    @OnItemSelected(R.id.spinnerFrom) void onItemSelected(int position) {
        populateAssetsSpinner();

    }
    @OnItemSelected(R.id.spAssets) void onAssetsSelected(int position) {
        updateAmountStatus();

    }
    @OnTextChanged(R.id.etLoyalty)
    void onLoyaltyChanged(CharSequence text){
        if (text != null & !text.toString().equals("")){
            if (text.toString().equals(".")){
                text = "0.";
            }
            Double loyaltyAmount = Double.parseDouble(text.toString());
            Double loyaltyBalance = Double.parseDouble(loyaltyAsset.ammount) / Math.pow(10, Integer.parseInt(loyaltyAsset.precision));
            if (loyaltyAmount > loyaltyBalance){
                tvLoyaltyStatus.setText(String.format(getString(R.string.str_warning_only_available), loyaltyBalance.toString(), loyaltyAsset.symbol));
            }else{
                String remainingBalance = String.format("%.4f",(loyaltyBalance - loyaltyAmount));
                tvLoyaltyStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, loyaltyAsset.symbol));
            }
//            tvLoyaltyStatus.setText(String.format(getString(R.string.str_balance_available), loyaltyBalance.toString(), loyaltyAsset.symbol));


        }
    }
    @OnClick(R.id.btnSend)
    public void setBtnSend(View view) {
        if (validateSend()){
            progressDialog = new ProgressDialog(this);
            showDialog("", "Transferring Funds...");
            transferAmount();
        }
    }
    public void updateAmountStatus(){
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        String selectedAsset = spAssets.getSelectedItem().toString();
        for (int i=0; i<accountDetails.size(); i++){
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)){
                for (int j=0; j<accountDetail.AccountAssets.size(); j++){
                    AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                    if (tempAccountAsset.symbol.toLowerCase().equals(selectedAsset.toLowerCase())){
                        selectedAccountAsset = accountDetail.AccountAssets.get(j);
                        break;
                    }
                }
            }
        }

        Double selectedBalance = Double.parseDouble(selectedAccountAsset.ammount) / Math.pow(10, Integer.parseInt(selectedAccountAsset.precision));
        if (etAmount.getText().length() > 0) {
            String enteredAmountStr = etAmount.getText().toString();
            if (enteredAmountStr.equals(".")){
                enteredAmountStr = "0.";
            }
            Double enteredAmount = Double.parseDouble(enteredAmountStr);
            if (enteredAmount != 0){
                String remainingBalance = "0";
                if (enteredAmount > selectedBalance){
                    //etAmount.setText(selectedBalance.toString());
                    validAmount = false;
                    tvAmountStatus.setText(String.format(getString(R.string.str_warning_only_available), selectedBalance.toString(), selectedAsset));
                }else{
                    validAmount = true;
                    remainingBalance = String.format("%.4f",(selectedBalance - enteredAmount));
                    tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), remainingBalance, selectedAsset));
                }

            }else{
                tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));
            }
        }else{
            tvAmountStatus.setText(String.format(getString(R.string.str_balance_available), selectedBalance.toString(), selectedAsset));
        }
    }
    private void loadWebView(WebView webView , int size, String encryptText) {
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

    void setCheckboxAvailabilty(){
        if(Helper.fetchBoolianSharePref(this,always_donate)){
            checkbox_donate.setChecked(true);
        }else checkbox_donate.setVisibility(View.GONE);
    }

    void setBackUpAsset(){
        String asset =Helper.fetchStringSharePref(this,backup_asset);
        if(asset!=null) {
            editTextAsset.setText(asset);
        }
    }
    public void popupwindow(View v,TextView textview){
        popUpwindow p =new popUpwindow(context,textview);
        p.show(v);
    }
    @OnClick(R.id.scanning)
    void OnScanning(){
        Intent intent = new Intent(context, qrcodeActivity.class);startActivityForResult(intent,90);
    }
    @OnClick(R.id.selectBTSAsset)
    void onSelectBTSAsset(View v){
        popupwindow(v,selectBTSAsset);
    }
    @OnClick(R.id.imageviewAsset)
    void imageviewAsset(View v){
        popupwindow(v,selectBTSAsset);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    try {
                        onScanResult(res.getString("sResult"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    void onScanResult(String result) throws JSONException {
        JSONObject resJson = new JSONObject(result);
        resJson = new JSONObject(resJson.get("json").toString());
        etReceiverAccount.setText(resJson.get("to").toString());
        validReceiver = true;
        spAssets.setSelection(getSpinnerIndex(spAssets,resJson.get("currency").toString()));
        spAssets.setEnabled(false);
        if(resJson.get("memo")!=null){
            llMemo.setVisibility(View.GONE);
            etMemo.setText(resJson.get("memo").toString());
        }else llMemo.setVisibility(View.VISIBLE);
        JSONArray lineItems = new JSONArray(resJson.get("line_items").toString());
        Double totalAmount = 0.0;
        for (int i=0; i<lineItems.length(); i++){
            JSONObject lineItem = (JSONObject) lineItems.get(i);
            totalAmount += (Double.parseDouble(lineItem.get("quantity").toString()) * Double.parseDouble(lineItem.get("price").toString()));
        }

        etAmount.setText(totalAmount.toString());
        etAmount.setEnabled(false);
//        selectBTSAmount.setText(hash.get("currency"));
        String loyaltypoints = resJson.get("ruia").toString();
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        if(loyaltypoints!=null) {
            for (int i=0; i<accountDetails.size(); i++){
                AccountDetails accountDetail = accountDetails.get(i);
                if (accountDetail.account_name.equals(selectedAccount)){
                    for (int j=0; j<accountDetail.AccountAssets.size(); j++){
                        AccountAssets tempAccountAsset = accountDetail.AccountAssets.get(j);
                        if (tempAccountAsset.id.equals(loyaltypoints)){
                            loyaltyAsset = accountDetail.AccountAssets.get(j);
                            break;
                        }
                    }
                }
            }
            if (loyaltyAsset != null) {
                Double loyaltyBalance = Double.parseDouble(loyaltyAsset.ammount) / Math.pow(10, Integer.parseInt(loyaltyAsset.precision));
                tvLoyalty.setText(loyaltyAsset.symbol);
                tvLoyaltyStatus.setText(String.format(getString(R.string.str_balance_available), loyaltyBalance.toString(), loyaltyAsset.symbol));
                llLoyalty.setVisibility(View.VISIBLE);
                tvLoyaltyStatus.setVisibility(View.VISIBLE);
            }
        }
        else {
            llLoyalty.setVisibility(View.GONE);
            tvLoyaltyStatus.setVisibility(View.GONE);
        }
    }
    public void createSpinner(List<String> spinnerArray, Spinner spinner){
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }
    public void populateAccountsSpinner(){
        List<String> spinnerArray =  new ArrayList<String>();
        for (int i=0; i<accountDetails.size(); i++){
            AccountDetails accountDetail = accountDetails.get(i);
            spinnerArray.add(accountDetail.account_name);
        }
        createSpinner(spinnerArray,spinnerFrom);
    }
    public void populateAssetsSpinner(){
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        List<String> spinnerArray =  new ArrayList<String>();
        for (int i=0; i<accountDetails.size(); i++){
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)){
                for (int j=0; j<accountDetail.AccountAssets.size(); j++){
                    selectedAccountAsset = accountDetail.AccountAssets.get(j);
                    spinnerArray.add(selectedAccountAsset.symbol);
                }
            }
        }
        createSpinner(spinnerArray,spAssets);
    }
    void setSpinner(){
        populateAccountsSpinner();
        populateAssetsSpinner();
    }

    public void get_exchange_rate(){
        if (application.webSocketG.isOpen()) {

            int db_identifier = Helper.fetchIntSharePref(context,context.getString(R.string.database_indentifier));
            String params = "{\"id\":7,\"method\":\"call\",\"params\":["+db_identifier+",\"get_limit_orders\",[\"1.3.121\",\"1.3.120\",1]]}";
            application.webSocketG.send(params);
        }
    }
    public boolean validateSend(){
        if (spinnerFrom.getSelectedItem().toString().equals("")){
            return false;
        }
        else if(!validReceiver){
            Toast.makeText(context, R.string.str_invalid_receiver, Toast.LENGTH_SHORT).show();
            return false;
        }else if (!validAmount){
            Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        }else if(Double.parseDouble(etAmount.getText().toString()) == 0){
            Toast.makeText(context, R.string.str_invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void transferAmount() {
        String selectedAccount = spinnerFrom.getSelectedItem().toString();
        String privateKey = "";
        for (int i=0; i<accountDetails.size(); i++){
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_name.equals(selectedAccount)){
                try {
                    privateKey = Crypt.getInstance().decrypt_string(accountDetail.wif_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap hm = new HashMap();
        hm.put("method","transfer");
        hm.put("wifkey",privateKey);
        hm.put("from_account",spinnerFrom.getSelectedItem().toString());
        hm.put("to_account",etReceiverAccount.getText().toString());
        hm.put("amount",String.format("%.4f",Double.parseDouble(etAmount.getText().toString())));
        hm.put("asset_symbol", spAssets.getSelectedItem().toString());
        hm.put("memo", etMemo.getText().toString());

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.transfer_server_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<TransferResponse> postingService = service.getTransferResponse(hm);
        postingService.enqueue(new Callback<TransferResponse>() {
            @Override
            public void onResponse(Response<TransferResponse> response) {
                if (response.isSuccess()) {
                    TransferResponse resp = response.body();
                    if (resp.status.equals("success")){
                        Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(context, R.string.str_transaction_failed, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
                hideDialog();
            }

            @Override
            public void onFailure(Throwable t) {
//                if (progressDialog.isShowing())
//                    progressDialog.dismiss();
            }
        });
    }
    private int getSpinnerIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }
    @Override
    public void callback_exchange_rate(JSONObject result) throws JSONException {
        if (result.length() > 0) {
            JSONObject sell_price = (JSONObject) result.get("sell_price");
            JSONObject base = (JSONObject) sell_price.get("quote");
            String base_amount = base.get("amount").toString();
            JSONObject quote = (JSONObject) sell_price.get("base");
            String quote_amount = quote.get("amount").toString();
        }else{
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
                }
            }
            if (!found){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validReceiver = false;
                        String acName = getString(R.string.account_name_not_exist);
                        String format = String.format(acName, etReceiverAccount.getText().toString());
                        tvErrorRecieverAccount.setText(format);
                        tvErrorRecieverAccount.setVisibility(View.VISIBLE);
                    }
                });
            }
        } catch (Exception e) {

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
}