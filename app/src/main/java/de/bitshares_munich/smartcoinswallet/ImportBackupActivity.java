package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.PermissionManager;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.FileBin;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetKeyReferences;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.models.backup.WalletBackup;

public class ImportBackupActivity extends BaseActivity {
    private final String TAG = this.getClass().getName();

    @Bind(R.id.tvFileChoosenBin)
    TextView tvFileChoosenBin;

    @Bind(R.id.etPinBin)
    EditText etPinBin;

    @Bind(R.id.etExistingPassword)
    EditText etExistingPassword;

    ArrayList<Integer> bytes;
    FileChooserDialog dialog;
    ProgressDialog progressDialog;
    /* Database interface */
    private SCWallDatabase database;
    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.dismiss();
            onSuccess(file.getAbsolutePath());
            tvFileChoosenBin.setText(file.getName());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_backup);
        setTitle(getResources().getString(R.string.app_name));
        ButterKnife.bind(this);
        setBackButton(true);
        progressDialog = new ProgressDialog(this);
        database = new SCWallDatabase(this);
    }

    @OnClick(R.id.btnChooseFile)
    public void onChooseFile() {
        chooseBinFile();
    }

    @OnClick(R.id.btnWalletBin)
    public void onClickbtnWalletBin() {
        String currentPassword = etExistingPassword.getText().toString();
        String pinText = etPinBin.getText().toString();

        if (currentPassword.length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.missing_existing_password), Toast.LENGTH_SHORT).show();
        } else if (pinText.length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.pin_number_request), Toast.LENGTH_SHORT).show();
        } else if (pinText.length() < 6) {
            Toast.makeText(this, getResources().getString(R.string.pin_number_warning), Toast.LENGTH_SHORT).show();
        } else {
            showDialog("", getString(R.string.importing_keys_from_bin_file));
            recoverAccountFromBackup(currentPassword, pinText);
        }
    }

    private void chooseBinFile() {
        if (dialog == null) {
            dialog = new FileChooserDialog(this);
            dialog.addListener(this.onFileSelectedListener);
            dialog.setFolderMode(false);
            dialog.setCanCreateFiles(false);
            dialog.setShowCancelButton(true);
            dialog.setShowOnlySelectable(false);
            dialog.setFilter(".*bin");


            // Activate the confirmation dialogs.
            dialog.setShowConfirmation(true, true);
            // Define the labels.
            FileChooserLabels labels = new FileChooserLabels();
            labels.createFileDialogAcceptButton = getApplicationContext().getString(R.string.ok);
            labels.createFileDialogCancelButton = getApplicationContext().getString(R.string.cancel);
            labels.labelSelectButton = getApplicationContext().getString(R.string.select);
            labels.messageConfirmSelection = getApplicationContext().getString(R.string.are_you_sure);
            labels.labelConfirmYesButton = getApplicationContext().getString(R.string.txt_yes);
            labels.labelConfirmNoButton = getApplicationContext().getString(R.string.txt_no);
            labels.labelCancelButton = getApplicationContext().getString(R.string.cancel);
            dialog.setLabels(labels);
        }

        // Show the dialog.
        dialog.show();

    }

    void onSuccess(String filepath) {
        PermissionManager manager = new PermissionManager();
        manager.verifyStoragePermissions(this);

        bytes = new BinHelper().getBytesFromBinFile(filepath);
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

    public void recoverAccountFromBackup(String existingPassword, final String pin) {
        try {
            byte[] byteArray = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                byteArray[i] = bytes.get(i).byteValue();
            }

            WalletBackup walletBackup = FileBin.deserializeWalletBackup(byteArray, existingPassword);

            //Empty Backup, or only wallet backup without account
            if (walletBackup.getKeyCount() == 0) {
                Toast.makeText(ImportBackupActivity.this,
                        getResources().getString(R.string.backup_no_keys_found_error),
                        Toast.LENGTH_LONG).show();
                return;
            }
            //If not empty backup continue

            //Get the WIF directly from the informed private key inside the bin backup file
            String wif = ECKey.fromPrivate(walletBackup.getPrivateKeys()[0].
                    decryptPrivateKey(walletBackup.getWallet(0).
                            getEncryptionKey(existingPassword))).decompress().
                    getPrivateKeyEncoded(NetworkParameters.fromID(NetworkParameters.ID_MAINNET)).
                    toString();

            String brainKey;
            brainKey = walletBackup.getWallet(0).decryptBrainKey(existingPassword);
            BrainKey bKey = new BrainKey(brainKey, 0);

            Address address = new Address(ECKey.fromPublicOnly(bKey.getPrivateKey().getPubKey()));
            //Get the WIF from the derivation of the private key using the informed Brain Key
            //inside the bin backup file
            String derived_wif = bKey.getWalletImportFormat();
            final String privkey = Crypt.getInstance().encrypt_string(derived_wif);
            final String pubkey = address.toString();
            Log.d(TAG, "Got brain key: " + brainKey);
            Log.d(TAG, "Looking up keys for address: " + address.toString());
            final String finalBrainKey = brainKey;


            Log.d(TAG, "Imported WIF: " + wif);
            Log.d(TAG, "Derived WIF: " + derived_wif);
            //Compare the private key generated by the Brain Key and the private key inside the backup
            //If they are different, than it should be the special case of WIF imported based bin backup
            if (!wif.equals(derived_wif)) {
                Log.d(TAG, "WIF based account backup.");
                //Assume it is an WIF imported backup type
                getAccountFromWif(wif, pin);
            }
            //Normal Account, proceed as usual
            else {
                /* Storing brainkey information */
                database.insertKey(bKey);

                Log.d(TAG, "Normal account.");
                new WebsocketWorkerThread(new GetKeyReferences(address, new WitnessResponseListener() {
                    @Override
                    public void onSuccess(final WitnessResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getAccountByAddress.onSuccess");
                                List<List<UserAccount>> resp = (List<List<UserAccount>>) response.result;
                                if (resp.size() > 0) {
                                    List<UserAccount> accounts = resp.get(0);
                                    if (accounts.size() == 0) {
                                        Log.w(TAG, "Found no account using the key given by backup.");
                                        Toast.makeText(ImportBackupActivity.this,
                                                getResources().getString(R.string.backup_no_keys_found_error),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        for (UserAccount account : accounts) {
                                            getAccountById(account.getObjectId(), privkey, pubkey, finalBrainKey, pin);
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Invalid address");
                                    Toast.makeText(getApplicationContext(), R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                                }
                                hideDialog();
                            }
                        });
                    }

                    @Override
                    public void onError(final BaseResponse.Error error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "onError. Msg: " + error.message);
                                hideDialog();
                                Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }), 0).start();
            }
        } catch (Exception e) {
            hideDialog();
            Toast.makeText(this, getString(R.string.please_make_sure_your_bin_file), Toast.LENGTH_LONG).show();
        }
    }

    public void getAccountFromWif(final String wif, final String pinCode) {
        try {

            /* Storing wif in database (Used to insert any key in the WIF format,
            regardless of which key generation scheme was used. */
            database.insertKey(wif);

            ECKey key = DumpedPrivateKey.fromBase58(NetworkParameters.fromID(NetworkParameters.ID_MAINNET), wif).getKey();

            Address address = new Address(ECKey.fromPublicOnly(key.getPubKey()));
            final String encryptedPrivateKey = Crypt.getInstance().encrypt_string(wif);
            final String pubkey = address.toString();
            Log.d(TAG, String.format("WIF: '%s'", wif));
            Log.d(TAG, String.format("WIF would generate address: %s", address.toString()));

            new WebsocketWorkerThread(new GetKeyReferences(address, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    final List<List<UserAccount>> resp = (List<List<UserAccount>>) response.result;
                    Log.d(TAG, "getAccountByAddress.onSuccess");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resp.size() > 0) {
                                List<UserAccount> accounts = resp.get(0);
                                if (accounts.size() > 0) {
                                    //It must be only one
                                    for (UserAccount account : accounts) {
                                        Log.d(TAG, String.format("Account: %s", account.toString()));
                                        getAccountById(account.getObjectId(), encryptedPrivateKey, pubkey, pinCode);
                                    }
                                } else {
                                    hideDialog();

                                    Log.w(TAG, "Found no account using the key given by backup.");
                                    Toast.makeText(ImportBackupActivity.this, getResources().getString(R.string.backup_no_keys_found_error), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                hideDialog();

                                Log.w(TAG, "Found no account using the key given by backup.");
                                Toast.makeText(ImportBackupActivity.this, getResources().getString(R.string.backup_no_keys_found_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.unable_to_load_wif, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }), 0).start();
        } catch (IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } catch (NoSuchPaddingException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }

    }

    private void getAccountById(final String accountId, final String privaKey, final String pubKey, final String brainkey, final String pinCode) {
        Log.d(TAG, "getAccountById");
        try {
            new WebsocketWorkerThread((new GetAccounts(accountId, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    hideDialog();
                    if (response.result.getClass() == ArrayList.class) {
                        List list = (List) response.result;
                        if (list.size() > 0) {
                            if (list.get(0).getClass() == AccountProperties.class) {
                                AccountProperties accountProperties = (AccountProperties) list.get(0);
                                AccountDetails accountDetails = new AccountDetails();
                                accountDetails.account_name = accountProperties.name;
                                accountDetails.account_id = accountProperties.id;
                                accountDetails.wif_key = privaKey;
                                accountDetails.pub_key = pubKey;
                                accountDetails.isSelected = true;
                                accountDetails.status = "success";
                                accountDetails.brain_key = brainkey;
                                accountDetails.pinCode = pinCode;
                                accountDetails.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;

                                //Success Import(Set app lock to false)
                                Application app = (Application) getApplicationContext();
                                app.setLock(false);

                                //Avoid view leak
                                hideDialog();

                                BinHelper myBinHelper = new BinHelper();
                                myBinHelper.addWallet(accountDetails, getApplicationContext(), ImportBackupActivity.this);
                                Intent intent;
                                int numberOfAccounts = myBinHelper.numberOfWalletAccounts(getApplicationContext());
                                if (myBinHelper.numberOfWalletAccounts(getApplicationContext()) <= 1) {
                                    intent = new Intent(getApplicationContext(), BackupBrainkeyActivity.class);
                                } else {
                                    intent = new Intent(getApplicationContext(), TabActivity.class);
                                }

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.unable_to_get_account_properties, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                    }
                    hideDialog();
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                }
            })), 0).start();
        } catch (Exception e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void getAccountById(String accountId, final String wif, final String pubKey, final String pinCode) {
        try {
            new WebsocketWorkerThread((new GetAccounts(accountId, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        List list = (List) response.result;
                        if (list.size() > 0) {
                            if (list.get(0).getClass() == AccountProperties.class) {
                                AccountProperties accountProperties = (AccountProperties) list.get(0);
                                AccountDetails accountDetails = new AccountDetails();
                                accountDetails.account_name = accountProperties.name;
                                accountDetails.account_id = accountProperties.id;
                                accountDetails.wif_key = wif;
                                accountDetails.pub_key = pubKey;
                                accountDetails.brain_key = "";
                                accountDetails.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;
                                accountDetails.isSelected = true;
                                accountDetails.status = "success";
                                accountDetails.pinCode = pinCode;

                                //Success Import(Set app lock to false)
                                Application app = (Application) getApplicationContext();
                                app.setLock(false);
                                //Don't force backup screen
                                Helper.storeBoolianSharePref(getApplicationContext(), getString(R.string.pref_backup_bin_exist), true);

                                //Avoid view leak
                                hideDialog();

                                BinHelper myBinHelper = new BinHelper();
                                myBinHelper.addWallet(accountDetails, getApplicationContext(), ImportBackupActivity.this);
                                Intent intent = new Intent(getApplicationContext(), TabActivity.class);


                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                hideDialog();
                                Toast.makeText(getApplicationContext(), R.string.unable_to_get_account_properties, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_wif, Toast.LENGTH_SHORT).show();
                }
            })), 0).start();
            //mWebSocket.connect();
        } catch (Exception e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.btnCancelBin)
    public void OnCancel(Button button) {
        this.finish();
    }
}
