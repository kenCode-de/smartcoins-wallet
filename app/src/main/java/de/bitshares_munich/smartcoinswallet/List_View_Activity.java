package de.bitshares_munich.smartcoinswallet;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.Interfaces.ContactsDelegate;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.Support_Methods;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/18/16.
 */
public class List_View_Activity extends BaseAdapter {
        ArrayList<ListviewContactItem> listContact;
        Context context;
        private LayoutInflater mInflater;
        TinyDB tinyDB;
        public List_View_Activity(Context _context) {
            context = _context;
            tinyDB = new TinyDB(context);
         //   contactsDelegate = _contactsDelegate;
            mInflater = LayoutInflater.from(context);
            listContact = GetlistContact();
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


        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_contacts, null);
                holder = new ViewHolder();
                holder.txtname = (TextView) convertView.findViewById(R.id.username);
                holder.txtaccount = (TextView) convertView.findViewById(R.id.accountname);
                holder.txtnote = (TextView) convertView.findViewById(R.id.note_txt);
                holder.webView = (WebView)convertView.findViewById(R.id.webViewContacts);
                holder.edit = (ImageButton)convertView.findViewById(R.id.editcontact);
                holder.delete = (ImageButton)convertView.findViewById(R.id.deleteitem);
                holder.delete.setTag(position);
                holder.edit.setTag(position);
                holder.edit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Integer index = (Integer) v.getTag();
                        Intent intent = new Intent(context, Add_Edit_Contacts.class);
                        intent.putExtra("id",index);
                        intent.putExtra("name",listContact.get(index).GetName());
                        intent.putExtra("account",listContact.get(index).GetAccount());
                        intent.putExtra("note",listContact.get(index).GetNote());
                        context.startActivity(intent);
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                       // Integer index = (Integer) v.getTag();
//                        listContact.remove(index.intValue());
//                        removeFromlist(index);
                        listContact.remove(position);
                        removeFromlist(position);
                        notifyDataSetChanged();
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = listContact.get(position).GetName();
            String accountnm = listContact.get(position).GetAccount();
            holder.txtname.setText(name);
            holder.txtnote.setText(listContact.get(position).GetNote());
            holder.txtaccount.setText(accountnm);
            loadWebView(holder.webView , 50, Helper.md5(accountnm));
            return convertView;
        }

        static class ViewHolder {
            TextView txtname, txtaccount ,txtnote;
            WebView webView; ImageButton edit;ImageButton delete;
        }

    private ArrayList<ListviewContactItem> GetlistContact(){
        ArrayList<ListviewContactItem> contactlist = new ArrayList<ListviewContactItem>();

        ListviewContactItem contact = new ListviewContactItem();


        ArrayList<ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListviewContactItem.class);
            for (int i = 0; i < contacts.size(); i++) {
                contact = new ListviewContactItem();
                contact.SetName(contacts.get(i).name);
                contact.SetAccount(contacts.get(i).account);
                contact.SaveNote(contacts.get(i).note);
                contactlist.add(contact);
            }

        return contactlist;
    }
    public static class ListviewContactItem{
        String name;
        String account;
        String note;
        void SetName(String n){
            name = n;
        }
        void SetAccount(String n){
            account = n;
        }
        void SaveNote(String n){
            note = n;
        }
        String GetName(){
            return name;
        }
        String GetAccount(){
            return account;
        }
        String GetNote(){
            return note;
        }
    }
    private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }
    void removeFromlist(int id){
        ArrayList<List_View_Activity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", List_View_Activity.ListviewContactItem.class);
        contacts.remove(id);
        tinyDB.putContactsObject("Contacts", contacts);
    }
    public void loadmore(){
        listContact.clear();
    }

    }