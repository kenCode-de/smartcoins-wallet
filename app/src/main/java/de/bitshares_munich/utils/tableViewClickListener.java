package de.bitshares_munich.utils;

import android.content.Intent;
import android.widget.Toast;

import de.bitshares_munich.fragments.BalancesFragment;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceipt;
import de.codecrafters.tableview.listeners.TableDataClickListener;

import android.content.Context;

import com.luminiasoft.bitshares.models.HistoricalTransfer;

/**
 * Created by developer on 5/26/16.
 */
public class tableViewClickListener implements TableDataClickListener<HistoricalTransfer> {

    private Context myContext;

    public tableViewClickListener(Context _context)
    {
        this.myContext = _context;
    }

    public void setContext(Context _context)
    {
        this.myContext = _context;
    }

    @Override
    public void onDataClicked(int rowIndex, HistoricalTransfer td) {
        if(!BalancesFragment.onClicked) {
            BalancesFragment.onClicked = true;
            Intent intent = new Intent(myContext, eReceipt.class);
            intent.putExtra(myContext.getResources().getString(R.string.e_receipt), td.toString());
            intent.putExtra("Memo", td.getOperation().getMemo().toString());
            intent.putExtra("Date", td.getDateStringWithYear(this.myContext));
            intent.putExtra("Time", td.getTimeString(this.myContext));
            intent.putExtra("TimeZone", td.getTimeZone(this.myContext));
            intent.putExtra("To", td.getOperation().getTo().getAccountName());
            intent.putExtra("From", td.getOperation().getFrom().getAccountName());
            intent.putExtra("Sent", td.getSent());
            myContext.startActivity(intent);
        }
    }
}
