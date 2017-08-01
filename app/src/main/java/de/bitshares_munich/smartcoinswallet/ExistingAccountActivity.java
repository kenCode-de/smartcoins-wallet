package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExistingAccountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_account);
        ButterKnife.bind(this);
        setBackButton(true);
        setTitle(getResources().getString(R.string.app_name));
    }

    @OnClick(R.id.tvImportBrainKey)
    public void importBrainKey(Button button) {
        Intent intent=new Intent(getApplicationContext(),BrainkeyActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.tvBackup)
    public void importBackup(Button button) {
        Intent intent=new Intent(getApplicationContext(),ImportBackupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnWifBackup)
    public void importWif(Button button) {
        Intent intent=new Intent(getApplicationContext(),ImportWifActivity.class);
        startActivity(intent);
    }
}
