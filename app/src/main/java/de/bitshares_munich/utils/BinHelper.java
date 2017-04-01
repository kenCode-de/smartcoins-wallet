package de.bitshares_munich.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.interfaces.BackupBinDelegate;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.Chains;
import de.bitsharesmunich.graphenej.FileBin;
import de.bitsharesmunich.graphenej.models.backup.LinkedAccount;
import de.bitsharesmunich.graphenej.models.backup.PrivateKeyBackup;
import de.bitsharesmunich.graphenej.models.backup.Wallet;
import de.bitsharesmunich.graphenej.models.backup.WalletBackup;

/**
 * Created by developer on 6/28/16.
 */
public class BinHelper {
    private String TAG = this.getClass().getName();
    private Activity myActivity;
    Handler mHandler;
    ProgressDialog progressDialog;
    BackupBinDelegate backupBinDelegate;

    public BinHelper() {
    }

    public BinHelper(Activity activity, BackupBinDelegate _backupBinDelegate) {
        myActivity = activity;
        mHandler = new Handler();
        progressDialog = new ProgressDialog(activity);
        backupBinDelegate = _backupBinDelegate;
    }

    private int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public ArrayList<Integer> getBytesFromBinFile(String filePath) {
        try {
            File file = new File(filePath);
            DataInputStream dis = new DataInputStream(new FileInputStream(file));

            ArrayList<Integer> result = new ArrayList<>();


            for (int i = 0; i < file.length(); i++) {
                int val = unsignedToBytes(dis.readByte());
                result.add(val);
            }

            dis.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public void addWallet(AccountDetails accountDetails, Context context, Activity activity) {
        TinyDB tinyDB = new TinyDB(context);
        ArrayList<AccountDetails> accountDetailsList = tinyDB.getListObject(context.getString(R.string.pref_wallet_accounts), AccountDetails.class);

        for (int i = 0; i < accountDetailsList.size(); i++) {
            if (accountDetailsList.get(i).account_name.equals(accountDetails.account_name)) {
                accountDetailsList.remove(i);
            }
        }

        for (int i = 0; i < accountDetailsList.size(); i++) {
            accountDetailsList.get(i).isSelected = false;
        }
        accountDetailsList.add(accountDetails);

        tinyDB.putListObject(context.getString(R.string.pref_wallet_accounts), accountDetailsList);

        List<TransactionDetails> emptyTransactions = new ArrayList<>();
        tinyDB.putTransactions(context.getString(R.string.pref_local_transactions), new ArrayList<>(emptyTransactions));

    }

    public boolean saveBinFile(String filePath, List<Integer> content, Activity _activity) {
        boolean success = false;
        try {
            PermissionManager Manager = new PermissionManager();
            Manager.verifyStoragePermissions(_activity);

            File file = new File(filePath);
            byte[] fileData = new byte[content.size()];

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            for (int i = 0; i < content.size(); i++) {
                fileData[i] = content.get(i).byteValue();
            }

            bos.write(fileData);
            bos.flush();
            bos.close();

            success = true;
        } catch (Exception e) {

        }

        return success;
    }

    public void get_bin_bytes_from_brainkey(final String pin, final String brnKey, final String _accountName,Context context)
    {
        Log.d(TAG, "get_bin_bytes_from_brainkey. pin: "+pin+", brnKey: "+brnKey);
        Log.d(TAG, "account name: "+_accountName);
        BrainKey brainKey = new BrainKey(brnKey, 0);
        try {
            byte[] results = FileBin.getBytesFromBrainKey(brnKey,pin,_accountName,context);
            ArrayList<Wallet> wallets = new ArrayList<>();
            ArrayList<LinkedAccount> accounts = new ArrayList<>();
            ArrayList<PrivateKeyBackup> keys = new ArrayList<>();

            Wallet wallet = new Wallet(accountName, brainKey.getBrainKey(), brainKey.getSequenceNumber(), Chains.BITSHARES.CHAIN_ID, pin);
            wallets.add(wallet);

            PrivateKeyBackup keyBackup = new PrivateKeyBackup(brainKey.getPrivateKey().getPrivKeyBytes(),
                    brainKey.getSequenceNumber(), brainKey.getSequenceNumber(), wallet.getEncryptionKey(pin));
            keys.add(keyBackup);

            LinkedAccount linkedAccount = new LinkedAccount(accountName, Chains.BITSHARES.CHAIN_ID);
            accounts.add(linkedAccount);

            WalletBackup backup = new WalletBackup(wallets, keys, accounts);
            byte[] results = FileBin.serializeWalletBackup(backup, pin);
            List<Integer> resultFile = new ArrayList<>();
            for (byte result : results) {
                resultFile.add(result & 0xff);
            }
            saveBinContentToFile(resultFile, accountName);
        } catch (Exception e) {
            hideDialog(false);
            Log.e(TAG, "Exception. Msg: " + e.getMessage());
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Create the backup bin file version for WIF Imported accounts.
     *
     * @param pin: Wallet PIN string.
     * @param wif_key: Encrypted WIF string.
     * @param accountName: Name of the account string.
     *
     */
    public void getBinBytesFromWif(final String pin, final String wif_key, final String accountName) {
        BrainKey brainKey = new BrainKey("", 0);
        try {
            ArrayList<Wallet> wallets = new ArrayList<>();
            ArrayList<LinkedAccount> accounts = new ArrayList<>();
            ArrayList<PrivateKeyBackup> keys = new ArrayList<>();

            Wallet wallet = new Wallet(accountName,
                    brainKey.getSequenceNumber(),
                    Chains.BITSHARES.CHAIN_ID, pin);
            wallets.add(wallet);

            String wif = Crypt.getInstance().decrypt_string(wif_key);
            ECKey key = DumpedPrivateKey.fromBase58(NetworkParameters.fromID(NetworkParameters.ID_MAINNET), wif ).getKey();

            PrivateKeyBackup keyBackup = new PrivateKeyBackup(key.getPrivKeyBytes(),
                    brainKey.getSequenceNumber(),
                    brainKey.getSequenceNumber(),
                    wallet.getEncryptionKey(pin));
            keys.add(keyBackup);

            LinkedAccount linkedAccount = new LinkedAccount(accountName, Chains.BITSHARES.CHAIN_ID);
            accounts.add(linkedAccount);

            WalletBackup backup = new WalletBackup(wallets, keys, accounts);
            byte[] results = FileBin.serializeWalletBackup(backup, pin);
            List<Integer> resultFile = new ArrayList<>();
            for (byte result : results) {
                resultFile.add(result & 0xff);
            }
            saveBinContentToFile(resultFile, accountName);
        } catch (Exception e) {
            hideDialog(false);
            Log.e(TAG, "Exception. Msg: " + e.getMessage());
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.unable_to_generate_bin_format_for_key), Toast.LENGTH_SHORT).show();
        }
    }


    public void saveBinContentToFile(List<Integer> content, String _accountName) {
        changeDialogMsg(myActivity.getResources().getString(R.string.saving_bin_file_to) + " : " + myActivity.getResources().getString(R.string.folder_name));

        String folder = Environment.getExternalStorageDirectory() + File.separator + myActivity.getResources().getString(R.string.folder_name);
        String path = folder + File.separator + _accountName + ".bin";

        boolean success = saveBinFile(path, content, myActivity);

        hideDialog(success);

        if (success) {
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.bin_file_saved_successfully_to) + " : " + path, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.unable_to_save_bin_file), Toast.LENGTH_LONG).show();
        }
    }

    private void hideDialog(boolean success) {

        backupBinDelegate.backupComplete(success);

        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                }
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

    private void changeDialogMsg(String msg) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.setMessage(msg);
            }
        }
    }

    public void createBackupBinFile(final String _brnKey,final String _accountName,final String pinCode,final Context context)
    {
        showDialog(myActivity.getResources().getString(R.string.creating_backup_file), myActivity.getResources().getString(R.string.fetching_key));

        if (_brnKey.isEmpty()) {
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.unable_to_load_brainkey), Toast.LENGTH_LONG).show();
            hideDialog(false);
            return;
        }

        if (pinCode.isEmpty()) {
            hideDialog(false);
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
            return;
        }

        changeDialogMsg(myActivity.getResources().getString(R.string.generating_bin_format));

        Runnable getFormat = new Runnable() {
            @Override
            public void run()
            {
                get_bin_bytes_from_brainkey(pinCode,_brnKey,_accountName,context);
            }
        };

        mHandler.postDelayed(getFormat, 200);
    }

    /*
     * Create the backup bin file for WIF imported account
     */
    public void createBackupBinFileFromWif(final String _wif, final String _accountName, final String pinCode) {
        showDialog(myActivity.getResources().getString(R.string.creating_backup_file), myActivity.getResources().getString(R.string.fetching_key));

        if (_wif.isEmpty()) {
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.unable_to_load_wif), Toast.LENGTH_LONG).show();
            hideDialog(false);
            return;
        }

        if (pinCode.isEmpty()) {
            hideDialog(false);
            Toast.makeText(myActivity, myActivity.getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
            return;
        }

        changeDialogMsg(myActivity.getResources().getString(R.string.generating_bin_format));

        Runnable getFormat = new Runnable() {
            @Override
            public void run() {
                getBinBytesFromWif(pinCode, _wif, _accountName);
            }
        };

        mHandler.postDelayed(getFormat, 200);
    }

    private String getPin(ArrayList<AccountDetails> accountDetails) {
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).pinCode;
            }
        }

        return "";
    }

    private String getBrainKey(ArrayList<AccountDetails> accountDetails) {
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).brain_key;
            }
        }

        return "";
    }

    private String getAccountName(ArrayList<AccountDetails> accountDetails) {
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).account_name;
            }
        }

        return "";
    }

    public void createBackupBinFile(Context context)
    {
        TinyDB tinyDB = new TinyDB(myActivity);
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(myActivity.getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String _brnKey = getBrainKey(accountDetails);
        String _accountName = getAccountName(accountDetails);
        String _pinCode = getPin(accountDetails);

        get_bin_bytes_from_brainkey(_pinCode, _brnKey, _accountName,context);
    }

    public int numberOfWalletAccounts(Context _context) {
        TinyDB tinyDB = new TinyDB(_context);
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(_context.getResources().getString(R.string.pref_wallet_accounts), AccountDetails.class);

        if (accountDetails != null) {
            return accountDetails.size();
        } else {
            return 0;
        }

    }


}
