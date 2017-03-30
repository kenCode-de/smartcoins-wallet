package de.bitsharesmunich.cryptocoincore.smartcoinwallets;

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
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.interfaces.ContactSelectionListener;
import de.bitshares_munich.smartcoinswallet.ContactListAdapter;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.Contact;

/**
 * Created by afnan on 7/5/16.
 */
public class GeneralCoinContactListDialogAdapter extends BaseAdapter {
    public final String TAG = this.getClass().getName();
    private ArrayList<GeneralCoinContactListAdapter.ListviewContactItem> listContact;
    private Context mContext;
    private ContactSelectionListener mClickListener;
    private LayoutInflater mInflater;
    //private TinyDB tinyDB;
    private SCWallDatabase db;
    private Coin coin;

    public GeneralCoinContactListDialogAdapter(Context context, ContactSelectionListener onClickListView) {
        this(context, onClickListView, Coin.BITSHARE);
    }

    public GeneralCoinContactListDialogAdapter(Context context, ContactSelectionListener onClickListView, Coin coin) {
        this.mContext = context;
        mClickListener = onClickListView;
        //tinyDB = new TinyDB(this.mContext);
        db = new SCWallDatabase(this.mContext);
        this.coin = coin;
        mInflater = LayoutInflater.from(this.mContext);
        listContact = getContactList();
    }

    @Override
    public int getCount() {
        return listContact.size();
    }

    @Override
    public Object getItem(int arg0) {
        return listContact.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    private ArrayList<GeneralCoinContactListAdapter.ListviewContactItem> getContactList(){
        ArrayList<GeneralCoinContactListAdapter.ListviewContactItem> contactlist = new ArrayList<GeneralCoinContactListAdapter.ListviewContactItem>();
        GeneralCoinContactListAdapter.ListviewContactItem contact;

        List<Contact> dbContactList = db.getContactsByCoin(this.coin);

        for (Contact nextContact : dbContactList){
            contact = new GeneralCoinContactListAdapter.ListviewContactItem();
            contact.SetName(nextContact.getName());
            contact.SetAccount(nextContact.getAddressByIndex(0).getAddress());
            contact.SaveNote(nextContact.getNote());
            if (!nextContact.getEmail().isEmpty()) {
                contact.isImage = true;
                contact.SaveEmail(nextContact.getEmail());
            }
            contactlist.add(contact);
        }

        /*if (this.coin == Coin.BITSHARE) {
            ArrayList<GeneralCoinContactListAdapter.ListviewContactItem> contacts = tinyDB.getGeneralCoinContactObject("Contacts", GeneralCoinContactListAdapter.ListviewContactItem.class);
            for (int i = 0; i < contacts.size(); i++) {
                contact = new GeneralCoinContactListAdapter.ListviewContactItem();
                contact.SetName(contacts.get(i).name);
                contact.SetAccount(contacts.get(i).account);
                contact.SaveNote(contacts.get(i).note);
                if (!contacts.get(i).email.isEmpty()) {
                    contact.isImage = true;
                    contact.SaveEmail(contacts.get(i).email);
                }
                contactlist.add(contact);
            }
        } else {
            List<Contact> contactsList = db.getContacts();
            Contact nextContact;
            for (int i = 0; i < contactsList.size(); i++) {
                nextContact = contactsList.get(i);
                contact = new GeneralCoinContactListAdapter.ListviewContactItem();
                contact.SetName(nextContact.getName());
                contact.SetAccount(nextContact.getAccount());
                contact.SaveNote(nextContact.getNote());
                contact.SetId(nextContact.getId());
                if (!nextContact.getEmail().isEmpty()) {
                    contact.isImage = true;
                    contact.SaveEmail(nextContact.getEmail());
                }
                contactlist.add(contact);
            }
        }*/
        Collections.sort(contactlist, new ContactNameComparator());

        return contactlist;
    }
    public static class ContactNameComparator implements Comparator<GeneralCoinContactListAdapter.ListviewContactItem> {
        public int compare(GeneralCoinContactListAdapter.ListviewContactItem left, GeneralCoinContactListAdapter.ListviewContactItem right) {
            return left.account.toLowerCase().compareTo(right.account.toLowerCase());
        }
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        Log.d(TAG, "getView. position: "+position+", is image: "+listContact.get(position).isImage);
        if(listContact.get(position).isImage) {
            convertView = mInflater.inflate(R.layout.listview_send_screen_imageview, null);
        }else {
            convertView = mInflater.inflate(R.layout.listview_send_screen_webview, null);
        }

        TextView txtaccount = (TextView) convertView.findViewById(R.id.accountname);
        final String accountnm = listContact.get(position).GetAccount();
        final String contactname = listContact.get(position).GetName();

        //if (this.coin == Coin.BITSHARE) {
        //    txtaccount.setText(accountnm);
        //} else {
            txtaccount.setText(contactname);
        //}

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
                mClickListener.onContactSelected(accountnm);
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
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(result==null) {
                Bitmap corner = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gravtr);
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
