package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import de.bitshares_munich.database.SCWallDatabase;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * This represents the account Bitcoin-type, this uses the BIP 44 to generate keys
 */

public abstract class GeneralCoinAccount extends CryptoCoinAccount {
    /**
     * The account number of the BIP-44
     */
    protected int mAccountNumber;
    /**
     * The index of the last used external address
     */
    protected int mLastExternalIndex;
    /**
     * The indes of the last used change address
     */
    protected int mLastChangeIndex;
    /**
     * The account key, this is calculated as a cache
     */
    protected DeterministicKey mAccountKey;
    /**
     * With this key we can calculate the external addresses
     */
    protected DeterministicKey mExternalKey;
    /**
     * With this key we can calculate the change address
     */
    protected DeterministicKey mChangeKey;
    /**
     * The keys for externals addresses
     */
    protected HashMap<Integer, GeneralCoinAddress> mExternalKeys = new HashMap<Integer,GeneralCoinAddress>();
    /**
     * The keys for the change addresses
     */
    protected HashMap<Integer, GeneralCoinAddress> mChangeKeys = new HashMap<Integer,GeneralCoinAddress>();
    /**
     * The listener for the balance change of this account
     */
    protected List<ChangeBalanceListener> mChangeBalanceListeners = new ArrayList<ChangeBalanceListener>();
    /**
     * The list of transaction that involves this account
     */
    protected List<GeneralTransaction> mTransactions = new ArrayList<GeneralTransaction>();

    /**
     * The Limit gap define in the BIP-44
     */
    private final static int sAddressGap = 20;

    /**
     * is the coin number defined by the SLIP-44
     */
    private final int mCoinNumber;

    /**
     * Basic consturctor, calculates each basic key.
     *
     * @param id The id on the database
     * @param name The name of this account, is only for tag
     * @param coin The coin network type
     * @param seed The account seed used to calculate the keys
     * @param coinNumber The coin number
     * @param accountNumber The account number
     * @param lastExternalIndex The last used external address index
     * @param lastChangeIndex The last used change address index
     */
    public GeneralCoinAccount(long id, String name, Coin coin, final AccountSeed seed,
                              int coinNumber, int accountNumber, int lastExternalIndex,
                              int lastChangeIndex) {
        super(id, name, coin, seed);
        this.mCoinNumber = coinNumber;
        this.mAccountNumber = accountNumber;
        this.mLastExternalIndex = lastExternalIndex;
        this.mLastChangeIndex = lastChangeIndex;
        calculateAddresses();
    }

    /**
     * Setter for the transactions of this account, this is used from the database
     */
    public void setTransactions(List<GeneralTransaction> transactions) {
        this.mTransactions = transactions;
    }

