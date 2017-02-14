package de.bitsharesmunich.cryptocoincore.insightapi;

import java.util.Date;

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

    public GetTransactionData(String txid, GeneralCoinAccount account) {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin());
        this.account = account;
        this.txid = txid;
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
    }

    @Override
    public void run() {
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<Txi> txiCall = service.getTransaction(txid);
        txiCall.enqueue(this);
    }

    @Override
    public void onResponse(Call<Txi> call, Response<Txi> response) {
        if(response.isSuccessful()){
            Txi txi = response.body();

            GeneralTransaction transaction = new GeneralTransaction();
            transaction.setTxid(txi.txid);
            transaction.setBlock(txi.blockheight);
            transaction.setDate(new Date(txi.time));
            transaction.setFee(txi.fee*InsightApiConstants.amountMultiplier);
            transaction.setConfirm(txi.confirmations);
            transaction.setType(account.getCoin());

            for (Vin vin : txi.vin) {
                GIOTx input = new GIOTx();
                input.setAmount(vin.valueSat);
                input.setTransaction(transaction);
                input.setOut(true);
                input.setType(account.getCoin());
                String addr = vin.addr;
                input.setAddressString(addr);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        input.setAddress(address);
                        address.getOutputTransaction().add(input);
                    }
                }
                transaction.getTxInputs().add(input);
            }

            for (Vout vout : txi.vout){
                GIOTx output = new GIOTx();
                output.setAmount(vout.value*InsightApiConstants.amountMultiplier);
                output.setTransaction(transaction);
                output.setOut(false);
                output.setType(account.getCoin());
                String addr = vout.scriptPubKey.addresses[0];
                output.setAddressString(addr);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        output.setAddress(address);
                        address.getInputTransaction().add(output);
                    }
                }
                transaction.getTxOutputs().add(output);
            }
            account.balanceChange();
        }
    }

    @Override
    public void onFailure(Call<Txi> call, Throwable t) {

    }
}
