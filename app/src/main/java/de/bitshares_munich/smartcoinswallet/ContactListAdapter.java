package de.bitshares_munich.smartcoinswallet;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/18/16.
 */
public class ContactListAdapter extends BaseAdapter {
    private ArrayList<ListviewContactItem> listContact;
    private HashMap<String,Bitmap> images = new HashMap<String,Bitmap>();
    private HashMap<String,Boolean> notEmail = new HashMap<String,Boolean>();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private int pos = 0;
    private Context context;
    private LayoutInflater mInflater;
    private TinyDB tinyDB;

    public ContactListAdapter(Context _context) {
        context = _context;
        tinyDB = new TinyDB(context);
        mInflater = LayoutInflater.from(context);
        listContact = GetlistContact();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
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

        if(listContact.get(position).isImage) {
            convertView = mInflater.inflate(R.layout.list_view_contacts_imageview, null);
        }else {
            convertView = mInflater.inflate(R.layout.list_view_contacts_webview, null);
        }

        TextView username = (TextView) convertView.findViewById(R.id.username);
        TextView txtaccount = (TextView) convertView.findViewById(R.id.accountname);
        ImageButton delete = (ImageButton) convertView.findViewById(R.id.deleteitem);
        TextView txtnote = (TextView) convertView.findViewById(R.id.note_txt);
        String accountnm = listContact.get(position).GetAccount();
        txtaccount.setText(accountnm);
        String name = listContact.get(position).GetName();
        username.setText(name);
        txtnote.setText(listContact.get(position).GetNote());
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(position);
            }
        });

        ImageButton ibEdit = (ImageButton) convertView.findViewById(R.id.editcontact);
        ibEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = position;
                Intent intent = new Intent(context, AddEditContacts.class);
                intent.putExtra("id", index);
                intent.putExtra("name", listContact.get(index).GetName());
                intent.putExtra("account", listContact.get(index).GetAccount());
                intent.putExtra("note", listContact.get(index).GetNote());
                intent.putExtra("email", listContact.get(index).GetEmail());
                context.startActivity(intent);

            }
        });


        if(listContact.get(position).isImage) {
            final ImageView ivEmail = (ImageView) convertView.findViewById(R.id.imageEmail);
            setGravator(listContact.get(position).GetEmail(),ivEmail);
        }else {
            final WebView webView = (WebView) convertView.findViewById(R.id.webViewContacts);
            loadWebView(webView, 40, Helper.hash(accountnm, Helper.SHA256));
        }

        return convertView;
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
                if(!contacts.get(i).email.isEmpty()) {
                    contact.isImage = true;
                    contact.SaveEmail(contacts.get(i).email);
                }
                contactlist.add(contact);
            }
        Collections.sort(contactlist, new ContactNameComparator());

        return contactlist;
    }

    public static class ListviewContactItem{
        String name;
        String email;
        String account;
        String note;
        public Boolean isImage = false;

        void SetName(String n){
            name = n;
        }
        void SetAccount(String n){
            account = n;
        }
        void SaveNote(String n){
            note = n;
        }
        void SaveEmail(String n){
            email = n;
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
        String GetEmail(){
            return email;
        }


    }


        private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }


    void removeFromlist(int id){
        ArrayList<ContactListAdapter.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ContactListAdapter.ListviewContactItem.class);
        contacts.remove(id);
        tinyDB.putContactsObject("Contacts", contacts);
    }
    public void loadmore(){
        pos=0;
        listContact.clear();
    }

    public void showDialog(final int position){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_confirmation_dialog);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        btnDone.setText(context.getString(R.string.delete));
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        TextView textView = (TextView) dialog.findViewById(R.id.alertMsg);


        String alertMsg =  context.getString(R.string.delete);
        String accountName = listContact.get(position).GetAccount();
        alertMsg = alertMsg + " \"" + accountName + "\" ?";
        textView.setText(alertMsg);

        Log.e("Error", alertMsg);


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
        dialog.show();


    }

    public static class ContactNameComparator implements Comparator<ContactListAdapter.ListviewContactItem>
    {
        public int compare(ContactListAdapter.ListviewContactItem left, ContactListAdapter.ListviewContactItem right) {
            return left.name.toLowerCase().compareTo(right.name.toLowerCase());
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView _bmImage) {
            bmImage = _bmImage;
       }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
                SupportMethods.testing("alpha",e.getMessage(),"error");
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(result==null) {
                Bitmap corner = BitmapFactory.decodeResource(context.getResources(), R.drawable.gravtr);
                bmImage.setImageBitmap(getRoundedCornerBitmap(corner));
            }
            else {
                Bitmap corner = getRoundedCornerBitmap(result);
                bmImage.setImageBitmap(corner);
            }
        }
    }
    void setGravator(String email,ImageView imageView){
            String emailGravatarUrl = "https://www.gravatar.com/avatar/" + Helper.hash(email, Helper.MD5) + "?s=130&r=pg&d=404";
            new DownloadImageTask(imageView)
                    .execute(emailGravatarUrl);

    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 90;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


}
