package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.bitshares_munich.Interfaces.OnClickListView;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by afnan on 7/5/16.
 */
public class SendScreenListViewActivity extends BaseAdapter {
    ArrayList<ContactListAdapter.ListviewContactItem> listContact;
    Context context;
    OnClickListView onClickListView;

    private LayoutInflater mInflater;
    TinyDB tinyDB;
    public SendScreenListViewActivity(Context _context, OnClickListView _onClickListView) {
        context = _context;
        onClickListView = _onClickListView;
        tinyDB = new TinyDB(context);
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

    private ArrayList<ContactListAdapter.ListviewContactItem> GetlistContact(){
        ArrayList<ContactListAdapter.ListviewContactItem> contactlist = new ArrayList<ContactListAdapter.ListviewContactItem>();

        ContactListAdapter.ListviewContactItem contact = new ContactListAdapter.ListviewContactItem();

        ArrayList<ContactListAdapter.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ContactListAdapter.ListviewContactItem.class);
        for (int i = 0; i < contacts.size(); i++) {
            contact = new ContactListAdapter.ListviewContactItem();
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
    public static class ContactNameComparator implements Comparator<ContactListAdapter.ListviewContactItem>
    {
        public int compare(ContactListAdapter.ListviewContactItem left, ContactListAdapter.ListviewContactItem right) {
            return left.account.toLowerCase().compareTo(right.account.toLowerCase());
        }
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

        if(listContact.get(position).isImage) {
            convertView = mInflater.inflate(R.layout.listview_send_screen_imageview, null);
        }else {
            convertView = mInflater.inflate(R.layout.listview_send_screen_webview, null);
        }

        TextView txtaccount = (TextView) convertView.findViewById(R.id.accountname);
        final  String accountnm = listContact.get(position).GetAccount();
        txtaccount.setText(accountnm);

        if(listContact.get(position).isImage) {
            final ImageView ivEmail = (ImageView) convertView.findViewById(R.id.imageEmail);
            setGravator(listContact.get(position).GetEmail(),ivEmail);
        }else {
            final WebView webView = (WebView) convertView.findViewById(R.id.webViewContacts);
            loadWebView(webView, 40, Helper.hash(accountnm, Helper.SHA256));
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListView.isClicked(accountnm);
            }
        });



        return convertView;
    }
    private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
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
