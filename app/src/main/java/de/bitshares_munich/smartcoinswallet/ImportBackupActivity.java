package de.bitshares_munich.smartcoinswallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ImportBackupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_backup);
        setTitle(getResources().getString(R.string.app_name));

    }
}
