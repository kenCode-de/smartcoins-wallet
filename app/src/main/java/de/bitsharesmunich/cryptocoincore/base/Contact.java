package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Created by henry on 28/02/2017.
 */

public class Contact {

    private long id;
    private String name;
    private String account;
    private String note;
    private String email;
    private List<ContactAddress> addresses = new ArrayList<ContactAddress>();

    private List<ContactListener> _listeners = new ArrayList<ContactListener>();

    public Contact() {
    }

    public Contact(long id, String name, String account, String note, String email) {
        this.id = id;
        this.name = name;
        this.account = account;
        this.note = note;
        this.email = email;
    }

    public synchronized void addListener( ContactListener listener ) {
        _listeners.add(listener);
    }

    public synchronized void removeListener( ContactListener listener ) {
        _listeners.remove(listener);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ContactAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<ContactAddress> addresses) {
        this.addresses = addresses;
    }

    public ContactAddress getAddressByIndex(int index) {
        return this.addresses.get(index);
    }

    public ContactAddress getAddressByCoin(Coin coin){
        for (ContactAddress address : addresses){
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
        this.addresses.add(contactAddress);
        this._fireOnNewContactAddressEvent(contactAddress);
    }

    public void updateAddress(ContactAddress contactAddress, Coin newCoin, String newAddress) {
        int index = addresses.indexOf(contactAddress);

        if (index >= 0) {
            contactAddress.setAddress(newAddress);
            contactAddress.setCoin(newCoin);
            this._fireOnContactAddressModifiedEvent(contactAddress, index);
        }
    }

    public void removeAddress(ContactAddress contactAddress) {
        int index = addresses.indexOf(contactAddress);
        this.addresses.remove(index);
        this._fireOnContactAddressRemovedEvent(contactAddress, index);
    }

    private synchronized void _fireOnNewContactAddressEvent(ContactAddress contactAddress) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onNewContactAddress( contactEvent );
        }
    }

    private synchronized void _fireOnContactAddressModifiedEvent(ContactAddress contactAddress, int index) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        contactEvent.setIndex(index);

        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onContactAddressModified( contactEvent );
        }
    }

    private synchronized void _fireOnContactAddressRemovedEvent(ContactAddress contactAddress, int index) {
        ContactEvent contactEvent = new ContactEvent( this, contactAddress );
        contactEvent.setIndex(index);

        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (ContactListener) listeners.next() ).onContactAddressRemoved( contactEvent );
        }
    }

    public int getIndexOfAddress(ContactAddress contactAddress) {
        return this.addresses.indexOf(contactAddress);
    }
}
