package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.InternalMovementListener;

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
    public void importBrainKey(TextView textView) {
        Intent intent=new Intent(getApplicationContext(),BrainkeyActivity.class);
        ((InternalMovementListener) this).onInternalAppMove();
        startActivity(intent);
    }

    @OnClick(R.id.tvBackup)
    public void importBackup(TextView textView) {
        Intent intent=new Intent(getApplicationContext(),ImportBackupActivity.class);
        ((InternalMovementListener) this).onInternalAppMove();
        startActivity(intent);
    }
}
