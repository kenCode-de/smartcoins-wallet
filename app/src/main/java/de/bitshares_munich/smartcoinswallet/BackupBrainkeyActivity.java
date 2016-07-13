package de.bitshares_munich.smartcoinswallet;

import android.os.Bundle;

import butterknife.ButterKnife;

/**
 * Created by qasim on 7/13/16.
 */
public class BackupBrainkeyActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_brainkey);
        ButterKnife.bind(this);
    }

}
