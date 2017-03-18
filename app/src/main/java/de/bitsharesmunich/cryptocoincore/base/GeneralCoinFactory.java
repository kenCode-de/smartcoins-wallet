package de.bitsharesmunich.cryptocoincore.base;

import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinAccount;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinValidator;
import de.bitsharesmunich.cryptocoincore.dash.DashAccount;
import de.bitsharesmunich.cryptocoincore.dash.DashValidator;

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
        }

        return null;
    }

    public static GeneralCoinAccount getGeneralCoinAccount(Coin coin, final AccountSeed seed, String name){
        switch (coin){
            case BITCOIN:
                return new BitcoinAccount(seed, name);
            case DASH:
                return new DashAccount(seed, name);
        }

        return null;
    }
}
