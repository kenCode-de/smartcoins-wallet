package de.bitshares_munich.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.AddressFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
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
import java.util.Properties;
import java.util.TimeZone;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 4/7/16.
 */
public class Helper {
    Gson gson;
    private final static String TAG = "Helper";
    public static final String MD5 = "MD5";
    public static final String SHA256 = "SHA-256";
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
        languages.add("zh-rCN");
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
        languages.add("pt-rPT");
        languages.add("pt-rBR");
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

    public static final String hash(final String s, final String algorithm) {
        try {
            // Create MD5 or SHA-256 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(algorithm);
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

    /*
     * Checks whether the preferences contains a preference.
     */
    public static Boolean checkSharedPref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(key);
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

    public static String fetchStringSharePref(Context context, String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    public static String fetchStringSharePref(Context context, String key) {
        return fetchStringSharePref(context, key, "");
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

    public static void storeLongSharePref(Context context, String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long fetchLongSharePref(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        return preferences.getLong(key, -1);
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
        }
        Collections.sort(countries);
        return countries;
    }

    public static String getCountryCode(String spinnerText) {
        String[] locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<>();
        for (String countryCode : locales) {

            Locale locale = new Locale("", countryCode);
            try {
                Currency currency = Currency.getInstance(locale);
                String proposedSpinnerText = locale.getDisplayCountry() + " (" + currency.getCurrencyCode() + ")";

                if (proposedSpinnerText.equals(spinnerText)) {
                    return countryCode;
                }
            } catch (Exception e) {

            }
        }
        return "";
    }

    public static String getSpinnertextCountry(String countryCode) {

        Locale locale = new Locale("", countryCode);
        try {
            Currency currency = Currency.getInstance(locale);
            return locale.getDisplayCountry() + " (" + currency.getCurrencyCode() + ")";
        } catch (Exception e) {

        }
        return "";
    }


    public static void setCountriesISOMap() {
        String[] isoCountryCodes = Locale.getISOCountries();
        for (int i = 0; i < isoCountryCodes.length; i++) {
            Locale locale = new Locale("", isoCountryCodes[i]);
            countriesISOMap.put(locale.getDisplayCountry(), isoCountryCodes[i]);
        }
    }

    /*
     * Setup app locale based on the language
     */
    public static void setLocale(String lang, Resources res) {
        Locale myLocale;
        if (lang.equalsIgnoreCase("zh-rTW")) {
            myLocale = Locale.TRADITIONAL_CHINESE;
        } else if (lang.equalsIgnoreCase("zh-rCN") || lang.equalsIgnoreCase("zh")) {
            myLocale = Locale.SIMPLIFIED_CHINESE;
        } else if (lang.equalsIgnoreCase("pt-rBR") || lang.equalsIgnoreCase("pt")) {
            myLocale = new Locale("pt", "BR");
        } else if (lang.equalsIgnoreCase("pt-rPT")) {
            myLocale = new Locale("pt", "PT");
        } else {
            myLocale = new Locale(lang);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    /*
     * Setup app Language LOCALE and app Language PREFERENCES.
     */
    public static void setLanguage(Context context, String language) {
        //Setup locale
        Helper.setLocale(language, context.getResources());
        //Setup Preferences
        Helper.storeStringSharePref(context, context.getString(R.string.pref_language), language);
    }

    /*
     * Setup app Country LOCALE and app Contry PREFERENCES.
     */
    public static void setCountry(Context context, String country) {
        //Setup locale
        Locale myLocale;
        myLocale = new Locale.Builder().setRegion(country).build();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        Configuration conf = context.getResources().getConfiguration();
        conf.locale = myLocale;
        context.getResources().updateConfiguration(conf, dm);
        //Setup Preferences
        Helper.storeStringSharePref(context, context.getString(R.string.pref_country), country);
    }


    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getDeviceCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }


    public static String setLocaleNumberFormat(Locale locale, Number number) {

        NumberFormat formatter = NumberFormat.getInstance(locale);
        formatter.setMaximumFractionDigits(4);
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

    public static String convertDateToGMTWithYear(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {

            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);

            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yy");
            destFormat.setTimeZone(tz);
            String result = destFormat.format(date);
            return result;

        } else {
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yyy");
            String result = destFormat.format(date);
            return result;
        }
    }


    public static String convertTimeToGMT(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {

            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);

            SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm");
            destFormat.setTimeZone(tz);
            String result = destFormat.format(date);
            return result;

        } else {
            SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm");
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

    public static String convertTimeZoneToRegion(Date date, Context context) {

        if (Helper.containKeySharePref(context, context.getString(R.string.date_time_zone))) {
            String dtz = Helper.fetchStringSharePref(context, context.getString(R.string.date_time_zone));
            TimeZone tz = TimeZone.getTimeZone(dtz);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(tz);
            String region = calendar.getTimeZone().getID();
            String[] arr = region.split("/");
            for (String ss : arr) {
                if (ss.equals("Europe")) {
                    region = "CET";
                }
            }
            return region;

        } else {
            return "UTC";
        }
    }

    public static String getFadeCurrency(Context context) {
        Boolean isFade = Helper.containKeySharePref(context, context.getString(R.string.pref_fade_currency));
        if (isFade) {
            String currency[] = Helper.fetchStringSharePref(context, context.getString(R.string.pref_fade_currency)).split(" ");
            return currency[currency.length - 1].replace("(", "").replace(")", "");
        } else {
            return "EUR";
        }
    }

    public static String getFadeCurrencySymbol(Context context) {
        String currrencyCode = getFadeCurrency(context);
        return Currency.getInstance(currrencyCode).getSymbol(Locale.ENGLISH);
    }

    public static String padString(String str) {
        if (str == null || str.isEmpty()) {
            return "0";
        } else if (str.equals(".")) {
            return "0.";
        } else {
            try {
                return String.format(Locale.ENGLISH, "%.4f", Double.parseDouble(str));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static int convertDOubleToInt(Double value) {
        String valueString = Double.toString(value);

        for (int i = 0; i < valueString.length(); i++) {
            if (valueString.charAt(i) == '.') {
                valueString = valueString.substring(0, i);
                break;
            }
        }

        int valueInteger = Integer.parseInt(valueString);

        return valueInteger;
    }

    public static boolean isRTL(Locale locale, String symbol) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);


        // We then tell our formatter to use this symbol.
        DecimalFormatSymbols decimalFormatSymbols = ((java.text.DecimalFormat) currencyFormat).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol(symbol);
        ((java.text.DecimalFormat) currencyFormat).setDecimalFormatSymbols(decimalFormatSymbols);

        String formattedtext = currencyFormat.format(100.0);

        if (formattedtext.startsWith(symbol)) {
            return false;
        } else {
            return true;
        }

    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
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

    public static NumberFormat newCurrencyFormat(Context context, Currency currency, Locale displayLocale) {
        Log.d(TAG, "newCurrencyFormat");
        NumberFormat retVal = NumberFormat.getCurrencyInstance(displayLocale);
        retVal.setCurrency(currency);

        //The default JDK handles situations well when the currency is the default currency for the locale
//        if (currency.equals(Currency.getInstance(displayLocale))) {
//            Log.d(TAG, "Let the JDK handle this");
//            return retVal;
//        }

        //otherwise we need to "fix things up" when displaying a non-native currency
        if (retVal instanceof DecimalFormat) {
            DecimalFormat decimalFormat = (DecimalFormat) retVal;
            String correctedI18NSymbol = getCorrectedInternationalCurrencySymbol(context, currency, displayLocale);
            if (correctedI18NSymbol != null) {
                DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols(); //this returns a clone of DFS
                dfs.setInternationalCurrencySymbol(correctedI18NSymbol);
                dfs.setCurrencySymbol(correctedI18NSymbol);
                decimalFormat.setDecimalFormatSymbols(dfs);
            }
        }

        return retVal;
    }

    private static String getCorrectedInternationalCurrencySymbol(Context context, Currency currency, Locale displayLocale) {
        AssetsPropertyReader assetsReader = new AssetsPropertyReader(context);
        Properties properties = assetsReader.getProperties("correctedI18nCurrencySymbols.properties");
        if (properties.containsKey(currency.getCurrencyCode())) {
            return properties.getProperty(currency.getCurrencyCode());
        } else {
            return currency.getCurrencyCode();
        }
    }

    /*
     * WIF checksum checking.
     * Ref.: https://en.bitcoin.it/wiki/Wallet_import_format
     *
     * @param wifText Wallet Import Format string
     * @return true if valid wif format or false
     */
    public static Boolean wifChecksumChecking(String wifText) {

        Log.d(TAG, "Checksum Checking " + wifText );

        byte[] testbytes;

        // Fail of Input is have invalid format or it is too short.
        try {
            testbytes = Base58.decode(wifText);
        } catch (AddressFormatException e) {
            Log.d(TAG, "WIF Format Invalid: " + e );
            return false;
        }
        // Test decode of empty String.
        if(testbytes.length == 0) return false;

        // Checksum should fail.
        try {
            Base58.decodeChecked(wifText);
        } catch (AddressFormatException e) {
            Log.d(TAG, "WIF Checksum failed: " + e );
            return false;
        }

        Log.d(TAG, "Checksum Checking OK");
        return true;
    }
}
