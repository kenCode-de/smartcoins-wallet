package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */
public class PaymentRecieved extends Activity {
    @Bind(R.id.btnOk)
    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_recieved);
        ButterKnife.bind(this);
        String block = getIntent().getStringExtra("block");



    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }
    @OnClick(R.id.btnOk)
    void onOkPressed(){
        Intent intent = new Intent(getApplicationContext(), TabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
