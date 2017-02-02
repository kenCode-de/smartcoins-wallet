package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.bitshares_munich.Interfaces.LockListener;
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
    protected void onRestart() {
        super.onRestart();
        if(tinyDB == null){
            tinyDB = new TinyDB(getApplicationContext());
        }
        app = (Application) getApplicationContext();
        Log.i(TAG, "Activity Created: " +  String.valueOf(app.getLock()) );

        //Lock only if timer set the lock t Application and there is any logged account
        ArrayList<AccountDetails> walletAccountList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if( (app.getLock()) && (walletAccountList.size() > 0) ){
            /**
             * We want to show the pin dialog if this restart was not caused by
             * an intentional internal app move.
             */
            showDialogPin();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(tinyDB == null){
            tinyDB = new TinyDB(getApplicationContext());
        }
        app = (Application) getApplicationContext();
        Log.i(TAG, "Activity Started: " +  String.valueOf(app.getLock()) );
        //Lock only if timer set the lock t Application and there is any logged account
        ArrayList<AccountDetails> walletAccountList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if( (app.getLock()) && (walletAccountList.size() > 0) ){
            /**
             * We might want to display the pin dialog if this onStart call is
             * not a result of an intentional internal app movement.
             */
            if(pinDialog == null || !pinDialog.isShowing()){
                /**
                 * The dialog must already be up, because onStart is called after
                 * onRestart. Here we just check for that.
                 */
                Bundle extras = getIntent().getExtras();
                if(extras != null){
                    boolean showPin = extras.getBoolean(SplashActivity.KEY_ASK_FOR_PIN);
                    if(showPin){
                        showDialogPin();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Activity Paused: " +  String.valueOf(app.getLock()) );
        if(pinDialog != null && pinDialog.isShowing()){
            pinDialog.dismiss();
        }
        pinDialog = null;
    }

    /**
     * Displays a dialog asking the user to input the pin number.
     */
    private void showDialogPin() {
        if(tinyDB == null){
            tinyDB = new TinyDB(getApplicationContext());
        }
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

    public void setLockListener(LockListener listener){
        this.mLockListener = listener;
    }

    public LockListener getLockListener(){
        return this.mLockListener;
    }


}
