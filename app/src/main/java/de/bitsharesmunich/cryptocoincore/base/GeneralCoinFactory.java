package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;

import de.bitsharesmunich.cryptocoincore.adapters.GeneralCoinSettingsDialogBuilder;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinAccount;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinSettings;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinValidator;
import de.bitsharesmunich.cryptocoincore.dash.DashAccount;
import de.bitsharesmunich.cryptocoincore.dash.DashValidator;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinAccount;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinValidator;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinAccount;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinValidator;

/**
 * Created by Henry Varona on 26/2/2017.
 */

public class GeneralCoinFactory {

    public static GeneralCoinValidator getValidator(Coin coin){
        switch(coin){
            case BITCOIN:
                return BitcoinValidator.getInstance();
            case DASH:
                return DashValidator.getInstance();
            case LITECOIN:
                return LiteCoinValidator.getInstance();
            case DOGECOIN:
                return DogeCoinValidator.getInstance();
        }

        return null;
    }

    public static GeneralCoinSettingsDialogBuilder getDialogBuilder(Context context, Coin coin){
        return new GeneralCoinSettingsDialogBuilder(context, coin);
    }

    public static GeneralCoinSettings getSettings(Coin coin){
        switch(coin){
            case BITCOIN:
                return BitcoinSettings.getInstance();
            case DASH:
                //return DashSettings.getInstance();
            case LITECOIN:
                //return LiteCoinSettings.getInstance();
            case DOGECOIN:
                //return DogeCoinSettings.getInstance();
        }

        return null;
    }

    public static GeneralCoinAccount getGeneralCoinAccount(Coin coin, final AccountSeed seed, String name){
        switch (coin){
            case BITCOIN:
                return new BitcoinAccount(seed, name);
            case DASH:
                return new DashAccount(seed, name);
            case LITECOIN:
                return new LiteCoinAccount(seed,name);
            case DOGECOIN:
                return new DogeCoinAccount(seed,name);
        }

        return null;
    }
}
