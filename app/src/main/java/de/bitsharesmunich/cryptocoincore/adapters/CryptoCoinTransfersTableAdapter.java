package de.bitsharesmunich.cryptocoincore.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Helper;
import de.bitsharesmunich.cryptocoincore.base.GIOTx;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Util;
import de.codecrafters.tableview.TableDataAdapter;


/**
 * Created by henry on 12/02/17.
 */
public class CryptoCoinTransfersTableAdapter extends TableDataAdapter<GeneralTransaction> {
    private String TAG = this.getClass().getName();

    private GeneralCoinAccount account;
    private Locale locale;

    public CryptoCoinTransfersTableAdapter(Context context, GeneralCoinAccount account, Locale locale, GeneralTransaction[] data) {
        super(context, data);
        this.locale = locale;
        this.account = account;
    }

    public void addOrReplaceData(GeneralTransaction[] data){
        List<GeneralTransaction> oldData = this.getData();
        GeneralTransaction oldTransactionFound;

        for (GeneralTransaction newTransaction : data){
            oldTransactionFound = null;

            for (GeneralTransaction oldTransaction : oldData){
                if (oldTransaction.equals(newTransaction)){
                    oldTransactionFound = oldTransaction;
                    break;
                }
            }

            if (oldTransactionFound != null) {
                oldData.set(oldData.indexOf(oldTransactionFound), newTransaction);
            } else {
                oldData.add(newTransaction);
            }
        }

        this.notifyDataSetChanged();
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        View renderedView = null;
        GeneralTransaction transferEntry = getRowData(rowIndex);
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

    private View renderDateView(GeneralTransaction historicalTransfer) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionsdateview, null);
        TextView dateTextView = (TextView) v.findViewById(R.id.transactiondate);
        TextView timeTextView = (TextView) v.findViewById(R.id.transactiontime);
        TextView timeZoneTextView = (TextView) v.findViewById(R.id.transactionttimezone);

        //if(historicalTransfer.getTransaction().getDate()getTimestamp() > 0){

            Date date = historicalTransfer.getDate();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
            SimpleDateFormat timeZone = new SimpleDateFormat("zzz");

            TimeZone tz = TimeZone.getTimeZone(timeZone.format(date));
            String formattedTimeZone = tz.getDisplayName(false, TimeZone.SHORT);

            // It was requested that we omit the last part of the time zone information
            // turning GMT-02:00 into GMT-2 for instance.
            // The following code does just that.
            Pattern pattern = Pattern.compile("(GMT[+-])(\\d)(\\d):(\\d\\d)");
            Matcher m = pattern.matcher(formattedTimeZone);
            if(m.matches()){
                if(m.group(4).equals("00")){
                    formattedTimeZone = m.group(1) + m.group(3);
                }
            }

