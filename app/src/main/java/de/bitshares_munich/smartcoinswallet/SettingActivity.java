package de.bitshares_munich.smartcoinswallet;
/**
 * Created by Syed Muhammad Muzzammil on 13/5/16.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.LangCode;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

public class SettingActivity extends AppCompatActivity {

    final String check_for_updates = "check_for_updates";
    final String automatically_install = "automatically_install";
    final String require_pin = "require_pin";
    final String close_bitshare = "close_bitshare";
    final String always_donate = "always_donate";
    final String hide_donations = "hide_donations";
    final String taxable_country = "taxable_country";
    final String preferred_lang = "preferred_lang";
    final String backup_asset = "backup_asset";
    final String date_time = "date_time";
    final String register_new_account = "register_new_account";

    ArrayAdapter<String> iniAdapter;
    @Bind(R.id.spCountry)
    Spinner spCountry;

    @Bind(R.id.spLanguage)
    Spinner spLanguage;

    @Bind(R.id.spTimeZones)
    Spinner spTimeZones;

    @Bind(R.id.spAccounts)
    Spinner spAccounts;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        tinyDB = new TinyDB(getApplicationContext());
        ButterKnife.bind(this);
        init();
        populateDropDowns();
    }

    public void init() {
      /*  int setSelection=-1;
        Spinner spinner1backup_asset = (Spinner) findViewById(R.id.spinner1backup_asset);
        createSpinner(spinner1backup_asset);
        spinner1backup_asset.setOnItemSelectedListener(new SpinnerActivity(0));
        setSelection = selectionPostion(backup_asset);
        if(setSelection!=-1) spinner1backup_asset.setSelection(setSelection);
        Spinner spCountry = (Spinner) findViewById(R.id.spCountry);
        createSpinner(spCountry);
        spinner2taxable_country.setOnItemSelectedListener(new SpinnerActivity(1));
        setSelection = selectionPostion(taxable_country);
        if(setSelection!=-1) spinner2taxable_country.setSelection(setSelection);
        Spinner spinner3preferred_lang = (Spinner) findViewById(R.id.spinner3preferred_lang);
        createSpinner(spinner3preferred_lang);
        spinner3preferred_lang.setOnItemSelectedListener(new SpinnerActivity(2));
        setSelection = selectionPostion(preferred_lang);
        if(setSelection!=-1) spinner3preferred_lang.setSelection(setSelection);
        Spinner spinner4date_time = (Spinner) findViewById(R.id.spinner4date_time);
        createSpinner(spinner4date_time);
        spinner4date_time.setOnItemSelectedListener(new SpinnerActivity(3));
        setSelection = selectionPostion(date_time);
        if(setSelection!=-1) spinner4date_time.setSelection(setSelection);
        setCheckedButtons();
        Spinner spinner5imported_created_accounts = (Spinner) findViewById(R.id.spinner5_imported_created_accounts);
        createSpinner(spinner5imported_created_accounts);
        spinner5imported_created_accounts.setOnItemSelectedListener(new SpinnerActivity(4));
        setSelection = selectionPostion(register_new_account);
        if(setSelection!=-1) spinner5imported_created_accounts.setSelection(setSelection);*/
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

    }

    public void onClickSecurePinbtn(View v) {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.settings_dialog);
        dialog.show();
    }

    public void onClickBackbtn(View v) {

    }

    Boolean isCHecked(View v) {
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            return true;
        }
        return false;
    }

    public void saveinPref(int id, String value) {
        switch (id) {
            case 0:
                Helper.storeStringSharePref(this, backup_asset, value);
                break;
            case 1:
                Helper.storeStringSharePref(this, taxable_country, value);
                break;
            case 2:
                Helper.storeStringSharePref(this, preferred_lang, value);
                break;
            case 3:
                Helper.storeStringSharePref(this, date_time, value);
                break;
            case 4:
                Helper.storeStringSharePref(this, register_new_account, value);
                break;
        }
    }

    public int selectionPostion(String compareValue) {
        compareValue = Helper.fetchStringSharePref(this, compareValue);
        if (!compareValue.equals(null)) {
            return iniAdapter.getPosition(compareValue);
        }
        return -1;
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
        String country = Helper.fetchStringSharePref(getApplicationContext(), "country");
        if (country != "") {
            spCountry.setSelection(Integer.parseInt(country));
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
        String langCode = Helper.fetchStringSharePref(getApplicationContext(), "language");
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


        // AccountsName
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<String> arrayAccountName = new ArrayList<>();
        for (int i = 0; i < accountDetails.size(); i++) {
            arrayAccountName.add(accountDetails.get(i).account_name);

        }
        Collections.sort(arrayAccountName);

        ArrayAdapter<String> adapterAccountName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAccountName);
        adapterAccountName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAccounts.setAdapter(adapterAccountName);

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

}
