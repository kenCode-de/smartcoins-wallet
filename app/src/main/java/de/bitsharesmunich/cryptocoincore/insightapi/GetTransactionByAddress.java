package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.GTxIO;
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
 * Get all the transaction data of the addresses of an account
 *
 */

public class GetTransactionByAddress extends Thread implements Callback<AddressTxi> {
    /**
     * The account to be query
     */
    private GeneralCoinAccount account;
    /**
     * The list of address to query
     */
    private List<GeneralCoinAddress> addresses = new ArrayList<>();
    /**
     * The serviceGenerator to call
     */
    private InsightApiServiceGenerator serviceGenerator;
    /**
     * This app context, used to save on the DB
     */
    private Context context;


    /**
     * Basic consturcotr
     * @param account The account to be query
     * @param context This app context
     */
    public GetTransactionByAddress(GeneralCoinAccount account, Context context) {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) +"/";
        this.account = account;
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
    }

    /**
     * add an address to be query
     * @param address the address to be query
     */
    public void addAddress(GeneralCoinAddress address) {
        addresses.add(address);
    }

    @Override
    public void onResponse(Call<AddressTxi> call, Response<AddressTxi> response) {
        if (response.isSuccessful()) {
            boolean changed = false;
            AddressTxi addressTxi = response.body();

            for (Txi txi : addressTxi.items) {
                GeneralCoinAccount tempAccount = null;
                GeneralTransaction transaction = new GeneralTransaction();
                transaction.setAccount(this.account);
                transaction.setTxid(txi.txid);
                transaction.setBlock(txi.blockheight);
                transaction.setDate(new Date(txi.time * 1000));
                transaction.setFee((long) (txi.fee * Math.pow(10,account.getCoin().getPrecision())));
                transaction.setConfirm(txi.confirmations);
                transaction.setType(account.getCoin());
                transaction.setBlockHeight(txi.blockheight);

                for (Vin vin : txi.vin) {
                    GTxIO input = new GTxIO();
                    input.setAmount((long) (vin.value * Math.pow(10,account.getCoin().getPrecision())));
                    input.setTransaction(transaction);
                    input.setOut(true);
                    input.setType(account.getCoin());
                    String addr = vin.addr;
                    input.setAddressString(addr);
                    input.setIndex(vin.n);
                    input.setScriptHex(vin.scriptSig.hex);
                    input.setOriginalTxid(vin.txid);
                    for (GeneralCoinAddress address : addresses) {
                        if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                            input.setAddress(address);
                            tempAccount = address.getAccount();

                            if (!address.hasTransactionOutput(input, account.getNetworkParam())) {
                                address.getTransactionOutput().add(input);
                            }
                            changed = true;
                        }
                    }
                    transaction.getTxInputs().add(input);
                }

                for (Vout vout : txi.vout) {
                    if(vout.scriptPubKey.addresses == null || vout.scriptPubKey.addresses.length <= 0){
                        // The address is null, this must be a memo
                        String hex = vout.scriptPubKey.hex;
                        int opReturnIndex = hex.indexOf("6a");
                        if(opReturnIndex >= 0) {
                            byte[] memoBytes = new byte[Integer.parseInt(hex.substring(opReturnIndex+2,opReturnIndex+4),16)];
                            for(int i = 0; i < memoBytes.length;i++){
                                memoBytes[i] = Byte.parseByte(hex.substring(opReturnIndex+4+(i*2),opReturnIndex+6+(i*2)),16);
                            }
                            transaction.setMemo(new String(memoBytes));
                        }
                    }else {
                        GTxIO output = new GTxIO();
                        output.setAmount((long) (vout.value * Math.pow(10, account.getCoin().getPrecision())));
                        output.setTransaction(transaction);
                        output.setOut(false);
                        output.setType(account.getCoin());
                        String addr = vout.scriptPubKey.addresses[0];
                        output.setAddressString(addr);
                        output.setIndex(vout.n);
                        output.setScriptHex(vout.scriptPubKey.hex);
                        for (GeneralCoinAddress address : addresses) {
                            if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                                output.setAddress(address);
                                tempAccount = address.getAccount();

                                if (!address.hasTransactionInput(output, account.getNetworkParam())) {
                                    address.getTransactionInput().add(output);
                                }
                                changed = true;
                            }
                        }

                        transaction.getTxOutputs().add(output);
                    }
                }
                SCWallDatabase db = new SCWallDatabase(this.context);
                long idTransaction = db.getGeneralTransactionId(transaction);
                if (idTransaction == -1) {
                    db.putGeneralTransaction(transaction);
                } else {
                    transaction.setId(idTransaction);
                    db.updateGeneralTransaction(transaction);
                }
                if (tempAccount != null && transaction.getConfirm() < account.getCoin().getConfirmationsNeeded()) {
                    new GetTransactionData(transaction.getTxid(), tempAccount, context, true).start();
                }
                for (GeneralCoinAddress address : addresses) {
                    if (address.updateTransaction(transaction)) {
                        break;
                    }
                }
            }
            if(changed) {
                account.balanceChange();
            }
        }
    }

    @Override
    public void onFailure(Call<AddressTxi> call, Throwable t) {
        Log.e("GetTransactionByAddress", "Error in json format");
    }

    @Override
    public void run() {
        if (addresses.size() > 0) {
            StringBuilder addressToQuery = new StringBuilder();
            for (GeneralCoinAddress address : addresses) {
                addressToQuery.append(address.getAddressString(account.getNetworkParam())).append(",");
            }
            addressToQuery.deleteCharAt(addressToQuery.length() - 1);
            InsightApiService service = serviceGenerator.getService(InsightApiService.class);
            Call<AddressTxi> addressTxiCall = service.getTransactionByAddress(InsightApiConstants.getPath(account.getCoin()),addressToQuery.toString());
            addressTxiCall.enqueue(this);
        }
    }
}
