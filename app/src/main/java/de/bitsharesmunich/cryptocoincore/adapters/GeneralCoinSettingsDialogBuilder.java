package de.bitsharesmunich.cryptocoincore.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
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

    private Coin coin;

    public GeneralCoinSettingsDialogBuilder(Coin coin){
        this.coin = coin;
    }

    public Dialog createDialog(final Context context, int themeResId){
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setTitle(this.coin.getLabel() + " " + context.getString(R.string.action_settings));
        dialog.setContentView(R.layout.general_coin_setting);
        final Spinner spPrecision = (Spinner)dialog.findViewById(R.id.precision);

        GeneralCoinSettings coinSettings = GeneralCoinFactory.getSettings(this.coin);
        final GeneralCoinSettings.GeneralCoinSetting precisionSetting = coinSettings.getSetting("precision");

        ArrayList<String> precisionArray = new ArrayList<String>();
        precisionArray.add(coin.getLabel());
        precisionArray.add("m"+coin.getLabel());
        precisionArray.add("μ"+coin.getLabel());
        final ArrayList<String> precisionValueArray = new ArrayList<String>();
        precisionValueArray.add("0");
        precisionValueArray.add("3");
        precisionValueArray.add("6");

        ArrayAdapter<String> precisionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, precisionArray);
        spPrecision.setAdapter(precisionAdapter);
        if (precisionSetting != null){
            spPrecision.setSelection(precisionAdapter.getPosition(precisionSetting.getValue()));
        }

        spPrecision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean initialized = false;
            private GeneralCoinSettings.GeneralCoinSetting setting;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //this is for avoiding the initialization triggering
                if (initialized){
                    String valueSelected = (String)spPrecision.getSelectedItem();

                    if (!setting.getValue().equals(valueSelected)){
                        setting.setValue(precisionValueArray.get(position));
                        SCWallDatabase db = new SCWallDatabase(context);
                        db.putGeneralCoinSetting(coin, setting);
                        //Save to db
                    }
                } else {
                    initialized = true;
                    setting = precisionSetting;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return dialog;
    }
}
