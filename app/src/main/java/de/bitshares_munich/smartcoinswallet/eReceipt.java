package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;

import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.models.EquivalentComponentResponse;
import de.bitshares_munich.models.EquivalentFiatStorage;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.models.TransactionIdResponse;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/26/16.
 */

public class eReceipt extends BaseActivity implements IBalancesDelegate {
    Context context;
    Application application = new Application();

    @Bind(R.id.ivOtherGravatar)
    ImageView ivOtherGravatar;

    @Bind(R.id.TvBlockNum)
    TextView TvBlockNum;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvOtherName)
    TextView tvOtherName;

    @Bind(R.id.tvUserName)
    TextView tvUserName;

    @Bind(R.id.tvOtherId)
    TextView tvOtherId;

    @Bind(R.id.tvUserId)
    TextView tvUserId;

    @Bind(R.id.tvMemo)
    TextView tvMemo;


    @Bind(R.id.tvAmount)
    TextView tvAmount;

    @Bind(R.id.tvAmountEquivalent)
    TextView tvAmountEquivalent;

    @Bind(R.id.tvBlockNumber)
    TextView tvBlockNumber;

    @Bind(R.id.tvTrxInBlock)
    TextView tvTrxInBlock;

    @Bind(R.id.tvFee)
    TextView tvFee;

    @Bind(R.id.tvFeeEquivalent)
    TextView tvFeeEquivalent;

    @Bind(R.id.tvPaymentAmount)
    TextView tvPaymentAmount;

    @Bind(R.id.tvPaymentEquivalent)
    TextView tvPaymentEquivalent;

    @Bind(R.id.tvTotalEquivalent)
    TextView tvTotalEquivalent;

    @Bind(R.id.tvTotal)
    TextView tvTotal;

    @Bind(R.id.tvUserStatus)
    TextView tvUserStatus;

    @Bind(R.id.ivImageTag)
    ImageView ivImageTag;

    @Bind(R.id.buttonSend)
    ImageButton buttonSend;

    @Bind(R.id.scrollView)
    ScrollView scrollView;
    //int names_in_work;
    //int names_total_size;
    int assets_id_in_work;
    int assets_id_total_size;

    HashMap<String, String> map = new HashMap<>();
    HashMap<String, String> eReciptmap = new HashMap<>();
    HashMap<String, String> OPmap = new HashMap<>();
    HashMap<String, String> Freemap = new HashMap<>();
    HashMap<String, String> Amountmap = new HashMap<>();
    HashMap<String, String> Memomap = new HashMap<>();
    List<String> Assetid = new ArrayList<>();
    HashMap<String, HashMap<String, String>> SymbolsPrecisions = new HashMap<>();
    String memoMsg;
    String date;
    String otherName;
    String userName;
    Boolean isSent = false;
    String feeSymbol = "";
    String amountSymbol = "";
    String feeAmount = "";
    String amountAmount = "";
    String time = "";
    String timeZone = "";
    String emailOther = "";
    String emailUser = "";
    ProgressDialog progressDialog;
    boolean btnPress = false;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.e_receipt);
        ButterKnife.bind(this);

        context = getApplicationContext();
        progressDialog = new ProgressDialog(this);
        application.registerBalancesDelegateEReceipt(this);
        setTitle(getResources().getString(R.string.e_receipt_activity_name));

        Intent intent = getIntent();
        String eReciept = intent.getStringExtra(getResources().getString(R.string.e_receipt));

        memoMsg = intent.getStringExtra("Memo");
        date = intent.getStringExtra("Date");
        time = intent.getStringExtra("Time");
        timeZone = intent.getStringExtra("TimeZone");

        if (intent.getBooleanExtra("Sent", false)) {
            userName = intent.getStringExtra("From");
            otherName = intent.getStringExtra("To");
            //tvUserAccount.setText("Recieved From");
            //tvOtherAccount.setText("Sent To");
            tvUserStatus.setText(getString(R.string.sender_account));
            isSent = true;
            ivImageTag.setImageResource(R.drawable.send);
//            email = get_email(to);
        } else {
            // tvOtherAccount.setText("Recieved From");
            // tvUserAccount.setText("Sent To");
            tvUserStatus.setText(getString(R.string.receiver_account));
            userName = intent.getStringExtra("To");
            otherName = intent.getStringExtra("From");
            isSent = false;
            ivImageTag.setImageResource(R.drawable.receive);
//            email = get_email(from);
        }
        tvOtherName.setText(otherName);
        tvUserName.setText(userName);
        TvBlockNum.setText(date);
        tvTime.setText(time + " " + timeZone);
        //  emailOther = get_email(otherName);
        //   emailOther = "fawaz_ahmed@live.com";

