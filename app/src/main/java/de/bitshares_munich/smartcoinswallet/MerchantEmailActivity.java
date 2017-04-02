package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import java.io.File;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import de.bitshares_munich.interfaces.BackupBinDelegate;
import de.bitshares_munich.models.MerchantEmail;

/**
 * Created by afnan on 10/11/16.
 */
public class MerchantEmailActivity implements BackupBinDelegate {
    static public BackupBinDelegate backupBinDelegate;
    Activity context;
    FileChooserDialog dialog;
    ProgressDialog progressDialog;
    String msg;
    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, final File file) {
            source.hide();

            showDialog();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    MerchantEmail merchantEmail = new MerchantEmail(context);
                    merchantEmail.readFromFile(file.getAbsolutePath());
                }
            });
            thread.start();
//            onSuccess(file.getAbsolutePath(),file.getName());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
        }
    };

    public MerchantEmailActivity(Activity _context) {
        context = _context;
        progressDialog = new ProgressDialog(context);
        backupBinDelegate = this;
        chooseMerchantBackUpFile();
    }

    private void chooseMerchantBackUpFile() {
        if (dialog == null) {
            dialog = new FileChooserDialog(context);
            dialog.addListener(this.onFileSelectedListener);
            dialog.setFolderMode(false);
            dialog.setCanCreateFiles(false);
            dialog.setShowCancelButton(true);
            dialog.setShowOnlySelectable(false);
            dialog.setFilter(".*txt");


            // Activate the confirmation dialogs.
            dialog.setShowConfirmation(true, true);
            // Define the labels.
            FileChooserLabels labels = new FileChooserLabels();
            labels.createFileDialogAcceptButton = context.getString(R.string.ok);
            labels.createFileDialogCancelButton = context.getString(R.string.cancel);
            labels.labelSelectButton = context.getString(R.string.select);
            labels.messageConfirmSelection = context.getString(R.string.are_you_sure);
            labels.labelConfirmYesButton = context.getString(R.string.txt_yes);
            labels.labelConfirmNoButton = context.getString(R.string.txt_no);
            labels.labelCancelButton = context.getString(R.string.cancel);
            dialog.setLabels(labels);
        }

        this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        // Show the dialog.
        dialog.show();

    }

    private void hideDialog() {

        context.runOnUiThread(new Runnable() {
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

    private void showDialog() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    if (!progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                }
            }
        });
    }

    @Override
    public void backupComplete(boolean success) {
        hideDialog();
        if (success) {
            msg = context.getResources().getString(R.string.merchant_email_has_been_imported_successfully);
        } else {
            msg = context.getResources().getString(R.string.please_import_the_correct_file);
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
