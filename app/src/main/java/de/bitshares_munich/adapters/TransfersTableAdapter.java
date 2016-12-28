package de.bitshares_munich.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Helper;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Util;
import de.codecrafters.tableview.TableDataAdapter;


/**
 * Created by nelson on 12/13/16.
 */
public class TransfersTableAdapter extends TableDataAdapter<HistoricalTransferEntry> {
    private String TAG = this.getClass().getName();

    private UserAccount userAccount;

    public TransfersTableAdapter(Context context, UserAccount userAccount, HistoricalTransferEntry[] data) {
        super(context, data);
        this.userAccount = userAccount;
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        View renderedView = null;
        HistoricalTransferEntry transferEntry = getRowData(rowIndex);
        switch (columnIndex) {
            case 0:
                renderedView = renderDateView(transferEntry);
                break;
            case 1:
                renderedView = renderSendRecieve(transferEntry);
                break;
            case 2:
                renderedView = renderDetails(transferEntry);
                break;
            case 3:
                renderedView = renderAmount(transferEntry);
                break;
        }
        return renderedView;
    }

    private View renderDateView(HistoricalTransferEntry historicalTransfer) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionsdateview, null);
        TextView dateTextView = (TextView) v.findViewById(R.id.transactiondate);
        TextView timeTextView = (TextView) v.findViewById(R.id.transactiontime);
        TextView timeZoneTextView = (TextView) v.findViewById(R.id.transactionttimezone);

        if(historicalTransfer.getTimestamp() > 0){

            Date date = new Date(historicalTransfer.getTimestamp() * 1000);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
            SimpleDateFormat timeZone = new SimpleDateFormat("z");

            dateTextView.setText(dateFormat.format(date));
            timeTextView.setText(timeFormat.format(date));
            timeZoneTextView.setText(timeZone.format(date));
        }
        return v;
    }

    private View renderSendRecieve(HistoricalTransferEntry historicalTransfer) {
        TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionssendrecieve, null);
        ImageView imgView = (ImageView) v.findViewById(R.id.iv);
        if (operation.getFrom().getObjectId().equals(userAccount.getObjectId()) ) {
            imgView.setImageResource(R.drawable.send);
        } else {
            imgView.setImageResource(R.drawable.receive);
        }
        return v;
    }

    private View renderDetails(HistoricalTransferEntry historicalTransfer) {
        TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactiondetailsview, null);

        String toMessage = getContext().getText(R.string.to_capital) + ": " + operation.getTo().getAccountName();
        TextView toUser = (TextView) v.findViewById(R.id.destination_account);
        toUser.setText(toMessage);

        String fromMessage = getContext().getText(R.string.from_capital) + ": " + operation.getFrom().getAccountName();
        TextView fromUser = (TextView) v.findViewById(R.id.origin_account);
        fromUser.setText(fromMessage);

        if(!operation.getMemo().getPlaintextMessage().equals("")){
            TextView memoTextView = (TextView) v.findViewById(R.id.memo);
            memoTextView.setText(operation.getMemo().getPlaintextMessage());
        }
        return v;
    }

    private View renderAmount(HistoricalTransferEntry historicalTransfer) {
        TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View root = me.inflate(R.layout.transactionsendamountview, null);
        TextView transferAmount = (TextView) root.findViewById(R.id.asset_amount);
        AssetAmount assetAmount = operation.getTransferAmount();

        TextView fiatAmountTextView = (TextView) root.findViewById(R.id.fiat_amount);
        AssetAmount fiatAmount = historicalTransfer.getEquivalentValue();

        String language = Helper.fetchStringSharePref(getContext(), getContext().getString(R.string.pref_language));
        Locale locale = new Locale(language);
        String symbol = "";
        if(assetAmount.getAsset() != null){
            symbol = assetAmount.getAsset().getSymbol();
        }
        int redColor = ContextCompat.getColor(getContext(),R.color.sendamount);
        int greenColor = ContextCompat.getColor(getContext(),R.color.recieveamount);

        if(operation.getFrom().getObjectId().equals(userAccount.getObjectId())){
            // User sent this transfer
            transferAmount.setTextColor(redColor);
            fiatAmountTextView.setTextColor(redColor);
            String amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(assetAmount));
            transferAmount.setText(String.format("- %s %s", amount, symbol));
        }else{
            // User received this transfer
            transferAmount.setTextColor(greenColor);
            fiatAmountTextView.setTextColor(greenColor);
            String amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(assetAmount));
            transferAmount.setText(String.format("+ %s %s", amount, symbol));
        }

        if(fiatAmount != null){
            String eqValue = String.format("~ %s %.2f", fiatAmount.getAsset().getSymbol(), Util.fromBase(fiatAmount));
            Log.d(TAG,"Fiat amount: "+eqValue);
            fiatAmountTextView.setText(eqValue);
        }else{
            Log.w(TAG, String.format("Fiat amount is null for transfer: %d %s", assetAmount.getAmount().longValue(), assetAmount.getAsset().getSymbol()));
        }
        return root;
    }

}
