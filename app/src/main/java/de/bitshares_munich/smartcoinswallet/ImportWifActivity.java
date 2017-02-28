package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Vinícius on 2/28/17.
 */
public class ImportWifActivity extends BaseActivity {
    private String TAG = this.getClass().getName();

    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;
    Boolean isBackupKey = false;

    @Bind(R.id.etWif)
    TextView etWif;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wif);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(getApplicationContext());

    }

    @OnClick(R.id.btnWallet)
    public void wallet(Button button) {
        if (etWif.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_wif, Toast.LENGTH_SHORT).show();
        }
        else {
            String trimmedBrainKey = etWif.getText().toString().trim();
            etWif.setText(trimmedBrainKey);
            if (etPin.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            }
            //PIN must have minimum of 6-digit
            else if (etPin.getText().length() < 6) {
                Toast.makeText(getApplicationContext(), R.string.pin_number_warning, Toast.LENGTH_SHORT).show();
            } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            } else {
                load(etPin.getText().toString());
            }
        }
    }

    void load(String pinCode) {
        String wifText = etWif.getText().toString();
        Helper.wifChecksumChecking(wifText);
        /*
        if (brainKeyText.contains(" ")) {
            String arr[] = brainKeyText.split(" ");
            if (arr.length >= 12 && arr.length <= 16) {

                if (checkBrainKeyExist(brainKeyText)) {
                    Toast.makeText(getApplicationContext(), R.string.account_already_exist, Toast.LENGTH_SHORT).show();
                } else {
                    showDialog("", getString(R.string.importing_your_wallet));
                    getAccountFromBrainkey(brainKeyText, pinCode);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
        }*/
    }

}
