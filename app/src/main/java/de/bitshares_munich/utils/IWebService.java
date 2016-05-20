package de.bitshares_munich.utils;

import java.util.Map;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.QrHash;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IWebService {


   /* @Headers({"Content-Type: application/json"})
    @POST("/get_qr_hash/")
    Call<QrHash> getQrHash(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/ws/")
    Call<AccountBlock> verifyAccount(@Body Map<String, Object> params);*/

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<AccountDetails> getAccount(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/get_qr_hash_json")
    Call<QrHash> getQrHash(@Body Map<String, String> params);

}
