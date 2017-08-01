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

/**
 * Base class that creates views for specific coins. Each coin
 * with more or different options than "precision" should extends
 * from this class and add its own options.
 *
 * If a coin doesn't extends this class, for default this class is used.
 * So, any coin has for default the precision setting.
 *
 */
public class GeneralCoinSettingsDialogBuilder {

    protected Coin coin; /**< the coin of the settings to build*/
    protected int settingsLayout; /**< the layout that will be used to create the settings dialog*/
    protected GeneralCoinSettings coinSettings; /**< the coin settings, usually loaded from database*/
    final protected Context context; /**< the application context*/

    public GeneralCoinSettingsDialogBuilder(Context context, Coin coin){
        this.context = context;
        this.coin = coin;
        this.settingsLayout = R.layout.general_coin_setting;
        this.coinSettings = GeneralCoinFactory.getSettings(context, this.coin);
    }

    /**
     * Adds a spinner precision option to a dialog
     *
     * @param dialog the dialog to add the precision option
     */
    public void setPrecision(Dialog dialog){
        final GeneralCoinSettings.GeneralCoinSetting precisionSetting;
        precisionSetting = coinSettings.getSetting("precision");
        final Spinner spPrecision = (Spinner)dialog.findViewById(R.id.precision);

        /**
         * The spinner of the precision will have 3 posible values:
         * 8 digits precision, 5 digits precision and 2 digits precision.
         * This last two values will have the prefixes: "m" and "μ" respectively.
         */
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

    /**
     * Creates the default settings Dialog for a coin
     */
    public Dialog createDialog(int themeResId){
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setTitle(this.coin.getLabel() + " " + context.getString(R.string.action_settings));
        dialog.setContentView(settingsLayout);
        this.setPrecision(dialog);
        return dialog;
    }

    /**
     * Adds settings options to the ViewGroup of the SendScreen
     * @param parent the container view of the SendScreen
     */
    public void addSendSettings(ViewGroup parent){
        //Does nothing for general coins for now
    }
}
