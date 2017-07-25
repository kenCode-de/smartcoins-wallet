package de.bitsharesmunich.cryptocoincore.base;

import de.bitshares_munich.models.GeneralCoinSettingEvent;

/**
 * Interface for the change settings event
 */
public interface ChangeSettingListener {
    /**
     * This method is invoke when a change of setting is done by the user see {@link GeneralCoinSettingEvent}
     */
    void settingChange(GeneralCoinSettingEvent e);
}

