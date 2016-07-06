package de.bitshares_munich.smartcoinswallet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by qasim on 5/27/16.
 */
public class PinActivity extends BaseActivity {


    @Bind(R.id.etOldPin)
    EditText etOldPin;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    @Bind(R.id.btnEdit)
    Button btnEdit;

    TinyDB tinyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(getApplicationContext());
        setBackButton(true);
        setTitle(getResources().getString(R.string.app_name));

    }

    @OnClick(R.id.btnEdit)
    public void create(Button button) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String pin = "";
        for (int i = 0; i < accountDetails.size(); i++) {

            if (accountDetails.get(i).isSelected) {
                pin = accountDetails.get(i).pinCode;
                accountDetails.get(i).pinCode = etPin.getText().toString();
                break;
            }

        }
        if (etOldPin.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_old_6_digit_pin, Toast.LENGTH_SHORT).show();
        } else if (etPin.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
        } else if (etPinConfirmation.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
        } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
        } else if (!etOldPin.getText().toString().equals(pin)) {
            Toast.makeText(getApplicationContext(), R.string.incorrect_old_pin, Toast.LENGTH_SHORT).show();
        } else {
            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
            Toast.makeText(getApplicationContext(), R.string.pin_changed_successfully, Toast.LENGTH_SHORT).show();
            finish();
        }


    }
}
