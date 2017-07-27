package de.bitsharesmunich.cryptocoincore.adapters;

/**
 * Created by Henry Varona on 15/3/2017.
 */

import java.util.ArrayList;
import java.util.List;

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

/**
 * Create a selection with all the coins available
 *
 * It is used when the user is asked to select a type of coin for a
 * specific purpose (like the creation of a new coin account)
 */
public class ArrayListCoinAdapter extends ArrayAdapter<String>{

    private Activity activity; /**< the activity holding this adapter*/
    private ArrayList data; /**< an array of Coins to be listed for selection*/
    private List<Coin> coinsUsed; /**< a list of coins to be disable in the list*/
    public Resources res;
    LayoutInflater inflater;
    public boolean showSelected; /**< Whether to show or not the selected item in the list*/

    public ArrayListCoinAdapter(Activity activity, int textViewResourceId, ArrayList objects, List<Coin> coinsUsed, Resources resLocal){
        super(activity, textViewResourceId, objects);

        //This is for preventing the list to select a valid item in the initialization
        objects.add(0,null);

        this.activity = activity;
        this.data = objects;
        this.coinsUsed = coinsUsed;
        this.res = resLocal;
        this.showSelected = true;

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setShowSelected(boolean newValue){
        this.showSelected = newValue;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = getCustomView(position, convertView, parent);

        if (!showSelected) {
            result.setVisibility(View.GONE);
        }

        return result;
    }

    @Override
    public boolean isEnabled(int position) {
        Coin nextCoin = (Coin) data.get(position);

        if (nextCoin != null) {
            if (coinsUsed != null) {
                if (coinsUsed.contains(nextCoin)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = inflater.inflate(R.layout.coin_spinner_row, parent, false);

        Coin nextCoin = (Coin) data.get(position);
        TextView coinLabel = (TextView) row.findViewById(R.id.coin_label);
        ImageView coinIcon = (ImageView) row.findViewById(R.id.coin_icon);

        if (nextCoin != null) {
            if (coinsUsed != null) {
                if (coinsUsed.contains(nextCoin)) {
                    coinLabel.setTextColor(res.getColor(R.color.gray));
                }
            }

            coinLabel.setText(nextCoin.getLabel());
            coinIcon.setImageResource(nextCoin.getIcon());
        } else {
            coinIcon.setImageResource(R.mipmap.coin_unknown);
        }

        return row;
    }
}