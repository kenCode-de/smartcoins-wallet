package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.interfaces.BackupBinDelegate;
import de.bitshares_munich.interfaces.InternalMovementListener;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.TabActivity;

/**
 * Created by qasim on 7/13/16.
 */
public class BackupBrainkeyActivity extends BaseActivity implements BackupBinDelegate {
    private String TAG = this.getClass().getName();

    TinyDB tinyDB;
    ArrayList<AccountDetails> accountDetails;
    Boolean isBackupKey = false;

    @Bind(R.id.btnDone)
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_brainkey);
        ButterKnife.bind(this);
        tinyDB = new TinyDB(getApplicationContext());
        BinHelper myBinHelper = new BinHelper(this, this);
        myBinHelper.createBackupBinFile(getApplicationContext());

    }

    private void showDialogBackupBrainKey() {
        accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_delete_dialog);
        dialog.setCancelable(false);
        final Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final TextView alertMsg = (TextView) dialog.findViewById(R.id.alertMsg);
        alertMsg.setText(getString(R.string.backup_brainkey_msg));
        final Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setVisibility(View.GONE);
        btnCancel.setBackgroundColor(Color.RED);
        btnCancel.setText(getString(R.string.txt_no));
        btnDone.setText(getString(R.string.got_it));
        btnDone.setEnabled(false);
        btnDone.setBackgroundColor(Color.rgb(211, 211, 211));
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnDone.setBackground(getResources().getDrawable(R.drawable.button_border));
                btnDone.setEnabled(true);
            }
        }, 2000);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "onCancel");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @OnClick(R.id.ivBackupBrainKey)
    public void ivBackupBrainKey(ImageView imageView) {
        showDialogCopyBrainKey();
    }

    @OnClick(R.id.tvBackupBrainKey)
    public void tvBackupBrainKey(TextView textView) {
        showDialogCopyBrainKey();
    }

    @OnClick(R.id.btnDone)
    public void btnDone(Button button) {

        if (isBackupKey) {
            Helper.storeBoolianSharePref(getApplicationContext(),getString(R.string.pref_backup_bin_exist),true);
            Intent intent = new Intent(getApplicationContext(), TabActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            ((InternalMovementListener)this).onInternalAppMove();
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.please_backup_your_brainkey), Toast.LENGTH_SHORT).show();
        }

    }

    private void showDialogCopyBrainKey() {
        isBackupKey = true;
        final Dialog dialog = new Dialog(this, R.style.stylishDialog);
        dialog.setTitle(getString(R.string.backup_brainkey));
        dialog.setContentView(R.layout.activity_copybrainkey);
        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
        try {
            String brainKey = getBrainKey();

            /*If there is a master seed, then add it to the mnemonics*/
            SCWallDatabase db = new SCWallDatabase(getApplicationContext());
            List<AccountSeed> seeds = db.getSeeds(SeedType.BIP39);
            if (seeds.size() > 0){
                AccountSeed masterSeed = (BIP39)seeds.get(0);
                if (!brainKey.isEmpty()) {
                    brainKey += " "+masterSeed.getMnemonicCodeString().toUpperCase();
                }
            }

            if (brainKey.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_to_load_brainkey), Toast.LENGTH_LONG).show();
                return;
            } else {
                etBrainKey.setText(brainKey);
            }
        } catch (Exception e) {

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
                Toast.makeText(BackupBrainkeyActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                clipboard.setPrimaryClip(clip);
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);

        dialog.show();
    }

    private String getBrainKey() {
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).brain_key;
            }
        }

        return "";
    }

    @Override
    public void backupComplete(boolean success) {
        showDialogBackupBrainKey();
    }

}
