package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import android.util.Log;

import org.bitcoinj.core.NetworkParameters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GIOTx;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.insightapi.models.AddressTxi;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Txi;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Vin;
import de.bitsharesmunich.cryptocoincore.insightapi.models.Vout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by henry on 12/02/2017.
 */

public class GetTransactionByAddress extends Thread implements Callback<AddressTxi> {



    private Coin coin;
    private NetworkParameters param;
    private List<GeneralCoinAddress> addresses = new ArrayList();
    private InsightApiServiceGenerator serviceGenerator;
    private Context context;


    public GetTransactionByAddress(NetworkParameters param, Coin coin, Context context) {

        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(coin) + ":" + InsightApiConstants.getPort(coin);
        this.param = param;
        this.coin = coin;
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
    }

    public void addAddress(GeneralCoinAddress address) {
        addresses.add(address);
    }

    @Override
    public void onResponse(Call<AddressTxi> call, Response<AddressTxi> response) {
        if(response.isSuccessful()){
            HashSet<GeneralCoinAccount> accountsChanged = new HashSet();
            AddressTxi addressTxi = response.body();

            for (Txi txi : addressTxi.items) {
                GeneralTransaction transaction = new GeneralTransaction();
                transaction.setTxid(txi.txid);
                transaction.setBlock(txi.blockheight);
                transaction.setDate(new Date(txi.time));
                transaction.setFee((long)(txi.fee*InsightApiConstants.amountMultiplier));
                transaction.setConfirm(txi.confirmations);
                transaction.setType(coin);
                for (Vin vin : txi.vin) {
                    GIOTx input = new GIOTx();
                    input.setAmount(vin.valueSat);
                    input.setTransaction(transaction);
                    input.setOut(true);
                    input.setType(coin);
                    String addr = vin.addr;
                    input.setAddressString(addr);
                    for (GeneralCoinAddress address : addresses) {
                        if (address.getAddressString(param).equals(addr)) {
                            input.setAddress(address);

                            if (!address.hasOutputTransaction(input, this.param)) {
                                address.getOutputTransaction().add(input);
                                accountsChanged.add(address.getAccount());
                            }
                        }
                    }
                    transaction.getTxInputs().add(input);
                }

                for (Vout vout : txi.vout){
                    GIOTx output = new GIOTx();
                    output.setAmount((long)(vout.value*InsightApiConstants.amountMultiplier));
                    output.setTransaction(transaction);
                    output.setOut(false);
                    output.setType(coin);
                    String addr = vout.scriptPubKey.addresses[0];
                    output.setAddressString(addr);
                    for (GeneralCoinAddress address : addresses) {
                        if (address.getAddressString(param).equals(addr)) {
                            output.setAddress(address);

                            if (!address.hasInputTransaction(output, this.param)) {
                                address.getInputTransaction().add(output);
                                accountsChanged.add(address.getAccount());
                            }
                        }
                    }

                    transaction.getTxOutputs().add(output);
                }
                SCWallDatabase db = new SCWallDatabase(this.context);
                String idTransaction =db.getGeneralTransactionId(transaction);
                if(idTransaction == null) {
                    db.putGeneralTransaction(transaction);
                }else{
                    transaction.setId(idTransaction);
                    db.updateGeneralTransaction(transaction);
                }
            }

            for(GeneralCoinAccount account : accountsChanged){
                account.balanceChange();
            }
        }
    }

    @Override
    public void onFailure(Call<AddressTxi> call, Throwable t) {
        Log.e("GetTransactionByAddress","Error in json format");
    }

    @Override
    public void run() {
        if (addresses.size() > 0) {
                StringBuilder addressToQuery = new StringBuilder();
                for (GeneralCoinAddress address : addresses) {
                    addressToQuery.append(address.getAddressString(param)).append(",");
                }
                addressToQuery.deleteCharAt(addressToQuery.length()-1);
            InsightApiService service = serviceGenerator.getService(InsightApiService.class);
            Call<AddressTxi> addressTxiCall = service.getTransactionByAddress(addressToQuery.toString());
            addressTxiCall.enqueue(this);
        }
    }
}
