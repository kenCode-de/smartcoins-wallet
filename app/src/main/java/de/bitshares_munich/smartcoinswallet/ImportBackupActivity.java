package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HTTP;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.PermissionManager;
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

    @Bind(R.id.btnWalletBin)
    Button btnWalletBin;

    ArrayList<Integer> bytes;

    Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_backup);
        setTitle(getResources().getString(R.string.app_name));
        ButterKnife.bind(this);
        myActivity = this;
        progressDialog = new ProgressDialog(this);
    }

    @OnClick(R.id.btnChooseFile)
    public void onChooseFile(){
        chooseBinFile();
    }

    @OnClick(R.id.btnWalletBin)
    public void onClickbtnWalletBin()
    {
        String pinText = etPinBin.getText().toString();

        if (pinText.length() == 0)
        {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (pinText.length() < 5)
            {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            }
            else if (pinText.length() < 5)
            {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            }
            else
            {
                showDialog("",getString(R.string.importing_keys_from_bin_file));
                get_account_from_brainkey(pinText);
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
        PermissionManager manager = new PermissionManager();
        manager.verifyStoragePermissions(this);

        bytes = new BinHelper().getBytesFromBinFile(filepath);
    }

    private void postData(String method,String pin, ArrayList<Integer> _bytes){
        try{
            String postReceiverUrl = getString(R.string.account_from_brainkey_url);

            HttpParams httpParameters = new BasicHttpParams();
            HttpClient httpClient = new DefaultHttpClient(httpParameters);

            httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
            httpClient.getParams().setParameter("http.socket.timeout", 2000);
            httpClient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
            httpParameters.setBooleanParameter("http.protocol.expect-continue", false);

            HttpPost httpPost = new HttpPost(postReceiverUrl);
            httpPost.getParams().setParameter("http.socket.timeout", 5000);

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("method", method));
            nameValuePairs.add(new BasicNameValuePair("pin", pin));
            //nameValuePairs.add(new );

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
            httpPost.setEntity(formEntity);

            HttpResponse response = httpClient.execute(httpPost);

            java.io.BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String result = sb.toString();

            Log.e("result", result);


            //HttpEntity resEntity = response.getEntity();


            //if (resEntity != null) {

            //String responseStr = EntityUtils.toString(resEntity).trim();

            //}

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ProgressDialog progressDialog;
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

    public void get_account_from_brainkey(final String pin) {

        try
        {
            ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
            IWebService service = sg.getService(IWebService.class);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("method", "import_bin");
            hashMap.put("password", pin);
            hashMap.put("content", bytes.toArray());

            Call<AccountDetails> postingService = service.getAccountFromBin(hashMap);

            postingService.enqueue(new Callback<AccountDetails>() {
                @Override
                public void onResponse(Response<AccountDetails> response) {
                    hideDialog();
                    if (response.isSuccess())
                    {
                        AccountDetails accountDetails = response.body();
                        if (accountDetails.status.equals("failure"))
                        {
                            Toast.makeText(myActivity, accountDetails.msg, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //  addWallet(accountDetails,brainKey,pinCode);
                            //Toast.makeText(myActivity, "success", Toast.LENGTH_SHORT).show();
                            try
                            {
                                //Crypt cr = new Crypt();
                                String brn = "";
                                try
                                {
                                    brn = Crypt.getInstance().decrypt_string(accountDetails.brain_key);
                                }
                                catch (Exception e)
                                {
                                    brn = "";
                                }
                                new BinHelper().addWallet(accountDetails, brn, pin, getApplicationContext(),myActivity);

                                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(myActivity, myActivity.getString(R.string.unable_to_import_account_from_bin_file) + " : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    else
                    {
                        // hideDialog();
                        Log.d("bin","fail");
                        Toast.makeText(myActivity, myActivity.getString(R.string.unable_to_import_account_from_bin_file), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    hideDialog();
                    Log.d("bin","fail");
                    Toast.makeText(myActivity, myActivity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e)
        {
            hideDialog();
            Log.d("bin",e.getMessage());
            Toast.makeText(myActivity, myActivity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
        }

    }

}
