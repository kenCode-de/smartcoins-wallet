package de.bitsharesmunich.cryptocoincore.base;

import java.util.EventObject;

/**
 * Created by Henry Varona on 25/3/2017.
 */

/**
 * This event object is used when a Contact fires
 * the insertion, modification and removal events of its addresses
 */
public class ContactEvent extends EventObject {
    private ContactAddress mAddress; /**< the new, modified o removed address*/
    private ContactAddress mOldAddress; /**< the old address before the modification event*/
    private int mIndex; /**< the index of the address when modified or removed*/
    private int mNewSize; /**< the new count of addresses after a modification o removal*/

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
