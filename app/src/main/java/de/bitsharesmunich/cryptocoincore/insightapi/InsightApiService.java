package de.bitsharesmunich.cryptocoincore.insightapi;

import de.bitsharesmunich.cryptocoincore.insightapi.models.AddressTxi;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by henry on 13/02/2017.
 */

public interface InsightApiService {

    @GET("/insight-api/tx/{txid}")
    Call<Txi> getTransaction(@Path(value = "txid", encoded = true) String txid);

    @GET("/insight-api/addrs/{addrs}/txs")
    Call<AddressTxi> getTransactionByAddress(@Path(value = "addrs", encoded = true) String addrs);

    @FormUrlEncoded
    @POST("/insight-api/tx/send")
    Call<Txi> broadcastTransaction(@Field("rawtx") String rawtx);

}
