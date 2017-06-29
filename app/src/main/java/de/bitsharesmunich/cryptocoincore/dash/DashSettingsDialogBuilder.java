package de.bitsharesmunich.cryptocoincore.dash;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.adapters.GeneralCoinSettingsDialogBuilder;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;

/**
 * Created by Henry Varona on 22/4/2017.
 */

public class DashSettingsDialogBuilder extends GeneralCoinSettingsDialogBuilder{

    protected int settingsLayout;
    //protected DashSettings coinSettings;

    public DashSettingsDialogBuilder(Context context){
        super(context, Coin.DASH);

        this.settingsLayout = R.layout.dash_setting;
    }

    public void setInstantsend(Dialog dialog){
        final GeneralCoinSettings.GeneralCoinSetting instantsendSetting;
        instantsendSetting = coinSettings.getSetting("instantsend");

        final CheckBox checkInstantsend = (CheckBox)dialog.findViewById(R.id.instantsend);
        checkInstantsend.setChecked(instantsendSetting.getValue().equals("true"));

        checkInstantsend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GeneralCoinSettings.GeneralCoinSetting setting = instantsendSetting;

                instantsendSetting.setValue((isChecked?"true":"false"));

                SCWallDatabase db = new SCWallDatabase(context);
                db.putGeneralCoinSetting(coin, setting);
            }
        });
    }

    public Dialog createDialog(int themeResId){
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setTitle(this.coin.getLabel() + " " + context.getString(R.string.action_settings));
        dialog.setContentView(settingsLayout);
        this.setPrecision(dialog);
        this.setInstantsend(dialog);
        return dialog;
    }

    public void addSendSettings(ViewGroup parent){
        //hay que revisar por qué no aparece la vista que se está agregando
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View sendPageDashSettings = (View)layoutInflater.inflate(R.layout.send_page_dash_settings, null);
        CheckBox checkInstantsend = (CheckBox)sendPageDashSettings.findViewById(R.id.osInstantsend);

        boolean defaultInstantsend = this.coinSettings.getSetting("instantsend").getValue().equals("true");
        checkInstantsend.setChecked(defaultInstantsend);
        parent.addView(sendPageDashSettings);
    }
}
