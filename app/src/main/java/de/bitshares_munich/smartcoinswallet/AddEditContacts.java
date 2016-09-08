package de.bitshares_munich.smartcoinswallet;

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

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.ContactsDelegate;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;

/**
 * Created by Syed Muhammad Muzzammil on 5/25/16.
 */
public class AddEditContacts extends BaseActivity implements IAccount {
    Boolean add = false;
    Boolean edit = false;
    TinyDB tinyDB;
    String contactname = "";
    String emailtxt = "";
    String accountid = "";
    String note = "";

    boolean validReceiver = false;

    @Bind(R.id.web)
    WebView web;

    @Bind(R.id.imageEmail)
    ImageView imageEmail;

    @Bind(R.id.Contactname)
    EditText Contactname;

    @Bind(R.id.SaveContact)
    Button SaveContact;

    @Bind(R.id.CancelContact)
    Button cancelContact;

    @Bind(R.id.note)
    EditText Note;

    @Bind(R.id.Accountname)
    EditText Accountname;

    @Bind(R.id.email)
    EditText etEmail;

    Context context;
    String contact_id;

    @Bind(R.id.warning)
    TextView warning;

    @Bind(R.id.emailHead)
    TextView emailHead;

    @Bind(R.id.tvWarningEmail)
    TextView tvWarningEmail;

