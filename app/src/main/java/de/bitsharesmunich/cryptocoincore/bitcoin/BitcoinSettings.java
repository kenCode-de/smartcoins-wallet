package de.bitsharesmunich.cryptocoincore.bitcoin;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Represents the user settings for the bitcoin coin
 *
 */
public class BitcoinSettings extends GeneralCoinSettings {

    public BitcoinSettings(){
        super(Coin.BITCOIN);
    }
}
