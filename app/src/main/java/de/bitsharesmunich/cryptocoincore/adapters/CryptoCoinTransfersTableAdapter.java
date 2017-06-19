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
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.Util;
import de.codecrafters.tableview.TableDataAdapter;

import static de.bitsharesmunich.cryptocoincore.base.TransactionLog.TransactionType.TRANSACTION_TYPE_BITCOIN;
import static de.bitsharesmunich.cryptocoincore.base.TransactionLog.TransactionType.TRANSACTION_TYPE_BITSHARE;


/**
 * Created by henry on 12/02/17.
 */
public class CryptoCoinTransfersTableAdapter extends TableDataAdapter<TransactionLog> {
    private String TAG = this.getClass().getName();

    private Locale locale;

    public TransactionLog t;

    public CryptoCoinTransfersTableAdapter(Context context, Locale locale, TransactionLog[] data) {
        super(context, data);
        this.locale = locale;
    }

    public void deleteTransactionsType(TransactionLog.TransactionType type){
        List<TransactionLog> transactionData = this.getData();

        for (TransactionLog nextTransaction : transactionData){
            if (nextTransaction.getType() == type){
                transactionData.remove(nextTransaction);
            }
        }

        this.notifyDataSetChanged();
    }

    public void deleteBitcoinTypeTransactions(){
        this.deleteTransactionsType(TRANSACTION_TYPE_BITCOIN);
    }

    public void deleteBitshareTypeTransactions(){
        this.deleteTransactionsType(TRANSACTION_TYPE_BITSHARE);
    }

    public void addOrReplaceData(TransactionLog[] data){
        List<TransactionLog> oldData = this.getData();
        TransactionLog oldTransactionFound;

        for (TransactionLog newTransaction : data){
            oldTransactionFound = null;

            for (TransactionLog oldTransaction : oldData){
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
        TransactionLog transferEntry = getRowData(rowIndex);
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

    private View renderDateView(TransactionLog historicalTransfer) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionsdateview, null);
        TextView dateTextView = (TextView) v.findViewById(R.id.transactiondate);
        TextView timeTextView = (TextView) v.findViewById(R.id.transactiontime);
        TextView timeZoneTextView = (TextView) v.findViewById(R.id.transactionttimezone);

        Date date = null;
        switch(historicalTransfer.getType()){
            case TRANSACTION_TYPE_BITSHARE:
                date = new Date( historicalTransfer.getBitshareTransactionLog().getTimestamp() * 1000);
                break;
            case TRANSACTION_TYPE_BITCOIN:
                date = historicalTransfer.getBitcoinTransactionLog().getDate();
                break;
        }

        if(date.getTime() > 0){

            //Date date = historicalTransfer.getDate();

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
        }
        return v;
    }

    private View renderSendRecieve(TransactionLog historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.transactionssendrecieve, null);
        ImageView imgView = (ImageView) v.findViewById(R.id.iv);


        switch(historicalTransfer.getType()){

            case TRANSACTION_TYPE_BITSHARE:
                TransferOperation operation = historicalTransfer.getBitshareTransactionLog().getHistoricalTransfer().getOperation();

                if (operation.getFrom().getObjectId().equals(historicalTransfer.getBitshareAccount().getObjectId()) ) {
                    imgView.setImageResource(R.drawable.send);
                } else {
                    imgView.setImageResource(R.drawable.receive);
                }
                break;
            case TRANSACTION_TYPE_BITCOIN:
                if (historicalTransfer.getBitcoinTransactionLog().getAccountBalanceChange() < 0) {
                    imgView.setImageResource(R.drawable.send);
                } else {
                    imgView.setImageResource(R.drawable.receive);
                }
                break;
        }

        return v;
    }

    private View renderDetails(TransactionLog historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactiondetailsview, null);
        TextView toUser = (TextView) v.findViewById(R.id.destination_account);
        TextView fromUser = (TextView) v.findViewById(R.id.origin_account);
        TextView memoTextView = (TextView) v.findViewById(R.id.memo);
        String toMessage;
        String fromMessage;


