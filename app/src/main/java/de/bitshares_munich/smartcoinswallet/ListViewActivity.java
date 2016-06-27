package de.bitshares_munich.smartcoinswallet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/18/16.
 */
public class ListViewActivity extends BaseAdapter {
        ArrayList<ListviewContactItem> listContact;
        Context context;
        private LayoutInflater mInflater;
        TinyDB tinyDB;
        public ListViewActivity(Context _context) {
            context = _context;
            tinyDB = new TinyDB(context);
            mInflater = LayoutInflater.from(context);
            listContact = GetlistContact();
            Collections.sort(listContact, new ContactNameComparator());
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


        public View getView(final int position, View convertView, final ViewGroup parent) {
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
                        Intent intent = new Intent(context, AddEditContacts.class);
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
                        showDialog(position);

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
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        contacts.remove(id);
        tinyDB.putContactsObject("Contacts", contacts);
    }
    public void loadmore(){
        listContact.clear();
    }

    public void showDialog(final int position){
                final Dialog dialog = new Dialog(context);
                //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
//                dialog.setTitle(R.string.pin_verification);
                dialog.setContentView(R.layout.alert_delete_dialog);
                Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
                Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
                TextView textView = (TextView) dialog.findViewById(R.id.alertMsg);
        String alertMsg =  context.getString(R.string.delete);
        String accountName = listContact.get(position).GetAccount();
            alertMsg = alertMsg + " \"" + accountName + "\" ?";
        textView.setText(alertMsg);

        btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listContact.remove(position);
                        removeFromlist(position);
                        notifyDataSetChanged();
                        dialog.cancel();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                      public void onClick(View v) {
                            dialog.cancel();
                         }
                   });
//                dialog.setCancelable(false);

                dialog.show();


    }

    public static class ContactNameComparator implements Comparator<ListViewActivity.ListviewContactItem>
    {
        public int compare(ListViewActivity.ListviewContactItem left, ListViewActivity.ListviewContactItem right) {
            return left.name.toLowerCase().compareTo(right.name.toLowerCase());
        }
    }

    }