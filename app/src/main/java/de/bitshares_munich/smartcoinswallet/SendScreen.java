package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.BinderThread;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.bitshares_munich.utils.Helper;

/**
 * Created by Syed Muhammad Muzzammil on 5/6/16.
 */
public class SendScreen extends Activity {
    Context context;
    final String always_donate = "always_donate";
    final String backup_asset = "backup_asset";
    ArrayAdapter<String> iniAdapter;
    final String register_new_account = "register_new_account";


    @Bind(R.id.FirstChild)
    LinearLayout FirstChild;

    @Bind(R.id.SecChild)
    LinearLayout SecChild;

    @Bind(R.id.ThirdChild)
    LinearLayout ThirdChild;

    @Bind(R.id.SixthChild)
    LinearLayout SixthChild_Memo;

    @Bind(R.id.FourthChild)
    LinearLayout FourthChild_LOYALTI;

    @Bind(R.id.StatusTwo)
    TextView StatusTwo_LOYALTI;

    @Bind(R.id.StatusFourth)
    TextView StatusFourth;

    @Bind(R.id.selectBTSAmount)
    TextView selectBTSAmount;

    @Bind(R.id.selectBTSAsset)
    TextView selectBTSAsset;

    @Bind(R.id.selectBTSLoyalty)
    TextView selectBTSLoyalty;

    @Bind(R.id.webviewFrom)
    WebView webviewFrom;

    @Bind(R.id.webviewTo)
    WebView webviewTo;


//    @Bind(R.id.editTextFrom)
//    TextView editTextFrom;

    @Bind(R.id.editTextTo)
    TextView editTextTo;

    @Bind(R.id.checkbox_donate)
    CheckBox checkbox_donate;

    @Bind(R.id.editTextMemo)
    EditText memo_edit;

    @Bind(R.id.editTextAmount)
    EditText editTextAmount;

    @Bind(R.id.editTextAsset)
    EditText editTextAsset;

    @Bind(R.id.spinnerFrom)
    Spinner spinnerFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);
        context = getApplicationContext();
        ButterKnife.bind(this);
        init();
        screenOne();
    }
    void init(){
        setCheckboxAvailabilty();
        setBackUpAsset();
        setSpinner();
    }
    void screenOne(){
        FourthChild_LOYALTI.setVisibility(View.GONE);
        StatusTwo_LOYALTI.setVisibility(View.GONE);
    }
    void screenTwo(){
        FourthChild_LOYALTI.setVisibility(View.GONE);
        StatusTwo_LOYALTI.setVisibility(View.GONE);
       // StatusFourth.setVisibility(View.GONE);
    }
    void screenThree(){
        SixthChild_Memo.setVisibility(View.GONE);
    }
