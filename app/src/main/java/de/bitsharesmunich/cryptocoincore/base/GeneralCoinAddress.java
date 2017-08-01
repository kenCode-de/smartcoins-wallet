package de.bitsharesmunich.cryptocoincore.base;

import de.bitsharesmunich.graphenej.Util;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an Address of a General Coin Account
 */
public class GeneralCoinAddress {
    /**
     * The id on the database
     */
    private long mId = -1;
    /**
     * The account that this address belongs
     */
    private final GeneralCoinAccount mAccount;
    /**
     * If this is change or external
     */
    private final boolean mIsChange;
    /**
     * The index fo this address in the account
     */
    private final int mIndex;
    /**
     * The ky used to calculate the address
     */
    private ECKey mKey;
    /**
     * The list of the transactions that used this address as input
     */
    private List<GTxIO> mTransactionInput = new ArrayList<>();
    /**
     * The list of the transactions that used this address as output
     */
    private List<GTxIO> mTransactionOutput = new ArrayList<>();

    /**
     * Contrsutcotr used from the database
     * @param id The id on the database
     * @param account The account of this address
     * @param isChange if it is change or external address
     * @param index the index on the account of this address
     * @param publicHexKey The public Address String
     */
    public GeneralCoinAddress(long id, GeneralCoinAccount account, boolean isChange, int index, String publicHexKey) {
        this.mId = id;
        this.mAccount = account;
        this.mIsChange = isChange;
        this.mIndex = index;
        this.mKey = ECKey.fromPublicOnly(Util.hexToBytes(publicHexKey));
    }

    /**
     * Basic constructor
     * @param account The account of this address
     * @param isChange if it is change or external address
     * @param index The index on the account of this address
     * @param key The key to generate the private and the public key of this address
     */
    public GeneralCoinAddress(GeneralCoinAccount account, boolean isChange, int index, DeterministicKey key) {
        this.mId = -1;
        this.mAccount = account;
        this.mIsChange = isChange;
        this.mIndex = index;
        this.mKey = key;
    }

    /**
     * Getter of the database id
     */
    public long getId() {
        return mId;
    }

    /**
     * Setter of the database id
     */
    public void setId(long id) {
        this.mId = id;
    }
    /**
     * Getter for he account
     */
    public GeneralCoinAccount getAccount() {
        return mAccount;
    }

    /**
     * Indicates if this addres is change, if not is external
     */
    public boolean isIsChange() {
        return mIsChange;
    }

    /**
     * Getter for the index on the account of this address
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Getter for the key of this address
     */
    public ECKey getKey() {
        return mKey;
    }

    /**
     * Set the key for generate private key, this is used when this address is loaded from the database
     * and want to be used to send transactions
     * @param key The key that generates the private and the public key
     */
    public void setKey(DeterministicKey key) {
        this.mKey = key;
    }

    /**
     * Get the address as a String
     * @param param The network param of this address
     */
    public String getAddressString(NetworkParameters param) {
        return mKey.toAddress(param).toString();
    }

    /**
     * Returns the bitcoinj Address representing this address
     * @param param The network parameter of this address
     */
    public Address getAddress(NetworkParameters param) {
        return mKey.toAddress(param);
    }

    /**
     * Gets the list of transaction that this address is input
     */
    public List<GTxIO> getTransactionInput() {
        return mTransactionInput;
    }

    /**
     * Set the transactions that this address is input
     */
    public void setTransactionInput(List<GTxIO> transactionInput) {
        this.mTransactionInput = transactionInput;
    }

