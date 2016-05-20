package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    TextView username;

    @Bind(R.id.notfound)
    TextView notfound;

    @Bind(R.id.qrimage)
    ImageView qrimage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recieve_activity);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        showDialog("", "Loading...");
        Intent intent = getIntent();
        String price = "";
        String currency = "";
        if (intent.hasExtra(getString(R.string.price))) {
            price = intent.getStringExtra(getString(R.string.price));
        }
        if (intent.hasExtra(getString(R.string.currency))) {
            currency = intent.getStringExtra(getString(R.string.currency));
        }

        if (price.isEmpty()) {
            notfound.setText(getString(R.string.no_amount_requested));
        } else {
            notfound.setText(price + " " + currency + " requested");

        }

        String qrJson = creatingQrJson("yasir-mobile", "yasir-mobile", currency, price);
        getQrHashKey(this, qrJson);
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

    private String creatingQrJson(String to, String toLabel, String currency, String price) {
        String orderId = UUID.randomUUID().toString();
        String accountId = "";
        String callback = getString(R.string.qr_callback_url) + accountId + "/" + orderId;

        String json = "{" +
                "\"to\":\"" + to + "\"," +
                "\"to_label\":\"" + toLabel + "\"," +
                "\"currency\":\"" + currency + "\"," +
                "\"memo\": \"\"," +
                "\"line_items\":[" +
                "{" +
                "\"label\":\"\"," +
                "\"quantity\":1," +
                "\"price\":\"" + price + "\"" +
                "}" +
                "]," +
                "\"note\":\"\"," +
                "\"ruia\":\"\"," +
                "\"callback\":\"" + callback + "\"" +
                "}";

        Log.v("qqr", json.replace("\\", ""));
        return json.replace("\\", "");
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

    public void getQrHashKey(final Activity activity, String json) {

        ServiceGenerator sg = new ServiceGenerator(getString(R.string.qr_hash_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("json", json);

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
        startActivity(intent);
    }

}
