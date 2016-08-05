package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import de.bitshares_munich.utils.Helper;

/**
 * Created by afnan on 7/27/16.
 */
public class AudioFilePath {

    Context context;

    public AudioFilePath(Context _context){
        context = _context;
    }

    public void storeAudioFilePath(String path){
        Helper.storeStringSharePref(context, context.getString(R.string.audio_file_path), path);
    }

    public String fetchAudioFilePathFromPref(){
        return Helper.fetchStringSharePref(context, context.getString(R.string.audio_file_path));
    }

    public String fetchAudioFile(){
        String path = fetchAudioFilePathFromPref();
        File audioFile = new File(path);
        if(audioFile.exists()){
            return path;
        }
        else if(defaultAudioFilePath()) {
           return fetchAudioFilePathFromPref();
        }
        else
        {
            return "";
        }
    }

    public MediaPlayer fetchMediaPlayer(){
        String audioFilePath = fetchAudioFile();
        MediaPlayer mediaPlayer;
        if(audioFilePath.isEmpty()) mediaPlayer = MediaPlayer.create(context , R.raw.woohoo);
        else mediaPlayer = MediaPlayer.create(context , Uri.parse(audioFilePath));
        return mediaPlayer;
    }

    Boolean defaultAudioFilePath(){
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.folder_name));
        File file2 = new File(folder.getAbsolutePath(), "Woohoo.wav");
        if (file2.exists()) {
            storeAudioFilePath(file2.getAbsolutePath());
            return true;
        }
        return false;
    }
    public String userAudioFilePathIfExist(){
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.folder_name));
        File file = new File(folder.getAbsolutePath(), "Woohoo.wav");
        String userFilePath = fetchAudioFilePathFromPref();
        String defaultAudioPath = file.getAbsolutePath();
        if(userFilePath.equals(defaultAudioPath) || userFilePath.isEmpty()){
            return "-------";
        }
        else {
            return userFilePath;
        }
    }
}
