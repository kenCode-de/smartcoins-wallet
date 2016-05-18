package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */
public class PaymentRecieved extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_recieved);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }
}
