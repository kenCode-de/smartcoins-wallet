package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents a Contact of the user to whom cryptocurrencies can be sent
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

    /**
     * The list of listeners registered in this contact.
     * The listener will be notified when an address is added, modified or deleted from this contact.
     */
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

    /**
     * Register a listener in this contact
     *
     * @param listener the listener to register
     */
    public synchronized void addListener( ContactListener listener ) {
        mListeners.add(listener);
    }

    /**
     * Remove a listener from this contact
     *
     * @param listener the listener to remove
     */
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

    /**
     * Returns the address with a specified coin in this contact
     *
     * @param coin the coin of the address to search
     * @return if exists, an address of the given coin, null if there's no address for the given coin
     */
    public ContactAddress getAddressByCoin(Coin coin){
        for (ContactAddress address : mAddresses){
            if (address.getCoin() == coin){
                return address;
            }
        }
        return null;
    }

    /**
     * Adds an address with the specified coin to this contact
     *
     * @param coin the coin type of the address to add
     * @param address the address to add
     * @return a ContactAddress with the address added
     */
    public ContactAddress addAddress(Coin coin, String address) {
        ContactAddress contactAddress = this.getAddressByCoin(coin);

        if (contactAddress == null) {
            contactAddress = new ContactAddress(coin, address);
        }

        this.addAddress(contactAddress);

        return contactAddress;
    }

    /**
     * Adds an address to this contact and fires the OnNewAddress event
     *
     * @param contactAddress the address to add
     */
    public void addAddress(ContactAddress contactAddress){
        this.mAddresses.add(contactAddress);
        this._fireOnNewContactAddressEvent(contactAddress);
    }

    /**
     * Modifies an address of this contact and fires the OnAddressModified
     *
     * @param contactAddress the address to modify
     * @param newCoin the new coin of the contact address
     * @param newAddress the new address string of the contact address
     */
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

    /**
     * Removes an address from this contact and fires the OnAddressRemoved event
     *
     * @param contactAddress the address to remove
     */
    public void removeAddress(ContactAddress contactAddress) {
        int index = mAddresses.indexOf(contactAddress);
        this.mAddresses.remove(index);
        this._fireOnContactAddressRemovedEvent(contactAddress, index);
    }

    /**
     * Notifies the OnNewAddress event to all the listeners
     *
     * @param contactAddress the added address
     */
    private synchronized void _fireOnNewContactAddressEvent(ContactAddress contactAddress) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        Iterator listeners = mListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onNewContactAddress( contactEvent );
        }
    }

    /**
     * Notifies the OnModifiedAddress event to all the listeners
     *
     * @param oldContactAddress a copy of the address before the changes
     * @param newContactAddress the address after the changes
     * @param index the index of the modified address
     */
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


    /**
     * Notifies the OnAddressRemoved event to all the listeners
     *
     * @param contactAddress the removed address
     * @param index the index of the address before the removal
     */
    private synchronized void _fireOnContactAddressRemovedEvent(ContactAddress contactAddress,
                                                                int index) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        contactEvent.setIndex(index);

        Iterator listeners = mListeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onContactAddressRemoved( contactEvent );
        }
    }

    /**
     * Returns the index of an address in this contact
     *
     * @param contactAddress the contact address to search for
     * @return if the address exists in this contact, the index of the address, -1 otherwise
     */
    public int getIndexOfAddress(ContactAddress contactAddress) {
        return this.mAddresses.indexOf(contactAddress);
    }
}
