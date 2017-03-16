package de.bitsharesmunich.cryptocoincore.adapters;

/**
 * Created by Henry Varona on 15/3/2017.
 */

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.base.Coin;

public class ArrayListCoinAdapter extends ArrayAdapter<String>{

    private Activity activity;
    private ArrayList data;
    public Resources res;
    LayoutInflater inflater;

    public ArrayListCoinAdapter(Activity activity, int textViewResourceId, ArrayList objects, Resources resLocal){
        super(activity, textViewResourceId, objects);

        this.activity = activity;
        this.data = objects;
        this.res = resLocal;

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = inflater.inflate(R.layout.coin_spinner_row, parent, false);

        Coin nextCoin = (Coin) data.get(position);

        TextView coinLabel = (TextView)row.findViewById(R.id.coin_label);
        ImageView coinIcon = (ImageView)row.findViewById(R.id.coin_icon);

        coinLabel.setText(nextCoin.getLabel());
        coinIcon.setImageResource(nextCoin.getIcon());

        return row;
    }
}