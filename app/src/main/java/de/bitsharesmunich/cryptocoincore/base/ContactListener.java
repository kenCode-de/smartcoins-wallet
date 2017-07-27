package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by Henry Varona on 25/3/2017.
 *
 */

/**
 * A listener interface for the Contact class for getting notifications
 * of insertions, modifications and removals of addresses in a user contact
 *
 */
public interface ContactListener {
    /**
     * A notification event for the insertion of a new address in a user contact
     *
     * @param event the event with the new address added
     */
    public void onNewContactAddress( ContactEvent event );
    /**
     * A notification event for the modification of an address in a user contact
     *
     * @param event the event with the address before and after the modifications
     */
    public void onContactAddressModified( ContactEvent event );
    /**
     * A notification event for the removal of an address in a user contact
     *
     * @param event the event with the address removed
     */
    public void onContactAddressRemoved( ContactEvent event );
}
