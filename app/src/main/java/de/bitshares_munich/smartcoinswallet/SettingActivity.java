package de.bitshares_munich.smartcoinswallet;
/**
 * Created by Syed Muhammad Muzzammil on 13/5/16.
 */
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.utils.Helper;

public class SettingActivity extends AppCompatActivity  {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
    }
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        int selectedid;
        SpinnerActivity(int id){
            selectedid = id;
        }
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // On selecting a spinner item
            String item = parent.getItemAtPosition(position).toString();
            saveinPref(selectedid,item);
        }
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
    public void init(){
        int setSelection=-1;
        Spinner spinner1backup_asset = (Spinner) findViewById(R.id.spinner1backup_asset);
        createSpinner(spinner1backup_asset);
        spinner1backup_asset.setOnItemSelectedListener(new SpinnerActivity(0));
        setSelection = selectionPostion(backup_asset);
        if(setSelection!=-1) spinner1backup_asset.setSelection(setSelection);
        Spinner spinner2taxable_country = (Spinner) findViewById(R.id.spinner2taxable_country);
        createSpinner(spinner2taxable_country);
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
        if(setSelection!=-1) spinner5imported_created_accounts.setSelection(setSelection);
        setCheckedButtons();
    }
    public void createSpinner(Spinner spinner){
        List<String> categories = new ArrayList<String>();
        categories.add("Automobile");
        categories.add("Business Services");
        categories.add("Computers");
        categories.add("Education");
        categories.add("Personal");
        categories.add("Travel");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        iniAdapter = dataAdapter;
    }
    public void onCheck(View v){
        switch (v.getId()){
            case R.id.check_for_updates:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,check_for_updates,true);
                else Helper.storeBoolianSharePref(this,check_for_updates,false);
                break;
            case R.id.automatically_install:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,automatically_install,true);
                else Helper.storeBoolianSharePref(this,automatically_install,false);
                break;
            case R.id.require_pin:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,require_pin,true);
                else Helper.storeBoolianSharePref(this,require_pin,false);
                break;
            case R.id.close_bitshare:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,close_bitshare,true);
                else Helper.storeBoolianSharePref(this,close_bitshare,false);
                break;
            case R.id.always_donate:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,always_donate,true);
                else Helper.storeBoolianSharePref(this,always_donate,false);
                break;
            case R.id.hide_donations:
                if(isCHecked(v)) Helper.storeBoolianSharePref(this,hide_donations,true);
                else Helper.storeBoolianSharePref(this,hide_donations,false);
                break;
            default:break;
        }
    }
    public void onClickBackupBrainkeybtn(View v){

    }
    public void onClickSecurePinbtn(View v){
                final Dialog dialog = new Dialog(SettingActivity.this);
                dialog.setContentView(R.layout.settings_dialog);
                dialog.show();
    }
    public void onClickBackbtn(View v){

    }
    Boolean isCHecked(View v){
        CheckBox checkBox = (CheckBox)v;
        if(checkBox.isChecked()){
            return true;
        }
        return false;
    }
    public void saveinPref(int id,String value){
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
    public int selectionPostion(String compareValue){
        compareValue = Helper.fetchStringSharePref(this,compareValue);
        if (!compareValue.equals(null)) {
           return iniAdapter.getPosition(compareValue);
        }
    return -1;
    }
    void setCheckedButtons(){
        CheckBox checkBox;
    if(Helper.fetchBoolianSharePref(this,check_for_updates)){
        checkBox = (CheckBox)findViewById(R.id.check_for_updates);
        checkBox.setChecked(true);
    }
        if(Helper.fetchBoolianSharePref(this,automatically_install)){
            checkBox = (CheckBox)findViewById(R.id.automatically_install);
            checkBox.setChecked(true);
        }
        if(Helper.fetchBoolianSharePref(this,require_pin)){
            checkBox = (CheckBox)findViewById(R.id.require_pin);
            checkBox.setChecked(true);
        }
        if(Helper.fetchBoolianSharePref(this,close_bitshare)){
            checkBox = (CheckBox)findViewById(R.id.close_bitshare);
            checkBox.setChecked(true);
        }
        if(Helper.fetchBoolianSharePref(this,always_donate)){
            checkBox = (CheckBox)findViewById(R.id.always_donate);
            checkBox.setChecked(true);
        }
        if(Helper.fetchBoolianSharePref(this,hide_donations)){
            checkBox = (CheckBox)findViewById(R.id.hide_donations);
            checkBox.setChecked(true);
        }
    }
}
