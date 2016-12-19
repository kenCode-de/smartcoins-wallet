package de.bitshares_munich.utils;

import android.content.Context;
import android.content.Intent;

import com.luminiasoft.bitshares.models.HistoricalTransfer;

import java.util.Date;

import de.bitshares_munich.Interfaces.InternalMovementListener;
import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceipt;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by developer on 5/26/16.
 */
public class TableViewClickListener implements TableDataClickListener<HistoricalTransfer> {

    private Context myContext;
    private InternalMovementListener mListener;

    public TableViewClickListener(Context _context, InternalMovementListener listener) {
        this.myContext = _context;
        this.mListener = listener;
    }

    @Override
    public void onDataClicked(int rowIndex, HistoricalTransfer td) {
        if(!BalancesFragment.onClicked) {
            BalancesFragment.onClicked = true;
            long timestamp = td.getTimestamp();

            Intent intent = new Intent(myContext, eReceipt.class);
            intent.putExtra(myContext.getResources().getString(R.string.e_receipt), td.toString());
            intent.putExtra("Memo", td.getOperation().getMemo().toString());
            intent.putExtra("Date", Helper.convertDateToGMT(new Date(timestamp), myContext));
            intent.putExtra("Time", Helper.convertDateToGMTWithYear(new Date(timestamp), myContext));
            intent.putExtra("TimeZone", Helper.convertTimeToGMT(new Date(timestamp),myContext));
            intent.putExtra("To", td.getOperation().getTo().getAccountName());
            intent.putExtra("From", td.getOperation().getFrom().getAccountName());
            //TODO: Fix this!
            intent.putExtra("Sent", true);
            mListener.onInternalAppMove();
            myContext.startActivity(intent);
        }
    }
}
