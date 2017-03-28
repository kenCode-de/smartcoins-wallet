package de.bitsharesmunich.cryptocoincore.base;

import java.util.EventObject;

/**
 * Created by Henry Varona on 25/3/2017.
 */
public class ContactEvent extends EventObject {
    private ContactAddress address;
    private ContactAddress oldAddress;
    private int index;
    private int newSize;

    public ContactEvent( Object source, ContactAddress address ) {
        super( source );
        this.address = address;
    }

    public ContactAddress getContactAddress() {
        return this.address;
    }

    public void setOldAddress(ContactAddress oldAddress){
        this.oldAddress = oldAddress;
    }

    public ContactAddress getOldAddress(){
        return this.oldAddress;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

    public void setNewSize(int newSize){
        this.newSize = newSize;
    }

    public int getNewSize(){
        return this.newSize;
    }
}
