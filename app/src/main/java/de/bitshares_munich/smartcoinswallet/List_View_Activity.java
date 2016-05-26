package de.bitshares_munich.smartcoinswallet;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.utils.Helper;

/**
 * Created by Syed Muhammad Muzzammil on 5/18/16.
 */
public class List_View_Activity  extends BaseAdapter {
        private static ArrayList<ListviewContactItem> listContact;

        private LayoutInflater mInflater;

        public List_View_Activity(Context photosFragment) {
            ArrayList<ListviewContactItem> results = GetlistContact();
            listContact = results;
            mInflater = LayoutInflater.from(photosFragment);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listContact.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return listContact.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_contacts, null);
                holder = new ViewHolder();
                holder.txtname = (TextView) convertView.findViewById(R.id.username);
                holder.txtphone = (TextView) convertView.findViewById(R.id.accountname);
                holder.webView = (WebView)convertView.findViewById(R.id.webViewContacts);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = listContact.get(position).GetName();
            holder.txtname.setText(name);
            holder.txtphone.setText(listContact.get(position).GetAccount());
            loadWebView(holder.webView , 50, Helper.md5(name));
            return convertView;
        }

        static class ViewHolder {
            TextView txtname, txtphone;
            WebView webView;
        }
    private ArrayList<ListviewContactItem> GetlistContact(){
        ArrayList<ListviewContactItem> contactlist = new ArrayList<ListviewContactItem>();

        ListviewContactItem contact = new ListviewContactItem();

        contact.SetName("Yasir Ibrahim");
        contact.SetAccount("yasir-ibrahim");
        contactlist.add(contact);

        contact = new ListviewContactItem();
        contact.SetName("Jean");
        contact.SetAccount("01213869102");
        contactlist.add(contact);

        contact = new ListviewContactItem();
        contact.SetName("Potter");
        contact.SetAccount("01213123985");
        contactlist.add(contact);

        return contactlist;
    }
    public static class ListviewContactItem{
        String name;
        String account;
        void SetName(String n){
            name = n;
        }
        void SetAccount(String n){
            account = n;
        }
        String GetName(){
            return name;
        }
        String GetAccount(){
            return account;
        }
    }
    private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }
    }