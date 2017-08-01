package de.bitsharesmunich.cryptocoincore.smartcoinwallets;

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
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.AddEditContacts;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.Contact;

/**
 * Adapter for the list of contacts showed in the contact fragment
 */
public class GeneralCoinContactListAdapter extends BaseAdapter {
    private ArrayList<ListviewContactItem> listContact; /**< contacts view list*/
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private int pos = 0;
    private Context context;
    private LayoutInflater mInflater;
    private TinyDB tinyDB; /**< Manages the bitshares contacts info*/
    private SCWallDatabase db; /**< Manages contacts info of all the coin types*/
    private Coin coin;

    public GeneralCoinContactListAdapter(Context _context) {
        this(_context, Coin.BITSHARE);
    }

    public GeneralCoinContactListAdapter(Context _context, Coin coin) {
        this.coin = coin;

        context = _context;
        tinyDB = new TinyDB(context);
        db = new SCWallDatabase(context);
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


    /**
     * Creates a contact view in the list of contacts.
     *
     * @param position the index of the contact in the list
     * @param convertView the view to return constructed
     * @param parent the parent view of the convertView
     * @return a view of the contact in the given position
     */
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

        //Creates the delete option
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(position);
            }
        });

        //Creates the edit option
        ImageButton ibEdit = (ImageButton) convertView.findViewById(R.id.editcontact);
        ibEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = position;
                Intent intent = new Intent(context, AddEditContacts.class);

                intent.putExtra("coin", coin.name());

                intent.putExtra("id", listContact.get(index).GetId());
                intent.putExtra("name", listContact.get(index).GetName());
                intent.putExtra("account", listContact.get(index).GetAccount());
                intent.putExtra("note", listContact.get(index).GetNote());
                intent.putExtra("email", listContact.get(index).GetEmail());
                context.startActivity(intent);

            }
        });

        //If the email of the contact is valid then the image could be retrieved
        if(listContact.get(position).isImage) {
            final ImageView ivEmail = (ImageView) convertView.findViewById(R.id.imageEmail);
            setGravator(listContact.get(position).GetEmail(),ivEmail);
        }else {
            //final WebView webView = (WebView) convertView.findViewById(R.id.webViewContacts);
            //loadWebView(webView, 40, Helper.hash(accountnm, Helper.SHA256));
        }

        return convertView;
    }

    /**
     * Initializes the contacts list data
     *
     * @return an array of user contacts as list items
     */
    private ArrayList<ListviewContactItem> GetlistContact(){
        ArrayList<ListviewContactItem> contactlist = new ArrayList<ListviewContactItem>();
        ListviewContactItem contact;

        List<Contact> contactsList = db.getContacts();
        Contact nextContact;
        for (int i = 0; i < contactsList.size(); i++) {
            nextContact = contactsList.get(i);
            contact = new ListviewContactItem();
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

        Collections.sort(contactlist, new ContactNameComparator());
        return contactlist;
    }

    /**
     * Represents an item in the list view of the contacts
     */
    public static class ListviewContactItem {
        public String name;
        public String email;
        public String account;
        public String note;
        public Boolean isImage = false;
        public long id;

        public void SetName(String n){
            name = n;
        }
        public void SetAccount(String n){
            account = n;
        }
        public void SaveNote(String n){
            note = n;
        }
        public void SaveEmail(String n){
            email = n;
        }
        public void SetId(long n){
            id = n;
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
        long GetId(){
            return id;
        }
    }


    private void loadWebView(WebView webView , int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html { margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }


    /**
     * Removes from the contacts list a contact with a specific id
     * @param id id of the contact to remove
     */
    void removeFromlist(long id){
        if (coin == Coin.BITSHARE) {
            ArrayList<GeneralCoinContactListAdapter.ListviewContactItem> contacts = tinyDB.getGeneralCoinContactObject("Contacts", GeneralCoinContactListAdapter.ListviewContactItem.class);
            contacts.remove(id);
            tinyDB.putGeneralCoinContactsObject("Contacts", contacts);
        } else {
            Contact contact = new Contact();
            contact.setId(id);
            db.removeContact(contact);
        }
    }

    public void loadmore(){
        pos=0;
        listContact.clear();
    }

    /**
     * Shows a dialog to confirm the elimination of a contact to the user
     * @param position the index of the contact to remove
     */
    public void showDialog(final int position){
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.alert_confirmation_dialog);
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
                        if (coin == Coin.BITSHARE) {
                            listContact.remove(position);
                            removeFromlist(position);
                        } else {
                            long id = listContact.get(position).GetId();
                            listContact.remove(position);
                            removeFromlist(id);
                        }
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

    public static class ContactNameComparator implements Comparator<GeneralCoinContactListAdapter.ListviewContactItem>
    {
        public int compare(GeneralCoinContactListAdapter.ListviewContactItem left, GeneralCoinContactListAdapter.ListviewContactItem right) {
            return left.name.toLowerCase().compareTo(right.name.toLowerCase());
        }
    }

    /**
     * downloads the image of a contact gravatar in an asynchronous manner
     */
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
                Bitmap corner = BitmapFactory.decodeResource(context.getResources(), R.drawable.gravtr);
                bmImage.setImageBitmap(getRoundedCornerBitmap(corner));
            }
            else {
                Bitmap corner = getRoundedCornerBitmap(result);
                bmImage.setImageBitmap(corner);
            }
        }
    }

    /**
     * set the gravatar image of a user contact
     * @param email the contact email associated with the gravatar
     * @param imageView the image view container to put the image
     */
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
