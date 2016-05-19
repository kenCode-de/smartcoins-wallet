package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.utils.Helper;

/**
 * Created by Syed Muhammad Muzzammil on 5/16/16.
 */
public class RecieveActivity extends Activity{
    @Bind(R.id.username)
    TextView username;

    @Bind(R.id.amount)
    TextView amount;

    @Bind(R.id.notfound)
    TextView notfound;

    @Bind(R.id.qrimage)
    ImageView qrimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recieve_activity);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }

    @OnClick(R.id.sharebtn)
    public void TellaFriend() {
//        Drawable loadImage = getResources().getDrawable(R.drawable.sample);
//           String str = Helper.saveToInternalStorage(this,((BitmapDrawable) loadImage).getBitmap());
//        Log.i("path",str);
        try{
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         Uri uri = Uri.parse("android.resource://de.bitshares_munich.smartcoinswallet/drawable/bts");
    //        Uri uri = Uri.parse(str);
            sharingIntent.setData(uri);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM,uri);
        startActivity(Intent.createChooser(sharingIntent,  "Hello Sir"));}catch (Exception e){

        }
    }
}
