package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.luminiasoft.bitshares.BrainKey;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IAccountID;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.GenerateKeys;
import de.bitshares_munich.models.RegisterAccount;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.PermissionManager;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccountActivity extends BaseActivity implements IAccount, IAccountID {
    private final String TAG = this.getClass().getName();
    Context context;
    TinyDB tinyDB;

    @Bind(R.id.etAccountName)
    EditText etAccountName;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.tvExistingAccount)
    TextView tvExistingAccount;

    Boolean settingScreen = false;
    Boolean validAccount = true;
    Boolean checkingValidation = false;

    Boolean accountCreated = false;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    Gson gson;
    ProgressDialog progressDialog;

    @Bind(R.id.tvErrorAccountName)
    TextView tvErrorAccountName;

    @Bind(R.id.tvPin)
    TextView tvPin;

    @Bind(R.id.tvPinConfirmation)
    TextView tvPinConfirmation;

    @Bind(R.id.tvBlockNumberHead)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected)
    ImageView ivSocketConnected;


    String pubKey;
    String wifPrivKey;
    String brainPrivKey;
    Boolean hasNumber;

    // icon_setting

    webSocketCallHelper myWebSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(this);
        setTitle(getResources().getString(R.string.app_name));
        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));

        context = this;

        myWebSocketHelper = new webSocketCallHelper(this);

        validationAccountName();
        gson = new Gson();
        Application.registerCallback(this);
        Application.registerCallbackIAccountID(this);
        progressDialog = new ProgressDialog(this);
        updateBlockNumberHead();

        final Handler handler = new Handler();
        final Runnable createFolder = new Runnable() {
            @Override
            public void run() {
                createFolder();
            }
        };

        handler.postDelayed(createFolder, 500);
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null) {
            if (res.containsKey("activity_id")) {
                if (res.getInt("activity_id") == 919) {
                    tvExistingAccount.setVisibility(View.GONE);
                    setBackButton(true);
                }
            }
        }
        hasNumber = true;
    }


    private void createFolder() {
        PermissionManager manager = new PermissionManager();
        manager.verifyStoragePermissions(this);

        final File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.folder_name));

        boolean success = false;

        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success) {
            // Do something on success
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.txt_folder_created) + " : " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File file2 = new File(folder.getAbsolutePath(), "Woohoo.wav");

                    if (!file2.exists()) {
                        FileOutputStream save = new FileOutputStream(file2);

                        byte[] buffer = null;
                        InputStream fIn = getResources().openRawResource(R.raw.woohoo);
                        int size = 0;

                        try {
                            size = fIn.available();
                            buffer = new byte[size];
                            fIn.read(buffer);
                            fIn.close();
                            save.write(buffer);
                            //save.flush();
                            //save.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                        }

                        save.flush();
                        save.close();
                    }
                } catch (Exception e) {

                }
            }
        });
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

        hasNumber = false;
        myWebSocketHelper.cleanUpTransactionsHandler();

        if (etAccountName.getText().length() > 5 && containsDigit(etAccountName.getText().toString()) && etAccountName.getText().toString().contains("-")) {
            checkingValidation = true;
            hasNumber = true;
            validAccount = true;
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }

    }

    CountDownTimer myAccountNameValidationTimer = new CountDownTimer(1000, 1000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            createBitShareAN(false);
        }
    };


    public void createBitShareAN(boolean focused) {
        if (!focused) {
            if (etAccountName.getText().length() > 5)
            {
                if (hasNumber) {
                    tvErrorAccountName.setText("");
                    tvErrorAccountName.setVisibility(View.GONE);
                }
                String socketText = getString(R.string.lookup_account_a);
                String socketText2 = getString(R.string.lookup_account_b) + "\"" + etAccountName.getText().toString() + "\"" + ",50]],\"id\": 6}";
                myWebSocketHelper.make_websocket_call(socketText,socketText2, webSocketCallHelper.api_identifier.database);
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
                checkingValidation = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Helper.containKeySharePref(getApplicationContext(), getString(R.string.pref_agreement))) {
            showDialogLiscence();
        }
    }

    private void showDialogLiscence() {
        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(R.string.agreement);
        dialog.setContentView(R.layout.custom_dialog_liscence);
        Button dialog_btn_cancel = (Button) dialog.findViewById(R.id.dialog_btn_cancel);
        WebView webView = (WebView) dialog.findViewById(R.id.webviewLisense);
        String html = getString(R.string.lisence_html);
        webView.loadData(html, "text/html", "UTF-8");

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
                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pref_agreement), true);
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

    Boolean checkLastIndex() {
        String name = etAccountName.getText().toString();
        String lastWord = String.valueOf(name.charAt(name.length() - 1));
        return lastWord.equals("-");
    }

    Boolean checkHyphen() {
        String name = etAccountName.getText().toString();
        return name.contains("-");
    }


    private void generateKeys() {
        //TODO: Read brainkey dictionary from assets or resources
        BrainKey brainKey = new BrainKey();

        //TODO: Generate keys locally and call method registerdKeys
//        HashMap hm = new HashMap();
//        hm.put("method", "generate_keys");
//        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
//        ServiceGenerator sg = new ServiceGenerator("http://54.165.40.16:9003");
//        IWebService service = sg.getService(IWebService.class);
//        final Call<GenerateKeys> postingService = service.getGeneratedKeys(hm);
//        postingService.enqueue(new Callback<GenerateKeys>() {
//            @Override
//            public void onResponse(Response<GenerateKeys> response) {
//                Log.d(TAG, "onResponse");
//                Log.d(TAG, "isSuccess: "+response.isSuccess());
//                if (response.isSuccess()) {
//
//
//                    GenerateKeys resp = response.body();
//                    Log.d(TAG, "private key wif: "+resp.keys.wif_priv_key);
//                    if (resp.status.equals("success")) {
//                        try {
//
//                            pubKey = resp.keys.pub_key;
//                            wifPrivKey = Crypt.getInstance().encrypt_string(resp.keys.wif_priv_key);
//                            brainPrivKey = resp.keys.brain_priv_key;
//                            String accountName = etAccountName.getText().toString();
//
//                            registerdKeys(accountName, resp.keys.pub_key);
//                        } catch (Exception e) {
//
//                            generateKeys();
//
//                        }
//
//                    } else if (resp.status.equals("failure")) {
//                        generateKeys();
//
//                    }
//                } else {
//                    generateKeys();
//                    Toast.makeText(context, R.string.txt_no_internet_connection , Toast.LENGTH_SHORT).show();
//
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                SupportMethods.testing("accountActivity", t, "past_break");
//                generateKeys();
//            }
//        });
    }

    private void registerdKeys(final String accountName, String key) {

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("name", accountName);
        hm.put("owner_key", key);
        hm.put("active_key", key);
        hm.put("memo_key", key);
        hm.put("refcode", "bitshares-munich");
        hm.put("referrer", "bitshares-munich");

        HashMap<String, HashMap> hashMap = new HashMap<>();
        hashMap.put("account", hm);


        try {
            ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_create_url));
            IWebService service = sg.getService(IWebService.class);
            final Call<RegisterAccount> postingService = service.getReg(hashMap);
            postingService.enqueue(new Callback<RegisterAccount>() {
                @Override
                public void onResponse(Response<RegisterAccount> response) {
                    if (response.isSuccess()) {
                        RegisterAccount resp = response.body();
                        if (resp.account != null) {
                            try {

                                if(resp.account.name.equals(accountName))
                                {
                                get_account_id(etAccountName.getText().toString(), "151");
                                tvErrorAccountName.setVisibility(View.GONE);
                                };
                            } catch (Exception e) {

                                Toast.makeText(getApplicationContext(),R.string.try_again , Toast.LENGTH_SHORT).show();

                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),R.string.try_again , Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(getApplicationContext(),R.string.try_again , Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),R.string.try_again , Toast.LENGTH_SHORT).show();
        }
    }

    @OnFocusChange(R.id.etAccountName)
    void onFocusChanged(boolean hasFocus){
        if(!hasFocus) {
            if (etAccountName.getText().length() <= 5) {
                checkingValidation = false;
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
            } else if (etAccountName.getText().length() > 5 && !containsDigit(etAccountName.getText().toString())) {
                tvErrorAccountName.setText(getString(R.string.account_name_must_include_dash_and_a_number));
                tvErrorAccountName.setVisibility(View.VISIBLE);
                hasNumber = false;
                checkingValidation = false;
            } else if (etAccountName.getText().length() > 5 && !etAccountName.getText().toString().contains("-")) {
                tvErrorAccountName.setText(getString(R.string.account_name_must_include_dash_and_a_number));
                tvErrorAccountName.setVisibility(View.VISIBLE);
                checkingValidation = false;
            } else {
                hasNumber = true;
                checkingValidation = true;
            }
        }
    }

    @OnClick(R.id.btnCreate)
    public void create(Button button) {

            if (checkingValidation) {
                Toast.makeText(getApplicationContext(), R.string.validation_in_progress, Toast.LENGTH_SHORT).show();
            } else if (etAccountName.getText().toString().length() == 0) {
                Toast.makeText(getApplicationContext(), R.string.kindly_create_account, Toast.LENGTH_SHORT).show();
            } else if (etAccountName.getText().toString().length() <= 5) {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
            } else if (checkLastIndex()) {
                tvErrorAccountName.setVisibility(View.VISIBLE);
                tvErrorAccountName.setText(R.string.last_letter_cannot);
            } else if (!checkHyphen()) {
                tvErrorAccountName.setVisibility(View.VISIBLE);
                tvErrorAccountName.setText(R.string.account_name_must_include_dash_and_a_number);
            } else if (!containsDigit(etAccountName.getText().toString())) {
                tvErrorAccountName.setVisibility(View.VISIBLE);
                tvErrorAccountName.setText(R.string.account_name_must_include_dash_and_a_number);
            } else {
                if (etPin.getText().length() < 5) {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
                } else if (etPinConfirmation.getText().length() < 5) {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
                } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                    Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
                } else {
                    if (validAccount) {
                        if (!checkingValidation) {
                            showDialog("", "");
                            generateKeys();
                        }
                    }
                }
            }
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
                progressDialog.setCancelable(false);
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

    @Override
    public void checkAccount(JSONObject jsonObject) {
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            boolean found = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                final String temp = jsonArray.getJSONArray(i).getString(0);
                if (temp.equals(etAccountName.getText().toString())) {
                    found = true;
                    validAccount = false;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvErrorAccountName.setText(R.string.validation_in_progress);
                        tvErrorAccountName.setVisibility(View.VISIBLE);
                    }
                });
            }
            if (found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String acName = getString(R.string.account_name_already_exist);
                        String format = String.format(acName.toString(), etAccountName.getText().toString());
                        tvErrorAccountName.setText(format);
                        tvErrorAccountName.setVisibility(View.VISIBLE);
                        checkingValidation = false;
                    }
                });
            }
            if (!found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validAccount = true;
                        tvErrorAccountName.setVisibility(View.GONE);
                        checkingValidation = false;
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    void addWallet(String account_id) {
              AccountDetails accountDetails = new AccountDetails();
        accountDetails.pinCode=etPin.getText().toString();
        accountDetails.wif_key = wifPrivKey;
        accountDetails.account_name = etAccountName.getText().toString();
        accountDetails.pub_key = pubKey;
        accountDetails.brain_key = brainPrivKey;
        accountDetails.isSelected = true;
        accountDetails.status = "success";
        accountDetails.account_id = account_id;

        BinHelper myBinHelper = new BinHelper();
        myBinHelper.addWallet(accountDetails,brainPrivKey,etPin.getText().toString(),getApplicationContext(),this);

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

        Application.timeStamp();

        myWebSocketHelper.cleanUpTransactionsHandler();

        finish();
    }


    void get_account_id(final String name_id, final String id)
    {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_account_by_name\",[\"" + name_id + "\"]]}";
        myWebSocketHelper.make_websocket_call(getDetails,getDetails2, webSocketCallHelper.api_identifier.database);

    }

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.isConnected()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
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

    @Override
    public void accountId(String string) {
        myWebSocketHelper.cleanUpTransactionsHandler();
        String result = SupportMethods.ParseJsonObject(string, "result");
        String id_account = SupportMethods.ParseJsonObject(result, "id");
        SupportMethods.testing("accountID", id_account, "getDetails");

        addWallet(id_account);
    }

    public boolean containsDigit(String s)
    {
        if (s != null && !s.isEmpty())
        {
            for (char c : s.toCharArray())
            {
                if (Character.isDigit(c))
                {
                    return true;
                }
            }
        }

        return false;
    }

}
