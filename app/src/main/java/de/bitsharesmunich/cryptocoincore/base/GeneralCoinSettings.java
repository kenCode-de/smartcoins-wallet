package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.database.SCWallDatabaseContract;
import de.bitshares_munich.models.BalanceItem;
import de.bitshares_munich.models.BalanceItemsEvent;
import de.bitshares_munich.models.BalanceItemsListener;
import de.bitshares_munich.models.GeneralCoinSettingEvent;

/**
 * Created by Henry Varona on 21/4/2017.
 */

public class GeneralCoinSettings {
    Coin coinType;
    List<GeneralCoinSetting> settings;
    private List<ChangeSettingListener> _changeSettingListeners = new ArrayList<ChangeSettingListener>();


    public GeneralCoinSettings(Coin coin){
        this.coinType = coin;
        this.settings = new ArrayList<GeneralCoinSetting>();

        //adding defaults
        this.addSetting("precision","8");
    };

    public Coin getCoinType(){
        return this.coinType;
    }

    public int getSettingsCount(){
        return this.settings.size();
    }

    public GeneralCoinSetting addSetting(long id, String setting, String value){
        GeneralCoinSetting generalCoinSetting = this.getSetting(setting);

        if (generalCoinSetting == null) {
            generalCoinSetting = new GeneralCoinSetting(id, setting, value);
            this.settings.add(generalCoinSetting);
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
            this.settings.add(generalCoinSetting);
        } else {
            generalCoinSetting.setValue(value);
        }

        return generalCoinSetting;
    }

    public List<GeneralCoinSetting> getSettings(){
        return this.settings;
    }

    public GeneralCoinSetting getSetting(String setting){
        for (GeneralCoinSetting nextSetting : this.settings){
            if (nextSetting.getSetting().equals(setting)){
                return nextSetting;
            }
        }

        return null;
    }

    public boolean settingExists(String setting){
        for (GeneralCoinSetting nextSetting : this.settings){
            if (nextSetting.getSetting().equals(setting)){
                return true;
            }
        }

        return false;
    }

    public void addChangeSettingListener(ChangeSettingListener listener) {
        this._changeSettingListeners.add(listener);
    }


    private synchronized void _fireOnSettingChangeEvent() {
        GeneralCoinSettingEvent event = new GeneralCoinSettingEvent( this );
        Iterator listeners = _changeSettingListeners.iterator();
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

