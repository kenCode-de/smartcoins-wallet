package de.bitshares_munich.smartcoinswallet;


import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
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
import com.luminiasoft.bitshares.models.AccountProperties;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.luminiasoft.bitshares.ws.GetAccounts;
import com.luminiasoft.bitshares.ws.TransactionBroadcastSequence;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.BackupBinDelegate;
import de.bitshares_munich.Interfaces.UpdatedAccountListener;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.fragments.PromptUpdateDialog;
import de.bitshares_munich.fragments.UpdatingAccountsDialog;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.TinyDB;

public class TabActivity extends BaseActivity implements BackupBinDelegate, PromptUpdateDialog.UpdateAccountsListListener {
    private String TAG = this.getClass().getName();

    private boolean DEBUG_ACCOUNT_UPDATE = false;

    private final String TAG_DIALOG_PROMPT_ACCOUNT_UPDATE = "prompt_account_update";
    private final String TAG_DIALOG_UPDATING_ACCOUNTS = "updating_accounts";

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

    /* Dialog displayed to the user only once after the security update */
    private UpdatingAccountsDialog updatingAccountsDialog;

    /* Pin pinDialog */
    private Dialog pinDialog;

    /* In memory reference to all accounts present in this wallet */
    private ArrayList<AccountDetails> accountDetails;

    /* List of accounts to be updated */
    private LinkedList<UpdateAccountTask> updateQueue;

    /* List of accounts that failed to be updated */
    private LinkedList<UpdateAccountTask> failedQueue;

    /* List of accounts successfully updated */
    private LinkedList<UpdateAccountTask> successQueue;

    /* Account currently being updated */
    private UpdateAccountTask currentTask;

    /* Storing old key here and then in preferences*/
    private String oldKey;

    /* Newly created brain key */
    private BrainKey newBrainKey;

    private int UPDATE_KEY_MAX_RETRIES = 1;
    private int updateKeyRetryCount = 0;
    private int nodeIndex = 0;
    private WebsocketWorkerThread getAccountsWorker;
    private WebsocketWorkerThread refreshKeyWorker;

