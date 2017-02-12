package de.bitsharesmunich.cryptocoincore.insightapi;

import org.bitcoinj.core.NetworkParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GIOTx;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;

/**
 * Created by henry on 12/02/2017.
 */

public class GetTransactionByAddress extends Thread {

    private final String urlQuery = "/insight-api/addrs/";
    private final String urlPostQuery = "/txs/";
    private Coin coin;
    private NetworkParameters param;
    private List<GeneralCoinAddress> addresses = new ArrayList();

    private String serverUrl;


    public GetTransactionByAddress(String server, int port, NetworkParameters param) {
        serverUrl = "http://" + server + ":" + port + urlQuery;
        this.param = param;
    }

    public void addAdress(GeneralCoinAddress address) {
        addresses.add(address);
    }

    @Override
    public void run() {
        if (addresses.size() > 0) {
            Set<GeneralCoinAccount> accountsChanged = new HashSet();
            try {
                for (GeneralCoinAddress address : addresses) {
                    serverUrl += address.getAddressString(param) + ",";
                }
                serverUrl = serverUrl.substring(0, serverUrl.length() - 1) + urlPostQuery;
                URLConnection connection = new URL(serverUrl).openConnection();
                InputStream response = connection.getInputStream();
                Scanner scanner = new Scanner(response);
                String responseBody = scanner.useDelimiter("\\A").next();
                System.out.println(responseBody);
                JSONObject responseObject = new JSONObject(responseBody);
                JSONArray items = responseObject.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject transactionObject = items.getJSONObject(i);
                    GeneralTransaction transaction = new GeneralTransaction();
                    transaction.setTxid(transactionObject.getString("txid"));
                    transaction.setBlock(transactionObject.getLong("blockheight"));
                    transaction.setDate(new Date(transactionObject.getLong("time")));
                    transaction.setFee(transactionObject.getDouble("fees"));
                    transaction.setConfirm(transactionObject.getInt("confirmations"));
                    transaction.setType(coin);

                    JSONArray vins = transactionObject.getJSONArray("vin");
                    for (int j = 0; j < vins.length(); j++) {
                        JSONObject vin = vins.getJSONObject(j);
                        GIOTx input = new GIOTx();
                        input.setAmount(vin.getDouble("value"));
                        input.setTransaction(transaction);
                        input.setOut(true);
                        input.setType(Coin.BITCOIN);
                        String addr = vin.getString("addr");
                        input.setAddressString(addr);
                        for (GeneralCoinAddress address : addresses) {
                            if (address.getAddressString(param).equals(addr)) {
                                input.setAddress(address);
                                address.getOutputTransaction().add(input);
                                accountsChanged.add(address.getAccount());
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
                        output.setType(Coin.BITCOIN);
                        String addr = vout.getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);
                        output.setAddressString(addr);
                        for (GeneralCoinAddress address : addresses) {
                            if (address.getAddressString(param).equals(addr)) {
                                output.setAddress(address);
                                address.getInputTransaction().add(output);
                                accountsChanged.add(address.getAccount());
                            }
                        }
                        transaction.getTxOutputs().add(output);
                    }
                }

                for(GeneralCoinAccount account : accountsChanged){
                    account.balanceChange();
                }

            } catch (JSONException | IOException ex) {
                Logger.getLogger(GetTransactionByAddress.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
