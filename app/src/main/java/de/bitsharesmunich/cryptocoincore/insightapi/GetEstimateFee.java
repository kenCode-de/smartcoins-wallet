package de.bitsharesmunich.cryptocoincore.insightapi;

import com.google.gson.JsonObject;

import java.io.IOException;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by henry on 27/03/2017.
 */

public abstract class GetEstimateFee {

    public static long getEstimateFee(final Coin coin) throws IOException {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(coin) + "/";
        InsightApiServiceGenerator serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<JsonObject> call = service.estimateFee(InsightApiConstants.getPath(coin));
        //JsonObject object = call.execute().body();
        final Object SYNC = new Object();
        final JsonObject answer = new JsonObject();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                synchronized (SYNC) {
                    answer.addProperty("answer", (long) (response.body().get("2").getAsDouble() * Math.pow(10, coin.getPrecision())));
                    SYNC.notifyAll();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                synchronized (SYNC) {
                    SYNC.notifyAll();
                }
            }
        });
        synchronized (SYNC){
            for(int i = 0; i < 6; i++) {
                try {
                    SYNC.wait(5000);
                } catch (InterruptedException e) {
                }
                if(answer.get("answer")!=null){
                    break;
                }
            }
        }
        if(answer.get("answer")==null){
            throw new IOException("");
        }
        return (long) (answer.get("answer").getAsDouble());
    }

}