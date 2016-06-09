package de.bitshares_munich.smartcoinswallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.bitshares_munich.Interfaces.ContactsDelegate;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.TinyDB;

/**
 * Created by Syed Muhammad Muzzammil on 5/25/16.
 */
public class AddEditContacts extends BaseActivity implements IAccount{
    Boolean add = false;
    Boolean edit = false;
    TinyDB tinyDB;
    String contactname = "";
    String accountid = "";
    String note = "";
    Application application = new Application();

    boolean validReceiver = false;

    @Bind(R.id.web)
    WebView web;

    @Bind(R.id.Contactname)
    EditText Contactname;

    @Bind(R.id.SaveContact)
    Button SaveContact;

    @Bind(R.id.note)
    EditText Note;

    @Bind(R.id.Accountname)
    EditText Accountname;

    Context context;
    String contact_id;

    @Bind(R.id.warning)
    TextView warning;

    ContactsDelegate contactsDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contacts);
        ButterKnife.bind(this);

        setBackButton(true);
        setTitle(getResources().getString(R.string.add_contact_activity_name));

        context = this;
        tinyDB = new TinyDB(context);
        application.registerCallback(this);
       this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        contactsDelegate = ContactsFragment.contactsDelegate;
        loadWebView(39, Helper.md5(""));
        SaveContact.setEnabled(false);
        SaveContact.setBackgroundColor(getResources().getColor(R.color.gray));
        Intent intent = getIntent();
        Bundle res = intent.getExtras();
        if(res!=null) {
            if (res.containsKey("activity")) {
                if(res.getInt("activity")==99999){
                    add = true;
                    SaveContact.setText(R.string.add_contact);
                }
            }else if(res.containsKey("id")) {

                edit = true;

                contact_id = Integer.toString(res.getInt("id"));

                if(res.containsKey("name")) contactname = res.getString("name");

                if(res.containsKey("account")) accountid = res.getString("account");

                if(res.containsKey("note")) note = res.getString("note");

                Contactname.setText(contactname);
                Accountname.setText(accountid);
                Note.setText(note);
                SaveContact.setText(R.string.edit_contact);
            }

           // if (res.containsKey("interface")) contactsDelegate =  (ContactsDelegate) res.getSerializable("interface");
        }

    }
    @OnClick(R.id.SaveContact)
    public void AddContatcs(){
        ListViewActivity.ListviewContactItem contact = new ListViewActivity.ListviewContactItem();
        ArrayList<ListViewActivity.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ListViewActivity.ListviewContactItem.class);
        String _contactname = Contactname.getText().toString();
        String _accountid = Accountname.getText().toString();
        String _note = Note.getText().toString();
        if(add){
            contact.SaveNote(_note);
            contact.SetAccount(_accountid);
            contact.SetName(_contactname);
            contacts.add(contact);
            tinyDB.putContactsObject("Contacts", contacts);
        }else if (edit){
            if(!_contactname.equals(contactname)) contacts.get(Integer.parseInt(contact_id)).SetName(_contactname);
            if(!_accountid.equals(accountid)) contacts.get(Integer.parseInt(contact_id)).SetAccount(_accountid);
            if(!_note.equals(note)) contacts.get(Integer.parseInt(contact_id)).SaveNote(_note);
            tinyDB.putContactsObject("Contacts", contacts);
        }
        contactsDelegate.OnUpdate("knysys",29);
        finish();
    }
    private void loadWebView(int size, String encryptText) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }
    @OnTextChanged(R.id.Accountname)
    void onTextChangedTo(CharSequence text) {
        loadWebView(39, Helper.md5(Accountname.getText().toString()));
        if (Accountname.getText().length() > 0) {
            loadWebView(39, Helper.md5(Accountname.getText().toString()));
            myLowerCaseTimer.cancel();
            myAccountNameValidationTimer.cancel();
            myLowerCaseTimer.start();
            myAccountNameValidationTimer.start();
        }
    }
    @OnClick(R.id.CancelContact)
    public void Cancel(){
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
    CountDownTimer myAccountNameValidationTimer = new CountDownTimer(1000, 1000) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            createBitShareAN(false);
        }
    };
    public void createBitShareAN(boolean focused) {
        if (!focused) {
            warning.setText("");
            //warning.setVisibility(View.GONE);
            if (Accountname.getText().length() > 2) {
                if (Application.webSocketG != null && (Application.webSocketG.isOpen())) {
                    String socketText = getString(R.string.lookup_account_a) + "\"" + Accountname.getText().toString() + "\"" + ",50]],\"id\": 6}";
                    Application.webSocketG.send(socketText);

                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
                loadWebView(39, Helper.md5(Accountname.getText().toString()));
                SaveContact.setEnabled(false);
                SaveContact.setBackgroundColor(getResources().getColor(R.color.gray));
            }
        }
    }
    @Override
    public void checkAccount(JSONObject jsonObject) {

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
                        SaveContact.setEnabled(true);
                        SaveContact.setBackgroundColor(getResources().getColor(R.color.pinkColor));
                        warning.setText(R.string.account_name_validate);
                        warning.setVisibility(View.VISIBLE);   }
                });
            }
            if (!found){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validReceiver = false;
                        String acName = getString(R.string.account_name_not_exist);
                        String format = String.format(acName, Accountname.getText().toString());
                        SaveContact.setEnabled(false);
                        SaveContact.setBackgroundColor(getResources().getColor(R.color.gray));
                        warning.setText(format);
                        warning.setVisibility(View.VISIBLE);
                    }
                });
            }
        } catch (Exception e) {

        }
    }
}
