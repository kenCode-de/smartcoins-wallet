package de.bitsharesmunich.cryptocoincore.base;

import java.util.HashMap;

/**
 * Created by henry on 28/02/2017.
 */

public class Contact {

    private long id;
    private String name;
    private String account;
    private String note;
    private String email;
    private HashMap<Coin,String> addresses = new HashMap();

    public Contact() {
    }

    public Contact(long id, String name, String account, String note, String email) {
        this.id = id;
        this.name = name;
        this.account = account;
        this.note = note;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<Coin, String> getAddresses() {
        return addresses;
    }

    public void setAddresses(HashMap<Coin, String> addresses) {
        this.addresses = addresses;
    }
}
