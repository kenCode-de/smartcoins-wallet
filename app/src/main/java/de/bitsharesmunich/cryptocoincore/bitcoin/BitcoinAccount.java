package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.Balance;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;

import static de.bitsharesmunich.cryptocoincore.base.Coin.BITCOIN;

/**
 * Created by henry on 05/02/2017.
 */

public class BitcoinAccount extends GeneralCoinAccount {

    private final static int ADDRESS_GAP = 20;


    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    public BitcoinAccount(String id, String name, AccountSeed seed, int accountNumber, int lastExternalIndex, int lastChangeIndex) {
        super(id, name, BITCOIN, seed, accountNumber, lastExternalIndex, lastChangeIndex);

    }

    public BitcoinAccount(final AccountSeed seed, String name) {
        this(seed, name, false);
    }

    public BitcoinAccount(final AccountSeed seed, String name, boolean importing) {
        super("", name, BITCOIN, seed, 0, 0, 0);
        if (importing) {
            //TODO calculate the number of account
        }
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

    @Override
    public List<Balance> getBalance() {
        long amount = 0;
        for (GeneralCoinAddress key : externalKeys.values()) {
            amount += key.getBalance();
        }

        for (GeneralCoinAddress key : changeKeys.values()) {
            amount += key.getBalance();
        }

        Balance balance = new Balance();
        balance.setType(BITCOIN);
        balance.setDate(new Date());
        balance.setAmmount(amount);
        List<Balance> balances = new ArrayList();
        balances.add(balance);
        return balances;
    }

    public String getNextRecieveAddress() {
        if (!externalKeys.containsKey(lastExternalIndex)) {
            externalKeys.put(lastExternalIndex, new GeneralCoinAddress(this, false, lastExternalIndex, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(lastExternalIndex, false))));
        }

        //Finding the next unused address
        while(externalKeys.get(lastExternalIndex).getInputTransaction().size()>0){
            ++lastExternalIndex;
            if (!externalKeys.containsKey(lastExternalIndex)) {
                externalKeys.put(lastExternalIndex, new GeneralCoinAddress(this, false, lastExternalIndex, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(lastExternalIndex, false))));
            }
        }
        return externalKeys.get(lastExternalIndex).getAddressString(param);
    }

    public void sendCoin(Address to, Coin amount) {

        //Get from address
        //Get Change address to use
        //Put all input in transaction
        //Put all output in transaction
        //sign transaction
    }

    public Address getAddress() {
        return externalKeys.get(lastExternalIndex).getAddress(param);
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
