package de.bitshares_munich.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by henry on 28/12/2016.
 */

public class BalancesItems implements BalanceItemsListener {
    ArrayList<BalanceItems> items;
    private List<BalanceItemsListener> _listeners = new ArrayList<BalanceItemsListener>();

    public BalancesItems(){
        this.items = new ArrayList<BalanceItems>();
    }

    public BalanceItems getBalancesItems(Coin coin){
        for (int i=0;i<this.items.size();i++){
            if (this.items.get(i).getCoin() == coin){
                return this.items.get(i);
            }
        }

        return null;
    }

    public BalanceItems addBalancesItems(Coin coin){
        BalanceItems balanceItems = this.getBalancesItems(coin);

        if (balanceItems == null){
            balanceItems = new BalanceItems(coin);
            this.items.add(balanceItems);
            balanceItems.addListener(this);
        }

        return balanceItems;
    }

    public synchronized void addListener( BalanceItemsListener listener ) {
        _listeners.add(listener);
    }

    public synchronized void removeListener( BalanceItemsListener listener ) {
        _listeners.remove(listener);
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

    private synchronized void _fireOnBalanceItemsRemovedEvent(Coin coin) {
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (BalanceItemsListener) listeners.next() ).onBalanceItemsRemoved(coin);
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

    @Override
    public void onNewBalanceItem(BalanceItemsEvent event) {
        this._fireOnNewBalanceItemEvent(event.getBalanceItem(),event.isInitialLoad());
    }

    @Override
    public void onBalanceItemRemoved(BalanceItemsEvent event) {
        this._fireOnBalanceItemRemovedEvent(event.getBalanceItem(),event.getIndex(),event.getNewSize());
    }

    @Override
    public void onBalanceItemUpdated(BalanceItemsEvent event) {
        this._fireOnBalanceItemUpdatedEvent(event.getOldItem(),event.getBalanceItem(),event.getIndex());
    }

    @Override
    public void onBalanceItemsRemoved(Coin coin) {
        this._fireOnBalanceItemsRemovedEvent(coin);
    }
}
