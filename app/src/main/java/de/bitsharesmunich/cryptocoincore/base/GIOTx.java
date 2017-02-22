package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by henry on 06/02/2017.
 */

public class GIOTx {
    private long id = -1;
    private Coin type;
    private int index;
    private GeneralCoinAddress address;
    private GeneralTransaction transaction;
    private long amount;
    private boolean isOut;
    private String addressString;
    private String scriptHex;


    public GIOTx(){

    }

    public GIOTx(long id, Coin type, GeneralCoinAddress address, GeneralTransaction transaction, long amount, boolean isOut, String addressString, int index, String scriptHex) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.transaction = transaction;
        this.amount = amount;
        this.isOut = isOut;
        this.addressString = addressString;
        this.index = index;
        this.scriptHex = scriptHex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Coin getType() {
        return type;
    }

    public void setType(Coin type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public GeneralCoinAddress getAddress() {
        return address;
    }

    public void setAddress(GeneralCoinAddress address) {
        this.address = address;
    }

    public GeneralTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(GeneralTransaction transaction) {
        this.transaction = transaction;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getScriptHex() {
        return scriptHex;
    }

    public void setScriptHex(String scriptHex) {
        this.scriptHex = scriptHex;
    }
}
