package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by Henry Varona on 25/3/2017.
 */
public class ContactAddress {
    Coin coin;
    String address;

    public ContactAddress(Coin coin, String address){
        this.coin = coin;
        this.address = address;
    }

    public Coin getCoin() {
        return coin;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
