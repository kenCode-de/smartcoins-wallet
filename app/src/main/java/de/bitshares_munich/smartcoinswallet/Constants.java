package de.bitshares_munich.smartcoinswallet;

import de.bitsharesmunich.graphenej.Asset;

/**
 * Created by nelson on 12/12/16.
 */

public class Constants {
    public static final String KEY_UPDATE_DONE = "key_update_done";
    public static final String KEY_OLD_KEYS = "key_old_keys";
    public static final String KEY_MIGRATED_OLD_TRANSACTIONS = "key_updated_old_transactions";

    /**
     * Default bucket size in seconds.
     */
    public static final long DEFAULT_BUCKET_SIZE = 3600;

    /**
     * Static method that returns network core currency, in this case BTS.
     * @return
     */
    public static Asset getCoreCurrency(){
        return new Asset("1.3.0");
    }

    public static final String KEY_SUGGESTED_BRAIN_KEY = "key_suggested_brain_key";
}
