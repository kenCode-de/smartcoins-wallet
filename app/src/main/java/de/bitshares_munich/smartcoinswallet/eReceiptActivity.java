package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.models.MerchantEmail;
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

public class eReceiptActivity extends BaseActivity implements IBalancesDelegate {
    Context context;
    Application application = new Application();

    @Bind(R.id.ll_operations)
    LinearLayout ll_operations;

    @Bind(R.id.TvOperationsResults)
    TextView TvOperationsResults;

    @Bind(R.id.TvTrxInBlock)
    TextView TvTrxInBlock;

    @Bind(R.id.TvOpInTrx)
    TextView TvOpInTrx;

    @Bind(R.id.imageEmail)
    ImageView imageEmail;

    @Bind(R.id.TvBlockNum)
    TextView TvBlockNum;

    @Bind(R.id.TvVirtualOp)
    TextView TvVirtualOp;

    @Bind(R.id.TvId)
    TextView TvId;

    //int names_in_work;
    //int names_total_size;
    int assets_id_in_work;
    int assets_id_total_size;

    HashMap<String,String> map = new HashMap<>();
    HashMap<String, String> eReciptmap = new HashMap<>();
    HashMap<String, String> OPmap = new HashMap<>();
    HashMap<String, String> Freemap = new HashMap<>();
    HashMap<String, String> Amountmap = new HashMap<>();
    HashMap<String, String> Memomap = new HashMap<>();
    List<String> Assetid = new ArrayList<>();
    HashMap<String,HashMap<String,String>> SymbolsPrecisions = new HashMap<>();
    String memoMsg;
    String date;
    String to;
    String from;
    String feeSymbol = "";
    String amountSymbol = "";
    String feeAmount = "";
    String amountAmount = "";
    String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_receipt);
        ButterKnife.bind(this);

        context = getApplicationContext();
        application.registerBalancesDelegateEReceipt(this);
        setTitle(getResources().getString(R.string.e_receipt_activity_name));

        Intent intent = getIntent();
        String eReciept = intent.getStringExtra(getResources().getString(R.string.e_receipt));
        memoMsg = intent.getStringExtra("Memo");
        date = intent.getStringExtra("Date");
        to = intent.getStringExtra("To");
        from = intent.getStringExtra("From");

        if(intent.getBooleanExtra("Sent",false))
        {
            email = get_email(to);
        }
        else
            email = get_email(from);

        init(eReciept);

            setBackButton(true);
    }

    @Override
    public void OnUpdate(String s,int id) {
        if(id==18)
        {
            //String result = SupportMethods.ParseJsonObject(s,"result");
            //String time = SupportMethods.ParseJsonObject(result,"timestamp");
            assets_id_in_work=0;
            get_asset(Assetid.get(assets_id_in_work),"19");
        }
        else if(id==19)
        {
            if (assets_id_in_work < assets_id_total_size) {
                String result = SupportMethods.ParseJsonObject(s,"result");
                String assetObject = SupportMethods.ParseObjectFromJsonArray(result,0);
                String symbol = SupportMethods.ParseJsonObject(assetObject,"symbol");
                String precision = SupportMethods.ParseJsonObject(assetObject,"precision");
                HashMap<String,String> de = new HashMap<>();
                de.put("symbol",symbol);
                de.put("precision",precision);
                SymbolsPrecisions.put(Assetid.get(assets_id_in_work),de);
                if(assets_id_in_work==(assets_id_total_size-1)){
                         onLastCall();
                }
                assets_id_in_work++;
                if(assets_id_in_work<Assetid.size()) get_asset(Assetid.get(assets_id_in_work),"19");
            }
        }
    }


    String transactionIdClipped = "";
    Boolean transactionIdUpdated = false;
    private void getTransactionId (final String block_num,final String trx_in_block)
    {
        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run()
            {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("method", "get_transaction_id");
                hashMap.put("block_num", block_num);
                hashMap.put("trx_in_block", trx_in_block);

                ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
                IWebService service = sg.getService(IWebService.class);
                final Call<TransactionIdResponse> postingService = service.getTransactionIdComponent(hashMap);

                postingService.enqueue(new Callback<TransactionIdResponse>() {

                    @Override
                    public void onResponse(Response<TransactionIdResponse> response)
                    {
                        if (response.isSuccess())
                        {
                            TransactionIdResponse resp = response.body();

                            if (resp.status.equals("success"))
                            {
                                try
                                {
                                    String trx_id = resp.transaction_id;
                                    transactionIdClipped = trx_id.substring(0,7);
                                    transactionIdUpdated = true;
                                }
                                catch (Exception e)
                                {
                                    //e.printStackTrace();
                                    //getTransactionId(block_num,trx_in_block);
                                }
                            }
                            else
                            {
                                //getTransactionId(block_num,trx_in_block);
                            }

                        }
                        else
                        {
                            //getTransactionId(block_num,trx_in_block);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        getTransactionId(block_num,trx_in_block);
                    }
                });
            }
        };
        handler.postDelayed(updateTask, 100);
    }

    void init(String eRecipt)
    {
        eReciptmap.put("id", SupportMethods.ParseJsonObject(eRecipt, "id"));
        eReciptmap.put("op", SupportMethods.ParseJsonObject(eRecipt, "op"));
        eReciptmap.put("result", SupportMethods.ParseJsonObject(eRecipt, "result"));
        String block_num = SupportMethods.ParseJsonObject(eRecipt, "block_num");
        eReciptmap.put("block_num", block_num);
        String trx_in_block = SupportMethods.ParseJsonObject(eRecipt, "trx_in_block");
        eReciptmap.put("trx_in_block", trx_in_block);
        getTransactionId(block_num,trx_in_block);
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

        get_Time(eReciptmap.get("block_num"),"18");

        Assetid.add(Freemap.get("asset_id"));
        Assetid.add(Amountmap.get("asset_id"));
        assets_id_total_size = Assetid.size();
    }

    void onLastCall()
    {
        this.runOnUiThread(new Runnable() {
            public void run() {
                String fromAccountName = from;
                String toAccountName = to;

                String emailGravatarUrl = "https://www.gravatar.com/avatar/"+Helper.hash(email, Helper.MD5)+"?s=130&r=pg&d=404";
                new DownloadImageTask(imageEmail)
                        .execute(emailGravatarUrl);

                SupportMethods.testing("alpha",imageEmail.toString(),"imageview");
                AssetsSymbols assetsSymbols = new AssetsSymbols(context);

                HashMap<String, String> sym_preFee = SymbolsPrecisions.get(Freemap.get("asset_id"));
                feeAmount = SupportMethods.ConvertValueintoPrecision(sym_preFee.get("precision"), Freemap.get("amount"));
                feeSymbol = assetsSymbols.updateString(sym_preFee.get("symbol"));

                HashMap<String, String> sym_preAmount = SymbolsPrecisions.get(Amountmap.get("asset_id"));
                amountAmount = SupportMethods.ConvertValueintoPrecision(sym_preAmount.get("precision"), Amountmap.get("amount"));
                amountSymbol = assetsSymbols.updateString(sym_preAmount.get("symbol"));


                TvId.setText(eReciptmap.get("id"));
                TvBlockNum.setText(date);
                TvTrxInBlock.setText(eReciptmap.get("trx_in_block"));
                TvVirtualOp.setText(eReciptmap.get("virtual_op"));
                TvOpInTrx.setText(eReciptmap.get("op_in_trx"));

                LayoutInflater layoutInflater = LayoutInflater.from(context);

                View customView = layoutInflater.inflate(R.layout.items_erecipt, null, false);
                LinearLayout linearLayout = (LinearLayout) customView;
                LinearLayout linearLayoutOne = (LinearLayout) linearLayout.getChildAt(0);
                LinearLayout LinearLayoutTwo = (LinearLayout) linearLayoutOne.getChildAt(1);
                LinearLayout LinearLayoutFee = (LinearLayout) LinearLayoutTwo.getChildAt(0);
                TextView textViewFee = (TextView) LinearLayoutFee.getChildAt(1);
                textViewFee.setText(feeAmount);
                LinearLayoutFee = (LinearLayout) LinearLayoutTwo.getChildAt(1);
                textViewFee = (TextView) LinearLayoutFee.getChildAt(1);
                textViewFee.setText(feeSymbol);
                LinearLayout LinearLayoutThree = (LinearLayout) linearLayout.getChildAt(1);
                TextView textViewFrom = (TextView) LinearLayoutThree.getChildAt(1);
                textViewFrom.setText(fromAccountName);
                LinearLayout LinearLayoutFour = (LinearLayout) linearLayout.getChildAt(2);
                TextView textViewTo = (TextView) LinearLayoutFour.getChildAt(1);
                textViewTo.setText(toAccountName);
                LinearLayout LinearLayoutFive = (LinearLayout) linearLayout.getChildAt(3);
                LinearLayout LinearLayoutSix = (LinearLayout) LinearLayoutFive.getChildAt(1);
                LinearLayout LinearLayoutAmount = (LinearLayout) LinearLayoutSix.getChildAt(0);
                TextView textViewAmount = (TextView) LinearLayoutAmount.getChildAt(1);
                textViewAmount.setText(amountAmount);
                LinearLayoutAmount = (LinearLayout) LinearLayoutSix.getChildAt(1);
                textViewAmount = (TextView) LinearLayoutAmount.getChildAt(1);
                textViewAmount.setText(amountSymbol);
                LinearLayout LinearLayoutSeven = (LinearLayout) linearLayout.getChildAt(4);
                LinearLayout LinearLayoutEight = (LinearLayout) LinearLayoutSeven.getChildAt(1);
                LinearLayout LinearLayoutMemo = (LinearLayout) LinearLayoutEight.getChildAt(0);
                TextView textViewMemo = (TextView) LinearLayoutMemo.getChildAt(1);
                textViewMemo.setText(memoMsg);

                LinearLayout LinearLayoutNine = (LinearLayout) linearLayout.getChildAt(5);
                TextView textViewExtensions = (TextView) LinearLayoutNine.getChildAt(1);
                textViewExtensions.setText(OPmap.get("extensions"));

                ll_operations.addView(customView);
            }
            });
    }

    void get_Time(String block_num,String id){
        int db_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
        //  {"id":4,"method":"call","params":[2,"get_block_header",[6356159]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_block_header\",[ " + block_num + "]]}";
        Application.webSocketG.send(getDetails);
    }

    void get_names(String name_id,String id){
        int db_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
        //    {"id":4,"method":"call","params":[2,"get_accounts",[["1.2.101520"]]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[" + db_id + ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        Application.webSocketG.send(getDetails);
    }

    void get_asset(String asset, String id) {
        //{"id":1,"method":"get_assets","params":[["1.3.0","1.3.120"]]}
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+asset+"\"]]}";
        Application.webSocketG.send(getDetails);
    }

    @OnClick(R.id.buttonSend)
    public void onSendButton()
    {
        if ( !transactionIdUpdated )
        {
            Toast.makeText(context,getResources().getString(R.string.updating_transaction_id),Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = getResources().getString(R.string.folder_name) + File.separator + "eReceipt-" +transactionIdClipped;

        map.put("id",eReciptmap.get("id"));
        map.put("time",date);
        map.put("trx_in_block",eReciptmap.get("trx_in_block"));

        map.put("amountFee",feeAmount);
        map.put("amountAmount",amountAmount);
        map.put("symbolFee",feeSymbol);
        map.put("symbolAmount",amountSymbol);

        map.put("from",from);
        map.put("to",to);
        map.put("memo",memoMsg);
        map.put("extensions","----");
        map.put("op_in_trx",eReciptmap.get("op_in_trx"));
        map.put("virtual_op",eReciptmap.get("virtual_op"));
        map.put("operation_results","----");


        pdfTable myTable = new pdfTable(context, this, filename);
        if (imageEmail.getVisibility() == View.VISIBLE)
        {
            myTable.createTransactionpdf(map,imageEmail);
        }
        else
        {
            myTable.createTransactionpdf(map,null);
        }
    }

    String get_email(String accountName){
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
                SupportMethods.testing("alpha",e.getMessage(),"error");
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
           if(result==null) bmImage.setVisibility(View.GONE);
            else bmImage.setImageBitmap(result);
        }
    }
}
