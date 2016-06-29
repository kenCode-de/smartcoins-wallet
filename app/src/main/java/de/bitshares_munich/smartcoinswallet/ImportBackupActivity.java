package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.SupportMethods;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportBackupActivity extends BaseActivity {
    @Bind(R.id.tvFileChoosenBin)
    TextView tvFileChoosenBin;

    @Bind(R.id.etPinBin)
    EditText etPinBin;

    String bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_backup);
        setTitle(getResources().getString(R.string.app_name));
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btnChooseFile)
    public void onChooseFile(){
        chooseBinFile();
    }
    @OnClick(R.id.btnWallet)

    public void wallet() {


        if (etPinBin.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        }  else {
            if (etPinBin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            } else if (etPinBin.getText().length() < 5) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            }  else {
                get_account_from_brainkey(this,etPinBin.getText().toString());
            }
        }
    }


    FileChooserDialog dialog;

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

    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.hide();
            onSuccess(file.getAbsolutePath());
          //  file.getAbsolutePath();
            tvFileChoosenBin.setText(file.getName());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
         //   Toast.makeText(getApplicationContext(), name +"::::1",Toast.LENGTH_LONG).show();

        }
    };

    void onSuccess(String filepath){
        bytes = new BinHelper().getBytesFromBinFile(filepath);
    }



    public void get_account_from_brainkey(final Activity activity , final String pin) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "import_bin");
        hashMap.put("password", pin);
        hashMap.put("content", "["+bytes+"]");
        final Call<AccountDetails> postingService = service.getAccount(hashMap);
        postingService.enqueue(new Callback<AccountDetails>() {
            @Override
            public void onResponse(Response<AccountDetails> response) {
                if (response.isSuccess()) {
                  //  hideDialog();
                    AccountDetails accountDetails = response.body();
                    if (accountDetails.status.equals("failure")) {
                        Toast.makeText(activity, accountDetails.msg, Toast.LENGTH_SHORT).show();
                    } else {
                      //  addWallet(accountDetails,brainKey,pinCode);
                    }

                } else {
                   // hideDialog();
                    Toast.makeText(activity, activity.getString(R.string.unable_to_create_account_from_brainkey), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
              //  hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