        switch (historicalTransfer.getType()) {

            case TRANSACTION_TYPE_BITCOIN:
                GeneralTransaction generalTransaction =  historicalTransfer.getBitcoinTransactionLog();
                String to = generalTransaction.getTxOutputs().get(0).getAddressString();
                to = to.substring(0, 2) + "..." + to.substring(to.length() - 4, to.length());
                toMessage = getContext().getText(R.string.to_capital) + ": " + to;
                toUser.setText(toMessage);

                String from = generalTransaction.getTxInputs().get(0).getAddressString();
                from = from.substring(0, 2) + "..." + from.substring(from.length() - 4, from.length());
                fromMessage = getContext().getText(R.string.from_capital) + ": " + from;
                fromUser.setText(fromMessage);

                if ((generalTransaction.getMemo() != null) && (!generalTransaction.getMemo().equals(""))) {
                    memoTextView.setText(generalTransaction.getMemo());
                }
                break;
            case TRANSACTION_TYPE_BITSHARE:
                HistoricalTransferEntry historicalTransferEntry =  historicalTransfer.getBitshareTransactionLog();
                TransferOperation operation = historicalTransferEntry.getHistoricalTransfer().getOperation();

                toMessage = getContext().getText(R.string.to_capital) + ": " + operation.getTo().getAccountName();
                toUser = (TextView) v.findViewById(R.id.destination_account);
                toUser.setText(toMessage);

                fromMessage = getContext().getText(R.string.from_capital) + ": " + operation.getFrom().getAccountName();
                fromUser = (TextView) v.findViewById(R.id.origin_account);
                fromUser.setText(fromMessage);

                if(!operation.getMemo().getPlaintextMessage().equals("")){
                    memoTextView = (TextView) v.findViewById(R.id.memo);
                    memoTextView.setText(operation.getMemo().getPlaintextMessage());
                }
                break;
        }

