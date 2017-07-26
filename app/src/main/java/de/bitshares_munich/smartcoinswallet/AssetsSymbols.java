package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import de.bitshares_munich.models.CCAssets;
import de.bitshares_munich.models.Smartcoin;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.models.Uia;
import de.bitshares_munich.utils.IWebService;
import de.bitshares_munich.utils.ServiceGenerator;
import de.bitshares_munich.utils.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by afnan on 7/19/16.
 */
public class AssetsSymbols {
    TinyDB tinyDB;
    Context context;

    public AssetsSymbols(Context aContext) {
        context = aContext;
        tinyDB = new TinyDB(aContext);
    }

    public Boolean isUiaSymbol(String sym) {
        ArrayList<String> uiaList = tinyDB.getListString(context.getString(R.string.pref_uia_coin));
        return uiaList.contains(sym);
    }

    public Boolean isSmartCoinSymbol(String sym) {
        sym = sym.replace("bit", "");
        ArrayList<String> smartcoinsList = tinyDB.getListString(context.getString(R.string.pref_desired_smart_coin));
        return smartcoinsList.contains(sym);
    }

    public ArrayList<String> updatedList(ArrayList<String> sym) {
        ArrayList<String> symbols = new ArrayList<>();
        for (String s : sym) {
            symbols.add(updateString(s));
        }
        return symbols;
    }

    public String updateString(String string) {
        if (string.equals("BTS")) {
            return string;
        } else if (isSmartCoinSymbol(string)) {
            return "bit" + string;
        } else if (isUiaSymbol(string)) {
            return string;
        } else return string;
    }

    public void displaySpannable(TextView textView, String bit) {
        if (bit.contains("bit")) {
            SpannableString ss1 = new SpannableString(bit);
            ss1.setSpan(new RelativeSizeSpan(0.8f), 0, 3, 0); // set size
            ss1.setSpan(0, 3, bit.length(), 0);
            textView.setText(ss1);
        } else {
            textView.setText(bit);
        }
    }
}
