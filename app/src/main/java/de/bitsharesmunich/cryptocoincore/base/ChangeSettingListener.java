package de.bitsharesmunich.cryptocoincore.base;

import de.bitshares_munich.models.GeneralCoinSettingEvent;

/**
 * Created by henry on 20/06/2017.
 */
public interface ChangeSettingListener {
    public void settingChange(GeneralCoinSettingEvent e);
}

