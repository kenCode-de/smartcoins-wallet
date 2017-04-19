package de.bitsharesmunich.cryptocoincore.smartcoinwallets;

import android.app.Activity;
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
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.interfaces.ContactsDelegate;
import de.bitshares_munich.interfaces.IAccount;
import de.bitshares_munich.smartcoinswallet.BaseActivity;
import de.bitshares_munich.smartcoinswallet.ContactListAdapter;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import de.bitsharesmunich.cryptocoincore.adapters.ArrayListCoinAdapter;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.Contact;
import de.bitsharesmunich.cryptocoincore.base.ContactAddress;
import de.bitsharesmunich.cryptocoincore.base.ContactEvent;
import de.bitsharesmunich.cryptocoincore.base.ContactListener;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;
import de.bitsharesmunich.cryptocoincore.fragments.GeneralCoinContactsFragment;

/**
 * Created by Syed Muhammad Muzzammil on 5/25/16.
 *
 * Modified by Henry Varona on 3/20/2017
 */
public class AddEditContacts extends BaseActivity implements IAccount, ContactListener {
    Boolean add = false;
    Boolean edit = false;
    TinyDB tinyDB;
    SCWallDatabase db;
    String contactname = "";
    String emailtxt = "";
    String accountid = "";
    String note = "";
    Coin coin;

    Contact contact;

    boolean validReceiver = false;

    //@Bind(R.id.web)
    //WebView web;

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

    //@Bind(R.id.Accountname)
    //EditText Accountname;

    @Bind(R.id.email)
    EditText etEmail;

    Context context;
    String contact_id;

    //@Bind(R.id.warning)
    //TextView warning;

    @Bind(R.id.emailHead)
    TextView emailHead;

    @Bind(R.id.tvWarningEmail)
    TextView tvWarningEmail;

    @Bind(R.id.accountsLayout)
    LinearLayout accountsLayout;

    ContactsDelegate contactsDelegate;
    webSocketCallHelper myWebSocketHelper;

    List<Coin> coinsUsed = new ArrayList<Coin>(); //this will be used for enabling/disabling coins in spinner selector

    ContactAddress lastContactAddressAdded; //this is used to prevent reinserting addressView when the user types a new address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contacts);
        ButterKnife.bind(this);

        setBackButton(true);


        context = this;
        tinyDB = new TinyDB(context);
        db = new SCWallDatabase(context);
        Application.registerCallback(this);

        myWebSocketHelper = new webSocketCallHelper(this);

        contactsDelegate = GeneralCoinContactsFragment.contactsDelegate;
        //loadWebView(39, Helper.hash("", Helper.SHA256));

        emailHead.setText(context.getString(R.string.email_name) + " :");
        SaveContact.setEnabled(false);
        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
        cancelContact.setBackgroundColor(getColorWrapper(context, R.color.red));
        Intent intent = getIntent();
        Bundle res = intent.getExtras();

        this.coin = Coin.BITSHARE;//default value

