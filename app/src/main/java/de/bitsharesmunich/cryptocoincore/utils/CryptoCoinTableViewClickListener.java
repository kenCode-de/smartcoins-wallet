package de.bitsharesmunich.cryptocoincore.utils;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import java.util.Date;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceipt;
import de.bitshares_munich.utils.Helper;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by developer on 5/26/16.
 */
public class CryptoCoinTableViewClickListener implements TableDataClickListener<TransactionLog> {
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

    public CryptoCoinTableViewClickListener(Context _context) {
        this.myContext = _context;
        //this.coin = coin;
    }

    @Override
    public void onDataClicked(int rowIndex, TransactionLog historicalTransfer) {
        if(!BalancesFragment.onClicked) {
            long timestamp = 0;
            String ereceiptString = "";
            String toString = "";
            String fromString = "";
            String jsonTransfer = "";
            String memoString = "";
            long transactionId = -1;

            switch (historicalTransfer.getType()){
                case TRANSACTION_TYPE_BITSHARE:
                    HistoricalTransferEntry historicalTransferEntry = historicalTransfer.getBitshareTransactionLog();
                    TransferOperation operation = historicalTransferEntry.getHistoricalTransfer().getOperation();
                    timestamp = historicalTransferEntry.getTimestamp();
                    ereceiptString = historicalTransferEntry.toString();
                    toString = operation.getTo().getAccountName();
                    fromString = operation.getFrom().getAccountName();
                    Gson gson = new Gson();
                    jsonTransfer = gson.toJson(historicalTransferEntry);
                    memoString = operation.getMemo().getPlaintextMessage();

                    break;
                case TRANSACTION_TYPE_BITCOIN:
                    GeneralTransaction generalTransaction = historicalTransfer.getBitcoinTransactionLog();
                    timestamp = generalTransaction.getDate().getTime();
                    ereceiptString = generalTransaction.toString();
                    transactionId = generalTransaction.getId();
                    break;
            }


            BalancesFragment.onClicked = true;

            Intent intent = new Intent(myContext, eReceipt.class);
            intent.putExtra(myContext.getResources().getString(R.string.e_receipt), ereceiptString);
            intent.putExtra("Memo", memoString);
            intent.putExtra("Date", Helper.convertDateToGMT(new Date(timestamp), myContext));
            intent.putExtra("Time", Helper.convertDateToGMTWithYear(new Date(timestamp), myContext));
            intent.putExtra("TimeZone", Helper.convertTimeToGMT(new Date(timestamp),myContext));
            intent.putExtra("To", toString);
            intent.putExtra("From", fromString);
            intent.putExtra("Sent", true);

            intent.putExtra("coin", this.coin.name());

            if (this.coin == Coin.BITSHARE) {
                intent.putExtra(KEY_OPERATION_ENTRY, jsonTransfer);
            } else {
                intent.putExtra("TransactionId", transactionId);
            }

            myContext.startActivity(intent);
        }
    }
}
