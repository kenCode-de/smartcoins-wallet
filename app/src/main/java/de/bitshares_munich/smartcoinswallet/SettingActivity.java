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
import android.os.Handler;
import android.preference.PreferenceManager;
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

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.bitshares_munich.Interfaces.BackupBinDelegate;
import de.bitshares_munich.Interfaces.InternalMovementListener;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.LangCode;
import de.bitshares_munich.models.MerchantEmail;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.AccountUpdateTransactionBuilder;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.TransactionBroadcastSequence;
import de.bitsharesmunich.graphenej.errors.MalformedTransactionException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

public class SettingActivity extends BaseActivity implements BackupBinDelegate {
    private final String TAG = this.getClass().getName();
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

    @Bind(R.id.tvMerchantPath)
    TextView tvMerchantPath;

    @Bind(R.id.spFolderPath)
    Spinner spFolderPath;

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


    ProgressDialog progressDialog;
    Activity activitySettings;

    String wifKey = "";

    /* Boolean variable set to true if the key update is meant for all 3 roles of the currently active account */
    private boolean updateAllRoles;
    private String oldKey;
    private AccountDetails updatedAccount;
    private int UPDATE_KEY_MAX_RETRIES = 2;
    private int updateKeyRetryCount = 0;
    private int nodeIndex = 0;

    /* Background worker threads, called in sequence */
    private WebsocketWorkerThread refreshKeyWorker;
    private WebsocketWorkerThread getAccountsWorker;

