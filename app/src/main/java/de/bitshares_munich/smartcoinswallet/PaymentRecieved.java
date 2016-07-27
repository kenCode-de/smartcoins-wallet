package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.IAccountObject;
import de.bitshares_munich.Interfaces.IAssetObject;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.DecodeMemo;
import de.bitshares_munich.models.TransferResponse;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */
public class PaymentRecieved extends BaseActivity implements ITransactionObject,IAccountObject,IAssetObject {
    Application application = new Application();
    String receiver_id;
    String sender_id;
    JSONObject amountObj;
    JSONObject feeObj;

    @Bind(R.id.btnOk)
    Button btnOk;
    @Bind(R.id.tvFrom)
    TextView tvFrom;
    @Bind(R.id.tvTo)
    TextView tvTo;
    @Bind(R.id.tvMainAmount)
    TextView tvMainAmount;
    @Bind(R.id.tvMainAsset)
    TextView tvMainAsset;
    @Bind(R.id.tvAmount)
    TextView tvAmount;
    @Bind(R.id.tvFee)
    TextView tvFee;
    @Bind(R.id.tvMemo)
    TextView tvMemo;

    Locale locale;
    String language;

    webSocketCallHelper myWebSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_recieved);
        ButterKnife.bind(this);

        setBackButton(true);
        setTitle(getResources().getString(R.string.payment_rcvd_screen_name));

        myWebSocketHelper = new webSocketCallHelper(getApplicationContext());

        language = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
        locale = new Locale(language);
        playSound();
        application.registerTransactionObject(this);
        application.registerAccountObjectCallback(this);
        application.registerAssetObjectCallback(this);
        String block = getIntent().getStringExtra("block");
        String trx = getIntent().getStringExtra("trx");
        receiver_id = getIntent().getStringExtra("receiver_id");
        sender_id = getIntent().getStringExtra("sender_id");
        getAccountObject();
        getTransactionObject(block,trx);




    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }
    @OnClick(R.id.btnOk)
    void onOkPressed(){
        //Intent intent = new Intent(getApplicationContext(), TabActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
        finish();
    }
    public void getAccountObject()
    {
        String params = "{\"id\":13,\"method\":\"call\",\"params\":[";
        String params2 = ",\"get_objects\",[[\""+sender_id+"\",\""+receiver_id+"\"],0]]}";
        myWebSocketHelper.make_websocket_call(params,params2, webSocketCallHelper.api_identifier.database);

        /*
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":13,\"method\":\"call\",\"params\":["+db_identifier+",\"get_objects\",[[\""+sender_id+"\",\""+receiver_id+"\"],0]]}";
            Application.webSocketG.send(params);
        }
        */
    }
    public void getTransactionObject(String block, String trx)
    {
        String params = "{\"id\":12,\"method\":\"call\",\"params\":[";
        String params2 = ",\"get_transaction\",[\""+block+"\","+trx+"]]}";
        myWebSocketHelper.make_websocket_call(params,params2, webSocketCallHelper.api_identifier.database);
        /*
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":12,\"method\":\"call\",\"params\":["+db_identifier+",\"get_transaction\",[\""+block+"\","+trx+"]]}";
            Application.webSocketG.send(params);
        }
        */
    }
    public void getAssetObject(String amountAsset, String feeAsset)
    {
        String params = "{\"id\":14,\"method\":\"call\",\"params\":[";
        String params2 = ",\"get_objects\",[[\""+amountAsset+"\",\""+feeAsset+"\"],0]]}";
        myWebSocketHelper.make_websocket_call(params,params2, webSocketCallHelper.api_identifier.database);

        /*
        if (Application.webSocketG.isOpen()) {
            int db_identifier = Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.database_indentifier));
            String params = "{\"id\":14,\"method\":\"call\",\"params\":["+db_identifier+",\"get_objects\",[[\""+amountAsset+"\",\""+feeAsset+"\"],0]]}";
            Application.webSocketG.send(params);
        }
        */
    }
    public void playSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.woohoo);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void accountObjectCallback(JSONObject jsonObject){
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONArray resultArr = (JSONArray) jsonObject.get("result");
            for (int i = 0; i < resultArr.length(); i++) {
                final JSONObject resultObj = (JSONObject) resultArr.get(i);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (resultObj.get("id").equals(sender_id)){
                                tvFrom.setText(resultObj.get("name").toString());
                            }else if (resultObj.get("id").equals(receiver_id)){
                                tvTo.setText(resultObj.get("name").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void checkTransactionObject(JSONObject jsonObject){
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONObject result = (JSONObject) jsonObject.get("result");
            JSONArray operations = (JSONArray) result.get("operations");
            JSONArray operationsInner = (JSONArray) operations.get(0);
            JSONObject resultObj = (JSONObject) operationsInner.get(1);
            amountObj = (JSONObject) resultObj.get("amount");
            feeObj = (JSONObject) resultObj.get("fee");
            getAssetObject(amountObj.get("asset_id").toString(),feeObj.get("asset_id").toString());
            if (resultObj.has("memo")){
                decodeMemo(resultObj.get("memo").toString(),resultObj.get("to").toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void decodeMemo(String memo,String accountId) {
        String privateKey = "";
        TinyDB tinyDB = new TinyDB(getApplicationContext());
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        for (int i=0; i<accountDetails.size(); i++){
            AccountDetails accountDetail = accountDetails.get(i);
            if (accountDetail.account_id.equals(accountId)){
                try {
                    privateKey = Crypt.getInstance().decrypt_string(accountDetail.wif_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap hm = new HashMap();
        hm.put("method","decode_memo");
        hm.put("wifkey",privateKey);
        hm.put("memo", memo);
        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<DecodeMemo> postingService = service.getDecodedMemo(hm);
        postingService.enqueue(new Callback<DecodeMemo>() {
            @Override
            public void onResponse(Response<DecodeMemo> response) {
                if (response.isSuccess()) {
                    DecodeMemo resp = response.body();
                    if (resp.status.equals("success")){
                        tvMemo.setText(resp.msg);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public void assetObjectCallback(JSONObject jsonObject){
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONArray resultArr = (JSONArray) jsonObject.get("result");
            for (int i = 0; i < resultArr.length(); i++) {
                final JSONObject resultObj = (JSONObject) resultArr.get(i);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (resultObj.get("id").equals(amountObj.get("asset_id").toString())){
                                Double amount = Double.parseDouble(amountObj.get("amount").toString());
                                amount = amount / Math.pow(10, Integer.parseInt(resultObj.get("precision").toString()));

                                tvMainAmount.setText(Helper.setLocaleNumberFormat(locale, amount));
                                tvMainAsset.setText(resultObj.get("symbol").toString());
                                tvAmount.setText(String.format("%.4f",amount)+resultObj.get("symbol").toString());
                            }
                            if (resultObj.get("id").equals(feeObj.get("asset_id").toString())){
                                Double fee = Double.parseDouble(feeObj.get("amount").toString());
                                fee = fee / Math.pow(10, Integer.parseInt(resultObj.get("precision").toString()));
                                tvFee.setText(String.format("%.4f",fee)+resultObj.get("symbol").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
