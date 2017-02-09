package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import de.bitshares_munich.interfaces.LockListener;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.TinyDB;

/**
 * This abstract class is used to implement way to make sure the user has recently
 * unlocked the app. We make use of the activities life-cycle to try to keep track of
 * the current state of the activity and decide whether or not to display the dialog
 * requesting the pin code.
 *
 * Every activity extending the LocableActivity must call the onInternalAppMove method
 * when calling startActivity() in order to prevent the dialog to show up when the user
 * comes back to the caller activity. If that is the desired behavior, of course.
 *
 * Created by nelson on 12/21/16.
 */
public abstract class LockableActivity extends AppCompatActivity {
    private final String TAG = "LockableActivity";

    /* Pin pinDialog */
    private Dialog pinDialog;

    /* Reference to TinyDB, which is basically a wrapper around shared preferences */
    private TinyDB tinyDB;

    //Reference to the Application
    private Application app;

    /**
     * This will inform any listener of the 'lock release' event.
     */
    private LockListener mLockListener;

    @Override
    protected void onResume() {
        super.onResume();
        if(tinyDB == null){
            tinyDB = new TinyDB(getApplicationContext());
        }
        app = (Application) getApplicationContext();
        //Lock only if timer set the lock t Application and there is any logged account
        ArrayList<AccountDetails> walletAccountList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        //Show the PIn dialog (No need to check if it is already visible because onPause events always dismiss the dialog)
        if( (app.getLock()) && (walletAccountList.size() > 0) ){
            showDialogPin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Dismiss the PIN dialog when the Activity is paused (to avoid activity memory leak)
        if(pinDialog != null && pinDialog.isShowing()){
            pinDialog.dismiss();
        }
        pinDialog = null;
    }

    /**
     * Displays a dialog asking the user to input the pin number.
     */
    private void showDialogPin() {
        final ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        pinDialog = new Dialog(this);
        pinDialog.setTitle(R.string.txt_6_digits_pin);
        pinDialog.setContentView(R.layout.activity_alert_pin_dialog);
        Button btnDone = (Button) pinDialog.findViewById(R.id.btnDone);
        final EditText etPin = (EditText) pinDialog.findViewById(R.id.etPin);
        final TextView errorPin = (TextView) pinDialog.findViewById(R.id.warning);
        //Listening to changes
        etPin.addTextChangedListener(new TextWatcher() {

            //When the user's changes and it reaches a 6 character string, it auto submits
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if(etPin.getText().length() >= 6){
                    for (int i = 0; i < accountDetails.size(); i++) {
                        if (accountDetails.get(i).isSelected) {
                            if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                                Log.d(TAG, "pin code matches");
                                pinDialog.cancel();
                                if(mLockListener != null){
                                    mLockListener.onLockReleased();
                                }
                                app = (Application) getApplicationContext();
                                //Set global context to unlocked
                                app.setLock(false);
                                break;
                            }
                        }
                    }
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }


        });

        pinDialog.setCancelable(false);
        pinDialog.show();
    }

    /**
     * Private method used to check whether the app has any account already registered.
     * @return
     */
    private boolean hasAccounts(){
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        return accountDetails.size() > 0;
    }

    public void setLockListener(LockListener listener){
        this.mLockListener = listener;
    }

    public LockListener getLockListener(){
        return this.mLockListener;
    }


}
