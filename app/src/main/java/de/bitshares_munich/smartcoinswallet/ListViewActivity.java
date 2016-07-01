package de.bitshares_munich.smartcoinswallet;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/18/16.
 */
public class ListViewActivity extends BaseAdapter {
        ArrayList<ListviewContactItem> listContact;
        HashMap<String,Bitmap> images = new HashMap<String,Bitmap>();
    HashMap<String,Boolean> notEmail = new HashMap<String,Boolean>();
    ImageLoader imageLoader = ImageLoader.getInstance();


    int pos = 0;
    Context context;


        private LayoutInflater mInflater;
        TinyDB tinyDB;
        public ListViewActivity(Context _context) {
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


//        public View getView(final int position, View convertView, final ViewGroup parent) {
//            if (convertView == null)
//                convertView = mInflater.inflate(R.layout.listview_contacts, null);
//
//            if (imageLoader == null) {
//                imageLoader = ImageLoader.getInstance();
//                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
//            }
//
//
//            TextView username = (TextView) convertView.findViewById(R.id.username);
//            TextView txtaccount = (TextView) convertView.findViewById(R.id.accountname);
//            ImageButton delete = (ImageButton) convertView.findViewById(R.id.deleteitem);
//            TextView txtnote = (TextView) convertView.findViewById(R.id.note_txt);
//            ImageButton ibEdit = (ImageButton) convertView.findViewById(R.id.editcontact);
//
//            String accountnm = listContact.get(position).GetAccount();
//            txtaccount.setText(accountnm);
//
//            String name = listContact.get(position).GetName();
//            username.setText(name);
//
//            txtnote.setText(listContact.get(position).GetNote());
//
//            final WebView webView = (WebView) convertView.findViewById(R.id.webViewContacts);
//            loadWebView(webView , 40, Helper.hash(accountnm, Helper.SHA256));
//
//
//            final ImageView ivEmail = (ImageView) convertView.findViewById(R.id.imageEmail);
//            ivEmail.setImageBitmap(null);
//
//            String emailGravatarUrl = "https://www.gravatar.com/avatar/" + Helper.hash(listContact.get(position).GetEmail(), Helper.MD5) + "?s=130&r=pg&d=404";
//            imageLoader.loadImage(emailGravatarUrl, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String imageUri, View view) {
//                    if (!listContact.get(position).GetEmail().isEmpty()) {
//                        webView.setVisibility(View.GONE);
//                    }
//                }
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    Bitmap corner = getRoundedCornerBitmap(loadedImage);
//                    ivEmail.setImageBitmap(corner);
//                    ivEmail.setVisibility(View.VISIBLE);
//
//                }
//            });
//
//
//            delete.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//
//                        showDialog(position);
//
//                    }
//                });
//
//
//            ibEdit.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        int index = position;
//                        Intent intent = new Intent(context, AddEditContacts.class);
//                        intent.putExtra("id", index);
//                        intent.putExtra("name", listContact.get(index).GetName());
//                        intent.putExtra("account", listContact.get(index).GetAccount());
//                        intent.putExtra("note", listContact.get(index).GetNote());
//                        intent.putExtra("email", listContact.get(index).GetEmail());
//                        context.startActivity(intent);
//
//                    }
//                });
//
//            return convertView;
//        }

    public View getView(final int position, View convertView, final ViewGroup parent) {

//        if (convertView == null) {
            if(listContact.get(position).isImage) {
                convertView = mInflater.inflate(R.layout.list_view_contacts_imageview, null);
            }else {
                convertView = mInflater.inflate(R.layout.list_view_contacts_webview, null);
            }
        //}

//        if (imageLoader == null) {
//            imageLoader = ImageLoader.getInstance();
//            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
//        }


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
//            String emailGravatarUrl = "https://www.gravatar.com/avatar/" + Helper.hash(listContact.get(position).GetEmail(), Helper.MD5) + "?s=130&r=pg&d=404";
//            imageLoader.loadImage(emailGravatarUrl, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String imageUri, View view) {
//                }
//
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    Bitmap corner = getRoundedCornerBitmap(loadedImage);
//                    ivEmail.setImageBitmap(corner);
//                }
//            });
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
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        contacts.remove(id);
        tinyDB.putContactsObject("Contacts", contacts);
    }
    public void loadmore(){
        pos=0;
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
        final float roundPx = 20;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


}
