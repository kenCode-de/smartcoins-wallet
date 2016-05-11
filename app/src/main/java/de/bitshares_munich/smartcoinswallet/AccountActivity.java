package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;


public class AccountActivity extends AppCompatActivity {

    @Bind(R.id.etAccountName)
    EditText etAccountName;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;
    public int socketCounter;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);
        validationAccountName();
        gson = new Gson();

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
            myLowerCaseTimer.start();
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
        socketCounter = 0;
        if (Application.webSocketG.isOpen()) {
            Application.webSocketG.send("{\"id\":2,\"method\":\"call\",\"params\":[1,\"login\",[\"\",\"\"]]}");
            Application.webSocketG.setStringCallback(new WebSocket.StringCallback() {
                public void onStringAvailable(String s) {
                    if (s.contains("true")) {
                        Application.webSocketG.send("{\"method\":\"call\",\"params\":[1,\"database\",[]],\"id\":2}");
                        Application.webSocketG.send("{\"method\":\"call\",\"params\":[1,\"network_broadcast\",[]],\"id\":3}");
                        Application.webSocketG.send("{\"method\":\"call\",\"params\":[1,\"history\",[]],\"id\":4}");
                        socketCounter = 1;
                    } else if (socketCounter == 1) {
                        try {
                            System.out.println("I got a string: " + s);
                            JSONObject jsonObject = new JSONObject(s);
                            int id = jsonObject.getInt("id");
                            if (id == 2) {
                                Helper.storeIntSharePref(getApplicationContext(), getString(R.string.sharePref_database), jsonObject.getInt("result"));
                            } else if (id == 3) {
                                Helper.storeIntSharePref(getApplicationContext(), getString(R.string.sharePref_network_broadcast), jsonObject.getInt("result"));
                            } else if (id == 4) {
                                Helper.storeIntSharePref(getApplicationContext(), getString(R.string.sharePref_history), jsonObject.getInt("result"));
                            }
                        } catch (JSONException e) {

                        }

                    }


                }
            });
        }

    }

    @OnClick(R.id.tvExistingAccount)
    public void existingAccount(TextView textView) {
        Intent intent = new Intent(getApplicationContext(), ExistingAccountActivity.class);
        startActivity(intent);
    }


}
