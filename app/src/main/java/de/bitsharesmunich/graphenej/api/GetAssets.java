package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by hvarona on 12/12/16.
 */
public class GetAssets extends WebSocketAdapter {

    private ArrayList<String> assetName;
    private WitnessResponseListener mListener;

    public GetAssets(ArrayList<String> assetName, WitnessResponseListener listener) {
        this.assetName = assetName;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(assetName);
        ApiCall getAssetCall = new ApiCall(0, RPC.CALL_GET_ASSET, accountParams, "2.0", 1);
        websocket.sendText(getAssetCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type LookupAssetSymbolsResponse = new TypeToken<WitnessResponse<List<Asset>>>(){}.getType();
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        WitnessResponse<List<Asset>> witnessResponse = gsonBuilder.create().fromJson(response, LookupAssetSymbolsResponse);
        mListener.onSuccess(witnessResponse);
        websocket.disconnect();
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}