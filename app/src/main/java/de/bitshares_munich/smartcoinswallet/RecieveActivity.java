package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.luminiasoft.bitshares.Invoice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.TransactionSmartCoin;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/16/16.
 */
public class RecieveActivity extends BaseActivity {
    private String TAG = this.getClass().getName();

    @Bind(R.id.username)
    TextView tvUsername;

    @Bind(R.id.notfound)
    TextView notfound;

    @Bind(R.id.qrimage)
    ImageView qrimage;

    @Bind(R.id.tvBlockNumberHead_rcv_screen_activity)
    TextView tvBlockNumberHead;

    @Bind(R.id.tvAppVersion_rcv_screen_activity)
    TextView tvAppVersion;

    @Bind(R.id.ivSocketConnected_rcv_screen_activity)
    ImageView ivSocketConnected;

    ProgressDialog progressDialog;

    String price = "";
    String currency = "";

    String to = "";
    String account_id = "";
    String orderId = "";

    Call<TransactionSmartCoin[]> transactionSmartcoinService;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recieve_activity);

        ButterKnife.bind(this);

        setBackButton(true);

        setTitle(getResources().getString(R.string.rcv_screen_name));

        progressDialog = new ProgressDialog(this);
//        showDialog("", this.getString(R.string.loading));
        orderId = UUID.randomUUID().toString();
        Intent intent = getIntent();


        if (intent.hasExtra(getString(R.string.to))) {
            to = intent.getStringExtra(getString(R.string.to));
            String concate = this.getString(R.string.pay_to) + " : " + to;
            tvUsername.setText(concate);

        }
        if (intent.hasExtra(getString(R.string.account_id))) {
            account_id = intent.getStringExtra(getString(R.string.account_id));

        }

        if (intent.hasExtra(getString(R.string.price))) {
            price = intent.getStringExtra(getString(R.string.price));
        } else {
            price = "0";
        }
        if (intent.hasExtra(getString(R.string.currency))) {
            currency = intent.getStringExtra(getString(R.string.currency));
        } else {
            currency = "BTS";
        }

        if (price.isEmpty()) {
            notfound.setText(getString(R.string.no_amount_requested));
        } else {
            String concate = this.getString(R.string.amount) + ": " + price + " " + currency + " " + this.getString(R.string.requested);
            notfound.setText(concate);

        }

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!price.equals("0") && !price.equals("")){
            Invoice.LineItem[] items = new Invoice.LineItem[]{ new Invoice.LineItem("transfer", 1, "%f".format(price))};
            Invoice invoice = new Invoice(to, "", "", currency.replace("bit",""), items, "", "");
            try {
                Bitmap bitmap = encodeAsBitmap(Invoice.toQrCode(invoice), "#006500");
                qrimage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.e(TAG, "WriterException while trying to encode QR-code data. Msg: "+e.getMessage());
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @OnClick(R.id.backbutton)
    void onBackButtonPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.sharebtn)
    public void TellaFriend() {
        verifyStoragePermissions(this);
        qrimage.buildDrawingCache();
        Bitmap bitmap = qrimage.getDrawingCache();
        File mFile = savebitmap(bitmap);
        try {

            String shareText = "";

            if ( !price.isEmpty() && price != "0" )
            {
                shareText = this.getString(R.string.please_pay) + " " + price + " " + currency + " " + this.getString(R.string.to) + " " + to;
            }
            else
            {
                shareText =  this.getString(R.string.please_pay) + ": " + to;
            }

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = null;
            uri = Uri.fromFile(mFile);
            sharingIntent.setData(uri);
            sharingIntent.setType("*/*");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareText);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sharingIntent, this.getString(R.string.share_qr_code)));
        } catch (Exception e) {

        }
    }

    private File savebitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() +  File.separator + getResources().getString(R.string.folder_name);
        OutputStream outStream = null;
        File file = new File(extStorageDirectory, "QrImage" + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, "QrImage" + ".png");
        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    Bitmap encodeAsBitmap(String str, String qrColor) throws WriterException {
        BitMatrix result;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.MARGIN, 0);
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, qrimage.getWidth(), qrimage.getHeight(), hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = qrimage.getWidth();
        int h = qrimage.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * (w);
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.parseColor(qrColor) : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
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

    @OnClick(R.id.ivGotoKeypad)
    void gotoKeypad() {
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), account_id);
        startActivity(intent);
        finish();
    }

    public void callIPNSmartCoins(final Activity activity) {
        Log.d(TAG, "callIPNSmartCoins. account id: "+account_id+", order id: "+orderId);
        ServiceGenerator sg = new ServiceGenerator(getString(R.string.node_server_url));
        IWebService service = sg.getService(IWebService.class);
        transactionSmartcoinService = service.getTransactionSmartCoin(account_id, orderId);
        transactionSmartcoinService.enqueue(new Callback<TransactionSmartCoin[]>() {
            @Override
            public void onResponse(Response<TransactionSmartCoin[]> response) {
                if (response.isSuccess()) {
                    if (response.body().length > 0) {
                        TransactionSmartCoin[] transactions = response.body();
                        Intent intent = new Intent(getApplicationContext(), PaymentRecieved.class);
                        intent.putExtra("block", transactions[0].block);
                        intent.putExtra("trx", transactions[0].trx);
                        intent.putExtra("receiver_id", transactions[0].account_id);
                        intent.putExtra("sender_id", transactions[0].sender_id);
                        startActivity(intent);
                        finish();
                    } else {
                        if (!isFinishing()) {
                            Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.isConnected()) {
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                        ivSocketConnected.clearAnimation();
                } else {
                    ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                    ivSocketConnected.startAnimation(myFadeInAnimation);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTask, 1000);
    }

    @OnClick(R.id.OnClickSettings_rcv_screen_activity)
    void OnClickSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
