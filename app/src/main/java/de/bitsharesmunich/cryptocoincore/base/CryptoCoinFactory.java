package de.bitsharesmunich.cryptocoincore.base;

import com.google.gson.JsonObject;

import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinManager;
import de.bitsharesmunich.cryptocoincore.dash.DashManager;

/**
 * Created by henry on 05/02/2017.
 */

public class CryptoCoinFactory {


    public static GeneralCoinManager getGeneralCoinManager(Coin coin){
        switch(coin){
            case BITCOIN:
                return BitcoinManager.getInstance();
        }

        return null;
    }

    public static CryptoCoinAccount getAccountFromJson(JsonObject accountObject, AccountSeed seed){
        Coin coin = Coin.valueOf(accountObject.get("type").getAsString());
        String name = accountObject.get("name").getAsString();
        switch(coin){
            case BITCOIN: {
                int accountNumber = accountObject.get("accountNumber").getAsInt();
                int externalIndex = accountObject.get("externalIndex").getAsInt();
                int changeIndex = accountObject.get("changeIndex").getAsInt();
                return BitcoinManager.getInstance().getAccount(-1, name, seed, accountNumber, externalIndex, changeIndex);
            }
            case DASH:
            {
                int accountNumber = accountObject.get("accountNumber").getAsInt();
                int externalIndex = accountObject.get("externalIndex").getAsInt();
                int changeIndex = accountObject.get("changeIndex").getAsInt();
                return DashManager.getInstance().getAccount(-1, name, seed, accountNumber, externalIndex, changeIndex);
            }
        }
        return null;
    }
}
