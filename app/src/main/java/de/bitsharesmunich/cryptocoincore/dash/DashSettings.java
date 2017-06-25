package de.bitsharesmunich.cryptocoincore.dash;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Created by Henry Varona on 24/6/2017.
 */

public class DashSettings extends GeneralCoinSettings {

    public DashSettings(){
        super(Coin.DASH);

        //adding defaults
        this.addSetting("instantsend","true");
    }
}
