package de.bitshares_munich.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luminiasoft.bitshares.AssetAmount;
import com.luminiasoft.bitshares.TransferOperation;
import com.luminiasoft.bitshares.UserAccount;
import com.luminiasoft.bitshares.Util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Helper;
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
        TextView toUser = (TextView) v.findViewById(R.id.transactiondetailsto);
        toUser.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
        toUser.setText(toMessage);

        String fromMessage = getContext().getText(R.string.from_capital) + ": " + operation.getFrom().getAccountName();
        TextView fromUser = (TextView) v.findViewById(R.id.transactiondetailsfrom);
        fromUser.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7);
        fromUser.setText(fromMessage);

        if(!operation.getMemo().getPlaintextMessage().equals("")){
            TextView memoTextView = (TextView) v.findViewById(R.id.transactiondetailsmemo);
            memoTextView.setText(operation.getMemo().getPlaintextMessage());
        }

//        if(transactiondetails.getDetailsMemo() == null || transactiondetails.getDetailsMemo().isEmpty()) {
//            TextView textView2 = (TextView) v.findViewById(R.id.transactiondetailsmemo);
//            textView2.setText("");
//            textView2.setTextSize(TypedValue.COMPLEX_UNIT_PT,7);
//            textView2.setVisibility(View.GONE);
//        } else {
//            toMessage = transactiondetails.getDetailsMemo();
//            toMessage = toMessage.substring(0, Math.min(toMessage.length(), 53));
//            toMessage = abbreviateString(toMessage, 50);
//            toMessage = context.getText(R.string.memo_capital) + " : " + toMessage;
//            TextView textView2 = (TextView) v.findViewById(R.id.transactiondetailsmemo);
//            textView2.setTextSize(TypedValue.COMPLEX_UNIT_PT,7);
//            textView2.setText(toMessage);
//        }
        return v;
    }

    private View renderAmount(HistoricalTransferEntry historicalTransfer) {
        TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View root = me.inflate(R.layout.transactionsendamountview, null);
        TextView transferAmount = (TextView) root.findViewById(R.id.transactionssendamount);
        AssetAmount assetAmount = operation.getTransferAmount();

        String language = Helper.fetchStringSharePref(getContext(), getContext().getString(R.string.pref_language));
        Locale locale = new Locale(language);
        String symbol = "";
        if(assetAmount.getAsset() != null){
            symbol = assetAmount.getAsset().getSymbol();
        }
        if(operation.getFrom().getObjectId().equals(userAccount.getObjectId())){
            // User sent this transfer
            transferAmount.setTextColor(ContextCompat.getColor(getContext(),R.color.sendamount));
            String amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(assetAmount));
            transferAmount.setText(String.format("- %s %s", amount, symbol));
        }else{
            // User received this transfer
            transferAmount.setTextColor(ContextCompat.getColor(getContext(),R.color.recieveamount));
            String amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(assetAmount));
            transferAmount.setText(String.format("+ %s %s", amount, symbol));
        }

//        Locale locale;
        NumberFormat format;
//        String language;
//        language = Helper.fetchStringSharePref(getContext(), getContext().getString(R.string.pref_language));
//        locale = new Locale(language);
        Helper.setLocaleNumberFormat(locale, 1);
//        if( historicalTransfer.getOperation().getFrom().getObjectId().equals(userAccount.getObjectId())) {
//            // The transaction was sent from this user's account
//            TextView transferAmount = (TextView) root.findViewById(R.id.transactionssendamount);
//            transferAmount.setTextColor(ContextCompat.getColor(getContext(),R.color.sendamount));
//            String amount = Helper.setLocaleNumberFormat(locale, historicalTransfer.getOperation().getTransferAmount().getAmount());
//            transferAmount.setText(String.format("- %s %s", amount, "-"));
//            amount = "";
//
//            if ( transactiondetails.getFaitAmount() == 0 )
//            {
//                TextView textView2 = (TextView) root.findViewById(R.id.transactionssendfaitamount);
//                textView2.setText("");
//            }
//            else
//            {
//                TextView textView2 = (TextView) root.findViewById(R.id.transactionssendfaitamount);
//                textView2.setTextColor(ContextCompat.getColor(getContext(),R.color.sendamount));
//
//                double faitAmount = transactiondetails.getFaitAmount();
//
//                if ( faitAmount > 0.009 )
//                {
//                    amount = String.format(locale,"%.2f",faitAmount);
//                }
//                else if ( (faitAmount < 0.009) && (faitAmount > 0.0009)  )
//                {
//                    amount = String.format(locale,"%.3f",faitAmount);
//                }
//                else if ( (faitAmount < 0.0009) && (faitAmount > 0.00009)  )
//                {
//                    amount = String.format(locale,"%.4f",faitAmount);
//                }
//                else
//                {
//                    amount = String.format(locale,"%.5f",faitAmount);
//                }
//
//                String displayFaitAmount = "";
//                if ( Helper.isRTL(locale,transactiondetails.getFaitAssetSymbol()) )
//                {
//                    displayFaitAmount =  String.format(locale,"%s %s",amount,transactiondetails.getFaitAssetSymbol());
//                }
//                else
//                {
//                    displayFaitAmount =  String.format(locale,"%s %s",transactiondetails.getFaitAssetSymbol(),amount);
//                }
//                textView2.setText("- " + displayFaitAmount);
//            }
//        }
//        else
//        {
//            TextView textView = (TextView) root.findViewById(R.id.transactionssendamount);
//            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.recieveamount));
//            String amount = Helper.setLocaleNumberFormat(locale,transactiondetails.getAmount());
//            textView.setText("+ " + amount + " " + transactiondetails.getAssetSymbol());
//            amount = "";
//
//            if ( transactiondetails.getFaitAmount() == 0 )
//            {
//                TextView textView2 = (TextView) root.findViewById(R.id.transactionssendfaitamount);
//                textView2.setText("");
//            }
//            else
//            {
//                TextView textView2 = (TextView) root.findViewById(R.id.transactionssendfaitamount);
//                textView2.setTextColor(ContextCompat.getColor(getContext(), R.color.recieveamount));
//
//                double faitAmount = transactiondetails.getFaitAmount();
//
//                if ( faitAmount > 0.009 )
//                {
//                    amount = String.format(locale,"%.2f",faitAmount);
//                }
//                else if ( (faitAmount < 0.009) && (faitAmount > 0.0009)  )
//                {
//                    amount = String.format(locale,"%.3f",faitAmount);
//                }
//                else if ( (faitAmount < 0.0009) && (faitAmount > 0.00009)  )
//                {
//                    amount = String.format(locale,"%.4f",faitAmount);
//                }
//                else
//                {
//                    amount = String.format(locale,"%.5f",faitAmount);
//                }
//
//                String displayFaitAmount = "";
//                if ( Helper.isRTL(locale,transactiondetails.getFaitAssetSymbol()) )
//                {
//                    displayFaitAmount =  String.format(locale,"%s %s",amount,transactiondetails.getFaitAssetSymbol());
//                }
//                else
//                {
//                    displayFaitAmount =  String.format(locale,"%s %s",transactiondetails.getFaitAssetSymbol(),amount);
//                }
//
//                textView2.setText("+ " + displayFaitAmount);
//            }
//        }
        return root;
    }

}
