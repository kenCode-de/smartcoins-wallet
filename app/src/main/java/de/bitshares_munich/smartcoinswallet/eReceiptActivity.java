package de.bitshares_munich.smartcoinswallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.HashMap;

import de.bitshares_munich.utils.SupportMethods;

public class eReceiptActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_receipt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String eReciept = intent.getStringExtra(getResources().getString(R.string.e_receipt));
        SupportMethods.testing("qanon",eReciept,"eReciept");
        init(eReciept);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    void init(String value){
//        value = value.substring(1, value.length()-1);           //remove curly brackets
//        String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
//        Map<String,String> map = new HashMap<>();
//
//        for(String pair : keyValuePairs)                        //iterate over the pairs
//        {
//            String[] entry = pair.split("=");                   //split the pairs to get key and value
//            map.put(entry[0], entry[1]);          //add them to the hashmap and trim whitespaces
//        }
//
//        Iterator it = map.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            SupportMethods.testing("qanon",pair.getKey() + " = " + pair.getValue(),"eReciept");
//            it.remove(); // avoids a ConcurrentModificationException
//        }
//    }[

    void init(String eRecipt){
        HashMap<String,String> eReciptmap = new HashMap<>();
        eReciptmap.put("id", SupportMethods.ParseJsonObject(eRecipt,"id"));
        eReciptmap.put("op", SupportMethods.ParseJsonObject(eRecipt,"op"));
        eReciptmap.put("result", SupportMethods.ParseJsonObject(eRecipt,"result"));
        eReciptmap.put("block_num", SupportMethods.ParseJsonObject(eRecipt,"block_num"));
        eReciptmap.put("trx_in_block", SupportMethods.ParseJsonObject(eRecipt,"trx_in_block"));
        eReciptmap.put("op_in_trx", SupportMethods.ParseJsonObject(eRecipt,"op_in_trx"));
        eReciptmap.put("virtual_op", SupportMethods.ParseJsonObject(eRecipt,"virtual_op"));
        String breakArray = SupportMethods.ParseObjectFromJsonArray(eReciptmap.get("op"), 1);
        SupportMethods.testing("qanon",breakArray,"eReciept");

        //  String breakArray = SupportMethods.ParseObjectFromJsonArray(eReciptmap.get("op").get(i), 1);
//
//        for(int i = 0 ; i<             String breakArray = returnArrayObj(arrayofOP.get("op").get(i), 1);
//        ) {
//            SupportMethods.testing("qanon", eReciptmap, "eReciptmap");
//        }
    }

}
