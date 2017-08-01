package de.bitshares_munich.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitshares_munich.fragments.BalancesFragment;
import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by javier on 28/12/2016.
 */

public class BalanceItems {
    ArrayList<BalanceItem> items;
    private List<BalanceItemsListener> _listeners = new ArrayList<BalanceItemsListener>();
    Coin coin;

    public BalanceItems(Coin coin){
        this.items = new ArrayList<BalanceItem>();
        this.coin = coin;
    }

    public BalanceItem addBalanceItem(String symbol, String precision, String ammount){
        return this.addDetailedBalanceItem(symbol, precision, ammount, -1, false);
    }

    public BalanceItem addBalanceItem(String symbol, String precision, String ammount, boolean initialLoad){
        return this.addDetailedBalanceItem(symbol, precision, ammount, -1, initialLoad);
    }

    public BalanceItem addDetailedBalanceItem(String symbol, String precision, String ammount, int confirmations, boolean initialLoad) {
        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit","");

        BalanceItem newBalanceItem = new BalanceItem(this.coin, symbol, precision, ammount);
        newBalanceItem.setConfirmations(confirmations);
        this.items.add(newBalanceItem);
        this._fireOnNewBalanceItemEvent(newBalanceItem, initialLoad);
        return newBalanceItem;
    }

    public void addBalanceItem(BalanceItem item){
        this.items.add(item);
    }

    public void removeBalanceItem(BalanceItem item){
        int index = this.items.indexOf(item);
        this.items.remove(index);
        this._fireOnBalanceItemRemovedEvent(item, index, this.count());
    }

    public BalanceItem findBalanceItemBySymbol(String symbol){
        BalanceItem nextBalanceItem;
        String withoutBit;

        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit","");

        for(int i=0;i<this.count();i++){
            nextBalanceItem = this.getBalanceItem(i);

            if (nextBalanceItem.getSymbol().equals(symbol)){
                return nextBalanceItem;
            }
        }

        return null;
    }

    public void updateFaitBalanceItem(String symbol, String fait){
        BalanceItem balanceItem = this.findBalanceItemBySymbol(symbol);

        if (balanceItem != null){
            int index = this.items.indexOf(balanceItem);
            BalanceItem oldBalanceItem = balanceItem.clone();
            balanceItem.setFait(fait);

            this._fireOnBalanceItemUpdatedEvent(oldBalanceItem, balanceItem, index);
        }
    }

    public void addOrUpdateBalanceItem(String symbol, String precision, String ammount){
        addOrUpdateDetailedBalanceItem(symbol,precision,ammount,-1);
    }

    public void addOrUpdateDetailedBalanceItem(String symbol, String precision, String ammount, int confirmations) {
        BalanceItem balanceItem = this.findBalanceItemBySymbol(symbol);

        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit","");

        if (balanceItem == null){
            this.addBalanceItem(symbol, precision, ammount);
        } else {
            int index = this.items.indexOf(balanceItem);
            BalanceItem oldBalanceItem = balanceItem.clone();
            balanceItem.setSymbol(symbol);
            balanceItem.setPrecision(precision);
            balanceItem.setAmmount(ammount);
            balanceItem.setConfirmations(confirmations);

            this._fireOnBalanceItemUpdatedEvent(oldBalanceItem, balanceItem, index);
        }
    }

    public void clear(){
        this.items.clear();
        this._fireOnBalanceItemsRemovedEvent();
    }

    public int count(){
        return this.items.size();
    }

    public Coin getCoin(){
        return this.coin;
    }

    public BalanceItem getBalanceItem(int index){
        return this.items.get(index);
    }

    public void removeZeroBalanceItems(){
        BalanceItem nextBalanceItem;
        int i=0;

        while(i<this.count()){
            nextBalanceItem = this.getBalanceItem(i);

            if ((nextBalanceItem.getAmmount().equals("")) || (nextBalanceItem.getAmmount().equals("0"))){
                this.removeBalanceItem(nextBalanceItem);
            } else {
                i++;
            }
        }
    }

    public synchronized void addListener( BalanceItemsListener listener ) {
        _listeners.add(listener);
    }

    public synchronized void removeListener( BalanceItemsListener listener ) {
        _listeners.remove(listener);
    }

    public void fireAllItemsUpdateEvent(){
        for (int i=0;i<this.items.size();i++){
            BalanceItem nextItem = this.items.get(i);
            this._fireOnBalanceItemUpdatedEvent(nextItem,nextItem,i);
        }
    }

    private synchronized void _fireOnNewBalanceItemEvent(BalanceItem item, boolean initialLoad) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent( this, item );
        balanceItemsEvent.setInitialLoad(initialLoad);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onNewBalanceItem( balanceItemsEvent );
        }
    }

    private synchronized void _fireOnBalanceItemRemovedEvent(BalanceItem item, int index, int newSize) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent( this, item );
        balanceItemsEvent.setIndex(index);
        balanceItemsEvent.setNewSize(newSize);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onBalanceItemRemoved( balanceItemsEvent );
        }
    }

    private synchronized void _fireOnBalanceItemsRemovedEvent() {
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onBalanceItemsRemoved(this.coin);
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
