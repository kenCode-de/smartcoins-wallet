package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Broadcast a transaction, using the InsightApi
 *
 */

public class BroadcastTransaction extends Thread implements Callback<Txi> {
    /**
     * The rawTX as Hex String
     */
    private String rawtx;
    /**
     * The serviceGenerator to call
     */
    private InsightApiServiceGenerator serviceGenerator;
    /**
     * This app context, used to save on the DB
     */
    private Context context;
    /**
     * The account who sign the transaction
     */
    private GeneralCoinAccount account;

    /**
     * Basic Consturctor
     * @param rawtx The RawTX in Hex String
     * @param account The account who signs the transaction
     * @param context This app context
     */
    public BroadcastTransaction(String rawtx, GeneralCoinAccount account, Context context){
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) +"/";
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
        this.rawtx = rawtx;
        this.account = account;
    }

    /**
     * Handles the response of the call
     *
     */
    @Override
    public void onResponse(Call<Txi> call, Response<Txi> response) {
        if (response.isSuccessful()) {
            //TODO invalidated send
            //TODO call getTransactionData
            GetTransactionData trData = new GetTransactionData(response.body().txid,account,context);
            trData.start();
        } else {
            System.out.println("SENDTEST: not succesful " + response.message());
            //TODO invalid transaction
        }
    }

    /**
     * Handles the failures of the call
     */
    @Override
    public void onFailure(Call<Txi> call, Throwable t) {
        //TODO invalid transaction
        System.out.println("SENDTEST: sendError " + t.getMessage() );
    }

    /**
     * Starts the call of the service
     */
    @Override
    public void run() {
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<Txi> broadcastTransaction = service.broadcastTransaction(InsightApiConstants.getPath(account.getCoin()),this.rawtx);
        broadcastTransaction.enqueue(this);
    }
}
