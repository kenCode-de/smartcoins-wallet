package de.bitshares_munich.smartcoinswallet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onStart(){
        super.onStart();
        showDialogLiscence();
    }

    private void showDialogLiscence() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_liscence);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.agreement);
        dialog.show();
    }
}
