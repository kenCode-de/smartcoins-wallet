package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.DecodeMemo;
import de.bitshares_munich.models.EquivalentComponentResponse;
import de.bitshares_munich.models.TransactionDetails;
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
 * Created by Syed Muhammad Muzzammil on 5/20/16.
 */
public class TransactionActivity implements IBalancesDelegate {

    Context context;
    AssetDelegate assetDelegate;
    Application application = new Application();
    int id_in_work;
    int id_total_size;
    int names_total_size;
    int names_in_work;
    int assets_id_in_work;
    int assets_id_total_size;
    int memo_in_work;
    int memo_total_size;
    int number_of_transactions_in_queue;
    String finalFaitCurrency;

    HashMap<String,HashMap<String,String>> arrayof_Amount_AssetId = new HashMap<>();
    ArrayList<String> blocks;
    HashMap<String,String> timestamp;
    ArrayList<HashMap<String,String>> memos = new ArrayList<>();
    HashMap<String,String> decodememos = new HashMap<>();;
    HashMap<String,String> eRecipts = new HashMap<>();;

    IBalancesDelegate balancesDelegate;
    String accountid;
    List<String> ofNames = new ArrayList<>();
    List<String> asset_ids = new ArrayList<>();
    HashMap<String,String> Names_from_Api = new HashMap<>();
    HashMap<String,HashMap<String,String>> Symbols_Precisions = new HashMap<>();
    String wifkey;

    webSocketCallHelper myWebSocketHelper;

    public TransactionActivity(Context c,final String account_id , AssetDelegate instance , String wif_key ,final int number_of_transactions_loaded,final int numberOfTransactionsToLoad)
    {
        context = c;
        assetDelegate = instance;
        balancesDelegate = this;
        application.registerBalancesDelegateTransaction(this);
        accountid = account_id;
        timestamp = new HashMap<>();

        myWebSocketHelper = new webSocketCallHelper(c);

        try
        {
            wifkey = Crypt.getInstance().decrypt_string(wif_key);
        }
        catch (Exception e)
        {
            wifkey = "";
        }

        if(account_id!=null)
        {
            get_relative_account_history(account_id, "8", number_of_transactions_loaded,numberOfTransactionsToLoad );
        }
    }

