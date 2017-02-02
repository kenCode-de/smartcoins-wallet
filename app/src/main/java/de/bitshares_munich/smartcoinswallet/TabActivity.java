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

import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.AccountUpdateTransactionBuilder;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.errors.MalformedTransactionException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.TransactionBroadcastSequence;

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
import de.bitshares_munich.Interfaces.LockListener;
import de.bitshares_munich.Interfaces.UpdatedAccountListener;
import de.bitshares_munich.adapters.ViewPagerAdapter;
import de.bitshares_munich.fragments.PromptUpdateDialog;
import de.bitshares_munich.fragments.UpdatingAccountsDialog;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.TinyDB;

public class TabActivity extends BaseActivity implements BackupBinDelegate, PromptUpdateDialog.UpdateAccountsListListener, LockListener {
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

    private TinyDB tinyDB;

    /* Currently active user account */
    private UserAccount currentlyActive;

    /* Dialog displayed to the user only once after the security update */
    private UpdatingAccountsDialog updatingAccountsDialog;

    /* In memory reference to all accounts present in this wallet */
    private ArrayList<AccountDetails> accountDetails;

    /* List of accounts to be updated */
    private LinkedList<UpdateAccountTask> updateQueue;

    /* List of accounts that failed to be updated */
    private LinkedList<UpdateAccountTask> failedQueue;

    /* List of accounts successfully updated */
    private LinkedList<UpdateAccountTask> successQueue;

