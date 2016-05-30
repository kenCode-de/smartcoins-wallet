package de.bitshares_munich.smartcoinswallet;
/**
 * Created by Syed Muhammad Muzzammil on 13/5/16.
 */

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.LangCode;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

public class SettingActivity extends BaseActivity {

    final String check_for_updates = "check_for_updates";
    final String automatically_install = "automatically_install";
    final String require_pin = "require_pin";
    final String close_bitshare = "close_bitshare";
    final String always_donate = "always_donate";
    final String hide_donations = "hide_donations";
    final String date_time = "date_time";
    final String register_new_account = "register_new_account";
    String brainKey = "";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setBackButton(true);
        tinyDB = new TinyDB(getApplicationContext());
        ButterKnife.bind(this);
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        init();
        populateDropDowns();

    }

    public void init() {
        setCheckedButtons();
    }

    public void onCheck(View v) {
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
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, always_donate, true);
                else Helper.storeBoolianSharePref(this, always_donate, false);
                break;
            case R.id.hide_donations:
                if (isCHecked(v)) Helper.storeBoolianSharePref(this, hide_donations, true);
                else Helper.storeBoolianSharePref(this, hide_donations, false);
                break;
            default:
                break;
        }
    }

    public void onClickBackupBrainkeybtn(View v) {
        showDialogCopyBrainKey();
    }

    public void onClickSecurePinbtn(View v) {

        Intent intent = new Intent(getApplicationContext(), PinActivity.class);
        startActivity(intent);
        //showDialogPinRequest();
    }

    public void onClickBackbtn(View v) {
        Intent intent = new Intent(getApplicationContext(), BrainkeyActivity.class);
        startActivity(intent);
    }

    Boolean isCHecked(View v) {
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
            checkBox.setChecked(true);
        }
        if (Helper.fetchBoolianSharePref(this, hide_donations)) {
            checkBox = (CheckBox) findViewById(R.id.hide_donations);
            checkBox.setChecked(true);
        }
    }

    private void populateDropDowns() {

        ArrayList<String> countries = Helper.getCountriesArray();
        ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countries);
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapterCountry);
        final AdapterView.OnItemSelectedListener listener = spCountry.getOnItemSelectedListener();

        Boolean isCountry = Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_country));
        if (isCountry) {
            int indexCountry = Helper.fetchIntSharePref(getApplicationContext(), getString(R.string.pref_country));
            spCountry.setSelection(indexCountry);
        } else {

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
        ArrayList<String> getLangCode =
                null;
        getLangCode = Helper.getLanguages();
        for (int i = 0; i < getLangCode.size(); i++) {

            if (getLangCode.get(i).equalsIgnoreCase("zh-rTW")) {
                LangCode langCode = new LangCode();
                langCode.code = "zh-rTW";
                langCode.lang = "繁體中文";
                langArray.add(langCode);
            } else {
                LangCode langCode = new LangCode();
                Locale locale = new Locale(getLangCode.get(i));
                langCode.lang = locale.getDisplayLanguage(locale);
                langCode.code = getLangCode.get(i);
                langArray.add(langCode);
            }

        }

        ArrayAdapter<LangCode> adapterLanguage = new ArrayAdapter<LangCode>(this, android.R.layout.simple_spinner_item, langArray);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapterLanguage);
        String langCode = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
        if (langCode != "") {
            for (int i = 0; i < langArray.size(); i++) {
                LangCode lc = langArray.get(i);
                if (lc.code.equalsIgnoreCase(langCode)) {
                    spLanguage.setSelection(i);
                }
            }
        } else {
            spLanguage.setSelection(13);
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
        for (int i = 0; i < accountDetails.size(); i++) {
            arrayAccountName.add(accountDetails.get(i).account_name);
        }

        ArrayAdapter<String> adapterAccountName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAccountName);
        adapterAccountName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAccounts.setAdapter(adapterAccountName);


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
            ArrayAdapter<String> adapterAccountAssets = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAccountAssets);
            adapterAccountAssets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spBackupAsset.setAdapter(adapterAccountAssets);

            Boolean isBackupAsset = Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_backup_asset));
            if (isBackupAsset) {
                int indexBackupAsset = Helper.fetchIntSharePref(getApplicationContext(), getString(R.string.pref_backup_asset));
                spBackupAsset.setSelection(indexBackupAsset);
            } else {
                int index = arrayAccountAssets.indexOf("BTS");
                if (index >= 0) {
                    spBackupAsset.setSelection(index);
                } else {
                    spBackupAsset.setSelection(0);
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

    @OnItemSelected(R.id.spAccounts)
    void onItemSelectedAccount(int position) {
        if (position >= 0) {
            for (int i = 0; i < accountDetails.size(); i++) {

                if (spAccounts.getSelectedItem().toString().equals(accountDetails.get(i).account_name)) {
                    accountDetails.get(i).isSelected = true;
                    brainKey = accountDetails.get(i).brain_key;
                    if (accountDetails.get(i).isLifeTime) {
                        ivLifeTime.setVisibility(View.VISIBLE);
                    } else {
                        ivLifeTime.setVisibility(View.GONE);
                    }
                } else {
                    accountDetails.get(i).isSelected = false;
                }

            }
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_account_name), spAccounts.getSelectedItem().toString());
        }
    }

    @OnItemSelected(R.id.spCountry)
    void onItemSelectedCountry(int position) {
        Helper.storeStringSharePref(getApplicationContext(),getString(R.string.pref_fade_currency),spCountry.getSelectedItem().toString());
        Helper.storeIntSharePref(getApplicationContext(), getString(R.string.pref_country), position);
    }

    @OnItemSelected(R.id.spTimeZones)
    void onItemSelectedTimeZone(int position) {
        if (position > 0) {

            String temp[] = spTimeZones.getSelectedItem().toString().split(" ");
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.date_time_zone), temp[0]);
            Helper.storeIntSharePref(getApplicationContext(), getString(R.string.pref_timezone), position);
        }
    }

    @OnItemSelected(R.id.spBackupAsset)
    void onItemSelectedBackupAsset(int position) {
        if (position >= 0) {
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_backup_symbol), spBackupAsset.getSelectedItem().toString());
            Helper.storeIntSharePref(getApplicationContext(), getString(R.string.pref_backup_asset), position);
        }
    }

    @OnItemSelected(R.id.spLanguage)
    void onItemSelectedLanguage(int position) {

        LangCode langSelection = (LangCode) spLanguage.getSelectedItem();
        // Helper.setLocale(langSelection.code, getResources());
        Helper.storeStringSharePref(getApplicationContext(), getString(R.string.pref_language), langSelection.code);

    }

    private void showDialogCopyBrainKey() {
String temp=        Helper.getFadeCurrency(this);
        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(getString(R.string.backup_brainkey));
        dialog.setContentView(R.layout.activity_copybrainkey);
        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
        try {
            etBrainKey.setText(Crypt.getInstance().decrypt_string(brainKey));
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
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                clipboard.setPrimaryClip(clip);
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);

        dialog.show();
    }


}
