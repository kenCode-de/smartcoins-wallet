package de.bitshares_munich.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by developer on 7/27/16.
 */
public class webSocketCallHelper {

    private Handler transactionsHandler;
    private boolean callInProgressForTransactions = false;
    private boolean callReceivedForTransactions = true;
    Context context;

    // 10 to 15 seconds are needed for the socket to connect
    // so a timeout interval of 20 seconds is best suited for a stable connection
    int time = 20000;

    public enum api_identifier {
        database,
        history,
        network,
        none
    }

    public webSocketCallHelper(Context _context)
    {
        transactionsHandler = new Handler(Looper.getMainLooper());
        context = _context;
    }

    public void cleanUpTransactionsHandler()
    {
        callInProgressForTransactions = false;
        callReceivedForTransactions = true;
        transactionsHandler.removeCallbacksAndMessages(null);
    }

    public void make_websocket_call(final String call_string_before_identifier, final String call_string_after_identifier,final api_identifier identifer)
    {
        transactionsHandler.removeCallbacksAndMessages(null);

        final Runnable checkifTransactionRecieved = new Runnable() {
            @Override
            public void run()
            {
                if ( callInProgressForTransactions && !callReceivedForTransactions && Application.isReady) // if balances are not returned in one second
                {
                    Application.disconnect();
                    make_websocket_call(call_string_before_identifier,call_string_after_identifier,identifer);
                }
                else if ( callInProgressForTransactions && !callReceivedForTransactions )
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
                    String call;

                    if ( identifer == api_identifier.database )
                    {
                        int database_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_database));
                        call = call_string_before_identifier + database_id + call_string_after_identifier;
                    }
                    else if ( identifer == api_identifier.history )
                    {
                        int history_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_history));
                        call = call_string_before_identifier + history_id + call_string_after_identifier;
                    }
                    else if ( identifer == api_identifier.network )
                    {
                        int nw_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_network_broadcast));
                        call = call_string_before_identifier + nw_id + call_string_after_identifier;
                    }
                    else
                    {
                        call = call_string_before_identifier;
                    }

                    callInProgressForTransactions = true;
                    callReceivedForTransactions = false;
                    Application.send(call);
                    transactionsHandler.postDelayed(checkifTransactionRecieved, time);
                }
                else
                {
                    make_websocket_call(call_string_before_identifier,call_string_after_identifier,identifer);
                }
            }
        };

        transactionsHandler.postDelayed(initiateTransactionsFetch,5);
    }
}
