package de.bitshares_munich.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitshares_munich.fragments.BalancesFragment;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItems {
    ArrayList<BalanceItem> items;
    private List<BalanceItemsListener> _listeners = new ArrayList<BalanceItemsListener>();

    public BalanceItems(){
        this.items = new ArrayList<BalanceItem>();
    }

    public BalanceItem addBalanceItem(String symbol, String precision, String ammount){
        BalanceItem newBalanceItem = new BalanceItem(symbol, precision, ammount);
        this.items.add(newBalanceItem);
        this._fireOnNewBalanceItemEvent(newBalanceItem);
        return newBalanceItem;
    }

    public void addBalanceItem(BalanceItem item){
        this.items.add(item);
    }

    public void removeBalanceItem(BalanceItem item){
        int index = this.items.indexOf(item);
        this.items.remove(index);
        this._fireOnBalanceItemRemovedEvent(item, index);
    }

    public BalanceItem findBalanceItemBySymbol(String symbol){
        BalanceItem nextBalanceItem;

        for(int i=0;i<this.count();i++){
            nextBalanceItem = this.getBalanceItem(i);

            if (nextBalanceItem.getSymbol().equals(symbol)){
                return nextBalanceItem;
            }
        }

        return null;
    }

    public void addOrUpdateBalanceItem(String symbol, String precision, String ammount){
        BalanceItem balanceItem = this.findBalanceItemBySymbol(symbol);

        if (balanceItem == null){
            this.addBalanceItem(symbol, precision, ammount);
        } else {
            int index = this.items.indexOf(balanceItem);
            BalanceItem oldBalanceItem = balanceItem.clone();
            balanceItem.setSymbol(symbol);
            balanceItem.setPrecision(precision);
            balanceItem.setAmmount(ammount);

            this._fireOnBalanceItemUpdatedEvent(oldBalanceItem, balanceItem, index);
        }
    }

    public void clear(){
        this.items.clear();
    }

    public int count(){
        return this.count();
    }

    public BalanceItem getBalanceItem(int index){
        return this.items.get(index);
    }

    public void removeZeroBalanceItems(){
        BalanceItem nextBalanceItem;
        int i=0;

        while(i<this.count()){
            nextBalanceItem = this.getBalanceItem(i);

            if (nextBalanceItem.getAmmount() == ""){
                this.removeBalanceItem(nextBalanceItem);
            } else {
                i++;
            }
        }
    }

    private synchronized void _fireOnNewBalanceItemEvent(BalanceItem item) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent( this, item );
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onNewBalanceItem( balanceItemsEvent );
        }
    }

    private synchronized void _fireOnBalanceItemRemovedEvent(BalanceItem item, int index) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent( this, item );
        balanceItemsEvent.setIndex(index);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onBalanceItemRemoved( balanceItemsEvent );
        }
    }

    private synchronized void _fireOnBalanceItemUpdatedEvent(BalanceItem oldBalanceItem, BalanceItem newBalanceItem, int index) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent( this, newBalanceItem );
        balanceItemsEvent.setIndex(index);
        balanceItemsEvent.setOldItem(oldBalanceItem);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onBalanceItemUpdated( balanceItemsEvent );
        }
    }
}
