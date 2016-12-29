package com.luminiasoft.bitshares.ws;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.Asset;
import com.luminiasoft.bitshares.RPC;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hvarona on 12/12/16.
 */
public class GetAssets extends WebSocketAdapter {

    static HashMap<String,AssetCache> assetCache;
    private ArrayList<String> assetName;
    private WitnessResponseListener mListener;

    public class AssetCache {
        public Asset asset;
        public Date date;

        public AssetCache(Asset asset, Date date){
            this.asset = asset;
            this.date = date;
        }
    }

    public static boolean inCache(ArrayList<String> assetName){
        Date now = new Date();

        for (String name : assetName){
            if (!GetAssets.assetCache.containsKey(assetName)){// if there's no asset in cache
                return false;
            } else {
                if (now.getTime() - GetAssets.assetCache.get(assetName).date.getTime() > 300000){ //if the cache date of the asset is too old, 300000 = 5 minutes
                    return false;
                }
            }
        }

        return true;
    }

    public static HashMap<String, Asset> getCache(ArrayList<String> assetName){
        HashMap<String, Asset> result = new HashMap<String, Asset>();

        for (String name : assetName) {
            result.put(name, GetAssets.assetCache.get(name).asset);
        }

        return result;
    }

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
        try {
            String response = frame.getPayloadText();
            Gson gson = new Gson();

            Type getAssetResponse = new TypeToken<WitnessResponse<ArrayList<Asset>>>() {
            }.getType();
            WitnessResponse<ArrayList<Asset>> witnessResponse = gson.fromJson(response, getAssetResponse);

            if (witnessResponse.error != null) {
                this.mListener.onError(witnessResponse.error);
            } else {
                //final HashMap<String, Asset> assets = new HashMap();
                if (witnessResponse.result.getClass() == ArrayList.class) {
                    Date now = new Date(); //This will be used for the cache timestamp

                    for (Object listObject : (ArrayList) witnessResponse.result) {
                        if (listObject != null) {
                            if (listObject.getClass() == Asset.class) {
                                Asset asset = (Asset) listObject;

                                //Entry for the assets cache
                                if (GetAssets.assetCache == null){GetAssets.assetCache = new HashMap<String,AssetCache>();}
                                GetAssets.assetCache.remove(asset.getSymbol());
                                GetAssets.assetCache.put(asset.getSymbol(), new GetAssets.AssetCache(asset, now));
                            }
                        }
                    }

                    this.mListener.onSuccess(witnessResponse);
                }
            }
        } catch (Exception e) {
            Log.d("henry", "exception e " + e.getMessage());
            e.printStackTrace();
        }

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