        return v;
    }

    private View renderAmount(TransactionLog historicalTransfer) {
        //TransferOperation operation = historicalTransfer.getAmount() getHistoricalTransfer().getOperation();
        LayoutInflater me = getLayoutInflater();
        View root = me.inflate(R.layout.transactionsendamountview, null);
        TextView transferAmountTextView = (TextView) root.findViewById(R.id.asset_amount);
        TextView fiatAmountTextView = (TextView) root.findViewById(R.id.fiat_amount);
        //String language = Helper.fetchStringSharePref(getContext(), getContext().getString(R.string.pref_language), Constants.DEFAULT_LANGUAGE_CODE);
        //Locale locale = new Locale(language);

        int redColor = ContextCompat.getColor(getContext(),R.color.send_amount);
        int greenColor = ContextCompat.getColor(getContext(),R.color.receive_amount);
        int lightRed = ContextCompat.getColor(getContext(), R.color.send_amount_light);
        int lightGreen = ContextCompat.getColor(getContext(), R.color.receive_amount_light);

        String symbol = "";
        String amount = "";
        String fiatAmountText = "";
        boolean isSending = true;

        switch (historicalTransfer.getType()) {

            case TRANSACTION_TYPE_BITSHARE:
                HistoricalTransferEntry historicalTransferEntry = historicalTransfer.getBitshareTransactionLog();
                TransferOperation operation = historicalTransferEntry.getHistoricalTransfer().getOperation();
                AssetAmount transferAmount = operation.getTransferAmount();

                AssetAmount smartcoinAmount = historicalTransferEntry.getEquivalentValue();

                GeneralCoinSettings coinSettings = GeneralCoinFactory.getSettings(getContext(),Coin.BITSHARE);
                GeneralCoinSettings.GeneralCoinSetting precisionSetting = coinSettings.getSetting("precision");

                if(transferAmount.getAsset() != null){
                    symbol = transferAmount.getAsset().getSymbol();

                    if (precisionSetting != null) {
                        switch (precisionSetting.getValue()) {
                            case "5":
                                symbol = "m" + symbol;
                                break;
                            case "2":
                                symbol = "μ" + symbol;
                                break;
                        }
                    }
                }

                /*If the precision were set by the user, then we have to used that one */
                if (precisionSetting != null){
                    int precision = Integer.parseInt(precisionSetting.getValue());

                    amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(transferAmount)*Math.pow(10,8-precision));
                } else {
                    amount = Helper.setLocaleNumberFormat(locale, Util.fromBase(transferAmount));
                }

                isSending = operation.getFrom().getObjectId().equals(historicalTransfer.getBitshareAccount().getObjectId());

                if(smartcoinAmount != null){
                    Log.d(TAG,"Using smartcoin: "+smartcoinAmount.getAsset().getObjectId());
                    final Currency currency = Currency.getInstance(smartcoinAmount.getAsset().getSymbol());
                    NumberFormat currencyFormatter = Helper.newCurrencyFormat(getContext(), currency, locale);
                    String eqValue = currencyFormatter.format(Util.fromBase(smartcoinAmount));

                    fiatAmountText = eqValue;

                    if (isSending){
                        fiatAmountTextView.setTextColor(lightRed);
                    } else {
                        fiatAmountTextView.setTextColor(lightGreen);
                    }
                }else{
                    Log.w(TAG, String.format("Fiat amount is null for transfer: %d %s", transferAmount.getAmount().longValue(), transferAmount.getAsset().getSymbol()));
                }
                break;
            case TRANSACTION_TYPE_BITCOIN:

                GeneralTransaction generalTransaction = historicalTransfer.getBitcoinTransactionLog();
                coinSettings = GeneralCoinFactory.getSettings(getContext(),generalTransaction.getType());
                precisionSetting = coinSettings.getSetting("precision");

                symbol = generalTransaction.getType().getLabel();

                if (precisionSetting != null) {
                    switch (precisionSetting.getValue()) {
                        case "5":
                            symbol = "m" + symbol;
                            break;
                        case "2":
                            symbol = "μ" + symbol;
                            break;
                    }
                }

                double balanceChange = generalTransaction.getAccountBalanceChange();

                isSending = balanceChange < 0;
                if (isSending){
                    balanceChange = -balanceChange;
                }

                /*If the precision were set by the user, then we have to used that one */
                int precision = 0;
                if (precisionSetting != null){
                    precision = Integer.parseInt(precisionSetting.getValue());
                } else {
                    precision = generalTransaction.getType().getPrecision();
                }

                amount = Helper.setLocaleNumberFormat(locale, balanceChange/Math.pow(10,precision));


                if (generalTransaction.getConfirm() < generalTransaction.getType().getConfirmationsNeeded()){
                    int percentageDone = (generalTransaction.getConfirm()+1)*100/generalTransaction.getType().getConfirmationsNeeded();
                    int confirmationColor = 0;

                    if (percentageDone < 34){
                        confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_starting);
                    } else if (percentageDone < 67){
                        confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_half);
                    } else {
                        confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_almost_complete);
                    }

                    fiatAmountTextView.setTextColor(confirmationColor);
                    fiatAmountText = generalTransaction.getConfirm()+" of "+generalTransaction.getType().getConfirmationsNeeded()+" conf";
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

                break;
        }

        if(isSending){
            // User sent this transfer
            transferAmountTextView.setTextColor(redColor);
            //fiatAmountTextView.setTextColor(lightRed);
            transferAmountTextView.setText(String.format("- %s %s", amount, symbol));
        }else{
            // User received this transfer
            transferAmountTextView.setTextColor(greenColor);
            //fiatAmountTextView.setTextColor(lightGreen);
            transferAmountTextView.setText(String.format("+ %s %s", amount, symbol));
        }

        fiatAmountTextView.setText(fiatAmountText);
        return root;
    }

}
