package de.bitshares_munich.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    public static String ParseJsonObject(String Json , String Req){
        try {
            if(Json.contains(Req)){
                JSONObject myJson = new JSONObject(Json);
                return  myJson.getString(Req);}
        }catch (Exception e){ testing("SupportMethods",e,"ParseJsonObject");}
        return "";
    }
    public static String ParseObjectFromJsonArray(String Json , int position){
        try {
            JSONArray myArray = new JSONArray(Json);
            if(myArray.length()>=position){
                return  myArray.get(position).toString();
            }
        }catch (Exception e){            testing("SupportMethods",e,"ParseObjectFromJsonArray");
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
        StackTraceElement[] stackTrace = e.getStackTrace();
        String fullClassName = stackTrace[stackTrace.length - 1].getClassName();
        String className = fullClassName.substring(fullClassName
                .lastIndexOf(".") + 1);
        String methodName = stackTrace[stackTrace.length - 1].getMethodName();
        int lineNumber = stackTrace[stackTrace.length - 1].getLineNumber();
        Log.i("Saiyed_Testing","=> Msg : "+ msg + " : nameOfObject : " + nameOfObject + " : " + fullClassName + "--" + className + "--" + methodName + "--" + lineNumber);
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
}