    /**
     * Listener called with the account data. This is done before the account authorities update
     * just to know what keys to update for each account.
     */
    private WitnessResponseListener getAccountsListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "getAccounts. onSuccess");
                    ArrayList<AccountDetails> details = tinyDB.getListObject(getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    AccountDetails currentAccount = null;
                    for(AccountDetails accountDetails : details){
                        if(accountDetails.isSelected){
                            currentAccount = accountDetails;
                            break;
                        }
                    }
                    List<AccountProperties> accountProperties = (List<AccountProperties>) response.result;
                    for(AccountProperties properties : accountProperties){
                        if(properties.name.equals(currentAccount.account_name)){
                            if(properties.active.equals(properties.owner)){
                                updateAllRoles = true;
                            }
                        }
                    }
                    Log.d(TAG, "Update all roles: "+updateAllRoles);
                    updateAccountAuthorities(currentAccount);
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "getAccounts.onError. Msg: "+error.message);
        }
    };

    /**
     * Listener called upon the 'account_update_operation' response.
     */
    private WitnessResponseListener mAuthorityChangeListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"onSuccess");
                    Toast.makeText(SettingActivity.this, R.string.refresh_keys_success, Toast.LENGTH_LONG).show();
                    String suggestion = "";
                    for(AccountDetails accountDetail : accountDetails){
                        if(accountDetail.account_id.equals(updatedAccount.account_id)){
                            accountDetail.wif_key = updatedAccount.wif_key;
                            accountDetail.brain_key = updatedAccount.brain_key;
                            Log.d(TAG,"updating account with name: "+accountDetail.account_name+", id: "+accountDetail.account_id+", key: "+accountDetail.brain_key);

                            suggestion = accountDetail.brain_key;
                        }
                        break;
                    }

                    tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
                    displayBrainKeyBackup();

                    /* Updating store of old keys*/
                    ArrayList<String> oldKeys = tinyDB.getListString(Constants.KEY_OLD_KEYS);
                    oldKeys.add(oldKey);
                    tinyDB.putListString(Constants.KEY_OLD_KEYS, oldKeys);

                    /* Removing brain key suggestion from shared preferences */
                    removeSuggestion(suggestion);
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.d(TAG, "onError. Msg: "+error.message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatedAccount = null;
                    if(updateKeyRetryCount < UPDATE_KEY_MAX_RETRIES){
                        Log.d(TAG, "Retrying. count: "+ updateKeyRetryCount +", max: "+ UPDATE_KEY_MAX_RETRIES);
                        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                        for(AccountDetails accountDetails : arrayList){
                            nodeIndex = (nodeIndex + 1) % Application.urlsSocketConnection.length;
                            Log.d(TAG,"account id: '"+accountDetails.account_id+"', name: "+accountDetails.account_name+", wif: "+accountDetails.wif_key);
                            if(accountDetails.isSelected){
                                updateAccountAuthorities(accountDetails);
                                updateKeyRetryCount++;
                                break;
                            }
                        }
                    }else{
                        Toast.makeText(SettingActivity.this, R.string.refresh_keys_fail, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        tvMerchantPath.setText(MerchantEmail.getPath());
        updateBlockNumberHead();

    }

    public void init() {
        setCheckedButtons();
        initAudioPath();
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
        displayBrainKeyBackup();
    }

    public void onClickSecurePinbtn(View v) {
        designMethod();
        Intent intent = new Intent(getApplicationContext(), PinActivity.class);
        ((InternalMovementListener) this).onInternalAppMove();
        startActivity(intent);
    }

    public void onClickBackbtn(View v) {
        designMethod();
        Intent intent = new Intent(getApplicationContext(), BrainkeyActivity.class);
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
        String result = "";
        if (hours > 0) {

            result = String.format("%s (GMT+%d:%02d)", tz.getID(), hours, minutes);
        } else {
            result = String.format("%s (GMT%d:%02d)", tz.getID(), hours, minutes);
        }

        return result;
    }
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

        Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_language), langSelection.code);

        if (inittLocale) {

            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        inittLocale = true;

    }

    /**
     * Will display a dialog prompting the user to make a backup of the brain key.
     */
    private void displayBrainKeyBackup() {
        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(getString(R.string.backup_brainkey));
        dialog.setContentView(R.layout.activity_copybrainkey);
        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
        try {
            String brainKey = getBrainKey();
            if (brainKey.isEmpty()) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_load_brainkey),Toast.LENGTH_LONG).show();
                return;
            } else {
                etBrainKey.setText(brainKey);
            }
        } catch (Exception e) {

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
                if (Application.isConnected()) {

                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();

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

    @OnClick(R.id.refresh_account_keys)
    void onRefreshAccountKeysPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.refresh_keys_title))
            .setMessage(getResources().getString(R.string.refresh_keys_summary))
            .setPositiveButton(getResources().getString(R.string.dialog_proceed), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick");
                    ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    for(AccountDetails accountDetails : arrayList){
                        if(accountDetails.isSelected){
                            checkAccountPermissions(accountDetails.account_id);
                            break;
                        }
                    }
                }
            }).setNegativeButton(getResources().getString(R.string.dialog_later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        builder.create().show();
    }

    private void checkAccountPermissions(String accountId){
        /* Asking for all account details */
        getAccountsWorker = new WebsocketWorkerThread(new GetAccounts(accountId, this.getAccountsListener));
        getAccountsWorker.start();
    }

    /**
     * Method that will actually perform a call to the full node and update the key currently
     * controlling the account passed as a parameter.
     *
     * @param accountDetails: The account whose key we want to update.
     */
    private void updateAccountAuthorities(AccountDetails accountDetails) {
        Log.d(TAG,"account to update. current brain key: "+accountDetails.brain_key);
        updatedAccount = accountDetails;
        try {
            String currentWif = Crypt.getInstance().decrypt_string(updatedAccount.wif_key);
            oldKey = String.format("%s:%s", updatedAccount.account_name, currentWif);

            // Coming up with a new brainkey suggestion
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(AccountActivity.BRAINKEY_FILE), "UTF-8"));
            String dictionary = reader.readLine();
            String suggestion = BrainKey.suggest(dictionary);
            BrainKey brainKey = new BrainKey(suggestion, 0);
            Log.d(TAG,"new brain key: "+suggestion);

            /* Keeping this suggestion in shared preferences in case we get interrupted */
            storeSuggestion(suggestion);

            // Keeping a reference of the account to be changed, with the updated values
            Address address = new Address(ECKey.fromPublicOnly(brainKey.getPrivateKey().getPubKey()));
            updatedAccount.wif_key = Crypt.getInstance().encrypt_string(brainKey.getWalletImportFormat());
            updatedAccount.brain_key = suggestion;
            updatedAccount.pub_key = address.toString();
            updatedAccount.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;

            // Building a transaction that will be used to update the account key
            HashMap<PublicKey, Integer> authMap = new HashMap<>();
            authMap.put(address.getPublicKey(), 1);
            Authority authority = new Authority(1, authMap, null);
            AccountOptions options = new AccountOptions(address.getPublicKey());
            AccountUpdateTransactionBuilder builder = new AccountUpdateTransactionBuilder(DumpedPrivateKey.fromBase58(null, currentWif).getKey())
                    .setAccont(new UserAccount(accountDetails.account_id))
                    .setActive(authority)
                    .setOptions(options);

            if(updateAllRoles){
                builder.setOwner(authority);
            }

            Transaction transaction = builder.build();
            refreshKeyWorker = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, new Asset("1.3.0"), mAuthorityChangeListener), nodeIndex);
            refreshKeyWorker.start();
        } catch (MalformedTransactionException e) {
            Log.e(TAG, "MalformedTransactionException. Msg: "+e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException. Msg: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "NoSuchPaddingException. Msg: "+e.getMessage());
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException. Msg: "+e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "InvalidAlgorithmParameterException. Msg: "+e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException. Msg: "+e.getMessage());
        } catch (BadPaddingException e) {
            Log.e(TAG, "BadPaddingException. Msg: "+e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException. Msg: "+e.getMessage());
        }
    }

    /**
     * We use this method to store a brain key suggestion in shared preferences
     * just in case the update procedure is interrupted.
     * @param suggestion
     */
    private void storeSuggestion(String suggestion){
        Log.d(TAG,"storeSuggestion. suggestion: "+suggestion);
        ArrayList<String> suggestionList = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
        if(suggestionList.size() > 0){
            Log.w(TAG,"Already have a previous suggestion!");
        }
        suggestionList.add(suggestion);
        tinyDB.putListString(Constants.KEY_SUGGESTED_BRAIN_KEY, suggestionList);
    }

    /**
     * Once the brain key procedure has finished, we no longer need to keep this brain key suggestion
     * in here.
     * @param suggestion
     */
    private void removeSuggestion(String suggestion){
        Log.d(TAG,"removeSuggestion. suggestion: "+suggestion);
        /* Checking that suggestion matches our memory-stored brain key */
        ArrayList<String> savedSuggestions = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
        if(savedSuggestions.size() > 0){
            if(savedSuggestions.size() > 1){
                Log.w(TAG,"Have more than one suggestion in memory");
            }
            for(int i = 0; i < savedSuggestions.size(); i++){
                if(savedSuggestions.get(i).equals(suggestion)){
                    savedSuggestions.remove(i);
                    break;
                }
            }
            if(savedSuggestions.size() == 0){
                Log.d(TAG,"saving empty suggestion list, this is expected");
            }else{
                Log.w(TAG,"even after removing suggestion, the list was not empty, signaling that a previous account update operation could have been interrupted");
            }
            tinyDB.putListString(Constants.KEY_SUGGESTED_BRAIN_KEY, savedSuggestions);
        }else{
            Log.w(TAG,"No saved suggestion");
        }
    }

    @OnClick(R.id.register_new_account)
    void setRegisterNewAccount() {
        if(Application.accountCanCreate()) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra("activity_name", "setting_screen");
            intent.putExtra("activity_id", 919);
            ((InternalMovementListener) this).onInternalAppMove();
            startActivity(intent);
        }else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.account_create_msg) , Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.import_new_account)
    void setImport_new_account() {
        Intent intent = new Intent(getApplicationContext(), ExistingAccountActivity.class);
        ((InternalMovementListener) this).onInternalAppMove();
        startActivity(intent);
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
    }

    public void clearApplicationData(Context mContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pref_agreement), true);

        Intent k = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        k.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(k);
        finish();
    }

    public void getAccountUpgradeInfo(final Activity activity, final String accountName) {
        //Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
        //Todo evaluate removal
        /*ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
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
        });*/
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

    @OnClick(R.id.backup_ic)
    public void onClickBackupDotBin()
    {

        String _brnKey = getBrainKey();
        String _accountName = getAccountName();
        String _pinCode = getPin();

        BinHelper myBinHelper = new BinHelper(this, this);
        myBinHelper.createBackupBinFile(_brnKey,_accountName,_pinCode);
    }

    @Override
    public void backupComplete(boolean success) {
        Log.d("Backup Complete","done");
    }



    FileChooserDialog dialog;
    ArrayList<String> list = new ArrayList<>();
    String itemSelected;
    String selected;


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

        this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if(itemSelected.contains(getString(R.string.change))){
                    initAudioPath();
                }
            }
        });

        // Show the dialog.
        dialog.show();

    }

    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.hide();
            onSuccess(file.getAbsolutePath(),file.getName());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
        }
    };

    void onSuccess(String filepath,String fileName){
        dialog = null;
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        audioFilePath.storeAudioFilePath(filepath);
        audioFilePath.storeAudioFileName(fileName);
        audioFilePath.storeAudioEnabled(false);
        initAudioPath();
    }
    void setAudioFilePath(){
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        selected = audioFilePath.userAudioFileNameIfExist();
    }

    Boolean checkAudioStatus(){
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        return audioFilePath.fetchAudioEnabled();
    }
    void initAudioPath(){

        setAudioFilePath();

        list.clear();

        if(checkAudioStatus()) {
            list.add(0, "-------");
            list.add(1, selected);
        }else {
            list.add(0, selected);
            list.add(1, "-------");
        }


        list.add(2,getString(R.string.change));


        ArrayAdapter<String> adapterAccountAssets = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapterAccountAssets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFolderPath.setAdapter(adapterAccountAssets);

    }
    Boolean startup = false;

    @OnItemSelected(R.id.spFolderPath)
    public void onItemSelectedAudio()
    {
        if(startup) {
            AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
            String selectedString = spFolderPath.getSelectedItem().toString();
            itemSelected = selectedString;
            if (selectedString.contains(getString(R.string.change))) {
                chooseAudioFile();
            } else if (selectedString.equals("-------")) {
                audioFilePath.storeAudioEnabled(true);
            } else {
                audioFilePath.storeAudioEnabled(false);
            }
        }else{
            startup = true;
        }

    }

    @OnClick(R.id.backup_emails)
    public void onClickBackupEmails() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MerchantEmailActivity(activity);
            }
        });
    }
}
