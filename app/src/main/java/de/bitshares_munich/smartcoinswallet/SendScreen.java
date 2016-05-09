package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by afnan on 5/6/16.
 */
public class SendScreen extends Activity{
    Context context;
    Boolean setting=false;
    TextView addnum;
    View view;
    EditText edto;
    static TextView popView;
    Handler handler;
    Boolean editTextset = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_screen);
        context = getApplicationContext();
        view = this.getWindow().getDecorView().findViewById(android.R.id.content);
        popView = (TextView)findViewById(R.id.popS);

        edto = (EditText)findViewById(R.id.edto);
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
              switch (v.getId()){
                  case R.id.d1:
                      break;
                  case R.id.d2:
                      break;
                  default:{setting=false;
                      if (addnum!=null)
                          addnum.setBackgroundResource(0);}
              }
                return false;
            }
        });
//        edto.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                    editTextset = true;
//                                    }
//        });
   //     edto.setFilters(new InputFilter[]{filter});
        edto.addTextChangedListener(mTextEditorWatcher);

        edto.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    Log.i("IME_TEST", "DEL KEY");
                }
                    //check if the right key was pressed

                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        editTextset = false;
                       Log.i("aa","aa");
                        return true;
                    }

//                }
                return false;
            }
        });

        handler = new Handler();
                handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(editTextset){
                    String str = edto.getText().toString();
                    str = str.replace("...", "");
                    if(str.length()>10){
                        str=str+"...";
                        edto.setText(str);
                        edto.setSelection(str.length());
                    }
                    editTextset = false;
                }
                handler.postDelayed(this, 1);

            }
        }, 1);
    }
    void editClick(){

    }
//    InputFilter filter = new InputFilter() {
//        public CharSequence filter(CharSequence source, int start, int end,
//                                   Spanned dest, int dstart, int dend) {
//
//           String str = edto.getText().toString();
//            str = str.replace("...", "");
//            if(str.length()>10){
//                return str+"...";
//            }
////            for (int i = start; i < end; i++) {
////
//////                if ( !Character.isLetterOrDigit(source.charAt(i)) || !Character.toString(source.charAt(i)) .equals("_") || !Character.toString(source.charAt(i)) .equals("-")) {
//////                    return "";
//////                }
////            }
//            return null;
//        }
//    };

        private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        int total;String str;
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        editTextset = true;
        }

        public void afterTextChanged(Editable s) {
        }
    };
    public void popupwindow(View v){
        popUpwindow p =new popUpwindow(context);
        p.show(v);
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
                number = ".";
                break;
        }
        if(setting)
        addNumber(number);
    }
    void addNumber(String number){
//        TextView addnum1 = (TextView)findViewById(R.id.addnum1);
        String addnumG = addnum.getText().toString();
        if(addnumG.equals("000")){
            addnumG = "";
        }
       addnum.setText(addnumG+number);
    }
    public void adding(View v){
        if (addnum!=null)
        addnum.setBackgroundResource(0);

        switch (v.getId()) {
            case R.id.addnum1:
                setting = true;
                addnum = (TextView)findViewById(R.id.addnum1);
                break;
            case R.id.addnum2:
                setting = true;
                addnum = (TextView)findViewById(R.id.addnum2);
                break;
        }

        addnum.setBackgroundDrawable( getResources().getDrawable(R.color.gray) );
    }
    public void backbtn(View v){
        if(setting) {
            String addnumG = addnum.getText().toString();
            if(!addnumG.equals("000")) {

                if (addnumG.length() > 0) {
                    addnumG = method(addnumG);
                }
                if (addnumG.isEmpty()) {
                    addnumG = "000";
                }
                addnum.setText(addnumG);
            }
        }
    }
    public String method(String str) {
            str = str.substring(0, str.length()-1);
        return str;
    }
    public static void popShow(String str){
        popView.setText(str);
    }

}