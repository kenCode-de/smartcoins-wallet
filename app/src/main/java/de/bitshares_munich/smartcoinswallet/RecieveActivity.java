package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.QrHash;
import de.bitshares_munich.models.TransactionSmartCoin;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Syed Muhammad Muzzammil on 5/16/16.
 */
public class RecieveActivity extends Activity {

    @Bind(R.id.username)
    TextView tvUsername;

    @Bind(R.id.notfound)
    TextView notfound;

    @Bind(R.id.qrimage)
    ImageView qrimage;

    ProgressDialog progressDialog;

    String price = "";
    String currency = "";

    String to = "";
    String account_id = "";
    String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recieve_activity);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        showDialog("", "Loading...");
        orderId = UUID.randomUUID().toString();
        Intent intent = getIntent();


        if (intent.hasExtra(getString(R.string.to))) {
            to = intent.getStringExtra(getString(R.string.to));
            tvUsername.setText("Pay to: " + to);

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
            notfound.setText("Amount: " + price + " " + currency + " requested");

        }

        HashMap hm = new HashMap();
        hm.put("account_name", to);
        hm.put("memo", "Order: " + orderId);
        hm.put("amount", price);
        hm.put("fee", 0);
        hm.put("symbol", currency);
        hm.put("callback", getString(R.string.node_server_url) + "/transaction/" + account_id + "/" + orderId);
        getQrHashKey(this, hm);
    }

    @OnClick(R.id.backbutton)
    void onBackButtonPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.sharebtn)
    public void TellaFriend() {
//        Drawable loadImage = getResources().getDrawable(R.drawable.sample);
//           String str = Helper.saveToInternalStorage(this,((BitmapDrawable) loadImage).getBitmap());
//        Log.i("path",str);
        try {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            Uri uri = Uri.parse("android.resource://de.bitshares_munich.smartcoinswallet/drawable/bts");
            //        Uri uri = Uri.parse(str);
            sharingIntent.setData(uri);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sharingIntent, "Hello Sir"));
        } catch (Exception e) {

        }
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        if (!isFinishing()) {
                            Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.failed_transaction, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
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


}
