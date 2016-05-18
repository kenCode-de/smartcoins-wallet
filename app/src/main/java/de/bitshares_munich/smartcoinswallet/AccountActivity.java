package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;


public class AccountActivity extends AppCompatActivity implements IAccount {

    @Bind(R.id.etAccountName)
    EditText etAccountName;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    Gson gson;
    ProgressDialog progressDialog;
    Application application;

    @Bind(R.id.tvErrorAccountName)
    TextView tvErrorAccountName;

    @Bind(R.id.tvBlockNumberHead)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion)
    TextView tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);
        tvAppVersion.setText("v"+BuildConfig.VERSION_NAME+getString(R.string.beta));
        validationAccountName();
        gson = new Gson();
        application = new Application();
        application.registerCallback(this);
        progressDialog = new ProgressDialog(this);
        updateBlockNumberHead();

    }

    CountDownTimer myLowerCaseTimer = new CountDownTimer(500, 500) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            if (!etAccountName.getText().toString().equals(etAccountName.getText().toString().toLowerCase())) {
                etAccountName.setText(etAccountName.getText().toString().toLowerCase());
                etAccountName.setSelection(etAccountName.getText().toString().length());
            }
        }
    };

    @OnTextChanged(R.id.etAccountName)
    void onTextChanged(CharSequence text) {

        if (etAccountName.getText().length() > 0) {
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }

    }

    CountDownTimer myAccountNameValidationTimer = new CountDownTimer(3000, 3000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            createBitShareAN(false);
        }
    };


    public void createBitShareAN(boolean focused) {
        if (!focused) {

            if (etAccountName.getText().length() > 2) {
                tvErrorAccountName.setText("");
                tvErrorAccountName.setVisibility(View.GONE);
                if (Application.webSocketG.isOpen()) {
                    // int databaseIndent=Helper.fetchIntSharePref(getApplicationContext(),getString(R.string.sharePref_database));
                    String socketText = getString(R.string.lookup_account_a) + "\"" + etAccountName.getText().toString() + "\"" + ",50]],\"id\": 6}";
                    Application.webSocketG.send(socketText);

                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Helper.containKeySharePref(getApplicationContext(), getString(R.string.agreement))) {
            showDialogLiscence();
        }
    }

    private void showDialogLiscence() {
        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(R.string.agreement);
        dialog.setContentView(R.layout.custom_dialog_liscence);
        Button dialog_btn_cancel = (Button) dialog.findViewById(R.id.dialog_btn_cancel);
        dialog_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
        Button dialog_btn_agree = (Button) dialog.findViewById(R.id.dialog_btn_agree);
        dialog_btn_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.agreement), true);
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);

        dialog.show();
    }

    private void validationAccountName() {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                for (int i = start; i < end; i++) {
                    if ((dstart == 0) && (!Character.isLetter(source.charAt(0)))) {
                        return "";
                    } else if (!Character.isLetterOrDigit(source.charAt(i)) && (source.charAt(i) != '-')) {
                        return "";
                    }
                }

                return null;
            }
        };
        etAccountName.setFilters(new InputFilter[]{filter});

    }

    @OnClick(R.id.btnCreate)
    public void create(Button button) {

      /*  if (etAccountName.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.kindly_create_account, Toast.LENGTH_SHORT).show();
        } else if (etPin.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
        } else if (etPinConfirmation.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
        } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
        } else {
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.sharePref_account_name), etAccountName.getText().toString());
            Helper.storeStringSharePref(getApplicationContext(), getString(R.string.txt_pin), etPin.getText().toString());
        }*/
        //SocketCounter 1 (Database) SocketCounter 2 (History)


    }

    @OnClick(R.id.tvExistingAccount)
    public void existingAccount(TextView textView) {
        Intent intent = new Intent(getApplicationContext(), ExistingAccountActivity.class);
        startActivity(intent);
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

    private void hideDialog() {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.hide();
            }
        }

    }

    @Override
    public void checkAccount(JSONObject jsonObject) {

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                String temp = jsonArray.getJSONArray(i).getString(0);
                if (temp.equals(etAccountName.getText().toString())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvErrorAccountName.setText(getString(R.string.account_name_already_exist));
                            tvErrorAccountName.setVisibility(View.VISIBLE);
                        }
                    });

                    break;
                }

            }
        } catch (Exception e) {

        }
    }

    private void updateBlockNumberHead()
    {
        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                tvBlockNumberHead.setText(Application.blockHead);
                handler.postDelayed(this, 3000);
            }
        };

        String versionName = BuildConfig.VERSION_NAME;
        handler.postDelayed(updateTask, 3000);

    }
}
