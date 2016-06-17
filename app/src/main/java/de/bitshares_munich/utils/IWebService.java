package de.bitshares_munich.utils;

import java.util.Map;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.DecodeMemo;
import de.bitshares_munich.models.GenerateKeys;
import de.bitshares_munich.models.QrHash;
import de.bitshares_munich.models.QrJson;
import de.bitshares_munich.models.RegisterAccount;
import de.bitshares_munich.models.TradeResponse;
import de.bitshares_munich.models.TransactionSmartCoin;
import de.bitshares_munich.models.TransferResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IWebService {

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<AccountDetails> getAccount(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/get_qr_hash/")
    Call<QrHash> getQrHash(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/get_qr_hash_w_note/")
    Call<QrHash> getQrHashWithNote(@Body Map<String, String> params);

    @GET("/get_transactions/{accountId}/{orderId}")
    Call<TransactionSmartCoin[]> getTransactionSmartCoin(@Path("accountId") String accountId, @Path("orderId") String orderId);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<TransferResponse> getTransferResponse(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<DecodeMemo> getDecodedMemo(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<GenerateKeys> getGeneratedKeys(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/api/v1/accounts")
    Call<RegisterAccount> getReg(@Body Map<String, Object> params);

    @GET
    Call<Void> sendCallback(@Url String urlSubString, @Query("block") String block, @Query("trx") String trx);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<TradeResponse> getTradeResponse(@Body Map<String, String> params);

    @GET("/get_json_for_hash/")
    Call<QrJson> getJson(@Query("hash") String hash);
}
