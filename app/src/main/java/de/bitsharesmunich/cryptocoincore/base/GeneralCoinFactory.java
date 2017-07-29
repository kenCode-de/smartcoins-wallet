package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;

import java.util.HashMap;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.adapters.GeneralCoinSettingsDialogBuilder;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinAccount;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinSettings;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinValidator;
import de.bitsharesmunich.cryptocoincore.dash.DashAccount;
import de.bitsharesmunich.cryptocoincore.dash.DashSettings;
import de.bitsharesmunich.cryptocoincore.dash.DashSettingsDialogBuilder;
import de.bitsharesmunich.cryptocoincore.dash.DashValidator;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinAccount;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinValidator;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinAccount;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinValidator;

/**
 * Created by Henry Varona on 26/2/2017.
 */

/**
 * Returns the related objects of every general coin
 */
public class GeneralCoinFactory {

    private static HashMap<Coin,GeneralCoinSettings> mSettingsCache = new HashMap<Coin,GeneralCoinSettings>(); /**< cache for the coin settings*/

    /**
     * returns the validator of a specific coin
     *
     * @param coin the coin of the validator to return
     * @return the validator of the given coin
     */
    public static GeneralCoinValidator getValidator(Coin coin){
        switch(coin){
            case BITCOIN:
                return BitcoinValidator.getInstance();
            case DASH:
                return DashValidator.getInstance();
            case LITECOIN:
                return LiteCoinValidator.getInstance();
            case DOGECOIN:
                return DogeCoinValidator.getInstance();
        }

        return null;
    }

    /**
     * returns a dialog builder for a determined coin.
     * The dialog builders can create forms for many purposes.
     *
     * @param context the context to create the views
     * @param coin the coin type of the dialog builder
     * @return a dialog builder for the given coin
     */
    public static GeneralCoinSettingsDialogBuilder getDialogBuilder(Context context, Coin coin){
        switch (coin) {
            case DASH:
                return new DashSettingsDialogBuilder(context);
            default:
                return new GeneralCoinSettingsDialogBuilder(context, coin);
        }
    }

    /**
     * returns a setting object for a specific coin
     *
     * @param context the context of the application
     * @param coin the coin type of the settings
     * @return a setting object for the given coin type
     */
    public static GeneralCoinSettings getSettings(Context context, Coin coin){
        GeneralCoinSettings settings;

        if (mSettingsCache.containsKey(coin)){
            settings = mSettingsCache.get(coin);
        } else {
            switch (coin) {
                case BITCOIN:
                    settings = new BitcoinSettings();
                    break;
                case DASH:
                    settings = new DashSettings();
                    break;
                case LITECOIN:
                    //return LiteCoinSettings.getInstance();
                case DOGECOIN:
                    //return DogeCoinSettings.getInstance();
                default:
                    settings = new GeneralCoinSettings(coin);
            }

            if (settings != null){
                SCWallDatabase db = new SCWallDatabase(context);
                db.getGeneralCoinSettings(settings);
                mSettingsCache.put(coin, settings);
            }
        }

        return settings;
    }

    /**
     * Returns a GeneralCoinAccount object for a specific coin
     *
     * @param coin the coin of the GeneralCoinAccount
     * @param seed the master seed of the account
     * @param name the name of the account
     * @return a GeneralCoinAccount for the given coin
     */
    public static GeneralCoinAccount getGeneralCoinAccount(Coin coin, final AccountSeed seed, String name){
        switch (coin){
            case BITCOIN:
                return new BitcoinAccount(seed, name);
            case DASH:
                return new DashAccount(seed, name);
            case LITECOIN:
                return new LiteCoinAccount(seed,name);
            case DOGECOIN:
                return new DogeCoinAccount(seed,name);
        }

        return null;
    }
}
