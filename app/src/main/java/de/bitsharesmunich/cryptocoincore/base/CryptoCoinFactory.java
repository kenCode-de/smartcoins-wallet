package de.bitsharesmunich.cryptocoincore.base;

import com.google.gson.JsonObject;

import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinManager;
import de.bitsharesmunich.cryptocoincore.dash.DashManager;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinManager;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinManager;

/**
 * Created by henry on 05/02/2017.
 */

public class CryptoCoinFactory {


    public static GeneralCoinManager getGeneralCoinManager(Coin coin){
        switch(coin){
            case BITCOIN:
                return BitcoinManager.getInstance();
            case DASH:
                return DashManager.getInstance();
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
            case LITECOIN:
            {
                int accountNumber = accountObject.get("accountNumber").getAsInt();
                int externalIndex = accountObject.get("externalIndex").getAsInt();
                int changeIndex = accountObject.get("changeIndex").getAsInt();
                return LiteCoinManager.getInstance().getAccount(-1, name, seed, accountNumber, externalIndex, changeIndex);
            }
            case DOGECOIN:
            {
                int accountNumber = accountObject.get("accountNumber").getAsInt();
                int externalIndex = accountObject.get("externalIndex").getAsInt();
                int changeIndex = accountObject.get("changeIndex").getAsInt();
                return DogeCoinManager.getInstance().getAccount(-1, name, seed, accountNumber, externalIndex, changeIndex);
            }
        }
        return null;
    }

    public static CryptoCoinAccount getAccountFromSeed(Coin coin, AccountSeed seed, String name){
        switch(coin){
            case BITCOIN:
                return BitcoinManager.getInstance().newAccount(seed, name);
            case DASH:
                return DashManager.getInstance().newAccount(seed, name);
            case LITECOIN:
                return LiteCoinManager.getInstance().newAccount(seed, name);
            case DOGECOIN:
                return DogeCoinManager.getInstance().newAccount(seed, name);
        }
        return null;
    }
}
