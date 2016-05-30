package de.bitshares_munich.utils;

import android.content.Intent;
import android.widget.Toast;

import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.eReceiptActivity;
import de.codecrafters.tableview.listeners.TableDataClickListener;

import android.content.Context;

/**
 * Created by developer on 5/26/16.
 */
public class tableViewClickListener implements TableDataClickListener<TransactionDetails> {

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
    public void onDataClicked(int rowIndex, TransactionDetails td) {
        //String clickedtdString = td.getDetailsFrom();
        //Toast.makeText(myContext, clickedtdString, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Application.getCurrentActivity(), eReceiptActivity.class);
        intent.putExtra( myContext.getResources().getString(R.string.e_receipt),td.eReceipt);
        intent.putExtra( "Memo",td.Memo);
        intent.putExtra( "Date",td.getDateString()+" "+td.getTimeString()+" "+td.getTimeZone());
        intent.putExtra( "To",td.To);
        intent.putExtra( "From",td.From);
        intent.putExtra( "Sent",td.Sent);

        Application.getCurrentActivity().startActivity(intent);
    }
}
