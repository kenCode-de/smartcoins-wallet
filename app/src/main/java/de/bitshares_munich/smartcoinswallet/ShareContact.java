package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

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
        setTitle(getResources().getString(R.string.share_account_activity_name));
    }

    @OnClick(R.id.sharetofriend)
    public void ShareWithFriend() {
        try {
            String emailContent = String.format("I just found this awesome Smartcoins Wallet that allows you to send and receive Smartcoins. <br></br><br></br> <a href= \"http://BitShares-Munich.de\">http://BitShares-Munich.de</a> <br></br><br></br> Check it out!");
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(emailContent));
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.setType("text/html");
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_heading)));
        } catch (Exception e) {
        }
    }
}
