package de.bitsharesmunich.cryptocoincore.base;

import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinManager;

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
}
