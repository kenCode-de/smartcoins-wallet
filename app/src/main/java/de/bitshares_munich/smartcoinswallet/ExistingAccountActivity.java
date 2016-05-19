package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExistingAccountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_account);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tvImportBrainKey)
    public void importBrainKey(TextView textView) {
        Intent intent=new Intent(getApplicationContext(),BrainkeyActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.tvBackup)
    public void importBackup(TextView textView) {
       /* Intent intent=new Intent(getApplicationContext(),ImportBackupActivity.class);
        startActivity(intent);*/
    }
}
