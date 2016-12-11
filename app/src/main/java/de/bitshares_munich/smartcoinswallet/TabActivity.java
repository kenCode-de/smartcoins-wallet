package de.bitshares_munich.smartcoinswallet;


import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luminiasoft.bitshares.AccountOptions;
import com.luminiasoft.bitshares.AccountUpdateTransactionBuilder;
import com.luminiasoft.bitshares.Address;
import com.luminiasoft.bitshares.Asset;
import com.luminiasoft.bitshares.Authority;
import com.luminiasoft.bitshares.BrainKey;
import com.luminiasoft.bitshares.PublicKey;
import com.luminiasoft.bitshares.Transaction;
import com.luminiasoft.bitshares.UserAccount;
import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.luminiasoft.bitshares.ws.TransactionBroadcastSequence;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.TinyDB;

public class TabActivity extends BaseActivity {
    private String TAG = this.getClass().getName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.tvBlockNumberHead_TabActivity)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_TabActivity)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected_TabActivity)
    ImageView ivSocketConnected;

    TinyDB tinyDB;

    ArrayList<AccountDetails> accountDetails;
    private AccountDetails updatedAccount;
    private int UPDATE_KEY_MAX_RETRIES = 3;
    private int updateKeyRetryCount = 0;
    private int nodeIndex = 0;
    private WebsocketWorkerThread refreshKeyWorker;
    private WitnessResponseListener mListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"onSuccess");
                    Toast.makeText(TabActivity.this, R.string.refresh_keys_success, Toast.LENGTH_LONG).show();
                    for(AccountDetails accountDetail : accountDetails){
                        if(accountDetail.account_id.equals(updatedAccount.account_id)){
                            accountDetail.wif_key = updatedAccount.wif_key;
                            accountDetail.brain_key = updatedAccount.brain_key;
                            Log.d(TAG,"updating account with name: "+accountDetail.account_name+", id: "+accountDetail.account_id+", key: "+accountDetail.brain_key);
                        }
                        break;
                    }
                    tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
                    displayBrainKeyBackup();
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.d(TAG, "onError. Msg: "+error.message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatedAccount = null;
                    if(updateKeyRetryCount < UPDATE_KEY_MAX_RETRIES){
                        Log.d(TAG, "Retrying. count: "+ updateKeyRetryCount +", max: "+ UPDATE_KEY_MAX_RETRIES);
                        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                        for(AccountDetails accountDetails : arrayList){
                            nodeIndex = (nodeIndex + 1) % Application.urlsSocketConnection.length;
                            Log.d(TAG,"account id: '"+accountDetails.account_id+"', name: "+accountDetails.account_name+", wif: "+accountDetails.wif_key);
                            if(accountDetails.isSelected){
                                updateAccountAuthorities(accountDetails);
                                updateKeyRetryCount++;
                                break;
                            }
                        }
                    }else{
                        Toast.makeText(TabActivity.this, R.string.refresh_keys_fail, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tab);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(getApplicationContext());
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationIcon(R.mipmap.btslogo);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        setTitle(getResources().getString(R.string.app_name));

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();

        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null) {
            if (res.containsKey("ask_for_pin")) {
                if (res.getBoolean("ask_for_pin")) {
                    showDialogPin();
                }
            }
        }
    }

    /**
     * Will display a dialog prompting the user to make a backup of the brain key.
     */
    private void displayBrainKeyBackup() {

        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(getString(R.string.backup_brainkey));
        dialog.setContentView(R.layout.activity_copybrainkey);
        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
        try {
            String brainKey = getBrainKey();
            if (brainKey.isEmpty()) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.unable_to_load_brainkey),Toast.LENGTH_LONG).show();
                return;
            } else {
                etBrainKey.setText(brainKey);
            }
        } catch (Exception e) {
            Log.e(TAG,"Exception in displayBrainKeyBackup. Msg: "+e.getMessage());
        }

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Button btnCopy = (Button) dialog.findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TabActivity.this, R.string.copied_to_clipboard , Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                clipboard.setPrimaryClip(clip);
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Returns the active's account brain key
     * @return
     */
    private String getBrainKey() {
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).brain_key;
            }
        }
        return "";
    }


    /**
     * Method that will actually perform a call to the full node and update the key currently
     * controlling the account passed as a parameter.
     *
     * @param accountDetails: The account whose key we want to update.
     */
    private void updateAccountAuthorities(AccountDetails accountDetails) {
        Log.d(TAG,"account to update. current brain key: "+accountDetails.brain_key);
        updatedAccount = accountDetails;
        try {
            String currentWif = Crypt.getInstance().decrypt_string(updatedAccount.wif_key);

            // Coming up with a new brainkey suggestion
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(AccountActivity.BRAINKEY_FILE), "UTF-8"));
            String dictionary = reader.readLine();
            String suggestion = BrainKey.suggest(dictionary);
            BrainKey brainKey = new BrainKey(suggestion, 0);
            Log.d(TAG,"new brain key: "+suggestion);

            // Keeping a reference of the account to be changed, with the updated values
            Address address = new Address(ECKey.fromPublicOnly(brainKey.getPrivateKey().getPubKey()));
            updatedAccount.wif_key = Crypt.getInstance().encrypt_string(brainKey.getWalletImportFormat());
            updatedAccount.brain_key = suggestion;
            updatedAccount.pub_key = address.toString();
            updatedAccount.isPostSecurityUpdate = true;

            // Building a transaction that will be used to update the account key
            HashMap<PublicKey, Integer> authMap = new HashMap<>();
            authMap.put(address.getPublicKey(), 1);
            Authority authority = new Authority(1, authMap, null);
            AccountOptions options = new AccountOptions(address.getPublicKey());
            Transaction transaction = new AccountUpdateTransactionBuilder(DumpedPrivateKey.fromBase58(null, currentWif).getKey())
                    .setAccont(new UserAccount(accountDetails.account_id))
                    .setOwner(authority)
                    .setActive(authority)
                    .setOptions(options)
                    .build();

            refreshKeyWorker = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, new Asset("1.3.0"), mListener), nodeIndex);
            refreshKeyWorker.start();
        } catch (MalformedTransactionException e) {
            Log.e(TAG, "MalformedTransactionException. Msg: "+e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException. Msg: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "NoSuchPaddingException. Msg: "+e.getMessage());
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException. Msg: "+e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "InvalidAlgorithmParameterException. Msg: "+e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException. Msg: "+e.getMessage());
        } catch (BadPaddingException e) {
            Log.e(TAG, "BadPaddingException. Msg: "+e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException. Msg: "+e.getMessage());
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BalancesFragment(), getString(R.string.balances));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        viewPager.setAdapter(adapter);
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

    @OnClick(R.id.OnClickSettings_TabActivity)
    void OnClickSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    // Block for pin
    private void showDialogPin() {

        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        final Dialog dialog = new Dialog(TabActivity.this);
        dialog.setTitle(R.string.pin_verification);
        dialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final EditText etPin = (EditText) dialog.findViewById(R.id.etPin);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                            Log.d(TAG, "pin code matches");
                            dialog.cancel();
                            checkSecurityUpdate();
                            break;
                        }else{
                            Log.d(TAG, "pin code doesn't match");
                        }
                    }
                }

            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void checkSecurityUpdate(){
        boolean isActiveAccountOld = false;
        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        AccountDetails activeAccount = null;
        for(AccountDetails account : arrayList){
            Log.d(TAG, "account: "+account.toString());
            if(account.isSelected){
                activeAccount = account;
                try {
                    if(account.isPostSecurityUpdate){
                        Log.d(TAG, "Account creation is post security update: " + account.isPostSecurityUpdate);
                    }else{
                        Log.d(TAG, "Account creation is previous to the security update");
                        isActiveAccountOld = true;
                    }
                }catch(NullPointerException e){
                    Log.e(TAG, "NullPointerException. Account creation is previous to the security update");
                    isActiveAccountOld = true;
                }
                break;
            }
        }
        if(isActiveAccountOld){
            final AccountDetails oldActiveAccount = activeAccount;
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.security_update_title))
                    .setMessage(getResources().getString(R.string.security_update_summary))
                    .setPositiveButton(getResources().getString(R.string.dialog_proceed), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateAccountAuthorities(oldActiveAccount);
                            dialog.dismiss();
                        }
                    }).setNegativeButton(getResources().getString(R.string.dialog_later), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            viewPager.setCurrentItem(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
