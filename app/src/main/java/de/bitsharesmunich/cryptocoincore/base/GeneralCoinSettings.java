package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitshares_munich.models.GeneralCoinSettingEvent;

/**
 * Created by Henry Varona on 21/4/2017.
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

    public GeneralCoinSetting getSetting(String setting){
        for (GeneralCoinSetting nextSetting : this.mSettings){
            if (nextSetting.getSetting().equals(setting)){
                return nextSetting;
            }
        }

        return null;
    }

    public boolean settingExists(String setting){
        for (GeneralCoinSetting nextSetting : this.mSettings){
            if (nextSetting.getSetting().equals(setting)){
                return true;
            }
        }

        return false;
    }

    public void addChangeSettingListener(ChangeSettingListener listener) {
        this.mChangeSettingListeners.add(listener);
    }


    private synchronized void _fireOnSettingChangeEvent() {
        GeneralCoinSettingEvent event = new GeneralCoinSettingEvent( this );
        Iterator listeners = mChangeSettingListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ChangeSettingListener) listeners.next() ).settingChange( event );
        }
    }

    public class GeneralCoinSetting {
        String setting;
        String value;
        long id;

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

