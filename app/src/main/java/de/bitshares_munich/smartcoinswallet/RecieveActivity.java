package de.bitshares_munich.smartcoinswallet;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.QrHash;
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

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        showDialog("", this.getString(R.string.loading));
        orderId = UUID.randomUUID().toString();
        Intent intent = getIntent();



        if (intent.hasExtra(getString(R.string.to))) {
            to = intent.getStringExtra(getString(R.string.to));
            String concate = this.getString(R.string.pay_to) + to;
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
            String concate = this.getString(R.string.amount) + ": " +  price + " " + currency + " " + this.getString(R.string.requested);
            notfound.setText(concate);

        }




    HashMap hm = new HashMap();
        hm.put("account_name", to);
        hm.put("memo", "Order: " + orderId);
        hm.put("amount", price);
        hm.put("fee", 0);
        hm.put("symbol", currency);
        hm.put("callback", getString(R.string.node_server_url) + "/transaction/" + account_id + "/" + orderId + "/");
//        hm.put("note","merchant_email:\"abc@live.com\"");
        getQrHashKey(this, hm);

        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();
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
        //Drawable loadImage = getDrawable(qrimage);
//        String str = Helper.saveToInternalStorage(this,((BitmapDrawable) loadImage).getBitmap());
        try {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            Uri uri = null;
            uri = Uri.fromFile(mFile);
            sharingIntent.setData(uri);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sharingIntent, this.getString(R.string.share_qr_code)));
        } catch (Exception e) {

        }
    }
    private File savebitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
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

    public void getQrHashKey(final Activity activity, HashMap hashMap) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.qr_hash_url));
        IWebService service = sg.getService(IWebService.class);
//        final Call<QrHash> postingService = service.getQrHashWithNote(hashMap);
        final Call<QrHash> postingService = service.getQrHash(hashMap);
        postingService.enqueue(new Callback<QrHash>() {
            @Override
            public void onResponse(Response<QrHash> response) {
                if (response.isSuccess()) {
                    hideDialog();
                    QrHash qrHash = response.body();
                    try {
                        Bitmap bitmap = encodeAsBitmap(qrHash.hash, "#006500");
                        qrimage.setImageBitmap(bitmap);
                        callIPNSmartCoins(activity);
                    } catch (Exception e) {
                    }

                } else {
                    Toast.makeText(activity, activity.getString(R.string.unable_to_create_qr_code), Toast.LENGTH_SHORT).show();
                    hideDialog();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });
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
        ServiceGenerator sg = new ServiceGenerator(getString(R.string.node_server_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<TransactionSmartCoin[]> postingService = service.getTransactionSmartCoin(account_id, orderId);
        postingService.enqueue(new Callback<TransactionSmartCoin[]>() {
            @Override
            public void onResponse(Response<TransactionSmartCoin[]> response) {
                if (response.isSuccess()) {
                    if (response.body().length > 0) {
                        TransactionSmartCoin[] transactions = response.body();
                        Intent intent = new Intent(getApplicationContext(), PaymentRecieved.class);
                        intent.putExtra("block",transactions[0].block);
                        intent.putExtra("trx",transactions[0].trx);
                        intent.putExtra("receiver_id",transactions[0].account_id);
                        intent.putExtra("sender_id",transactions[0].sender_id);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        if (!isFinishing()) {
                            Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                            //Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //startActivity(intent);
                            finish();
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);
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


    /// Updating Block Number and status
    private String prevBlockNumber = "";
    private int counterBlockCheck = 0;

    private Boolean isBlockUpdated()
    {
        if ( Application.blockHead != prevBlockNumber )
        {
            prevBlockNumber = Application.blockHead;
            counterBlockCheck = 0;
            return true;
        }
        else if ( counterBlockCheck++ >= 30 )
        {
            return false;
        }

        return true;
    }

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.webSocketG != null)
                {
                    if (Application.webSocketG.isOpen() && (isBlockUpdated()))
                    {
                        boolean paused = Application.webSocketG.isPaused();
                        ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                        tvBlockNumberHead.setText(Application.blockHead);
                    }
                    else
                    {
                        ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                        Application.webSocketG.close();
                        Application.webSocketConnection();
                    }
                }
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(updateTask, 2000);
    }

    @OnClick(R.id.OnClickSettings_rcv_screen_activity)
    void OnClickSettings(){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
    ///////


}
