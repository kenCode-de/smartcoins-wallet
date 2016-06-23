package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.models.DecodeMemo;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.codecrafters.tableview.TableDataAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/26/16.
 */

public class eReceiptActivity extends BaseActivity implements BalancesDelegate {
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

    int names_in_work;
    int names_total_size;
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
        application.registerBalancesDelegate(this);
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

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public void OnUpdate(String s,int id) {
        if(id==9){
            String result = SupportMethods.ParseJsonObject(s,"result");
            String time = SupportMethods.ParseJsonObject(result,"timestamp");
            assets_id_in_work=0;
            get_asset(Assetid.get(assets_id_in_work),"11");
        }
        if(id==11) {
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
                  //  decodeMemo(OPmap.get("memo"));
                         onLastCall();
                }
                assets_id_in_work++;
                if(assets_id_in_work<Assetid.size()) get_asset(Assetid.get(assets_id_in_work),"11");
            }
        }

    }
//    void init(String value){
//        value = value.substring(1, value.length()-1);           //remove curly brackets
//        String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
//        Map<String,String> map = new HashMap<>();
//
//        for(String pair : keyValuePairs)                        //iterate over the pairs
//        {
//            String[] entry = pair.split("=");                   //split the pairs to get key and value
//            map.put(entry[0], entry[1]);          //add them to the hashmap and trim whitespaces
//        }
//
//        Iterator it = map.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            SupportMethods.testing("qanon",pair.getKey() + " = " + pair.getValue(),"eReciept");
//            it.remove(); // avoids a ConcurrentModificationException
//        }
//    }[

    void init(String eRecipt) {

        eReciptmap.put("id", SupportMethods.ParseJsonObject(eRecipt, "id"));
        eReciptmap.put("op", SupportMethods.ParseJsonObject(eRecipt, "op"));
        eReciptmap.put("result", SupportMethods.ParseJsonObject(eRecipt, "result"));
        eReciptmap.put("block_num", SupportMethods.ParseJsonObject(eRecipt, "block_num"));
        eReciptmap.put("trx_in_block", SupportMethods.ParseJsonObject(eRecipt, "trx_in_block"));
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

        get_Time(eReciptmap.get("block_num"),"9");

        Assetid.add(Freemap.get("asset_id"));
        Assetid.add(Amountmap.get("asset_id"));
        assets_id_total_size = Assetid.size();

    }
    void onLastCall(){

        this.runOnUiThread(new Runnable() {
            public void run() {
                String fromAccountName = from;
                String toAccountName = to;

                String emailGravatarUrl = "https://www.gravatar.com/avatar/"+Helper.md5(email)+"?s=130&r=pg&d=404";
                new DownloadImageTask(imageEmail)
                        .execute(emailGravatarUrl);

                SupportMethods.testing("alpha",imageEmail.toString(),"imageview");

                HashMap<String, String> sym_preFee = SymbolsPrecisions.get(Freemap.get("asset_id"));
                feeAmount = SupportMethods.ConvertValueintoPrecision(sym_preFee.get("precision"), Freemap.get("amount"));
                feeSymbol = sym_preFee.get("symbol");

                HashMap<String, String> sym_preAmount = SymbolsPrecisions.get(Amountmap.get("asset_id"));
                amountAmount = SupportMethods.ConvertValueintoPrecision(sym_preAmount.get("precision"), Amountmap.get("amount"));
                amountSymbol = sym_preAmount.get("symbol");


                TvId.setText(eReciptmap.get("id"));
                TvBlockNum.setText(date);
                TvTrxInBlock.setText(eReciptmap.get("trx_in_block"));
                TvVirtualOp.setText(eReciptmap.get("virtual_op"));
                TvOpInTrx.setText(eReciptmap.get("op_in_trx"));

                LayoutInflater layoutInflater = LayoutInflater.from(context);

                //for(int i=0;i<3;i++){
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
//                LinearLayoutMemo = (LinearLayout) LinearLayoutEight.getChildAt(1);
//                textViewMemo = (TextView) LinearLayoutMemo.getChildAt(1);
//                textViewMemo.setText(Memomap.get("to"));
//                LinearLayoutMemo = (LinearLayout) LinearLayoutEight.getChildAt(2);
//                textViewMemo = (TextView) LinearLayoutMemo.getChildAt(1);
//                textViewMemo.setText(Memomap.get("nonce"));
//                LinearLayoutMemo = (LinearLayout) LinearLayoutEight.getChildAt(3);
//                textViewMemo = (TextView) LinearLayoutMemo.getChildAt(1);
//                textViewMemo.setText(Memomap.get("message"));
                LinearLayout LinearLayoutNine = (LinearLayout) linearLayout.getChildAt(5);
                TextView textViewExtensions = (TextView) LinearLayoutNine.getChildAt(1);
                textViewExtensions.setText(OPmap.get("extensions"));

                ll_operations.addView(customView);
            }
            });

        //}
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
    public void onSendButton() {
        String filename = getResources().getString(R.string.txt_folder_name) + File.separator + "eReceipt-scwall";

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
        if (imageEmail.getVisibility() == View.VISIBLE) {
            myTable.createTransactionpdf(map,imageEmail);
        } else {
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
//        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
//        File pdfDir = new File(extStorage+"/Transaction-scwall");
//        if (!pdfDir.exists()){
//            pdfDir.mkdir();
//        }
//        File pdffile = new File(extStorage,"Transaction-scwall.pdf");
//
//        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
//        ScrollView root = (ScrollView) inflater.inflate
//                (R.layout.activity_e_receipt, null); //RelativeLayout is root view of my UI(xml) file.
//        root.setDrawingCacheEnabled(true);
//        Bitmap screen= getBitmapFromView(this.getWindow().findViewById(R.id.relativelayout));
//
//        try {
//            Document  document = new Document();
//
//            PdfWriter.getInstance(document, new FileOutputStream(pdffile));
//            document.open();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            screen.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            addImage(document,byteArray);
//            document.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//try {
//    Intent intent = new Intent(Intent.ACTION_VIEW);
//    Uri uri = Uri.fromFile(pdffile);
//    intent.setDataAndType(uri, "application/pdf");
//    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//    startActivity(intent);
//}catch (Exception e){
//
//}


//        Intent email = new Intent(Intent.ACTION_SEND);
//        email.putExtra(Intent.EXTRA_EMAIL, "receiver_email_address");
//        email.putExtra(Intent.EXTRA_SUBJECT, "subject");
//        email.putExtra(Intent.EXTRA_TEXT, "email body");
//        Uri uri = Uri.fromFile(new File(pdfDir,  "pdfFileName"));
//        email.putExtra(Intent.EXTRA_STREAM, uri);
//        email.setType("application/pdf");
//        email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getActivity().startActivity(email);
 //   }
  //  public static Bitmap getBitmapFromView(View view) {
//        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(returnedBitmap);
//        Drawable bgDrawable =view.getBackground();
//        if (bgDrawable!=null)
//            bgDrawable.draw(canvas);
//        else
//            canvas.drawColor(Color.WHITE);
//        view.draw(canvas);
//        return returnedBitmap;
//    }
//  //  private static void addImage(Document document,byte[] byteArray) {
//        Image image = null;
//        try
//        {
//            image = Image.getInstance(byteArray);
//        }
//        catch (BadElementException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (MalformedURLException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        // image.scaleAbsolute(150f, 150f);
//        try
//        {
//            document.add(image);
//        } catch (DocumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
