package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import java.util.Date;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.GTxIO;
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
    /**
     * The account to be query
     */
    private final GeneralCoinAccount account;
    /**
     * The transaction txid to be query
     */
    private String txid;
    /**
     * The serviceGenerator to call
     */
    private InsightApiServiceGenerator serviceGenerator;
    /**
     * This app context, used to save on the DB
     */
    private Context context;
    /**
     * If has to wait for another confirmation
     */
    private boolean mustWait = false;

    /**
     * Constructor used to query for a transaction with unknown confirmations
     * @param txid The txid of the transaciton to be query
     * @param account The account to be query
     * @param context This app Context
     */
    public GetTransactionData(String txid, GeneralCoinAccount account, Context context) {
        this(txid, account, context, false);
    }

    /**
     * Consturctor to be used qhen the confirmations of the transaction are known
     * @param txid The txid of the transaciton to be query
     * @param account The account to be query
     * @param context This app Context
     * @param mustWait If there is less confirmation that needed
     */
    public GetTransactionData(String txid, GeneralCoinAccount account, Context context, boolean mustWait) {
        String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) +"/";
        this.account = account;
        this.txid = txid;
        serviceGenerator = new InsightApiServiceGenerator(serverUrl);
        this.context = context;
        this.mustWait = mustWait;
    }

    /**
     * Function to start the insight api call
     */
    @Override
    public void run() {
        if (mustWait) {
            try {
                Thread.sleep(InsightApiConstants.WAIT_TIME);
            } catch (InterruptedException ignored) {
            }
        }
        InsightApiService service = serviceGenerator.getService(InsightApiService.class);
        Call<Txi> txiCall = service.getTransaction(InsightApiConstants.getPath(account.getCoin()),txid);
        txiCall.enqueue(this);
    }

    @Override
    public void onResponse(Call<Txi> call, Response<Txi> response) {
        if (response.isSuccessful()) {
            Txi txi = response.body();

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
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        input.setAddress(address);
                        if (!address.hasTransactionOutput(input, account.getNetworkParam())) {
                            address.getTransactionOutput().add(input);
                        }
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
                        System.out.println("Memo read : " + transaction.getMemo());
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
                    for (GeneralCoinAddress address : account.getAddresses()) {
                        if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                            output.setAddress(address);
                            if (!address.hasTransactionInput(output, account.getNetworkParam())) {
                                address.getTransactionInput().add(output);
                            }
                        }
                    }
                    transaction.getTxOutputs().add(output);
                }
            }

            if(txi.txlock && txi.confirmations< account.getCoin().getConfirmationsNeeded()){
                transaction.setConfirm(account.getCoin().getConfirmationsNeeded());
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
