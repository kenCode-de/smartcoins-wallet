package de.bitsharesmunich.cryptocoincore.insightapi;

import com.google.gson.JsonObject;

import de.bitsharesmunich.cryptocoincore.insightapi.models.AddressTxi;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Holds each call to the insigh api server
 * Created by henry on 13/02/2017.
 */

interface InsightApiService {

    /**
     * The query for the info of a single transaction
     * @param path The path of the insight api without the server address
     * @param txid the transasction to be query
     */
    @GET("{path}/tx/{txid}")
    Call<Txi> getTransaction(@Path(value = "path", encoded = true) String path, @Path(value = "txid", encoded = true) String txid);

    /**
     * The query for the transasctions of multiples addresses
     * @param path     The path of the insight api without the server address
     * @param addrs the addresses to be query each separated with a ","
     */
    @GET("{path}/addrs/{addrs}/txs")
    Call<AddressTxi> getTransactionByAddress(@Path(value = "path", encoded = true) String path, @Path(value = "addrs", encoded = true) String addrs);

    /**
     * Broadcast Transaction
     * @param path The path of the insight api without the server address
     * @param rawtx the rawtx to send in Hex String
     */
    @FormUrlEncoded
    @POST("{path}/tx/send")
    Call<Txi> broadcastTransaction(@Path(value = "path", encoded = true) String path, @Field("rawtx") String rawtx);

    @GET("{path}/utils/estimatefee?nbBlocks=2")
    Call<JsonObject> estimateFee(@Path(value = "path", encoded = true) String path);

}
