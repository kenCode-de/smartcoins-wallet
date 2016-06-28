package de.bitshares_munich.utils;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by developer on 6/28/16.
 */
public class BinHelper {

    private int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public String getBytesFromBinFile(String filePath)
    {
        try
        {
            File file = new File(filePath);
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));

            String result = "";

            for ( int i = 0 ; i < fileData.length ; i++ )
            {
                int val = unsignedToBytes(dis.readByte());

                result += Integer.toString(val) + ",";
            }

            result = result.substring(0,(result.length() - 1));

            dis.close();
            return result;
        }
        catch (Exception e)
        {
            return "";
        }
    }
}
