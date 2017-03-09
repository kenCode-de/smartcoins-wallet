package de.bitsharesmunich.cryptocoincore.utils;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import java.util.Date;

import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceipt;
import de.bitshares_munich.utils.Helper;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by developer on 5/26/16.
 */
public class CryptoCoinTableViewClickListener implements TableDataClickListener<GeneralTransaction> {
    public static final String TAG = "TableViewClickListener";
    public static final String KEY_MEMO = "memo";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_TIME_ZONE = "time_zone";
    public static final String KEY_TO = "to";
    public static final String KEY_FROM = "from";
    public static final String KEY_SENT = "sent";

    public static final String KEY_OPERATION_ENTRY = "operation_entry";

    private Context myContext;
    private Coin coin;

    //public CryptoCoinTableViewClickListener(Context _context, InternalMovementListener listener) {
    //    this(_context, listener, Coin.BITSHARE);
    //}

    public CryptoCoinTableViewClickListener(Context _context,  Coin coin) {
        this.myContext = _context;
        this.coin = coin;
    }

    @Override
    public void onDataClicked(int rowIndex, GeneralTransaction historicalTransferEntry) {
        if(!BalancesFragment.onClicked) {
            //TransferOperation operation = historicalTransferEntry.getHistoricalTransfer().getOperation();
            BalancesFragment.onClicked = true;
            long timestamp = historicalTransferEntry.getDate().getTime();

            Intent intent = new Intent(myContext, eReceipt.class);
            intent.putExtra(myContext.getResources().getString(R.string.e_receipt), historicalTransferEntry.toString());
            intent.putExtra("Memo", "");
            intent.putExtra("Date", Helper.convertDateToGMT(new Date(timestamp), myContext));
            intent.putExtra("Time", Helper.convertDateToGMTWithYear(new Date(timestamp), myContext));
            intent.putExtra("TimeZone", Helper.convertTimeToGMT(new Date(timestamp),myContext));
            intent.putExtra("To", "");//operation.getTo().getAccountName());
            intent.putExtra("From", "");//operation.getFrom().getAccountName());
            intent.putExtra("Sent", true);

            intent.putExtra("coin", this.coin.name());

            if (this.coin == Coin.BITSHARE) {
                Gson gson = new Gson();
                String jsonTransfer = gson.toJson(historicalTransferEntry);
                intent.putExtra(KEY_OPERATION_ENTRY, jsonTransfer);
            } else {
                intent.putExtra("TransactionId", historicalTransferEntry.getId());
            }

            myContext.startActivity(intent);
        }
    }
}