    /**
     * Listener called only once with all the accounts data. This is done before the security update
     * and only once, just to know what keys to update for each account.
     */
    private WitnessResponseListener getAccountsListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(final WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "getAccounts. onSuccess");
                    List<AccountProperties> accountProperties = (List<AccountProperties>) response.result;
                    for(AccountProperties accountProperty : accountProperties){
                        BrainKey brainKey = null;
                        ArrayList<AccountDetails> details = tinyDB.getListObject(getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
                        for(AccountDetails detail : details){
                            if(detail.account_name.equals(accountProperty.name)){
                                brainKey = new BrainKey(detail.brain_key, BrainKey.DEFAULT_SEQUENCE_NUMBER);
                            }
                        }
                        boolean updateOwner = accountProperty.owner.equals(accountProperty.active);
                        boolean updateMemo = accountProperty.options.equals(accountProperty.active);
                        UpdateAccountTask updateTask = new UpdateAccountTask(new UserAccount(accountProperty.id, accountProperty.name), brainKey);
                        updateTask.setUpdateOwner(updateOwner);
                        updateTask.setUpdateMemo(updateMemo);
                        updateQueue.add(updateTask);
                    }

                    /* Updating the UI status of the first account from the list */
                    updatingAccountsDialog.onUpdateStatusChange(updateQueue.peek().getAccount(), UpdatedAccountListener.UPDATING);

                    /* Check the first account to update, if there is any */
                    checkAccountUpdateStatus();

                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "getAccounts. onError. Msg: "+error.message);
        }
    };

    /**
     * Listener called once a single account update operation is over.
     */
    private WitnessResponseListener refreshKeysListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"update.onSuccess");
                    for(AccountDetails accountDetail : accountDetails){
                        UserAccount account = currentTask.getAccount();
                        BrainKey brainKey = currentTask.getBrainKey();
                        if(accountDetail.account_id.equals(account.getObjectId())){
                            try{
                                accountDetail.wif_key = Crypt.getInstance().encrypt_string(brainKey.getWalletImportFormat());
                                accountDetail.brain_key = brainKey.getBrainKey();
                                accountDetail.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;
                                Log.d(TAG,"updating account with name: "+accountDetail.account_name+", id: "+accountDetail.account_id+", key: "+accountDetail.brain_key);

                                /* Creating automatic bin backup */
                                BinHelper myBinHelper = new BinHelper(TabActivity.this, TabActivity.this);
                                myBinHelper.createBackupBinFile(accountDetail.brain_key, accountDetail.account_name, accountDetail.pinCode);
                            }catch(Exception e){
                                Log.e(TAG, String.format("Exception while trying to update local copy of authority keys from account %s. Msg: %s", accountDetail.account_name, e.getMessage()));
                            }
                        }
                        break;
                    }
                    /* Storing the updated account list */
                    tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
                    accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

                    /* Updating the UI */
                    updatingAccountsDialog.onUpdateStatusChange(currentTask.getAccount(), UpdatedAccountListener.SUCCESS);

                    /* Moving the task from the update to the success list */
                    successQueue.add(currentTask);

                    /* Updating store of old keys*/
                    ArrayList<String> oldKeys = tinyDB.getListString(Constants.KEY_OLD_KEYS);
                    oldKeys.add(oldKey);
                    tinyDB.putListString(Constants.KEY_OLD_KEYS, oldKeys);

                    /* Check next account to update, if there is any */
                    checkAccountUpdateStatus();
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.d(TAG, "update.onError. Msg: "+error.message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(updateKeyRetryCount < UPDATE_KEY_MAX_RETRIES){
                        Log.d(TAG, "Retrying. count: "+ updateKeyRetryCount +", max: "+ UPDATE_KEY_MAX_RETRIES);
                        nodeIndex = (nodeIndex + 1) % Application.urlsSocketConnection.length;
                        updateKeyRetryCount++;
                        updateAccountAuthorities();

//                        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
//                        for(AccountDetails accountDetails : arrayList){
//                            nodeIndex = (nodeIndex + 1) % Application.urlsSocketConnection.length;
//                            Log.d(TAG,"account id: '"+accountDetails.account_id+"', name: "+accountDetails.account_name+", wif: "+accountDetails.wif_key);
//
//                            /* Retrying */
//                            if(accountDetails.isSelected){
//                                updateAccountAuthorities();
//                                updateKeyRetryCount++;
//                                break;
//                            }
//                        }
                    }else{
                        failedQueue.add(currentTask);
                        updateKeyRetryCount = 0;

                        /* Updating the UI */
                        updatingAccountsDialog.onUpdateStatusChange(currentTask.getAccount(), UpdatedAccountListener.FAILURE);

                        /* Check next account to update, if there is any */
                        checkAccountUpdateStatus();
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

    @Override
    protected void onPause() {
        super.onPause();
        if(pinDialog != null && pinDialog.isShowing()){
            pinDialog.dismiss();
        }
    }

    /**
     * Will display a pinDialog prompting the user to make a backup of the brain key.
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
     * Checks if we still have accounts to update, and act accordingly.
     * If we don't have any account left to update, we notify the user.
     */
    private void checkAccountUpdateStatus(){
        Log.d(TAG, "checkAccountUpdateStatus");
        if(updateQueue.size() == 0){
            if(successQueue.size() == 0 && failedQueue.size() == 0){
                // Nothing to update
                return;
            }else{
                // Account update is finished
                displayUpdateSummary();
            }
        } else {
            // Update next account
            currentTask = updateQueue.poll();

            // Updating the UI
            updatingAccountsDialog.onUpdateStatusChange(currentTask.getAccount(), UpdatedAccountListener.UPDATING);

            // Actually sending the transaction data
            updateAccountAuthorities();
        }
    }

    /**
     * Displays a summary of the update procedure.
     */
    private void displayUpdateSummary(){
        // Saving updated status
        tinyDB.putBoolean(Constants.KEY_UPDATE_DONE, true);

        updatingAccountsDialog.onUpdateFinished();
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_update_done), Toast.LENGTH_SHORT).show();
    }

    /**
     * Method that will actually perform a call to the full node and update the key controlling
     * the account specified at the private field called 'updatingAccount'.
     */
    private void updateAccountAuthorities() {
        UserAccount account = currentTask.getAccount();
        BrainKey brainKey = currentTask.getBrainKey();
        oldKey = String.format("%s:%s", account.getAccountName(), brainKey.getWalletImportFormat());

        Log.d(TAG,"updateAccountAuthorities. account to update: "+account.getAccountName()+", id: "+account.getObjectId());
        Log.d(TAG,"current brain key: "+brainKey.getBrainKey());
        try {
            // Coming up with a new brain key suggestion
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(AccountActivity.BRAINKEY_FILE), "UTF-8"));
            String dictionary = reader.readLine();
            String suggestion = BrainKey.suggest(dictionary);
            newBrainKey = new BrainKey(suggestion, 0);
            Log.d(TAG,"new brain key: "+suggestion);

            // Keeping a reference of the account to be changed, with the updated values
            Address address = new Address(ECKey.fromPublicOnly(newBrainKey.getPrivateKey().getPubKey()));

            // Building a transaction that will be used to update the account key
            HashMap<PublicKey, Integer> authMap = new HashMap<>();
            authMap.put(address.getPublicKey(), 1);
            Authority authority = new Authority(1, authMap, null);
            AccountOptions options = new AccountOptions(address.getPublicKey());
            AccountUpdateTransactionBuilder builder = new AccountUpdateTransactionBuilder(DumpedPrivateKey.fromBase58(null, brainKey.getWalletImportFormat()).getKey())
                    .setAccont(currentTask.getAccount())
                    .setActive(authority);

            if(currentTask.isUpdateOwner()){
                // Only changing the "owner" authority in some cases.
                builder.setOwner(authority);
            }
            if(currentTask.isUpdateMemo()){
                // Only changing the "memo" authority if it is the same as the active.
                builder.setOptions(options);
            }

            Transaction transaction = builder.build();

            refreshKeyWorker = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, new Asset("1.3.0"), refreshKeysListener), nodeIndex);
            refreshKeyWorker.start();
        } catch (MalformedTransactionException e) {
            Log.e(TAG, "MalformedTransactionException. Msg: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
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
        pinDialog = new Dialog(TabActivity.this);
//        pinDialog.setTitle(R.string.pin_verification);
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
                            Log.d(TAG, "pin code matches");
                            pinDialog.cancel();
                            if(!tinyDB.getBoolean(Constants.KEY_UPDATE_DONE) || DEBUG_ACCOUNT_UPDATE ){
                                Log.d(TAG, "starting security update");
                                startSecurityUpdate();
                            }else{
                                Log.d(TAG, "Security update already performed");
                            }
                            break;
                        }else{
                            Toast.makeText(TabActivity.this, getResources().getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        pinDialog.setCancelable(false);
        pinDialog.show();
    }

    /**
     * Starts the security update procedure. It does this by first checking
     * all accounts and deciding whether any of them needs a security update.
     *
     * Only accounts existing previously in this device are considered for the
     * update. Meaning we exclude newly created accounts as well as imported
     * accounts.
     */
    private void startSecurityUpdate(){
        this.updateQueue = new LinkedList<>();
        this.failedQueue = new LinkedList<>();
        this.successQueue = new LinkedList<>();

        ArrayList<AccountDetails> arrayList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        ArrayList<AccountDetails> toUpdate = new ArrayList<>();
        for(AccountDetails account : arrayList){
            boolean isOld = true;
            Log.d(TAG, "account: "+account.toString());
            try {
                if(account.securityUpdateFlag == AccountDetails.PRE_SECURITY_UPDATE && !DEBUG_ACCOUNT_UPDATE){
                    Log.d(TAG, "Account creation is post security update: " + account.securityUpdateFlag);
                    isOld = false;
                }else{
                    Log.d(TAG, "Account creation is previous to the security update");
                }
            }catch(NullPointerException e){
                Log.e(TAG, "NullPointerException. Account creation is previous to the security update");
            }
            if(isOld){
                toUpdate.add(account);
            }
        }
        /* In case we have accounts to update, prompt the user with a nice dialog explaining the situation
        * and giving the option to do it later */
        if(toUpdate.size() > 0){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_PROMPT_ACCOUNT_UPDATE);
            if(prev != null){
                ft.remove(prev);
            }
            PromptUpdateDialog dialog = PromptUpdateDialog.newInstance(toUpdate);
            dialog.show(ft, TAG_DIALOG_PROMPT_ACCOUNT_UPDATE);
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

    @Override
    public void backupComplete(boolean success) {
        Log.d(TAG, "bin backup complete. success: "+success);
    }

    @Override
    public void onAccountList(List<UserAccount> accountList) {
        for(UserAccount account : accountList){
            Log.d(TAG, String.format("Account to update: %s", account.getAccountName()));
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_UPDATING_ACCOUNTS);
        if(prev != null){
            ft.remove(prev);
        }
        updatingAccountsDialog = UpdatingAccountsDialog.newInstance(accountList);
        updatingAccountsDialog.show(ft, TAG_DIALOG_UPDATING_ACCOUNTS);

        /* Asking for all account details */
        getAccountsWorker = new WebsocketWorkerThread(new GetAccounts(accountList, this.getAccountsListener));
        getAccountsWorker.start();
    }
}
