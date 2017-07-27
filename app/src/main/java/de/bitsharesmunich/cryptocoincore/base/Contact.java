package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents a Contact of the user, that can be send cryptocurrencie
 */

public class Contact {

    /**
     * The id on the database
     */
    private long mId;
    /**
     * The name of the Contact
     */
    private String mName;
    /**
     * The bitshares Account
     */
    private String mAccount;
    /**
     * Any notes about the contact
     */
    private String mNote;
    /**
     * The email of the contact
     */
    private String mEmail;
    /**
     * The list of address of differents cryptocurrencies, like bitcoin
     */
    private List<ContactAddress> mAddresses = new ArrayList<ContactAddress>();

    private List<ContactListener> mListeners = new ArrayList<ContactListener>();

    public Contact() {
    }

    public Contact(long id, String name, String account, String note, String email) {
        this.mId = id;
        this.mName = name;
        this.mAccount = account;
        this.mNote = note;
        this.mEmail = email;
    }

    public synchronized void addListener( ContactListener listener ) {
        mListeners.add(listener);
    }

    public synchronized void removeListener( ContactListener listener ) {
        mListeners.remove(listener);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String account) {
        this.mAccount = account;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        this.mNote = note;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public List<ContactAddress> getAddresses() {
        return mAddresses;
    }

    public void setAddresses(List<ContactAddress> addresses) {
        this.mAddresses = addresses;
    }

    public ContactAddress getAddressByIndex(int index) {
        return this.mAddresses.get(index);
    }

    public ContactAddress getAddressByCoin(Coin coin){
        for (ContactAddress address : mAddresses){
            if (address.getCoin() == coin){
                return address;
            }
        }
        return null;
    }

    public ContactAddress addAddress(Coin coin, String address) {
        ContactAddress contactAddress = this.getAddressByCoin(coin);

        if (contactAddress == null) {
            contactAddress = new ContactAddress(coin, address);
        }

        this.addAddress(contactAddress);

        return contactAddress;
    }

    public void addAddress(ContactAddress contactAddress){
        this.mAddresses.add(contactAddress);
        this._fireOnNewContactAddressEvent(contactAddress);
    }

    public void updateAddress(ContactAddress contactAddress, Coin newCoin, String newAddress) {
        int index = mAddresses.indexOf(contactAddress);

        if (index >= 0) {
            if ((newCoin != contactAddress.getCoin())
                    || (!newAddress.equals(contactAddress.getAddress()))) {
                ContactAddress oldContactAddress = contactAddress.clone();
                contactAddress.setAddress(newAddress);
                contactAddress.setCoin(newCoin);
                this._fireOnContactAddressModifiedEvent(oldContactAddress, contactAddress, index);
            }
        }
    }

    public void removeAddress(ContactAddress contactAddress) {
        int index = mAddresses.indexOf(contactAddress);
        this.mAddresses.remove(index);
        this._fireOnContactAddressRemovedEvent(contactAddress, index);
    }

    private synchronized void _fireOnNewContactAddressEvent(ContactAddress contactAddress) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        Iterator listeners = mListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onNewContactAddress( contactEvent );
        }
    }

    private synchronized void _fireOnContactAddressModifiedEvent(ContactAddress oldContactAddress,
                                                                 ContactAddress newContactAddress,
                                                                 int index) {
        ContactEvent contactEvent = new ContactEvent( this, newContactAddress );
        contactEvent.setOldAddress(oldContactAddress);
        contactEvent.setIndex(index);

        Iterator listeners = mListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onContactAddressModified( contactEvent );
        }
    }

    private synchronized void _fireOnContactAddressRemovedEvent(ContactAddress contactAddress,
                                                                int index) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        contactEvent.setIndex(index);

        Iterator listeners = mListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onContactAddressRemoved( contactEvent );
        }
    }

    public int getIndexOfAddress(ContactAddress contactAddress) {
        return this.mAddresses.indexOf(contactAddress);
    }
}
