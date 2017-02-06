package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.HashMap;
import java.util.List;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.Balance;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;

import static de.bitsharesmunich.cryptocoincore.base.Coin.BITCOIN;

/**
 * Created by henry on 05/02/2017.
 */

public class BitcoinAccount extends GeneralCoinAccount {

    private final static int ADDRESS_GAP = 20;
    private DeterministicKey accountKey;
    private DeterministicKey externalKey;
    private DeterministicKey changeKey;
    private HashMap<Integer, BitcoinAddress> externalKeys = new HashMap();
    private HashMap<Integer, BitcoinAddress> changeKeys = new HashMap();

    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    public BitcoinAccount(String id, String name, AccountSeed seed, int accountNumber, int lastExternalIndex, int lastChangeIndex) {
        super(id, name, BITCOIN, seed, accountNumber, lastExternalIndex, lastChangeIndex);
        calculateAddresses();
    }

    public BitcoinAccount(final AccountSeed seed, String name) {
        this(seed, name, false);
    }

    public BitcoinAccount(final AccountSeed seed, String name, boolean importing) {
        super("", name, BITCOIN, seed, 0, 0, 0);
        if (importing) {
            //TODO calculate the number of account

        }
        calculateAddresses();
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

    public void loadAddresses(List<BitcoinAddress> addresses) {
        for (BitcoinAddress address : addresses) {
            if (address.isIsChange()) {
                changeKeys.put(address.getIndex(), address);
            } else {
                externalKeys.put(address.getIndex(), address);
            }
        }
    }

    @Override
    public Balance getBalance() {
        return null;
    }

    public String getNextRecieveAddress() {
        //TODO Check if current address has balance
        if (!externalKeys.containsKey(lastExternalIndex)) {
            externalKeys.put(lastExternalIndex, new BitcoinAddress(HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(lastExternalIndex, false)), lastExternalIndex, false, param));
        }
        return externalKeys.get(lastExternalIndex).getAddressString();
    }

    public void sendCoin(Address to, Coin amount) {

        //Get from address
        //Get Change address to use
        //Put all input in transaction
        //Put all output in transaction
        //sign transaction
    }

    public Address getAddress() {
        return externalKeys.get(lastExternalIndex).getAddress();
    }

    @Override
    public String toString() {
        return "BitcoinAccount{"
                + "name=" + name
                + ", idSeed=" + seed.getId()
                + ", AccountNumber=" + accountNumber
                + ", nextAddress=" + getNextRecieveAddress()
                + ", param=" + param + '}';
    }
}
