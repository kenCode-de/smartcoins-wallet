package de.bitshares_munich.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.bitshares_munich.interfaces.PdfGeneratorListener;
import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.smartcoinswallet.PdfTable;
import de.bitsharesmunich.graphenej.UserAccount;

/**
 * AsyncTask subclass used to move the PDF generation procedure to a background thread
 * and inform the UI of the progress.
 *
 * Created by nelson on 12/28/16.
 */
public class PdfGeneratorTask extends AsyncTask<HistoricalTransferEntry, Float, String> {
    private Context mContext;
    private UserAccount userAccount;
    private PdfGeneratorListener mListener;

    public PdfGeneratorTask(Context context, UserAccount user, PdfGeneratorListener listener){
        this.mContext = context;
        this.userAccount = user;
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(HistoricalTransferEntry... historicalTransferEntries) {
        List<HistoricalTransferEntry> historicalTransfers = new ArrayList<>(Arrays.asList(historicalTransferEntries));
        PdfTable myTable = new PdfTable(mContext, "Transactions-scwall", mListener);
        return myTable.createTable(mContext, historicalTransfers, userAccount);
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        mListener.onUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(String message) {
        mListener.onReady(message);
    }
}
