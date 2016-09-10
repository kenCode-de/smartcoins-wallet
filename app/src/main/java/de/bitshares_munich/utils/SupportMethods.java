package de.bitshares_munich.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Syed Muhammad Muzzammil on 5/24/16.
 */
public class SupportMethods {
    public static String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static File extStorageFile = new File(extStorage);
    public static String ConvertValueintoPrecision(String precision,String number){
        Double ok = 1.0;
        Double pre = Double.valueOf(precision);
        Double value = Double.valueOf(number);
        for(int k = 0 ; k<pre ; k++ ){
            ok = ok*10;
        }
        return  Double.toString(value/ok);
    }

    public static double convertAssetAmountToDouble(String precision,String number){
        Double ok = 1.0;
        Double pre = Double.valueOf(precision);
        Double value = Double.valueOf(number);
        for(int k = 0 ; k<pre ; k++ ){
            ok = ok*10;
        }
        return  value/ok;
    }

    public static double convertAssetAmountToFiat(double amount,double exchanegrate){
        return  amount*exchanegrate;
    }

    public static double convertAssetAmountToFiat(double amount,String exchanegrate){
        double eR = Double.valueOf(exchanegrate);
        return  convertAssetAmountToFiat(amount,eR);
    }

    public static String ParseJsonObject(String Json , String Req)
    {
        try
        {
            if(Json.contains(Req))
            {
                JSONObject myJson = new JSONObject(Json);
                return  myJson.getString(Req);
            }
        }
        catch (Exception e)
        {
            testing("SupportMethods",e,"ParseJsonObject");
        }
        return "";
    }
    public static String ParseObjectFromJsonArray(String Json , int position)
    {
        try
        {
            JSONArray myArray = new JSONArray(Json);
            if(myArray.length()>=position){
                return  myArray.get(position).toString();
            }
        }
        catch (Exception e)
        {
            testing("SupportMethods",e,"ParseObjectFromJsonArray");
        }
        return "";
    }

    @Nullable
    public static HashMap<String,ArrayList<String>> ParseJsonArray(String Json , String req){
        try {
            JSONArray myArray = new JSONArray(Json);
            ArrayList<String> array = new ArrayList<>();
            HashMap<String, ArrayList<String>> pairs = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < myArray.length(); i++) {
                JSONObject j = myArray.optJSONObject(i);
                Iterator it = j.keys();
                while (it.hasNext()) {
                    String n = (String) it.next();
                    if (n.equals(req)) {
                        array.add(j.getString(n));
                        pairs.put(req, array);
                    }
                }

            }
            return pairs;
        }catch (Exception e){
            testing("SupportMethods",e,"ParseJsonArray");
        }
        return null;
    }
    public static void testing(String msg , Object obj , String nameOfObject){
        Log.i("Saiyed_Testing","=> Msg : "+ msg + " : nameOfObject : " + nameOfObject + " : " + obj);
    }
    public static void testing(String msg , Exception e , String nameOfObject){
        try {
            StackTraceElement[] stackTrace = e.getStackTrace();
            String fullClassName = stackTrace[stackTrace.length - 1].getClassName();
            String className = fullClassName.substring(fullClassName
                    .lastIndexOf(".") + 1);
            String methodName = stackTrace[stackTrace.length - 1].getMethodName();
            int lineNumber = stackTrace[stackTrace.length - 1].getLineNumber();
            Log.i("Saiyed_Testing", "=> Msg : " + msg + " : nameOfObject : " + nameOfObject + " : " + fullClassName + "--" + className + "--" + methodName + "--" + lineNumber);
        }
        catch (Exception ex)
        {

        }
    }
    public static int CheckJsonFormat(String s){
        try {
            Object json = new JSONTokener(s).nextValue();
            if (json instanceof JSONObject){
                return 0;
            }
            else if(json instanceof JSONArray){
                return 1;
            }
        }catch (Exception e){
            testing("SupportMethods",e,"CheckJsonFormat");
        }
        return -1;
    }
    public static int TotalArraysOfObj(String Json){
        try {
            JSONArray myArray = new JSONArray(Json);
            return myArray.length();
        }catch (Exception e){            testing("SupportMethods",e,"TotalArraysOfObj");
        }
        return -1;
    }
    public static void openPdf(Context context,String file){
        try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(extStorageFile, file + ".pdf"));
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
                }
        catch (Exception e){
            testing("SupportMethods",e,"openPdf");
        }
    }
    public static void sendPdfViaEmail(Context context,String file,String subject , String emailBody ){
        try {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, "");
            email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, emailBody);
            Uri uri = Uri.fromFile(new File(extStorageFile, file + ".pdf"));
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("application/pdf");
            email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(email);
        }
        catch (Exception e){
            testing("SupportMethods",e,"sendPdfViaEmail");
        }
    }
    public static void sendPngViaEmail(Context context, ImageView image){
        image.buildDrawingCache();
        Bitmap bitmap = image.getDrawingCache();
        OutputStream outStream = null;
        File mFile = new File(extStorageFile, "Image" + ".png");
        if (mFile.exists()) {
            mFile.delete();
            mFile = new File(extStorageFile, "Image" + ".png");
        }
        try {
            outStream = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            Uri uri = null;
            uri = Uri.fromFile(mFile);
            sharingIntent.setData(uri);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(sharingIntent, "Hello Sir"));
        }
        catch (Exception e){
            testing("SupportMethods",e,"sendPngViaEmail");
        }
    }
    public static Bitmap highlightImage(float radiusBlurMaskFilter , Bitmap src) {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() , src.getHeight() , Bitmap.Config.ARGB_8888);
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(radiusBlurMaskFilter, BlurMaskFilter.Blur.NORMAL));
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(Color.GRAY);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();

        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);

        // return out final image
        return bmOut;
    }
    public static boolean isEmailValid(String emailAddress) {
        boolean isValid = false;

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        CharSequence inputStr = new String(emailAddress);

        Pattern pattern = Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    public static void setLocale(Context base ,String language){
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        base.getResources().updateConfiguration(config,
                base.getResources().getDisplayMetrics());
    }
}
