package com.luminiasoft.bitshares.ws;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.AssetAmount;
import com.luminiasoft.bitshares.BlockData;
import com.luminiasoft.bitshares.RPC;
import com.luminiasoft.bitshares.Transaction;
import com.luminiasoft.bitshares.TransferTransactionBuilder;
import com.luminiasoft.bitshares.UserAccount;
import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import com.luminiasoft.bitshares.interfaces.TransactionBroadcastListener;
import com.luminiasoft.bitshares.models.ApiCall;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.DynamicGlobalProperties;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.bitcoinj.core.DumpedPrivateKey;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class that will handle the transaction publication procedure.
 */
public class TransactionBroadcastSequence extends WebSocketAdapter {
    private final String TAG = this.getClass().getName();

    private final static int LOGIN_ID = 1;
    private final static int GET_NETWORK_BROADCAST_ID = 2;
    private final static int GET_NETWORK_DYNAMIC_PARAMETERS = 3;
    private final static int BROADCAST_TRANSACTION = 4;

    public final static int EXPIRATION_TIME = 30;

    private String wif;
    private UserAccount source;
    private UserAccount destination;
    private AssetAmount transferred;
    private AssetAmount fee;
    private Transaction transaction;
    private TransactionBroadcastListener mListener;

    private int currentId = 1;
    private int broadcastApiId = -1;


    /**
     * Constructor of this class. The ids required
     * @param source: The source user account.
     * @param destination: The destination account.
     * @param transferred: The asset being transferred.
     * @param fee: The fee that is being charged.
     * @param listener: A class implementing the TransactionBroadcastListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public TransactionBroadcastSequence(String wif,
                                        UserAccount source,
                                        UserAccount destination,
                                        AssetAmount transferred,
                                        AssetAmount fee,
                                        TransactionBroadcastListener listener){
        this.mListener = listener;
        this.wif = wif;
        this.source = source;
        this.destination = destination;
        this.transferred = transferred;
        this.fee = fee;
    }

    public TransactionBroadcastSequence(Transaction transaction, TransactionBroadcastListener listener){
        this.transaction = transaction;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, "2.0", currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Log.d(TAG, "<< "+response);
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else{
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall networkApiIdCall = new ApiCall(1, RPC.CALL_NETWORK_BROADCAST, emptyParams, "2.0", currentId);
                websocket.sendText(networkApiIdCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_BROADCAST_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                broadcastApiId = witnessResponse.result;

                ApiCall getDynamicParametersCall = new ApiCall(0, RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES, emptyParams, "2.0", currentId);
                websocket.sendText(getDynamicParametersCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_DYNAMIC_PARAMETERS){
                Type DynamicGlobalPropertiesResponse = new TypeToken<WitnessResponse<DynamicGlobalProperties>>(){}.getType();
                WitnessResponse<DynamicGlobalProperties> witnessResponse = gson.fromJson(response, DynamicGlobalPropertiesResponse);
                DynamicGlobalProperties dynamicProperties = witnessResponse.result;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = dateFormat.parse(dynamicProperties.time);

                // Obtained block data
                long expirationTime = (date.getTime() / 1000) + EXPIRATION_TIME;
                String headBlockId = dynamicProperties.head_block_id;
                long headBlockNumber = dynamicProperties.head_block_number;

                try{
                    if(this.transaction == null){
                        transaction = new TransferTransactionBuilder()
                                .setSource(this.source)
                                .setDestination(this.destination)
                                .setAmount(this.transferred)
                                .setFee(this.fee)
                                .setBlockData(new BlockData(headBlockNumber, headBlockId, expirationTime))
                                .setPrivateKey(DumpedPrivateKey.fromBase58(null, this.wif).getKey())
                                .build();
                    }

                    ArrayList<Serializable> transactionList = new ArrayList<>();
                    transactionList.add(transaction);
                    ApiCall call = new ApiCall(broadcastApiId,
                            RPC.CALL_BROADCAST_TRANSACTION,
                            transactionList,
                            "2.0",
                            currentId);
                    String jsonCall = call.toJsonString();

                    // Finally sending transaction
                    websocket.sendText(jsonCall);
                }catch(MalformedTransactionException e){
                    mListener.onError(new BaseResponse.Error(e.getMessage()));
                }
            }else if(baseResponse.id == BROADCAST_TRANSACTION){
                Type WitnessResponseType = new TypeToken<WitnessResponse<String>>(){}.getType();
                WitnessResponse<WitnessResponse<String>> witnessResponse = gson.fromJson(response, WitnessResponseType);
                if(witnessResponse.result == null){
                    mListener.onSuccess();
                }else{
                    mListener.onError(witnessResponse.error);
                }
                websocket.disconnect();
            }
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.d(TAG,"onError");
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        Log.d(TAG,"handleCallbackError");
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.d(TAG, ">> "+frame.getPayloadText());
    }
}