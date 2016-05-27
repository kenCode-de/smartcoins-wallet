package de.bitshares_munich.smartcoinswallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.utils.Helper;

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

    @Bind(R.id.tvOldPIN)
    TextView tvOldPIN;

    @Bind(R.id.btnCreate)
    Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);
        ButterKnife.bind(this);

        boolean pin = Helper.containKeySharePref(getApplicationContext(), getString(R.string.txt_pin));
        if (pin) {
            etOldPin.setVisibility(View.VISIBLE);
            tvOldPIN.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(R.string.edit_pin);
            btnCreate.setText(R.string.txt_edit);
        } else {
            etOldPin.setVisibility(View.GONE);
            tvOldPIN.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.create_pin);
            btnCreate.setText(R.string.create);
        }


    }

    @OnClick(R.id.btnCreate)
    public void create(Button button) {

        if (button.getText().toString().equalsIgnoreCase(getString(R.string.txt_edit))) {
            String oldPin = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.txt_pin));
            if (etOldPin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_old_6_digit_pin, Toast.LENGTH_SHORT).show();
            } else if (etPin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            } else if (etPinConfirmation.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            } else if (!etOldPin.getText().toString().equals(oldPin)) {
                Toast.makeText(getApplicationContext(), R.string.incorrect_old_pin, Toast.LENGTH_SHORT).show();
            } else {
                Helper.storeStringSharePref(getApplicationContext(), getString(R.string.txt_pin), etPin.getText().toString());
                Toast.makeText(getApplicationContext(), R.string.pin_changed_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (etPin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            } else if (etPinConfirmation.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            } else {
                Helper.storeStringSharePref(getApplicationContext(), getString(R.string.txt_pin), etPin.getText().toString());
                Toast.makeText(getApplicationContext(), R.string.pin_created_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}
