package de.bitshares_munich.smartcoinswallet;
/**
 * Created by Syed Muhammad Muzzammil on 13/5/16.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AlertDialog;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.bitshares_munich.Interfaces.BackupBinDelegate;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.AccountUpgrade;
import de.bitshares_munich.models.LangCode;
import de.bitshares_munich.models.ResponseBinFormat;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.PermissionManager;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity implements BackupBinDelegate {

    final String check_for_updates = "check_for_updates";
    final String automatically_install = "automatically_install";
    final String require_pin = "require_pin";
    final String close_bitshare = "close_bitshare";
    final String always_donate = "always_donate";
    final String hide_donations = "hide_donations";
    final String hide_donations_isChanged = "hide_donations_isChanged";
    final String date_time = "date_time";
    Boolean isFirstTime = true;
    final String register_new_account = "register_new_account";
    //String brainKey = "";

    @Bind(R.id.spCountry)
    Spinner spCountry;

    @Bind(R.id.spLanguage)
    Spinner spLanguage;

    @Bind(R.id.spTimeZones)
    Spinner spTimeZones;

    @Bind(R.id.spAccounts)
    Spinner spAccounts;

    @Bind(R.id.spBackupAsset)
    Spinner spBackupAsset;

    TinyDB tinyDB;

    ArrayList<AccountDetails> accountDetails;

    @Bind(R.id.ivLifeTime)
    ImageView ivLifeTime;

    @Bind(R.id.tvBlockNumberHead_content_settings)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_content_settings)
    TextView tvAppVersion;

    @Bind(R.id.tvAccounts)
    TextView tvAccounts;


    @Bind(R.id.backup_ic)
    ImageView backup_ic;

    @Bind(R.id.brainkey_ic)
    ImageView brainkey_ic;

    @Bind(R.id.pin_ic)
    ImageView pin_ic;

    Boolean inittLocale = false;

    @Bind(R.id.ivSocketConnected_content_settings)
    ImageView ivSocketConnected;

    @Bind(R.id.upgrade_account)
    Button btnUpgrade;

    @Bind(R.id.tvFolderPath)
    TextView tvFolderPath;

    ProgressDialog progressDialog;
    Activity activitySettings;

    String wifKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.invalidateOptionsMenu();

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_setting);
        setBackButton(true);
        setTitle(getResources().getString(R.string.settings_activity_name));

        tinyDB = new TinyDB(getApplicationContext());
        ButterKnife.bind(this);
        activitySettings = this;
        progressDialog = new ProgressDialog(this);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pre_ischecked_timezone), false);
        init();
        populateDropDowns();
        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();

        //updateBrainKey();
    }

    public void init() {
        setCheckedButtons();
        setAudioFilePath();
    }

    public void onCheck(View v) {
        designMethod();
        switch (v.getId()) {
            case R.id.check_for_updates:
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, check_for_updates, true);
                else Helper.storeBoolianSharePref(this, check_for_updates, false);
                break;
            case R.id.automatically_install:
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, automatically_install, true);
                else Helper.storeBoolianSharePref(this, automatically_install, false);
                break;
            case R.id.require_pin:
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, require_pin, true);
                else Helper.storeBoolianSharePref(this, require_pin, false);
                break;
            case R.id.close_bitshare:
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, close_bitshare, true);
                else Helper.storeBoolianSharePref(this, close_bitshare, false);
                break;
            case R.id.always_donate:
                //if (isCHecked(v)) Helper.storeBoolianSharePref(this, always_donate, true);
                //else Helper.storeBoolianSharePref(this, always_donate, false);
                Helper.storeBoolianSharePref(this, always_donate, false);
                break;
            case R.id.hide_donations:
                Helper.storeBoolianSharePref(this, hide_donations_isChanged, true);
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, hide_donations, true);
                else Helper.storeBoolianSharePref(this, hide_donations, false);
                break;
            default:
                break;
        }
    }

    public void onClickBackupBrainkeybtn(View v) {
        designMethod();
        showDialogCopyBrainKey();
    }

    public void onClickSecurePinbtn(View v) {
        designMethod();
        Intent intent = new Intent(getApplicationContext(), PinActivity.class);
        startActivity(intent);
        //showDialogPinRequest();
    }

    public void onClickBackbtn(View v) {
        designMethod();
        Intent intent = new Intent(getApplicationContext(), BrainkeyActivity.class);
        //startActivity(intent);
    }

    Boolean isCHecked(View v) {
        designMethod();
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            return true;
        }
        return false;
    }

    void setCheckedButtons() {
        CheckBox checkBox;
        if (Helper.fetchBoolianSharePref(this, check_for_updates)) {
            checkBox = (CheckBox) findViewById(R.id.check_for_updates);
            checkBox.setChecked(true);
        }
        if (Helper.fetchBoolianSharePref(this, automatically_install)) {
            checkBox = (CheckBox) findViewById(R.id.automatically_install);
            checkBox.setChecked(true);
        }
        if (Helper.fetchBoolianSharePref(this, require_pin)) {
            checkBox = (CheckBox) findViewById(R.id.require_pin);
            checkBox.setChecked(true);
        }
        if (Helper.fetchBoolianSharePref(this, close_bitshare)) {
            checkBox = (CheckBox) findViewById(R.id.close_bitshare);
            checkBox.setChecked(true);
        }
        if (Helper.fetchBoolianSharePref(this, always_donate)) {
            checkBox = (CheckBox) findViewById(R.id.always_donate);
            //checkBox.setChecked(true);
            checkBox.setChecked(false);
        }
        if (Helper.fetchBoolianSharePref(this, hide_donations)) {
            checkBox = (CheckBox) findViewById(R.id.hide_donations);
            checkBox.setChecked(true);
        }
    }

    boolean dontCallCountryChangedOnStart = true;

    @SuppressLint("NewApi")
    private void populateDropDowns() {

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if ( selectedItemView != null )
                {
                    if ( dontCallCountryChangedOnStart )
                    {
                        dontCallCountryChangedOnStart = false;
                    }
                    else {
                        spCountryItemSelected(position);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ArrayList<String> countries = Helper.getCountriesArray();
        ArrayAdapter<String> adapterCountry = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, countries);
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapterCountry);

        Boolean isCountry = Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_country));
        if (isCountry)
        {
            String countryCode = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_country));
            String spinnertext = Helper.getSpinnertextCountry(countryCode);
            int index = countries.indexOf(spinnertext);
            spCountry.setSelection(index);
        }
        else
        {
            Locale locale = Locale.GERMANY;
            final int index = countries.indexOf(locale.getDisplayCountry() + " (EUR)");
            if (index > 0) {
                spCountry.post(new Runnable() {
                    public void run() {
                        spCountry.setSelection(index);
                    }
                });
            }
        }

        ArrayList<LangCode> langArray = new ArrayList<>();
        ArrayList<String> getLangCode = null;
        getLangCode = Helper.getLanguages();
        for (int i = 0; i < getLangCode.size(); i++) {

            if (getLangCode.get(i).equalsIgnoreCase("zh-rTW")) {
                LangCode langCode = new LangCode();
                langCode.code = "zh-rTW";
                langCode.lang = "Chinese"+ "; " + "zh-rTW" +  " (繁體中文)";
                langArray.add(langCode);
            }
            else if(getLangCode.get(i).equalsIgnoreCase("zh-rCN") || getLangCode.get(i).equalsIgnoreCase("zh"))
            {
                LangCode langCode = new LangCode();
                langCode.code = "zh-rCN";
                langCode.lang = "Chinese"+ "; " + "zh-rCN" +  " (简体中文)";
                langArray.add(langCode);
            }
            else {
                LangCode langCode = new LangCode();
                Locale locale = new Locale(getLangCode.get(i));
                langCode.lang = locale.getDisplayName() + "; " + locale.toString() + " ("+locale.getDisplayLanguage(locale)+")" ;
                langCode.code = getLangCode.get(i);
                langArray.add(langCode);
            }
        }

        ArrayAdapter<LangCode> adapterLanguage = new ArrayAdapter<LangCode>(this, android.R.layout.simple_spinner_item, langArray);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inittLocale = false;
        spLanguage.setAdapter(adapterLanguage);
        String langCode = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
        //  Helper.setLocale(langCode, getResources());
        if (langCode != "")
        {
            for (int i = 0; i < langArray.size(); i++) {
                LangCode lc = langArray.get(i);

                if ( langCode.equalsIgnoreCase("zh") && lc.code.equalsIgnoreCase("zh-rcn") )
                {
                    spLanguage.setSelection(i);
                }
                else if (lc.code.equalsIgnoreCase(langCode))
                {
                    spLanguage.setSelection(i);
                }
            }
        }
        else
        {
            spLanguage.setSelection(13);
            Helper.setLocale("de", getResources());
        }


        //Time Zones
        ArrayList<String> arrayTimeZones = new ArrayList<>();

        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            arrayTimeZones.add(displayTimeZone(TimeZone.getTimeZone(id)));
        }
        Collections.sort(arrayTimeZones);
        arrayTimeZones.add(0, getString(R.string.select_timezone));
        ArrayAdapter<String> adapterTimezone = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayTimeZones);
        adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTimeZones.setAdapter(adapterTimezone);

        Boolean isTimezone = Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_timezone));
        if (isTimezone) {
            int indexTimezone = Helper.fetchIntSharePref(getApplicationContext(), getString(R.string.pref_timezone));
            spTimeZones.setSelection(indexTimezone);
        } else {
            spTimeZones.setSelection(0);
        }


        // AccountsName

        ArrayList<String> arrayAccountName = new ArrayList<>();

        if (accountDetails.size() > 1) {
            spAccounts.setVisibility(View.VISIBLE);
            tvAccounts.setVisibility(View.GONE);
        } else {
            tvAccounts.setVisibility(View.VISIBLE);
            spAccounts.setVisibility(View.GONE);
        }


        String accountName = "";
        int posBackupAssets=-9;
        for (int i = 0; i < accountDetails.size(); i++) {
            arrayAccountName.add(accountDetails.get(i).account_name);
            tvAccounts.setText(accountDetails.get(i).account_name);
            if (accountDetails.get(i).isSelected) {
                accountName = accountDetails.get(i).account_name;
                posBackupAssets=accountDetails.get(i).posBackupAsset;
            }
            if (accountDetails.get(i).isLifeTime) {
                ivLifeTime.setVisibility(View.VISIBLE);
                btnUpgrade.setEnabled(false);
                btnUpgrade.setBackgroundColor(Color.rgb(211, 211, 211));

            } else {
                ivLifeTime.setVisibility(View.GONE);
                btnUpgrade.setEnabled(true);
                btnUpgrade.setBackground(getResources().getDrawable(R.drawable.button_border));
            }
        }

        Collections.sort(arrayAccountName);
        ArrayAdapter<String> adapterAccountName = new ArrayAdapter<>(this, R.layout.mytextview, arrayAccountName);
        adapterAccountName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAccounts.setAdapter(adapterAccountName);
        if (accountName.isEmpty()) {
            spAccounts.setSelection(0);
        } else {
            spAccounts.setSelection(arrayAccountName.indexOf(accountName));
        }

        //Asset
        ArrayList<AccountAssets> accountAssets = null;
        for (int i = 0; i < accountDetails.size(); i++) {

            if (spAccounts.getSelectedItem().toString().equals(accountDetails.get(i).account_name)) {
                accountAssets = accountDetails.get(i).AccountAssets;
            }

        }
        if (accountAssets != null) {
            ArrayList<String> arrayAccountAssets = new ArrayList<>();
            for (int j = 0; j < accountAssets.size(); j++) {
                arrayAccountAssets.add(accountAssets.get(j).symbol);
            }

            AssetsSymbols assetsSymbols = new AssetsSymbols(getApplicationContext());
            arrayAccountAssets = assetsSymbols.updatedList(arrayAccountAssets);

            arrayAccountAssets.add(0,"-------");

            ArrayAdapter<String> adapterAccountAssets = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAccountAssets);
            adapterAccountAssets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spBackupAsset.setAdapter(adapterAccountAssets);

            spBackupAsset.setSelection(0);

            Boolean isBackupAsset = Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_backup_asset_selected));
            if(isBackupAsset) {
                if (Helper.fetchBoolianSharePref(getApplicationContext(), getString(R.string.pref_backup_asset_selected))) {
                    if (posBackupAssets != -9) {
                        spBackupAsset.setSelection(posBackupAssets);
                    }
                }
            }
        }


    }

    private static String displayTimeZone(TimeZone tz) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(tz);
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset())
                - TimeUnit.HOURS.toMinutes(hours);
        minutes = Math.abs(minutes);
        //String temp= calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT);
        String result = "";
        if (hours > 0) {

            result = String.format("%s (GMT+%d:%02d)", tz.getID(), hours, minutes);
        } else {
            result = String.format("%s (GMT%d:%02d)", tz.getID(), hours, minutes);
        }

        return result;
    }

    /*
    private void updateBrainKey ()
    {
        for (int i = 0; i < accountDetails.size(); i++)
        {
            if (accountDetails.get(i).isSelected)
            {
                brainKey = accountDetails.get(i).brain_key;
            }
        }
    }
    */

    private String getPin()
    {
        for (int i = 0; i < accountDetails.size(); i++)
        {
            if (accountDetails.get(i).isSelected)
            {
                return accountDetails.get(i).pinCode;
            }
        }

        return "";
    }

    private String getBrainKey()
    {
        for (int i = 0; i < accountDetails.size(); i++)
        {
            if (accountDetails.get(i).isSelected)
            {
                return accountDetails.get(i).brain_key;
            }
        }

        return "";
    }

    private String getAccountName()
    {
        for (int i = 0; i < accountDetails.size(); i++)
        {
            if (accountDetails.get(i).isSelected)
            {
                return accountDetails.get(i).account_name;
            }
        }

        return "";
    }

    @SuppressLint("NewApi")
    @OnItemSelected(R.id.spAccounts)
    void onItemSelectedAccount(int position) {
        designMethod();
        if (position >= 0) {
            for (int i = 0; i < accountDetails.size(); i++) {

                if (spAccounts.getSelectedItem().toString().equals(accountDetails.get(i).account_name)) {
                    accountDetails.get(i).isSelected = true;
                    //brainKey = accountDetails.get(i).brain_key;
                    if (accountDetails.get(i).isLifeTime) {
                        ivLifeTime.setVisibility(View.VISIBLE);
                        btnUpgrade.setEnabled(false);
                        btnUpgrade.setBackgroundColor(Color.rgb(211, 211, 211));

                    } else {
                        ivLifeTime.setVisibility(View.GONE);
                        btnUpgrade.setEnabled(true);
                        btnUpgrade.setBackground(getResources().getDrawable(R.drawable.button_border));

                    }
                } else {
                    accountDetails.get(i).isSelected = false;
                }

            }
            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_account_name), spAccounts.getSelectedItem().toString());
        }
    }

    private void spCountryItemSelected(int position)
    {
        designMethod();
        String countryCode = Helper.getCountryCode(spCountry.getSelectedItem().toString());
        Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_fade_currency), spCountry.getSelectedItem().toString());
        Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_country), countryCode);
    }

    /*
    @OnItemSelected(R.id.spCountry)
    void onItemSelectedCountry(int position) {
        //spCountryItemSelected(position);
    }
    */

    @OnItemSelected(R.id.spTimeZones)
    void onItemSelectedTimeZone(int position) {
        designMethod();
        if (position > 0) {

            String temp[] = spTimeZones.getSelectedItem().toString().split(" ");
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.date_time_zone), temp[0]);
            if (Helper.fetchIntSharePref(getApplicationContext(), getString(R.string.pref_timezone)) != position) {
                Helper.storeIntSharePref(getApplicationContext(), getString(R.string.pref_timezone), position);
                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pre_ischecked_timezone), true);
            }

        }
    }