        if (res != null) {
            if (res.getString(getString(R.string.coin)) != null){
                this.coin = Coin.valueOf(res.getString(getString(R.string.coin)));
            }
            if (res.containsKey("activity")) {
                if (res.getInt("activity") == 99999) {
                    add = true;
                    this.contact = new Contact();
                    this.contact.addListener(this);
                    setTitle(getResources().getString(R.string.add_contact_activity_name));
                    SaveContact.setText(R.string.add_contact);
                }
            } else if (res.containsKey("id")) {

                edit = true;
                setTitle(getResources().getString(R.string.edit_contact_activity_name));

                this.contact = db.getContactById(res.getLong("id"));
                this.contact.addListener(this);

                contact_id = Long.toString(res.getLong("id"));

                if (res.containsKey("name")) contactname = res.getString("name");

                if (res.containsKey("account")) accountid = res.getString("account");

                if (res.containsKey("note")) note = res.getString("note");

                if (res.containsKey("email")) emailtxt = res.getString("email");

                Contactname.setText(contactname);
                //Accountname.setText(accountid);
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
                    /*if (!Accountname.getText().toString().equals(accountid) && validReceiver) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                        SaveContact.setEnabled(true);
                    }*/
                }
                //if (add) {
                    //if (validReceiver) {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.green));
                        SaveContact.setEnabled(true);
                    /*} else {
                        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                        SaveContact.setEnabled(false);
                    }
                }*/
                /*if (Accountname.getText().length() == 0) {
                    SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
                    SaveContact.setEnabled(false);
                }*/

                ColorDrawable buttonColor = (ColorDrawable) SaveContact.getBackground();
                int colorId = buttonColor.getColor();
                if (colorId == R.color.green) {
                    SaveContact.setEnabled(true);
                }
                handler.postDelayed(this, 2000);
            }
        }, 2000);

        loadAddresses();
    }

    @OnClick(R.id.SaveContact)
    public void AddContacts() {

        /*if (this.coin == Coin.BITSHARE) {
            ContactListAdapter.ListviewContactItem contact = new ContactListAdapter.ListviewContactItem();
            ArrayList<ContactListAdapter.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ContactListAdapter.ListviewContactItem.class);
            String _contactname = Contactname.getText().toString();
            //String _accountid = Accountname.getText().toString();
            String _note = Note.getText().toString();
            String _email = etEmail.getText().toString();
            if (!SupportMethods.isEmailValid(_email)) {
                _email = "";
            }

            if (add) {
                contact.SaveNote(_note);
                contact.SetName(_contactname);
                //contact.SetAccount(_accountid);
                contact.SaveEmail(_email);
                contacts.add(contact);
                Collections.sort(contacts, new ContactNameComparator());
                tinyDB.putContactsObject("Contacts", contacts);
            } else if (edit) {
                if (!_contactname.equals(contactname))
                    contacts.get(Integer.parseInt(contact_id)).SetName(_contactname);
                //if (!_accountid.equals(accountid))
                //    contacts.get(Integer.parseInt(contact_id)).SetAccount(_accountid);
                if (!_note.equals(note)) contacts.get(Integer.parseInt(contact_id)).SaveNote(_note);
                if (!_email.equals(emailtxt))
                    contacts.get(Integer.parseInt(contact_id)).SaveEmail(_email);
                Collections.sort(contacts, new ContactNameComparator());
                tinyDB.putContactsObject("Contacts", contacts);
            }

        } else {*/
            //Contact contact = new Contact();
            //contact.setAccount(Accountname.getText().toString());
            this.contact.setName(Contactname.getText().toString());
            this.contact.setNote(Note.getText().toString());
            String _email = etEmail.getText().toString();
            if (!SupportMethods.isEmailValid(_email)) {
                this.contact.setEmail("");
            } else {
                this.contact.setEmail(_email);
            }

            if (add) {
                db.putContact(contact);
            } else {
                contact.setId(Long.parseLong(contact_id));
                db.updateContact(contact);
            }
        //}
        contactsDelegate.OnUpdate("knysys", 29);
        finish();
    }

    public void onContactAddressViewChange(View contactAddressView){
        int childIndex = accountsLayout.indexOfChild(contactAddressView);
        EditText accountName = (EditText) contactAddressView.findViewById(R.id.Accountname);
        String accountNameString = accountName.getText().toString();
        Spinner addressCoinSpinner = (Spinner) contactAddressView.findViewById(R.id.address_coin_spinner);
        final Coin coinSelected = (Coin)addressCoinSpinner.getSelectedItem();

        if (childIndex == accountsLayout.getChildCount()-1) {//If it is the last child
            if ((coinSelected != null) && (!accountNameString.isEmpty()) && (!coinsUsed.contains(coinSelected))) {
                ContactAddress newContactAddress = new ContactAddress(coinSelected, accountName.getText().toString());
                lastContactAddressAdded = newContactAddress;
                this.contact.addAddress(newContactAddress);
                coinsUsed.add(coinSelected);
                validateAddress(newContactAddress, contactAddressView);
            }
        } else { //then is an address already added to contact
            ContactAddress contactAddress = this.contact.getAddressByIndex(childIndex);

            if (accountNameString.equals("")){
                this.contact.removeAddress(contactAddress);
            } else {
                //contactAddress.setAddress(accountNameString);
                //contactAddress.setCoin(coinSelected);
                this.contact.updateAddress(contactAddress,coinSelected, accountNameString);
                validateAddress(contactAddress, contactAddressView);
            }
        }

        addNewAccountForm();
    }

    @Override
    public void onNewContactAddress(final ContactEvent event) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                addNewAddressView(event.getContactAddress());
            }
        });
    }

    @Override
    public void onContactAddressModified(final ContactEvent event) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                updateAddressView(event.getOldAddress(), event.getContactAddress(), event.getIndex());
            }
        });
    }

    @Override
    public void onContactAddressRemoved(final ContactEvent event) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                removeAddressView(event.getContactAddress(), event.getIndex());
            }
        });
    }

    public void addNewAddressView(ContactAddress address){
        if ((lastContactAddressAdded == null) || (this.lastContactAddressAdded != address)) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View newAddress = (LinearLayout) layoutInflater.inflate(R.layout.general_contact_account, null);
            EditText addressEdit = (EditText) newAddress.findViewById(R.id.Accountname);
            Spinner coinSpinner = (Spinner) newAddress.findViewById(R.id.address_coin_spinner);

            ArrayList<Coin> data = new ArrayList<Coin>();

            for (Coin coin : Coin.values()) {
                data.add(coin);
            }

            final ArrayListCoinAdapter coinAdapter = new ArrayListCoinAdapter(this, R.layout.coin_spinner_row, data, coinsUsed, getResources());
            coinSpinner.setAdapter(coinAdapter);

            if (address != null) {
                addressEdit.setText(address.getAddress());
                coinSpinner.setSelection(data.indexOf(address.getCoin()));

                if (!coinsUsed.contains(address.getCoin())){
                    coinsUsed.add(address.getCoin());
                }
            }

            coinSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onContactAddressViewChange(newAddress);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            addressEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    onContactAddressViewChange(newAddress);
                }
            });

            accountsLayout.addView(newAddress);
        }
    }

    public void updateAddressView(ContactAddress oldAddress, ContactAddress newAddress, int index){
        final View addressView = accountsLayout.getChildAt(index);
        EditText addressEdit = (EditText)addressView.findViewById(R.id.Accountname);
        Spinner coinSpinner = (Spinner)addressView.findViewById(R.id.address_coin_spinner);

        if (newAddress != null){
            addressEdit.setText(newAddress.getAddress());
            coinSpinner.setSelection(((ArrayListCoinAdapter)coinSpinner.getAdapter()).getPosition(newAddress.getCoin().getLabel()));
            coinsUsed.remove(oldAddress.getCoin());
            if (!coinsUsed.contains(newAddress.getCoin())) {
                coinsUsed.add(newAddress.getCoin());
            }
        }
    }

    public void removeAddressView(ContactAddress address, int index){
        accountsLayout.removeViewAt(index);
        coinsUsed.remove(address.getCoin());
    }

    public void loadAddresses(){
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (this.contact != null) {
            //getContactsAddress loads into the contact the addresses and for every address the onNewContactAddress event gets called
            db.getContactAddresses(this.contact);
        }

        addNewAccountForm();
    }

    //Adds a new form to add a new address only if it's needed
    public void addNewAccountForm(){
        Boolean addNewView = false;
        final Activity activity = this;
        if (accountsLayout.getChildCount() == 0){
            addNewView = true;
        } else {
            LinearLayout lastAddress = (LinearLayout)accountsLayout.getChildAt(accountsLayout.getChildCount()-1);
            EditText addressEdit = (EditText)lastAddress.findViewById(R.id.Accountname);
            Spinner lastAddressCoinSpinner = (Spinner) lastAddress.findViewById(R.id.address_coin_spinner);
            final Coin lastCoinSelected = (Coin)lastAddressCoinSpinner.getSelectedItem();

            if (!addressEdit.getText().toString().equals("")) {
                addNewView = true;
            }

            View nextLayout = null;
            Spinner addressCoinSpinner = null;
            for (int i=0;i<accountsLayout.getChildCount()-1;i++){
                nextLayout = accountsLayout.getChildAt(i);
                addressCoinSpinner = (Spinner) nextLayout.findViewById(R.id.address_coin_spinner);
                final Coin coinSelected = (Coin)addressCoinSpinner.getSelectedItem();
                if (coinSelected == lastCoinSelected){
                    addNewView = false;
                    break;
                }
            }
        }

        if (addNewView){
            addNewAddressView(null);
        }
    }

    Boolean checkIfAlreadyAdded() {
        /*ArrayList<ContactListAdapter.ListviewContactItem> contacts = tinyDB.getContactObject("Contacts", ContactListAdapter.ListviewContactItem.class);
        String _accountid = Accountname.getText().toString();

        for (int i = 0; i < contacts.size(); i++) {

            if (contacts.get(i).account.equals(_accountid)) {
                return true;
            }

        }*/


        return false;
    }

    private void loadWebView(int size, String encryptText, WebView web) {
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encryptText + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    //@OnTextChanged(R.id.Accountname)
    void validateAddress(ContactAddress contactAddress, View contactAddressView) {
        WebView web = (WebView)contactAddressView.findViewById(R.id.web);
        TextView warning = (TextView)contactAddressView.findViewById(R.id.address_warning);

        loadWebView(39, Helper.hash(contactAddress.getAddress(), Helper.SHA256), web);
        warning.setText(getString(R.string.txt_validating_account));
        warning.setTextColor(getColorWrapper(context, R.color.black));
        SaveContact.setBackgroundColor(getColorWrapper(context, R.color.gray));
        SaveContact.setEnabled(false);

        if (!contactAddress.getAddress().toString().equals(contactAddress.getAddress().toString().trim())) {
            contactAddress.setAddress(contactAddress.getAddress().trim());
        }


        if (!contactAddress.getAddress().equals(accountid)) {
            if (contactAddress.getAddress().length() > 0) {

                validReceiver = false;

                loadWebView(39, Helper.hash(contactAddress.getAddress(), Helper.SHA256),web);

                if (contactAddress.getCoin() == Coin.BITSHARE){
                    myLowerCaseTimer.cancel();
                    myAccountNameValidationTimer.cancel();
                    myLowerCaseTimer.start();
                    myAccountNameValidationTimer.start();

                } else {
                    if (contactAddress.getCoin() != null) {
                        GeneralCoinValidator validator = GeneralCoinFactory.getValidator(contactAddress.getCoin());
                        validReceiver = validator.validateAddress(contactAddress.getAddress());

                        if (!validReceiver) {
                            warning.setTextColor(getColorWrapper(context, R.color.red));
                            warning.setText(getString(R.string.address_invalid_format));
                            warning.setVisibility(View.VISIBLE);
                        } else {
                            warning.setText("");
                        }
                    }
                }
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
                //web.setVisibility(View.GONE);
                tvWarningEmail.setText("");
                setGravator(etEmail.getText().toString(), imageEmail);
            } else {
                tvWarningEmail.setText(getString(R.string.in_valid_email));
                tvWarningEmail.setTextColor(getColorWrapper(context, R.color.red));
            }
        }
        if (etEmail.getText().toString().length() <= 0) {
            imageEmail.setVisibility(View.GONE);
            //web.setVisibility(View.VISIBLE);
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
            ContactAddress contactAddress = contact.getAddressByCoin(Coin.BITSHARE);

            if (contactAddress != null) {
                if (!contactAddress.getAddress().equals(contactAddress.getAddress().toLowerCase())) {
                    contactAddress.setAddress(contactAddress.getAddress().toLowerCase());
                    //Accountname.setSelection(Accountname.getText().toString().length());
                }
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
        ContactAddress contactAddress = contact.getAddressByCoin(Coin.BITSHARE);

        if (contactAddress != null) {
            int index = this.contact.getIndexOfAddress(contactAddress);
            View accountLayoutRow = accountsLayout.getChildAt(index);
            TextView warning = (TextView)accountLayoutRow.findViewById(R.id.address_warning);
            WebView web = (WebView)accountLayoutRow.findViewById(R.id.web);

            if (!focused) {
                if (contactAddress.getAddress().length() > 2) {
                    if (!checkIfAlreadyAdded()) {
                        String socketText = getString(R.string.lookup_account_a);
                        String socketText2 = getString(R.string.lookup_account_b) + "\"" + contactAddress.getAddress() + "\"" + ",50]],\"id\": 6}";
                        myWebSocketHelper.make_websocket_call(socketText, socketText2, webSocketCallHelper.api_identifier.database);
                    } else {
                        warning.setText(contactAddress.getAddress() + " " + getString(R.string.is_already_added));
                        warning.setTextColor(getColorWrapper(context, R.color.red));
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.account_name_should_be_longer, Toast.LENGTH_SHORT).show();
                    loadWebView(39, Helper.hash(contactAddress.getAddress(), Helper.SHA256), web);
                    warning.setText("");
                    SaveContact.setEnabled(false);
                    SaveContact.setBackgroundColor(getResources().getColor(R.color.gray));
                }
            }
        }
    }

    @Override
    public void checkAccount(JSONObject jsonObject) {
        final ContactAddress contactAddress = contact.getAddressByCoin(Coin.BITSHARE);

        if (contactAddress != null) {
            int index = this.contact.getIndexOfAddress(contactAddress);
            View accountLayoutRow = accountsLayout.getChildAt(index);
            final TextView warning = (TextView)accountLayoutRow.findViewById(R.id.address_warning);

            myWebSocketHelper.cleanUpTransactionsHandler();
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                boolean found = false;
                for (int i = 0; i < jsonArray.length(); i++) {
                    final String temp = jsonArray.getJSONArray(i).getString(0);
                    if (temp.equals(contactAddress.getAddress())) {
                        found = true;
                        validReceiver = true;
                    }
                }
                if (found) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (contactAddress.getAddress().equals(accountid)) {
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
                                String format = String.format(acName.toString(), contactAddress.getAddress());
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
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                bmImage.setVisibility(View.GONE);
                //web.setVisibility(View.VISIBLE);
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

    public static class ContactNameComparator implements Comparator<ContactListAdapter.ListviewContactItem> {
        public int compare(ContactListAdapter.ListviewContactItem left, ContactListAdapter.ListviewContactItem right) {
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
