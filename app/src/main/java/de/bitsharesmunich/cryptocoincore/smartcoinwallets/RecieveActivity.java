package de.bitsharesmunich.cryptocoincore.smartcoinwallets;

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
import de.bitshares_munich.smartcoinswallet.BaseActivity;
import de.bitshares_munich.smartcoinswallet.BuildConfig;
import de.bitshares_munich.smartcoinswallet.PaymentRecieved;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.SettingActivity;
import de.bitshares_munich.smartcoinswallet.SplashActivity;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.graphenej.Invoice;
import de.bitsharesmunich.graphenej.LineItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
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

    Coin coin;


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
        orderId = UUID.randomUUID().toString();
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.coin))){
            this.coin = Coin.valueOf(this.getIntent().getStringExtra(getString(R.string.coin)));
        } else {
            this.coin = Coin.BITSHARE;
        }

        if (intent.hasExtra(getString(R.string.to))) {
            to = intent.getStringExtra(getString(R.string.to));String concate;
            if(coin == Coin.BITSHARE) {
                concate = this.getString(R.string.pay_to) + " : " + to;
            }else{
                concate = this.getString(R.string.pay_to) + " : " + to.substring(0,4)+"..."+to.substring(to.length()-4);
            }
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
            //currency = "BTS";
            currency = coin.getLabel();
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
        LineItem[] items = new LineItem[]{ new LineItem("transfer", 1, Double.valueOf(price))};
        if(coin == Coin.BITSHARE) {
            Invoice invoice = new Invoice(to, "", "", currency.replace("bit", ""), items, "", "");
            try {
                Bitmap bitmap = encodeAsBitmap(Invoice.toQrCode(invoice), "#006500");
                qrimage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.e(TAG, "WriterException while trying to encode QR-code data. Msg: " + e.getMessage());
            }
        }else{
            System.out.println("RecieveActivity new bitmap");
            double amount;
            if(price == null || price.isEmpty() || price.equalsIgnoreCase("0")){
                amount = 0;
            }else{
                amount = Double.parseDouble(price);
            }

            String uri = GeneralCoinFactory.getValidator(coin).toURI(to,amount);
            try {
                Bitmap bitmap = encodeAsBitmap(uri.toString(), "#006500");
                qrimage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.e(TAG, "WriterException while trying to encode QR-code data. Msg: " + e.getMessage());
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

            String shareText;

            if ( !price.isEmpty() && !price.equals("0") )
            {
                shareText = this.getString(R.string.please_pay) + " " + price + " " + currency + " " + this.getString(R.string.to) + " " + to;
            }
            else
            {
                shareText =  this.getString(R.string.please_pay) + ": " + to;
            }

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = Uri.fromFile(mFile);
            sharingIntent.setData(uri);
            sharingIntent.setType("*/*");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareText);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            // This is not specifically an internal app move, but it should be treated as such
            // because we don't want to keep asking the user for the pin number again when he returns
            // from the sharing scree. Perhaps this should be renamed to "intentionalAppMove" to
            // better reflect this situation.
            startActivity(Intent.createChooser(sharingIntent, this.getString(R.string.share_qr_code)));
        } catch (Exception ignored) {

        }
    }

    private File savebitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() +  File.separator + getResources().getString(R.string.folder_name);
        OutputStream outStream;
        File file = new File(extStorageDirectory, "QrImage" + ".png");
        if (file.exists()) {
            if(file.delete()) {
                file = new File(extStorageDirectory, "QrImage" + ".png");
            }else{
                return null;//failed
            }
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
            Map<EncodeHintType, Object> hints = new HashMap<>();
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
        System.out.println("RecieveActivity Keypad activity");
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), account_id);
        intent.putExtra(getString(R.string.coin), coin.name());
        intent.putExtra(SplashActivity.KEY_ASK_FOR_PIN, false);
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
            public void onResponse(Call<TransactionSmartCoin[]> call, Response<TransactionSmartCoin[]> response) {
                if (response.isSuccessful()) {
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
            public void onFailure(Call<TransactionSmartCoin[]> call, Throwable t) {
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
