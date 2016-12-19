package de.bitshares_munich.database;

import com.luminiasoft.bitshares.AssetAmount;
import com.luminiasoft.bitshares.models.HistoricalTransfer;

/**
 * This class is very similar to the HistoricalTransfer, but while the later is used to deserialize
 * the transfer information exactly as it comes from the 'get_relative_account_history' API call,
 * this class is used to represent a single entry in the local database.
 *
 * Every entry in the transfers table needs a bit more information than what is provided by the
 * HistoricalTransfer. We need to know the specific timestamp of a transaction for instance, instead
 * of just a block number.
 *
 * There's also the data used for the equivalent fiat value.
 *
 * Created by nelson on 12/18/16.
 */

public class HistoricalTransferEntry {
    private HistoricalTransfer historicalTransfer;
    private long timestamp;
    private AssetAmount equivalentValue;
}
