package com.luminiasoft.bitshares.ws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.Asset;
import com.luminiasoft.bitshares.AssetAmount;
import com.luminiasoft.bitshares.BaseOperation;
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
public class GetRequiredFees extends WebSocketAdapter {

    private OnDatabaseQueryListener mListener;
    private List<BaseOperation> operations;
    private Asset asset;

    public GetRequiredFees(List<BaseOperation> operations, Asset asset, OnDatabaseQueryListener listener){
        this.operations = operations;
        this.asset = asset;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.addAll(this.operations);
        accountParams.add(this.asset.getObjectId());
        ApiCall getRequiredFees = new ApiCall(0, "get_required_fees", accountParams, "2.0", 1);
        websocket.sendText(getRequiredFees.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onResult(response);
        }else{
            mListener.onError(response);
        }

        //TODO: Maybe it would be better if the OnDatabaseQueryListener could take parsed objects instead of a raw String
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetDeserializer());
        AssetAmount assetAmount = gsonBuilder.create().fromJson(response, AssetAmount.class);
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
