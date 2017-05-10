package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.database.SCWallDatabaseContract;

/**
 * Created by Henry Varona on 21/4/2017.
 */

public class GeneralCoinSettings {
    Coin coinType;
    List<GeneralCoinSetting> settings;

    public GeneralCoinSettings(Coin coin){
        this.coinType = coin;
    };

    public Coin getCoinType(){
        return this.coinType;
    }

    public int getSettingsCount(){
        return this.settings.size();
    }

    public void addSetting(long id, String setting, String value){
        this.settings.add(new GeneralCoinSetting(id,setting,value));
    }

    public void addSetting(String setting, String value){
        this.settings.add(new GeneralCoinSetting(setting,value));
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