//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//
//        ivOtherGravatar.requestLayout();
//
//        ivOtherGravatar.getLayoutParams().height = (width * 40) / 100;
//        ivOtherGravatar.getLayoutParams().width = (width * 40) / 100;

        init(eReciept);

        setBackButton(true);
    }

    @Override
    public void OnUpdate(String s, int id) {
        if (id == 18) {
            //String result = SupportMethods.ParseJsonObject(s,"result");
            //String time = SupportMethods.ParseJsonObject(result,"timestamp");
            assets_id_in_work = 0;
            get_asset(Assetid.get(assets_id_in_work), "19");
        } else if (id == 19) {
            if (assets_id_in_work < assets_id_total_size) {
                String result = SupportMethods.ParseJsonObject(s, "result");
                String assetObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
                String symbol = SupportMethods.ParseJsonObject(assetObject, "symbol");
                String precision = SupportMethods.ParseJsonObject(assetObject, "precision");
                HashMap<String, String> de = new HashMap<>();
                de.put("symbol", symbol);
                de.put("precision", precision);
                SymbolsPrecisions.put(Assetid.get(assets_id_in_work), de);
                if (assets_id_in_work == (assets_id_total_size - 1)) {
                    onLastCall();
                }
                assets_id_in_work++;
                if (assets_id_in_work < Assetid.size())
                    get_asset(Assetid.get(assets_id_in_work), "19");
            }
        }
    }


    String transactionIdClipped = "";
    Boolean transactionIdUpdated = false;

    private void getTransactionId(final String block_num, final String trx_in_block) {

        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("method", "get_transaction_id");
                hashMap.put("block_num", block_num);
                hashMap.put("trx_in_block", trx_in_block);

                ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
                IWebService service = sg.getService(IWebService.class);
                final Call<TransactionIdResponse> postingService = service.getTransactionIdComponent(hashMap);

                postingService.enqueue(new Callback<TransactionIdResponse>() {

                    @Override
                    public void onResponse(Response<TransactionIdResponse> response) {
                        if (response.isSuccess()) {
                            TransactionIdResponse resp = response.body();

                            if (resp.status.equals("success")) {
                                try {
                                    String trx_id = resp.transaction_id;
                                    transactionIdClipped = trx_id.substring(0, 7);
                                    transactionIdUpdated = true;
                                    if (btnPress) {
                                        generatePdf();
                                    }
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                    //getTransactionId(block_num,trx_in_block);
                                }
                            } else {
                                //getTransactionId(block_num,trx_in_block);
                            }

                        } else {
                            //getTransactionId(block_num,trx_in_block);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        getTransactionId(block_num, trx_in_block);
                    }
                });
            }
        };
        handler.postDelayed(updateTask, 100);
    }

    void init(String eRecipt) {
        eReciptmap.put("id", SupportMethods.ParseJsonObject(eRecipt, "id"));
        eReciptmap.put("op", SupportMethods.ParseJsonObject(eRecipt, "op"));
        //eReciptmap.put("result", SupportMethods.ParseJsonObject(eRecipt, "result"));
        String block_num = SupportMethods.ParseJsonObject(eRecipt, "block_num");
        eReciptmap.put("block_num", block_num);
        String trx_in_block = SupportMethods.ParseJsonObject(eRecipt, "trx_in_block");
        eReciptmap.put("trx_in_block", trx_in_block);
        getTransactionId(block_num, trx_in_block);
        eReciptmap.put("op_in_trx", SupportMethods.ParseJsonObject(eRecipt, "op_in_trx"));
        eReciptmap.put("virtual_op", SupportMethods.ParseJsonObject(eRecipt, "virtual_op"));
        String fetch_OP = SupportMethods.ParseObjectFromJsonArray(eReciptmap.get("op"), 1);
        OPmap.put("fee", SupportMethods.ParseJsonObject(fetch_OP, "fee"));
        OPmap.put("from", SupportMethods.ParseJsonObject(fetch_OP, "from"));
        OPmap.put("to", SupportMethods.ParseJsonObject(fetch_OP, "to"));
        OPmap.put("amount", SupportMethods.ParseJsonObject(fetch_OP, "amount"));
        OPmap.put("memo", SupportMethods.ParseJsonObject(fetch_OP, "memo"));
        OPmap.put("extensions", SupportMethods.ParseJsonObject(fetch_OP, "extensions"));
        Freemap.put("amount", SupportMethods.ParseJsonObject(OPmap.get("fee"), "amount"));
        Freemap.put("asset_id", SupportMethods.ParseJsonObject(OPmap.get("fee"), "asset_id"));
        Amountmap.put("asset_id", SupportMethods.ParseJsonObject(OPmap.get("amount"), "asset_id"));
        Amountmap.put("amount", SupportMethods.ParseJsonObject(OPmap.get("amount"), "amount"));
        Memomap.put("from", SupportMethods.ParseJsonObject(OPmap.get("memo"), "from"));
        Memomap.put("to", SupportMethods.ParseJsonObject(OPmap.get("memo"), "to"));
        Memomap.put("nonce", SupportMethods.ParseJsonObject(OPmap.get("memo"), "nonce"));
        Memomap.put("message", SupportMethods.ParseJsonObject(OPmap.get("memo"), "message"));

        get_Time(eReciptmap.get("block_num"), "18");

        Assetid.add(Freemap.get("asset_id"));
        Assetid.add(Amountmap.get("asset_id"));
        assets_id_total_size = Assetid.size();
    }

    void onLastCall() {
        this.runOnUiThread(new Runnable() {
            public void run() {

                //   createEmail(emailOther, ivOtherGravatar);

                // createEmail(emailUser,ivUserGravatar);


                AssetsSymbols assetsSymbols = new AssetsSymbols(context);

                HashMap<String, String> sym_preFee = SymbolsPrecisions.get(Freemap.get("asset_id"));
                feeAmount = SupportMethods.ConvertValueintoPrecision(sym_preFee.get("precision"), Freemap.get("amount"));
                feeSymbol = sym_preFee.get("symbol");

                HashMap<String, String> sym_preAmount = SymbolsPrecisions.get(Amountmap.get("asset_id"));
                amountAmount = SupportMethods.ConvertValueintoPrecision(sym_preAmount.get("precision"), Amountmap.get("amount"));
                amountSymbol = sym_preAmount.get("symbol");


                EquivalentComponents equivalentAmount = new EquivalentComponents();
                equivalentAmount.Amount = Float.parseFloat(amountAmount);
                equivalentAmount.assetSymbol = amountSymbol;
                equivalentAmount.id = 0;

                EquivalentComponents equivalentFee = new EquivalentComponents();
                equivalentFee.Amount = Float.parseFloat(feeAmount);
                equivalentFee.assetSymbol = feeSymbol;
                equivalentFee.id = 1;

                ArrayList<EquivalentComponents> arrayList = new ArrayList<>();
                arrayList.add(equivalentAmount);
                arrayList.add(equivalentFee);

                getEquivalentComponents(arrayList);

//                if(faitAmount!=null && faitSymbol!=null) {
//                    tvTotalEquivalent.setText(faitAmount + " " + faitSymbol);
//                    tvPaymentEquivalent.setText(faitAmount + " " + faitSymbol);
//                    ifEquivalentFailed();
//                }

                feeSymbol = assetsSymbols.updateString(sym_preFee.get("symbol"));
                amountSymbol = assetsSymbols.updateString(sym_preAmount.get("symbol"));

                tvBlockNumber.setText(eReciptmap.get("block_num"));
                tvTrxInBlock.setText(eReciptmap.get("id"));


                tvAmount.setText(amountAmount + " " + amountSymbol);
                tvFee.setText(feeAmount + " " + feeSymbol);
                tvTotal.setText(tvAmount.getText() + " + " + tvFee.getText());

                tvPaymentAmount.setText(tvTotal.getText());

                if (isSent) {
                    tvOtherId.setText(OPmap.get("to"));
                    tvUserId.setText(OPmap.get("from"));
                } else {
                    tvOtherId.setText(OPmap.get("from"));
                    tvUserId.setText(OPmap.get("to"));
                }


                tvMemo.setText(memoMsg);


            }
        });
    }

    void get_Time(String block_num, String id) {
        int db_id = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database));
        //  {"id":4,"method":"call","params":[2,"get_block_header",[6356159]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_block_header\",[ " + block_num + "]]}";
        Application.webSocketG.send(getDetails);
    }

    void get_names(String name_id, String id) {
        int db_id = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database));
        //    {"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        Application.webSocketG.send(getDetails);
    }

    void get_asset(String asset, String id) {
        //{"id":1,"method":"get_assets","params":[["1.3.0","1.3.120"]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\"" + asset + "\"]]}";
        Application.webSocketG.send(getDetails);
    }

    @OnClick(R.id.buttonSend)
    public void onSendButton() {
        btnPress = true;
        if (!transactionIdUpdated) {
            showDialog("", getResources().getString(R.string.updating_transaction_id));
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    showDialog("", getResources().getString(R.string.updating_transaction_id));

                }
            }, 0);
            Handler handlerStop = new Handler();
            handlerStop.postDelayed(new Runnable() {
                public void run() {
                    hideDialog();

                }
            }, 1500);
            generatePdf();

        }

    }

    String get_email(String accountName) {
        MerchantEmail merchantEmail = new MerchantEmail(context);
        return merchantEmail.getMerchantEmail(accountName);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
                SupportMethods.testing("alpha", e.getMessage(), "error");
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) bmImage.setVisibility(View.GONE);
            else {
                // Bitmap corner = getRoundedCornerBitmap(result);
                bmImage.setImageBitmap(result);
            }
        }
    }

    String finalFaitCurrency;


    private class EquivalentComponents {
        int id;
        float Amount;
        String assetSymbol;
        float faitAmount;
        Boolean available;
        String faitAssetSymbol;

        float getAmount() {
            return this.Amount;
        }

        String getAssetSymbol() {
            return this.assetSymbol;
        }
    }