    void get_relative_account_history(final String account_id, final String id,final int n, final int numberOfTransactionsToLoad)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_relative_account_history\",[\""+account_id+"\",0," + Integer.toString(numberOfTransactionsToLoad) + ","+n+"]]}";
        myWebSocketHelper.make_websocket_call(getDetails,getDetails2, webSocketCallHelper.api_identifier.history);
    }

    void get_Time(final String block_num,final String id)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_block_header\",[ " + block_num + "]]}";
        myWebSocketHelper.make_websocket_call(getDetails,getDetails2, webSocketCallHelper.api_identifier.database);
    }

    void get_names(final String name_id, final String id)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        myWebSocketHelper.make_websocket_call(getDetails,getDetails2, webSocketCallHelper.api_identifier.database);
    }

    void get_asset(final String asset, final String id)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+asset+"\"]]}";
        myWebSocketHelper.make_websocket_call(getDetails,"", webSocketCallHelper.api_identifier.none);
    }

    @Override
    public void OnUpdate(String s,int id)
    {
        myWebSocketHelper.cleanUpTransactionsHandler();

        if(id==8)
        {
            //sentCallForTransactions = false;
            //rcvdCallForTransactions = true;
            onFirstCall(s);
        }
        else if(id==9)
        {
            //sentCallForTimes = false;
            //rcvdCallForTimes = true;
            if(id_in_work<id_total_size)
            {
                String result = SupportMethods.ParseJsonObject(s,"result");
                String time = SupportMethods.ParseJsonObject(result,"timestamp");
                timestamp.put(Integer.toString(id_in_work),time);
                if(id_in_work==(id_total_size-1))
                {
                    names_in_work=0;
                    get_names(ofNames.get(names_in_work),"10");
                }
                id_in_work++;
                if(id_in_work<blocks.size()) {
                    get_Time(blocks.get(id_in_work), "9");
                }
            }
        }
        else if(id==10)
        {
            //sentCallForNames = false;
            //rcvdCallForNames = true;
            if (names_in_work < names_total_size) {
                String result = SupportMethods.ParseJsonObject(s,"result");
               // String nameObject = returnArrayObj(result,0);
                String nameObject = SupportMethods.ParseObjectFromJsonArray(result,0);
                String name = SupportMethods.ParseJsonObject(nameObject,"name");
                Names_from_Api.put(ofNames.get(names_in_work),name);
                if(names_in_work==(names_total_size-1)){
                    assets_id_in_work=0;
                    get_asset(asset_ids.get(assets_id_in_work),"11");
                }
                names_in_work++;
                if(names_in_work<ofNames.size())
                get_names(ofNames.get(names_in_work),"10");
            }
        }
        else if(id==11)
        {
            //sentCallForAssets = false;
            //rcvdCallForAssets = true;
            if (assets_id_in_work < assets_id_total_size)
            {
               String result = SupportMethods.ParseJsonObject(s,"result");

                String assetObject = SupportMethods.ParseObjectFromJsonArray(result,0);
                String symbol = SupportMethods.ParseJsonObject(assetObject,"symbol");
                String precision = SupportMethods.ParseJsonObject(assetObject,"precision");
                HashMap<String,String> de = new HashMap<>();
                de.put("symbol",symbol);
                de.put("precision",precision);
                Symbols_Precisions.put(asset_ids.get(assets_id_in_work),de);

                if(assets_id_in_work==(assets_id_total_size-1))
                {
                    HashMap<String,String> def = new HashMap<>();
                    memo_in_work = 0;
                    if(memos.size()>0)
                    {
                        def = memos.get(memo_in_work);
                        decodeMemo(def.get("memo"),def.get("memo_id"));
                    }
                    else onLastCall();
                }
                assets_id_in_work++;
                if(assets_id_in_work<asset_ids.size()) get_asset(asset_ids.get(assets_id_in_work),"11");
            }
        }
    }

    void onFirstCall(String s)
    {
        String result = SupportMethods.ParseJsonObject(s, "result");
        int totalarrays = SupportMethods.TotalArraysOfObj(result);

        if(totalarrays!=-1)
        {
            for (int i = 0; i < totalarrays; i++)
            {
                eRecipts.put(Integer.toString(i),SupportMethods.ParseObjectFromJsonArray(result,i));
            }
        }

        HashMap<String, ArrayList<String>> arrayofOP = SupportMethods.ParseJsonArray(result, "op");
        HashMap<String, ArrayList<String>> arrayofblock_num = SupportMethods.ParseJsonArray(result, "block_num");

        blocks = arrayofblock_num.get("block_num");
        number_of_transactions_in_queue = arrayofOP.get("op").size();

        for (int i = 0; i < arrayofOP.get("op").size(); i++)
        {
            String breakArray = SupportMethods.ParseObjectFromJsonArray(arrayofOP.get("op").get(i), 1);
            HashMap<String, String> mapof_All = new HashMap<>();
            String parseOfamount = SupportMethods.ParseJsonObject(breakArray, "amount");
            String from = SupportMethods.ParseJsonObject(breakArray, "from");
            String to = SupportMethods.ParseJsonObject(breakArray, "to");
            String amount = SupportMethods.ParseJsonObject(parseOfamount, "amount");
            String asset_id = SupportMethods.ParseJsonObject(parseOfamount, "asset_id");
            mapof_All.put("amount", amount);
            mapof_All.put("asset_id", asset_id);
            mapof_All.put("from", from);
            mapof_All.put("to", to);
            ofNames.add(from);
            ofNames.add(to);
            asset_ids.add(asset_id);
            if (breakArray.contains("memo"))
            {
                String memojson = SupportMethods.ParseJsonObject(breakArray, "memo");
                HashMap<String,String> de = new HashMap<>();
                de.put("memo",memojson);
                de.put("memo_id",Integer.toString(i));
                memos.add(de);
            }
            arrayof_Amount_AssetId.put(Integer.toString(i), mapof_All);
        }

        HashSet<String> hashSet = new HashSet<>();
        hashSet.addAll(ofNames);
        ofNames.clear();
        ofNames.addAll(hashSet);
        names_total_size = ofNames.size();
        assets_id_total_size = asset_ids.size();
        id_total_size = blocks.size();
        memo_total_size = memos.size();
        id_in_work = 0;
        get_Time(blocks.get(id_in_work),"9");
    }

    private void decodeMemo(final String memo, final String key)
    {
        HashMap<String,String> hm= new HashMap<>();
        hm.put("method","decode_memo");
        hm.put("wifkey",wifkey);
        hm.put("memo", memo);
        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<DecodeMemo> postingService = service.getDecodedMemo(hm);
        postingService.enqueue(new Callback<DecodeMemo>() {
            @Override
            public void onResponse(Response<DecodeMemo> response) {
                if (response.isSuccess()) {
                    DecodeMemo resp = response.body();
                    if (resp.status.equals("success")){
                        //tvMemo.setText(resp.msg);
                     //   testing("namak",resp.msg,"msg");
                        if(memo_in_work<memo_total_size) {
                            decodememos.put(key, resp.msg);
                            if(memo_in_work==(memo_total_size-1)){
                               onLastCall();
                            }
                            HashMap<String, String> def = new HashMap<>();
                            memo_in_work++;
                            if(memo_in_work<memos.size()) {
                                def = memos.get(memo_in_work);
                                decodeMemo(def.get("memo"), def.get("memo_id"));
                            }
                        }
                    }else if(resp.status.equals("failure")){
                        if(memo_in_work<memo_total_size) {
                            decodememos.put(key,"----");
                            if(memo_in_work==(memo_total_size-1)){
                                onLastCall();
                            }
                            HashMap<String, String> def = new HashMap<>();
                            memo_in_work++;
                            if(memo_in_work<memos.size()) {
                                def = memos.get(memo_in_work);
                                decodeMemo(def.get("memo"), def.get("memo_id"));
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Throwable t) {
          //      testing("bijli",t,"past_break");
            }
        });
    }

    void testing(String msg , Object obj , String nameOfObject){
        Log.i("Saiyed_Testing","=> Msg : "+ msg + " : nameOfObject : " + nameOfObject + " : " + obj);
    }

    void testing(String msg , Exception e , String nameOfObject){
        StackTraceElement[] stackTrace = e.getStackTrace();
        String fullClassName = stackTrace[stackTrace.length - 1].getClassName();
        String className = fullClassName.substring(fullClassName
                .lastIndexOf(".") + 1);
        String methodName = stackTrace[stackTrace.length - 1].getMethodName();
        int lineNumber = stackTrace[stackTrace.length - 1].getLineNumber();
        Log.i("Saiyed_Testing","=> Msg : "+ msg + " : nameOfObject : " + nameOfObject + " : " + fullClassName + "--" + className + "--" + methodName + "--" + lineNumber);
    }

    void onLastCall()
    {
        Boolean isDonate = false;
        ArrayList<TransactionDetails> transactionDetails = new ArrayList<>();

        if(Helper.containKeySharePref(context, "hide_donations"))
        {
            isDonate = Helper.fetchBoolianSharePref(context,"hide_donations");
        }

        for(int i = 0 ; i < arrayof_Amount_AssetId.size() ; i++)
        {
            try
            {
                HashMap<String, String> mapof_All = new HashMap<>();
                mapof_All = arrayof_Amount_AssetId.get(Integer.toString(i));
                String Date = timestamp.get(Integer.toString(i));
                Boolean Sent = false;
                String fromid = mapof_All.get("from");
                if (fromid.equals(accountid)) Sent = true;
                String from = Names_from_Api.get(fromid);
                String toid = mapof_All.get("to");
                String to = Names_from_Api.get(toid);

                String memo = "----";
                String eRecipt = "";
                if (decodememos.containsKey(Integer.toString(i))) {
                    memo = decodememos.get(Integer.toString(i));
                }
                if (eRecipts.containsKey(Integer.toString(i))) {
                    eRecipt = eRecipts.get(Integer.toString(i));
                }
                //  String memo = mapof_All.get("message");
                String assetid = mapof_All.get("asset_id");
                HashMap<String, String> sym_pre = Symbols_Precisions.get(assetid);
                String amount = mapof_All.get("amount");
                String amount_pre = SupportMethods.ConvertValueintoPrecision(sym_pre.get("precision"), amount);
                String symbol = sym_pre.get("symbol");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date formatted = formatter.parse(Date);


                if (isDonate && symbol.equals("BTS") && amount_pre.equals("2.0") && to.equals("bitshares-munich")) {
                    testing("found","found", "found,found");
                } else {
                    TransactionDetails object = new TransactionDetails(formatted, Sent, to, from, memo, Float.parseFloat(amount_pre), symbol, 0f, "", eRecipt);
                    object.updateContext(context);
                    transactionDetails.add(object);
                }

            }
            catch(Exception e) {
                testing("error", e, "Try,Catch");
            }
        }
        getEquivalentComponents(transactionDetails);
    }

    private void getEquivalentComponents(final ArrayList<TransactionDetails> transactionDetailses)
    {
        String faitCurrency = Helper.getFadeCurrency(context);

        if (faitCurrency.isEmpty())
        {
            faitCurrency = "EUR";
        }

        String values = "";
        for (int i = 0; i < transactionDetailses.size(); i++) {
            TransactionDetails transactionDetails = transactionDetailses.get(i);
            if (!transactionDetails.assetSymbol.equals(faitCurrency)) {
                values += transactionDetails.assetSymbol + ":" + faitCurrency + ",";
            }
        }

        if ( values.isEmpty() )
        {
            return;
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "equivalent_component");
        if(values.length()>1)
        hashMap.put("values", values.substring(0, values.length() - 1));
        else hashMap.put("values", "");
        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
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
                            HashMap<String,Object> hm = new HashMap<>();

                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                hm.put(key.split(":")[0], rates.get(key));
                            }

                            try
                            {
                                for (int i = 0; i < transactionDetailses.size(); i++)
                                {
                                    String asset = transactionDetailses.get(i).getAssetSymbol();
                                    String amount = String.valueOf(transactionDetailses.get(i).getAmount());

                                    if (!amount.isEmpty() && hm.containsKey(asset))
                                    {
                                        Currency currency = Currency.getInstance(finalFaitCurrency);
                                        Double eqAmount = Double.parseDouble(amount) * Double.parseDouble(hm.get(asset).toString());
                                        transactionDetailses.get(i).faitAssetSymbol = currency.getSymbol();
                                        transactionDetailses.get(i).faitAmount = Float.parseFloat(String.format(Locale.ENGLISH,"%.4f", eqAmount));
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                testing("trasac",e, "found,found");
                            }
                            assetDelegate.TransactionUpdate(transactionDetailses,number_of_transactions_in_queue);
                        }
                        catch (JSONException e)
                        {
                            testing("trasac",e, "found,found");
                        }
//                        Toast.makeText(getActivity(), getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        testing("trasac","1", "found,found");
//                        Toast.makeText(getActivity(), getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    testing("trasac","2", "found,found");
                    Toast.makeText(context, context.getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, context.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }

        });
    }
}
