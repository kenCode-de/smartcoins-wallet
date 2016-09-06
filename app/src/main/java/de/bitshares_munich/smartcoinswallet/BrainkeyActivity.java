package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
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

    @Bind(R.id.tvBlockNumberHead_brain_key_activity)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_brain_key_activity)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected_brain_key_activity)
    ImageView ivSocketConnected;

    ProgressDialog progressDialog;
    TinyDB tinyDB;
    Boolean settingScreen = false;

    @Bind(R.id.tvPin)
    TextView tvPin;

    @Bind(R.id.tvPinConfirmation)
    TextView tvPinConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brainkey);
        ButterKnife.bind(this);
        setBackButton(true);
        setTitle(getResources().getString(R.string.app_name));

        progressDialog = new ProgressDialog(this);
        tinyDB = new TinyDB(getApplicationContext());
        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();
        etBrainKey.addTextChangedListener(brainKeyWatcher);

    }

    private final TextWatcher brainKeyWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if (!s.equals(s.toLowerCase())) {
                s = s.toLowerCase();
                etBrainKey.setText(s);
                etBrainKey.setSelection(etBrainKey.getText().toString().length());
            }
        }
    };

    @OnClick(R.id.btnCancel)
    public void cancel(Button button) {
        this.finish();
    }

    @OnClick(R.id.btnWallet)
    public void wallet(Button button) {


        if (etBrainKey.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        } else {
            String trimmedBrainKey = etBrainKey.getText().toString().trim();
            etBrainKey.setText(trimmedBrainKey);
            if (etPin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            } else if (etPinConfirmation.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            } else {
                load(etPin.getText().toString());
            }
        }
    }

    void load(String pinCode) {
        String temp = etBrainKey.getText().toString();
        if (temp.contains(" ")) {
            String arr[] = temp.split(" ");
            if (arr.length == 16) {

                if (checkBrainKeyExist(temp)) {
                    Toast.makeText(getApplicationContext(), R.string.account_already_exist, Toast.LENGTH_SHORT).show();
                } else {
                    showDialog("", getString(R.string.importing_your_wallet));
                    get_account_from_brainkey(this, temp, pinCode);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkBrainKeyExist(String brainKey) {
        boolean isBrainKey = false;
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        for (int i = 0; i < accountDetails.size(); i++) {
            try {
                if (brainKey.equals(accountDetails.get(i).brain_key)) {
                    isBrainKey = true;
                    break;
                }
            } catch (Exception e) {
            }
        }
        return isBrainKey;

    }

    public void get_account_from_brainkey(final Activity activity, final String brainKey, final String pinCode) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "get_account_from_brainkey");
        hashMap.put("brainkey", brainKey);
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
                        /*
                        Crypt cr = new Crypt();
                        try {
                            String unenWif = cr.decrypt_string(accountDetails.wif_key);
                            String unenBrn = cr.decrypt_string(accountDetails.brain_key);
                        }
                        catch (Exception e)
                        {
                            Log.d("decrypt",e.getMessage());
                        }
                        */

                        addWallet(accountDetails, brainKey, pinCode);
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

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.isConnected()) {
   //                 if (Application.webSocketG.isOpen()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
//                    } else {
//                        ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
//                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
//                        ivSocketConnected.startAnimation(myFadeInAnimation);
//                    }
                } else {
                    ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                    ivSocketConnected.startAnimation(myFadeInAnimation);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTask, 1000);
    }
    /////////////////

    void addWallet(AccountDetails accountDetail, String brainKey, String pinCode) {
        //ArrayList<AccountDetails> accountDetailsList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.wif_key = accountDetail.wif_key;
        accountDetails.pinCode = pinCode;
        accountDetails.account_name = accountDetail.account_name;
        accountDetails.pub_key = accountDetail.pub_key;
        accountDetails.brain_key = brainKey;
        accountDetails.isSelected = true;
        accountDetails.status = "success";
        accountDetails.account_id = accountDetail.account_id;

        BinHelper myBinHelper = new BinHelper();
        myBinHelper.addWallet(accountDetails,brainKey,pinCode,getApplicationContext(),this);


        /*
        for (int i = 0; i < accountDetailsList.size(); i++) {

            if (accountDetailsList.get(i).account_name.equals(accountDetails.account_name)) {
                accountDetailsList.remove(i);
            }
        }
        for (int i = 0; i < accountDetailsList.size(); i++) {
            accountDetailsList.get(i).isSelected = false;
        }

        accountDetailsList.add(accountDetails);

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetailsList);

        List<TransactionDetails> emptyTransactions = new ArrayList<>();
        tinyDB.putTransactions(this, getApplicationContext(), getResources().getString(R.string.pref_local_transactions), new ArrayList<>(emptyTransactions));
        */

        Intent intent;

        if ( myBinHelper.numberOfWalletAccounts(getApplicationContext()) <= 1 )
        {
            intent = new Intent(getApplicationContext(), BackupBrainkeyActivity.class);
        }
        else
        {
            intent = new Intent(getApplicationContext(), TabActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
