package de.bitshares_munich.models;

/**
 * Created by javier on 28/12/2016.
 */

public interface BalanceItemsListener
{
    public void onNewBalanceItem( BalanceItemsEvent event );
    public void onBalanceItemRemoved( BalanceItemsEvent event );
    public void onBalanceItemUpdated( BalanceItemsEvent event );
}
