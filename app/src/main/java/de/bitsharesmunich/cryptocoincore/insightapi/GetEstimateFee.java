package de.bitsharesmunich.cryptocoincore.insightapi;

import com.google.gson.JsonObject;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

/**
 * Get the estimete fee amount from an insight api server.
 * This class gets the rate of the fee for a giving coin in about to block for a transaction to be
 * confirmated.
 *
 * This ammount is giving as amount of currency / kbytes,  as example btc / kbytes
 *
  */

public abstract class GetEstimateFee {

    //TODO add a funciton to get the rate of a specific port

    /**
     * The funciton to get the rate for the transaction be included in the next 2 blocks
     * @param coin The coin to get the rate
     * @return The rate number (coin/kbytes)
     * @throws IOException If the server answer null, or the rate couldn't be calculated
     */
    public static long getEstimateFee(final Coin coin) throws IOException {
        String serverUrl = InsightApiConstants.sProtocol + "://"
                + InsightApiConstants.getAddress(coin) + "/";
        InsightApiServiceGenerator serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<JsonObject> call = service.estimateFee(InsightApiConstants.getPath(coin));
        final Object SYNC = new Object();
        final JsonObject answer = new JsonObject();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                synchronized (SYNC) {
                    answer.addProperty("answer",
                            (long) (response.body().get("2").getAsDouble()* Math.pow(10, coin.getPrecision())));
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
                    // this interruption never rises
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