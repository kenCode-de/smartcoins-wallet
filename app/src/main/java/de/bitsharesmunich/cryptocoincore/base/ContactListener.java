package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by Henry Varona on 25/3/2017.
 */
public interface ContactListener {
    public void onNewContactAddress( ContactEvent event );
    public void onContactAddressModified( ContactEvent event );
    public void onContactAddressRemoved( ContactEvent event );
}
