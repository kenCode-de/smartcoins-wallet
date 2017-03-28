package de.bitsharesmunich.cryptocoincore.insightapi;

import com.google.gson.JsonObject;

import java.io.IOException;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import retrofit2.Call;

/**
 * Created by henry on 27/03/2017.
 */

public abstract class GetEstimateFee {

    public static long getEstimateFee(Coin coin) throws IOException {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(coin) + "/";
        InsightApiServiceGenerator serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<JsonObject> call = service.estimateFee(InsightApiConstants.getPath(coin));
        JsonObject object = call.execute().body();
        return (long) (object.get("2").getAsDouble()*Math.pow(10, coin.getPrecision()));
    }

}