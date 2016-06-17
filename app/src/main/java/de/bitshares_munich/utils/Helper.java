package de.bitshares_munich.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 4/7/16.
 */
public class Helper {
    Gson gson;
    public static final ArrayList<String> languages = new ArrayList<>();
    public static Map<String, String> countriesISOMap = new HashMap<String, String>();

    public static void setLanguages() {
        languages.clear();
        languages.add("sq");
        languages.add("ar");
        languages.add("hy");
        languages.add("bn");
        languages.add("bs");
        languages.add("bg");
        languages.add("ca");
        languages.add("zh");
        languages.add("zh-rTW");
        languages.add("hr");
        languages.add("cs");
        languages.add("da");
        languages.add("nl");
        languages.add("en");
        languages.add("et");
        languages.add("fa");
        languages.add("fi");
        languages.add("fr");
        languages.add("de");
        languages.add("el");
        languages.add("he");
        languages.add("hi");
        languages.add("hu");
        languages.add("id");
        languages.add("it");
        languages.add("ja");
        languages.add("ko");
        languages.add("lv");
        languages.add("lt");
        languages.add("ms");
        languages.add("no");
        languages.add("pl");
        languages.add("pt");
        languages.add("ro");
        languages.add("ru");
        languages.add("sr");
        languages.add("sk");
        languages.add("sl");
        languages.add("es");
        languages.add("sv");
        languages.add("th");
        languages.add("tr");
        languages.add("uk");
        languages.add("vi");
    }

    public static ArrayList<String> getLanguages() {
        setLanguages();
        return languages;
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void storeStringSharePref(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int fetchIntSharePref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }


    public static void storeIntSharePref(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static String fetchStringSharePref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    public static void storeObjectSharePref(Context context, String key, Object object) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        prefsEditor.putString(key, json);
        prefsEditor.commit();

    }

    public static String fetchObjectSharePref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        return preferences.getString(key, "");
    }

    public static void storeBoolianSharePref(Context context, String key, Boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean fetchBoolianSharePref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        return preferences.getBoolean(key, false);
    }

    public static Boolean containKeySharePref(Context context, String key) {
        Boolean isContainer = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(key)) {
            isContainer = true;
        } else {
            isContainer = false;
        }
        return isContainer;

    }

    public static String saveToInternalStorage(Context context, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "gravatar.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    public static Bitmap loadImageFromStorage(Context context) {
        Bitmap bitmap = null;
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory, "gravatar.jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;

    }


    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());


    }

    public static ArrayList<String> getCountriesArray1() {
        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            Currency currency = Currency.getInstance(locale);
            if (country.trim().length() > 0 && !countries.contains(country) && !country.trim().equals("World")) {
                countries.add(country + " (" + currency + ")");
            }
        }
        Collections.sort(countries);
        setCountriesISOMap();
        return countries;
    }


    public static ArrayList<String> getCountriesArray() {
        String[] locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<String>();
        for (String countryCode : locales) {

            Locale locale = new Locale("", countryCode);
            try {
                Currency currency = Currency.getInstance(locale);
                countries.add(locale.getDisplayCountry() + " (" + currency.getCurrencyCode() + ")");
            } catch (Exception e) {

            }
            Collections.sort(countries);

        }
        return countries;
    }


    public static void setCountriesISOMap() {
        String[] isoCountryCodes = Locale.getISOCountries();
        for (int i = 0; i < isoCountryCodes.length; i++) {
            Locale locale = new Locale("", isoCountryCodes[i]);
            countriesISOMap.put(locale.getDisplayCountry(), isoCountryCodes[i]);
        }
    }

    public static void setLocale(String lang, Resources res) {
        Locale myLocale = new Locale(lang);
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public static String setLocaleNumberFormat(Locale locale, Number number) {

        NumberFormat formatter = NumberFormat.getInstance(locale);
        formatter.setMaximumFractionDigits(6);
        String localeFormattedNumber = formatter.format(number);
        return localeFormattedNumber;

    }

    public static char setDecimalSeparator(Locale locale) {

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        return decimalFormatSymbols.getDecimalSeparator();

    }

    public static String convertDateToGMT(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {

            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);

            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM");
            destFormat.setTimeZone(tz);
            String result = destFormat.format(date);
            return result;

        } else {
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM");
            String result = destFormat.format(date);
            return result;
        }
    }


    public static String convertTimeToGMT(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {

            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);

            SimpleDateFormat destFormat = new SimpleDateFormat("hh:mm a");
            destFormat.setTimeZone(tz);
            String result = destFormat.format(date);
            return result;

        } else {
            SimpleDateFormat destFormat = new SimpleDateFormat("hh:mm a");
            String result = destFormat.format(date);
            return result;
        }
    }

    public static String convertTimeZoneToGMT(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {
            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(tz);
            return calendar.getTimeZone().getDisplayName(false, TimeZone.SHORT);

        } else {
            return "UTC";
        }
    }

    public static String getFadeCurrency(Context context) {
        Boolean isFade = Helper.containKeySharePref(context, context.getString(R.string.pref_fade_currency));
        if (isFade) {
            String currency[]=Helper.fetchStringSharePref(context,context.getString(R.string.pref_fade_currency)).split(" ");
            return currency[1].replace("(","").replace(")","");
        } else {
            return "";
        }
    }

    public static String padString(String str)
    {
        if (str == null || str.isEmpty())
        {
            return "0";
        }
        else if (str.equals("."))
        {
            return "0.";
        }
        else
        {
            try
            {
                return String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(str));
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }

}
