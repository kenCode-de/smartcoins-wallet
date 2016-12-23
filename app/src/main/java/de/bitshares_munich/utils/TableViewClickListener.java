package de.bitshares_munich.utils;

import android.content.Context;
import android.content.Intent;

import com.luminiasoft.bitshares.TransferOperation;

import java.util.Date;

import de.bitshares_munich.Interfaces.InternalMovementListener;
import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceipt;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by developer on 5/26/16.
 */
public class TableViewClickListener implements TableDataClickListener<HistoricalTransferEntry> {

    private Context myContext;
    private InternalMovementListener mListener;

    public TableViewClickListener(Context _context, InternalMovementListener listener) {
        this.myContext = _context;
        this.mListener = listener;
    }

    @Override
    public void onDataClicked(int rowIndex, HistoricalTransferEntry td) {
        if(!BalancesFragment.onClicked) {
            TransferOperation operation = td.getHistoricalTransfer().getOperation();
            BalancesFragment.onClicked = true;
            long timestamp = td.getTimestamp();

            Intent intent = new Intent(myContext, eReceipt.class);
            intent.putExtra(myContext.getResources().getString(R.string.e_receipt), td.toString());
            intent.putExtra("Memo", operation.getMemo().getPlaintextMessage());
            intent.putExtra("Date", Helper.convertDateToGMT(new Date(timestamp), myContext));
            intent.putExtra("Time", Helper.convertDateToGMTWithYear(new Date(timestamp), myContext));
            intent.putExtra("TimeZone", Helper.convertTimeToGMT(new Date(timestamp),myContext));
            intent.putExtra("To", operation.getTo().getAccountName());
            intent.putExtra("From", operation.getFrom().getAccountName());
            //TODO: Fix this!
            intent.putExtra("Sent", true);
            mListener.onInternalAppMove();
//            myContext.startActivity(intent);
        }
    }
}
