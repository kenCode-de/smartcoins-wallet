package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import de.bitshares_munich.Interfaces.InternalMovementListener;
import de.bitshares_munich.Interfaces.LockListener;
import de.bitshares_munich.models.AccountDetails;
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
public abstract class LockableActivity extends AppCompatActivity implements InternalMovementListener {
    private final String TAG = "LockableActivity";

    /* Pin pinDialog */
    private Dialog pinDialog;

    /* Reference to TinyDB, which is basically a wrapper around shared preferences */
    private TinyDB tinyDB;

    /* Internal attribute used to keep track of the activity state */
    private boolean mInternalMove = false;

    /**
     * This will inform any listener of the 'lock release' event.
     */
    private LockListener mLockListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getApplicationContext());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!mInternalMove){
            /**
             * We want to show the pin dialog if this restart was not caused by
             * an intentional internal app move.
             */
            if(this.hasAccounts()){
                /*
                * It only makes sense to show the pin dialog if there are accounts.
                */
                showDialogPin();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mInternalMove){
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
        mInternalMove = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        if (etPin.getText().toString().equals(accountDetails.get(i).pinCode)) {
                            Log.d(TAG, "pin code matches");
                            pinDialog.cancel();
                            if(mLockListener != null){
                                mLockListener.onLockReleased();
                            }
                            break;
                        }else{
                            Toast.makeText(LockableActivity.this, getResources().getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
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

    /**
     * Method used to keep state of this activity and prevent the pin dialog from showing up
     * once the user comes back to it from internal activity moves.
     */
    @Override
    public void onInternalAppMove() {
        mInternalMove = true;
    }
}
