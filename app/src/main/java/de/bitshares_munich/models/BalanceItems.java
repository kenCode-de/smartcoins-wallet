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

    public BalanceItems() {
        this.items = new ArrayList<BalanceItem>();
    }

    public BalanceItem addBalanceItem(String symbol, String precision, String ammount) {
        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit", "");

        BalanceItem newBalanceItem = new BalanceItem(symbol, precision, ammount);
        this.items.add(newBalanceItem);
        this._fireOnNewBalanceItemEvent(newBalanceItem, false);
        return newBalanceItem;
    }

    public BalanceItem addBalanceItem(String symbol, String precision, String ammount, boolean initialLoad) {
        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit", "");

        BalanceItem newBalanceItem = new BalanceItem(symbol, precision, ammount);
        this.items.add(newBalanceItem);
        this._fireOnNewBalanceItemEvent(newBalanceItem, initialLoad);
        return newBalanceItem;
    }

    public void addBalanceItem(BalanceItem item) {
        this.items.add(item);
    }

    public void removeBalanceItem(BalanceItem item) {
        int index = this.items.indexOf(item);
        this.items.remove(index);
        this._fireOnBalanceItemRemovedEvent(item, index, this.count());
    }

    public BalanceItem findBalanceItemBySymbol(String symbol) {
        BalanceItem nextBalanceItem;
        String withoutBit;

        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit", "");

        for (int i = 0; i < this.count(); i++) {
            nextBalanceItem = this.getBalanceItem(i);

            if (nextBalanceItem.getSymbol().equals(symbol)) {
                return nextBalanceItem;
            }
        }

        return null;
    }

    public void updateFiatBalanceItem(String symbol, String fiat) {
        BalanceItem balanceItem = this.findBalanceItemBySymbol(symbol);

        if (balanceItem != null) {
            int index = this.items.indexOf(balanceItem);
            BalanceItem oldBalanceItem = balanceItem.clone();
            balanceItem.setFiat(fiat);

            this._fireOnBalanceItemUpdatedEvent(oldBalanceItem, balanceItem, index);
        }
    }

    public void addOrUpdateBalanceItem(String symbol, String precision, String amount) {
        BalanceItem balanceItem = this.findBalanceItemBySymbol(symbol);

        //TODO eliminate the "bit" string from the logic, this should only be in the view
        symbol = symbol.replace("bit", "");

        if (balanceItem == null) {
            this.addBalanceItem(symbol, precision, amount);
        } else {
            int index = this.items.indexOf(balanceItem);
            BalanceItem oldBalanceItem = balanceItem.clone();
            balanceItem.setSymbol(symbol);
            balanceItem.setPrecision(precision);
            balanceItem.setAmount(amount);

            this._fireOnBalanceItemUpdatedEvent(oldBalanceItem, balanceItem, index);
        }
    }

    public void clear() {
        this.items.clear();
    }

    public int count() {
        return this.items.size();
    }

    public BalanceItem getBalanceItem(int index) {
        return this.items.get(index);
    }

    public void removeZeroBalanceItems() {
        BalanceItem nextBalanceItem;
        int i = 0;

        while (i < this.count()) {
            nextBalanceItem = this.getBalanceItem(i);

            if ((nextBalanceItem.getAmount().equals("")) || (nextBalanceItem.getAmount().equals("0"))) {
                this.removeBalanceItem(nextBalanceItem);
            } else {
                i++;
            }
        }
    }

    public synchronized void addListener(BalanceItemsListener listener) {
        _listeners.add(listener);
    }

    public synchronized void removeListener(BalanceItemsListener listener) {
        _listeners.remove(listener);
    }

    private synchronized void _fireOnNewBalanceItemEvent(BalanceItem item, boolean initialLoad) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent(this, item);
        balanceItemsEvent.setInitialLoad(initialLoad);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((BalanceItemsListener) listeners.next()).onNewBalanceItem(balanceItemsEvent);
        }
    }

    private synchronized void _fireOnBalanceItemRemovedEvent(BalanceItem item, int index, int newSize) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent(this, item);
        balanceItemsEvent.setIndex(index);
        balanceItemsEvent.setNewSize(newSize);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((BalanceItemsListener) listeners.next()).onBalanceItemRemoved(balanceItemsEvent);
        }
    }

    private synchronized void _fireOnBalanceItemUpdatedEvent(BalanceItem oldBalanceItem, BalanceItem newBalanceItem, int index) {
        BalanceItemsEvent balanceItemsEvent = new BalanceItemsEvent(this, newBalanceItem);
        balanceItemsEvent.setIndex(index);
        balanceItemsEvent.setOldItem(oldBalanceItem);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((BalanceItemsListener) listeners.next()).onBalanceItemUpdated(balanceItemsEvent);
        }
    }
}
