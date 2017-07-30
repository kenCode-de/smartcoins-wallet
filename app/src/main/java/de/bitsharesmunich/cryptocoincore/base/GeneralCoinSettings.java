package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitshares_munich.models.GeneralCoinSettingEvent;

/**
 * Created by Henry Varona on 21/4/2017.
 */

/**
 * Represents a user settings for a specific coin type
 *
 */
public class GeneralCoinSettings {
    Coin mCoinType;
    List<GeneralCoinSetting> mSettings;
    private List<ChangeSettingListener> mChangeSettingListeners = new ArrayList<ChangeSettingListener>();


    public GeneralCoinSettings(Coin coin){
        this.mCoinType = coin;
        this.mSettings = new ArrayList<GeneralCoinSetting>();

        //adding defaults
        this.addSetting("precision","8");
    };

    public Coin getCoinType(){
        return this.mCoinType;
    }

    public int getSettingsCount(){
        return this.mSettings.size();
    }

    /**
     * Add a new setting to this settings
     *
     * @param id the id of the setting. if the value is -1, the setting will get a new id when saved to the database
     * @param setting the name of the new setting to add
     * @param value the value of the new setting to add
     * @return a setting object with the given name and value
     */
    public GeneralCoinSetting addSetting(long id, String setting, String value){
        GeneralCoinSetting generalCoinSetting = this.getSetting(setting);

        if (generalCoinSetting == null) {
            generalCoinSetting = new GeneralCoinSetting(id, setting, value);
            this.mSettings.add(generalCoinSetting);
        } else {
            generalCoinSetting.setId(id);
            generalCoinSetting.setValue(value);
        }

        return generalCoinSetting;
    }

    /**
     * Add a new setting to this settings
     *
     * @param setting the name of the new setting to add
     * @param value the value of the new setting to add
     * @return a setting object with the given name and value. The id of the setting will be -1
     */
    public GeneralCoinSetting addSetting(String setting, String value){
        GeneralCoinSetting generalCoinSetting = getSetting(setting);

        if (generalCoinSetting == null) {
            generalCoinSetting = new GeneralCoinSetting(setting,value);
            this.mSettings.add(generalCoinSetting);
        } else {
            generalCoinSetting.setValue(value);
        }

        return generalCoinSetting;
    }

    public List<GeneralCoinSetting> getSettings(){
        return this.mSettings;
    }

    /**
     * Returns a setting in this settings with a specific name
     *
     * @param setting the name of the setting to search for
     * @return the setting in this settings with the given name, null if the setting can't be found
     */
    public GeneralCoinSetting getSetting(String setting){
        for (GeneralCoinSetting nextSetting : this.mSettings){
            if (nextSetting.getSetting().equals(setting)){
                return nextSetting;
            }
        }

        return null;
    }

    /**
     * Verifies whether or not a setting with a specific name exists in this settings
     *
     * @param setting the name of the setting to search for
     * @return true if the setting exists in this settings, false otherwise
     */
    public boolean settingExists(String setting){
        for (GeneralCoinSetting nextSetting : this.mSettings){
            if (nextSetting.getSetting().equals(setting)){
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a new listener to this settings. The listener will be
     * notified when this settings change
     *
     * @param listener the new listener to add
     */
    public void addChangeSettingListener(ChangeSettingListener listener) {
        this.mChangeSettingListeners.add(listener);
    }

    /**
     * Fires a change notify event to all the listeners of this settings.
     */
    private synchronized void _fireOnSettingChangeEvent() {
        GeneralCoinSettingEvent event = new GeneralCoinSettingEvent( this );
        Iterator listeners = mChangeSettingListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ChangeSettingListener) listeners.next() ).settingChange( event );
        }
    }

    /**
     * Represent one user setting of a specific coin. GeneralCoinSettings
     * use a list of this class for managing many user settings of one coin
     *
     */
    public class GeneralCoinSetting {
        String setting; /**< The name of the setting*/
        String value; /**< The value of the setting*/
        long id; /**< The id of the setting in the database*/

        public GeneralCoinSetting(String setting, String value){
            this.id = -1;
            this.setting = setting;
            this.value = value;
        }

        public GeneralCoinSetting(long id, String setting, String value){
            this.id = id;
            this.setting = setting;
            this.value = value;
        }

        public String getSetting() {
            return this.setting;
        }

        public void setSetting(String setting) {
            this.setting = setting;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            if (!this.value.equals(value)) {
                this.value = value;
                GeneralCoinSettings.this._fireOnSettingChangeEvent();
            }
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return this.id;
        }
    }
}

