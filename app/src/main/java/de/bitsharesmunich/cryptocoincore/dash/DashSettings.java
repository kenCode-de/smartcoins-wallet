package de.bitsharesmunich.cryptocoincore.dash;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Define default settings for the dash coin
 */

public class DashSettings extends GeneralCoinSettings {

    public DashSettings(){
        super(Coin.DASH);

        //adding defaults
        this.addSetting("instantsend","true");
    }
}