            dateTextView.setText(dateFormat.format(date));
            timeTextView.setText(timeFormat.format(date));
            timeZoneTextView.setText(formattedTimeZone);
        //}
        return v;
    }

    private View renderSendRecieve(GeneralTransaction historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionssendrecieve, null);
        ImageView imgView = (ImageView) v.findViewById(R.id.iv);
        if (historicalTransfer.getAccountBalanceChange() < 0) {
            imgView.setImageResource(R.drawable.send);
        } else {
            imgView.setImageResource(R.drawable.receive);
        }
        return v;
    }

    private View renderDetails(GeneralTransaction historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactiondetailsview, null);

        String to = historicalTransfer.getTxOutputs().get(0).getAddressString();
        to = to.substring(0,2)+"..."+to.substring(to.length()-4,to.length());

        String toMessage = getContext().getText(R.string.to_capital) + ": " + to;
        TextView toUser = (TextView) v.findViewById(R.id.destination_account);
        toUser.setText(toMessage);

        String from = historicalTransfer.getTxInputs().get(0).getAddressString();
        from = from.substring(0,2)+"..."+from.substring(from.length()-4,from.length());

        String fromMessage = getContext().getText(R.string.from_capital) + ": " + from;
        TextView fromUser = (TextView) v.findViewById(R.id.origin_account);
        fromUser.setText(fromMessage);

        if((historicalTransfer.getMemo() != null) && (!historicalTransfer.getMemo().equals(""))){
            TextView memoTextView = (TextView) v.findViewById(R.id.memo);
            memoTextView.setText(historicalTransfer.getMemo());
        }
        return v;
    }

    private View renderAmount(GeneralTransaction historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getAmount() getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View root = me.inflate(R.layout.transactionsendamountview, null);
        TextView transferAmountTextView = (TextView) root.findViewById(R.id.asset_amount);
        //AssetAmount transferAmount =  historicalTransfer.getAmount();

        TextView fiatAmountTextView = (TextView) root.findViewById(R.id.fiat_amount);
        //AssetAmount smartcoinAmount = historicalTransfer.getEquivalentValue();

//        String language = Helper.fetchStringSharePref(getContext(), getContext().getString(R.string.pref_language), Constants.DEFAULT_LANGUAGE_CODE);
//        Locale locale = new Locale(language);
        String symbol = historicalTransfer.getType().getLabel();
        //if(transferAmount.getAsset() != null){
        //    symbol = transferAmount.getAsset().getSymbol();
        //}
        int redColor = ContextCompat.getColor(getContext(),R.color.send_amount);
        int greenColor = ContextCompat.getColor(getContext(),R.color.receive_amount);
        int lightRed = ContextCompat.getColor(getContext(), R.color.send_amount_light);
        int lightGreen = ContextCompat.getColor(getContext(), R.color.receive_amount_light);

        double balanceChange = historicalTransfer.getAccountBalanceChange();

        if(balanceChange < 0){
            // User sent this transfer
            transferAmountTextView.setTextColor(redColor);
            fiatAmountTextView.setTextColor(lightRed);
            String amount = Helper.setLocaleNumberFormat(locale, -balanceChange/Math.pow(10,historicalTransfer.getType().getPrecision()));
            transferAmountTextView.setText(String.format("- %s %s", amount, symbol));
        }else{
            // User received this transfer
            transferAmountTextView.setTextColor(greenColor);
            fiatAmountTextView.setTextColor(lightGreen);
            String amount = Helper.setLocaleNumberFormat(locale, balanceChange/Math.pow(10,historicalTransfer.getType().getPrecision()));
            transferAmountTextView.setText(String.format("+ %s %s", amount, symbol));
        }

        if (historicalTransfer.getConfirm() < historicalTransfer.getType().getConfirmationsNeeded()){
            int percentageDone = (historicalTransfer.getConfirm()+1)*100/historicalTransfer.getType().getConfirmationsNeeded();
            int confirmationColor = 0;

            if (percentageDone < 34){
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_starting);
            } else if (percentageDone < 67){
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_half);
            } else {
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_almost_complete);
            }

            fiatAmountTextView.setTextColor(confirmationColor);
            fiatAmountTextView.setText(historicalTransfer.getConfirm()+" of "+historicalTransfer.getType().getConfirmationsNeeded()+" conf");
        } else {

            /*if(smartcoinAmount != null){
                Log.d(TAG,"Using smartcoin: "+smartcoinAmount.getAsset().getObjectId());
                final Currency currency = Currency.getInstance(smartcoinAmount.getAsset().getSymbol());
                NumberFormat currencyFormatter = Helper.newCurrencyFormat(getContext(), currency, locale);
                String eqValue = currencyFormatter.format(Util.fromBase(smartcoinAmount));

    //            String fiatSymbol = Smartcoins.getFiatSymbol(smartcoinAmount.getAsset());
    //            String eqValue = String.format("~ %s %.2f", fiatSymbol, Util.fromBase(smartcoinAmount));
                fiatAmountTextView.setText(eqValue);
            }else{
                Log.w(TAG, String.format("Fiat amount is null for transfer: %d %s", transferAmount.getAmount().longValue(), transferAmount.getAsset().getSymbol()));
            }*/
        }
        return root;
    }

}