//    @OnTextChanged(R.id.editTextFrom)
//    void onTextChangedFrom(CharSequence text) {
//        if (editTextFrom.getText().length() > 0) {
//            loadWebView(webviewFrom , 34, Helper.md5(editTextFrom.getText().toString()));
//        }
//    }

    @OnTextChanged(R.id.editTextTo)
    void onTextChangedTo(CharSequence text) {
        if (editTextTo.getText().length() > 0) {
            loadWebView(webviewTo , 34, Helper.md5(editTextTo.getText().toString()));
        }
    }

    private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }
    void setCheckboxAvailabilty(){
        if(Helper.fetchBoolianSharePref(this,always_donate)){
            checkbox_donate.setChecked(true);
        }else checkbox_donate.setVisibility(View.GONE);
    }
    void setBackUpAsset(){
        String asset =Helper.fetchStringSharePref(this,backup_asset);
        if(asset!=null) {
            editTextAsset.setText(asset);
        }
    }
    public void popupwindow(View v,TextView textview){
        popUpwindow p =new popUpwindow(context,textview);
        p.show(v);
    }
    @OnClick(R.id.scanning)
    void OnScanning(){
        Intent intent = new Intent(context, qrcodeActivity.class);startActivityForResult(intent,90);
       // HashMap<String,String> hash =  parseStringtoJson("{\"json\":\"{\\\"to\\\":\\\"srk\\\",\\\"to_label\\\":\\\"srk\\\",\\\"currency\\\":\\\"BTS\\\",\\\"memo\\\":\\\"Order: 8f04a475-4c1a-4bb5-a548-b7e1fafaefa7 #sapos\\\",\\\"ruia\\\":\\\"1.3.541\\\",\\\"line_items\\\":[{\\\"label\\\":\\\"Your Purchase\\\",\\\"quantity\\\":1,\\\"price\\\":\\\"1.6977125632925415E8\\\"},{\\\"label\\\":\\\"Donation fee\\\",\\\"quantity\\\":1,\\\"price\\\":\\\"848859.0826970935\\\"}],\\\"note\\\":\\\"\\\",\\\"callback\\\":\\\"http://188.166.147.110:8000/transaction/1.2.88346/8f04a475-4c1a-4bb5-a548-b7e1fafaefa7\\\"}\",\"status\":\"success\"}");
    }
    @OnClick(R.id.selectBTSAmount)
    void onSelectBTSAmount(View v){
        popupwindow(v,selectBTSAmount);
    }
    @OnClick(R.id.imageviewAmount)
    void imageviewAmount(View v){
        popupwindow(v,selectBTSAmount);
    }
    @OnClick(R.id.selectBTSAsset)
    void onSelectBTSAsset(View v){
        popupwindow(v,selectBTSAsset);
    }
    @OnClick(R.id.imageviewAsset)
    void imageviewAsset(View v){
        popupwindow(v,selectBTSAsset);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                     HashMap<String, String>  parseddata = (HashMap<String, String>) res.getSerializable("sResult");
                    onScanResult(parseddata);
                    Log.i("kamal",parseddata+"");
                }
                break;
        }
    }
    void onScanResult(HashMap<String,String> hash){
        editTextTo.setText(hash.get("to"));
       // memo_edit.setText(hash.get("memo"));
        if(hash.get("memo")!=null){
            SixthChild_Memo.setVisibility(View.GONE);
        }else SixthChild_Memo.setVisibility(View.VISIBLE);
        editTextAmount.setText(hash.get("price0")+hash.get("price1"));
        selectBTSAmount.setText(hash.get("currency"));
        String loyaltypoints = hash.get("ruia");
        if(loyaltypoints!=null) {selectBTSLoyalty.setText(loyaltypoints);
            FourthChild_LOYALTI.setVisibility(View.VISIBLE);
            StatusTwo_LOYALTI.setVisibility(View.VISIBLE);
        }
        else {FourthChild_LOYALTI.setVisibility(View.GONE);
            StatusTwo_LOYALTI.setVisibility(View.GONE);}
    }
    public void createSpinner(Spinner spinner){
        List<String> categories = new ArrayList<String>();
        categories.add("Automobile");
        categories.add("Business Services");
        categories.add("Computers");
        categories.add("Education");
        categories.add("Personal");
        categories.add("Travel");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        iniAdapter = dataAdapter;
    }
    void setSpinner(){
        createSpinner(spinnerFrom);
        int setSelection=-1;
        spinnerFrom.setOnItemSelectedListener(new SpinnerActivity(0));
        setSelection = selectionPostion(register_new_account);
        if(setSelection!=-1) spinnerFrom.setSelection(setSelection);
    }
    public int selectionPostion(String compareValue){
        compareValue = Helper.fetchStringSharePref(this,compareValue);
        if (!compareValue.equals(null)) {
            return iniAdapter.getPosition(compareValue);
        }
        return -1;
    }
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        int selectedid;
        SpinnerActivity(int id){
            selectedid = id;
        }
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // On selecting a spinner item
            String item = parent.getItemAtPosition(position).toString();
            loadWebView(webviewFrom , 34, Helper.md5(item));        }
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
}