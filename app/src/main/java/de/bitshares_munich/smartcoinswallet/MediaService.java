package de.bitshares_munich.smartcoinswallet;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import de.bitshares_munich.fragments.BalancesFragment;

/**
 * Created by qasim on 6/30/16.
 */
public class MediaService extends Service {
    MediaPlayer mp;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        AudioFilePath audioFilePath = new AudioFilePath(getApplicationContext());
        mp = audioFilePath.fetchMediaPlayer();
        mp.setLooping(false);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                BalancesFragment.iSound.soundFinish();
            }
        });
    }

    public void onDestroy() {

    }

    public void onStart(Intent intent, int startid) {
        mp.start();
    }
}
