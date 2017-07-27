package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A General Coin Transaction, of Cryptocurrency like bitcoin
 *
 * Created by henry on 06/02/2017.
 */

public class GeneralTransaction {
    /**
     * The id on the database
     */
    private long mId = -1;
    /**
     * The Tx id of this transaciton
     */
    private String mTxId;
    /**
     * the type of crypto coin fo this transaction
     */
    private Coin mType;
    /**
     * If this is confirmed, the block where it belongs, 0 means this hasn't be included in any block
     */
    private long mBlock;
    /**
     * The amount of fee of this transaction
     */
    private long mFee;
    /**
     * the number of confirmations of this transacion, 0 means it hasn't been included in any block
     */
    private int mConfirm;
    /**
     * The date of this transaction first broadcast
     */
    private Date mDate;
    /**
     * The height of this transaction on the block
     */
    private int mBlockHeight;
    /**
     * The memo of this transaciton
     */
    private String mMemo = null;
    /**
     * The account that this transaction belong as input or output.
     */
    private GeneralCoinAccount mAccount;
    /**
     * The inputs of this transactions
     */
    private List<GTxIO> mTxInputs = new ArrayList();
    /**
     * the outputs of this transasctions
     */
    private List<GTxIO> mTxOutputs = new ArrayList();

    /**
     * empty constructor
     */
    public GeneralTransaction() {
    }

    /**
     * Constructor form the database
     * @param id the id on the database
     * @param txid the txid of this transaction
     * @param type The cryptocoin type
     * @param block The block where this transaction is, 0 means this hasn't be confirmed
     * @param fee the fee of this transaction
     * @param confirm the number of confirmations of this transasciton
     * @param date the date of this transaction
     * @param blockHeight the height on the block where this transasciton is
     * @param memo the memo of this transaction
     * @param account The account to this transaction belongs, as input or output
     */
    public GeneralTransaction(long id, String txid, Coin type, long block, long fee, int confirm, Date date, int blockHeight, String memo, GeneralCoinAccount account) {
        this.mId = id;
        this.mTxId = txid;
        this.mType = type;
        this.mBlock = block;
        this.mFee = fee;
        this.mConfirm = confirm;
        this.mDate = date;
        this.mBlockHeight = blockHeight;
        this.mMemo = memo;
        this.mAccount = account;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getTxid() {        return mTxId;    }

    public void setTxid(String txid) {        this.mTxId = txid;    }

    public Coin getType() {
        return mType;
    }

    public void setType(Coin type) {
        this.mType = type;
    }

    public long getBlock() {
        return mBlock;
    }

    public void setBlock(long block) {
        this.mBlock = block;
    }

    public long getFee() {
        return mFee;
    }

    public void setFee(long fee) {
        this.mFee = fee;
    }

    public int getConfirm() {
        return mConfirm;
    }

    public void setConfirm(int confirm) {
        this.mConfirm = confirm;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public int getBlockHeight() {
        return mBlockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.mBlockHeight = blockHeight;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String memo) {
        this.mMemo = memo;
    }

    public List<GTxIO> getTxInputs() {
        return mTxInputs;
    }

    public void setTxInputs(List<GTxIO> txInputs) {
        this.mTxInputs = txInputs;
    }

    public List<GTxIO> getTxOutputs() {
        return mTxOutputs;
    }

    public void setTxOutputs(List<GTxIO> txOutputs) {
        this.mTxOutputs = txOutputs;
    }

    public GeneralCoinAccount getAccount() {
        return mAccount;
    }

    public void setAccount(GeneralCoinAccount account) {
        this.mAccount = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralTransaction that = (GeneralTransaction) o;

        if (mTxId != null ? !mTxId.equals(that.mTxId) : that.mTxId != null) return false;
        return mType == that.mType;

    }

    @Override
    public int hashCode() {
        int result = mTxId != null ? mTxId.hashCode() : 0;
        result = 31 * result + mType.hashCode();
        return result;
    }


    /**
     * Returns how this transaction changes the balance of the account
     * @return The amount of balance this transasciton adds to the total balance of the account
     */
    public double getAccountBalanceChange(){
        double balance = 0;
        boolean theresAccountInput = false;

        for (GTxIO txInputs : this.getTxInputs()){
            if (txInputs.isOut() && (txInputs.getAddress() != null)){
                balance += -txInputs.getAmount();
                theresAccountInput = true;
            }
        }

        for (GTxIO txOutput : this.getTxOutputs()){
            if (!txOutput.isOut() && (txOutput.getAddress() != null)){
                balance += txOutput.getAmount();
            }
        }

        if (theresAccountInput){
            balance += -this.getFee();
        }

        return balance;
    }
}
