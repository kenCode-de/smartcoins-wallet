package de.bitshares_munich.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.models.DecodeMemo;
import de.bitshares_munich.models.EquivalentComponentResponse;
import de.bitshares_munich.models.EquivalentFiatStorage;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
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

    String accountid;
    String wifkey;
    final int time = 5000;

    long numberOfTransactionsLoaded = 0;
    long numberOfTransactionsToLoad = 0;

    public TransactionActivity(Context c, final String account_id , AssetDelegate instance , String wif_key , final long _numberOfTransactionsLoaded, final long _numberOfTransactionsToLoad)
    {
        context = c;
        assetDelegate = instance;
        //balancesDelegate = this;
        application.registerBalancesDelegateTransaction(this);
        accountid = account_id;
        //timestamp = new HashMap<>();

        handleHourlyTransactions = new Handler(Looper.getMainLooper());
        transactionsRecievedHm = new HashMap<>();
        assetsRecievedHm = new HashMap<>();
        headerTimings = new HashMap<>();
        namesToResolveHm = new HashMap<>();
        memosToDecodeHm = new HashMap<>();
        //hourlyNumberOfTransactionsLoaded = 0;
        finalBlockRecieved = false;

        numberOfTransactionsLoaded = _numberOfTransactionsLoaded;
        numberOfTransactionsToLoad = _numberOfTransactionsToLoad;

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
            get_relative_account_history(account_id, "8", _numberOfTransactionsLoaded,_numberOfTransactionsToLoad );
        }
    }

    public TransactionActivity(Context c, final String account_id , AssetDelegate instance , String wif_key , final Date _transactionsTimeSpan)
    {
        context = c;
        assetDelegate = instance;
        application.registerBalancesDelegateTransaction(this);
        accountid = account_id;

        try
        {
            wifkey = Crypt.getInstance().decrypt_string(wif_key);
        }
        catch (Exception e)
        {
            wifkey = "";
        }

        handleHourlyTransactions = new Handler(Looper.getMainLooper());
        transactionsTimeSpan = _transactionsTimeSpan;
        transactionsRecievedHm = new HashMap<>();
        assetsRecievedHm = new HashMap<>();
        headerTimings = new HashMap<>();
        namesToResolveHm = new HashMap<>();
        memosToDecodeHm = new HashMap<>();
        hourlyNumberOfTransactionsLoaded = 0;
        finalBlockRecieved = false;

        if( account_id != null )
        {
            get_relative_account_history(account_id, "20", hourlyNumberOfTransactionsLoaded );
        }
    }

    Handler handleHourlyTransactions;
    boolean callInProgressForHourlyTransactions = false;
    boolean callReceivedForHourlyTransactions = true;
    Date transactionsTimeSpan;
    long hourlyNumberOfTransactionsLoaded = 0;
    String hourlyTransactionsAccountId;

    public enum api_identifier {
        database,
        history,
        network,
        none
    }

    private void make_websocket_call(final String call_string_before_identifier, final String call_string_after_identifier,final api_identifier identifer)
    {
        handleHourlyTransactions.removeCallbacksAndMessages(null);

        final Runnable checkifTransactionRecieved = new Runnable() {
            @Override
            public void run()
            {
                if ( callInProgressForHourlyTransactions && !callReceivedForHourlyTransactions && Application.isReady) // if balances are not returned in one second
                {
                    Application.webSocketG.close();
                    make_websocket_call(call_string_before_identifier,call_string_after_identifier,identifer);
                }
                else if ( callInProgressForHourlyTransactions && !callReceivedForHourlyTransactions )
                {
                    make_websocket_call(call_string_before_identifier,call_string_after_identifier,identifer);
                }
            }
        };


        final Runnable initiateTransactionsFetch = new Runnable() {
            @Override
            public void run()
            {
                if ( Application.isReady )
                {
                    String call = "";

                    if ( identifer == api_identifier.database )
                    {
                        if ( context == null ) return;
                        int database_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
                        call = call_string_before_identifier + database_id + call_string_after_identifier;
                    }
                    else if ( identifer == api_identifier.history )
                    {
                        if ( context == null ) return;
                        int history_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_history));
                        call = call_string_before_identifier + history_id + call_string_after_identifier;
                    }
                    else if ( identifer == api_identifier.network )
                    {
                        if ( context == null ) return;
                        int nw_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_network_broadcast));
                        call = call_string_before_identifier + nw_id + call_string_after_identifier;
                    }
                    else
                    {
                        call = call_string_before_identifier;
                    }

                    Application.webSocketG.send(call);
                    callInProgressForHourlyTransactions = true;
                    callReceivedForHourlyTransactions = false;
                    handleHourlyTransactions.postDelayed(checkifTransactionRecieved, time);
                }
                else
                {
                    make_websocket_call(call_string_before_identifier,call_string_after_identifier,identifer);
                }
            }
        };

        handleHourlyTransactions.postDelayed(initiateTransactionsFetch,5);
    }

    void get_relative_account_history(final String account_id, final String id,final long _numberOfTransactionsLoaded)
    {
        handleHourlyTransactions.removeCallbacksAndMessages(null);
        hourlyTransactionsAccountId = account_id;

        String getDetails_before = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails_after = ",\"get_relative_account_history\",[\""+account_id+"\",0," + Integer.toString(100) + ","+ Long.toString(_numberOfTransactionsLoaded) +"]]}";

        make_websocket_call(getDetails_before,getDetails_after, api_identifier.history);
    }

    void get_relative_account_history(final String account_id, final String id,final long _numberOfTransactionsLoaded, final long _numberOfTransactionsToLoad)
    {
        handleHourlyTransactions.removeCallbacksAndMessages(null);
        hourlyTransactionsAccountId = account_id;

        String getDetails_before = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails_after = ",\"get_relative_account_history\",[\""+account_id+"\",0," + Long.toString(_numberOfTransactionsToLoad) + ","+ Long.toString(_numberOfTransactionsLoaded) +"]]}";

        make_websocket_call(getDetails_before,getDetails_after, api_identifier.history);
    }

    void get_block_header(final String id,final String block_num)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_block_header\",[" + block_num + "]]}";
        make_websocket_call(getDetails,getDetails2, api_identifier.database);
    }

    void get_assets(final String asset, final String id)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[" + asset + "]]}";
        make_websocket_call(getDetails,"", api_identifier.none);
    }

    void get_names_transactions_recieved(final String names, final String id) {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_accounts\",[[" + names + "]]]}";
        make_websocket_call(getDetails, getDetails2, api_identifier.database);
    }

    @Override
    public void OnUpdate(String s,int id)
    {
        callInProgressForHourlyTransactions = false;
        callReceivedForHourlyTransactions = true;
        handleHourlyTransactions.removeCallbacksAndMessages(null);

        if(id==8)
        {
            //onFirstCall(s);
            transactionsReceived(s);
        }
        else if (id == 20)
        {
            hourlyTransactionsReceived(s);
        }
        else if (id == 21)
        {
            if ( numberOfTransactionsToLoad > 0 )
            {
                blockNumberTimeReceived(s);
            }
            else
            {
                blockNumberTimeReceivedHourly(s);
            }
        }
        else if (id == 22)
        {
            assetsRecieved(s);
        }
        else if (id == 23)
        {
            namesRecieved(s);
        }
    }

    String firstTransactionId = "";
    // block number -> ids -> operation
    HashMap<String,HashMap<String,JSONObject>> transactionsRecievedHm;
    HashMap<String,JSONObject> assetsRecievedHm;
    HashMap<String,JSONObject> namesToResolveHm;
    HashMap<JSONObject,String> memosToDecodeHm;
    public boolean finalBlockRecieved = false;

    void hourlyTransactionsReceived (String result)
    {
        try
        {
            JSONArray myJson = new JSONObject(result).getJSONArray("result");

            // extract first transaction to detect when all the transactions are loaded
            if ( hourlyNumberOfTransactionsLoaded == 0 )
            {
                for (int i = 0 ; i < myJson.length(); i++)
                {
                    firstTransactionId = "";

                    JSONObject firstTx = myJson.getJSONObject(i);

                    if ( firstTx.has("id") )
                    {
                        firstTransactionId = firstTx.get("id").toString();
                        break;
                    }
                }

                if ( firstTransactionId.isEmpty() )
                {
                    if ( context == null ) return;
                    assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_transactions));
                    return;
                }
            }

            // initialize few HashMaps and else
            if ( transactionsRecievedHm == null )
            {
                transactionsRecievedHm = new HashMap<>();
                finalBlockRecieved = false;
                assetsRecievedHm = new HashMap<>();
                namesToResolveHm = new HashMap<>();
                memosToDecodeHm = new HashMap<>();
            }

            //List<String> headersReceived = new ArrayList<>();
            headersTimeToFetch = new ArrayList<>();

            for (int i = 0 ; i < myJson.length(); i++)
            {
                if (    !( (hourlyNumberOfTransactionsLoaded == 0) && (i == 0) )
                        && myJson.getJSONObject(i).has("id")
                        && myJson.getJSONObject(i).get("id").toString().equals(firstTransactionId)
                   )
                {
                    // first transaction occurred again in history
                    // meaning last transaction is already received
                    finalBlockRecieved = true;
                }
                else
                {
                    if ( myJson.getJSONObject(i).has("block_num")  && myJson.getJSONObject(i).has("id") )
                    {
                        if ( transactionsRecievedHm.containsKey(myJson.getJSONObject(i).get("block_num").toString()) )
                        {
                            if ( transactionsRecievedHm.get(myJson.getJSONObject(i).get("block_num").toString()).containsKey(myJson.getJSONObject(i).get("id").toString()) )
                            {
                                // id already exists
                            }
                            else
                            {
                                transactionsRecievedHm.get(myJson.getJSONObject(i).get("block_num").toString()).put(myJson.getJSONObject(i).get("id").toString(),myJson.getJSONObject(i));
                            }
                        }
                        else
                        {
                            HashMap<String, JSONObject> newENtry = new HashMap<>();
                            newENtry.put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                            transactionsRecievedHm.put(myJson.getJSONObject(i).get("block_num").toString(),newENtry );

                            headersTimeToFetch.add(myJson.getJSONObject(i).get("block_num").toString());
                        }
                    }
                    else
                    {
                        Log.d("Loading Transactions","duplication");
                    }
                }

                try
                {
                    if ( myJson.getJSONObject(i).has("op") )
                    {
                        for (int j = 0; j < myJson.getJSONObject(i).getJSONArray("op").length(); j++)
                        {
                            if (myJson.getJSONObject(i).getJSONArray("op").get(j) instanceof JSONObject)
                            {
                                // get fee asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("fee"))
                                {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("fee").get("asset_id").toString();

                                    if ( !assetsRecievedHm.containsKey(asset_id) )
                                    {
                                        assetsRecievedHm.put(asset_id,null);
                                    }
                                }

                                // get amount asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("amount"))
                                {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("amount").get("asset_id").toString();

                                    if ( !assetsRecievedHm.containsKey(asset_id) )
                                    {
                                        assetsRecievedHm.put(asset_id,null);
                                    }
                                }

                                // get names of person from which money is recieved
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("from"))
                                {
                                    if ( !namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from")) )
                                    {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"),null);
                                    }
                                }

                                // get names of person to whom money is sent
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("to"))
                                {
                                    if ( !namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to")) )
                                    {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"),null);
                                    }
                                }

                                // get memos to decode
                                if ( myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("memo") && myJson.getJSONObject(i).has("block_num") )
                                {
                                    if ( !memosToDecodeHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo")) )
                                    {
                                        memosToDecodeHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"),myJson.getJSONObject(i).getString("block_num"));
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {

                }
            }

            hourlyNumberOfTransactionsLoaded += 100;
            //assetDelegate.rawTransactionsLoaded(hourlyNumberOfTransactionsLoaded);

            if ( headersTimeToFetch.size() == 0 )
            {
                getAssetsInTransactionsReceived();
                return;
            }

            Collections.sort(headersTimeToFetch);
            Collections.reverse(headersTimeToFetch);

            indexHeadersTimeToFetch = 0;

            if ( headerTimings == null )
            {
                headerTimings = new HashMap<>();
            }

            getTimeForTransactionsRecieved();
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Loading Transactions",e.getMessage());
        }
    }

    void transactionsReceived (String result)
    {
        try
        {
            JSONArray myJson = new JSONObject(result).getJSONArray("result");

            // extract first transaction to detect when all the transactions are loaded
            if ( numberOfTransactionsLoaded == 0 )
            {
                for (int i = 0 ; i < myJson.length(); i++)
                {
                    firstTransactionId = "";

                    JSONObject firstTx = myJson.getJSONObject(i);

                    if ( firstTx.has("id") )
                    {
                        firstTransactionId = firstTx.get("id").toString();
                        break;
                    }
                }

                if ( firstTransactionId.isEmpty() )
                {
                    if ( context == null ) return;
                    assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_transactions));
                    return;
                }
            }

            // initialize few HashMaps and else
            if ( transactionsRecievedHm == null )
            {
                transactionsRecievedHm = new HashMap<>();
                finalBlockRecieved = false;
                assetsRecievedHm = new HashMap<>();
                namesToResolveHm = new HashMap<>();
                memosToDecodeHm = new HashMap<>();
            }

            //List<String> headersReceived = new ArrayList<>();
            headersTimeToFetch = new ArrayList<>();

            for (int i = 0 ; i < myJson.length(); i++)
            {
                if (    !( (hourlyNumberOfTransactionsLoaded == 0) && (i == 0) )
                        && myJson.getJSONObject(i).has("id")
                        && myJson.getJSONObject(i).get("id").toString().equals(firstTransactionId)
                        )
                {
                    // first transaction occurred again in history
                    // meaning last transaction is already received
                    finalBlockRecieved = true;
                }
                else
                {
                    if ( myJson.getJSONObject(i).has("block_num")  && myJson.getJSONObject(i).has("id") )
                    {
                        if ( transactionsRecievedHm.containsKey(myJson.getJSONObject(i).get("block_num").toString()) )
                        {
                            if ( transactionsRecievedHm.get(myJson.getJSONObject(i).get("block_num").toString()).containsKey(myJson.getJSONObject(i).get("id").toString()) )
                            {
                                // id already exists
                            }
                            else
                            {
                                transactionsRecievedHm.get(myJson.getJSONObject(i).get("block_num").toString()).put(myJson.getJSONObject(i).get("id").toString(),myJson.getJSONObject(i));
                            }
                        }
                        else
                        {
                            HashMap<String, JSONObject> newENtry = new HashMap<>();
                            newENtry.put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                            transactionsRecievedHm.put(myJson.getJSONObject(i).get("block_num").toString(),newENtry );

                            headersTimeToFetch.add(myJson.getJSONObject(i).get("block_num").toString());
                        }
                    }
                    else
                    {
                        Log.d("Loading Transactions","duplication");
                    }
                }

                try
                {
                    if ( myJson.getJSONObject(i).has("op") )
                    {
                        for (int j = 0; j < myJson.getJSONObject(i).getJSONArray("op").length(); j++)
                        {
                            if (myJson.getJSONObject(i).getJSONArray("op").get(j) instanceof JSONObject)
                            {
                                // get fee asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("fee"))
                                {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("fee").get("asset_id").toString();

                                    if ( !assetsRecievedHm.containsKey(asset_id) )
                                    {
                                        assetsRecievedHm.put(asset_id,null);
                                    }
                                }

                                // get amount asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("amount"))
                                {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("amount").get("asset_id").toString();

                                    if ( !assetsRecievedHm.containsKey(asset_id) )
                                    {
                                        assetsRecievedHm.put(asset_id,null);
                                    }
                                }

                                // get names of person from which money is recieved
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("from"))
                                {
                                    if ( !namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from")) )
                                    {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"),null);
                                    }
                                }

                                // get names of person to whom money is sent
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("to"))
                                {
                                    if ( !namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to")) )
                                    {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"),null);
                                    }
                                }

                                // get memos to decode
                                if ( myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("memo") && myJson.getJSONObject(i).has("block_num") )
                                {
                                    if ( !memosToDecodeHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo")) )
                                    {
                                        memosToDecodeHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"),myJson.getJSONObject(i).getString("block_num"));
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {

                }
            }

            numberOfTransactionsLoaded += numberOfTransactionsToLoad;
            //hourlyNumberOfTransactionsLoaded += 100;
            //assetDelegate.rawTransactionsLoaded(hourlyNumberOfTransactionsLoaded);

            if ( headersTimeToFetch.size() == 0 )
            {
                getAssetsInTransactionsReceived();
                return;
            }

            Collections.sort(headersTimeToFetch);
            Collections.reverse(headersTimeToFetch);

            indexHeadersTimeToFetch = 0;

            if ( headerTimings == null )
            {
                headerTimings = new HashMap<>();
            }

            getTimeForTransactionsRecieved();
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Loading Transactions",e.getMessage());
        }
    }


    List<String> headersTimeToFetch;
    int indexHeadersTimeToFetch;
    void getTimeForTransactionsRecieved()
    {
        try
        {
            get_block_header("21",headersTimeToFetch.get(indexHeadersTimeToFetch));
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Transactions Time",e.getMessage());
        }
    }

    HashMap<String,Date> headerTimings;
    void blockNumberTimeReceivedHourly(String result)
    {
        try
        {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if ( !resultJson.has("result") )
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            JSONObject header = resultJson.getJSONObject("result");

            if ( !header.has("timestamp"))
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            String time = header.getString("timestamp");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date formattedDate = formatter.parse(time);

            // compare if less than or more than required Hours To load
            // if loaded transactions are enough then get asset objects
            // else get next block of transactions
            if ( formattedDate.before(transactionsTimeSpan) )
            {
                getAssetsInTransactionsReceived();
            }
            else
            {
                //assetDelegate.transactionTimeLoaded(indexHeadersTimeToFetch + 1 + hourlyNumberOfTransactionsLoaded - 100 ,headersTimeToFetch.size() + hourlyNumberOfTransactionsLoaded - 100);
                String msg = String.format("%d / %d",indexHeadersTimeToFetch + 1 + hourlyNumberOfTransactionsLoaded - 100,headersTimeToFetch.size() + hourlyNumberOfTransactionsLoaded - 100);
                assetDelegate.transactionsLoadMessageStatus(msg);

                if (++indexHeadersTimeToFetch < headersTimeToFetch.size())
                {
                    headerTimings.put(headersTimeToFetch.get(indexHeadersTimeToFetch - 1), formattedDate);
                    getTimeForTransactionsRecieved();
                }
                else if ( finalBlockRecieved )
                {
                    getAssetsInTransactionsReceived();
                }
                else
                {
                    get_relative_account_history(hourlyTransactionsAccountId, "20", hourlyNumberOfTransactionsLoaded);
                }
            }
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Transactions Time",e.getMessage());
        }
    }

    void blockNumberTimeReceived(String result)
    {
        try
        {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if ( !resultJson.has("result") )
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            JSONObject header = resultJson.getJSONObject("result");

            if ( !header.has("timestamp"))
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            String time = header.getString("timestamp");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date formattedDate = formatter.parse(time);

            String msg = String.format(Locale.ENGLISH,"%d / %d",indexHeadersTimeToFetch + 1 + numberOfTransactionsLoaded - numberOfTransactionsToLoad,headersTimeToFetch.size() + numberOfTransactionsLoaded - numberOfTransactionsToLoad);
            assetDelegate.transactionsLoadMessageStatus(msg);

            if (++indexHeadersTimeToFetch < headersTimeToFetch.size())
            {
                headerTimings.put(headersTimeToFetch.get(indexHeadersTimeToFetch - 1), formattedDate);
                getTimeForTransactionsRecieved();
            }
            else if ( finalBlockRecieved )
            {
                getAssetsInTransactionsReceived();
            }
            else
            {
                getAssetsInTransactionsReceived();
                //get_relative_account_history(hourlyTransactionsAccountId, "20", hourlyNumberOfTransactionsLoaded);
            }
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Transactions Time",e.getMessage());
        }
    }

    void getAssetsInTransactionsReceived()
    {
        try
        {
            // fetch assets
            // usables :
            // transactionsRecievedHm
            // headerTimings
            // assetsRecievedHm

            if ( context == null ) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fetching_assets_in_transactions));

            if ( assetsRecievedHm.size() > 0 )
            {
                String values = "";

                for (String assets : assetsRecievedHm.keySet())
                {
                    values += "\"" + assets + "\"" + ",";
                }

                values = values.substring(0, values.length() - 1);

                get_assets(values,"22");
            }
            else
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.no_assets_found));
            }
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void assetsRecieved(String result)
    {
        try
        {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if ( !resultJson.has("result") )
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_assets));
                return;
            }

            JSONArray assets = resultJson.getJSONArray("result");

            for (int i = 0 ; i < assets.length() ; i++)
            {
                JSONObject asset = assets.getJSONObject(i);

                if ( asset.has("id") && assetsRecievedHm.containsKey(asset.getString("id")) )
                {
                    assetsRecievedHm.put(asset.getString("id"), asset);
                }
            }

            if ( context == null ) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.assets_retrieved) + assetsRecievedHm.size() );

            getNamesInTransactionsRecieved();
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void getNamesInTransactionsRecieved()
    {
        try
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.resolving_account_names_in_transactions));
            if ( namesToResolveHm.size() > 0 )
            {
                String values = "";

                for (String assets : namesToResolveHm.keySet())
                {
                    values += "\"" + assets + "\"" + ",";
                }

                values = values.substring(0, values.length() - 1);

                get_names_transactions_recieved(values,"23");
            }
            else
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.account_names_not_found));
            }
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void namesRecieved(String result)
    {
        try
        {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if ( !resultJson.has("result") )
            {
                if ( context == null ) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_accounts_name));
                return;
            }

            JSONArray names = resultJson.getJSONArray("result");

            for (int i = 0 ; i < names.length() ; i++)
            {
                JSONObject asset = names.getJSONObject(i);

                if ( asset.has("id") && namesToResolveHm.containsKey(asset.getString("id")) )
                {
                    namesToResolveHm.put(asset.getString("id"), asset);
                }
            }

            if ( context == null ) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.account_names_retrieved)+ namesToResolveHm.size());

            if ( context == null ) return;
            String faitCurrency = Helper.getFadeCurrency(context);
            getEquivalentFiatRates(faitCurrency);
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    private void decodeMemoTransactionsRecieved(final JSONObject memo)
    {
        HashMap<String,String> hm = new HashMap<>();
        hm.put("method","decode_memo");
        hm.put("wifkey",wifkey);
        hm.put("memo", memo.toString());

        if ( context == null ) return;
        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<DecodeMemo> postingService = service.getDecodedMemo(hm);
        postingService.enqueue(new Callback<DecodeMemo>() {
            @Override
            public void onResponse(Response<DecodeMemo> response)
            {
                if (response.isSuccess())
                {
                    DecodeMemo resp = response.body();
                    if (resp.status.equals("success"))
                    {
                        //resp.msg
                        if ( memosToDecodeHm.containsKey(memo) )
                        {
                            memosToDecodeHm.put(memo,resp.msg);
                        }

                        loadMemoSListForDecoding();
                    }
                    else
                    {
                        loadMemoSListForDecoding();
                    }
                }
                else
                {
                    loadMemoSListForDecoding();
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                loadMemoSListForDecoding();
            }
        });
    }

    JSONObject[] encryptedMemos;
    int indexEncryptedMemos;
    private void loadMemoSListForDecoding()
    {
        try
        {
            if ( indexEncryptedMemos < memosToDecodeHm.size() )
            {
                decodeMemoTransactionsRecieved(encryptedMemos[indexEncryptedMemos]);
                indexEncryptedMemos++;
                if ( context == null ) return;
                assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.decrypting_memos) + indexEncryptedMemos + " of " + memosToDecodeHm.size());
            }
            else
            {
                generateTransactionsDetailArray();
            }

        }
        catch (Exception e)
        {
            generateTransactionsDetailArray();
        }
    }

    private void decodeRecievedMemos()
    {
        try
        {

            // trim down extra memos
            JSONObject[] keys = memosToDecodeHm.keySet().toArray(new JSONObject[memosToDecodeHm.size()]);
            for ( JSONObject key:keys )
            {
                if ( !headerTimings.containsKey(memosToDecodeHm.get(key)) )
                {
                    memosToDecodeHm.remove(key);
                }
                else
                {
                    memosToDecodeHm.put(key,null);
                }
            }


            if ( memosToDecodeHm.size() > 0 )
            {
                encryptedMemos = memosToDecodeHm.keySet().toArray(new JSONObject[memosToDecodeHm.size()]);
                indexEncryptedMemos = 0;
                loadMemoSListForDecoding();
            }
            else
            {
                generateTransactionsDetailArray();
            }
        }
        catch (Exception e)
        {
            generateTransactionsDetailArray();
        }
    }

    private void generateTransactionsDetailArray()
    {
        try
        {
            String[] blocks = headerTimings.keySet().toArray(new String[headerTimings.size()]);
            List<String> listOfBlocks = Arrays.asList(blocks);

            Collections.sort(listOfBlocks);
            Collections.reverse(listOfBlocks);

            List<TransactionDetails> myTransactions = new ArrayList<>();

            for (String blockNum:listOfBlocks)
            {
                HashMap<String,JSONObject> transactions = transactionsRecievedHm.get(blockNum);
                String[] transactionsIds = transactions.keySet().toArray(new String[transactions.size()]);
                List<String> transactionIdsList = Arrays.asList(transactionsIds);
                Collections.sort(transactionIdsList);
                Collections.reverse(transactionIdsList);

                for(String id:transactionIdsList)
                {
                    JSONObject transaction = transactions.get(id);

                    if ( transaction.has("op") )
                    {
                        for (int j = 0; j < transaction.getJSONArray("op").length(); j++)
                        {
                            if (transaction.getJSONArray("op").get(j) instanceof JSONObject)
                            {
                                TransactionDetails myTransactionDetails = new TransactionDetails();
                                myTransactionDetails.Date = headerTimings.get(blockNum);

                                // get amount asset
                                if (transaction.getJSONArray("op").getJSONObject(j).has("amount"))
                                {
                                    // get asset symbol
                                    myTransactionDetails.assetSymbol = assetsRecievedHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("asset_id")).getString("symbol");

                                    // get asset amount
                                    String precision = assetsRecievedHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("asset_id")).getString("precision");
                                    String number = transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("amount");
                                    myTransactionDetails.Amount = SupportMethods.convertAssetAmountToDouble(precision,number);

                                    // get fiat amount and symbol
                                    if ( equivalentRatesHm.get(myTransactionDetails.assetSymbol) != null )
                                    {
                                        myTransactionDetails.faitAmount = SupportMethods.convertAssetAmountToFiat(myTransactionDetails.Amount,equivalentRatesHm.get(myTransactionDetails.assetSymbol));
                                        if ( context == null ) return;
                                        myTransactionDetails.faitAssetSymbol = Helper.getFadeCurrencySymbol(context);
                                    }
                                    else
                                    {
                                        myTransactionDetails.faitAmount = 0;
                                        myTransactionDetails.faitAssetSymbol = "";
                                    }
                                }

                                // get names of person from which money is recieved
                                if ( transaction.getJSONArray("op").getJSONObject(j).has("from") )
                                {
                                    myTransactionDetails.From = namesToResolveHm.get(transaction.getJSONArray("op").getJSONObject(j).getString("from")).getString("name");
                                }
                                else
                                {
                                    myTransactionDetails.From = "";
                                }

                                // get names of person to whom money is sent
                                if ( transaction.getJSONArray("op").getJSONObject(j).has("to") )
                                {
                                    myTransactionDetails.To = namesToResolveHm.get(transaction.getJSONArray("op").getJSONObject(j).getString("to")).getString("name");
                                }
                                else
                                {
                                    myTransactionDetails.To = "";
                                }

                                // get if sent or received
                                if ( transaction.getJSONArray("op").getJSONObject(j).has("to") )
                                {
                                    if ( accountid.equals(transaction.getJSONArray("op").getJSONObject(j).getString("to")) )
                                    {
                                        myTransactionDetails.Sent = false;
                                    }
                                    else
                                    {
                                        myTransactionDetails.Sent = true;
                                    }
                                }
                                else
                                {
                                    continue;
                                }

                                // get memo message
                                if ( transaction.getJSONArray("op").getJSONObject(j).has("memo") )
                                {
                                    if ( memosToDecodeHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("memo")) != null )
                                    {
                                        myTransactionDetails.Memo = memosToDecodeHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("memo"));
                                    }
                                    else
                                    {
                                        myTransactionDetails.Memo = "";
                                    }
                                }
                                else
                                {
                                    myTransactionDetails.Memo = "";
                                }

                                myTransactionDetails.eReceipt = transaction.toString();

                                myTransactions.add(myTransactionDetails);
                                if ( context == null ) return;
                                assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.generating_list_of_transactions) + myTransactions.size());
                            }
                        }
                    }
                }
            }

            // return transactiondetails array
            assetDelegate.transactionsLoadComplete(myTransactions);
        }
        catch (Exception e)
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    HashMap<String,String> equivalentRatesHm;
    int retryGetEquivalentRates = 0 ;

    private void reTryGetEquivalentComponents (final String fc)
    {
        if ( retryGetEquivalentRates++ < 2 )
        {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getEquivalentFiatRates(fc);
                }
            }, 100);
        }
        else
        {
            if ( context == null ) return;
            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
            equivalentRatesHm = myFiatStorage.getEqHM(fc);
            decodeRecievedMemos();
        }
    }

    private void getEquivalentFiatRates(final String faitCurrency)
    {
        if ( context == null ) return;
        assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.getting_equivalent_flat_exchange_rate));
        equivalentRatesHm = new HashMap<>();

        //if ( context == null ) return;
        //final String faitCurrency = Helper.getFadeCurrency(context);

        final List<String> pairs = new ArrayList<>();
        String values = "";

        for (JSONObject asset:assetsRecievedHm.values())
        {
            try
            {
                if ( !asset.getString("symbol").equals(faitCurrency) )
                {
                    values += asset.getString("symbol") + ":" + faitCurrency + ",";
                    pairs.add(asset.getString("symbol") + ":" + faitCurrency);
                }
            }
            catch (Exception e)
            {

            }
        }

        if ( values.isEmpty() )
        {
            if ( context == null ) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_input_for_fiat_conversion));
            return;
        }

        values = values.substring(0, values.length() - 1);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "equivalent_component");
        hashMap.put("values", values);

        if ( context == null ) return;
        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);

        postingService.enqueue(new Callback<EquivalentComponentResponse>() {
            @Override
            public void onResponse(Response<EquivalentComponentResponse> response)
            {
                if (response.isSuccess())
                {
                    EquivalentComponentResponse resp = response.body();

                    if (resp.status.equals("success"))
                    {
                        try
                        {
                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            equivalentRatesHm = new HashMap<>();



                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                equivalentRatesHm.put(key.split(":")[0], rates.get(key).toString());

                                if ( pairs.contains(key) )
                                {
                                    pairs.remove(key);
                                }
                            }

                            if ( context == null ) return;
                            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                            myFiatStorage.saveEqHM(faitCurrency,equivalentRatesHm);
                            equivalentRatesHm = myFiatStorage.getEqHM(faitCurrency);

                            if ( context == null ) return;
                            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fiat_exchange_rate_received));

                            if ( pairs.size() > 0 )
                            {
                                getIndirectEquivalentFiatRates(pairs,faitCurrency);
                            }
                            else
                            {
                                decodeRecievedMemos();
                            }

                        }
                        catch (Exception e)
                        {
                            reTryGetEquivalentComponents(faitCurrency);
                        }
                    }
                    else
                    {
                        reTryGetEquivalentComponents(faitCurrency);
                    }
                }
                else
                {
                    reTryGetEquivalentComponents(faitCurrency);
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                reTryGetEquivalentComponents(faitCurrency);
            }
        });
    }

    int retryGetIndirectEquivalentRates = 0 ;

    private void reTryGetIndirectEquivalentComponents (final List<String> leftOvers, final String faitCurrency)
    {
        if ( retryGetIndirectEquivalentRates++ < 2 )
        {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getIndirectEquivalentFiatRates(leftOvers,faitCurrency);
                }
            }, 100);
        }
        else
        {
            if ( context == null ) return;
            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
            equivalentRatesHm = myFiatStorage.getEqHM(faitCurrency);
            decodeRecievedMemos();
        }
    }

    private void getIndirectEquivalentFiatRates(final List<String> leftOvers, final String faitCurrency)
    {
        List<String> newPairs = new ArrayList<>();

        for (String pair : leftOvers) {
            String firstHalf = pair.split(":")[0];
            newPairs.add(firstHalf + ":" + "BTS");
        }

        newPairs.add("BTS" + ":" + faitCurrency);

        String values = "";

        for (String pair : newPairs) {
            values += pair + ",";
        }

        values = values.substring(0, values.length() - 1);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "equivalent_component");
        hashMap.put("values", values);

        if ( context == null ) return;
        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);

        postingService.enqueue(new Callback<EquivalentComponentResponse>() {
            @Override
            public void onResponse(Response<EquivalentComponentResponse> response)
            {
                if (response.isSuccess())
                {
                    EquivalentComponentResponse resp = response.body();

                    if (resp.status.equals("success"))
                    {
                        try
                        {
                            /*
                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            equivalentRatesHm = new HashMap<>();

                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                equivalentRatesHm.put(key.split(":")[0], rates.get(key).toString());
                            }
                            */

                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            String btsToFait = "";

                            while (keys.hasNext())
                            {
                                String key = keys.next();

                                if (key.equals("BTS:" + faitCurrency)) {
                                    btsToFait = rates.get("BTS:" + faitCurrency).toString();
                                    break;
                                }
                            }

                            //HashMap hm = new HashMap();

                            if (!btsToFait.isEmpty())
                            {
                                keys = rates.keys();


                                while (keys.hasNext())
                                {
                                    String key = keys.next();

                                    if (!key.equals("BTS:" + faitCurrency))
                                    {
                                        String asset = key.split(":")[0];

                                        String assetConversionToBTS = rates.get(key).toString();

                                        double newConversionRate = convertLocalizeStringToDouble(assetConversionToBTS) * convertLocalizeStringToDouble(btsToFait);

                                        String assetToFaitConversion = Double.toString(newConversionRate);

                                        equivalentRatesHm.put(asset, assetToFaitConversion);
                                    }
                                }

                                if ( context == null ) return;
                                EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                                myFiatStorage.saveEqHM(faitCurrency,equivalentRatesHm);
                            }

                            if ( context == null ) return;
                            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                            equivalentRatesHm = myFiatStorage.getEqHM(faitCurrency);

                            if ( context == null ) return;
                            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fiat_exchange_rate_received));

                            decodeRecievedMemos();
                        }
                        catch (Exception e)
                        {
                            reTryGetIndirectEquivalentComponents(leftOvers,faitCurrency);
                        }
                    }
                    else
                    {
                        reTryGetIndirectEquivalentComponents(leftOvers,faitCurrency);
                    }
                }
                else
                {
                    reTryGetIndirectEquivalentComponents(leftOvers,faitCurrency);
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                reTryGetIndirectEquivalentComponents(leftOvers,faitCurrency);
            }
        });
    }

    private double convertLocalizeStringToDouble(String text) {
        double txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.doubleValue();
        } catch (Exception e)
        {
            try {
                if ( context == null ) return txtAmount_d;
                String language = Helper.fetchStringSharePref(context, context.getString(R.string.pref_language));
                Locale locale = new Locale(language);
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.doubleValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }
}
