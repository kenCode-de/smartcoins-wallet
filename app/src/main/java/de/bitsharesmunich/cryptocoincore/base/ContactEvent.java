package de.bitsharesmunich.cryptocoincore.base;

import java.util.EventObject;

/**
 * Created by Henry Varona on 25/3/2017.
 */
public class ContactEvent extends EventObject {
    private ContactAddress mAddress;
    private ContactAddress mOldAddress;
    private int mIndex;
    private int mNewSize;

    public ContactEvent( Object source, ContactAddress address ) {
        super( source );
        this.mAddress = address;
    }

    public ContactAddress getContactAddress() {
        return this.mAddress;
    }

    public void setOldAddress(ContactAddress oldAddress){
        this.mOldAddress = oldAddress;
    }

    public ContactAddress getOldAddress(){
        return this.mOldAddress;
    }

    public void setIndex(int index){
        this.mIndex = index;
    }

    public int getIndex(){
        return this.mIndex;
    }

    public void setNewSize(int newSize){
        this.mNewSize = newSize;
    }

    public int getNewSize(){
        return this.mNewSize;
    }
}
