package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Syed Muhammad Muzzammil on 5/26/16.
 */
public class ShareContact extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_account);
        ButterKnife.bind(this);

        setBackButton(true);
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @OnClick(R.id.sharetofriend)
    public void ShareWithFriend(){
        try {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.share_subject));
            sharingIntent.setType("text/plain");
            startActivity(Intent.createChooser(sharingIntent,getString(R.string.share_heading)));
        } catch (Exception e) {
        }
    }

}
