package de.bitsharesmunich.cryptocoincore.base;

/**
 * Represents a contact addres of one of the coin different of the bitshares account.
 */
public class ContactAddress {
    /**
     * The coin type of the address
     */
    Coin mCoin;
    /**
     * The address of the contact
     */
    String mAddress;

    /**
     * Basic Constructor
     *
     * @param coin The coin type
     * @param address The address
     */
    public ContactAddress(Coin coin, String address){
        this.mCoin = coin;
        this.mAddress = address;
    }

    /**
     * Getter fo the coin type
     */
    public Coin getCoin() {
        return mCoin;
    }

    /**
     * The setter fo the coin type
     */
    public void setCoin(Coin coin) {
        this.mCoin = coin;
    }

    /**
     * Getter of the Address
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Setter fo the Address
     */
    public void setAddress(String address) {
        this.mAddress = address;
    }

    /**
     * This creates a duplicate of this object
     * @return
     */
    public ContactAddress clone(){
        ContactAddress clon = new ContactAddress(this.mCoin,this.mAddress);
        return clon;
    }
}
