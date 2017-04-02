package de.bitshares_munich.models;

import java.util.EventObject;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItemsEvent extends EventObject {
    private BalanceItem item;
    private BalanceItem oldItem;
    private int index;
    private int newSize;
    private boolean initialLoad;

    public BalanceItemsEvent(Object source, BalanceItem item) {
        super(source);
        this.item = item;
        this.initialLoad = false;
    }

    public BalanceItem getBalanceItem() {
        return this.item;
    }

    public BalanceItem getOldItem() {
        return this.oldItem;
    }

    public void setOldItem(BalanceItem oldItem) {
        this.oldItem = oldItem;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNewSize() {
        return this.newSize;
    }

    public void setNewSize(int newSize) {
        this.newSize = newSize;
    }

    public boolean isInitialLoad() {
        return this.initialLoad;
    }

    public void setInitialLoad(boolean initialLoad) {
        this.initialLoad = initialLoad;
    }
}
