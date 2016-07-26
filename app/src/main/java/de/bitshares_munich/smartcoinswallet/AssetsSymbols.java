package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

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
    public AssetsSymbols(Context aContext){
        context = aContext;
        tinyDB = new TinyDB(aContext);
    }


    public void getAssetsFromServer() {

        ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.node_server_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<CCAssets> postingService = service.getAssets();
        postingService.enqueue(new Callback<CCAssets>() {
            @Override
            public void onResponse(Response<CCAssets> response) {
                if (response.isSuccess()) {
                    CCAssets ccAssets;
                    ccAssets = response.body();
                    saveAssetsSymbols(ccAssets);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    void saveAssetsSymbols(CCAssets ccAssets){

        ArrayList<String> smartcoinsList = tinyDB.getListString(context.getString(R.string.pref_desired_smart_coin));
        for(Smartcoin smartcoins : ccAssets.smartcoins){
            smartcoinsList.add(smartcoins.symbol);
        }

        HashSet<String> hashSet1 = new HashSet<>();
        hashSet1.addAll(smartcoinsList);
        smartcoinsList.clear();
        smartcoinsList.addAll(hashSet1);

        ArrayList<String> uiaList = tinyDB.getListString(context.getString(R.string.pref_uia_coin));
        for(Uia uia : ccAssets.uia){
            uiaList.add(uia.symbol);
        }

        HashSet<String> hashSet2 = new HashSet<>();
        hashSet2.addAll(uiaList);
        uiaList.clear();
        uiaList.addAll(hashSet2);

        tinyDB.putListString(context.getString(R.string.pref_desired_smart_coin),smartcoinsList);
        tinyDB.putListString(context.getString(R.string.pref_uia_coin),uiaList);

    }

    public Boolean isUiaSymbol(String sym){
        ArrayList<String> uiaList = tinyDB.getListString(context.getString(R.string.pref_uia_coin));
        return uiaList.contains(sym);
    }

    public Boolean isSmartCoinSymbol(String sym){
        sym = sym.replace("bit","");
        ArrayList<String> smartcoinsList = tinyDB.getListString(context.getString(R.string.pref_desired_smart_coin));
        return smartcoinsList.contains(sym);
    }
    public ArrayList<String> updatedList(ArrayList<String> sym){
        ArrayList<String> symbols = new ArrayList<>();
        for(String s : sym){
                symbols.add(updateString(s));
        }
        return symbols;
    }
    public ArrayList<TransactionDetails> updatedTransactionDetails(ArrayList<TransactionDetails> td){
        ArrayList<TransactionDetails> symbols = new ArrayList<>();
        for(TransactionDetails s : td){
            if(!s.assetSymbol.contains("bit")) {
                s.assetSymbol = updateString(s.assetSymbol);
            }
            symbols.add(s);
        }
        return symbols;
    }
    public String updateString(String string){
        if(string.equals("BTS")){
            return string;
        }else if(isSmartCoinSymbol(string)){
                return "bit"+string;
        }else if(isUiaSymbol(string)){
            return string;
        }else return string;
    }
}