//    private void getEquivalentComponents(final ArrayList<EquivalentComponents> equivalentComponentses) {
//        String faitCurrency = Helper.getFadeCurrency(context);
//
//        if (faitCurrency.isEmpty()) {
//            faitCurrency = "EUR";
//        }
//
//        String values = "";
//        for (int i = 0; i < equivalentComponentses.size(); i++) {
//            EquivalentComponents transactionDetails = equivalentComponentses.get(i);
//            if (!transactionDetails.assetSymbol.equals(faitCurrency)) {
//                values += transactionDetails.assetSymbol + ":" + faitCurrency + ",";
//            }
//        }
//
//        if (values.isEmpty()) {
//            return;
//        }
//
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("method", "equivalent_component");
//        if (values.length() > 1)
//            hashMap.put("values", values.substring(0, values.length() - 1));
//        else hashMap.put("values", "");
//        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
//        IWebService service = sg.getService(IWebService.class);
//        final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);
//        finalFaitCurrency = faitCurrency;
//
//        postingService.enqueue(new Callback<EquivalentComponentResponse>() {
//            @Override
//            public void onResponse(Response<EquivalentComponentResponse> response) {
//                if (response.isSuccess()) {
//                    EquivalentComponentResponse resp = response.body();
//                    if (resp.status.equals("success")) {
//                        try {
//                            JSONObject rates = new JSONObject(resp.rates);
//                            Iterator<String> keys = rates.keys();
//                            HashMap hm = new HashMap();
//
//                            while (keys.hasNext()) {
//                                String key = keys.next();
//                                hm.put(key.split(":")[0], rates.get(key));
//                            }
//
//                            try {
//                                for (int i = 0; i < equivalentComponentses.size(); i++) {
//                                    String asset = equivalentComponentses.get(i).getAssetSymbol();
//                                    String amount = String.valueOf(equivalentComponentses.get(i).getAmount());
//                                    equivalentComponentses.get(i).available = false;
//                                    if (!amount.isEmpty() && hm.containsKey(asset)) {
//                                        equivalentComponentses.get(i).available = true;
//                                        Currency currency = Currency.getInstance(finalFaitCurrency);
//                                        Double eqAmount = Double.parseDouble(amount) * Double.parseDouble(hm.get(asset).toString());
//                                        equivalentComponentses.get(i).faitAssetSymbol = currency.getSymbol();
//                                        equivalentComponentses.get(i).faitAmount = Float.parseFloat(String.format("%.4f", eqAmount));
//                                    }
//                                }
//                            } catch (Exception e) {
//                                // ifEquivalentFailed();
//                            }
//
//                            setEquivalentComponents(equivalentComponentses);
//
//                        } catch (JSONException e) {
//                            ifEquivalentFailed();
//                            //  testing("trasac",e, "found,found");
//                        }
////                        Toast.makeText(getActivity(), getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
//                    } else {
//                        ifEquivalentFailed();
//                        //   testing("trasac","1", "found,found");
////                        Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    ifEquivalentFailed();
//                    //  testing("trasac","2", "found,found");
//                    Toast.makeText(context, context.getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                ifEquivalentFailed();
//                Toast.makeText(context, context.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
//            }
//
//        });
//    }


    private void getEquivalentComponents(final ArrayList<EquivalentComponents> equivalentComponentses) {

        String faitCurrency = Helper.getFadeCurrency(context);

        if (faitCurrency.isEmpty()) {
            faitCurrency = "EUR";
        }

        String values = "";
        for (int i = 0; i < equivalentComponentses.size(); i++) {
            EquivalentComponents transactionDetails = equivalentComponentses.get(i);
            if (!transactionDetails.assetSymbol.equals(faitCurrency)) {
                values += transactionDetails.assetSymbol + ":" + faitCurrency + ",";
            }
        }

        if (values.isEmpty()) {
            return;
        }

        EquivalentFiatStorage equivalentFiatStorage = new EquivalentFiatStorage(context);
        HashMap hm = equivalentFiatStorage.getEqHM(faitCurrency);

        try {
            for (int i = 0; i < equivalentComponentses.size(); i++) {
                String asset = equivalentComponentses.get(i).getAssetSymbol();
                String amount = String.valueOf(equivalentComponentses.get(i).getAmount());
                equivalentComponentses.get(i).available = false;
                if (!amount.isEmpty() && hm.containsKey(asset)) {
                    equivalentComponentses.get(i).available = true;
                    Currency currency = Currency.getInstance(faitCurrency);
                    Double eqAmount = Double.parseDouble(amount) * Double.parseDouble(hm.get(asset).toString());
                    equivalentComponentses.get(i).faitAssetSymbol = currency.getSymbol();
                    equivalentComponentses.get(i).faitAmount = Float.parseFloat(String.format("%.4f", eqAmount));
                } else {
                    equivalentComponentses.get(i).faitAssetSymbol = "";
                    equivalentComponentses.get(i).faitAmount = 0f;
                }
            }
        } catch (Exception e) {
            ifEquivalentFailed();
        }

        setEquivalentComponents(equivalentComponentses);

        // ifEquivalentFailed();


    }

    void setEquivalentComponents(final ArrayList<EquivalentComponents> equivalentComponentse) {

        String value = "";
        Boolean available = false;

        EquivalentComponents equivalentAmount = equivalentComponentse.get(0);
        if (equivalentAmount.id == 0) {
            if (equivalentAmount.available) {
                available = true;
                tvAmountEquivalent.setText(equivalentAmount.faitAmount + " " + equivalentAmount.faitAssetSymbol);
            } else {
                available = false;
                tvAmountEquivalent.setVisibility(View.GONE);
                setWeight(tvAmount);
            }

            EquivalentComponents equivalentFee = equivalentComponentse.get(1);

            if (equivalentFee.id == 1) {
                if (available) {
                    tvFeeEquivalent.setText(equivalentFee.faitAmount + " " + equivalentFee.faitAssetSymbol);
                } else {
                    tvFeeEquivalent.setVisibility(View.GONE);
                    setWeight(tvFee);
                }
            }

            if (equivalentFee.id == 0) {
                if (equivalentAmount.available) {
                    available = true;
                    tvAmountEquivalent.setText(equivalentAmount.faitAmount + " " + equivalentAmount.faitAssetSymbol);
                } else {
                    available = false;
                    tvAmountEquivalent.setVisibility(View.GONE);
                    setWeight(tvAmount);
                }
            }

            if (equivalentAmount.id == 1) {
                if (available) {
                    tvFeeEquivalent.setText(equivalentFee.faitAmount + " " + equivalentFee.faitAssetSymbol);
                } else {
                    tvFeeEquivalent.setVisibility(View.GONE);
                    setWeight(tvFee);
                }
            }


            if (available) {
                tvTotalEquivalent.setText(equivalentAmount.faitAmount + equivalentFee.faitAmount + " " + equivalentAmount.faitAssetSymbol);
            } else {
                tvTotalEquivalent.setText(value);
                setWeight(tvTotal);
            }

            tvPaymentEquivalent.setText(tvTotalEquivalent.getText());
        }

    }

    void ifEquivalentFailed() {
        setWeight(tvAmount);
        setWeight(tvFee);
        setWeight(tvTotal);
    }

    void createEmail(String email, ImageView imageView) {
        String emailGravatarUrl = "https://www.gravatar.com/avatar/" + Helper.hash(email, Helper.MD5) + "?s=130&r=pg&d=404";
        new DownloadImageTask(imageView)
                .execute(emailGravatarUrl);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 90;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    void setWeight(TextView textView) {
        ViewGroup.LayoutParams params = textView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(params);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void generatePdf() {
        verifyStoragePermissions(this);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getResources().getString(R.string.folder_name) + File.separator + "eReceipt-" + transactionIdClipped + ".pdf";
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            buttonSend.setVisibility(View.INVISIBLE);
            Bitmap bitmap = Bitmap.createBitmap(
                    scrollView.getChildAt(0).getWidth(),
                    scrollView.getChildAt(0).getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            scrollView.getChildAt(0).draw(c);
            buttonSend.setVisibility(View.VISIBLE);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            Image myImage = Image.getInstance(imageInByte);
            float documentWidth = document.getPageSize().getWidth();
            float documentHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
            myImage.scaleToFit(documentWidth, documentHeight);
            myImage.setAlignment(Image.ALIGN_CENTER | Image.MIDDLE);
            document.add(myImage);
            hideDialog();

            Intent email = new Intent(Intent.ACTION_SEND);
            Uri uri = Uri.fromFile(new File(path));
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("application/pdf");
            email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(email);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getText(R.string.pdf_generated_msg_error) + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.setCancelable(false);
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