package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by henry on 20/02/2017.
 */

public class BroadcastTransaction extends Thread implements Callback<Txi> {

    private String rawtx;
    private InsightApiServiceGenerator serviceGenerator;
    private Context context;
    private GeneralCoinAccount account;

    public BroadcastTransaction(String rawtx, GeneralCoinAccount account, Context context){
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin());
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
        this.rawtx = rawtx;
        this.account = account;
    }

    @Override
    public void onResponse(Call<Txi> call, Response<Txi> response) {
        if (response.isSuccessful()) {
            System.out.println("SENDTEST: sendSuccesfull " + response.body().txid );
            //TODO invalidated send
            //TODO call getTransactionData
            GetTransactionData trData = new GetTransactionData(response.body().txid,account,context);
            trData.start();
        } else {
            System.out.println("SENDTEST: not succesful " + response.message());
            //TODO invalid transaction
        }
    }

    @Override
    public void onFailure(Call<Txi> call, Throwable t) {
        //TODO invalid transaction
        System.out.println("SENDTEST: sendError " + t.getMessage() );
    }

    @Override
    public void run() {
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<Txi> broadcastTransaction = service.broadcastTransaction(this.rawtx);
        broadcastTransaction.enqueue(this);
    }
}