    ContactsDelegate contactsDelegate;
    webSocketCallHelper myWebSocketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contacts);
        ButterKnife.bind(this);

        setBackButton(true);


        context = this;
        tinyDB = new TinyDB(context);
        Application.registerCallback(this);

        myWebSocketHelper = new webSocketCallHelper(this);

        contactsDelegate = ContactsFragment.contactsDelegate;
        loadWebView(39, Helper.hash("", Helper.SHA256));

        emailHead.setText(context.getString(R.string.email_name) + " :");
        SaveContact.setEnabled(false);
        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
        cancelContact.setBackgroundColor(getColorWrapper(context, R.color.red));
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if (res != null) {
            if (res.containsKey("activity")) {
                if (res.getInt("activity") == 99999) {
                    add = true;
                    setTitle(getResources().getString(R.string.add_contact_activity_name));
                    SaveContact.setText(R.string.add_contact);
                }
            } else if (res.containsKey("id")) {

                edit = true;
                setTitle(getResources().getString(R.string.edit_contact_activity_name));

                contact_id = Integer.toString(res.getInt("id"));

                if (res.containsKey("name")) contactname = res.getString("name");

                if (res.containsKey("account")) accountid = res.getString("account");

                if (res.containsKey("note")) note = res.getString("note");

                if (res.containsKey("email")) emailtxt = res.getString("email");

                Contactname.setText(contactname);
                Accountname.setText(accountid);
                Note.setText(note);
                SaveContact.setText(R.string.edit_contact);
                etEmail.setText(emailtxt);
                setOnEmail();
            }

        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Boolean contactNameEnabled = false;
                Boolean noteEnabled = false;
                Boolean emailEnabled = false;
                //Do something after 100ms
                if (edit && validReceiver) {
                    if (Contactname.getText().toString().equals(contactname)) {
                        contactNameEnabled = false;
                    } else {
                        contactNameEnabled = true;
                    }
                    if (Note.getText().toString().equals(note)) {
                        noteEnabled = false;
                    } else {
                        noteEnabled = true;
                    }
                    if (etEmail.getText().toString().equals(emailtxt)) {
                        emailEnabled = false;
                    } else {
                        emailEnabled = true;
                    }
                    if (contactNameEnabled || noteEnabled || emailEnabled) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                        SaveContact.setEnabled(true);
                    }
                    if (!contactNameEnabled && !noteEnabled && !emailEnabled) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                        SaveContact.setEnabled(false);
                    }
                    if (!Accountname.getText().toString().equals(accountid) && validReceiver) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                        SaveContact.setEnabled(true);
                    }
                }
                if (add) {
                    if (validReceiver) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                        SaveContact.setEnabled(true);
                    } else {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                        SaveContact.setEnabled(false);
                    }
                }
                if (Accountname.getText().length() == 0) {
                    SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                    SaveContact.setEnabled(false);
                }

                ColorDrawable buttonColor = (ColorDrawable) SaveContact.getBackground();
                int colorId = buttonColor.getColor();
                if (colorId == R.color.green) {
                    SaveContact.setEnabled(true);
                }
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    @OnClick(R.id.SaveContact)
    public void AddContatcs() {
        ListViewActivity.ListviewContactItem contact = new ListViewActivity.ListviewContactItem();
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        String _contactname = Contactname.getText().toString();
        String _accountid = Accountname.getText().toString();
        String _note = Note.getText().toString();
        String _email = etEmail.getText().toString();
        if (!SupportMethods.isEmailValid(_email)) {
            _email = "";
        }

        if (add) {
            contact.SaveNote(_note);
            contact.SetName(_contactname);
            contact.SetAccount(_accountid);
            contact.SaveEmail(_email);
            contacts.add(contact);
            Collections.sort(contacts, new ContactNameComparator());
            tinyDB.putContactsObject("Contacts", contacts);
        } else if (edit) {
            if (!_contactname.equals(contactname))
                contacts.get(Integer.parseInt(contact_id)).SetName(_contactname);
            if (!_accountid.equals(accountid))
                contacts.get(Integer.parseInt(contact_id)).SetAccount(_accountid);
            if (!_note.equals(note)) contacts.get(Integer.parseInt(contact_id)).SaveNote(_note);
            if (!_email.equals(emailtxt))
                contacts.get(Integer.parseInt(contact_id)).SaveEmail(_email);
            Collections.sort(contacts, new ContactNameComparator());
            tinyDB.putContactsObject("Contacts", contacts);
        }
        contactsDelegate.OnUpdate("knysys", 29);
        finish();
    }

    Boolean checkIfAlreadyAdded() {
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        String _accountid = Accountname.getText().toString();

        for (int i = 0; i < contacts.size(); i++) {

            if (contacts.get(i).account.equals(_accountid)) {
                return true;
            }

        }


        return false;
    }

    private void loadWebView(int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    @OnTextChanged(R.id.Accountname)
    void onTextChangedTo(CharSequence text) {
        loadWebView(39, Helper.hash(Accountname.getText().toString(), Helper.SHA256));
        warning.setText(getString(R.string.txt_validating_account));
        warning.setTextColor(getColorWrapper(context, R.color.black));
        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
        SaveContact.setEnabled(false);

        if (!text.toString().equals(text.toString().trim())) {
            Accountname.setText(text.toString().trim());
        }

        if (!Accountname.getText().toString().equals(accountid)) {
            if (Accountname.getText().length() > 0) {

                validReceiver = false;

                loadWebView(39, Helper.hash(Accountname.getText().toString(), Helper.SHA256));


                myLowerCaseTimer.cancel();
                myAccountNameValidationTimer.cancel();
                myLowerCaseTimer.start();
                myAccountNameValidationTimer.start();
            }
        } else {
            warning.setText("");
        }

    }

    @OnTextChanged(R.id.Contactname)
    void onTextChangedName(CharSequence text) {
        if (edit) {
            if (Contactname.getText().toString().equals(contactname)) {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                SaveContact.setEnabled(false);
            } else {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                SaveContact.setEnabled(true);
            }
        }
    }

    @OnTextChanged(R.id.note)
    void onTextChangedNote(CharSequence text) {
        if (edit) {
            if (Note.getText().toString().equals(note)) {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                SaveContact.setEnabled(false);
            } else {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                SaveContact.setEnabled(true);
            }
        }
    }

    void setOnEmail() {
        if (etEmail.getText().toString().length() > 0) {
            if (SupportMethods.isEmailValid(etEmail.getText().toString())) {
                imageEmail.setVisibility(View.VISIBLE);
                web.setVisibility(View.GONE);
                tvWarningEmail.setText("");
                setGravator(etEmail.getText().toString(), imageEmail);
            } else {
                tvWarningEmail.setText(getString(R.string.in_valid_email));
                tvWarningEmail.setTextColor(getColorWrapper(context, R.color.red));
            }
        }
        if (etEmail.getText().toString().length() <= 0) {
            imageEmail.setVisibility(View.GONE);
            web.setVisibility(View.VISIBLE);
            tvWarningEmail.setText("");
        }

        if (edit) {
            if (etEmail.getText().toString().equals(emailtxt)) {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                SaveContact.setEnabled(false);
            } else {
                SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                SaveContact.setEnabled(true);
            }
        }
    }

    @OnFocusChange(R.id.email)
    void onTextFocusChangedEmail(boolean hasFocus) {
        setOnEmail();
    }

    @OnTextChanged(R.id.email)
    void onTextChangedEmail() {
        String sEmail = etEmail.getText().toString();

        boolean hasSpecial = !sEmail.equals(sEmail.toLowerCase());

        if (hasSpecial) {
            etEmail.setText("");
            etEmail.append(sEmail.toLowerCase());
        }
        if (!etEmail.getText().toString().equals(etEmail.getText().toString().trim())) {
            etEmail.setText(etEmail.getText().toString().trim());
        }
    }

    @OnClick(R.id.CancelContact)
    public void Cancel() {
        finish();
    }

    CountDownTimer myLowerCaseTimer = new CountDownTimer(500, 500) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            if (!Accountname.getText().toString().equals(Accountname.getText().toString().toLowerCase())) {
                Accountname.setText(Accountname.getText().toString().toLowerCase());
                Accountname.setSelection(Accountname.getText().toString().length());
            }
        }
    };
    CountDownTimer myAccountNameValidationTimer = new CountDownTimer(3000, 1000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            createBitShareAN(false);
        }
    };

    public void createBitShareAN(boolean focused) {
        if (!focused) {
            if (Accountname.getText().length() > 2) {
                if (!checkIfAlreadyAdded()) {
                    String socketText = getString(R.string.lookup_account_a);
                    String socketText2 = getString(R.string.lookup_account_b) + "\"" + Accountname.getText().toString() + "\"" + ",50]],\"id\": 6}";
                    myWebSocketHelper.make_websocket_call(socketText, socketText2, webSocketCallHelper.api_identifier.database);
                } else {
                    warning.setText(Accountname.getText().toString() + " " + getString(R.string.is_already_added));
                    warning.setTextColor(getColorWrapper(context, R.color.red));
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
                loadWebView(39, Helper.hash(Accountname.getText().toString(), Helper.SHA256));
                warning.setText("");
                SaveContact.setEnabled(false);
                SaveContact.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        }
    }

    @Override
    public void checkAccount(JSONObject jsonObject) {
        myWebSocketHelper.cleanUpTransactionsHandler();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            boolean found = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                final String temp = jsonArray.getJSONArray(i).getString(0);
                if (temp.equals(Accountname.getText().toString())) {
                    found = true;
                    validReceiver = true;
                }
            }
            if (found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Accountname.getText().toString().equals(accountid)) {
                            SaveContact.setEnabled(false);
                            SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                        } else {
                            SaveContact.setEnabled(true);
                            SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                            warning.setText(R.string.account_name_validate);
                            warning.setVisibility(View.VISIBLE);
                            warning.setTextColor(getColorWrapper(context, R.color.black));
                        }
                    }
                });
            }
            if (!found) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        validReceiver = false;
                        try {
                            String acName = getString(R.string.account_name_not_exist);
                            String format = String.format(acName.toString(), Accountname.getText().toString());
                            warning.setText(format);
                        } catch (Exception e) {
                            warning.setText("");
                        }
                        SaveContact.setEnabled(false);
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));

                        warning.setVisibility(View.VISIBLE);
                        warning.setTextColor(getColorWrapper(context, R.color.red));


                    }
                });
            }
        } catch (Exception e) {

        }
    }

    public static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
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
                SupportMethods.testing("alpha", e.getMessage(), "error");
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                bmImage.setVisibility(View.GONE);
                web.setVisibility(View.VISIBLE);
            } else {
                Bitmap corner = getRoundedCornerBitmap(result);
                bmImage.setImageBitmap(corner);
            }
        }
    }

    void setGravator(String email, ImageView imageEmail) {
        String emailGravatarUrl = "https://www.gravatar.com/avatar/" + Helper.hash(email, Helper.MD5) + "?s=130&r=pg&d=404";
        new DownloadImageTask(imageEmail)
                .execute(emailGravatarUrl);
    }

    public static class ContactNameComparator implements Comparator<ListViewActivity.ListviewContactItem> {
        public int compare(ListViewActivity.ListviewContactItem left, ListViewActivity.ListviewContactItem right) {
            return left.name.toLowerCase().compareTo(right.name.toLowerCase());
        }
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
