package de.bitshares_munich.models;

import java.util.EventObject;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItemsEvent extends EventObject {
    private BalanceItem item;
    private BalanceItem oldItem;
    private int index;

    public BalanceItemsEvent( Object source, BalanceItem item ) {
        super( source );
        this.item = item;
    }

    public BalanceItem getBalanceItem() {
        return this.item;
    }

    public void setOldItem(BalanceItem oldItem){
        this.oldItem = oldItem;
    }

    public BalanceItem getOldItem(){
        return this.oldItem;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

}
