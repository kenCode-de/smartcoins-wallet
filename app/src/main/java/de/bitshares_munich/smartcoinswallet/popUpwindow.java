package de.bitshares_munich.smartcoinswallet;

/**
 * Created by Syed Muhammad Muzzammil on 5/9/16.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class popUpwindow {

    String popUpContents[];
    PopupWindow popupWindow;
    Context context;
    TextView textView;

    popUpwindow(Context c,TextView textview) {
        context = c;
        List<String> optionsList = new ArrayList<String>();
        optionsList.add("BTS::1");
        optionsList.add("CNY::2");
        optionsList.add("EUR::3");
        optionsList.add("USD::4");
        textView = textview;
        popUpContents = new String[optionsList.size()];

        optionsList.toArray(popUpContents);

        popupWindow = popupWindow();
//add
    }

    public PopupWindow popupWindow() {

        // initialize a pop up window type
        PopupWindow popupWindow = new PopupWindow(context);

        // the drop down list is a list view
        ListView listView = new ListView(context);

        // set our adapter and pass our pop up window contents
        listView.setAdapter(optionsAdapter(popUpContents));

        // set the item click listener
        listView.setOnItemClickListener(new DropdownOnItemClickListener());

        listView.setBackgroundColor(Color.WHITE);

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setWidth(350);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(listView);

        return popupWindow;
    }


    private ArrayAdapter<String> optionsAdapter(String sArray[]) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, sArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String item = getItem(position);
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                String id = itemArr[1];

                // visual settings for the list item
                TextView listItem = new TextView(context);
                listItem.setTextColor(Color.BLACK);
                listItem.setGravity(Gravity.CENTER);
                listItem.setText(text);
                listItem.setTag(id);
                listItem.setTextSize(18);
                listItem.setPadding(10, 10, 10, 10);

                return listItem;
            }
        };

        return adapter;
    }


    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

            // get the context and main activity to access variables
            // add some animation when a list item was clicked
            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            v.startAnimation(fadeInAnimation);

            // dismiss the pop up
            popupWindow.dismiss();


            // get the text and set it as the button text
            String selectedItemText = ((TextView) v).getText().toString();
//
           //SendScreen.popShow(selectedItemText);
            textView.setText(selectedItemText);
        }
    }
    public void show(View v){

        popupWindow.showAsDropDown(v, -5, 0);
    }



    //inialize in other activity like this
//    popupwindow(v,selectBTSAmount);


//    public void popupwindow(View v,TextView textview){
//        popUpwindow p =new popUpwindow(this,textview);
//        p.show(v);
//    }

}