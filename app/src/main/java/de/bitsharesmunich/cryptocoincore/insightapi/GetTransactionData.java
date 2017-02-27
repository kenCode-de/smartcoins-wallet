package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;
import android.util.Log;

import java.util.Date;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.GIOTx;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Vin;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Vout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by henry on 12/02/2017.
 */

public class GetTransactionData extends Thread implements Callback<Txi> {

    private final GeneralCoinAccount account;
    private String txid;
    private InsightApiServiceGenerator serviceGenerator;
    private Context context;
    private boolean mustWait = false;

    public GetTransactionData(String txid, GeneralCoinAccount account, Context context) {
        this(txid, account, context, false);
    }

    public GetTransactionData(String txid, GeneralCoinAccount account, Context context, boolean mustWait) {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin());
        this.account = account;
        this.txid = txid;
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
        this.mustWait = mustWait;
    }

    @Override
    public void run() {
        if (mustWait) {
            try {
                Thread.sleep(InsightApiConstants.WAIT_TIME);
            } catch (InterruptedException ignored) {
            }
        }
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<Txi> txiCall = service.getTransaction(txid);
        txiCall.enqueue(this);
    }

    @Override
    public void onResponse(Call<Txi> call, Response<Txi> response) {
        if (response.isSuccessful()) {
            Txi txi = response.body();

            GeneralTransaction transaction = new GeneralTransaction();
            transaction.setTxid(txi.txid);
            transaction.setBlock(txi.blockheight);
            transaction.setDate(new Date(txi.time * 1000));
            transaction.setFee((long) (txi.fee * InsightApiConstants.amountMultiplier));
            transaction.setConfirm(txi.confirmations);
            transaction.setType(account.getCoin());
            transaction.setBlockHeight(txi.blockheight);

            for (Vin vin : txi.vin) {
                GIOTx input = new GIOTx();
                input.setAmount(vin.valueSat);
                input.setTransaction(transaction);
                input.setOut(true);
                input.setType(account.getCoin());
                String addr = vin.addr;
                input.setAddressString(addr);
                input.setIndex(vin.n);
                input.setScriptHex(vin.scriptSig.hex);
                input.setOriginalTxid(vin.txid);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        input.setAddress(address);
                        if (!address.hasOutputTransaction(input, account.getNetworkParam())) {
                            address.getOutputTransaction().add(input);
                        }
                    }
                }
                transaction.getTxInputs().add(input);
            }

            for (Vout vout : txi.vout) {
                GIOTx output = new GIOTx();
                output.setAmount((long) (vout.value * InsightApiConstants.amountMultiplier));
                output.setTransaction(transaction);
                output.setOut(false);
                output.setType(account.getCoin());
                String addr = vout.scriptPubKey.addresses[0];
                output.setAddressString(addr);
                output.setIndex(vout.n);
                output.setScriptHex(vout.scriptPubKey.hex);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        output.setAddress(address);
                        if (!address.hasInputTransaction(output, account.getNetworkParam())) {
                            address.getInputTransaction().add(output);
                        }
                    }
                }
                transaction.getTxOutputs().add(output);
            }

            SCWallDatabase db = new SCWallDatabase(this.context);
            long idTransaction = db.getGeneralTransactionId(transaction);
            if (idTransaction == -1) {
                db.putGeneralTransaction(transaction);
            } else {
                transaction.setId(idTransaction);
                db.updateGeneralTransaction(transaction);
            }
            account.updateTransaction(transaction);
            account.balanceChange();
            if (transaction.getConfirm() < account.getCoin().getConfirmationsNeeded()) {
                new GetTransactionData(txid, account, context, true).start();
            }
        }
    }

    @Override
    public void onFailure(Call<Txi> call, Throwable t) {

    }
}
