package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */

public class RequestActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.popwin1)
    TextView popwin;

    String to = "";
    String account_id = "";

    Locale locale;
    NumberFormat format;
    String language;
    @Bind(R.id.btnOne) Button btnOne;
    @Bind(R.id.btnTwo) Button btnTwo;
    @Bind(R.id.btnThree) Button btnThree;
    @Bind(R.id.btnFour) Button btnFour;
    @Bind(R.id.btnFive) Button btnFive;
    @Bind(R.id.btnSix) Button btnSix;
    @Bind(R.id.btnSeven) Button btnSeven;
    @Bind(R.id.btnEight) Button btnEight;
    @Bind(R.id.btnNine) Button btnNine;
    @Bind(R.id.btnZero) Button btnZero;
    @Bind(R.id.btnDot) Button btnDot;
    @Bind(R.id.btnDoubleZero) Button btnDoubleZero;
    @Bind(R.id.txtScreen) TextView txtScreen;
    @Bind(R.id.tvNext) TextView tvNext;
    @Bind(R.id.llNext) LinearLayout llNext;

    /* Internal attribute used to keep track of the activity state */
    private boolean mRestarting;

    /* Pin pinDialog */
    private Dialog pinDialog;

    /* TinyDB instance */
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_screen);
        ButterKnife.bind(this);
        forwardDisabling();
        setBackButton(true);
        setTitle(getResources().getString(R.string.request_amount_screen_name));
        language = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
        locale = new Locale(language);
        format = NumberFormat.getInstance(locale);
        fieldsReference();

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.to))) {
            to = intent.getStringExtra(getString(R.string.to));
        }
        if (intent.hasExtra(getString(R.string.account_id))) {
            account_id = intent.getStringExtra(getString(R.string.account_id));
        }

        tinyDB = new TinyDB(getApplicationContext());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mRestarting = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        * Ask for pin number if this is not a restart (first time) or
        * if this is not we coming back from an internal move.
        */
        if(mRestarting){
            showDialogPin();
        }
    }

    // Block for pin
    private void showDialogPin() {
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        pinDialog = new Dialog(RequestActivity.this);
        pinDialog.setTitle(R.string.txt_6_digits_pin);
        pinDialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) pinDialog.findViewById(R.id.btnDone);
        final EditText etPin = (EditText) pinDialog.findViewById(R.id.etPin);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                            pinDialog.cancel();
                            break;
                        }else{
                            Toast.makeText(RequestActivity.this, getResources().getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        pinDialog.setCancelable(false);
        pinDialog.show();
    }

    @OnClick(R.id.backbutton)
    void onBackButtonPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.tvCancel)
    void cancel() {
        Intent intent = new Intent(getApplicationContext(), RecieveActivity.class);
        intent.putExtra(getString(R.string.currency), popwin.getText().toString());
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), account_id);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.tvNext)
    void next() {
        try {
            Number number = format.parse(removeSpecialCharacters());
            if (number.doubleValue() > 0) {
                String amount = number.toString();
                Intent intent = new Intent(getApplicationContext(), RecieveActivity.class);
                intent.putExtra(getString(R.string.currency), popwin.getText().toString());
                intent.putExtra(getString(R.string.to), to);
                intent.putExtra(getString(R.string.price), amount);
                intent.putExtra(getString(R.string.account_id), account_id);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_enter_valid_amount, Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public void backbtn(View v) {

        String str = txtScreen.getText().toString();
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
            txtScreen.setText(str);
            try {

                if (!txtScreen.getText().toString().isEmpty()) {
                    Number number = format.parse(removeSpecialCharacters());
                    if (number.doubleValue() > 0) {
                        forwardEnabling();
                    } else {
                        forwardDisabling();
                    }
                    txtScreen.setText(Helper.setLocaleNumberFormat(locale, number));
                } else {
                    forwardDisabling();
                }
            } catch (ParseException e) {

                e.printStackTrace();

            }
        } else {
            forwardDisabling();
        }
    }

    public String method(String str) {
        str = str.substring(0, str.length() - 1);
        return str;
    }

    public void popupwindow(View v, TextView textview) {
        popUpwindow p = new popUpwindow(this, textview);
        p.show(v);
    }

    public void showpop(View v) {
        popupwindow(v, popwin);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String currentkey = button.getText().toString();
        String dot = btnDot.getText().toString();
        if (currentkey.equals(dot)) {
            txtScreen.append(currentkey);
        } else if (currentkey.equals(btnZero.getText().toString())) {
            txtScreen.append(currentkey);
        } else if (currentkey.equals(btnDoubleZero.getText().toString())) {
            txtScreen.append(currentkey);
        } else {

            txtScreen.append(currentkey);
            try {
                Number number = format.parse(removeSpecialCharacters());
                if (number.doubleValue() > 0) {
                    forwardEnabling();
                } else {
                    forwardDisabling();
                }
                txtScreen.setText(Helper.setLocaleNumberFormat(locale, number));
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }
    }

    private void fieldsReference() {

        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
        btnZero.setOnClickListener(this);
        btnDoubleZero.setOnClickListener(this);
        btnDot.setOnClickListener(this);
        keypadNumbersLocalization();

    }

    private void keypadNumbersLocalization() {

        btnOne.setText(Helper.setLocaleNumberFormat(locale, 1));
        btnTwo.setText(Helper.setLocaleNumberFormat(locale, 2));
        btnThree.setText(Helper.setLocaleNumberFormat(locale, 3));
        btnFour.setText(Helper.setLocaleNumberFormat(locale, 4));
        btnFive.setText(Helper.setLocaleNumberFormat(locale, 5));
        btnSix.setText(Helper.setLocaleNumberFormat(locale, 6));
        btnSeven.setText(Helper.setLocaleNumberFormat(locale, 7));
        btnEight.setText(Helper.setLocaleNumberFormat(locale, 8));
        btnNine.setText(Helper.setLocaleNumberFormat(locale, 9));
        btnZero.setText(Helper.setLocaleNumberFormat(locale, 0));
        btnDot.setText(String.valueOf(Helper.setDecimalSeparator(locale)));
        btnDoubleZero.setText(Helper.setLocaleNumberFormat(locale, 0) + "" + Helper.setLocaleNumberFormat(locale, 0));
    }

    private String removeSpecialCharacters() {
        //Farsi and arabic
        String inputNumber = txtScreen.getText().toString();
        String dot = btnDot.getText().toString();
        inputNumber = inputNumber.replace("٬", "");
        inputNumber = inputNumber.replace(String.valueOf((char) 160), "");
        if (dot.equals(",")) {
            inputNumber = inputNumber.replace(".", "");
        } else if (dot.equals(".")) {
            inputNumber = inputNumber.replace(",", "");
        }
        return inputNumber;
    }

    private void forwardEnabling() {
        tvNext.setEnabled(true);
        llNext.setBackgroundColor(Color.rgb(112, 136, 46));
    }

    private void forwardDisabling() {
        tvNext.setEnabled(false);
        llNext.setBackgroundColor(Color.rgb(211, 211, 211));
    }


}