package de.bitshares_munich.smartcoinswallet;

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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetAccountsByAddress;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by Vinícius on 2/28/17.
 */
public class ImportWifActivity extends BaseActivity {
    private String TAG = this.getClass().getName();
    ProgressDialog progressDialog;
    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;


    @Bind(R.id.etWif)
    TextView etWif;

    @Bind(R.id.etPin)
    EditText etPin;

    @Bind(R.id.etPinConfirmation)
    EditText etPinConfirmation;

    /* Database interface */
    private SCWallDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wif);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(getApplicationContext());

        progressDialog = new ProgressDialog(this);
        database = new SCWallDatabase(this);

    }

    @OnClick(R.id.btnWallet)
    public void wallet(Button button) {
        //WIF must not be empty
        if (etWif.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_wif, Toast.LENGTH_SHORT).show();
        }
        else {
            String trimmedWif = etWif.getText().toString().trim();
            etWif.setText(trimmedWif);
            if (etPin.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            }
            //PIN must have minimum of 6-digit
            else if (etPin.getText().length() < 6) {
                Toast.makeText(getApplicationContext(), R.string.pin_number_warning, Toast.LENGTH_SHORT).show();
            }
            //PIN and confirm must be equal
            else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            }
            //WIF Checksum Checking
            else if ( !(Helper.wifChecksumChecking(trimmedWif)) ) {
                Toast.makeText(getApplicationContext(), R.string.invalid_wif, Toast.LENGTH_SHORT).show();
            }
            //If an account with this WIF already exists
            else if (checkWifExist(trimmedWif)) {
                Toast.makeText(getApplicationContext(), R.string.account_already_exist, Toast.LENGTH_SHORT).show();
            }
            //If success at all validations
            else {
                //showDialog("", getString(R.string.importing_your_wallet));
                getAccountFromWif(trimmedWif, etPin.getText().toString());
            }
        }
    }

    /*
     * Check if an account with the passed WIF already exists.
     *
     * @param wif Wallet Import Format string
     * @return boolean
     */
    private boolean checkWifExist(String wif) {
        boolean isWif = false;
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        for (int i = 0; i < accountDetails.size(); i++) {
            try {
                if (wif.equals(accountDetails.get(i).wif_key)) {
                    isWif = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return isWif;

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
            Log.d(TAG,String.format("WIF: '%s'",wif));
            Log.d(TAG, String.format("WIF would generate address: %s", address.toString()));

            new WebsocketWorkerThread(new GetAccountsByAddress(address, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    final List<List<UserAccount>> resp = (List<List<UserAccount>>) response.result;
                    Log.d(TAG, "getAccountByAddress.onSuccess");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(resp.size() > 0){
                                List<UserAccount> accounts = resp.get(0);
                                if(accounts.size() > 0){
                                    //It must be only one
                                    for(UserAccount account : accounts) {
                                        Log.d(TAG, String.format("Account: %s", account.toString()));
                                        getAccountById(account.getObjectId(), encryptedPrivateKey, pubkey, pinCode);
                                    }
                                }else{
                                    hideDialog();
                                    Toast.makeText(getApplicationContext(), R.string.wif_error_invalid_account, Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                hideDialog();
                                Toast.makeText(getApplicationContext(), R.string.wif_error_invalid_account, Toast.LENGTH_SHORT).show();
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

    private void getAccountById(String accountId, final String wif, final String pubKey,  final String pinCode){
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
                                Helper.storeBoolianSharePref(getApplicationContext(),getString(R.string.pref_backup_bin_exist),true);

                                BinHelper myBinHelper = new BinHelper();
                                myBinHelper.addWallet(accountDetails, getApplicationContext(), ImportWifActivity.this);
                                Intent intent = new Intent(getApplicationContext(), TabActivity.class);


                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
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
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_wif, Toast.LENGTH_SHORT).show();
                }
            })),0).start();
            //mWebSocket.connect();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }
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
