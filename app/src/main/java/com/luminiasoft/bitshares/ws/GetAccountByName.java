package com.luminiasoft.bitshares.ws;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;
import com.luminiasoft.bitshares.interfaces.OnDatabaseQueryListener;
import com.luminiasoft.bitshares.models.AccountProperties;
import com.luminiasoft.bitshares.models.ApiCall;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 11/15/16.
 */
public class GetAccountByName extends WebSocketAdapter {

    private String accountName;
    private OnDatabaseQueryListener mListener;

    public GetAccountByName(String accountName, OnDatabaseQueryListener listener){
        this.accountName = accountName;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        ApiCall getAccountByName = new ApiCall(0, "get_account_by_name", accountParams, "2.0", 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Gson gson = new Gson();

        Type WitnessResponseType = new TypeToken<AccountProperties>(){}.getType();
        WitnessResponse<AccountProperties> witnessResponse = gson.fromJson(response, WitnessResponseType);

        if(witnessResponse.error != null){
            this.mListener.onError(witnessResponse.error.data.message);
        }else{
            this.mListener.onResult(witnessResponse.result.id);
        }
        websocket.disconnect();
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(cause.getMessage());
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(cause.getMessage());
        websocket.disconnect();
    }
}
