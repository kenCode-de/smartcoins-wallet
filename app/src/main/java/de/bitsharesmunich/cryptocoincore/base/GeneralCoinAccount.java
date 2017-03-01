package de.bitsharesmunich.cryptocoincore.base;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.database.SCWallDatabaseContract;

/**
 * Created by henry on 05/02/2017.
 */

public abstract class GeneralCoinAccount extends CryptoCoinAccount {
    protected int accountNumber;
    protected int lastExternalIndex;
    protected int lastChangeIndex;
    protected DeterministicKey accountKey;
    protected DeterministicKey externalKey;
    protected DeterministicKey changeKey;
    protected HashMap<Integer, GeneralCoinAddress> externalKeys = new HashMap();
    protected HashMap<Integer, GeneralCoinAddress> changeKeys = new HashMap();
    protected List<ChangeBalanceListener> changeBalanceListeners = new ArrayList();

    protected List<GeneralTransaction> transactions = new ArrayList();

    private final static int ADDRESS_GAP = 20;

    public GeneralCoinAccount(long id, String name, Coin coin, final AccountSeed seed, int accountNumber, int lastExternalIndex, int lastChangeIndex) {
        super(id, name, coin, seed);
        this.accountNumber = accountNumber;
        this.lastExternalIndex = lastExternalIndex;
        this.lastChangeIndex = lastChangeIndex;
        calculateAddresses();
    }

    public void setTransactions(List<GeneralTransaction> transactions) {
        this.transactions = transactions;
    }

    private void calculateAddresses() {
        //BIP44
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeed());
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey, new ChildNumber(44, true));
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey, new ChildNumber(0, true));
        accountKey = HDKeyDerivation.deriveChildKey(coinKey, new ChildNumber(accountNumber, true));
        externalKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(0, false));
        changeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(1, false));
    }

    public void calculateGapExternal() {
        if (externalKey == null) {
            calculateAddresses();
        }
        for (int i = 0; i < lastExternalIndex + ADDRESS_GAP; i++) {
            if (!externalKeys.containsKey(i)) {
                externalKeys.put(i, new GeneralCoinAddress(this, false, i, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(i, false))));
            }
        }
    }

    public void calculateGapChange() {
        if (changeKey == null) {
            calculateAddresses();
        }
        for (int i = 0; i < lastChangeIndex + ADDRESS_GAP; i++) {
            if (!changeKeys.containsKey(i)) {
                changeKeys.put(i, new GeneralCoinAddress(this, true, i, HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(i, false))));
            }
        }
    }

    public List<GeneralCoinAddress> getAddresses(SCWallDatabase db) {
        this.getNextRecieveAddress();
        this.getNextChangeAddress();
        calculateGapExternal();
        calculateGapChange();

        List<GeneralCoinAddress> addresses = new ArrayList();
        addresses.addAll(changeKeys.values());
        addresses.addAll(externalKeys.values());
        this.saveAddresses(db);
        return addresses;
    }

    public List<GeneralCoinAddress> getAddresses() {
        List<GeneralCoinAddress> addresses = new ArrayList();
        addresses.addAll(changeKeys.values());
        addresses.addAll(externalKeys.values());
        return addresses;
    }

    public void loadAddresses(List<GeneralCoinAddress> addresses) {
        for (GeneralCoinAddress address : addresses) {
            if (address.isIsChange()) {
                changeKeys.put(address.getIndex(), address);
            } else {
                externalKeys.put(address.getIndex(), address);
            }
        }
    }

    public void saveAddresses(SCWallDatabase db) {
        for (GeneralCoinAddress externalAddress : externalKeys.values()) {
            if (externalAddress.getId() == -1) {
                long id = db.putGeneralCoinAddress(externalAddress);
                if(id != -1)
                externalAddress.setId(id);
            } else {
                db.updateGeneralCoinAddress(externalAddress);
            }
        }

        for (GeneralCoinAddress changeAddress : changeKeys.values()) {
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

    public int getAccountNumber() {
        return accountNumber;
    }

    public int getLastExternalIndex() {
        return lastExternalIndex;
    }

    public int getLastChangeIndex() {
        return lastChangeIndex;
    }

    public abstract String getNextRecieveAddress();

    public abstract String getNextChangeAddress();

    public abstract void send(String toAddress, Coin coin, long amount, String memo, Context context);

    public JsonObject toJson() {
        JsonObject answer = new JsonObject();
        answer.addProperty("type", this.coin.name());
        answer.addProperty("name", this.name);
        answer.addProperty("accountNumber", this.accountNumber);
        answer.addProperty("changeIndex", this.lastChangeIndex);
        answer.addProperty("externalIndex", this.lastExternalIndex);
        return answer;
    }

    public List<GeneralTransaction> getTransactions() {
        List<GeneralTransaction> transactions = new ArrayList();
        for (GeneralCoinAddress address : externalKeys.values()) {
            for (GIOTx giotx : address.getInputTransaction()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            for (GIOTx giotx : address.getOutputTransaction()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
        }

        for (GeneralCoinAddress address : changeKeys.values()) {
            for (GIOTx giotx : address.getInputTransaction()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            for (GIOTx giotx : address.getOutputTransaction()) {
                if (!transactions.contains(giotx.getTransaction())) {
                    transactions.add(giotx.getTransaction());
                }
            }
            ;
        }

        Collections.sort(transactions, new TransactionsCustomComparator());

        return transactions;
    }

    public abstract String getAddressString(int index, boolean change);

    public abstract GeneralCoinAddress getAddress(int index, boolean change);

    public abstract NetworkParameters getNetworkParam();

    public void balanceChange() {
        this._fireOnChangeBalance(this.getBalance().get(0)); //TODO make it more genertic
    }

    public class TransactionsCustomComparator implements Comparator<GeneralTransaction> {
        @Override
        public int compare(GeneralTransaction o1, GeneralTransaction o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    public void addChangeBalanceListener(ChangeBalanceListener listener) {
        this.changeBalanceListeners.add(listener);
    }

    protected void _fireOnChangeBalance(Balance balance) {
        for (ChangeBalanceListener listener : this.changeBalanceListeners) {
            listener.balanceChange(balance);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralCoinAccount that = (GeneralCoinAccount) o;

        if (coin != that.coin) return false;
        if (accountNumber != that.accountNumber) return false;
        return accountKey != null ? accountKey.equals(that.accountKey) : that.accountKey == null;

    }

    @Override
    public int hashCode() {
        int result = accountNumber;
        result = 31 * result + (accountKey != null ? accountKey.hashCode() : 0);
        return result;
    }

    public void updateTransaction(GeneralTransaction transaction){
        for (GeneralCoinAddress address : externalKeys.values()) {
            if(address.updateTransaction(transaction)){
                return;
            }
        }

        for (GeneralCoinAddress address : changeKeys.values()) {
            if(address.updateTransaction(transaction)){
                return;
            }
        }
    }
}
