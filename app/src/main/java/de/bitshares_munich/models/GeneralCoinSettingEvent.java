package de.bitshares_munich.models;

/**
 * Created by Henry Varona on 20/6/2017.
 */

import java.util.EventObject;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

public class GeneralCoinSettingEvent extends EventObject {
    public GeneralCoinSettingEvent( Object source ) {
        super( source );
    }
}