    /**
     * Find if this address is input of a transaction
     * @param inputToFind The GTxIO to find
     * @param param The network parameter of this address
     * @return if this address belongs to the transaction
     */
    public boolean hasTransactionInput(GTxIO inputToFind, NetworkParameters param) {
        for (GTxIO input : mTransactionInput) {
            if ((input.getTransaction().getTxid().equals(inputToFind.getTransaction().getTxid()))
                    && (input.getAddress().getAddressString(param).equals(inputToFind.getAddress()
                    .getAddressString(param)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of transaction that this address is output
     */
    public List<GTxIO> getTransactionOutput() {
        return mTransactionOutput;
    }

    /**
     * Find if this address is output of a transaction
     * @param outputToFind The GTxIO to find
     * @param param the network parameter of this address
     * @return if this address belongs to the transaction
     */
    public boolean hasTransactionOutput(GTxIO outputToFind, NetworkParameters param) {
        for (GTxIO output : mTransactionOutput) {
            if ((output.getTransaction().getTxid().equals(outputToFind.getTransaction().getTxid()))
                    && (output.getAddress().getAddressString(param).equals(outputToFind.getAddress()
                    .getAddressString(param)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the list of transaction that this address is output
     */
    public void setTransactionOutput(List<GTxIO> outputTransaction) {
        this.mTransactionOutput = outputTransaction;
    }

    /**
     * Get the amount of uncofirmed balance
     */
    public long getUnconfirmedBalance() {
        long answer = 0;
        for (GTxIO input : mTransactionInput) {
            if (input.getTransaction().getConfirm() < mAccount.getCoin().getConfirmationsNeeded()) {
                answer += input.getAmount();
            }
        }

        for (GTxIO output : mTransactionOutput) {
            if (output.getTransaction().getConfirm() < mAccount.getCoin().getConfirmationsNeeded()) {
                answer -= output.getAmount();
            }
        }

        return answer;
    }

    /**
     * Get the amount of confirmed balance
     */
    public long getConfirmedBalance() {
        long answer = 0;
        for (GTxIO input : mTransactionInput) {
            if (input.getTransaction().getConfirm() >= mAccount.getCoin().getConfirmationsNeeded()) {
                answer += input.getAmount();
            }
        }

        for (GTxIO output : mTransactionOutput) {
            if (output.getTransaction().getConfirm() >= mAccount.getCoin().getConfirmationsNeeded()) {
                answer -= output.getAmount();
            }
        }

        return answer;
    }

    /**
     * Get the date of the last transaction or null if there is no transaction
     */
    public Date getLastDate() {
        Date lastDate = null;
        for (GTxIO input : mTransactionInput) {
            if (lastDate == null || lastDate.before(input.getTransaction().getDate())) {
                lastDate = input.getTransaction().getDate();
            }
        }
        for (GTxIO output : mTransactionOutput) {
            if (lastDate == null || lastDate.before(output.getTransaction().getDate())) {
                lastDate = output.getTransaction().getDate();
            }
        }
        return lastDate;
    }

    /**
     * Get the amount of the less cofnirmed transaction, this is used to set how confirmations are
     * left
     */
    public int getLessConfirmed(){
        int lessConfirm = -1;
        for (GTxIO input : mTransactionInput) {
            if (lessConfirm == -1 || input.getTransaction().getConfirm() < lessConfirm) {
                lessConfirm = input.getTransaction().getConfirm();
            }
        }

        for (GTxIO output : mTransactionOutput) {
            if (lessConfirm == -1 || output.getTransaction().getConfirm() < lessConfirm) {
                lessConfirm = output.getTransaction().getConfirm();
            }
        }
        return lessConfirm;
    }

    /**
     * Gets the unspend transactions input
     * @return The list with the unspend transasctions
     */
    public List<GTxIO> getUTXos(){
        List<GTxIO> utxo = new ArrayList<>();
        for(GTxIO gitx : mTransactionInput){
            boolean find = false;
            for(GTxIO gotx : mTransactionOutput){
                if(gitx.getTransaction().getTxid().equals(gotx.getOriginalTxid())){
                    find = true;
                    break;
                }
            }
            if(!find){
                utxo.add(gitx);
            }
        }
        return utxo;
    }

    /**
     * Fire the onBalanceChange event
     */
    public void BalanceChange() {
        this.getAccount().balanceChange();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralCoinAddress that = (GeneralCoinAddress) o;

        return mIsChange == that.mIsChange && mIndex == that.mIndex && mId == -1
                && (mAccount != null ? mAccount.equals(that.mAccount) : that.mAccount == null
                && (mKey != null ? mKey.equals(that.mKey) : that.mKey == null
                && (mTransactionInput != null ? mTransactionInput.equals(that.mTransactionInput)
                : that.mTransactionInput == null && (mTransactionOutput != null
                ? mTransactionOutput.equals(that.mTransactionOutput)
                : that.mTransactionOutput == null))));

    }

    @Override
    public int hashCode() {
        int result = (int) mId;
        result = 31 * result + (mAccount != null ? mAccount.hashCode() : 0);
        result = 31 * result + (mIsChange ? 1 : 0);
        result = 31 * result + mIndex;
        result = 31 * result + (mKey != null ? mKey.hashCode() : 0);
        result = 31 * result + (mTransactionInput != null ? mTransactionInput.hashCode() : 0);
        result = 31 * result + (mTransactionOutput != null ? mTransactionOutput.hashCode() : 0);
        return result;
    }

    /**
     * Update the transactions of this Address
     * @param transaction The transaction to update
     * @return true if this address has the transaction false otherwise
     */
    public boolean updateTransaction(GeneralTransaction transaction){
        for(GTxIO gitx : mTransactionInput){
            if(gitx.getTransaction().equals(transaction)){
                gitx.getTransaction().setConfirm(transaction.getConfirm());
                gitx.getTransaction().setBlock(transaction.getBlock());
                gitx.getTransaction().setBlockHeight(transaction.getBlockHeight());
                gitx.getTransaction().setDate(transaction.getDate());
                gitx.getTransaction().setMemo(transaction.getMemo());
                return true;
            }
        }

        for(GTxIO gotx : mTransactionOutput){
            if(gotx.getTransaction().equals(transaction)){
                gotx.getTransaction().setConfirm(transaction.getConfirm());
                gotx.getTransaction().setBlock(transaction.getBlock());
                gotx.getTransaction().setBlockHeight(transaction.getBlockHeight());
                gotx.getTransaction().setDate(transaction.getDate());
                gotx.getTransaction().setMemo(transaction.getMemo());
                return true;
            }
        }
        return false;
    }
}

