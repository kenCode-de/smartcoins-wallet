package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;


/**
 * Created by Syed Muhammad Muzzammil on 5/19/16.
 */
public class BalancesLoad{
    Context context;
    WebSocket socket;
    String getDetails="";
    BalancesDelegate balancesDelegate;

    public BalancesLoad(Context c,BalancesDelegate callbackClass){
        context = c;
        balancesDelegate = callbackClass;
        ButterKnife.setDebug(true);
    }
    void get_json_account_balances(String account_name,String id) {
        getDetails = "{\"id\":" + id + ",\"method\":\"get_named_account_balances\",\"params\":[\"" + account_name + "\",[]]}";
        bConnection();
    }
    void get_asset(ArrayList<String> asset, String id) {
        //{"id":1,"method":"get_assets","params":[["1.3.0","1.3.120"]]}
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ;i<asset.size();i++){
            stringBuilder.append(asset.get(i));
            if((i+1)<asset.size())
            stringBuilder.append("\",\"");
        }
        getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+stringBuilder.toString()+"\"]]}";
        bConnection();
    }
    void get_asset(String asset, String id) {
        getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[\""+asset+"\"]]}";
        bConnection();
    }
    void get_relative_account_history(String account_id, String id) {
        int history_id = Helper.fetchIntSharePref(context,context.getString(R.string.sharePref_history));
//"{\"id\":" + id + ",\"method\":\"call\",\"params\":[2,\"get_relative_account_history\",[\"" + account_id +"\",0,10,0]]}";
        getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":["+history_id+",\"get_relative_account_history\",[\""+account_id+"\",0,10,0]]}";
        bConnection();
    }
    void bConnection() {
        AsyncHttpGet get = new AsyncHttpGet(context.getString(R.string.url_bitshares_openledger));
        get.setTimeout(1000000000);
        AsyncHttpClient.getDefaultInstance().websocket(get, null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                socket = webSocket;
                loadInitialSocket();
            }

        });
    }
    void loadInitialSocket() {
        if (socket.isOpen()) {
            socket.send(getDetails);
            socket.setStringCallback(new WebSocket.StringCallback() {
                public void onStringAvailable(String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        int id = 0;
                        if (jsonObject.has("id")) {
                            id = jsonObject.getInt("id");
                        }
                        balancesDelegate.OnUpdate(s,id);
                    }catch (Exception json) {

                    }

                }
            });

        }
    }
}
