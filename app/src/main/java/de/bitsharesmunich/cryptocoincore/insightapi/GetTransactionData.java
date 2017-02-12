package de.bitsharesmunich.cryptocoincore.insightapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bitsharesmunich.cryptocoincore.base.GIOTx;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;

/**
 * Created by henry on 12/02/2017.
 */

public class GetTransactionData extends Thread {
    private final String urlQuery = "/insight-api/tx/";

    private final GeneralCoinAccount account;
    private final String serverUrl;

    public GetTransactionData(String server, int port, String txi, GeneralCoinAccount account) {
        serverUrl = "http://" + server + ":" + port + urlQuery + txi;
        this.account = account;
    }

    @Override
    public void run() {
        try {
            URLConnection connection = new URL(serverUrl).openConnection();
            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);
            String responseBody = scanner.useDelimiter("\\A").next();

            JSONObject transactionObject = new JSONObject(responseBody);
            GeneralTransaction transaction = new GeneralTransaction();
            transaction.setTxid(transactionObject.getString("txid"));
            transaction.setBlock(transactionObject.getLong("blockheight"));
            transaction.setDate(new Date(transactionObject.getLong("time")));
            transaction.setFee(transactionObject.getDouble("fees"));
            transaction.setConfirm(transactionObject.getInt("confirmations"));
            transaction.setType(account.getCoin());//TODO

            JSONArray vins = transactionObject.getJSONArray("vin");
            for (int j = 0; j < vins.length(); j++) {
                JSONObject vin = vins.getJSONObject(j);
                GIOTx input = new GIOTx();
                input.setAmount(vin.getDouble("value"));
                input.setTransaction(transaction);
                input.setOut(true);
                input.setType(account.getCoin());
                String addr = vin.getString("addr");
                input.setAddressString(addr);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        input.setAddress(address);
                        address.getOutputTransaction().add(input);
                    }
                }
                transaction.getTxInputs().add(input);
            }

            JSONArray vouts = transactionObject.getJSONArray("vout");
            for (int j = 0; j < vouts.length(); j++) {
                JSONObject vout = vouts.getJSONObject(j);
                GIOTx output = new GIOTx();
                output.setAmount(vout.getDouble("value"));
                output.setTransaction(transaction);
                output.setOut(false);
                output.setType(account.getCoin());
                String addr = vout.getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);
                output.setAddressString(addr);
                for (GeneralCoinAddress address : account.getAddresses()) {
                    if (address.getAddressString(account.getNetworkParam()).equals(addr)) {
                        output.setAddress(address);
                        address.getInputTransaction().add(output);
                    }
                }
                transaction.getTxOutputs().add(output);
            }
            //TODO notify account that balance change

        } catch (JSONException | IOException ex) {
            Logger.getLogger(GetTransactionByAddress.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
