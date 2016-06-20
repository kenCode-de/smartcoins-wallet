package de.bitshares_munich.adapters;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by developer on 5/20/16.
 */
public class TransactionsTableAdapter extends TableDataAdapter<TransactionDetails> {
Context context;
    public TransactionsTableAdapter(Context _context, List<TransactionDetails> data) {
        super(_context, data);
        context = _context;
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        TransactionDetails transactiondetails = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderDateView(transactiondetails);
                break;
            case 1:
                renderedView = renderSendRecieve(transactiondetails);
                break;
            case 2:
                renderedView = renderDetails(transactiondetails);
                break;
            case 3:
                renderedView = renderAmount(transactiondetails);
                break;
        }

        return renderedView;
    }



    private View renderDateView(TransactionDetails transactiondetails)
    {
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactionsdateview, null);
        TextView textView = (TextView) v.findViewById(R.id.transactiondate);
        textView.setText(transactiondetails.getDateString());
        TextView textView2 = (TextView) v.findViewById(R.id.transactiontime);
        textView2.setText(transactiondetails.getTimeString());
        TextView textView3 = (TextView) v.findViewById(R.id.transactionttimezone);
        textView3.setText(transactiondetails.getTimeZone());
        return v;
    }

    private View renderSendRecieve(TransactionDetails transactiondetails)
    {
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactionssendrecieve, null);
        ImageView imgView = (ImageView) v.findViewById(R.id.iv);
        if ( transactiondetails.getSent() )
        {
            imgView.setImageResource(R.drawable.sendicon);
        }
        else
        {
            imgView.setImageResource(R.drawable.rcvicon);
        }


        //MyCustomView vi = new MyCustomView(getContext(),null,transactiondetails.getDate());
        return v;
    }

    private View renderDetails(TransactionDetails transactiondetails)
    {
        LayoutInflater me = getLayoutInflater();
        View v = me.inflate(R.layout.transactiondetailsview, null);
        TextView textView = (TextView) v.findViewById(R.id.transactiondetailsto);
        String tString = context.getText(R.string.to_capital) + ": " + transactiondetails.getDetailsTo();
        textView.setText(tString);
        tString = context.getText(R.string.from_capital) + ": " + transactiondetails.getDetailsFrom();
        TextView textView1 = (TextView) v.findViewById(R.id.transactiondetailsfrom);
        textView1.setText(tString);
        if(transactiondetails.getDetailsMemo().equals("----")){
        TextView textView2 = (TextView) v.findViewById(R.id.transactiondetailsmemo);
            textView2.setText("");
            textView2.setVisibility(View.GONE);
        }else{
            tString = context.getText(R.string.memo_capital) + " : " + transactiondetails.getDetailsMemo();
            TextView textView2 = (TextView) v.findViewById(R.id.transactiondetailsmemo);
            textView2.setText(tString);
        }
        return v;
    }

    private View renderAmount(TransactionDetails transactiondetails)
    {
        LayoutInflater me = getLayoutInflater();
        View  v = me.inflate(R.layout.transactionsendamountview, null);
        int colorText;
        if( transactiondetails.getSent() )
        {
            TextView textView = (TextView) v.findViewById(R.id.transactionssendamount);
            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.sendamount));
            textView.setText("- " + transactiondetails.getAmount() + " " + transactiondetails.getAssetSymbol());

            TextView textView2 = (TextView) v.findViewById(R.id.transactionssendfaitamount);
            textView2.setTextColor(ContextCompat.getColor(getContext(),R.color.sendamount));
            textView2.setText("- " + transactiondetails.getFaitAmount() + " " + transactiondetails.getFaitAssetSymbol());
        }
        else
        {
            TextView textView = (TextView) v.findViewById(R.id.transactionssendamount);
            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.recieveamount));
            textView.setText("+ " + transactiondetails.getAmount() + " " + transactiondetails.getAssetSymbol());

            TextView textView2 = (TextView) v.findViewById(R.id.transactionssendfaitamount);
            textView2.setTextColor(ContextCompat.getColor(getContext(),R.color.recieveamount));
            textView2.setText("+ " + transactiondetails.getFaitAmount() + " " + transactiondetails.getFaitAssetSymbol());
        }
        return v;
    }
}