    /**
     * Calculates each basic key, not the addresses keys using the BIP-44
     */
    private void calculateAddresses() {
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(this.mSeed.getSeed());
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey,
                new ChildNumber(44, true));
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey,
                new ChildNumber(this.mCoinNumber, true));
        this.mAccountKey = HDKeyDerivation.deriveChildKey(coinKey,
                new ChildNumber(this.mAccountNumber, true));
        this.mExternalKey = HDKeyDerivation.deriveChildKey(this.mAccountKey,
                new ChildNumber(0, false));
        this.mChangeKey = HDKeyDerivation.deriveChildKey(this.mAccountKey,
                new ChildNumber(1, false));
    }

    /**
     * Calculate the external address keys until the index + gap
     */
    public void calculateGapExternal() {
        if (this.mExternalKey == null) {
            calculateAddresses();
        }
        for (int i = 0; i < this.mLastExternalIndex + this.sAddressGap; i++) {
            if (!this.mExternalKeys.containsKey(i)) {
                this.mExternalKeys.put(i, new GeneralCoinAddress(this, false, i,
                        HDKeyDerivation.deriveChildKey(this.mExternalKey,
                                new ChildNumber(i, false))));
            }
        }
    }

    /**
     * Calculate the change address keys until the index + gap
     */
    public void calculateGapChange() {
        if (this.mChangeKey == null) {
            calculateAddresses();
        }
        for (int i = 0; i < this.mLastChangeIndex + this.sAddressGap; i++) {
            if (!this.mChangeKeys.containsKey(i)) {
                this.mChangeKeys.put(i, new GeneralCoinAddress(this, true, i,
                        HDKeyDerivation.deriveChildKey(this.mChangeKey,
                                new ChildNumber(i, false))));
            }
        }
    }

    /**
     * Generates external and change address until the gap. then it saves it on the db for cache
     * @param db The database to sabe the calculated address
     * @return The list of the addresses avaible, this includes the address previously used
     */
    public List<GeneralCoinAddress> getAddresses(SCWallDatabase db) {
        //TODO check for used address
        this.getNextRecieveAddress();
        this.getNextChangeAddress();
        this.calculateGapExternal();
        this.calculateGapChange();

        List<GeneralCoinAddress> addresses = new ArrayList();
        addresses.addAll(this.mChangeKeys.values());
        addresses.addAll(this.mExternalKeys.values());
        this.saveAddresses(db);
        return addresses;
    }

    /**
     * Get the list of all the address, external and change addresses
     * @return a list with all the addresses of this account
     */
    public List<GeneralCoinAddress> getAddresses() {
        List<GeneralCoinAddress> addresses = new ArrayList();
        addresses.addAll(this.mChangeKeys.values());
        addresses.addAll(this.mExternalKeys.values());
        return addresses;
    }

    /**
     * Charges the list of addresse of this account, this is used from the database
     */
    public void loadAddresses(List<GeneralCoinAddress> addresses) {
        for (GeneralCoinAddress address : addresses) {
            if (address.isIsChange()) {
                this.mChangeKeys.put(address.getIndex(), address);
            } else {
                this.mExternalKeys.put(address.getIndex(), address);
            }
        }
    }

    /**
     * Saves the addresses of this account into the database
     */
    public void saveAddresses(SCWallDatabase db) {
        for (GeneralCoinAddress externalAddress : this.mExternalKeys.values()) {
            if (externalAddress.getId() == -1) {
                long id = db.putGeneralCoinAddress(externalAddress);
                if(id != -1)
                externalAddress.setId(id);
            } else {
                db.updateGeneralCoinAddress(externalAddress);
            }
        }

        for (GeneralCoinAddress changeAddress : this.mChangeKeys.values()) {
            if (changeAddress.getId() == -1) {
                Log.i("SCW","change address id " + changeAddress.getId());
                long id = db.putGeneralCoinAddress(changeAddress);
                if(id != -1)
                changeAddress.setId(id);
            } else {
                db.updateGeneralCoinAddress(changeAddress);
            }
        }

        db.updateGeneralCoinAccount(this);
    }

    /**
     * Getter of the account number
     */
    public int getAccountNumber() {
        return this.mAccountNumber;
    }

    /**
     * Getter of the last external address used index
     */
    public int getLastExternalIndex() {
        return this.mLastExternalIndex;
    }

    /**
     * Getter of the last change address used index
     */
    public int getLastChangeIndex() {
        return this.mLastChangeIndex;
    }

    /**
     * Getter of the next receive address
     * @return The next unused recieve address to be used
     */
    public abstract String getNextRecieveAddress();

    /**
     * Getter of the next change address
     * @return The next unused change address to be used
     */
    public abstract String getNextChangeAddress();

    /**
     * Transfer coin amount to another address
     *
     * @param toAddress The destination address
     * @param coin the coin
     * @param amount the amount to send in satoshi
     * @param memo the memo, this can be empty
     * @param context the android context
     */
    public abstract void send(String toAddress, Coin coin, long amount, String memo,
                              Context context);

    /**
     * Transform this account into json object to be saved in the bin file, or any other file
     */
    public JsonObject toJson() {
        JsonObject answer = new JsonObject();
        answer.addProperty("type", this.mCoin.name());
        answer.addProperty("name", this.mName);
        answer.addProperty("accountNumber", this.mAccountNumber);
        answer.addProperty("changeIndex", this.mLastChangeIndex);
        answer.addProperty("externalIndex", this.mLastExternalIndex);
        return answer;
    }

    /**
     * Getter of the list of transactions
     */
    public List<GeneralTransaction> getTransactions() {
        List<GeneralTransaction> transactions = new ArrayList();
        for (GeneralCoinAddress address : this.mExternalKeys.values()) {
            for (GTxIO giotx : address.getTransactionInput()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            for (GTxIO giotx : address.getTransactionOutput()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
        }

        for (GeneralCoinAddress address : this.mChangeKeys.values()) {
            for (GTxIO giotx : address.getTransactionInput()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            for (GTxIO giotx : address.getTransactionOutput()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            ;
        }

        Collections.sort(transactions, new TransactionsCustomComparator());
        return transactions;
    }

    /**
     * Get the address as string of an adrees index
     * @param index The index of the address
     * @param change if it is change addres or is a external address
     * @return The Address as string
     */
    public abstract String getAddressString(int index, boolean change);

    /**
     * Get the GeneralCoinAddress object of an address
     * @param index the index of the address
     * @param change if it is change addres or is a external address
     * @return The GeneralCoinAddress of the address
     */
    public abstract GeneralCoinAddress getAddress(int index, boolean change);

    /**
     * Return the network parameters, this is used for the bitcoiinj library
     */
    public abstract NetworkParameters getNetworkParam();

    /**
     * Triggers the event onBalanceChange
     */
    public void balanceChange() {
        this._fireOnChangeBalance(this.getBalance().get(0)); //TODO make it more genertic
    }

    /**
     * Compare the transaction, to order it for the list of transaction
     */
    public class TransactionsCustomComparator implements Comparator<GeneralTransaction> {
        @Override
        public int compare(GeneralTransaction o1, GeneralTransaction o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    /**
     * Add listener for the onChangebalance Event
     */
    public void addChangeBalanceListener(ChangeBalanceListener listener) {
        this.mChangeBalanceListeners.add(listener);
    }

    /**
     * Fire the onChangeBalance event
     */
    protected void _fireOnChangeBalance(Balance balance) {
        for (ChangeBalanceListener listener : this.mChangeBalanceListeners) {
            listener.balanceChange(balance);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralCoinAccount that = (GeneralCoinAccount) o;

        if (this.mCoin != that.mCoin) return false;
        if (this.mAccountNumber != that.mAccountNumber) return false;
        return this.mAccountKey != null ? this.mAccountKey.equals(that.mAccountKey)
                : that.mAccountKey == null;

    }

    @Override
    public int hashCode() {
        int result = this.mAccountNumber;
        result = 31 * result + (this.mAccountKey != null ? this.mAccountKey.hashCode() : 0);
        return result;
    }

    /**
     * Updates a transaction
     *
     * @param transaction The transaction to update
     */
    public void updateTransaction(GeneralTransaction transaction){
        // Checks if it has an external address
        for (GeneralCoinAddress address : this.mExternalKeys.values()) {
            if(address.updateTransaction(transaction)){
                return;
            }
        }

        for (GeneralCoinAddress address : this.mChangeKeys.values()) {
            if(address.updateTransaction(transaction)){
                return;
            }
        }
    }
}
