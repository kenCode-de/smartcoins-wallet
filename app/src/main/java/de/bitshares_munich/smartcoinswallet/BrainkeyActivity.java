package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrainkeyActivity extends BaseActivity {

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    @Bind(R.id.etBrainKey)
    EditText etBrainKey;

    ProgressDialog progressDialog;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brainkey);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        tinyDB = new TinyDB(getApplicationContext());
    }

    @OnClick(R.id.btnCancel)
    public void cancel(Button button) {
        this.finish();
    }

    @OnClick(R.id.btnWallet)
    public void wallet(Button button) {

        if (etPin.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
        } else if (etPinConfirmation.getText().length() < 5) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
        } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
        } else if (etBrainKey.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        } else {
            String temp = etBrainKey.getText().toString();
            if (temp.contains(" ")) {
                String arr[] = temp.split(" ");
                if (arr.length == 16) {
                    showDialog("", getString(R.string.importing_your_wallet));
                    get_account_from_brainkey(this);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void get_account_from_brainkey(final Activity activity) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "get_account_from_brainkey");
        hashMap.put("brainkey", etBrainKey.getText().toString());
        final Call<AccountDetails> postingService = service.getAccount(hashMap);
        postingService.enqueue(new Callback<AccountDetails>() {
            @Override
            public void onResponse(Response<AccountDetails> response) {
                if (response.isSuccess()) {
                    hideDialog();
                    AccountDetails accountDetails = response.body();
                    if (accountDetails.status.equals("failure")) {
                        Toast.makeText(activity, accountDetails.msg, Toast.LENGTH_SHORT).show();
                    } else {
                        tinyDB.putObject(getString(R.string.pref_account_from_brainkey), accountDetails);
                        Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                        startActivity(intent);
                    }


                } else {
                    hideDialog();
                    Toast.makeText(activity, activity.getString(R.string.unable_to_create_account_from_brainkey), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
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

    private void hideDialog() {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }

    }


}
