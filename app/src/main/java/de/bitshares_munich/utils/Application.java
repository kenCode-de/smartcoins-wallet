package de.bitshares_munich.utils;

import android.os.Bundle;

import butterknife.ButterKnife;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application{

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
    }
}
