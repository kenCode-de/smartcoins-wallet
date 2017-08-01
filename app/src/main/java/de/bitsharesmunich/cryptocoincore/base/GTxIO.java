package de.bitsharesmunich.cryptocoincore.base;

/**
 * General Coin Transaction Input/Output
 *
 * This class represent each Input or Output Transaction of a General Coin Transaction
 *
 * Created by henry on 06/02/2017.
 */

public class GTxIO {
    /**
     * The id on the database
     */
    private long mId = -1;
    /**
     * The Coin type of this transaction
     */
    private Coin mType;
    /**
     * The index on the transaction Input/Output
     */
    private int mIndex;
    /**
     * The address that this transaction Input/Output belongs
     */
    private GeneralCoinAddress mAddress;
    /**
     * The transaction that this Input/Output belongs
     */
    private GeneralTransaction mTransaction;
    /**
     * The amount
     */
    private long mAmount;
    /**
     * If this transaction is output or input
     */
    private boolean mIsOut;
    /**
     * The address of this transaction as String
     */
    private String mAddressString;
    /**
     * The Script as Hex
     */
    private String mScriptHex;
    /**
     * If this is a transaction output, the original transaction where this is input
     */
    private String mOriginalTxId;

    /**
     * Empty Constructor
     */
    public GTxIO() {

    }

    /**
     * General Constructor, used by the DB.
     *
     * @param id The id in the dataabase
     * @param type The coin mType
     * @param address The addres fo an account on the wallet, or null if the address is external
     * @param transaction The transaction where this belongs
     * @param amount The amount with the lowest precision
     * @param isOut if this is an output
     * @param addressString The string of the General Coin address, this can't be null
     * @param index The index on the transaction
     * @param scriptHex The script in hex String
     */
    public GTxIO(long id, Coin type, GeneralCoinAddress address, GeneralTransaction transaction, long amount, boolean isOut, String addressString, int index, String scriptHex) {
        this.mId = id;
        this.mType = type;
        this.mAddress = address;
        this.mTransaction = transaction;
        this.mAmount = amount;
        this.mIsOut = isOut;
        this.mAddressString = addressString;
        this.mIndex = index;
        this.mScriptHex = scriptHex;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public Coin getType() {
        return mType;
    }

    public void setType(Coin type) {
        this.mType = type;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public GeneralCoinAddress getAddress() {
        return mAddress;
    }

    public void setAddress(GeneralCoinAddress address) {
        this.mAddress = address;
    }

    public GeneralTransaction getTransaction() {
        return mTransaction;
    }

    public void setTransaction(GeneralTransaction transaction) {
        this.mTransaction = transaction;
    }

    public long getAmount() {
        return mAmount;
    }

    public void setAmount(long amount) {
        this.mAmount = amount;
    }

    public boolean isOut() {
        return mIsOut;
    }

    public void setOut(boolean out) {
        mIsOut = out;
    }

    public String getAddressString() {
        return mAddressString;
    }

    public void setAddressString(String addressString) {
        this.mAddressString = addressString;
    }

    public String getScriptHex() {
        return mScriptHex;
    }

    public void setScriptHex(String scriptHex) {
        this.mScriptHex = scriptHex;
    }

    public String getOriginalTxid() {
        return mOriginalTxId;
    }

    public void setOriginalTxid(String originalTxid) {
        this.mOriginalTxId = originalTxid;
    }
}
