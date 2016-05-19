package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Syed Muhammad Muzzammil on 5/17/16.
 */
public class RequestActivity extends Activity {
    @Bind(R.id.editTextView)
    TextView editTextView;

    @Bind(R.id.popwin1)
    TextView popwin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_screen);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.backbutton)
    void onBackButtonPressed(){
        super.onBackPressed();
    }

    public void digitClick(View v){
        String number="";
        switch (v.getId()){
            case R.id.one:
                number = "1";
                break;
            case R.id.two:
                number = "2";
                break;
            case R.id.three:
                number = "3";
                break;
            case R.id.four:
                number = "4";
                break;
            case R.id.five:
                number = "5";
                break;
            case R.id.six:
                number = "6";
                break;
            case R.id.seven:
                number = "7";
                break;
            case R.id.eight:
                number = "8";
                break;
            case R.id.nine:
                number = "9";
                break;
            case R.id.zero:
                number = "0";
                break;
            case R.id.doublezero:
                number = "00";
                break;
            case R.id.dot:
                if(!editTextView.getText().toString().contains("."))
                number = ".";
                break;
        }
            addNumber(number);
    }

    void addNumber(String number){
//        TextView addnum1 = (TextView)findViewById(R.id.addnum1);
        String addnumG = editTextView.getText().toString();
        if(addnumG.equals("000")){
            addnumG = "";
        }
        addnumG = addnumG+number;
        editTextView.setText(addnumG);
    }
    public void backbtn(View v){

            String addnumG = editTextView.getText().toString();
            if(!addnumG.equals("000")) {

                if (addnumG.length() > 0) {
                    addnumG = method(addnumG);
                }
                if (addnumG.isEmpty()) {
                    addnumG = "000";
                }
                editTextView.setText(addnumG);
            }
    }
    public String method(String str) {
        str = str.substring(0, str.length()-1);
        return str;
    }
        public void popupwindow(View v,TextView textview){
        popUpwindow p =new popUpwindow(this,textview);
        p.show(v);
    }
    public void showpop(View v){
        popupwindow(v,popwin);
    }

}