    /* Account currently being updated */
    private UpdateAccountTask currentTask = null;

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
     * This listener will be called in the rare case in which we end up with the wrong keys because
     * of an interrupted account update operation. In this situation we might end up with a mismatch
     * between the currently account controlling key and the one we have stored in preferences.
     *
     * The account update mechanism prepares for this situation, by also storing the newly generated
     * brain key suggestion in shared preferences as a sort of cache while the request is being
     * sent to the network.
     *
     * In case the operation is successful, but there is an error before updating the currently
     * control key, we still don't loose the newly generated brain key thanks to this cache.
     *
     * A check is performed every time the app starts looking at this cache, which should be empty.
     * If it is not, this might signal an interrupted account update operation and to make sure we're
     * doing the right thing we ask the network for the current authorities controlling this account.
     *
     * If the key currently being hold at the cache matches what the network says is the public key
     * for this account, then we can safely proceed to update the key and clear the cache.
     *
     * This listener is called upon network response and will perform the checks described in the last
     * parragraph.
     *
     */
    private WitnessResponseListener recoveryGetAccountListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG,"recovery.onSuccess. current update account task: "+currentTask);
            AccountProperties account = ((List<AccountProperties>) response.result).get(0);
            for(PublicKey publicKey : account.active.getKeyAuths().keySet()){
                int weight = account.active.getKeyAuths().get(publicKey);
                Address networkAddress = new Address(publicKey.getKey());
                Log.d(TAG, String.format("Key controlling account: %s, weight: %d", networkAddress.toString(), weight));

                // Recovering task information
                BrainKey brainKey = currentTask.getBrainKey();
                ECKey privateKey = brainKey.getPrivateKey();
                Address cachedKeyAddress = new Address(ECKey.fromPublicOnly(privateKey.getPubKey()));
                Log.d(TAG, String.format("Network address: %s, key derived address: %s", cachedKeyAddress.toString(), networkAddress.toString()));
                if(networkAddress.toString().equals(cachedKeyAddress.toString())){
                    // Only if we get the absolute confirmation that this key we're holding
                    // is the actual authority for this account we proceed to update the local
                    // information.
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    for(AccountDetails accountDetail : accountDetails){
                        if(accountDetail.account_name.equals(currentlyActive.getAccountName())){
                            try {
                                accountDetail.brain_key = currentTask.getBrainKey().getBrainKey();
                                accountDetail.wif_key = Crypt.getInstance().encrypt_string(currentTask.getBrainKey().getWalletImportFormat());
                            }catch(Exception e){
                                Log.e(TAG,"Exception while trying to update local key. Msg: "+e.getMessage());
                            }
                            break;
                        }
                    }
                    Log.i(TAG, String.format("Updating account with brain key: %s -> %s", brainKey.getBrainKey(), networkAddress.toString()));
                    /* Updating key of currently active account */
                    tinyDB.putListObject(getResources().getString(R.string.pref_wallet_accounts), accountDetails);

                    /* Updating store of old keys*/
                    oldKey = String.format("%s:%s", currentTask.getAccount().getAccountName(), brainKey.getWalletImportFormat());
                    ArrayList<String> oldKeys = tinyDB.getListString(Constants.KEY_OLD_KEYS);
                    oldKeys.add(oldKey);
                    Log.d(TAG,String.format("Updating old keys, adding: %s. List is %d items long now", brainKey.getWalletImportFormat(), oldKeys.size()));
                    tinyDB.putListString(Constants.KEY_OLD_KEYS, oldKeys);

                    /* Removing this suggestion from the stored list */
                    ArrayList<String> suggestions = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
                    for(int i = 0; i < suggestions.size(); i++){
                        if(suggestions.get(i).equals(brainKey.getBrainKey())){
                            suggestions.remove(i);
                        }
                    }
                    tinyDB.putListString(Constants.KEY_SUGGESTED_BRAIN_KEY, suggestions);
                    break;
                }else{
                    Log.d(TAG, "Got old key suggestion stored, but it does not correspond to the current network obtained current key, so we're not updating");
                }
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG,"onError. Msg: "+error.message);
        }
    };

    /**
     * Listener called only once with all the accounts data. This is done before the security update
     * and only once, just to know what keys to update for each account.
     */
    private WitnessResponseListener secUpdateGetAccountsListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(final WitnessResponse response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "accoutUpdate.getAccounts.onSuccess");
                    List<AccountProperties> accountProperties = (List<AccountProperties>) response.result;
                    for(AccountProperties accountProperty : accountProperties){
                        BrainKey brainKey = null;
                        ArrayList<AccountDetails> details = tinyDB.getListObject(getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
                        for(AccountDetails detail : details){
                            if(detail.account_name.equals(accountProperty.name)){
                                brainKey = new BrainKey(detail.brain_key, BrainKey.DEFAULT_SEQUENCE_NUMBER);
                            }
                        }
                        Log.d(TAG,"account: "+accountProperty.name);
                        boolean updateOwner = accountProperty.owner.equals(accountProperty.active);
                        boolean updateMemo = accountProperty.options.equals(accountProperty.active);
                        Log.d(TAG, "owner equals active: "+updateOwner);
                        Log.d(TAG, "memo equals active: "+updateMemo);
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
                    ArrayList<AccountDetails> currentAccountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    for(AccountDetails localAccountDetail : currentAccountDetails){
                        UserAccount account = currentTask.getAccount();
                        if(localAccountDetail.account_id.equals(account.getObjectId())){
                            try{
                                localAccountDetail.wif_key = Crypt.getInstance().encrypt_string(newBrainKey.getWalletImportFormat());
                                localAccountDetail.brain_key = newBrainKey.getBrainKey();
                                localAccountDetail.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;
                                Log.d(TAG,"updating account with name: "+localAccountDetail.account_name+", id: "+localAccountDetail.account_id+", key: "+localAccountDetail.brain_key);

                                /* Creating automatic bin backup */
                                BinHelper myBinHelper = new BinHelper(TabActivity.this, TabActivity.this);
                                myBinHelper.get_bin_bytes_from_brainkey(localAccountDetail.brain_key, localAccountDetail.account_name, localAccountDetail.pinCode);
                            }catch(Exception e){
                                Log.e(TAG, String.format("Exception while trying to update local copy of authority keys from account %s. Msg: %s", localAccountDetail.account_name, e.getMessage()));
                            }
                        }else{
                            Log.d(TAG,"Not touching account with name: "+localAccountDetail.account_name);
                        }
                    }
                    /* Storing the updated account list */
                    tinyDB.putListObject(getString(R.string.pref_wallet_accounts), currentAccountDetails);
                    accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

                    /* Updating the UI */
                    updatingAccountsDialog.onUpdateStatusChange(currentTask.getAccount(), UpdatedAccountListener.SUCCESS);

                    /* Moving the task from the update to the success list */
                    successQueue.add(currentTask);

                    /* Updating store of old keys*/
                    ArrayList<String> oldKeys = tinyDB.getListString(Constants.KEY_OLD_KEYS);
                    oldKeys.add(oldKey);
                    tinyDB.putListString(Constants.KEY_OLD_KEYS, oldKeys);

                    /* Removing brain key suggestion from shared preferences */
                    removeSuggestion(newBrainKey.getBrainKey());

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

        this.setLockListener(this);
    }

    /**
     * Checking for any interrupted account update procedure.
     * This might rarely be used.
     */
    private void checkInterruptedUpdate(){
        Log.d(TAG, "checkInterruptedUpdate");
        ArrayList<String> suggestions = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
        Log.d(TAG, String.format("suggestions is %d items long", suggestions.size()));
        if(suggestions.size() > 0){
            Log.d(TAG, String.format("Recovered %d brain keys, probably from interrupted account updates", suggestions.size()));
            BrainKey brainKey = new BrainKey(suggestions.get(0), BrainKey.DEFAULT_SEQUENCE_NUMBER);

            if(currentTask == null){
                currentTask = new UpdateAccountTask(currentlyActive, brainKey);
                getAccountsWorker = new WebsocketWorkerThread(new GetAccounts(currentlyActive.getObjectId(), recoveryGetAccountListener));
                getAccountsWorker.start();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<AccountDetails> accounts = tinyDB.getListObject(getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
        for(AccountDetails accountDetail : accounts){
            if(accountDetail.isSelected){
                this.currentlyActive = new UserAccount(accountDetail.account_id, accountDetail.account_name);
            }
        }
        checkInterruptedUpdate();
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

            /* Keeping this suggestion in shared preferences in case we get interrupted */
            storeSuggestion(suggestion);

            // Keeping a reference of the account to be changed, with the updated values
            Address address = new Address(ECKey.fromPublicOnly(newBrainKey.getPrivateKey().getPubKey()));

            // Building a transaction that will be used to update the account key
            HashMap<PublicKey, Integer> authMap = new HashMap<>();
            authMap.put(address.getPublicKey(), 1);
            Authority authority = new Authority(1, authMap, null);
            AccountOptions options = new AccountOptions(address.getPublicKey());
            AccountUpdateTransactionBuilder builder = new AccountUpdateTransactionBuilder(DumpedPrivateKey.fromBase58(null, brainKey.getWalletImportFormat()).getKey())
                    .setAccont(currentTask.getAccount())
                    .setActive(authority)
                    .setOptions(options);

            if(currentTask.isUpdateOwner()){
                // Only changing the "owner" authority in some cases.
                builder.setOwner(authority);
            }
//            if(currentTask.isUpdateMemo()){
//                // Only changing the "memo" authority if it is the same as the active.
//                builder.setOptions(options);
//            }

            Transaction transaction = builder.build();

            refreshKeyWorker = new WebsocketWorkerThread(new TransactionBroadcastSequence(transaction, new Asset("1.3.0"), refreshKeysListener), nodeIndex);
            refreshKeyWorker.start();
        } catch (MalformedTransactionException e) {
            Log.e(TAG, "MalformedTransactionException. Msg: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
        }
    }

    /**
     * We use this method to store a brain key suggestion in shared preferences
     * just in case the update procedure is interrupted.
     * @param suggestion
     */
    private void storeSuggestion(String suggestion){
        Log.d(TAG,"storeSuggestion. suggestion: "+suggestion);
        ArrayList<String> suggestionList = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
        if(suggestionList.size() > 0){
            Log.w(TAG,"Already have a previous suggestion!");
        }
        suggestionList.add(suggestion);
        tinyDB.putListString(Constants.KEY_SUGGESTED_BRAIN_KEY, suggestionList);
    }

    /**
     * Once the brain key procedure has finished, we no longer need to keep this brain key suggestion
     * in here.
     * @param suggestion
     */
    private void removeSuggestion(String suggestion){
        Log.d(TAG,"removeSuggestion. suggestion: "+suggestion);
        /* Checking that suggestion matches our memory-stored brain key */
        ArrayList<String> savedSuggestions = tinyDB.getListString(Constants.KEY_SUGGESTED_BRAIN_KEY);
        if(savedSuggestions.size() > 0){
            if(savedSuggestions.size() > 1){
                Log.w(TAG,"Have more than one suggestion in memory");
            }
            for(int i = 0; i < savedSuggestions.size(); i++){
                if(savedSuggestions.get(i).equals(suggestion)){
                    savedSuggestions.remove(i);
                    break;
                }
            }
            if(savedSuggestions.size() == 0){
                Log.d(TAG,"saving empty suggestion list, this is expected");
            }else{
                Log.w(TAG,"even after removing suggestion, the list was not empty, signaling that a previous account update operation could have been interrupted");
            }
            tinyDB.putListString(Constants.KEY_SUGGESTED_BRAIN_KEY, savedSuggestions);
        }else{
            Log.w(TAG,"No saved suggestion");
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, getSupportFragmentManager());
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
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        Fragment currentFragment = adapter.getRegisteredFragment(viewPager.getCurrentItem());
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
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
                if(account.securityUpdateFlag > AccountDetails.PRE_SECURITY_UPDATE && !DEBUG_ACCOUNT_UPDATE){
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
        if(accountList.size() == 0){
            Log.d(TAG, "Not updating account this time");
            tinyDB.putBoolean(Constants.KEY_UPDATE_DONE, true);
            return;
        }else{
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
            getAccountsWorker = new WebsocketWorkerThread(new GetAccounts(accountList, this.secUpdateGetAccountsListener));
            getAccountsWorker.start();
        }
    }

    @Override
    public void onLockReleased() {
        if(!tinyDB.getBoolean(Constants.KEY_UPDATE_DONE) || DEBUG_ACCOUNT_UPDATE ){
            startSecurityUpdate();
        }
    }
}
