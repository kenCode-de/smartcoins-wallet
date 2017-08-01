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
    private String mRawTx;
    /**
     * The serviceGenerator to call
     */
    private InsightApiServiceGenerator mServiceGenerator;
    /**
     * This app context, used to save on the DB
     */
    private Context mContext;
    /**
     * The account who sign the transaction
     */
    private GeneralCoinAccount mAccount;

    /**
     * Basic Consturctor
     * @param RawTx The RawTX in Hex String
     * @param account The account who signs the transaction
     * @param context This app context
     */
    public BroadcastTransaction(String RawTx, GeneralCoinAccount account, Context context){
        String serverUrl = InsightApiConstants.sProtocol + "://" + InsightApiConstants.getAddress(account.getCoin()) +"/";
        this.mServiceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.mContext = context;
        this.mRawTx = RawTx;
        this.mAccount = account;
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
            GetTransactionData trData = new GetTransactionData(response.body().txid,this.mAccount,this.mContext);
            trData.start();
        } else {
            System.out.println("SENDTEST: not succesful " + response.message());
            //TODO change how to handle invalid transaction
        }
    }

    /**
     * Handles the failures of the call
     */
    @Override
    public void onFailure(Call<Txi> call, Throwable t) {
        //TODO change how to handle invalid transaction
        System.out.println("SENDTEST: sendError " + t.getMessage() );
    }

    /**
     * Starts the call of the service
     */
    @Override
    public void run() {
        InsightApiService service = this.mServiceGenerator.getService(InsightApiService.class);
        Call<Txi> broadcastTransaction = service.broadcastTransaction(InsightApiConstants.getPath(this.mAccount.getCoin()),this.mRawTx);
        broadcastTransaction.enqueue(this);
    }
}
