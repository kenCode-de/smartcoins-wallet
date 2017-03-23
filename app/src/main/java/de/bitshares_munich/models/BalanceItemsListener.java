package de.bitshares_munich.models;

import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by henry on 28/12/2016.
 */

public interface BalanceItemsListener
{
    public void onNewBalanceItem( BalanceItemsEvent event );
    public void onBalanceItemRemoved( BalanceItemsEvent event );
    public void onBalanceItemsRemoved(Coin coin);
    public void onBalanceItemUpdated( BalanceItemsEvent event );

}
