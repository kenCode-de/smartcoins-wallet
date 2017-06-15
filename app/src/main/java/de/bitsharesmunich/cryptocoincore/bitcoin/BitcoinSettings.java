package de.bitsharesmunich.cryptocoincore.bitcoin;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Created by Henry Varona on 12/6/2017.
 */

public class BitcoinSettings extends GeneralCoinSettings {

    static private BitcoinSettings instance = null;

    public static BitcoinSettings getInstance() {
        if (BitcoinSettings.instance == null) {
            BitcoinSettings.instance = new BitcoinSettings();
        }

        return BitcoinSettings.instance;
    }

    public BitcoinSettings(){
        super(Coin.BITCOIN);
    }
}