//    @OnNothe
//    ArrayList<String> arrayAccountAssets = new ArrayList<>();
//    for (int j = 0; j < 10 ; j++) {
//        arrayAccountAssets.add(String.valueOf(j));
//    }
//    ArrayAdapter<String> adapterAccountAssets = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAccountAssets);
//    adapterAccountAssets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//    spBackupAsset.setAdapter(adapterAccountAssets);




    @OnItemSelected(R.id.spBackupAsset)
    void onItemSelectedBackupAsset(int position) {
        if(!isFirstTime) {
            designMethod();
            if (position > 0) {
                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pref_backup_asset_selected), true);
                String selected = spBackupAsset.getSelectedItem().toString();
                selected = selected.replace("bit", "");
                Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_backup_symbol), selected);
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        accountDetails.get(i).posBackupAsset = position;
                    }
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
                // Helper.storeIntSharePref(getApplicationContext(), getString(R.string.pref_backup_asset), position);
            }
        else  if (position == 0)
            {
                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pref_backup_asset_selected), false);
                Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_backup_symbol), "");
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        accountDetails.get(i).posBackupAsset = -9;
                    }
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
            }
        }else {
            isFirstTime=false;
        }
    }

    @OnItemSelected(R.id.spLanguage)
    void onItemSelectedLanguage(int position) {
        designMethod();

        LangCode langSelection = (LangCode) spLanguage.getSelectedItem();
        Helper.setLocale(langSelection.code, getResources());

//        SupportMethods.setLocale(getApplicationContext(),langSelection.code);

        Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_language), langSelection.code);

        if (inittLocale) {

            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);

            //this.supportInvalidateOptionsMenu();
            //this.invalidateOptionsMenu();

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        inittLocale = true;
//}
//        Locale locale = new Locale(langSelection.code);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,
//                getBaseContext().getResources().getDisplayMetrics());


    }

    private void showDialogCopyBrainKey() {

        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(getString(R.string.backup_brainkey));
        dialog.setContentView(R.layout.activity_copybrainkey);
        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
        try
        {
            String brainKey = getBrainKey();
            if (brainKey.isEmpty())
            {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_load_brainkey),Toast.LENGTH_LONG).show();
                return;
                //brainKey = accountDetails.get(0).brain_key;
            }
            else
            {
                etBrainKey.setText(brainKey);
            }
        }
        catch (Exception e) {

        }

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Button btnCopy = (Button) dialog.findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingActivity.this, R.string.copied_to_clipboard , Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                clipboard.setPrimaryClip(clip);
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);

        dialog.show();
    }

    // Blocks Updation
    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.webSocketG != null) {
                    if (Application.webSocketG.isOpen()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
                    } else {
                        ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                        ivSocketConnected.startAnimation(myFadeInAnimation);
                    }
                } else {
                    ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                    ivSocketConnected.startAnimation(myFadeInAnimation);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTask, 1000);
    }

    void designMethod() {
        if (android.os.Build.VERSION.SDK_INT > 21)
            getWindow().setExitTransition(new Explode());
    }

    @OnClick(R.id.register_new_account)
    void setRegisterNewAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("activity_name", "setting_screen");
        intent.putExtra("activity_id", 919);
        startActivity(intent);
    }

    @OnClick(R.id.import_new_account)
    void setImport_new_account() {
        Intent intent = new Intent(getApplicationContext(), ExistingAccountActivity.class);
        startActivity(intent);
//        Intent intent = new Intent(this, BrainkeyActivity.class);
//        intent.putExtra("activity_name", "setting_screen");
//        intent.putExtra("activity_id", 919);
//        startActivity(intent);
    }

    @OnClick(R.id.upgrade_account)
    void setUpgradeNewAccount() {

        final boolean[] balanceValid = {true};
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_delete_dialog);
        final Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final TextView alertMsg = (TextView) dialog.findViewById(R.id.alertMsg);
        alertMsg.setText(getString(R.string.help_message));
        final Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setBackgroundColor(Color.RED);
        btnCancel.setText(getString(R.string.txt_back));
        btnDone.setText(getString(R.string.next));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check Balance
                String ltmAmount = Helper.fetchStringSharePref(getApplicationContext(), "ltmAmount");
                if (btnDone.getText().equals(getString(R.string.next))) {
                   // alertMsg.setText("Upgrade to LTM now? " + ltmAmount + " BTS will be deducted from " + spAccounts.getSelectedItem().toString() + " account.");
                    alertMsg.setText(getString(R.string.upgrade_to_ltm) + ltmAmount + getString(R.string.bts_will_be_deducted) + spAccounts.getSelectedItem().toString() + getString(R.string.account).toLowerCase()+".");

                    btnDone.setText(getString(R.string.txt_yes));
                    btnCancel.setText(getString(R.string.txt_no));
                } else {
                    dialog.cancel();
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    try {
                        for (int i = 0; i < accountDetails.size(); i++) {
                            if (accountDetails.get(i).isSelected) {
                                wifKey = accountDetails.get(i).wif_key;
                                ArrayList<AccountAssets> arrayListAccountAssets = accountDetails.get(i).AccountAssets;
                                for (int j = 0; j < arrayListAccountAssets.size(); j++) {
                                    AccountAssets accountAssets = arrayListAccountAssets.get(j);
                                    if (accountAssets.symbol.equalsIgnoreCase("BTS")) {
                                        Double amount = Double.valueOf(SupportMethods.ConvertValueintoPrecision(accountAssets.precision, accountAssets.ammount));
                                        if (amount < Double.parseDouble(ltmAmount)) {
                                            balanceValid[0] = false;
                                            Toast.makeText(getApplicationContext(), getString(R.string.insufficient_funds), Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    }
                                }

                            }
                        }
                    } catch (Exception e) {
                    }
                    if (balanceValid[0]) {
                        showDialog("", getString(R.string.upgrading));
                        getAccountUpgradeInfo(activitySettings, spAccounts.getSelectedItem().toString());
                    }
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @OnClick(R.id.remove_account)
    void setRemoveNewAccount() {
        showDialog();
    }

    public void showDialog() {

        final Dialog dialog = new Dialog(this);
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
//                dialog.setTitle(R.string.pin_verification);
        dialog.setContentView(R.layout.alert_delete_dialog);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        TextView textView = (TextView) dialog.findViewById(R.id.alertMsg);
        btnCancel.setText(R.string.txt_no);
        btnDone.setText(R.string.txt_yes);
        String alertMsg = getString(R.string.txt_alertmsg_fr);
        if (accountDetails.size() > 1) {
            alertMsg = alertMsg + " " + spAccounts.getSelectedItem().toString() + " " + getString(R.string.txt_alertmsg_la);
        } else {
            alertMsg = getString(R.string.txt_wallet_atleast_one_account);
            btnDone.setVisibility(View.GONE);
            btnCancel.setText(getString(R.string.cancel));
        }
        textView.setText(alertMsg);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteAccount();
                dialog.cancel();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
//                dialog.setCancelable(false);

        dialog.show();


    }

    void deleteAccount()
    {
        if (accountDetails.size() > 1)
        {
            for (int i = 0; i < accountDetails.size(); i++)
            {
                if (spAccounts.getSelectedItem().toString().equals(accountDetails.get(i).account_name))
                {
                    accountDetails.remove(i);
                    break;
                }
            }

            for (int i = 0; i < accountDetails.size(); i++)
            {
                if ( i == 0 )
                {
                    accountDetails.get(i).isSelected = true;
                }
                else
                {
                    accountDetails.get(i).isSelected = false;
                }
            }

            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

            accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
            populateDropDowns();
        }
        /*
        else
        {
            //tvAccounts.setText("");
            //tvAccounts.setVisibility(View.GONE);
            //clearApplicationData(getApplicationContext());
        }
        */
    }

    public void clearApplicationData(Context mContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.agreement), true);

        Intent k = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        k.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(k);
        finish();
    }

    public void getAccountUpgradeInfo(final Activity activity, final String accountName) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account");
        hashMap.put("account", accountName);
        try {
            hashMap.put("wifkey", Crypt.getInstance().decrypt_string(wifKey));
        } catch (Exception e) {
        }

        final Call<AccountUpgrade> postingService = service.getAccountUpgrade(hashMap);
        postingService.enqueue(new Callback<AccountUpgrade>() {
            @Override
            public void onResponse(Response<AccountUpgrade> response) {
                if (response.isSuccess()) {
                    AccountUpgrade accountDetails = response.body();
                    if (accountDetails.status.equals("success")) {
                        updateLifeTimeModel(accountName);
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                }
            }
        });
    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }

    private void changeDialogMsg(String msg) {
        if (progressDialog != null)
        {
            if (progressDialog.isShowing())
            {
                progressDialog.setMessage(msg);
            }
        }
    }

    private void updateLifeTimeModel(String accountName) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        try {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).account_name.equals(accountName)) {
                    accountDetails.get(i).isLifeTime = true;
                    break;
                }
            }
        } catch (Exception e) {
        }

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
    }

    /*
    Handler createBackUp = new Handler();

    private int convertDOubleToInt(Double value)
    {
        String valueString = Double.toString(value);

        for ( int i = 0 ; i < valueString.length() ; i++ )
        {
            if ( valueString.charAt(i) == '.' )
            {
                valueString = valueString.substring(0,i);
                break;
            }
        }

        int valueInteger = Integer.parseInt(valueString);

        return valueInteger;
    }

    public void saveBinContentToFile(List<Integer> content, String _accountName)
    {
        changeDialogMsg(getResources().getString(R.string.saving_bin_file_to) + " : " + getResources().getString(R.string.folder_name));

        String folder = Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.folder_name);
        String path =  folder + File.separator + _accountName + ".bin";

        boolean success = new BinHelper(this,getApplicationContext()).saveBinFile(path,content,activitySettings);

        hideDialog();

        if ( success )
        {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.bin_file_saved_successfully_to) + " : " + path,Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_save_bin_file),Toast.LENGTH_LONG).show();
        }
    }

    public void get_bin_bytes_from_brainkey(final String pin, final String brnKey, final String _accountName)
    {
        try
        {
            ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
            IWebService service = sg.getService(IWebService.class);

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("method", "backup_bin");
            hashMap.put("password", pin);
            hashMap.put("brainkey", brnKey);

            Call<ResponseBinFormat> postingService = service.getBytesFromBrainKey(hashMap);

            postingService.enqueue(new Callback<ResponseBinFormat>() {
                @Override
                public void onResponse(Response<ResponseBinFormat> response) {

                    if (response.isSuccess())
                    {
                        ResponseBinFormat responseContent = response.body();
                        if (responseContent.status.equals("failure"))
                        {
                            Toast.makeText(activitySettings, getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }
                        else
                        {
                            try
                            {
                                List<Object> abc = (List<Object>) responseContent.content;

                                List<Integer> resultContent = new ArrayList<>();

                                for (Object in: abc)
                                {
                                    int _in = convertDOubleToInt((Double)in);
                                    resultContent.add(_in);
                                }

                                saveBinContentToFile(resultContent, _accountName);
                            }
                            catch (Exception e)
                            {
                                hideDialog();
                                Toast.makeText(activitySettings, activitySettings.getString(R.string.unable_to_import_account_from_bin_file) + " : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    else
                    {
                        hideDialog();
                        Log.d("bin","fail");
                        Toast.makeText(activitySettings, getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    hideDialog();
                    Log.d("bin","fail");
                    Toast.makeText(activitySettings, getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e)
        {
            hideDialog();
            Log.d("bin",e.getMessage());
            Toast.makeText(activitySettings, getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
        }

    }

    public void createBackupBinFile(final String _brnKey,final String _accountName,final String pinCode)
    {
        showDialog(getResources().getString(R.string.creating_backup_file),getResources().getString(R.string.fetching_key));

        if (_brnKey.isEmpty())
        {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_load_brainkey),Toast.LENGTH_LONG).show();
            hideDialog();
            return;
        }

        changeDialogMsg(getResources().getString(R.string.generating_bin_format));

        Runnable getFormat = new Runnable() {
            @Override
            public void run()
            {
                //String pinCode = getPin();
                if ( pinCode.isEmpty() )
                {
                    hideDialog();
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_pin),Toast.LENGTH_LONG).show();
                }
                get_bin_bytes_from_brainkey(pinCode,_brnKey,_accountName);
            }
        };

        createBackUp.postDelayed(getFormat,200);
    }
    */

    @OnClick(R.id.backup_ic)
    public void onClickBackupDotBin()
    {
        /*
        showDialog(getResources().getString(R.string.creating_backup_file),getResources().getString(R.string.fetching_key));

        final String _brnKey = getBrainKey();

        if (_brnKey.isEmpty())
        {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_load_brainkey),Toast.LENGTH_LONG).show();
            hideDialog();
            return;
        }

        final String _accountName = getAccountName();

        changeDialogMsg(getResources().getString(R.string.generating_bin_format));

        Runnable getFormat = new Runnable() {
            @Override
            public void run()
            {
                String pinCode = getPin();
                if ( pinCode.isEmpty() )
                {
                    hideDialog();
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.invalid_pin),Toast.LENGTH_LONG).show();
                }
                get_bin_bytes_from_brainkey(pinCode,_brnKey,_accountName);
            }
        };

        createBackUp.postDelayed(getFormat,200);
        */

        String _brnKey = getBrainKey();
        String _accountName = getAccountName();
        String _pinCode = getPin();

        BinHelper myBinHelper = new BinHelper(this,getApplicationContext(),this);
        myBinHelper.createBackupBinFile(_brnKey,_accountName,_pinCode);

    }

    @Override
    public void backupComplete(boolean success) {
        Log.d("Backup Complete","done");
    }


    FileChooserDialog dialog;

    private void chooseAudioFile() {
        if (dialog == null) {
            dialog = new FileChooserDialog(this);
            dialog.addListener(this.onFileSelectedListener);
            dialog.setFolderMode(false);
            dialog.setCanCreateFiles(false);
            dialog.setShowCancelButton(true);
            dialog.setShowOnlySelectable(false);
            dialog.setFilter(".*wav|.*mp3");


            // Activate the confirmation dialogs.
            dialog.setShowConfirmation(true, true);
            // Define the labels.
            FileChooserLabels labels = new FileChooserLabels();
            labels.createFileDialogAcceptButton = getApplicationContext().getString(R.string.ok);
            labels.createFileDialogCancelButton = getApplicationContext().getString(R.string.cancel);
            labels.labelSelectButton = getApplicationContext().getString(R.string.select);
            labels.messageConfirmSelection = getApplicationContext().getString(R.string.are_you_sure);
            labels.labelConfirmYesButton = getApplicationContext().getString(R.string.txt_yes);
            labels.labelConfirmNoButton = getApplicationContext().getString(R.string.txt_no);
            labels.labelCancelButton = getApplicationContext().getString(R.string.cancel);
            dialog.setLabels(labels);
        }

        // Show the dialog.
        dialog.show();

    }

    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.hide();
            onSuccess(file.getAbsolutePath());
            tvFolderPath.setText(file.getAbsolutePath());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
            //   Toast.makeText(getApplicationContext(), name +"::::1",Toast.LENGTH_LONG).show();

        }
    };

    void onSuccess(String filepath){
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        audioFilePath.storeAudioFilePath(filepath);
        setAudioFilePath();
    }
    @OnClick(R.id.llFolderPath)
    public void llFolderSelect(View view){
        chooseAudioFile();
    }
    @OnClick(R.id.tvFolderPath)
    public void tvFolderSelect(View view){
        chooseAudioFile();
    }
    void setAudioFilePath(){
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        String path = audioFilePath.userAudioFilePathIfExist();
        tvFolderPath.setText(path);
    }
    @OnClick(R.id.spFolderPath)
    public void spFolderSelect(View view){
        chooseAudioFile();
    }
}
