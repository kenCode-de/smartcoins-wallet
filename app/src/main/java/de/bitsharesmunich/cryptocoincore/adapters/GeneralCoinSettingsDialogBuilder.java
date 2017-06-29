package de.bitsharesmunich.cryptocoincore.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.database.SCWallDatabaseContract;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Created by Henry Varona on 22/4/2017.
 */

public class GeneralCoinSettingsDialogBuilder {

    protected Coin coin;
    protected int settingsLayout;
    protected GeneralCoinSettings coinSettings;
    final protected Context context;

    public GeneralCoinSettingsDialogBuilder(Context context, Coin coin){
        this.context = context;
        this.coin = coin;
        this.settingsLayout = R.layout.general_coin_setting;
        this.coinSettings = GeneralCoinFactory.getSettings(context, this.coin);
    }

    public void setPrecision(Dialog dialog){
       final GeneralCoinSettings.GeneralCoinSetting precisionSetting;
       //if (coinSettings.settingExists("precision")) {
           precisionSetting = coinSettings.getSetting("precision");
       //} else {
       //    precisionSetting = coinSettings.addSetting("precision","8");
       //}

        final Spinner spPrecision = (Spinner)dialog.findViewById(R.id.precision);

        ArrayList<String> precisionArray = new ArrayList<String>();
        precisionArray.add(coin.getLabel());
        precisionArray.add("m"+coin.getLabel());
        precisionArray.add("μ"+coin.getLabel());
        final ArrayList<String> precisionValueArray = new ArrayList<String>();
        precisionValueArray.add("8");
        precisionValueArray.add("5");
        precisionValueArray.add("2");

        ArrayAdapter<String> precisionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, precisionArray);
        spPrecision.setAdapter(precisionAdapter);
        if (precisionSetting != null){
            spPrecision.setSelection(precisionValueArray.indexOf(precisionSetting.getValue()));
        }

        spPrecision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean initialized = false;
            private GeneralCoinSettings.GeneralCoinSetting setting;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //this is for avoiding the initialization triggering
                if (initialized){
                    String valueSelected = (String)spPrecision.getSelectedItem();

                    if (!this.setting.getValue().equals(valueSelected)){
                        this.setting.setValue(precisionValueArray.get(position));
                        SCWallDatabase db = new SCWallDatabase(context);
                        db.putGeneralCoinSetting(coin, setting);
                        //Save to db
                    }
                } else {
                    initialized = true;
                    this.setting = precisionSetting;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public Dialog createDialog(int themeResId){
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setTitle(this.coin.getLabel() + " " + context.getString(R.string.action_settings));
        dialog.setContentView(settingsLayout);
        this.setPrecision(dialog);
        return dialog;
    }

    public void addSendSettings(ViewGroup parent){
        //Does nothing for general coins for now
    }
}
